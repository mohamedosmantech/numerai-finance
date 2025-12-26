package com.fincalc.adapter.in.web;

import com.fincalc.adapter.out.persistence.entity.LegalPageEntity;
import com.fincalc.adapter.out.persistence.repository.LegalPageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.format.DateTimeFormatter;

/**
 * Controller for legal pages (Privacy Policy, Terms of Service).
 * Content is stored in database and editable via admin dashboard.
 */
@Controller
@RequiredArgsConstructor
public class LegalController {

    private final LegalPageRepository legalPageRepository;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    @GetMapping("/privacy")
    public String privacy(Model model) {
        return showLegalPage("privacy", model);
    }

    @GetMapping("/terms")
    public String terms(Model model) {
        return showLegalPage("terms", model);
    }

    @GetMapping("/legal/{slug}")
    public String legalPage(@PathVariable String slug, Model model) {
        return showLegalPage(slug, model);
    }

    private String showLegalPage(String slug, Model model) {
        LegalPageEntity page = legalPageRepository.findById(slug)
                .orElse(createDefaultPage(slug));

        model.addAttribute("title", page.getTitle());
        model.addAttribute("content", page.getContent());
        model.addAttribute("lastUpdated", page.getLastUpdated() != null
                ? page.getLastUpdated().format(DATE_FORMAT)
                : "December 26, 2025");

        return "legal/page";
    }

    private LegalPageEntity createDefaultPage(String slug) {
        LegalPageEntity page = new LegalPageEntity();
        page.setSlug(slug);
        page.setTitle(slug.substring(0, 1).toUpperCase() + slug.substring(1));
        page.setContent("<p>Content not found.</p>");
        return page;
    }
}
