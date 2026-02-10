package lk.sliit.customer_care_system.controller;

import lk.sliit.customer_care_system.modelentity.AgentResponse;
import lk.sliit.customer_care_system.modelentity.Ticket;
import lk.sliit.customer_care_system.modelentity.User;
import lk.sliit.customer_care_system.repository.AgentResponseRepository;
import lk.sliit.customer_care_system.repository.TicketRepository;
import lk.sliit.customer_care_system.repository.UserRepository;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AgentResponseRepository agentResponseRepository;

    public TicketController(TicketRepository ticketRepository,
                            UserRepository userRepository,
                            AgentResponseRepository agentResponseRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.agentResponseRepository = agentResponseRepository;
    }

    // ✅ Show ticket submission form
    @GetMapping("/submit")
    public String showSubmitForm(Model model) {
        model.addAttribute("ticket", new Ticket());
        return "submit-ticket";
    }

    // ✅ Show ticket success page
    @GetMapping("/success")
    public String showSuccessPage(@RequestParam(required = false) Long id, Model model) {
        if (id != null) {
            Ticket ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));
            model.addAttribute("ticket", ticket);
        }
        return "ticket-success";
    }

    // ✅ Handle ticket submission
    @PostMapping("/submit")
    public String submitTicket(@Valid @ModelAttribute Ticket ticket, BindingResult result, Authentication authentication, Model model) {
        try {
            // Set status before validation (required field)
            if (ticket.getStatus() == null || ticket.getStatus().isEmpty()) {
                ticket.setStatus("Open");
            }

            // Check for validation errors
            if (result.hasErrors()) {
                model.addAttribute("ticket", ticket);
                model.addAttribute("errors", result.getAllErrors());
                return "submit-ticket";
            }

            // Set timestamps
            ticket.setCreatedAt(LocalDateTime.now());

            // Find logged-in user
            if (authentication == null || authentication.getName() == null) {
                model.addAttribute("error", "You must be logged in to submit a ticket");
                return "redirect:/login";
            }

            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            ticket.setUser(user);

            Ticket savedTicket = ticketRepository.save(ticket);

            return "redirect:/tickets/success?id=" + savedTicket.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Error submitting ticket: " + e.getMessage());
            model.addAttribute("ticket", ticket);
            return "submit-ticket";
        }
    }

    // ✅ Get all tickets (for debugging / API use)
    @ResponseBody
    @GetMapping("/all")
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    // ✅ Update ticket status (used by Agent UI)
    @PostMapping("/update-status/{id}")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());

        ticketRepository.save(ticket);

        // redirect back to agent's ticket view
        return "redirect:/agent/tickets";
    }

    // ✅ Show edit ticket form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Check if the ticket belongs to the current user
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only edit your own tickets");
        }

        model.addAttribute("ticket", ticket);
        return "edit-ticket";
    }

    // ✅ Handle ticket update
    @PostMapping("/edit/{id}")
    public String updateTicket(@PathVariable Long id, @Valid @ModelAttribute Ticket updatedTicket, BindingResult result, Authentication authentication, Model model) {
        Ticket existingTicket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Check if the ticket belongs to the current user
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!existingTicket.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only edit your own tickets");
        }

        if (result.hasErrors()) {
            model.addAttribute("ticket", updatedTicket);
            return "edit-ticket";
        }

        // Update ticket fields
        existingTicket.setTitle(updatedTicket.getTitle());
        existingTicket.setDescription(updatedTicket.getDescription());
        existingTicket.setCategory(updatedTicket.getCategory());
        existingTicket.setUpdatedAt(LocalDateTime.now());

        ticketRepository.save(existingTicket);

        return "redirect:/user/tickets?updated=true";
    }

    // ✅ Handle ticket deletion (Users can delete their own, Agents can delete any)
    @PostMapping("/delete/{id}")
    public String deleteTicket(@PathVariable Long id, Authentication authentication) {
        try {
            Ticket ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            // Get current user
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check permissions: Users can only delete their own tickets, Agents/Admins can delete any
            boolean isAgent = user.getRole().equals("ROLE_AGENT");
            boolean isAdmin = user.getRole().equals("ROLE_ADMIN");
            boolean isOwner = ticket.getUser().getId().equals(user.getId());

            if (!isAgent && !isAdmin && !isOwner) {
                throw new RuntimeException("You do not have permission to delete this ticket");
            }

            // Delete associated responses first (to handle foreign key constraints)
            List<AgentResponse> responses = agentResponseRepository.findByTicket(ticket);
            if (!responses.isEmpty()) {
                agentResponseRepository.deleteAll(responses);
            }

            // Delete the ticket
            ticketRepository.delete(ticket);

            // Redirect based on user role
            if (isAgent || isAdmin) {
                return "redirect:/agent/tickets?deleted=true";
            } else {
                return "redirect:/user/tickets?deleted=true";
            }
        } catch (Exception e) {
            // Handle errors and redirect appropriately
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            if (role.equals("ROLE_AGENT") || role.equals("ROLE_ADMIN")) {
                return "redirect:/agent/tickets?error=true";
            } else {
                return "redirect:/user/tickets?error=true";
            }
        }
    }
}
