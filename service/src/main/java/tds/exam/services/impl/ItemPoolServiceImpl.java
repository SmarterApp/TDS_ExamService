/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.assessment.Item;
import tds.assessment.ItemConstraint;
import tds.assessment.ItemProperty;
import tds.exam.ExamAccommodation;
import tds.exam.services.ExamAccommodationService;
import tds.exam.services.ItemPoolService;

@Service
public class ItemPoolServiceImpl implements ItemPoolService {
    private final ExamAccommodationService examAccommodationService;

    @Autowired
    public ItemPoolServiceImpl(final ExamAccommodationService examAccommodationService) {
        this.examAccommodationService = examAccommodationService;
    }

    @Override
    public Set<Item> getItemPool(final UUID examId, final List<ItemConstraint> itemConstraints, final List<Item> items) {
        return getItemPool(examId, itemConstraints, items, null);
    }

    @Override
    public Set<Item> getFieldTestItemPool(final UUID examId, final List<ItemConstraint> itemConstraints, final List<Item> items) {
        return getItemPool(examId, itemConstraints, items, true);
    }

    private Set<Item> getItemPool(final UUID examId, final List<ItemConstraint> itemConstraints, final List<Item> items, final Boolean isFieldTest) {
        /*
            This method is meant to replace StudentDLL._AA_ItempoolString_FNOptimized() [1643]
            The purpose of this method is to find the list of items to include in the segment by taking the following steps:

            1. Retrieve the accommodations that the student has enabled
            2. Find the matching set of (inclusive) item constraints - typically this is "Language"
            3. Find the set of items that satisfy/match the inclusive item constraints
            4. Exclude the items that match the "excluded" accommodations (based on constraints)
        */
        //TODO: remove intermediary steps/optimize
        List<ExamAccommodation> allAccommodations = examAccommodationService.findAllAccommodations(examId);
        // Gather the full list of item props
        List<ItemProperty> allItemProperties = items.stream()
            .flatMap(item -> item.getItemProperties().stream())
            .collect(Collectors.toList());

        // First, get all the constraints for our exam accommodations marked as "inclusive"
        Set<ExamAccommodation> includedAccommodations = allAccommodations.stream()
            .flatMap(accommodation -> itemConstraints.stream()
                .filter(itemConstraint -> itemConstraint.isInclusive() &&
                    itemConstraint.getPropertyName().equalsIgnoreCase(accommodation.getType()) &&
                    itemConstraint.getPropertyValue().equals(accommodation.getCode()))
                .map(itemConstraint -> accommodation))
            .collect(Collectors.toSet());

        // For the included accommodations above, find the list of compatible item ids
        Set<String> itemPoolIds = allItemProperties.stream()
            .flatMap(itemProperty -> includedAccommodations.stream()
                .filter(accommodation ->
                    itemProperty.getName().equalsIgnoreCase(accommodation.getType()) &&
                        itemProperty.getValue().equals(accommodation.getCode()))
                .map(accommodation -> itemProperty.getItemId()))
            .collect(Collectors.toSet());

        /* These next two lambdas represent the "NOT EXISTS" portion in the WHERE clause of the large ItemPoolString
           query. Here we  want to exclude items that may satisfy have a satisfactory "INCLUSIVE" condition met (such
           as having the correct "Language" value), but also have an explicit exclusive condition met               */
        // Get the excluded accommodations
        Set<ExamAccommodation> excludedAccommodations = allAccommodations.stream()
            .flatMap(accommodation -> itemConstraints.stream()
                .filter(itemConstraint -> !itemConstraint.isInclusive() &&
                    itemConstraint.getPropertyName().equalsIgnoreCase(accommodation.getType()) &&
                    itemConstraint.getPropertyValue().equals(accommodation.getCode()))
                .map(itemConstraint -> accommodation))
            .collect(Collectors.toSet());

        // Filter the items from itemprops with excluded accommodations:
        Set<String> excludedItemIds = allItemProperties.stream()
            .flatMap(itemProperty -> excludedAccommodations.stream()
                .filter(accommodation ->
                    itemProperty.getName().equalsIgnoreCase(accommodation.getType()) &&
                        itemProperty.getValue().equals(accommodation.getCode()))
                .map(accommodation -> itemProperty.getItemId()))
            .collect(Collectors.toSet());

        return items.stream()
            .filter(item ->
                (isFieldTest == null || item.isFieldTest() == isFieldTest) &&
                    itemPoolIds.contains(item.getId()) &&
                    !excludedItemIds.contains(item.getId()))
            .collect(Collectors.toSet());
    }
}
