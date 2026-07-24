package eu.dieterbaier.budget.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fixed_costs")
public class FixedCostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_interval", nullable = false, length = 20)
    private String paymentInterval;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name = "anchor_date", nullable = false)
    private LocalDate anchorDate;

    protected FixedCostEntity() {
    }

    public FixedCostEntity(String name, BigDecimal amount, String paymentInterval, CategoryEntity category,
                           LocalDate anchorDate) {
        this.name = name;
        this.amount = amount;
        this.paymentInterval = paymentInterval;
        this.category = category;
        this.anchorDate = anchorDate;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentInterval() {
        return paymentInterval;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public LocalDate getAnchorDate() {
        return anchorDate;
    }
}
