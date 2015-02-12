package cn.edu.nju.cs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class DetectAllRelationships {

	/**
	 * 
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		String modelFileName = args[0];
		PrintStream fileStream = new PrintStream(new File("relationship.csv"));
		System.out.println("Start detection.");
		AssociationDetector.detect(modelFileName, fileStream);
		NestedDetector.detect(modelFileName, fileStream);
		ParameterDetector.detect(modelFileName, fileStream);

		GeneralizationDetector.detect(modelFileName, fileStream);
		RealizationDetector.detect(modelFileName, fileStream);
		
		System.out.println("Done! The output file is relationship.csv.");
	}

}
