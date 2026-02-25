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
public class EventPublisher {

    private static final String EVENT_QUEUE = "event:queue";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String type, Object payload) {
        try {
            AppEvent event = new AppEvent(type, payload);
            String json = objectMapper.writeValueAsString(event);
            redisTemplate.opsForList().rightPush(EVENT_QUEUE, json);
            log.info("Event published: type={}", type);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event", e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}
