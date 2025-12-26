package com.fincalc.adapter.in.admin;

import com.fincalc.adapter.out.persistence.entity.LegalPageEntity;
import com.fincalc.adapter.out.persistence.repository.LegalPageRepository;
import com.fincalc.application.AnalyticsService;
import com.fincalc.domain.port.out.ConfigurationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ConfigurationPort configurationPort;
    private final LegalPageRepository legalPageRepository;
    private final AnalyticsService analyticsService;

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails user) {
        model.addAttribute("username", user != null ? user.getUsername() : "Admin");
        model.addAttribute("countries", configurationPort.getAllCountries());
        model.addAttribute("currencies", configurationPort.getAllCurrencies());
        model.addAttribute("rateProviders", configurationPort.getAllRateProviders());
        model.addAttribute("messages", configurationPort.getAllMessages());
        model.addAttribute("templates", configurationPort.getAllTemplates());
        return "admin/dashboard";
    }

    @GetMapping("/countries")
    public String countries(Model model, @AuthenticationPrincipal UserDetails user) {
        model.addAttribute("username", user != null ? user.getUsername() : "Admin");
        model.addAttribute("countries", configurationPort.getAllCountries());
        return "admin/countries";
    }

    @GetMapping("/currencies")
    public String currencies(Model model, @AuthenticationPrincipal UserDetails user) {
        model.addAttribute("username", user != null ? user.getUsername() : "Admin");
        model.addAttribute("currencies", configurationPort.getAllCurrencies());
        return "admin/currencies";
    }

    @GetMapping("/messages")
    public String messages(Model model, @AuthenticationPrincipal UserDetails user) {
        model.addAttribute("username", user != null ? user.getUsername() : "Admin");
        model.addAttribute("messages", configurationPort.getAllMessages());
        return "admin/messages";
    }

    @GetMapping("/rate-providers")
    public String rateProviders(Model model, @AuthenticationPrincipal UserDetails user) {
        model.addAttribute("username", user != null ? user.getUsername() : "Admin");
        model.addAttribute("rateProviders", configurationPort.getAllRateProviders());
        return "admin/rate-providers";
    }

    @GetMapping("/legal")
    public String legalPages(Model model, @AuthenticationPrincipal UserDetails user) {
        model.addAttribute("username", user != null ? user.getUsername() : "Admin");
        model.addAttribute("pages", legalPageRepository.findAll());
        return "admin/legal-pages";
    }

    @GetMapping("/legal/{slug}")
    public String editLegalPage(@PathVariable String slug, Model model, @AuthenticationPrincipal UserDetails user) {
        model.addAttribute("username", user != null ? user.getUsername() : "Admin");
        LegalPageEntity page = legalPageRepository.findById(slug)
                .orElse(new LegalPageEntity(slug, slug.substring(0, 1).toUpperCase() + slug.substring(1), "", null));
        model.addAttribute("page", page);
        return "admin/legal-edit";
    }

    @PostMapping("/legal/{slug}")
    public String saveLegalPage(@PathVariable String slug,
                                @RequestParam String title,
                                @RequestParam String content,
                                RedirectAttributes redirectAttributes) {
        LegalPageEntity page = legalPageRepository.findById(slug)
                .orElse(new LegalPageEntity());
        page.setSlug(slug);
        page.setTitle(title);
        page.setContent(content);
        legalPageRepository.save(page);
        redirectAttributes.addFlashAttribute("success", "Page saved successfully!");
        return "redirect:/admin/legal";
    }

    @GetMapping("/analytics")
    public String analytics(Model model, @AuthenticationPrincipal UserDetails user) {
        model.addAttribute("username", user != null ? user.getUsername() : "Admin");
        model.addAttribute("analytics", analyticsService.getDashboardSummary());
        model.addAttribute("toolTrend", analyticsService.getDailyTrend("tool", 7));
        model.addAttribute("sessionTrend", analyticsService.getDailyTrend("mcp", 7));
        return "admin/analytics";
    }
}
