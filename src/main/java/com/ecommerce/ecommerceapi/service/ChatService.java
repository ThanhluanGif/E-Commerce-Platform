package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.dto.ChatConversationDTO;
import com.ecommerce.ecommerceapi.dto.ChatMessageDTO;
import com.ecommerce.ecommerceapi.entity.ChatConversation;
import com.ecommerce.ecommerceapi.entity.ChatMessage;
import com.ecommerce.ecommerceapi.entity.Shop;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.exception.BadRequestException;
import com.ecommerce.ecommerceapi.exception.ResourceNotFoundException;
import com.ecommerce.ecommerceapi.repository.ChatConversationRepository;
import com.ecommerce.ecommerceapi.repository.ChatMessageRepository;
import com.ecommerce.ecommerceapi.repository.ShopRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    @Autowired
    private ChatConversationRepository conversationRepository;

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    public ChatConversation getOrCreateConversation(Integer buyerId, Integer shopId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người mua!"));
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy shop!"));

        if (shop.getUser().getId().equals(buyerId)) {
            throw new BadRequestException("Bạn không thể chat với chính gian hàng của mình!");
        }

        return conversationRepository.findByBuyerIdAndShopId(buyerId, shopId)
                .orElseGet(() -> {
                    ChatConversation conv = ChatConversation.builder()
                            .buyer(buyer)
                            .seller(shop.getUser())
                            .shop(shop)
                            .lastMessage("Bắt đầu cuộc trò chuyện")
                            .lastMessageAt(LocalDateTime.now())
                            .build();
                    return conversationRepository.save(conv);
                });
    }

    public ChatMessage sendMessage(Integer conversationId, Integer senderId, String content) {
        ChatConversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hội thoại!"));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người gửi!"));

        ChatMessage msg = ChatMessage.builder()
                .conversation(conv)
                .sender(sender)
                .content(content)
                .build();

        ChatMessage saved = messageRepository.save(msg);

        conv.setLastMessage(content);
        conv.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conv);

        return saved;
    }

    public List<ChatMessageDTO> getMessages(Integer conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(this::convertToMessageDTO)
                .collect(Collectors.toList());
    }

    public List<ChatConversationDTO> getConversations(Integer userId, boolean isSeller) {
        List<ChatConversation> list;
        if (isSeller) {
            list = conversationRepository.findBySellerIdOrderByLastMessageAtDesc(userId);
        } else {
            list = conversationRepository.findByBuyerIdOrderByLastMessageAtDesc(userId);
        }
        return list.stream()
                .map(this::convertToConversationDTO)
                .collect(Collectors.toList());
    }

    public ChatConversationDTO convertToConversationDTO(ChatConversation conv) {
        return ChatConversationDTO.builder()
                .id(conv.getId())
                .buyerId(conv.getBuyer().getId())
                .buyerUsername(conv.getBuyer().getUsername())
                .buyerAvatarUrl(conv.getBuyer().getAvatarUrl())
                .sellerId(conv.getSeller().getId())
                .shopId(conv.getShop().getId())
                .shopName(conv.getShop().getName())
                .shopLogoUrl(conv.getShop().getLogoUrl())
                .lastMessage(conv.getLastMessage())
                .lastMessageAt(conv.getLastMessageAt())
                .build();
    }

    public ChatMessageDTO convertToMessageDTO(ChatMessage msg) {
        return ChatMessageDTO.builder()
                .id(msg.getId())
                .conversationId(msg.getConversation().getId())
                .senderId(msg.getSender().getId())
                .senderUsername(msg.getSender().getUsername())
                .content(msg.getContent())
                .read(msg.isRead())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}
