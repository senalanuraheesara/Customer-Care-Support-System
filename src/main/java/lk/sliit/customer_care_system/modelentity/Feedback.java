package lk.sliit.customer_care_system.modelentity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "Subject is required")
    @Size(min = 5, max = 200, message = "Subject must be between 5 and 200 characters")
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Message is required")
    @Size(min = 10, max = 2000, message = "Message must be between 10 and 2000 characters")
    private String message;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Category is required")
    @Pattern(regexp = "Service|Product|Complaint|Suggestion|Other", message = "Invalid category")
    private String category;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "rating")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    private Integer rating; // 1-5 star rating (optional)

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Many feedbacks can belong to one user (optional for anonymous feedback)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Admin response to feedback
    @Column(name = "admin_response", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Admin response must not exceed 2000 characters")
    private String adminResponse;

    @Column(name = "admin_response_at")
    private LocalDateTime adminResponseAt;

    // === Constructors ===
    public Feedback() {}

    public Feedback(String subject, String message, String category, User user) {
        this.subject = subject;
        this.message = message;
        this.category = category;
        this.user = user;
        this.status = "New";
        this.createdAt = LocalDateTime.now();
    }

    // === Getters & Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public LocalDateTime getAdminResponseAt() { return adminResponseAt; }
    public void setAdminResponseAt(LocalDateTime adminResponseAt) { this.adminResponseAt = adminResponseAt; }

    @Override
    public String toString() {
        return "Feedback{" +
                "id=" + id +
                ", subject='" + subject + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                ", rating=" + rating +
                ", isAnonymous=" + isAnonymous +
                ", createdAt=" + createdAt +
                ", user=" + (user != null ? user.getUsername() : "Anonymous") +
                '}';
    }
}
