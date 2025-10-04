package ai.timefold.solver.benchmarks.examples.flowshop.phase;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import ai.timefold.solver.benchmarks.examples.flowshop.domain.Job;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.JobScheduleSolution;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.api.solver.phase.PhaseCommand;

/**
 * This custom phase implements the NEH construction heuristic with the Taillard acceleration strategy.
 * The algorithm is based on the following work:
 * <p>
 * Some efficient heuristic methods for the flow shop sequencing problem
 * European Journal of Operational Research
 * Taillard, E. (1990).
 */
public class NEHCustomPhase implements PhaseCommand<JobScheduleSolution> {

    @Override
    public void changeWorkingSolution(ScoreDirector<JobScheduleSolution> scoreDirector, BooleanSupplier isPhaseTerminated) {
        // The enrichment step, after loading the solution,
        // will sort the jobs by the sum of processing times in decreasing order
        var workingSolution = scoreDirector.getWorkingSolution();
        var jobs = workingSolution.getJobs();
        var currentJobSchedule = new ArrayList<Job>(jobs.size());
        currentJobSchedule.add(jobs.get(0));
        var numMachines = workingSolution.getAllMachines().length;
        for (var k = 1; k < jobs.size(); k++) {
            var earliestCompletionTimeMatrix = new int[k + 2][numMachines + 1];
            var tailMatrix = new int[k + 2][numMachines + 1];
            var earliestRelativeCompletionTimeMatrix = new int[k + 2][numMachines + 1];
            computeJobSequenceTime(k, numMachines, earliestCompletionTimeMatrix, tailMatrix,
                    earliestRelativeCompletionTimeMatrix, currentJobSchedule, jobs);
            var partialMakespan = computePartialMakespan(k, numMachines, tailMatrix, earliestRelativeCompletionTimeMatrix);
            var minMakespan = partialMakespan[0];
            var minIndex = 0;
            for (var i = 1; i <= k; i++) {
                if (partialMakespan[i] < minMakespan) {
                    minMakespan = partialMakespan[i];
                    minIndex = i;
                }
            }
            currentJobSchedule.add(minIndex, jobs.get(k));
        }
        scoreDirector.beforeListVariableChanged(workingSolution.getMachine(), "jobs", 0, 0);
        for (var job : currentJobSchedule) {
            scoreDirector.beforeListVariableElementAssigned(workingSolution.getMachine(), "jobs", job);
        }
        workingSolution.getMachine().setJobs(currentJobSchedule);
        scoreDirector.afterListVariableChanged(workingSolution.getMachine(), "jobs", 0, currentJobSchedule.size());
        for (var job : currentJobSchedule) {
            scoreDirector.afterListVariableElementAssigned(workingSolution.getMachine(), "jobs", job);
        }
        scoreDirector.triggerVariableListeners();
    }

    /**
     * Compute the earliest completion time of i-th job on j-th machine, the tail of the i-th job on the j-th machine and the
     * earliest relative completion time for the k-th job in i-th position on j-th machine.
     *
     * @param k the max number of positions where the job can be inserted
     * @param numMachines the number of machines
     * @param earliestCompletionTimeMatrix the earliest completion time matrix
     * @param tailMatrix the job tail time matrix
     * @param earliestRelativeCompletionTimeMatrix the earliest relative completion time
     * @param currentJobSchedule the current scheduled jobs
     * @param allJobs the job list
     */
    private void computeJobSequenceTime(int k, int numMachines, int[][] earliestCompletionTimeMatrix, int[][] tailMatrix,
            int[][] earliestRelativeCompletionTimeMatrix, List<Job> currentJobSchedule, List<Job> allJobs) {
        for (var i = 0; i <= k; i++) { // Scheduled jobs
            for (var j = 0; j < numMachines; j++) { // Available machines
                if (i < k) {
                    var completionPreviousMachine = j - 1 >= 0 ? earliestCompletionTimeMatrix[i][j - 1] : 0;
                    var completionPreviousJob = i - 1 >= 0 ? earliestCompletionTimeMatrix[i - 1][j] : 0;
                    earliestCompletionTimeMatrix[i][j] =
                            Math.max(completionPreviousMachine, completionPreviousJob)
                                    + currentJobSchedule.get(i).getProcessingTime(j);
                }
                if (i > 0) {
                    tailMatrix[k - i][numMachines - j - 1] =
                            Math.max(tailMatrix[k - i][numMachines - j], tailMatrix[k - i + 1][numMachines - j - 1])
                                    + currentJobSchedule.get(k - i).getProcessingTime(numMachines - j - 1);
                }
                var relativeCompletionPreviousMachine = j - 1 >= 0 ? earliestRelativeCompletionTimeMatrix[i][j - 1] : 0;
                var completionPreviousJob = i - 1 >= 0 ? earliestCompletionTimeMatrix[i - 1][j] : 0;
                earliestRelativeCompletionTimeMatrix[i][j] =
                        Math.max(relativeCompletionPreviousMachine, completionPreviousJob)
                                + allJobs.get(k).getProcessingTime(j);
            }
        }
    }

    /**
     * Compute the partial makespan when adding a job k at the i-th position.
     *
     * @param k the max number of positions where the job can be inserted
     * @param numMachines the number of machines
     * @param tailMatrix the job tail time matrix
     * @param earliestRelativeCompletionTimeMatrix the earliest relative completion time
     * 
     * @return the partial makespan
     */
    private int[] computePartialMakespan(int k, int numMachines, int[][] tailMatrix,
            int[][] earliestRelativeCompletionTimeMatrix) {
        var partialMakespan = new int[k + 1];
        for (var i = 0; i <= k; i++) {
            var makespan = 0;
            for (var j = 0; j < numMachines; j++) {
                var value = earliestRelativeCompletionTimeMatrix[i][j] + tailMatrix[i][j];
                if (value > makespan) {
                    makespan = value;
                }
            }
            partialMakespan[i] = makespan;
        }
        return partialMakespan;
    }
}
