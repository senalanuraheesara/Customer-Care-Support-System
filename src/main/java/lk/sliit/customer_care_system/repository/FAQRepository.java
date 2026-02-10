package lk.sliit.customer_care_system.repository;

import lk.sliit.customer_care_system.modelentity.FAQ;
import lk.sliit.customer_care_system.modelentity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Long> {

    // Find all FAQs by category
    List<FAQ> findByCategory(String category);

    // Find all FAQs by category (case insensitive)
    List<FAQ> findByCategoryIgnoreCase(String category);

    // Find all distinct categories
    @Query("SELECT DISTINCT f.category FROM FAQ f ORDER BY f.category")
    List<String> findAllCategories();

    // Find FAQs containing keyword in question or answer
    @Query("SELECT f FROM FAQ f WHERE LOWER(f.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.answer) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<FAQ> findByKeyword(@Param("keyword") String keyword);

    // Find FAQs by category and keyword
    @Query("SELECT f FROM FAQ f WHERE LOWER(f.category) = LOWER(:category) AND (LOWER(f.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.answer) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<FAQ> findByCategoryAndKeyword(@Param("category") String category, @Param("keyword") String keyword);

    // Find all FAQs ordered by category and creation date
    List<FAQ> findAllByOrderByCategoryAscCreatedAtDesc();

    // Find all approved FAQs for users
    List<FAQ> findByIsApprovedTrueOrderByCategoryAscCreatedAtDesc();

    // Find approved FAQs by category
    List<FAQ> findByCategoryIgnoreCaseAndIsApprovedTrue(String category);

    // Find approved FAQs by keyword
    @Query("SELECT f FROM FAQ f WHERE f.isApproved = true AND (LOWER(f.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.answer) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<FAQ> findApprovedByKeyword(@Param("keyword") String keyword);

    // Find approved FAQs by category and keyword
    @Query("SELECT f FROM FAQ f WHERE f.isApproved = true AND LOWER(f.category) = LOWER(:category) AND (LOWER(f.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(f.answer) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<FAQ> findApprovedByCategoryAndKeyword(@Param("category") String category, @Param("keyword") String keyword);

    // Find all pending FAQs (not approved)
    List<FAQ> findByIsApprovedFalseOrderByCreatedAtDesc();

    // Find distinct categories from approved FAQs only
    @Query("SELECT DISTINCT f.category FROM FAQ f WHERE f.isApproved = true ORDER BY f.category")
    List<String> findApprovedCategories();

    // Find FAQs created by a specific user
    List<FAQ> findByCreatedBy(User user);

    // Find FAQs approved by a specific user
    List<FAQ> findByApprovedBy(User user);
}
