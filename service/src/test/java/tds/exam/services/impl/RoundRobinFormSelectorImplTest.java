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


import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import tds.assessment.Form;
import tds.assessment.Segment;
import tds.common.Algorithm;
import tds.exam.services.FormSelector;

import static org.assertj.core.api.Assertions.assertThat;


public class RoundRobinFormSelectorImplTest {
    private FormSelector formSelector;

    @Before
    public void setUp() {
        formSelector = new RoundRobinFormSelectorImpl();
    }

    @Test
    public void shouldReturnFormsRoundRobin() {
        final String languageCode = "ENU";

        Form enuForm1 = new Form.Builder("formkey1")
            .withLanguage(languageCode)
            .build();
        Form enuForm2 = new Form.Builder("formkey2")
            .withLanguage(languageCode)
            .build();
        Form enuForm3 = new Form.Builder("formkey3")
            .withLanguage(languageCode)
            .build();
        Form enuForm4 = new Form.Builder("formkey4")
            .withLanguage(languageCode)
            .build();
        Form esnForm = new Form.Builder("formkey5")
            .withLanguage("ESN")
            .build();

        Segment segment = new Segment("segmentKey", Algorithm.FIXED_FORM);
        segment.setForms(Arrays.asList(enuForm1, enuForm2, enuForm3, enuForm4, esnForm));

        Optional<Form> maybeRetEnuForm1A = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetEnuForm1A).isPresent();
        Form retForm1 = maybeRetEnuForm1A.get();
        assertThat(retForm1.getKey()).isEqualTo(enuForm1.getKey());

        Optional<Form> maybeRetEnuForm2A = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetEnuForm2A).isPresent();
        Form retForm2A = maybeRetEnuForm2A.get();
        assertThat(retForm2A.getKey()).isEqualTo(enuForm2.getKey());

        Optional<Form> maybeRetEnuForm3A = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetEnuForm3A).isPresent();
        Form retForm3A = maybeRetEnuForm3A.get();
        assertThat(retForm3A.getKey()).isEqualTo(enuForm3.getKey());

        Optional<Form> maybeRetEnuForm4A = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetEnuForm4A).isPresent();
        Form retForm4A = maybeRetEnuForm4A.get();
        assertThat(retForm4A.getKey()).isEqualTo(enuForm4.getKey());

        Optional<Form> maybeRetEnuForm1B = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetEnuForm1B).isPresent();
        Form retForm1B = maybeRetEnuForm1B.get();
        assertThat(retForm1B.getKey()).isEqualTo(enuForm1.getKey());

        Optional<Form> maybeRetEnuForm2B = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetEnuForm2B).isPresent();
        Form retForm2B = maybeRetEnuForm2B.get();
        assertThat(retForm2B.getKey()).isEqualTo(enuForm2.getKey());

        Optional<Form> maybeRetEnuForm3B = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetEnuForm3B).isPresent();
        Form retForm3B = maybeRetEnuForm3B.get();
        assertThat(retForm3B.getKey()).isEqualTo(enuForm3.getKey());

        Optional<Form> maybeRetEnuForm4B = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetEnuForm4B).isPresent();
        Form retForm4B = maybeRetEnuForm4B.get();
        assertThat(retForm4B.getKey()).isEqualTo(enuForm4.getKey());
    }
}
