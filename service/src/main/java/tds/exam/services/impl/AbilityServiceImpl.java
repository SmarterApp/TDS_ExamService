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
import java.util.Optional;

import tds.assessment.Assessment;
import tds.exam.Exam;
import tds.exam.models.Ability;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.HistoryQueryRepository;
import tds.exam.services.AbilityService;

@Service
public class AbilityServiceImpl implements AbilityService{
    private final HistoryQueryRepository historyQueryRepository;
    private final ExamQueryRepository examQueryRepository;

    @Autowired
    public AbilityServiceImpl(final HistoryQueryRepository historyQueryRepository, final ExamQueryRepository examQueryRepository) {
        this.historyQueryRepository = historyQueryRepository;
        this.examQueryRepository = examQueryRepository;
    }

    @Override
    public Optional<Double> getInitialAbility(final Exam exam, final Assessment assessment) {
        Optional<Double> ability = Optional.empty();
        float slope = assessment.getAbilitySlope();
        float intercept = assessment.getAbilityIntercept();
        List<Ability> testAbilities = examQueryRepository.findAbilities(exam.getId(), exam.getClientName(),
            assessment.getSubject(), exam.getStudentId());

        // Attempt to retrieve the most recent ability for the current subject and assessment
        Optional<Ability> initialAbility = getMostRecentTestAbilityForSameAssessment(testAbilities, exam.getAssessmentId());
        if (initialAbility.isPresent()) {
            ability = Optional.of(initialAbility.get().getScore());
        } else if (assessment.isInitialAbilityBySubject()) {
            // if no ability for a similar assessment was retrieved above, attempt to get the initial ability for another
            // assessment of the same subject
            initialAbility = getMostRecentTestAbilityForDifferentAssessment(testAbilities, exam.getAssessmentId());
            if (initialAbility.isPresent()) {
                ability = Optional.of(initialAbility.get().getScore());
            } else {
                // if no value was returned from the previous call, get the initial ability from the previous year
                Optional<Double> initialAbilityFromHistory = historyQueryRepository.findAbilityFromHistoryForSubjectAndStudent(
                    exam.getClientName(), exam.getSubject(), exam.getStudentId());

                if (initialAbilityFromHistory.isPresent()) {
                    ability = Optional.of(initialAbilityFromHistory.get() * slope + intercept);
                }
            }
        }

        // If the ability was not retrieved from any of the exam tables, query the assessment service
        if (!ability.isPresent()) {
            ability = Optional.of((double) assessment.getStartAbility());
        }

        return ability;
    }


    /**
     * Gets the most recent {@link tds.exam.models.Ability} based on the dateScored value for the same assessment.
     *
     * @param abilityList  the list of {@link tds.exam.models.Ability}s to iterate through
     * @param assessmentId The test key
     * @return the {@link tds.exam.models.Ability} that lines up with the assessment id
     */
    private Optional<Ability> getMostRecentTestAbilityForSameAssessment(final List<Ability> abilityList, final String assessmentId) {
        for (Ability ability : abilityList) {
            if (assessmentId.equals(ability.getAssessmentId())) {
                /* NOTE: The query that retrieves the list of abilities is sorted by the "date_scored" of the exam in
                   descending order. Therefore we can assume the first match is the most recent */
                return Optional.of(ability);
            }
        }

        return Optional.empty();
    }

    /**
     * Gets the most recent {@link tds.exam.models.Ability} based on the dateScored value for a different assessment.
     *
     * @param abilityList  the list of {@link tds.exam.models.Ability}s to iterate through
     * @param assessmentId The test key
     * @return the {@link tds.exam.models.Ability} that lines up with the assessment id
     */
    private Optional<Ability> getMostRecentTestAbilityForDifferentAssessment(final List<Ability> abilityList, final String assessmentId) {
        for (Ability ability : abilityList) {
            if (!assessmentId.equals(ability.getAssessmentId())) {
                /* NOTE: The query that retrieves the list of abilities is sorted by the "date_scored" of the exam in
                   descending order. Therefore we can assume the first match is the most recent */
                return Optional.of(ability);
            }
        }

        return Optional.empty();
    }
}
