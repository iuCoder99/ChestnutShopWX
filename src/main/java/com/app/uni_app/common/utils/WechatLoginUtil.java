package com.app.uni_app.common.utils;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.app.uni_app.properties.WeChatProperties;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class WechatLoginUtil {
    @Resource
    private WeChatProperties weChatProperties;
    /**
     * 用code换openid和session_key
     * @param code 前端传的临时code
     * @return 微信返回的结果（包含openid）
     */
    public  JSONObject getWechatUserInfo(String code) {
        // 1. 拼接请求地址
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                weChatProperties.getAppid(), weChatProperties.getSecret(), code
        );
        // 2. 调用微信接口（Hutool的HttpUtil简化GET请求）
        String result = HttpUtil.get(url);
        // 3. 转成JSON对象返回
        JSONObject jsonObject = JSONUtil.parseObj(result);
        // 4. 检查错误码
        if (jsonObject.containsKey("errcode") && jsonObject.getInt("errcode") != 0) {
            throw new RuntimeException("微信登录失败：" + jsonObject.getStr("errmsg"));
        }
        return jsonObject;
    }
}
