package ai.timefold.solver.benchmarks.examples.flowshop.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import ai.timefold.solver.benchmarks.examples.common.persistence.AbstractTxtSolutionImporter;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.Job;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.JobScheduleSolution;
import ai.timefold.solver.benchmarks.examples.flowshop.domain.Machine;

/**
 * Import Taillard dataset files.
 * First line: number of jobs, number of machines, seed, upper bound and lower bound.
 * Second line: processing time of job_i on machine 1, processing time of job_(i + 1) on machine 1, and so on.
 * Third line: processing time of job_i on machine 2, processing time of job_(i + 1) on machine 2, and so on.
 * Nth line: processing time of job_i on machine n, processing time of job_(i + 1) on machine n, and so on.
 */
public class TaillardImporter extends AbstractTxtSolutionImporter<JobScheduleSolution> {

    @Override
    public TxtInputBuilder<JobScheduleSolution> createTxtInputBuilder() {
        return new FlowshopInputBuilder();
    }

    public static class FlowshopInputBuilder extends TxtInputBuilder<JobScheduleSolution> {

        @Override
        public JobScheduleSolution readSolution() throws IOException {
            try (var firstLineScanner = new Scanner(readStringValue())) {
                var numJobs = firstLineScanner.nextInt();
                var numMachines = firstLineScanner.nextInt();
                var machines = new Machine[numMachines];
                var jobs = new ArrayList<Job>(numJobs);
                for (var i = 0; i < numMachines; i++) {
                    int[] processingTime = new int[numJobs];
                    machines[i] = new Machine(i, processingTime);
                    try (var processingTimeScanner = new Scanner(readStringValue())) {
                        for (var j = 0; j < numJobs; j++) {
                            processingTime[j] = processingTimeScanner.nextInt();
                        }
                    }
                }
                for (var i = 0; i < numJobs; i++) {
                    jobs.add(new Job(i, machines));
                }
                return new JobScheduleSolution(machines, jobs);
            }
        }
    }
}
