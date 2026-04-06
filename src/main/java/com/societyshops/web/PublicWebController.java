package com.societyshops.web;

import com.societyshops.service.InventoryService;
import com.societyshops.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class PublicWebController {

    private final ShopService shopService;
    private final InventoryService inventoryService;

    @GetMapping("/")
    public String root() { return "redirect:/shops"; }

    @GetMapping("/shops")
    public String shops(Model model,
                        @RequestParam(defaultValue = "") String search,
                        @RequestParam(defaultValue = "0") int page) {
        var shopPage = shopService.getApprovedShops(search, page);
        model.addAttribute("shops", shopPage.getContent());
        model.addAttribute("totalPages", shopPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);
        return "public/shops";
    }

    @GetMapping("/shops/{shopId}/inventory")
    public String inventory(@PathVariable Long shopId, Model model) {
        model.addAttribute("shop", shopService.getShopById(shopId));
        model.addAttribute("items", inventoryService.getAvailableItems(shopId));
        return "public/inventory";
    }
}
