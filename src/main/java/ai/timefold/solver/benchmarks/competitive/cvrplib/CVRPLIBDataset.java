package ai.timefold.solver.benchmarks.competitive.cvrplib;

import java.math.BigDecimal;
import java.nio.file.Path;

import ai.timefold.solver.benchmarks.competitive.Dataset;

/**
 * The datasets come <a href="http://vrp.galgos.inf.puc-rio.br/index.php/en/">from here</a>.
 * The data on best known solutions were downloaded from the website on December 6, 2024.
 * <p>
 * Out of that, roughly 100 datasets were excluded because they require two-decimal precision,
 * but CVRP only supports integer or single-decimal precision.
 * Specifically, the old datasets require integer, and these newer ones suddenly switch to using double.
 * And they did not switch to the single-decimal double of CVRPTW, they switched to two-decimal double.
 * Supporting all three methods would be undesiredly complex.
 * The datasets in question are:
 * <ul>
 * <li>Golden et al.</li>
 * <li>Li et al.</li>
 * <li>Rochat and Taillard</li>
 * <li>Christofides, Mingozzi and Toth (1979)</li>
 * </ul>
 * <p>
 * Some datasets were removed for other reasons:
 * <ul>
 * <li>XML100; hundreds of very similar, seemingly randomly generated datasets.</li>
 * </ul>
 * <p>
 *
 */
public enum CVRPLIBDataset implements Dataset<CVRPLIBDataset> {

