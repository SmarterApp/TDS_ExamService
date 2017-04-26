package tds.exam.services.impl;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.repositories.ExamItemQueryRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.services.ExamPageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamPageServiceImplTest {

    @Mock
    private ExamPageCommandRepository mockExamPageCommandRepository;

    @Mock
    private ExamPageQueryRepository mockExamPageQueryRepository;

    @Mock
    private ExamItemQueryRepository mockExamItemQueryRepository;

    private ExamPageService examPageService;

    @Before
    public void setUp() {
        examPageService = new ExamPageServiceImpl(mockExamPageQueryRepository,
            mockExamPageCommandRepository,
            mockExamItemQueryRepository);
    }

    @Test
    public void shouldReturnAllPagesForExam() {
        final UUID examId = UUID.randomUUID();
        ExamPage examPage1 = new ExamPageBuilder()
            .build();
        ExamPage examPage2 = new ExamPageBuilder()
            .withId(UUID.randomUUID())
            .build();
        List<ExamPage> examPages = new ArrayList<>();
        examPages.add(examPage1);
        examPages.add(examPage2);

        when(mockExamPageQueryRepository.findAll(examId)).thenReturn(examPages);
        List<ExamPage> retExamPages = examPageService.findAllPages(examId);
        assertThat(retExamPages).hasSize(2);
    }

    @Test
    public void shouldDeletePagesForExamId() {
        final UUID examId = UUID.randomUUID();
        examPageService.deletePages(examId);
        verify(mockExamPageCommandRepository).deleteAll(examId);
    }

    @Test
    public void shouldInsertPagesForExamId() {
        ExamPage examPage1 = new ExamPageBuilder()
            .build();

        examPageService.insertPages(examPage1);
        verify(mockExamPageCommandRepository).insert(examPage1);
    }

    @Test
    public void shouldGetAnExamPageWithItems() {
        UUID mockExamId = UUID.randomUUID();

        UUID firstItemId = UUID.randomUUID();
        Instant respondedAtInstant = Instant.now().minus(200000);
        ExamItem mockFirstExamItem = new ExamItemBuilder()
            .withId(firstItemId)
            .withExamPageId(ExamPageBuilder.DEFAULT_ID)
            .withItemKey("187-1234")
            .withRequired(true)
            .withResponse(new ExamItemResponse.Builder()
                .withExamItemId(firstItemId)
                .withResponse("first item response")
                .withValid(true)
                .withCreatedAt(respondedAtInstant)
                .build())
            .build();
        UUID secondItemId = UUID.randomUUID();
        ExamItem mockSecondExamItem = new ExamItemBuilder()
            .withId(secondItemId)
            .withExamPageId(ExamPageBuilder.DEFAULT_ID)
            .withItemKey("187-5678")
            .withAssessmentItemKey(5678L)
            .withItemType("TEST")
            .withItemFilePath("/path/to/item/187-5678.xml")
            .build();

        List<ExamItem> mockExamItems = Arrays.asList(mockFirstExamItem, mockSecondExamItem);

        ExamPage mockExamPage = new ExamPageBuilder()
            .withExamId(mockExamId)
            .withExamItems(mockExamItems)
            .build();

        when(mockExamPageQueryRepository.findPageWithItems(mockExamId, mockExamPage.getPagePosition()))
            .thenReturn(Optional.of(mockExamPage));

        Response<ExamPage> examPageResponse = examPageService.getPage(mockExamId, mockExamPage.getPagePosition());
        verify(mockExamPageQueryRepository).findPageWithItems(mockExamPage.getExamId(), mockExamPage.getPagePosition());
        verify(mockExamPageCommandRepository).update(any(ExamPage[].class));

        assertThat(examPageResponse.getData().isPresent()).isTrue();
        assertThat(examPageResponse.hasError()).isFalse();

        ExamPage examPage = examPageResponse.getData().get();
        assertThat(examPage).isEqualToComparingFieldByFieldRecursively(mockExamPage);
    }

    @Test
    public void shouldFindExamPageById() {
        ExamPage examPage = new ExamPageBuilder().build();

        when(mockExamPageQueryRepository.find(examPage.getId())).thenReturn(Optional.of(examPage));

        assertThat(examPageService.find(examPage.getId()).get()).isEqualTo(examPage);
        verify(mockExamPageQueryRepository).find(examPage.getId());
    }

    @Test
    public void shouldUpdateExamPage() {
        ExamPage examPage = new ExamPageBuilder().build();
        ArgumentCaptor<ExamPage> captor = ArgumentCaptor.forClass(ExamPage.class);

        examPageService.update(examPage);
        verify(mockExamPageCommandRepository).update(captor.capture());

        assertThat(captor.getValue()).isEqualTo(examPage);
    }

    @Test
    public void shouldGetAllPagesWithItemsForAnExam() {
        UUID firstPageId = UUID.randomUUID();
        UUID secondPageId = UUID.randomUUID();

        ExamItem firstItem = new ExamItemBuilder().withExamPageId(firstPageId).build();
        ExamItem secondItem = new ExamItemBuilder().withExamPageId(secondPageId).build();

        ExamPage firstPage = new ExamPageBuilder()
            .withId(firstPageId)
            .build();
        ExamPage secondPage = new ExamPageBuilder()
            .withId(secondPageId)
            .build();

        Exam exam = new ExamBuilder().build();

        when(mockExamPageQueryRepository.findAll(any(UUID.class)))
            .thenReturn(Arrays.asList(firstPage, secondPage));
        when(mockExamItemQueryRepository.findExamItemAndResponses(any(UUID.class)))
            .thenReturn(Arrays.asList(firstItem, secondItem));

        List<ExamPage> result = examPageService.findAllPagesWithItems(exam.getId());
        verify(mockExamPageQueryRepository).findAll(any(UUID.class));
        verify(mockExamItemQueryRepository).findExamItemAndResponses(any(UUID.class));

        assertThat(result).hasSize(2);

        ExamPage firstPageResult = result.get(0);
        assertThat(firstPageResult.getExamItems()).hasSize(1);
        ExamItem firstPageResultExamItem = firstPageResult.getExamItems().get(0);
        assertThat(firstPageResultExamItem).isEqualToComparingFieldByFieldRecursively(firstItem);

        ExamPage secondPageResult = result.get(1);
        assertThat(secondPageResult.getExamItems()).hasSize(1);
        ExamItem secondPageResultExamItem = secondPageResult.getExamItems().get(0);
        assertThat(secondPageResultExamItem).isEqualToComparingFieldByFieldRecursively(secondItem);
    }
}
