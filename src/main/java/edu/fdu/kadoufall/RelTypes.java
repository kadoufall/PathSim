package edu.fdu.kadoufall;

import org.neo4j.graphdb.RelationshipType;

public enum RelTypes implements RelationshipType {
    Publish,            // A -> P
    BelongsTo,          // P -> A
    Collaborate,        // A - A
    Topic,              // P -> T
    RelatedWork,        // T -> P
    Cite,               // P -> P
    In,                 // P -> V
    Accept,             // V -> P

}
