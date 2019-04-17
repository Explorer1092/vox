package com.voxlearning.utopia.agent.mockexam.service.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 试卷流程状态通知
 *
 * @author xiaolei.li
 * @version 2018/8/6
 */
@Data
public class ExamPaperProcessStateNotify implements Serializable {

    /**
     * 考试计划id
     */
    private String businessId;

    /**
     * 试卷流程id
     */
    private String processId;

    /**
     * 流程状态
     */
    private Status status;

    /**
     * 当前处理人姓名
     */
    private String operatorName;

    /**
     * 驳回理由
     */
    private String rejectReason;

    /**
     * 试卷列表
     */
    private List<Paper> papers;

    /**
     * 流程状态
     */
    @AllArgsConstructor
    public enum Status {
        CHECKING("待审核"),
        REJECT("驳回"),
        PROCESSING("录入中"),
        DONE("完成"),;
        public final String desc;
    }


    /**
     * 试卷
     */
    @Data
    public static class Paper implements Serializable {

        /**
         * 试卷id
         */
        private String paperId;

        /**
         * 试卷名称
         */
        private String title;

        /**
         * 试卷文档url
         */
        private String docUrl;
    }
}
