package com.chen.InterviewAce.model.dto.questionBankQuestion;

import com.chen.InterviewAce.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询题库题目关联请求
 *

*
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionBankQuestionRemoveRequest extends PageRequest implements Serializable {


    /**
     * 题库 id
     */
    private Long questionBankId;

    /**
     * 题目 id
     */
    private Long questionId;


    private static final long serialVersionUID = 1L;
}