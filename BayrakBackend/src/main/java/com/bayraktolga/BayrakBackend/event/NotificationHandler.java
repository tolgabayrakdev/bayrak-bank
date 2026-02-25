package com.bayraktolga.BayrakBackend.event;

import com.bayraktolga.BayrakBackend.service.NotificationService;
import com.bayraktolga.BayrakBackend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationHandler implements EventHandler {

    public static final String EVENT_TYPE = "NOTIFICATION";

    private final NotificationService notificationService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public void handle(Object payload) {
        try {
            NotificationEvent event = objectMapper.convertValue(payload, NotificationEvent.class);
            var user = userService.findUserById(event.getUserId());
            notificationService.createNotification(user, event.getTitle(), event.getMessage(), event.getType());
            log.info("Notification created for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to handle notification event", e);
        }
    }
}
