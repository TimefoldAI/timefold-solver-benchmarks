package ai.timefold.solver.benchmarks.examples.examination.persistence;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractTxtSolutionImporter;
import ai.timefold.solver.benchmarks.examples.common.persistence.SolutionConverter;
import ai.timefold.solver.benchmarks.examples.examination.app.ExaminationApp;
import ai.timefold.solver.benchmarks.examples.examination.domain.Exam;
import ai.timefold.solver.benchmarks.examples.examination.domain.Examination;
import ai.timefold.solver.benchmarks.examples.examination.domain.ExaminationConstraintProperties;
import ai.timefold.solver.benchmarks.examples.examination.domain.FollowingExam;
import ai.timefold.solver.benchmarks.examples.examination.domain.LeadingExam;
import ai.timefold.solver.benchmarks.examples.examination.domain.Period;
import ai.timefold.solver.benchmarks.examples.examination.domain.PeriodPenalty;
import ai.timefold.solver.benchmarks.examples.examination.domain.PeriodPenaltyType;
import ai.timefold.solver.benchmarks.examples.examination.domain.Room;
import ai.timefold.solver.benchmarks.examples.examination.domain.RoomPenalty;
import ai.timefold.solver.benchmarks.examples.examination.domain.RoomPenaltyType;
import ai.timefold.solver.benchmarks.examples.examination.domain.Student;
import ai.timefold.solver.benchmarks.examples.examination.domain.Topic;
import ai.timefold.solver.benchmarks.examples.examination.domain.solver.TopicConflict;
import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardSoftScore;

public class ExaminationImporter extends AbstractTxtSolutionImporter<Examination> {

    private static final String INPUT_FILE_SUFFIX = "exam";
    private static final String SPLIT_REGEX = "\\,\\ ?";

    public static void main(String[] args) {
        var converter = SolutionConverter.createImportConverter(ExaminationApp.DATA_DIR_NAME,
                new ExaminationImporter(), new ExaminationSolutionFileIO());
        converter.convertAll();
    }

    @Override
    public String getInputFileSuffix() {
        return INPUT_FILE_SUFFIX;
    }

    @Override
    public TxtInputBuilder<Examination> createTxtInputBuilder() {
        return new ExaminationInputBuilder();
    }

    public static class ExaminationInputBuilder extends TxtInputBuilder<Examination> {

        private static final Comparator<Topic> COMPARATOR = Comparator.comparing(Topic::getStudentSize)
                .thenComparingLong(Topic::getId);
        private Examination examination;
        private Map<Topic, Set<Topic>> coincidenceMap;
        private Map<Topic, Set<Topic>> exclusionMap;
        private Map<Topic, Set<Topic>> afterMap;

        @Override
        public Examination readSolution() throws IOException {
            examination = new Examination(0L);

            readTopicListAndStudentList();
            readPeriodList();
            readRoomList();

            readPeriodPenaltyList();
            readRoomPenaltyList();
            readInstitutionalWeighting();
            tagFrontLoadLargeTopics();
            tagFrontLoadLastPeriods();

            createExamList();

            var possibleForOneExamSize = examination.getPeriodList().size() * examination.getRoomList().size();
            var possibleSolutionSize = BigInteger.valueOf(possibleForOneExamSize).pow(
                    examination.getExamList().size());
            logger.info("Examination {} has {} students, {} exams, {} periods, {} rooms, {} period constraints"
                    + " and {} room constraints with a search space of {}.",
                    getInputId(),
                    examination.getStudentList().size(),
                    examination.getExamList().size(),
                    examination.getPeriodList().size(),
                    examination.getRoomList().size(),
                    examination.getPeriodPenaltyList().size(),
                    examination.getRoomPenaltyList().size(),
                    getFlooredPossibleSolutionSize(possibleSolutionSize));
            return examination;
        }

