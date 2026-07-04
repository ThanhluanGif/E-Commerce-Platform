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
public class ChatConversationDTO {
    private Integer id;
    private Integer buyerId;
    private String buyerUsername;
    private String buyerAvatarUrl;
    private Integer sellerId;
    private Integer shopId;
    private String shopName;
    private String shopLogoUrl;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
}
