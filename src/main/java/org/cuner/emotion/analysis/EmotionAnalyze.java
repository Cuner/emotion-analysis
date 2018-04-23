package org.cuner.emotion.analysis;

import org.ansj.domain.Term;
import org.cuner.emotion.analysis.constant.WordConstant;
import org.cuner.emotion.analysis.dictionary.Dictionary;
import org.cuner.emotion.analysis.entity.Emotion;
import org.cuner.emotion.analysis.entity.Word;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yihui on 16/7/6.
 */
public class EmotionAnalyze {

    //主词库
    private Map<String, Word> mainDictionary;

    //属性词
    private String mainProperty;

    //根属性词
    private String mainRootProperty;

    //对应的句子
    private ArrayList<String> commentContents;

    //匹配的句子
    private String sentencesOfProperty;

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

        mainProperty = property;
        mainRootProperty = Dictionary.getInstance().getRootProperty(property);
        commentContents = sentences;

        int emotionVal = analyzeEmotion();
        return new Emotion(emotionVal, property, mainRootProperty, sentencesOfProperty);
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
        Iterator<String> iteratorOfComment = commentContents.iterator();
        while (iteratorOfComment.hasNext()) {
            String sentence = iteratorOfComment.next();
            sentencesOfProperty += sentence;

            //是否疑问句，疑问句的情感直接中性，因为它既不属于正负二者
            if (sentence.endsWith("?") || sentence.endsWith("？")) {
                sentencesScores[sentenceIndex] = 0;
                sentenceIndex++;
                continue;
            }

            ArrayList<String> words = segmentationAnalyze(sentence);
            Iterator<String> iteratorOfWord = words.iterator();
            int segmentLength = words.size();
            int index = 0;
            Word propertyWords[] = new Word[segmentLength];
            boolean hasProperty = false;//zilin
            while (iteratorOfWord.hasNext()) {
                String word = iteratorOfWord.next();
                propertyWords[index] = mainDictionary.get(word);
                if (word.equals(mainProperty)) { // 若word正好为属性词, 则设置属性词标记 mainPropertyFlag
                    mainPropertyFlag.set(sentenceIndex);
                    if (!mainProperty.equals(mainRootProperty)) {
                        propertyWords[index] = new Word(propertyWords[index].getDicId(), propertyWords[index].getName(),
                                propertyWords[index].getWeight(), propertyWords[index].getCategory(),
                                propertyWords[index].getRelevance(), propertyWords[index].getExtra());
                        propertyWords[index].setName(mainRootProperty);
                    }
                    hasProperty = true;
                } else if (WordConstant.DIC_PROPERTY_MAPPING_VAL == propertyWords[index].getCategory()) {
                    String rootProperty = Dictionary.getInstance().getRootProperty(word);
                    if (rootProperty.isEmpty()) {
                        rootProperty = word;
                    }
                    if (rootProperty.equals(mainRootProperty)) {
                        mainPropertyFlag.set(sentenceIndex);
                        propertyWords[index] = new Word(propertyWords[index].getDicId(), propertyWords[index].getName(),
                                propertyWords[index].getWeight(), propertyWords[index].getCategory(),
                                propertyWords[index].getRelevance(), propertyWords[index].getExtra());
                        propertyWords[index].setName(mainRootProperty);
                        hasProperty = true;
                    }
                }

                index++;
            }
            if (!hasProperty) // 表示没有属性词时, 在走一下情感词的逻辑
            {
                index = 0;
                Iterator<String> iteratorOfWord2 = words.iterator();
                while (iteratorOfWord2.hasNext()) {
                    String word = iteratorOfWord2.next();
                    //获取情感词的属性词根
                    String rootProperty = Dictionary.getInstance().getEmotionRootProperty(word);
                    if (rootProperty.equals(mainProperty)) {
                        mainPropertyFlag.set(sentenceIndex);
                        propertyWords[index] = new Word(propertyWords[index].getDicId(), propertyWords[index].getName(),
                                propertyWords[index].getWeight(), propertyWords[index].getCategory(),
                                propertyWords[index].getRelevance(), propertyWords[index].getExtra());
                        propertyWords[index].setName(rootProperty);
                        propertyWords[index].setCategory(WordConstant.DIC_PROPERTY_MAPPING_VAL);
                    }
                    index++;
                }
            }
            sentencesScores[sentenceIndex] = _countEmotion(propertyWords, segmentLength);
            sentenceIndex++;
        }
        return _verifyEmotion(sentencesScores, mainPropertyFlag, sentenceIndex);
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
                    this.sentencesOfProperty = this.commentContents.get(i);
                    if (pres[i] != 0) {   //可以去掉看看
                        return (int) pres[i];
                    }
                }
                this.sentencesOfProperty = this.commentContents.get(i);
            }
        }
        return sum;
    }

    private double _countEmotion(Word[] psrc, int len) {
        if (0 == len || null == psrc) {
            return 0;
        }
        double sum = 0;
        double resValue[] = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int index = 0;

        int pCategory = 0;
        String extra;
        double pWeight = 0;

        double pEmotion = 0;   //情感词权重
        int pfaceValue = 0;     //表情词权重
        double pMainWeight = 0;  //主干词情绪
        double pRele = 1;  //连词权重
        double pDegree = 1;
        boolean polar = false;
        String polarFlag = "";


        for (int i = 0; i < len; i++) {
            pCategory = psrc[i].getCategory();
            pWeight = psrc[i].getWeight();
            extra = psrc[i].getExtra();
            //System.out.println(psrc[i].getValue());
            switch (pCategory) {
                case WordConstant.DIC_RELEVANCE_MAPPING_VAL:
                    if (i != 0) {
                        if (pMainWeight == 0) {
                            resValue[index] = 0;
                        } else {
                            if (pEmotion == 0) {
                                if (Math.abs(pMainWeight) == 1)
                                    pMainWeight = 0;
                                resValue[index] = pMainWeight;

                            } else {
                                resValue[index] = pEmotion;
                            }
                            resValue[index] = resValue[index] * pDegree * pRele;

                        }
                        if (polar && !polarFlag.equals("prefix")) {
                            resValue[index] = -resValue[index];
                        }
                        polar = false;
                        polarFlag = "";
                        pRele = 1;
                        pMainWeight = 0;
                        pEmotion = 0;
                        pDegree = 1;
                        index++;
                        if (index == 10) {
                            //System.out.println("一个小小的短句十个关联词，你坑爹啊");
                            break;
                        }
                    }
                    pRele = pWeight;
                    break;
                case WordConstant.DIC_DENY_MAPPING_VAL:
                    polar = true;
                    polarFlag = extra;
                    break;
                case WordConstant.DIC_DEGREE_MAPPING_VAL:
                    pWeight = pWeight / DEGREE_ADJUST;
                    pDegree = pWeight;
                    break;
                case WordConstant.DIC_EMOTION_MAPPING_VAL:
                    if (extra.equals("表情符")) {
                        pfaceValue += pWeight;
                    } else {
                        if (polar) {
                            pWeight = -pWeight;
                            polar = false;
                        }
                        pEmotion += pWeight;
                    }
                    break;
                case WordConstant.DIC_PROPERTY_MAPPING_VAL:
                    if (polar) {
                        pWeight = -pWeight;
                        polar = false;
                    }

                    if (psrc[i].getName().equals(this.mainRootProperty)) {
                        pMainWeight = pWeight;
                    }
                    break;
            }
        }

		/*决策树*/
        if (pMainWeight == 0) {
            resValue[index] = 0;
        } else {
            if (pEmotion == 0) {
                if (Math.abs(pMainWeight) == 1)
                    pMainWeight = 0;
                resValue[index] = pMainWeight;
            } else {
                resValue[index] = pEmotion;
            }
            resValue[index] = resValue[index] * pDegree * pRele;
        }
        if (polar) //&& polarFlag.equals("suffix"))
        {
            resValue[index] = -resValue[index];
        }
        /*
		 * 其实可以给出更精确的位置信息 还有更精确的判断
		 * 表情符的权重   认为还是比较大
		 */
        for (int k = 0; k <= index; k++) {
            sum += resValue[k];
        }
        if (pfaceValue != 0) {
            if ((pfaceValue > 0 && sum < 0) || (pfaceValue < 0 && sum > 0)) {
                //System.out.println("情绪不符合!!!");
                return pfaceValue;
            }
        }
        return sum;
    }

    /**
     * 片段分析
     *
     * @param pStentive
     * @return
     */
    private ArrayList<String> segmentationAnalyze(String pStentive) {
        // 下面的正则表示,将[]中的语句作为一个单词
        ArrayList<String> SingleResult = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\\[([^\\]\\[]*)\\]");
        Matcher matcher = pattern.matcher(pStentive);
        while (matcher.find()) {
            String matWord = matcher.group();
            if (mainDictionary.containsKey(matWord)) {
                SingleResult.add(matWord);
                pStentive = pStentive.replace(matWord, "");
            }
        } // end while

        List<Term> parse = AnalysisTool.splitSentence2Word(pStentive);
        for (Term term : parse) {
            String word = term.getName();
            if (mainDictionary.containsKey(word)) {
                SingleResult.add(word);
            }
        }

        /*StringReader textReader = new StringReader(pStentive);
        IKSegmenter ikSegmenter = new IKSegmenter(textReader, true);
        Lexeme lexeme = null;
        try {
            while ((lexeme = ikSegmenter.next()) != null) {
                String currentWord = lexeme.getLexemeText();
                if (mainDictionary.containsKey(currentWord)) {
                    SingleResult.add(currentWord);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        return SingleResult;
    }


}
