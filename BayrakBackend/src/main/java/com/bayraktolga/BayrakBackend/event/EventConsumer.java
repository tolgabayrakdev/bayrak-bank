package com.bayraktolga.BayrakBackend.event;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class EventConsumer {

    private static final String EVENT_QUEUE = "event:queue";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final List<EventHandler> handlers;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public EventConsumer(StringRedisTemplate redisTemplate, 
                        ObjectMapper objectMapper, 
                        List<EventHandler> handlers) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.handlers = handlers;
    }

    @PostConstruct
    public void startConsuming() {
        running.set(true);
        executor.submit(this::consume);
        log.info("Event consumer started with {} handlers", handlers.size());
    }

    @PreDestroy
    public void stopConsuming() {
        running.set(false);
        executor.shutdown();
        log.info("Event consumer stopped");
    }

    private void consume() {
        while (running.get()) {
            try {
                String json = redisTemplate.opsForList().leftPop(EVENT_QUEUE);
                if (json != null) {
                    processMessage(json);
                } else {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error consuming event", e);
            }
        }
    }

    private void processMessage(String json) {
        try {
            AppEvent event = objectMapper.readValue(json, AppEvent.class);
            
            for (EventHandler handler : handlers) {
                if (handler.getEventType().equals(event.type())) {
                    handler.handle(event.payload());
                    log.info("Event handled: type={}", event.type());
                    return;
                }
            }
            
            log.warn("No handler found for event type: {}", event.type());
            
        } catch (Exception e) {
            log.error("Failed to process event: {}", json, e);
        }
    }
}
