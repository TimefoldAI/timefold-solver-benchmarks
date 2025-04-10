package ai.timefold.solver.benchmarks.examples.nurserostering.persistence;

import static java.time.temporal.ChronoUnit.DAYS;

import java.io.IOException;
import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractXmlSolutionImporter;
import ai.timefold.solver.benchmarks.examples.common.persistence.SolutionConverter;
import ai.timefold.solver.benchmarks.examples.nurserostering.app.NurseRosteringApp;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.Employee;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.NurseRoster;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.NurseRosterParametrization;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.Shift;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.ShiftAssignment;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.ShiftDate;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.ShiftType;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.ShiftTypeSkillRequirement;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.Skill;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.SkillProficiency;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.WeekendDefinition;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.contract.BooleanContractLine;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.contract.Contract;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.contract.ContractLine;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.contract.ContractLineType;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.contract.MinMaxContractLine;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.contract.PatternContractLine;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.pattern.FreeBefore2DaysWithAWorkDayPattern;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.pattern.Pattern;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.pattern.ShiftType2DaysPattern;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.pattern.ShiftType3DaysPattern;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.pattern.WorkBeforeFreeSequencePattern;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.request.DayOffRequest;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.request.DayOnRequest;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.request.ShiftOffRequest;
import ai.timefold.solver.benchmarks.examples.nurserostering.domain.request.ShiftOnRequest;

import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.jdom2.JDOMException;

public class NurseRosteringImporter extends AbstractXmlSolutionImporter<NurseRoster> {

    public static void main(String[] args) {
        var converter = SolutionConverter.createImportConverter(NurseRosteringApp.DATA_DIR_NAME,
                new NurseRosteringImporter(), new NurseRosterSolutionFileIO());
        converter.convertAll();
    }

    @Override
    public XmlInputBuilder<NurseRoster> createXmlInputBuilder() {
        return new NurseRosteringInputBuilder();
    }

    public static class NurseRosteringInputBuilder extends XmlInputBuilder<NurseRoster> {

        protected Map<LocalDate, ShiftDate> shiftDateMap;
        protected Map<String, Skill> skillMap;
        protected Map<String, ShiftType> shiftTypeMap;
        protected Map<Pair<LocalDate, String>, Shift> dateAndShiftTypeToShiftMap;
        protected Map<Pair<DayOfWeek, ShiftType>, List<Shift>> dayOfWeekAndShiftTypeToShiftListMap;
        protected Map<String, Pattern> patternMap;
        protected Map<String, Contract> contractMap;
        protected Map<String, Employee> employeeMap;

        @Override
        public NurseRoster readSolution() throws IOException, JDOMException {
            // Note: javax.xml is terrible. JDom is much, much easier.

            var schedulingPeriodElement = document.getRootElement();
            assertElementName(schedulingPeriodElement, "SchedulingPeriod");
            var nurseRoster = new NurseRoster(0L);
            nurseRoster.setCode(schedulingPeriodElement.getAttribute("ID").getValue());

            generateShiftDateList(nurseRoster,
                    schedulingPeriodElement.getChild("StartDate"),
                    schedulingPeriodElement.getChild("EndDate"));
            generateNurseRosterInfo(nurseRoster);
            readSkillList(nurseRoster, schedulingPeriodElement.getChild("Skills"));
            readShiftTypeList(nurseRoster, schedulingPeriodElement.getChild("ShiftTypes"));
            generateShiftList(nurseRoster);
            readPatternList(nurseRoster, schedulingPeriodElement.getChild("Patterns"));
            readContractList(nurseRoster, schedulingPeriodElement.getChild("Contracts"));
            readEmployeeList(nurseRoster, schedulingPeriodElement.getChild("Employees"));
            readRequiredEmployeeSizes(schedulingPeriodElement.getChild("CoverRequirements"));
            readDayOffRequestList(nurseRoster, schedulingPeriodElement.getChild("DayOffRequests"));
            readDayOnRequestList(nurseRoster, schedulingPeriodElement.getChild("DayOnRequests"));
            readShiftOffRequestList(nurseRoster, schedulingPeriodElement.getChild("ShiftOffRequests"));
            readShiftOnRequestList(nurseRoster, schedulingPeriodElement.getChild("ShiftOnRequests"));
            createShiftAssignmentList(nurseRoster);

            var possibleSolutionSize = BigInteger.valueOf(nurseRoster.getEmployeeList().size()).pow(
                    nurseRoster.getShiftAssignmentList().size());
            logger.info("NurseRoster {} has {} skills, {} shiftTypes, {} patterns, {} contracts, {} employees," +
                    " {} shiftDates, {} shiftAssignments and {} requests with a search space of {}.",
                    getInputId(),
                    nurseRoster.getSkillList().size(),
                    nurseRoster.getShiftTypeList().size(),
                    nurseRoster.getPatternList().size(),
                    nurseRoster.getContractList().size(),
                    nurseRoster.getEmployeeList().size(),
                    nurseRoster.getShiftDateList().size(),
                    nurseRoster.getShiftAssignmentList().size(),
                    nurseRoster.getDayOffRequestList().size() + nurseRoster.getDayOnRequestList().size()
                            + nurseRoster.getShiftOffRequestList().size() + nurseRoster.getShiftOnRequestList().size(),
                    getFlooredPossibleSolutionSize(possibleSolutionSize));
            return nurseRoster;
        }

