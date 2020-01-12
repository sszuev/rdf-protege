package org.protege.editor.owl.ui.view.sparql;

import com.github.owlcs.ontapi.jena.vocabulary.OWL;
import com.github.owlcs.ontapi.jena.vocabulary.RDF;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDFS;

/**
 * A Jena (ARQ) based SPARQL engine.
 * Created by @ssz on 07.01.2020.
 */
public interface SPARQLEngine {

    /**
     * Executes the given query.
     *
     * @param data   {@link Model Jena RDF Model} to execute on
     * @param query, {@code String}, not {@code null}
     * @return {@link Res}
     * @throws Error - something is wrong while executing query
     */
    Res execute(Model data, String query) throws Error;

    /**
     * Answers a query type.
     *
     * @return {@link Type}
     */
    Type getType();

    /**
     * Describes query type.
     */
    enum Type {
        SELECT {
            @Override
            boolean test(Prologue q) {
                return q instanceof Query && ((Query) q).isSelectType();
            }

            @Override
            public String getSampleQuery() {
                PrefixMapping pm = SPARQLFactory.FULL_PM;
                StringBuilder res = new StringBuilder();
                res.append("# Example of SPARQL SELECT: \n");
                SPARQLFactory.writePrefixes(pm, res);
                return res.append("SELECT ?subject ?object\n{\n  ?subject ")
                        .append(pm.shortForm(RDFS.subClassOf.getURI()))
                        .append(" ?object .\n}")
                        .toString();
            }
        }, UPDATE {
            @Override
            boolean test(Prologue q) {
                return q instanceof UpdateRequest;
            }

            @Override
            public String getSampleQuery() {
                PrefixMapping pm = SPARQLFactory.FULL_PM;
                StringBuilder res = new StringBuilder();
                res.append("# Example of SPARQL INSERT (adding an anonymous individual): \n");
                SPARQLFactory.writePrefixes(pm, res);
                return res.append("INSERT\n{\n  ?b ")
                        .append(pm.shortForm(RDF.type.getURI())).append(" ")
                        .append(pm.shortForm(OWL.Thing.getURI())).append(" .\n  ?b ")
                        .append(pm.shortForm(RDFS.comment.getURI()))
                        .append(" 'This is an anonymous individual' .\n}\nWHERE\n{\n  BIND( BNODE() AS ?b ) .\n}")
                        .toString();
            }
        }, ASK {
            @Override
            boolean test(Prologue q) {
                return q instanceof Query && ((Query) q).isAskType();
            }

            @Override
            public String getSampleQuery() {
                PrefixMapping pm = SPARQLFactory.SHORT_PM;
                StringBuilder res = new StringBuilder();
                res.append("# Example of SPARQL ASK: \n");
                SPARQLFactory.writePrefixes(pm, res);
                return res.append("ASK\n{\n  ?s ")
                        .append(pm.shortForm(RDF.type.getURI())).append(" ")
                        .append(pm.shortForm(OWL.Ontology.getURI())).append(" .\n}").toString();
            }
        }, JSON {
            @Override
            boolean test(Prologue q) {
                return q instanceof Query && ((Query) q).isJsonType();
            }

            @Override
            public String getSampleQuery() {
                return "# Example of ARQ JSON Query: \n" +
                        "JSON {\n  \"s\": ?s , \"p\": ?p , \"o\" : ?o \n} WHERE {\n   ?s ?p ?o \n}";
            }

            @Override
            Syntax getSyntax() {
                return Syntax.syntaxARQ;
            }
        }, DESCRIBE {
            @Override
            boolean test(Prologue q) {
                return q instanceof Query && ((Query) q).isDescribeType();
            }

            @Override
            public String getSampleQuery() {
                PrefixMapping pm = SPARQLFactory.SHORT_PM;
                StringBuilder res = new StringBuilder();
                res.append("# Example of SPARQL DESCRIBE: \n");
                SPARQLFactory.writePrefixes(pm, res);
                return res.append("DESCRIBE ?s WHERE\n{\n  ?s ")
                        .append(pm.shortForm(RDF.type.getURI())).append(" ")
                        .append(pm.shortForm(OWL.Ontology.getURI())).append(" .\n}").toString();
            }
        }, CONSTRUCT {
            @Override
            boolean test(Prologue q) {
                return q instanceof Query && ((Query) q).isConstructType();
            }

            @Override
            public String getSampleQuery() {
                PrefixMapping pm = SPARQLFactory.FULL_PM;
                StringBuilder res = new StringBuilder();
                res.append("# Example of SPARQL CONSTRUCT: \n");
                SPARQLFactory.writePrefixes(pm, res);
                return res.append("CONSTRUCT\n{\n  ?s ")
                        .append(pm.shortForm(RDFS.comment.getURI()))
                        .append(" 'Autogenerated ontology header (comment)' .\n}\nWHERE\n{\n  ?s ")
                        .append(pm.shortForm(RDF.type.getURI())).append(" ")
                        .append(pm.shortForm(OWL.Ontology.getURI())).append(" .\n}").toString();
            }
        };

        Syntax getSyntax() {
            return Syntax.defaultSyntax;
        }

        abstract boolean test(Prologue q);

        /**
         * Answers the sample SPARQL-query.
         *
         * @return String
         */
        public abstract String getSampleQuery();
    }

    /**
     * Describes a result set table.
     */
    interface Res {
        int getColumnCount();

        int getRowCount();

        String getColumnName(int col);

        Object getResult(int row, int col);
    }

    /**
     * An exception that may happen while query execution.
     * <p>
     * Created by @ssz on 07.01.2020.
     */
    class Error extends Exception {
        public Error(String message, Throwable t) {
            super(message, t);
        }

        public Error(String message) {
            super(message);
        }
    }

}
