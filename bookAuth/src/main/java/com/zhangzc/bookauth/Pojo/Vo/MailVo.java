package com.zhangzc.bookauth.Pojo.Vo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data               // 生成getter、setter、toString等
@Builder            // 提供建造者模式
@NoArgsConstructor  // 生成无参构造函数（Jackson反序列化必需）
@AllArgsConstructor // 生成全参构造函数（配合@Builder使用）
public class MailVo implements Serializable {
    private String to;      // 移除final修饰符，允许Jackson赋值
    private String title;
    private String content;
}
