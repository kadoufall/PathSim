package edu.fdu.kadoufall.model;

import lombok.Getter;
import org.neo4j.graphdb.Node;

/**
 * The class is no use
 */
public class Author {
    @Getter
    private final Node underlyingNode;

    public Author(Node authorNode) {
        this.underlyingNode = authorNode;
    }

    public String getId() {
        return (String) underlyingNode.getProperty("id");
    }

    public String getName() {
        return (String) underlyingNode.getProperty("name");
    }


    @Override
    public int hashCode() {
        return underlyingNode.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Author && underlyingNode.equals(((Author) obj).getUnderlyingNode());
    }

    @Override
    public String toString() {
        return "Author[" + getName() + "]";
    }

}
