/**
 * Created by Laurent on 22/08/2016.
 */

var SearchState = {
    LOADING: {value: 0},
    NO_RESULTS: {value: 1},
    RESULTS: {value: 2}
};

var EPMC = "http://www.europepmc.org/abstract/MED/";
var OLS  = "http://www.ebi.ac.uk/ols/search?q=";

$(document).ready(function() {
    var searchTerm = $('#query').text();
    console.log("Loading search module!");
    console.log("rsID: "+searchTerm);
    if (searchTerm != '') {
        console.log("Start search for SNP "+searchTerm);
        getSnpData(searchTerm);
    }
    //getLDPopulations();
});

function getSnpData(rsId) {
    console.log("Solr research request received for " + rsId);

    setState(SearchState.LOADING);
    $.getJSON('/gwas/api/search/association',
              {
                  'q': "rsId:"+rsId,
                  'max': 1000
              })
              .done(function(data) {
                  console.log(data.response);
                  processSnpData(data.response,rsId);
              });
    console.log("Solr research done for " + rsId);
}

function processSnpData(data,rsId) {
    // Variant summary panel
    getVariantInfo(data.docs);
    // External links panel
    getLinkButtons(rsId);
    // Associations table
    getVariantAssociations(data.docs);
    // Studies table
    getVariantStudies(data.docs);
    // Traits table
    getVariantTraits(data.docs);
}

function getVariantInfo(data,rsId) {
    var data_sample = data[0];
    var chr = data_sample.chromosomeName[0];
    var pos = data_sample.chromosomePosition[0];
    var region = data_sample.region[0];
    var func = data_sample.context[0];


    var genes_mapped = [];
    var genes_mapped_url = [];
    var traits_reported = [];
    var traits_reported_url = [];
    $.each(data, function (index, doc) {
        // Mapped genes
        var mappedGenes = doc.entrezMappedGenes;
        $.each(mappedGenes, function (index, gene) {
            if (jQuery.inArray(gene, genes_mapped) == -1) {
                genes_mapped.push(gene);
                genes_mapped_url.push(setQueryUrl(gene));
            }
        });
        // Reported traits
        var traits = [];
        var reportedTraits = doc.traitName;
        if (reportedTraits) {
            $.each(reportedTraits, function(index, trait) {
                if (jQuery.inArray(trait, traits_reported) == -1) {
                    traits_reported.push(trait);
                    traits_reported_url.push(setQueryUrl(trait));
                }
            });
        } else {
        }
    });
    genes_mapped_url.sort();
    traits_reported_url.sort();

    // Traits display
    if (traits_reported.length <= 5) {
        $("#variant-traits").html(traits_reported_url.join(', '));
    }
    else {
        longContentList("known_traits_div","variant-traits",traits_reported_url,'traits');
    }

    $("#variant-location").html("chr"+chr+":"+pos);
    $("#variant-region").html(region);
    $("#variant-class").html(setExternalLink(OLS+func,variationClassLabel(func)));
    $("#variant-mapped-genes").html(genes_mapped_url.join(', '));
    $("#variant-summary-content").html(getSummary(data));
}


