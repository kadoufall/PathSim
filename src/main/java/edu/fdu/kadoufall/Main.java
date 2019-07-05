package edu.fdu.kadoufall;

public class Main {

    public static void main(String[] args) {
        String dataPath = "D:/neo4j-community-3.5.7/data/databases/";
        String databaseName = "demo1.graphdb";

        GraphDB graphDB = new GraphDB(dataPath + databaseName);

//        graphDB.initializeGraphDB("src/main/resources/dblp/");


        graphDB.getTopKSimAuthors("APVPA", "68855", 10);

        graphDB.getGraphDB().shutdown();
    }


}
