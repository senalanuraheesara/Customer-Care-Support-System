package lk.sliit.customer_care_system.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sliit.customer_care_system.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.UUID;

@Configuration
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CSRF CONFIG
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/auth/**",
                                "/register",
                                "/login",
                                "/api/**"
                        )
                )

                // AUTHORIZATION
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/auth/register",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/faq",
                                "/faq/**"
                        ).permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/agent/**").hasRole("AGENT")
                        .requestMatchers("/user/**", "/tickets/**", "/feedback/**", "/profile")
                        .hasAnyRole("USER", "AGENT", "ADMIN")

                        .anyRequest().authenticated()
                )

                // LOGIN
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // REMEMBER ME
                .rememberMe(remember -> remember
                        .key(UUID.randomUUID().toString())
                        .tokenValiditySeconds(86400)
                        .userDetailsService(userDetailsService())
                )

                // LOGOUT (POST recommended)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                );

        return http.build();
    }

    // PASSWORD ENCODER
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // USER DETAILS SERVICE
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {

            // HARDCODED ADMIN
            if ("admin".equals(username)) {
                return org.springframework.security.core.userdetails.User
                        .withUsername("admin")
                        .password("$2a$10$Q9xXH9ZrYhN0J6U3J6k7WONxk3hR0b4H8wZQxJpC8p9QGZ8q1H6eW")
                        // password: admin1234
                        .roles("ADMIN")
                        .build();
            }

            return userRepository.findByUsername(username)
                    .map(user -> org.springframework.security.core.userdetails.User
                            .withUsername(user.getUsername())
                            .password(user.getPassword()) // already BCrypt encoded
                            .roles(user.getRole().replace("ROLE_", ""))
                            .build())
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found: " + username));
        };
    }

    // LOGIN REDIRECT HANDLER
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    Authentication authentication
            ) throws IOException, ServletException {

                String role = authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority();

                if ("ROLE_ADMIN".equals(role)) {
                    response.sendRedirect("/admin/dashboard");
                } else if ("ROLE_AGENT".equals(role)) {
                    response.sendRedirect("/agent/dashboard");
                } else {
                    response.sendRedirect("/user/dashboard");
                }
            }
        };
    }
}
