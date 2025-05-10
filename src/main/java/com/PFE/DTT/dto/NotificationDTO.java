package com.PFE.DTT.dto;

import com.PFE.DTT.model.NotificationType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class NotificationDTO {
    private String description;
    private String link;
    private Long userId;
    private NotificationType notificationType;

}