        private void generateShiftDateList(NurseRoster nurseRoster, Element startDateElement, Element endDateElement) {
            LocalDate startDate;
            try {
                startDate = LocalDate.parse(startDateElement.getText(), DateTimeFormatter.ISO_DATE);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid startDate (" + startDateElement.getText() + ").", e);
            }
            LocalDate endDate;
            try {
                endDate = LocalDate.parse(endDateElement.getText(), DateTimeFormatter.ISO_DATE);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid endDate (" + endDateElement.getText() + ").", e);
            }
            if (!startDate.isBefore(endDate)) {
                throw new IllegalStateException("The startDate (" + startDate + " must be before endDate (" + endDate + ").");
            }
            var maxDayIndex = Math.toIntExact(DAYS.between(startDate, endDate));
            var shiftDateSize = maxDayIndex + 1;
            List<ShiftDate> shiftDateList = new ArrayList<>(shiftDateSize);
            shiftDateMap = new LinkedHashMap<>(shiftDateSize);
            var id = 0L;
            var dayIndex = 0;
            var date = startDate;
            for (var i = 0; i < shiftDateSize; i++) {
                var shiftDate = new ShiftDate(id, dayIndex, date);
                shiftDate.setShiftList(new ArrayList<>());
                shiftDateList.add(shiftDate);
                shiftDateMap.put(date, shiftDate);
                id++;
                dayIndex++;
                date = date.plusDays(1);
            }
            nurseRoster.setShiftDateList(shiftDateList);
        }

        private void generateNurseRosterInfo(NurseRoster nurseRoster) {
            var shiftDateList = nurseRoster.getShiftDateList();
            var nurseRosterParametrization = new NurseRosterParametrization(0L,
                    shiftDateList.get(0), shiftDateList.get(shiftDateList.size() - 1), shiftDateList.get(0));
            nurseRoster.setNurseRosterParametrization(nurseRosterParametrization);
        }

        private void readSkillList(NurseRoster nurseRoster, Element skillsElement) {
            List<Skill> skillList;
            if (skillsElement == null) {
                skillList = Collections.emptyList();
            } else {
                var skillElementList = skillsElement.getChildren();
                skillList = new ArrayList<>(skillElementList.size());
                skillMap = new LinkedHashMap<>(skillElementList.size());
                var id = 0L;
                for (var element : skillElementList) {
                    assertElementName(element, "Skill");
                    var skill = new Skill(id, element.getText());
                    skillList.add(skill);
                    if (skillMap.containsKey(skill.getCode())) {
                        throw new IllegalArgumentException("There are 2 skills with the same code ("
                                + skill.getCode() + ").");
                    }
                    skillMap.put(skill.getCode(), skill);
                    id++;
                }
            }
            nurseRoster.setSkillList(skillList);
        }

        private void readShiftTypeList(NurseRoster nurseRoster, Element shiftTypesElement) {
            var shiftTypeElementList = shiftTypesElement.getChildren();
            List<ShiftType> shiftTypeList = new ArrayList<>(shiftTypeElementList.size());
            shiftTypeMap = new LinkedHashMap<>(shiftTypeElementList.size());
            var id = 0L;
            var index = 0;
            List<ShiftTypeSkillRequirement> shiftTypeSkillRequirementList = new ArrayList<>(shiftTypeElementList.size() * 2);
            var shiftTypeSkillRequirementId = 0L;
            for (var element : shiftTypeElementList) {
                assertElementName(element, "Shift");
                var startTimeString = element.getChild("StartTime").getText();
                var endTimeString = element.getChild("EndTime").getText();
                var shiftType = new ShiftType(id, element.getAttribute("ID").getValue(), index,
                        startTimeString, endTimeString, startTimeString.compareTo(endTimeString) > 0,
                        element.getChild("Description").getText());

                var skillsElement = element.getChild("Skills");
                if (skillsElement != null) {
                    var skillElementList = skillsElement.getChildren();
                    for (var skillElement : skillElementList) {
                        assertElementName(skillElement, "Skill");
                        var skill = skillMap.get(skillElement.getText());
                        if (skill == null) {
                            throw new IllegalArgumentException("The skill (" + skillElement.getText()
                                    + ") of shiftType (" + shiftType.getCode() + ") does not exist.");
                        }
                        var shiftTypeSkillRequirement =
                                new ShiftTypeSkillRequirement(shiftTypeSkillRequirementId, shiftType, skill);
                        shiftTypeSkillRequirementList.add(shiftTypeSkillRequirement);
                        shiftTypeSkillRequirementId++;
                    }
                }

                shiftTypeList.add(shiftType);
                if (shiftTypeMap.containsKey(shiftType.getCode())) {
                    throw new IllegalArgumentException("There are 2 shiftTypes with the same code ("
                            + shiftType.getCode() + ").");
                }
                shiftTypeMap.put(shiftType.getCode(), shiftType);
                id++;
                index++;
            }
            nurseRoster.setShiftTypeList(shiftTypeList);
            nurseRoster.setShiftTypeSkillRequirementList(shiftTypeSkillRequirementList);
        }

