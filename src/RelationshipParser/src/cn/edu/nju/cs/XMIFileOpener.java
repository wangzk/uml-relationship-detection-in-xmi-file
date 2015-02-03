package cn.edu.nju.cs;


import org.jdom2.input.SAXBuilder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * Created by bsidb on 15-2-2.
 */
public class XMIFileOpener {
    public static org.w3c.dom.Document open(String filePath) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.parse(new File(filePath));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static org.jdom2.Document openJDOMDocument(String filePath) {
        SAXBuilder builder = new SAXBuilder();
        try {
            org.jdom2.Document document = builder.build(new File(filePath));
            return document;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
