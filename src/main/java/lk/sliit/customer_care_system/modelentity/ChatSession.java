package lk.sliit.customer_care_system.modelentity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull(message = "User is required")
    private User user;

    @ManyToOne
    @JoinColumn(name = "agent_id")
    private User agent;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    private ChatStatus status;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Enum for chat states ✅
    public enum ChatStatus {
        WAITING,   // User created session, waiting for agent
        ACTIVE,    // Agent assigned
        CLOSED     // Ended chat
    }

    // Constructors
    public ChatSession() {}

    public ChatSession(String sessionId, User user) {
        this.sessionId = sessionId;
        this.user = user;
        this.status = ChatStatus.WAITING;
        this.createdAt = LocalDateTime.now();
    }

    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public User getAgent() { return agent; }
    public void setAgent(User agent) { this.agent = agent; }

    public ChatStatus getStatus() { return status; }
    public void setStatus(ChatStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
