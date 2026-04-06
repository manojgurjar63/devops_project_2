package com.societyshops.web;

import com.societyshops.enums.OrderStatus;
import com.societyshops.service.OrderService;
import com.societyshops.dto.InventoryRequest;
import com.societyshops.dto.ShopRequest;
import com.societyshops.entity.Shop;
import com.societyshops.entity.User;
import com.societyshops.repository.UserRepository;
import com.societyshops.service.InventoryService;
import com.societyshops.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/shopkeeper")
@RequiredArgsConstructor
public class ShopkeeperWebController {

    private final ShopService shopService;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    // ── Dashboard: list shopkeeper's own shops ────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        var shops = shopService.getMyShops(userId);
        model.addAttribute("shops", shops);
        model.addAttribute("approvedCount", shops.stream().filter(s -> Boolean.TRUE.equals(s.getIsApproved())).count());
        model.addAttribute("pendingCount",  shops.stream().filter(s -> !Boolean.TRUE.equals(s.getIsApproved())).count());
        model.addAttribute("shopRequest", new ShopRequest());
        return "shopkeeper/dashboard";
    }

    @PostMapping("/shops/{id}/edit")
    public String editShop(@PathVariable Long id,
                           @ModelAttribute ShopRequest request,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes ra) {
        shopService.updateShop(id, request, getUserId(userDetails));
        ra.addFlashAttribute("success", "Shop updated successfully!");
        return "redirect:/web/shopkeeper/dashboard";
    }
    @PostMapping("/shops/register")
    public String registerShop(@ModelAttribute ShopRequest request,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes ra) {
        shopService.registerShop(request, getUserId(userDetails));
        ra.addFlashAttribute("success", "Shop registered! Waiting for admin approval.");
        return "redirect:/web/shopkeeper/dashboard";
    }

    // ── Toggle shop OPEN/CLOSED ───────────────────────────────────────────────
    @PostMapping("/shops/{id}/toggle")
    public String toggleStatus(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes ra) {
        Shop shop = shopService.toggleStatus(id, getUserId(userDetails));
        ra.addFlashAttribute("success", "Shop is now " + shop.getStatus());
        return "redirect:/web/shopkeeper/dashboard";
    }

    // ── Inventory page for a shop ─────────────────────────────────────────────
    @GetMapping("/shops/{shopId}/inventory")
    public String inventoryPage(@PathVariable Long shopId, Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {
        var items = inventoryService.getAllItems(shopId);
        model.addAttribute("items", items);
        model.addAttribute("availableCount",   items.stream().filter(i -> Boolean.TRUE.equals(i.getIsAvailable())).count());
        model.addAttribute("unavailableCount", items.stream().filter(i -> !Boolean.TRUE.equals(i.getIsAvailable())).count());
        model.addAttribute("shopId", shopId);
        model.addAttribute("itemRequest", new InventoryRequest());
        return "shopkeeper/inventory";
    }

    // ── Add inventory item ────────────────────────────────────────────────────
    @PostMapping("/shops/{shopId}/inventory/add")
    public String addItem(@PathVariable Long shopId,
                          @ModelAttribute InventoryRequest request,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes ra) {
        inventoryService.addItem(shopId, request, getUserId(userDetails));
        ra.addFlashAttribute("success", "Item added successfully!");
        return "redirect:/web/shopkeeper/shops/" + shopId + "/inventory";
    }

    @PostMapping("/inventory/{itemId}/edit")
    public String editItem(@PathVariable Long itemId,
                           @ModelAttribute InventoryRequest request,
                           @RequestParam Long shopId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes ra) {
        inventoryService.updateItem(itemId, request, getUserId(userDetails));
        ra.addFlashAttribute("success", "Item updated!");
        return "redirect:/web/shopkeeper/shops/" + shopId + "/inventory";
    }

    // ── Toggle item availability ──────────────────────────────────────────────
    @PostMapping("/inventory/{itemId}/toggle")
    public String toggleItem(@PathVariable Long itemId,
                             @RequestParam Long shopId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes ra) {
        inventoryService.toggleAvailability(itemId, getUserId(userDetails));
        ra.addFlashAttribute("success", "Item availability updated!");
        return "redirect:/web/shopkeeper/shops/" + shopId + "/inventory";
    }

    // ── Delete inventory item ─────────────────────────────────────────────────
    @PostMapping("/inventory/{itemId}/delete")
    public String deleteItem(@PathVariable Long itemId,
                             @RequestParam Long shopId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes ra) {
        inventoryService.deleteItem(itemId, getUserId(userDetails));
        ra.addFlashAttribute("success", "Item deleted.");
        return "redirect:/web/shopkeeper/shops/" + shopId + "/inventory";
    }

    @GetMapping("/shops/{shopId}/orders")
    public String shopOrders(@PathVariable Long shopId, Model model) {
        var orders = orderService.getShopOrders(shopId);
        model.addAttribute("orders", orders);
        model.addAttribute("shopId", shopId);
        model.addAttribute("pendingCount",   orders.stream().filter(o -> o.getStatus().name().equals("PENDING")).count());
        model.addAttribute("confirmedCount", orders.stream().filter(o -> o.getStatus().name().equals("CONFIRMED")).count());
        return "shopkeeper/orders";
    }

    @PostMapping("/orders/{orderId}/status")
    public String updateOrderStatus(@PathVariable Long orderId,
                                    @RequestParam String status,
                                    @RequestParam Long shopId,
                                    RedirectAttributes ra) {
        orderService.updateStatus(orderId, OrderStatus.valueOf(status));
        ra.addFlashAttribute("success", "Order status updated to " + status);
        return "redirect:/web/shopkeeper/shops/" + shopId + "/orders";
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}
