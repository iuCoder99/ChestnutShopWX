package com.app.uni_app.service;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.NoticeDTO;
import com.app.uni_app.pojo.entity.Notice;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author 20589
 * @description 针对表【notice(系统公告表)】的数据库操作Service
 * @createDate 2025-12-28 10:16:11
 */
public interface NoticeService extends IService<Notice> {

    Result getLatestNotice(Integer limit);

    Result addNotice(NoticeDTO noticeDTO);

    Result updateNotice(NoticeDTO noticeDTO);


    Result deleteNotice(String id);
}

