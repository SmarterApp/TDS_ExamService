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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import tds.assessment.Form;
import tds.assessment.Segment;
import tds.exam.services.FormSelector;

@Component
public class RoundRobinFormSelectorImpl implements FormSelector {
    // Keeps track of the index of the next form to assign for a segment
    private Cache<String, FormIndex> formIndexCache;

    public RoundRobinFormSelectorImpl() {
        formIndexCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build();
    }

    @Override
    public Optional<Form> selectForm(final Segment segment, final String languageCode) {
        List<Form> forms = segment.getForms(languageCode);
        int index = 0;

        if (forms.isEmpty()) {
            return Optional.empty();
        }

        int formSize = forms.size();
        if (formSize > 1) { // Round robin multi-form
            index = selectNextIndex(segment.getKey(), formSize);
        }

        return Optional.of(forms.get(index));
    }

    private int selectNextIndex(String segmentKey, int formSize) {
        int index = 0;

        if (!formIndexCache.asMap().containsKey((segmentKey))) {
            formIndexCache.put(segmentKey, new FormIndex());
        } else {
            index = formIndexCache.getIfPresent(segmentKey).getNext(formSize);
        }

        return index;
    }

    private class FormIndex {
        private AtomicInteger currentIndex = new AtomicInteger(0);

        private int getNext(int max) {
            int returnIndex;
            if (currentIndex.get() + 1 == max) {
                currentIndex.set(0);
                returnIndex = currentIndex.get();
            } else {
                returnIndex = currentIndex.incrementAndGet();
            }

            return returnIndex;
        }
    }
}
