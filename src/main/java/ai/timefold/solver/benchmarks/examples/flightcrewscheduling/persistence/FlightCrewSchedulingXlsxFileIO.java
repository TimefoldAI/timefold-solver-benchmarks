package ai.timefold.solver.benchmarks.examples.flightcrewscheduling.persistence;

import static ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightCrewParametrization.EMPLOYEE_UNAVAILABILITY;
import static ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightCrewParametrization.FLIGHT_CONFLICT;
import static ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightCrewParametrization.LOAD_BALANCE_FLIGHT_DURATION_TOTAL_PER_EMPLOYEE;
import static ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightCrewParametrization.REQUIRED_SKILL;
import static ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightCrewParametrization.TRANSFER_BETWEEN_TWO_FLIGHTS;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractXlsxSolutionFileIO;
import ai.timefold.solver.benchmarks.examples.flightcrewscheduling.app.FlightCrewSchedulingApp;
import ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.Airport;
import ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.Employee;
import ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.Flight;
import ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightAssignment;
import ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightCrewParametrization;
import ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightCrewSolution;
import ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.Skill;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FlightCrewSchedulingXlsxFileIO extends
        AbstractXlsxSolutionFileIO<ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightCrewSolution> {

    public static final DateTimeFormatter MILITARY_TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm", Locale.ENGLISH);

    @Override
    public ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightCrewSolution read(File inputSolutionFile) {
        try (InputStream in = new BufferedInputStream(new FileInputStream(inputSolutionFile))) {
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            return new FlightCrewSchedulingXlsxReader(workbook).read();
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Failed reading inputSolutionFile ("
                    + inputSolutionFile + ").", e);
        }
    }

    private static class FlightCrewSchedulingXlsxReader extends
            AbstractXlsxReader<ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightCrewSolution, HardSoftLongScore> {

        private Map<String, Skill> skillMap;
        private Map<String, Employee> nameToEmployeeMap;
        private Map<String, Airport> airportMap;

        public FlightCrewSchedulingXlsxReader(XSSFWorkbook workbook) {
            super(workbook, FlightCrewSchedulingApp.SOLVER_CONFIG);
        }

        @Override
        public ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightCrewSolution read() {
            solution = new FlightCrewSolution();
            readConfiguration();
            readSkillList();
            readAirportList();
            readTaxiTimeMaps();
            readEmployeeList();
            readFlightListAndFlightAssignmentList();
            return solution;
        }

        private void readConfiguration() {
            nextSheet("Configuration");
            nextRow(false);
            readHeaderCell("Schedule start UTC Date");
            solution.setScheduleFirstUTCDate(LocalDate.parse(nextStringCell().getStringCellValue(), DAY_FORMATTER));
            nextRow(false);
            readHeaderCell("Schedule end UTC Date");
            solution.setScheduleLastUTCDate(LocalDate.parse(nextStringCell().getStringCellValue(), DAY_FORMATTER));
            nextRow(false);
            nextRow(false);
            readHeaderCell("Constraint");
            readHeaderCell("Weight");
            readHeaderCell("Description");
            FlightCrewParametrization parametrization = new FlightCrewParametrization(0L);
            readLongConstraintParameterLine(LOAD_BALANCE_FLIGHT_DURATION_TOTAL_PER_EMPLOYEE,
                    parametrization::setLoadBalanceFlightDurationTotalPerEmployee,
                    "Soft penalty per 0.001 minute difference with the average flight duration total per employee.");
            readIntConstraintParameterLine(REQUIRED_SKILL, null,
                    "Hard penalty per missing required skill for a flight assignment");
            readIntConstraintParameterLine(FLIGHT_CONFLICT, null,
                    "Hard penalty per 2 flights of an employee that directly overlap");
            readIntConstraintParameterLine(TRANSFER_BETWEEN_TWO_FLIGHTS, null,
                    "Hard penalty per 2 sequential flights of an employee with no viable transfer from the arrival airport to the departure airport");
            readIntConstraintParameterLine(EMPLOYEE_UNAVAILABILITY, null,
                    "Hard penalty per flight assignment to an employee that is unavailable");
            solution.setParametrization(parametrization);
        }

        private void readSkillList() {
            nextSheet("Skills");
            nextRow(false);
            readHeaderCell("Name");
            List<Skill> skillList = new ArrayList<>(currentSheet.getLastRowNum() - 1);
            skillMap = new HashMap<>(currentSheet.getLastRowNum() - 1);
            long id = 0L;
            while (nextRow()) {
                Skill skill = new Skill(id++, nextStringCell().getStringCellValue());
                skillMap.put(skill.getName(), skill);
                skillList.add(skill);
            }
            solution.setSkillList(skillList);
        }

        private void readAirportList() {
            nextSheet("Airports");
            nextRow(false);
            readHeaderCell("Code");
            readHeaderCell("Name");
            readHeaderCell("Latitude");
            readHeaderCell("Longitude");
            List<Airport> airportList = new ArrayList<>(currentSheet.getLastRowNum() - 1);
            airportMap = new HashMap<>(currentSheet.getLastRowNum() - 1);
            long id = 0L;
            while (nextRow()) {
                Airport airport =
                        new Airport(id++, nextStringCell().getStringCellValue(), nextStringCell().getStringCellValue(),
                                nextNumericCell().getNumericCellValue(), nextNumericCell().getNumericCellValue());
                airportMap.put(airport.getCode(), airport);
                airportList.add(airport);
            }
            solution.setAirportList(airportList);
        }

        private void readTaxiTimeMaps() {
            nextSheet("Taxi time");
            nextRow();
            readHeaderCell(
                    "Driving time in minutes by taxi between two nearby airports to allow employees to start from a different airport.");
            List<Airport> airportList = solution.getAirportList();
            nextRow();
            readHeaderCell("Airport code");
            for (Airport airport : airportList) {
                readHeaderCell(airport.getCode());
            }
            for (Airport a : airportList) {
                a.setTaxiTimeInMinutesMap(new LinkedHashMap<>(airportList.size()));
                nextRow();
                readHeaderCell(a.getCode());
                for (Airport b : airportList) {
                    XSSFCell taxiTimeCell = nextNumericCellOrBlank();
                    if (taxiTimeCell != null) {
                        a.getTaxiTimeInMinutesMap().put(b, (long) taxiTimeCell.getNumericCellValue());
                    }
                }
            }
        }

        private void readEmployeeList() {
            nextSheet("Employees");
            nextRow(false);
            readHeaderCell("");
            readHeaderCell("");
            readHeaderCell("");
            readHeaderCell("Unavailability");
            nextRow(false);
            readHeaderCell("Name");
            readHeaderCell("Home airport");
            readHeaderCell("Skills");
            LocalDate firstDate = solution.getScheduleFirstUTCDate();
            LocalDate lastDate = solution.getScheduleLastUTCDate();
            for (LocalDate date = firstDate; date.compareTo(lastDate) <= 0; date = date.plusDays(1)) {
                readHeaderCell(DAY_FORMATTER.format(date));
            }
            List<Employee> employeeList = new ArrayList<>(currentSheet.getLastRowNum() - 2);
            nameToEmployeeMap = new HashMap<>(currentSheet.getLastRowNum() - 2);
            long id = 0L;
            while (nextRow()) {
                String name = nextStringCell().getStringCellValue();
                if (!VALID_NAME_PATTERN.matcher(name).matches()) {
                    throw new IllegalStateException(currentPosition() + ": The employee name (" + name
                            + ") must match to the regular expression (" + VALID_NAME_PATTERN + ").");
                }
                String homeAirportCode = nextStringCell().getStringCellValue();
                Airport homeAirport = airportMap.get(homeAirportCode);
                Employee employee = new Employee(id++, name, homeAirport);
                if (homeAirport == null) {
                    throw new IllegalStateException(currentPosition()
                            + ": The employee (" + employee.getName()
                            + ")'s homeAirport (" + homeAirportCode
                            + ") does not exist in the airports (" + airportMap.keySet()
                            + ") of the other sheet (Airports).");
                }
                employee.setHomeAirport(homeAirport);
                String[] skillNames = nextStringCell().getStringCellValue().split(", ");
                Set<Skill> skillSet = new LinkedHashSet<>(skillNames.length);
                for (String skillName : skillNames) {
                    ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.Skill skill = skillMap.get(skillName);
                    if (skill == null) {
                        throw new IllegalStateException(currentPosition()
                                + ": The employee (" + employee + ")'s skill (" + skillName
                                + ") does not exist in the skills (" + skillMap.keySet()
                                + ") of the other sheet (Skills).");
                    }
                    skillSet.add(skill);
                }
                employee.setSkillSet(skillSet);
                Set<LocalDate> unavailableDaySet = new LinkedHashSet<>();
                for (LocalDate date = firstDate; date.compareTo(lastDate) <= 0; date = date.plusDays(1)) {
                    XSSFCell cell = nextStringCell();
                    if (Objects.equals(extractColor(cell, UNAVAILABLE_COLOR), UNAVAILABLE_COLOR)) {
                        unavailableDaySet.add(date);
                    }
                    if (!cell.getStringCellValue().isEmpty()) {
                        throw new IllegalStateException(currentPosition() + ": The cell (" + cell.getStringCellValue()
                                + ") should be empty.");
                    }
                }
                employee.setUnavailableDaySet(unavailableDaySet);
                employee.setFlightAssignmentSet(new TreeSet<>());
                nameToEmployeeMap.put(employee.getName(), employee);
                employeeList.add(employee);
            }
            solution.setEmployeeList(employeeList);
        }

        private void readFlightListAndFlightAssignmentList() {
            nextSheet("Flights");
            nextRow(false);
            readHeaderCell("Flight number");
            readHeaderCell("Departure airport code");
            readHeaderCell("Departure UTC date time");
            readHeaderCell("Arrival airport code");
            readHeaderCell("Arrival UTC date time");
            readHeaderCell("Employee skill requirements");
            readHeaderCell("Employee assignments");
            List<Flight> flightList = new ArrayList<>(currentSheet.getLastRowNum() - 1);
            List<ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightAssignment> flightAssignmentList =
                    new ArrayList<>((currentSheet.getLastRowNum() - 1) * 5);
            long id = 0L;
            long flightAssignmentId = 0L;
            while (nextRow()) {
                String flightNumber = nextStringCell().getStringCellValue();
                String departureAirportCode = nextStringCell().getStringCellValue();
                Airport departureAirport = airportMap.get(departureAirportCode);
                if (departureAirport == null) {
                    throw new IllegalStateException(currentPosition()
                            + ": The flight (" + flightNumber + ")'s departureAirport (" + departureAirportCode
                            + ") does not exist in the airports (" + airportMap.keySet()
                            + ") of the other sheet (Airports).");
                }
                LocalDateTime departureDateTime =
                        LocalDateTime.parse(nextStringCell().getStringCellValue(), DATE_TIME_FORMATTER);
                String arrivalAirportCode = nextStringCell().getStringCellValue();
                Airport arrivalAirport = airportMap.get(arrivalAirportCode);
                if (arrivalAirport == null) {
                    throw new IllegalStateException(currentPosition()
                            + ": The flight (" + flightNumber + ")'s arrivalAirport (" + arrivalAirportCode
                            + ") does not exist in the airports (" + airportMap.keySet()
                            + ") of the other sheet (Airports).");
                }
                LocalDateTime arrivalDateTime = LocalDateTime.parse(nextStringCell().getStringCellValue(), DATE_TIME_FORMATTER);
                Flight flight =
                        new Flight(id++, flightNumber, departureAirport, departureDateTime, arrivalAirport, arrivalDateTime);

                String[] skillNames = nextStringCell().getStringCellValue().split(", ");
                String[] employeeNames = nextStringCell().getStringCellValue().split(", ");
                for (int i = 0; i < skillNames.length; i++) {
                    ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.Skill requiredSkill =
                            skillMap.get(skillNames[i]);
                    if (requiredSkill == null) {
                        throw new IllegalStateException(currentPosition()
                                + ": The flight (" + flight.getFlightNumber()
                                + ")'s requiredSkill (" + requiredSkill
                                + ") does not exist in the skills (" + skillMap.keySet()
                                + ") of the other sheet (Skills).");
                    }
                    ai.timefold.solver.benchmarks.examples.flightcrewscheduling.domain.FlightAssignment flightAssignment =
                            new FlightAssignment(flightAssignmentId, flight, i, requiredSkill);
                    if (employeeNames.length > i && !employeeNames[i].isEmpty()) {
                        Employee employee = nameToEmployeeMap.get(employeeNames[i]);
                        if (employee == null) {
                            throw new IllegalStateException(currentPosition()
                                    + ": The flight (" + flight.getFlightNumber()
                                    + ")'s employeeAssignment's name (" + employeeNames[i]
                                    + ") does not exist in the employees (" + nameToEmployeeMap.keySet()
                                    + ") of the other sheet (Employees).");
                        }
                        flightAssignment.setEmployee(employee);
                    }
                    flightAssignmentList.add(flightAssignment);
                    flightAssignmentId++;
                }
                flightList.add(flight);
            }
            solution.setFlightList(flightList);
            solution.setFlightAssignmentList(flightAssignmentList);
        }
    }

}
