package lk.sliit.customer_care_system.dto;

import lk.sliit.customer_care_system.modelentity.ChatSession;
import java.time.LocalDateTime;

public class ChatSessionDTO {

    private String sessionId;
    private String username;
    private Long userId;
    private Long agentId;
    private String status;
    private LocalDateTime createdAt;

    public ChatSessionDTO() {}

    public ChatSessionDTO(ChatSession session) {
        this.sessionId = session.getSessionId();
        this.username = session.getUser() != null ? session.getUser().getUsername() : "Unknown";
        this.userId = session.getUser() != null ? session.getUser().getId() : null;
        this.agentId = session.getAgent() != null ? session.getAgent().getId() : null;
        this.status = session.getStatus() != null ? session.getStatus().name() : "WAITING";
        this.createdAt = session.getCreatedAt();
    }

    // Getters & Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
