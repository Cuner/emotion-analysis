package org.cuner.emotion.analysis.test;

import org.cuner.emotion.analysis.entity.Word;
import org.cuner.emotion.analysis.repository.WordRepository;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by houan on 18/4/23.
 */
public class WordRepositoryTest {

    private WordRepository wordRepository = new WordRepository();

    @Test
    public void getWordListByXML() {
        List<Word> list = wordRepository.getWordListFormXml();
        Assert.assertTrue(list.size() > 0);
    }

    @Test
    public void getWordListByDB() {
        List<Word> list = wordRepository.getWordListFromDB();
        Assert.assertTrue(list.size() > 0);
    }
}
