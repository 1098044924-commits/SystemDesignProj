package org.example.accounting.service;

import java.util.Map;

/**
 * 通知服务接口（用于服务器向特定用户或所有用户推送事件）。
 * 实现可以使用 SSE、WebSocket、消息队列等。
 */
public interface NotificationService {
    /**
     * 向指定用户名（或用户标识）发送事件通知。
     *
     * @param username 接收者用户名（或标识）
     * @param event    事件类型
     * @param payload  负载（通常为 Map，可被序列化为 JSON）
     */
    void notifyUser(String username, String event, Map<String, ?> payload);

    /**
     * 向所有在线/连接的用户广播事件。
     *
     * @param event   事件类型
     * @param payload 负载
     */
    void notifyAllUsers(String event, Map<String, ?> payload);
    /**
     * 注册一个 SSE 连接（SseEmitter）用于向指定用户或所有用户推送事件。
     * @param username 接收者用户名；若为 null 或空则表示订阅“所有用户”广播
     * @return 新建的 SseEmitter
     */
    org.springframework.web.servlet.mvc.method.annotation.SseEmitter registerEmitter(String username);

    /**
     * 取消注册某个 SseEmitter（当连接完成或超时时调用）。
     */
    void unregisterEmitter(String username, org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter);
}


