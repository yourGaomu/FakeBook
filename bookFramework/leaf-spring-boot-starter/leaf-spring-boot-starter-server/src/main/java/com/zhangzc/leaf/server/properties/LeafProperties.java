package com.zhangzc.leaf.server.properties;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiaojie
 * @version 1.0
 * @description: 配置类
 * @date 2022/8/26 0:18
 */
@Data
@ConfigurationProperties(prefix = "zhangzc.leaf")
public class LeafProperties {

    //是否开启分段，默认false
    public Boolean segmentEnable = false;
    //数据库连接地址
    public  String jdbcUrl;
    //数据库用户名称
    public String jdbcUsername;
    //密码
    public String jdbcPassword;
    //是否开启雪花算法 默认false
    public Boolean snowflakeEnable = false;
    //zk端口 默认2181
    public Integer snowflakePort = 2181;
    //zk连接地址
    public String snowflakeZkAddress = "127.0.0.1";



    @PostConstruct()
    public void init() {
        System.out.println("==> leaf 配置类初始化: " + this);
    }
}
