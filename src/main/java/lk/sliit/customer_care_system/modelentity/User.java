package lk.sliit.customer_care_system.modelentity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.List;

@Entity
@Table(name = "users") // avoid reserved word "user"
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Username is required")
    private String username;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$", message = "Password must contain at least one digit, one uppercase letter, one special character (@#$%^&+=!), and be at least 8 characters long")
    private String password;

    @Column(nullable = false, length = 50)
    private String role = "ROLE_USER"; // default role

    @Column(name = "phone_number", length = 10, nullable = false)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "Address is required")
    private String address;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "user" })
    private List<lk.sliit.customer_care_system.modelentity.Ticket> tickets;

    // Getters & setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<lk.sliit.customer_care_system.modelentity.Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<lk.sliit.customer_care_system.modelentity.Ticket> tickets) {
        this.tickets = tickets;
    }
}
