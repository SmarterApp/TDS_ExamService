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

package tds.exam.configuration.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import tds.exam.services.ExamApprovalService;
import tds.exam.web.interceptors.VerifyAccessInterceptor;

/**
 * Configure additional interceptors for Spring
 */
@Configuration
public class InterceptorConfiguration extends WebMvcConfigurerAdapter {

    private final ExamApprovalService examApprovalService;

    @Autowired
    public InterceptorConfiguration(final ExamApprovalService examApprovalService) {
        this.examApprovalService = examApprovalService;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new VerifyAccessInterceptor(examApprovalService)).addPathPatterns("/exam/**");
    }
}
