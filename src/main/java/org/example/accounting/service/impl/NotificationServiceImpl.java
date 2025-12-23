package org.example.accounting.service.impl;

import org.example.accounting.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 基于 SSE 的通知实现。
 * 管理 SseEmitter 连接并支持向指定用户或广播发送事件。
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    // 用户 -> 该用户的 emitters 列表
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();
    // 全局广播订阅（未指定 user 的订阅会放这里）
    private final CopyOnWriteArrayList<SseEmitter> globalEmitters = new CopyOnWriteArrayList<>();
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "sse-heartbeat");
        t.setDaemon(true);
        return t;
    });

    @Override
    public SseEmitter registerEmitter(String username) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30 minutes
        // 注册回调：完成/超时/出错时移除
        emitter.onCompletion(() -> unregisterEmitter(username, emitter));
        emitter.onTimeout(() -> unregisterEmitter(username, emitter));
        emitter.onError((ex) -> unregisterEmitter(username, emitter));

        if (username == null || username.isBlank()) {
            globalEmitters.add(emitter);
        } else {
            emittersByUser.computeIfAbsent(username, k -> new CopyOnWriteArrayList<>()).add(emitter);
        }
        logger.info("SSE registered for user={}, emittersGlobal={}, usersWithEmitters={}", username, globalEmitters.size(), emittersByUser.size());
        return emitter;
    }

    @PostConstruct
    public void startHeartbeat() {
        // send heartbeat every 20 seconds to keep proxies from closing idle connections
        heartbeatScheduler.scheduleAtFixedRate(this::sendHeartbeat, 20, 20, TimeUnit.SECONDS);
        logger.info("Started SSE heartbeat scheduler");
    }

    @PreDestroy
    public void stopHeartbeat() {
        try {
            heartbeatScheduler.shutdownNow();
        } catch (Exception ignored) {}
        logger.info("Stopped SSE heartbeat scheduler");
    }

    private void sendHeartbeat() {
        Map<String, Object> payload = java.util.Collections.singletonMap("ts", Instant.now().toString());
        // global
        for (SseEmitter emitter : globalEmitters) {
            try {
                emitter.send(SseEmitter.event().name("ping").data(payload));
            } catch (IOException e) {
                logger.debug("Heartbeat send failed to global emitter, unregistering", e);
                unregisterEmitter(null, emitter);
            } catch (IllegalStateException e) {
                logger.debug("Heartbeat illegal state, unregistering emitter", e);
                unregisterEmitter(null, emitter);
            }
        }
        // per-user
        for (Map.Entry<String, CopyOnWriteArrayList<SseEmitter>> entry : emittersByUser.entrySet()) {
            String user = entry.getKey();
            for (SseEmitter emitter : entry.getValue()) {
                try {
                    emitter.send(SseEmitter.event().name("ping").data(payload));
                } catch (IOException e) {
                    logger.debug("Heartbeat send failed to user={} emitter, unregistering", user, e);
                    unregisterEmitter(user, emitter);
                } catch (IllegalStateException e) {
                    logger.debug("Heartbeat illegal state for user={}, unregistering emitter", user, e);
                    unregisterEmitter(user, emitter);
                }
            }
        }
    }

    @Override
    public void unregisterEmitter(String username, SseEmitter emitter) {
        if (username == null || username.isBlank()) {
            globalEmitters.remove(emitter);
        } else {
            List<SseEmitter> list = emittersByUser.get(username);
            if (list != null) {
                list.remove(emitter);
                if (list.isEmpty()) emittersByUser.remove(username);
            }
        }
        try { emitter.complete(); } catch (Exception ignored) {}
    }

    @Override
    public void notifyUser(String username, String event, Map<String, ?> payload) {
        if (username == null) return;
        List<SseEmitter> list = emittersByUser.get(username);
        if (list == null || list.isEmpty()) return;
        for (SseEmitter emitter : list) {
            try {
                SseEmitter.SseEventBuilder ev = SseEmitter.event().name(event).data(payload);
                emitter.send(ev);
            } catch (IOException e) {
                unregisterEmitter(username, emitter);
            }
        }
    }

    @Override
    public void notifyAllUsers(String event, Map<String, ?> payload) {
        // 先发送给全局订阅者
        for (SseEmitter emitter : globalEmitters) {
            try {
                SseEmitter.SseEventBuilder ev = SseEmitter.event().name(event).data(payload);
                emitter.send(ev);
            } catch (IOException e) {
                unregisterEmitter(null, emitter);
            }
        }
        // 再发送给所有按用户订阅的连接
        for (Map.Entry<String, CopyOnWriteArrayList<SseEmitter>> entry : emittersByUser.entrySet()) {
            for (SseEmitter emitter : entry.getValue()) {
                try {
                    SseEmitter.SseEventBuilder ev = SseEmitter.event().name(event).data(payload);
                    emitter.send(ev);
                } catch (IOException e) {
                    unregisterEmitter(entry.getKey(), emitter);
                }
            }
        }
    }
}


