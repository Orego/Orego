package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.OFF_BOARD_COLOR;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.patterns.Pattern.diagramToNeighborhood;
import orego.patterns.ColorSpecificPattern;
import orego.patterns.Cut1Pattern;
import orego.patterns.Pattern;
import orego.patterns.SimplePattern;
import orego.util.BitVector;

public abstract class AbstractPatternHeuristic extends Heuristic {

	/**
	 * The number of total patterns, including impossible ones.
	 */
	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;
	
	public static final BitVector[] BAD_NEIGHBORHOODS = {
		new BitVector(NUMBER_OF_NEIGHBORHOODS),
		new BitVector(NUMBER_OF_NEIGHBORHOODS) };
	
	public static final BitVector[] GOOD_NEIGHBORHOODS = {
		new BitVector(NUMBER_OF_NEIGHBORHOODS),
		new BitVector(NUMBER_OF_NEIGHBORHOODS) };
	
	/**
	 * Used by isPossibleNeighborhood().
	 */
	public static final char[] VALID_OFF_BOARD_PATTERNS = {
				diagramToNeighborhood("...\n. .\n..."),
				diagramToNeighborhood("*..\n* .\n*.."),
				diagramToNeighborhood("..*\n. *\n..*"),
				diagramToNeighborhood("***\n. .\n..."),
				diagramToNeighborhood("...\n. .\n***"),
				diagramToNeighborhood("***\n* .\n*.."),
				diagramToNeighborhood("***\n. *\n..*"),
				diagramToNeighborhood("*..\n* .\n***"),
				diagramToNeighborhood("..*\n. *\n***") };

