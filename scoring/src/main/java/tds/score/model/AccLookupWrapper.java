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

package tds.score.model;

import org.apache.commons.lang.builder.HashCodeBuilder;

import tds.itemrenderer.data.AccLookup;

public class AccLookupWrapper {
    private AccLookup accLookup;

    public AccLookupWrapper(AccLookup accLookup) {
        this.accLookup = accLookup;
    }

    public AccLookup getValue() {
        return this.accLookup;
    }

    @Override
    public int hashCode() {
        if (this.accLookup == null)
            return 0;

        return new HashCodeBuilder(13, 41)
            .appendSuper(this.accLookup.getTypes().hashCode())
            .append(this.accLookup.getPosition())
            .append(this.accLookup.getId())
            .toHashCode();
    }
}
