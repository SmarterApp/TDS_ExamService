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

package tds.exam.configuration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExamServicePropertiesTest {
    private ExamServiceProperties properties;

    @Before
    public void setUp() {
        properties = new ExamServiceProperties();
    }

    @After
    public void tearDown() {}

    @Test
    public void itShouldAppendSlashIfNotPresentForSessionUrl(){
        properties.setSessionUrl("http://localhost:8080/sessions");
        assertThat(properties.getSessionUrl()).isEqualTo("http://localhost:8080/sessions");

        properties.setSessionUrl("http://localhost:8080/sessions/");
        assertThat(properties.getSessionUrl()).isEqualTo("http://localhost:8080/sessions");
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldNotAllowNullSesssionUrl() {
        properties.setSessionUrl(null);
    }

    @Test
    public void itShouldAppendSlashIfNotPresentForStudentUrl() {
        properties.setStudentUrl("http://localhost:8080/students");
        assertThat(properties.getStudentUrl()).isEqualTo("http://localhost:8080/students");

        properties.setSessionUrl("http://localhost:8080/students/");
        assertThat(properties.getStudentUrl()).isEqualTo("http://localhost:8080/students");
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldNotAllowNullStudentUrl() {
        properties.setStudentUrl(null);
    }
}