        private void readTopicListAndStudentList() throws IOException {
            coincidenceMap = new LinkedHashMap<>();
            exclusionMap = new LinkedHashMap<>();
            afterMap = new LinkedHashMap<>();
            Map<Integer, Student> studentMap = new HashMap<>();
            var examSize = readHeaderWithNumber("Exams");
            List<Topic> topicList = new ArrayList<>(examSize);
            for (var i = 0; i < examSize; i++) {
                var topic = new Topic()
                        .withId(i);
                var line = bufferedReader.readLine();
                var lineTokens = line.split(SPLIT_REGEX);
                topic.setDuration(Integer.parseInt(lineTokens[0]));
                Set<Student> topicStudentList = new LinkedHashSet<>(lineTokens.length - 1);
                for (var j = 1; j < lineTokens.length; j++) {
                    topicStudentList.add(findOrCreateStudent(studentMap, Integer.parseInt(lineTokens[j])));
                }
                topic.setStudentSet(topicStudentList);
                topic.setFrontLoadLarge(false);
                topicList.add(topic);
                coincidenceMap.put(topic, new HashSet<>());
                exclusionMap.put(topic, new HashSet<>());
                afterMap.put(topic, new HashSet<>());
            }
            examination.setTopicList(topicList);
            examination.setTopicConflictList(calculateTopicConflictList(topicList));
            List<Student> studentList = new ArrayList<>(studentMap.values());
            examination.setStudentList(studentList);
        }

        private List<TopicConflict> calculateTopicConflictList(List<Topic> topicList) {
            long nextId = 0;
            List<TopicConflict> topicConflictList = new ArrayList<>();
            for (var leftTopic : topicList) {
                for (var rightTopic : topicList) {
                    if (leftTopic.getId() < rightTopic.getId()) {
                        var studentSize = 0;
                        for (var student : leftTopic.getStudentSet()) {
                            if (rightTopic.getStudentSet().contains(student)) {
                                studentSize++;
                            }
                        }
                        if (studentSize > 0) {
                            topicConflictList.add(new TopicConflict(nextId++, leftTopic, rightTopic, studentSize));
                        }
                    }
                }
            }
            return topicConflictList;
        }

        private Student findOrCreateStudent(Map<Integer, Student> studentMap, int id) {
            var student = studentMap.get(id);
            if (student == null) {
                student = new Student(id);
                studentMap.put(id, student);
            }
            return student;
        }

        private void readPeriodList() throws IOException {
            var periodSize = readHeaderWithNumber("Periods");
            List<Period> periodList = new ArrayList<>(periodSize);
            var DATE_FORMAT = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm:ss", Locale.UK);
            LocalDateTime referenceDateTime = null;
            for (var i = 0; i < periodSize; i++) {
                var period = new Period().withId(i);
                var line = bufferedReader.readLine();
                var lineTokens = line.split(SPLIT_REGEX);
                if (lineTokens.length != 4) {
                    throw new IllegalArgumentException("Read line (" + line + ") is expected to contain 4 tokens.");
                }
                var startDateTimeString = lineTokens[0] + " " + lineTokens[1];
                period.setStartDateTimeString(startDateTimeString);
                period.setPeriodIndex(i);
                LocalDateTime dateTime;
                try {
                    dateTime = LocalDateTime.parse(startDateTimeString, DATE_FORMAT);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Illegal startDateTimeString (" + startDateTimeString + ").", e);
                }
                if (referenceDateTime == null) {
                    referenceDateTime = dateTime;
                }
                var dayIndex = (int) ChronoUnit.DAYS.between(referenceDateTime, dateTime);
                if (dayIndex < 0) {
                    throw new IllegalStateException("The periods should be in ascending order.");
                }
                period.setDayIndex(dayIndex);
                period.setDuration(Integer.parseInt(lineTokens[2]));
                period.setPenalty(Integer.parseInt(lineTokens[3]));
                periodList.add(period);
            }
            examination.setPeriodList(periodList);
        }

        private void readRoomList() throws IOException {
            var roomSize = readHeaderWithNumber("Rooms");
            List<Room> roomList = new ArrayList<>(roomSize);
            for (var i = 0; i < roomSize; i++) {
                var room = new Room().withId(i);
                var line = bufferedReader.readLine();
                var lineTokens = line.split(SPLIT_REGEX);
                if (lineTokens.length != 2) {
                    throw new IllegalArgumentException("Read line (" + line + ") is expected to contain 2 tokens.");
                }
                room.setCapacity(Integer.parseInt(lineTokens[0]));
                room.setPenalty(Integer.parseInt(lineTokens[1]));
                roomList.add(room);
            }
            examination.setRoomList(roomList);
        }

