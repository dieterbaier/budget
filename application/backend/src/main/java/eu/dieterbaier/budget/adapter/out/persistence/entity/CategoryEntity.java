package eu.dieterbaier.budget.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "pension_relevant", nullable = false)
    private boolean pensionRelevant;

    protected CategoryEntity() {
    }

    public CategoryEntity(String name, boolean pensionRelevant) {
        this.name = name;
        this.pensionRelevant = pensionRelevant;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isPensionRelevant() {
        return pensionRelevant;
    }
}
