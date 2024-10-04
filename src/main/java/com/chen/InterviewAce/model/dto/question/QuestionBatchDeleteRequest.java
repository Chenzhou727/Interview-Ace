package com.chen.InterviewAce.model.dto.question;

import lombok.Data;

import java.util.List;

@Data
public class QuestionBatchDeleteRequest {

    /*
    题目id列表
     */
    private List<Long> questionIdList;

    private static final long serialVersionUID = 1L;
}
