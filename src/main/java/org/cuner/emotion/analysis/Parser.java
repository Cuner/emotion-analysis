package org.cuner.emotion.analysis;

import org.ansj.domain.Term;
import org.cuner.emotion.analysis.dictionary.Dictionary;
import org.cuner.emotion.analysis.entity.Emotion;

import java.util.*;


/**
 * Created by houan on 18/4/23.
 */
public class Parser {

    private EmotionAnalyze analyze = new EmotionAnalyze();

    /**
     * 分析文本倾向性
     * 1.过滤 -> 不满足要求的直接不继续下去玩
     * 2.匹配 -> get 属性词和情感词
     * 3.切分
     * 4.分析
     *
     * @param content
     */
    public List<Emotion> parseElement(String content) {
        List<Emotion> emotionList = new ArrayList<>();
        // 过滤不需要判断的对象
        if (filter(content)) {
            return emotionList;
        }

        //匹配属性词和情感词
        Set<String> matchProperties = new HashSet<>();
        Set<String> matchEmotions = new HashSet<>();
        if (!matchPropertyAndEmotion(content, matchProperties, matchEmotions)) {
            // 没有命中的属性次or情感词,则不用继续玩下去了
//            log.info("has no properties! radData: {}", rawData);
            return emotionList;
        }

        //切分
        Map<String, List<String>> splitedSentences = splitComment(matchProperties, matchEmotions, content);
        if (splitedSentences.size() == 0) {
            return emotionList;
        }

        //迭代分析
        for (Map.Entry<String, List<String>> entry : splitedSentences.entrySet()) {
            Emotion analyzeEmotionResult = analyze.analyze(entry.getKey(), new ArrayList<>(entry.getValue()));
            emotionList.add(analyzeEmotionResult);
        }

        return emotionList;
    }

    private boolean filter(String content) {
        Boolean isFilter = false;
        if (content == null || content.length() < 3) {
            isFilter = true;
        }
        return isFilter;
    }

    /**
     * 分词,然后get命中的属性词和情感词
     *
     * @param content         基本的数据
     * @param matchProperties 命中的属性词列表
     * @param matchEmotions   命中的情感词列表
     * @return true 表示有词命中; false 则表示不用继续了,没有关键词
     */
    private boolean matchPropertyAndEmotion(String content, Set<String> matchProperties, Set<String> matchEmotions) {
        List<Term> parse = AnalysisTool.splitSentence2Word(content);

        String word;
        for (Term term : parse) {
            word = term.getName();
            if (Dictionary.getInstance().containsKeyInCommonProperties(word)) {
                matchProperties.add(word);
            } else if (Dictionary.getInstance().containsKeyInCommonEmotions(word)) {
                matchEmotions.add(word);
            } else if (word != null) {  //对于没有出现在属性词库的，则认为是新词
                Dictionary.getInstance().statNewWord(word);
            }
        }

        return matchEmotions.size() > 0 || matchProperties.size() > 0;
    }

    /**
     * 根据匹配到的词语，将评论的长句进行切分
     * 切分规则：匹配词A，B
     * 评论原句：************,****A***,*****,******,****,*****B**,***.
     * 匹配结果：|<---------A对应的子句----->||<---------B对应的子句---->|
     * 匹配返回一个HashMap，key为匹配到的属性词，Value为对应匹配到的子句
     *
     * @param matchProperties 文本中匹配到的属性词
     * @param matchEmotions   文本中匹配的到情感词
     * @param comment         待匹配的数据结构
     * @return
     */
    private Map<String, List<String>> splitComment(Set<String> matchProperties, Set<String> matchEmotions, String comment) {
        // 将评价的文本根据特殊字符划分为短句
        List<String> sentences = AnalysisTool.splitText2Sentences(comment);

        // key 为匹配到的属性词, value为对应的子句集合
        Map<String, List<String>> sentencesOfProperties = new HashMap<>();
        // key 为词根, value为对应的子句集合
        Map<String, List<String>> sentencesOfRoot = new HashMap<>();
        List<String> sentenceList;
        for (String sentence : sentences) {
            boolean hasProperty = false;

            // 匹配属性词
            for (String matchProperty : matchProperties) {
                if (!sentence.contains(matchProperty)) {
                    continue; // 不包含属性词, 退出
                }
                hasProperty = true;
                //get 词根
                String rootProperty = Dictionary.getInstance().getRootProperty(matchProperty);
                if (sentencesOfRoot.containsKey(rootProperty)) {
                    sentenceList = sentencesOfRoot.get(rootProperty);
                } else {
                    sentenceList = new ArrayList<>(16);
                }
                sentenceList.add(sentence);
                sentencesOfRoot.put(rootProperty, sentenceList);
                sentencesOfProperties.put(matchProperty, sentenceList);
            }

            if (!hasProperty) {
                for (String matchEmotion : matchEmotions) {
                    if (!sentence.contains(matchEmotion)) {
                        continue;
                    }
                    //获取情感词的属性词根
                    String rootProperty = Dictionary.getInstance().getEmotionRootProperty(matchEmotion);

                    if (sentencesOfRoot.containsKey(rootProperty)) {
                        sentenceList = sentencesOfRoot.get(rootProperty);
                    } else {
                        sentenceList = new ArrayList<>(16);
                    }
                    sentenceList.add(sentence);
                    sentencesOfRoot.put(rootProperty, sentenceList);
                    sentencesOfProperties.put(matchEmotion, sentenceList);
                }
            }
        }

        return sentencesOfProperties;
    }

}
