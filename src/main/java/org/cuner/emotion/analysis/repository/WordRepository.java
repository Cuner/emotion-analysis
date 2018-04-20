package org.cuner.emotion.analysis.repository;

import org.cuner.emotion.analysis.dao.WordDao;
import org.cuner.emotion.analysis.dao.WordDaoImpl;
import org.cuner.emotion.analysis.entity.Word;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by houan on 18/4/20.
 */
public class WordRepository {

    //每次请求偏移量
    public static final int QUERY_OFFSET_PRE = 1000;

    public List<Word> getWordListFormXml() {
        List<Word> wordList = new ArrayList<>();
        try {
            SAXReader saxReader = new SAXReader();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Dictionary.xml");
            Document doc = saxReader.read(inputStream);
            List rowList = doc.selectNodes("/mysqldump/database/table_data/row");
            for (Object o : rowList) {
                Word word = new Word();
                Element element = (Element) o;
                List fieldList = element.elements();
                for (Object e : fieldList) {
                    Element field = (Element) e;
                    String fieldName = field.attribute("name").getValue();
                    switch (fieldName) {
                        case "dicId" :
                            word.setDicId(Integer.parseInt(field.getData().toString()));
                            break;
                        case "name" :
                            word.setName(field.getData().toString());
                            break;
                        case "weight" :
                            word.setWeight(Integer.parseInt(field.getData().toString()));
                            break;
                        case "category" :
                            word.setCategory(Integer.parseInt(field.getData().toString()));
                            break;
                        case "relevance" :
                            word.setRelevance(Integer.parseInt(field.getData().toString()));
                            break;
                        case "extra" :
                            word.setExtra(field.getData().toString());
                            break;
                    }
                }
                wordList.add(word);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return wordList;
    }

    public List<Word> getWordListFromDB() {
        WordDao wordDao = new WordDaoImpl();
        Set<Word> wordList = new HashSet<>();
        int lastQueryOffset = 0;
        while (true) {
            List<Word> partWordList = wordDao.listWord(lastQueryOffset, QUERY_OFFSET_PRE);
            if (CollectionUtils.isEmpty(partWordList)) {
                break;
            }
            wordList.addAll(partWordList);
            lastQueryOffset += WordRepository.QUERY_OFFSET_PRE;
        }

        return new ArrayList<>(wordList);


    }

    public static void main (String [] args) {
        WordRepository wordRepository = new WordRepository();
        wordRepository.getWordListFromDB();
    }

}
