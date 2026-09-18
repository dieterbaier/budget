package eu.dieterbaier.budget.application.port.out;

import eu.dieterbaier.budget.domain.model.Fixkostenposition;

import java.util.List;

/** Outbound port for reading Fixkostenposition definitions. Implemented by a persistence adapter. */
public interface FixkostenpositionRepository {

    List<Fixkostenposition> findAll();
}
