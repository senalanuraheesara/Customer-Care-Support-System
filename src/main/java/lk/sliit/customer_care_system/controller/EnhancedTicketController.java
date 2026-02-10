package lk.sliit.customer_care_system.controller;

import lk.sliit.customer_care_system.modelentity.Ticket;
import lk.sliit.customer_care_system.modelentity.User;
import lk.sliit.customer_care_system.repository.TicketRepository;
import lk.sliit.customer_care_system.repository.UserRepository;
import lk.sliit.customer_care_system.service.SingletonServiceManager;
import lk.sliit.customer_care_system.strategy.NotificationContext;
import lk.sliit.customer_care_system.strategy.ValidationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Ticket Controller using Design Patterns
 * Demonstrates Singleton, Strategy patterns
 */
@RestController
@RequestMapping("/api/enhanced/tickets")
public class EnhancedTicketController {

    @Autowired
    private SingletonServiceManager serviceManager;

    @Autowired
    private NotificationContext notificationContext;

    @Autowired
    private ValidationContext validationContext;

    /**
     * Create ticket with enhanced validation and notification
     */
    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody Ticket ticket, Authentication authentication) {
        // Use Strategy pattern for validation
        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("title", ticket.getTitle());
        ticketData.put("description", ticket.getDescription());
        ticketData.put("category", ticket.getCategory());

        Map<String, String> validationErrors = validationContext.validateTicket(ticketData);

        if (!validationErrors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Validation failed", "details", validationErrors));
        }

        // Use Singleton pattern for repository access
        TicketRepository ticketRepository = serviceManager.getTicketRepository();
        UserRepository userRepository = serviceManager.getUserRepository();

        // Get current user
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Set ticket properties
        ticket.setUser(user);
        ticket.setStatus("Open");
        ticket.setCreatedAt(LocalDateTime.now());

        // Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);

        // Use Strategy pattern for notifications
        notificationContext.sendInAppNotification(
                user,
                "Ticket Created",
                "Your ticket #" + savedTicket.getId() + " has been created successfully!"
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Ticket created successfully!", "ticket", savedTicket));
    }

    /**
     * Update ticket status with notification
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTicketStatus(@PathVariable Long id,
                                                @RequestParam String status,
                                                Authentication authentication) {
        // Use Singleton pattern for repository access
        TicketRepository ticketRepository = serviceManager.getTicketRepository();
        UserRepository userRepository = serviceManager.getUserRepository();

        Ticket ticket = ticketRepository.findById(id).orElse(null);
        if (ticket == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Ticket not found"));
        }

        // Update ticket status
        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        // Use Strategy pattern for notifications
        notificationContext.sendInAppNotification(
                ticket.getUser(),
                "Ticket Status Updated",
                "Your ticket #" + ticket.getId() + " status has been updated to: " + status
        );

        return ResponseEntity.ok(Map.of("message", "Ticket status updated successfully"));
    }

    /**
     * Get ticket statistics using singleton pattern
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getTicketStatistics() {
        // Use Singleton pattern for repository access
        TicketRepository ticketRepository = serviceManager.getTicketRepository();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalTickets", ticketRepository.count());
        statistics.put("openTickets", ticketRepository.countByStatus("Open"));
        statistics.put("inProgressTickets", ticketRepository.countByStatus("In Progress"));
        statistics.put("resolvedTickets", ticketRepository.countByStatus("Resolved"));
        statistics.put("closedTickets", ticketRepository.countByStatus("Closed"));

        return ResponseEntity.ok(statistics);
    }
}

