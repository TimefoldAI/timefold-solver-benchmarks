package ai.timefold.solver.benchmarks.micro.moveprovider;

import java.io.File;

import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingAssignment;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingSchedule;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.persistence.MeetingSchedulingSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.score.MeetingSchedulingConstraintProvider;
import ai.timefold.solver.benchmarks.examples.pas.domain.BedDesignation;
import ai.timefold.solver.benchmarks.examples.pas.domain.PatientAdmissionSchedule;
import ai.timefold.solver.benchmarks.examples.pas.persistence.PatientAdmissionScheduleSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.pas.score.PatientAdmissionScheduleConstraintProvider;
import ai.timefold.solver.benchmarks.examples.taskassigning.domain.Employee;
import ai.timefold.solver.benchmarks.examples.taskassigning.domain.Task;
import ai.timefold.solver.benchmarks.examples.taskassigning.domain.TaskAssigningSolution;
import ai.timefold.solver.benchmarks.examples.taskassigning.persistence.TaskAssigningSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.taskassigning.score.TaskAssigningConstraintProvider;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Customer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Vehicle;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.score.VehicleRoutingConstraintProvider;
import ai.timefold.solver.benchmarks.micro.moveprovider.jmh.AbstractMoveProviderBenchmark;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.SolverConfig;

/**
 * The five datasets the move-provider benchmark suite reads, per
 * {@code /home/agent/.claude/plans/zany-drifting-ocean.md}. {@code MEETING_SCHEDULING_DENSE} is
 * deliberately 22.9x over-subscribed so that composite (room, startingTimeGrain) pillars average 4
 * members instead of the singletons every feasible meeting-scheduling shape produces; only
 * {@code PILLAR_SWAP} and {@code SUB_PILLAR_SWAP} (in {@code BasicMoveProviderCase}) use it.
 * {@code PATIENT_ADMISSION_SCHEDULING} and {@code TASK_ASSIGNING} are fully assigned datasets, so
 * every {@link #UNASSIGN_EVERY_NTH} loaded designation/task is unassigned once at load time, or the
 * *Assign* providers would draw from an empty source.
 */
public enum Example {

    MEETING_SCHEDULING {
        @Override
        public SolverConfig buildSolverConfig() {
            return buildMeetingSchedulingSolverConfig();
        }

        @Override
        public <Solution_> Solution_ loadDataset() {
            var io = new MeetingSchedulingSolutionFileIO();
            return (Solution_) io.read(new File("data/meetingscheduling/meetingscheduling-400-1280-5.json"));
        }
    },
    MEETING_SCHEDULING_DENSE {
        @Override
        public SolverConfig buildSolverConfig() {
            return buildMeetingSchedulingSolverConfig();
        }

        @Override
        public <Solution_> Solution_ loadDataset() {
            var io = new MeetingSchedulingSolutionFileIO();
            return (Solution_) io.read(new File("data/meetingscheduling/meetingscheduling-1600-80-5.json"));
        }
    },
    PATIENT_ADMISSION_SCHEDULING {
        @Override
        public SolverConfig buildSolverConfig() {
            return new SolverConfig()
                    .withSolutionClass(PatientAdmissionSchedule.class)
                    .withEntityClasses(BedDesignation.class)
                    .withScoreDirectorFactory(new ScoreDirectorFactoryConfig()
                            .withConstraintProviderClass(PatientAdmissionScheduleConstraintProvider.class));
        }

        @Override
        public <Solution_> Solution_ loadDataset() {
            var io = new PatientAdmissionScheduleSolutionFileIO();
            var solution = io.read(new File("data/pas/pas-12.json"));
            partiallyUnassignBeds(solution);
            return (Solution_) solution;
        }
    },
    VEHICLE_ROUTING {
        @Override
        public SolverConfig buildSolverConfig() {
            return new SolverConfig()
                    .withSolutionClass(VehicleRoutingSolution.class)
                    .withEntityClasses(Vehicle.class, Customer.class)
                    .withScoreDirectorFactory(new ScoreDirectorFactoryConfig()
                            .withConstraintProviderClass(VehicleRoutingConstraintProvider.class));
        }

        @Override
        public <Solution_> Solution_ loadDataset() {
            var io = new VehicleRoutingSolutionFileIO();
            return (Solution_) io.read(new File("data/vehiclerouting/vehiclerouting-RC2_4_10.json"));
        }
    },
    TASK_ASSIGNING {
        @Override
        public SolverConfig buildSolverConfig() {
            return new SolverConfig()
                    .withSolutionClass(TaskAssigningSolution.class)
                    .withEntityClasses(Employee.class, Task.class)
                    .withScoreDirectorFactory(new ScoreDirectorFactoryConfig()
                            .withConstraintProviderClass(TaskAssigningConstraintProvider.class));
        }

        @Override
        public <Solution_> Solution_ loadDataset() {
            var io = new TaskAssigningSolutionFileIO();
            var solution = io.read(new File("data/taskassigning/taskassigning-500-20.json"));
            partiallyUnassignTasks(solution);
            return (Solution_) solution;
        }
    };

    private static SolverConfig buildMeetingSchedulingSolverConfig() {
        return new SolverConfig()
                .withSolutionClass(MeetingSchedule.class)
                .withEntityClasses(MeetingAssignment.class)
                .withScoreDirectorFactory(new ScoreDirectorFactoryConfig()
                        .withConstraintProviderClass(MeetingSchedulingConstraintProvider.class));
    }

    private static void partiallyUnassignBeds(PatientAdmissionSchedule solution) {
        var bedDesignationList = solution.getBedDesignationList();
        for (var i = 0; i < bedDesignationList.size(); i++) {
            if (i % AbstractMoveProviderBenchmark.UNASSIGN_EVERY_NTH == 0) {
                bedDesignationList.get(i).setBed(null);
            }
        }
    }

    private static void partiallyUnassignTasks(TaskAssigningSolution solution) {
        var index = 0;
        for (var employee : solution.getEmployeeList()) {
            var taskList = employee.getTasks();
            var taskIterator = taskList.iterator();
            while (taskIterator.hasNext()) {
                taskIterator.next();
                if (index % AbstractMoveProviderBenchmark.UNASSIGN_EVERY_NTH == 0) {
                    taskIterator.remove();
                }
                index++;
            }
        }
    }

    public abstract SolverConfig buildSolverConfig();

    public abstract <Solution_> Solution_ loadDataset();

}
