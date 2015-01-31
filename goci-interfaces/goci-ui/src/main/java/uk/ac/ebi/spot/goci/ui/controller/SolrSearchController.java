package uk.ac.ebi.spot.goci.ui.controller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ebi.spot.goci.model.Association;
import uk.ac.ebi.spot.goci.model.DiseaseTrait;
import uk.ac.ebi.spot.goci.model.SingleNucleotidePolymorphism;
import uk.ac.ebi.spot.goci.model.Study;
import uk.ac.ebi.spot.goci.ui.SearchConfiguration;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/01/15
 */
@Controller
public class SolrSearchController {
    private SearchConfiguration searchConfiguration;

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    public SolrSearchController(SearchConfiguration searchConfiguration) {
        this.searchConfiguration = searchConfiguration;
    }

    protected Logger getLog() {
        return log;
    }

    @RequestMapping(value = "api/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public void doSolrSearch(
            @RequestParam("q") String query,
            @RequestParam("json.wrf") String callbackFunction,
            @RequestParam(value = "maxResults", required = false, defaultValue = "10") int maxResults,
            HttpServletResponse response) throws IOException {
        StringBuilder solrSearchBuilder = buildBaseSearchRequest();

        addFacet(solrSearchBuilder, searchConfiguration.getDefaultFacet());
        addJsonpCallback(solrSearchBuilder, callbackFunction);
        addMaxResults(solrSearchBuilder, maxResults);
        addQuery(solrSearchBuilder, query);

        // dispatch search
        dispatchSearch(solrSearchBuilder.toString(), response.getOutputStream());
    }

    @RequestMapping(value = "api/search/study", produces = MediaType.APPLICATION_JSON_VALUE)
    public void doStudySolrSearch(
            @RequestParam("q") String query,
            @RequestParam("json.wrf") String callbackFunction,
            @RequestParam(value = "maxResults", required = false, defaultValue = "10000") int maxResults,
            HttpServletResponse response) throws IOException {
        StringBuilder solrSearchBuilder = buildBaseSearchRequest();

        addJsonpCallback(solrSearchBuilder, callbackFunction);
        addMaxResults(solrSearchBuilder, maxResults);
        addFilterQuery(solrSearchBuilder, searchConfiguration.getDefaultFacet(), Study.class.getSimpleName());
        addQuery(solrSearchBuilder, query);

        // dispatch search
        dispatchSearch(solrSearchBuilder.toString(), response.getOutputStream());
    }

    @RequestMapping(value = "api/search/snp", produces = MediaType.APPLICATION_JSON_VALUE)
    public void doSnpSolrSearch(
            @RequestParam("q") String query,
            @RequestParam("json.wrf") String callbackFunction,
            @RequestParam(value = "maxResults", required = false, defaultValue = "10000") int maxResults,
            HttpServletResponse response) throws IOException {
        StringBuilder solrSearchBuilder = buildBaseSearchRequest();

        addJsonpCallback(solrSearchBuilder, callbackFunction);
        addMaxResults(solrSearchBuilder, maxResults);
        addFilterQuery(solrSearchBuilder,
                       searchConfiguration.getDefaultFacet(),
                       SingleNucleotidePolymorphism.class.getSimpleName());
        addQuery(solrSearchBuilder, query);

        // dispatch search
        dispatchSearch(solrSearchBuilder.toString(), response.getOutputStream());
    }

    @RequestMapping(value = "api/search/association", produces = MediaType.APPLICATION_JSON_VALUE)
    public void doAssociationSolrSearch(
            @RequestParam("q") String query,
            @RequestParam("json.wrf") String callbackFunction,
            @RequestParam(value = "maxResults", required = false, defaultValue = "10000") int maxResults,
            HttpServletResponse response) throws IOException {
        StringBuilder solrSearchBuilder = buildBaseSearchRequest();

        addJsonpCallback(solrSearchBuilder, callbackFunction);
        addMaxResults(solrSearchBuilder, maxResults);
        addFilterQuery(solrSearchBuilder, searchConfiguration.getDefaultFacet(), Association.class.getSimpleName());
        addQuery(solrSearchBuilder, query);

        // dispatch search
        dispatchSearch(solrSearchBuilder.toString(), response.getOutputStream());
    }

    @RequestMapping(value = "api/search/trait", produces = MediaType.APPLICATION_JSON_VALUE)
    public void doTraitSolrSearch(
            @RequestParam("q") String query,
            @RequestParam("json.wrf") String callbackFunction,
            @RequestParam(value = "maxResults", required = false, defaultValue = "10000") int maxResults,
            HttpServletResponse response) throws IOException {
        StringBuilder solrSearchBuilder = buildBaseSearchRequest();

        addJsonpCallback(solrSearchBuilder, callbackFunction);
        addMaxResults(solrSearchBuilder, maxResults);
        addFilterQuery(solrSearchBuilder, searchConfiguration.getDefaultFacet(), DiseaseTrait.class.getSimpleName());
        addQuery(solrSearchBuilder, query);

        // dispatch search
        dispatchSearch(solrSearchBuilder.toString(), response.getOutputStream());
    }

    private StringBuilder buildBaseSearchRequest() {
        // build base request
        StringBuilder solrSearchBuilder = new StringBuilder();
        solrSearchBuilder.append(searchConfiguration.getGwasSearchServer().toString());
        solrSearchBuilder.append("/select?");
        return solrSearchBuilder;
    }

    private void addFacet(StringBuilder solrSearchBuilder, String facet) {
        // add configuration
        solrSearchBuilder.append("&facet=true&facet.field=").append(facet);
    }

    private void addJsonpCallback(StringBuilder solrSearchBuilder, String callbackFunction) {
        solrSearchBuilder.append("&wt=json");
        solrSearchBuilder.append("&json.wrf=").append(callbackFunction);
    }

    private void addMaxResults(StringBuilder solrSearchBuilder, int maxResults) {
        solrSearchBuilder.append("&rows=").append(maxResults);
    }

    private void addFilterQuery(StringBuilder solrSearchBuilder, String filterOn, String filterBy) {
        solrSearchBuilder.append("&fq=").append(filterOn).append("%3A").append(filterBy);
    }

    private void addQuery(StringBuilder solrSearchBuilder, String query) throws IOException {
        try {
            solrSearchBuilder.append("&q=").append(URLEncoder.encode(query, "UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new IOException("Invalid query string - " + query, e);
        }
    }

    private void dispatchSearch(String searchString, OutputStream out) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(searchString);
        if (System.getProperty("http.proxyHost") != null) {
            HttpHost proxy;
            if (System.getProperty("http.proxyPort") != null) {
                proxy = new HttpHost(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty
                        ("http.proxyPort")));
            }
            else {
                proxy = new HttpHost(System.getProperty("http.proxyHost"));
            }
            httpGet.setConfig(RequestConfig.custom().setProxy(proxy).build());
        }

        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            entity.writeTo(out);
            EntityUtils.consume(entity);
        }
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    private String handleIOException(IOException e) {
        getLog().error("An IOException occurred during solr search communication", e);
        return "Your search could not be performed - we encountered a problem.  We've been notified and will attempt " +
                "to rectify the problem as soon as possible.  If problems persist, please email gwas-info@ebi.ac.uk";
    }
}
