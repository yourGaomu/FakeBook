package com.zhangzc.miniospringbootstart.utills;


import com.zhangzc.miniospringbootstart.config.MinioProperties;
import com.zhangzc.miniospringbootstart.domain.dto.VideoResultDto;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.bytedeco.javacv.Frame;

import javax.imageio.ImageIO;




import java.awt.*;
import java.io.*;


@Slf4j
@RequiredArgsConstructor
@Component
public class MinioUtil {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    /**
     * 获取视频缩略图
     *
     * @param videoUrl：视频路径
     * @throws Exception
     */
    public VideoResultDto getCoverUrlByVideoUrl(String videoUrl) throws Exception {
            VideoResultDto videoResultDto = new VideoResultDto();
            FFmpegFrameGrabber ff = FFmpegFrameGrabber.createDefault(videoUrl);
            ff.start();
            //判断是否是竖屏小视频
            String rotate = ff.getVideoMetadata("rotate");
            int ffLength = ff.getLengthInFrames();
            Frame f;
            int i = 0;
            int index = 1;//截取图片第几帧
            while (i < ffLength) {
                f = ff.grabImage();
                if (i == index) {
                    if (null != rotate && rotate.length() > 1) {
                        videoResultDto = doExecuteFrame(f, true);   //获取缩略图
                    } else {
                        videoResultDto = doExecuteFrame(f, false);   //获取缩略图
                    }
                    break;
                }
                i++;
            }
            ff.stop();
            return videoResultDto;  //返回的是视频第N帧
    }

    /**
     * 截取缩略图，存入阿里云OSS（按自己的上传类型自定义转换文件格式）
     *
     * @param f
     * @return
     * @throws Exception
     */
    public VideoResultDto doExecuteFrame(Frame f, boolean bool) throws Exception {
        if (null == f || null == f.image) {
            return null;
        }
        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage bi = converter.getBufferedImage(f);
        if (bool) {
            Image image = (Image) bi;
            bi = rotate(image, 90);//图片旋转90度
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", os);
        InputStream input = new ByteArrayInputStream(os.toByteArray());
        String url = uploadFile(input, "image.png", "image/png", os.size());
        return new VideoResultDto().setCoverUrl(url).setWidth(bi.getWidth()).setHeight(bi.getHeight());
    }

    /**
     * 图片旋转角度
     *
     * @param src   源图片
     * @param angel 角度
     * @return 目标图片
     */
    private BufferedImage rotate(Image src, int angel) {
        int src_width = src.getWidth(null);
        int src_height = src.getHeight(null);
        // calculate the new image size
        Rectangle rect_des = CalcRotatedSize(new Rectangle(new Dimension(
                src_width, src_height)), angel);

        BufferedImage res = null;
        res = new BufferedImage(rect_des.width, rect_des.height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = res.createGraphics();
        // transform(这里先平移、再旋转比较方便处理；绘图时会采用这些变化，绘图默认从画布的左上顶点开始绘画，源图片的左上顶点与画布左上顶点对齐，然后开始绘画，修改坐标原点后，绘画对应的画布起始点改变，起到平移的效果；然后旋转图片即可)

        //平移（原理修改坐标系原点，绘图起点变了，起到了平移的效果，如果作用于旋转，则为旋转中心点）
        g2.translate((rect_des.width - src_width) / 2, (rect_des.height - src_height) / 2);


        //旋转（原理transalte(dx,dy)->rotate(radians)->transalte(-dx,-dy);修改坐标系原点后，旋转90度，然后再还原坐标系原点为(0,0),但是整个坐标系已经旋转了相应的度数 ）
        g2.rotate(Math.toRadians(angel), src_width / 2, src_height / 2);

//        //先旋转（以目标区域中心点为旋转中心点，源图片左上顶点对准目标区域中心点，然后旋转）
//        g2.translate(rect_des.width/2,rect_des.height/ 2);
//        g2.rotate(Math.toRadians(angel));
//        //再平移（原点恢复到源图的左上顶点处（现在的右上顶点处），否则只能画出1/4）
//        g2.translate(-src_width/2,-src_height/2);


        g2.drawImage(src, null, null);
        return res;
    }

    /**
     * 计算转换后目标矩形的宽高
     *
     * @param src   源矩形
     * @param angel 角度
     * @return 目标矩形
     */
    private Rectangle CalcRotatedSize(Rectangle src, int angel) {
        double cos = Math.abs(Math.cos(Math.toRadians(angel)));
        double sin = Math.abs(Math.sin(Math.toRadians(angel)));
        int des_width = (int) (src.width * cos) + (int) (src.height * sin);
        int des_height = (int) (src.height * cos) + (int) (src.width * sin);
        return new java.awt.Rectangle(new Dimension(des_width, des_height));

    }

    public String uploadFile(MultipartFile file) throws Exception {
        // 判断文件是否为空
        if (file == null || file.getSize() == 0) {
            log.error("==> 上传文件异常：文件大小为空 ...");
            throw new RuntimeException("文件大小不能为空");
        }
        return uploadFile(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), file.getSize());
    }

    private String uploadFile(InputStream inputStream, String originalFileName, String contentType, long size) throws Exception {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 时间戳格式（用于文件名，移除非法字符）
        DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = now.format(timestampFormatter);

        // 日期文件夹格式（用于组织文件）
        DateTimeFormatter dateFolderFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateFolder = now.format(dateFolderFormatter);

        // 生成无横线的UUID
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 安全获取文件后缀
        String suffix = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // 构建完整对象名：日期文件夹/时间戳_UUID.后缀
        String objectName = dateFolder + "/" + timestamp + "_" + uuid + suffix;

        log.info("==> 开始上传文件至 Minio, ObjectName: {}", objectName);

        // 上传文件至 Minio
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            log.error("==> 上传文件至 Minio 失败: {}", e.getMessage());
            throw new RuntimeException("上传文件至 Minio 失败");
        }

        // 返回文件的访问链接
        String url = String.format("%s/%s/%s", minioProperties.getEndpoint(), minioProperties.getBucketName(), objectName);
        log.info("==> 上传文件至 Minio 成功，访问路径: {}", url);
        return url;
    }

