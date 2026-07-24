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
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType;

    protected TransactionEntity() {
    }

    public TransactionEntity(LocalDate bookingDate, BigDecimal amount, CategoryEntity category, String transactionType) {
        this.bookingDate = bookingDate;
        this.amount = amount;
        this.category = category;
        this.transactionType = transactionType;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public String getTransactionType() {
        return transactionType;
    }
}