        private void generateShiftList(NurseRoster nurseRoster) {
            var shiftTypeList = nurseRoster.getShiftTypeList();
            var shiftListSize = shiftDateMap.size() * shiftTypeList.size();
            List<Shift> shiftList = new ArrayList<>(shiftListSize);
            dateAndShiftTypeToShiftMap = new LinkedHashMap<>(shiftListSize);
            dayOfWeekAndShiftTypeToShiftListMap = new LinkedHashMap<>(7 * shiftTypeList.size());
            var id = 0L;
            var index = 0;
            for (var shiftDate : nurseRoster.getShiftDateList()) {
                for (var shiftType : shiftTypeList) {
                    // Required employee size filled in later.
                    var shift = new Shift(id, shiftDate, shiftType, index, 0);
                    shiftDate.getShiftList().add(shift);
                    shiftList.add(shift);
                    var key = shiftDate.getDate();
                    var value = shiftType.getCode();
                    dateAndShiftTypeToShiftMap.put(new Pair<>(key, value), shift);
                    addShiftToDayOfWeekAndShiftTypeToShiftListMap(shiftDate, shiftType, shift);
                    id++;
                    index++;
                }
            }
            nurseRoster.setShiftList(shiftList);
        }

        private void addShiftToDayOfWeekAndShiftTypeToShiftListMap(ShiftDate shiftDate, ShiftType shiftType, Shift shift) {
            var key1 = shiftDate.getDayOfWeek();
            var key = new Pair<DayOfWeek, ShiftType>(key1, shiftType);
            var dayOfWeekAndShiftTypeToShiftList = dayOfWeekAndShiftTypeToShiftListMap.computeIfAbsent(key,
                    k -> new ArrayList<>((shiftDateMap.size() + 6) / 7));
            dayOfWeekAndShiftTypeToShiftList.add(shift);
        }

