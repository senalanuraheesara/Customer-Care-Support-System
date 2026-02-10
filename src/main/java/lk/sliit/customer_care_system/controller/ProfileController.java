package lk.sliit.customer_care_system.controller;

import lk.sliit.customer_care_system.modelentity.User;
import lk.sliit.customer_care_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Show profile page
    @GetMapping
    public String showProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            model.addAttribute("user", user);
            return "profile";
        }

        return "redirect:/login";
    }

    // Get current user data (AJAX)
    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProfileData() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("phoneNumber", user.getPhoneNumber());
            userData.put("address", user.getAddress());
            userData.put("role", user.getRole());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "user", userData
            ));
        }

        return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "User not authenticated"));
    }

    // Update profile
    @PutMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> requestBody) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "User not authenticated"));
            }

            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update user details
            String phoneNumber = (String) requestBody.get("phoneNumber");
            String address = (String) requestBody.get("address");
            String currentPassword = (String) requestBody.get("currentPassword");
            String newPassword = (String) requestBody.get("newPassword");

            // Validate phone number
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                user.setPhoneNumber(phoneNumber);
            }

            // Validate address
            if (address != null && !address.trim().isEmpty()) {
                user.setAddress(address);
            }

            // Update password if provided
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                // Verify current password
                if (currentPassword == null || currentPassword.trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Current password is required to change password"));
                }

                if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Current password is incorrect"));
                }

                user.setPassword(passwordEncoder.encode(newPassword));
            }

            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Profile updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
