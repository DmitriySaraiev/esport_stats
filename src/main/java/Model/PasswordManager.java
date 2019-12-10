package Model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class PasswordManager {

    private static final String pathToPasswordXML = "/static/passwords.xml";
    private Document document;
    private Node passwordNode;

    public PasswordManager() {
        InputStream inputStream = this.getClass().getResourceAsStream(pathToPasswordXML);
        initPasswordNode(inputStream);
    }

    private void initPasswordNode(InputStream inputStream){
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            document = documentBuilder.parse(inputStream);
            passwordNode = document.getElementsByTagName("variables").item(0);
        }
        catch (ParserConfigurationException e){
            e.printStackTrace();
        }
        catch (SAXException e){e.printStackTrace();}
        catch (IOException e){e.printStackTrace();}
    }

    public String getDbLogin(){
        Element element = (Element) passwordNode;
        String mysqlLogin = element.getElementsByTagName("mysqlLogin").item(0).getTextContent();
        return mysqlLogin;
    }

    public String getDbPassword(){
        Element element = (Element) passwordNode;
        String mysqlPassword = element.getElementsByTagName("mysqlPassword").item(0).getTextContent();
        return mysqlPassword;
    }

    public String getServerIP(){
        Element element = (Element) passwordNode;
        String serverIP = element.getElementsByTagName("serverIP").item(0).getTextContent();
        return serverIP;
    }

}
