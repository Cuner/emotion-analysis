package org.cuner.emotion.analysis.test;

import org.apache.commons.collections4.CollectionUtils;
import org.cuner.emotion.analysis.Parser;
import org.cuner.emotion.analysis.dictionary.Dictionary;
import org.cuner.emotion.analysis.entity.Emotion;
import org.cuner.emotion.analysis.entity.Word;
import org.cuner.emotion.analysis.repository.WordRepository;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by houan on 18/4/23.
 */
public class ParserTest {

    private WordRepository wordRepository = new WordRepository();

    public void init() {
        List<Word> wordList = wordRepository.getWordListFromDB();
        Dictionary.getInstance().loadDictionary(wordList);
        Dictionary.getInstance().initDistionary();
    }

    @Test
    public void parseElement() {
        init();
        Parser parser = new Parser();
        List<Emotion> emotionList = parser.parseElement("质量很好没话说。");
        Assert.assertTrue(CollectionUtils.isNotEmpty(emotionList));
    }
}
