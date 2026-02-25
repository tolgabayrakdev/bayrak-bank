package com.bayraktolga.BayrakBackend.event;

import com.bayraktolga.BayrakBackend.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID userId;
    private String title;
    private String message;
    private NotificationType type;
}


// Event modeli