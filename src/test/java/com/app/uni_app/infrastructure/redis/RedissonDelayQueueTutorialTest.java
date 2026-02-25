package com.app.uni_app.infrastructure.redis;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 延迟队列教学实践测试类
 * <p>
 * 场景：用户修改购物车后，延迟 5 秒（为了测试方便，用 5 秒代替 5 小时）同步到数据库。
 * 如果在延迟期间用户再次修改，则重置倒计时（防抖）。
 * <p>
 * 核心原理：
 * 1. <b>RBlockingQueue</b>: 普通的阻塞队列，消费者从这里 'take' 数据。
 * 2. <b>RDelayedQueue</b>: 延迟队列，数据先存放在这里，到期后 Redisson 会自动将数据移动到 RBlockingQueue 中。
 */
@SpringBootTest
public class RedissonDelayQueueTutorialTest {

    @Resource
    private RedissonClient redissonClient;

    // 定义队列名称
    private static final String QUEUE_NAME = "cart:sync:queue";

    @Test
    public void testCartSyncDebounce() throws InterruptedException {
        System.out.println("====== 开始测试 Redisson 延迟队列 (防抖模式) ======");

        // 1. 获取队列实例
        // 目标队列：任务到期后会进入这个队列
        RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(QUEUE_NAME);
        // 延迟队列：任务先发到这里
        RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);


        // 为了测试干净，先清空队列
        delayedQueue.clear();
        blockingQueue.clear();

        // 2. 启动消费者线程 (模拟后台定时任务或监听器)
        Thread consumerThread = new Thread(() -> {
            System.out.println("Consumer: 消费者线程已启动，正在等待任务...");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // take() 是阻塞方法，没有任务时会等待
                    String userId = blockingQueue.take();
                    System.out.println(String.format("[%s] Consumer: 收到同步任务! 用户ID: %s -> 开始写入数据库...", 
                        LocalDateTime.now(), userId));
                    
                    // 模拟数据库写入耗时
                    // cartService.syncToDatabase(userId);
                    
                } catch (InterruptedException e) {
                    System.out.println("Consumer: 消费者被中断，退出...");
                    break;
                }
            }
        });
        consumerThread.start();

        // 3. 模拟用户行为
        String userId = "user_10086";

        // --- 第 1 次修改购物车 ---
        System.out.println(String.format("[%s] Producer: 用户第 1 次修改购物车", LocalDateTime.now()));
        addSyncTask(delayedQueue, userId);

        // 模拟过了 2 秒
        TimeUnit.SECONDS.sleep(2);

        // --- 第 2 次修改购物车 (此时第1次任务还没到期) ---
        // 核心逻辑：因为我们想"覆盖"上一次任务，所以要先移除，再添加
        System.out.println(String.format("[%s] Producer: 用户第 2 次修改购物车 (重置倒计时)", LocalDateTime.now()));
        addSyncTask(delayedQueue, userId);

        // 4. 等待足够长的时间，观察消费者输出
        // 预期：只会收到一次任务，且时间是在第2次修改后的 5 秒
        System.out.println("Main: 等待 8 秒观察结果...");
        TimeUnit.SECONDS.sleep(8);

        // 停止消费者
        consumerThread.interrupt();
        System.out.println("====== 测试结束 ======");
    }

    /**
     * 添加同步任务（防抖逻辑核心）
     * @param delayedQueue 延迟队列
     * @param userId 用户ID
     */
    private void addSyncTask(RDelayedQueue<String> delayedQueue, String userId) {
        // 1. 尝试移除旧任务 (如果存在)
        // remove 方法会根据 equals() 判断元素是否相同
        // String 的 equals 是比较内容，所以只要 userId 相同就能移除
        boolean removed = delayedQueue.remove(userId);
        if (removed) {
            System.out.println("   -> 已移除旧任务，重置倒计时");
        }

        // 2. 添加新任务
        // 参数：元素, 延迟时间, 时间单位
        // 这里设置为 5 秒 (实际业务中是 5 小时: 5, TimeUnit.HOURS)
        delayedQueue.offer(userId, 5, TimeUnit.SECONDS);
        System.out.println("   -> 已添加新任务，5秒后执行");
    }
}
