package lk.sliit.customer_care_system.repository;

import lk.sliit.customer_care_system.modelentity.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
}
