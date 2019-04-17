package com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description: 试卷类型
 * @author: kaibo.he
 * @create: 2019-03-18 16:39
 **/
public interface MiddleSchoolExamPaperEnum {

    @AllArgsConstructor
    enum PaperType {
        LISTENING(0, "听力套卷"),
        ORAL(1, "口语套卷"),
        NORMAL(2, "笔试套卷"),
        LISTENING_ORAL(3, "听说套卷"),
        LISTENING_NORMAL(4, "听力笔试套卷"),
        ORAL_NORMAL(5, "口语笔试套卷"),
        LISTENING_ORAL_NORMAL(6, "口语听力笔试套卷"),
        ;
        @Getter
        private Integer id;
        public final String desc;
    }
}