    // CVRP
    A_N32_K5("A-n32-k5.vrp", 784),
    A_N33_K5("A-n33-k5.vrp", 661),
    A_N33_K6("A-n33-k6.vrp", 742),
    A_N34_K5("A-n34-k5.vrp", 778),
    A_N36_K5("A-n36-k5.vrp", 799),
    A_N37_K5("A-n37-k5.vrp", 669),
    A_N37_K6("A-n37-k6.vrp", 949),
    A_N38_K5("A-n38-k5.vrp", 730),
    A_N39_K5("A-n39-k5.vrp", 822),
    A_N39_K6("A-n39-k6.vrp", 831),
    A_N44_K6("A-n44-k6.vrp", 937),
    A_N45_K6("A-n45-k6.vrp", 944),
    A_N45_K7("A-n45-k7.vrp", 1146),
    A_N46_K7("A-n46-k7.vrp", 914),
    A_N48_K7("A-n48-k7.vrp", 1073),
    A_N53_K7("A-n53-k7.vrp", 1010),
    A_N54_K7("A-n54-k7.vrp", 1167),
    A_N55_K9("A-n55-k9.vrp", 1073),
    A_N60_K9("A-n60-k9.vrp", 1354),
    A_N61_K9("A-n61-k9.vrp", 1034),
    A_N62_K8("A-n62-k8.vrp", 1288),
    A_N63_K10("A-n63-k10.vrp", 1314),
    A_N63_K9("A-n63-k9.vrp", 1616),
    A_N64_K9("A-n64-k9.vrp", 1401),
    A_N65_K9("A-n65-k9.vrp", 1174),
    A_N69_K9("A-n69-k9.vrp", 1159),
    A_N80_K10("A-n80-k10.vrp", 1763),
    B_N31_K5("B-n31-k5.vrp", 672),
    B_N34_K5("B-n34-k5.vrp", 788),
    B_N35_K5("B-n35-k5.vrp", 955),
    B_N38_K6("B-n38-k6.vrp", 805),
    B_N39_K5("B-n39-k5.vrp", 549),
    B_N41_K6("B-n41-k6.vrp", 829),
    B_N43_K6("B-n43-k6.vrp", 742),
    B_N44_K7("B-n44-k7.vrp", 909),
    B_N45_K5("B-n45-k5.vrp", 751),
    B_N45_K6("B-n45-k6.vrp", 678),
    B_N50_K7("B-n50-k7.vrp", 741),
    B_N50_K8("B-n50-k8.vrp", 1312),
    B_N51_K7("B-n51-k7.vrp", 1032),
    B_N52_K7("B-n52-k7.vrp", 747),
    B_N56_K7("B-n56-k7.vrp", 707),
    B_N57_K7("B-n57-k7.vrp", 1153),
    B_N57_K9("B-n57-k9.vrp", 1598),
    B_N63_K10("B-n63-k10.vrp", 1496),
    B_N64_K9("B-n64-k9.vrp", 861),
    B_N66_K9("B-n66-k9.vrp", 1316),
    B_N67_K10("B-n67-k10.vrp", 1032),
    B_N68_K9("B-n68-k9.vrp", 1272),
    B_N78_K10("B-n78-k10.vrp", 1221),
    E_N13_K4("E-n13-k4.vrp", 247),
    E_N22_K4("E-n22-k4.vrp", 375),
    E_N23_K3("E-n23-k3.vrp", 569),
    E_N30_K3("E-n30-k3.vrp", 534),
    E_N31_K7("E-n31-k7.vrp", 379),
    E_N33_K4("E-n33-k4.vrp", 835),
    E_N51_K5("E-n51-k5.vrp", 521),
    E_N76_K10("E-n76-k10.vrp", 830),
    E_N76_K14("E-n76-k14.vrp", 1021),
    E_N76_K7("E-n76-k7.vrp", 682),
    E_N76_K8("E-n76-k8.vrp", 735),
    E_N101_K8("E-n101-k8.vrp", 815),
    E_N101_K14("E-n101-k14.vrp", 1067),
    F_N45_K4("F-n45-k4.vrp", 724),
    F_N72_K4("F-n72-k4.vrp", 237),
    F_N135_K7("F-n135-k7.vrp", 1162),
    LOGGI_N401_K23("Loggi-n401-k23.vrp", 336903, false),
    LOGGI_N501_K24("Loggi-n501-k24.vrp", 177176, false),
    LOGGI_N601_K19("Loggi-n601-k19.vrp", 113155, false),
    LOGGI_N601_K42("Loggi-n601-k42.vrp", 347046, false),
    LOGGI_N901_K42("Loggi-n901-k42.vrp", 246301, false),
    LOGGI_N1001_K31("Loggi-n1001-k31.vrp", 284356, false),
    M_N101_K10("M-n101-k10.vrp", 820),
    M_N121_K7("M-n121-k7.vrp", 1034),
    M_N151_K12("M-n151-k12.vrp", 1015),
    M_N200_K16("M-n200-k16.vrp", 1274),
    M_N200_K17("M-n200-k17.vrp", 1275),
    ORTEC_N242_K12("ORTEC-n242-k12.vrp", 123750, false),
    ORTEC_N323_K21("ORTEC-n323-k21.vrp", 214071, false),
    ORTEC_N405_K18("ORTEC-n405-k18.vrp", 200986, false),
    ORTEC_N455_K41("ORTEC-n455-k41.vrp", 292485, false),
    ORTEC_N510_K23("ORTEC-n510-k23.vrp", 184529, false),
    ORTEC_N701_K64("ORTEC-n701-k64.vrp", 445541, false),
    P_N16_K8("P-n16-k8.vrp", 450),
    P_N19_K2("P-n19-k2.vrp", 212),
    P_N20_K2("P-n20-k2.vrp", 216),
    P_N21_K2("P-n21-k2.vrp", 211),
    P_N22_K2("P-n22-k2.vrp", 216),
    P_N22_K8("P-n22-k8.vrp", 603),
    P_N23_K8("P-n23-k8.vrp", 529),
    P_N40_K5("P-n40-k5.vrp", 458),
    P_N45_K5("P-n45-k5.vrp", 510),
    P_N50_K7("P-n50-k7.vrp", 554),
    P_N50_K8("P-n50-k8.vrp", 631),
    P_N50_K10("P-n50-k10.vrp", 696),
    P_N51_K10("P-n51-k10.vrp", 741),
    P_N55_K7("P-n55-k7.vrp", 568),
    P_N55_K8("P-n55-k8.vrp", 588, false), // Website doesn't even list this one.
    P_N55_K10("P-n55-k10.vrp", 694),
    P_N55_K15("P-n55-k15.vrp", 989),
    P_N60_K10("P-n60-k10.vrp", 744),
    P_N60_K15("P-n60-k15.vrp", 968),
    P_N65_K10("P-n65-k10.vrp", 792),
    P_N70_K10("P-n70-k10.vrp", 827),
    P_N76_K4("P-n76-k4.vrp", 593),
    P_N76_K5("P-n76-k5.vrp", 627),
    P_N101_K4("P-n101-k4.vrp", 681),
    X_N101_K25("X-n101-k25.vrp", 27591),
    X_N106_K14("X-n106-k14.vrp", 26362),
    X_N110_K13("X-n110-k13.vrp", 14971),
    X_N115_K10("X-n115-k10.vrp", 12747),
    X_N120_K6("X-n120-k6.vrp", 13332),
    X_N125_K30("X-n125-k30.vrp", 55539),
    X_N129_K18("X-n129-k18.vrp", 28940),
    X_N134_K13("X-n134-k13.vrp", 10916),
    X_N139_K10("X-n139-k10.vrp", 13590),
    X_N143_K7("X-n143-k7.vrp", 15700),
    X_N148_K46("X-n148-k46.vrp", 43448),
    X_N153_K22("X-n153-k22.vrp", 21220),
    X_N157_K13("X-n157-k13.vrp", 16876),
    X_N162_K11("X-n162-k11.vrp", 14138),
    X_N167_K10("X-n167-k10.vrp", 20557),
    X_N172_K51("X-n172-k51.vrp", 45607),
    X_N176_K26("X-n176-k26.vrp", 47812),
    X_N181_K23("X-n181-k23.vrp", 25569),
    X_N186_K15("X-n186-k15.vrp", 24145),
    X_N190_K8("X-n190-k8.vrp", 16980),
    X_N195_K51("X-n195-k51.vrp", 44225),
    X_N200_K36("X-n200-k36.vrp", 58578),
    X_N204_K19("X-n204-k19.vrp", 19565),
    X_N209_K16("X-n209-k16.vrp", 30656),
    X_N214_K11("X-n214-k11.vrp", 10856),
    X_N219_K73("X-n219-k73.vrp", 117595),
    X_N223_K34("X-n223-k34.vrp", 40437),
    X_N228_K23("X-n228-k23.vrp", 25742),
    X_N233_K16("X-n233-k16.vrp", 19230),
    X_N237_K14("X-n237-k14.vrp", 27042),
    X_N242_K48("X-n242-k48.vrp", 82751),
    X_N247_K50("X-n247-k50.vrp", 37274),
    X_N251_K28("X-n251-k28.vrp", 38684),
    X_N256_K16("X-n256-k16.vrp", 18839),
    X_N261_K13("X-n261-k13.vrp", 26558),
    X_N266_K58("X-n266-k58.vrp", 75478),
    X_N270_K35("X-n270-k35.vrp", 35291),
    X_N275_K28("X-n275-k28.vrp", 21245),
    X_N280_K17("X-n280-k17.vrp", 33503, false),
    X_N284_K15("X-n284-k15.vrp", 20226),
    X_N289_K60("X-n289-k60.vrp", 95151),
    X_N294_K50("X-n294-k50.vrp", 47161),
    X_N298_K31("X-n298-k31.vrp", 34231),
    X_N303_K21("X-n303-k21.vrp", 21736, false),
    X_N308_K13("X-n308-k13.vrp", 25859, false),
    X_N313_K71("X-n313-k71.vrp", 94043, false),
    X_N317_K53("X-n317-k53.vrp", 78355),
    X_N322_K28("X-n322-k28.vrp", 29834),
    X_N327_K20("X-n327-k20.vrp", 27532, false),
    X_N331_K15("X-n331-k15.vrp", 31102),
    X_N336_K84("X-n336-k84.vrp", 139111, false),
    X_N344_K43("X-n344-k43.vrp", 42050),
    X_N351_K40("X-n351-k40.vrp", 25896),
    X_N359_K29("X-n359-k29.vrp", 51505, false),
    X_N367_K17("X-n367-k17.vrp", 22814),
    X_N376_K94("X-n376-k94.vrp", 147713),
    X_N384_K52("X-n384-k52.vrp", 65940, false),
    X_N393_K38("X-n393-k38.vrp", 38260),
    X_N401_K29("X-n401-k29.vrp", 66154, false),
    X_N411_K19("X-n411-k19.vrp", 19712, false),
    X_N420_K130("X-n420-k130.vrp", 107798),
    X_N429_K61("X-n429-k61.vrp", 65449, false),
    X_N439_K37("X-n439-k37.vrp", 36391),
    X_N449_K29("X-n449-k29.vrp", 55233, false),
    X_N459_K26("X-n459-k26.vrp", 24139, false),
    X_N469_K138("X-n469-k138.vrp", 221824),
    X_N480_K70("X-n480-k70.vrp", 89449, false),
    X_N491_K59("X-n491-k59.vrp", 66483, false),
    X_N502_K39("X-n502-k39.vrp", 69226, false),
    X_N513_K21("X-n513-k21.vrp", 24201, false),
    X_N524_K153("X-n524-k153.vrp", 154593),
    X_N536_K96("X-n536-k96.vrp", 94846, false),
    X_N548_K50("X-n548-k50.vrp", 86700),
    X_N561_K42("X-n561-k42.vrp", 42717, false),
    X_N573_K30("X-n573-k30.vrp", 50673, false),
    X_N586_K159("X-n586-k159.vrp", 190316, false),
    X_N599_K92("X-n599-k92.vrp", 108451, false),
    X_N613_K62("X-n613-k62.vrp", 59535, false),
    X_N627_K43("X-n627-k43.vrp", 62164, false),
    X_N641_K35("X-n641-k35.vrp", 63684, false),
    X_N655_K131("X-n655-k131.vrp", 106780),
    X_N670_K130("X-n670-k130.vrp", 146332),
    X_N685_K75("X-n685-k75.vrp", 68205, false),
    X_N701_K44("X-n701-k44.vrp", 81923, false),
    X_N716_K35("X-n716-k35.vrp", 43373, false),
    X_N733_K159("X-n733-k159.vrp", 136187, false),
    X_N749_K98("X-n749-k98.vrp", 77269, false),
    X_N766_K71("X-n766-k71.vrp", 114417, false),
    X_N783_K48("X-n783-k48.vrp", 72386, false),
    X_N801_K40("X-n801-k40.vrp", 73311, false),
    X_N819_K171("X-n819-k171.vrp", 158121, false),
    X_N837_K142("X-n837-k142.vrp", 193737, false),
    X_N856_K95("X-n856-k95.vrp", 88965),
    X_N876_K59("X-n876-k59.vrp", 99299, false),
    X_N895_K37("X-n895-k37.vrp", 53860, false),
    X_N916_K207("X-n916-k207.vrp", 329179, false),
    X_N936_K151("X-n936-k151.vrp", 132715, false),
    X_N957_K87("X-n957-k87.vrp", 85465, false),
    X_N979_K58("X-n979-k58.vrp", 118976, false),
    X_N1001_K43("X-n1001-k43.vrp", 72355, false),
    // CVRP Arnold, Gendreau and Sörensen (2017); k decided by rounding the BKS number of vehicles up to nearest 50.
    ANTWERP1("Antwerp1-n6000-k350.vrp", 477277, false, true),
    ANTWERP2("Antwerp2-n7000-k150.vrp", 291350, false, true),
    BRUSSELS1("Brussels1-n15000-k550.vrp", 501719, false, true),
    BRUSSELS2("Brussels2-n16000-k200.vrp", 345468, false, true),
    FLANDERS1("Flanders1-n20000-k700.vrp", 7240118, false, true),
    FLANDERS2("Flanders2-n30000-k300.vrp", 4373244, false, true),
    GHENT1("Ghent1-n10000-k500.vrp", 469531, false, true),
    GHENT2("Ghent2-n11000-k150.vrp", 257748, false, true),
    LEUVEN1("Leuven1-n3000-k250.vrp", 192848, false, true),
    LEUVEN2("Leuven2-n4000-k50.vrp", 111391, false, true),
    // CVRPTW Solomon
    C101("C101.txt", 827.3),
    C102("C102.txt", 827.3),
    C103("C103.txt", 826.3),
    C104("C104.txt", 822.9),
    C105("C105.txt", 827.3),
    C106("C106.txt", 827.3),
    C107("C107.txt", 827.3),
    C108("C108.txt", 827.3),
    C109("C109.txt", 827.3),
    C201("C201.txt", 589.1),
    C202("C202.txt", 589.1),
    C203("C203.txt", 588.7),
    C204("C204.txt", 588.1),
    C205("C205.txt", 586.4),
    C206("C206.txt", 586),
    C207("C207.txt", 585.8),
    C208("C208.txt", 585.8),
    R101("R101.txt", 1637.7),
    R102("R102.txt", 1466.6),
    R103("R103.txt", 1208.7),
    R104("R104.txt", 971.5),
    R105("R105.txt", 1355.3),
    R106("R106.txt", 1234.6),
    R107("R107.txt", 1064.6),
    R108("R108.txt", 932.1),
    R109("R109.txt", 1146.9),
    R110("R110.txt", 1068),
    R111("R111.txt", 1048.7),
    R112("R112.txt", 948.6),
    R201("R201.txt", 1143.2),
    R202("R202.txt", 1029.6),
    R203("R203.txt", 870.8),
    R204("R204.txt", 731.3),
    R205("R205.txt", 949.8),
    R206("R206.txt", 875.9),
    R207("R207.txt", 794),
    R208("R208.txt", 701),
    R209("R209.txt", 854.8),
    R210("R210.txt", 900.5),
    R211("R211.txt", 746.7),
    RC101("RC101.txt", 1619.8),
    RC102("RC102.txt", 1457.4),
    RC103("RC103.txt", 1258),
    RC104("RC104.txt", 1132.3),
    RC105("RC105.txt", 1513.7),
    RC106("RC106.txt", 1372.7),
    RC107("RC107.txt", 1207.8),
    RC108("RC108.txt", 1114.2),
    RC201("RC201.txt", 1261.8),
    RC202("RC202.txt", 1092.3),
    RC203("RC203.txt", 923.7),
    RC204("RC204.txt", 783.5),
    RC205("RC205.txt", 1154),
    RC206("RC206.txt", 1051.1),
    RC207("RC207.txt", 962.9),
    RC208("RC208.txt", 776.1),
    // CVRPTW Homberger
    C1_2_1("C1_2_1.txt", 2698.6),
    C1_2_2("C1_2_2.txt", 2694.3),
    C1_2_3("C1_2_3.txt", 2675.8),
    C1_2_4("C1_2_4.txt", 2625.6),
    C1_2_5("C1_2_5.txt", 2694.9),
    C1_2_6("C1_2_6.txt", 2694.9),
    C1_2_7("C1_2_7.txt", 2694.9),
    C1_2_8("C1_2_8.txt", 2684),
    C1_2_9("C1_2_9.txt", 2639.6),
    C1_2_10("C1_2_10.txt", 2624.7),
    C1_4_1("C1_4_1.txt", 7138.8),
    C1_4_2("C1_4_2.txt", 7113.3),
    C1_4_3("C1_4_3.txt", 6929.9),
    C1_4_4("C1_4_4.txt", 6777.7),
    C1_4_5("C1_4_5.txt", 7138.8),
    C1_4_6("C1_4_6.txt", 7140.1),
    C1_4_7("C1_4_7.txt", 7136.2),
    C1_4_8("C1_4_8.txt", 7083),
    C1_4_9("C1_4_9.txt", 6927.8),
    C1_4_10("C1_4_10.txt", 6825.4),
    C1_6_1("C1_6_1.txt", 14076.6, false),
    C1_6_2("C1_6_2.txt", 13948.3, false),
    C1_6_3("C1_6_3.txt", 13756.5, false),
    C1_6_4("C1_6_4.txt", 13538.6, false),
    C1_6_5("C1_6_5.txt", 14066.8, false),
    C1_6_6("C1_6_6.txt", 14070.9, false),
    C1_6_7("C1_6_7.txt", 14066.8, false),
    C1_6_8("C1_6_8.txt", 13991.2, false),
    C1_6_9("C1_6_9.txt", 13664.5, false),
    C1_6_10("C1_6_10.txt", 13617.5, false),
    C1_8_1("C1_8_1.txt", 25156.9),
    C1_8_2("C1_8_2.txt", 24974.1, false),
    C1_8_3("C1_8_3.txt", 24156.1, false),
    C1_8_4("C1_8_4.txt", 23797.3, false),
    C1_8_5("C1_8_5.txt", 25138.6),
    C1_8_6("C1_8_6.txt", 25133.3),
    C1_8_7("C1_8_7.txt", 25127.3),
    C1_8_8("C1_8_8.txt", 24809.7, false),
    C1_8_9("C1_8_9.txt", 24200.4, false),
    C1_8_10("C1_8_10.txt", 24026.7, false),
    C1_10_1("C1_10_1.txt", 42444),
    C1_10_2("C1_10_2.txt", 41337, false),
    C1_10_3("C1_10_3.txt", 40060, false),
    C1_10_4("C1_10_4.txt", 39434, false),
    C1_10_5("C1_10_5.txt", 42434),
    C1_10_6("C1_10_6.txt", 42437),
    C1_10_7("C1_10_7.txt", 42420, false),
    C1_10_8("C1_10_8.txt", 41648, false),
    C1_10_9("C1_10_9.txt", 40288, false),
    C1_10_10("C1_10_10.txt", 39816, false),
    C2_2_1("C2_2_1.txt", 1922.1),
    C2_2_2("C2_2_2.txt", 1851.4),
    C2_2_3("C2_2_3.txt", 1763.4),
    C2_2_4("C2_2_4.txt", 1695),
    C2_2_5("C2_2_5.txt", 1869.6),
    C2_2_6("C2_2_6.txt", 1844.8),
    C2_2_7("C2_2_7.txt", 1842.2),
    C2_2_8("C2_2_8.txt", 1813.7),
    C2_2_9("C2_2_9.txt", 1815),
    C2_2_10("C2_2_10.txt", 1791.2),
    C2_4_1("C2_4_1.txt", 4100.3),
    C2_4_2("C2_4_2.txt", 3914.1),
    C2_4_3("C2_4_3.txt", 3755.2, false),
    C2_4_4("C2_4_4.txt", 3523.7, false),
    C2_4_5("C2_4_5.txt", 3923.2),
    C2_4_6("C2_4_6.txt", 3860.1),
    C2_4_7("C2_4_7.txt", 3870.9),
    C2_4_8("C2_4_8.txt", 3773.7),
    C2_4_9("C2_4_9.txt", 3842.1, false),
    C2_4_10("C2_4_10.txt", 3665.1, false),
    C2_6_1("C2_6_1.txt", 7752.2),
    C2_6_2("C2_6_2.txt", 7471.5),
    C2_6_3("C2_6_3.txt", 7215),
    C2_6_4("C2_6_4.txt", 6877),
    C2_6_5("C2_6_5.txt", 7553.8),
    C2_6_6("C2_6_6.txt", 7449.8),
    C2_6_7("C2_6_7.txt", 7491.3),
    C2_6_8("C2_6_8.txt", 7303.7),
    C2_6_9("C2_6_9.txt", 7303.2),
    C2_6_10("C2_6_10.txt", 7123.9),
    C2_8_1("C2_8_1.txt", 11631.9, false),
    C2_8_2("C2_8_2.txt", 11394.5, false),
    C2_8_3("C2_8_3.txt", 11138.1, false),
    C2_8_4("C2_8_4.txt", 10639.6, false),
    C2_8_5("C2_8_5.txt", 11395.6, false),
    C2_8_6("C2_8_6.txt", 11316.3, false),
    C2_8_7("C2_8_7.txt", 11332.9, false),
    C2_8_8("C2_8_8.txt", 11133.9, false),
    C2_8_9("C2_8_9.txt", 11140.4, false),
    C2_8_10("C2_8_10.txt", 10946, false),
    C2_10_1("C2_10_1.txt", 16841.1),
    C2_10_2("C2_10_2.txt", 16462.6, false),
    C2_10_3("C2_10_3.txt", 16036.5, false),
    C2_10_4("C2_10_4.txt", 15459.5, false),
    C2_10_5("C2_10_5.txt", 16521.3, false),
    C2_10_6("C2_10_6.txt", 16290.7, false),
    C2_10_7("C2_10_7.txt", 16378.4, false),
    C2_10_8("C2_10_8.txt", 16029.1, false),
    C2_10_9("C2_10_9.txt", 16075.4, false),
    C2_10_10("C2_10_10.txt", 15728.6, false),
    R1_2_1("R1_2_1.txt", 4667.2),
    R1_2_2("R1_2_2.txt", 3919.9),
    R1_2_3("R1_2_3.txt", 3373.9),
    R1_2_4("R1_2_4.txt", 3047.6),
    R1_2_5("R1_2_5.txt", 4053.2),
    R1_2_6("R1_2_6.txt", 3559.1),
    R1_2_7("R1_2_7.txt", 3141.9),
    R1_2_8("R1_2_8.txt", 2938.4),
    R1_2_9("R1_2_9.txt", 3734.7),
    R1_2_10("R1_2_10.txt", 3293.1),
    R1_4_1("R1_4_1.txt", 10305.8),
    R1_4_2("R1_4_2.txt", 8873.2),
    R1_4_3("R1_4_3.txt", 7781.6, false),
    R1_4_4("R1_4_4.txt", 7266.2, false),
    R1_4_5("R1_4_5.txt", 9184.6),
    R1_4_6("R1_4_6.txt", 8340.4),
    R1_4_7("R1_4_7.txt", 7599.8, false),
    R1_4_8("R1_4_8.txt", 7240.5, false),
    R1_4_9("R1_4_9.txt", 8673.8, false),
    R1_4_10("R1_4_10.txt", 8077.8, false),
    R1_6_1("R1_6_1.txt", 21274.2, false),
    R1_6_2("R1_6_2.txt", 18519.8, false),
    R1_6_3("R1_6_3.txt", 16874.9, false),
    R1_6_4("R1_6_4.txt", 15720.8, false),
    R1_6_5("R1_6_5.txt", 19294.9, false),
    R1_6_6("R1_6_6.txt", 17763.7, false),
    R1_6_7("R1_6_7.txt", 16496.2, false),
    R1_6_8("R1_6_8.txt", 15584.3, false),
    R1_6_9("R1_6_9.txt", 18474.1, false),
    R1_6_10("R1_6_10.txt", 17583.7, false),
    R1_8_1("R1_8_1.txt", 36345, false),
    R1_8_2("R1_8_2.txt", 32277.6, false),
    R1_8_3("R1_8_3.txt", 29301.2, false),
    R1_8_4("R1_8_4.txt", 27734.7, false),
    R1_8_5("R1_8_5.txt", 33494, false),
    R1_8_6("R1_8_6.txt", 30872.4, false),
    R1_8_7("R1_8_7.txt", 28789, false),
    R1_8_8("R1_8_8.txt", 27609.4, false),
    R1_8_9("R1_8_9.txt", 32257.3, false),
    R1_8_10("R1_8_10.txt", 30918.3, false),
    R1_10_1("R1_10_1.txt", 53026.1, false),
    R1_10_2("R1_10_2.txt", 48261.6, false),
    R1_10_3("R1_10_3.txt", 44673.3, false),
    R1_10_4("R1_10_4.txt", 42440.7, false),
    R1_10_5("R1_10_5.txt", 50406.7, false),
    R1_10_6("R1_10_6.txt", 46928.2, false),
    R1_10_7("R1_10_7.txt", 43997.4, false),
    R1_10_8("R1_10_8.txt", 42279.3, false),
    R1_10_9("R1_10_9.txt", 49162.8, false),
    R1_10_10("R1_10_10.txt", 47364.6, false),
    R2_2_1("R2_2_1.txt", 3468),
    R2_2_2("R2_2_2.txt", 3008.2),
    R2_2_3("R2_2_3.txt", 2537.5),
    R2_2_4("R2_2_4.txt", 1928.5),
    R2_2_5("R2_2_5.txt", 3061.1),
    R2_2_6("R2_2_6.txt", 2675.4),
    R2_2_7("R2_2_7.txt", 2304.7),
    R2_2_8("R2_2_8.txt", 1842.4),
    R2_2_9("R2_2_9.txt", 2843.3),
    R2_2_10("R2_2_10.txt", 2549.4),
    R2_4_1("R2_4_1.txt", 7520.7),
    R2_4_2("R2_4_2.txt", 6482.9),
    R2_4_3("R2_4_3.txt", 5372.2),
    R2_4_4("R2_4_4.txt", 4211.9, false),
    R2_4_5("R2_4_5.txt", 6567.9, false),
    R2_4_6("R2_4_6.txt", 5813.3, false),
    R2_4_7("R2_4_7.txt", 4893, false),
    R2_4_8("R2_4_8.txt", 4000.5, false),
    R2_4_9("R2_4_9.txt", 6067.2),
    R2_4_10("R2_4_10.txt", 5638.1, false),
    R2_6_1("R2_6_1.txt", 15145.3, false),
    R2_6_2("R2_6_2.txt", 12976.3, false),
    R2_6_3("R2_6_3.txt", 10455.3, false),
    R2_6_4("R2_6_4.txt", 7914.5, false),
    R2_6_5("R2_6_5.txt", 13790.2, false),
    R2_6_6("R2_6_6.txt", 11847.8, false),
    R2_6_7("R2_6_7.txt", 9770.3, false),
    R2_6_8("R2_6_8.txt", 7512.3, false),
    R2_6_9("R2_6_9.txt", 12736.8, false),
    R2_6_10("R2_6_10.txt", 11837, false),
    R2_8_1("R2_8_1.txt", 24963.8, false),
    R2_8_2("R2_8_2.txt", 21312.1, false),
    R2_8_3("R2_8_3.txt", 17229.7, false),
    R2_8_4("R2_8_4.txt", 13152.2, false),
    R2_8_5("R2_8_5.txt", 22795.9, false),
    R2_8_6("R2_8_6.txt", 19740.7, false),
    R2_8_7("R2_8_7.txt", 16351, false),
    R2_8_8("R2_8_8.txt", 12611, false),
    R2_8_9("R2_8_9.txt", 21282.6, false),
    R2_8_10("R2_8_10.txt", 19964.2, false),
    R2_10_1("R2_10_1.txt", 36881, false),
    R2_10_2("R2_10_2.txt", 31241.9, false),
    R2_10_3("R2_10_3.txt", 24399, false),
    R2_10_4("R2_10_4.txt", 17811.4, false),
    R2_10_5("R2_10_5.txt", 34132.8, false),
    R2_10_6("R2_10_6.txt", 29124.7, false),
    R2_10_7("R2_10_7.txt", 23102.2, false),
    R2_10_8("R2_10_8.txt", 17403.7, false),
    R2_10_9("R2_10_9.txt", 31990.6, false),
    R2_10_10("R2_10_10.txt", 29840.5, false),
    RC1_2_1("RC1_2_1.txt", 3516.9),
    RC1_2_2("RC1_2_2.txt", 3221.6),
    RC1_2_3("RC1_2_3.txt", 3001.4),
    RC1_2_4("RC1_2_4.txt", 2845.2),
    RC1_2_5("RC1_2_5.txt", 3325.6),
    RC1_2_6("RC1_2_6.txt", 3300.7),
    RC1_2_7("RC1_2_7.txt", 3177.8),
    RC1_2_8("RC1_2_8.txt", 3060),
    RC1_2_9("RC1_2_9.txt", 3073.3),
    RC1_2_10("RC1_2_10.txt", 2990.5),
    RC1_4_1("RC1_4_1.txt", 8522.9),
    RC1_4_2("RC1_4_2.txt", 7878.2),
    RC1_4_3("RC1_4_3.txt", 7516.9, false),
    RC1_4_4("RC1_4_4.txt", 7292.9, false),
    RC1_4_5("RC1_4_5.txt", 8152.3, false),
    RC1_4_6("RC1_4_6.txt", 8148, false),
    RC1_4_7("RC1_4_7.txt", 7932.5, false),
    RC1_4_8("RC1_4_8.txt", 7757.2, false),
    RC1_4_9("RC1_4_9.txt", 7717.7, false),
    RC1_4_10("RC1_4_10.txt", 7581.2, false),
    RC1_6_1("RC1_6_1.txt", 16944.2, false),
    RC1_6_2("RC1_6_2.txt", 15890.6, false),
    RC1_6_3("RC1_6_3.txt", 15181.3, false),
    RC1_6_4("RC1_6_4.txt", 14753.2, false),
    RC1_6_5("RC1_6_5.txt", 16536.3, false),
    RC1_6_6("RC1_6_6.txt", 16473.3, false),
    RC1_6_7("RC1_6_7.txt", 16055.3, false),
    RC1_6_8("RC1_6_8.txt", 15891.8, false),
    RC1_6_9("RC1_6_9.txt", 15803.5, false),
    RC1_6_10("RC1_6_10.txt", 15651.3, false),
    RC1_8_1("RC1_8_1.txt", 29952.8, false),
    RC1_8_2("RC1_8_2.txt", 28290.1, false),
    RC1_8_3("RC1_8_3.txt", 27447.7, false),
    RC1_8_4("RC1_8_4.txt", 26557.2, false),
    RC1_8_5("RC1_8_5.txt", 29219.9, false),
    RC1_8_6("RC1_8_6.txt", 29148.7, false),
    RC1_8_7("RC1_8_7.txt", 28734, false),
    RC1_8_8("RC1_8_8.txt", 28390, false),
    RC1_8_9("RC1_8_9.txt", 28331.6, false),
    RC1_8_10("RC1_8_10.txt", 28168.5, false),
    RC1_10_1("RC1_10_1.txt", 45790.7, false),
    RC1_10_2("RC1_10_2.txt", 43678.3, false),
    RC1_10_3("RC1_10_3.txt", 42121.9, false),
    RC1_10_4("RC1_10_4.txt", 41357.4, false),
    RC1_10_5("RC1_10_5.txt", 45028.1, false),
    RC1_10_6("RC1_10_6.txt", 44898.2, false),
    RC1_10_7("RC1_10_7.txt", 44409, false),
    RC1_10_8("RC1_10_8.txt", 43916.5, false),
    RC1_10_9("RC1_10_9.txt", 43858, false),
    RC1_10_10("RC1_10_10.txt", 43533.7, false),
    RC2_2_1("RC2_2_1.txt", 2797.4),
    RC2_2_2("RC2_2_2.txt", 2481.6),
    RC2_2_3("RC2_2_3.txt", 2227.7),
    RC2_2_4("RC2_2_4.txt", 1854.8),
    RC2_2_5("RC2_2_5.txt", 2491.4),
    RC2_2_6("RC2_2_6.txt", 2495.1),
    RC2_2_7("RC2_2_7.txt", 2287.7),
    RC2_2_8("RC2_2_8.txt", 2151.2),
    RC2_2_9("RC2_2_9.txt", 2086.6),
    RC2_2_10("RC2_2_10.txt", 1989.2),
    RC2_4_1("RC2_4_1.txt", 6147.3),
    RC2_4_2("RC2_4_2.txt", 5407.5),
    RC2_4_3("RC2_4_3.txt", 4573),
    RC2_4_4("RC2_4_4.txt", 3597.9, false),
    RC2_4_5("RC2_4_5.txt", 5392.3, false),
    RC2_4_6("RC2_4_6.txt", 5324.6, false),
    RC2_4_7("RC2_4_7.txt", 4987.8, false),
    RC2_4_8("RC2_4_8.txt", 4693.3, false),
    RC2_4_9("RC2_4_9.txt", 4510.4, false),
    RC2_4_10("RC2_4_10.txt", 4252.3, false),
    RC2_6_1("RC2_6_1.txt", 11966.1, false),
    RC2_6_2("RC2_6_2.txt", 10336.9, false),
    RC2_6_3("RC2_6_3.txt", 8894.9, false),
    RC2_6_4("RC2_6_4.txt", 6967.5, false),
    RC2_6_5("RC2_6_5.txt", 11080.7, false),
    RC2_6_6("RC2_6_6.txt", 10830.5, false),
    RC2_6_7("RC2_6_7.txt", 10289.4, false),
    RC2_6_8("RC2_6_8.txt", 9779, false),
    RC2_6_9("RC2_6_9.txt", 9436, false),
    RC2_6_10("RC2_6_10.txt", 8973.3, false),
    RC2_8_1("RC2_8_1.txt", 19201.3, false),
    RC2_8_2("RC2_8_2.txt", 16709.5, false),
    RC2_8_3("RC2_8_3.txt", 14013.6, false),
    RC2_8_4("RC2_8_4.txt", 10969.4, false),
    RC2_8_5("RC2_8_5.txt", 17466.1, false),
    RC2_8_6("RC2_8_6.txt", 17190.6, false),
    RC2_8_7("RC2_8_7.txt", 16362.2, false),
    RC2_8_8("RC2_8_8.txt", 15528.8, false),
    RC2_8_9("RC2_8_9.txt", 15177.2, false),
    RC2_8_10("RC2_8_10.txt", 14370.9, false),
    RC2_10_1("RC2_10_1.txt", 28122.6, false),
    RC2_10_2("RC2_10_2.txt", 24248.6, false),
    RC2_10_3("RC2_10_3.txt", 19618.1, false),
    RC2_10_4("RC2_10_4.txt", 15654.7, false),
    RC2_10_5("RC2_10_5.txt", 25797.5, false),
    RC2_10_6("RC2_10_6.txt", 25782.5, false),
    RC2_10_7("RC2_10_7.txt", 24391.4, false),
    RC2_10_8("RC2_10_8.txt", 23279.8, false),
    RC2_10_9("RC2_10_9.txt", 22731.6, false),
    RC2_10_10("RC2_10_10.txt", 21731.2, false);

