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
				".OO#O###", // 3263/6511 = 0.5011518967900476
				"####OOO#", // 7110/14170 = 0.5017642907551164
				"...#.OOO", // 15142/30011 = 0.5045483322781646
				"##..OOO#", // 373/739 = 0.5047361299052774
				".O##...O", // 19995/39479 = 0.5064717951315889
				"#OO#..OO", // 557/1097 = 0.5077484047402006
				"####..OO", // 13212/25880 = 0.5105100463678516
				"..OO####", // 644/1261 = 0.5107057890563045
				"..#...OO", // 42193/82616 = 0.5107122107097899
				"..O...##", // 36881/72082 = 0.5116533947448739
				"*#O.**.#", // 757/1478 = 0.5121786197564276
				"O.##..OO", // 6808/13285 = 0.5124576590139255
				"O###.OO#", // 4304/8381 = 0.5135425366901324
				".#O#O.O#", // 2555/4974 = 0.5136710896662646
				"*#OO**.#", // 925/1799 = 0.5141745414118954
				"O###.OOO", // 774/1503 = 0.5149700598802395
				"*##O**O#", // 831/1603 = 0.5184029943855272
				"O.#O#.O#", // 8088/15561 = 0.5197609408135724
				".###.OO#", // 9478/18163 = 0.5218300941474426
				".#.OO.O#", // 3020/5787 = 0.5218593398997754
				".O.#...O", // 30213/57315 = 0.527139492279508
				"O.#O.O##", // 5306/9988 = 0.5312374849819784
				".O.O.###", // 6143/11489 = 0.5346853512055009
				".#.#O.O#", // 22967/42877 = 0.5356484828696038
				"..O#.###", // 9415/17565 = 0.5360091090236265
				"####OOOO", // 584/1079 = 0.541241890639481
				".#O##OO#", // 5564/10244 = 0.5431472081218274
				"..OO.###", // 996/1833 = 0.5433715220949263
				"..O#..OO", // 38960/71219 = 0.5470450301183672
				"#.O##.O#", // 2962/5413 = 0.547201182338814
				"O.#O..##", // 3891/7086 = 0.5491109229466554
				"#.O#O.O#", // 1257/2286 = 0.5498687664041995
				"O#O##OO#", // 1393/2522 = 0.5523394131641555
				"..#OOO##", // 2282/4131 = 0.5524086177680949
				"#OOO..##", // 484/874 = 0.5537757437070938
				"*.O#**##", // 14014/25295 = 0.5540225340976478
				"O###O..O", // 8171/14670 = 0.5569870483980913
				"##.OOOO#", // 474/850 = 0.5576470588235294
				"#.OO..##", // 4801/8605 = 0.557931435212086
				".O#O..##", // 16280/28929 = 0.562757094956618
				"OOO#####", // 489/864 = 0.5659722222222222
				"#.O#.OO#", // 4057/7110 = 0.570604781997187
				"##OO.###", // 1458/2555 = 0.5706457925636008
				"#OO..O##", // 786/1373 = 0.5724690458849235
				".OO##..#", // 6598/11523 = 0.5725939425496832
				"#.O#..O#", // 5494/9578 = 0.5736061808310712
				"#OO...##", // 539/936 = 0.5758547008547008
				"O#O##.O#", // 1903/3303 = 0.5761429003935816
				"O####.O#", // 4338/7500 = 0.5784
				"#OO#.OO#", // 1544/2662 = 0.580015026296018
				"#O#O..##", // 6672/11500 = 0.5801739130434782
				"#OO#....", // 1026/1762 = 0.5822928490351873
				"...O.###", // 12688/21773 = 0.5827400909383181
				"O#.O...#", // 7881/13462 = 0.5854256425493983
				"#.O#...#", // 9633/16432 = 0.5862341772151899
				"#.O...O#", // 16301/27554 = 0.591601945271104
				".#..O.O#", // 13811/23249 = 0.5940470557873457
				"..O#####", // 3496/5872 = 0.5953678474114441
				".#O##.O#", // 6949/11519 = 0.6032641722371733
				".###OOOO", // 445/736 = 0.6046195652173914
				"..O#...#", // 38218/62910 = 0.6075027817517088
				"#.#OOO##", // 1344/2212 = 0.6075949367088608
				"#OO#...O", // 2937/4831 = 0.6079486648726972
				"####.OOO", // 8634/14166 = 0.6094875052943668
				"O#.OO###", // 1110/1819 = 0.6102253985706432
				"*.O#**.#", // 20621/33686 = 0.6121534168497299
				".##O.O##", // 5914/9631 = 0.6140587685598587
				"#O.#...O", // 11799/19092 = 0.6180075424261471
				"O#OO.###", // 864/1396 = 0.6189111747851003
				"*..O**##", // 20391/32934 = 0.6191473856804518
				".##OOO##", // 577/928 = 0.6217672413793104
				"#.O#####", // 1849/2965 = 0.6236087689713322
				"O##O..O#", // 2907/4628 = 0.628133102852204
				".OO#.###", // 4150/6602 = 0.62859739472887
				"O##...OO", // 1332/2117 = 0.6291922531884743
				"#O#O.O##", // 2568/4063 = 0.6320452867339404
				"#OO#OO##", // 1049/1658 = 0.6326899879372738
				"O###..OO", // 1499/2364 = 0.6340947546531303
				"O..O####", // 155/244 = 0.6352459016393442
				"#OO#..O#", // 2000/3129 = 0.6391818472355385
				"##O#OOO#", // 1382/2162 = 0.6392229417206291
				"O##OO.O#", // 1442/2236 = 0.644901610017889
				"##O#O###", // 9188/14207 = 0.6467234461884986
				".#OO.###", // 2762/4265 = 0.6475967174677608
				"O####OO#", // 7069/10893 = 0.6489488662443771
				".#OO...#", // 40599/62505 = 0.649532037437005
				"*##O**##", // 2563/3922 = 0.6534931157572667
				"#OO#O..#", // 3194/4874 = 0.6553139105457529
				"##.#O.O#", // 17687/26879 = 0.658022991926783
				"O#.O.###", // 1279/1940 = 0.6592783505154639
				"..#O.O##", // 7940/12011 = 0.6610606943635001
				"..O...O#", // 56180/84905 = 0.6616807019610152
				"##.#OOO#", // 8560/12786 = 0.6694822462067886
				"*##O**.O", // 469/699 = 0.670958512160229
				"#OO#...#", // 5574/8267 = 0.674247006169106
				"#.O#....", // 42351/62794 = 0.6744434181609708
				"#.##..OO", // 34791/50983 = 0.6824039385677578
				".#.OO###", // 2612/3825 = 0.682875816993464
				"O##O..##", // 2944/4310 = 0.683062645011601
				"##O#O.O#", // 2812/4104 = 0.6851851851851852
				"#OO#.O##", // 3208/4680 = 0.6854700854700855
				"O##OOO##", // 2346/3411 = 0.6877748460861918
				".###.OOO", // 7884/11455 = 0.6882584024443474
				"#OO##OO#", // 671/972 = 0.6903292181069959
				"##O#..O#", // 13740/19855 = 0.6920171241500881
				"#OO##..#", // 954/1378 = 0.6923076923076923
				"#.O#O###", // 7406/10683 = 0.6932509594683142
				".#O#..O#", // 19364/27519 = 0.7036592899451288
				".......O", // 60801/86239 = 0.7050290471828291
				"*.#O**##", // 2708/3836 = 0.7059436913451512
				"###OOO##", // 1867/2635 = 0.70853889943074
				"#OO##.O#", // 2128/2981 = 0.7138544112713855
				".#.O.###", // 4150/5761 = 0.7203610484290922
				"##OOO###", // 959/1330 = 0.7210526315789474
				"O###...O", // 31368/42943 = 0.7304566518408122
				"*.O.**.#", // 34491/47093 = 0.732401843161404
				"#OO#..##", // 1659/2254 = 0.7360248447204969
				"..O#..O#", // 43893/59625 = 0.7361509433962264
				"O##O.O##", // 2751/3736 = 0.7363490364025695
				".#.O...#", // 50161/67467 = 0.7434894096373041
				"##O#.OO#", // 14410/19296 = 0.7467868988391376
				"###O.O##", // 9485/12423 = 0.7635031795862514
				"#.O#.###", // 4201/5492 = 0.7649308084486526
				"..#O..##", // 28329/36653 = 0.7728971707636483
				".OO#####", // 927/1199 = 0.7731442869057548
				".#.O..O#", // 44589/57028 = 0.7818790769446587
				"#.#O.O##", // 8817/11276 = 0.7819262149698475
				"..##..OO", // 55899/70980 = 0.7875316990701606
				"##.OO###", // 9034/11405 = 0.7921087242437528
				"#OO#O###", // 5660/7064 = 0.8012457531143827
				"...O..##", // 59242/73689 = 0.8039463149181018
				".###..OO", // 31240/38723 = 0.806755674921881
				"...O....", // 70267/86239 = 0.8147937707997541
				"..OO...#", // 70692/85964 = 0.8223442371225164
				"#OO#.###", // 3985/4819 = 0.826935048765304
				".##O..##", // 3996/4706 = 0.8491287717807054
				"...#..OO", // 69182/78479 = 0.8815351877572344
				"#OO#####", // 1933/2102 = 0.9196003805899143
				"..O#....", // 80403/85839 = 0.9366721420333415
				"..O#...O", // 81527/85961 = 0.9484184688405207
				"...#...O", // 84610/86088 = 0.982831521234086
				"...O...#", // 85634/86084 = 0.994772547744064
