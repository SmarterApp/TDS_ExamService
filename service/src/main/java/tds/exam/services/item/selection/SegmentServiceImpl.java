package tds.exam.services.item.selection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import tds.assessment.ContentLevelSpecification;
import tds.assessment.Segment;
import tds.assessment.SegmentItemInformation;
import tds.common.web.exceptions.NotFoundException;
import tds.dll.common.performance.caching.CacheType;
import tds.exam.services.AssessmentService;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.BpElement;
import tds.itemselection.impl.blueprint.ReportingCategory;
import tds.itemselection.impl.math.AAMath;
import tds.itemselection.loader.TestSegment;
import tds.itemselection.services.SegmentService;

/**
 * This is a port of the {@link tds.itemselection.loader.SegmentCollection2} for loading segment data
 */
@Service
public class SegmentServiceImpl implements SegmentService {
    private final AssessmentService assessmentService;

    @Autowired
    public SegmentServiceImpl(final AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @Override
    public TestSegment getSegment(final String segmentKey) throws Exception {
        return getSegment(null, segmentKey);
    }

    @Override
    @Cacheable(CacheType.LongTerm)
    public TestSegment getSegment(UUID sessionKey, String segmentKey) throws Exception {
        //The session key is only present for caching purposes in the legacy SegmentCollection2 class
        //It is unused for a data access purpose.  We are using Spring caching instead.

        SegmentItemInformation segmentItemInformation = assessmentService.findSegmentItemInformation(segmentKey)
            .orElseThrow(() -> new NotFoundException("Unable to find the requested segment by %s", segmentKey));

        TestSegment testSegment = new TestSegment(segmentKey, false);

        Segment segment = segmentItemInformation.getSegment();

        //Load data from our segment object
        testSegment.refreshMinutes = segment.getRefreshMinutes();
        testSegment.parentTest = segment.getAssessmentKey();
        testSegment.position = segment.getPosition();

        Blueprint blueprint = testSegment.getBp();

        populateBlueprintFromSegment(blueprint, segment);
        populateBlueprintConsraints(blueprint, segmentItemInformation.getContentLevelSpecifications());

        return null;
    }

    private static void populateBlueprintConsraints(final Blueprint blueprint, final List<ContentLevelSpecification> specs) {
        //Blueprint.initializeBluePrintConstraints

        HashMap<String, ReportingCategory> reportingCategories = new HashMap<>();
        HashMap<String, BpElement> bluePrintElements = new HashMap<>();

        for(ContentLevelSpecification spec : specs) {
            if(spec.isReportingCategory()) {
                ReportingCategory rc = convertReportingCateogry(spec);
                reportingCategories.put(rc.ID, rc);
                blueprint.elements.addBpElement(rc);
            } else {
                BpElement bpElement = new BpElement();
                updateBlueprintElement(bpElement, spec);
                bluePrintElements.put(bpElement.ID, bpElement);
                blueprint.elements.addBpElement(bpElement);
            }
        }

        blueprint.setReportingCategories(reportingCategories);
        blueprint.setBluePrintElements(bluePrintElements);
    }

    private static void updateBlueprintElement(BpElement bpElement, ContentLevelSpecification spec) {
        bpElement.ID = spec.getContentLevel();
        bpElement.minRequired = spec.getMinItems();
        bpElement.maxRequired = spec.getMaxItems();
        bpElement.isStrictMax = spec.isStrictMax();
        bpElement.weight = (double) spec.getBpWeight();
        bpElement.bpElementType = BpElement.BpElementType.values()[spec.getElementType()];
    }

    private static ReportingCategory convertReportingCateogry(ContentLevelSpecification spec) {
        //ReportingCategory.intialize
        ReportingCategory reportingCategory = new ReportingCategory();
        updateBlueprintElement(reportingCategory, spec);
        reportingCategory.startInfo = spec.getStartInfo();
        reportingCategory.info = spec.getStartInfo();
        reportingCategory.startAbility = spec.getStartAbility();
        reportingCategory.standardError = AAMath.SEfromInfo(reportingCategory.startInfo);

        reportingCategory.abilityWeight = spec.getAbilityWeight() != null && spec.getAbilityWeight() != 0
            ? spec.getAbilityWeight().doubleValue()
            : ReportingCategory.DEFAULT_ABILITY_WEIGHT;
        reportingCategory.adaptiveWeight = spec.getScalar() != null && spec.getScalar() != 0
            ? spec.getScalar()
            : ReportingCategory.DEFAULT_ADAPTIVE_WEIGHT;
        reportingCategory.precisionTarget = spec.getPrecisionTarget() != null
            ? spec.getPrecisionTarget()
            : ReportingCategory.DEFAULT_PRECISION_TARGET;
        reportingCategory.precisionTargetMetWeight = spec.getPrecisionTargetMetWeight() != null && spec.getPrecisionTarget() != 0
            ? spec.getPrecisionTargetMetWeight()
            : ReportingCategory.DEFAULT_PRECISION_TARGET_MET_WEIGHT;
        reportingCategory.precisionTargetNotMetWeight = spec.getPrecisionTargetNotMetWeight() != null && spec.getPrecisionTargetNotMetWeight() != 0
            ? spec.getPrecisionTargetNotMetWeight()
            : ReportingCategory.DEFAULT_PRECISION_TARGET_NOT_MET_WEIGHT;

        reportingCategory.isStrand = false;
        reportingCategory.minLambda = reportingCategory.lambda = ReportingCategory.DEFAULT_LAMBDA;
        reportingCategory.setReportingCategory(true);
        return reportingCategory;
    }

    private static void populateBlueprintFromSegment(final Blueprint blueprint, final Segment segment) {
        //Port of Bluepring initialization in Blueprint.initializeOverallBluePrint


        blueprint.segmentKey = segment.getKey();
//        blueprint.segmentID
//        minOpItemsTest = long2Integer(record, "minOpItemsTest");
//        maxOpItemsTest = long2Integer(record, "maxOpItemsTest");
        blueprint.segmentPosition = segment.getPosition();
        blueprint.minOpItems = segment.getMinItems();
        blueprint.maxOpItems = segment.getMaxItems();
        blueprint.bpWeight = (double) segment.getBlueprintWeight();
        blueprint.itemWeight = segment.getItemWeight();
        blueprint.abilityOffset = segment.getAbilityOffset();
        blueprint.abilityOffset = segment.getAbilityOffset();
        blueprint.randomizerIndex = segment.getRandomizer();
        blueprint.randomizerInitialIndex = segment.getInitialRandom();
        blueprint.startInfo = segment.getStartInfo();
        blueprint.info = segment.getStartInfo();
        blueprint.standardError = AAMath.SEfromInfo(segment.getStartInfo());
        blueprint.startAbility = segment.getStartAbility();
        blueprint.theta = blueprint.startAbility;
        blueprint.cset1Order = segment.getCandidateSet1Order();
        blueprint.cSet1Size = segment.getCandidateSet1Size();
        blueprint.cSet2Size = (blueprint.randomizerIndex <= blueprint.cSet1Size)
            ? blueprint.randomizerIndex
            : blueprint.cSet1Size;

        blueprint.slope = segment.getSlope();
        blueprint.intercept = segment.getIntercept();
        blueprint.adaptiveVersion = segment.getAdaptiveVersion();
        blueprint.overallInformationMatchWeight = (double) segment.getAbilityWeight();
        blueprint.abilityWeight = (double) segment.getAbilityWeight();
        blueprint.rcAbilityWeight = (double) segment.getReportingCandidateAbilityWeight();

        blueprint.overallTargetInformation = (double) segment.getPrecisionTarget();
        blueprint.precisionTarget = (double) segment.getPrecisionTarget();
        blueprint.precisionTargetMetWeight = segment.getPrecisionTargetMetWeight();
        blueprint.precisionTargetNotMetWeight = segment.getPrecisionTargetNotMetWeight();

        blueprint.fieldTestStartPosition = segment.getFieldTestStartPosition();
        blueprint.fieldTestEndPosition = segment.getFieldTestEndPosition();
        blueprint.fieldTestMaximumItems = segment.getFieldTestMaxItems();
        blueprint.fieldTestMinimumItems = segment.getFieldTestMinItems();

        blueprint.adaptiveCut = (double) segment.getAdaptiveCut();
        blueprint.tooCloseSEs = (double) segment.getTooCloseStandardErrors();

        blueprint.terminateBasedOnCount = segment.isTerminationMinCount();
        blueprint.terminateBasedOnOverallInformation = segment.isTerminationOverallInformation();
        blueprint.terminateBasedOnReportingCategoryInformation = segment.isTerminationReportingCategoryInfo();
        blueprint.terminateBasedOnScoreTooClose = segment.isTerminationTooClose();
        blueprint.terminateBaseOnFlagsAnd = segment.isTerminationFlagsAnd();
    }
}
