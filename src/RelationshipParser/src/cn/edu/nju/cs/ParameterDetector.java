package cn.edu.nju.cs;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.jdom2.util.IteratorIterable;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Detect Parameter Relationship Created by wzk on 15-2-2.
 */
public class ParameterDetector {

	final static Namespace UML_Namespace = Namespace
			.getNamespace("org.omg.xmi.namespace.UML");
	String filePath;
	org.jdom2.Document document;
	// xmi.id to java class name map object.
	// you can use this map object to convert xmi.id to java class name
	Map<String, String> idToName;
	// xmi.id to class type map
	Map<String, String> idToClassType;

	PrintStream outputStream;

	/**
	 * filePath=model file path
	 */
	public ParameterDetector(String filePath, PrintStream outputStream) {
		this.filePath = filePath;
		this.outputStream = outputStream;
		// open an JDOM document object to be parsed
		document = XMIFileOpener.openJDOMDocument(filePath);
		// get the xmi.id to java class name map function
		idToName = XMIIDDetector.getIDtoNameMap(filePath);
		idToClassType = XMIIDDetector.getIDtoClassTypeMap(filePath);

	}

	public static void detect(String filePath, PrintStream stream) {
		ParameterDetector detector = new ParameterDetector(filePath, stream);
		detector.detectAllClassInstances();
	}

	public static void main(String[] args) {
		ParameterDetector.detect("model.xml", System.out);
	}
	
	String toClassDescriptionString(String classID) {
		return Utils.generateClassDescriptionString(idToName.get(classID), "", idToClassType.get(classID));
	}

	void parseAnOperationElement(String currentClassID, Element operation) {
		Element parameterList = operation.getChild(
				"BehavioralFeature.parameter", UML_Namespace);
		if(parameterList == null) return;
		List<Element> parameters = parameterList.getChildren("Parameter",
				UML_Namespace);
		for (Element parameter : parameters) {
			if (!parameter.getAttributeValue("kind").equals("in"))
				continue;
			Element parameterType = parameter.getChild("Parameter.type",
					UML_Namespace);
			String typeID = parameterType.getChild("Classifier", UML_Namespace)
					.getAttributeValue("xmi.idref");
			String className = idToName.get(typeID);
			if(className.startsWith("java")) continue;
			if(className.length() < 1) continue;
			outputStream.println("parameter," + toClassDescriptionString(currentClassID) + ","
					+ toClassDescriptionString(typeID) + ", operationID="
					+ operation.getAttributeValue("xmi.id"));
		}
	}

	/**
	 * parse a specific UML:Class element
	 */
	void parseUMLClassInstance(Element element) {

		Element classifer = element.getChild("Classifier.feature",
				UML_Namespace);
		if(classifer == null) return;
		List<Element> operations = classifer.getChildren("Operation",
				UML_Namespace);
		for (Element operation : operations) {
			parseAnOperationElement(
					element.getAttributeValue("xmi.id"),
					operation);
		}

	}

	void detectAllClassInstances() {
		IteratorIterable<Element> list = document.getRootElement()
				.getDescendants(new ElementFilter("Class"));
		int count = 0;
		for (Element element : list) {
			parseUMLClassInstance(element);
			count++;
		}
		System.out.println("Total get " + count + " UML:Class instances.");
		list = document.getRootElement().getDescendants(
				new ElementFilter("Interface"));
		count = 0;
		for (Element element : list) {
			parseUMLClassInstance(element);
			count++;
		}
		System.out.println("Total get " + count + " UML:Interface instances.");

	}

}
