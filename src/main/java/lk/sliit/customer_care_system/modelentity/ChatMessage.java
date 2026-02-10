package lk.sliit.customer_care_system.modelentity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    @NotBlank(message = "Message content is required")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @NotNull(message = "Sender is required")
    private User sender;

    // Removed problematic chatSession relationship - use chatSessionId string instead

    @Column(name = "sender_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SenderType senderType;

    @Column(name = "chat_session_id", nullable = false)
    private String chatSessionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_edited", nullable = false)
    private Boolean isEdited = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    public enum SenderType {
        USER, AGENT, SYSTEM
    }

    // Constructors
    public ChatMessage() {
        this.createdAt = LocalDateTime.now();
        this.isEdited = false;
        this.isDeleted = false;
    }

    public ChatMessage(String content, User sender, SenderType senderType, String chatSessionId) {
        this();
        this.content = content;
        this.sender = sender;
        this.senderType = senderType;
        this.chatSessionId = chatSessionId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    // Removed getChatSession() and setChatSession() - use chatSessionId string instead

    public SenderType getSenderType() { return senderType; }
    public void setSenderType(SenderType senderType) { this.senderType = senderType; }

    public String getChatSessionId() { return chatSessionId; }
    public void setChatSessionId(String chatSessionId) { this.chatSessionId = chatSessionId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsEdited() { return isEdited; }
    public void setIsEdited(Boolean isEdited) { this.isEdited = isEdited; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (!this.isEdited) {
            this.isEdited = true;
        }
    }
}
