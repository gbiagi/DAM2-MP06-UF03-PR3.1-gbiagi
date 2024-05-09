package cat.iesesteveterradas;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD;

public class PR32QueryMain {
    public static void main(String[] args) {
        // Connectar-se a MongoDB
        try (var mongoClient = MongoClients.create("mongodb://root:example@localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("PRMongo");
            MongoCollection<Document> collection = database.getCollection("pr31");

            // Calcula la mitjana de ViewCount
            double avgViewCount = calculateAverageViewCount(collection);

            // Encuentra los títulos de las preguntas con más ViewCount que la media
            List<String> titlesAboveAvg = findQuestionAboveAverage(collection, avgViewCount);
            // Imprime los títulos
            System.out.println("Títulos de las preguntas con más views que la media:");
            for (String title : titlesAboveAvg) {
                System.out.println(title);
            }
            // Generar el report en pdf
            generatePDFReport(titlesAboveAvg, "questionsAboveAvg");

            // Troba les preguntes amb títols que continguin les lletres especificades
            List<String> titlesWithLetters = findQuestionsWithTitleLetters(collection);

            // Generar el report en pdf
            //generatePDFReport(titlesWithLetters, "questionsWithLetters");

            // Imprimeix el resultat
            System.out.println("\nPreguntes amb títols que continguin les lletres especificades: " + titlesWithLetters);        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static double calculateAverageViewCount(MongoCollection<Document> collection) {
        MongoCursor<Document> cursor = collection.aggregate(Arrays.asList(
                new Document("$group", new Document("_id", null).append("avgViewCount", new Document("$avg", new Document("$toInt", "$ViewCount"))))
        )).iterator();

        double avgViewCount = 0;
        if (cursor.hasNext()) {
            Document result = cursor.next();
            avgViewCount = result.getDouble("avgViewCount");
        }
        cursor.close();
        System.out.println("Average ViewCount: " + avgViewCount);
        return avgViewCount - 2000;
    }

    private static List<String> findQuestionAboveAverage(MongoCollection<Document> collection, double avgViewCount) {
        List<String> titles = new ArrayList<>();
        List<Bson> pipeline = Arrays.asList(
                new Document("$addFields", new Document("ViewCountInt", new Document("$toInt", "$ViewCount"))),
                new Document("$match", new Document("ViewCountInt", new Document("$gt", avgViewCount))),
                new Document("$project", new Document("Title", 1).append("_id", 0))
        );
        MongoCursor<Document> cursor = collection.aggregate(pipeline).iterator();

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            String title = doc.getString("Title");
            titles.add(title);
        }
        System.out.println( titles.size() + " questions found with more views than the average");
        cursor.close();
        return titles;
    }
    private static List<String> findQuestionsWithTitleLetters(MongoCollection<Document> collection) {
        List<String> titles = new ArrayList<>();
        Pattern pattern = Pattern.compile("(pug|wig|yak|nap|jig|mug|zap|gag|oaf|elf)", Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("Title", pattern);
        Bson projection = Projections.fields(Projections.include("Title"), Projections.excludeId());
        MongoCursor<Document> cursor = collection.find(filter).projection(projection).iterator();

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            String title = doc.getString("Title");
            titles.add(title);
        }
        cursor.close();
        return titles;
    }
    private static void generatePDFReport(List<String> titles, String documentName) {
        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            PDType1Font font = new PDType1Font(HELVETICA_BOLD);
            contentStream.setFont(font, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(25, 700);

            for (String title : titles) {
                contentStream.showText(title);
                contentStream.newLineAtOffset(0, -15);
            }

            contentStream.endText();
            contentStream.close();

            document.save("data/out/"+documentName+".pdf");
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