    /**
     * 上传文件至临时存储桶 (Temporarystorage)
     * 路径格式：/projectPrefix/日期/时间戳_UUID.后缀
     *
     * @param file          上传的文件
     * @param projectPrefix 项目目录前缀 (例如: shareThing)
     * @return 文件的访问 URL
     * @throws Exception
     */
    public String uploadTemporaryFile(MultipartFile file, String projectPrefix) throws Exception {
        // 判断文件是否为空
        if (file == null || file.getSize() == 0) {
            log.error("==> 上传文件异常：文件大小为空 ...");
            throw new RuntimeException("文件大小不能为空");
        }

        InputStream inputStream = file.getInputStream();
        String originalFileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        long size = file.getSize();

        // 临时存储桶名称
        String bucketName = "temporarystorage";

        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 时间戳格式（用于文件名，移除非法字符）
        DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = now.format(timestampFormatter);

        // 日期文件夹格式（用于组织文件）
        DateTimeFormatter dateFolderFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateFolder = now.format(dateFolderFormatter);

        // 生成无横线的UUID
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 安全获取文件后缀
        String suffix = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // 构建完整对象名：projectPrefix/日期文件夹/时间戳_UUID.后缀
        // 例如：shareThing/2023-10-27/20231027_101010_uuid.jpg
        String objectName = projectPrefix + "/" + dateFolder + "/" + timestamp + "_" + uuid + suffix;

        log.info("==> 开始上传文件至 Minio 临时桶: {}, ObjectName: {}", bucketName, objectName);

        // 上传文件至 Minio
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            log.error("==> 上传文件至 Minio 临时桶失败: {}", e.getMessage());
            throw new RuntimeException("上传文件至 Minio 临时桶失败: " + e.getMessage());
        }

        // 返回文件的访问链接
        String url = String.format("%s/%s/%s", minioProperties.getEndpoint(), bucketName, objectName);
        log.info("==> 上传文件至 Minio 临时桶成功，访问路径: {}", url);
        return url;
    }

}
