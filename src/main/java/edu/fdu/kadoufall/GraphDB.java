package edu.fdu.kadoufall;

import lombok.Data;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GraphDB {
    private GraphDatabaseService graphDB;

    public GraphDB(String graphDBPath) {
        this.graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphDBPath));
        Utils.registerShutdownHook(this.graphDB);
    }

    public void initializeGraphDB(String dataDir) {
        try (Transaction tx = this.graphDB.beginTx()) {
            buildNodeRelation(dataDir);

            tx.success();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildNodeRelation(String dataDir) throws IOException {
        List<String[]> authors = Utils.parseFile(dataDir + "author.txt");
        for (String[] item : authors) {
            Node node = this.graphDB.createNode(NodeType.Author);
            node.setProperty("id", item[0]);
            node.setProperty("name", item[1]);
        }

        List<String[]> venues = Utils.parseFile(dataDir + "venue.txt");
        for (String[] item : venues) {
            Node node = this.graphDB.createNode(NodeType.Venue);
            node.setProperty("id", item[0]);
            node.setProperty("name", item[1]);
        }

        List<String[]> papers = Utils.parseFile(dataDir + "paper.txt");
        for (String[] item : papers) {
            Node node = this.graphDB.createNode(NodeType.Paper);
            node.setProperty("id", item[0]);
            node.setProperty("title", item[1]);
        }

        List<String[]> terms = Utils.parseFile(dataDir + "term.txt");
        for (String[] item : terms) {
            Node node = this.graphDB.createNode(NodeType.Term);
            node.setProperty("id", item[0]);
            node.setProperty("data", item[1]);
        }

        List<String[]> relationships = Utils.parseFile(dataDir + "relation.txt");
        for (String[] item : relationships) {
            String paperId = item[0];
            String key = item[1];

            Node paper = this.graphDB.findNode(NodeType.Paper, "id", paperId);

            Node author = this.graphDB.findNode(NodeType.Author, "id", key);
            if (author != null) {
                author.createRelationshipTo(paper, RelTypes.Publish);
                paper.createRelationshipTo(author, RelTypes.BelongsTo);
            } else {
                Node venue = this.graphDB.findNode(NodeType.Venue, "id", key);
                if (venue != null) {
                    paper.createRelationshipTo(venue, RelTypes.In);
                    venue.createRelationshipTo(paper, RelTypes.Accept);
                } else {
                    Node term = this.graphDB.findNode(NodeType.Term, "id", key);
                    paper.createRelationshipTo(term, RelTypes.Topic);
                    term.createRelationshipTo(paper, RelTypes.RelatedWork);
                }
            }
        }
    }

    private Map<String, Integer> getReletedAuthor(String metapath, String id) {
        String query = "MATCH (:Author {id:'" + id + "'})-[:Publish]->(:Paper)-[:In]->(:Venue)-[:Accept]->" +
                "(:Paper)-[:BelongsTo]->(a:Author) RETURN a.id";

        if (metapath.equals("APTPA")) {
            query = "MATCH (:Author {id:'" + id + "'})-[:Publish]->(:Paper)-[:Topic]->(:Term)-[:RelatedWork]->" +
                    "(:Paper)-[:BelongsTo]->(a:Author) RETURN a.id";
        }

        Map<String, Integer> paths = new HashMap<>();
        try (Transaction ignored = this.graphDB.beginTx();
             Result result = this.graphDB.execute(query)) {
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                String value = (String) row.get("a.id");
                if (paths.containsKey(value)) {
                    paths.put(value, paths.get(value) + 1);
                } else {
                    paths.put(value, 1);
                }
            }
        }

        return paths;
    }

    private int getPathNum(String metapath, String startId, String endId) {
        String query = "MATCH (:Author {id:'" + startId + "'})-[:Publish]->(:Paper)-[:In]->(:Venue)-[:Accept]->" +
                "(:Paper)-[:BelongsTo]->(a:Author {id: '" + endId + "'}) RETURN a.id";

        if (metapath.equals("APTPA")) {
            query = "MATCH (:Author {id:'" + startId + "'})-[:Publish]->(:Paper)-[:Topic]->(:Term)-[:RelatedWork]->" +
                    "(:Paper)-[:BelongsTo]->(a:Author {id: '" + endId + "'}) RETURN a.id";
        }

        int count = 0;
        try (Transaction ignored = this.graphDB.beginTx();
             Result result = this.graphDB.execute(query)) {
            while (result.hasNext()) {
                result.next();
                count++;
            }
        }


        return count;
    }


    /**
     * Find the TopK similar Author and print the score
     *
     * @param metapath metapath to use
     * @param id       author Id
     * @param k        TopK similar authors
     */
    public void getTopKSimAuthors(String metapath, String id, int k) {
        Map<String, Double> sims = new HashMap<>();

        Map<String, Integer> related = getReletedAuthor(metapath, id);

        int score2 = related.get(id);   // x->x

        for (Map.Entry<String, Integer> r : related.entrySet()) {
            int score1 = r.getValue();      // x->y
            int score3 = getPathNum(metapath, r.getKey(), r.getKey());      // y->y

            double sim = 2.0 * score1 / (score2 + score3);

            sims.put(r.getKey(), sim);
        }

        List<Map.Entry<String, Double>> list = Utils.sortMap(sims);

        for (int i = 0; i < list.size(); i++) {
            if (i == k) {
                break;
            }

            Map.Entry<String, Double> item = list.get(i);

            try (Transaction ignored = this.graphDB.beginTx()) {
                Node author = this.graphDB.findNode(NodeType.Author, "id", item.getKey());
                String name = String.valueOf(author.getProperty("name"));
                System.out.println(name + ":" + item.getValue());
            }
        }
    }

}
