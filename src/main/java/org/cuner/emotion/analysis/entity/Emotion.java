package org.cuner.emotion.analysis.entity;

/**
 * Created by houan on 18/3/14.
 */
public class Emotion {
    /**
     * 情感
     */
    private int emotion;

    /**
     * 匹配的词
     */
    private String property;

    /**
     * 词根
     */
    private String rootProperty;

    /**
     * 匹配的子句
     */
    private String sentence;

    public Emotion() {
    }

    public Emotion(int emotion, String property, String rootProperty, String sentence)
    {
        this.emotion = emotion;
        this.property = property;
        this.rootProperty = rootProperty;
        this.sentence = sentence;
    }

    public int getEmotion() {
        return emotion;
    }

    public void setEmotion(int emotion) {
        this.emotion = emotion;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getRootProperty() {
        return rootProperty;
    }

    public void setRootProperty(String rootProperty) {
        this.rootProperty = rootProperty;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    @Override
    public String toString() {
        return "Emotion{" +
                "emotion=" + emotion +
                ", property='" + property + '\'' +
                ", rootProperty='" + rootProperty + '\'' +
                ", sentence='" + sentence + '\'' +
                '}';
    }
}
