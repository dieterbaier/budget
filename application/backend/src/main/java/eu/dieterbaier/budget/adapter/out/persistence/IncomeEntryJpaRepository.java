package eu.dieterbaier.budget.adapter.out.persistence;

import eu.dieterbaier.budget.adapter.out.persistence.entity.IncomeEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface IncomeEntryJpaRepository extends JpaRepository<IncomeEntryEntity, Long> {

    /** Average of all recorded monthly income amounts; null when no income is recorded. */
    @Query("select avg(e.amount) from IncomeEntryEntity e")
    BigDecimal averageAmount();
}
