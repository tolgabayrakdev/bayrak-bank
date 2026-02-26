package com.bayraktolga.BayrakBackend.service;

import com.bayraktolga.BayrakBackend.dto.response.NotificationResponse;
import com.bayraktolga.BayrakBackend.dto.response.PageResponse;
import com.bayraktolga.BayrakBackend.entity.Notification;
import com.bayraktolga.BayrakBackend.entity.User;
import com.bayraktolga.BayrakBackend.enums.NotificationType;
import com.bayraktolga.BayrakBackend.event.EventPublisher;
import com.bayraktolga.BayrakBackend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EventPublisher eventPublisher;

    public void publishNotification(UUID userId, String title, String message, NotificationType type) {
        Map<String, Object> event = new HashMap<>();
        event.put("userId", userId.toString());
        event.put("title", title);
        event.put("message", message);
        event.put("type", type.name());
        eventPublisher.publish("NOTIFICATION", event);
    }

    @Transactional
    public void createNotification(User user, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotificationsByUser(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return new PageResponse<>(
                notificationPage.getContent().stream().map(this::toResponse).toList(),
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages(),
                notificationPage.isLast()
        );
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Bildirim bulunamadı."));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bu bildirimi okuma yetkiniz yok.");
        }

        notification.setIsRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getUser().getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getIsRead(),
                n.getCreatedAt()
        );
    }
}