        private void readPeriodPenaltyList() throws IOException {
            readConstantLine("\\[PeriodHardConstraints\\]");
            var topicList = examination.getTopicList();
            List<PeriodPenalty> periodPenaltyList = new ArrayList<>();
            var line = bufferedReader.readLine();
            var id = 0;
            while (!line.equals("[RoomHardConstraints]")) {
                var lineTokens = line.split(SPLIT_REGEX);
                if (lineTokens.length != 3) {
                    throw new IllegalArgumentException("Read line (" + line + ") is expected to contain 3 tokens.");
                }
                var leftTopic = topicList.get(Integer.parseInt(lineTokens[0]));
                var periodPenaltyType = PeriodPenaltyType.valueOf(lineTokens[1]);
                var rightTopic = topicList.get(Integer.parseInt(lineTokens[2]));
                var periodPenalty = new PeriodPenalty(id, leftTopic, rightTopic, periodPenaltyType);
                id++;
                var ignorePenalty = false;

                switch (periodPenaltyType) {
                    case EXAM_COINCIDENCE:
                        if (leftTopic.getId() == rightTopic.getId()) {
                            logger.warn("  Filtering out periodPenalty (" + periodPenalty
                                    + ") because the left and right topic are the same.");
                            ignorePenalty = true;
                        } else if (!Collections.disjoint(leftTopic.getStudentSet(), rightTopic.getStudentSet())) {
                            throw new IllegalStateException("PeriodPenalty (" + periodPenalty
                                    + ") for leftTopic (" + leftTopic + ") and rightTopic (" + rightTopic
                                    + ")'s left and right topic share students.");
                        } else if (coincidenceMap.get(leftTopic).contains(rightTopic)) {
                            logger.trace("  Filtering out periodPenalty (" + periodPenalty
                                    + ") for leftTopic (" + leftTopic + ") and rightTopic (" + rightTopic
                                    + ") because it is mentioned twice.");
                            ignorePenalty = true;
                        } else {
                            var added = coincidenceMap.get(leftTopic).add(rightTopic)
                                    && coincidenceMap.get(rightTopic).add(leftTopic);
                            if (!added) {
                                throw new IllegalStateException("The periodPenaltyType (" + periodPenaltyType
                                        + ") for leftTopic (" + leftTopic + ") and rightTopic (" + rightTopic
                                        + ") was not successfully added twice.");
                            }
                        }
                        break;
                    case EXCLUSION:
                        if (leftTopic.getId() == rightTopic.getId()) {
                            logger.warn("  Filtering out periodPenalty (" + periodPenalty
                                    + ") for leftTopic (" + leftTopic + ") and rightTopic (" + rightTopic
                                    + ") because the left and right topic are the same.");
                            ignorePenalty = true;
                        } else if (exclusionMap.get(leftTopic).contains(rightTopic)) {
                            logger.trace("  Filtering out periodPenalty (" + periodPenalty
                                    + ") for leftTopic (" + leftTopic + ") and rightTopic (" + rightTopic
                                    + ") because it is mentioned twice.");
                            ignorePenalty = true;
                        } else {
                            var added = exclusionMap.get(leftTopic).add(rightTopic)
                                    && exclusionMap.get(rightTopic).add(leftTopic);
                            if (!added) {
                                throw new IllegalStateException("The periodPenaltyType (" + periodPenaltyType
                                        + ") for leftTopic (" + leftTopic + ") and rightTopic (" + rightTopic
                                        + ") was not successfully added twice.");
                            }
                        }
                        break;
                    case AFTER:
                        if (afterMap.get(leftTopic).contains(rightTopic)) {
                            ignorePenalty = true;
                        } else {
                            var added = afterMap.get(leftTopic).add(rightTopic);
                            if (!added) {
                                throw new IllegalStateException("The periodPenaltyType (" + periodPenaltyType
                                        + ") for leftTopic (" + leftTopic + ") and rightTopic (" + rightTopic
                                        + ") was not successfully added.");
                            }
                        }
                        break;
                    default:
                        throw new IllegalStateException("The periodPenaltyType ("
                                + periodPenalty.getPeriodPenaltyType() + ") is not implemented.");
                }
                if (!ignorePenalty) {
                    periodPenaltyList.add(periodPenalty);
                }
                line = bufferedReader.readLine();
            }
            // createIndirectPeriodPenalties of type EXAM_COINCIDENCE
            for (var entry : coincidenceMap.entrySet()) {
                var leftTopic = entry.getKey();
                var middleTopicSet = entry.getValue();
                for (var middleTopic : new ArrayList<>(middleTopicSet)) {
                    for (var rightTopic : new ArrayList<>(coincidenceMap.get(middleTopic))) {
                        if (rightTopic != leftTopic
                                && !middleTopicSet.contains(rightTopic)) {
                            var indirectPeriodPenalty =
                                    new PeriodPenalty(id, leftTopic, rightTopic, PeriodPenaltyType.EXAM_COINCIDENCE);
                            periodPenaltyList.add(indirectPeriodPenalty);
                            id++;

                            var added = coincidenceMap.get(leftTopic).add(rightTopic)
                                    && coincidenceMap.get(rightTopic).add(leftTopic);
                            if (!added) {
                                throw new IllegalStateException("The periodPenalty (" + indirectPeriodPenalty
                                        + ") for leftTopic (" + leftTopic + ") and rightTopic (" + rightTopic
                                        + ") was not successfully added twice.");
                            }
                        }
                    }
                }
            }
            // createIndirectPeriodPenalties of type AFTER
            for (var entry : afterMap.entrySet()) {
                var leftTopic = entry.getKey();
                var afterLeftSet = entry.getValue();
                Queue<Topic> queue = new ArrayDeque<>();
                for (var topic : afterMap.get(leftTopic)) {
                    queue.add(topic);
                    queue.addAll(coincidenceMap.get(topic));
                }
                while (!queue.isEmpty()) {
                    var rightTopic = queue.poll();
                    if (!afterLeftSet.contains(rightTopic)) {
                        var indirectPeriodPenalty =
                                new PeriodPenalty(id, leftTopic, rightTopic, PeriodPenaltyType.AFTER);
                        periodPenaltyList.add(indirectPeriodPenalty);
                        id++;

                        var added = afterMap.get(leftTopic).add(rightTopic);
                        if (!added) {
                            throw new IllegalStateException("The periodPenalty (" + indirectPeriodPenalty
                                    + ") for leftTopic (" + leftTopic + ") and rightTopic (" + rightTopic
                                    + ") was not successfully added.");
                        }
                    }
                    for (var topic : afterMap.get(rightTopic)) {
                        queue.add(topic);
                        queue.addAll(coincidenceMap.get(topic));
                    }
                }
            }
            examination.setPeriodPenaltyList(periodPenaltyList);
        }

