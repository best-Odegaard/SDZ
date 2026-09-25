package com.saidazi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信配置
 */
@Configuration
@ConfigurationProperties(prefix = "wx")
public class WxConfig {

    private String appId;
    private String appSecret;
    private String subscribeTemplateId;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }

    public String getSubscribeTemplateId() { return subscribeTemplateId; }
    public void setSubscribeTemplateId(String subscribeTemplateId) { this.subscribeTemplateId = subscribeTemplateId; }
}
