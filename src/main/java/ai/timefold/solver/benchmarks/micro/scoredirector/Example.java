package ai.timefold.solver.benchmarks.micro.scoredirector;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.solver.nearby.CustomerNearbyDistanceMeter;
import ai.timefold.solver.benchmarks.micro.scoredirector.problems.CloudBalancingProblem;
import ai.timefold.solver.benchmarks.micro.scoredirector.problems.ConferenceSchedulingProblem;
import ai.timefold.solver.benchmarks.micro.scoredirector.problems.CurriculumCourseProblem;
import ai.timefold.solver.benchmarks.micro.scoredirector.problems.ExaminationProblem;
import ai.timefold.solver.benchmarks.micro.scoredirector.problems.FlowShopProblem;
import ai.timefold.solver.benchmarks.micro.scoredirector.problems.MachineReassignmentProblem;
import ai.timefold.solver.benchmarks.micro.scoredirector.problems.MeetingSchedulingProblem;
import ai.timefold.solver.benchmarks.micro.scoredirector.problems.NurseRosteringProblem;
import ai.timefold.solver.benchmarks.micro.scoredirector.problems.PatientAdmissionSchedulingProblem;
import ai.timefold.solver.benchmarks.micro.scoredirector.problems.Problem;
import ai.timefold.solver.benchmarks.micro.scoredirector.problems.TaskAssigningProblem;
import ai.timefold.solver.benchmarks.micro.scoredirector.problems.TravelingTournamentProblem;
import ai.timefold.solver.benchmarks.micro.scoredirector.problems.VehicleRoutingProblem;
import ai.timefold.solver.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;

public enum Example {

    CLOUD_BALANCING(CloudBalancingProblem::new),
    CONFERENCE_SCHEDULING(ConferenceSchedulingProblem::new),
    CURRICULUM_COURSE(CurriculumCourseProblem::new),
    EXAMINATION(ExaminationProblem::new),
    FLOW_SHOP(FlowShopProblem::new),
    MACHINE_REASSIGNMENT(MachineReassignmentProblem::new),
    MEETING_SCHEDULING(MeetingSchedulingProblem::new),
    NURSE_ROSTERING(NurseRosteringProblem::new),
    PATIENT_ADMISSION_SCHEDULING(PatientAdmissionSchedulingProblem::new),
    TASK_ASSIGNING(TaskAssigningProblem::new),
    TRAVELING_TOURNAMENT(TravelingTournamentProblem::new),
    VEHICLE_ROUTING(VehicleRoutingProblem::new, CustomerNearbyDistanceMeter.class);

    private final Function<ScoreDirectorType, Problem> problemFactory;
    private final Class<? extends NearbyDistanceMeter<?, ?>> nearbyDistanceMeter;
    private final Set<ScoreDirectorType> supportedScoreDirectorTypes;

    Example(Function<ScoreDirectorType, Problem> problemFactory, ScoreDirectorType... supportedScoreDirectorType) {
        this(problemFactory, null, supportedScoreDirectorType);
    }

    Example(Function<ScoreDirectorType, Problem> problemFactory, Class<? extends NearbyDistanceMeter<?, ?>> nearbyDistanceMeter,
            ScoreDirectorType... supportedScoreDirectorType) {
        this.problemFactory = Objects.requireNonNull(problemFactory);
        this.nearbyDistanceMeter = nearbyDistanceMeter;
        if (supportedScoreDirectorType.length == 0) {
            this.supportedScoreDirectorTypes = EnumSet.allOf(ScoreDirectorType.class);
        } else {
            this.supportedScoreDirectorTypes = EnumSet.copyOf(Arrays.asList(supportedScoreDirectorType));
        }
    }

    public Optional<Class<? extends NearbyDistanceMeter<?, ?>>> getNearbyDistanceMeter() {
        return Optional.ofNullable(nearbyDistanceMeter);
    }

    public boolean isSupportedOn(ScoreDirectorType scoreDirectorType) {
        return supportedScoreDirectorTypes.contains(scoreDirectorType);
    }

    public Problem create(ScoreDirectorType scoreDirectorType) {
        if (!isSupportedOn(scoreDirectorType)) {
            throw new IllegalArgumentException("Unsupported score director (" + scoreDirectorType + ") for example ("
                    + this + ").");
        }
        return problemFactory.apply(scoreDirectorType);
    }

    public String getDirectoryName() {
        if (this == PATIENT_ADMISSION_SCHEDULING) {
            return "pas";
        }
        return name().toLowerCase().replace("_", "");
    }

}
