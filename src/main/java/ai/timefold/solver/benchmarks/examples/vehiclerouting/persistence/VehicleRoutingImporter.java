package ai.timefold.solver.benchmarks.examples.vehiclerouting.persistence;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ai.timefold.solver.benchmarks.examples.common.domain.location.AirLocation;
import ai.timefold.solver.benchmarks.examples.common.domain.location.DistanceType;
import ai.timefold.solver.benchmarks.examples.common.domain.location.Location;
import ai.timefold.solver.benchmarks.examples.common.domain.location.RoadLocation;
import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractTxtSolutionImporter;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Customer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Depot;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.Vehicle;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.VehicleRoutingSolution;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.timewindowed.TimeWindowedCustomer;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.timewindowed.TimeWindowedDepot;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.domain.timewindowed.TimeWindowedVehicleRoutingSolution;

public class VehicleRoutingImporter extends
        AbstractTxtSolutionImporter<VehicleRoutingSolution> {

    @Override
    public String getInputFileSuffix() {
        return "vrp";
    }

    @Override
    public TxtInputBuilder<VehicleRoutingSolution>
            createTxtInputBuilder() {
        return new VehicleRoutingInputBuilder();
    }

    public static class VehicleRoutingInputBuilder
            extends TxtInputBuilder<VehicleRoutingSolution> {

        private VehicleRoutingSolution solution;

        private boolean timewindowed;
        private int customerListSize;
        private int vehicleListSize;
        private int capacity;
        private Map<Long, Location> locationMap;
        private List<Depot> depotList;

        @Override
        public VehicleRoutingSolution readSolution()
                throws IOException {
            String firstLine = readStringValue();
            if (firstLine.matches("\\s*NAME\\s*:.*")) {
                // Might be replaced by TimeWindowedVehicleRoutingSolution later on
                solution = new VehicleRoutingSolution();
                solution.setName(removePrefixSuffixFromLine(firstLine, "\\s*NAME\\s*:", ""));
                readVrpWebFormat();
            } else {
                timewindowed = true;
                solution = new TimeWindowedVehicleRoutingSolution();
                solution.setName(firstLine);
                readTimeWindowedFormat();
            }
            BigInteger a = factorial(customerListSize + vehicleListSize - 1);
            BigInteger b = factorial(vehicleListSize - 1);
            BigInteger possibleSolutionSize = (a == null || b == null) ? null : a.divide(b);
            logger.info("VehicleRoutingSolution {} has {} depots, {} vehicles and {} customers with a search space of {}.",
                    getInputId(),
                    solution.getDepotList().size(),
                    solution.getVehicleList().size(),
                    solution.getCustomerList().size(),
                    getFlooredPossibleSolutionSize(possibleSolutionSize));
            return solution;
        }

        // ************************************************************************
        // CVRP normal format. See https://neo.lcc.uma.es/vrp/
        // ************************************************************************

        public void readVrpWebFormat() throws IOException {
            readVrpWebHeaders();
            readVrpWebLocationList();
            readVrpWebCustomerList();
            readVrpWebDepotList();
            createVrpWebVehicleList();
            readConstantLine("EOF");
        }

        private void readVrpWebHeaders() throws IOException {
            skipOptionalConstantLines("COMMENT *:.*");
            String vrpType = readStringValue("TYPE *:");
            switch (vrpType) {
                case "CVRP":
                    timewindowed = false;
                    break;
                case "CVRPTW":
                    timewindowed = true;
                    Long solutionId = solution.getId();
                    String solutionName = solution.getName();
                    solution = new TimeWindowedVehicleRoutingSolution(solutionId);
                    solution.setName(solutionName);
                    break;
                default:
                    throw new IllegalArgumentException("The vrpType (" + vrpType + ") is not supported.");
            }
            customerListSize = readIntegerValue("DIMENSION *:");
            String edgeWeightType = readStringValue("EDGE_WEIGHT_TYPE *:");
            if (edgeWeightType.equalsIgnoreCase("EUC_2D")) {
                solution.setDistanceType(
                        DistanceType.AIR_DISTANCE);
            } else if (edgeWeightType.equalsIgnoreCase("EXPLICIT")) {
                solution.setDistanceType(
                        DistanceType.ROAD_DISTANCE);
                String edgeWeightFormat = readStringValue("EDGE_WEIGHT_FORMAT *:");
                if (!edgeWeightFormat.equalsIgnoreCase("FULL_MATRIX")) {
                    throw new IllegalArgumentException("The edgeWeightFormat (" + edgeWeightFormat + ") is not supported.");
                }
            } else {
                throw new IllegalArgumentException("The edgeWeightType (" + edgeWeightType + ") is not supported.");
            }
            solution.setDistanceUnitOfMeasurement(readOptionalStringValue("EDGE_WEIGHT_UNIT_OF_MEASUREMENT *:", "distance"));
            capacity = readIntegerValue("CAPACITY *:");
        }

        private void readVrpWebLocationList() throws IOException {
            DistanceType distanceType =
                    solution.getDistanceType();
            locationMap = new LinkedHashMap<>(customerListSize);
            List<Location> customerLocationList =
                    new ArrayList<>(customerListSize);
            readConstantLine("NODE_COORD_SECTION");
            for (int i = 0; i < customerListSize; i++) {
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpacesOrTabs(line.trim(), 3, 4);
                long id = Long.parseLong(lineTokens[0]);
                double latitude = Double.parseDouble(lineTokens[1]);
                double longitude = Double.parseDouble(lineTokens[2]);
                Location location = switch (distanceType) {
                    case AIR_DISTANCE -> new AirLocation(id, latitude, longitude);
                    case ROAD_DISTANCE -> new RoadLocation(id, latitude, longitude);
                    default -> throw new IllegalStateException("The distanceType (" + distanceType + ") is not supported.");
                };
                if (lineTokens.length >= 4) {
                    location.setName(lineTokens[3]);
                }
                customerLocationList.add(location);
                locationMap.put(location.getId(), location);
            }
            if (distanceType == DistanceType.ROAD_DISTANCE) {
                readConstantLine("EDGE_WEIGHT_SECTION");
                for (int i = 0; i < customerListSize; i++) {
                    RoadLocation location =
                            (RoadLocation) customerLocationList
                                    .get(i);
                    Map<RoadLocation, Double> travelDistanceMap =
                            new LinkedHashMap<>(customerListSize);
                    String line = bufferedReader.readLine();
                    String[] lineTokens = splitBySpacesOrTabs(line.trim(), customerListSize);
                    for (int j = 0; j < customerListSize; j++) {
                        double travelDistance = Double.parseDouble(lineTokens[j]);
                        if (i == j) {
                            if (travelDistance != 0.0) {
                                throw new IllegalStateException("The travelDistance (" + travelDistance
                                        + ") should be zero.");
                            }
                        } else {
                            RoadLocation otherLocation =
                                    (RoadLocation) customerLocationList.get(j);
                            travelDistanceMap.put(otherLocation, travelDistance);
                        }
                    }
                    location.setTravelDistanceMap(travelDistanceMap);
                }
            }
            solution.setLocationList(customerLocationList);
        }

        private void readVrpWebCustomerList() throws IOException {
            readConstantLine("DEMAND_SECTION");
            depotList = new ArrayList<>(customerListSize);
            List<Customer> customerList =
                    new ArrayList<>(customerListSize);
            for (int i = 0; i < customerListSize; i++) {
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBySpacesOrTabs(line.trim(), timewindowed ? 5 : 2);
                long id = Long.parseLong(lineTokens[0]);
                int demand = Integer.parseInt(lineTokens[1]);
                // Depots have no demand
                if (demand == 0) {
                    Location location =
                            locationMap.get(id);
                    if (location == null) {
                        throw new IllegalArgumentException("The depot with id (" + id
                                + ") has no location (" + location + ").");
                    }
                    if (timewindowed) {
                        long serviceDuration = Long.parseLong(lineTokens[4]);
                        if (serviceDuration != 0L) {
                            throw new IllegalArgumentException("The depot with id (" + id
                                    + ") has a serviceDuration (" + serviceDuration + ") that is not 0.");
                        }
                        depotList.add(
                                new TimeWindowedDepot(
                                        id, location, Long.parseLong(lineTokens[2]),
                                        Long.parseLong(lineTokens[3])));
                    } else {
                        depotList.add(new Depot(id, location));
                    }
                } else {
                    Location location =
                            locationMap.get(id);
                    if (location == null) {
                        throw new IllegalArgumentException("The customer with id (" + id
                                + ") has no location (" + location + ").");
                    }
                    // Notice that we leave the PlanningVariable properties on null
                    if (timewindowed) {
                        customerList.add(new TimeWindowedCustomer(id, location, demand, Long.parseLong(lineTokens[2]),
                                Long.parseLong(lineTokens[3]), Long.parseLong(lineTokens[4])));
                    } else {
                        customerList.add(new Customer(id, location,
                                demand));
                    }
                }
            }
            solution.setCustomerList(customerList);
            solution.setDepotList(depotList);
        }

        private void readVrpWebDepotList() throws IOException {
            readConstantLine("DEPOT_SECTION");
            int depotCount = 0;
            long id = readLongValue();
            while (id != -1) {
                depotCount++;
                id = readLongValue();
            }
            if (depotCount != depotList.size()) {
                throw new IllegalStateException("The number of demands with 0 demand (" + depotList.size()
                        + ") differs from the number of depots (" + depotCount + ").");
            }
        }

        private void createVrpWebVehicleList() throws IOException {
            String inputFileName = inputFile.getName();
            if (inputFileName.toLowerCase().startsWith("tutorial")) {
                vehicleListSize = readIntegerValue("VEHICLES *:");
            } else {
                String inputFileNameRegex = "^.+\\-k(\\d+)\\.vrp$";
                if (!inputFileName.matches(inputFileNameRegex)) {
                    throw new IllegalArgumentException("The inputFileName (" + inputFileName
                            + ") does not match the inputFileNameRegex (" + inputFileNameRegex + ").");
                }
                String vehicleListSizeString = inputFileName.replaceAll(inputFileNameRegex, "$1");
                try {
                    vehicleListSize = Integer.parseInt(vehicleListSizeString);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("The inputFileName (" + inputFileName
                            + ") has a vehicleListSizeString (" + vehicleListSizeString + ") that is not a number.", e);
                }
            }
            createVehicleList();
        }

        private void createVehicleList() {
            List<Vehicle> vehicleList =
                    new ArrayList<>(vehicleListSize);
            long id = 0;
            for (int i = 0; i < vehicleListSize; i++) {
                // Round robin the vehicles to a depot if there are multiple depots
                Vehicle vehicle =
                        new Vehicle(i, capacity, depotList.get(i % depotList.size()));
                vehicleList.add(vehicle);
            }
            solution.setVehicleList(vehicleList);
        }

        // ************************************************************************
        // CVRPTW normal format. See https://neo.lcc.uma.es/vrp/
        // ************************************************************************

        public void readTimeWindowedFormat() throws IOException {
            readTimeWindowedHeaders();
            readTimeWindowedDepotAndCustomers();
            createVehicleList();
        }

        private void readTimeWindowedHeaders() throws IOException {
            solution.setDistanceType(DistanceType.AIR_DISTANCE);
            solution.setDistanceUnitOfMeasurement("distance");
            readEmptyLine();
            readConstantLine("VEHICLE");
            readConstantLine("NUMBER +CAPACITY");
            String[] lineTokens = splitBySpacesOrTabs(readStringValue(), 2);
            vehicleListSize = Integer.parseInt(lineTokens[0]);
            capacity = Integer.parseInt(lineTokens[1]);
            readEmptyLine();
            readConstantLine("CUSTOMER");
            readConstantLine(
                    "CUST\\s+NO\\.\\s+XCOORD\\.\\s+YCOORD\\.\\s+DEMAND\\s+READY\\s+TIME\\s+DUE\\s+DATE\\s+SERVICE\\s+TIME");
            readEmptyLine();
        }

        private void readTimeWindowedDepotAndCustomers() throws IOException {
            String line = bufferedReader.readLine();
            int locationListSizeEstimation = 25;
            List<Location> locationList = new ArrayList<>(locationListSizeEstimation);
            depotList = new ArrayList<>(1);
            TimeWindowedDepot depot = null;
            List<Customer> customerList = new ArrayList<>(locationListSizeEstimation);
            boolean first = true;
            while (line != null && !line.trim().isEmpty()) {
                String[] lineTokens = splitBySpacesOrTabs(line.trim(), 7);
                long id = Long.parseLong(lineTokens[0]);
                AirLocation location =
                        new AirLocation(id, Double.parseDouble(lineTokens[1]), Double.parseDouble(lineTokens[2]));
                locationList.add(location);
                int demand = Integer.parseInt(lineTokens[3]);
                long minStartTime = Long.parseLong(lineTokens[4]) * 1000L;
                long maxEndTime = Long.parseLong(lineTokens[5]) * 1000L;
                long serviceDuration = Long.parseLong(lineTokens[6]) * 1000L;
                if (first) {
                    if (demand != 0) {
                        throw new IllegalArgumentException("The depot with id (" + id
                                + ") has a demand (" + demand + ").");
                    }
                    if (serviceDuration != 0) {
                        throw new IllegalArgumentException("The depot with id (" + id
                                + ") has a serviceDuration (" + serviceDuration + ").");
                    }
                    depot = new TimeWindowedDepot(id, location, minStartTime, maxEndTime);
                    depotList.add(depot);
                    first = false;
                } else {
                    // Score constraint arrivalAfterMaxEndTimeAtDepot is a built-in hard constraint in VehicleRoutingImporter
                    long maximumAllowedMaxEndTime = depot.getMaxEndTime()
                            - serviceDuration - location.getDistanceTo(depot.getLocation());
                    if (maxEndTime > maximumAllowedMaxEndTime) {
                        logger.warn("The customer ({})'s maxEndTime ({}) was automatically reduced" +
                                " to maximumAllowedMaxEndTime ({}) because of the depot's maxEndTime ({}).",
                                id, maxEndTime, maximumAllowedMaxEndTime, depot.getMaxEndTime());
                        maxEndTime = maximumAllowedMaxEndTime;
                    }
                    // Do not add a customer that has no demand
                    if (demand != 0) {
                        // Notice that we leave the PlanningVariable properties on null
                        TimeWindowedCustomer customer =
                                new TimeWindowedCustomer(id, location, demand, minStartTime, maxEndTime, serviceDuration);
                        customerList.add(customer);
                    }
                }
                line = bufferedReader.readLine();
            }
            solution.setLocationList(locationList);
            solution.setDepotList(depotList);
            solution.setCustomerList(customerList);
            customerListSize = locationList.size();
        }

    }

}
