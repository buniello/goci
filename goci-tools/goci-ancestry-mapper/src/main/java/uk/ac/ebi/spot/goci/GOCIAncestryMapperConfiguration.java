package uk.ac.ebi.spot.goci;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.goci.ontology.owl.OntologyLoader;
import uk.ac.ebi.spot.goci.ontology.owl.ReasonedOntologyLoader;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Collections;

/**
 * Created by dwelter on 06/10/15.
 */
@Component
public class GOCIAncestryMapperConfiguration {

    @NotNull
    @Value("${ancestro.location}")
    private Resource ancestroResource;

    @Bean OntologyLoader ontologyLoader() {
        ReasonedOntologyLoader loader = new ReasonedOntologyLoader();
        loader.setOntologyName("ancestro");
        loader.setOntologyURI(URI.create("http://www.ebi.ac.uk/ancestro"));
        loader.setOntologyResource(ancestroResource);
        //        loader.setExclusionClassURI(URI.create("http://www.geneontology.org/formats/oboInOwl#ObsoleteClass"));
        //        loader.setExclusionAnnotationURI(URI.create("http://www.ebi.ac.uk/efo/organizational_class"));
        loader.setSynonymURIs(Collections.singleton(URI.create("http://www.ebi.ac.uk/ancestro/ancestro_0121")));
        loader.init();
        return loader;
    }
}
