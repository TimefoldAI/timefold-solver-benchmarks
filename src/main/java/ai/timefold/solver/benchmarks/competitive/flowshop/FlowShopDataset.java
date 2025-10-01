package ai.timefold.solver.benchmarks.competitive.flowshop;

import java.math.BigDecimal;
import java.nio.file.Path;

import ai.timefold.solver.benchmarks.competitive.Dataset;

/**
 * The Taillard dataset comes <a href="https://figshare.com/articles/dataset/Flowshop_instances/26485930?file=48152884">from
 * here</a>.
 * The best solution is based on the upper bound value defined by each dataset, and the values were updated according
 * <a href="https://zenodo.org/records/17028980">to here</a>.
 * <p>
 * The datasets in question are:
 * <ul>
 * <li>Benchmarks for basic scheduling problems, E. Taillard</li>
 * </ul>
 */
public enum FlowShopDataset implements Dataset<FlowShopDataset> {

    // 120 instances - Taillard93
    Ta001("taillard93", "Ta001.txt", 20, 1278),
    Ta002("taillard93", "Ta002.txt", 20, 1359),
    Ta003("taillard93", "Ta003.txt", 20, 1081),
    Ta004("taillard93", "Ta004.txt", 20, 1293),
    Ta005("taillard93", "Ta005.txt", 20, 1235),
    Ta006("taillard93", "Ta006.txt", 20, 1195),
    Ta007("taillard93", "Ta007.txt", 20, 1234),
    Ta008("taillard93", "Ta008.txt", 20, 1206),
    Ta009("taillard93", "Ta009.txt", 20, 1230),
    Ta010("taillard93", "Ta010.txt", 20, 1108),
    Ta011("taillard93", "Ta011.txt", 20, 1582),
    Ta012("taillard93", "Ta012.txt", 20, 1659),
    Ta013("taillard93", "Ta013.txt", 20, 1496),
    Ta014("taillard93", "Ta014.txt", 20, 1377),
    Ta015("taillard93", "Ta015.txt", 20, 1419),
    Ta016("taillard93", "Ta016.txt", 20, 1397),
    Ta017("taillard93", "Ta017.txt", 20, 1484),
    Ta018("taillard93", "Ta018.txt", 20, 1538),
    Ta019("taillard93", "Ta019.txt", 20, 1593),
    Ta020("taillard93", "Ta020.txt", 20, 1591),
    Ta021("taillard93", "Ta021.txt", 20, 2297),
    Ta022("taillard93", "Ta022.txt", 20, 2099),
    Ta023("taillard93", "Ta023.txt", 20, 2326),
    Ta024("taillard93", "Ta024.txt", 20, 2223),
    Ta025("taillard93", "Ta025.txt", 20, 2291),
    Ta026("taillard93", "Ta026.txt", 20, 2226),
    Ta027("taillard93", "Ta027.txt", 20, 2273),
    Ta028("taillard93", "Ta028.txt", 20, 2200),
    Ta029("taillard93", "Ta029.txt", 20, 2237),
    Ta030("taillard93", "Ta030.txt", 20, 2178),
    Ta031("taillard93", "Ta031.txt", 50, 2724),
    Ta032("taillard93", "Ta032.txt", 50, 2834),
    Ta033("taillard93", "Ta033.txt", 50, 2621),
    Ta034("taillard93", "Ta034.txt", 50, 2751),
    Ta035("taillard93", "Ta035.txt", 50, 2863),
    Ta036("taillard93", "Ta036.txt", 50, 2829),
    Ta037("taillard93", "Ta037.txt", 50, 2725),
    Ta038("taillard93", "Ta038.txt", 50, 2683),
    Ta039("taillard93", "Ta039.txt", 50, 2552),
    Ta040("taillard93", "Ta040.txt", 50, 2782),
    Ta041("taillard93", "Ta041.txt", 50, 2991),
    Ta042("taillard93", "Ta042.txt", 50, 2867),
    Ta043("taillard93", "Ta043.txt", 50, 2839),
    Ta044("taillard93", "Ta044.txt", 50, 3063),
    Ta045("taillard93", "Ta045.txt", 50, 2976),
    Ta046("taillard93", "Ta046.txt", 50, 3006),
    Ta047("taillard93", "Ta047.txt", 50, 3093),
    Ta048("taillard93", "Ta048.txt", 50, 3037),
    Ta049("taillard93", "Ta049.txt", 50, 2897),
    Ta050("taillard93", "Ta050.txt", 50, 3065),
    Ta051("taillard93", "Ta051.txt", 50, 3846),
    Ta052("taillard93", "Ta052.txt", 50, 3699),
    Ta053("taillard93", "Ta053.txt", 50, 3640),
    Ta054("taillard93", "Ta054.txt", 50, 3719),
    Ta055("taillard93", "Ta055.txt", 50, 3610),
    Ta056("taillard93", "Ta056.txt", 50, 3679),
    Ta057("taillard93", "Ta057.txt", 50, 3704),
    Ta058("taillard93", "Ta058.txt", 50, 3691),
    Ta059("taillard93", "Ta059.txt", 50, 3741),
    Ta060("taillard93", "Ta060.txt", 50, 3755),
    Ta061("taillard93", "Ta061.txt", 100, 5493),
    Ta062("taillard93", "Ta062.txt", 100, 5268),
    Ta063("taillard93", "Ta063.txt", 100, 5175),
    Ta064("taillard93", "Ta064.txt", 100, 5014),
    Ta065("taillard93", "Ta065.txt", 100, 5250),
    Ta066("taillard93", "Ta066.txt", 100, 5135),
    Ta067("taillard93", "Ta067.txt", 100, 5246),
    Ta068("taillard93", "Ta068.txt", 100, 5094),
    Ta069("taillard93", "Ta069.txt", 100, 5448),
    Ta070("taillard93", "Ta070.txt", 100, 5322),
    Ta071("taillard93", "Ta071.txt", 100, 5770),
    Ta072("taillard93", "Ta072.txt", 100, 5349),
    Ta073("taillard93", "Ta073.txt", 100, 5676),
    Ta074("taillard93", "Ta074.txt", 100, 5781),
    Ta075("taillard93", "Ta075.txt", 100, 5467),
    Ta076("taillard93", "Ta076.txt", 100, 5303),
    Ta077("taillard93", "Ta077.txt", 100, 5595),
    Ta078("taillard93", "Ta078.txt", 100, 5617),
    Ta079("taillard93", "Ta079.txt", 100, 5871),
    Ta080("taillard93", "Ta080.txt", 100, 5845),
    Ta081("taillard93", "Ta081.txt", 100, 6134),
    Ta082("taillard93", "Ta082.txt", 100, 6183),
    Ta083("taillard93", "Ta083.txt", 100, 6252),
    Ta084("taillard93", "Ta084.txt", 100, 6254),
    Ta085("taillard93", "Ta085.txt", 100, 6270),
    Ta086("taillard93", "Ta086.txt", 100, 6311),
    Ta087("taillard93", "Ta087.txt", 100, 6223),
    Ta088("taillard93", "Ta088.txt", 100, 6367),
    Ta089("taillard93", "Ta089.txt", 100, 6246),
    Ta090("taillard93", "Ta090.txt", 100, 6404),
    Ta091("taillard93", "Ta091.txt", 200, 10862),
    Ta092("taillard93", "Ta092.txt", 200, 10480),
    Ta093("taillard93", "Ta093.txt", 200, 10922),
    Ta094("taillard93", "Ta094.txt", 200, 10889),
    Ta095("taillard93", "Ta095.txt", 200, 10524),
    Ta096("taillard93", "Ta096.txt", 200, 10329),
    Ta097("taillard93", "Ta097.txt", 200, 10854),
    Ta098("taillard93", "Ta098.txt", 200, 10730),
    Ta099("taillard93", "Ta099.txt", 200, 10438),
    Ta100("taillard93", "Ta100.txt", 200, 10675),
    Ta101("taillard93", "Ta101.txt", 200, 11158),
    Ta102("taillard93", "Ta102.txt", 200, 11160),
    Ta103("taillard93", "Ta103.txt", 200, 11281),
    Ta104("taillard93", "Ta104.txt", 200, 11275),
    Ta105("taillard93", "Ta105.txt", 200, 11259),
    Ta106("taillard93", "Ta106.txt", 200, 11176),
    Ta107("taillard93", "Ta107.txt", 200, 11337),
    Ta108("taillard93", "Ta108.txt", 200, 1131),
    Ta109("taillard93", "Ta109.txt", 200, 11146),
    Ta110("taillard93", "Ta110.txt", 200, 11284),
    Ta111("taillard93", "Ta111.txt", 500, 26040),
    Ta112("taillard93", "Ta112.txt", 500, 26500),
    Ta113("taillard93", "Ta113.txt", 500, 26371),
    Ta114("taillard93", "Ta114.txt", 500, 26456),
    Ta115("taillard93", "Ta115.txt", 500, 26334),
    Ta116("taillard93", "Ta116.txt", 500, 26469),
    Ta117("taillard93", "Ta117.txt", 500, 26389),
    Ta118("taillard93", "Ta118.txt", 500, 26560),
    Ta119("taillard93", "Ta119.txt", 500, 26005),
    Ta120("taillard93", "Ta120.txt", 500, 26457);

    private final String module;
    private final String filename;
    private final BigDecimal bestSolution;
    private final boolean large;

    FlowShopDataset(String module, String filename, int jobs, int bestSolution) {
        this.module = module;
        this.filename = filename;
        this.bestSolution = BigDecimal.valueOf(bestSolution);
        this.large = jobs >= 100;
    }

    @Override
    public BigDecimal getBestKnownSolution() {
        return bestSolution;
    }

    @Override
    public boolean isLarge() {
        return this.large;
    }

    @Override
    public boolean isBestKnownSolutionOptimal() {
        return true;
    }

    @Override
    public Path getPath() {
        return Path.of("data", "flowshop", "import", this.module, this.filename);
    }

}
