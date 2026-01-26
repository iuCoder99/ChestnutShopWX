package com.app.uni_app.common.util;

import com.app.uni_app.common.constant.DataConstant;
import com.app.uni_app.common.constant.SessionConstant;
import com.app.uni_app.pojo.entity.Order;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;


@Component
public class SessionUtils {
    @Resource
    private HttpSession session;

    @Resource
    private CaffeineUtils caffeineUtils;

    // 商品模块

    /**
     * 获取已经查询过的 id set
     */
    @SuppressWarnings("unchecked")
    public HashSet<Long> getLoadedIdSet() {
        Object loadedSet = session.getAttribute(SessionConstant.SCROLL_LOADED_ID_SET);
        if (Objects.isNull(loadedSet)) {
            return new HashSet<>();
        }
        if (loadedSet instanceof HashSet) {
            return (HashSet<Long>) loadedSet;
        }
        throw new ClassCastException(SessionConstant.ID_SET_STRUCTURE_TRANSFORM_ERROR);
    }


    /**
     * 获取滚动加载后的结尾 id
     */
    public Long getScrollLoadedEndId() {
        Object scrollLoadedEndId = session.getAttribute(SessionConstant.SCROLL_LAST_LOADED_END_ID);
        if (Objects.isNull(scrollLoadedEndId)) {
            return DataConstant.ZERO_LONG;
        }
        if (scrollLoadedEndId instanceof Long) {
            return (Long) scrollLoadedEndId;
        }
        throw new ClassCastException(SessionConstant.LOADED_END_ID__STRUCTURE_TRANSFORM_ERROR);
    }

    /**
     * 存储已经加载的 id set
     */
    public void setLoadedIdSet(HashSet<Long> loadedIdSet) {
        session.setAttribute(SessionConstant.SCROLL_LOADED_ID_SET, loadedIdSet);
    }

    /**
     *存储加载后的结尾 id
     */
    public void setScrollLoadedEndId(Long scrollLoadedEndId) {
        session.setAttribute(SessionConstant.SCROLL_LAST_LOADED_END_ID, scrollLoadedEndId);
    }

    /**
     * 删除已经加载的 id set
     */
    public void removeLoadedIdSet() {
        session.removeAttribute(SessionConstant.SCROLL_LOADED_ID_SET);
    }

    /**
     * 删除加载后的结尾 id
     */
    public void removeScrollLoadedEndId() {
        session.removeAttribute(SessionConstant.SCROLL_LAST_LOADED_END_ID);
    }

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
