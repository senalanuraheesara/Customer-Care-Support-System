package lk.sliit.customer_care_system.repository;

import lk.sliit.customer_care_system.modelentity.ChatSession;
import lk.sliit.customer_care_system.modelentity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findBySessionId(String sessionId);

    List<ChatSession> findByUserOrderByCreatedAtDesc(User user);

    List<ChatSession> findByAgentOrderByCreatedAtDesc(User agent);

    @Query("SELECT cs FROM ChatSession cs WHERE cs.status = :status ORDER BY cs.createdAt ASC")
    List<ChatSession> findByStatusOrderByCreatedAtAsc(@Param("status") ChatSession.ChatStatus status);

    @Query("SELECT cs FROM ChatSession cs WHERE cs.user = :user AND cs.status = :status ORDER BY cs.createdAt DESC")
    List<ChatSession> findByUserAndStatusOrderByCreatedAtDesc(@Param("user") User user, @Param("status") ChatSession.ChatStatus status);

    @Query("SELECT cs FROM ChatSession cs WHERE cs.agent = :agent AND cs.status = :status ORDER BY cs.createdAt DESC")
    List<ChatSession> findByAgentAndStatusOrderByCreatedAtDesc(@Param("agent") User agent, @Param("status") ChatSession.ChatStatus status);

    @Query("SELECT cs FROM ChatSession cs WHERE cs.status = 'WAITING_FOR_AGENT' ORDER BY cs.createdAt ASC")
    List<ChatSession> findWaitingForAgentSessions();

    @Query("SELECT cs FROM ChatSession cs WHERE cs.agent IS NULL AND cs.status = 'ACTIVE' ORDER BY cs.createdAt ASC")
    List<ChatSession> findActiveSessionsWithoutAgent();
}
