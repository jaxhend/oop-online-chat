package Webscraper;

import com.mongodb.client.*;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.ReturnDocument.AFTER;

public class Mongo {
    public static void main(String[] args) {
        // MongoDB käivitamiseks
        // cmd -> mongod

        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("webscraping");
        MongoCollection<Document> countersCollection = database.getCollection("counters");


        // Auto-incrementing ID
        Document counter = countersCollection.findOneAndUpdate(
                eq("_id", "testing"),
                Updates.inc("seq", 1),
                new FindOneAndUpdateOptions().returnDocument(AFTER)
        );

        // ID väärtus
        int nextId = counter.getInteger("seq");


        MongoCollection<Document> collection = database.getCollection("testing");

        Document document = new Document("_id", nextId) // Use the auto-incremented ID
                .append("title", "Test pealkiri")
                .append("content", "Veebikooritud sisu")
                .append("url", "http://example.com");

        collection.insertOne(document);

        FindIterable<Document> documents = collection.find();
        for (Document doc : documents) {
            System.out.println(doc.toJson());
        }
        mongoClient.close();
    }
}