package com.bayraktolga.BayrakBackend.event;

import com.bayraktolga.BayrakBackend.service.NotificationService;
import com.bayraktolga.BayrakBackend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private static final String NOTIFICATION_QUEUE = "notification:queue";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final UserService userService;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean running = new AtomicBoolean(false);

    @PostConstruct
    public void startConsuming() {
        running.set(true);
        executor.submit(this::consume);
        log.info("Notification consumer started");
    }

    @PreDestroy
    public void stopConsuming() {
        running.set(false);
        executor.shutdown();
        log.info("Notification consumer stopped");
    }

    private void consume() {
        while (running.get()) {
            try {
                String json = redisTemplate.opsForList().leftPop(NOTIFICATION_QUEUE);
                if (json != null) {
                    processMessage(json);
                } else {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error consuming notification message", e);
            }
        }
    }

    private void processMessage(String json) {
        try {
            NotificationEvent event = objectMapper.readValue(json, NotificationEvent.class);
            var user = userService.findUserById(event.getUserId());
            notificationService.createNotification(user, event.getTitle(), event.getMessage(), event.getType());
            log.info("Notification processed for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to process notification event: {}", json, e);
        }
    }
}
