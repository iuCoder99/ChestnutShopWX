package com.app.uni_app.common.util;

import com.app.uni_app.common.constant.SessionConstant;
import com.app.uni_app.pojo.entity.Order;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;


@Component
public class SessionUtils {

    @Resource
    private HttpSession session;

    //订单模块

    /**
     *获取当前用户所有 order
     */
    @SuppressWarnings("unchecked")
    public  List<Order> getUserAllOrder( Function<String, List<Order>> function,String userId) {
        Object attribute = session.getAttribute(SessionConstant.USER_ALL_ORDER);
        if (Objects.isNull(attribute)) {
            List<Order> orderList = function.apply(userId);
            session.setAttribute(SessionConstant.USER_ALL_ORDER, orderList);
            return orderList;
        }
        return (List<Order>) attribute;
    }

    /**
     * 存储当前用户所有 order
     */
    public void setUserAllOrder(List<Order> orderList) {
        session.setAttribute(SessionConstant.USER_ALL_ORDER, orderList);
    }

    /**
     * 删除当前用户所有 order
     */
    public void removeUserAllOrder() {
        session.removeAttribute(SessionConstant.USER_ALL_ORDER);
    }
}
