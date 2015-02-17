/**
 * Created by dwelter on 04/02/15.
 */


$(document).ready(function () {
    $('#filter-form').submit(function (event) {
        event.preventDefault();
        doFiltering();
    });

    $('#clear-filter').click(function () {
        clearFilters();
    });

    $('#date-min').datepicker({
        format: "yyyy-mm",
        endDate: "today",
        startView: 1,
        minViewMode: 1
    });

    $('#date-max').datepicker({
        format: "yyyy-mm",
        endDate: "today",
        startView: 1,
        minViewMode: 1
    });

//if the dropdown button is clicked, toggle the open element on the div
    $('.dropdown-toggle.multiSelect').on('click', function(event){
        $(this).parent().toggleClass("open");
    });

//if there is a click anywhere in the page body and the trait selector is open but the click was in the dropdown, close the dropdown
    $('body').on('click', function (e) {
        if ($('#trait-dropdown').hasClass('open') && !$('#trait-dropdown').is(e.target) && !$('#trait-dropdown').children().is(e.target) && !$('#trait-dropdown').children().find('input').is(e.target)) {
            $('#trait-dropdown').removeClass("open");
        }
    });

});

function doFiltering() {
    var pvalRange = '';
    var orRange = '';
    var betaRange = '';
    var dateRange = '';
    var traits = [];

    //var pvalMin = $('#pval-min').val();
    //var pvalMax = $('#pval-max').val();
    //if (pvalMin || pvalMax) {
    //    pvalRange = "[";
    //    if (pvalMin) {
    //        pvalRange = pvalRange.concat(pvalMin);
    //    }
    //    else {
    //        pvalRange = pvalRange.concat("*");
    //    }
    //    pvalRange = pvalRange.concat("+TO+");
    //    if (pvalMax) {
    //        pvalRange = pvalRange.concat(pvalMax);
    //    }
    //    else {
    //        pvalRange = pvalRange.concat("*");
    //    }
    //    pvalRange = pvalRange.concat("]");
    //    console.log(pvalRange);
    //}

    var pvalMant = $('#pval-mant').val();
    var pvalExp = $('#pval-exp').val();
    if (pvalMant || pvalExp) {
        pvalRange = "[*+TO+";
        if (pvalMant) {
            pvalRange = pvalRange.concat(pvalMant);
        }
        else {
            //if no mantissa was entered, assume 1
            pvalRange = pvalRange.concat("1");
        }
        pvalRange = pvalRange.concat("e");
        if (pvalExp) {
            pvalRange = pvalRange.concat(pvalExp);
        }
        else {
            //if no exponent was entered, use the catalog cut-off
            pvalRange = pvalRange.concat("-5");
        }
        pvalRange = pvalRange.concat("]");
        console.log(pvalRange);
    }

    var orMin = $('#or-min').val();
    var orMax = $('#or-max').val();
    if (orMin || orMax) {
        orRange = "[";
        if (orMin) {
            orRange = orRange.concat(orMin);
        }
        else {
            orRange = orRange.concat("*");
        }
        orRange = orRange.concat("+TO+");
        if (orMax) {
            orRange = orRange.concat(orMax);
        }
        else {
            orRange = orRange.concat("*");
        }
        orRange = orRange.concat("]");
        console.log(orRange);
    }

    var betaMin = $('#beta-min').val();
    var betaMax = $('#beta-max').val();
    if (betaMin || betaMax) {
        betaRange = "[";
        if (betaMin) {
            betaRange = betaRange.concat(betaMin);
        }
        else {
            betaRange = betaRange.concat("*");
        }
        betaRange = betaRange.concat("+TO+");
        if (betaMax) {
            betaRange = betaRange.concat(betaMax);
        }
        else {
            betaRange = betaRange.concat("*");
        }
        betaRange = betaRange.concat("]");
        console.log(betaRange);
    }
    var dateMin = $('#date-min').val();
    var dateMax = $('#date-max').val();
    if (dateMin || dateMax) {
        dateRange = "[";
        if (dateMin) {
            dateRange = dateRange.concat(dateMin).concat("-01T00:00:00Z");
        }
        else {
            dateRange = dateRange.concat("*");
        }
        dateRange = dateRange.concat("+TO+");
        if (dateMax) {
            var year = dateMax.split("-")[0];
            var month = parseInt(dateMax.split("-")[1]);
            var newdate = dateMax;
            if(month === 12){
                newdate = (parseInt(year)+1).toString().concat('-01');
            }
            else if (month > 9 && month < 12){
                newdate = year.concat("-").concat(month+1);
            }
            else{
                newdate = year.concat("-0").concat(month+1);
            }
            dateRange = dateRange.concat(newdate).concat("-01T00:00:00Z");
        }
        else {
            dateRange = dateRange.concat("*");
        }
        dateRange = dateRange.concat("]");
        console.log(dateRange);
    }

    var traitInput = $('#trait-dropdown ul li input:checked');
    for(var i=0; i<traitInput.length; i++){

        var trait = traitInput[i].value;
        trait = trait.replace(/\s/g, '+');
        console.log(trait);
        traits[i] = trait;

    }
    console.log(traits);


    solrfilter(pvalRange, orRange, betaRange, dateRange, traits);
}

function clearFilters() {
    console.log("Clearing all filters");
    $('#filter-form').find('input').val('');

    //if ($('#facet').text()) {
    //    console.log("No facet, so I'm redoing the search");
        //doSearch();
        loadResults();
    //}
    //else {
    //    console.log("Reapplying the facet without filters for facet " + $('#facet').text());
    //    applyFacet();
    //}
}


function solrfilter(pval, or, beta, date, traits) {
    var query = $('#query').text();
    console.log("Solr research request received for " + query + " and filters " + pval + ", " + or + ", " + beta + ", " + date + " and " + traits);
    var searchTerm = 'text:"'.concat(query).concat('"');

    $.getJSON('api/search/filter', {
        'q': searchTerm,
        'max': 100000,
        'pvalfilter': pval,
        'orfilter': or,
        'betafilter': beta,
        'datefilter': date,
        'traitfilter[]': traits
    })
        .done(function (data) {
            console.log(data);
            processData(data);
        });
}
