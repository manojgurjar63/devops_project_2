package com.societyshops.web;

import com.societyshops.dto.OrderRequest;
import com.societyshops.service.OrderService;
import com.societyshops.entity.User;
import com.societyshops.repository.UserRepository;
import com.societyshops.service.FavoriteService;
import com.societyshops.service.InventoryService;
import com.societyshops.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/resident")
@RequiredArgsConstructor
public class ResidentWebController {

    private final ShopService shopService;
    private final InventoryService inventoryService;
    private final FavoriteService favoriteService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            @RequestParam(defaultValue = "") String search,
                            @RequestParam(defaultValue = "0") int page,
                            @AuthenticationPrincipal UserDetails userDetails) {
        var shopPage = shopService.getApprovedShops(search, page);
        var favorites = favoriteService.getMyFavorites(getUserId(userDetails));
        var orders = orderService.getMyOrders(getUserId(userDetails));
        model.addAttribute("shops", shopPage.getContent());
        model.addAttribute("favorites", favorites);
        model.addAttribute("openCount", shopPage.getContent().stream().filter(s -> s.getStatus().name().equals("OPEN")).count());
        model.addAttribute("totalPages", shopPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);
        model.addAttribute("orderCount", orders.size());
        model.addAttribute("pendingOrderCount", orders.stream().filter(o -> o.getStatus().name().equals("PENDING")).count());
        return "resident/dashboard";
    }

    @GetMapping("/shops/{shopId}/inventory")
    public String inventory(@PathVariable Long shopId, Model model) {
        model.addAttribute("items", inventoryService.getAvailableItems(shopId));
        model.addAttribute("shopId", shopId);
        model.addAttribute("shop", shopService.getShopById(shopId));
        return "resident/inventory";
    }

    @PostMapping("/favorites/{shopId}/add")
    public String addFavorite(@PathVariable Long shopId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes ra) {
        try {
            favoriteService.addFavorite(getUserId(userDetails), shopId);
            ra.addFlashAttribute("success", "Added to favorites!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/resident/dashboard";
    }

    @PostMapping("/favorites/{shopId}/remove")
    public String removeFavorite(@PathVariable Long shopId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes ra) {
        favoriteService.removeFavorite(getUserId(userDetails), shopId);
        ra.addFlashAttribute("success", "Removed from favorites.");
        return "redirect:/web/resident/dashboard";
    }

    @PostMapping("/orders/place")
    @ResponseBody
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            var order = orderService.placeOrder(getUserId(userDetails), request);
            return ResponseEntity.ok(java.util.Map.of("orderId", order.getId(), "total", order.getTotalAmount()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/orders/{orderId}/paid")
    @ResponseBody
    public ResponseEntity<?> markPaid(@PathVariable Long orderId,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        try {
            orderService.markPaymentDone(orderId, getUserId(userDetails));
            return ResponseEntity.ok(java.util.Map.of("message", "Payment marked"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/orders/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        try {
            orderService.cancelOrder(orderId, getUserId(userDetails));
            return ResponseEntity.ok(java.util.Map.of("message", "Order cancelled"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/orders")
    public String myOrders(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("orders", orderService.getMyOrders(getUserId(userDetails)));
        return "resident/orders";
    }

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}
