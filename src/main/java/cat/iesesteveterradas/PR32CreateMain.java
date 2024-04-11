package cat.iesesteveterradas;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class PR32CreateMain {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Connect to MongoDB (replace with your connection URI)
        try (var mongoClient = MongoClients.create("mongodb://root:example@localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("PRMongo");
            MongoCollection<Document> collection = database.getCollection("pr31");

            logger.info("Connected to MongoDB server.");

            // Read query file in data/output
            File resultQuery = new File("data/Output/query1.xml");
            logger.info("Reading query file: " + resultQuery.getAbsolutePath());

            // Parse the XML file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(resultQuery);

            // Normalize the XML structure
            doc.getDocumentElement().normalize();

            // Get all <post> nodes
            NodeList nodeList = doc.getElementsByTagName("post");

            // Iterate over the <post> nodes
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    // Create a new MongoDB document for each <post>
                    Document post = new Document();

                    // Iterate over the child nodes of <post>
                    NodeList childNodes = element.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node childNode = childNodes.item(j);

                        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                            // Unescape HTML entities in the body text
                            String nodeContent = childNode.getTextContent();
                            if (childNode.getNodeName().equals("Body")) {
                                nodeContent = StringEscapeUtils.unescapeHtml4(nodeContent);
                            }

                            // Add each child node to the MongoDB document
                            post.append(childNode.getNodeName(), nodeContent);
                        }
                    }
                    logger.info("Inserting document: " + post.toJson());
                    // Insert the document into the MongoDB collection
                    collection.insertOne(post);
                }
            }

            logger.info("Documents inserted successfully");
        } catch (Exception e) {
            logger.info("An error occurred: " + e.getMessage());
        }
    }
}