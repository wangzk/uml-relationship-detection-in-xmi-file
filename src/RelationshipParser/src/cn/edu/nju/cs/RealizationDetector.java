/******************************************************************************
 * @author Wang Kun
 *         Email: nju.wangkun@gmail.com
 *         Date: Feb 11, 2015
 * @file RealizationDetector.java
 * @brief Brief Description
 ******************************************************************************/

package cn.edu.nju.cs;

import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.jdom2.util.IteratorIterable;

public class RealizationDetector
{
    /* ========================================================================================== */
    final static Namespace UML_Namespace  = Namespace.getNamespace("org.omg.xmi.namespace.UML");
    final static Namespace JUDE_Namespace = Namespace
                                                  .getNamespace("http://objectclub.esm.co.jp/Jude/namespace/");
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
        RealizationDetector.detect("model.xml", new PrintStream(new File("relationship.csv")));
    }
    
    public RealizationDetector(String filePath, PrintStream outputStream)
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
        RealizationDetector detector = new RealizationDetector(filePath, stream);
        detector.detectAllUsages();
    }
    
    String parseStereotype(String stereotypeXMIID)
    {
        return idToName.get(stereotypeXMIID).split("-")[1].trim();
    }
    
    String parseClassInfo(String classXMIID)
    {
        String className = idToName.get(classXMIID);
        String classType = idToClassType.get(classXMIID);
        
        return Utils.generateClassDescriptionString(className, classXMIID, classType);
    }
    
    /**
     * Detect a specific UML:Usage Node
     */
    void detectUsageInstance(Element node)
    {
        // Get UML:Association XMI ID
        currentUsageXMIID = node.getAttributeValue("xmi.id");
        
        // get visibility mask
        String visibility = node.getChild("ModelElement.visibility", UML_Namespace)
                .getAttributeValue("xmi.value");
        
        // get the stereotype of Usage instance
        Element stereotypeElement = node.getChild("ModelElement.stereotype", UML_Namespace)
                .getChild("Stereotype", UML_Namespace);
        String stereotypeXMIID = null;
        if (null != stereotypeElement)
            stereotypeXMIID = stereotypeElement.getAttributeValue("xmi.idref");
        
        // get the client of Usage instance (i.e., the class who realize other classes)
        Element clientClassElement = node.getChild("Dependency.client", UML_Namespace).getChild(
                "ModelElement", JUDE_Namespace);
        String clientClassXMIID = clientClassElement.getAttributeValue("xmi.idref");
        
        // get the supplier of Usage instance (i.e., the class who is realized by another class)
        Element supplierClassElement = node.getChild("Dependency.supplier", UML_Namespace)
                .getChild("ModelElement", JUDE_Namespace);
        String supplierClassXMIID = supplierClassElement.getAttributeValue("xmi.idref");
        
        StringBuilder outputRecord = new StringBuilder("realization");
        outputRecord.append("," + parseClassInfo(clientClassXMIID));
        outputRecord.append("," + parseClassInfo(supplierClassXMIID));
        outputRecord.append(",id=" + currentUsageXMIID);
        outputRecord.append(",visibility=" + visibility);
       
      //  System.out.println(outputRecord.toString());
        outputStream.println(outputRecord.toString());
    }
    
    boolean isUsageInstance(Element node)
    {
        String usageXMIID = node.getAttributeValue("xmi.id");
        if (usageXMIID == null)
            return false;
        else
            return true;
        
    }
    
    void detectAllUsages()
    {
        IteratorIterable<Element> list = document.getRootElement().getDescendants(
                new ElementFilter("Usage"));
        int count = 0;
        for (Element element : list)
        {
            if (isUsageInstance(element))
            {
                detectUsageInstance(element);
                count++;
            }
        }
        System.out.println("Total get " + count + " usage relations.");
    }
    
}
