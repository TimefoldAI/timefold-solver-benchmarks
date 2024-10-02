package ai.timefold.solver.benchmarks.micro.factorial.configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ai.timefold.solver.benchmarks.examples.cloudbalancing.app.CloudBalancingApp;
import ai.timefold.solver.benchmarks.examples.conferencescheduling.app.ConferenceSchedulingApp;
import ai.timefold.solver.benchmarks.examples.tsp.app.TspApp;
import ai.timefold.solver.benchmarks.examples.vehiclerouting.app.VehicleRoutingApp;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.impl.solver.DefaultSolver;

public class Observation {

    private int id;
    private final ExperimentWriter writer;
    private final Map<String, AbstractConfiguration> configurationMap;
    private final List<String> outputColumns;


    public Observation(ExperimentWriter writer, List<AbstractConfiguration> configurationList, List<String> outputColumns) {
        this.writer = writer;
        this.configurationMap = configurationList.stream().collect(Collectors.toMap(AbstractConfiguration::getKey, c -> c));
        this.outputColumns = outputColumns;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void set(AbstractConfiguration configuration) {
        configurationMap.put(configuration.getKey(), configuration);
    }

    public Object getValue(String key) {
        return configurationMap.get(key).getValue();
    }

    public void prepare(SolverConfig solverConfig) {
        configurationMap.forEach((key, c) -> c.apply(solverConfig));
    }

    public void save(Object solution, Solver<?> solver) {
        StringBuilder line = new StringBuilder();
        outputColumns.forEach(outputColumn -> {
            switch (outputColumn) {
                case "score": {
                    if (solver != null) {
                        line.append(((DefaultSolver<?>) solver).getSolverScope().getBestScore()).append(";");
                    } else {
                        line.append("ERROR;");
                    }
                    break;
                }
                case "scoreConverted": {
                    if (solver != null) {
                        var bestScore = ((DefaultSolver<?>) solver).getSolverScope().getBestScore();
                        line.append(convertScore(bestScore)).append(";");
                    } else {
                        line.append("ERROR;");
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
                        line.append(finalScore).append(";");
                    } else {
                        line.append("ERROR;");
                    }
                    break;
                }
                case "scoreSpeed": {
                    if (solver != null) {
                        line.append(((DefaultSolver<?>) solver).getScoreCalculationSpeed()).append(";");
                    } else {
                        line.append("ERROR;");
                    }
                    break;
                }
                case "scoreCount": {
                    if (solver != null) {
                        line.append(((DefaultSolver<?>) solver).getScoreCalculationCount()).append(";");
                    } else {
                        line.append("ERROR;");
                    }
                    break;
                }
                case "moveSpeed": {
                    if (solver != null) {
                        line.append(((DefaultSolver<?>) solver).getMoveEvaluationSpeed()).append(";");
                    } else {
                        line.append("ERROR;");
                    }
                    break;
                }
                case "moveCount": {
                    if (solver != null) {
                        line.append(((DefaultSolver<?>) solver).getMoveEvaluationCount()).append(";");
                    } else {
                        line.append("ERROR;");
                    }
                    break;
                }
                case "timeSpent": {
                    if (solver != null) {
                        line.append(((DefaultSolver<?>) solver).getTimeMillisSpent()).append(";");
                    } else {
                        line.append("ERROR;");
                    }
                    break;
                }
                default: {
                    var value = configurationMap.get(outputColumn);
                    if (value != null) {
                        line.append(value.toCSV());
                    } else {
                        line.append("-");
                    }
                    line.append(";");
                }
            }
        });
        line.append("\n");
        writer.saveResult(line.toString());
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
        writer.log("Running observation %d: %s".formatted(id, this));
        try {
            var type = getValue("type").toString();
            var datasetName = getValue("datasetName").toString();
            switch (type) {
                case "cloudbalancing" ->
                        new CloudBalancingApp().solve(datasetName, this, Observation::prepare, Observation::save);
                case "conferencescheduling" ->
                        new ConferenceSchedulingApp().solve(datasetName, this, Observation::prepare, Observation::save);
                case "tsp" -> new TspApp().solve(datasetName, this, Observation::prepare, Observation::save);
                case "vehiclerouting" -> new VehicleRoutingApp().solve(datasetName, this, Observation::prepare,
                        Observation::save);
                default -> throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            // Add the result as error
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
