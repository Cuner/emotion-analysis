package org.cuner.emotion.analysis;

import org.ansj.domain.Term;
import org.cuner.emotion.analysis.constant.WordConstant;
import org.cuner.emotion.analysis.dictionary.Dictionary;
import org.cuner.emotion.analysis.entity.Emotion;
import org.cuner.emotion.analysis.entity.Word;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by yihui on 16/7/6.
 */
public class EmotionAnalyze {

    //主词库
    private Map<String, Word> mainDictionary;

    //词
    private String mainWord;

    //词跟
    private String mainRootWord;

    //对应的句子
    private ArrayList<String> commentContents;

    //匹配的句子
    private String sentencesOfWord;

    private static final int DEGREE_ADJUST = 10;

    public EmotionAnalyze() {
        if (null == mainDictionary) {
            mainDictionary = Dictionary.getInstance().getWordBank();
        }
    }

    /**
     * 情感分析，唯一公开的API
     *
     * @param property  属性词
     * @param sentences 属性词对应的句子
     * @return
     */
    public Emotion analyze(String property, ArrayList<String> sentences) {
        if (property.length() == 0 || sentences.size() == 0) {
            return null;
        }

        mainWord = property;
        if (!property.equals(Dictionary.getInstance().getRootProperty(property))) {
            mainRootWord = Dictionary.getInstance().getRootProperty(property);
        } else if (!property.equals(Dictionary.getInstance().getRootEmotion(property))) {
            mainRootWord = Dictionary.getInstance().getRootEmotion(property);
        } else {
            mainRootWord = Dictionary.getInstance().getRootProperty(property);
        }
        commentContents = sentences;

        sentencesOfWord = "";
        int emotionVal = analyzeEmotion();
        if (emotionVal == (-1) * Integer.MAX_VALUE) {
            return null;
        }
        return new Emotion(emotionVal, mainWord, mainRootWord, sentencesOfWord);
    }


    /**
     * 分析情感，返回一个Int
     *
     * @return
     */
    private int analyzeEmotion() {
        int numOfSentence = commentContents.size();
        double sentencesScores[] = new double[numOfSentence];
        BitSet mainPropertyFlag = new BitSet(numOfSentence);
        int sentenceIndex = 0;
        int errorMatchCount = 0;
        for (String sentence : commentContents) {
            //是否疑问句，疑问句的情感直接中性，因为它既不属于正负二者
            if (sentence.endsWith("?") || sentence.endsWith("？")) {
                sentencesScores[sentenceIndex] = 0;
                sentenceIndex++;
                continue;
            }

            //片段分析
            Set<String> words = segmentationAnalyze(sentence);

            int segmentLength = words.size();
            int index = 0;
            Word propertyWords[] = new Word[segmentLength];
            for (String word : words) {
                propertyWords[index] = mainDictionary.get(word);
                if (word.equals(mainWord)) {
                    mainPropertyFlag.set(sentenceIndex);
                    if (!mainWord.equals(mainRootWord)) {
                        propertyWords[index].setName(mainRootWord);
                    }
                } else if (WordConstant.DIC_PROPERTY_MAPPING_VAL == propertyWords[index].getCategory()) {
                    String rootProperty = Dictionary.getInstance().getRootProperty(word);
                    if (StringUtils.isEmpty(rootProperty)) {
                        rootProperty = word;
                    }
                    if (rootProperty.equals(mainRootWord)) {
                        mainPropertyFlag.set(sentenceIndex);
                        propertyWords[index].setName(mainRootWord);
                    }
                } else if (WordConstant.DIC_EMOTION_MAPPING_VAL == propertyWords[index].getCategory()) {
                    String rootProperty = Dictionary.getInstance().getRootEmotion(word);
                    if (StringUtils.isEmpty(rootProperty)) {
                        rootProperty = word;
                    }
                    if (rootProperty.equals(mainRootWord)) {
                        mainPropertyFlag.set(sentenceIndex);
                        propertyWords[index].setName(mainRootWord);
                    }
                }

                index++;
            }
            if (mainPropertyFlag.get(sentenceIndex)) {
                sentencesScores[sentenceIndex] = _countEmotion(propertyWords, segmentLength);
            } else {
                // 对应关系冗余 例如 mainWord为"不错"，sentence为"很不错" 虽然"很不错"包含"不错"，但是："很不错"句子分词只能得到"很不错"关键字
                // mainWord为"好"，sentences为"XXX好"、"XXX很好"，虽然"XXX很好"包含"好",但是不属于"好"，而属于"很好"这个关键字，所以需要过滤
                sentencesScores[sentenceIndex] = 0;//无效
                errorMatchCount++;
            }
            sentenceIndex++;
        }

        if (errorMatchCount == sentenceIndex) {
            return -1 * Integer.MAX_VALUE;
        }
        return _verifyEmotion(sentencesScores, mainPropertyFlag, sentenceIndex);
    }

