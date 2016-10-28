package tds.exam.repositories.impl;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import tds.exam.Accommodation;
import tds.exam.repositories.AccommodationQueryRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class AccommodationQueryRepositoryIntegrationTests {
    @Autowired
    @Qualifier("commandDataSource")
    private DataSource commandDataSource;

    AccommodationQueryRepository accommodationQueryRepository;
    NamedParameterJdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        accommodationQueryRepository = new AccommodationQueryRepositoryImpl(commandDataSource);
        jdbcTemplate = new NamedParameterJdbcTemplate(commandDataSource);

        final UUID firstExamId = UUID.randomUUID();
        final UUID secondExamId = UUID.randomUUID();

        List<Accommodation> mockAccommodations = new ArrayList<>();


        mockAccommodations.forEach(this::insertMockAccommodationData);
    }

    private void insertMockAccommodationData(Accommodation accommodation) {

    }
}
