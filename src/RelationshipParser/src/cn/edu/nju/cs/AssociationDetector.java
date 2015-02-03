package cn.edu.nju.cs;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.jdom2.util.IteratorIterable;

import java.util.List;
import java.util.Map;

/**
 * Detect Association Relationship
 * Created by wzk on 15-2-2.
 */
public class AssociationDetector {

    final static Namespace UML_Namespace = Namespace.getNamespace("org.omg.xmi.namespace.UML");
    String filePath;
    org.jdom2.Document document;
    // xmi.id to java class name map object.
    // you can use this map object to convert xmi.id to java class name
    Map<String, String> idToName;
    // record current UML:Association node xmi.id
    String currentAssociationXMIID;

    /**
     * filePath=model file path
     */
    public AssociationDetector(String filePath) {
        this.filePath = filePath;
        // open an JDOM document object to be parsed
        document = XMIFileOpener.openJDOMDocument(filePath);
        // get the xmi.id to java class name map function
        idToName = XMIIDDetector.getIDtoNameMap(filePath);
    }

    public static void detect(String filePath) {
        AssociationDetector detector = new AssociationDetector(filePath);
        detector.detectAllAssociation();
    }

    public static void main(String[] args) {
        AssociationDetector.detect("model.xml");
    }

    String getVisibilityMark(String visilibity) {
        if (visilibity.equals("private")) return "-";
        if (visilibity.equals("public")) return "+";
        if (visilibity.equals("protected")) return "#";
        return "?";
    }

    /**
     * parse a specific UML:AssociationEnd element
     */
    void parseAssociationEndInstance(Element element) {
        assert (element.getName().equals("AssociationEnd"));

        // Source Detection
        Element sourceClassElement = element.getChild("Feature.owner", UML_Namespace).getChild("Classifier", UML_Namespace);
        String sourceClassXMIID = sourceClassElement.getAttributeValue("xmi.idref");
        String sourceClassName = idToName.get(sourceClassXMIID);
        Element targetClassElement = element.getChild("AssociationEnd.participant", UML_Namespace).getChild("Classifier", UML_Namespace);
        // Target Detection
        String targetClassXMIID = targetClassElement.getAttributeValue("xmi.idref");
        String targetClassName = idToName.get(targetClassXMIID);
        // Additional info Detection
        String memberName = element.getAttributeValue("name");
        String visibility = element.getChild("AssociationEnd.visibility", UML_Namespace).getAttributeValue("xmi.value");
        String vMark = getVisibilityMark(visibility);
        System.out.println(sourceClassName + "(" + vMark + memberName + ")" + " -> " + targetClassName + ", " + currentAssociationXMIID);
    }

    /**
     * Detect a specific UML:AssociationConnection Node
     */
    void detectAssociationConnectionInstance(Element node) {
        // Get UML:Association XMI ID
        Element parent = node.getParentElement();
        currentAssociationXMIID = parent.getAttributeValue("xmi.id");
        List<Element> children;
        children = node.getChildren(); // get two children of this node
        Element withNameElement = null;
        // get two children elements' name attribute
        Attribute firstName = children.get(0).getAttribute("name");
        Attribute lastName = children.get(1).getAttribute("name");
        // the children with valid name attribute stores the connection info
        if (firstName != null && firstName.getValue().length() > 0) {
            withNameElement = children.get(0);
            System.out.println("1 get");
        } else if (lastName != null && lastName.getValue().length() > 0) {
            withNameElement = children.get(1);
        }
        // if no children stores the info, the info must be stored in the second children's reference node
        if (withNameElement == null) {
            // we need to get the reference ID to UML:AssociationEnd element of second children.
            String refID = children.get(1).getAttributeValue("xmi.idref");
            if (refID == null) {
                System.err.println("!!!! ERROR!");
            }
            // now we need to find the node with that ID
            IteratorIterable<Element> it = document.getRootElement().getDescendants(new ElementFilter("AssociationEnd"));
            for (Element element : it) {
                if (element.getAttributeValue("xmi.id").equals(refID)) {
                    withNameElement = element;
                    break;
                }
            }
            if (withNameElement == null) {
                System.err.println("Error ref ID!");
                System.exit(1);
            }
        }
        // Now we get the AssociationEnd with Name
        String targetEndName = withNameElement.getAttributeValue("name");
        parseAssociationEndInstance(withNameElement);
    }

    void detectAllAssociation() {
        IteratorIterable<Element> list = document.getRootElement().getDescendants(new ElementFilter("Association.connection"));
        int count = 0;
        for (Element element : list) {
            detectAssociationConnectionInstance(element);
            count++;
        }
        System.out.println("Total get " + count + " association relathions.");
    }

}
