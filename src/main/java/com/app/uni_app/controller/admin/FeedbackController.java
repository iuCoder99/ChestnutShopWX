package com.app.uni_app.controller.admin;


import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.FeedbackDTO;
import com.app.uni_app.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "反馈管理")
public class FeedbackController {

    @Resource
    private FeedbackService feedbackService;

    @PostMapping("/feedback/add")
    @Operation(summary = "用户提交反馈")
    public Result<Object> addFeedback(@RequestBody @NotNull FeedbackDTO feedbackDTO){
       return feedbackService.addressService(feedbackDTO);

    }

}
