package com.app.uni_app.service.impl;

import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.mapper.FeedbackMapper;
import com.app.uni_app.pojo.dto.FeedbackDTO;
import com.app.uni_app.pojo.entity.Feedback;
import com.app.uni_app.service.FeedbackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {
    /**
     * 用户提交反馈
     * @param feedbackDTO
     * @return
     */
    @Override
    public Result<Object> addressService(FeedbackDTO feedbackDTO) {
        String userId = BaseContext.getUserId();
        Feedback feedback = Feedback.builder()
                .userId(Long.valueOf(userId))
                .content(feedbackDTO.getContent())
                .imageUrls(feedbackDTO.getImages())
                .contact(feedbackDTO.getContact())
                .createTime(LocalDateTime.now())
                .build();
        boolean isSuccess = save(feedback);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);

        }
        return Result.success();
    }
}
