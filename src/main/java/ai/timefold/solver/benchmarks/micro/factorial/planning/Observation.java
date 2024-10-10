package ai.timefold.solver.benchmarks.micro.factorial.planning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import ai.timefold.solver.benchmarks.examples.cloudbalancing.app.CloudBalancingApp;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.app.ConferenceSchedulingApp;
import ai.timefold.solver.benchmarks.examples.curriculumcourse.app.CurriculumCourseApp;
import ai.timefold.solver.benchmarks.examples.examination.app.ExaminationApp;
import ai.timefold.solver.benchmarks.examples.machinereassignment.app.MachineReassignmentApp;
import ai.timefold.solver.benchmarks.examples.meetingscheduling.app.MeetingSchedulingApp;
import ai.timefold.solver.benchmarks.examples.nurserostering.app.NurseRosteringApp;
import ai.timefold.solver.benchmarks.examples.pas.app.PatientAdmissionScheduleApp;
import ai.timefold.solver.benchmarks.examples.taskassigning.app.TaskAssigningApp;
import ai.timefold.solver.benchmarks.examples.travelingtournament.app.TravelingTournamentApp;
import ai.timefold.solver.benchmarks.examples.tsp.app.TspApp;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.app.VehicleRoutingApp;
import ai.timefold.solver.benchmarks.micro.factorial.configuration.AbstractConfiguration;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.impl.solver.DefaultSolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Observation {

    private static final Logger LOGGER = LoggerFactory.getLogger(Observation.class);
    private int id;
    private final ExperimentWriter writer;
    private final Map<String, AbstractConfiguration> configurationMap;
    private final List<String> outputColumns;

    public Observation(ExperimentWriter writer, List<AbstractConfiguration> configurationList, List<String> outputColumns) {
        this.writer = writer;
        this.configurationMap = configurationList.stream().collect(Collectors.toMap(AbstractConfiguration::getFactorName,
                c -> c));
        this.outputColumns = outputColumns;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void set(AbstractConfiguration configuration) {
        configurationMap.put(configuration.getFactorName(), configuration);
    }

    public Object getValue(String factorName) {
        var factor = configurationMap.get(factorName);
        if (factor != null) {
            var level = factor.getLevel();
            if (level == null) {
                return null;
            }
            return factor.getLevel().getValue();
        }
        return null;
    }

    private void initConfiguration() {
        configurationMap.forEach((key, c) -> c.init(this));
    }

    public void prepare(SolverConfig solverConfig) {
        configurationMap.forEach((key, c) -> c.apply(this, solverConfig));
    }

    public void save(Object solution, Solver<?> solver) {
        List<String> line = new ArrayList<>();
        outputColumns.forEach(outputColumn -> {
            switch (outputColumn) {
                case "id": {
                    line.add(String.valueOf(id));
                    break;
                }
                case "score": {
                    if (solver != null) {
                        line.add(String.valueOf(((DefaultSolver<?>) solver).getSolverScope().getBestScore()));
                    } else {
                        line.add("ERROR");
                    }
                    break;
                }
                case "scoreConverted": {
                    if (solver != null) {
                        var bestScore = ((DefaultSolver<?>) solver).getSolverScope().getBestScore();
                        line.add(String.valueOf(convertScore(bestScore)));
                    } else {
                        line.add("ERROR");
                    }
                    break;
                }
                case "scoreLogarithmic": {
                    if (solver != null) {
                        var bestScore = ((DefaultSolver<?>) solver).getSolverScope().getBestScore();
                        var finalScore = convertScore(bestScore);
                        if (finalScore < 0) {
                            finalScore = Math.log(finalScore * -1) * -1;
                        } else {
                            finalScore = Math.log(finalScore);
                        }
                        line.add(String.valueOf(finalScore));
                    } else {
                        line.add("ERROR");
                    }
                    break;
                }
                case "scoreSpeed": {
                    if (solver != null) {
                        line.add(String.valueOf(((DefaultSolver<?>) solver).getScoreCalculationSpeed()));
                    } else {
                        line.add("ERROR");
                    }
                    break;
                }
                case "scoreCount": {
                    if (solver != null) {
                        line.add(String.valueOf(((DefaultSolver<?>) solver).getScoreCalculationCount()));
                    } else {
                        line.add("ERROR");
                    }
                    break;
                }
                case "moveSpeed": {
                    if (solver != null) {
                        line.add(String.valueOf(((DefaultSolver<?>) solver).getMoveEvaluationSpeed()));
                    } else {
                        line.add("ERROR");
                    }
                    break;
                }
                case "moveCount": {
                    if (solver != null) {
                        line.add(String.valueOf(((DefaultSolver<?>) solver).getMoveEvaluationCount()));
                    } else {
                        line.add("ERROR");
                    }
                    break;
                }
                case "timeSpent": {
                    if (solver != null) {
                        line.add(String.valueOf(((DefaultSolver<?>) solver).getTimeMillisSpent()));
                    } else {
                        line.add("ERROR");
                    }
                    break;
                }
                default: {
                    var value = configurationMap.get(outputColumn);
                    if (value != null) {
                        line.add(value.toCSV());
                    } else {
                        line.add("-");
                    }
                }
            }
        });
        writer.saveResult(String.join(";", line) + "\n");
    }

    private double convertScore(Score<?> score) {
        Number[] values = score.toLevelNumbers();
        double finalScore = 0;
        switch (values.length) {
            case 3: {
                // Hard
                finalScore += 10_000_000 * values[0].doubleValue();
                // Medium
                finalScore += 1_000_000 * values[1].doubleValue();
                // Soft
                finalScore += values[2].doubleValue();
                break;
            }
            case 2: {
                // Hard
                finalScore += 10_000_000 * values[0].doubleValue();
                // Soft
                finalScore += values[1].doubleValue();
                break;
            }
            case 1: {
                finalScore = values[0].doubleValue();
                break;
            }
            default:
                throw new IllegalArgumentException(
                        "Unsupported score type: " + score.getClass().getSimpleName());
        }
        return finalScore;
    }

    public void run() {
        LOGGER.info("Running observation {}: {}", id, this);
        try {
            initConfiguration();
            if (getValue("type") == null) {
                throw new IllegalArgumentException("The factor \"type\" is required");
            }
            if (getValue("datasetName") == null) {
                throw new IllegalArgumentException("The factor \"datasetName\" is required");
            }
            var type = getValue("type").toString();
            var datasetName = getValue("datasetName").toString();
            var configFile = Optional.ofNullable(getValue("configFile")).map(Object::toString).orElse(null);
            switch (type) {
                case "cloudbalancing" ->
                    new CloudBalancingApp().solve(datasetName, configFile, this, Observation::prepare, Observation::save);
                case "conferencescheduling" ->
                    new ConferenceSchedulingApp().solve(datasetName, configFile, this, Observation::prepare, Observation::save);
                case "curriculumcourse" ->
                    new CurriculumCourseApp().solve(datasetName, configFile, this, Observation::prepare, Observation::save);
                case "examination" ->
                    new ExaminationApp().solve(datasetName, configFile, this, Observation::prepare, Observation::save);
                case "machinereassignment" ->
                    new MachineReassignmentApp().solve(datasetName, configFile, this, Observation::prepare, Observation::save);
                case "meetingscheduling" ->
                    new MeetingSchedulingApp().solve(datasetName, configFile, this, Observation::prepare, Observation::save);
                case "nurserostering" ->
                    new NurseRosteringApp().solve(datasetName, configFile, this, Observation::prepare, Observation::save);
                case "pas" ->
                    new PatientAdmissionScheduleApp().solve(datasetName, configFile, this, Observation::prepare,
                            Observation::save);
                case "taskassigning" ->
                    new TaskAssigningApp().solve(datasetName, configFile, this, Observation::prepare, Observation::save);
                case "travelingtournament" ->
                    new TravelingTournamentApp().solve(datasetName, configFile, this, Observation::prepare, Observation::save);
                case "tsp" ->
                    new TspApp().solve(datasetName, configFile, this, Observation::prepare, Observation::save);
                case "vehiclerouting" ->
                    new VehicleRoutingApp().solve(datasetName, configFile, this, Observation::prepare,
                            Observation::save);
                default -> throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            // Add the result as error
            LOGGER.error("Error running observation: " + id, e);
            save(null, null);
        }
    }

    @Override
    public String toString() {
        return "Observation{" +
                "id=" + id +
                ", configurationMap=" + configurationMap +
                ", outputColumns=" + outputColumns +
                '}';
    }
}