//				"O...#O??", // Hane4
//				"#??*?O**", // Edge3
//				"O?+*?#**", // Edge4
//				"O#O*?#**", // Edge5	
		};
		Pattern[] BLACK_GOOD_PATTERNS = new Pattern[colorSpecificPatterns.length];
		Pattern[] WHITE_GOOD_PATTERNS = new Pattern[colorSpecificPatterns.length];
		for (int i = 0; i < BLACK_GOOD_PATTERNS.length; i++) {
			BLACK_GOOD_PATTERNS[i] = new ColorSpecificPattern(colorSpecificPatterns[i], BLACK);
			WHITE_GOOD_PATTERNS[i] = new ColorSpecificPattern(colorSpecificPatterns[i], WHITE);
		}
//		String[] colorIndependentPatterns = {
//				"O..?##??", // Hane1
//				"O...#.??", // Hane2
//				"O#..#???", // Hane3
//				"#OO+??++", // Cut2
//				".O?*#?**", // Edge1
//				"#oO*??**", // Edge2				
//		};
//		Pattern[] INDEPENDENT_GOOD_PATTERNS = new Pattern[colorIndependentPatterns.length + 1];			
//		for (int i = 0; i < colorIndependentPatterns.length; i++) {
//			INDEPENDENT_GOOD_PATTERNS[i] = new SimplePattern(colorIndependentPatterns[i]);
//		}
//		INDEPENDENT_GOOD_PATTERNS[INDEPENDENT_GOOD_PATTERNS.length - 1] = new Cut1Pattern();
		String[] badPatterns = {
				"O.OO?oo?", // Tiger's mouth 
				".#..#.?.", // Empty triangle
				".OO?OO??", // Push through bamboo
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
//			for (Pattern pattern : INDEPENDENT_GOOD_PATTERNS) {
//				if (pattern.matches((char) i)) {
//					GOOD_NEIGHBORHOODS[BLACK].set(i, true);
//					GOOD_NEIGHBORHOODS[WHITE].set(i, true);
//				}
//			}
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
