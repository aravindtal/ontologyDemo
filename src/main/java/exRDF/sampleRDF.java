package exRDF;



import info.aduna.iteration.Iterations;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.util.iterators.Iterators;

/**
 * Created by aravindp on 17/2/16.
 */
public class sampleRDF {
    public static void main(String argd[]) throws Exception{
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();

        ValueFactory vf = repo.getValueFactory();

        RepositoryConnection conn = repo.getConnection();

        String nameSpace = "http://example.org/";
        URI john = vf.createURI(nameSpace,"john");
        try {
            conn.add(john, RDF.TYPE, FOAF.PERSON);
            conn.add(john, RDFS.LABEL,vf.createLiteral("John"));
            conn.add(john, DC.CREATOR,vf.createLiteral("John"));

            RepositoryResult<Statement> results = conn.getStatements(null,null,null,true);
            Model model = Iterations.addAll(results,new LinkedHashModel());
            model.setNamespace("ex",nameSpace);
            model.setNamespace("foaf",FOAF.NAMESPACE);
            model.setNamespace("rdf",RDF.NAMESPACE);
            model.setNamespace("rdfs",RDFS.NAMESPACE);
            model.setNamespace("dc",DC.NAMESPACE);

            Rio.write(model,System.out, RDFFormat.N3);
        } catch (Exception e){

        } finally {

        }
    }
}
