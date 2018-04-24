package org.cuner.emotion.analysis.dictionary;

import org.cuner.emotion.analysis.constant.WordConstant;
import org.cuner.emotion.analysis.entity.Word;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by houan on 18/3/14.
 */
public class Dictionary {

    //所有词库
    public Map<String, Word> wordBank = new HashMap<>();

    //词根（同义词指向的对象）
    public Map<Integer, Word> rootWords = new HashMap<>();

    //属性词词库，Key为属性词本身
    public Map<String, Word> propertyWords = new HashMap<>();

    //所有近义词，不包含词根
    public Map<String, Word> propertySynonymWords = new HashMap<>();

    //情感词词库，Key为情感词本身。
    public Map<String, Word> emotionWords = new HashMap<>();

    //情感词所对应的属性词词根
    public Map<String, Word> emotionSynonymWords = new HashMap<>();

    //新词语（会定期刷到本地文件中，然后拉下来给小编check）
    public Map<String, Integer> newWords = new HashMap<>();

    //是否初始化
    protected boolean isInitialize = false;

    //Singleton
    static class SingletonHandler {
        public static final Dictionary INSTANCE = new Dictionary();
    }

    public static Dictionary getInstance() {
        return SingletonHandler.INSTANCE;
    }

    /**
     * 初始化字典, 从db中读取词根, 常驻内存
     */
    public void initDistionary() {
        //同义词处理
        Collection<Word> propertyWordSet = propertyWords.values();
        for (Word word : propertyWordSet) {
            if (word.getRelevance() > 0 && rootWords.containsKey(word.getRelevance())) {
                // 将同义词和其词根, 塞入属性词同义词库中
                propertySynonymWords.put(word.getName(), rootWords.get(word.getRelevance()));
            }
        }

        //情感词处理
        Collection<Word> emotionWordSet = emotionWords.values();
        for (Word word : emotionWordSet) {
            if (word.getRelevance() > 0 && rootWords.containsKey(word.getRelevance())) {
                // 将情感词的词根塞入情感词同义词库中
                emotionSynonymWords.put(word.getName(), rootWords.get(word.getRelevance()));
            }
        }

        isInitialize = true;
    }


    /**
     * xxx 说明 本方法必须在 initDistionary方法之前调用,否则会出错
     *
     * @param words
     * @return
     */
    public boolean loadDictionary(List<Word> words) {
        if (CollectionUtils.isEmpty(words)) {
            // db的记录已经全部读完
            return false;
        }

        List<String> extendsIkWords = new ArrayList<>();
        for (Word word : words) {
            // 加入所有的词库中
            wordBank.put(word.getName(), word);

            // 判断该词是属性词还是情感词
            if (word.getCategory() == WordConstant.DIC_PROPERTY_MAPPING_VAL) {
                propertyWords.put(word.getName(), word);
            } else if (word.getCategory() == WordConstant.DIC_EMOTION_MAPPING_VAL) {
                emotionWords.put(word.getName(), word);
            } else {
                // 添加到扩展词库中
                extendsIkWords.add(word.getName());
                continue;
            }

            // 如果是词根,则塞入词根库中 --> 词根库中,包含了属性词和情感词的词根
            // 扩展词的词根就不考虑了
            if (word.getRelevance() == 0) {
                rootWords.put(word.getDicId(), word);
            }
        }

        this.extendIkWords(extendsIkWords);

        return true;
    }


    /**
     * 扩展IK词库
     *
     * @param words
     */
    protected void extendIkWords(List<String> words) {

    }

    /**
     * 获取属性词的词根
     *
     * @param property 属性词
     * @return 存在, 则返回词根; 不存在,则返回入参
     */
    public String getRootProperty(String property) {
        Word rootWord = propertySynonymWords.get(property);
        if (rootWord == null || StringUtils.isEmpty(rootWord.getName())) {
            return property;
        } else {
            return rootWord.getName();
        }
    }

    /**
     * 获取情感词的属性词根
     *
     * @param emotion 情感词
     * @return 存在, 则返回词根; 不存在,则返回空字符串
     */
    public String getRootEmotion(String emotion) {
        Word rootWord = emotionSynonymWords.get(emotion);
        if (rootWord == null || StringUtils.isEmpty(rootWord.getName())) {
            return emotion;
        } else {
            return rootWord.getName();
        }
    }

    /**
     * 获取所有词库
     *
     * @return
     */
    public Map<String, Word> getWordBank() {
        return wordBank;
    }

    /**
     * 属性词库中是否包含指定的属性词
     *
     * @param property 属性词
     * @return
     */
    public boolean containsKeyInCommonProperties(String property) {
        return propertyWords.containsKey(property);
    }

    /**
     * 情感词库中是否包含指定的情感词
     *
     * @param emotion
     * @return
     */
    public boolean containsKeyInCommonEmotions(String emotion) {
        return emotionWords.containsKey(emotion);
    }


    /**
     * 统计新词
     */
    public void statNewWord(String word) {
        Integer wordFrequency = newWords.get(word);
        newWords.put(word, (wordFrequency == null ? 1 : (wordFrequency + 1)));
    }

}
