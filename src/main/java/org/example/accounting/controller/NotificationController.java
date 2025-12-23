package org.example.accounting.controller;

import org.example.accounting.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSE 订阅端点。
 * 前端可调用：GET /api/notifications/subscribe?user=<username> （user 可选）
 */
@RestController
public class NotificationController {

    private final NotificationService notificationService;
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/api/notifications/subscribe")
    public SseEmitter subscribe(@RequestParam(name = "user", required = false) String user) {
        logger.debug("Subscribe called for user={}", user);
        SseEmitter emitter = notificationService.registerEmitter(user);
        // send an initial connected event to verify the connection
        try {
            SseEmitter.SseEventBuilder ev = SseEmitter.event().name("connected").data(java.util.Collections.singletonMap("ts", java.time.Instant.now().toString()));
            emitter.send(ev);
        } catch (Exception ex) {
            logger.warn("Failed to send initial SSE event to user={}", user, ex);
        }
        return emitter;
    }

    // alias to support previous client paths
    @GetMapping("/api/notifications/stream")
    public SseEmitter stream(@RequestParam(name = "user", required = false) String user) {
        logger.debug("Stream (alias) called for user={}", user);
        SseEmitter emitter = notificationService.registerEmitter(user);
        try {
            SseEmitter.SseEventBuilder ev = SseEmitter.event().name("connected").data(java.util.Collections.singletonMap("ts", java.time.Instant.now().toString()));
            emitter.send(ev);
        } catch (Exception ex) {
            logger.warn("Failed to send initial SSE event (alias) to user={}", user, ex);
        }
        return emitter;
    }

    @GetMapping("/api/notifications/ping")
    public String ping() {
        return "pong";
    }
}


