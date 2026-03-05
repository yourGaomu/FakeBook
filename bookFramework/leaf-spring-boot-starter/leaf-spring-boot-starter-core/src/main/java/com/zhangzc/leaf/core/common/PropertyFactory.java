package com.zhangzc.leaf.core.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class PropertyFactory {
    private static final Logger logger = LoggerFactory.getLogger(PropertyFactory.class);
    private static final Properties prop = new Properties();
    static {
        try {
            var in = PropertyFactory.class.getClassLoader().getResourceAsStream("leaf.properties");
            if (in != null) {
                prop.load(in);
            } else {
                logger.warn("leaf.properties not found in classpath, using default 'leaf' name");
                prop.setProperty("leaf.name", "leaf");
            }
        } catch (IOException e) {
            logger.warn("Load Properties Ex", e);
        }
    }
    public static Properties getProperties() {
        return prop;
    }
}
