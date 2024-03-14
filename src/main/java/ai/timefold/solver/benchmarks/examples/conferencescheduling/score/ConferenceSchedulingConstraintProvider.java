package ai.timefold.solver.benchmarks.examples.conferencescheduling.score;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.compose;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.countBi;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.max;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.min;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.Joiners.greaterThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Objects;

import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintConfiguration;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.Speaker;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.Talk;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

/**
 * Provides the constraints for the conference scheduling problem.
 * <p>
 * Makes heavy use of CS expand() functionality to cache computation results,
 * except in cases where doing so less is efficient than recomputing the result.
 * That is the case in filtering joiners.
 * In this case, it is better to reduce the size of the joins even at the expense of duplicating some calculations.
 * In other words, time saved by caching those calculations is far outweighed by the time spent in unrestricted joins.
 */
public class ConferenceSchedulingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                // Hard constraints
                roomUnavailableTimeslot(factory),
                roomConflict(factory),
                speakerUnavailableTimeslot(factory),
                speakerConflict(factory),
                talkPrerequisiteTalks(factory),
                talkMutuallyExclusiveTalksTags(factory),
                consecutiveTalksPause(factory),
                crowdControl(factory),
                speakerRequiredTimeslotTags(factory),
                speakerProhibitedTimeslotTags(factory),
                talkRequiredTimeslotTags(factory),
                talkProhibitedTimeslotTags(factory),
                speakerRequiredRoomTags(factory),
                speakerProhibitedRoomTags(factory),
                talkRequiredRoomTags(factory),
                talkProhibitedRoomTags(factory),
                // Medium constraints
                publishedTimeslot(factory),
                // Soft constraints
                publishedRoom(factory),
                themeTrackConflict(factory),
                themeTrackRoomStability(factory),
                sectorConflict(factory),
                audienceTypeDiversity(factory),
                audienceTypeThemeTrackConflict(factory),
                audienceLevelDiversity(factory),
                contentAudienceLevelFlowViolation(factory),
                contentConflict(factory),
                languageDiversity(factory),
                sameDayTalks(factory),
                popularTalks(factory),
                speakerPreferredTimeslotTags(factory),
                speakerUndesiredTimeslotTags(factory),
                talkPreferredTimeslotTags(factory),
                talkUndesiredTimeslotTags(factory),
                speakerPreferredRoomTags(factory),
                speakerUndesiredRoomTags(factory),
                talkPreferredRoomTags(factory),
                talkUndesiredRoomTags(factory),
                speakerMakespan(factory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    Constraint roomUnavailableTimeslot(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .filter(Talk::hasUnavailableRoom)
                .penalizeConfigurable(
                        Talk::getDurationInMinutes)
                .asConstraint(
                        ConferenceConstraintConfiguration.ROOM_UNAVAILABLE_TIMESLOT);
    }

    Constraint roomConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                equal(Talk::getRoom),
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()))
                .penalizeConfigurable(
                        Talk::overlappingDurationInMinutes)
                .asConstraint(
                        ConferenceConstraintConfiguration.ROOM_CONFLICT);
    }

    Constraint speakerUnavailableTimeslot(ConstraintFactory factory) {
        return factory.forEachIncludingUnassigned(Talk.class)
                .filter(talk -> talk.getTimeslot() != null)
                .join(Speaker.class,
                        filtering((talk, speaker) -> talk.hasSpeaker(speaker)
                                && speaker.getUnavailableTimeslotSet().contains(talk.getTimeslot())))
                .penalizeConfigurable((talk, speaker) -> talk.getDurationInMinutes())
                .asConstraint(
                        ConferenceConstraintConfiguration.SPEAKER_UNAVAILABLE_TIMESLOT);
    }

    Constraint speakerConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()))
                .join(Speaker.class,
                        filtering((talk1, talk2, speaker) -> talk1.hasSpeaker(speaker) && talk2.hasSpeaker(speaker)))
                .penalizeConfigurable((talk1, talk2, speaker) -> talk2.overlappingDurationInMinutes(talk1))
                .asConstraint(
                        ConferenceConstraintConfiguration.SPEAKER_CONFLICT);
    }

    Constraint talkPrerequisiteTalks(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .join(Talk.class,
                        greaterThan(t -> t.getTimeslot().getEndDateTime(), t -> t.getTimeslot().getStartDateTime()),
                        filtering((talk1, talk2) -> talk2.getPrerequisiteTalkSet().contains(talk1)))
                .penalizeConfigurable(
                        Talk::combinedDurationInMinutes)
                .asConstraint(
                        ConferenceConstraintConfiguration.TALK_PREREQUISITE_TALKS);
    }

    Constraint talkMutuallyExclusiveTalksTags(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()),
                filtering((talk1, talk2) -> talk2.overlappingMutuallyExclusiveTalksTagCount(talk1) > 0))
                .penalizeConfigurable((talk1, talk2) -> talk1.overlappingMutuallyExclusiveTalksTagCount(talk2) *
                        talk1.overlappingDurationInMinutes(talk2))
                .asConstraint(
                        ConferenceConstraintConfiguration.TALK_MUTUALLY_EXCLUSIVE_TALKS_TAGS);
    }

    Constraint consecutiveTalksPause(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                filtering((talk1, talk2) -> talk2.hasMutualSpeaker(talk1)))
                .ifExists(
                        ConferenceConstraintConfiguration.class,
                        filtering((talk1, talk2, config) -> !talk1.getTimeslot().pauseExists(talk2.getTimeslot(),
                                config.getMinimumConsecutiveTalksPauseInMinutes())))
                .penalizeConfigurable(
                        Talk::combinedDurationInMinutes)
                .asConstraint(
                        ConferenceConstraintConfiguration.CONSECUTIVE_TALKS_PAUSE);
    }

    Constraint crowdControl(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .filter(talk -> talk.getCrowdControlRisk() > 0)
                .join(factory.forEach(Talk.class)
                        .filter(talk -> talk.getCrowdControlRisk() > 0),
                        equal(Talk::getTimeslot))
                .filter((talk1, talk2) -> !Objects.equals(talk1, talk2))
                .groupBy((talk1, talk2) -> talk1, countBi())
                .filter((talk, count) -> count != 1)
                .penalizeConfigurable((talk, count) -> talk.getDurationInMinutes())
                .asConstraint(
                        ConferenceConstraintConfiguration.CROWD_CONTROL);
    }

    Constraint speakerRequiredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingSpeakerRequiredTimeslotTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.SPEAKER_REQUIRED_TIMESLOT_TAGS);
    }

    Constraint speakerProhibitedTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingSpeakerProhibitedTimeslotTagCount)
                .filter((talk, prohibitedTagCount) -> prohibitedTagCount > 0)
                .penalizeConfigurable((talk, prohibitedTagCount) -> prohibitedTagCount * talk.getDurationInMinutes())
                .indictWith((talk, prohibitedTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.SPEAKER_PROHIBITED_TIMESLOT_TAGS);
    }

    Constraint talkRequiredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingRequiredTimeslotTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.TALK_REQUIRED_TIMESLOT_TAGS);
    }

    Constraint talkProhibitedTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingProhibitedTimeslotTagCount)
                .filter((talk, prohibitedTagCount) -> prohibitedTagCount > 0)
                .penalizeConfigurable((talk, prohibitedTagCount) -> prohibitedTagCount * talk.getDurationInMinutes())
                .indictWith((talk, prohibitedTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.TALK_PROHIBITED_TIMESLOT_TAGS);
    }

    Constraint speakerRequiredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingSpeakerRequiredRoomTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.SPEAKER_REQUIRED_ROOM_TAGS);
    }

    Constraint speakerProhibitedRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingSpeakerProhibitedRoomTagCount)
                .filter((talk, prohibitedTagCount) -> prohibitedTagCount > 0)
                .penalizeConfigurable((talk, prohibitedTagCount) -> prohibitedTagCount * talk.getDurationInMinutes())
                .indictWith((talk, prohibitedTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.SPEAKER_PROHIBITED_ROOM_TAGS);
    }

    Constraint talkRequiredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingRequiredRoomTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.TALK_REQUIRED_ROOM_TAGS);
    }

    Constraint talkProhibitedRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingProhibitedRoomTagCount)
                .filter((talk, prohibitedTagCount) -> prohibitedTagCount > 0)
                .penalizeConfigurable((talk, prohibitedTagCount) -> prohibitedTagCount * talk.getDurationInMinutes())
                .indictWith((talk, prohibitedTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.TALK_PROHIBITED_ROOM_TAGS);
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    Constraint publishedTimeslot(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .filter(talk -> talk.getPublishedTimeslot() != null
                        && talk.getTimeslot() != talk.getPublishedTimeslot())
                .penalizeConfigurable()
                .asConstraint(
                        ConferenceConstraintConfiguration.PUBLISHED_TIMESLOT);
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    Constraint publishedRoom(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .filter(talk -> talk.getPublishedRoom() != null && talk.getRoom() != talk.getPublishedRoom())
                .penalizeConfigurable()
                .asConstraint(
                        ConferenceConstraintConfiguration.PUBLISHED_ROOM);
    }

    Constraint themeTrackConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()),
                filtering((talk1, talk2) -> talk2.overlappingThemeTrackCount(talk1) > 0))
                .penalizeConfigurable((talk1, talk2) -> talk1.overlappingThemeTrackCount(talk2) *
                        talk1.overlappingDurationInMinutes(talk2))
                .asConstraint(
                        ConferenceConstraintConfiguration.THEME_TRACK_CONFLICT);
    }

    Constraint themeTrackRoomStability(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                equal(talk -> talk.getTimeslot().getStartDateTime().toLocalDate()),
                filtering((talk1, talk2) -> talk2.overlappingThemeTrackCount(talk1) > 0))
                .filter((talk1, talk2) -> talk1.getRoom() != talk2.getRoom())
                .penalizeConfigurable((talk1, talk2) -> talk1.overlappingThemeTrackCount(talk2) *
                        talk1.combinedDurationInMinutes(talk2))
                .asConstraint(
                        ConferenceConstraintConfiguration.THEME_TRACK_ROOM_STABILITY);
    }

    Constraint sectorConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()),
                filtering((talk1, talk2) -> talk2.overlappingSectorCount(talk1) > 0))
                .penalizeConfigurable((talk1, talk2) -> talk1.overlappingSectorCount(talk2)
                        * talk1.overlappingDurationInMinutes(talk2))
                .asConstraint(
                        ConferenceConstraintConfiguration.SECTOR_CONFLICT);
    }

    Constraint audienceTypeDiversity(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                equal(Talk::getTimeslot),
                filtering((talk1, talk2) -> talk2.overlappingAudienceTypeCount(talk1) > 0))
                .rewardConfigurable((talk1, talk2) -> talk1.overlappingAudienceTypeCount(talk2)
                        * talk1.getTimeslot().getDurationInMinutes())
                .asConstraint(
                        ConferenceConstraintConfiguration.AUDIENCE_TYPE_DIVERSITY);
    }

    Constraint audienceTypeThemeTrackConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()),
                filtering((talk1, talk2) -> talk2.overlappingThemeTrackCount(talk1) > 0),
                filtering((talk1, talk2) -> talk2.overlappingAudienceTypeCount(talk1) > 0))
                .penalizeConfigurable((talk1, talk2) -> talk1.overlappingThemeTrackCount(talk2)
                        * talk1.overlappingAudienceTypeCount(talk2)
                        * talk1.overlappingDurationInMinutes(talk2))
                .asConstraint(
                        ConferenceConstraintConfiguration.AUDIENCE_TYPE_THEME_TRACK_CONFLICT);
    }

    Constraint audienceLevelDiversity(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                equal(Talk::getTimeslot))
                .filter((talk1, talk2) -> talk1.getAudienceLevel() != talk2.getAudienceLevel())
                .rewardConfigurable((talk1, talk2) -> talk1.getTimeslot().getDurationInMinutes())
                .asConstraint(
                        ConferenceConstraintConfiguration.AUDIENCE_LEVEL_DIVERSITY);
    }

    Constraint contentAudienceLevelFlowViolation(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .join(Talk.class,
                        lessThan(Talk::getAudienceLevel),
                        greaterThan(talk1 -> talk1.getTimeslot().getEndDateTime(),
                                talk2 -> talk2.getTimeslot().getStartDateTime()),
                        filtering((talk1, talk2) -> talk2.overlappingContentCount(talk1) > 0))
                .penalizeConfigurable((talk1, talk2) -> talk1.overlappingContentCount(talk2)
                        * talk1.combinedDurationInMinutes(talk2))
                .asConstraint(
                        ConferenceConstraintConfiguration.CONTENT_AUDIENCE_LEVEL_FLOW_VIOLATION);
    }

    Constraint contentConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                overlapping(t -> t.getTimeslot().getStartDateTime(), t -> t.getTimeslot().getEndDateTime()),
                filtering((talk1, talk2) -> talk2.overlappingContentCount(talk1) > 0))
                .penalizeConfigurable((talk1, talk2) -> talk1.overlappingContentCount(talk2)
                        * talk1.overlappingDurationInMinutes(talk2))
                .asConstraint(
                        ConferenceConstraintConfiguration.CONTENT_CONFLICT);
    }

    Constraint languageDiversity(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class,
                equal(Talk::getTimeslot))
                .filter((talk1, talk2) -> !talk1.getLanguage().equals(talk2.getLanguage()))
                .rewardConfigurable((talk1, talk2) -> talk1.getTimeslot().getDurationInMinutes())
                .asConstraint(
                        ConferenceConstraintConfiguration.LANGUAGE_DIVERSITY);
    }

    Constraint sameDayTalks(ConstraintFactory factory) {
        return factory.forEachUniquePair(Talk.class)
                .filter((talk1, talk2) -> !talk1.getTimeslot().isOnSameDayAs(talk2.getTimeslot()) &&
                        (talk1.overlappingContentCount(talk2) > 0 || talk1.overlappingThemeTrackCount(talk2) > 0))
                .penalizeConfigurable(
                        (talk1, talk2) -> (talk2.overlappingThemeTrackCount(talk1) + talk2.overlappingContentCount(talk1))
                                * talk1.combinedDurationInMinutes(talk2))
                .asConstraint(
                        ConferenceConstraintConfiguration.SAME_DAY_TALKS);
    }

    Constraint popularTalks(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .join(Talk.class,
                        lessThan(Talk::getFavoriteCount),
                        greaterThan(talk -> talk.getRoom().getCapacity()))
                .penalizeConfigurable(
                        Talk::combinedDurationInMinutes)
                .asConstraint(
                        ConferenceConstraintConfiguration.POPULAR_TALKS);
    }

    Constraint speakerPreferredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingSpeakerPreferredTimeslotTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.SPEAKER_PREFERRED_TIMESLOT_TAGS);
    }

    Constraint speakerUndesiredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingSpeakerUndesiredTimeslotTagCount)
                .filter((talk, undesiredTagCount) -> undesiredTagCount > 0)
                .penalizeConfigurable((talk, undesiredTagCount) -> undesiredTagCount * talk.getDurationInMinutes())
                .indictWith((talk, undesiredTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.SPEAKER_UNDESIRED_TIMESLOT_TAGS);
    }

    Constraint talkPreferredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingPreferredTimeslotTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.TALK_PREFERRED_TIMESLOT_TAGS);
    }

    Constraint talkUndesiredTimeslotTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingUndesiredTimeslotTagCount)
                .filter((talk, undesiredTagCount) -> undesiredTagCount > 0)
                .penalizeConfigurable((talk, undesiredTagCount) -> undesiredTagCount * talk.getDurationInMinutes())
                .indictWith((talk, undesiredTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.TALK_UNDESIRED_TIMESLOT_TAGS);
    }

    Constraint speakerPreferredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingSpeakerPreferredRoomTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.SPEAKER_PREFERRED_ROOM_TAGS);
    }

    Constraint speakerUndesiredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingSpeakerUndesiredRoomTagCount)
                .filter((talk, undesiredTagCount) -> undesiredTagCount > 0)
                .penalizeConfigurable((talk, undesiredTagCount) -> undesiredTagCount * talk.getDurationInMinutes())
                .indictWith((talk, undesiredTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.SPEAKER_UNDESIRED_ROOM_TAGS);
    }

    Constraint talkPreferredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::missingPreferredRoomTagCount)
                .filter((talk, missingTagCount) -> missingTagCount > 0)
                .penalizeConfigurable((talk, missingTagCount) -> missingTagCount * talk.getDurationInMinutes())
                .indictWith((talk, missingTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.TALK_PREFERRED_ROOM_TAGS);
    }

    Constraint talkUndesiredRoomTags(ConstraintFactory factory) {
        return factory.forEach(Talk.class)
                .expand(Talk::prevailingUndesiredRoomTagCount)
                .filter((talk, undesiredTagCount) -> undesiredTagCount > 0)
                .penalizeConfigurable((talk, undesiredTagCount) -> undesiredTagCount * talk.getDurationInMinutes())
                .indictWith((talk, undesiredTagCount) -> Collections.singleton(talk))
                .asConstraint(
                        ConferenceConstraintConfiguration.TALK_UNDESIRED_ROOM_TAGS);
    }

    Constraint speakerMakespan(ConstraintFactory factory) {
        return factory.forEach(Speaker.class)
                .join(Talk.class,
                        filtering((speaker, talk) -> talk.hasSpeaker(speaker)))
                .groupBy((speaker, talk) -> speaker,
                        compose(
                                min((Speaker speaker,
                                        Talk talk) -> talk,
                                        talk -> talk.getTimeslot().getStartDateTime()),
                                max((Speaker speaker, Talk talk) -> talk, talk -> talk.getTimeslot().getStartDateTime()),
                                (firstTalk, lastTalk) -> {
                                    LocalDate firstDate = firstTalk.getTimeslot().getStartDateTime().toLocalDate();
                                    LocalDate lastDate = lastTalk.getTimeslot().getStartDateTime().toLocalDate();
                                    return (int) Math.abs(ChronoUnit.DAYS.between(firstDate, lastDate));
                                }))
                .filter((speaker, daysBetweenTalks) -> daysBetweenTalks > 1)
                // Each such day counts for 8 hours.
                .penalizeConfigurable((speaker, daysBetweenTalks) -> (daysBetweenTalks - 1) * 8 * 60)
                .indictWith((speaker, daysBetweenTalks) -> Collections.singleton(speaker))
                .asConstraint(
                        ConferenceConstraintConfiguration.SPEAKER_MAKESPAN);
    }

}
