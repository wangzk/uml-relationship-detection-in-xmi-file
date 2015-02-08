package cn.edu.nju.cs;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class XMIIDDetector {

    final static String ATTRIBUTES = "UML:Attribute";
    final static String METHODS = "UML:Operation";
    final static String CLASSES = "UML:Classifier.feature";
    final static String COUPLINGS = "UML:AssociationEnd";
    final static String CLASS_INHERITANCES = "UML:GeneralizableElement.generalization";
    final static String FILE_ADDRESS = "model.xml";
    final static String PACKAGE = "UML:Package";
    final static String INTERFACE = "UML:Interface";
    final static String CLASS = "UML:Class";

    public static HashMap<String, String> idToName = new HashMap();
    public static HashMap<String, String> idToClassType = new HashMap();

    public static String currentPackageName = "";

    /**
     * add a package name into 'currentPackageName'.
     * for example, before calling this function, currentPackageName is "a.b",
     * after calling it with name='c', currentPackageName will become "a.b.c"
     *
     * @param name the new package name to be added
     */
    public static void addPackagePath(String name) {
        if (currentPackageName.length() == 0)
            currentPackageName = name;
        else
            currentPackageName = currentPackageName + "." + name;
    }

    /**
     * for example, before calling this function, currentPackageName is "a.b.c",
     * after calling this function, currentPackageName will become "a.b"
     */
    public static void upPackagePath() {
    	// remove the tailing symbol
    	currentPackageName = currentPackageName.substring(0, currentPackageName.length() - 1);
        int pos1 = currentPackageName.lastIndexOf('.');
        int pos2 = currentPackageName.lastIndexOf('$');
        int pos = (pos1 > pos2)? pos1 : pos2;
        if (pos > 0) {
            currentPackageName = currentPackageName.substring(0, pos + 1);
        } else {
            currentPackageName = "";
        }
    }


    public static void printDOM2XMLFile(Document doc, String filePath) {
        try {
            TransformerFactory transformerFactory = TransformerFactory
                                                            .newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            PrintStream outStream = new PrintStream(new File(filePath));
            StreamResult consoleResult = new StreamResult(outStream);
            transformer.transform(source, consoleResult);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void navigateTree(org.w3c.dom.Node node, int depth) {

        if (node.getNodeName().equals(CLASS)) {
            //    System.out.print("!");
        }
        if (node.getNodeType() == Node.TEXT_NODE) {

        } else {
            /*
            for (int i = 0; i < depth; i++)
				System.out.print(" ");
				*/

        }
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            Node xmiID = attributes.getNamedItem("xmi.id");
            Node name = attributes.getNamedItem("name");
            String parentName = node.getNodeName();
            if (xmiID != null) {
                String newName = null;
                String ID = xmiID.getNodeValue();
                if (name != null && name.getNodeValue().length() > 0) {
                    newName = xmiID.getNodeValue().split("-")[0] + " - "
                                      + name.getNodeValue();
                    // We have special treatment for UML:Class Node
                    if (node.getNodeName().equals(CLASS) || node.getNodeName().equals(INTERFACE)) {
                    		newName = currentPackageName + name.getNodeValue();
                            //System.out.println("Found Class: " + newName);
                    }

                } else {
                    newName = xmiID.getNodeValue().split("-")[0] + " - ["
                                      + parentName + "]";
                    //	System.out.println(newName);
                }
                idToName.put(ID, newName);
                if (node.getNodeName().equals(CLASS)) {
                    idToClassType.put(ID, "Class");
                    currentPackageName = newName + "$";
                } else if (node.getNodeName().equals(INTERFACE)) {
                    idToClassType.put(ID, "Interface");
                    currentPackageName = newName + "$";
                }
                if (node.getNodeName().equals(PACKAGE)) {
                    String packageName = name.getNodeValue();
                    
                    currentPackageName = currentPackageName + packageName + ".";
                    //System.out.println("In Package: " + currentPackageName);
                }
            }
        }

        NodeList list = node.getChildNodes();
        int length = list.getLength();
        for (int i = 0; i < length; i++) {
            navigateTree(list.item(i), depth + 1);
        }

        if (node.getNodeName().equals(PACKAGE)) {
            upPackagePath();
        }
        if (node.getNodeName().equals(CLASS) || node.getNodeName().equals(INTERFACE)) {
           upPackagePath();
        }
    }

    public static void changeXMIRefIDtoName(org.w3c.dom.Node node) {

        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            Node xmiIDRef = attributes.getNamedItem("xmi.idref");
            if (xmiIDRef != null) {
                String currentID = xmiIDRef.getNodeValue();
                String newName = idToName.get(currentID);
                xmiIDRef.setNodeValue(newName);
            }
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            changeXMIRefIDtoName(list.item(i));
        }
    }

    /**
     * Get the XMI ID to java class name HashMap data structure
     *
     * @param filePath model file path
     * @return the map from XMI.ID to Java class name
     */
    public static Map<String, String> getIDtoNameMap(String filePath) {
        try {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                                                               .newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(filePath));

            navigateTree(doc, 0);
            return idToName;

        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line "
                                       + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    /**
     * Get the XMI ID to Class Type(Class or Interface) HashMap data structure
     *
     * @param filePath model file path
     * @return the map from XMI.ID to Class Type
     */
    public static Map<String, String> getIDtoClassTypeMap(String filePath) {
        try {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                                                               .newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(filePath));
            navigateTree(doc, 0);
            return idToClassType;

        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line "
                                       + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }




    public static void main(String[] args) {

        try {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                                                               .newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(FILE_ADDRESS));
            // normalize text representation
            // doc.getDocumentElement().normalize();
            System.out.println("==============================");
            System.out.println("Root element of the doc is "
                                       + doc.getDocumentElement().getNodeName());

            NodeList listClass = doc.getElementsByTagName(CLASSES);
            int totalClass = listClass.getLength();
            System.out.println("Total Class : " + totalClass);

            NodeList listAttributes = doc.getElementsByTagName(ATTRIBUTES);
            int totalAttributes = listAttributes.getLength();
            System.out.println("Total Attribute : " + totalAttributes);

            navigateTree(doc, 0);
            changeXMIRefIDtoName(doc);

            printDOM2XMLFile(doc, "model.withName.xml");

        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line "
                                       + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
