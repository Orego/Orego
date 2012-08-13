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
				"###OO###", //6
				"#O#OO###", //7
				"#.#OO###", //8
				"#O#.O###", //10
				"#O#OO#O#", //15
				"#.#.O#O#", //17
				"#O#..#O#", //18
				"#.#..#O#", //19
				"O##OO###", //30
				"O.#OO###", //32
				"O##.O###", //33
				"O.#.O###", //35
				"O###O#O#", //45
				"OO##O#O#", //46
				"O.##O#O#", //47
				"O.#.O#O#", //50
				"OO##.#O#", //52
				"O.##.#O#", //53
				"OO#..#O#", //58
				"O.#..#O#", //59
				"OO##.#.#", //61
				"OO#O.#.#", //63
				"O.#O.#.#", //64
				".##OO###", //75
				".O#OO###", //76
				"..#OO###", //77
				"..#.O###", //80
				".O##O#O#", //91
				"..##O#O#", //92
				"..#.O#O#", //95
				".O##.#O#", //97
				"..##.#O#", //98
				".##O.#O#", //99
				"..#O.#O#", //101
				".O#..#O#", //103
				"..#..#O#", //104
				".O##.#.#", //106
				".O#O.#.#", //108
				"..#O.#.#", //109
				"O#OOO###", //120
				"O#O.O###", //123
				"OOO###O#", //130
				"O.O###O#", //131
				"O#OO##O#", //132
				"OOOO##O#", //133
				"O.OO##O#", //134
				"O#O.##O#", //135
				"OOO.##O#", //136
				"O.O.##O#", //137
				"O#O.O#O#", //141
				"O#O..#O#", //144
				"O.O..#O#", //146
				"O#OO##.#", //150
				"O#OOO#.#", //156
				"O#O.O#.#", //159
				"O.O.O#.#", //161
				"O#O..#.#", //162
				".#OO####", //168
				".#OOO###", //177
				".#OO.###", //186
				".OO###O#", //193
				"..O###O#", //194
				".#OO##O#", //195
				".OOO##O#", //196
				"..OO##O#", //197
				".#O.##O#", //198
				".OO.##O#", //199
				"..O.##O#", //200
				".#OOO#O#", //204
				".#O.O#O#", //207
				".#O#.#O#", //210
				".OO#.#O#", //211
				"..O#.#O#", //212
				".#OO.#O#", //213
				".#O..#O#", //216
				".OO..#O#", //217
				"..O..#O#", //218
				".#OOO#.#", //231
				".#O.O#.#", //234
				".#OO.#.#", //240
				".#.OO###", //255
				"...OO###", //257
				".O.###O#", //265
				"...###O#", //266
				".#.O##O#", //267
				".O.O##O#", //268
				"...O##O#", //269
				".O..##O#", //271
				"....##O#", //272
				".#.OO#O#", //273
				".#..O#O#", //276
				"....O#O#", //278
				".#...#O#", //279
				".O...#O#", //280
				".....#O#", //281
				".#.OO#.#", //291
				"...OO#.#", //293
				".#..O#.#", //294
				"***OO###", //303
				"***###O#", //306
				"***O##O#", //307
				"***.##O#", //308
				"***OO#O#", //309
				"***..#O#", //311
				"***.O#.#", //316
				"#OO##O##", //319
				"#OOO#O##", //321
				"#OO#OO##", //324
				"#.O#OO##", //325
				"##OOOO##", //326
				"#OOOOO##", //327
				"#.OOOO##", //328
				"##O.OO##", //329
				"#OO.OO##", //330
				"#.O.OO##", //331
				"#OOO.O##", //333
				"##O..O##", //335
				"#.O..O##", //337
				"#OOOOOO#", //338
				"#.O.OOO#", //340
				"#OO..OO#", //342
				"#.O..OO#", //343
				"#.O..O.#", //344
				"O#O#OO##", //351
				"OOO#OO##", //352
				"O.O#OO##", //353
				"O#OOOO##", //354
				"O#O.OO##", //357
				"O.O.OO##", //359
				"O.O#.O##", //362
				"O#OO.O##", //363
				"O#O..O##", //366
				"O#O#OOO#", //369
				"O#O#.OO#", //375
				"OOO#.OO#", //376
				"O.O#.OO#", //377
				"O#O..OO#", //381
				"O#O#.O.#", //384
				"OOO#.O.#", //385
				"O.O#.O.#", //386
				"O.O..O.#", //389
				".#O#OO##", //396
				".OO#OO##", //397
				"..O#OO##", //398
				".#OOOO##", //399
				"..OOOO##", //401
				".#O.OO##", //402
				".OO#.O##", //406
				".#OO.O##", //408
				".#O#OOO#", //414
				".OO#OOO#", //415
				"..O#OOO#", //416
				".#O#.OO#", //420
				".OO#.OO#", //421
				"..O#.OO#", //422
				".#OO.OO#", //423
				".#O..OO#", //426
				".OO#.O.#", //430
				"..O#.O.#", //431
				"..O..O.#", //434
				"#O.O#O##", //439
				"#O.#OO##", //444
				"#..#OO##", //445
				"##.OOO##", //446
				"#O.OOO##", //447
				"#..OOO##", //448
				"##..OO##", //449
				"#O..OO##", //450
				"#...OO##", //451
				"#O.O.O##", //454
				"#O..#OO#", //463
				"##..OOO#", //467
				"#...OOO#", //469
				"#O..OO.#", //477
				"#...OO.#", //478
				"#....O.#", //479
				"O#.#OO##", //489
				"OO.#OO##", //490
				"O..#OO##", //491
				"O#.OOO##", //492
				"O#..OO##", //495
				"O...OO##", //497
				"OO.#.O##", //499
				"O#.##OO#", //507
				"OO.##OO#", //508
				"O..##OO#", //509
				"O#.#OOO#", //516
				"O..#OOO#", //518
				"O#.#.OO#", //525
				"O..#.OO#", //527
				"OO.##O.#", //535
				"O..##O.#", //536
				"O#.#OO.#", //543
				"O..#OO.#", //545
				"O#.OOO.#", //546
				"O#..OO.#", //549
				"O#.#.O.#", //552
				"OO.#.O.#", //553
				"O..#.O.#", //554
				".O.#OO##", //571
				"...#OO##", //572
				".#.OOO##", //573
				"...OOO##", //575
				".#..OO##", //576
				"....OO##", //578
				".O.#.O##", //580
				".O.##OO#", //589
				"...##OO#", //590
				".#.#OOO#", //597
				".O.#OOO#", //598
				"...#OOO#", //599
				".#.OOOO#", //600
				".#..OOO#", //603
				".#.#.OO#", //606
				".O.#.OO#", //607
				"...#.OO#", //608
				".O.##O.#", //616
				".O.#OO.#", //625
				".#.OOO.#", //627
				".#..OO.#", //630
				".O.#.O.#", //634
				"...#.O.#", //635
				"***#OO##", //645
				"***OOO##", //646
				"***.OO##", //647
				"***##OO#", //651
				"***#.OO#", //657
				"##.OO.##", //677
				"#O.OO.##", //678
				"#..OO.##", //679
				"#O.O..##", //684
				"#...O.O#", //691
				"#O....O#", //693
				"#.....O#", //694
				"O#.#O.##", //702
				"O#.OO.##", //705
				"O#..O.##", //708
				"O#.#O.O#", //720
				"OO.#O.O#", //721
				"O..#O.O#", //722
				"O#.#..O#", //726
				"OO.#..O#", //727
				"O..#..O#", //728
				"O#.O..O#", //729
				"O#....O#", //732
				".#.OO.##", //750
				"...OO.##", //752
				".#..O.##", //753
				"....O.##", //755
				".#.#O.O#", //765
				".O.#O.O#", //766
				"...#O.O#", //767
				"....O.O#", //770
				".O.#..O#", //772
				"...#..O#", //773
				".#.O..O#", //774
				".O....O#", //778
				"......O#", //779
				"***OO.##", //790
				"***##.O#", //795
				"***#O.O#", //798
				"***#..O#", //801
				"***...O#", //803
				"***.O..#", //809
				"****O*O#", //816
				"O#O##O#O", //819
				"O.O.#O#O", //824
				"O#OOOO#O", //825
				"O#O.OO#O", //828
				"O.O.OO#O", //830
				"O#O..O#O", //831
				"O.O..O#O", //833
				".OO##O#O", //841
				".OOO#O#O", //843
				"..OO#O#O", //844
				"..O.#O#O", //845
				".#O#OO#O", //846
				".OO#OO#O", //847
				"..O#OO#O", //848
				".#OOOO#O", //849
				".#O.OO#O", //852
				".#O#.O#O", //855
				".OO#.O#O", //856
				"..O#.O#O", //857
				".#OO.O#O", //858
				"..OO.O#O", //860
				".#O..O#O", //861
				"..O..O#O", //863
				"..O#.O.O", //881
				".O.##O#O", //886
				".#.O#O#O", //888
				".O.O#O#O", //889
				"...O#O#O", //890
				".O..#O#O", //892
				"....#O#O", //893
				".#.OOO#O", //894
				".#..OO#O", //897
				"....OO#O", //899
				".#...O#O", //900
				".O...O#O", //901
				".....O#O", //902
				".O.##OOO", //904
				"...##OOO", //905
				".O.##O.O", //922
				".O..#O.O", //928
				"***O#O#O", //940
				"***OOO#O", //942
				"***.OO#O", //943
				"***..O#O", //944
				"OO.##.#O", //958
				"OO.O#.#O", //960
				"O..O#.#O", //961
				"O...#.#O", //962
				"O#.OO.#O", //965
				"O#..O.#O", //968
				"O#....#O", //974
				"O.....#O", //976
				"O......O", //983
				".O.##.#O", //985
				".O.O#.#O", //987
				"...O#.#O", //988
				".#.OO.#O", //993
				".#..O.#O", //996
				".#.O..#O", //1002
				"...O..#O", //1004
				"......#O", //1007
				"***OO.#O", //1033
				"***O..#O", //1036
				"...OO.#.", //1070
				".#..O.#.", //1071
				"........", //1082
				"***OO.#.", //1086
				"***##.O.", //1089
		};
		Pattern[] BLACK_GOOD_PATTERNS = new Pattern[colorSpecificPatterns.length];
		Pattern[] WHITE_GOOD_PATTERNS = new Pattern[colorSpecificPatterns.length];
		for (int i = 0; i < BLACK_GOOD_PATTERNS.length; i++) {
			BLACK_GOOD_PATTERNS[i] = new ColorSpecificPattern(colorSpecificPatterns[i], BLACK);
			WHITE_GOOD_PATTERNS[i] = new ColorSpecificPattern(colorSpecificPatterns[i], WHITE);
		}
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
