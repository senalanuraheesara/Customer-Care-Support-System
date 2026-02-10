package lk.sliit.customer_care_system.controller;

import lk.sliit.customer_care_system.modelentity.FAQ;
import lk.sliit.customer_care_system.modelentity.User;
import lk.sliit.customer_care_system.repository.FAQRepository;
import lk.sliit.customer_care_system.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/agent/faq")
@PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
public class FAQController {

    private final FAQRepository faqRepository;
    private final UserRepository userRepository;

    public FAQController(FAQRepository faqRepository, UserRepository userRepository) {
        this.faqRepository = faqRepository;
        this.userRepository = userRepository;
    }

    // Display all FAQ articles with optional category filtering
    @GetMapping
    public String manageFAQ(@RequestParam(value = "category", required = false) String category, Model model) {
        List<FAQ> faqs;
        List<String> categories = faqRepository.findAllCategories();

        if (category != null && !category.trim().isEmpty() && !category.equals("all")) {
            faqs = faqRepository.findByCategoryIgnoreCase(category);
        } else {
            faqs = faqRepository.findAllByOrderByCategoryAscCreatedAtDesc();
        }

        model.addAttribute("faqs", faqs);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        return "agent-faq-management";
    }

    // Show form for adding new FAQ
    @GetMapping("/add")
    public String showAddFAQForm(Model model) {
        model.addAttribute("faq", new FAQ());
        model.addAttribute("categories", faqRepository.findAllCategories());
        model.addAttribute("isEdit", false);
        return "agent-faq-form";
    }

    // Handle adding new FAQ
    @PostMapping("/add")
    public String addFAQ(@Valid @ModelAttribute FAQ faq, BindingResult result,
                         RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", faqRepository.findAllCategories());
            model.addAttribute("isEdit", false);
            return "agent-faq-form";
        }

        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            faq.setCreatedBy(currentUser);
            faq.setIsApproved(false); // Requires admin approval
            faqRepository.save(faq);
            redirectAttributes.addFlashAttribute("successMessage", "FAQ article submitted for approval!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding FAQ article. Please try again.");
        }

        return "redirect:/agent/faq";
    }

    // Show form for editing existing FAQ
    @GetMapping("/edit/{id}")
    public String showEditFAQForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        FAQ faq = faqRepository.findById(id).orElse(null);

        if (faq == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "FAQ article not found.");
            return "redirect:/agent/faq";
        }

        model.addAttribute("faq", faq);
        model.addAttribute("categories", faqRepository.findAllCategories());
        model.addAttribute("isEdit", true);
        return "agent-faq-form";
    }

    // Handle editing existing FAQ
    @PostMapping("/edit/{id}")
    public String editFAQ(@PathVariable Long id, @Valid @ModelAttribute FAQ faq,
                          BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", faqRepository.findAllCategories());
            model.addAttribute("isEdit", true);
            return "agent-faq-form";
        }

        try {
            FAQ existingFAQ = faqRepository.findById(id).orElse(null);
            if (existingFAQ == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "FAQ article not found.");
                return "redirect:/agent/faq";
            }

            // Update the existing FAQ
            existingFAQ.setCategory(faq.getCategory());
            existingFAQ.setQuestion(faq.getQuestion());
            existingFAQ.setAnswer(faq.getAnswer());

            faqRepository.save(existingFAQ);
            redirectAttributes.addFlashAttribute("successMessage", "FAQ article updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating FAQ article. Please try again.");
        }

        return "redirect:/agent/faq";
    }

    // Delete FAQ article
    @PostMapping("/delete/{id}")
    public String deleteFAQ(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            FAQ faq = faqRepository.findById(id).orElse(null);
            if (faq == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "FAQ article not found.");
                return "redirect:/agent/faq";
            }

            faqRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "FAQ article deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting FAQ article. Please try again.");
        }

        return "redirect:/agent/faq";
    }

    // Search FAQs by keyword
    @GetMapping("/search")
    public String searchFAQ(@RequestParam("keyword") String keyword,
                            @RequestParam(value = "category", required = false) String category,
                            Model model) {
        List<FAQ> faqs;
        List<String> categories = faqRepository.findAllCategories();

        if (category != null && !category.trim().isEmpty() && !category.equals("all")) {
            faqs = faqRepository.findByCategoryAndKeyword(category, keyword);
        } else {
            faqs = faqRepository.findByKeyword(keyword);
        }

        model.addAttribute("faqs", faqs);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchKeyword", keyword);
        return "agent-faq-management";
    }

    // API endpoint to get categories (for AJAX requests)
    @GetMapping("/categories")
    @ResponseBody
    public List<String> getCategories() {
        return faqRepository.findAllCategories();
    }

    // API endpoint to get FAQs by category (for AJAX requests)
    @GetMapping("/by-category")
    @ResponseBody
    public List<FAQ> getFAQsByCategory(@RequestParam String category) {
        if (category.equals("all")) {
            return faqRepository.findAllByOrderByCategoryAscCreatedAtDesc();
        }
        return faqRepository.findByCategoryIgnoreCase(category);
    }
}