function getVariantAssociations(data) {
    var asso_count = data.length;

    $("#association_count").html(asso_count);

    if (asso_count == 1) {
        $("#association_label").html("Association");
    }

    $.each(data, function(index,asso) {

        var row = $('<tr/>');

        // Risk allele
        var riskAllele = asso.strongestAllele[0];
        riskAllele = setQueryUrl(riskAllele);
        row.append(newCell(riskAllele));

        // Risk allele frequency
        var riskAlleleFreq = asso.riskFrequency;
        row.append(newCell(riskAlleleFreq));

        // p-value
        var pValue = asso.pValueMantissa;
        if (pValue) {
            var pValueExp = " x 10<sup>" + asso.pValueExponent + "</sup>";
            pValue += pValueExp;
            if (asso.qualifier) {
                if (asso.qualifier[0].match(/\w/)) {
                    pValue += " " + asso.qualifier.join(',');
                }
            }
            row.append(newCell(pValue));
        } else {
            row.append(newCell('-'));
        }

        // OR
        var orValue = asso.orPerCopyNum;
        if (orValue) {
            if (asso.orDescription) {
                orValue += " " + asso.orDescription;
            }
            row.append(newCell(orValue));
        } else {
            row.append(newCell('-'));
        }

        // Beta
        var beta = asso.betaNum;
        if (beta) {
            if (asso.betaUnit) {
                beta += " " + asso.betaUnit;
            }
            if (asso.betaDirection) {
                beta += " " + asso.betaDirection;
            }
            row.append(newCell(beta));
        } else {
            row.append(newCell('-'));
        }

        // CI
        var ci = asso.range;
        if (ci) {
            row.append(newCell(ci));
        } else {
            row.append(newCell('-'));
        }

        // Reported genes
        var genes = [];
        var reportedGenes = asso.reportedGene;
        if (reportedGenes) {
            $.each(reportedGenes, function(index, gene) {
                genes.push(setQueryUrl(gene));
            });
            row.append(newCell(genes.join(', ')));
        } else {
            row.append(newCell('-'));
        }

        // Reported traits
        var traits = [];
        var reportedTraits = asso.traitName;
        if (reportedTraits) {
            $.each(reportedTraits, function(index, trait) {
                traits.push(setQueryUrl(trait));
            });
            row.append(newCell(traits.join(', ')));
        } else {
            row.append(newCell('-'));
        }

        // Mapped traits
        var mappedTraits = asso.mappedLabel;
        if (mappedTraits) {
            row.append(newCell(mappedTraits.join(', ')));
        } else {
            row.append(newCell('-'));
        }

        // Study
        var author = asso.author_s;
        var publicationDate = asso.publicationDate;
        var pubDate = publicationDate.split("-");
        var pubmedId = asso.pubmedId;
        var study = setQueryUrl(author, author + " - " + pubDate[0]);
        study += ' <small>'+setExternalLink(EPMC+pubmedId,'PMID:'+pubmedId)+'</small>';
        row.append(newCell(study));

        var studyId = asso.studyId;

        //row.append(newCell(showHideStudy(studyId)));

        // Populate the table
        $("#association-table-body").append(row);

        // Add hidden study description/details
        /*var studyRow = $('<tr/>');
        studyRow.addClass("accordion-body hidden-study-row collapse");
        studyRow.attr('id',"study-"+studyId);

        var sudyContent = $("<td></td>");
        sudyContent.attr('colspan',11);
        sudyContent.attr('style',"border-top: none");

        var initialSampleDescription = asso.initialSampleDescription;
        var replicateSampleDescription = asso.replicateSampleDescription;
        var ancestralGroups = asso.ancestralGroups;

        var studyTable = $("<table>").addClass('sample-info sample-info-border');

        // Header //
        studyTable.append($("<thead>").append($("<tr>").append($("<th>").attr("colspan","2").attr("style", "background-color:#E7F7F9").html("Study information"))));

        // Content //
        var studyTableContent = $("<tbody>");

        // Initial sample description
        var initialSampleDescriptionRow = $("<tr>");
        initialSampleDescriptionRow.append($("<td>").attr("style", "max-width:30%;text-align:right;font-weight:bold").html("Initial sample description"));
        initialSampleDescriptionRow.append($("<td>").attr("style", "text-align:left").html(initialSampleDescription));
        studyTableContent.append(initialSampleDescriptionRow);

        // Replication sample description
        var replicateSampleDescriptionRow = $("<tr>");
        replicateSampleDescriptionRow.append($("<td>").attr("style", "max-width:30%;text-align:right;font-weight:bold").html("Replication sample description"));
        replicateSampleDescriptionRow.append($("<td>").attr("style", "text-align:left").html(replicateSampleDescription));
        studyTableContent.append(replicateSampleDescriptionRow);

        // Ancestral groups
        var ancestralGroupsRow = $("<tr>");
        ancestralGroupsRow.append($("<td>").attr("style", "max-width:30%;text-align:right;font-weight:bold").html("Ancestral groups"));
        ancestralGroupsRow.append($("<td>").attr("style", "text-align:left").html(ancestralGroups.join(', ')));
        studyTableContent.append(ancestralGroupsRow);

        studyTable.append(studyTableContent);

        sudyContent.html(studyTable);
        studyRow.append(sudyContent);

        $("#association-table-body").append(studyRow);*/
    });
}


function getVariantStudies(data) {
    var study_ids = [];
    $.each(data, function(index, asso) {
        var study_id = asso.studyId;
        if (jQuery.inArray(study_id, study_ids) == -1) {

            var row = $('<tr/>');

            study_ids.push(study_id);

            // Author
            var author = asso.author_s;
            var publicationDate = asso.publicationDate;
            var pubDate = publicationDate.split("-");
            var pubmedId = asso.pubmedId;
            var study_author = setQueryUrl(author, author);
            study_author += '<div><small>'+setExternalLink(EPMC+pubmedId,'PMID:'+pubmedId)+'</small></div>';
            row.append(newCell(study_author));

            // Publication date
            var p_date = asso.publicationDate;
            var publi = p_date.split('T')[0];
            row.append(newCell(publi));

            // Journal
            row.append(newCell(asso.publication));

            // Title
            row.append(newCell(asso.title));

            // Initial sample desc
            var initial_sample_text = displayArrayAsList(asso.initialSampleDescription.split(', '));
            row.append(newCell(initial_sample_text));

            // Replicate sample desc
            var replicate_sample_text = displayArrayAsList(asso.replicateSampleDescription.split(', '));
            row.append(newCell(replicate_sample_text));

            // ancestralGroups
            var ancestral_groups_text = displayArrayAsList(asso.ancestralGroups);
            row.append(newCell(ancestral_groups_text));

            // Populate the table
            $("#study-table-body").append(row);
        }
    });
    // Study count //
    $("#study_count").html(study_ids.length);

    if (study_ids.length == 1) {
        $("#study_label").html("Study");
    }
}


