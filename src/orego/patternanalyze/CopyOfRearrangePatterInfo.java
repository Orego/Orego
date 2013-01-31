package orego.patternanalyze;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class CopyOfRearrangePatterInfo {

	public static void main(String[] args) throws IOException {
		String fileName = "testFiles/outputRatio8.txt";
		String outputFile = "testFiles/newnewOutputRatio8.txt";
		BufferedReader input = new BufferedReader(new FileReader(fileName));
		BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
		while (input.ready()) {
			String line = input.readLine();
			if (line != null) {
				if (line.endsWith("#")) {
					int lastIndex = line.lastIndexOf(" ");
					String front = line.substring(0, lastIndex);
					String back = line.substring(lastIndex + 1, line.length() - 2);
					
//					output.write("\"");
					output.write(back.charAt(4));
					output.write(back.charAt(0));
					output.write(back.charAt(5) + "\n");
					output.write(back.charAt(3) + " ");
					output.write(back.charAt(1) + "\n");
					output.write(back.charAt(7));
					output.write(back.charAt(2));
					output.write(back.charAt(6));


					output.write("\", // " +  front + "\n\n");
				}
			}
		}

		input.close();
		output.close();
	}

}
