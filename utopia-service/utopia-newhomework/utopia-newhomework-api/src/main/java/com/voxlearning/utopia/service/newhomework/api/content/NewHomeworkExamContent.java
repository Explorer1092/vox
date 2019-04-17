package com.voxlearning.utopia.service.newhomework.api.content;

import com.voxlearning.utopia.service.question.api.entity.EmbedBook;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 同步习题教学目标配置内容
 * 数学、英语配置知识点和和题包
 * 语文配置题id
 *
 * @author guoqiang.li
 * @since 2016/7/21
 */
@Getter
@Setter
public class NewHomeworkExamContent implements Serializable {
    private static final long serialVersionUID = 1422957626159223118L;
    private List<ExamQuestion> questions;                   // 配置的题
    private List<ExamQuestionBox> questionBoxes;            // 配置的题包
    private List<ExamKnowledgePoint> knowledgePoints;       // 配置的知识点

    @Getter
    @Setter
    public static class ExamQuestion implements Serializable {
        private static final long serialVersionUID = 5924816179720030016L;
        private List<String> questionDocIds;                // 题docIds
        private EmbedBook embedBook;                        // 教学目标所属教材
    }

    @Getter
    @Setter
    public static class ExamQuestionBox implements Serializable{
        private static final long serialVersionUID = -1677637960935803169L;
        private String id;                                  // 题包id
        private String name;                                // 题包名字
        private Integer difficulty;                         // 题包难度
        private ObjectiveConfig.UsageType usageType;        // 题包类型
        private List<String> questionDocIds;                // 题docIds
        private EmbedBook embedBook;                        // 教学目标所属教材

        List<NewQuestion> newQuestions;                     // 最后需要推出的题
    }

    @Getter
    @Setter
    public static class ExamKnowledgePoint implements Serializable {
        private static final long serialVersionUID = -4849616474694296695L;
        private String kpId;                                // 知识点id
        private Integer contentTypeId;                      // 题型
        private EmbedBook embedBook;                        // 教学目标所属教材
    }
}
