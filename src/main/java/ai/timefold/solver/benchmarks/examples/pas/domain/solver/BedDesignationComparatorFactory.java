package ai.timefold.solver.benchmarks.examples.pas.domain.solver;

import java.util.Comparator;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.pas.domain.BedDesignation;
import ai.timefold.solver.benchmarks.examples.pas.domain.PatientAdmissionSchedule;
import ai.timefold.solver.core.api.domain.common.ComparatorFactory;

public class BedDesignationComparatorFactory implements ComparatorFactory<PatientAdmissionSchedule, BedDesignation> {

    @Override
    public Comparator<BedDesignation> createComparator(PatientAdmissionSchedule schedule) {
        return Comparator
                .<BedDesignation> comparingInt(
                        bedDesignation -> bedDesignation.getPatient().getRequiredPatientEquipmentList().size()
                                * bedDesignation.getAdmissionPart().getNightCount())
                .thenComparingInt(bedDesignation -> bedDesignation.getHardDisallowedCount(schedule)
                        * bedDesignation.getAdmissionPart().getNightCount())
                .thenComparingInt(bedDesignation -> bedDesignation.getAdmissionPart().getNightCount())
                .thenComparingInt(bedDesignation -> bedDesignation.getSoftDisallowedCount(schedule)
                        * bedDesignation.getAdmissionPart().getNightCount())
                // Descending (earlier nights are more difficult) // TODO probably because less occupancy
                .thenComparingInt(bedDesignation -> -bedDesignation.getAdmissionPart().getFirstNight().getIndex())
                .thenComparingLong(AbstractPersistable::getId);
    }
}