    private final String filename;
    private final BigDecimal bestKnownDistance;
    private final boolean bestKnownDistanceOptimal;
    private final boolean large;

    CVRPLIBDataset(String filename, int bestKnownDistance) {
        this(filename, bestKnownDistance, true);
    }

    CVRPLIBDataset(String filename, int bestKnownDistance, boolean bestKnownDistanceOptimal) {
        this(filename, bestKnownDistance, bestKnownDistanceOptimal, false);
    }

    CVRPLIBDataset(String filename, double bestKnownDistance) {
        this(filename, bestKnownDistance, true);
    }

    CVRPLIBDataset(String filename, double bestKnownDistance, boolean bestKnownDistanceOptimal) {
        this(filename, bestKnownDistance, bestKnownDistanceOptimal, false);
    }

    CVRPLIBDataset(String filename, double bestKnownDistance, boolean bestKnownDistanceOptimal, boolean large) {
        this.filename = filename;
        this.bestKnownDistance = BigDecimal.valueOf(bestKnownDistance);
        this.bestKnownDistanceOptimal = bestKnownDistanceOptimal;
        this.large = large;
    }

    @Override
    public BigDecimal getBestKnownDistance() {
        return bestKnownDistance;
    }

    @Override
    public boolean isLarge() {
        return large;
    }

    @Override
    public boolean isBestKnownDistanceOptimal() {
        return bestKnownDistanceOptimal;
    }

    public boolean isTimeWindowed() {
        return this.filename.endsWith(".txt");
    }

    @Override
    public Path getPath() {
        return Path.of("data", "vehiclerouting", "import", this.filename);
    }

}
