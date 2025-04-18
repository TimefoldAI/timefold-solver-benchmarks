
package ai.timefold.solver.benchmarks.examples.conferencescheduling.persistence;

import java.io.File;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import ai.timefold.solver.benchmarks.examples.common.app.CommonApp;
import ai.timefold.solver.benchmarks.examples.common.app.LoggingMain;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractSolutionImporter;
import ai.timefold.solver.benchmarks.examples.common.persistence.generator.StringDataGenerator;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.app.ConferenceSchedulingApp;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceConstraintProperties;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.ConferenceSolution;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.Room;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.Speaker;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.Talk;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.TalkType;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.domain.Timeslot;
import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;

public class ConferenceSchedulingGenerator extends LoggingMain {

    public static void main(String[] args) {
        var generator = new ConferenceSchedulingGenerator();
        generator.writeConferenceSolution(1, 5);
        generator.writeConferenceSolution(2, 5);
        generator.writeConferenceSolution(2, 10);
        generator.writeConferenceSolution(3, 10);
        generator.writeConferenceSolution(3, 20);
    }

    private final StringDataGenerator conferenceNameGenerator = new StringDataGenerator()
            .addPart(true, 0,
                    "Javoxx",
                    "Red Bonnet Summit",
                    "JayFocus",
                    "YCon",
                    "JAQ")
            .addPart(false, 0,
                    "2021",
                    "2022",
                    "2023",
                    "2024",
                    "2025");

    private static final String LAB_TALK_TYPE = "Lab";
    private static final String BREAKOUT_TALK_TYPE = "Breakout";

    private final LocalDate timeslotFirstDay = LocalDate.of(2018, 10, 1);

    private final List<Pair<LocalTime, LocalTime>> timeslotOptions = Arrays.asList(
            //        new Pair<>(LocalTime.of(8, 30), LocalTime.of(9, 30)), // General session
            new Pair<>(LocalTime.of(10, 15), LocalTime.of(12, 15)), // Lab
            new Pair<>(LocalTime.of(10, 15), LocalTime.of(11, 0)),
            new Pair<>(LocalTime.of(11, 30), LocalTime.of(12, 15)),
            new Pair<>(LocalTime.of(13, 0), LocalTime.of(15, 0)), // Lab
            //        new Pair<>(LocalTime.of(13, 45), LocalTime.of(15, 0)), // General session
            new Pair<>(LocalTime.of(15, 30), LocalTime.of(16, 15)),
            new Pair<>(LocalTime.of(16, 30), LocalTime.of(17, 15)));

    private final List<Pair<String, Double>> roomTagProbabilityList = Arrays.asList(
            new Pair<>("Large", 0.20),
            new Pair<>("Recorded", 0.50));

    private final StringDataGenerator speakerNameGenerator = StringDataGenerator.buildFullNames();

    private final String[] contentTagOptions = {
            "OpenShift",
            "WildFly",
            "Spring",
            "Drools",
            "Timefold",
            "jBPM",
            "Camel",
            "Jackson",
            "Docker",
            "Hibernate",
            "GWT",
            "Errai",
            "Angular",
            "Weld",
            "RestEasy",
            "Android",
            "Tensorflow",
            "VertX",
            "JUnit",
            "Keycloak"
    };
    private final StringDataGenerator talkTitleGenerator = new StringDataGenerator()
            .addPart(true, 0,
                    "Hands on",
                    "Advanced",
                    "Learn",
                    "Intro to",
                    "Discover",
                    "Mastering",
                    "Tuning",
                    "Building",
                    "Securing",
                    "Debug",
                    "Prepare for",
                    "Understand",
                    "Applying",
                    "Grok",
                    "Troubleshooting",
                    "Using",
                    "Deliver",
                    "Implement",
                    "Program",
                    "Hack")
            .addPart(true, 0,
                    "real-time",
                    "containerized",
                    "virtualized",
                    "serverless",
                    "AI-driven",
                    "machine learning",
                    "IOT-driven",
                    "deep learning",
                    "scalable",
                    "enterprise",
                    "streaming",
                    "mobile",
                    "modern",
                    "distributed",
                    "reliable",
                    "secure",
                    "stable",
                    "platform-independent",
                    "flexible",
                    "modularized")
            .addPart(true, 1,
                    contentTagOptions)
            .addPart(false, 3,
                    "in a nutshell",
                    "in practice",
                    "for dummies",
                    "in action",
                    "recipes",
                    "on the web",
                    "for decision makers",
                    "on the whiteboard",
                    "out of the box",
                    "for programmers",
                    "for managers",
                    "for QA engineers",
                    "in Java",
                    "in Scala",
                    "in Kotlin",
                    "in Lisp",
                    "in C++",
                    "in Assembly",
                    "with style",
                    "like a pro");

