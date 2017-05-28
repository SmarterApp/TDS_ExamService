package tds.exam.services.item.selection;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.assessment.ContentLevelSpecification;
import tds.assessment.Item;
import tds.assessment.ItemControlParameter;
import tds.assessment.ItemGroup;
import tds.assessment.ItemMeasurement;
import tds.assessment.ItemProperty;
import tds.assessment.Segment;
import tds.assessment.SegmentItemInformation;
import tds.dll.common.performance.caching.CacheType;
import tds.exam.services.AssessmentService;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.BpElement;
import tds.itemselection.impl.blueprint.ReportingCategory;
import tds.itemselection.impl.math.AAMath;
import tds.itemselection.impl.sets.ItemPool;
import tds.itemselection.loader.TestSegment;
import tds.itemselection.services.SegmentService;

import static tds.exam.services.item.selection.ItemSelectionMappingUtility.convertItem;

/**
 * This is a port of the {@link tds.itemselection.loader.SegmentCollection2} for loading segment data
 */
@Service
public class SegmentServiceImpl implements SegmentService {
    private static final Logger LOG = LoggerFactory.getLogger(SegmentServiceImpl.class);
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
            .orElseThrow(() -> new ItemSelectionException(String.format("Unable to find the requested segment by %s", segmentKey)));

        TestSegment testSegment = new TestSegment(segmentKey, false);

        Segment segment = segmentItemInformation.getSegment();

        //Load data from our segment object
        testSegment.refreshMinutes = segment.getRefreshMinutes();
        testSegment.parentTest = segment.getAssessmentKey();
        testSegment.position = segment.getPosition();

        Blueprint blueprint = testSegment.getBp();
        ItemPool itemPool = testSegment.getPool();

        populateBlueprintFromSegment(blueprint, segment);
        populateBlueprintConstraints(blueprint, segmentItemInformation.getContentLevelSpecifications());
        populateItemGroups(itemPool, segmentItemInformation.getItemGroups());
        populateSegmentItems(itemPool, segmentItemInformation.getSegmentItems(), blueprint.segmentPosition);
        populateParentItems(itemPool, segmentItemInformation.getParentItems());
        populateItemDimensions(itemPool, segmentItemInformation.getItemMeasurements());
        populateBlueprintOffGradeItemProps(blueprint, segmentItemInformation.getControlParameters());
        populateBlueprintOffGradeItemsDesignator(blueprint, segmentItemInformation.getPoolFilterProperties());

