/******************************************************************************
 * @author Wang Kun
 *         Email: nju.wangkun@gmail.com
 *         Date: Feb 12, 2015
 * @file GeneralizationDetector.java
 * @brief Brief Description
 ******************************************************************************/

package cn.edu.nju.cs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.jdom2.util.IteratorIterable;

public class GeneralizationDetector
{
    /* ========================================================================================== */
    final static Namespace UML_Namespace = Namespace.getNamespace("org.omg.xmi.namespace.UML");
    String                 filePath;
    org.jdom2.Document     document;
    // xmi.id to java class name map object.
    // you can use this map object to convert xmi.id to java class name
    Map<String, String>    idToName;
    // xmi.id to class type map
    Map<String, String>    idToClassType;
    // record current UML:Usage node xmi.id
    String                 currentUsageXMIID;
    // output to outer string
    PrintStream            outputStream;
    
    /* ========================================================================================== */
    
    public static void main(String[] args) throws FileNotFoundException
    {
        GeneralizationDetector.detect("model.xml", new PrintStream(new File(
                "relationship.csv")));
    }
    
    public GeneralizationDetector(String filePath, PrintStream outputStream)
    {
        this.filePath = filePath;
        this.outputStream = outputStream;
        // open an JDOM document object to be parsed
        document = XMIFileOpener.openJDOMDocument(filePath);
        // get the xmi.id to java class name map function
        idToName = XMIIDDetector.getIDtoNameMap(filePath);
        idToClassType = XMIIDDetector.getIDtoClassTypeMap(filePath);
    }
    
    public static void detect(String filePath, PrintStream stream)
    {
        GeneralizationDetector detector = new GeneralizationDetector(filePath, stream);
        detector.detectAllGeneralizations();
    }
    
    String parseClassInfo(String classXMIID)
    {
        String className = idToName.get(classXMIID);
        String classType = idToClassType.get(classXMIID);
        
        return Utils.generateClassDescriptionString(className, classXMIID, classType);
    }
    
    /**
     * Detect a specific UML:Generalization Node
     */
    void detectGeneralizationInstance(Element node)
    {
        // Get UML:Association XMI ID
        currentUsageXMIID = node.getAttributeValue("xmi.id");
        
        // get visibility mask
        String visibility = node.getChild("ModelElement.visibility", UML_Namespace)
                .getAttributeValue("xmi.value");
        
        // get child of Generalization instance (i.e., the class who extends another class)
        Element childClassElement = node.getChild("Generalization.child", UML_Namespace).getChild(
                "GeneralizableElement", UML_Namespace);
        String childClassXMIID = childClassElement.getAttributeValue("xmi.idref");
        
        // get parent of Generalization instance (i.e., the class who is extended by another class)
        Element parentClassElement = node.getChild("Generalization.parent", UML_Namespace)
                .getChild("GeneralizableElement", UML_Namespace);
        String parentClassXMIID = parentClassElement.getAttributeValue("xmi.idref");
        
        StringBuilder outputRecord = new StringBuilder("generalization");
        outputRecord.append("," + parseClassInfo(childClassXMIID));
        outputRecord.append("," + parseClassInfo(parentClassXMIID));
        outputRecord.append(",id=" + currentUsageXMIID);
        outputRecord.append(",visibility=" + visibility);
        
       // System.out.println(outputRecord.toString());
        outputStream.println(outputRecord.toString());
    }
    
    boolean isGeneralizationInstance(Element node)
    {
        String generalizationXMIID = node.getAttributeValue("xmi.id");
        if (generalizationXMIID == null)
            return false;
        else
            return true;
        
    }
    
    void detectAllGeneralizations()
    {
        IteratorIterable<Element> list = document.getRootElement().getDescendants(
                new ElementFilter("Generalization"));
        int count = 0;
        for (Element element : list)
        {
            if (isGeneralizationInstance(element))
            {
                detectGeneralizationInstance(element);
                count++;
            }
        }
        System.out.println("Total get " + count + " generalization relations.");
    }
    
}
