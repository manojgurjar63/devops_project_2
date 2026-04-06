package com.societyshops.dto;

import lombok.Data;
import java.util.List;

import com.societyshops.enums.PaymentMethod;

@Data
public class OrderRequest {
    private Long shopId;
    private String paymentMethod;
    private String deliveryAddress;
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long inventoryId;
        private Integer quantity;
    }
}
