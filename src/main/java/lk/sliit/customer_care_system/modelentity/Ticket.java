package lk.sliit.customer_care_system.modelentity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 150, message = "Title must be between 5 and 150 characters")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Category is required")
    private String category;

    @Column(nullable = false, length = 20)
    private String status = "Open"; // default value

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 👇 Many tickets can belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "tickets"})
    private User user;

    // 👇 NEW: One ticket can have multiple agent responses
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "ticket"})
    private List<lk.sliit.customer_care_system.modelentity.AgentResponse> responses;

    // === Getters & Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        // Validate status value
        if (status != null && !status.matches("Open|In Progress|Resolved|Closed")) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Must be: Open, In Progress, Resolved, or Closed");
        }
        this.status = status != null ? status : "Open";
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<lk.sliit.customer_care_system.modelentity.AgentResponse> getResponses() { return responses; }
    public void setResponses(List<lk.sliit.customer_care_system.modelentity.AgentResponse> responses) { this.responses = responses; }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", responses=" + (responses != null ? responses.size() : 0) +
                '}';
    }
}
