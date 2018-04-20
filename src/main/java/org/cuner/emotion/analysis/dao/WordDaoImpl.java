package org.cuner.emotion.analysis.dao;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.cuner.emotion.analysis.entity.Word;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by houan on 18/4/20.
 */
public class WordDaoImpl implements WordDao {
    @Override
    public List<Word> listWord(int start, int offset) {
        String resource = "sql/SqlMapConfig.xml";
        InputStream inputStream;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            return new ArrayList<>();
        }
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession session = factory.openSession();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("start", start);
        paramMap.put("offset", offset);
        List<Word> wordList =  session.selectList("dictionary.listWord", paramMap);
        session.close();
        return wordList;
    }
}
