package org.cuner.emotion.analysis;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang3.StringUtils;
import org.cuner.emotion.analysis.constant.WordConstant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by houan on 18/3/14.
 */
public class AnalysisTool {

    protected static String separateSymbolString = new String(WordConstant.SENTENCE_SPLIT_SYMBOL);

    /**
     * 将文本根据标点符号拆分成短句
     * 原始文本: "这是一块让人心动的小花布，缤纷的花瓣雨洒落初秋的微风里。有点文艺气息的姑娘一定第一眼就爱上了吧！衣服刚刚收到,拆开包裹,店家的包装盒也很漂亮,衬衣的料子摸起来挺软的,很舒服,做工比较精致~~值得购买哦~~~";
     * 返回结果: [这是一块让人心动的小花布， | 缤纷的花瓣雨洒落初秋的微风里。 | 有点文艺气息的姑娘一定第一眼就爱上了吧！ | 衣服刚刚收到, | 拆开包裹, | 店家的包装盒也很漂亮, | 衬衣的料子摸起来挺软的, | 很舒服, | 做工比较精致~~ | 值得购买哦~~~]
     * @param text 原始文本
     * @return 子句集合
     */
    public static List<String> splitText2Sentences(String text) {
        List<String> sentences = new ArrayList<>();
        StringTokenizer tokens = new StringTokenizer(text, separateSymbolString, true);
        int firstTokenCnt = tokens.countTokens() - 1;
        StringBuilder subSentences = new StringBuilder();
        String token;
        while (tokens.hasMoreElements()) {
            token = (String) tokens.nextElement();
            if (firstTokenCnt == tokens.countTokens()) {
                subSentences.append(token);
                continue;
            }

            if (1 == token.length() && separateSymbolString.indexOf(token.charAt(0)) != -1) {
                subSentences.append(token);
                continue;
            }

            sentences.add(subSentences.toString());
            subSentences = new StringBuilder();
            subSentences.append(token);
        }
        sentences.add(subSentences.toString());
        return sentences;
    }


    /**
     * 调用 IKAnalysis 开源工具包,对句子进行分词
     * @param sentence 子句
     * @return
     */
    public static List<Term> splitSentence2Word(String sentence) {
        if (StringUtils.isBlank(sentence)) {
            return Collections.emptyList();
        }

        Result parse = ToAnalysis.parse(sentence);
        return parse.getTerms();
    }
}
