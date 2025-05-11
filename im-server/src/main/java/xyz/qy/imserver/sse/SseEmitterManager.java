package xyz.qy.imserver.sse;


import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SseEmitterManager {

    // 存储用户ID与对应的Emitter集合（一个用户可能多个连接）
    private static final Map<String, SseEmitter> USER_EMITTERS = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService HEARTBEAT_SCHEDULER = Executors.newScheduledThreadPool(1);

    // 添加Emitter
    public static void addEmitter(String userId, SseEmitter emitter) {
        USER_EMITTERS.put(userId, emitter); // 简单示例，实际可改为存储多个Emitter（如List）
        startHeartbeatForEmitter(userId, emitter);
        //setupEmitterCleanup(userId, emitter);
    }

    // 清理失效的Emitter
    private static void setupEmitterCleanup(String userId, SseEmitter emitter) {
        emitter.onCompletion(() -> USER_EMITTERS.remove(userId, emitter));
        emitter.onTimeout(() -> USER_EMITTERS.remove(userId, emitter));
    }

    // 为每个Emitter启动独立心跳
    private static void startHeartbeatForEmitter(String userId, SseEmitter emitter) {
        final ScheduledFuture<?>[] heartbeatFuture = new ScheduledFuture<?>[1];

        // 发送心跳任务
        Runnable heartbeatTask = () -> {
            try {
                if (emitter != null) {
                    // 发送空注释作为心跳（前端event-source-polyfill会自动忽略）
                    emitter.send(SseEmitter.event().comment(""));
                }
            } catch (IOException e) {
                // 心跳失败则取消任务并清理Emitter
                if (heartbeatFuture[0] != null) {
                    heartbeatFuture[0].cancel(true);
                }
                USER_EMITTERS.remove(userId, emitter);
            }
        };

        // 每25秒发送一次心跳（略小于前端超时时间）
        heartbeatFuture[0] = HEARTBEAT_SCHEDULER.scheduleAtFixedRate(
                heartbeatTask, 0, 25, TimeUnit.SECONDS
        );

        // 监听Emitter完成/超时以取消心跳
        emitter.onCompletion(() -> {
            if (heartbeatFuture[0] != null) {
                heartbeatFuture[0].cancel(true);
            }
        });
    }

    // 获取用户的Emitter
    public static SseEmitter getEmitter(String userId) {
        return USER_EMITTERS.get(userId);
    }

    // 发送消息给指定用户
    public static void sendToUser(String userId, String message) {
        SseEmitter emitter = USER_EMITTERS.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(message));
            } catch (IOException e) {
                emitter.completeWithError(e);
                USER_EMITTERS.remove(userId, emitter);
            }
        }
    }
}
