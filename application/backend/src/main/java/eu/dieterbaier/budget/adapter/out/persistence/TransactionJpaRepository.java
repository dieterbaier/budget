package eu.dieterbaier.budget.adapter.out.persistence;

import eu.dieterbaier.budget.adapter.out.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Long> {

    List<TransactionEntity> findByBookingDateBetween(LocalDate start, LocalDate end);
}
