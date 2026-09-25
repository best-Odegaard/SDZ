package com.saidazi.util;

import com.saidazi.config.WxConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 微信消息推送工具类
 * 调用微信订阅消息接口发送模板消息
 */
@Slf4j
@Component
public class WxMessageUtil {

    @Autowired
    private WxConfig wxConfig;

    /**
     * 发送报名提醒订阅消息
     *
     * @param openId       用户 OpenID
     * @param title        赛事名称
     * @param remindTime   提醒触发时间
     * @param reminderType 提醒类型描述
     */
    public boolean sendSubscribeMessage(String openId, String title, String remindTime, String reminderType) {
        try {
            // 1. 获取 access_token
            String accessToken = getAccessToken();

            // 2. 构建消息体
            String body = buildMessageBody(openId, title, remindTime, reminderType);

            // 3. 调用微信接口
            // POST https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=ACCESS_TOKEN
            // （实际发送需引入 HttpClient 调用，此处打印日志模拟）
            log.info("发送微信模板消息 -> toUser: {}, title: {}, remindType: {}, token: {}",
                    openId, title, reminderType, accessToken);

            // 模拟发送成功
            return true;

        } catch (Exception e) {
            log.error("发送微信订阅消息失败: openId={}, title={}", openId, title, e);
            return false;
        }
    }

    /**
     * 获取微信 access_token
     */
    private String getAccessToken() {
        // 实际实现应调用微信 API：
        // GET https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=SECRET
        // 生产环境建议缓存 token（2小时有效期）
        return "simulated_access_token";
    }

    /**
     * 构建订阅消息 JSON body
     */
    private String buildMessageBody(String openId, String title, String remindTime, String reminderType) {
        return String.format("""
                {
                    "touser": "%s",
                    "template_id": "%s",
                    "data": {
                        "thing1": {"value": "%s"},
                        "time2": {"value": "%s"},
                        "thing4": {"value": "%s"}
                    }
                }
                """, openId, wxConfig.getSubscribeTemplateId(), title, remindTime, reminderType);
    }
}
