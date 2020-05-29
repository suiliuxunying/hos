package com.imooc.bigdata.hos.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Created by jixin on 18-3-8.
 */
public class HosConfiguration {

  private static HosConfiguration configuration;
  private static Properties properties;

  static {

    PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
    configuration = new HosConfiguration();
    try {
      configuration.properties = new Properties();
      Resource[] resources = resourceLoader.getResources("classpath:*.properties");//获取到两个配置文件 (web resources里的 zook 和 log4j)
      for (Resource resource : resources) {
        Properties props = new Properties();
        InputStream input = resource.getInputStream();
        props.load(input);//props 存着各个配置项
        input.close();
        configuration.properties.putAll(props);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private HosConfiguration() {
  }

  public static HosConfiguration getConfiguration() {
    return configuration;
  }


  public String getString(String key) {
    return properties.getProperty(key);
  }

  public int getInt(String key) {
    return Integer.parseInt(properties.getProperty(key));
  }

  public boolean getBoolean(String key) {
    return Boolean.parseBoolean(properties.getProperty(key));
  }

  public long getLong(String key) {
    return Long.parseLong(properties.getProperty(key));
  }

}
