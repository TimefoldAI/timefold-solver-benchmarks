package ai.timefold.solver.benchmarks.examples.app;

import ai.timefold.solver.benchmarks.examples.cloudbalancing.persistence.CloudBalancingGenerator;
import ai.timefold.solver.benchmarks.examples.common.app.LoggingMain;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.persistence.ConferenceSchedulingGenerator;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.persistence.CurriculumCourseImporter;
import ai.timefold.solver.benchmarks.examples.examination.persistence.ExaminationImporter;
import ai.timefold.solver.benchmarks.examples.flightcrewscheduling.persistence.FlightCrewSchedulingGenerator;
import ai.timefold.solver.benchmarks.examples.machinereassignment.persistence.MachineReassignmentImporter;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.persistence.MeetingSchedulingGenerator;
import ai.timefold.solver.benchmarks.examples.nurserostering.persistence.NurseRosteringImporter;
import ai.timefold.solver.benchmarks.examples.pas.persistence.PatientAdmissionScheduleImporter;
import ai.timefold.solver.benchmarks.examples.projectjobscheduling.persistence.ProjectJobSchedulingImporter;
import ai.timefold.solver.benchmarks.examples.taskassigning.persistence.TaskAssigningGenerator;
import ai.timefold.solver.benchmarks.examples.tennis.persistence.TennisGenerator;
import ai.timefold.solver.benchmarks.examples.travelingtournament.persistence.TravelingTournamentImporter;
import ai.timefold.solver.benchmarks.examples.tsp.persistence.TspImporter;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence.VehicleRoutingImporter;

public class AllExamplesSolutionImporter extends LoggingMain {

    public static void main(String[] args) {
        new AllExamplesSolutionImporter().importAll();
    }

    public void importAll() {
        CloudBalancingGenerator.main(new String[0]);
        ConferenceSchedulingGenerator.main(new String[0]);
        CurriculumCourseImporter.main(new String[0]);
        ExaminationImporter.main(new String[0]);
        FlightCrewSchedulingGenerator.main(new String[0]);
        MachineReassignmentImporter.main(new String[0]);
        MeetingSchedulingGenerator.main(new String[0]);
        NurseRosteringImporter.main(new String[0]);
        PatientAdmissionScheduleImporter.main(new String[0]);
        ProjectJobSchedulingImporter.main(new String[0]);
        TaskAssigningGenerator.main(new String[0]);
        TennisGenerator.main(new String[0]);
        TravelingTournamentImporter.main(new String[0]);
        TspImporter.main(new String[0]);
        VehicleRoutingImporter.main(new String[0]);
    }

}