    private final List<String> themeTagOptions = Arrays.asList(
            "Artificial Intelligence",
            "Cloud",
            "Big Data",
            "Culture",
            "Middleware",
            "Mobile",
            "IoT",
            "Modern Web",
            "Security");

    private final List<String> sectorTagOptions = Arrays.asList(
            "Education",
            "Financial services",
            "Government",
            "Healthcare",
            "Telecommunications",
            "Transportation");
    private final List<String> audienceTypeOptions = Arrays.asList(
            "Programmers",
            "Business analysts",
            "Managers");

    private static final String TIMESLOT_AFTER_LUNCH_TAG = "After lunch";
    private static final List<String> mutuallyExclusiveTagList = List.of("Platinum Sponsor");

    protected final SolutionFileIO<ConferenceSolution> solutionFileIO;
    protected final File outputDir;

    private TalkType breakoutTalkType;
    private TalkType labTalkType;
    protected int labTalkCount;
    protected Random random;

    public ConferenceSchedulingGenerator() {
        solutionFileIO = new ConferenceSchedulingSolutionFileIO();
        outputDir = new File(CommonApp.determineDataDir(ConferenceSchedulingApp.DATA_DIR_NAME), "unsolved");
    }

    private void writeConferenceSolution(int dayListSize, int roomListSize) {
        var labTimeslotCount = (int) timeslotOptions.stream()
                .filter(pair -> Duration.between(pair.key(), pair.value()).toMinutes() >= 120).count();
        var labRoomCount = roomListSize / 5;
        labTalkCount = (dayListSize * labTimeslotCount) * labRoomCount;

        var timeslotListSize = dayListSize * timeslotOptions.size();
        var talkListSize = (dayListSize * (timeslotOptions.size() - labTimeslotCount)) * (roomListSize - labRoomCount)
                + labTalkCount;
        var speakerListSize = talkListSize * 2 / 3;

        var fileName = talkListSize + "talks-" + timeslotListSize + "timeslots-" + roomListSize + "rooms";
        var outputFile = new File(outputDir, fileName + "." + solutionFileIO.getOutputFileExtension());
        var solution = createConferenceSolution(fileName, timeslotListSize, roomListSize, speakerListSize, talkListSize);
        solutionFileIO.write(solution, outputFile);
    }

    public ConferenceSolution createConferenceSolution(String fileName, int timeslotListSize, int roomListSize,
            int speakerListSize, int talkListSize) {
        random = new Random(37);
        var solution = new ConferenceSolution(0L);
        solution.setConstraintWeightOverrides(ConstraintWeightOverrides.none());
        solution.setConferenceName(conferenceNameGenerator.generateNextValue());

        var constraintProperties = new ConferenceConstraintProperties(0L);
        constraintProperties.setMinimumConsecutiveTalksPauseInMinutes(15);
        solution.setConstraintProperties(constraintProperties);
        solution.setConstraintWeightOverrides(ConstraintWeightOverrides.none());

        createTalkTypeList(solution);
        createTimeslotList(solution, timeslotListSize);
        createRoomList(solution, roomListSize);
        createSpeakerList(solution, speakerListSize);
        createTalkList(solution, talkListSize);

        var possibleSolutionSize = BigInteger.valueOf((long) timeslotListSize * roomListSize)
                .pow(talkListSize);
        logger.info("Conference {} has {} talks, {} timeslots and {} rooms with a search space of {}.",
                fileName,
                talkListSize,
                timeslotListSize,
                roomListSize,
                AbstractSolutionImporter.getFlooredPossibleSolutionSize(possibleSolutionSize));
        return solution;
    }

