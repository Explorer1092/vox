package com.voxlearning.utopia.agent.mockexam.service.dto.input;

import lombok.Data;

import java.io.Serializable;

/**
 * 试卷查询条件
 *
 * @author xiaolei.li
 * @version 2018/8/7
 */
@Data
public class ExamPaperQueryParams implements Serializable {

    /**
     * 试卷ID
     */
    private String paperId;
    /**
     * 试卷名称
     */
    private String paperName;
    /**
     * 所属学科
     */
    private String subject;
    /**
     * 试卷来源
     */
    private String source;
    /**
     * 教材ID
     */
    private String bookId;

    /**
     * 教材名称
     */
    private String bookName;
    /**
     * 试卷状态
     */
    private String status;
    /**
     * 所属区域code
     */
    private String regionCode;
    /**
     * 所属模块类型
     */
    private String partType;

    /**
     * 使用月份
     */
    private String usageMonth;
}