        private void readPatternList(NurseRoster nurseRoster, Element patternsElement) throws JDOMException {
            List<Pattern> patternList;
            if (patternsElement == null) {
                patternList = Collections.emptyList();
            } else {
                var patternElementList = patternsElement.getChildren();
                patternList = new ArrayList<>(patternElementList.size());
                patternMap = new LinkedHashMap<>(patternElementList.size());
                var id = 0L;
                for (var element : patternElementList) {
                    assertElementName(element, "Pattern");
                    var code = element.getAttribute("ID").getValue();
                    var weight = element.getAttribute("weight").getIntValue();

                    var patternEntryElementList = element.getChild("PatternEntries")
                            .getChildren();
                    if (patternEntryElementList.size() < 2) {
                        throw new IllegalArgumentException("The size of PatternEntries ("
                                + patternEntryElementList.size() + ") of pattern (" + code + ") should be at least 2.");
                    }
                    Pattern pattern;
                    if (patternEntryElementList.get(0).getChild("ShiftType").getText().equals("None")) {
                        pattern = new FreeBefore2DaysWithAWorkDayPattern(id, code);
                        if (patternEntryElementList.size() != 3) {
                            throw new IllegalStateException("boe");
                        }
                    } else if (patternEntryElementList.get(1).getChild("ShiftType").getText().equals("None")) {
                        throw new UnsupportedOperationException("The pattern (" + code + ") is not supported."
                                + " None of the test data exhibits such a pattern.");
                    } else {
                        pattern = switch (patternEntryElementList.size()) {
                            case 2 -> new ShiftType2DaysPattern(id, code);
                            case 3 -> new ShiftType3DaysPattern(id, code);
                            default -> throw new IllegalArgumentException("A size of PatternEntries ("
                                    + patternEntryElementList.size() + ") of pattern (" + code
                                    + ") above 3 is not supported.");
                        };
                    }
                    pattern.setWeight(weight);
                    var patternEntryIndex = 0;
                    DayOfWeek firstDayOfWeek = null;
                    for (var patternEntryElement : patternEntryElementList) {
                        assertElementName(patternEntryElement, "PatternEntry");
                        var shiftTypeElement = patternEntryElement.getChild("ShiftType");
                        boolean shiftTypeIsNone;
                        ShiftType shiftType;
                        if (shiftTypeElement.getText().equals("Any")) {
                            shiftTypeIsNone = false;
                            shiftType = null;
                        } else if (shiftTypeElement.getText().equals("None")) {
                            shiftTypeIsNone = true;
                            shiftType = null;
                        } else {
                            shiftTypeIsNone = false;
                            shiftType = shiftTypeMap.get(shiftTypeElement.getText());
                            if (shiftType == null) {
                                throw new IllegalArgumentException("The shiftType (" + shiftTypeElement.getText()
                                        + ") of pattern (" + pattern.getCode() + ") does not exist.");
                            }
                        }
                        var dayElement = patternEntryElement.getChild("Day");
                        DayOfWeek dayOfWeek;
                        if (dayElement.getText().equals("Any")) {
                            dayOfWeek = null;
                        } else {
                            dayOfWeek = null;
                            for (var possibleDayOfWeek : DayOfWeek.values()) {
                                if (possibleDayOfWeek.name().equalsIgnoreCase(dayElement.getText())) {
                                    dayOfWeek = possibleDayOfWeek;
                                    break;
                                }
                            }
                            if (dayOfWeek == null) {
                                throw new IllegalArgumentException("The dayOfWeek (" + dayElement.getText()
                                        + ") of pattern (" + pattern.getCode() + ") does not exist.");
                            }
                        }
                        if (patternEntryIndex == 0) {
                            firstDayOfWeek = dayOfWeek;
                        } else {
                            if (firstDayOfWeek != null) {
                                var distance = dayOfWeek.getValue() - firstDayOfWeek.getValue();
                                if (distance < 0) {
                                    distance += 7;
                                }
                                if (distance != patternEntryIndex) {
                                    throw new IllegalArgumentException("On patternEntryIndex (" + patternEntryIndex
                                            + ") of pattern (" + pattern.getCode()
                                            + ") the dayOfWeek (" + dayOfWeek
                                            + ") is not valid with previous entries.");
                                }
                            } else {
                                if (dayOfWeek != null) {
                                    throw new IllegalArgumentException("On patternEntryIndex (" + patternEntryIndex
                                            + ") of pattern (" + pattern.getCode()
                                            + ") the dayOfWeek should be (Any), in line with previous entries.");
                                }
                            }
                        }
                        if (pattern instanceof FreeBefore2DaysWithAWorkDayPattern castedPattern) {
                            if (patternEntryIndex == 0) {
                                if (dayOfWeek == null) {
                                    throw new UnsupportedOperationException("On patternEntryIndex (" + patternEntryIndex
                                            + ") of FreeBeforeWorkSequence pattern (" + pattern.getCode()
                                            + ") the dayOfWeek should not be (Any)."
                                            + "\n None of the test data exhibits such a pattern.");
                                }
                                castedPattern.setFreeDayOfWeek(dayOfWeek);
                            }
                            if (patternEntryIndex == 1) {
                                if (shiftType != null) {
                                    throw new UnsupportedOperationException("On patternEntryIndex (" + patternEntryIndex
                                            + ") of FreeBeforeWorkSequence pattern (" + pattern.getCode()
                                            + ") the shiftType should be (Any)."
                                            + "\n None of the test data exhibits such a pattern.");
                                }
                            }
                            if (patternEntryIndex != 0 && shiftTypeIsNone) {
                                throw new IllegalArgumentException("On patternEntryIndex (" + patternEntryIndex
                                        + ") of FreeBeforeWorkSequence pattern (" + pattern.getCode()
                                        + ") the shiftType cannot be (None).");
                            }
                        } else if (pattern instanceof WorkBeforeFreeSequencePattern castedPattern) {
                            if (patternEntryIndex == 0) {
                                castedPattern.setWorkDayOfWeek(dayOfWeek);
                                castedPattern.setWorkShiftType(shiftType);
                                castedPattern.setFreeDayLength(patternEntryElementList.size() - 1);
                            }
                            if (patternEntryIndex != 0 && !shiftTypeIsNone) {
                                throw new IllegalArgumentException("On patternEntryIndex (" + patternEntryIndex
                                        + ") of WorkBeforeFreeSequence pattern (" + pattern.getCode()
                                        + ") the shiftType should be (None).");
                            }
                        } else if (pattern instanceof ShiftType2DaysPattern castedPattern) {
                            if (patternEntryIndex == 0) {
                                if (dayOfWeek != null) {
                                    throw new UnsupportedOperationException("On patternEntryIndex (" + patternEntryIndex
                                            + ") of FreeBeforeWorkSequence pattern (" + pattern.getCode()
                                            + ") the dayOfWeek should be (Any)."
                                            + "\n None of the test data exhibits such a pattern.");
                                }
                            }
                            if (shiftType == null) {
                                throw new UnsupportedOperationException("On patternEntryIndex (" + patternEntryIndex
                                        + ") of FreeBeforeWorkSequence pattern (" + pattern.getCode()
                                        + ") the shiftType should not be (Any)."
                                        + "\n None of the test data exhibits such a pattern.");
                            }
                            switch (patternEntryIndex) {
                                case 0:
                                    castedPattern.setDayIndex0ShiftType(shiftType);
                                    break;
                                case 1:
                                    castedPattern.setDayIndex1ShiftType(shiftType);
                                    break;
                                default:
                                    throw new IllegalArgumentException("The patternEntryIndex ("
                                            + patternEntryIndex + ") is not supported.");
                            }
                        } else if (pattern instanceof ShiftType3DaysPattern castedPattern) {
                            if (patternEntryIndex == 0) {
                                if (dayOfWeek != null) {
                                    throw new UnsupportedOperationException("On patternEntryIndex (" + patternEntryIndex
                                            + ") of FreeBeforeWorkSequence pattern (" + pattern.getCode()
                                            + ") the dayOfWeek should be (Any)."
                                            + "\n None of the test data exhibits such a pattern.");
                                }
                            }
                            if (shiftType == null) {
                                throw new UnsupportedOperationException("On patternEntryIndex (" + patternEntryIndex
                                        + ") of FreeBeforeWorkSequence pattern (" + pattern.getCode()
                                        + ") the shiftType should not be (Any)."
                                        + "\n None of the test data exhibits such a pattern.");
                            }
                            switch (patternEntryIndex) {
                                case 0:
                                    castedPattern.setDayIndex0ShiftType(shiftType);
                                    break;
                                case 1:
                                    castedPattern.setDayIndex1ShiftType(shiftType);
                                    break;
                                case 2:
                                    castedPattern.setDayIndex2ShiftType(shiftType);
                                    break;
                                default:
                                    throw new IllegalArgumentException("The patternEntryIndex ("
                                            + patternEntryIndex + ") is not supported.");
                            }
                        } else {
                            throw new IllegalStateException("Unsupported patternClass (" + pattern.getClass() + ").");
                        }
                        patternEntryIndex++;
                    }
                    patternList.add(pattern);
                    if (patternMap.containsKey(pattern.getCode())) {
                        throw new IllegalArgumentException("There are 2 patterns with the same code ("
                                + pattern.getCode() + ").");
                    }
                    patternMap.put(pattern.getCode(), pattern);
                    id++;
                }
            }
            nurseRoster.setPatternList(patternList);
        }

