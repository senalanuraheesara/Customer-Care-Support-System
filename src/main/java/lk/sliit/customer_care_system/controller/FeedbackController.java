package lk.sliit.customer_care_system.controller;

import lk.sliit.customer_care_system.modelentity.Feedback;
import lk.sliit.customer_care_system.modelentity.User;
import lk.sliit.customer_care_system.repository.FeedbackRepository;
import lk.sliit.customer_care_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    @Autowired
    public FeedbackController(FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    // ==================== USER ENDPOINTS ====================

    // Show feedback submission form
    @GetMapping("/submit")
    public String showFeedbackForm(Model model) {
        model.addAttribute("feedback", new Feedback());
        return "submit-feedback";
    }

    // Handle feedback submission
    @PostMapping("/submit")
    public String submitFeedback(@Valid @ModelAttribute Feedback feedback, BindingResult result, Authentication authentication, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("feedback", feedback);
            return "submit-feedback";
        }
        feedback.setStatus("New");
        feedback.setCreatedAt(LocalDateTime.now());

        // Set user if authenticated, otherwise mark as anonymous
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            feedback.setUser(user);
        } else {
            feedback.setIsAnonymous(true);
        }

        Feedback savedFeedback = feedbackRepository.save(feedback);
        return "redirect:/feedback/success?id=" + savedFeedback.getId();
    }

    // Show feedback success page
    @GetMapping("/success")
    public String showFeedbackSuccess(@RequestParam Long id, Model model) {
        Optional<Feedback> feedback = feedbackRepository.findById(id);
        if (feedback.isPresent()) {
            model.addAttribute("feedback", feedback.get());
            return "feedback-success";
        }
        return "redirect:/feedback/submit";
    }

    // Show user's feedback history (if authenticated)
    @GetMapping("/my-feedback")
    public String showMyFeedback(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Feedback> feedbacks = feedbackRepository.findByUser(user);
        model.addAttribute("feedbacks", feedbacks);
        return "my-feedback";
    }

    // Show edit form for a feedback (owner only)
    @GetMapping("/edit/{id}")
    public String showEditFeedback(@PathVariable Long id, Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<Feedback> optionalFeedback = feedbackRepository.findById(id);
        if (optionalFeedback.isEmpty()) {
            return "redirect:/feedback/my-feedback";
        }

        Feedback feedback = optionalFeedback.get();

        // Only the owner can edit (skip for anonymous feedbacks without user)
        String username = authentication.getName();
        if (feedback.getUser() == null || feedback.getUser().getUsername() == null || !feedback.getUser().getUsername().equals(username)) {
            return "redirect:/feedback/my-feedback";
        }

        model.addAttribute("feedback", feedback);
        return "edit-feedback";
    }

    // Handle edit submission (owner only)
    @PostMapping("/edit/{id}")
    public String updateFeedback(@PathVariable Long id,
                                 @Valid @ModelAttribute Feedback formFeedback,
                                 BindingResult result,
                                 Authentication authentication,
                                 Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<Feedback> optionalFeedback = feedbackRepository.findById(id);
        if (optionalFeedback.isEmpty()) {
            return "redirect:/feedback/my-feedback";
        }

        Feedback feedback = optionalFeedback.get();
        String username = authentication.getName();
        if (feedback.getUser() == null || feedback.getUser().getUsername() == null || !feedback.getUser().getUsername().equals(username)) {
            return "redirect:/feedback/my-feedback";
        }

        if (result.hasErrors()) {
            model.addAttribute("feedback", formFeedback);
            return "edit-feedback";
        }

        feedback.setSubject(formFeedback.getSubject());
        feedback.setMessage(formFeedback.getMessage());
        feedback.setCategory(formFeedback.getCategory());
        feedback.setRating(formFeedback.getRating());
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);

        return "redirect:/feedback/my-feedback?updated=true";
    }

    // Delete feedback (owner only)
    @PostMapping("/delete/{id}")
    public String deleteOwnFeedback(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<Feedback> optionalFeedback = feedbackRepository.findById(id);
        if (optionalFeedback.isPresent()) {
            Feedback feedback = optionalFeedback.get();
            String username = authentication.getName();
            if (feedback.getUser() != null && username.equals(feedback.getUser().getUsername())) {
                feedbackRepository.deleteById(id);
            }
        }
        return "redirect:/feedback/my-feedback?deleted=true";
    }

    // ==================== ADMIN ENDPOINTS ====================

    // Show all feedbacks for admin
    @GetMapping("/admin/all")
    public String showAllFeedbacks(Model model,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String category,
                                   @RequestParam(required = false) Boolean isAnonymous,
                                   @RequestParam(required = false) Boolean hasAdminResponse) {

        List<Feedback> feedbacks;

        if (status != null || category != null || isAnonymous != null || hasAdminResponse != null) {
            // Filtered search
            feedbacks = feedbackRepository.findFeedbacksByCriteria(status, category, isAnonymous, hasAdminResponse);
        } else {
            // Show all feedbacks
            feedbacks = feedbackRepository.findAll();
        }

        // Sort by creation date (newest first)
        feedbacks.sort((f1, f2) -> f2.getCreatedAt().compareTo(f1.getCreatedAt()));

        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentIsAnonymous", isAnonymous);
        model.addAttribute("currentHasAdminResponse", hasAdminResponse);

        // Add statistics
        model.addAttribute("totalFeedbacks", feedbackRepository.count());
        model.addAttribute("newFeedbacks", feedbackRepository.countByStatus("New"));
        model.addAttribute("respondedFeedbacks", feedbackRepository.countByAdminResponseIsNotNull());
        model.addAttribute("anonymousFeedbacks", feedbackRepository.countByIsAnonymousTrue());

        return "admin-feedback-management";
    }

    // Show individual feedback details for admin
    @GetMapping("/admin/view/{id}")
    public String viewFeedback(@PathVariable Long id, Model model) {
        Optional<Feedback> feedback = feedbackRepository.findById(id);
        if (feedback.isPresent()) {
            model.addAttribute("feedback", feedback.get());
            return "admin-feedback-details";
        }
        return "redirect:/feedback/admin/all";
    }

    // Update feedback status
    @PostMapping("/admin/update-status/{id}")
    public String updateFeedbackStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<Feedback> feedback = feedbackRepository.findById(id);
        if (feedback.isPresent()) {
            Feedback f = feedback.get();
            f.setStatus(status);
            f.setUpdatedAt(LocalDateTime.now());
            feedbackRepository.save(f);
        }
        return "redirect:/feedback/admin/view/" + id;
    }

    // Add admin response to feedback
    @PostMapping("/admin/respond/{id}")
    public String addAdminResponse(@PathVariable Long id, @RequestParam String adminResponse) {
        Optional<Feedback> feedback = feedbackRepository.findById(id);
        if (feedback.isPresent()) {
            Feedback f = feedback.get();
            f.setAdminResponse(adminResponse);
            f.setAdminResponseAt(LocalDateTime.now());
            f.setUpdatedAt(LocalDateTime.now());
            f.setStatus("Responded");
            feedbackRepository.save(f);
        }
        return "redirect:/feedback/admin/view/" + id;
    }

    // Delete feedback (admin only)
    @PostMapping("/admin/delete/{id}")
    public String deleteFeedback(@PathVariable Long id) {
        feedbackRepository.deleteById(id);
        return "redirect:/feedback/admin/all?deleted=true";
    }

    // ==================== API ENDPOINTS ====================

    // Get feedback statistics (for dashboard)
    @GetMapping("/api/stats")
    @ResponseBody
    public Object getFeedbackStats() {
        return new Object() {
            public final long totalFeedbacks = feedbackRepository.count();
            public final long newFeedbacks = feedbackRepository.countByStatus("New");
            public final long respondedFeedbacks = feedbackRepository.countByAdminResponseIsNotNull();
            public final long anonymousFeedbacks = feedbackRepository.countByIsAnonymousTrue();
        };
    }
}
