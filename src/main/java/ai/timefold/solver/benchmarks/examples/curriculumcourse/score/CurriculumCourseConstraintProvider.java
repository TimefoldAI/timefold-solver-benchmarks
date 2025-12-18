package ai.timefold.solver.benchmarks.examples.curriculumcourse.score;

import static ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore.ONE_HARD;
import static ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore.ofHard;
import static ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore.ofSoft;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.countDistinct;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;

import java.util.Objects;

import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.Curriculum;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.Lecture;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.UnavailablePeriodPenalty;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.domain.solver.CourseConflict;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.PrecomputeFactory;
import ai.timefold.solver.core.api.score.stream.bi.BiConstraintStream;
import ai.timefold.solver.core.api.score.stream.common.SequenceChain;
import ai.timefold.solver.core.api.score.stream.tri.TriConstraintStream;

public class CurriculumCourseConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                conflictingLecturesDifferentCourseInSamePeriod(factory),
                conflictingLecturesSameCourseInSamePeriod(factory),
                roomOccupancy(factory),
                unavailablePeriodPenalty(factory),
                roomCapacity(factory),
                minimumWorkingDays(factory),
                curriculumCompactness(factory),
                roomStability(factory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    Constraint conflictingLecturesDifferentCourseInSamePeriod(ConstraintFactory factory) {
        return factory.precompute(CurriculumCourseConstraintProvider::conflictingCourseLeft)
                .filter(((courseConflict, lecture1, lecture2) -> Objects.equals(lecture1.getPeriod(), lecture2.getPeriod())))
                .penalize(ONE_HARD,
                        (courseConflict, lecture1, lecture2) -> courseConflict.getConflictCount())
                .asConstraint("conflictingLecturesDifferentCourseInSamePeriod");
    }

    private static TriConstraintStream<CourseConflict, Lecture, Lecture> conflictingCourseLeft(PrecomputeFactory factory) {
        return factory.forEachUnfiltered(CourseConflict.class)
                .join(Lecture.class,
                        equal(CourseConflict::getLeftCourse, Lecture::getCourse))
                .join(Lecture.class,
                        equal((courseConflict, lecture1) -> courseConflict.getRightCourse(), Lecture::getCourse),
                        filtering((courseConflict, lecture1, lecture2) -> lecture1 != lecture2));
    }

    Constraint conflictingLecturesSameCourseInSamePeriod(ConstraintFactory factory) {
        return factory.forEachUniquePair(Lecture.class,
                equal(Lecture::getPeriod),
                equal(Lecture::getCourse))
                .penalize(ONE_HARD,
                        (lecture1, lecture2) -> 1 + lecture1.getCurriculumSet().size())
                .asConstraint("conflictingLecturesSameCourseInSamePeriod");
    }

    Constraint roomOccupancy(ConstraintFactory factory) { // Faster than a simpler forEachUniquePair(Lecture.class).
        return factory.forEach(Lecture.class)
                .groupBy(Lecture::getRoom, Lecture::getPeriod, count())
                .filter((room, period, count) -> count > 1)
                .penalize(ONE_HARD, (room, period, count) -> {
                    var n = 2; // We're looking for unique pairs.
                    var nominator = factorial(count);
                    var denominator = factorial(n) * factorial(count - n);
                    return nominator / denominator;
                })
                .asConstraint("roomOccupancy");
    }

    private static final int MAX_ANTICIPATED_CONFLICTING_LESSONS = 20; // Arbitrary limit for caching.
    private static final int[] FACTORIAL_CACHE = new int[MAX_ANTICIPATED_CONFLICTING_LESSONS];

    private static int factorial(int number) {
        if (number < MAX_ANTICIPATED_CONFLICTING_LESSONS) {
            var cache = FACTORIAL_CACHE[number];
            if (cache == 0) {
                cache = factorialUncached(number);
                FACTORIAL_CACHE[number] = cache;
            }
            return cache;
        }
        return factorialUncached(number);
    }

    private static int factorialUncached(int number) {
        return switch (number) {
            case 0, 1 -> 1;
            case 2 -> 2;
            default -> number * factorial(number - 1);
        };
    }

    Constraint unavailablePeriodPenalty(ConstraintFactory factory) {
        return factory.forEach(UnavailablePeriodPenalty.class)
                .join(Lecture.class,
                        equal(UnavailablePeriodPenalty::getCourse, Lecture::getCourse),
                        equal(UnavailablePeriodPenalty::getPeriod, Lecture::getPeriod))
                .penalize(ofHard(10))
                .asConstraint("unavailablePeriodPenalty");
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    Constraint roomCapacity(ConstraintFactory factory) {
        return factory.forEach(Lecture.class)
                .filter(lecture -> lecture.getStudentSize() > lecture.getRoom().getCapacity())
                .penalize(ofSoft(1),
                        lecture -> lecture.getStudentSize() - lecture.getRoom().getCapacity())
                .asConstraint("roomCapacity");
    }

    Constraint minimumWorkingDays(ConstraintFactory factory) {
        return factory.forEach(Lecture.class)
                .groupBy(Lecture::getCourse, countDistinct(Lecture::getDay))
                .filter((course, dayCount) -> course.getMinWorkingDaySize() > dayCount)
                .penalize(ofSoft(5),
                        (course, dayCount) -> course.getMinWorkingDaySize() - dayCount)
                .asConstraint("minimumWorkingDays");
    }

    Constraint curriculumCompactness(ConstraintFactory factory) {
        return factory.precompute(CurriculumCourseConstraintProvider::curriculumLectureLeft)
                .groupBy((c, l) -> c,
                        (c, l) -> l.getDay(),
                        ConstraintCollectors.conditionally(
                                (c, l) -> l.getDay() != null,
                                ConstraintCollectors.toConsecutiveSequences(
                                        (Curriculum c, Lecture l) -> l,
                                        Lecture::getTimeslotIndex)))
                .flattenLast(SequenceChain::getConsecutiveSequences)
                .filter((curriculum, day, sequence) -> sequence.getLength() == 1)
                .penalize(ofSoft(2))
                .asConstraint("curriculumCompactness");
    }

    private static BiConstraintStream<Curriculum, Lecture> curriculumLectureLeft(PrecomputeFactory factory) {
        return factory.forEachUnfiltered(Curriculum.class)
                .join(Lecture.class,
                        filtering((curriculum, lecture) -> lecture.getCurriculumSet().contains(curriculum)));
    }

    Constraint roomStability(ConstraintFactory factory) {
        return factory.forEach(Lecture.class)
                .groupBy(Lecture::getCourse, countDistinct(Lecture::getRoom))
                .filter((course, roomCount) -> roomCount > 1)
                .penalize(HardSoftScore.ONE_SOFT,
                        (course, roomCount) -> roomCount - 1)
                .asConstraint("roomStability");
    }

}