    private void createTalkTypeList(ConferenceSolution solution) {
        var talkTypeList = new ArrayList<TalkType>(2);
        breakoutTalkType = new TalkType(0L);
        breakoutTalkType.setName(BREAKOUT_TALK_TYPE);
        breakoutTalkType.setCompatibleTimeslotSet(new LinkedHashSet<>());
        breakoutTalkType.setCompatibleRoomSet(new LinkedHashSet<>());
        talkTypeList.add(breakoutTalkType);
        labTalkType = new TalkType(1L);
        labTalkType.setName(LAB_TALK_TYPE);
        labTalkType.setCompatibleTimeslotSet(new LinkedHashSet<>());
        labTalkType.setCompatibleRoomSet(new LinkedHashSet<>());
        talkTypeList.add(labTalkType);
        solution.setTalkTypeList(talkTypeList);
    }

    private void createTimeslotList(ConferenceSolution solution, int timeslotListSize) {
        var timeslotList = new ArrayList<Timeslot>(timeslotListSize);
        var timeslotOptionsIndex = 0;
        var day = timeslotFirstDay;
        for (var i = 0; i < timeslotListSize; i++) {
            var timeslot = new Timeslot(i);
            if (timeslotOptionsIndex >= timeslotOptions.size()) {
                timeslotOptionsIndex = 0;
                day = day.plusDays(1);
            }
            var pair = timeslotOptions.get(timeslotOptionsIndex);
            timeslot.setStartDateTime(LocalDateTime.of(day, pair.key()));
            timeslot.setEndDateTime(LocalDateTime.of(day, pair.value()));
            var talkType = timeslot.getDurationInMinutes() >= 120 ? labTalkType : breakoutTalkType;
            talkType.getCompatibleTimeslotSet().add(timeslot);
            timeslot.setTalkTypeSet(Collections.singleton(talkType));
            timeslotOptionsIndex++;
            var tagSet = new LinkedHashSet<String>(2);
            if (timeslot.getStartDateTime().getHour() == 13) {
                tagSet.add(TIMESLOT_AFTER_LUNCH_TAG);
            }
            timeslot.setTagSet(tagSet);
            logger.trace("Created timeslot ({}) with tags ({}).", timeslot, tagSet);
            timeslotList.add(timeslot);
        }
        solution.setTimeslotList(timeslotList);
    }

    private void createRoomList(ConferenceSolution solution, int roomListSize) {
        var roomsPerFloor = 12;
        var roomList = new ArrayList<Room>(roomListSize);
        for (var i = 0; i < roomListSize; i++) {
            var room = new Room(i);
            room.setName("R " + ((i / roomsPerFloor * 100) + (i % roomsPerFloor) + 1));
            room.setCapacity((1 + random.nextInt(100)) * 10);
            TalkType talkType;
            if (i % 5 == 4) {
                talkType = labTalkType;
            } else {
                talkType = breakoutTalkType;
            }
            talkType.getCompatibleRoomSet().add(room);
            room.withTalkTypeSet(Collections.singleton(talkType));
            var tagSet = new LinkedHashSet<String>(roomTagProbabilityList.size());
            for (var roomTagProbability : roomTagProbabilityList) {
                if ((i == 0 || i == 4 || random.nextDouble() < roomTagProbability.value())
                        && roomTagProbability.key().equals("Recorded")) {
                    tagSet.add(roomTagProbability.key());
                }
            }
            if (room.getCapacity() >= 500) {
                tagSet.add("Large");
            }
            room.setTagSet(tagSet);
            logger.trace("Created room with name ({}) and tags ({}).",
                    room.getName(), tagSet);
            roomList.add(room);
        }
        solution.setRoomList(roomList);
    }

