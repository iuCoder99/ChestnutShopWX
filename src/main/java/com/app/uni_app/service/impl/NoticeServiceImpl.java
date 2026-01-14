package com.app.uni_app.service.impl;

import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.mapper.NoticeMapper;
import com.app.uni_app.pojo.dto.NoticeDTO;
import com.app.uni_app.pojo.entity.Notice;
import com.app.uni_app.service.NoticeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 20589
 * @description 针对表【notice(系统公告表)】的数据库操作Service实现
 * @createDate 2025-12-28 10:16:11
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice>
        implements NoticeService {
    @Resource
    private CopyMapper copyMapper;
    /**
     * 获取最新 notice
     *
     * @return
     */
    @Override
    public Result getLatestNotice(Integer limit) {
        List<Notice> list = lambdaQuery().eq(Notice::getStatus, 1)
                .orderByDesc(Notice::getUpdateTime)
                .page(new Page<>(1, limit)).getRecords();
        return Result.success(list);
    }

    /**
     * 添加通知
     *
     * @param noticeDTO
     * @return
     */
    @Override
    public Result addNotice(NoticeDTO noticeDTO) {
        Notice notice = copyMapper.noticeDTOToNotice(noticeDTO);
        boolean isSuccess = save(notice);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        return Result.success(notice);
    }


    /**
     * 更新通知
     *
     * @param noticeDTO
     * @return
     */
    @Override
    public Result updateNotice(NoticeDTO noticeDTO) {
        if (noticeDTO.getId() == null) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        Notice notice = copyMapper.noticeDTOToNotice(noticeDTO);
        boolean isSuccess = updateById(notice);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        return Result.success(notice);
    }

    /**
     * 根据指定 id删除通知
     * @param id
     * @return
     */
    @Override
    public Result deleteNotice(String id) {
        boolean isSuccess = removeById(id);
        if (!isSuccess) {
            return Result.error(MessageConstant.DELETE_ERROR);
        }
        return Result.success(id);
    }
}




