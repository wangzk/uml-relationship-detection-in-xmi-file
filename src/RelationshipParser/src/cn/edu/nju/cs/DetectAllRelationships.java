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
		PrintStream fileStream = new PrintStream(new File("relathionship.csv"));
		AssociationDetector.detect(modelFileName, fileStream);
		NestedDetector.detect(modelFileName, fileStream);
		ParameterDetector.detect(modelFileName, fileStream);

	}

}
