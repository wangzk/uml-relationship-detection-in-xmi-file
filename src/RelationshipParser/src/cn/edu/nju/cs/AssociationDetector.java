package cn.edu.nju.cs;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.AttributeFilter;
import org.jdom2.filter.ElementFilter;
import org.jdom2.util.IteratorIterable;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

/**
 * Detect Association Relationship
 * Created by bsidb on 15-2-2.
 */
public class AssociationDetector {

    final static Namespace UML_Namespace = Namespace.getNamespace("org.omg.xmi.namespace.UML");
    String filePath;
    org.jdom2.Document document;
    Map<String, String> idToName;

    public AssociationDetector(String filePath) {
        this.filePath = filePath;
        document = XMIFileOpener.openJDOMDocument(filePath);

        idToName = XMIIDDetector.getIDtoNameMap(filePath);
    }


    String getVisibilityMark(String visilibity) {
        if(visilibity.equals("private")) return "-";
        if(visilibity.equals("public")) return "+";
        if(visilibity.equals("protected")) return "#";
        return "?";
    }

    /** parse a specific UML:AssociationEnd element */
    void parseAssociationEndInstance(Element element) {
        assert(element.getName().equals("AssociationEnd"));

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
        System.out.println(sourceClassName + "(" + vMark +  memberName + ")" + " -> " + targetClassName);
    }

    /** Detect a specific UML:AssociationConnection Node */
    void detectAssociationConnectionInstance(Element node) {
        List<Element> children;
        children = node.getChildren(); // get two children of this node
        Element withNameElement = null;
        // get two children elements' name attribute
        Attribute firstName = children.get(0).getAttribute("name");
        Attribute lastName = children.get(1).getAttribute("name");
        // the children with valid name attribute stores the connection info
        if(firstName != null && firstName.getValue().length() > 0) {
            withNameElement = children.get(0);
            System.out.println("1 get");
        }
        else if (lastName != null && lastName.getValue().length() > 0 ) {
            withNameElement = children.get(1);
        }
        // if no children stores the info, the info must be stored in the second children's reference node
        if(withNameElement == null) {
            // we need to get the reference ID to UML:AssociationEnd element of second children.
            String refID = children.get(1).getAttributeValue("xmi.idref");
            if(refID == null) {
                System.err.println("!!!! ERROR!");
            }
            // now we need to find the node with that ID
            IteratorIterable<Element> it = document.getRootElement().getDescendants(new ElementFilter("AssociationEnd"));
            for(Element element : it) {
                if(element.getAttributeValue("xmi.id").equals(refID)) {
                    withNameElement = element;
                    break;
                }
            }
            if(withNameElement == null) {
                System.err.println("Error ref ID!");
                System.exit(1);
            }
        }
        // Now we get the AssociationEnd with Name
        String targetEndName = withNameElement.getAttributeValue("name");
        parseAssociationEndInstance(withNameElement);
    }

    /*
    void detectAnAssociationConnectionInstance(Node node) {
        NodeList children = node.getChildNodes();
        List<Node> validChildrenList = Utils.filterTextNode(children);
        if(validChildrenList.size() != 2) {
            System.out.println("ERROR! " + validChildrenList.size());
            System.out.println(node.getParentNode().getAttributes().getNamedItem("xmi.id").getNodeValue());
            return;
        }
        Node firstEnd = validChildrenList.get(0);
        String firstName = Utils.getNodeAttributeValue(firstEnd, "name");
        Node lastEnd = validChildrenList.get(1);
        String lastName = Utils.getNodeAttributeValue(lastEnd, "name");
        if(firstName != null && firstName.length() > 0) {
            System.out.println("1 has the name");
        }
        if(lastName != null && lastName.length() > 0) {
            System.out.println("2 has the name");
            // Detect Specific Node
        } else {
            System.out.println("None has the name!");
            //System.out.println(Utils.getNodeAttributeValue(node.getParentNode(), "xmi.id"));
            String refID = Utils.getNodeAttributeValue(lastEnd,"xmi.idref");
            System.out.println(refID);
            lastEnd = Utils.getNodeByAttributeValue(document, "UML:AssociationEnd", "xmi.id", refID);
            System.out.println(lastEnd.getNodeName());

        }

        System.out.println("---");


    }
    */

    void detectAllAssociation() {
        IteratorIterable<Element> list = document.getRootElement().getDescendants(new ElementFilter("Association.connection"));
        System.out.println(list.hasNext());
        int count = 0;
        for(Element element : list) {
            detectAssociationConnectionInstance(element);
            count++;
        }
        System.out.println("Total get " + count + " association relathions.");
        //NodeList list = document.getElementsByTagName("UML:Association.connection");
        /*
        for(int i = 0; i < list.size(); i++) {
            Element node = list.get(i);
            System.out.println(node.getName());
            //detectAnAssociationConnectionInstance(node);
        }*/
    }




    public static void detect(String filePath) {
        AssociationDetector detector = new AssociationDetector(filePath);
        detector.detectAllAssociation();
    }

    public static void main(String[] args) {
        AssociationDetector.detect("model.xml");
    }

}
