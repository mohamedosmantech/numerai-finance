package com.fincalc.adapter.in.admin;

import com.fincalc.domain.port.out.ConfigurationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ConfigurationPort configurationPort;

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
}