        private void readRoomPenaltyList() throws IOException {
            var topicList = examination.getTopicList();
            List<RoomPenalty> roomPenaltyList = new ArrayList<>();
            var line = bufferedReader.readLine();
            var id = 0;
            while (!line.equals("[InstitutionalWeightings]")) {
                var lineTokens = line.split(SPLIT_REGEX);
                if (lineTokens.length != 2) {
                    throw new IllegalArgumentException("Read line (" + line + ") is expected to contain 2 tokens.");
                }
                var roomPenalty = new RoomPenalty()
                        .withId(id)
                        .withTopic(topicList.get(Integer.parseInt(lineTokens[0])))
                        .withRoomPenaltyType(RoomPenaltyType.valueOf(lineTokens[1]));
                roomPenaltyList.add(roomPenalty);
                line = bufferedReader.readLine();
                id++;
            }
            examination.setRoomPenaltyList(roomPenaltyList);
        }

        private int readHeaderWithNumber(String header) throws IOException {
            var line = bufferedReader.readLine();
            if (!line.startsWith("[" + header + ":") || !line.endsWith("]")) {
                throw new IllegalStateException("Read line (" + line + " is not the expected header (["
                        + header + ":number])");
            }
            return Integer.parseInt(line.substring(header.length() + 2, line.length() - 1));
        }

        private void readInstitutionalWeighting() throws IOException {
            var constraintProperties = new ExaminationConstraintProperties(0L);
            examination.setConstraintProperties(constraintProperties);
            var overrides = new LinkedHashMap<String, HardSoftScore>();
            var lineTokens = readInstitutionalWeightingProperty("TWOINAROW", 2);
            overrides.put("twoExamsInARow", HardSoftScore.ofSoft(Integer.parseInt(lineTokens[1])));
            lineTokens = readInstitutionalWeightingProperty("TWOINADAY", 2);
            overrides.put("twoExamsInADay", HardSoftScore.ofSoft(Integer.parseInt(lineTokens[1])));
            lineTokens = readInstitutionalWeightingProperty("PERIODSPREAD", 2);
            constraintProperties.setPeriodSpreadLength(Integer.parseInt(lineTokens[1]));
            overrides.put("periodSpread", HardSoftScore.ONE_SOFT);
            lineTokens = readInstitutionalWeightingProperty("NONMIXEDDURATIONS", 2);
            overrides.put("mixedDurations", HardSoftScore.ofSoft(Integer.parseInt(lineTokens[1])));
            lineTokens = readInstitutionalWeightingProperty("FRONTLOAD", 4);
            constraintProperties.setFrontLoadLargeTopicSize(Integer.parseInt(lineTokens[1]));
            constraintProperties.setFrontLoadLastPeriodSize(Integer.parseInt(lineTokens[2]));
            overrides.put("frontLoad", HardSoftScore.ofSoft(Integer.parseInt(lineTokens[3])));
            examination.setConstraintWeightOverrides(ConstraintWeightOverrides.of(overrides));
        }