	/**
	 * Set of 3x3 patterns taken from Gelly et al,
	 * "Modification of UCT with Patterns in Monte-Carlo Go"
	 * 
	 * @see orego.core.Coordinates#NEIGHBORS
	 */
	static {
		String[] colorSpecificPatterns = {
				"O...#O??", // Hane4
				"#??*?O**", // Edge3
				"O?+*?#**", // Edge4
				"O#O*?#**", // Edge5	
		};
		Pattern[] BLACK_GOOD_PATTERNS = new Pattern[colorSpecificPatterns.length];
		Pattern[] WHITE_GOOD_PATTERNS = new Pattern[colorSpecificPatterns.length];
		for (int i = 0; i < BLACK_GOOD_PATTERNS.length; i++) {
			BLACK_GOOD_PATTERNS[i] = new ColorSpecificPattern(colorSpecificPatterns[i], BLACK);
			WHITE_GOOD_PATTERNS[i] = new ColorSpecificPattern(colorSpecificPatterns[i], WHITE);
		}
		String[] colorIndependentPatterns = {
				"O..?##??", // Hane1
				"O...#.??", // Hane2
				"O#..#???", // Hane3
				"#OO+??++", // Cut2
				".O?*#?**", // Edge1
				"#oO*??**", // Edge2				
		};
		Pattern[] INDEPENDENT_GOOD_PATTERNS = new Pattern[colorIndependentPatterns.length + 1];			
		for (int i = 0; i < colorIndependentPatterns.length; i++) {
			INDEPENDENT_GOOD_PATTERNS[i] = new SimplePattern(colorIndependentPatterns[i]);
		}
		INDEPENDENT_GOOD_PATTERNS[INDEPENDENT_GOOD_PATTERNS.length - 1] = new Cut1Pattern();
		String[] badPatterns = {
//				"###O##O#", // Seen:19860 Played:1578 Ratio:0.07945619335347431 Min Turn:21 Max Turn:401 Ave Turn:5378.571752265861
//				"#O##.##O", // Seen:10098 Played:790 Ratio:0.07823331352743118 Min Turn:15 Max Turn:379 Ave Turn:2012.9892057833235
//				"...*##**", // Seen:103139 Played:7958 Ratio:0.07715801006408828 Min Turn:7 Max Turn:425 Ave Turn:19714.12944666906
//				"..O*..**", // Seen:46493 Played:3586 Ratio:0.07712989052115372 Min Turn:3 Max Turn:377 Ave Turn:3071.3331469253435
//				"#O##.#O.", // Seen:10023 Played:771 Ratio:0.07692307692307693 Min Turn:13 Max Turn:389 Ave Turn:3237.9032225880474
//				"###OO###", // Seen:19060 Played:1466 Ratio:0.07691500524658972 Min Turn:17 Max Turn:397 Ave Turn:2828.1089716684155
//				"..O...#O", // Seen:116119 Played:8878 Ratio:0.07645604939760074 Min Turn:5 Max Turn:417 Ave Turn:3949.558521861194
//				"O#O#..##", // Seen:14818 Played:1132 Ratio:0.07639357538129302 Min Turn:13 Max Turn:369 Ave Turn:2791.6464435146445
//				"O*.O*.*.", // Seen:3145 Played:240 Ratio:0.07631160572337042 Min Turn:27 Max Turn:425 Ave Turn:10356.423211446741
//				"...O#.#O", // Seen:53074 Played:4049 Ratio:0.07628970870859554 Min Turn:7 Max Turn:363 Ave Turn:2022.5982213513207
//				"#OOO.O.O", // Seen:14708 Played:1122 Ratio:0.07628501495784606 Min Turn:15 Max Turn:367 Ave Turn:3057.0566358444385
//				"OOO#.O#.", // Seen:11173 Played:851 Ratio:0.07616575673498613 Min Turn:11 Max Turn:353 Ave Turn:2815.0775977803632
//				".#O..#.O", // Seen:50783 Played:3852 Ratio:0.07585215524880373 Min Turn:5 Max Turn:339 Ave Turn:1846.7639761337455
//				"*O#.**.#", // Seen:2022 Played:153 Ratio:0.07566765578635015 Min Turn:17 Max Turn:377 Ave Turn:2061.2987141444114
//				"#.OO.##O", // Seen:11036 Played:834 Ratio:0.07557085900688655 Min Turn:13 Max Turn:357 Ave Turn:2210.582004349402
//				"O.OOO.#O", // Seen:8040 Played:606 Ratio:0.07537313432835821 Min Turn:17 Max Turn:433 Ave Turn:2922.5119402985074
//				"O#O#OO..", // Seen:14562 Played:1091 Ratio:0.07492102733141053 Min Turn:15 Max Turn:385 Ave Turn:3024.422881472325
//				"#.OO....", // Seen:95410 Played:7127 Ratio:0.07469866890263076 Min Turn:5 Max Turn:375 Ave Turn:2367.4976312755475
//				"##.OO###", // Seen:14520 Played:1079 Ratio:0.07431129476584022 Min Turn:17 Max Turn:365 Ave Turn:1661.071349862259
//				"O.##O##.", // Seen:25773 Played:1913 Ratio:0.07422496410972723 Min Turn:13 Max Turn:377 Ave Turn:5085.764327008886
//				"##O##O..", // Seen:13080 Played:963 Ratio:0.07362385321100917 Min Turn:13 Max Turn:381 Ave Turn:2879.183027522936
//				"#.#*O.**", // Seen:58498 Played:4291 Ratio:0.07335293514308182 Min Turn:9 Max Turn:399 Ave Turn:6591.543710896099
//				"O..###.#", // Seen:18314 Played:1337 Ratio:0.07300425903680245 Min Turn:11 Max Turn:385 Ave Turn:1615.2870481598777
//				".O#.#.O#", // Seen:19527 Played:1422 Ratio:0.07282224612075587 Min Turn:13 Max Turn:359 Ave Turn:2256.853177651457
//				"..#.#.#.", // Seen:43334 Played:3150 Ratio:0.07269118936631744 Min Turn:7 Max Turn:385 Ave Turn:6618.254234550238
//				"O#...O.O", // Seen:42993 Played:3108 Ratio:0.07229083804340242 Min Turn:7 Max Turn:339 Ave Turn:1638.6000279115206
//				"#..O####", // Seen:11122 Played:803 Ratio:0.07219924474015464 Min Turn:11 Max Turn:367 Ave Turn:3980.262812443805
//				".#OO.#OO", // Seen:14125 Played:1019 Ratio:0.07214159292035398 Min Turn:13 Max Turn:355 Ave Turn:2517.762477876106
//				".O#*##**", // Seen:5829 Played:420 Ratio:0.07205352547606794 Min Turn:27 Max Turn:381 Ave Turn:1710.875278778521
//				"##.#.#O#", // Seen:47141 Played:3394 Ratio:0.07199677563055515 Min Turn:13 Max Turn:393 Ave Turn:6980.91340871004
//				"#.#.#.#O", // Seen:12294 Played:882 Ratio:0.07174231332357248 Min Turn:13 Max Turn:383 Ave Turn:2222.4293151130632
//				"#..#.##O", // Seen:28282 Played:2024 Ratio:0.0715649529736228 Min Turn:13 Max Turn:369 Ave Turn:3019.5545223110107
//				"O..OO..O", // Seen:24632 Played:1737 Ratio:0.07051802533290029 Min Turn:11 Max Turn:385 Ave Turn:7590.4183582331925
//				"##O#.###", // Seen:15062 Played:1050 Ratio:0.0697118576550259 Min Turn:19 Max Turn:407 Ave Turn:4547.810250962688
//				"#.###OO.", // Seen:39135 Played:2698 Ratio:0.06894084579021337 Min Turn:9 Max Turn:375 Ave Turn:3774.627264596908
//				"#O#..###", // Seen:7415 Played:511 Ratio:0.0689143627781524 Min Turn:19 Max Turn:379 Ave Turn:2611.5585974376263
//				"###.#OO#", // Seen:19292 Played:1328 Ratio:0.06883682355380469 Min Turn:15 Max Turn:377 Ave Turn:4038.0292867509847
//				"OOO..O#O", // Seen:9794 Played:673 Ratio:0.06871554012660813 Min Turn:15 Max Turn:395 Ave Turn:3117.232795589136
//				"O..OO#O.", // Seen:37159 Played:2529 Ratio:0.06805888210124061 Min Turn:9 Max Turn:379 Ave Turn:3760.7788691837777
//				"OOOO#..O", // Seen:18450 Played:1252 Ratio:0.06785907859078591 Min Turn:11 Max Turn:389 Ave Turn:8940.708346883468
//				"O....O#O", // Seen:42978 Played:2915 Ratio:0.06782539904137 Min Turn:7 Max Turn:361 Ave Turn:2538.1965424170508
//				"O...OOO#", // Seen:35377 Played:2395 Ratio:0.06769935268677389 Min Turn:9 Max Turn:395 Ave Turn:3745.9086694745174
//				"O..OOO..", // Seen:45622 Played:3074 Ratio:0.06737977291657533 Min Turn:9 Max Turn:417 Ave Turn:8888.772500109597
//				"##O#....", // Seen:16058 Played:1069 Ratio:0.06657117947440529 Min Turn:7 Max Turn:373 Ave Turn:3254.57796736829
//				"O##*..**", // Seen:441 Played:29 Ratio:0.06575963718820861 Min Turn:29 Max Turn:407 Ave Turn:3087.1496598639455
//				"##O..#.O", // Seen:41986 Played:2754 Ratio:0.06559329300242939 Min Turn:9 Max Turn:361 Ave Turn:3745.4574143762206
//				"O.####.O", // Seen:14018 Played:914 Ratio:0.06520188329290912 Min Turn:15 Max Turn:399 Ave Turn:1299.4550577828506
//				"O##...##", // Seen:15456 Played:1004 Ratio:0.06495859213250517 Min Turn:11 Max Turn:383 Ave Turn:5196.405214803312
//				"*.O.**.#", // Seen:52732 Played:3421 Ratio:0.06487521808389593 Min Turn:5 Max Turn:395 Ave Turn:939.280683455966
//				"OOO#....", // Seen:16775 Played:1087 Ratio:0.06479880774962742 Min Turn:7 Max Turn:423 Ave Turn:2960.2191952309986
//				".#OO#O..", // Seen:20545 Played:1323 Ratio:0.06439522998296422 Min Turn:11 Max Turn:425 Ave Turn:2420.15332197615
//				"..O*#.**", // Seen:8378 Played:534 Ratio:0.06373836237765576 Min Turn:15 Max Turn:363 Ave Turn:1538.0253043685843
//				"#.#O..##", // Seen:24365 Played:1551 Ratio:0.0636568848758465 Min Turn:9 Max Turn:385 Ave Turn:2552.9921198440384
//				"OO...###", // Seen:5670 Played:359 Ratio:0.06331569664902999 Min Turn:9 Max Turn:345 Ave Turn:2402.6291005291005
//				"O.O.#O##", // Seen:2626 Played:166 Ratio:0.06321401370906321 Min Turn:15 Max Turn:347 Ave Turn:4143.482482863671
//				".O.OO..#", // Seen:31858 Played:2003 Ratio:0.06287274781844435 Min Turn:11 Max Turn:345 Ave Turn:1901.187770732626
//				"O.O*..**", // Seen:65366 Played:4108 Ratio:0.06284612795642995 Min Turn:7 Max Turn:403 Ave Turn:8179.474405654316
//				"O#####.O", // Seen:19065 Played:1189 Ratio:0.06236559139784946 Min Turn:13 Max Turn:397 Ave Turn:2353.4947810123263
//				"O#...O.#", // Seen:31978 Played:1988 Ratio:0.06216774032147101 Min Turn:7 Max Turn:419 Ave Turn:1852.8969916817812
//				"#*..*.*#", // Seen:16075 Played:996 Ratio:0.06195956454121306 Min Turn:11 Max Turn:397 Ave Turn:3935.5942768273717
//				"#.#.##OO", // Seen:15664 Played:969 Ratio:0.06186159346271706 Min Turn:11 Max Turn:377 Ave Turn:3670.2341675178754
//				"O.O.#..#", // Seen:7604 Played:468 Ratio:0.061546554445028934 Min Turn:9 Max Turn:339 Ave Turn:1527.4185954760653
//				"..#.O#O.", // Seen:49843 Played:3067 Ratio:0.06153321429287964 Min Turn:7 Max Turn:363 Ave Turn:2622.760869129065
//				"O#...O#O", // Seen:17558 Played:1079 Ratio:0.06145346850438547 Min Turn:11 Max Turn:353 Ave Turn:2042.9510764323954
//				"#.#.O##O", // Seen:1141 Played:70 Ratio:0.06134969325153374 Min Turn:17 Max Turn:341 Ave Turn:2623.2261174408413
//				"#..#....", // Seen:120199 Played:7277 Ratio:0.06054126906213862 Min Turn:5 Max Turn:387 Ave Turn:23469.75093802777
//				"O.#.O#.#", // Seen:51052 Played:3075 Ratio:0.06023270390973909 Min Turn:9 Max Turn:379 Ave Turn:3506.791859280733
//				".#..#O#O", // Seen:30143 Played:1812 Ratio:0.06011345917791859 Min Turn:7 Max Turn:387 Ave Turn:8210.070231894635
//				"#..#..#O", // Seen:75995 Played:4529 Ratio:0.059596026054345684 Min Turn:7 Max Turn:397 Ave Turn:2740.3566813606158
//				".O..O#.#", // Seen:51573 Played:3015 Ratio:0.05846082252341341 Min Turn:7 Max Turn:349 Ave Turn:2601.540961355748
//				"#*..*.*.", // Seen:41147 Played:2403 Ratio:0.05840036940724719 Min Turn:3 Max Turn:397 Ave Turn:3444.814470070722
//				"OO.OO..#", // Seen:7706 Played:447 Ratio:0.058006747988580326 Min Turn:13 Max Turn:365 Ave Turn:2551.9979236958216
//				".###.#.O", // Seen:82131 Played:4759 Ratio:0.05794401626669589 Min Turn:9 Max Turn:387 Ave Turn:7260.415872204162
//				"O#.O.O#O", // Seen:41199 Played:2365 Ratio:0.057404305929755574 Min Turn:9 Max Turn:369 Ave Turn:4086.470860943227
//				".##O.O##", // Seen:9962 Played:570 Ratio:0.05721742621963461 Min Turn:17 Max Turn:393 Ave Turn:945.154286287894
//				".##.##O#", // Seen:31942 Played:1810 Ratio:0.056665205685304615 Min Turn:15 Max Turn:381 Ave Turn:4777.272212134494
//				".O.O.#.#", // Seen:43973 Played:2477 Ratio:0.05633002069451709 Min Turn:5 Max Turn:341 Ave Turn:2054.72066950174
//				"#*.O*#*O", // Seen:4452 Played:245 Ratio:0.055031446540880505 Min Turn:27 Max Turn:375 Ave Turn:4501.058625336927
//				"O###.#.#", // Seen:12949 Played:711 Ratio:0.05490771488145803 Min Turn:17 Max Turn:383 Ave Turn:3486.4561742219475
//				".O#OO...", // Seen:42935 Played:2344 Ratio:0.054594153953650866 Min Turn:7 Max Turn:371 Ave Turn:2213.2268545475717
//				"#.O.#O.O", // Seen:49830 Played:2714 Ratio:0.0544651816174995 Min Turn:9 Max Turn:385 Ave Turn:3395.620830824804
//				"O.O.O..O", // Seen:25531 Played:1382 Ratio:0.054130273001449215 Min Turn:11 Max Turn:417 Ave Turn:8593.449806118053
//				"#*.O*.*#", // Seen:2005 Played:108 Ratio:0.053865336658354114 Min Turn:29 Max Turn:345 Ave Turn:1411.4538653366583
//				"..#.O.##", // Seen:44287 Played:2362 Ratio:0.05333393546638968 Min Turn:7 Max Turn:379 Ave Turn:2394.8530945875764
//				"..O##O#.", // Seen:16209 Played:861 Ratio:0.05311863779381825 Min Turn:13 Max Turn:349 Ave Turn:2155.5746190388054
//				"#OO#####", // Seen:2218 Played:117 Ratio:0.0527502254283138 Min Turn:23 Max Turn:385 Ave Turn:6542.421550946799
//				".O.O.OOO", // Seen:21452 Played:1123 Ratio:0.052349431288457954 Min Turn:17 Max Turn:431 Ave Turn:4831.535474547828
//				"..#.O..#", // Seen:81666 Played:4237 Ratio:0.05188205618005045 Min Turn:5 Max Turn:351 Ave Turn:2268.5092449734284
//				"#..#.#O#", // Seen:37555 Played:1946 Ratio:0.05181733457595526 Min Turn:9 Max Turn:391 Ave Turn:3555.711196911197
//				"O#.#...#", // Seen:41062 Played:2123 Ratio:0.051702303833227804 Min Turn:9 Max Turn:361 Ave Turn:2265.1952413423605
//				"#O#O#.O.", // Seen:12822 Played:656 Ratio:0.05116206520043675 Min Turn:15 Max Turn:369 Ave Turn:2878.4472001247855
//				"O###.##.", // Seen:8069 Played:411 Ratio:0.0509356797620523 Min Turn:15 Max Turn:387 Ave Turn:3365.421241789565
//				".#O..O..", // Seen:111535 Played:5583 Ratio:0.05005603622181378 Min Turn:5 Max Turn:363 Ave Turn:3345.583520867889
//				"O...OO##", // Seen:29013 Played:1446 Ratio:0.04983972701892255 Min Turn:7 Max Turn:355 Ave Turn:8420.645469272395
//				"O#.#.##.", // Seen:4167 Played:207 Ratio:0.04967602591792657 Min Turn:13 Max Turn:363 Ave Turn:1574.0890328773698
//				"#O#.O#..", // Seen:35951 Played:1720 Ratio:0.047842897276849046 Min Turn:7 Max Turn:363 Ave Turn:3425.45247698256
//				"OO#O..O.", // Seen:10174 Played:482 Ratio:0.0473756634558679 Min Turn:11 Max Turn:375 Ave Turn:2178.7603695694906
//				".O.O#.O#", // Seen:3520 Played:166 Ratio:0.04715909090909091 Min Turn:13 Max Turn:349 Ave Turn:2626.4758522727275
//				"O#######", // Seen:9388 Played:441 Ratio:0.04697486152535151 Min Turn:27 Max Turn:407 Ave Turn:9709.216340008521
				"..##O...", // Seen:45260 Played:2077 Ratio:0.04589041095890411 Min Turn:5 Max Turn:341 Ave Turn:4542.603954927088
				"..#.O###", // Seen:36548 Played:1676 Ratio:0.04585750246251505 Min Turn:9 Max Turn:359 Ave Turn:3575.062028017949
				".OOO.#.O", // Seen:13260 Played:604 Ratio:0.04555052790346908 Min Turn:15 Max Turn:377 Ave Turn:2667.2851432880843
				".#OO.#.O", // Seen:7766 Played:349 Ratio:0.044939479783672416 Min Turn:11 Max Turn:331 Ave Turn:1703.799381921195
				"#####OO#", // Seen:21241 Played:953 Ratio:0.04486606091991902 Min Turn:19 Max Turn:399 Ave Turn:15769.232569088084
				"*#.#**.#", // Seen:37565 Played:1660 Ratio:0.044190070544389726 Min Turn:11 Max Turn:397 Ave Turn:6542.109676560628
				"#..O##..", // Seen:60763 Played:2658 Ratio:0.043743725622500534 Min Turn:7 Max Turn:367 Ave Turn:5476.507463423465
				"OO...O.#", // Seen:35437 Played:1496 Ratio:0.04221576318537122 Min Turn:7 Max Turn:363 Ave Turn:2908.781951068093
				".O#O.#..", // Seen:35835 Played:1505 Ratio:0.04199804660248361 Min Turn:7 Max Turn:363 Ave Turn:1663.9075763917958
				"**##***#", // Seen:1811 Played:76 Ratio:0.041965764770844835 Min Turn:23 Max Turn:425 Ave Turn:12250.614577581448
				"O*OO*.*.", // Seen:12509 Played:524 Ratio:0.0418898393156927 Min Turn:21 Max Turn:453 Ave Turn:10950.107842353505
				"*O..**##", // Seen:41056 Played:1708 Ratio:0.041601714731098985 Min Turn:9 Max Turn:391 Ave Turn:866.9153838659392
				"O.OOO.O.", // Seen:22925 Played:945 Ratio:0.04122137404580153 Min Turn:15 Max Turn:453 Ave Turn:11972.608724100328
				"..**O***", // Seen:68297 Played:2792 Ratio:0.040880272925604345 Min Turn:3 Max Turn:395 Ave Turn:12805.834707234579
				".#OO..OO", // Seen:38584 Played:1577 Ratio:0.04087186398507153 Min Turn:9 Max Turn:389 Ave Turn:3686.7959776072985
				"O#.O.O.O", // Seen:16341 Played:665 Ratio:0.04069518389327458 Min Turn:9 Max Turn:375 Ave Turn:5207.201456459213
				"...##.#.", // Seen:120254 Played:4871 Ratio:0.04050592911670298 Min Turn:7 Max Turn:385 Ave Turn:8991.914306384819
				".#...##O", // Seen:33282 Played:1347 Ratio:0.04047232738417163 Min Turn:9 Max Turn:353 Ave Turn:2555.5820263205337
				"O.O.OO##", // Seen:16131 Played:635 Ratio:0.0393651974459116 Min Turn:15 Max Turn:385 Ave Turn:3585.8910792883266
				"#####OO.", // Seen:36571 Played:1439 Ratio:0.03934811736075032 Min Turn:13 Max Turn:397 Ave Turn:8053.906619999453
				"##OO....", // Seen:15893 Played:623 Ratio:0.039199647643616685 Min Turn:9 Max Turn:375 Ave Turn:2792.3525451456617
				"#*..*#*#", // Seen:33867 Played:1325 Ratio:0.03912363067292645 Min Turn:13 Max Turn:425 Ave Turn:5862.586411551068
				"#.#####O", // Seen:48090 Played:1881 Ratio:0.039114160948222086 Min Turn:13 Max Turn:425 Ave Turn:8701.189956331878
				"O#O.#O#O", // Seen:24784 Played:951 Ratio:0.038371530019367335 Min Turn:17 Max Turn:387 Ave Turn:6420.3753227888965
				"O.O..OO.", // Seen:10042 Played:378 Ratio:0.037641904003186615 Min Turn:13 Max Turn:437 Ave Turn:7228.719677355109
				"..O..O#.", // Seen:77744 Played:2886 Ratio:0.037121835768676684 Min Turn:5 Max Turn:359 Ave Turn:2382.5576507511832
				"OOOO#OO.", // Seen:31742 Played:1165 Ratio:0.03670216117446916 Min Turn:13 Max Turn:453 Ave Turn:12306.914592653267
				"..##OO.#", // Seen:10452 Played:380 Ratio:0.03635667814772292 Min Turn:11 Max Turn:337 Ave Turn:2289.494450822809
				".#OO#OOO", // Seen:21705 Played:786 Ratio:0.03621285418106427 Min Turn:17 Max Turn:379 Ave Turn:6212.384703985257
				"#.#*..**", // Seen:64741 Played:2331 Ratio:0.03600500455661791 Min Turn:5 Max Turn:425 Ave Turn:8383.621352774902
				"*O..**O#", // Seen:16041 Played:576 Ratio:0.03590798578642229 Min Turn:19 Max Turn:407 Ave Turn:3592.805186709058
				"#.#O.###", // Seen:9953 Played:348 Ratio:0.03496433236210188 Min Turn:17 Max Turn:381 Ave Turn:2678.8983221139356
				"OO*.O*O*", // Seen:32287 Played:1120 Ratio:0.03468888407098832 Min Turn:19 Max Turn:453 Ave Turn:5506.507201040667
				"#.#O.#.#", // Seen:36199 Played:1232 Ratio:0.03403408933948451 Min Turn:13 Max Turn:387 Ave Turn:3702.505787452692
				"#....##.", // Seen:75786 Played:2554 Ratio:0.03370015570158077 Min Turn:7 Max Turn:387 Ave Turn:4677.2105138152165
				"..OO#...", // Seen:47886 Played:1601 Ratio:0.03343357139873867 Min Turn:5 Max Turn:335 Ave Turn:4426.8271937518275
				"###.O..#", // Seen:40498 Played:1353 Ratio:0.033409057237394436 Min Turn:9 Max Turn:385 Ave Turn:4300.360882018865
				"OO##...#", // Seen:3544 Played:118 Ratio:0.03329571106094808 Min Turn:11 Max Turn:335 Ave Turn:3175.5403498871333
				"...#####", // Seen:23078 Played:765 Ratio:0.033148453072189964 Min Turn:11 Max Turn:407 Ave Turn:10042.862509749544
				"###.O#.#", // Seen:41921 Played:1360 Ratio:0.03244197418954701 Min Turn:15 Max Turn:397 Ave Turn:5661.471291238281
				"#O.O#.O.", // Seen:35667 Played:1140 Ratio:0.03196231810917655 Min Turn:11 Max Turn:377 Ave Turn:3387.525527798806
				"..O#.O#.", // Seen:26813 Played:823 Ratio:0.03069406631111774 Min Turn:11 Max Turn:363 Ave Turn:1684.0841755864692
				"#.#O..O.", // Seen:31205 Played:950 Ratio:0.030443839128344818 Min Turn:7 Max Turn:377 Ave Turn:1873.1493350424612
				"#.#.#.O#", // Seen:8822 Played:267 Ratio:0.03026524597596917 Min Turn:15 Max Turn:383 Ave Turn:6637.322035819542
				".###O.#O", // Seen:22303 Played:658 Ratio:0.029502757476572657 Min Turn:15 Max Turn:363 Ave Turn:1327.0961754024122
				"##*##*#*", // Seen:7131 Played:210 Ratio:0.029448885149347917 Min Turn:27 Max Turn:407 Ave Turn:11211.07095778993
				"*..#**.#", // Seen:124347 Played:3646 Ratio:0.029321173811993857 Min Turn:7 Max Turn:425 Ave Turn:23390.56867475693
				"*O##**.#", // Seen:4098 Played:119 Ratio:0.029038555392874574 Min Turn:21 Max Turn:383 Ave Turn:2911.2227916056613
				"O.OOOOO.", // Seen:23743 Played:687 Ratio:0.028934843954007496 Min Turn:19 Max Turn:453 Ave Turn:5393.791096323127
				".O#*O.**", // Seen:1371 Played:39 Ratio:0.028446389496717725 Min Turn:17 Max Turn:355 Ave Turn:2862.7768052516412
				".O##O#O#", // Seen:24547 Played:697 Ratio:0.02839450849390964 Min Turn:19 Max Turn:393 Ave Turn:6349.973520185766
				"OOOOO.O#", // Seen:42681 Played:1192 Ratio:0.027928117897893675 Min Turn:13 Max Turn:453 Ave Turn:6471.506993744289
				"#...##O.", // Seen:79635 Played:2208 Ratio:0.027726502166132982 Min Turn:9 Max Turn:393 Ave Turn:3400.193507879701
				"##.O###O", // Seen:21639 Played:598 Ratio:0.027635288137159757 Min Turn:17 Max Turn:429 Ave Turn:6121.206848745321
				"#.#...O#", // Seen:36308 Played:997 Ratio:0.02745951305497411 Min Turn:7 Max Turn:393 Ave Turn:2892.5741985237414
				"*O.O**O#", // Seen:17842 Played:483 Ratio:0.027070956170832865 Min Turn:17 Max Turn:401 Ave Turn:3913.9469229906963
				"#..#...#", // Seen:107348 Played:2905 Ratio:0.027061519543913254 Min Turn:7 Max Turn:407 Ave Turn:6700.052399672095
				"..OO.#.O", // Seen:43941 Played:1162 Ratio:0.026444550647459094 Min Turn:9 Max Turn:365 Ave Turn:2217.1643567510982
				"OOO.OOOO", // Seen:13185 Played:347 Ratio:0.026317785362153963 Min Turn:15 Max Turn:453 Ave Turn:9025.639969662496
				".#.#....", // Seen:128510 Played:3368 Ratio:0.026208077192436386 Min Turn:5 Max Turn:387 Ave Turn:20736.164430783596
				"O###.###", // Seen:9352 Played:245 Ratio:0.02619760479041916 Min Turn:25 Max Turn:425 Ave Turn:3854.414777587682
				".#.#..#O", // Seen:48958 Played:1282 Ratio:0.026185710200580088 Min Turn:7 Max Turn:361 Ave Turn:1915.064769802688
				"O.##.###", // Seen:26666 Played:696 Ratio:0.026100652516312908 Min Turn:15 Max Turn:401 Ave Turn:4560.985449636241
				"#O##.#..", // Seen:9845 Played:254 Ratio:0.02579989842559675 Min Turn:15 Max Turn:383 Ave Turn:2126.699746063992
				"..#.#.##", // Seen:41398 Played:1067 Ratio:0.025774191989951206 Min Turn:9 Max Turn:407 Ave Turn:4386.2160732402535
				".###...O", // Seen:107067 Played:2735 Ratio:0.02554475235133141 Min Turn:7 Max Turn:387 Ave Turn:6412.698011525494
				".O##O#..", // Seen:18646 Played:475 Ratio:0.025474632628982086 Min Turn:13 Max Turn:421 Ave Turn:2627.265901533841
				"#O#.O#.#", // Seen:24856 Played:630 Ratio:0.025345992919214678 Min Turn:9 Max Turn:379 Ave Turn:3970.5031783070485
				".OOO##OO", // Seen:3930 Played:97 Ratio:0.024681933842239184 Min Turn:23 Max Turn:363 Ave Turn:6050.755470737913
				"#..*..**", // Seen:131055 Played:3202 Ratio:0.024432490175880357 Min Turn:3 Max Turn:399 Ave Turn:25682.300370073634
				"OO##O...", // Seen:3363 Played:82 Ratio:0.024382991376746953 Min Turn:17 Max Turn:349 Ave Turn:3617.674992566161
				"O#O.#O#.", // Seen:17719 Played:432 Ratio:0.024380608386477792 Min Turn:13 Max Turn:407 Ave Turn:3723.882781195327
				"OO.#OOO.", // Seen:27612 Played:651 Ratio:0.02357670578009561 Min Turn:11 Max Turn:407 Ave Turn:4479.926589888454
				"#.##.OO.", // Seen:25721 Played:597 Ratio:0.023210606119513238 Min Turn:9 Max Turn:357 Ave Turn:1731.8658294778586
				".#OO#.OO", // Seen:15955 Played:370 Ratio:0.023190222500783453 Min Turn:15 Max Turn:379 Ave Turn:3674.0577875274207
				"OO..O#.#", // Seen:11068 Played:256 Ratio:0.023129743404409108 Min Turn:11 Max Turn:357 Ave Turn:2421.0332490061437
				"#O#O.#O#", // Seen:6413 Played:147 Ratio:0.022922189302978326 Min Turn:21 Max Turn:379 Ave Turn:3332.1492281303604
				"#.OO.#OO", // Seen:23473 Played:529 Ratio:0.02253653133387296 Min Turn:13 Max Turn:385 Ave Turn:4058.1603118476546
				"O#OO.OOO", // Seen:11011 Played:247 Ratio:0.02243211334120425 Min Turn:19 Max Turn:397 Ave Turn:3369.7559713014257
				"#..O#..O", // Seen:41451 Played:925 Ratio:0.022315505054160335 Min Turn:7 Max Turn:369 Ave Turn:2289.4038503293045
				"OOO.....", // Seen:47387 Played:1054 Ratio:0.022242387152594592 Min Turn:7 Max Turn:425 Ave Turn:8389.374490894126
				"###.O##.", // Seen:35443 Played:780 Ratio:0.022007166436249753 Min Turn:11 Max Turn:397 Ave Turn:5710.895437745112
				"OOO...O.", // Seen:33269 Played:718 Ratio:0.02158165258949773 Min Turn:15 Max Turn:451 Ave Turn:4629.993026541225
				"...#.###", // Seen:78750 Played:1696 Ratio:0.021536507936507937 Min Turn:11 Max Turn:397 Ave Turn:6505.778844444444
				"O.##OO.#", // Seen:15781 Played:325 Ratio:0.020594385653634115 Min Turn:11 Max Turn:375 Ave Turn:3625.0759140738865
				"O...OO#.", // Seen:75099 Played:1506 Ratio:0.020053529341269524 Min Turn:7 Max Turn:389 Ave Turn:3510.056911543429
				"#*O**.**", // Seen:3820 Played:76 Ratio:0.019895287958115182 Min Turn:21 Max Turn:397 Ave Turn:4048.1217277486912
				"#.##O.#.", // Seen:16773 Played:332 Ratio:0.019793716091337267 Min Turn:11 Max Turn:367 Ave Turn:3011.824002861742
				"OO##O.O#", // Seen:6604 Played:128 Ratio:0.019382192610539067 Min Turn:21 Max Turn:423 Ave Turn:3188.3808298001213
				"#.##.O.#", // Seen:63471 Played:1205 Ratio:0.018985048289770132 Min Turn:9 Max Turn:425 Ave Turn:5509.557924091318
				"O..#OO..", // Seen:51187 Played:942 Ratio:0.018403110164690255 Min Turn:7 Max Turn:375 Ave Turn:6437.544630472581
				"#O##.#O#", // Seen:7767 Played:142 Ratio:0.018282477146903568 Min Turn:19 Max Turn:375 Ave Turn:3504.12913608858
				"##O.##.O", // Seen:14985 Played:265 Ratio:0.01768435101768435 Min Turn:15 Max Turn:387 Ave Turn:3658.0335001668336
				".#O.#O..", // Seen:57953 Played:1013 Ratio:0.017479681811122805 Min Turn:9 Max Turn:363 Ave Turn:3235.5835418356255
				"**.O***O", // Seen:17212 Played:300 Ratio:0.017429700209156403 Min Turn:11 Max Turn:395 Ave Turn:6447.351498954218
				"*..#**##", // Seen:75827 Played:1309 Ratio:0.017262980204940194 Min Turn:11 Max Turn:425 Ave Turn:29398.129571260895
				"#OOO#.OO", // Seen:8932 Played:153 Ratio:0.017129422301836096 Min Turn:13 Max Turn:381 Ave Turn:3114.9378638602775
				"#..#..##", // Seen:43235 Played:703 Ratio:0.01625997455765005 Min Turn:9 Max Turn:385 Ave Turn:9445.794078871284
				"...#..##", // Seen:124966 Played:2004 Ratio:0.016036361890434198 Min Turn:7 Max Turn:377 Ave Turn:33830.64912856297
				"####O###", // Seen:29506 Played:458 Ratio:0.015522266657628956 Min Turn:19 Max Turn:407 Ave Turn:14392.095404324544
				".###O...", // Seen:29063 Played:447 Ratio:0.015380380552592644 Min Turn:7 Max Turn:355 Ave Turn:2403.577641674982
				".#O#..#.", // Seen:25854 Played:393 Ratio:0.01520074263170109 Min Turn:9 Max Turn:391 Ave Turn:1738.6847296356464
				"OO.#O...", // Seen:25293 Played:380 Ratio:0.01502391966156644 Min Turn:7 Max Turn:373 Ave Turn:2038.7359743802633
				"*##.**..", // Seen:3370 Played:49 Ratio:0.014540059347181009 Min Turn:27 Max Turn:377 Ave Turn:11738.918397626112
				"##O##.#.", // Seen:3918 Played:56 Ratio:0.014293006636038795 Min Turn:23 Max Turn:389 Ave Turn:3819.7741194486985
				"O#.#..##", // Seen:17391 Played:246 Ratio:0.014145247541831982 Min Turn:11 Max Turn:383 Ave Turn:2471.695532171813
				"#..#####", // Seen:12954 Played:183 Ratio:0.01412691060676239 Min Turn:25 Max Turn:393 Ave Turn:24567.000385981166
				".##.#..#", // Seen:22654 Played:310 Ratio:0.013684117595126688 Min Turn:11 Max Turn:407 Ave Turn:7830.437317912952
				".O#.O#.#", // Seen:42143 Played:544 Ratio:0.01290843081887858 Min Turn:11 Max Turn:389 Ave Turn:4064.7561160809623
				"..#####.", // Seen:14909 Played:192 Ratio:0.012878127305654304 Min Turn:17 Max Turn:385 Ave Turn:10454.503923804414
				".#.#.##O", // Seen:6645 Played:82 Ratio:0.012340105342362679 Min Turn:15 Max Turn:399 Ave Turn:2274.1461249059444
				"#.#.#...", // Seen:104106 Played:1273 Ratio:0.012227921541505773 Min Turn:7 Max Turn:387 Ave Turn:7803.05419476303
				"OO*OO*.*", // Seen:18721 Played:228 Ratio:0.012178836600608941 Min Turn:19 Max Turn:453 Ave Turn:6176.417231985471
				"O#O..O#.", // Seen:6976 Played:84 Ratio:0.012041284403669725 Min Turn:13 Max Turn:393 Ave Turn:2051.008744266055
				"**OO***O", // Seen:1596 Played:19 Ratio:0.011904761904761904 Min Turn:29 Max Turn:397 Ave Turn:11994.698621553885
				"###.#.#O", // Seen:9572 Played:111 Ratio:0.011596322607605515 Min Turn:19 Max Turn:395 Ave Turn:2923.5410572503133
				".###.O#.", // Seen:8254 Played:95 Ratio:0.011509571117034165 Min Turn:15 Max Turn:387 Ave Turn:2315.0134480252
				"**O.***#", // Seen:33204 Played:376 Ratio:0.011323936875075291 Min Turn:11 Max Turn:403 Ave Turn:3342.61438380918
				"O.OOOO#O", // Seen:9206 Played:104 Ratio:0.011296980230284597 Min Turn:23 Max Turn:425 Ave Turn:2888.1924831631545
				"##..####", // Seen:13856 Played:156 Ratio:0.011258660508083142 Min Turn:23 Max Turn:407 Ave Turn:10430.274898960739
				".##..###", // Seen:38460 Played:421 Ratio:0.0109464378575143 Min Turn:15 Max Turn:425 Ave Turn:6067.514586583463
				"##...##.", // Seen:24680 Played:264 Ratio:0.010696920583468396 Min Turn:11 Max Turn:385 Ave Turn:8803.514141004862
				"#.##O##.", // Seen:7720 Played:79 Ratio:0.010233160621761658 Min Turn:19 Max Turn:379 Ave Turn:2878.077849740933
				"#..O#.OO", // Seen:38342 Played:390 Ratio:0.010171613374367535 Min Turn:11 Max Turn:419 Ave Turn:4345.675864587137
				"#.##..O#", // Seen:13815 Played:131 Ratio:0.009482446615997104 Min Turn:11 Max Turn:391 Ave Turn:2546.9788635541076
				"#*.#*#*#", // Seen:9934 Played:94 Ratio:0.009462452184417153 Min Turn:17 Max Turn:407 Ave Turn:9841.056070062412
				"##.....#", // Seen:27682 Played:250 Ratio:0.009031139368542735 Min Turn:7 Max Turn:363 Ave Turn:6672.343580666136
				"O.O.OO.#", // Seen:29218 Played:263 Ratio:0.009001300568142925 Min Turn:11 Max Turn:377 Ave Turn:2927.539633102882
				"O*..*O*.", // Seen:57719 Played:500 Ratio:0.008662658743221469 Min Turn:5 Max Turn:395 Ave Turn:7999.365668150869
				"#.#O.#O.", // Seen:6021 Played:52 Ratio:0.008636439129712672 Min Turn:17 Max Turn:395 Ave Turn:2123.0680950008305
				"O.O#OO..", // Seen:17281 Played:149 Ratio:0.008622186216075458 Min Turn:9 Max Turn:373 Ave Turn:2596.245529772583
				"O.O.OO..", // Seen:74086 Played:612 Ratio:0.008260670032124828 Min Turn:9 Max Turn:389 Ave Turn:6778.683840401695
				"O.OO.O#O", // Seen:6544 Played:53 Ratio:0.008099022004889975 Min Turn:19 Max Turn:373 Ave Turn:2698.926650366748
				"##OO#..O", // Seen:1013 Played:8 Ratio:0.007897334649555774 Min Turn:29 Max Turn:359 Ave Turn:4079.4126357354394
				".##...##", // Seen:41427 Played:325 Ratio:0.007845125159919858 Min Turn:9 Max Turn:397 Ave Turn:10139.090810341082
				"O.O..O..", // Seen:77282 Played:600 Ratio:0.007763774229445407 Min Turn:7 Max Turn:395 Ave Turn:10625.062744235398
				"..##O..#", // Seen:6378 Played:49 Ratio:0.007682659140796488 Min Turn:11 Max Turn:387 Ave Turn:4329.732361241769
				"*OOO**OO", // Seen:6953 Played:52 Ratio:0.007478786135481088 Min Turn:27 Max Turn:453 Ave Turn:11121.63742269524
				"..####..", // Seen:31308 Played:234 Ratio:0.007474128018397854 Min Turn:13 Max Turn:407 Ave Turn:4527.157467739875
				"#*.#*.*#", // Seen:6429 Played:48 Ratio:0.007466168922071862 Min Turn:13 Max Turn:397 Ave Turn:5000.485300979934
				"OO..O..#", // Seen:7019 Played:52 Ratio:0.007408462743980624 Min Turn:9 Max Turn:357 Ave Turn:4694.656931186779
				".OOO.OO.", // Seen:16490 Played:119 Ratio:0.007216494845360825 Min Turn:17 Max Turn:441 Ave Turn:4547.513098847787
				"O#OO.O.O", // Seen:3385 Played:24 Ratio:0.0070901033973412115 Min Turn:21 Max Turn:397 Ave Turn:4868.423338257016
				"..##.O##", // Seen:31086 Played:217 Ratio:0.006980634369169401 Min Turn:9 Max Turn:379 Ave Turn:2363.2461236569516
				"*#.#**##", // Seen:30947 Played:207 Ratio:0.006688855139431932 Min Turn:17 Max Turn:425 Ave Turn:6253.217920961644
				".#######", // Seen:11786 Played:78 Ratio:0.006618021381299848 Min Turn:19 Max Turn:397 Ave Turn:10118.101221788562
				"##..##.#", // Seen:20143 Played:133 Ratio:0.006602790051134389 Min Turn:15 Max Turn:407 Ave Turn:5299.41845802512
				"O.OOOO..", // Seen:23118 Played:150 Ratio:0.006488450558006748 Min Turn:13 Max Turn:451 Ave Turn:4621.18790552816
				".O#.O#O#", // Seen:23824 Played:150 Ratio:0.006296171927468099 Min Turn:15 Max Turn:381 Ave Turn:14702.08147246474
				"O#O..O#O", // Seen:10216 Played:64 Ratio:0.006264682850430697 Min Turn:17 Max Turn:425 Ave Turn:2727.4524275646045
				"#.#.O###", // Seen:10141 Played:62 Ratio:0.006113795483680111 Min Turn:15 Max Turn:385 Ave Turn:2776.103638694409
				"**.O***.", // Seen:15688 Played:95 Ratio:0.006055583885772565 Min Turn:3 Max Turn:403 Ave Turn:3343.5689699133095
				".#.*#.**", // Seen:55043 Played:323 Ratio:0.005868139454608215 Min Turn:5 Max Turn:399 Ave Turn:7797.726341224134
				".O##O#.#", // Seen:8724 Played:50 Ratio:0.005731315910132966 Min Turn:19 Max Turn:427 Ave Turn:3095.700137551582
				"O.OO.O..", // Seen:37923 Played:204 Ratio:0.005379321256229729 Min Turn:11 Max Turn:425 Ave Turn:4670.003111568178
				"#.#..#O#", // Seen:13673 Played:73 Ratio:0.0053389892488846635 Min Turn:13 Max Turn:375 Ave Turn:2924.116726395085
				"O.O..O#O", // Seen:12977 Played:66 Ratio:0.005085921245280111 Min Turn:13 Max Turn:375 Ave Turn:3158.244355398012
				"..##O###", // Seen:7768 Played:39 Ratio:0.005020597322348095 Min Turn:19 Max Turn:385 Ave Turn:7804.866503604531
				"OO*.O*.*", // Seen:38986 Played:193 Ratio:0.0049504950495049506 Min Turn:13 Max Turn:433 Ave Turn:5885.333119581388
				"###.###O", // Seen:8217 Played:39 Ratio:0.004746257758305951 Min Turn:25 Max Turn:395 Ave Turn:3027.59437751004
				"#.#####.", // Seen:23212 Played:110 Ratio:0.0047389281406169225 Min Turn:17 Max Turn:425 Ave Turn:5834.991986903326
				"##..###.", // Seen:25593 Played:116 Ratio:0.00453248935255734 Min Turn:15 Max Turn:399 Ave Turn:10698.119759309186
				"OOOOOOOO", // Seen:2474 Played:11 Ratio:0.00444624090541633 Min Turn:37 Max Turn:453 Ave Turn:31507.489490703316
				"OO..OOO#", // Seen:7078 Played:28 Ratio:0.003955919751342187 Min Turn:21 Max Turn:387 Ave Turn:8404.899547894885
				"*O*O**.*", // Seen:5676 Played:21 Ratio:0.0036997885835095136 Min Turn:9 Max Turn:453 Ave Turn:13752.412438336856
				"OOOO.OO.", // Seen:3612 Played:13 Ratio:0.0035991140642303433 Min Turn:27 Max Turn:453 Ave Turn:16342.383444075305
				"##*.#*.*", // Seen:38625 Played:132 Ratio:0.00341747572815534 Min Turn:7 Max Turn:425 Ave Turn:6359.1619935275085
				".#.#.##.", // Seen:8210 Played:27 Ratio:0.0032886723507917176 Min Turn:13 Max Turn:397 Ave Turn:7824.858343483556
				".####...", // Seen:33760 Played:101 Ratio:0.002991706161137441 Min Turn:11 Max Turn:407 Ave Turn:4879.297808056872
				"##.##O#.", // Seen:6714 Played:20 Ratio:0.002978850163836759 Min Turn:19 Max Turn:367 Ave Turn:2474.2570747691393
				"##.#.#.#", // Seen:22607 Played:67 Ratio:0.0029636838147476447 Min Turn:15 Max Turn:377 Ave Turn:12665.558234175256
				"O.OO.O.O", // Seen:15878 Played:47 Ratio:0.0029600705378511147 Min Turn:15 Max Turn:405 Ave Turn:8769.386509635975
				"*.*.**#*", // Seen:67402 Played:194 Ratio:0.0028782528708346934 Min Turn:3 Max Turn:425 Ave Turn:14853.561452182428
				".#.#..#.", // Seen:71618 Played:196 Ratio:0.002736742159792231 Min Turn:7 Max Turn:385 Ave Turn:10631.198902510541
				"########", // Seen:2228 Played:6 Ratio:0.0026929982046678637 Min Turn:47 Max Turn:401 Ave Turn:32456.94658886894
				"OOOOOO.O", // Seen:8602 Played:23 Ratio:0.00267379679144385 Min Turn:37 Max Turn:453 Ave Turn:8474.333759590792
				"O.O.OO.O", // Seen:25713 Played:67 Ratio:0.002605685839847548 Min Turn:11 Max Turn:393 Ave Turn:10523.564850464745
				"OOOO#OOO", // Seen:30671 Played:77 Ratio:0.0025105148185582474 Min Turn:19 Max Turn:453 Ave Turn:14544.438753219654
				"OOOO.O.O", // Seen:11492 Played:28 Ratio:0.0024364775495997215 Min Turn:19 Max Turn:453 Ave Turn:9493.69430908458
				"#.##....", // Seen:47876 Played:118 Ratio:0.0024647004762302616 Min Turn:7 Max Turn:375 Ave Turn:8907.020615757374
				"O.OOOO.O", // Seen:17568 Played:38 Ratio:0.002163023679417122 Min Turn:23 Max Turn:447 Ave Turn:4561.614469489982
				"**#.***.", // Seen:14198 Played:31 Ratio:0.002183406113537118 Min Turn:3 Max Turn:385 Ave Turn:4270.131004366812
				"OOOO...O", // Seen:13876 Played:30 Ratio:0.0021620063418852694 Min Turn:15 Max Turn:453 Ave Turn:9785.70906601326
				"##..#.#.", // Seen:73532 Played:150 Ratio:0.002039928194527553 Min Turn:11 Max Turn:393 Ave Turn:7089.026192678018
				"###.##..", // Seen:14730 Played:28 Ratio:0.0019008825526137135 Min Turn:17 Max Turn:397 Ave Turn:9458.03849287169
				"#*.**#**", // Seen:18120 Played:33 Ratio:0.0018211920529801326 Min Turn:13 Max Turn:427 Ave Turn:6912.9676048565125
				"#.##...#", // Seen:36851 Played:63 Ratio:0.0017095872567908605 Min Turn:11 Max Turn:395 Ave Turn:4944.401427369678
				"###.#.#.", // Seen:22646 Played:37 Ratio:0.0016338426212134593 Min Turn:15 Max Turn:397 Ave Turn:4865.558862492272
				"OOOO....", // Seen:7494 Played:12 Ratio:0.0016012810248198558 Min Turn:9 Max Turn:453 Ave Turn:37477.05324259408
				"##.#.##.", // Seen:16127 Played:24 Ratio:0.001488187511626465 Min Turn:13 Max Turn:407 Ave Turn:4785.067960563031
				"####O..O", // Seen:14795 Played:8 Ratio:5.407232173031429E-4 Min Turn:15 Max Turn:385 Ave Turn:18134.406421088206
				"**..***.", // Seen:135485 Played:48 Ratio:3.5428276192936485E-4 Min Turn:2 Max Turn:395 Ave Turn:56833.015957486066
				"##.####.", // Seen:16524 Played:5 Ratio:3.025901718712176E-4 Min Turn:17 Max Turn:407 Ave Turn:4853.9492858871945
				"#######.", // Seen:8336 Played:1 Ratio:1.199616122840691E-4 Min Turn:29 Max Turn:407 Ave Turn:8582.302183301343
				"####.O##", // Seen:40940 Played:2 Ratio:4.885197850512946E-5 Min Turn:19 Max Turn:407 Ave Turn:6624.5363214460185
				"####O##.", // Seen:30753 Played:1 Ratio:3.2517152798100995E-5 Min Turn:17 Max Turn:425 Ave Turn:12488.105127954996
				"#####..#", // Seen:3890 Played:0 Ratio:0.0 Min Turn:17 Max Turn:387 Ave Turn:18627.874293059125
				"#####..O", // Seen:18362 Played:0 Ratio:0.0 Min Turn:11 Max Turn:395 Ave Turn:9349.00942163163
				"####..##", // Seen:11387 Played:0 Ratio:0.0 Min Turn:21 Max Turn:399 Ave Turn:9749.01088961096
				"####....", // Seen:7888 Played:0 Ratio:0.0 Min Turn:9 Max Turn:395 Ave Turn:38251.00849391481
				"####O...", // Seen:49922 Played:0 Ratio:0.0 Min Turn:9 Max Turn:381 Ave Turn:13896.877669163896
				"#####...", // Seen:14488 Played:0 Ratio:0.0 Min Turn:13 Max Turn:407 Ave Turn:10070.466109884042
				"#####O..", // Seen:71531 Played:0 Ratio:0.0 Min Turn:11 Max Turn:401 Ave Turn:8398.943129552223
				"**##***.", // Seen:6063 Played:0 Ratio:0.0 Min Turn:15 Max Turn:397 Ave Turn:14004.34158007587
				"*###**.#", // Seen:18978 Played:0 Ratio:0.0 Min Turn:23 Max Turn:407 Ave Turn:6232.3100432079245
				"###*..**", // Seen:12578 Played:0 Ratio:0.0 Min Turn:21 Max Turn:407 Ave Turn:11223.693989505486
//				"O.OO?oo?", // Tiger's mouth 
//				".#..#.?.", // Empty triangle
//				".OO?OO??", // Push through bamboo
		};
		Pattern[] BLACK_BAD_PATTERNS = new Pattern[badPatterns.length];
		Pattern[] WHITE_BAD_PATTERNS = new Pattern[badPatterns.length];
		for (int i = 0; i < BLACK_BAD_PATTERNS.length; i++) {
			BLACK_BAD_PATTERNS[i] = new ColorSpecificPattern(badPatterns[i], BLACK);
			WHITE_BAD_PATTERNS[i] = new ColorSpecificPattern(badPatterns[i], WHITE);
		}
		// Find all good neighborhoods, i.e., neighborhoods where a player
		// should play.
		// Note that i has to be an int, rather than a char, because
		// otherwise incrementing it after Character.MAX_VALUE would
		// return it to 0, resulting in an infinite loop.
		for (int i = 0; i < NUMBER_OF_NEIGHBORHOODS; i++) {
			if (!isPossibleNeighborhood((char) i)) {
				continue;
			}
			for (Pattern pattern : BLACK_GOOD_PATTERNS) {
				if (pattern.matches((char) i)) {
					GOOD_NEIGHBORHOODS[BLACK].set(i, true);
				}
			}
			for (Pattern pattern : WHITE_GOOD_PATTERNS) {
				if (pattern.matches((char) i)) {
					GOOD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}
			for (Pattern pattern : INDEPENDENT_GOOD_PATTERNS) {
				if (pattern.matches((char) i)) {
					GOOD_NEIGHBORHOODS[BLACK].set(i, true);
					GOOD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}
			for (Pattern pattern : BLACK_BAD_PATTERNS) {
				if (pattern.matches((char) i)) {
					BAD_NEIGHBORHOODS[BLACK].set(i, true);
				}
			}
			for (Pattern pattern : WHITE_BAD_PATTERNS) {
				if (pattern.matches((char) i)) {
					BAD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}	
		}
	}

	/**
	 * Returns true if the the specified 3x3 neighborhood can possibly occur.
	 * Neighborhoods are impossible if, for example, there are non-contiguous
	 * off-board points.
	 */
	public static boolean isPossibleNeighborhood(char neighborhood) {
		int mask = 0x3;
		// Replace black and white with vacant, leaving only
		// vacant and off-board colors
		for (int i = 0; i < 16; i += 2) {
			if ((neighborhood >>> i & mask) != OFF_BOARD_COLOR) {
				neighborhood &= ~(mask << i);
				neighborhood |= VACANT << i;
			}
		}
		// Verify that the resulting pattern is valid
		assert VALID_OFF_BOARD_PATTERNS != null;
		for (char v : VALID_OFF_BOARD_PATTERNS) {
			if (neighborhood == v) {
				return true;
			}
		}
		return false;
	}

	public AbstractPatternHeuristic(int weight) {
		super(weight);
	}

}
