package com.societyshops.web;

import com.societyshops.enums.Role;
import com.societyshops.repository.UserRepository;
import com.societyshops.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/admin")
@RequiredArgsConstructor
public class AdminWebController {

    private final ShopService shopService;
    private final UserRepository userRepository;

    // Admin dashboard → shows pending shops
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pendingShops", shopService.getPendingShops());
        model.addAttribute("allShops", shopService.getApprovedShops());
        model.addAttribute("shopkeepers", userRepository.findByRole(Role.SHOPKEEPER));
        return "admin/dashboard";
    }

    // Approve a shop
    @PostMapping("/shops/{id}/approve")
    public String approveShop(@PathVariable Long id, RedirectAttributes ra) {
        shopService.approveShop(id);
        ra.addFlashAttribute("success", "Shop approved successfully!");
        return "redirect:/web/admin/dashboard";
    }

    // Reject a shop
    @PostMapping("/shops/{id}/reject")
    public String rejectShop(@PathVariable Long id, RedirectAttributes ra) {
        shopService.rejectShop(id);
        ra.addFlashAttribute("success", "Shop rejected and removed.");
        return "redirect:/web/admin/dashboard";
    }
}
