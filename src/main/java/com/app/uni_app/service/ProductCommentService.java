package com.app.uni_app.service;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.FirstProductCommentDTO;
import com.app.uni_app.pojo.dto.SecondProductCommentDTO;
import jakarta.validation.Valid;


public interface ProductCommentService {

    Result<?> saveProductFirstComment(@Valid FirstProductCommentDTO firstProductCommentDTO);

    Result<?> saveProductSecondComment(@Valid SecondProductCommentDTO secondProductCommentDTO);

}
