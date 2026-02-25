package com.bayraktolga.BayrakBackend.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationQueuePublisher {

    private static final String NOTIFICATION_QUEUE = "notification:queue";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(NotificationEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            redisTemplate.opsForList().rightPush(NOTIFICATION_QUEUE, json);
            log.info("Notification event published to Redis queue: {}", event.getTitle());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize notification event", e);
            throw new RuntimeException("Failed to publish notification event", e);
        }
    }
}