        private String[] readInstitutionalWeightingProperty(String property,
                int propertySize) throws IOException {
            String[] lineTokens;
            lineTokens = bufferedReader.readLine().split(SPLIT_REGEX);
            if (!lineTokens[0].equals(property) || lineTokens.length != propertySize) {
                throw new IllegalArgumentException("Read line (" + Arrays.toString(lineTokens)
                        + ") is expected to contain " + propertySize + " tokens and start with " + property + ".");
            }
            return lineTokens;
        }

        private void tagFrontLoadLargeTopics() {
            List<Topic> sortedTopicList = new ArrayList<>(examination.getTopicList());
            sortedTopicList.sort(COMPARATOR);
            int frontLoadLargeTopicSize = examination.getConstraintProperties().getFrontLoadLargeTopicSize();
            if (frontLoadLargeTopicSize == 0) {
                return;
            }
            var minimumTopicId = sortedTopicList.size() - frontLoadLargeTopicSize;
            if (minimumTopicId < 0) {
                logger.warn("The frontLoadLargeTopicSize (" + frontLoadLargeTopicSize
                        + ") is bigger than topicListSize (" + sortedTopicList.size()
                        + "). Tagging all topic as frontLoadLarge...");
                minimumTopicId = 0;
            }
            for (var topic : sortedTopicList.subList(minimumTopicId, sortedTopicList.size())) {
                topic.setFrontLoadLarge(true);
            }
        }

        private void tagFrontLoadLastPeriods() {
            var periodList = examination.getPeriodList();
            int frontLoadLastPeriodSize = examination.getConstraintProperties().getFrontLoadLastPeriodSize();
            if (frontLoadLastPeriodSize == 0) {
                return;
            }
            var minimumPeriodId = periodList.size() - frontLoadLastPeriodSize;
            if (minimumPeriodId < 0) {
                logger.warn("The frontLoadLastPeriodSize (" + frontLoadLastPeriodSize
                        + ") is bigger than periodListSize (" + periodList.size()
                        + "). Tagging all periods as frontLoadLast...");
                minimumPeriodId = 0;
            }
            for (var period : periodList.subList(minimumPeriodId, periodList.size())) {
                period.setFrontLoadLast(true);
            }
        }

        private void createExamList() {
            var topicList = examination.getTopicList();
            List<Exam> examList = new ArrayList<>(topicList.size());
            Map<Topic, LeadingExam> leadingTopicToExamMap = new HashMap<>(topicList.size());
            for (var topic : topicList) {
                Exam exam;
                var leadingTopic = topic;
                for (var coincidenceTopic : coincidenceMap.get(topic)) {
                    if (coincidenceTopic.getId() < leadingTopic.getId()) {
                        leadingTopic = coincidenceTopic;
                    }
                }
                if (leadingTopic == topic) {
                    var leadingExam = new LeadingExam().withId(topic.getId());
                    leadingExam.setFollowingExamList(new ArrayList<>(10));
                    leadingTopicToExamMap.put(topic, leadingExam);
                    exam = leadingExam;
                } else {
                    var followingExam = new FollowingExam().withId(topic.getId());
                    var leadingExam = leadingTopicToExamMap.get(leadingTopic);
                    if (leadingExam == null) {
                        throw new IllegalStateException("The followingExam (" + topic.getId()
                                + ")'s leadingExam (" + leadingExam + ") cannot be null.");
                    }
                    followingExam.setLeadingExam(leadingExam);
                    leadingExam.getFollowingExamList().add(followingExam);
                    exam = followingExam;
                }
                exam.setTopic(topic);
                // Notice that we leave the PlanningVariable properties on null
                examList.add(exam);
            }
            examination.setExamList(examList);
        }
    }
}
