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
 * Detect Nested Relationship
 * Created by wzk on 15-2-2.
 */
public class NestedDetector {

    final static Namespace UML_Namespace = Namespace.getNamespace("org.omg.xmi.namespace.UML");
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
    public NestedDetector(String filePath, PrintStream outputStream) {
        this.filePath = filePath;
        this.outputStream = outputStream;
        // open an JDOM document object to be parsed
        document = XMIFileOpener.openJDOMDocument(filePath);
        // get the xmi.id to java class name map function
        idToName = XMIIDDetector.getIDtoNameMap(filePath);
        idToClassType = XMIIDDetector.getIDtoClassTypeMap(filePath);
        
    }

    public static void detect(String filePath, PrintStream stream) {
        NestedDetector detector = new NestedDetector(filePath, stream);
        detector.detectAllClassInstances();
    }

    public static void main(String[] args) {
        NestedDetector.detect("model.xml", System.out);
    }

    /**
     * parse a specific UML:Class element
     */
    void parseUMLClassInstance(Element element) {
    	
    	String callerClassString = Utils.generateClassDescriptionString(
    			idToName.get(element.getAttributeValue("xmi.id")),
    			element.getAttributeValue("xmi.id"),
    			idToClassType.get(element.getAttributeValue("xmi.id"))
    			);
        
    	// ownedElement Element
    	Element ownedElement = element.getChild("Namespace.ownedElement", UML_Namespace);
    	if(ownedElement == null) return; // no need to detect
    	// Detect all inner class elements
    	List<Element> innerClassesNode = ownedElement.getChildren("Class", UML_Namespace);
    	for(Element innerClass : innerClassesNode) {
    		StringBuilder outputRecord = new StringBuilder("nested");
    		String calleeClassName = idToName.get(innerClass.getAttributeValue("xmi.id"));
    		String calleeXMIID = innerClass.getAttributeValue("xmi.id");
    		String calleeClassType = idToClassType.get(calleeXMIID);
    		String calleeClassString = Utils.generateClassDescriptionString(calleeClassName, calleeXMIID, calleeClassType);
    		outputRecord.append("," + callerClassString + "," + calleeClassString);
    		if(!(callerClassString.startsWith("java") && callerClassString.indexOf('.') == 4) )
    			outputStream.println(outputRecord.toString());    		
    	}
    }


    void detectAllClassInstances() {
        IteratorIterable<Element> list = document.getRootElement().getDescendants(new ElementFilter("Class"));
        int count = 0;
        for (Element element : list) {
        	parseUMLClassInstance(element);
            count++;
        }
        System.out.println("Total get " + count + " UML:Class instances.");
    }

}