        private void readContractList(NurseRoster nurseRoster, Element contractsElement) throws JDOMException {
            var contractLineTypeListSize = ContractLineType.values().length;
            var contractElementList = contractsElement.getChildren();
            List<Contract> contractList = new ArrayList<>(contractElementList.size());
            contractMap = new LinkedHashMap<>(contractElementList.size());
            var id = 0L;
            List<ContractLine> contractLineList = new ArrayList<>(
                    contractElementList.size() * contractLineTypeListSize);
            var contractLineId = 0L;
            List<PatternContractLine> patternContractLineList = new ArrayList<>(
                    contractElementList.size() * 3);
            var patternContractLineId = 0L;
            for (var element : contractElementList) {
                assertElementName(element, "Contract");
                var contract = new Contract(id, element.getAttribute("ID").getValue(),
                        element.getChild("Description").getText());

                List<ContractLine> contractLineListOfContract = new ArrayList<>(contractLineTypeListSize);
                contractLineId = readBooleanContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("SingleAssignmentPerDay"),
                        ContractLineType.SINGLE_ASSIGNMENT_PER_DAY);
                contractLineId = readMinMaxContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("MinNumAssignments"),
                        element.getChild("MaxNumAssignments"),
                        ContractLineType.TOTAL_ASSIGNMENTS);
                contractLineId = readMinMaxContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("MinConsecutiveWorkingDays"),
                        element.getChild("MaxConsecutiveWorkingDays"),
                        ContractLineType.CONSECUTIVE_WORKING_DAYS);
                contractLineId = readMinMaxContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("MinConsecutiveFreeDays"),
                        element.getChild("MaxConsecutiveFreeDays"),
                        ContractLineType.CONSECUTIVE_FREE_DAYS);
                contractLineId = readMinMaxContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("MinConsecutiveWorkingWeekends"),
                        element.getChild("MaxConsecutiveWorkingWeekends"),
                        ContractLineType.CONSECUTIVE_WORKING_WEEKENDS);
                contractLineId = readMinMaxContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, null,
                        element.getChild("MaxWorkingWeekendsInFourWeeks"),
                        ContractLineType.TOTAL_WORKING_WEEKENDS_IN_FOUR_WEEKS);
                var weekendDefinition = WeekendDefinition.valueOfCode(
                        element.getChild("WeekendDefinition").getText());
                contract.setWeekendDefinition(weekendDefinition);
                contractLineId = readBooleanContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("CompleteWeekends"),
                        ContractLineType.COMPLETE_WEEKENDS);
                contractLineId = readBooleanContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("IdenticalShiftTypesDuringWeekend"),
                        ContractLineType.IDENTICAL_SHIFT_TYPES_DURING_WEEKEND);
                contractLineId = readBooleanContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("NoNightShiftBeforeFreeWeekend"),
                        ContractLineType.NO_NIGHT_SHIFT_BEFORE_FREE_WEEKEND);
                contractLineId = readBooleanContractLine(contract, contractLineList, contractLineListOfContract,
                        contractLineId, element.getChild("AlternativeSkillCategory"),
                        ContractLineType.ALTERNATIVE_SKILL_CATEGORY);
                contract.setContractLineList(contractLineListOfContract);

                var unwantedPatternElementList = element.getChild("UnwantedPatterns")
                        .getChildren();
                for (var patternElement : unwantedPatternElementList) {
                    assertElementName(patternElement, "Pattern");
                    var pattern = patternMap.get(patternElement.getText());
                    if (pattern == null) {
                        throw new IllegalArgumentException("The pattern (" + patternElement.getText()
                                + ") of contract (" + contract.getCode() + ") does not exist.");
                    }
                    var patternContractLine = new PatternContractLine(patternContractLineId, contract, pattern);
                    patternContractLineList.add(patternContractLine);
                    patternContractLineId++;
                }

                contractList.add(contract);
                if (contractMap.containsKey(contract.getCode())) {
                    throw new IllegalArgumentException("There are 2 contracts with the same code ("
                            + contract.getCode() + ").");
                }
                contractMap.put(contract.getCode(), contract);
                id++;
            }
            nurseRoster.setContractList(contractList);
            nurseRoster.setContractLineList(contractLineList);
            nurseRoster.setPatternContractLineList(patternContractLineList);
        }

        private long readBooleanContractLine(Contract contract, List<ContractLine> contractLineList,
                List<ContractLine> contractLineListOfContract, long contractLineId, Element element,
                ContractLineType contractLineType) throws DataConversionException {
            var enabled = Boolean.parseBoolean(element.getText());
            int weight;
            if (enabled) {
                weight = element.getAttribute("weight").getIntValue();
                if (weight < 0) {
                    throw new IllegalArgumentException("The weight (" + weight
                            + ") of contract (" + contract.getCode() + ") and contractLineType (" + contractLineType
                            + ") should be 0 or at least 1.");
                } else if (weight == 0) {
                    // If the weight is zero, the constraint should not be considered.
                    enabled = false;
                    logger.warn("In contract ({}), the contractLineType ({}) is enabled with weight 0.",
                            contract.getCode(), contractLineType);
                }
            } else {
                weight = 0;
            }
            if (enabled) {
                var contractLine =
                        new BooleanContractLine(contractLineId, contract, contractLineType, enabled, weight);
                contractLineList.add(contractLine);
                contractLineListOfContract.add(contractLine);
                contractLineId++;
            }
            return contractLineId;
        }

        private long readMinMaxContractLine(Contract contract, List<ContractLine> contractLineList,
                List<ContractLine> contractLineListOfContract, long contractLineId,
                Element minElement, Element maxElement,
                ContractLineType contractLineType) throws DataConversionException {
            var minimumEnabled = minElement != null && minElement.getAttribute("on").getBooleanValue();
            int minimumWeight;
            if (minimumEnabled) {
                minimumWeight = minElement.getAttribute("weight").getIntValue();
                if (minimumWeight < 0) {
                    throw new IllegalArgumentException("The minimumWeight (" + minimumWeight
                            + ") of contract (" + contract.getCode() + ") and contractLineType (" + contractLineType
                            + ") should be 0 or at least 1.");
                } else if (minimumWeight == 0) {
                    // If the weight is zero, the constraint should not be considered.
                    minimumEnabled = false;
                    logger.warn("In contract ({}), the contractLineType ({}) minimum is enabled with weight 0.",
                            contract.getCode(), contractLineType);
                }
            } else {
                minimumWeight = 0;
            }
            var maximumEnabled = maxElement != null && maxElement.getAttribute("on").getBooleanValue();
            int maximumWeight;
            if (maximumEnabled) {
                maximumWeight = maxElement.getAttribute("weight").getIntValue();
                if (maximumWeight < 0) {
                    throw new IllegalArgumentException("The maximumWeight (" + maximumWeight
                            + ") of contract (" + contract.getCode() + ") and contractLineType (" + contractLineType
                            + ") should be 0 or at least 1.");
                } else if (maximumWeight == 0) {
                    // If the weight is zero, the constraint should not be considered.
                    maximumEnabled = false;
                    logger.warn("In contract ({}), the contractLineType ({}) maximum is enabled with weight 0.",
                            contract.getCode(), contractLineType);
                }
            } else {
                maximumWeight = 0;
            }
            if (minimumEnabled || maximumEnabled) {
                var contractLine =
                        new MinMaxContractLine(contractLineId, contract, contractLineType, minimumEnabled, maximumEnabled);
                if (minimumEnabled) {
                    var minimumValue = Integer.parseInt(minElement.getText());
                    if (minimumValue < 1) {
                        throw new IllegalArgumentException("The minimumValue (" + minimumValue
                                + ") of contract (" + contract.getCode() + ") and contractLineType ("
                                + contractLineType + ") should be at least 1.");
                    }
                    contractLine.setMinimumValue(minimumValue);
                    contractLine.setMinimumWeight(minimumWeight);
                }
                if (maximumEnabled) {
                    var maximumValue = Integer.parseInt(maxElement.getText());
                    if (maximumValue < 0) {
                        throw new IllegalArgumentException("The maximumValue (" + maximumValue
                                + ") of contract (" + contract.getCode() + ") and contractLineType ("
                                + contractLineType + ") should be at least 0.");
                    }
                    contractLine.setMaximumValue(maximumValue);
                    contractLine.setMaximumWeight(maximumWeight);
                }
                contractLineList.add(contractLine);
                contractLineListOfContract.add(contractLine);
                contractLineId++;
            }
            return contractLineId;
        }

        private void readEmployeeList(NurseRoster nurseRoster, Element employeesElement) {
            var employeeElementList = employeesElement.getChildren();
            List<Employee> employeeList = new ArrayList<>(employeeElementList.size());
            employeeMap = new LinkedHashMap<>(employeeElementList.size());
            var id = 0L;
            List<SkillProficiency> skillProficiencyList = new ArrayList<>(employeeElementList.size() * 2);
            var skillProficiencyId = 0L;
            for (var element : employeeElementList) {
                assertElementName(element, "Employee");
                var code = element.getAttribute("ID").getValue();
                var contractElement = element.getChild("ContractID");
                var contract = contractMap.get(contractElement.getText());
                if (contract == null) {
                    throw new IllegalArgumentException("The contract (" + contractElement.getText()
                            + ") of employee (" + code + ") does not exist.");
                }
                var employee = new Employee(id, code, element.getChild("Name").getText(), contract);
                var estimatedRequestSize = (shiftDateMap.size() / employeeElementList.size()) + 1;
                employee.setDayOffRequestMap(new LinkedHashMap<>(estimatedRequestSize));
                employee.setDayOnRequestMap(new LinkedHashMap<>(estimatedRequestSize));
                employee.setShiftOffRequestMap(new LinkedHashMap<>(estimatedRequestSize));
                employee.setShiftOnRequestMap(new LinkedHashMap<>(estimatedRequestSize));

                var skillsElement = element.getChild("Skills");
                if (skillsElement != null) {
                    var skillElementList = skillsElement.getChildren();
                    for (var skillElement : skillElementList) {
                        assertElementName(skillElement, "Skill");
                        var skill = skillMap.get(skillElement.getText());
                        if (skill == null) {
                            throw new IllegalArgumentException("The skill (" + skillElement.getText()
                                    + ") of employee (" + employee.getCode() + ") does not exist.");
                        }
                        var skillProficiency = new SkillProficiency(skillProficiencyId, employee, skill);
                        skillProficiencyList.add(skillProficiency);
                        skillProficiencyId++;
                    }
                }

                employeeList.add(employee);
                if (employeeMap.containsKey(employee.getCode())) {
                    throw new IllegalArgumentException("There are 2 employees with the same code ("
                            + employee.getCode() + ").");
                }
                employeeMap.put(employee.getCode(), employee);
                id++;
            }
            nurseRoster.setEmployeeList(employeeList);
            nurseRoster.setSkillProficiencyList(skillProficiencyList);
        }

        private void readRequiredEmployeeSizes(Element coverRequirementsElement) {
            var coverRequirementElementList = coverRequirementsElement.getChildren();
            for (var element : coverRequirementElementList) {
                if (element.getName().equals("DayOfWeekCover")) {
                    var dayOfWeekElement = element.getChild("Day");
                    DayOfWeek dayOfWeek = null;
                    for (var possibleDayOfWeek : DayOfWeek.values()) {
                        if (possibleDayOfWeek.name().equalsIgnoreCase(dayOfWeekElement.getText())) {
                            dayOfWeek = possibleDayOfWeek;
                            break;
                        }
                    }
                    if (dayOfWeek == null) {
                        throw new IllegalArgumentException("The dayOfWeek (" + dayOfWeekElement.getText()
                                + ") of an entity DayOfWeekCover does not exist.");
                    }

                    var coverElementList = element.getChildren("Cover");
                    for (var coverElement : coverElementList) {
                        var shiftTypeElement = coverElement.getChild("Shift");
                        var shiftType = shiftTypeMap.get(shiftTypeElement.getText());
                        if (shiftType == null) {
                            if (shiftTypeElement.getText().equals("Any")) {
                                throw new IllegalStateException("The shiftType Any is not supported on DayOfWeekCover.");
                            } else if (shiftTypeElement.getText().equals("None")) {
                                throw new IllegalStateException("The shiftType None is not supported on DayOfWeekCover.");
                            } else {
                                throw new IllegalArgumentException("The shiftType (" + shiftTypeElement.getText()
                                        + ") of an entity DayOfWeekCover does not exist.");
                            }
                        }
                        var key = new Pair<DayOfWeek, ShiftType>(dayOfWeek, shiftType);
                        var shiftList = dayOfWeekAndShiftTypeToShiftListMap.get(key);
                        if (shiftList == null) {
                            throw new IllegalArgumentException("The dayOfWeek (" + dayOfWeekElement.getText()
                                    + ") with the shiftType (" + shiftTypeElement.getText()
                                    + ") of an entity DayOfWeekCover does not have any shifts.");
                        }
                        var requiredEmployeeSize = Integer.parseInt(coverElement.getChild("Preferred").getText());
                        for (var shift : shiftList) {
                            shift.setRequiredEmployeeSize(shift.getRequiredEmployeeSize() + requiredEmployeeSize);
                        }
                    }
                } else if (element.getName().equals("DateSpecificCover")) {
                    var dateElement = element.getChild("Date");
                    var coverElementList = element.getChildren("Cover");
                    for (var coverElement : coverElementList) {
                        var shiftTypeElement = coverElement.getChild("Shift");
                        var date = LocalDate.parse(dateElement.getText(), DateTimeFormatter.ISO_DATE);
                        var value = shiftTypeElement.getText();
                        var shift = dateAndShiftTypeToShiftMap.get(new Pair<>(date, value));
                        if (shift == null) {
                            throw new IllegalArgumentException("The date (" + dateElement.getText()
                                    + ") with the shiftType (" + shiftTypeElement.getText()
                                    + ") of an entity DateSpecificCover does not have a shift.");
                        }
                        var requiredEmployeeSize = Integer.parseInt(coverElement.getChild("Preferred").getText());
                        shift.setRequiredEmployeeSize(shift.getRequiredEmployeeSize() + requiredEmployeeSize);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown cover entity (" + element.getName() + ").");
                }
            }
        }

        private void readDayOffRequestList(NurseRoster nurseRoster, Element dayOffRequestsElement) throws JDOMException {
            List<DayOffRequest> dayOffRequestList;
            if (dayOffRequestsElement == null) {
                dayOffRequestList = Collections.emptyList();
            } else {
                var dayOffElementList = dayOffRequestsElement.getChildren();
                dayOffRequestList = new ArrayList<>(dayOffElementList.size());
                var id = 0L;
                for (var element : dayOffElementList) {
                    assertElementName(element, "DayOff");

                    var employeeElement = element.getChild("EmployeeID");
                    var employee = employeeMap.get(employeeElement.getText());
                    if (employee == null) {
                        throw new IllegalArgumentException("The shiftDate (" + employeeElement.getText()
                                + ") of dayOffRequest (" + id + ") does not exist.");
                    }

                    var dateElement = element.getChild("Date");
                    var shiftDate = shiftDateMap.get(LocalDate.parse(dateElement.getText(), DateTimeFormatter.ISO_DATE));
                    if (shiftDate == null) {
                        throw new IllegalArgumentException("The date (" + dateElement.getText()
                                + ") of dayOffRequest (" + id + ") does not exist.");
                    }

                    var dayOffRequest =
                            new DayOffRequest(id, employee, shiftDate, element.getAttribute("weight").getIntValue());
                    dayOffRequestList.add(dayOffRequest);
                    employee.getDayOffRequestMap().put(shiftDate, dayOffRequest);
                    id++;
                }
            }
            nurseRoster.setDayOffRequestList(dayOffRequestList);
        }

        private void readDayOnRequestList(NurseRoster nurseRoster, Element dayOnRequestsElement) throws JDOMException {
            List<DayOnRequest> dayOnRequestList;
            if (dayOnRequestsElement == null) {
                dayOnRequestList = Collections.emptyList();
            } else {
                var dayOnElementList = dayOnRequestsElement.getChildren();
                dayOnRequestList = new ArrayList<>(dayOnElementList.size());
                var id = 0L;
                for (var element : dayOnElementList) {
                    assertElementName(element, "DayOn");

                    var employeeElement = element.getChild("EmployeeID");
                    var employee = employeeMap.get(employeeElement.getText());
                    if (employee == null) {
                        throw new IllegalArgumentException("The shiftDate (" + employeeElement.getText()
                                + ") of dayOnRequest (" + id + ") does not exist.");
                    }

                    var dateElement = element.getChild("Date");
                    var shiftDate = shiftDateMap.get(LocalDate.parse(dateElement.getText(), DateTimeFormatter.ISO_DATE));
                    if (shiftDate == null) {
                        throw new IllegalArgumentException("The date (" + dateElement.getText()
                                + ") of dayOnRequest (" + id + ") does not exist.");
                    }

                    var dayOnRequest =
                            new DayOnRequest(id, employee, shiftDate, element.getAttribute("weight").getIntValue());
                    dayOnRequestList.add(dayOnRequest);
                    employee.getDayOnRequestMap().put(shiftDate, dayOnRequest);
                    id++;
                }
            }
            nurseRoster.setDayOnRequestList(dayOnRequestList);
        }

        private void readShiftOffRequestList(NurseRoster nurseRoster, Element shiftOffRequestsElement) throws JDOMException {
            List<ShiftOffRequest> shiftOffRequestList;
            if (shiftOffRequestsElement == null) {
                shiftOffRequestList = Collections.emptyList();
            } else {
                var shiftOffElementList = shiftOffRequestsElement.getChildren();
                shiftOffRequestList = new ArrayList<>(shiftOffElementList.size());
                var id = 0L;
                for (var element : shiftOffElementList) {
                    assertElementName(element, "ShiftOff");

                    var employeeElement = element.getChild("EmployeeID");
                    var employee = employeeMap.get(employeeElement.getText());
                    if (employee == null) {
                        throw new IllegalArgumentException("The shift (" + employeeElement.getText()
                                + ") of shiftOffRequest (" + id + ") does not exist.");
                    }

                    var dateElement = element.getChild("Date");
                    var shiftTypeElement = element.getChild("ShiftTypeID");
                    var date = LocalDate.parse(dateElement.getText(), DateTimeFormatter.ISO_DATE);
                    var value = shiftTypeElement.getText();
                    var shift = dateAndShiftTypeToShiftMap.get(new Pair<>(date, value));
                    if (shift == null) {
                        throw new IllegalArgumentException("The date (" + dateElement.getText()
                                + ") or the shiftType (" + shiftTypeElement.getText()
                                + ") of shiftOffRequest (" + id + ") does not exist.");
                    }

                    var shiftOffRequest =
                            new ShiftOffRequest(id, employee, shift, element.getAttribute("weight").getIntValue());
                    shiftOffRequestList.add(shiftOffRequest);
                    employee.getShiftOffRequestMap().put(shift, shiftOffRequest);
                    id++;
                }
            }
            nurseRoster.setShiftOffRequestList(shiftOffRequestList);
        }

        private void readShiftOnRequestList(NurseRoster nurseRoster, Element shiftOnRequestsElement) throws JDOMException {
            List<ShiftOnRequest> shiftOnRequestList;
            if (shiftOnRequestsElement == null) {
                shiftOnRequestList = Collections.emptyList();
            } else {
                var shiftOnElementList = shiftOnRequestsElement.getChildren();
                shiftOnRequestList = new ArrayList<>(shiftOnElementList.size());
                var id = 0L;
                for (var element : shiftOnElementList) {
                    assertElementName(element, "ShiftOn");

                    var employeeElement = element.getChild("EmployeeID");
                    var employee = employeeMap.get(employeeElement.getText());
                    if (employee == null) {
                        throw new IllegalArgumentException("The shift (" + employeeElement.getText()
                                + ") of shiftOnRequest (" + id + ") does not exist.");
                    }

                    var dateElement = element.getChild("Date");
                    var shiftTypeElement = element.getChild("ShiftTypeID");
                    var date = LocalDate.parse(dateElement.getText(), DateTimeFormatter.ISO_DATE);
                    var value = shiftTypeElement.getText();
                    var shift = dateAndShiftTypeToShiftMap.get(new Pair<>(date, value));
                    if (shift == null) {
                        throw new IllegalArgumentException("The date (" + dateElement.getText()
                                + ") or the shiftType (" + shiftTypeElement.getText()
                                + ") of shiftOnRequest (" + id + ") does not exist.");
                    }

                    var shiftOnRequest =
                            new ShiftOnRequest(id, employee, shift, element.getAttribute("weight").getIntValue());
                    shiftOnRequestList.add(shiftOnRequest);
                    employee.getShiftOnRequestMap().put(shift, shiftOnRequest);
                    id++;
                }
            }
            nurseRoster.setShiftOnRequestList(shiftOnRequestList);
        }

        private void createShiftAssignmentList(NurseRoster nurseRoster) {
            var shiftList = nurseRoster.getShiftList();
            var shiftAssignmentList = new ArrayList<ShiftAssignment>(shiftList.size());
            var id = 0L;
            for (var shift : shiftList) {
                for (var i = 0; i < shift.getRequiredEmployeeSize(); i++) {
                    var shiftAssignment = new ShiftAssignment(id, shift, i);
                    var isPinned = !nurseRoster.getNurseRosterParametrization()
                            .isInPlanningWindow(shiftAssignment.getShiftDate());
                    shiftAssignment.setPinned(isPinned);
                    id++;
                    // Notice that we leave the PlanningVariable properties on null
                    shiftAssignmentList.add(shiftAssignment);
                }
            }
            nurseRoster.setShiftAssignmentList(shiftAssignmentList);
        }

    }

    private record Pair<A, B>(A key, B value) {

    }

}
