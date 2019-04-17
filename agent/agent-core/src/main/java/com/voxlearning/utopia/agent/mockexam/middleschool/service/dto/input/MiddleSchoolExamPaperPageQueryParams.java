package com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.input;

import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2019-03-18 15:16
 **/
@Data
public class MiddleSchoolExamPaperPageQueryParams extends MiddleSchoolExamPaperParams implements Serializable{
    private Integer page;
    private Integer size;
}
