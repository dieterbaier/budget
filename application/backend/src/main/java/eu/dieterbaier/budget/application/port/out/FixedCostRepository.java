package eu.dieterbaier.budget.application.port.out;

import eu.dieterbaier.budget.domain.model.FixedCost;

import java.util.List;

/** Outbound port for reading fixed-cost definitions. Implemented by a persistence adapter. */
public interface FixedCostRepository {

    List<FixedCost> findAll();
}
