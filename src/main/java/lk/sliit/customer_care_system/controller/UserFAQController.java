package lk.sliit.customer_care_system.controller;

import lk.sliit.customer_care_system.modelentity.FAQ;
import lk.sliit.customer_care_system.repository.FAQRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/faq")
public class UserFAQController {

    private final FAQRepository faqRepository;

    public UserFAQController(FAQRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    // Display all FAQ articles for users (public access) - Only approved FAQs
    @GetMapping
    public String viewFAQs(@RequestParam(value = "category", required = false) String category,
                           @RequestParam(value = "search", required = false) String search,
                           Model model) {
        List<FAQ> faqs;
        List<String> categories = faqRepository.findApprovedCategories();

        // Handle search and filtering - Only show approved FAQs
        if (search != null && !search.trim().isEmpty()) {
            if (category != null && !category.trim().isEmpty() && !category.equals("all")) {
                faqs = faqRepository.findApprovedByCategoryAndKeyword(category, search);
            } else {
                faqs = faqRepository.findApprovedByKeyword(search);
            }
        } else if (category != null && !category.trim().isEmpty() && !category.equals("all")) {
            faqs = faqRepository.findByCategoryIgnoreCaseAndIsApprovedTrue(category);
        } else {
            faqs = faqRepository.findByIsApprovedTrueOrderByCategoryAscCreatedAtDesc();
        }

        model.addAttribute("faqs", faqs);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchKeyword", search);
        return "user-faq-view";
    }

    // API endpoint to get FAQs for dashboard widget (AJAX) - Only approved FAQs
    @GetMapping("/recent")
    @ResponseBody
    public List<FAQ> getRecentFAQs(@RequestParam(value = "limit", defaultValue = "5") int limit) {
        List<FAQ> allFaqs = faqRepository.findByIsApprovedTrueOrderByCategoryAscCreatedAtDesc();
        // Return only the specified number of recent FAQs
        return allFaqs.size() > limit ? allFaqs.subList(0, limit) : allFaqs;
    }

    // API endpoint to get FAQs by category (AJAX) - Only approved FAQs
    @GetMapping("/by-category")
    @ResponseBody
    public List<FAQ> getFAQsByCategory(@RequestParam String category) {
        if (category.equals("all")) {
            return faqRepository.findByIsApprovedTrueOrderByCategoryAscCreatedAtDesc();
        }
        return faqRepository.findByCategoryIgnoreCaseAndIsApprovedTrue(category);
    }

    // API endpoint to search FAQs (AJAX) - Only approved FAQs
    @GetMapping("/search")
    @ResponseBody
    public List<FAQ> searchFAQs(@RequestParam String keyword,
                                @RequestParam(value = "category", required = false) String category) {
        if (category != null && !category.trim().isEmpty() && !category.equals("all")) {
            return faqRepository.findApprovedByCategoryAndKeyword(category, keyword);
        } else {
            return faqRepository.findApprovedByKeyword(keyword);
        }
    }
}
