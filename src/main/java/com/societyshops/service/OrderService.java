package com.societyshops.service;

import com.societyshops.dto.OrderRequest;
import com.societyshops.entity.*;
import com.societyshops.enums.OrderStatus;
import com.societyshops.enums.PaymentMethod;
import com.societyshops.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public Order placeOrder(Long residentId, OrderRequest request) {
        User resident = userRepository.findById(residentId).orElseThrow();
        Shop shop = shopRepository.findById(request.getShopId()).orElseThrow();

        Order order = Order.builder()
                .resident(resident)
                .shop(shop)
                .status(OrderStatus.PENDING)
                .paymentMethod(request.getPaymentMethod() != null ?
                    PaymentMethod.valueOf(request.getPaymentMethod()) : PaymentMethod.COD)
                .paymentDone(false)
                .totalAmount(BigDecimal.ZERO)
                .deliveryAddress(request.getDeliveryAddress())
                .build();

        List<OrderItem> orderItems = request.getItems().stream().map(req -> {
            Inventory inv = inventoryRepository.findById(req.getInventoryId()).orElseThrow();
            return OrderItem.builder()
                    .order(order)
                    .inventory(inv)
                    .quantity(req.getQuantity())
                    .price(inv.getPrice())
                    .build();
        }).toList();

        BigDecimal total = orderItems.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setItems(orderItems);
        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    public List<Order> getMyOrders(Long residentId) {
        return orderRepository.findByResidentIdOrderByCreatedAtDesc(residentId);
    }

    public List<Order> getShopOrders(Long shopId) {
        return orderRepository.findByShopIdOrderByCreatedAtDesc(shopId);
    }

    @Transactional
    public void markPaymentDone(Long orderId, Long residentId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (!order.getResident().getId().equals(residentId))
            throw new RuntimeException("Not your order");
        order.setPaymentDone(true);
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId, Long residentId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (!order.getResident().getId().equals(residentId))
            throw new RuntimeException("Not your order");
        if (order.getStatus() != OrderStatus.PENDING)
            throw new RuntimeException("Only pending orders can be cancelled");
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional
    public void updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(status);
        orderRepository.save(order);
    }
}