function getVariantTraits(data) {

    // Fetch data //
    var traits = [];
    var mappedtraits = {};
    var mappedtraitsUri = {};
    var synonymtraits = {};
    var studytraits = {};
    $.each(data, function(index, asso) {
        // Trait
        var trait = asso.traitName_s;
        if (jQuery.inArray(trait, traits) == -1) {
            traits.push(trait);
        }

        // Mapped trait(s)
        mappedtraits[trait] = asso.mappedLabel;
        mappedtraitsUri[trait] = asso.mappedUri;

        // Synonym trait(s)
        synonymtraits[trait] = asso.synonym;

        // Study trait(s)
        var author = asso.author_s;
        var publicationDate = asso.publicationDate.split("-");
        var study  = setQueryUrl(author, author + " - " + publicationDate[0]);
            study += setExternalLinkIcon(EPMC + asso.pubmedId);
        if (jQuery.inArray(study, studytraits[trait]) == -1) {
            if (!studytraits[trait]) {
                studytraits[trait] = [];
            }
            studytraits[trait].push(study);
        }
    });

    // Display/print data //
    $("#diseasetrait_count").html(traits.length);

    if (traits.length == 1) {
        $("#diseasetrait_label").html("Trait");
    }

    traits.sort();

    $.each(traits, function(index,trait) {
        var row = $('<tr/>');

        // Trait
        row.append(newCell(setQueryUrl(trait)));

        // Mapped trait(s)
        if (mappedtraits[trait]) {
            var mappedtrait = [];
            $.each(mappedtraits[trait], function(index, mtrait) {
                var mapped_html = setQueryUrl(mtrait);
                mapped_html += setExternalLinkIcon(mappedtraitsUri[trait][index]);
                mappedtrait.push(mapped_html);
            });
            row.append(newCell(mappedtrait.join(',<br />')));
        } else {
            row.append(newCell('-'));
        }

        // Synonym trait(s)
        if (synonymtraits[trait]) {
            var synonymtrait = [];
            $.each(synonymtraits[trait], function(index, syntrait) {
                synonymtrait.push(setQueryUrl(syntrait));
            });
            row.append(newCell(synonymtrait.join(', ')));
        } else {
            row.append(newCell('-'));
        }

        // Study trait(s)
        if (studytraits[trait]) {
            row.append(newCell(studytraits[trait].join(',<br />')));
        } else {
            row.append(newCell('-'));
        }
        $("#diseasetrait-table-body").append(row);
    });
}

// Generate the summary sentence, at the bottom of the summary panel
function getSummary(data) {
    var first_report  = getFirstReportYear(data);
    var count_studies = countStudies(data);

    if (count_studies == 0) {
        count_studies = '';
    }
    else if (count_studies == 1) {
        count_studies = '<b>1</b> study reports this variant';
    }
    else if (count_studies > 1) {
        count_studies = '<b>'+count_studies+'</b> studies report this variant';
    }

    if (first_report != '' && count_studies != '') {
        first_report += ', ';
    }
    return first_report+count_studies;
}

// Create external link buttons
function getLinkButtons (rsId) {
    $("#ensembl_button").attr('onclick', "window.open('http://www.ensembl.org/Homo_sapiens/Variation/Explore?v="+rsId+"', '_blank')");
    $("#ucsc_button").attr('onclick',    "window.open('https://genome.ucsc.edu/cgi-bin/hgTracks?hgFind.matches="+rsId+"', '_blank')");
    $("#dbsnp_button").attr('onclick',   "window.open('http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?rs="+rsId+"', '_blank')");
}

// Pick up the most recent publication year
function getFirstReportYear(data) {
    var study_date = '';
    $.each(data, function(index,asso) {
        var p_date = asso.publicationDate;
        var year = p_date.split('-')[0];
        if (year < study_date || study_date == '') {
            study_date = year;
        }
    });
    if (study_date != '') {
        study_date = "Variant first reported in <b>" + study_date + "</b>";
    }
    return study_date;
}

