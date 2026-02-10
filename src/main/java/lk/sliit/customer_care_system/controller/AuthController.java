package lk.sliit.customer_care_system.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lk.sliit.customer_care_system.modelentity.User;
import lk.sliit.customer_care_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth") // ✅ Base path to avoid URL conflicts
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ----------------------------
    // Registration Page (GET)
    // ----------------------------
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register"; // Loads register.html
    }

    // ----------------------------
    // Handle Registration (POST)
    // ----------------------------
    @PostMapping("/register")
    @Transactional
    public String register(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model
    ) {
        // 1️⃣ Form validation
        if (result.hasErrors()) {
            model.addAttribute("error", "Please correct the highlighted errors.");
            return "register";
        }

        // 2️⃣ Password match check
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "register";
        }

        // 3️⃣ Username uniqueness check
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Username already exists. Please choose another one.");
            return "register";
        }

        // 4️⃣ Validate phone number
        if (user.getPhoneNumber() == null || !user.getPhoneNumber().matches("\\d{10}")) {
            model.addAttribute("error", "Phone number must be exactly 10 digits.");
            return "register";
        }

        // 5️⃣ Validate address
        if (user.getAddress() == null || user.getAddress().trim().length() < 3) {
            model.addAttribute("error", "Address must be at least 3 characters long.");
            return "register";
        }

        // 6️⃣ Encode password and set role
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");

        // 7️⃣ Save user
        userRepository.save(user);

        // 8️⃣ Redirect to login with success message
        return "redirect:/login?success=registered";
    }
}
