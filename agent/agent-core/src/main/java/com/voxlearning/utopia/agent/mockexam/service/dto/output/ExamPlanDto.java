package com.voxlearning.utopia.agent.mockexam.service.dto.output;

import com.voxlearning.utopia.agent.mockexam.service.dto.OperateRequest;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.BooleanEnum;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 考试计划数据传输模型
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExamPlanDto extends OperateRequest implements Serializable {
    private Long id;                               // ID
    private String name;                           // 名称
    private Subject subject;                        // 学科
    private List<Type> type;                           // 类型
    private Grade grade;                          // 年级
    private Form form;                           // 形式
    private Date teacherQueryTime;                 // 教师查看试卷时间
    private BooleanEnum allowTeacherQuery;             // 是否允许教师查看成绩
    private BooleanEnum allowTeacherModify;             // 是否允许教师修改成绩
    private Date teacherMarkDeadline;              // 教师判分截止时间
    private BooleanEnum allowStudentQuery;              // 是否允许学生查看成绩
    private Date scorePublishTime;               // 成绩发布时间
    private Integer finishExamTime;                // 允许交卷时间
    private Integer examTotalTime;                    // 答卷时长
//    private Integer totalScore;                       // 试卷总分
    private SpokenScoreType spokenScoreType;                // 口语算分类型
    private SpokenAnswerTimes spokenAnswerTimes;             // 口语可答题次数
    private ScoreRuleType scoreRuleType;                      // 成绩类型
    private List<Rule> scoreRule;                      // 等级制内容
    private String bookId;                  // 教材ID
    private String bookName;                // 教材名称
    private PaperType paperType;                      // 试卷类型
    private String paperId;                        // 试卷ID
    private String paperDocUrls;                   // 试卷文档地址，多个逗号分隔
    private String paperDocNames;                  // 试卷名称，多个逗号分隔
    /**
     * 考试id
     */
    private String examId;
    private DistributeType distributeType;                 // 获取试卷方式
    private Scene scene;                          // 场景
    private RegionLevel regionLevel;                    // 区域级别,0:省级,1:市级,2:区级,3:校级
    private String regionCodes;                    // 所属区域ID
    private String regionNames;                    // 所属区域
    private String schoolIds;                        // 学校ID
    private String schoolNames;                    // 学校名称
    private Date startTime;                        // 开始时间
    private Date endTime;                          // 结束时间
    private Status status;                         // 状态
    private Date createDatetime;                   // 创建时间
    private Date updateDatetime;                   // 更新时间
    private Long creatorId;                        // 申请人ID
    private String creatorName;                    // 申请人名称
    private List<OperationLog> logs;        // 审核意见

    private String attachment;                         // 上传的附件json, [{fileName:"", fileUrl:""},{fileName:"", fileUrl:""}]
    private String comment;                          // 备注内容
    private Pattern pattern;                             //测评模式
    private Date registrationDeadlineTime;              //报名截止时间


    /**
     * 规则
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Rule implements Serializable {

        /**
         * 排行名称
         */
        private String rankName;

        /**
         * 分数下限
         */
        private Float bottom;

        /**
         * 分数上线
         */
        private Float top;
    }

    /**
     * 审核日志
     */
    @Data
    public static class OperationLog implements Serializable {
        private Date date;
        private String operatorName;
        private String desc;
    }
}
