package lk.sliit.customer_care_system.controller;

import lk.sliit.customer_care_system.modelentity.AgentResponse;
import lk.sliit.customer_care_system.modelentity.Ticket;
import lk.sliit.customer_care_system.modelentity.User;
import lk.sliit.customer_care_system.repository.AgentResponseRepository;
import lk.sliit.customer_care_system.repository.TicketRepository;
import lk.sliit.customer_care_system.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/tickets")
public class AgentResponseController {

    private final AgentResponseRepository agentResponseRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public AgentResponseController(AgentResponseRepository agentResponseRepository,
                                   TicketRepository ticketRepository,
                                   UserRepository userRepository) {
        this.agentResponseRepository = agentResponseRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    // Add or update a response for a ticket
    @PostMapping("/respond/{ticketId}")
    public String addOrUpdateResponse(@PathVariable Long ticketId,
                                      @RequestParam String responseText,
                                      @RequestParam String action,
                                      Authentication authentication,
                                      Model model) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            // Get the currently logged-in agent
            String username = authentication.getName();
            User agent = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Agent not found"));

            // Create a new response (allow multiple responses)
            AgentResponse newResponse = new AgentResponse();
            newResponse.setTicket(ticket);
            newResponse.setAgent(agent);
            newResponse.setResponseText(responseText);
            newResponse.setCreatedAt(LocalDateTime.now());
            agentResponseRepository.save(newResponse);

            // Update the ticket status based on the response action
            ticket.setStatus(action);
            ticket.setUpdatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);

            return "redirect:/agent/tickets?success=true";
        } catch (Exception e) {
            return "redirect:/agent/tickets?error=true";
        }
    }

    // Update an existing response
    @PostMapping("/response/update/{responseId}")
    public String updateResponse(@PathVariable Long responseId,
                                 @RequestParam String responseText,
                                 @RequestParam(required = false) String status,
                                 Authentication authentication) {
        try {
            AgentResponse response = agentResponseRepository.findById(responseId)
                    .orElseThrow(() -> new RuntimeException("Response not found"));

            // Verify the agent owns this response
            String username = authentication.getName();
            User agent = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Agent not found"));

            if (!response.getAgent().getId().equals(agent.getId())) {
                throw new RuntimeException("You can only edit your own responses");
            }

            // Update the response text
            response.setResponseText(responseText);
            agentResponseRepository.save(response);

            // Update ticket status if provided
            if (status != null && !status.isEmpty()) {
                Ticket ticket = response.getTicket();
                ticket.setStatus(status);
                ticket.setUpdatedAt(LocalDateTime.now());
                ticketRepository.save(ticket);
            }

            return "redirect:/agent/tickets?updated=true";
        } catch (Exception e) {
            return "redirect:/agent/tickets?error=true";
        }
    }

    // Delete a response
    @PostMapping("/response/delete/{responseId}")
    public String deleteResponse(@PathVariable Long responseId,
                                 Authentication authentication) {
        try {
            AgentResponse response = agentResponseRepository.findById(responseId)
                    .orElseThrow(() -> new RuntimeException("Response not found"));

            // Verify the agent owns this response or is admin
            String username = authentication.getName();
            User agent = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Agent not found"));

            boolean isAdmin = agent.getRole().equals("ROLE_ADMIN");
            boolean isOwner = response.getAgent().getId().equals(agent.getId());

            if (!isAdmin && !isOwner) {
                throw new RuntimeException("You can only delete your own responses");
            }

            agentResponseRepository.delete(response);
            return "redirect:/agent/tickets?deleted=true";
        } catch (Exception e) {
            return "redirect:/agent/tickets?error=true";
        }
    }

    // Show the response for a specific ticket (history view)
    @GetMapping("/responses/{ticketId}")
    public String getResponseForTicket(@PathVariable Long ticketId, Model model) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Fetch all responses for the ticket
        List<AgentResponse> responses = agentResponseRepository.findByTicket(ticket);

        // Display the first response if available, or null if not
        AgentResponse response = responses.isEmpty() ? null : responses.get(0);

        // Add the ticket and its response (if available)
        model.addAttribute("ticket", ticket);
        model.addAttribute("response", response); // Display the single response (null if none)

        return "ticket-response"; // Render the response view
    }

    // Debug/REST endpoint
    @ResponseBody
    @GetMapping("/responses/all")
    public Iterable<AgentResponse> getAllResponses() {
        return agentResponseRepository.findAll();
    }
}