    private void createSpeakerList(ConferenceSolution solution, int speakerListSize) {
        var speakerList = new ArrayList<Speaker>(speakerListSize);
        speakerNameGenerator.predictMaximumSizeAndReset(speakerListSize);
        for (var i = 0; i < speakerListSize; i++) {
            var speaker = new Speaker(i);
            speaker.setName(speakerNameGenerator.generateNextValue());
            Set<Timeslot> unavailableTimeslotSet;
            var preferredTimeslotTagSet = new LinkedHashSet<String>();
            var undesiredTimeslotTagSet = new LinkedHashSet<String>();
            var timeslotList = solution.getTimeslotList();
            if (random.nextDouble() < 0.10) {
                var segmentRandom = random.nextDouble();
                if (segmentRandom < 0.10) {
                    // No mornings
                    unavailableTimeslotSet = timeslotList.stream()
                            .filter(timeslot -> timeslot.getStartDateTime().toLocalTime().isBefore(LocalTime.of(12, 0)))
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    preferredTimeslotTagSet.add(TIMESLOT_AFTER_LUNCH_TAG);
                } else if (segmentRandom < 0.20) {
                    // No afternoons
                    unavailableTimeslotSet = timeslotList.stream()
                            .filter(timeslot -> !timeslot.getStartDateTime().toLocalTime().isBefore(LocalTime.of(12, 0)))
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                } else if (segmentRandom < 0.30) {
                    // Only 1 day available
                    var availableDate = timeslotList.get(random.nextInt(timeslotList.size())).getDate();
                    unavailableTimeslotSet = timeslotList.stream()
                            .filter(timeslot -> !timeslot.getDate().equals(availableDate))
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                } else {
                    unavailableTimeslotSet = timeslotList.stream()
                            .filter(timeslot -> random.nextDouble() < 0.05)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    undesiredTimeslotTagSet.add(TIMESLOT_AFTER_LUNCH_TAG);
                }
            } else {
                unavailableTimeslotSet = new LinkedHashSet<>(timeslotList.size());
            }
            speaker.setUnavailableTimeslotSet(unavailableTimeslotSet);
            speaker.setPreferredTimeslotTagSet(preferredTimeslotTagSet);
            speaker.setUndesiredTimeslotTagSet(undesiredTimeslotTagSet);
            var requiredRoomTagSet = new LinkedHashSet<String>();
            var preferredRoomTagSet = new LinkedHashSet<String>();
            var prohibitedRoomTagSet = new LinkedHashSet<String>();
            var undesiredRoomTagSet = new LinkedHashSet<String>();
            initializeRoomTagSets(requiredRoomTagSet, preferredRoomTagSet, prohibitedRoomTagSet, undesiredRoomTagSet);
            speaker.setRequiredRoomTagSet(requiredRoomTagSet);
            speaker.setPreferredRoomTagSet(preferredRoomTagSet);
            speaker.setProhibitedRoomTagSet(prohibitedRoomTagSet);
            speaker.setUndesiredRoomTagSet(undesiredRoomTagSet);
            logger.trace("Created speaker with name ({}).", speaker.getName());
            speakerList.add(speaker);
        }
        solution.setSpeakerList(speakerList);
    }

