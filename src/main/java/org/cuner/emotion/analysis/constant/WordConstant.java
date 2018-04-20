package org.cuner.emotion.analysis.constant;

/**
 * Created by houan on 18/3/14.
 */
public interface WordConstant {

    // 评价文本分割成子句的符号
    char[] SENTENCE_SPLIT_SYMBOL = {',', ' ', '，', '~', '~', '！', '!', '。', '.', ' ', '?','？','\n','\t',';','；','…','^','～','、','丶','·',' '};

    // 单词的分类: 属性词
    int DIC_PROPERTY_MAPPING_VAL = 1;
    // 品牌, 如大宝,华为,中兴
    int DIC_BRAND_MAPPING_VAL = 2;
    // 量词,  很,非常等等
    int DIC_DEGREE_MAPPING_VAL = 3;
    // 否定,  没有,不行
    int DIC_DENY_MAPPING_VAL = 4;
    // 情感词   实惠, 超好, 值, 不咋样
    int DIC_EMOTION_MAPPING_VAL = 5;
    // 产品词   面霜,面粉
    int DIC_PRODUCTION_MAPPING_VAL = 6;
    // 关联词   而且,但,还是
    int DIC_RELEVANCE_MAPPING_VAL = 7;
}