package lk.sliit.customer_care_system.modelentity;

import jakarta.persistence.*;

@Entity
@Table(name = "TestEntity")   // 👈 forces SQL table name
public class TestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
