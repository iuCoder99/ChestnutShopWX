package com.app.uni_app.service.impl;

import com.app.uni_app.common.constant.DataConstant;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.mapper.ProductCommentMapper;
import com.app.uni_app.pojo.dto.FirstProductCommentDTO;
import com.app.uni_app.pojo.dto.SecondProductCommentDTO;
import com.app.uni_app.pojo.entity.ProductComment;
import com.app.uni_app.service.ProductCommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;


@Service
public class ProductCommentServiceImpl extends ServiceImpl<ProductCommentMapper, ProductComment> implements ProductCommentService {

    @Resource
    private CopyMapper copyMapper;

    private final static ProductComment emptyProductComment = ProductComment.builder().id(DataConstant.ZERO_LONG)
            .build();

    /**
     * 添加用户一级评论
     */
    @Override
    public Result<?> saveProductFirstComment(FirstProductCommentDTO firstProductCommentDTO) {
        if (Objects.isNull(firstProductCommentDTO)) {
            return Result.error(MessageConstant.NETWORK_ERROR);
        }
        String userId = BaseContext.getUserId();
        ProductComment productComment = copyMapper.firstProductCommentDTOToProductComment(firstProductCommentDTO);
        testIsAnonymous(productComment)
                .setParentId(DataConstant.ZERO_LONG)
                .setUserId(Long.valueOf(userId))
                .setIsBuyer(1)
                .setCreateTime(LocalDateTime.now())
                .setUpdatedTime(LocalDateTime.now());
        boolean isSuccess = save(productComment);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        return Result.success();
    }

    /**
     * 判断用户是否匿名,进行匿名处理
     * @param productComment
     * @return
     */
    private ProductComment testIsAnonymous(ProductComment productComment) {
        int isAnonymous = productComment.getIsAnonymous();
        if (isAnonymous == 1) {
            productComment.setUserNickname(DataConstant.ANONYMOUS_NICKNAME)
                    .setUserAvatar(DataConstant.DEFAULT_AVATAR);
        }
        return productComment;
    }

    /**
     * 添加用户二级以上的评论
     * @param secondProductCommentDTO
     * @return
     */
    @Override
    public Result<?> saveProductSecondComment(SecondProductCommentDTO secondProductCommentDTO) {
        if (Objects.isNull(secondProductCommentDTO)) {
            return Result.error(MessageConstant.NETWORK_ERROR);
        }
        String userId = BaseContext.getUserId();
        ProductComment productComment = copyMapper.secondProductCommentDTOToProductComment(secondProductCommentDTO);
        Long parentCommentId = productComment.getParentId();
        String firstCommentKey = RedisKeyGenerator.firstCommentKey(parentCommentId);
        if (Objects.isNull(parentCommentId)) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        ProductComment firstProductComment = RedisConnector.getHashObject(firstCommentKey, ProductComment.class);
        if (Objects.isNull(firstProductComment)) {
            firstProductComment = lambdaQuery().eq(com.app.uni_app.pojo.entity.ProductComment::getId, parentCommentId).one();
            //缓存空对象
            if (Objects.isNull(firstProductComment)) {
                RedisConnector.setHashObject(firstCommentKey, emptyProductComment);
                return Result.error(MessageConstant.DATA_ERROR);
            }
            RedisConnector.setHashObject(firstCommentKey, firstProductComment);

        }
        //空对象过滤
        if (firstProductComment.getId().equals(DataConstant.ZERO_LONG)) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        Long firstProductCommentUserId = firstProductComment.getUserId();
        //确定是买家,进行标记
        if (firstProductCommentUserId.equals(Long.valueOf(userId))) {
            productComment.setIsBuyer(1);
        }
        testIsAnonymous(productComment).setUserId(Long.valueOf(userId)).setCreateTime(LocalDateTime.now()).setUpdatedTime(LocalDateTime.now());
        boolean isSuccess = save(productComment);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        return Result.success();
    }

}
