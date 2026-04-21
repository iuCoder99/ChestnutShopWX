package com.app.uni_app.job.schedule;


import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.infrastructure.redis.generator.RedisMessageGenerator;
import com.app.uni_app.job.constant.common.JobCommonConstant;
import com.app.uni_app.job.constant.schedule.JobScheduleConstant;
import com.app.uni_app.mapper.ProductCommentLikeMapper;
import com.app.uni_app.mapper.ProductCommentMapper;
import com.app.uni_app.pojo.entity.ProductCommentLike;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutorConsumeProductCommentLikeTask {

    private static final String BUSINESS = "消费商品评论点赞信息";

    private final ProductCommentLikeMapper productCommentLikeMapper;

    private final ProductCommentMapper productCommentMapper;

    private final ThreadPoolTaskExecutor executorSchedulerCommon;


    @Scheduled(cron = "0/30 * * * * ?")
    public void consumeProductCommentLike() {
        executorSchedulerCommon.execute(() -> {
                        log.info(JobScheduleConstant.PREFIX_SCHEDULED_EXECUTOR_TASK + BUSINESS
                                + JobCommonConstant.THREAD_NAME + "{}"
                                + JobCommonConstant.THREAD_ID + "{}"
                                , Thread.currentThread().getName()
                                , Thread.currentThread().getId());
                        String key = RedisKeyGenerator.productCommentLikeMessageList();
                        List<Object> messageList = RedisConnector.opsForList().range(key, -500, -1);
                        if (Objects.isNull(messageList) || messageList.isEmpty()) {
                            return;
                        }
                        List<String> stringMessageList = messageList.stream().map(object -> (String) object).toList();
                        Map<RedisMessageGenerator.CommentLikeMessageKey, Integer> doMap = RedisMessageGenerator.CommentLikeMessageParse(stringMessageList);
                        RedisConnector.opsForList().trim(key, 0, -501);
                        if (doMap.isEmpty()) {
                            return;
                        }
                        List<ProductCommentLike> productCommentLikeList = doMap.entrySet().stream().map(entrySet -> {
                            RedisMessageGenerator.CommentLikeMessageKey commentLikeMessageKey = entrySet.getKey();
                            return ProductCommentLike.builder().commentId(Long.valueOf(commentLikeMessageKey.commentId()))
                                    .userId(Long.valueOf(commentLikeMessageKey.userId()))
                                    .status(entrySet.getValue()).build();
                        }).toList();
                        productCommentLikeMapper.batchUpdate(productCommentLikeList);
                        Map<Long, Integer> dataMap = doMap.entrySet().stream().collect(Collectors.groupingBy(
                                entry -> Long.valueOf(entry.getKey().commentId()),
                                Collectors.summingInt(entry -> entry.getValue() == 0 ? -1 : 1)
                        ));
                        if (dataMap.isEmpty()){
                            return;
                        }
                        productCommentMapper.updateProductCommentLikeCount(dataMap);
        });
}
}
