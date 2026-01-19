package com.app.uni_app.common.utils;

import com.app.uni_app.common.constant.DataConstant;
import com.app.uni_app.common.constant.SessionConstant;
import com.app.uni_app.mapper.ProductMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;


@Component
public class SessionUtil {
    @Resource
    private HttpSession session;

    @Resource
    private ProductMapper productMapper;

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

    public Long getMaxIdInData() {
        return productMapper.getMaxProductIdInData();
    }

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

    public void setLoadedIdSet(HashSet<Long> loadedIdSet) {
        session.setAttribute(SessionConstant.SCROLL_LOADED_ID_SET, loadedIdSet);
    }

    public void setScrollLoadedEndId(Long scrollLoadedEndId) {
        session.setAttribute(SessionConstant.SCROLL_LAST_LOADED_END_ID, scrollLoadedEndId);
    }

    public void removeLoadedIdSet() {
        session.removeAttribute(SessionConstant.SCROLL_LOADED_ID_SET);
    }

    public void removeScrollLoadedEndId() {
        session.removeAttribute(SessionConstant.SCROLL_LAST_LOADED_END_ID);
    }


}
