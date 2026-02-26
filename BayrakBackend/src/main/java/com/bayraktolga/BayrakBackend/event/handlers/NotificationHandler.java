package com.bayraktolga.BayrakBackend.event.handlers;

import com.bayraktolga.BayrakBackend.enums.NotificationType;
import com.bayraktolga.BayrakBackend.event.ListenEvent;
import com.bayraktolga.BayrakBackend.service.NotificationService;
import com.bayraktolga.BayrakBackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationHandler {

    private final NotificationService notificationService;
    private final UserService userService;

    @ListenEvent("NOTIFICATION")
    public void handleNotification(Map<String, Object> event) {
        try {
            UUID userId = UUID.fromString((String) event.get("userId"));
            String title = (String) event.get("title");
            String message = (String) event.get("message");
            NotificationType type = NotificationType.valueOf((String) event.get("type"));
            
            var user = userService.findUserById(userId);
            notificationService.createNotification(user, title, message, type);
            log.info("Notification created for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to handle notification event", e);
        }
    }
}
