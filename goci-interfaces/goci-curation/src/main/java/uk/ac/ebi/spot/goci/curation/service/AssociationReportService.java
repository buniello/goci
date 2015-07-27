package uk.ac.ebi.spot.goci.curation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.goci.model.Association;
import uk.ac.ebi.spot.goci.model.AssociationReport;
import uk.ac.ebi.spot.goci.repository.AssociationReportRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Created by emma on 27/07/2015.
 *
 * @author emma
 *         <p>
 *         Service class that creates an association report based on errors returned from mapping pipeline.
 */
@Service
public class AssociationReportService {

    // Repositories
    private AssociationReportRepository associationReportRepository;

    //Constructor
    @Autowired
    public AssociationReportService(AssociationReportRepository associationReportRepository) {
        this.associationReportRepository = associationReportRepository;
    }

    /**
     * Method used to format data returned from view via form so it can be stored in database
     *
     * @param association association, used to create entry association
     * @param errors      collection of errors returned from mapping pipeline
     */
    public void processAssociationErrors(Association association, Collection<String> errors) {

        Collection<String> snpErrors = new ArrayList<>();
        Collection<String> snpGeneOnDiffChrErrors = new ArrayList<>();
        Collection<String> noGeneForSymbolErrors = new ArrayList<>();

        // Look for standard format error messages returned from mapping pipeline
        for (String error : errors) {
            // rsID is not valid
            if (error.contains("different chromosome")) {
                snpGeneOnDiffChrErrors.add(error);
            }

            if (error.contains("not found in Ensembl")) {

                // Gene not in the same chromosome as the variant
                if (error.contains("Variant")) {
                    snpErrors.add(error);
                }

                // Gene symbol not found in Ensembl
                else {noGeneForSymbolErrors.add(error);}
            }
        }

        // Format errors into string so they can be stored in database
        String allSnpErrors = null;
        String allSnpGeneOnDiffChrErrors = null;
        String allNoGeneForSymbolErrors = null;

        if (!snpErrors.isEmpty()) {
            allSnpErrors = String.join(", ", snpErrors);
        }

        if (!snpGeneOnDiffChrErrors.isEmpty()) {
            allSnpGeneOnDiffChrErrors = String.join(", ", snpGeneOnDiffChrErrors);
        }

        if (!noGeneForSymbolErrors.isEmpty()) {
            allNoGeneForSymbolErrors = String.join(", ", noGeneForSymbolErrors);
        }

        // Create association report object
        AssociationReport associationReport = new AssociationReport();
        associationReport.setLastUpdateDate(new Date());
        associationReport.setSnpError(allSnpErrors);
        associationReport.setSnpGeneOnDiffChr(allSnpGeneOnDiffChrErrors);
        associationReport.setNoGeneForSymbol(allNoGeneForSymbolErrors);

        // Before setting link to association remove any existing reports linked to this association
        AssociationReport existingReport = associationReportRepository.findByAssociationId(association.getId());
        if (existingReport != null) {
            associationReportRepository.delete(existingReport);
        }

        associationReport.setAssociation(association);

        // Save association report
        associationReportRepository.save(associationReport);

    }
}
