package com.app.uni_app.service.impl;

import com.app.uni_app.common.constant.DataConstant;
import com.app.uni_app.common.constant.DatePatternConstants;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.exception.EmptyObjectException;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.CursorCommonEntity;
import com.app.uni_app.common.result.CursorCommonResult;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.common.util.BloomFilterUtils;
import com.app.uni_app.infrastructure.mp.util.MyBatisBatchExecutor;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.connect.StringRedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.infrastructure.redis.generator.RedisMessageGenerator;
import com.app.uni_app.infrastructure.redis.properties.RedisCacheTtlProperties;
import com.app.uni_app.infrastructure.rocketmq.constant.order.MqOrderConstant;
import com.app.uni_app.infrastructure.rocketmq.consumer.order.OrderStatusConsumer;
import com.app.uni_app.mapper.ProductCommentAppendMapper;
import com.app.uni_app.mapper.ProductCommentLikeMapper;
import com.app.uni_app.mapper.ProductCommentMapper;
import com.app.uni_app.pojo.dto.AppendProductFirstCommentDTO;
import com.app.uni_app.pojo.dto.FirstProductCommentDTO;
import com.app.uni_app.pojo.dto.SecondProductCommentDTO;
import com.app.uni_app.pojo.emums.OrderStatusEnum;
import com.app.uni_app.pojo.emums.ProductCommentQuerySortTypeEnum;
import com.app.uni_app.pojo.entity.ProductComment;
import com.app.uni_app.pojo.entity.ProductCommentAppend;
import com.app.uni_app.pojo.entity.ProductCommentLike;
import com.app.uni_app.pojo.vo.ProductAppendCommentVO;
import com.app.uni_app.service.ProductCommentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductCommentServiceImpl extends ServiceImpl<ProductCommentMapper, ProductComment> implements ProductCommentService {

    private final RocketMQTemplate rocketMQTemplate;

    private final CopyMapper copyMapper;

    private final ProductCommentAppendMapper productCommentAppendMapper;

    private final ProductCommentLikeMapper productCommentLikeMapper;

    private final RedisCacheTtlProperties redisCacheTtlProperties;

    private final MyBatisBatchExecutor myBatisBatchExecutor;

    private final BloomFilterUtils bloomFilterUtils;

    private final static ProductComment emptyProductComment = ProductComment
            .builder()
            .id(DataConstant.ZERO_LONG)
            .build();

    private final static ProductCommentAppend emptyProductCommentAppend = ProductCommentAppend
            .builder()
            .id(DataConstant.ZERO_LONG)
            .build();

    private final static String emptyProductCommentCount = "-1";

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
        String destination = MqOrderConstant.TOPIC_ORDER + ":" + MqOrderConstant.TAG_ORDER_STATUS;
        HashMap<String, Object> mqMessageMap = new HashMap<>(2);
        mqMessageMap.put(OrderStatusConsumer.ORDER_NO, firstProductCommentDTO.getOrderNo());
        mqMessageMap.put(OrderStatusConsumer.ORDER_STATUS_ENUM, OrderStatusEnum.EVALUATED.getValue());
        rocketMQTemplate.convertAndSend(destination, mqMessageMap);
        StringRedisConnector.delete(RedisKeyGenerator.productCommentCount(Long.valueOf(firstProductCommentDTO.getProductId())));
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
        firstProductComment = getProductCommentIfRedisCacheNull(firstProductComment, parentCommentId, firstCommentKey);
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
        StringRedisConnector.delete(RedisKeyGenerator.productCommentCount(Long.valueOf(secondProductCommentDTO.getProductId())));
        return Result.success();
    }

    /**
     * 传入 redis 查询后的一级评论结果
     * 进行判断是否为 null ,是会进行数据库查询,如果为空会缓存空对象
     * 不是 null ,放行,不做处理
     * @param firstProductComment
     * @param parentCommentId
     * @param firstCommentKey
     * @return
     */
    private ProductComment getProductCommentIfRedisCacheNull(ProductComment firstProductComment, Long parentCommentId, String firstCommentKey) {
        if (Objects.isNull(firstProductComment)) {
            firstProductComment = lambdaQuery().eq(ProductComment::getId, parentCommentId).one();
            //缓存空对象
            if (Objects.isNull(firstProductComment)) {
                RedisConnector.setHashObject(firstCommentKey, emptyProductComment);
                throw new EmptyObjectException(MessageConstant.DATA_ERROR);
            }
            RedisConnector.setHashObject(firstCommentKey, firstProductComment);
            RedisConnector.expire(firstCommentKey, redisCacheTtlProperties.getProductFirstCommentTtl(), TimeUnit.SECONDS);

        }
        return firstProductComment;
    }

    /**
     * 对一级评论追加评价
     * @param appendProductFirstCommentDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> appendProductFirstComment(AppendProductFirstCommentDTO appendProductFirstCommentDTO) {
        String userId = BaseContext.getUserId();
        ProductComment productComment = lambdaQuery().eq(ProductComment::getOrderNo, appendProductFirstCommentDTO.getOrderNo()).one();
        ProductCommentAppend productCommentAppend = copyMapper.appendProductFirstCommentDTOToProductCommentAppend(appendProductFirstCommentDTO);
        Long commentId = productComment.getId();
        String firstCommentKey = RedisKeyGenerator.firstCommentKey(commentId);
        ProductComment firstProductComment = RedisConnector.getHashObject(firstCommentKey, ProductComment.class);
        firstProductComment = getProductCommentIfRedisCacheNull(firstProductComment, commentId, firstCommentKey);
        //空对象过滤
        if (firstProductComment.getId().equals(DataConstant.ZERO_LONG)) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        //评论伪造过滤
        if (!firstProductComment.getProductId().equals(Long.valueOf(appendProductFirstCommentDTO.getProductId()))
                && firstProductComment.getUserId().equals(Long.valueOf(userId))) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        //已经追评过滤
        if (firstProductComment.getIsAppendComment() == 1) {
            return Result.error(MessageConstant.HAVE_APPEND);
        }
        productCommentAppend.setProductId(firstProductComment.getProductId())
                .setProductSpecId(firstProductComment.getProductSpecId())
                .setOrderNo(firstProductComment.getOrderNo())
                .setUserId(Long.valueOf(userId))
                .setCommentId(commentId);
        myBatisBatchExecutor.executeBatch(sqlSession -> {
            ProductCommentAppendMapper batchAppendMapper = sqlSession.getMapper(ProductCommentAppendMapper.class);
            ProductCommentMapper batchCommentMapper = sqlSession.getMapper(ProductCommentMapper.class);
            batchAppendMapper.insert(productCommentAppend);
            LambdaUpdateWrapper<ProductComment> updateWrapper = new LambdaUpdateWrapper<ProductComment>()
                    .eq(ProductComment::getId, commentId)
                    .set(ProductComment::getIsAppendComment, 1);
            batchCommentMapper.update(updateWrapper);
            return null;
        });
        RedisConnector.delete(firstCommentKey);
        String destination = MqOrderConstant.TOPIC_ORDER + ":" + MqOrderConstant.TAG_ORDER_STATUS;
        HashMap<String, Object> mqMessageMap = new HashMap<>(2);
        mqMessageMap.put(OrderStatusConsumer.ORDER_NO, appendProductFirstCommentDTO.getOrderNo());
        mqMessageMap.put(OrderStatusConsumer.ORDER_STATUS_ENUM, OrderStatusEnum.REVIEWED.getValue());
        rocketMQTemplate.convertAndSend(destination, mqMessageMap);
        return Result.success();
    }

    /**
     * 游标分类查询一级评论
     * @param cursorCommonEntity
     * @param productId
     * @return
     */
    @Override
    public Result<?> getProductCommentBySortType(CursorCommonEntity cursorCommonEntity, String productId) {
        if (!bloomFilterUtils.contains(Long.valueOf(productId))) {
            return Result.error(MessageConstant.DATA_ERROR);
        }

        String sortType = cursorCommonEntity.getSortType();
        String endCommentCreateTimeText = cursorCommonEntity.getSortValue();
        LocalDateTime endCommentCreateTime;
        if (StringUtils.isNotBlank(endCommentCreateTimeText)) {
            try {
                endCommentCreateTime = LocalDateTime.parse(cursorCommonEntity.getSortValue(), DatePatternConstants.NORMAL_DATETIME_FORMATTER);
            } catch (DateTimeParseException e) {
                log.error(MessageConstant.DATE_TIME_PARSE_ERROR);
                return Result.error(MessageConstant.DATE_TIME_PARSE_ERROR);
            }
        } else {
            endCommentCreateTime = LocalDateTime.now();
        }
        Long sortId = cursorCommonEntity.getSortId();
        Integer querySize = cursorCommonEntity.getQuerySize();
        ProductCommentQuerySortTypeEnum productCommentQuerySortTypeEnum = ProductCommentQuerySortTypeEnum.getByValue(sortType);
        SFunction<ProductComment, Object> function = productCommentQuerySortTypeEnum.getFunction();
        Object parameter = productCommentQuerySortTypeEnum.getParameter();

        LambdaQueryChainWrapper<ProductComment> productCommentLambdaQueryChainWrapper = lambdaQuery();
        if (!Objects.isNull(function)) {
            productCommentLambdaQueryChainWrapper = productCommentLambdaQueryChainWrapper.eq(function, parameter);
        }
        LocalDateTime finalEndCommentCreateTime = endCommentCreateTime;
        Page<ProductComment> pageResult = productCommentLambdaQueryChainWrapper
                .eq(ProductComment::getProductId, Long.valueOf(productId))
                .eq(ProductComment::getParentId, 0)
                .and(wrapper -> wrapper
                        .lt(ProductComment::getCreateTime, finalEndCommentCreateTime)
                        .or(wrapper2 -> wrapper2
                                .eq(ProductComment::getCreateTime, finalEndCommentCreateTime)
                                .lt(ProductComment::getId, sortId)
                        )
                )
                .orderByDesc(ProductComment::getCreateTime)
                .orderByDesc(ProductComment::getId)
                .page(new Page<>(1, querySize));
        List<ProductComment> productCommentList = pageResult.getRecords();
        setProductCommentIsLike(productCommentList).forEach(productComment -> {
                    String key = RedisKeyGenerator.firstCommentKey(productComment.getId());
                    if (RedisConnector.hasKey(key)) {
                        Integer likeCount = RedisConnector.getHashField(key, ProductComment.Fields.likeCount, Integer.class);
                        productComment.setLikeCount(likeCount);
                    }
                }
        );
        return getCursorCommonResult(cursorCommonEntity, productCommentList, copyMapper::productCommentToProductFirstCommentVO);
    }

    /**
     * 业务结果封装方法
     * @param cursorCommonEntity
     * @param productCommentList
     * @param copyMapperFunction
     * @return
     * @param <T>
     */

    private <T> Result<CursorCommonResult> getCursorCommonResult(CursorCommonEntity cursorCommonEntity, List<ProductComment> productCommentList, Function<ProductComment, T> copyMapperFunction) {
        if (productCommentList.isEmpty()) {
            return Result.success(CursorCommonResult.builder().isEnd(true).build());
        }
        ProductComment productComment = productCommentList.get(productCommentList.size() - 1);
        List<T> resultList = productCommentList.stream().map(copyMapperFunction).toList();
        cursorCommonEntity.setSortId(productComment.getId()).setSortValue(productComment.getCreateTime().format(DatePatternConstants.NORMAL_DATETIME_FORMATTER));
        CursorCommonResult cursorResult = CursorCommonResult.builder().cursorCommonEntity(cursorCommonEntity).list(resultList).build();
        return Result.success(cursorResult);
    }

    /**
     * 根据指定一级评论 id 查询二级评论
     * @param firstCommentId
     * @return
     */
    @Override
    public Result<?> getSecondComment(String firstCommentId, CursorCommonEntity cursorCommonEntity) {
        String endCommentCreateTimeText = cursorCommonEntity.getSortValue();
        LocalDateTime endCommentCreateTime;
        if (StringUtils.isNotBlank(endCommentCreateTimeText)) {
            try {
                endCommentCreateTime = LocalDateTime.parse(cursorCommonEntity.getSortValue(), DatePatternConstants.NORMAL_DATETIME_FORMATTER);
            } catch (DateTimeParseException e) {
                log.error(MessageConstant.DATE_TIME_PARSE_ERROR);
                return Result.error(MessageConstant.DATE_TIME_PARSE_ERROR);
            }
        } else {
            endCommentCreateTime = LocalDateTime.now();
        }
        Long endCommentId = cursorCommonEntity.getSortId();
        Integer querySize = cursorCommonEntity.getQuerySize();
        Page<ProductComment> pageResult = lambdaQuery().eq(ProductComment::getParentId, firstCommentId)
                .and(wrapper ->
                        wrapper.lt(ProductComment::getCreateTime, endCommentCreateTime)
                                .or(wrapper2 -> {
                                    wrapper2.eq(ProductComment::getCreateTime, endCommentCreateTime)
                                            .lt(ProductComment::getId, endCommentId);
                                })
                )
                .orderByDesc(ProductComment::getCreateTime)
                .orderByDesc(ProductComment::getId)
                .page(new Page<>(1, querySize));
        List<ProductComment> productCommentList = pageResult.getRecords();
        setProductCommentIsLike(productCommentList).forEach(productComment -> {
                    String key = RedisKeyGenerator.secondCommentKey(productComment.getId());
                    if (RedisConnector.hasKey(key)) {
                        Integer likeCount = RedisConnector.getHashField(key, ProductComment.Fields.likeCount, Integer.class);
                        productComment.setLikeCount(likeCount);
                    }
                }
        );
        return getCursorCommonResult(cursorCommonEntity, productCommentList, copyMapper::productCommentToProductSecondCommentVO);
    }


    /**
     * 查询一级评论下的追评
     * @param firstCommentId
     * @return
     */
    @Override
    public Result<?> getAppendComment(String firstCommentId) {
        Long aboutFirstCommentId = Long.valueOf(firstCommentId);
        String appendCommentKey = RedisKeyGenerator.appendCommentKey(aboutFirstCommentId);
        ProductCommentAppend productCommentAppend = RedisConnector.getHashObject(appendCommentKey, ProductCommentAppend.class);
        if (Objects.isNull(productCommentAppend)) {
            ProductCommentAppend productCommentAppendSelectOne = productCommentAppendMapper
                    .selectOne(new LambdaQueryWrapper<>(ProductCommentAppend.class)
                            .eq(ProductCommentAppend::getCommentId, aboutFirstCommentId)
                    );
            if (Objects.isNull(productCommentAppendSelectOne)) {
                RedisConnector.setHashObject(appendCommentKey, emptyProductCommentAppend);
                RedisConnector.expire(appendCommentKey, redisCacheTtlProperties.getProductAppendCommentTtl(), TimeUnit.SECONDS);
                return Result.error(MessageConstant.DATA_ERROR);
            }
            RedisConnector.setHashObject(appendCommentKey, productCommentAppendSelectOne);
            RedisConnector.expire(appendCommentKey, redisCacheTtlProperties.getProductAppendCommentTtl(), TimeUnit.SECONDS);
            ProductAppendCommentVO productAppendCommentVO = copyMapper.productCommentAppendToProductCommentAppendVO(productCommentAppendSelectOne);
            return Result.success(productAppendCommentVO);
        }
        //空对象过滤
        if (productCommentAppend.getId().equals(DataConstant.ZERO_LONG)) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        ProductAppendCommentVO productAppendCommentVO = copyMapper.productCommentAppendToProductCommentAppendVO(productCommentAppend);
        return Result.success(productAppendCommentVO);

    }

    /**
     * 获取商品下评论数
     */
    @Override
    public Result<?> getProductCommentCount(String productId) {
        boolean isContain = bloomFilterUtils.contains(Long.valueOf(productId));
        if (!isContain) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        Long productIdLong = Long.valueOf(productId);
        String key = RedisKeyGenerator.productCommentCount(productIdLong);
        String productCommentCount = StringRedisConnector.opsForValue().get(key);
        if (StringUtils.isBlank(productCommentCount)) {
            Long productCommentCountLong = lambdaQuery().eq(ProductComment::getProductId, productIdLong).count();
            if (Objects.isNull(productCommentCountLong)) {
                StringRedisConnector.opsForValue().set(key, emptyProductCommentCount);
                StringRedisConnector.expire(key, redisCacheTtlProperties.getProductCommentCountTtl(), TimeUnit.SECONDS);
                return Result.error(MessageConstant.DATA_ERROR);
            }
            productCommentCount = productCommentCountLong.toString();
            StringRedisConnector.opsForValue().set(key, productCommentCount);
            StringRedisConnector.expire(key, redisCacheTtlProperties.getProductCommentCountTtl(), TimeUnit.SECONDS);
            return Result.success(productCommentCount);
        }
        if (StringUtils.equals(productCommentCount, emptyProductCommentCount)) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        return Result.success(productCommentCount);

    }

    /**
     * 对商品评论进行点赞和取消点赞
     * 判断是否为一级评论,如果是,更新缓存,刷新缓存时间
     * 如果是二级评论,新建一个二级缓存,存储点赞数(在查询二级评论的时可以进行过滤)
     * 在评论下维护一个set集合用来判断当前用户是否已经点赞
     *
     */
    @Override
    public Result<?> updateProductCommentLike(String productCommentId, Integer isLike, Integer isFirstComment) {
        String userId = BaseContext.getUserId();
        String message = RedisMessageGenerator.CommentLikeMessageCreate(userId, productCommentId, isLike);
        String listKey = RedisKeyGenerator.productCommentLikeMessageList();
        RedisConnector.opsForList().leftPush(listKey, message);
        if (isFirstComment == 1) {
            String firstCommentKey = RedisKeyGenerator.firstCommentKey(Long.valueOf(productCommentId));
            ProductComment firstCommentCache = RedisConnector.getHashObject(firstCommentKey, ProductComment.class);
            if (Objects.isNull(firstCommentCache)) {
                ProductComment firstProductComment = lambdaQuery().eq(ProductComment::getId, productCommentId).one();
                if (Objects.isNull(firstProductComment)) {
                    setCommentRedisCache(firstCommentKey, emptyProductComment, redisCacheTtlProperties.getProductFirstCommentTtl());
                    return Result.error(MessageConstant.DATA_ERROR);
                }
                updateLikeCount(firstProductComment, isLike);
                setCommentRedisCache(firstCommentKey, firstProductComment, redisCacheTtlProperties.getProductFirstCommentTtl());
                updateProductCommentUserIdSet(firstProductComment.getId(), isLike, Long.valueOf(userId));
                return Result.success();
            }
            updateLikeCount(firstCommentCache, isLike);
            setCommentRedisCache(firstCommentKey, firstCommentCache, redisCacheTtlProperties.getProductFirstCommentTtl());
            updateProductCommentUserIdSet(firstCommentCache.getId(), isLike, Long.valueOf(userId));
            return Result.success();
        } else {
            String secondCommentKey = RedisKeyGenerator.secondCommentKey(Long.valueOf(productCommentId));
            ProductComment secondCommentCache = RedisConnector.getHashObject(secondCommentKey, ProductComment.class);
            if (Objects.isNull(secondCommentCache)) {
                ProductComment secondComment = lambdaQuery().eq(ProductComment::getId, productCommentId).one();
                updateLikeCount(secondComment, isLike);
                setCommentRedisCache(secondCommentKey, secondComment, redisCacheTtlProperties.getProductSecondCommentTtl());
                updateProductCommentUserIdSet(secondComment.getId(), isLike, Long.valueOf(userId));
                return Result.success();

            }
            updateLikeCount(secondCommentCache, isLike);
            setCommentRedisCache(secondCommentKey, secondCommentCache, redisCacheTtlProperties.getProductSecondCommentTtl());
            updateProductCommentUserIdSet(secondCommentCache.getId(), isLike, Long.valueOf(userId));
            return Result.success();
        }

    }

    /**
     * 传入评论,对评论修改点赞数
     *
     * @param productComment 传入评论实体(一级评论或二级评论)
     * @param isLike         是否点赞
     */
    private void updateLikeCount(ProductComment productComment, Integer isLike) {
        if (Objects.isNull(productComment) || Objects.isNull(isLike)) {
            log.error("点赞数更新异常：参数为空，位置：updateLikeCount()");
            throw new IllegalArgumentException("点赞参数不能为空");
        }
        Integer likeCount = productComment.getLikeCount();
        if (isLike == 0) {
            likeCount--;
        } else {
            likeCount++;
        }
        productComment.setLikeCount(likeCount).setUpdatedTime(LocalDateTime.now());
    }

    /**
     * 存储评论缓存到 redis
     * @param key 一级 key
     * @param productComment 评论实体(一级评论 二级评论)
     * @param ttl 过期时间
     */
    private void setCommentRedisCache(String key, ProductComment productComment, Long ttl) {
        RedisConnector.setHashObject(key, productComment);
        RedisConnector.expire(key, ttl, TimeUnit.SECONDS);
    }

    /**
     * 更新评论点赞用户 id set 缓存
     * @param commentId 评论 id
     * @param isLike 是否点赞
     * @param userId 当前操作的用户 id
     */
    private void updateProductCommentUserIdSet(Long commentId, Integer isLike, Long userId) {
        String key = RedisKeyGenerator.productCommentUserIdList(commentId);
        Boolean hasKey = RedisConnector.hasKey(key);
        if (!hasKey) {
            LambdaQueryWrapper<com.app.uni_app.pojo.entity.ProductCommentLike> lambdaQueryWrapper = new LambdaQueryWrapper<>(com.app.uni_app.pojo.entity.ProductCommentLike.class)
                    .eq(com.app.uni_app.pojo.entity.ProductCommentLike::getCommentId, commentId)
                    .eq(com.app.uni_app.pojo.entity.ProductCommentLike::getStatus, 1);
            Set<Long> commentUserIdSet = productCommentLikeMapper.selectList(lambdaQueryWrapper).stream().filter(Objects::nonNull).map(ProductCommentLike::getUserId).collect(Collectors.toSet());
            if (isLike == 1) {
                commentUserIdSet.add(userId);
            } else {
                commentUserIdSet.remove(userId);
            }
            RedisConnector.opsForSet().add(key, commentUserIdSet.toArray());
            RedisConnector.expire(key, redisCacheTtlProperties.getProductCommentUserIdSetTtl(), TimeUnit.SECONDS);
            return;
        }
        if (isLike == 1) {
            RedisConnector.opsForSet().add(key, userId);
        } else {
            RedisConnector.opsForSet().remove(key, userId);
        }
        RedisConnector.expire(key, redisCacheTtlProperties.getProductCommentUserIdSetTtl(), TimeUnit.SECONDS);
    }


    /**
     * 为评论设置 like , 更新评论下的用户set缓存
     * @param productCommentList 未赋值的评论列表
     * @return 赋值后的评论列表
     */
    private List<ProductComment> setProductCommentIsLike(List<ProductComment> productCommentList) {
        String userId;
        try {
            userId = BaseContext.getUserId();
        } catch (UnauthenticatedException e) {
            //用户未登录
            productCommentList.forEach(productComment -> productComment.setLike(false));
            return productCommentList;
        }
        if (productCommentList.isEmpty()) {
            return productCommentList;
        }
        HashMap<Long, Integer> needQueryEntityMap = new HashMap<>();
        for (int i = 0; i < productCommentList.size(); i++) {
            ProductComment productComment = productCommentList.get(i);
            String key = RedisKeyGenerator.productCommentUserIdList(productComment.getId());
            if (!RedisConnector.hasKey(key)) {
                needQueryEntityMap.put(productComment.getId(), i);
            } else {
                boolean isLike = Boolean.TRUE.equals(RedisConnector.opsForSet().isMember(key, Long.valueOf(userId)));
                productComment.setLike(isLike);
            }
        }
        if (needQueryEntityMap.isEmpty()) {
            return productCommentList;
        }
        Set<Long> needQueryEntityIdSet = needQueryEntityMap.keySet();
        LambdaQueryWrapper<ProductCommentLike> lambdaQueryWrapper = new LambdaQueryWrapper<ProductCommentLike>()
                .in(ProductCommentLike::getCommentId, needQueryEntityIdSet)
                .eq(ProductCommentLike::getStatus, 1);
        List<ProductCommentLike> allLike = productCommentLikeMapper.selectList(lambdaQueryWrapper);
        Map<Long, Set<Long>> commentUserMap = allLike.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        ProductCommentLike::getCommentId,
                        Collectors.mapping(ProductCommentLike::getUserId, Collectors.toSet())
                ));
        //消费有点赞的评论
        commentUserMap.forEach((commentId, userIdSet) -> {
            boolean isLike = userIdSet.contains(Long.valueOf(userId));
            Integer index = needQueryEntityMap.get(commentId);
            productCommentList.get(index).setLike(isLike);
            String key = RedisKeyGenerator.productCommentUserIdList(commentId);
            RedisConnector.opsForSet().add(key, userIdSet.toArray(new Long[0]));
            RedisConnector.expire(key, redisCacheTtlProperties.getProductCommentUserIdSetTtl(), TimeUnit.SECONDS);
            needQueryEntityIdSet.remove(commentId);
        });
        //消费无点赞的评论 , 缓存用户 set 存 0L
        if (!needQueryEntityMap.isEmpty()) {
            needQueryEntityMap.forEach((commentId, index) -> {
                ProductComment productComment = productCommentList.get(index);
                productComment.setLike(false);
                String key = RedisKeyGenerator.productCommentUserIdList(commentId);
                RedisConnector.opsForSet().add(key, DataConstant.ZERO_LONG);
                RedisConnector.expire(key, redisCacheTtlProperties.getProductCommentUserIdSetTtl(), TimeUnit.SECONDS);

            });
        }
        return productCommentList;

    }

    /**
     * 使用用户点过的所有评论 判断用户是否点赞,如果用户未登录,返回false
     * (该接口暂时未使用,后续有查看用户点赞评论记录时,可以使用)
     * @param commentId 评论 id
     * @return 是否点赞
     */
    private boolean getUserIsLikeByUserCommentIdSet(Long commentId) {
        String userId;
        try {
            userId = BaseContext.getUserId();
        } catch (UnauthenticatedException e) {
            //用户未登录
            return false;
        }
        String keySet = RedisKeyGenerator.userProductCommentIdList(Long.valueOf(userId));
        Boolean isHasKey = RedisConnector.hasKey(keySet);
        if (!isHasKey) {
            LambdaQueryWrapper<ProductCommentLike> lambdaQueryWrapper = new LambdaQueryWrapper<ProductCommentLike>().eq(ProductCommentLike::getUserId, userId).eq(ProductCommentLike::getStatus, 1);
            List<ProductCommentLike> productCommentLikeList = productCommentLikeMapper.selectList(lambdaQueryWrapper);
            Set<Long> commentLikeIdSet = productCommentLikeList.stream().map(ProductCommentLike::getCommentId).collect(Collectors.toSet());
            RedisConnector.opsForSet().add(keySet, commentLikeIdSet.toArray());
            RedisConnector.expire(keySet, redisCacheTtlProperties.getUserProductCommentLikeIdSetTtl(), TimeUnit.SECONDS);
            return commentLikeIdSet.contains(commentId);

        }
        return Boolean.TRUE.equals(RedisConnector.opsForSet().isMember(keySet, commentId));

    }

}
