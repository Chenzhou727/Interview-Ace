package com.chen.InterviewAce.model.dto.questionBankQuestion;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * 批量向题库添加题目
 */
@Data
public class QuestionBankQuestionBatchAddRequest implements Serializable {

    /**
     * 题库 id
     */
    private Long questionBankId;

    /**
     * 题目 id 列表
     */
    private List<Long> questionIdList;

    private static final long serialVersionUID = 1L;
}
