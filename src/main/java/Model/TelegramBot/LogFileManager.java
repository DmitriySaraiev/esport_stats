package Model.TelegramBot;

import Model.Main;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Node;


public class LogFileManager {

    //private static final String pathToSubsriptionXML = "src/main/resources/data/subscriptions.xml";
    //private static final String pathToPatchXML = "src/main/resources/data/patch.txt";
    private static final String pathToSubsriptionXML = "data/subscriptions.xml";
    private static final String pathToPatchXML = "data/patch.txt";
    private Socket clientSocket;
    private ServerSocket serverSocket;

    /*public LogFileManager(){
        try {
            if(Main.isServer) {
                Runnable serverTask = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("new Thread started");
                            serverSocket = new ServerSocket(12345);
                            while(true) {
                                clientSocket = serverSocket.accept();
                                System.out.println("client connected");
                                clientSocket.close();
                                System.out.println("client closed");
                            }
                        }
                        catch (IOException e){e.printStackTrace(); }
                    }
                };
                Thread serverThread = new Thread(serverTask);
                serverThread.start();
            }
            else
                clientSocket = new Socket("167.71.45.132", 12345);
        }
        catch (IOException e){e.printStackTrace(); }
    }

    public Subscription getSubscriptionByCode(String code) {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(pathToSubsriptionXML);
            NodeList nodeList = document.getElementsByTagName("subscription");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (element.getElementsByTagName("code").item(0).getTextContent().equals(code)) {

                        String codeEl = element.getElementsByTagName("code").item(0).getTextContent();
                        boolean isReusable = Boolean.valueOf(element.getElementsByTagName("isReusable").item(0).getTextContent());

                        Subscription subscription;
                        if (isReusable) {
                            try {
                                String tilldateString = element.getElementsByTagName("tillDate").item(0).getTextContent();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date parsedDate = dateFormat.parse(tilldateString);
                                subscription = new Subscription(code, new Timestamp(parsedDate.getTime()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                                subscription = new Subscription();
                            }
                        } else {
                            int timeEl = Integer.valueOf(element.getElementsByTagName("time").item(0).getTextContent());
                            subscription = new Subscription(codeEl, timeEl);
                            element.getParentNode().removeChild(element);
                            TransformerFactory transformerFactory = TransformerFactory.newInstance();
                            Transformer transformer = transformerFactory.newTransformer();
                            DOMSource domSource = new DOMSource(document);
                            StreamResult streamResult = new StreamResult(new File(pathToSubsriptionXML));
                            transformer.transform(domSource, streamResult);
                        }
                        return subscription;
                    }
                }
            }
            return new Subscription();
        }catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Subscription();
        }

    public void addSubscriptionToFile(String code, String hours, boolean isReusable, String tillDate) {
        try {
            if (Main.isServer) {
                clientSocket = serverSocket.accept();
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                Subscription subscription = (Subscription)in.readObject();
                //BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());
            }
            else {

            }

            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(pathToSubsriptionXML);
            Element subscriptionsEl = (Element) document.getElementsByTagName("subscriptions").item(0);
            Element subscriptionEl = document.createElement("subscription");
            Element codeEl = document.createElement("code");
            Element timeEl = document.createElement("time");
            Element isReusableEl = document.createElement("isReusable");
            Element tillDateEl = document.createElement("tillDate");
            codeEl.setTextContent(code);
            timeEl.setTextContent(hours);
            isReusableEl.setTextContent(String.valueOf(isReusable));
            tillDateEl.setTextContent(tillDate);
            subscriptionEl.appendChild(codeEl);
            subscriptionEl.appendChild(timeEl);
            subscriptionEl.appendChild(isReusableEl);
            subscriptionEl.appendChild(tillDateEl);
            subscriptionsEl.appendChild(subscriptionEl);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(pathToSubsriptionXML));
            transformer.transform(domSource, streamResult);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static double getPatchFromFile() {
        double patch = 0;
        try {
            File file = new File(pathToPatchXML);
            FileReader fileReader = new FileReader(file);
            BufferedReader bf = new BufferedReader(fileReader);
            patch = Double.valueOf(bf.readLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return patch;
    }

    public static void logSubscriptionFromUser(long chatId, Subscription subscription) {
        try {
            String path = "users\\" + chatId + "\\subscription.xml";
            File f = new File(path);
            boolean isFileExist;

            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document;
            Element subscriptionsEl;
            isFileExist = Files.exists(f.toPath());
            if (isFileExist) {
                document = documentBuilder.parse(path);
                subscriptionsEl = (Element) document.getElementsByTagName("subscriptions").item(0);
            } else {
                document = documentBuilder.newDocument();
                document.setXmlVersion("1.0");
                subscriptionsEl = document.createElement("subscriptions");
            }
            Element subscriptionEl = document.createElement("subscription");
            Element codeEl = document.createElement("code");
            Element timeEl = document.createElement("time");
            Element activationTimeEl = document.createElement("activation_time");

            codeEl.setTextContent(subscription.getCode());
            timeEl.setTextContent(String.valueOf(subscription.getHours()));
            activationTimeEl.setTextContent(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Timestamp(System.currentTimeMillis())));

            subscriptionEl.appendChild(codeEl);
            subscriptionEl.appendChild(timeEl);
            subscriptionEl.appendChild(activationTimeEl);
            subscriptionsEl.appendChild(subscriptionEl);
            if (!isFileExist)
                document.appendChild(subscriptionsEl);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(path));
            transformer.transform(domSource, streamResult);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}
