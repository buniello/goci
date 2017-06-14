package uk.ac.ebi.spot.goci.curation.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.ac.ebi.spot.goci.builder.AssociationBuilder;
import uk.ac.ebi.spot.goci.builder.SecureUserBuilder;
import uk.ac.ebi.spot.goci.builder.StudyBuilder;
import uk.ac.ebi.spot.goci.curation.service.AssociationDeletionService;
import uk.ac.ebi.spot.goci.curation.service.AssociationDownloadService;
import uk.ac.ebi.spot.goci.curation.service.AssociationEventsViewService;
import uk.ac.ebi.spot.goci.curation.service.AssociationOperationsService;
import uk.ac.ebi.spot.goci.curation.service.AssociationUploadService;
import uk.ac.ebi.spot.goci.curation.service.AssociationValidationReportService;
import uk.ac.ebi.spot.goci.curation.service.CheckEfoTermAssignmentService;
import uk.ac.ebi.spot.goci.curation.service.CheckMappingService;
import uk.ac.ebi.spot.goci.curation.service.CurrentUserDetailsService;
import uk.ac.ebi.spot.goci.curation.service.SingleSnpMultiSnpAssociationService;
import uk.ac.ebi.spot.goci.curation.service.SnpAssociationTableViewService;
import uk.ac.ebi.spot.goci.curation.service.SnpInteractionAssociationService;
import uk.ac.ebi.spot.goci.curation.service.StudyAssociationBatchDeletionEventService;
import uk.ac.ebi.spot.goci.model.Association;
import uk.ac.ebi.spot.goci.model.SecureUser;
import uk.ac.ebi.spot.goci.model.Study;
import uk.ac.ebi.spot.goci.repository.AssociationRepository;
import uk.ac.ebi.spot.goci.repository.EfoTraitRepository;
import uk.ac.ebi.spot.goci.repository.StudyRepository;
import uk.ac.ebi.spot.goci.service.EnsemblRestTemplateService;
import uk.ac.ebi.spot.goci.service.MapCatalogService;
import uk.ac.ebi.spot.goci.service.MappingService;

/**
 * Created by emma on 11/07/2016.
 *
 * @author emma
 */
@RunWith(MockitoJUnitRunner.class)
public class AssociationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AssociationRepository associationRepository;

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private EfoTraitRepository efoTraitRepository;

    @Mock
    private AssociationDownloadService associationDownloadService;

    @Mock
    private SnpAssociationTableViewService snpAssociationTableViewService;

    @Mock
    private SingleSnpMultiSnpAssociationService singleSnpMultiSnpAssociationService;

    @Mock
    private SnpInteractionAssociationService snpInteractionAssociationService;

    @Mock
    private CheckEfoTermAssignmentService checkEfoTermAssignmentService;

    @Mock
    private AssociationOperationsService associationOperationsService;

    @Mock
    private MappingService mappingService;

    @Mock
    private AssociationUploadService associationUploadService;

    @Mock
    private CurrentUserDetailsService currentUserDetailsService;

    @Mock
    private AssociationValidationReportService associationValidationReportService;

    @Mock
    private AssociationDeletionService associationDeletionService;

    @Mock
    private AssociationEventsViewService associationsEventsViewService;

    @Mock
    private StudyAssociationBatchDeletionEventService studyAssociationBatchDeletionEventService;

    @Mock
    private EnsemblRestTemplateService ensemblRestTemplateService;

    @Mock
    private CheckMappingService checkMappingService;

    @Mock
    private MapCatalogService mapCatalogService;

    private static final SecureUser SECURE_USER =
            new SecureUserBuilder().setId(564L).setEmail("test@test.com").setPasswordHash("738274$$").build();

    private static final Study STUDY = new StudyBuilder().setId(1234L).build();

    private static final Association ASSOCIATION =
            new AssociationBuilder().setId(100L)
                    .setStudy(STUDY)
                    .build();

    private static final Association EDITED_ASSOCIATION =
            new AssociationBuilder()
                    .setStudy(STUDY)
                    .build();

    @Before
    public void setUp() throws Exception {
        AssociationController associationController = new AssociationController(associationRepository,
                studyRepository,
                efoTraitRepository,
                associationDownloadService,
                snpAssociationTableViewService,
                singleSnpMultiSnpAssociationService,
                snpInteractionAssociationService,
                checkEfoTermAssignmentService,
                associationOperationsService,
                associationUploadService,
                currentUserDetailsService,
                associationValidationReportService,
                associationDeletionService,
                associationsEventsViewService,
                studyAssociationBatchDeletionEventService,
                ensemblRestTemplateService,
                checkMappingService,
                mapCatalogService);

        mockMvc = MockMvcBuilders.standaloneSetup(associationController).build();
        //System.setProperty("ensembl.server", "http://rest.ensembl.org");
    }

