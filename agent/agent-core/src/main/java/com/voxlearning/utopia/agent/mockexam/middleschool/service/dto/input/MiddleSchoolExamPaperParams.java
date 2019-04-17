package com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.input;

import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2019-03-18 15:16
 **/
@Data
public class MiddleSchoolExamPaperParams implements Serializable {
    private String paperId;
    private Integer regionId;
    private String examName;
    private String bookId;
    private String paperTag;
    private String usageMonth;
}
