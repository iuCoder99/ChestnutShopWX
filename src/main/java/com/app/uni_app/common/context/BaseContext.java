package com.app.uni_app.common.context;

import com.app.uni_app.common.exception.UnauthenticatedException;
import com.app.uni_app.common.result.UserInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class BaseContext {
    public static ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    public static void setUserInfo(UserInfo userInfo) {
        threadLocal.set(userInfo);
    }

    public static UserInfo getUserInfo() {
        return threadLocal.get();
    }

    public static void removeUserInfo() {
        threadLocal.remove();
    }

    public static String getUserId() {
        UserInfo userInfo = threadLocal.get();
        if (Objects.isNull(userInfo)) {
            throw new UnauthenticatedException();
        }
        String userId = userInfo.getId();
        if (StringUtils.isBlank(userId)) {
            throw new UnauthenticatedException();
        }
        return userId;
    }
}
