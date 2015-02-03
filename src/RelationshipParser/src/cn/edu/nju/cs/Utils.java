package cn.edu.nju.cs;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by bsidb on 15-2-2.
 */
public class Utils {

    /**
     * filter all #text nodes out
     */
    public static List<Node> filterTextNode(NodeList list) {
        LinkedList<Node> newList = new LinkedList<Node>();
        for (int i = 0; i < list.getLength(); i++) {
            if (!list.item(i).getNodeName().equals("#text")) {
                newList.add(list.item(i));
            }
        }
        return newList;
    }

    public static String getNodeAttributeValue(Node node, String attribute) {
        String value = null;
        NamedNodeMap attributes = node.getAttributes();
        /*
        for(int i = 0 ; i < attributes.getLength(); i++) {
            //System.out.println(attributes.item(i).getNodeName());
            if(attributes.item(i).getNodeName().equals("xmi.id")) {
                System.out.println(attributes.item(i).getNodeValue());
                String id = attributes.item(i).getNodeValue();
                if(id.startsWith("3ij")) {
                    System.out.println("!!!!!!!!!");
                }
            }
        }
        */
        Node valueNode = attributes.getNamedItem(attribute);
        if (valueNode != null)
            value = valueNode.getNodeValue();
        return value;
    }


    public static Node getNodeByAttributeValue(Document doc, String tag, String attribute, String value) {
        NodeList list = doc.getElementsByTagName(tag);
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (Utils.getNodeAttributeValue(node, attribute) != null
                        &&
                        Utils.getNodeAttributeValue(node, attribute).equals(value))
                return node;
        }
        return null;
    }
}
