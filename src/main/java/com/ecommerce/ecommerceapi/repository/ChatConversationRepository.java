package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Integer> {
    List<ChatConversation> findByBuyerIdOrderByLastMessageAtDesc(Integer buyerId);
    List<ChatConversation> findBySellerIdOrderByLastMessageAtDesc(Integer sellerId);
    Optional<ChatConversation> findByBuyerIdAndShopId(Integer buyerId, Integer shopId);
}