    private void createTalkList(ConferenceSolution solution, int talkListSize) {
        var talkList = new ArrayList<Talk>(talkListSize);
        talkTitleGenerator.predictMaximumSizeAndReset(talkListSize);
        var speakerListIndex = 0;
        for (var i = 0; i < talkListSize; i++) {
            var talk = new Talk(i);
            talk.setCode(String.format("S%0" + ((String.valueOf(talkListSize).length()) + "d"), i));
            talk.setTitle(talkTitleGenerator.generateNextValue());
            var speakerRandomDouble = random.nextDouble();
            talk.setTalkType(i < labTalkCount ? labTalkType : breakoutTalkType);
            var speakerCount = (speakerRandomDouble < 0.01) ? 4
                    : (speakerRandomDouble < 0.03) ? 3 : (speakerRandomDouble < 0.40) ? 2 : 1;
            var speakerList = new ArrayList<Speaker>(speakerCount);
            for (var j = 0; j < speakerCount; j++) {
                speakerList.add(solution.getSpeakerList().get(speakerListIndex));
                speakerListIndex = (speakerListIndex + 1) % solution.getSpeakerList().size();
            }
            talk.setSpeakerList(speakerList);
            var themeTagSet = new LinkedHashSet<String>();
            themeTagSet.add(themeTagOptions.get(random.nextInt(themeTagOptions.size())));
            if (random.nextDouble() < 0.20) {
                themeTagSet.add(themeTagOptions.get(random.nextInt(themeTagOptions.size())));
            }
            talk.setThemeTrackTagSet(themeTagSet);
            var sectorTagSet = new LinkedHashSet<String>();
            if (random.nextDouble() < 0.20) {
                sectorTagSet.add(sectorTagOptions.get(random.nextInt(sectorTagOptions.size())));
            }
            talk.setAudienceTypeSet(Collections.singleton(audienceTypeOptions.get(random.nextInt(audienceTypeOptions.size()))));
            talk.setAudienceLevel(1 + random.nextInt(3));
            var contentTagSet = new LinkedHashSet<String>();
            for (var contentTagOption : contentTagOptions) {
                if (talk.getTitle().contains(contentTagOption)) {
                    contentTagSet.add(contentTagOption);
                    if ((contentTagOption.equalsIgnoreCase("OpenShift") || contentTagOption.equalsIgnoreCase("Docker"))
                            && random.nextDouble() < 0.40) {
                        contentTagSet.add("Kubernetes");
                    }
                    break;
                }
            }
            talk.setContentTagSet(contentTagSet);
            talk.setSectorTagSet(sectorTagSet);
            talk.setLanguage("en");

            var requiredRoomTagSet = new LinkedHashSet<String>();
            var preferredRoomTagSet = new LinkedHashSet<String>();
            var prohibitedRoomTagSet = new LinkedHashSet<String>();
            var undesiredRoomTagSet = new LinkedHashSet<String>();
            initializeRoomTagSets(requiredRoomTagSet, preferredRoomTagSet, prohibitedRoomTagSet, undesiredRoomTagSet);
            talk.setRequiredRoomTagSet(requiredRoomTagSet);
            talk.setPreferredRoomTagSet(preferredRoomTagSet);
            talk.setProhibitedRoomTagSet(prohibitedRoomTagSet);
            talk.setUndesiredRoomTagSet(undesiredRoomTagSet);

            var mutuallyExclusiveTagSet = new LinkedHashSet<String>();
            if (random.nextDouble() < 0.025) {
                mutuallyExclusiveTagSet.add(mutuallyExclusiveTagList.get(random.nextInt(mutuallyExclusiveTagList.size())));
            }
            talk.setMutuallyExclusiveTalksTagSet(mutuallyExclusiveTagSet);

            var prerequisiteTalkCodeSet = new LinkedHashSet<Talk>();
            if (random.nextDouble() < 0.025) {
                prerequisiteTalkCodeSet.add(talkList.get(random.nextInt(talkList.size())));
            }
            talk.setPrerequisiteTalkSet(prerequisiteTalkCodeSet);

            talk.setFavoriteCount(random.nextInt(1000));
            if (random.nextDouble() < 0.02) {
                talk.setCrowdControlRisk(1);
                // Need an even number of talks with crowd control > 1 for a feasible solution
                var pairedTalk = talkList.get(random.nextInt(talkList.size()));
                while (pairedTalk.getCrowdControlRisk() != 0 || !pairedTalk.getTalkType().equals(talk.getTalkType())) {
                    pairedTalk = talkList.get(random.nextInt(talkList.size()));
                }
                pairedTalk.setCrowdControlRisk(1);
            }
            talk.setCrowdControlRisk(0); // Disabled for now: the unsolved schedules must have a feasible solution
            logger.trace("Created talk with code ({}), title ({}) and speakers ({}).",
                    talk.getCode(), talk.getTitle(), speakerList);
            talkList.add(talk);
        }

        var pinnedTalk = talkList.get(labTalkCount + random.nextInt(talkListSize - labTalkCount));
        pinnedTalk.setPinnedByUser(true);
        pinnedTalk.setTimeslot(solution.getTimeslotList().stream()
                .filter(timeslot -> timeslot.getTalkTypeSet().contains(breakoutTalkType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("There is no breakout timeslot.")));
        pinnedTalk.setRoom(solution.getRoomList().get(0));
        solution.setTalkList(talkList);
    }

    private void initializeRoomTagSets(Set<String> requiredRoomTagSet, Set<String> preferredRoomTagSet,
            Set<String> prohibitedRoomTagSet, Set<String> undesiredRoomTagSet) {
        for (var roomTagProbability : roomTagProbabilityList) {
            var segmentRandom = random.nextDouble();
            if (segmentRandom < roomTagProbability.value() / 25.0) {
                requiredRoomTagSet.add(roomTagProbability.key());
            } else if (segmentRandom < roomTagProbability.value() / 20.0) {
                prohibitedRoomTagSet.add(roomTagProbability.key());
            } else if (segmentRandom < roomTagProbability.value() / 15.0) {
                preferredRoomTagSet.add(roomTagProbability.key());
            } else if (segmentRandom < roomTagProbability.value() / 10.0) {
                undesiredRoomTagSet.add(roomTagProbability.key());
            }
        }
    }

    private record Pair<A, B>(A key, B value) {

    }

}
