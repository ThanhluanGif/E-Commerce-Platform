package com.ecommerce.ecommerceapi.dto;

import com.ecommerce.ecommerceapi.entity.VoucherType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherDTO {
    private Integer id;
    private String code;
    private VoucherType type;
    private BigDecimal value;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;
    private Integer usageLimit;
    private Integer usedCount;
    private String scope;
    private Integer shopId;
    private String shopName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active;
}