    /**
     * 片段分析
     *
     * @param pStentive
     * @return
     */
    private Set<String> segmentationAnalyze(String pStentive) {
        Set<String> SingleResult = new HashSet<>();
        List<Term> parse = AnalysisTool.splitSentence2Word(pStentive);
        for (Term term : parse) {
            String word = term.getName();
            if (mainDictionary.containsKey(word)) {
                SingleResult.add(word);
            }
        }

        return SingleResult;
    }

    private double _countEmotion(Word[] psrc, int len) {
        if (0 == len || null == psrc) {
            return 0;
        }
        double sum = 0;
        double resValue[] = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int index = 0;

        int pCategory;//关键词的类别
        double pWeight;//关键词给定的权重

        double pEmotion = 0;   //情感词权重
        double pMainWeight = 0;  //主干词情绪
        double pRele = 1;  //连词权重
        double pDegree = 1; //量词权重
        boolean polar = false;//是否去反


        for (int i = 0; i < len; i++) {
            pCategory = psrc[i].getCategory();
            pWeight = psrc[i].getWeight();
            switch (pCategory) {
                case WordConstant.DIC_RELEVANCE_MAPPING_VAL:
                    if (i != 0) {
                        if (pEmotion == 0) {
                            if (Math.abs(pMainWeight) == 1) {
                                pMainWeight = 0;
                            }
                        }
                        resValue[index] = getResultScore(pMainWeight, pEmotion, pDegree, pRele, polar);
                        polar = false;
                        pRele = 1;
                        pMainWeight = 0;
                        pEmotion = 0;
                        pDegree = 1;
                        index++;
                        if (index == 10) {
                            //一个小小的短句十个关联词 跳过
                            break;
                        }
                    }
                    pRele = pWeight;
                    break;
                case WordConstant.DIC_DENY_MAPPING_VAL:
                    polar = true;
                    break;
                case WordConstant.DIC_DEGREE_MAPPING_VAL:
                    pWeight = pWeight / DEGREE_ADJUST;
                    pDegree = pWeight;
                    break;
                case WordConstant.DIC_EMOTION_MAPPING_VAL:
                    if (polar) {
                        pWeight = -pWeight;
                        polar = false;
                    }
                    pEmotion += pWeight;
                    break;
                case WordConstant.DIC_PROPERTY_MAPPING_VAL:
                    if (polar) {
                        pWeight = -pWeight;
                        polar = false;
                    }

                    if (psrc[i].getName().equals(this.mainRootWord)) {
                        pMainWeight = pWeight;
                    }
                    break;
            }
        }

        if (pEmotion == 0) {
            if (Math.abs(pMainWeight) == 1) {
                pMainWeight = 0;
            }
        }
        resValue[index] = getResultScore(pMainWeight, pEmotion, pDegree, pRele, polar);
        for (int k = 0; k <= index; k++) {
            sum += resValue[k];
        }
        return sum;
    }

    /*决策树*/
    private double getResultScore(double pMainWeight, double pEmotion, double pDegree, double pRele, boolean polar) {
        double mainScore;
        // TODO if (pMainWeight == 0) {
        if (pMainWeight == 0 && pEmotion == 0) {
            mainScore = 0;
        } else {
            if (pEmotion == 0) {
                mainScore = pMainWeight;
            } else {
                mainScore = pEmotion;
            }
            mainScore = mainScore * pDegree * pRele;
        }

        if (polar) {
            mainScore = mainScore * (-1);
        }
        return mainScore;
    }

    /**
     * 计算最终的情感值
     *
     * @param pres
     * @param bs
     * @param len
     * @return
     */
    private int _verifyEmotion(double pres[], BitSet bs, int len) {
        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum += pres[i];
        }

        for (int i = 0; i < len; i++) {
            if (bs.get(i)) {
                if ((sum > 0 && pres[i] < 0) || (sum < 0 && pres[i] > 0)) {
                    //句子情感多样化
                    if (pres[i] != 0) {   //可以去掉看看
                        //return (int) pres[i];
                    }
                }
                this.sentencesOfWord += this.commentContents.get(i);
            }
        }
        return sum;
    }

}
