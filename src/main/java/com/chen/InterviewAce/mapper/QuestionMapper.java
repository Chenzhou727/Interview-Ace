package com.chen.InterviewAce.mapper;

import com.chen.InterviewAce.model.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
* @author 11915
* @description 针对表【question(题目)】的数据库操作Mapper
* @createDate 2024-09-27 11:34:20
* @Entity com.chen.InterviewAce.model.entity.Question
*/
public interface QuestionMapper extends BaseMapper<Question> {

    @Select("SELECT * FROM question WHERE updateTime >= #{minUpdateTime}")
    List<Question> listQuestionWithDelete(Date fiveMinutesAgoDate);
}




