package com.app.uni_app.service;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.FeedbackDTO;
import com.app.uni_app.pojo.entity.Feedback;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;

public interface FeedbackService extends IService<Feedback> {
    Result<Object> addressService( @NotNull FeedbackDTO feedbackDTO);
}

