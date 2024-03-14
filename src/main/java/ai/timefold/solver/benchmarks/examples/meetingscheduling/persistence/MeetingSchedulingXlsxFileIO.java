package ai.timefold.solver.benchmarks.examples.meetingscheduling.persistence;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.LongFunction;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractXlsxSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.app.MeetingSchedulingApp;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.Attendance;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.Day;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.Meeting;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingAssignment;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingConstraintConfiguration;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.MeetingSchedule;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.Person;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.PreferredAttendance;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.RequiredAttendance;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.Room;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.domain.TimeGrain;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MeetingSchedulingXlsxFileIO extends AbstractXlsxSolutionFileIO<MeetingSchedule> {

    @Override
    public MeetingSchedule read(File inputScheduleFile) {
        try (InputStream in = new BufferedInputStream(new FileInputStream(inputScheduleFile))) {
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            return new MeetingSchedulingXlsxReader(workbook).read();
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Failed reading inputScheduleFile ("
                    + inputScheduleFile + ").", e);
        }
    }

    private static class MeetingSchedulingXlsxReader extends AbstractXlsxReader<MeetingSchedule, HardMediumSoftScore> {

        MeetingSchedulingXlsxReader(XSSFWorkbook workbook) {
            super(workbook, MeetingSchedulingApp.SOLVER_CONFIG);
        }

        @Override
        public MeetingSchedule read() {
            solution = new MeetingSchedule();
            readConfiguration();
            readDayList();
            readRoomList();
            readPersonList();
            readMeetingList();

            return solution;
        }

        private void readConfiguration() {
            nextSheet("Configuration");
            nextRow();
            nextRow(true);
            readHeaderCell("Constraint");
            readHeaderCell("Weight");
            readHeaderCell("Description");

            MeetingConstraintConfiguration constraintConfiguration =
                    new MeetingConstraintConfiguration(0L);

            // TODO refactor this to allow setting pos/neg, weight and score level
            readIntConstraintParameterLine(
                    MeetingConstraintConfiguration.ROOM_CONFLICT,
                    hardScore -> constraintConfiguration.setRoomConflict(HardMediumSoftScore.ofHard(hardScore)), "");
            readIntConstraintParameterLine(
                    MeetingConstraintConfiguration.DONT_GO_IN_OVERTIME,
                    hardScore -> constraintConfiguration.setDontGoInOvertime(HardMediumSoftScore.ofHard(hardScore)), "");
            readIntConstraintParameterLine(
                    MeetingConstraintConfiguration.REQUIRED_ATTENDANCE_CONFLICT,
                    hardScore -> constraintConfiguration.setRequiredAttendanceConflict(HardMediumSoftScore.ofHard(hardScore)),
                    "");
            readIntConstraintParameterLine(
                    MeetingConstraintConfiguration.REQUIRED_ROOM_CAPACITY,
                    hardScore -> constraintConfiguration.setRequiredRoomCapacity(HardMediumSoftScore.ofHard(hardScore)), "");
            readIntConstraintParameterLine(
                    MeetingConstraintConfiguration.START_AND_END_ON_SAME_DAY,
                    hardScore -> constraintConfiguration.setStartAndEndOnSameDay(HardMediumSoftScore.ofHard(hardScore)), "");

            readIntConstraintParameterLine(
                    MeetingConstraintConfiguration.REQUIRED_AND_PREFERRED_ATTENDANCE_CONFLICT,
                    mediumScore -> constraintConfiguration
                            .setRequiredAndPreferredAttendanceConflict(HardMediumSoftScore.ofMedium(mediumScore)),
                    "");
            readIntConstraintParameterLine(
                    MeetingConstraintConfiguration.PREFERRED_ATTENDANCE_CONFLICT,
                    mediumScore -> constraintConfiguration
                            .setPreferredAttendanceConflict(HardMediumSoftScore.ofMedium(mediumScore)),
                    "");

            readIntConstraintParameterLine(
                    MeetingConstraintConfiguration.DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE,
                    softScore -> constraintConfiguration
                            .setDoAllMeetingsAsSoonAsPossible(HardMediumSoftScore.ofSoft(softScore)),
                    "");
            readIntConstraintParameterLine(
                    MeetingConstraintConfiguration.ONE_TIME_GRAIN_BREAK_BETWEEN_TWO_CONSECUTIVE_MEETINGS,
                    softScore -> constraintConfiguration
                            .setOneTimeGrainBreakBetweenTwoConsecutiveMeetings(HardMediumSoftScore.ofSoft(softScore)),
                    "");
            readIntConstraintParameterLine(
                    MeetingConstraintConfiguration.OVERLAPPING_MEETINGS,
                    softScore -> constraintConfiguration.setOverlappingMeetings(HardMediumSoftScore.ofSoft(softScore)), "");
            readIntConstraintParameterLine(
                    MeetingConstraintConfiguration.ASSIGN_LARGER_ROOMS_FIRST,
                    softScore -> constraintConfiguration.setAssignLargerRoomsFirst(HardMediumSoftScore.ofSoft(softScore)), "");
            readIntConstraintParameterLine(
                    MeetingConstraintConfiguration.ROOM_STABILITY,
                    softScore -> constraintConfiguration.setRoomStability(HardMediumSoftScore.ofSoft(softScore)), "");

            solution.setConstraintConfiguration(constraintConfiguration);
        }

        private void readPersonList() {
            nextSheet("Persons");
            nextRow(false);
            readHeaderCell("Full name");
            List<Person> personList = new ArrayList<>(currentSheet.getLastRowNum() - 1);
            long id = 0L;
            while (nextRow()) {
                Person person = new Person(id++, nextStringCell().getStringCellValue());
                if (!VALID_NAME_PATTERN.matcher(person.getFullName()).matches()) {
                    throw new IllegalStateException(
                            currentPosition() + ": The person name (" + person.getFullName()
                                    + ") must match to the regular expression (" + VALID_NAME_PATTERN + ").");
                }
                personList.add(person);
            }
            solution.setPersonList(personList);
        }

        private void readMeetingList() {
            Map<String, Person> personMap = solution.getPersonList().stream().collect(
                    toMap(Person::getFullName, person -> person));
            nextSheet("Meetings");
            nextRow(false);
            readHeaderCell("Topic");
            readHeaderCell("Group");
            readHeaderCell("Duration");
            readHeaderCell("Speakers");
            readHeaderCell("Content");
            readHeaderCell("Required attendance list");
            readHeaderCell("Preferred attendance list");
            readHeaderCell("Day");
            readHeaderCell("Starting time");
            readHeaderCell("Room");

            List<Meeting> meetingList =
                    new ArrayList<>(currentSheet.getLastRowNum() - 1);
            List<MeetingAssignment> meetingAssignmentList = new ArrayList<>(currentSheet.getLastRowNum() - 1);
            List<Attendance> attendanceList =
                    new ArrayList<>(currentSheet.getLastRowNum() - 1);
            AtomicLong attendanceIdCounter = new AtomicLong();
            long meetingId = 0L;
            long meetingAssignmentId = 0L;
            Map<LocalDateTime, TimeGrain> timeGrainMap =
                    solution.getTimeGrainList().stream().collect(
                            toMap(TimeGrain::getDateTime,
                                    Function.identity()));
            Map<String, Room> roomMap =
                    solution.getRoomList().stream().collect(
                            toMap(Room::getName,
                                    Function.identity()));

            while (nextRow()) {
                Meeting meeting =
                        new Meeting(meetingId++);
                List<Attendance> speakerAttendanceList =
                        new ArrayList<>();
                Set<Person> speakerSet = new HashSet<>();
                MeetingAssignment meetingAssignment = new MeetingAssignment(meetingAssignmentId++);

                meeting.setTopic(nextStringCell().getStringCellValue());
                meeting.setEntireGroupMeeting(nextStringCell().getStringCellValue().equalsIgnoreCase("y"));
                readMeetingDuration(meeting);
                readSpeakerList(personMap, meeting, speakerAttendanceList, speakerSet, attendanceIdCounter);
                meeting.setContent(nextStringCell().getStringCellValue());

                if (meeting.isEntireGroupMeeting()) {
                    List<RequiredAttendance> requiredAttendanceList =
                            new ArrayList<>(solution.getPersonList().size());
                    for (Person person : solution.getPersonList()) {
                        RequiredAttendance requiredAttendance =
                                createAttendance(attendanceIdCounter,
                                        id -> new RequiredAttendance(
                                                id, meeting));
                        requiredAttendance.setPerson(person);
                        requiredAttendanceList.add(requiredAttendance);
                        attendanceList.add(requiredAttendance);
                    }
                    meeting.setRequiredAttendanceList(requiredAttendanceList);
                    meeting.setPreferredAttendanceList(new ArrayList<>());
                } else {
                    attendanceList.addAll(speakerAttendanceList);
                    List<Attendance> meetingAttendanceList =
                            getAttendanceLists(meeting, personMap, speakerSet, attendanceIdCounter);
                    attendanceList.addAll(meetingAttendanceList);
                }
                meetingAssignment.setStartingTimeGrain(extractTimeGrain(meeting, timeGrainMap));
                meetingAssignment.setRoom(extractRoom(meeting, roomMap));
                meetingList.add(meeting);
                meetingAssignment.setMeeting(meeting);
                meetingAssignmentList.add(meetingAssignment);
            }
            solution.setMeetingList(meetingList);
            solution.setMeetingAssignmentList(meetingAssignmentList);
            solution.setAttendanceList(attendanceList);
        }

        private void readSpeakerList(Map<String, Person> personMap,
                Meeting meeting,
                List<Attendance> speakerAttendanceList,
                Set<Person> speakerSet, AtomicLong attendanceIdCounter) {
            meeting.setSpeakerList(Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                    .filter(speaker -> !speaker.isEmpty())
                    .map(speakerName -> {
                        Person speaker = personMap.get(speakerName);
                        if (speaker == null) {
                            throw new IllegalStateException(
                                    currentPosition() + ": The meeting with id (" + meeting.getId()
                                            + ") has a speaker (" + speakerName + ") that doesn't exist in the Persons list.");
                        }
                        if (speakerSet.contains(speaker)) {
                            throw new IllegalStateException(
                                    currentPosition() + ": The meeting with id (" + meeting.getId()
                                            + ") has a duplicate speaker (" + speakerName + ").");
                        }
                        speakerSet.add(speaker);
                        RequiredAttendance speakerAttendance =
                                createAttendance(attendanceIdCounter,
                                        id -> new RequiredAttendance(
                                                id, meeting));
                        speakerAttendance.setMeeting(meeting);
                        speakerAttendance.setPerson(speaker);
                        speakerAttendanceList.add(speakerAttendance);
                        return speaker;
                    }).collect(toList()));
        }

        private <E extends Attendance> E
                createAttendance(AtomicLong idCounter, LongFunction<E> constructor) {
            return constructor.apply(idCounter.getAndIncrement());
        }

        private void readMeetingDuration(Meeting meeting) {
            double durationDouble = nextNumericCell().getNumericCellValue();
            if (durationDouble <= 0 || durationDouble != Math.floor(durationDouble)) {
                throw new IllegalStateException(
                        currentPosition() + ": The meeting with id (" + meeting.getId()
                                + ")'s has a duration (" + durationDouble + ") that isn't a strictly positive integer number.");
            }
            if (durationDouble
                    % TimeGrain.GRAIN_LENGTH_IN_MINUTES != 0) {
                throw new IllegalStateException(
                        currentPosition() + ": The meeting with id (" + meeting.getId()
                                + ") has a duration (" + durationDouble + ") that isn't a multiple of "
                                + TimeGrain.GRAIN_LENGTH_IN_MINUTES
                                + ".");
            }
            meeting.setDurationInGrains((int) durationDouble
                    / TimeGrain.GRAIN_LENGTH_IN_MINUTES);
        }

        private List<Attendance> getAttendanceLists(
                Meeting meeting, Map<String, Person> personMap,
                Set<Person> speakerSet,
                AtomicLong attendanceIdCounter) {
            List<Attendance> attendanceList = new ArrayList<>(currentSheet.getLastRowNum() - 1);
            Set<Person> requiredPersonSet = new HashSet<>();

            List<RequiredAttendance> requiredAttendanceList =
                    getRequiredAttendanceList(meeting, personMap, speakerSet, requiredPersonSet, attendanceIdCounter);
            meeting.setRequiredAttendanceList(requiredAttendanceList);
            attendanceList.addAll(requiredAttendanceList);

            List<PreferredAttendance> preferredAttendanceList =
                    getPreferredAttendanceList(meeting, personMap, speakerSet, requiredPersonSet, attendanceIdCounter);
            meeting.setPreferredAttendanceList(preferredAttendanceList);
            attendanceList.addAll(preferredAttendanceList);

            return attendanceList;
        }

        private List<RequiredAttendance>
                getRequiredAttendanceList(Meeting meeting,
                        Map<String, Person> personMap,
                        Set<Person> speakerSet, Set<Person> requiredPersonSet, AtomicLong attendanceIdCounter) {
            return Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                    .filter(requiredAttendee -> !requiredAttendee.isEmpty())
                    .map(personName -> {
                        RequiredAttendance requiredAttendance =
                                createAttendance(attendanceIdCounter, id -> new RequiredAttendance(id, meeting));
                        Person person = personMap.get(personName);
                        if (person == null) {
                            throw new IllegalStateException(
                                    currentPosition() + ": The meeting with id (" + meeting.getId()
                                            + ") has a required attendee (" + personName
                                            + ") that doesn't exist in the Persons list.");
                        }
                        if (requiredPersonSet.contains(person)) {
                            throw new IllegalStateException(
                                    currentPosition() + ": The meeting with id (" + meeting.getId()
                                            + ") has a duplicate required attendee (" + personName + ").");
                        }
                        if (speakerSet.contains(person)) {
                            throw new IllegalStateException(
                                    currentPosition() + ": The meeting with id (" + meeting.getId()
                                            + ") has a required attendee  (" + personName + ") who is also the speaker.");
                        }
                        requiredPersonSet.add(person);
                        requiredAttendance.setMeeting(meeting);
                        requiredAttendance.setPerson(person);
                        return requiredAttendance;
                    })
                    .collect(toList());
        }

        private List<PreferredAttendance>
                getPreferredAttendanceList(Meeting meeting,
                        Map<String, Person> personMap,
                        Set<Person> speakerSet, Set<Person> requiredPersonSet, AtomicLong attendanceIdCounter) {
            Set<Person> preferredPersonSet = new HashSet<>();
            return Arrays.stream(nextStringCell().getStringCellValue().split(", "))
                    .filter(preferredAttendee -> !preferredAttendee.isEmpty())
                    .map(personName -> {
                        PreferredAttendance preferredAttendance =
                                createAttendance(attendanceIdCounter, id -> new PreferredAttendance(id, meeting));
                        Person person = personMap.get(personName);
                        if (person == null) {
                            throw new IllegalStateException(
                                    currentPosition() + ": The meeting with id (" + meeting.getId()
                                            + ") has a preferred attendee (" + personName
                                            + ") that doesn't exist in the Persons list.");
                        }
                        if (preferredPersonSet.contains(person)) {
                            throw new IllegalStateException(
                                    currentPosition() + ": The meeting with id (" + meeting.getId()
                                            + ") has a duplicate preferred attendee (" + personName + ").");
                        }
                        if (requiredPersonSet.contains(person)) {
                            throw new IllegalStateException(
                                    currentPosition() + ": The meeting with id (" + meeting.getId()
                                            + ") has a preferred attendee (" + personName
                                            + ") that is also a required attendee.");
                        }
                        if (speakerSet.contains(person)) {
                            throw new IllegalStateException(
                                    currentPosition() + ": The meeting with id (" + meeting.getId()
                                            + ") has a preferred attendee  (" + personName + ") who is also the speaker.");
                        }
                        preferredPersonSet.add(person);
                        preferredAttendance.setMeeting(meeting);
                        preferredAttendance.setPerson(person);
                        return preferredAttendance;
                    })
                    .collect(toList());
        }

        private TimeGrain extractTimeGrain(
                Meeting meeting,
                Map<LocalDateTime, TimeGrain> timeGrainMap) {
            String dateString = nextStringCell().getStringCellValue();
            String startTimeString = nextStringCell().getStringCellValue();
            if (!dateString.isEmpty() || !startTimeString.isEmpty()) {
                LocalDateTime dateTime;
                try {
                    dateTime = LocalDateTime.of(LocalDate.parse(dateString, DAY_FORMATTER),
                            LocalTime.parse(startTimeString, TIME_FORMATTER));
                } catch (DateTimeParseException e) {
                    throw new IllegalStateException(currentPosition() + ": The meeting with id (" + meeting.getId()
                            + ") has a timeGrain date (" + dateString + ") and startTime (" + startTimeString
                            + ") that doesn't parse as a date or time.", e);
                }

                TimeGrain timeGrain =
                        timeGrainMap.get(dateTime);
                if (timeGrain == null) {
                    throw new IllegalStateException(currentPosition() + ": The meeting with id (" + meeting.getId()
                            + ") has a timeGrain date (" + dateString + ") and startTime (" + startTimeString
                            + ") that doesn't exist in the other sheet (Day).");
                }
                return timeGrain;
            }
            return null;
        }

        private Room extractRoom(Meeting meeting,
                Map<String, Room> roomMap) {
            String roomName = nextStringCell().getStringCellValue();
            if (!roomName.isEmpty()) {
                Room room = roomMap.get(roomName);
                if (room == null) {
                    throw new IllegalStateException(currentPosition() + ": The meeting with id (" + meeting.getId()
                            + ") has a roomName (" + roomName
                            + ") that doesn't exist in the other sheet (Rooms).");
                }
                return room;
            }
            return null;
        }

        private void readDayList() {
            nextSheet("Days");
            nextRow(false);
            readHeaderCell("Day");
            readHeaderCell("Start");
            readHeaderCell("End");
            List<Day> dayList =
                    new ArrayList<>(currentSheet.getLastRowNum() - 1);
            List<TimeGrain> timeGrainList = new ArrayList<>();
            int dayId = 0;
            int timeGrainId = 0;
            while (nextRow()) {
                Day day =
                        new Day(dayId, LocalDate.parse(nextStringCell().getStringCellValue(), DAY_FORMATTER).getDayOfYear());
                dayList.add(day);
                dayId++;

                LocalTime startTime = LocalTime.parse(nextStringCell().getStringCellValue(), TIME_FORMATTER);
                LocalTime endTime = LocalTime.parse(nextStringCell().getStringCellValue(), TIME_FORMATTER);
                LocalTime lunchHourStartTime = LocalTime.parse(nextStringCell().getStringCellValue(), TIME_FORMATTER);
                int startMinuteOfDay = startTime.getHour() * 60 + startTime.getMinute();
                int endMinuteOfDay = endTime.getHour() * 60 + endTime.getMinute();
                int lunchHourStartMinuteOfDay = lunchHourStartTime.getHour() * 60 + lunchHourStartTime.getMinute();
                for (int i = 0; (endMinuteOfDay - startMinuteOfDay) > i
                        * TimeGrain.GRAIN_LENGTH_IN_MINUTES; i++) {
                    int timeGrainStartingMinuteOfDay = i
                            * TimeGrain.GRAIN_LENGTH_IN_MINUTES
                            + startMinuteOfDay;
                    if (timeGrainStartingMinuteOfDay < lunchHourStartMinuteOfDay
                            || timeGrainStartingMinuteOfDay >= lunchHourStartMinuteOfDay + 60) {
                        TimeGrain timeGrain =
                                new TimeGrain(timeGrainId, timeGrainId++, day, timeGrainStartingMinuteOfDay);
                        timeGrainList.add(timeGrain);
                    }
                }
            }
            solution.setDayList(dayList);
            solution.setTimeGrainList(timeGrainList);
        }

        private void readRoomList() {
            nextSheet("Rooms");
            nextRow();
            readHeaderCell("Name");
            readHeaderCell("Capacity");
            List<Room> roomList =
                    new ArrayList<>(currentSheet.getLastRowNum() - 1);
            long id = 0L;
            while (nextRow()) {
                String name = nextStringCell().getStringCellValue();
                if (!VALID_NAME_PATTERN.matcher(name).matches()) {
                    throw new IllegalStateException(
                            currentPosition() + ": The room name (" + name
                                    + ") must match to the regular expression (" + VALID_NAME_PATTERN + ").");
                }
                double capacityDouble = nextNumericCell().getNumericCellValue();
                if (capacityDouble <= 0 || capacityDouble != Math.floor(capacityDouble)) {
                    throw new IllegalStateException(
                            currentPosition() + ": The room with name (" + name
                                    + ") has a capacity (" + capacityDouble
                                    + ") that isn't a strictly positive integer number.");
                }
                Room room =
                        new Room(id++, name, (int) capacityDouble);
                roomList.add(room);
            }
            solution.setRoomList(roomList);
        }
    }

}
