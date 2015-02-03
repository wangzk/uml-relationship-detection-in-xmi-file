package cn.edu.nju.cs;

import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.util.IteratorIterable;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * Detect Association Relationship
 * Created by bsidb on 15-2-2.
 */
public class AssociationDetector {

    String filePath;
    org.jdom2.Document document;
    Map<String, String> idToName;

    public AssociationDetector(String filePath) {
        this.filePath = filePath;
        document = XMIFileOpener.openJDOMDocument(filePath);

        idToName = XMIIDDetector.getIDtoNameMap(filePath);
    }


    void detectAnAssociationEndInstance(Node node) {

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
        for(Element element : list) {
            System.out.println(element.getName());
        }
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
