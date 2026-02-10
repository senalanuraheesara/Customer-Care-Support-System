package lk.sliit.customer_care_system.controller;

import lk.sliit.customer_care_system.dto.ChatMessageDTO;
import lk.sliit.customer_care_system.modelentity.ChatMessage;
import lk.sliit.customer_care_system.modelentity.ChatSession;
import lk.sliit.customer_care_system.repository.UserRepository;
import lk.sliit.customer_care_system.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired private ChatService chatService;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private UserRepository userRepository;

    // ---- User Live Chat ----
    @GetMapping("/live-chat")
    public String liveChat(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            var user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            ChatSession session = chatService.getOrCreateActiveSession(user);
            model.addAttribute("sessionId", session.getSessionId());
            model.addAttribute("userId", user.getId());
            model.addAttribute("username", user.getUsername());
        }
        return "live-chat";
    }

    // ---- Agent Chat ----
    @GetMapping("/agent/chat")
    public String agentChat(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            var agent = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Agent not found"));
            model.addAttribute("agentId", agent.getId());
            model.addAttribute("waitingSessions", chatService.getWaitingSessions());
            model.addAttribute("agentSessions", chatService.getAllActiveSessions()); // Show all active sessions
        }
        return "agent-chat";
    }

    // ---- Message Handling ----
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, Object> payload) {
        try {
            chatService.sendMessage(
                    (String) payload.get("sessionId"),
                    Long.valueOf(payload.get("senderId").toString()),
                    (String) payload.get("content"),
                    ChatMessage.SenderType.valueOf(payload.get("senderType").toString())
            );
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("type", "error");
            error.put("message", e.getMessage());
            messagingTemplate.convertAndSend("/topic/chat/" + payload.get("sessionId"), error);
        }
    }

    // ---- REST APIs ----
    @PostMapping("/api/chat/assign-agent")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> assignAgent(@RequestParam String sessionId, @RequestParam Long agentId) {
        try {
            ChatSession s = chatService.assignAgentToSession(sessionId, agentId);
            return ResponseEntity.ok(Map.of("success", true, "session", s));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/api/chat/history/{sessionId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDTO>> getHistory(@PathVariable String sessionId) {
        try {
            logger.info("REST API: Fetching chat history for session: {}", sessionId);
            var messages = chatService.getChatHistory(sessionId);
            var dto = messages.stream().map(ChatMessageDTO::new).toList();
            logger.info("REST API: Returning {} messages for session {}", dto.size(), sessionId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error fetching chat history for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/api/chat/session/{sessionId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteSession(@PathVariable String sessionId) {
        try {
            chatService.deleteSession(sessionId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Session deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/api/chat/message/{messageId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editMessage(
            @PathVariable Long messageId,
            @RequestBody Map<String, Object> requestBody) {
        try {
            String content = (String) requestBody.get("content");
            Long userId = Long.valueOf(requestBody.get("userId").toString());

            logger.info("Edit message request: messageId={}, userId={}, content={}", messageId, userId, content);

            ChatMessage message = chatService.editMessage(messageId, content, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Message edited successfully",
                    "data", new ChatMessageDTO(message)
            ));
        } catch (Exception e) {
            logger.error("Error editing message {}: {}", messageId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/chat/message/{messageId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMessage(
            @PathVariable Long messageId,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Long userId = Long.valueOf(requestBody.get("userId").toString());

            logger.info("Delete message request: messageId={}, userId={}", messageId, userId);

            ChatMessage message = chatService.deleteMessage(messageId, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Message deleted successfully",
                    "data", new ChatMessageDTO(message)
            ));
        } catch (Exception e) {
            logger.error("Error deleting message {}: {}", messageId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
