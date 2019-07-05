package edu.fdu.kadoufall;

import org.neo4j.graphdb.GraphDatabaseService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utils {

    public static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread(graphDb::shutdown));
    }

    public static List<String[]> parseFile(String path) throws IOException {
        List<String[]> ret = new ArrayList<>();
        File file = new File(path);

        if (!file.exists()) {
            return ret;
        }

        BufferedReader br = new BufferedReader(new FileReader(file));
        String contentLine = br.readLine();
        while (contentLine != null) {
            String[] splited = contentLine.trim().split("\\t");
            ret.add(splited);
            contentLine = br.readLine();
        }
        br.close();

        return ret;
    }

    public static List<Map.Entry<String, Double>> sortMap(Map<String, Double> map) {
        List<Map.Entry<String, Double>> list = new ArrayList<>(map.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        return list;
    }
}
