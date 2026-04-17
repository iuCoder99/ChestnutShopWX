package com.app.uni_app.service;

import com.app.uni_app.common.result.CursorCommonEntity;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.AppendProductFirstCommentDTO;
import com.app.uni_app.pojo.dto.FirstProductCommentDTO;
import com.app.uni_app.pojo.dto.SecondProductCommentDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public interface ProductCommentService {

    Result<?> saveProductFirstComment(@Valid FirstProductCommentDTO firstProductCommentDTO);

    Result<?> saveProductSecondComment(@Valid SecondProductCommentDTO secondProductCommentDTO);

    Result<?> getProductCommentBySortType(@Valid CursorCommonEntity cursorCommonEntity, @NotBlank String productId);

    Result<?> appendProductFirstComment(@Valid @NotNull AppendProductFirstCommentDTO appendProductFirstCommentDTO);

    Result<?> getSecondComment(@NotBlank String firstCommentId , @NotNull CursorCommonEntity cursorCommonEntity);

    Result<?> getAppendComment(@NotBlank String firstCommentId);

    Result<?> getProductCommentCount(@NotBlank String productId);

    Result<?> updateProductCommentLike(@NotBlank String productCommentId, @NotNull Integer isLike ,@NotNull Integer isFirstComment);

}