// Pick up the most recent publication year
function countStudies(data) {
    var study_text = '';
    var studies = [];
    $.each(data, function(index,asso) {
        var study_id = asso.studyId;
        if (jQuery.inArray(study_id, studies) == -1) {
            studies.push(study_id);
        }
    });
    return studies.length;
}

// Display the input array as a HTML list
function displayArrayAsList(data_array) {
    var data_text = '';
    if (data_array.length == 1) {
        data_text = data_array[0];
    }
    else if (data_array.length > 1) {
        var list = $('<ul/>');
        list.css('padding-left', '0px');
        $.each(data_array, function(index, value) {
            list.append(newItem(value));
        });
        data_text = list;
    }
    return data_text;
}

// Generate an internal link to the GWAS search page
function setQueryUrl(query, label) {
    if (!label) {
        label = query;
    }
    return '<a href="/gwas/search?query='+query+'">'+label+'</a>';
}

// Update the display of the variant functional class
function variationClassLabel(label) {
    var new_label = label.replace('_', ' ');
    return new_label.charAt(0).toUpperCase() + new_label.slice(1);
}

// Generate an external link (text + icon)
function setExternalLink(url,label) {
    return '<a href="'+url+'">'+label+'<span class="glyphicon glyphicon-new-window external-link-smaller"></span></a>';

}

// Generate an external link (icon only)
function setExternalLinkIcon(url) {
    return '<a href="'+url+'" class="glyphicon glyphicon-new-window external-link" title="External link" target="_blank"></a>';
}

// Create a cell tag (<td>) and add content in it
function newCell(content) {
    return $("<td></td>").html(content);
}

// Create a list item tag (<li>) and add content in it
function newItem(content) {
    return $("<li></li>").html(content);
}

// Create a hidden list of items - Used when we have to display a more or less long list of information
function longContentList (content_id,container_id, list, type) {

    var content_text = $('<span></span>');
    content_text.css('padding-right', '8px');
    content_text.html('<b>'+list.length+'</b> '+type);

    var content_div  = $('<div></div>');
    content_div.attr('id', content_id);
    content_div.addClass('collapse');

    var content_list = $('<ul></ul>');
    content_list.css('padding-left', '25px');
    content_list.css('padding-top', '6px');
    $.each(list, function(index, item) {
        content_list.append(newItem(item));
    });
    content_div.append(content_list);

    $("#"+container_id).append(content_text);
    $("#"+container_id).append(showHideDiv(content_id));
    $("#"+container_id).append(content_div);
}

// Create a button to show/hide content
function showHideDiv(div_id) {
    var div_button = $("<button></button>");
    div_button.attr('title', 'Click to show/hide more information');
    div_button.attr('id', 'button-'+div_id);
    div_button.attr('onclick', 'toggleDiv("'+div_id+'")');
    div_button.addClass("btn btn-default btn-xs btn-study");
    div_button.html('<span class="glyphicon glyphicon-plus tgb"></span>');

    return div_button;
}

/*function showHideStudy(studyId) {
    //return '<button title="Click to show/hide more study information" class="row-toggle btn btn-default btn-xs accordion-toggle" data-toggle="collapse" data-target=".study-'+studyId+'.hidden-study-row" aria-expanded="false" aria-controls="study:'+studyId+'">' +
    var study_button = $("<button></button>");
    study_button.attr('title', 'Click to show/hide more study information');
    study_button.attr('id', 'button-study-'+studyId);
    study_button.attr('onclick', 'toggleDiv("study-'+studyId+'")');
    study_button.addClass("btn btn-default btn-xs btn-study");
    study_button.html('<span class="glyphicon glyphicon-plus tgb"></span>');

    return study_button;
}

function getLDPopulations() {
    $.getJSON('http://rest.ensembl.org/info/variation/populations/homo_sapiens?content-type=application/json;filter=LD')
            .done(function(data) {
                console.log(data);
                processLDPopulationData(data);
            });
    console.log("Ensembl REST query done to retrieve LD populations");
}

function processLDPopulationData(data) {
    $.each(data, function(index, pop) {
        var popName = pop.name;
        var popLabel = popName.split(':')[2] + " - " + pop.description;
        $('#ld-population-selection').append($("<option>").attr("value", popName).html(popLabel));
    });
}*/

function setState(state) {
    var loading = $('#loading');
    var noresults = $('#noResults');
    var results = $('#results');
    console.log("Search state update...");
    console.log(state);
    switch (state.value) {
        case 0:
            loading.show();
            noresults.hide();
            results.hide();
            break;
        case 1:
            loading.hide();
            noresults.show();
            results.hide();
            break;
        case 2:
            loading.hide();
            noresults.hide();
            results.show();
            break;
        default:
            console.log("Unknown search state; redirecting to search page");
            window.location = "snp";
    }
}