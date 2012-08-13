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
				"########", //0
				"#O######", //1
				"#.######", //2
				"#.#O####", //4
				"#.#.####", //5
				"###.O###", //9
				"###..###", //12
				"#.#..###", //14
				"#.#..#.#", //20
				"O#######", //21
				"OO######", //22
				"O.######", //23
				"OO#O####", //24
				"O.#O####", //25
				"O.#.####", //26
				"OO##O###", //28
				"OO#OO###", //31
				"OO#.O###", //34
				"O###.###", //36
				"OO##.###", //37
				"O.##.###", //38
				"O##O.###", //39
				"OO#O.###", //40
				"O.#O.###", //41
				"O##..###", //42
				"OO#..###", //43
				"O.#..###", //44
				"OO#OO#O#", //48
				"O.#OO#O#", //49
				"OO#O.#O#", //55
				"O###.#.#", //60
				"O.##.#.#", //62
				"O.#..#.#", //65
				".#######", //66
				".O######", //67
				"..######", //68
				".O#O####", //69
				"..#O####", //70
				"..#.####", //71
				".###O###", //72
				"..##O###", //74
				".###.###", //81
				".O##.###", //82
				"..##.###", //83
				".##O.###", //84
				"..#O.###", //86
				".##..###", //87
				".O#..###", //88
				"..#..###", //89
				"..#OO#O#", //94
				".###.#O#", //96
				".O#O.#O#", //100
				".###.#.#", //105
				"O#O#####", //111
				"OOO#####", //112
				"O.O#####", //113
				"OOOO####", //115
				"O.OO####", //116
				"O#O.####", //117
				"OOO.####", //118
				"O.O.####", //119
				"OOOOO###", //121
				"O.OOO###", //122
				"OOO.O###", //124
				"O.O.O###", //125
				"OOO..###", //127
				"O.O..###", //128
				"OOOOO#O#", //139
				"O.OOO#O#", //140
				"OOO.O#O#", //142
				"O.O.O#O#", //143
				"O#O###.#", //147
				"OOO###.#", //148
				"O.O###.#", //149
				"OOOO##.#", //151
				"O#O.##.#", //153
				"OOO.##.#", //154
				"O.O.##.#", //155
				"OOOOO#.#", //157
				"O.OOO#.#", //158
				"OOO.O#.#", //160
				"OOO..#.#", //163
				".#O#####", //165
				".OO#####", //166
				"..O#####", //167
				".OOO####", //169
				"..OO####", //170
				".#O.####", //171
				".OO.####", //172
				"..O.####", //173
				".#O#O###", //174
				".OO#O###", //175
				"..O#O###", //176
				".OOOO###", //178
				".OO.O###", //181
				"..O.O###", //182
				".#O#.###", //183
				"..O#.###", //185
				".OOO.###", //187
				"..OO.###", //188
				".#O..###", //189
				".OO..###", //190
				"..O..###", //191
				"..OOO#O#", //206
				".OO.O#O#", //208
				".#O###.#", //219
				".OO###.#", //220
				"..O###.#", //221
				".#O.##.#", //225
				".OO.##.#", //226
				"..O.##.#", //227
				".OO.O#.#", //235
				".#O#.#.#", //237
				".OO#.#.#", //238
				"..O#.#.#", //239
				".OO..#.#", //244
				".#.#####", //246
				".O.#####", //247
				"...#####", //248
				".#.O####", //249
				".O.O####", //250
				"...O####", //251
				".#..####", //252
				".O..####", //253
				".O..O###", //259
				"....O###", //260
				".#...###", //261
				".O...###", //262
				".....###", //263
				"...OO#O#", //275
				".O..O#O#", //277
				".#.###.#", //282
				".O.###.#", //283
				"...###.#", //284
				".#..##.#", //288
				".O..##.#", //289
				"....##.#", //290
				".O.OO#.#", //292
				".....#.#", //299
				"***#####", //300
				"***O####", //301
				"***.####", //302
				"***.O###", //304
				"***..###", //305
				"***.O#O#", //310
				"***###.#", //312
				"***O##.#", //313
				"***.##.#", //314
				"***..#.#", //317
				"#.O##O##", //320
				"#.O.#O##", //323
				"#.OOOOO#", //339
				"OOOO#O##", //348
				"O.OO#O##", //349
				"OOOOOO##", //355
				"O.OOOO##", //356
				"OOO.OO##", //358
				"OOOO.O##", //364
				"O.OO.O##", //365
				"OOO..O##", //367
				"O.OOOOO#", //373
				"O.O.OOO#", //374
				"OOOO.OO#", //379
				"O.OO.OO#", //380
				"OOO..OO#", //382
				"O.O..OO#", //383
				"OOOO.O.#", //387
				"O.OO.O.#", //388
				".#O##O##", //390
				"..O##O##", //392
				".OOO#O##", //393
				"..OO#O##", //394
				".OOOOO##", //400
				".#O#.O##", //405
				".OOO.O##", //409
				"..OOOOO#", //418
				".OOO.OO#", //424
				"..OO.OO#", //425
				".OO..OO#", //427
				".OO#.O.#", //430
				"..O#.O.#", //431
				".OOO.O.#", //432
				"##.##O##", //435
				"#..##O##", //437
				"##.O#O##", //438
				"##..#O##", //441
				"#...#O##", //443
				"#..#.O##", //452
				"##...O##", //456
				"##.O#OO#", //459
				"##..#OO#", //462
				"#O..OOO#", //468
				"#..O.OO#", //470
				"##..#O.#", //474
				"#...#O.#", //476
				"O#.##O##", //480
				"O#.O#O##", //483
				"OO.O#O##", //484
				"O..O#O##", //485
				"O#..#O##", //486
				"OO..#O##", //487
				"O...#O##", //488
				"OO.OOO##", //493
				"OO.O.O##", //502
				"O..O.O##", //503
				"O#...O##", //504
				"OO...O##", //505
				"O....O##", //506
				"O#.O#OO#", //510
				"OO.O#OO#", //511
				"O..O#OO#", //512
				"O#..#OO#", //513
				"OO..#OO#", //514
				"O..OOOO#", //521
				"OO..OOO#", //523
				"O...OOO#", //524
				"OO.O.OO#", //529
				"O..O.OO#", //530
				"OO...OO#", //532
				"O....OO#", //533
				"O#.##O.#", //534
				"O#.O#O.#", //537
				"OO.O#O.#", //538
				"O..O#O.#", //539
				"O#..#O.#", //540
				"OO..#O.#", //541
				"O...#O.#", //542
				"OO.OOO.#", //547
				"O..OOO.#", //548
				"OO..OO.#", //550
				"OO.O.O.#", //556
				"O..O.O.#", //557
				"OO...O.#", //559
				".#.##O##", //561
				"...##O##", //563
				".#.O#O##", //564
				".O.O#O##", //565
				"...O#O##", //566
				".#..#O##", //567
				"....#O##", //569
				".#.#.O##", //579
				".O.O.O##", //583
				"...O.O##", //584
				".#...O##", //585
				".O...O##", //586
				".....O##", //587
				".#.##OO#", //588
				".#.O#OO#", //591
				".O.O#OO#", //592
				"...O#OO#", //593
				".#..#OO#", //594
				".O.OOOO#", //601
				"...OOOO#", //602
				".O..OOO#", //604
				".O.O.OO#", //610
				"...O.OO#", //611
				".O...OO#", //613
				".#.##O.#", //615
				"...##O.#", //617
				".#.O#O.#", //618
				".O.O#O.#", //619
				"...O#O.#", //620
				".#..#O.#", //621
				"....#O.#", //623
				".O.OOO.#", //628
				"...OOO.#", //629
				".O..OO.#", //631
				"...O.O.#", //638
				"***O.O##", //649
				"***..O##", //650
				"***O#OO#", //652
				"***OOOO#", //655
				"***.OOO#", //656
				"***O.OO#", //658
				"***O#O.#", //661
				"***.#O.#", //662
				"***OOO.#", //664
				"***O.O.#", //667
				"##.##.##", //669
				"#O.##.##", //670
				"#..##.##", //671
				"#...#.##", //674
				"#O.#O.##", //675
				"#..#O.##", //676
				"#..#..##", //683
				"##....##", //686
				"#.....##", //688
				"#..OO.O#", //690
				"O#.##.##", //696
				"OO.##.##", //697
				"O..##.##", //698
				"OO.O#.##", //699
				"O..O#.##", //700
				"O...#.##", //701
				"OO.#O.##", //703
				"OO.OO.##", //706
				"O#.#..##", //711
				"OO.#..##", //712
				"O..#..##", //713
				"OO.O..##", //715
				"O..O..##", //716
				"OO....##", //718
				"O.....##", //719
				"OO.OO.O#", //723
				"O..OO.O#", //724
				"OO.O..O#", //730
				"O..O..O#", //731
				"OO....O#", //733
				"OO.O...#", //738
				"O..O...#", //739
				".#.##.##", //741
				".O.##.##", //742
				"...##.##", //743
				".O.O#.##", //744
				"...O#.##", //745
				"....#.##", //746
				".O.#O.##", //748
				"...#O.##", //749
				".#.#..##", //756
				".O.#..##", //757
				"...#..##", //758
				".O.O..##", //760
				"...O..##", //761
				".O....##", //763
				"......##", //764
				"...OO.O#", //769
				".O.O..O#", //775
				"...O..O#", //776
				".#.#...#", //780
				"***##.##", //786
				"***O#.##", //787
				"***.#.##", //788
				"***#O.##", //789
				"***...##", //794
				"***OO.O#", //799
				"***O..O#", //802
				"***##..#", //804
				"***O#..#", //805
				"***OO..#", //808
				"***#...#", //810
				"***....#", //812
				"****#*##", //813
				"****.*##", //815
				"OOOO#O#O", //822
				"OOOOOO#O", //826
				"O.OOOO#O", //827
				"OOO.OO#O", //829
				"O.OOOOOO", //835
				"O.O.OOOO", //836
				"OOO..OOO", //837
				"O.O..OOO", //838
				".OOOOO#O", //850
				"..OOOO#O", //851
				".#O.OO#O", //852
				".OO.OO#O", //853
				".OOO.O#O", //859
				".#O#OOOO", //864
				".OO#OOOO", //865
				"..O#OOOO", //866
				"..OOOOOO", //868
				"..O.OOOO", //869
				".#O#.OOO", //870
				".OO#.OOO", //871
				"..O#.OOO", //872
				".#OO.OOO", //873
				".OOO.OOO", //874
				"..OO.OOO", //875
				".#O..OOO", //876
				".OO..OOO", //877
				"..O..OOO", //878
				".OOO.O.O", //882
				"..OO.O.O", //883
				".O.OOO#O", //895
				".#.O#OOO", //906
				".O.O#OOO", //907
				"...O#OOO", //908
				".#..#OOO", //909
				"....#OOO", //911
				".#.OOOOO", //912
				"...OOOOO", //914
				".#..OOOO", //915
				".O..OOOO", //916
				"....OOOO", //917
				".#...OOO", //918
				".O...OOO", //919
				".....OOO", //920
				".#.##O.O", //921
				".#.O#O.O", //924
				"...O#O.O", //926
				".#..#O.O", //927
				".#.OOO.O", //930
				".O.OOO.O", //931
				"...OOO.O", //932
				".#..OO.O", //933
				".O..OO.O", //934
				"....OO.O", //935
				".#...O.O", //936
				"***O#OOO", //946
				"***.#OOO", //947
				"***.OOOO", //949
				"***..OOO", //950
				"***O#O.O", //952
				"***OOO.O", //954
				"***.OO.O", //955
				"***..O.O", //956
				"O#.##.#O", //957
				"O..##.#O", //959
				"OO.#O.#O", //963
				"OO.OO.#O", //966
				"O..OO.OO", //978
				"O...O.OO", //979
				"OO....OO", //981
				"O.....OO", //982
				".#.##.#O", //984
				"...##.#O", //986
				".O.#O.#O", //991
				"...#O.#O", //992
				".O.OO.#O", //994
				".O..O.#O", //997
				".#.#..#O", //999
				"...#..#O", //1001
				".#.#O.OO", //1008
				".O.#O.OO", //1009
				"...#O.OO", //1010
				"...OO.OO", //1012
				"....O.OO", //1013
				".#.#..OO", //1014
				".#.O..OO", //1017
				".O.O..OO", //1018
				"...O..OO", //1019
				".#....OO", //1020
				".O....OO", //1021
				"......OO", //1022
				".#.#...O", //1023
				".O.O...O", //1026
				"***##.#O", //1029
				"***#..#O", //1035
				"***#O.OO", //1041
				"***.O.OO", //1043
				"***O..OO", //1045
				"***...OO", //1046
				"***##..O", //1047
				"***#O..O", //1050
				"***OO..O", //1051
				"***.O..O", //1052
				"***#...O", //1053
				"***O...O", //1054
				"****#*#O", //1056
				"****O*#O", //1057
				"****.*OO", //1060
				".#.##.#.", //1062
				".O.##.#.", //1063
				"...##.#.", //1064
				".O.O#.#.", //1065
				"...O#.#.", //1066
				".O..O.#.", //1072
				".#....#.", //1074
				"...OO.O.", //1078
				"....O.O.", //1079
				".O....O.", //1080
				"***##.#.", //1083
				"***O#.#.", //1084
				"***.#.#.", //1085
				"***...#.", //1088
				"***O#.O.", //1090
				"***.O.O.", //1093
				"***...O.", //1094
				"***##...", //1095
				"***.#...", //1097
				"***OO...", //1098
				"***.O...", //1099
				"***.....", //1100
				"****#*#.", //1101
				"****O*#.", //1102
				"****.*#.", //1103
				"****.*O.", //1105
				"****.*..", //1106
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
