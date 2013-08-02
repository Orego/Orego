package orego.shape;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Extracts patterns from SGF files.
 */
public class TestEvaluator {

	private double[][][][][] array; //[threshold][initNoise][finalNoise][decay][cutoff]
	
	public static void main(String[] args) {
//		new TestEvaluator().run(orego.experiment.Debug.OREGO_ROOT_DIRECTORY + "SgfTestFiles" + File.separator);
		new TestEvaluator().run("/Network/Servers/maccsserver.lclark.edu/Users/lvonessen/Desktop/");
	}

	/**
	 * Takes a directory of index#.html files and walks through them, looking
	 * for the number of tests that passed
	 */
	public void run(String directory) {
		array = new double[3][2][3][3][3];
		for (int i = 0; i < 162; i++) {
			try {
				File file = new File(directory + File.separator + "indexfiles8-1a/index" + i
						+ ".html");
				double passes = lookForPasses(file);
				file = new File(directory + File.separator + "indexfiles8-1b/index" + i
						+ ".html");
				passes = (passes + lookForPasses(file))/2.0;
				System.out.println(i + " " + passes);
				store(i,passes);
			} catch (Exception e) {
				System.out.println(i + " is missing");
			}
		}
		printArray();
	}

	private void printArray() {
		// [threshold][initNoise][finalNoise][decay][cutoff]
		System.out.println("            thr     thr     thr\n" +
						   "            cut cut cut cut cut cut cut cut cut");
		for (int initNoise = 0; initNoise < 2; initNoise++) {
			System.out.print("beg ");
			for (int finalNoise = 0; finalNoise < 3; finalNoise++) {
				if (finalNoise != 0){
					System.out.print("    ");
				}
				System.out.print("fin ");
				for (int decay = 0; decay < 3; decay++) {
					if (decay != 0){
						System.out.print("        ");
					}
					System.out.print("dec ");
					for (int threshold = 0; threshold < 3; threshold++) {
						for (int cutoff = 0; cutoff < 3; cutoff++) {
							if (array[threshold][initNoise][finalNoise][decay][cutoff] == 0)
								System.out.print("--- ");
							else
								System.out
									.print(array[threshold][initNoise][finalNoise][decay][cutoff]
											+ " ");
						}
					}
					System.out.println();
				}
			}
		}
	}

	private void store(int index, double passes) {
		//array[threshold][initNoise][finalNoise][decay][cutoff]
		int threshold = index/54;
		index%=54;
		int initialNoise = index/27;
		index%=27;
		int finalNoise = index/9;
		index%=9;
		int noiseDecay = index/3;
		int cutOff = index%3;
		array[threshold][initialNoise][finalNoise][noiseDecay][cutOff] = passes;
	}

	/**
	 * Check for the patterns in a particular file.
	 */
	public int lookForPasses(File file) throws FileNotFoundException {
		Scanner s=null;
		s = new Scanner (file);
		String string="";
		while (s.hasNextLine()) {
			string = s.nextLine();
			if (string.contains("542")){
				break;
			}
		}
		for (int i=0; i<4; i++){
			s.nextLine();
		}
		StringTokenizer stoken = new StringTokenizer(s.nextLine(),"<>td/");
		s.close();
		return Integer.parseInt(stoken.nextToken());
	}
}
