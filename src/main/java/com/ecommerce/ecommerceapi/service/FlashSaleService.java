package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.dto.FlashSaleDTO;
import com.ecommerce.ecommerceapi.dto.FlashSaleItemDTO;
import com.ecommerce.ecommerceapi.entity.FlashSale;
import com.ecommerce.ecommerceapi.entity.FlashSaleItem;
import com.ecommerce.ecommerceapi.entity.Product;
import com.ecommerce.ecommerceapi.exception.BadRequestException;
import com.ecommerce.ecommerceapi.exception.ResourceNotFoundException;
import com.ecommerce.ecommerceapi.repository.FlashSaleItemRepository;
import com.ecommerce.ecommerceapi.repository.FlashSaleRepository;
import com.ecommerce.ecommerceapi.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FlashSaleService {

    @Autowired
    private FlashSaleRepository flashSaleRepository;

    @Autowired
    private FlashSaleItemRepository flashSaleItemRepository;

    @Autowired
    private ProductRepository productRepository;

    public FlashSale createFlashSale(FlashSaleDTO dto) {
        FlashSale fs = FlashSale.builder()
                .name(dto.getName())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
        return flashSaleRepository.save(fs);
    }

    public FlashSaleItem addProductToFlashSale(Integer flashSaleId, FlashSaleItemDTO itemDto) {
        FlashSale fs = flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương trình Flash Sale!"));
        Product product = productRepository.findById(itemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm!"));

        if (flashSaleItemRepository.findByFlashSaleIdAndProductId(flashSaleId, product.getId()).isPresent()) {
            throw new BadRequestException("Sản phẩm đã tồn tại trong chương trình Flash Sale này!");
        }

        FlashSaleItem item = FlashSaleItem.builder()
                .flashSale(fs)
                .product(product)
                .salePrice(itemDto.getSalePrice())
                .saleQuantity(itemDto.getSaleQuantity())
                .build();

        return flashSaleItemRepository.save(item);
    }

    public List<FlashSaleDTO> getActiveFlashSales() {
        LocalDateTime now = LocalDateTime.now();
        List<FlashSale> activeSales = flashSaleRepository.findByActiveTrueAndStartTimeBeforeAndEndTimeAfter(now, now);

        return activeSales.stream().map(fs -> {
            List<FlashSaleItemDTO> items = flashSaleItemRepository.findByFlashSaleId(fs.getId()).stream()
                    .map(this::convertToItemDTO)
                    .collect(Collectors.toList());

            return FlashSaleDTO.builder()
                    .id(fs.getId())
                    .name(fs.getName())
                    .startTime(fs.getStartTime())
                    .endTime(fs.getEndTime())
                    .active(fs.isActive())
                    .items(items)
                    .build();
        }).collect(Collectors.toList());
    }

    public FlashSaleItemDTO convertToItemDTO(FlashSaleItem item) {
        return FlashSaleItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImageUrl(item.getProduct().getImageUrl())
                .salePrice(item.getSalePrice())
                .saleQuantity(item.getSaleQuantity())
                .soldCount(item.getSoldCount())
                .build();
    }
}
