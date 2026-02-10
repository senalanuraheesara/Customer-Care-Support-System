package lk.sliit.customer_care_system.repository;

import lk.sliit.customer_care_system.modelentity.Feedback;
import lk.sliit.customer_care_system.modelentity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Find feedbacks by user
    List<Feedback> findByUser(User user);

    // Find feedbacks by user ID
    List<Feedback> findByUserId(Long userId);

    // Find feedbacks by status
    List<Feedback> findByStatus(String status);

    // Find feedbacks by category
    List<Feedback> findByCategory(String category);

    // Find anonymous feedbacks
    List<Feedback> findByIsAnonymousTrue();

    // Find non-anonymous feedbacks
    List<Feedback> findByIsAnonymousFalse();

    // Find feedbacks with admin response
    List<Feedback> findByAdminResponseIsNotNull();

    // Find feedbacks without admin response
    List<Feedback> findByAdminResponseIsNull();

    // Find feedbacks by rating
    List<Feedback> findByRating(Integer rating);

    // Find feedbacks with rating greater than or equal to given rating
    List<Feedback> findByRatingGreaterThanEqual(Integer rating);

    // Custom query to find feedbacks by multiple criteria
    @Query("SELECT f FROM Feedback f WHERE " +
            "(:status IS NULL OR f.status = :status) AND " +
            "(:category IS NULL OR f.category = :category) AND " +
            "(:isAnonymous IS NULL OR f.isAnonymous = :isAnonymous) AND " +
            "(:hasAdminResponse IS NULL OR " +
            "(:hasAdminResponse = true AND f.adminResponse IS NOT NULL) OR " +
            "(:hasAdminResponse = false AND f.adminResponse IS NULL))")
    List<Feedback> findFeedbacksByCriteria(
            @Param("status") String status,
            @Param("category") String category,
            @Param("isAnonymous") Boolean isAnonymous,
            @Param("hasAdminResponse") Boolean hasAdminResponse
    );

    // Count feedbacks by status
    long countByStatus(String status);

    // Count feedbacks by category
    long countByCategory(String category);

    // Count anonymous feedbacks
    long countByIsAnonymousTrue();

    // Count feedbacks with admin response
    long countByAdminResponseIsNotNull();

    // Find recent feedbacks (last 30 days)
    @Query("SELECT f FROM Feedback f WHERE f.createdAt >= CURRENT_DATE - 30 DAY ORDER BY f.createdAt DESC")
    List<Feedback> findRecentFeedbacks();

    // Find feedbacks by date range
    @Query("SELECT f FROM Feedback f WHERE f.createdAt BETWEEN :startDate AND :endDate ORDER BY f.createdAt DESC")
    List<Feedback> findFeedbacksByDateRange(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );
}
