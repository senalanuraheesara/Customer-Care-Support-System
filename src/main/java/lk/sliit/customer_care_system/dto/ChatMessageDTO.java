package lk.sliit.customer_care_system.dto;

import lk.sliit.customer_care_system.modelentity.ChatMessage;
import lk.sliit.customer_care_system.modelentity.ChatMessage.SenderType;
import java.time.LocalDateTime;

public class ChatMessageDTO {
    private Long id;
    private String content;
    private String senderUsername;
    private SenderType senderType;
    private String sessionId;
    private LocalDateTime createdAt;
    private Boolean isEdited;
    private Boolean isDeleted;

    public ChatMessageDTO() {}

    public ChatMessageDTO(ChatMessage message) {
        this.id = message.getId();
        this.content = message.getContent();
        // Safely get username with null checks
        if (message.getSender() != null) {
            this.senderUsername = message.getSender().getUsername();
        } else {
            this.senderUsername = "System";
        }
        this.senderType = message.getSenderType();
        // Use chatSessionId directly to avoid lazy loading issues
        this.sessionId = message.getChatSessionId();
        this.createdAt = message.getCreatedAt();
        this.isEdited = message.getIsEdited();
        this.isDeleted = message.getIsDeleted();
    }

    // Extra constructor for system messages
    public ChatMessageDTO(String senderUsername, String content, SenderType senderType, String sessionId) {
        this.senderUsername = senderUsername;
        this.content = content;
        this.senderType = senderType;
        this.sessionId = sessionId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public SenderType getSenderType() { return senderType; }
    public void setSenderType(SenderType senderType) { this.senderType = senderType; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getIsEdited() { return isEdited; }
    public void setIsEdited(Boolean isEdited) { this.isEdited = isEdited; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
}
