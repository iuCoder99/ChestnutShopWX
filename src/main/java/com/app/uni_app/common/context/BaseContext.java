package com.app.uni_app.common.context;

import com.app.uni_app.common.result.UserInfo;
import com.app.uni_app.pojo.entity.User;

public class BaseContext {
    public static ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    public static void setUserInfo(UserInfo userInfo) {
        threadLocal.set(userInfo);
    }

    public static UserInfo getUserInfo(){
        return threadLocal.get();
    }

    public static void removeUserInfo() {
        threadLocal.remove();
    }
}
