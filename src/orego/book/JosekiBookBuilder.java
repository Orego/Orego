package orego.book;

import java.io.IOException;
import static orego.experiment.Debug.*;

/**
 * Reads in a directory of files containing SGF games, then stores the
 * information into a map, which is then saved in a file. The file can be read
 * by JosekiBook.
 */
public class JosekiBookBuilder extends JosekiBuilder {

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		setDebugToStderr(true);
		JosekiBookBuilder builder = new JosekiBookBuilder(10);
		// Uncomment this to build the book from scratch.
		builder.buildRawBook("SgfFiles");
		builder.buildFinalBook("SgfFiles");
	}

	public JosekiBookBuilder(int manyTimes) {
		super(manyTimes);
	}

	public Object computeFinalEntries() {
		getBigMap().put(0L, null);
		getBigMap().put(~0L, null);
		findHighestCounts();
		return getFinalMap();
	}

	public String getFinalBookName() {
		return "Joseki";
	}

}
