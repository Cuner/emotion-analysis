package org.cuner.emotion.analysis.dao;

import org.apache.ibatis.annotations.Param;
import org.cuner.emotion.analysis.entity.Word;

import java.util.List;

/**
 * Created by houan on 18/4/20.
 */
public interface WordDao {

    List<Word> listWord(@Param("start") int start, @Param("offset") int offset);
}
