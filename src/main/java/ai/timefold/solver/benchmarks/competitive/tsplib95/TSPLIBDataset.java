package ai.timefold.solver.benchmarks.competitive.tsplib95;

import java.math.BigDecimal;
import java.nio.file.Path;

import ai.timefold.solver.benchmarks.competitive.Dataset;

/**
 * <dl>
 * <dt>Symmetric datasets</dt>
 * <dd>
 * <ul>
 * <li><a href="http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/tsp/ALL_tsp.tar.gz">Source</a>.</li>
 * <li><a href="http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/STSP.html">Best known distances</a>.</li>
 * <li><a href="https://www.localsolver.com/benchmark/localsolver-vs-gurobi-traveling-salesman-problem-tsp">LocalSolver
 * performance data</a>.</li>
 * </ul>
 * </dd>
 *
 * <dt>Symmetric datasets</dt>
 * <dd>
 * <ul>
 * <li><a href="http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/atsp/ALL_atsp.tar">Source</a>.</li>
 * <li><a href="http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/ATSP.html">Best known distances</a>.</li>
 * <li>LocalSolver did not publish performance data.</li>
 * </ul>
 * </dd>
 * </dl>
 *
 * <p>
 * All of the data was downloaded on November 11, 2023 and copy-pasted manually into this file.
 */
public enum TSPLIBDataset implements Dataset<TSPLIBDataset> {

    a280(2579),
    ali535(202339),
    att48(10628),
    att532(27686),
    bayg29(1610),
    bays29(2020),
    berlin52(7542),
    bier127(118282),
    br17(39, false),
    brazil58(25395),
    brd14051(469385, true, true),
    brg180(1950),
    burma14(3323),
    ch130(6110),
    ch150(6528),
    d198(15780),
    d493(35002),
    d657(48912),
    d1291(50801),
    d1655(62128),
    d2103(80450),
    d15112(157308, true, true),
    d18512(64523, true, true),
    dantzig42(699),
    dsj1000(18660188),
    eil51(426),
    eil76(538),
    eil101(629),
    fl417(11861),
    fl1400(20127),
    fl1577(22249),
    fl3795(28772),
    fnl4461(182566),
    fri26(937),
    ft53(6905, false),
    ft70(38673, false),
    ftv33(1286, false),
    ftv35(1473, false),
    ftv38(1530, false),
    ftv44(1613, false),
    ftv47(1776, false),
    ftv55(1608, false),
    ftv64(1839, false),
    ftv70(1950, false),
    ftv170(2755, false),
    gil262(2378),
    gr17(2085),
    gr21(2707),
    gr24(1272),
    gr48(5046),
    gr96(55209),
    gr120(6942),
    gr137(69853),
    gr202(40160),
    gr229(134602),
    gr431(171414),
    gr666(294358),
    hk48(11461),
    kro124p(36230, false),
    kroA100(21282),
    kroA150(26524),
    kroA200(29368),
    kroB100(22141),
    kroB150(26130),
    kroB200(29437),
    kroC100(20749),
    kroD100(21294),
    kroE100(22068),
    lin105(14379),
    lin318(42029),
    // linhp318(41345), // No support for FIXED_EDGES; LocalSolver has it.
    nrw1379(56638),
    p43(5620, false),
    p654(34643),
    pa561(2763),
    pcb442(50778),
    pcb1173(56892),
    pcb3038(137694),
    pla7397(23260728, true, true),
    pla33810(66048945, true, true),
    pla85900(142382641, true, true),
    pr76(108159),
    pr107(44303),
    pr124(59030),
    pr136(96772),
    pr144(58537),
    pr152(73682),
    pr226(80369),
    pr264(49135),
    pr299(48191),
    pr439(107217),
    pr1002(259045),
    pr2392(378032),
    rat99(1211),
    rat195(2323),
    rat575(6773),
    rat783(8806),
    rbg323(1326, false),
    rbg358(1163, false),
    rbg403(2465, false),
    rbg443(2720, false),
    rd100(7910),
    rd400(15281),
    rl1304(252948),
    rl1323(270199),
    rl1889(316536),
    rl5915(565530, true, true),
    rl5934(556045, true, true),
    rl11849(923288, true, true),
    ry48p(14422, false),
    si175(21407),
    si535(48450),
    si1032(92650),
    st70(675),
    swiss42(1273),
    ts225(126643),
    tsp225(3916),
    u159(42080),
    u574(36905),
    u724(41910),
    u1060(224094),
    u1432(152970),
    u1817(57201),
    u2152(64253),
    u2319(234256),
    ulysses16(6859),
    ulysses22(7013),
    usa13509(19982859, true, true),
    vm1084(239297),
    vm1748(336556);

    private final int bestKnownDistance;
    private final boolean symmetric;
    private final boolean large;

    TSPLIBDataset(int bestKnownDistance) {
        this(bestKnownDistance, true);
    }

    TSPLIBDataset(int bestKnownDistance, boolean symmetric) {
        this(bestKnownDistance, symmetric, false);
    }

    TSPLIBDataset(int bestKnownDistance, boolean symmetric, boolean large) {
        this.bestKnownDistance = bestKnownDistance;
        this.symmetric = symmetric;
        this.large = large;
    }

    @Override
    public BigDecimal getBestKnownSolution() {
        return BigDecimal.valueOf(bestKnownDistance);
    }

    public boolean isSymmetric() {
        return symmetric;
    }

    @Override
    public boolean isLarge() {
        return large;
    }

    @Override
    public boolean isBestKnownSolutionOptimal() {
        return true;
    }

    @Override
    public Path getPath() {
        var dir = isSymmetric() ? "symmetric" : "asymmetric";
        var extension = isSymmetric() ? "tsp" : "atsp";
        return Path.of("data", "tsp", "import", dir, this.name() + "." + extension);
    }

}
