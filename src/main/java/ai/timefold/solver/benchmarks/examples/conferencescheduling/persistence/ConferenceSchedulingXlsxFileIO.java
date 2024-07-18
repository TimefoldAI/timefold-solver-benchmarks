package ai.timefold.solver.benchmarks.examples.conferencescheduling.persistence;

import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.AUDIENCE_LEVEL_DIVERSITY;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.AUDIENCE_TYPE_DIVERSITY;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.AUDIENCE_TYPE_THEME_TRACK_CONFLICT;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.CONSECUTIVE_TALKS_PAUSE;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.CONTENT_AUDIENCE_LEVEL_FLOW_VIOLATION;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.CONTENT_CONFLICT;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.CROWD_CONTROL;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.LANGUAGE_DIVERSITY;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.POPULAR_TALKS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.ROOM_CONFLICT;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.ROOM_UNAVAILABLE_TIMESLOT;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.SAME_DAY_TALKS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.SECTOR_CONFLICT;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.SPEAKER_CONFLICT;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.SPEAKER_PREFERRED_ROOM_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.SPEAKER_PREFERRED_TIMESLOT_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.SPEAKER_PROHIBITED_ROOM_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.SPEAKER_PROHIBITED_TIMESLOT_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.SPEAKER_REQUIRED_ROOM_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.SPEAKER_REQUIRED_TIMESLOT_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.SPEAKER_UNAVAILABLE_TIMESLOT;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.SPEAKER_UNDESIRED_ROOM_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.SPEAKER_UNDESIRED_TIMESLOT_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.TALK_MUTUALLY_EXCLUSIVE_TALKS_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.TALK_PREFERRED_ROOM_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.TALK_PREFERRED_TIMESLOT_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.TALK_PREREQUISITE_TALKS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.TALK_PROHIBITED_ROOM_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.TALK_PROHIBITED_TIMESLOT_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.TALK_REQUIRED_ROOM_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.TALK_REQUIRED_TIMESLOT_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.TALK_UNDESIRED_ROOM_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.TALK_UNDESIRED_TIMESLOT_TAGS;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.THEME_TRACK_CONFLICT;
import static ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties.THEME_TRACK_ROOM_STABILITY;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractXlsxSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.app.ConferenceSchedulingApp;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceSolution;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.Room;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.Speaker;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.Talk;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.TalkType;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.Timeslot;
import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ConferenceSchedulingXlsxFileIO extends
        AbstractXlsxSolutionFileIO<ConferenceSolution> {

    private static final String ROOM_UNAVAILABLE_TIMESLOT_DESCRIPTION =
            "Penalty per talk with an unavailable room in its timeslot, per minute";
    private static final String ROOM_CONFLICT_DESCRIPTION =
            "Penalty per 2 talks in the same room and overlapping timeslots, per overlapping minute";
    private static final String SPEAKER_UNAVAILABLE_TIMESLOT_DESCRIPTION =
            "Penalty per talk with an unavailable speaker in its timeslot, per minute";
    private static final String SPEAKER_CONFLICT_DESCRIPTION =
            "Penalty per 2 talks with the same speaker and overlapping timeslots, per overlapping minute";
    private static final String TALK_PREREQUISITE_TALKS_DESCRIPTION =
            "Penalty per prerequisite talk of a talk that doesn't end before the second talk starts, per minute of either talk";
    private static final String TALK_MUTUALLY_EXCLUSIVE_TALKS_TAGS_DESCRIPTION =
            "Penalty per common mutually exclusive talks tag of 2 talks with overlapping timeslots, per overlapping minute";
    private static final String CONSECUTIVE_TALKS_PAUSE_DESCRIPTION =
            "Penalty per 2 consecutive talks for the same speaker with a pause less than the minimum pause, per minute of either talk";
    private static final String CROWD_CONTROL_DESCRIPTION =
            "Penalty per talk with a non-zero crowd control risk that are not in paired with exactly one other such talk, per minute of either talk";

    private static final String SPEAKER_REQUIRED_TIMESLOT_TAGS_DESCRIPTION =
            "Penalty per missing required tag in a talk's timeslot, per minute";
    private static final String SPEAKER_PROHIBITED_TIMESLOT_TAGS_DESCRIPTION =
            "Penalty per prohibited tag in a talk's timeslot, per minute";
    private static final String TALK_REQUIRED_TIMESLOT_TAGS_DESCRIPTION =
            "Penalty per missing required tag in a talk's timeslot, per minute";
    private static final String TALK_PROHIBITED_TIMESLOT_TAGS_DESCRIPTION =
            "Penalty per prohibited tag in a talk's timeslot, per minute";
    private static final String SPEAKER_REQUIRED_ROOM_TAGS_DESCRIPTION =
            "Penalty per missing required tag in a talk's room, per minute";
    private static final String SPEAKER_PROHIBITED_ROOM_TAGS_DESCRIPTION =
            "Penalty per prohibited tag in a talk's room, per minute";
    private static final String TALK_REQUIRED_ROOM_TAGS_DESCRIPTION =
            "Penalty per missing required tag in a talk's room, per minute";
    private static final String TALK_PROHIBITED_ROOM_TAGS_DESCRIPTION =
            "Penalty per prohibited tag in a talk's room, per minute";

    private static final String PUBLISHED_TIMESLOT_DESCRIPTION =
            "Penalty per published talk with a different timeslot than its published timeslot, per match";

    private static final String PUBLISHED_ROOM_DESCRIPTION =
            "Penalty per published talk with a different room than its published room, per match";
    private static final String THEME_TRACK_CONFLICT_DESCRIPTION =
            "Penalty per common theme track of 2 talks with overlapping timeslots, per overlapping minute";
    private static final String THEME_TRACK_ROOM_STABILITY_DESCRIPTION =
            "Penalty per common theme track of 2 talks in a different room on the same day, per minute of either talk";
    private static final String SECTOR_CONFLICT_DESCRIPTION =
            "Penalty per common sector of 2 talks with overlapping timeslots, per overlapping minute";
    private static final String AUDIENCE_TYPE_DIVERSITY_DESCRIPTION =
            "Reward per 2 talks with a different audience type and the same timeslot, per (overlapping) minute";
    private static final String AUDIENCE_TYPE_THEME_TRACK_CONFLICT_DESCRIPTION =
            "Penalty per 2 talks with a common audience type, a common theme track and overlapping timeslots, per overlapping minute";
    private static final String AUDIENCE_LEVEL_DIVERSITY_DESCRIPTION =
            "Reward per 2 talks with a different audience level and the same timeslot, per (overlapping) minute";
    private static final String CONTENT_AUDIENCE_LEVEL_FLOW_VIOLATION_DESCRIPTION =
            "Penalty per common content of 2 talks with a different audience level for which the easier talk isn't scheduled earlier than the other talk, per minute of either talk";
    private static final String CONTENT_CONFLICT_DESCRIPTION =
            "Penalty per common content of 2 talks with overlapping timeslots, per overlapping minute";
    private static final String LANGUAGE_DIVERSITY_DESCRIPTION =
            "Reward per 2 talks with a different language and the the same timeslot, per (overlapping) minute";
    private static final String SAME_DAY_TALKS_DESCRIPTION =
            "Penalty per common content or theme track of 2 talks with a different day, per minute of either talk";
    private static final String POPULAR_TALKS_DESCRIPTION =
            "Penalty per 2 talks where the less popular one (has lower favorite count) is assigned a larger room than the more popular talk";

    private static final String SPEAKER_PREFERRED_TIMESLOT_TAGS_DESCRIPTION =
            "Penalty per missing preferred tag in a talk's timeslot, per minute";
    private static final String SPEAKER_UNDESIRED_TIMESLOT_TAGS_DESCRIPTION =
            "Penalty per undesired tag in a talk's timeslot, per minute";
    private static final String TALK_PREFERRED_TIMESLOT_TAGS_DESCRIPTION =
            "Penalty per missing preferred tag in a talk's timeslot, per minute";
    private static final String TALK_UNDESIRED_TIMESLOT_TAGS_DESCRIPTION =
            "Penalty per undesired tag in a talk's timeslot, per minute";
    private static final String SPEAKER_PREFERRED_ROOM_TAGS_DESCRIPTION =
            "Penalty per missing preferred tag in a talk's room, per minute";
    private static final String SPEAKER_UNDESIRED_ROOM_TAGS_DESCRIPTION =
            "Penalty per undesired tag in a talk's room, per minute";
    private static final String TALK_PREFERRED_ROOM_TAGS_DESCRIPTION =
            "Penalty per missing preferred tag in a talk's room, per minute";
    private static final String TALK_UNDESIRED_ROOM_TAGS_DESCRIPTION = "Penalty per undesired tag in a talk's room, per minute";

    private static final Comparator<Timeslot> COMPARATOR =
            comparing(Timeslot::getStartDateTime)
                    .thenComparing(reverseOrder(comparing(
                            Timeslot::getEndDateTime)));

    private final boolean strict;

    public ConferenceSchedulingXlsxFileIO() {
        this(true);
    }

    public ConferenceSchedulingXlsxFileIO(boolean strict) {
        super();
        this.strict = strict;
    }

    @Override
    public ConferenceSolution read(File inputSolutionFile) {
        try (InputStream in = new BufferedInputStream(new FileInputStream(inputSolutionFile))) {
            var workbook = new XSSFWorkbook(in);
            return new ConferenceSchedulingXlsxReader(workbook).read();
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Failed reading inputSolutionFile ("
                    + inputSolutionFile + ").", e);
        }
    }

    private class ConferenceSchedulingXlsxReader extends
            AbstractXlsxReader<ConferenceSolution, HardSoftScore> {

        private Map<String, TalkType> totalTalkTypeMap;
        private Set<String> totalTimeslotTagSet;
        private Set<String> totalRoomTagSet;
        private Map<String, Talk> totalTalkCodeMap;

        public ConferenceSchedulingXlsxReader(XSSFWorkbook workbook) {
            super(workbook, ConferenceSchedulingApp.SOLVER_CONFIG);
        }

        @Override
        public ConferenceSolution read() {
            solution = new ConferenceSolution();
            totalTalkTypeMap = new HashMap<>();
            totalTimeslotTagSet = new HashSet<>();
            totalRoomTagSet = new HashSet<>();
            totalTalkCodeMap = new HashMap<>();
            readConfiguration();
            readTimeslotList();
            readRoomList();
            readSpeakerList();
            readTalkList();
            // Needed for merging in the sheet Rooms views
            solution.getTimeslotList().sort(COMPARATOR);
            return solution;
        }

        private void readConfiguration() {
            nextSheet("Configuration");
            nextRow();
            readHeaderCell("Conference name");
            solution.setConferenceName(nextStringCell().getStringCellValue());
            if (strict && !VALID_NAME_PATTERN.matcher(solution.getConferenceName()).matches()) {
                throw new IllegalStateException(currentPosition() + ": The conference name (" + solution.getConferenceName()
                        + ") must match to the regular expression (" + VALID_NAME_PATTERN + ").");
            }
            var constraintProperties = new ConferenceConstraintProperties(0L);
            readIntConstraintParameterLine("Minimum consecutive talks pause in minutes",
                    constraintProperties::setMinimumConsecutiveTalksPauseInMinutes,
                    "The amount of time a speaker needs between 2 talks");
            readScoreConstraintHeaders();
            solution.setConstraintProperties(constraintProperties);

            var constraintWeightOverrides = ConstraintWeightOverrides.of(
                    Map.ofEntries(
                            getConstraintWeight(ROOM_UNAVAILABLE_TIMESLOT, ROOM_UNAVAILABLE_TIMESLOT_DESCRIPTION),
                            getConstraintWeight(ROOM_CONFLICT, ROOM_CONFLICT_DESCRIPTION),
                            getConstraintWeight(SPEAKER_UNAVAILABLE_TIMESLOT, SPEAKER_UNAVAILABLE_TIMESLOT_DESCRIPTION),
                            getConstraintWeight(SPEAKER_CONFLICT, SPEAKER_CONFLICT_DESCRIPTION),
                            getConstraintWeight(TALK_PREREQUISITE_TALKS, TALK_PREREQUISITE_TALKS_DESCRIPTION),
                            getConstraintWeight(TALK_MUTUALLY_EXCLUSIVE_TALKS_TAGS,
                                    TALK_MUTUALLY_EXCLUSIVE_TALKS_TAGS_DESCRIPTION),
                            getConstraintWeight(CONSECUTIVE_TALKS_PAUSE, CONSECUTIVE_TALKS_PAUSE_DESCRIPTION),
                            getConstraintWeight(CROWD_CONTROL, CROWD_CONTROL_DESCRIPTION),
                            getConstraintWeight(SPEAKER_REQUIRED_TIMESLOT_TAGS, SPEAKER_REQUIRED_TIMESLOT_TAGS_DESCRIPTION),
                            getConstraintWeight(SPEAKER_PROHIBITED_TIMESLOT_TAGS, SPEAKER_PROHIBITED_TIMESLOT_TAGS_DESCRIPTION),
                            getConstraintWeight(TALK_REQUIRED_TIMESLOT_TAGS, TALK_REQUIRED_TIMESLOT_TAGS_DESCRIPTION),
                            getConstraintWeight(TALK_PROHIBITED_TIMESLOT_TAGS, TALK_PROHIBITED_TIMESLOT_TAGS_DESCRIPTION),
                            getConstraintWeight(SPEAKER_REQUIRED_ROOM_TAGS, SPEAKER_REQUIRED_ROOM_TAGS_DESCRIPTION),
                            getConstraintWeight(SPEAKER_PROHIBITED_ROOM_TAGS, SPEAKER_PROHIBITED_ROOM_TAGS_DESCRIPTION),
                            getConstraintWeight(TALK_REQUIRED_ROOM_TAGS, TALK_REQUIRED_ROOM_TAGS_DESCRIPTION),
                            getConstraintWeight(TALK_PROHIBITED_ROOM_TAGS, TALK_PROHIBITED_ROOM_TAGS_DESCRIPTION),
                            getConstraintWeight(THEME_TRACK_CONFLICT, THEME_TRACK_CONFLICT_DESCRIPTION),
                            getConstraintWeight(THEME_TRACK_ROOM_STABILITY, THEME_TRACK_ROOM_STABILITY_DESCRIPTION),
                            getConstraintWeight(SECTOR_CONFLICT, SECTOR_CONFLICT_DESCRIPTION),
                            getConstraintWeight(AUDIENCE_TYPE_DIVERSITY, AUDIENCE_TYPE_DIVERSITY_DESCRIPTION),
                            getConstraintWeight(AUDIENCE_TYPE_THEME_TRACK_CONFLICT,
                                    AUDIENCE_TYPE_THEME_TRACK_CONFLICT_DESCRIPTION),
                            getConstraintWeight(AUDIENCE_LEVEL_DIVERSITY, AUDIENCE_LEVEL_DIVERSITY_DESCRIPTION),
                            getConstraintWeight(CONTENT_AUDIENCE_LEVEL_FLOW_VIOLATION,
                                    CONTENT_AUDIENCE_LEVEL_FLOW_VIOLATION_DESCRIPTION),
                            getConstraintWeight(CONTENT_CONFLICT, CONTENT_CONFLICT_DESCRIPTION),
                            getConstraintWeight(LANGUAGE_DIVERSITY, LANGUAGE_DIVERSITY_DESCRIPTION),
                            getConstraintWeight(SAME_DAY_TALKS, SAME_DAY_TALKS_DESCRIPTION),
                            getConstraintWeight(POPULAR_TALKS, POPULAR_TALKS_DESCRIPTION),
                            getConstraintWeight(SPEAKER_PREFERRED_TIMESLOT_TAGS, SPEAKER_PREFERRED_TIMESLOT_TAGS_DESCRIPTION),
                            getConstraintWeight(SPEAKER_UNDESIRED_TIMESLOT_TAGS, SPEAKER_UNDESIRED_TIMESLOT_TAGS_DESCRIPTION),
                            getConstraintWeight(TALK_PREFERRED_TIMESLOT_TAGS, TALK_PREFERRED_TIMESLOT_TAGS_DESCRIPTION),
                            getConstraintWeight(TALK_UNDESIRED_TIMESLOT_TAGS, TALK_UNDESIRED_TIMESLOT_TAGS_DESCRIPTION),
                            getConstraintWeight(SPEAKER_PREFERRED_ROOM_TAGS, SPEAKER_PREFERRED_ROOM_TAGS_DESCRIPTION),
                            getConstraintWeight(SPEAKER_UNDESIRED_ROOM_TAGS, SPEAKER_UNDESIRED_ROOM_TAGS_DESCRIPTION),
                            getConstraintWeight(TALK_PREFERRED_ROOM_TAGS, TALK_PREFERRED_ROOM_TAGS_DESCRIPTION),
                            getConstraintWeight(TALK_UNDESIRED_ROOM_TAGS, TALK_UNDESIRED_ROOM_TAGS_DESCRIPTION)));
            solution.setConstraintWeightOverrides(constraintWeightOverrides);
        }

        private Map.Entry<String, HardSoftScore> getConstraintWeight(String constraintName, String description) {
            var score = readScoreConstraintLine(constraintName, description);
            return Map.entry(constraintName, score);
        }

        private void readTimeslotList() {
            nextSheet("Timeslots");
            nextRow(false);
            readHeaderCell("Day");
            readHeaderCell("Start");
            readHeaderCell("End");
            readHeaderCell("Talk types");
            readHeaderCell("Tags");
            List<TalkType> talkTypeList = new ArrayList<>();
            List<Timeslot> timeslotList =
                    new ArrayList<>(currentSheet.getLastRowNum() - 1);
            var id = 0L;
            var talkTypeId = 0L;
            while (nextRow()) {
                var timeslot =
                        new Timeslot(id++);
                var day = LocalDate.parse(nextStringCell().getStringCellValue(), DAY_FORMATTER);
                var startTime = LocalTime.parse(nextStringCell().getStringCellValue(), TIME_FORMATTER);
                var endTime = LocalTime.parse(nextStringCell().getStringCellValue(), TIME_FORMATTER);
                if (startTime.compareTo(endTime) >= 0) {
                    throw new IllegalStateException(currentPosition() + ": The startTime (" + startTime
                            + ") must be less than the endTime (" + endTime + ").");
                }
                timeslot.setStartDateTime(LocalDateTime.of(day, startTime));
                timeslot.setEndDateTime(LocalDateTime.of(day, endTime));
                var talkTypeNames = nextStringCell().getStringCellValue().split(", ");
                Set<TalkType> talkTypeSet = new LinkedHashSet<>(talkTypeNames.length);
                for (var talkTypeName : talkTypeNames) {
                    var talkType = totalTalkTypeMap.get(talkTypeName);
                    if (talkType == null) {
                        talkType = new TalkType(talkTypeId);
                        talkTypeId++;
                        if (strict && !VALID_TAG_PATTERN.matcher(talkTypeName).matches()) {
                            throw new IllegalStateException(currentPosition()
                                    + ": The timeslot (" + timeslot + ")'s talkType (" + talkTypeName
                                    + ") must match to the regular expression (" + VALID_TAG_PATTERN + ").");
                        }
                        talkType.setName(talkTypeName);
                        talkType.setCompatibleTimeslotSet(new LinkedHashSet<>());
                        talkType.setCompatibleRoomSet(new LinkedHashSet<>());
                        totalTalkTypeMap.put(talkTypeName, talkType);
                        talkTypeList.add(talkType);
                    }
                    talkTypeSet.add(talkType);
                    talkType.getCompatibleTimeslotSet().add(timeslot);
                }
                if (talkTypeSet.isEmpty()) {
                    throw new IllegalStateException(currentPosition()
                            + ": The timeslot (" + timeslot + ")'s talk types (" + timeslot.getTalkTypeSet()
                            + ") must not be empty.");
                }
                timeslot.setTalkTypeSet(talkTypeSet);
                timeslot.setTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                for (var tag : timeslot.getTagSet()) {
                    if (strict && !VALID_TAG_PATTERN.matcher(tag).matches()) {
                        throw new IllegalStateException(currentPosition()
                                + ": The timeslot (" + timeslot + ")'s tag (" + tag
                                + ") must match to the regular expression (" + VALID_TAG_PATTERN + ").");
                    }
                }
                totalTimeslotTagSet.addAll(timeslot.getTagSet());
                timeslotList.add(timeslot);
            }
            solution.setTimeslotList(timeslotList);
            solution.setTalkTypeList(talkTypeList);
        }

        private void readRoomList() {
            nextSheet("Rooms");
            nextRow(false);
            readHeaderCell("");
            readHeaderCell("");
            readHeaderCell("");
            readHeaderCell("");
            readTimeslotDaysHeaders();
            nextRow(false);
            readHeaderCell("Name");
            readHeaderCell("Capacity");
            readHeaderCell("Talk types");
            readHeaderCell("Tags");
            readTimeslotHoursHeaders();
            List<Room> roomList =
                    new ArrayList<>(currentSheet.getLastRowNum() - 1);
            var id = 0L;
            while (nextRow()) {
                var room =
                        new Room(id++);
                room.setName(nextStringCell().getStringCellValue());
                if (strict && !VALID_NAME_PATTERN.matcher(room.getName()).matches()) {
                    throw new IllegalStateException(currentPosition() + ": The room name (" + room.getName()
                            + ") must match to the regular expression (" + VALID_NAME_PATTERN + ").");
                }
                room.setCapacity(getNextStrictlyPositiveIntegerCell("room name (" + room.getName(), "capacity"));
                var talkTypeNames = nextStringCell().getStringCellValue().split(", ");
                Set<TalkType> talkTypeSet;
                if (talkTypeNames.length == 0 || (talkTypeNames.length == 1 && talkTypeNames[0].isEmpty())) {
                    talkTypeSet = new LinkedHashSet<>(totalTalkTypeMap.values());
                    for (var talkType : talkTypeSet) {
                        talkType.getCompatibleRoomSet().add(room);
                    }
                } else {
                    talkTypeSet = new LinkedHashSet<>(talkTypeNames.length);
                    for (var talkTypeName : talkTypeNames) {
                        var talkType = totalTalkTypeMap.get(talkTypeName);
                        if (talkType == null) {
                            throw new IllegalStateException(currentPosition()
                                    + ": The room (" + room + ")'s talkType (" + talkTypeName
                                    + ") does not exist in the talk types (" + totalTalkTypeMap.keySet()
                                    + ") of the other sheet (Timeslots).");
                        }
                        talkTypeSet.add(talkType);
                        talkType.getCompatibleRoomSet().add(room);
                    }
                }
                room.setTalkTypeSet(talkTypeSet);
                room.setTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                for (var tag : room.getTagSet()) {
                    if (strict && !VALID_TAG_PATTERN.matcher(tag).matches()) {
                        throw new IllegalStateException(currentPosition() + ": The room (" + room + ")'s tag (" + tag
                                + ") must match to the regular expression (" + VALID_TAG_PATTERN + ").");
                    }
                }
                totalRoomTagSet.addAll(room.getTagSet());
                Set<Timeslot> unavailableTimeslotSet =
                        new LinkedHashSet<>();
                for (var timeslot : solution
                        .getTimeslotList()) {
                    var cell = nextStringCell();
                    if (Objects.equals(extractColor(cell, UNAVAILABLE_COLOR), UNAVAILABLE_COLOR)) {
                        unavailableTimeslotSet.add(timeslot);
                    }
                    if (!cell.getStringCellValue().isEmpty()) {
                        throw new IllegalStateException(currentPosition() + ": The cell (" + cell.getStringCellValue()
                                + ") should be empty. Use the talks sheet pre-assign rooms and timeslots.");
                    }
                }
                room.setUnavailableTimeslotSet(unavailableTimeslotSet);
                roomList.add(room);
            }
            solution.setRoomList(roomList);
        }

        private void readSpeakerList() {
            nextSheet("Speakers");
            nextRow(false);
            readHeaderCell("");
            readHeaderCell("");
            readHeaderCell("");
            readHeaderCell("");
            readHeaderCell("");
            readHeaderCell("");
            readHeaderCell("");
            readHeaderCell("");
            readHeaderCell("");
            readTimeslotDaysHeaders();
            nextRow(false);
            readHeaderCell("Name");
            readHeaderCell("Required timeslot tags");
            readHeaderCell("Preferred timeslot tags");
            readHeaderCell("Prohibited timeslot tags");
            readHeaderCell("Undesired timeslot tags");
            readHeaderCell("Required room tags");
            readHeaderCell("Preferred room tags");
            readHeaderCell("Prohibited room tags");
            readHeaderCell("Undesired room tags");

            readTimeslotHoursHeaders();
            List<Speaker> speakerList =
                    new ArrayList<>(currentSheet.getLastRowNum() - 1);
            var id = 0L;
            while (nextRow()) {
                var speaker =
                        new Speaker(id++);
                speaker.setName(nextStringCell().getStringCellValue());
                if (strict && !VALID_NAME_PATTERN.matcher(speaker.getName()).matches()) {
                    throw new IllegalStateException(currentPosition() + ": The speaker name (" + speaker.getName()
                            + ") must match to the regular expression (" + VALID_NAME_PATTERN + ").");
                }
                speaker.setRequiredTimeslotTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyTimeslotTags(speaker.getRequiredTimeslotTagSet());
                speaker.setPreferredTimeslotTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyTimeslotTags(speaker.getPreferredTimeslotTagSet());
                speaker.setProhibitedTimeslotTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyTimeslotTags(speaker.getProhibitedTimeslotTagSet());
                speaker.setUndesiredTimeslotTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyTimeslotTags(speaker.getUndesiredTimeslotTagSet());
                speaker.setRequiredRoomTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyRoomTags(speaker.getRequiredRoomTagSet());
                speaker.setPreferredRoomTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyRoomTags(speaker.getPreferredRoomTagSet());
                speaker.setProhibitedRoomTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyRoomTags(speaker.getProhibitedRoomTagSet());
                speaker.setUndesiredRoomTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyRoomTags(speaker.getUndesiredRoomTagSet());
                Set<Timeslot> unavailableTimeslotSet =
                        new LinkedHashSet<>();
                for (var timeslot : solution
                        .getTimeslotList()) {
                    var cell = nextStringCell();
                    if (Objects.equals(extractColor(cell, UNAVAILABLE_COLOR), UNAVAILABLE_COLOR)) {
                        unavailableTimeslotSet.add(timeslot);
                    }
                    if (!cell.getStringCellValue().isEmpty()) {
                        throw new IllegalStateException(currentPosition() + ": The cell (" + cell.getStringCellValue()
                                + ") should be empty. Use the other sheet (Talks) to pre-assign rooms and timeslots.");
                    }
                }
                speaker.setUnavailableTimeslotSet(unavailableTimeslotSet);
                speakerList.add(speaker);
            }
            solution.setSpeakerList(speakerList);
        }

        private void readTalkList() {
            var speakerMap =
                    solution.getSpeakerList().stream().collect(
                            toMap(Speaker::getName,
                                    speaker -> speaker));
            nextSheet("Talks");
            nextRow(false);
            readHeaderCell("Code");
            readHeaderCell("Title");
            readHeaderCell("Talk type");
            readHeaderCell("Speakers");
            readHeaderCell("Theme track tags");
            readHeaderCell("Sector tags");
            readHeaderCell("Audience types");
            readHeaderCell("Audience level");
            readHeaderCell("Content tags");
            readHeaderCell("Language");
            readHeaderCell("Required timeslot tags");
            readHeaderCell("Preferred timeslot tags");
            readHeaderCell("Prohibited timeslot tags");
            readHeaderCell("Undesired timeslot tags");
            readHeaderCell("Required room tags");
            readHeaderCell("Preferred room tags");
            readHeaderCell("Prohibited room tags");
            readHeaderCell("Undesired room tags");
            readHeaderCell("Mutually exclusive talks tags");
            readHeaderCell("Prerequisite talks codes");
            readHeaderCell("Favorite count");
            readHeaderCell("Crowd control risk");
            readHeaderCell("Pinned by user");
            readHeaderCell("Timeslot day");
            readHeaderCell("Start");
            readHeaderCell("End");
            readHeaderCell("Room");
            readHeaderCell("Published Timeslot");
            readHeaderCell("Published Start");
            readHeaderCell("Published End");
            readHeaderCell("Published Room");
            List<Talk> talkList =
                    new ArrayList<>(currentSheet.getLastRowNum() - 1);
            var id = 0L;
            var timeslotMap =
                    solution.getTimeslotList().stream().collect(
                            toMap(timeslot -> {
                                var key = timeslot.getStartDateTime();
                                return new Pair<>(key, timeslot.getEndDateTime());
                            },
                                    Function.identity()));
            var roomMap =
                    solution.getRoomList().stream().collect(
                            toMap(Room::getName,
                                    Function.identity()));
            Map<Talk, Set<String>> talkToPrerequisiteTalkSetMap =
                    new HashMap<>();
            while (nextRow()) {
                var talk =
                        new Talk(id++);
                talk.setCode(nextStringCell().getStringCellValue());
                totalTalkCodeMap.put(talk.getCode(), talk);
                if (strict && !VALID_CODE_PATTERN.matcher(talk.getCode()).matches()) {
                    throw new IllegalStateException(currentPosition() + ": The talk code (" + talk.getCode()
                            + ") must match to the regular expression (" + VALID_CODE_PATTERN + ").");
                }
                talk.setTitle(nextStringCell().getStringCellValue());
                var talkTypeName = nextStringCell().getStringCellValue();
                var talkType = totalTalkTypeMap.get(talkTypeName);
                if (talkType == null) {
                    throw new IllegalStateException(currentPosition()
                            + ": The talk (" + talk + ")'s talkType (" + talkTypeName
                            + ") does not exist in the talk types (" + totalTalkTypeMap.keySet()
                            + ") of the other sheet (Timeslots).");
                }
                talk.setTalkType(talkType);
                talk.setSpeakerList(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).map(speakerName -> {
                            var speaker = speakerMap.get(speakerName);
                            if (speaker == null) {
                                throw new IllegalStateException(currentPosition() + ": The talk with code (" + talk.getCode()
                                        + ") has a speaker (" + speakerName + ") that doesn't exist in the speaker list.");
                            }
                            return speaker;
                        }).collect(toList()));
                talk.setThemeTrackTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                for (var tag : talk.getThemeTrackTagSet()) {
                    if (strict && !VALID_TAG_PATTERN.matcher(tag).matches()) {
                        throw new IllegalStateException(currentPosition() + ": The talk (" + talk + ")'s theme tag (" + tag
                                + ") must match to the regular expression (" + VALID_TAG_PATTERN + ").");
                    }
                }
                talk.setSectorTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                for (var tag : talk.getSectorTagSet()) {
                    if (strict && !VALID_TAG_PATTERN.matcher(tag).matches()) {
                        throw new IllegalStateException(currentPosition() + ": The talk (" + talk + ")'s sector tag (" + tag
                                + ") must match to the regular expression (" + VALID_TAG_PATTERN + ").");
                    }
                }
                talk.setAudienceTypeSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                for (var audienceType : talk.getAudienceTypeSet()) {
                    if (strict && !VALID_TAG_PATTERN.matcher(audienceType).matches()) {
                        throw new IllegalStateException(
                                currentPosition() + ": The talk (" + talk + ")'s audience type (" + audienceType
                                        + ") must match to the regular expression (" + VALID_TAG_PATTERN + ").");
                    }
                }
                talk.setAudienceLevel(
                        getNextStrictlyPositiveIntegerCell("talk with code (" + talk.getCode(), "an audience level"));
                talk.setContentTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                for (var tag : talk.getContentTagSet()) {
                    if (strict && !VALID_TAG_PATTERN.matcher(tag).matches()) {
                        throw new IllegalStateException(currentPosition() + ": The talk (" + talk + ")'s content tag (" + tag
                                + ") must match to the regular expression (" + VALID_TAG_PATTERN + ").");
                    }
                }
                talk.setLanguage(nextStringCell().getStringCellValue());
                talk.setRequiredTimeslotTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyTimeslotTags(talk.getRequiredTimeslotTagSet());
                talk.setPreferredTimeslotTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyTimeslotTags(talk.getPreferredTimeslotTagSet());
                talk.setProhibitedTimeslotTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyTimeslotTags(talk.getProhibitedTimeslotTagSet());
                talk.setUndesiredTimeslotTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyTimeslotTags(talk.getUndesiredTimeslotTagSet());
                talk.setRequiredRoomTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyRoomTags(talk.getRequiredRoomTagSet());
                talk.setPreferredRoomTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyRoomTags(talk.getPreferredRoomTagSet());
                talk.setProhibitedRoomTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyRoomTags(talk.getProhibitedRoomTagSet());
                talk.setUndesiredRoomTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                verifyRoomTags(talk.getUndesiredRoomTagSet());
                talk.setMutuallyExclusiveTalksTagSet(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                talkToPrerequisiteTalkSetMap.put(talk, Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                        .filter(tag -> !tag.isEmpty()).collect(toCollection(LinkedHashSet::new)));
                talk.setFavoriteCount(getNextPositiveIntegerCell("talk with code (" + talk.getCode(), "a Favorite count"));
                talk.setCrowdControlRisk(
                        getNextPositiveIntegerCell("talk with code (" + talk.getCode(), "a crowd control risk"));
                talk.setPinnedByUser(nextBooleanCell().getBooleanCellValue());
                talk.setTimeslot(extractTimeslot(timeslotMap, talk));
                talk.setRoom(extractRoom(roomMap, talk));
                talk.setPublishedTimeslot(extractTimeslot(timeslotMap, talk));
                talk.setPublishedRoom(extractRoom(roomMap, talk));

                talkList.add(talk);
            }

            setPrerequisiteTalkSets(talkToPrerequisiteTalkSetMap);
            solution.setTalkList(talkList);
        }

        private Timeslot extractTimeslot(
                Map<Pair<LocalDateTime, LocalDateTime>, Timeslot> timeslotMap,
                Talk talk) {
            Timeslot assignedTimeslot;
            var dateString = nextStringCell().getStringCellValue();
            var startTimeString = nextStringCell().getStringCellValue();
            var endTimeString = nextStringCell().getStringCellValue();
            if (!dateString.isEmpty() || !startTimeString.isEmpty() || !endTimeString.isEmpty()) {
                LocalDateTime startDateTime;
                LocalDateTime endDateTime;
                try {
                    startDateTime = LocalDateTime.of(LocalDate.parse(dateString, DAY_FORMATTER),
                            LocalTime.parse(startTimeString, TIME_FORMATTER));
                    endDateTime = LocalDateTime.of(LocalDate.parse(dateString, DAY_FORMATTER),
                            LocalTime.parse(endTimeString, TIME_FORMATTER));
                } catch (DateTimeParseException e) {
                    throw new IllegalStateException(currentPosition() + ": The talk with code (" + talk.getCode()
                            + ") has a timeslot date (" + dateString
                            + "), startTime (" + startTimeString + ") and endTime (" + endTimeString
                            + ") that doesn't parse as a date or time.", e);
                }

                assignedTimeslot = timeslotMap.get(new Pair<>(startDateTime, endDateTime));
                if (assignedTimeslot == null) {
                    throw new IllegalStateException(currentPosition() + ": The talk with code (" + talk.getCode()
                            + ") has a timeslot date (" + dateString
                            + "), startTime (" + startTimeString + ") and endTime (" + endTimeString
                            + ") that doesn't exist in the other sheet (Timeslots).");
                }

                return assignedTimeslot;
            }

            return null;
        }

        private Room extractRoom(
                Map<String, Room> roomMap,
                Talk talk) {
            var roomName = nextStringCell().getStringCellValue();
            if (!roomName.isEmpty()) {
                var room = roomMap.get(roomName);
                if (room == null) {
                    throw new IllegalStateException(currentPosition() + ": The talk with code (" + talk.getCode()
                            + ") has a roomName (" + roomName
                            + ") that doesn't exist in the other sheet (Rooms).");
                }
                return room;
            }

            return null;
        }

        private int getNextStrictlyPositiveIntegerCell(String classSpecifier, String columnName) {
            var cellValueDouble = nextNumericCell().getNumericCellValue();
            if (strict && (cellValueDouble <= 0 || cellValueDouble != Math.floor(cellValueDouble))) {
                throw new IllegalStateException(currentPosition() + ": The" + classSpecifier
                        + ")'s has " + columnName + " (" + cellValueDouble
                        + ") that isn't a strictly positive integer number.");
            }
            return (int) cellValueDouble;
        }

        private int getNextPositiveIntegerCell(String classSpecifier, String columnName) {
            var cellValueDouble = nextNumericCell().getNumericCellValue();
            if (strict && (cellValueDouble < 0 || cellValueDouble != Math.floor(cellValueDouble))) {
                throw new IllegalStateException(currentPosition() + ": The " + classSpecifier
                        + ")'s has " + columnName + " (" + cellValueDouble + ") that isn't a positive integer number.");
            }
            return (int) cellValueDouble;
        }

        private void verifyTimeslotTags(Set<String> timeslotTagSet) {
            for (var tag : timeslotTagSet) {
                if (!totalTimeslotTagSet.contains(tag)) {
                    throw new IllegalStateException(currentPosition() + ": The timeslot tag (" + tag
                            + ") does not exist in the tags (" + totalTimeslotTagSet
                            + ") of the other sheet (Timeslots).");
                }
            }
        }

        private void verifyRoomTags(Set<String> roomTagSet) {
            for (var tag : roomTagSet) {
                if (!totalRoomTagSet.contains(tag)) {
                    throw new IllegalStateException(currentPosition() + ": The room tag (" + tag
                            + ") does not exist in the tags (" + totalRoomTagSet + ") of the other sheet (Rooms).");
                }
            }
        }

        private void setPrerequisiteTalkSets(
                Map<Talk, Set<String>> talkToPrerequisiteTalkSetMap) {
            for (var entry : talkToPrerequisiteTalkSetMap
                    .entrySet()) {
                var currentTalk = entry.getKey();
                Set<Talk> prerequisiteTalkSet =
                        new HashSet<>();
                for (var prerequisiteTalkCode : entry.getValue()) {
                    var prerequisiteTalk = totalTalkCodeMap.get(prerequisiteTalkCode);
                    if (prerequisiteTalk == null) {
                        throw new IllegalStateException("The talk (" + currentTalk.getCode()
                                + ") has a prerequisite talk (" + prerequisiteTalkCode
                                + ") that doesn't exist in the talk list.");
                    }
                    prerequisiteTalkSet.add(prerequisiteTalk);
                }
                currentTalk.setPrerequisiteTalkSet(prerequisiteTalkSet);
            }
        }

        private void readTimeslotDaysHeaders() {
            LocalDate previousTimeslotDay = null;
            for (var timeslot : solution
                    .getTimeslotList()) {
                var timeslotDay = timeslot.getDate();
                if (timeslotDay.equals(previousTimeslotDay)) {
                    readHeaderCell("");
                } else {
                    readHeaderCell(DAY_FORMATTER.format(timeslotDay));
                    previousTimeslotDay = timeslotDay;
                }
            }
        }

        private void readTimeslotHoursHeaders() {
            for (var timeslot : solution.getTimeslotList()) {
                readHeaderCell(TIME_FORMATTER.format(timeslot.getStartDateTime())
                        + "-" + TIME_FORMATTER.format(timeslot.getEndDateTime()));
            }
        }
    }

    private record Pair<A, B>(A key, B value) {

    }

}
