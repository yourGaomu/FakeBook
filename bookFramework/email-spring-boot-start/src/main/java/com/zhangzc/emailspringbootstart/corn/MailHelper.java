package com.zhangzc.emailspringbootstart.corn;

import com.zhangzc.emailspringbootstart.vo.MailVo;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.concurrent.Executor;

@Slf4j
@RequiredArgsConstructor
public class MailHelper {

    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;
    private final Executor taskExecutor;

    public void sendHtmlAsync(String to, String title, String html) {
        taskExecutor.execute(() -> {
            boolean b = doSend(to, title, html, true);
            if (!b) {
                log.error("异步邮件发送失败, to: {}", to);
            }
        });
    }

    public void send(MailVo mailVo) {
        boolean isHtml = Boolean.TRUE.equals(mailVo.getIsHtml());
        boolean b = doSend(mailVo.getTo(), mailVo.getTitle(), mailVo.getContent(), isHtml);
        if (!b) {
            throw new RuntimeException("邮件发送失败");
        }
    }

    /**
     * 兼容旧方法名
     */
    public void sendCode(MailVo mailVo) {
        send(mailVo);
    }

    private boolean doSend(String to, String title, String content, boolean isHtml) {
        log.info("==> 开始发送邮件 to: {}, title: {}, isHtml: {}", to, title, isHtml);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            // 邮件发送来源
            mimeMessageHelper.setFrom(mailProperties.getUsername());
            // 邮件发送目标
            mimeMessageHelper.setTo(to);
            // 设置标题
            mimeMessageHelper.setSubject(title);
            // 设置内容
            mimeMessageHelper.setText(content, isHtml);

            javaMailSender.send(mimeMessage);
            log.info("==> 邮件发送成功, to: {}, title: {}", to, title);
            return true;
        } catch (Exception e) {
            log.error("==> 发送邮件异常: ", e);
            return false;
        }
    }
}
