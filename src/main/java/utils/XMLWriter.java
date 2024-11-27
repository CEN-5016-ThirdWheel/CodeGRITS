package utils;

import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

/**
 * Utility class for writing XML documents to files.
 */
public class XMLWriter {

    /**
     * Writes the formatted XML document to the specified file.
     *
     * @param document The XML document. Must not be null.
     * @param filePath The path of the XML file. Must not be null or empty.
     * @throws IllegalArgumentException if document or filePath is null or invalid.
     * @throws TransformerException if an error occurs during the XML transformation.
     * @throws IOException if the file cannot be created or written to.
     */
    public static void writeToXML(Document document, String filePath) throws TransformerException, IOException {
        if (document == null) {
            throw new IllegalArgumentException("Document must not be null.");
        }
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path must not be null or empty.");
        }

        File file = new File(filePath);
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Failed to create the file at path: " + filePath);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // Configure transformer properties
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        // Perform the transformation
        DOMSource source = new DOMSource(document);
        try (StreamResult result = new StreamResult(file)) {
            transformer.transform(source, result);
        }
    }
}
