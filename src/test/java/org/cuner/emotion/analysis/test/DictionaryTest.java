package org.cuner.emotion.analysis.test;

import org.apache.commons.collections4.MapUtils;
import org.cuner.emotion.analysis.dictionary.Dictionary;
import org.cuner.emotion.analysis.entity.Word;
import org.cuner.emotion.analysis.repository.WordRepository;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Created by houan on 18/4/23.
 */
public class DictionaryTest {

    private WordRepository wordRepository = new WordRepository();

    @Test
    public void loadDictionary() {
        List<Word> wordList = wordRepository.getWordListFromDB();
        Dictionary.getInstance().loadDictionary(wordList);
        Dictionary.getInstance().initDistionary();
        Map<String, Word> map = Dictionary.getInstance().getWordBank();
        Assert.assertTrue(MapUtils.isNotEmpty(map));
    }
}
