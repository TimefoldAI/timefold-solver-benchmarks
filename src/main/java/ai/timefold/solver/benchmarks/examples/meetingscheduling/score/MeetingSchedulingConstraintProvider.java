package ai.timefold.solver.benchmarks.examples.meetingscheduling.score;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.Joiners.greaterThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;

import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.Attendance;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingAssignment;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.PreferredAttendance;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.RequiredAttendance;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.Room;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.TimeGrain;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.PrecomputeFactory;
import ai.timefold.solver.core.api.score.stream.tri.TriConstraintStream;

public class MeetingSchedulingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                roomConflict(constraintFactory),
                avoidOvertime(constraintFactory),
                requiredAttendanceConflict(constraintFactory),
                requiredRoomCapacity(constraintFactory),
                startAndEndOnSameDay(constraintFactory),
                requiredAndPreferredAttendanceConflict(constraintFactory),
                preferredAttendanceConflict(constraintFactory),
                doMeetingsAsSoonAsPossible(constraintFactory),
                oneBreakBetweenConsecutiveMeetings(constraintFactory),
                overlappingMeetings(constraintFactory),
                assignLargerRoomsFirst(constraintFactory),
                roomStability(constraintFactory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    protected Constraint roomConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(MeetingAssignment.class,
                equal(MeetingAssignment::getRoom),
                overlapping(assignment -> assignment.getStartingTimeGrain().getGrainIndex(),
                        assignment -> assignment.getStartingTimeGrain().getGrainIndex() +
                                assignment.getMeeting().getDurationInGrains()))
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (leftAssignment, rightAssignment) -> rightAssignment.calculateOverlap(leftAssignment))
                .asConstraint("Room conflict");
    }

    protected Constraint avoidOvertime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .ifNotExists(TimeGrain.class,
                        equal(MeetingAssignment::getLastTimeGrainIndex,
                                TimeGrain::getGrainIndex))
                .penalize(HardMediumSoftScore.ONE_HARD, MeetingAssignment::getLastTimeGrainIndex)
                .asConstraint("Don't go in overtime");
    }

    protected Constraint requiredAttendanceConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.precompute(MeetingSchedulingConstraintProvider::requiredAttendanceAssignmentLeft)
                .filter((leftRequiredAttendance, rightRequiredAttendance,
                        assignment) -> assignment.getStartingTimeGrain() != null)
                .join(MeetingAssignment.class,
                        equal((leftRequiredAttendance, rightRequiredAttendance, leftAssignment) -> rightRequiredAttendance
                                .getMeeting(),
                                MeetingAssignment::getMeeting),
                        overlapping((attendee1, attendee2, assignment) -> assignment.getStartingTimeGrain().getGrainIndex(),
                                (attendee1, attendee2, assignment) -> assignment.getStartingTimeGrain().getGrainIndex() +
                                        assignment.getMeeting().getDurationInGrains(),
                                assignment -> assignment.getStartingTimeGrain().getGrainIndex(),
                                assignment -> assignment.getStartingTimeGrain().getGrainIndex() +
                                        assignment.getMeeting().getDurationInGrains()))
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (leftRequiredAttendance, rightRequiredAttendance, leftAssignment, rightAssignment) -> rightAssignment
                                .calculateOverlap(leftAssignment))
                .asConstraint("Required attendance conflict");
    }

    private static TriConstraintStream<RequiredAttendance, RequiredAttendance, MeetingAssignment>
            requiredAttendanceAssignmentLeft(PrecomputeFactory factory) {
        return factory.forEachUnfilteredUniquePair(RequiredAttendance.class,
                equal(RequiredAttendance::getPerson))
                .join(MeetingAssignment.class,
                        equal((leftRequiredAttendance, rightRequiredAttendance) -> leftRequiredAttendance.getMeeting(),
                                MeetingAssignment::getMeeting));
    }

    protected Constraint requiredRoomCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getRequiredCapacity() > meetingAssignment.getRoomCapacity())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        meetingAssignment -> meetingAssignment.getRequiredCapacity() - meetingAssignment.getRoomCapacity())
                .asConstraint("Required room capacity");
    }

    protected Constraint startAndEndOnSameDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .join(TimeGrain.class,
                        equal(MeetingAssignment::getLastTimeGrainIndex, TimeGrain::getGrainIndex),
                        filtering((meetingAssignment,
                                timeGrain) -> meetingAssignment.getStartingTimeGrain().getDay() != timeGrain.getDay()))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Start and end on same day");
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    protected Constraint requiredAndPreferredAttendanceConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.precompute(MeetingSchedulingConstraintProvider::requiredAndPreferredAttendanceAssignmentLeft)
                .filter((requiredAttendance, preferredAttendance, assignment) -> assignment.getStartingTimeGrain() != null)
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(assignment -> assignment.getStartingTimeGrain() != null),
                        equal((requiredAttendance, preferredAttendance, leftAssignment) -> preferredAttendance.getMeeting(),
                                MeetingAssignment::getMeeting),
                        overlapping((attendee1, attendee2, assignment) -> assignment.getStartingTimeGrain().getGrainIndex(),
                                (attendee1, attendee2, assignment) -> assignment.getStartingTimeGrain().getGrainIndex() +
                                        assignment.getMeeting().getDurationInGrains(),
                                assignment -> assignment.getStartingTimeGrain().getGrainIndex(),
                                assignment -> assignment.getStartingTimeGrain().getGrainIndex() +
                                        assignment.getMeeting().getDurationInGrains()))
                .penalize(HardMediumSoftScore.ONE_MEDIUM,
                        (requiredAttendance, preferredAttendance, leftAssignment, rightAssignment) -> rightAssignment
                                .calculateOverlap(leftAssignment))
                .asConstraint("Required and preferred attendance conflict");
    }

    private static TriConstraintStream<RequiredAttendance, PreferredAttendance, MeetingAssignment>
            requiredAndPreferredAttendanceAssignmentLeft(PrecomputeFactory factory) {
        return factory.forEachUnfiltered(RequiredAttendance.class)
                .join(PreferredAttendance.class,
                        equal(RequiredAttendance::getPerson, PreferredAttendance::getPerson))
                .join(MeetingAssignment.class,
                        equal((requiredAttendance, preferredAttendance) -> requiredAttendance.getMeeting(),
                                MeetingAssignment::getMeeting));
    }

    protected Constraint preferredAttendanceConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.precompute(MeetingSchedulingConstraintProvider::preferredAttendanceAssignmentLeft)
                .filter((leftPreferredAttendance, rightPreferredAttendance,
                        assignment) -> assignment.getStartingTimeGrain() != null)
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(assignment -> assignment.getStartingTimeGrain() != null),
                        equal((leftAttendance, rightAttendance, leftAssignment) -> rightAttendance.getMeeting(),
                                MeetingAssignment::getMeeting),
                        overlapping((attendee1, attendee2, assignment) -> assignment.getStartingTimeGrain().getGrainIndex(),
                                (attendee1, attendee2, assignment) -> assignment.getStartingTimeGrain().getGrainIndex() +
                                        assignment.getMeeting().getDurationInGrains(),
                                assignment -> assignment.getStartingTimeGrain().getGrainIndex(),
                                assignment -> assignment.getStartingTimeGrain().getGrainIndex() +
                                        assignment.getMeeting().getDurationInGrains()))
                .penalize(HardMediumSoftScore.ONE_MEDIUM,
                        (leftPreferredAttendance, rightPreferredAttendance, leftAssignment, rightAssignment) -> rightAssignment
                                .calculateOverlap(leftAssignment))
                .asConstraint("Preferred attendance conflict");
    }

    private static TriConstraintStream<PreferredAttendance, PreferredAttendance, MeetingAssignment>
            preferredAttendanceAssignmentLeft(PrecomputeFactory factory) {
        return factory.forEachUnfilteredUniquePair(PreferredAttendance.class,
                equal(PreferredAttendance::getPerson))
                .join(MeetingAssignment.class,
                        equal((leftAttendance, rightAttendance) -> leftAttendance.getMeeting(), MeetingAssignment::getMeeting));
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    protected Constraint doMeetingsAsSoonAsPossible(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .penalize(HardMediumSoftScore.ONE_SOFT, MeetingAssignment::getLastTimeGrainIndex)
                .asConstraint("Do all meetings as soon as possible");
    }

    protected Constraint oneBreakBetweenConsecutiveMeetings(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(assignment -> assignment.getStartingTimeGrain() != null),
                        equal(MeetingAssignment::getLastTimeGrainIndex,
                                (rightAssignment) -> rightAssignment.getStartingTimeGrain().getGrainIndex() - 1))
                .penalize(HardMediumSoftScore.ofSoft(100))
                .asConstraint("One TimeGrain break between two consecutive meetings");
    }

    protected Constraint overlappingMeetings(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
                .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                        .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null),
                        greaterThan((leftAssignment) -> leftAssignment.getMeeting().getId(),
                                (rightAssignment) -> rightAssignment.getMeeting().getId()),
                        overlapping(assignment -> assignment.getStartingTimeGrain().getGrainIndex(),
                                assignment -> assignment.getStartingTimeGrain().getGrainIndex() +
                                        assignment.getMeeting().getDurationInGrains()))
                .penalize(HardMediumSoftScore.ofSoft(10), MeetingAssignment::calculateOverlap)
                .asConstraint("Overlapping meetings");
    }

    protected Constraint assignLargerRoomsFirst(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
                .filter(meetingAssignment -> meetingAssignment.getRoom() != null)
                .join(Room.class,
                        lessThan(MeetingAssignment::getRoomCapacity, Room::getCapacity))
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        (meetingAssignment, room) -> room.getCapacity() - meetingAssignment.getRoomCapacity())
                .asConstraint("Assign larger rooms first");
    }

    protected Constraint roomStability(ConstraintFactory constraintFactory) {
        return constraintFactory.precompute(MeetingSchedulingConstraintProvider::roomStabilityJoin)
                .filter((leftAttendance, rightAttendance, assignment) -> assignment.getStartingTimeGrain() != null
                        && assignment.getRoom() != null)
                .join(MeetingAssignment.class,
                        equal((leftAttendance, rightAttendance, leftAssignment) -> rightAttendance.getMeeting(),
                                MeetingAssignment::getMeeting),
                        lessThan((leftAttendance, rightAttendance, leftAssignment) -> leftAssignment.getStartingTimeGrain(),
                                MeetingAssignment::getStartingTimeGrain),
                        filtering((leftAttendance, rightAttendance, leftAssignment,
                                rightAssignment) -> leftAssignment.getRoom() != rightAssignment.getRoom()),
                        filtering((leftAttendance, rightAttendance, leftAssignment,
                                rightAssignment) -> rightAssignment.getStartingTimeGrain().getGrainIndex() -
                                        leftAttendance.getMeeting().getDurationInGrains() -
                                        leftAssignment.getStartingTimeGrain().getGrainIndex() <= 2))
                .penalize(HardMediumSoftScore.ONE_SOFT)
                .asConstraint("Room stability");
    }

    private static TriConstraintStream<Attendance, Attendance, MeetingAssignment> roomStabilityJoin(PrecomputeFactory factory) {
        return factory.forEachUnfiltered(Attendance.class)
                .join(Attendance.class,
                        equal(Attendance::getPerson),
                        filtering((leftAttendance,
                                rightAttendance) -> leftAttendance.getMeeting() != rightAttendance.getMeeting()))
                .join(MeetingAssignment.class,
                        equal((leftAttendance, rightAttendance) -> leftAttendance.getMeeting(), MeetingAssignment::getMeeting));
    }

}