        return testSegment;
    }

    /**
     * Populates the {@link tds.itemselection.impl.blueprint.Blueprint} with blueprint constraints.
     *
     * @param blueprint {@link tds.itemselection.impl.blueprint.Blueprint} to update with constraints
     * @param specs     the {@link tds.assessment.ContentLevelSpecification} data to use to update the blueprint
     */
    private static void populateBlueprintConstraints(final Blueprint blueprint, final List<ContentLevelSpecification> specs) {
        //Blueprint.initializeBluePrintConstraints
        HashMap<String, ReportingCategory> reportingCategories = new HashMap<>();
        HashMap<String, BpElement> bluePrintElements = new HashMap<>();

        for (ContentLevelSpecification spec : specs) {
            if (spec.isReportingCategory()) {
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
        reportingCategory.startAbility = spec.getStartAbility() == null ? 0 : spec.getStartAbility();
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

    /**
     * Populates the {@link tds.itemselection.impl.blueprint.Blueprint} for how to select items in the segment when
     * leveraging an adaptive exam
     *
     * @param blueprint the {@link tds.itemselection.impl.blueprint.Blueprint} to update
     * @param segment   the {@link tds.assessment.Segment} containing the information to add to the blueprint
     */
    private static void populateBlueprintFromSegment(final Blueprint blueprint, final Segment segment) {
        //Port of Bluepring initialization in Blueprint.initializeOverallBluePrint
        blueprint.segmentKey = segment.getKey();
        blueprint.segmentID = segment.getSegmentId();
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

    private static void populateItemGroups(ItemPool itemPool, List<ItemGroup> itemGroups) {
        for (ItemGroup itemGroup : itemGroups) {
            tds.itemselection.base.ItemGroup legacyGroup = new tds.itemselection.base.ItemGroup();
            legacyGroup.setGroupID(itemGroup.getGroupId());
            legacyGroup.setNumberOfItemsRequired(itemGroup.getRequiredItemCount());
            legacyGroup.setMaximumNumberOfItems(itemGroup.getMaxItems());
            itemPool.addItemgroup(legacyGroup);
        }
    }

    private static void populateSegmentItems(ItemPool itemPool, List<Item> items, Integer segmentPosition) {
        for (Item item : items) {
            TestItem testItem = convertItem(item);
            testItem.setSegmentPosition(segmentPosition);

            itemPool.addItem(testItem);

            if (itemPool.getItemGroup(testItem.getGroupID()) == null) {
                String groupId = testItem.getGroupID();
                if (StringUtils.isEmpty(groupId)) {
                    groupId = "I-".concat(testItem.getItemID());
                }

                tds.itemselection.base.ItemGroup group = new tds.itemselection.base.ItemGroup(groupId, 0, 1);
                group.addItem(testItem);
                itemPool.addItemgroup(group);
            } else {
                tds.itemselection.base.ItemGroup group = itemPool.getItemGroup(testItem.groupID);
                group.getItems().add(testItem);
                group.setMaximumNumberOfItems(group.getMaximumNumberOfItems() + 1);
            }
        }
    }

    private static void populateParentItems(ItemPool itemPool, List<Item> items) {
        for (Item item : items) {
            itemPool.addSiblingItem(convertItem(item));
        }
    }

    private static void populateItemDimensions(ItemPool itemPool, List<ItemMeasurement> measurements) throws Exception {
        //Port of ItemPool.InitializeItemDimensions
        for (ItemMeasurement measurement : measurements) {
            TestItem testItem = itemPool.getItem(measurement.getItemKey());

            if (testItem == null) {
                continue;
            }

            testItem.initializeDimensionEntry(measurement.getDimension(),
                measurement.getItemResponseTheoryModel(),
                measurement.getParameterNumber(),
                measurement.getParameterName(),
                measurement.getParameterValue() == null ? null : (double) measurement.getParameterValue()
            );

            testItem.dimensions.get(testItem.dimensions.size() - 1).initializeIRT();
            testItem.hasDimensions = !testItem.dimensions.isEmpty();
        }
    }

    private static void populateBlueprintOffGradeItemProps(Blueprint blueprint, List<ItemControlParameter> itemControlParameters) {
        //Port of Blueprint.initializeBluePrintOffGradeItemsProps
        for (ItemControlParameter param : itemControlParameters) {
            if (StringUtils.isEmpty(param.getValue())) {
                continue;
            }

            if (blueprint.segmentID.equals(param.getBlueprintElementId())) {
                blueprint.offGradeItemsProps.populateBluePrintOffGradeItemsDesignator(param.getName(), param.getValue());
            } else if (blueprint.getReportingCategory(param.getBlueprintElementId()) != null) {
                blueprint.getReportingCategory(param.getBlueprintElementId()).putItemSelectionParam(param.getName(), param.getValue());
            } else if (blueprint.getBPElement(param.getBlueprintElementId()) != null) {
                blueprint.getBPElement(param.getBlueprintElementId()).putItemSelectionParam(param.getName(), param.getValue());
            } else {
                LOG.warn("There is no bpElement with bpElementId = " + param.getBlueprintElementId());
            }
        }
    }

    private static void populateBlueprintOffGradeItemsDesignator(Blueprint blueprint, List<ItemProperty> itemProperties) {
        //Port of Blueprint.initializeBluePrintOffGradeItemsDesignator
        //Assessment service returns all the properties rather than grouping in the DB
        Map<String, List<ItemProperty>> groupedProperties = itemProperties.stream()
            .filter(itemProperty -> itemProperty.getValue() != null)
            .collect(Collectors.groupingBy(ItemProperty::getValue));

        for (Map.Entry<String, List<ItemProperty>> entry : groupedProperties.entrySet()) {
            blueprint.offGradeItemsProps.countByDesignator.put(entry.getKey(), entry.getValue().size());
        }
    }
}
