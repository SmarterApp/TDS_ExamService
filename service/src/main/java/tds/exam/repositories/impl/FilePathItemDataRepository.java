package tds.exam.repositories.impl;

import org.apache.commons.io.IOUtils;

import org.springframework.stereotype.Repository;
import org.springframework.util.StopWatch;

import java.io.FileInputStream;
import java.io.IOException;

import tds.score.repositories.ItemDataRepository;

@Repository
public class FilePathItemDataRepository implements ItemDataRepository {
    @Override
    public String findOne(final String itemPath) throws IOException {
        StopWatch timer = new StopWatch();
        timer.start("FilePathItemDataRepository");
        String result = IOUtils.toString(new FileInputStream(itemPath));
        timer.stop();
        System.out.println(timer.prettyPrint());
        return result;
    }
}
