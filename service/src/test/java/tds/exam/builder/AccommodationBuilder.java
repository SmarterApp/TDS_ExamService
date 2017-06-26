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

package tds.exam.builder;

import tds.accommodation.Accommodation;

public class AccommodationBuilder {
    private Accommodation.Builder builder;

    public AccommodationBuilder() {
        builder = new Accommodation.Builder()
            .withAccommodationCode("ENU")
            .withAccommodationType("Language")
            .withAccommodationValue("ENU")
            .withSegmentKey("segmentKey")
            .withSegmentPosition(0)
            .withTypeMode("typeMode");
    }

    public Accommodation build() {
        return builder.build();
    }

    public AccommodationBuilder withSegmentPosition(int segmentPosition) {
        builder.withSegmentPosition(segmentPosition);
        return this;
    }

    public AccommodationBuilder withDisableOnGuestSession(boolean disableOnGuestSession) {
        builder.withDisableOnGuestSession(disableOnGuestSession);
        return this;
    }

    public AccommodationBuilder withToolTypeSortOrder(int toolTypeSortOrder) {
        builder.withToolTypeSortOrder(toolTypeSortOrder);
        return this;
    }

    public AccommodationBuilder withToolValueSortOrder(int toolValueSortOrder) {
        builder.withToolValueSortOrder(toolValueSortOrder);
        return this;
    }

    public AccommodationBuilder withToolMode(String toolMode) {
        builder.withToolMode(toolMode);
        return this;
    }

    public AccommodationBuilder withType(String accType) {
        builder.withAccommodationType(accType);
        return this;
    }

    public AccommodationBuilder withValue(String accValue) {
        builder.withAccommodationValue(accValue);
        return this;
    }

    public AccommodationBuilder withCode(String accCode) {
        builder.withAccommodationCode(accCode);
        return this;
    }

    public AccommodationBuilder withAllowCombine(boolean allowCombine) {
        builder.withAllowCombine(allowCombine);
        return this;
    }

    public AccommodationBuilder withFunctional(boolean functional) {
        builder.withFunctional(functional);
        return this;
    }

    public AccommodationBuilder withSelectable(boolean selectable) {
        builder.withSelectable(selectable);
        return this;
    }

    public AccommodationBuilder withVisible(boolean visible) {
        builder.withVisible(visible);
        return this;
    }

    public AccommodationBuilder withStudentControl(boolean studentControl) {
        builder.withStudentControl(studentControl);
        return this;
    }

    public AccommodationBuilder withEntryControl(boolean entryControl) {
        builder.withEntryControl(entryControl);
        return this;
    }

    public AccommodationBuilder withDependsOnToolType(String dependsOnToolType) {
        builder.withDependsOnToolType(dependsOnToolType);
        return this;
    }

    public AccommodationBuilder withDefaultAccommodation(boolean defaultAccommodation) {
        builder.withDefaultAccommodation(defaultAccommodation);
        return this;
    }

    public AccommodationBuilder withTypeMode(String typeMode) {
        builder.withTypeMode(typeMode);
        return this;
    }

    public AccommodationBuilder withAllowChange(boolean allowChange) {
        builder.withAllowChange(allowChange);
        return this;
    }

    public AccommodationBuilder withSegmentKey(String segmentKey) {
        builder.withSegmentKey(segmentKey);
        return this;
    }

    public AccommodationBuilder withTypeTotal(int typeTotal) {
        builder.withTypeTotal(typeTotal);
        return this;
    }
}
