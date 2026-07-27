package eu.dieterbaier.budget.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Persistence view of a category. The surrogate id stays here and never reaches
 * the domain (ADR-009); it is what carries a transaction's reference across a
 * rename, so the domain needs no propagation logic (ADR-021).
 */
@Entity
@Table(name = "categories")
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private CategoryGroupEntity group;

    @Column(name = "pension_relevant", nullable = false)
    private boolean pensionRelevant;

    protected CategoryEntity() {
    }

    public CategoryEntity(String name, CategoryGroupEntity group, boolean pensionRelevant) {
        this.name = name;
        this.group = group;
        this.pensionRelevant = pensionRelevant;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CategoryGroupEntity getGroup() {
        return group;
    }

    public boolean isPensionRelevant() {
        return pensionRelevant;
    }

    public void update(String newName, CategoryGroupEntity newGroup, boolean newPensionRelevant) {
        this.name = newName;
        this.group = newGroup;
        this.pensionRelevant = newPensionRelevant;
    }
}
