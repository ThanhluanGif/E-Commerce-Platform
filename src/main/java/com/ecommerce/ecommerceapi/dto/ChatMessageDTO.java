package com.ecommerce.ecommerceapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Integer id;
    private Integer conversationId;
    private Integer senderId;
    private String senderUsername;
    private String content;
    private Boolean read;
    private LocalDateTime createdAt;
}