/*
    @Test
    public void viewStudySnps() throws Exception {

        SnpAssociationTableView snpAssociationTableView = new SnpAssociationTableView();
        LastViewedAssociation lastViewedAssociation = new LastViewedAssociation();

        when(associationRepository.findByStudyId(STUDY.getId())).thenReturn(Collections.singletonList(ASSOCIATION));
        when(snpAssociationTableViewService.createSnpAssociationTableView(ASSOCIATION)).thenReturn(
                snpAssociationTableView);
        when(associationOperationsService.getLastViewedAssociation(Matchers.anyLong())).thenReturn(lastViewedAssociation);
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);

        mockMvc.perform(get("/studies/1234/associations").accept(MediaType.TEXT_HTML_VALUE))
                .andExpect(status().isOk())
                .andExpect(model().attribute("snpAssociationTableViews", hasSize(1)))
                .andExpect(model().attribute("snpAssociationTableViews", instanceOf(Collection.class)))
                .andExpect(model().attribute("lastViewedAssociation", instanceOf(LastViewedAssociation.class)))
                .andExpect(model().attribute("totalAssociations", 1))
                .andExpect(model().attributeExists("study"))
                .andExpect(view().name("study_association"));

        verify(associationRepository, times(1)).findByStudyId(STUDY.getId());
        verify(snpAssociationTableViewService, times(1)).createSnpAssociationTableView(ASSOCIATION);
        verify(associationOperationsService, times(1)).getLastViewedAssociation(Matchers.anyLong());
        verify(studyRepository, times(1)).findOne(Matchers.anyLong());
    }

    @Test
    public void getAssociationEvents() throws Exception {

        AssociationEventView eventView =
                new AssociationEventView("EVENT", new Date(), (long) 121, "test@email.com", "rs123, rs456");

        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);
        when(associationsEventsViewService.createViews(STUDY.getId())).thenReturn(Collections.singletonList(eventView));
        mockMvc.perform(get("/studies/1234/association_tracking").accept(MediaType.TEXT_HTML_VALUE))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attribute("events", hasSize(1)))
                .andExpect(model().attribute("events", instanceOf(List.class)))
                .andExpect(view().name("association_events"));
    }

    @Test
    public void uploadStudySnpsFileWithError() throws Exception {

        // Create objects required for testing
        MockMultipartFile file =
                new MockMultipartFile("file", "filename.txt", "text/plain", "TEST".getBytes());
        AssociationUploadErrorView associationUploadErrorView1 =
                new AssociationUploadErrorView(1, "OR", "Value is not empty", false, "data");
        List<AssociationUploadErrorView> uploadErrorViews = Collections.singletonList(associationUploadErrorView1);

        // Stubbing
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);

        Future<Boolean> f = mock(Future.class);

        HashMap<String, Object> sessionattr = new HashMap<String, Object>();
        sessionattr.put("fileName", file.getOriginalFilename());
        sessionattr.put("fileErrors", uploadErrorViews);
        sessionattr.put("future", f);

        mockMvc.perform(get("/studies/1234/associations/getUploadResults").sessionAttrs(sessionattr).param("studyId", "1234"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attribute("fileName", file.getOriginalFilename()))
                .andExpect(model().attribute("fileErrors", instanceOf(List.class)))
                .andExpect(model().attribute("fileErrors", hasSize(1)))
                .andExpect(model().attributeExists("study"))
                .andExpect(view().name("error_pages/association_file_upload_error"));

        verify(studyRepository, times(1)).findOne(Matchers.anyLong());
    }

    @Test
    public void uploadStudySnpsFileWithNoError() throws Exception {

        // Create objects required for testing
        MockMultipartFile file =
                new MockMultipartFile("file", "filename.txt", "text/plain", "TEST".getBytes());

        // Stubbing
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);
        when(currentUserDetailsService.getUserFromRequest(Matchers.any(HttpServletRequest.class))).thenReturn(
                SECURE_USER);
        when(associationUploadService.upload(file, STUDY, SECURE_USER)).thenReturn(Collections.EMPTY_LIST);

        MvcResult mvcResult = this.mockMvc.perform(fileUpload("/studies/1234/associations/upload").file(file).param("studyId", "1234"))
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult("association_upload_progress"))
                .andReturn();

        this.mockMvc.perform(asyncDispatch(mvcResult))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(model().attributeExists("study"))
                .andExpect(forwardedUrl("association_upload_progress"));
        verify(studyRepository, times(1)).findOne(Matchers.anyLong());

    }

    @Test
    public void uploadStudySnpsFileWithMappingError() throws Exception {

        // Create objects required for testing
        MockMultipartFile file =
                new MockMultipartFile("file", "filename.txt", "text/plain", "TEST".getBytes());


        // Stubbing
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);

        Future<Boolean> f = mock(Future.class);

        HashMap<String, Object> sessionattr = new HashMap<String, Object>();
        sessionattr.put("fileName", file.getOriginalFilename());
        sessionattr.put("ensemblMappingFailure", true);
        sessionattr.put("exception", new EnsemblMappingException());
        sessionattr.put("future", f);

        mockMvc.perform(get("/studies/1234/associations/getUploadResults").sessionAttrs(sessionattr).param("studyId", "1234"))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(model().attributeExists("study"))
                .andExpect(forwardedUrl("ensembl_mapping_failure"));
        verify(studyRepository, times(1)).findOne(Matchers.anyLong());
    }

    @Test
    public void addStandardSnpsWithRowErrors() throws Exception {
        // Row errors prevent saving
        AssociationValidationView associationValidationView =
                new AssociationValidationView("SNP", "Value is empty", false);
        List<AssociationValidationView> errors = Collections.singletonList(associationValidationView);

        // Stubbing
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);
        when(associationOperationsService.checkSnpAssociationFormErrors(Matchers.any(SnpAssociationStandardMultiForm.class),
                Matchers.anyString()))
                .thenReturn(errors);

        mockMvc.perform(post("/studies/1234/associations/add_standard").param("measurementType", "or"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", instanceOf(SnpAssociationStandardMultiForm.class)))
                .andExpect(model().attribute("errors", instanceOf(List.class)))
                .andExpect(model().attribute("errors", hasSize(1)))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("measurementType"))
                .andExpect(view().name("add_standard_snp_association"));

        //verify properties of bound object
        ArgumentCaptor<SnpAssociationStandardMultiForm> formArgumentCaptor =
                ArgumentCaptor.forClass(SnpAssociationStandardMultiForm.class);
        verify(associationOperationsService).checkSnpAssociationFormErrors(formArgumentCaptor.capture(),
                Matchers.anyString());
        verify(studyRepository, times(1)).findOne(Matchers.anyLong());
        verifyZeroInteractions(singleSnpMultiSnpAssociationService);
        verify(associationOperationsService, never()).saveAssociationCreatedFromForm(Matchers.any(Study.class),
                Matchers.any(Association.class),
                Matchers.any(SecureUser.class),
                Matchers.anyString());
        verifyZeroInteractions(currentUserDetailsService);
    }

    @Test
    public void addStandardSnpsWithErrors() throws Exception {

        AssociationValidationView associationValidationView =
                new AssociationValidationView("OR", "Value is empty", false);
        List<AssociationValidationView> errors = Collections.singletonList(associationValidationView);

        // Stubbing
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);
        when(currentUserDetailsService.getUserFromRequest(Matchers.any(HttpServletRequest.class))).thenReturn(
                SECURE_USER);
        when(associationOperationsService.checkSnpAssociationFormErrors(Matchers.any(SnpAssociationStandardMultiForm.class),
                Matchers.anyString()))
                .thenReturn(Collections.EMPTY_LIST);
        when(singleSnpMultiSnpAssociationService.createAssociation(Matchers.any(SnpAssociationStandardMultiForm.class)))
                .thenReturn(ASSOCIATION);
        when(associationOperationsService.saveAssociationCreatedFromForm(STUDY, ASSOCIATION, SECURE_USER, "")).thenReturn(
                errors);

        when(ensemblRestTemplateService.getRelease()).thenReturn("88");
        mockMvc.perform(post("/studies/1234/associations/add_standard").param("measurementType", "or"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", instanceOf(SnpAssociationStandardMultiForm.class)))
                .andExpect(model().attribute("errors", instanceOf(List.class)))
                .andExpect(model().attribute("errors", hasSize(1)))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("measurementType"))
                .andExpect(view().name("add_standard_snp_association"));

        //verify properties of bound object
        ArgumentCaptor<SnpAssociationStandardMultiForm> formArgumentCaptor =
                ArgumentCaptor.forClass(SnpAssociationStandardMultiForm.class);
        verify(associationOperationsService).checkSnpAssociationFormErrors(formArgumentCaptor.capture(),
                Matchers.anyString());
        verify(singleSnpMultiSnpAssociationService).createAssociation(formArgumentCaptor.capture());
        verify(studyRepository, times(1)).findOne(Matchers.anyLong());
        verify(currentUserDetailsService, times(1)).getUserFromRequest(Matchers.any(HttpServletRequest.class));
        verify(associationOperationsService, times(1)).saveAssociationCreatedFromForm(Matchers.any(Study.class),
                Matchers.any(Association.class),
                Matchers.any(SecureUser.class),
                Matchers.anyString());
    }

    @Test
    public void addStandardSnpsWithNoErrors() throws Exception {

        AssociationValidationView associationValidationView =
                new AssociationValidationView("SNP", "SNP identifier rs34tt is not valid", true);
        List<AssociationValidationView> errors = Collections.singletonList(associationValidationView);

        // Stubbing
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);
        when(currentUserDetailsService.getUserFromRequest(Matchers.any(HttpServletRequest.class))).thenReturn(
                SECURE_USER);
        when(associationOperationsService.checkSnpAssociationFormErrors(Matchers.any(SnpAssociationStandardMultiForm.class),
                Matchers.anyString()))
                .thenReturn(Collections.EMPTY_LIST);
        when(singleSnpMultiSnpAssociationService.createAssociation(Matchers.any(SnpAssociationStandardMultiForm.class)))
                .thenReturn(ASSOCIATION);
        when(associationOperationsService.saveAssociationCreatedFromForm(STUDY, ASSOCIATION, SECURE_USER, "88")).thenReturn(
                errors);

        mockMvc.perform(post("/studies/1234/associations/add_standard").param("measurementType", "or"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("measurementType"))
                .andExpect(view().name("redirect:/associations/100"));

        //verify properties of bound object
        ArgumentCaptor<SnpAssociationStandardMultiForm> formArgumentCaptor =
                ArgumentCaptor.forClass(SnpAssociationStandardMultiForm.class);
        verify(associationOperationsService).checkSnpAssociationFormErrors(formArgumentCaptor.capture(),
                Matchers.anyString());
        verify(singleSnpMultiSnpAssociationService).createAssociation(formArgumentCaptor.capture());
        verify(studyRepository, times(1)).findOne(Matchers.anyLong());
        verify(currentUserDetailsService, times(1)).getUserFromRequest(Matchers.any(HttpServletRequest.class));
        verify(associationOperationsService, times(1)).saveAssociationCreatedFromForm(Matchers.any(Study.class),
                Matchers.any(Association.class),
                Matchers.any(SecureUser.class),
                Matchers.anyString());
    }

    @Test
    public void addMultiSnpsWithRowErrors() throws Exception {
        AssociationValidationView associationValidationView =
                new AssociationValidationView("SNP", "Value is empty", false);
        List<AssociationValidationView> errors = Collections.singletonList(associationValidationView);

        // Stubbing
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);
        when(associationOperationsService.checkSnpAssociationFormErrors(Matchers.any(SnpAssociationStandardMultiForm.class),
                Matchers.anyString()))
                .thenReturn(errors);

        mockMvc.perform(post("/studies/1234/associations/add_multi").param("measurementType", "or"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", instanceOf(SnpAssociationStandardMultiForm.class)))
                .andExpect(model().attribute("errors", instanceOf(List.class)))
                .andExpect(model().attribute("errors", hasSize(1)))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("measurementType"))
                .andExpect(view().name("add_multi_snp_association"));

        //verify properties of bound object
        ArgumentCaptor<SnpAssociationStandardMultiForm> formArgumentCaptor =
                ArgumentCaptor.forClass(SnpAssociationStandardMultiForm.class);
        verify(associationOperationsService).checkSnpAssociationFormErrors(formArgumentCaptor.capture(),
                Matchers.anyString());
        verify(studyRepository, times(1)).findOne(Matchers.anyLong());
        verifyZeroInteractions(singleSnpMultiSnpAssociationService);
        verifyZeroInteractions(currentUserDetailsService);
        verify(associationOperationsService, never()).saveAssociationCreatedFromForm(Matchers.any(Study.class),
                Matchers.any(Association.class),
                Matchers.any(SecureUser.class),
                Matchers.anyString());
    }

    @Test
    public void addMultiSnpsWithErrors() throws Exception {
        AssociationValidationView associationValidationView =
                new AssociationValidationView("OR", "Value is empty", false);
        List<AssociationValidationView> errors = Collections.singletonList(associationValidationView);

        // Stubbing
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);
        when(currentUserDetailsService.getUserFromRequest(Matchers.any(HttpServletRequest.class))).thenReturn(
                SECURE_USER);
        when(associationOperationsService.checkSnpAssociationFormErrors(Matchers.any(SnpAssociationStandardMultiForm.class),
                Matchers.anyString()))
                .thenReturn(Collections.EMPTY_LIST);
        when(singleSnpMultiSnpAssociationService.createAssociation(Matchers.any(SnpAssociationStandardMultiForm.class)))
                .thenReturn(ASSOCIATION);
        when(associationOperationsService.saveAssociationCreatedFromForm(STUDY, ASSOCIATION, SECURE_USER, "")).thenReturn(
                errors);

        mockMvc.perform(post("/studies/1234/associations/add_multi").param("measurementType", "or"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", instanceOf(SnpAssociationStandardMultiForm.class)))
                .andExpect(model().attribute("errors", instanceOf(List.class)))
                .andExpect(model().attribute("errors", hasSize(1)))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("measurementType"))
                .andExpect(view().name("add_multi_snp_association"));

        //verify properties of bound object
        ArgumentCaptor<SnpAssociationStandardMultiForm> formArgumentCaptor =
                ArgumentCaptor.forClass(SnpAssociationStandardMultiForm.class);
        verify(associationOperationsService).checkSnpAssociationFormErrors(formArgumentCaptor.capture(),
                Matchers.anyString());
        verify(singleSnpMultiSnpAssociationService).createAssociation(formArgumentCaptor.capture());
        verify(studyRepository, times(1)).findOne(Matchers.anyLong());
        verify(currentUserDetailsService, times(1)).getUserFromRequest(Matchers.any(HttpServletRequest.class));
        verify(associationOperationsService, times(1)).saveAssociationCreatedFromForm(Matchers.any(Study.class),
                Matchers.any(Association.class),
                Matchers.any(SecureUser.class),
                Matchers.anyString());
    }

    @Test
    public void addMultiSnpsWithNoErrors() throws Exception {
        AssociationValidationView associationValidationView =
                new AssociationValidationView("SNP", "SNP identifier rs34tt is not valid", true);
        List<AssociationValidationView> errors = Collections.singletonList(associationValidationView);

        // Stubbing
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);
        when(currentUserDetailsService.getUserFromRequest(Matchers.any(HttpServletRequest.class))).thenReturn(
                SECURE_USER);
        when(associationOperationsService.checkSnpAssociationFormErrors(Matchers.any(SnpAssociationStandardMultiForm.class),
                Matchers.anyString()))
                .thenReturn(Collections.EMPTY_LIST);
        when(singleSnpMultiSnpAssociationService.createAssociation(Matchers.any(SnpAssociationStandardMultiForm.class)))
                .thenReturn(ASSOCIATION);
        when(associationOperationsService.saveAssociationCreatedFromForm(STUDY, ASSOCIATION, SECURE_USER,"")).thenReturn(
                errors);

        mockMvc.perform(post("/studies/1234/associations/add_multi").param("measurementType", "or"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("measurementType"))
                .andExpect(view().name("redirect:/associations/100"));

        //verify properties of bound object
        ArgumentCaptor<SnpAssociationStandardMultiForm> formArgumentCaptor =
                ArgumentCaptor.forClass(SnpAssociationStandardMultiForm.class);
        verify(associationOperationsService).checkSnpAssociationFormErrors(formArgumentCaptor.capture(),
                Matchers.anyString());
        verify(singleSnpMultiSnpAssociationService).createAssociation(formArgumentCaptor.capture());
        verify(studyRepository, times(1)).findOne(Matchers.anyLong());
        verify(currentUserDetailsService, times(1)).getUserFromRequest(Matchers.any(HttpServletRequest.class));
        verify(associationOperationsService, times(1)).saveAssociationCreatedFromForm(Matchers.any(Study.class),
                Matchers.any(Association.class),
                Matchers.any(SecureUser.class),
                Matchers.anyString());
    }

    @Test
    public void addSnpInteractionWithRowErrors() throws Exception {
        AssociationValidationView associationValidationView =
                new AssociationValidationView("SNP", "Value is empty", false);
        List<AssociationValidationView> errors = Collections.singletonList(associationValidationView);

        // Stubbing
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);
        when(associationOperationsService.checkSnpAssociationInteractionFormErrors(Matchers.any(
                SnpAssociationInteractionForm.class), Matchers.anyString()))
                .thenReturn(errors);

        mockMvc.perform(post("/studies/1234/associations/add_interaction").param("measurementType", "or"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", instanceOf(SnpAssociationInteractionForm.class)))
                .andExpect(model().attribute("errors", instanceOf(List.class)))
                .andExpect(model().attribute("errors", hasSize(1)))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("measurementType"))
                .andExpect(view().name("add_snp_interaction_association"));

        //verify properties of bound object
        ArgumentCaptor<SnpAssociationInteractionForm> formArgumentCaptor =
                ArgumentCaptor.forClass(SnpAssociationInteractionForm.class);
        verify(associationOperationsService).checkSnpAssociationInteractionFormErrors(formArgumentCaptor.capture(),
                Matchers.anyString());
        verify(studyRepository, times(1)).findOne(Matchers.anyLong());
        verifyZeroInteractions(snpInteractionAssociationService);
        verifyZeroInteractions(currentUserDetailsService);
    }

    @Test
    public void addSnpInteractionWithErrors() throws Exception {
        AssociationValidationView associationValidationView =
                new AssociationValidationView("OR", "Value is empty", false);
        List<AssociationValidationView> errors = Collections.singletonList(associationValidationView);

        // Stubbing
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);
        when(currentUserDetailsService.getUserFromRequest(Matchers.any(HttpServletRequest.class))).thenReturn(
                SECURE_USER);
        when(associationOperationsService.checkSnpAssociationInteractionFormErrors(Matchers.any(
                SnpAssociationInteractionForm.class), Matchers.anyString()))
                .thenReturn(Collections.EMPTY_LIST);
        when(snpInteractionAssociationService.createAssociation(Matchers.any(SnpAssociationInteractionForm.class)))
                .thenReturn(ASSOCIATION);
        when(associationOperationsService.saveAssociationCreatedFromForm(STUDY, ASSOCIATION, SECURE_USER,"")).thenReturn(
                errors);

        mockMvc.perform(post("/studies/1234/associations/add_interaction").param("measurementType", "or"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", instanceOf(SnpAssociationInteractionForm.class)))
                .andExpect(model().attribute("errors", instanceOf(List.class)))
                .andExpect(model().attribute("errors", hasSize(1)))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("measurementType"))
                .andExpect(view().name("add_snp_interaction_association"));

        //verify properties of bound object
        ArgumentCaptor<SnpAssociationInteractionForm> formArgumentCaptor =
                ArgumentCaptor.forClass(SnpAssociationInteractionForm.class);
        verify(associationOperationsService).checkSnpAssociationInteractionFormErrors(formArgumentCaptor.capture(),
                                                                                      Matchers.anyString());
        verify(snpInteractionAssociationService).createAssociation(formArgumentCaptor.capture());
        verify(studyRepository, times(1)).findOne(Matchers.anyLong());
        verify(currentUserDetailsService, times(1)).getUserFromRequest(Matchers.any(HttpServletRequest.class));
        verify(associationOperationsService, times(1)).saveAssociationCreatedFromForm(Matchers.any(Study.class),
                Matchers.any(Association.class),
                Matchers.any(SecureUser.class),
                Matchers.anyString());
    }

    @Test
    public void addSnpInteractionsWithNoErrors() throws Exception {
        AssociationValidationView associationValidationView =
                new AssociationValidationView("SNP", "SNP identifier rs34tt is not valid", true);
        List<AssociationValidationView> errors = Collections.singletonList(associationValidationView);

        // Stubbing
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);
        when(currentUserDetailsService.getUserFromRequest(Matchers.any(HttpServletRequest.class))).thenReturn(
                SECURE_USER);
        when(associationOperationsService.checkSnpAssociationInteractionFormErrors(Matchers.any(
                SnpAssociationInteractionForm.class), Matchers.anyString()))
                .thenReturn(Collections.EMPTY_LIST);
        when(snpInteractionAssociationService.createAssociation(Matchers.any(SnpAssociationInteractionForm.class)))
                .thenReturn(ASSOCIATION);
        when(associationOperationsService.saveAssociationCreatedFromForm(STUDY, ASSOCIATION, SECURE_USER,"")).thenReturn(
                errors);

        mockMvc.perform(post("/studies/1234/associations/add_interaction").param("measurementType", "or"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("measurementType"))
                .andExpect(view().name("redirect:/associations/100"));

        //verify properties of bound object
        ArgumentCaptor<SnpAssociationInteractionForm> formArgumentCaptor =
                ArgumentCaptor.forClass(SnpAssociationInteractionForm.class);
        verify(associationOperationsService).checkSnpAssociationInteractionFormErrors(formArgumentCaptor.capture(),
                Matchers.anyString());
        verify(snpInteractionAssociationService).createAssociation(formArgumentCaptor.capture());
        verify(studyRepository, times(1)).findOne(Matchers.anyLong());
        verify(currentUserDetailsService, times(1)).getUserFromRequest(Matchers.any(HttpServletRequest.class));
        verify(associationOperationsService, times(1)).saveAssociationCreatedFromForm(Matchers.any(Study.class),
                Matchers.any(Association.class),
                Matchers.any(SecureUser.class),
                Matchers.anyString());
    }

    @Test
    public void viewAssociation() throws Exception {

    }

    @Test
    public void editAssociationWithWarnings() throws Exception {

        // Warnings will not prevent a save
        AssociationValidationView associationValidationView =
                new AssociationValidationView("SNP", "SNP identifier rs34tt is not valid", true);
        List<AssociationValidationView> errors = Collections.singletonList(associationValidationView);

        // Stubbing
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);
        when(associationRepository.findOne(Matchers.anyLong())).thenReturn(ASSOCIATION);
        when(currentUserDetailsService.getUserFromRequest(Matchers.any(HttpServletRequest.class))).thenReturn(
                SECURE_USER);
        when(associationOperationsService.checkSnpAssociationFormErrors(Matchers.any(SnpAssociationStandardMultiForm.class),
                Matchers.anyString()))
                .thenReturn(Collections.EMPTY_LIST);
        when(singleSnpMultiSnpAssociationService.createAssociation(Matchers.any(SnpAssociationStandardMultiForm.class)))
                .thenReturn(EDITED_ASSOCIATION);
        when(associationOperationsService.saveEditedAssociationFromForm(STUDY,
                EDITED_ASSOCIATION,
                ASSOCIATION.getId(),
                SECURE_USER,"")).thenReturn(errors);

        mockMvc.perform(post("/associations/100").param("associationtype", "multi"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attributeExists("study"))
                .andExpect(view().name("redirect:/associations/100"));

        //verify properties of bound object
        ArgumentCaptor<SnpAssociationStandardMultiForm> formArgumentCaptor =
                ArgumentCaptor.forClass(SnpAssociationStandardMultiForm.class);
        verify(associationOperationsService).checkSnpAssociationFormErrors(formArgumentCaptor.capture(),
                Matchers.anyString());
        verify(singleSnpMultiSnpAssociationService).createAssociation(formArgumentCaptor.capture());
        verify(studyRepository, times(1)).findOne(Matchers.anyLong());
        verify(associationRepository, times(1)).findOne(Matchers.anyLong());
        verify(currentUserDetailsService, times(1)).getUserFromRequest(Matchers.any(HttpServletRequest.class));
        verify(associationOperationsService, times(1)).saveEditedAssociationFromForm(Matchers.any(Study.class),
                Matchers.any(Association.class),
                Matchers.anyLong(),
                Matchers.any(SecureUser.class),
                Matchers.anyString());
    }

    @Test
    public void editAssociationNonCriticalErrors() throws Exception {

        // This error should prevent a save
        AssociationValidationView associationValidationView =
                new AssociationValidationView("P-value exponent", "Value is empty", false);
        List<AssociationValidationView> errors = Collections.singletonList(associationValidationView);

        MappingDetails mappingDetails = new MappingDetails();

        // Stubbing
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);
        when(associationRepository.findOne(Matchers.anyLong())).thenReturn(ASSOCIATION);
        when(currentUserDetailsService.getUserFromRequest(Matchers.any(HttpServletRequest.class))).thenReturn(
                SECURE_USER);
        when(associationOperationsService.checkSnpAssociationInteractionFormErrors(Matchers.any(
                SnpAssociationInteractionForm.class), Matchers.anyString()))
                .thenReturn(Collections.EMPTY_LIST);
        when(snpInteractionAssociationService.createAssociation(Matchers.any(SnpAssociationInteractionForm.class)))
                .thenReturn(EDITED_ASSOCIATION);
        when(associationOperationsService.saveEditedAssociationFromForm(STUDY,
                EDITED_ASSOCIATION,
                ASSOCIATION.getId(),
                SECURE_USER,"")).thenReturn(errors);
        when(associationOperationsService.determineIfAssociationIsOrType(Matchers.any(Association.class))).thenReturn(
                "or");
        when(associationOperationsService.createMappingDetails(Matchers.any(Association.class))).thenReturn(
                mappingDetails);


        mockMvc.perform(post("/associations/100").param("associationtype", "interaction"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attribute("measurementType", "or"))
                .andExpect(model().attributeExists("mappingDetails"))
                .andExpect(model().attribute("form", instanceOf(SnpAssociationInteractionForm.class)))
                .andExpect(model().attribute("errors", instanceOf(List.class)))
                .andExpect(model().attribute("errors", hasSize(1)))
                .andExpect(view().name("edit_snp_interaction_association"));

        //verify properties of bound object
        ArgumentCaptor<SnpAssociationInteractionForm> formArgumentCaptor =
                ArgumentCaptor.forClass(SnpAssociationInteractionForm.class);
        verify(associationOperationsService).checkSnpAssociationInteractionFormErrors(formArgumentCaptor.capture(),
                Matchers.anyString());
        verify(snpInteractionAssociationService).createAssociation(formArgumentCaptor.capture());
        verify(studyRepository, times(1)).findOne(Matchers.anyLong());
        verify(associationRepository, times(1)).findOne(Matchers.anyLong());
        verify(currentUserDetailsService, times(1)).getUserFromRequest(Matchers.any(HttpServletRequest.class));
        verify(associationOperationsService, times(1)).saveEditedAssociationFromForm(Matchers.any(Study.class),
                Matchers.any(Association.class),
                Matchers.anyLong(),
                Matchers.any(SecureUser.class),
                Matchers.anyString());
        verify(associationOperationsService, times(1)).determineIfAssociationIsOrType(Matchers.any(Association.class));
        verify(associationOperationsService, times(1)).createMappingDetails(Matchers.any(Association.class));
        verify(snpInteractionAssociationService,
                times(1)).createAssociation(Matchers.any(SnpAssociationInteractionForm.class));
        verifyZeroInteractions(singleSnpMultiSnpAssociationService);
    }

    @Test
    public void editAssociationCriticalErrors() throws Exception {

        AssociationValidationView associationValidationView =
                new AssociationValidationView("SNP", "Value is empty", false);
        List<AssociationValidationView> errors = Collections.singletonList(associationValidationView);

        MappingDetails mappingDetails = new MappingDetails();

        // Stubbing
        when(studyRepository.findOne(Matchers.anyLong())).thenReturn(STUDY);
        when(associationRepository.findOne(Matchers.anyLong())).thenReturn(ASSOCIATION);
        when(associationOperationsService.checkSnpAssociationInteractionFormErrors(Matchers.any(
                SnpAssociationInteractionForm.class), Matchers.anyString())).thenReturn(errors);
        when(associationOperationsService.determineIfAssociationIsOrType(Matchers.any(Association.class))).thenReturn(
                "or");
        when(associationOperationsService.createMappingDetails(Matchers.any(Association.class))).thenReturn(
                mappingDetails);

        mockMvc.perform(post("/associations/100").param("associationtype", "interaction"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attribute("measurementType", "or"))
                .andExpect(model().attributeExists("mappingDetails"))
                .andExpect(model().attribute("form", instanceOf(SnpAssociationInteractionForm.class)))
                .andExpect(model().attribute("errors", instanceOf(List.class)))
                .andExpect(model().attribute("errors", hasSize(1)))
                .andExpect(view().name("edit_snp_interaction_association"));

        //verify properties of bound object
        ArgumentCaptor<SnpAssociationInteractionForm> formArgumentCaptor =
                ArgumentCaptor.forClass(SnpAssociationInteractionForm.class);
        verify(associationOperationsService).checkSnpAssociationInteractionFormErrors(formArgumentCaptor.capture(),
                Matchers.anyString());
        verify(studyRepository, times(1)).findOne(Matchers.anyLong());
        verify(associationRepository, times(1)).findOne(Matchers.anyLong());
        verify(associationOperationsService, times(1)).checkSnpAssociationInteractionFormErrors(Matchers.any(
                SnpAssociationInteractionForm.class), Matchers.anyString());
        verify(associationOperationsService, never()).saveEditedAssociationFromForm(Matchers.any(Study.class),
                Matchers.any(Association.class),
                Matchers.anyLong(),
                Matchers.any(SecureUser.class),
                Matchers.anyString());
        verify(associationOperationsService, times(1)).determineIfAssociationIsOrType(Matchers.any(Association.class));
        verify(associationOperationsService, times(1)).createMappingDetails(Matchers.any(Association.class));
        verify(snpInteractionAssociationService,
                never()).createAssociation(Matchers.any(SnpAssociationInteractionForm.class));
        verifyZeroInteractions(singleSnpMultiSnpAssociationService);
    }

    @Test
    public void deleteAllAssociations() throws Exception {

        when(associationRepository.findByStudyId(STUDY.getId())).thenReturn(Collections.singletonList(ASSOCIATION));
        when(currentUserDetailsService.getUserFromRequest(Matchers.any(HttpServletRequest.class))).thenReturn(
                SECURE_USER);

        mockMvc.perform(get("/studies/1234/associations/delete_all").accept(MediaType.TEXT_HTML_VALUE))
                .andExpect(status().is3xxRedirection());

        verify(associationRepository, times(1)).findByStudyId(STUDY.getId());
        verify(currentUserDetailsService, times(1)).getUserFromRequest(Matchers.any(HttpServletRequest.class));
        verify(studyAssociationBatchDeletionEventService, times(1)).createBatchUploadEvent(STUDY.getId(),
                1,
                SECURE_USER);
        verify(associationDeletionService, times(1)).deleteAssociation(ASSOCIATION, SECURE_USER);
    }

    @Test
    public void deleteAllAssociationsWhereStudyHasNoAssociations() throws Exception {

        // Case where study has no association attached
        when(associationRepository.findByStudyId(STUDY.getId())).thenReturn(Collections.EMPTY_LIST);
        when(currentUserDetailsService.getUserFromRequest(Matchers.any(HttpServletRequest.class))).thenReturn(
                SECURE_USER);

        mockMvc.perform(get("/studies/1234/associations/delete_all").accept(MediaType.TEXT_HTML_VALUE))
                .andExpect(status().is3xxRedirection());

        verify(associationRepository, times(1)).findByStudyId(STUDY.getId());
        verifyZeroInteractions(currentUserDetailsService);
        verifyZeroInteractions(studyAssociationBatchDeletionEventService);
        verifyZeroInteractions(associationDeletionService);
    }
*/

    @Test
    public void deleteChecked() throws Exception {

    }

    @Test
    public void approveSnpAssociation() throws Exception {

    }

    @Test
    public void unapproveSnpAssociation() throws Exception {

    }

    @Test
    public void approveChecked() throws Exception {

    }

    @Test
    public void unapproveChecked() throws Exception {

    }

    @Test
    public void approveAll() throws Exception {

    }

    @Test
    public void validateAll() throws Exception {

    }

    @Test
    public void downloadStudySnps() throws Exception {

    }

    @Test
    public void applyStudyEFOtraitToSnps() throws Exception {

    }

    @Test
    public void handleDataIntegrityException() throws Exception {

    }

    @Test
    public void handleInvalidFormatExceptionAndInvalidOperationException() throws Exception {

    }

    @Test
    public void handleIOException() throws Exception {

    }

    @Test
    public void handleFileUploadException() throws Exception {

    }

    @Test
    public void handleFileNotFound() throws Exception {

    }

    @Test
    public void populateEfoTraits() throws Exception {

    }
}