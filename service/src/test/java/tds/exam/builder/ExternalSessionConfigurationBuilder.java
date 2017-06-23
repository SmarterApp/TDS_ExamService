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

import tds.session.ExternalSessionConfiguration;

public class ExternalSessionConfigurationBuilder {
    private String clientName = "SBAC_PT";
    private String environment = "Development";
    private int shiftWindowStart;
    private int shiftWindowEnd;
    private int shiftFormStart;
    private int shiftFormEnd;

    public ExternalSessionConfiguration build() {
        return new ExternalSessionConfiguration(
            clientName,
            environment,
            shiftWindowStart,
            shiftWindowEnd,
            shiftFormStart,
            shiftFormEnd
        );
    }

    public ExternalSessionConfigurationBuilder withClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    public ExternalSessionConfigurationBuilder withEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public ExternalSessionConfigurationBuilder withShiftWindowStart(int shiftWindowStart) {
        this.shiftWindowStart = shiftWindowStart;
        return this;
    }

    public ExternalSessionConfigurationBuilder withShiftWindowEnd(int shiftWindowEnd) {
        this.shiftWindowEnd = shiftWindowEnd;
        return this;
    }

    public ExternalSessionConfigurationBuilder withShiftFormStart(int shiftFormStart) {
        this.shiftFormStart = shiftFormStart;
        return this;
    }

    public ExternalSessionConfigurationBuilder withShiftFormEnd(int shiftFormEnd) {
        this.shiftFormEnd = shiftFormEnd;
        return this;
    }
}
