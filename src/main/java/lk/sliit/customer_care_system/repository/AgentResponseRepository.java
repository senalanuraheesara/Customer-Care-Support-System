package lk.sliit.customer_care_system.repository;

import lk.sliit.customer_care_system.modelentity.AgentResponse;
import lk.sliit.customer_care_system.modelentity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgentResponseRepository extends JpaRepository<AgentResponse, Long> {

    // Return a list of responses for a ticket (even if there is only one)
    List<AgentResponse> findByTicket(Ticket ticket);
}
