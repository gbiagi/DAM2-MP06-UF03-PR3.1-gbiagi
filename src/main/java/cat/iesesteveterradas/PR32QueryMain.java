package cat.iesesteveterradas;

import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class PR32QueryMain {
    public static void main(String[] args) {
        // Connectar-se a MongoDB (substitueix amb la teva URI de connexió)
        try (var mongoClient = MongoClients.create("mongodb://root:example@localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("PRMongo");
            MongoCollection<Document> collection = database.getCollection("pr31");

            // Calcula la mitjana de ViewCount
            double avgViewCount = calculateAverageViewCount(collection);

            // Encuentra los títulos de las preguntas con más ViewCount que la media
            List<String> titles = findQuestionAboveAverage(collection, avgViewCount);
            // Imprime los títulos
            System.out.println("Títulos de las preguntas con más views que la media:");
            for (String title : titles) {
                System.out.println(title);
            }
            // Troba les preguntes amb títols que continguin les lletres especificades
            long countWithTitleLetters = findQuestionsWithTitleLetters(collection);

            // Imprimeix el resultat
            System.out.println("Nombre de preguntes amb títols que continguin les lletres especificades: " + countWithTitleLetters);        } catch (Exception e) {
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
        return avgViewCount;
    }

    private static List<String> findQuestionAboveAverage(MongoCollection<Document> collection, double avgViewCount) {
        List<String> titles = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find(new Document("ViewCount", new Document("$gt", avgViewCount)))
                .projection(new Document("Title", 1).append("_id", 0))
                .iterator();

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            String title = doc.getString("Title");
            titles.add(title);
        }
        cursor.close();

        return titles;
    }
    private static long findQuestionsWithTitleLetters(MongoCollection<Document> collection) {
        Pattern pattern = Pattern.compile("(pug|wig|yak|nap|jig|mug|zap|gag|oaf|elf)", Pattern.CASE_INSENSITIVE);
        return collection.countDocuments(new Document("Title", pattern));
    }
}
