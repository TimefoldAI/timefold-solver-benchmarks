<?xml version="1.0" encoding="UTF-8"?>
<plannerBenchmark xmlns="https://timefold.ai/xsd/benchmark" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="https://timefold.ai/xsd/benchmark https://timefold.ai/xsd/benchmark/benchmark.xsd">
    <benchmarkDirectory>local/</benchmarkDirectory>
    <parallelBenchmarkCount>4</parallelBenchmarkCount>
    <warmUpSecondsSpentLimit>30</warmUpSecondsSpentLimit>

    <inheritedSolverBenchmark>
        <solver>
            <solutionClass>ai.timefold.solver.benchmarks.examples.nurserostering.domain.NurseRoster</solutionClass>
            <entityClass>ai.timefold.solver.benchmarks.examples.nurserostering.domain.ShiftAssignment</entityClass>
            <scoreDirectorFactory>
                <constraintProviderClass>ai.timefold.solver.benchmarks.examples.nurserostering.score.NurseRosteringConstraintProvider</constraintProviderClass>
            </scoreDirectorFactory>
        </solver>
        <problemBenchmarks>
            <solutionFileIOClass>ai.timefold.solver.benchmarks.examples.nurserostering.persistence.NurseRosterSolutionFileIO</solutionFileIOClass>
            <inputSolutionFile>data/nurserostering/unsolved/long01.json</inputSolutionFile>
            <problemStatisticType>BEST_SCORE</problemStatisticType>
            <problemStatisticType>SCORE_CALCULATION_SPEED</problemStatisticType>
        </problemBenchmarks>
        <subSingleCount>4</subSingleCount>
    </inheritedSolverBenchmark>

    <solverBenchmark>
        <name>Base</name>
        <solver>
            <constructionHeuristic>
                <constructionHeuristicType>WEAKEST_FIT</constructionHeuristicType>
            </constructionHeuristic>
            <localSearch>
                <termination>
                    <unimprovedStepCountLimit>100</unimprovedStepCountLimit>
                </termination>
                <unionMoveSelector>
                    <changeMoveSelector/>
                    <swapMoveSelector/>
                    <pillarChangeMoveSelector/>
                    <pillarSwapMoveSelector/>
                </unionMoveSelector>
                <acceptor>
                    <entityTabuSize>7</entityTabuSize>
                </acceptor>
                <forager>
                    <acceptedCountLimit>800</acceptedCountLimit>
                </forager>
            </localSearch>
        </solver>
    </solverBenchmark>
    <#list [5, 10, 20] as min>
      <#list [20, 40, 80] as max>
        <#if max gt min>
          <#list [10, 20, 100] as prob>
            <solverBenchmark>
                <name>RR-min${min}-max${max}-prob${prob}</name>
                <solver>
                    <constructionHeuristic>
                        <constructionHeuristicType>WEAKEST_FIT</constructionHeuristicType>
                    </constructionHeuristic>
                    <localSearch>
                        <termination>
                            <unimprovedStepCountLimit>100</unimprovedStepCountLimit>
                        </termination>
                        <unionMoveSelector>
                            <unionMoveSelector>
                                <fixedProbabilityWeight>${prob}</fixedProbabilityWeight>
                                <changeMoveSelector />
                                <swapMoveSelector />
                                <pillarChangeMoveSelector />
                                <pillarSwapMoveSelector />
                            </unionMoveSelector>
                            <ruinMoveSelector>
                                <fixedProbabilityWeight>1</fixedProbabilityWeight>
                                <minimumRuinedCount>${min}</minimumRuinedCount>
                                <maximumRuinedCount>${max}</maximumRuinedCount>
                            </ruinMoveSelector>
                        </unionMoveSelector>
                        <acceptor>
                            <entityTabuSize>7</entityTabuSize>
                        </acceptor>
                        <forager>
                            <acceptedCountLimit>800</acceptedCountLimit>
                        </forager>
                    </localSearch>
                </solver>
            </solverBenchmark>
          </#list>
        </#if>
      </#list>
    </#list>
</plannerBenchmark>
