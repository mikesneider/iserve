package uk.ac.open.kmi.iserve.discovery.api.freetextsearch.impl;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.freetextsearch.FreeTextSearchPlugin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by Luca Panziera on 03/11/14.
 */
@Singleton
public abstract class SparqlSearchPlugin implements FreeTextSearchPlugin {

    protected String queryEndpoint;
    protected String updateEndpoint;
    private Logger logger = LoggerFactory.getLogger(SparqlSearchPlugin.class);
    private String searchProperty;

    public SparqlSearchPlugin(String searchProperty) {
        this.searchProperty = searchProperty;
    }

    @Override
    public Map<URI, MatchResult> search(String query) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> ")
                .append("PREFIX sawsdl:<http://www.w3.org/ns/sawsdl#> ")
                .append("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> ")
                .append("SELECT * ")
                .append("WHERE { ?s <").append(searchProperty).append("> \"").append(query).append("\" . } ");

        Query sparqlQuery = QueryFactory.create(queryBuilder.toString());
        return search(sparqlQuery);
    }

    private Map<URI, MatchResult> search(Query sparqlQuery) {
        logger.debug("Executing SPARQL query: {}", sparqlQuery);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(queryEndpoint.toString(), sparqlQuery);
        ResultSet resultSet = qexec.execSelect();

        Map<URI, MatchResult> r = Maps.newLinkedHashMap();
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();
            RDFNode s = solution.get("s");
            if (s.isURIResource()) {
                try {
                    String resource = s.asResource().getURI();
                    FreeTextMatchResult result = new FreeTextMatchResult(new URI(searchProperty), new URI(resource));
                    r.put(new URI(resource), result);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        return r;
    }

    @Override
    public Map<URI, MatchResult> search(String query, URI type) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> ")
                .append("PREFIX sawsdl:<http://www.w3.org/ns/sawsdl#> ")
                .append("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> ")
                .append("SELECT * ")
                .append("WHERE { ?s <").append(searchProperty).append("> \"").append(query).append("\" . ")
                .append("?s rdf:type <").append(type.toASCIIString()).append("> . } ");

        Query sparqlQuery = QueryFactory.create(queryBuilder.toString());
        return search(sparqlQuery);
    }

}
