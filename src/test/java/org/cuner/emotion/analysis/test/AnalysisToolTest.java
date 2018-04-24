package org.cuner.emotion.analysis.test;

import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.apache.commons.collections4.CollectionUtils;
import org.cuner.emotion.analysis.AnalysisTool;
import org.cuner.emotion.analysis.entity.Word;
import org.cuner.emotion.analysis.repository.WordRepository;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by houan on 18/4/23.
 */
public class AnalysisToolTest {

    private WordRepository wordRepository = new WordRepository();

    public void init() {
        List<Word> wordList = wordRepository.getWordListFromDB();
        for (Word word : wordList) {
            UserDefineLibrary.insertWord(word.getName(), "userDefine", 1000);
        }
    }

    @Test
    public void splitText2Sentences() {
        List<String> splitSentences = AnalysisTool.splitText2Sentences("这是一块让人心动的小花布，缤纷的花瓣雨洒落初秋的微风里。有点文艺气息的姑娘一定第一眼就爱上了吧！衣服刚刚收到,拆开包裹,店家的包装盒也很漂亮,衬衣的料子摸起来挺软的,很舒服,做工比较精致~~值得购买哦~~~");
        Assert.assertTrue(CollectionUtils.isNotEmpty(splitSentences));
    }

    @Test
    public void splitSentence2Word() {
        List<Term> termList = AnalysisTool.splitSentence2Word("这是一块让人心动的小花布，缤纷的花瓣雨洒落初秋的微风里。有点文艺气息的姑娘一定第一眼就爱上了吧！衣服刚刚收到,拆开包裹,店家的包装盒也很漂亮,衬衣的料子摸起来挺软的,很舒服,做工比较精致~~值得购买哦~~~");
        Assert.assertTrue(CollectionUtils.isNotEmpty(termList));
        init();
        termList = AnalysisTool.splitSentence2Word("质量好的没话说");
        Assert.assertTrue(CollectionUtils.isNotEmpty(termList));
    }
}
