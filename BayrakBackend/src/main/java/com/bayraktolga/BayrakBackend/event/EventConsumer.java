package com.bayraktolga.BayrakBackend.event;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class EventConsumer {

    private static final String EVENT_QUEUE = "event:queue";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationContext applicationContext;

    private final Map<String, EventMethod> handlers = new HashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public EventConsumer(StringRedisTemplate redisTemplate, 
                        ObjectMapper objectMapper,
                        ApplicationContext applicationContext) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        registerHandlers();
        running.set(true);
        executor.submit(this::consume);
        log.info("Event consumer started with {} handlers: {}", handlers.size(), handlers.keySet());
    }

    private void registerHandlers() {
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(org.springframework.stereotype.Service.class);
        
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            for (Method method : bean.getClass().getDeclaredMethods()) {
                ListenEvent annotation = method.getAnnotation(ListenEvent.class);
                if (annotation != null) {
                    method.setAccessible(true);
                    handlers.put(annotation.value(), new EventMethod(bean, method));
                    log.info("Registered handler: {} -> {}.{}()", annotation.value(), beanName, method.getName());
                }
            }
        }
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
            
            EventMethod handler = handlers.get(event.type());
            if (handler != null) {
                handler.invoke(event.payload());
                log.info("Event handled: type={}", event.type());
            } else {
                log.warn("No handler found for event type: {}", event.type());
            }
            
        } catch (Exception e) {
            log.error("Failed to process event: {}", json, e);
        }
    }

    private record EventMethod(Object bean, Method method) {
        public void invoke(Object payload) throws Exception {
            method.invoke(bean, payload);
        }
    }
}
