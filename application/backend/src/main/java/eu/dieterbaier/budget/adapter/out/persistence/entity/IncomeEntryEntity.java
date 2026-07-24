package eu.dieterbaier.budget.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "income_entries")
public class IncomeEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "income_month", nullable = false)
    private LocalDate incomeMonth;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    protected IncomeEntryEntity() {
    }

    public IncomeEntryEntity(LocalDate incomeMonth, BigDecimal amount) {
        this.incomeMonth = incomeMonth;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getIncomeMonth() {
        return incomeMonth;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
