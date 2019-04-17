package com.voxlearning.utopia.agent.mockexam.service.dto.output;

import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPaperEnums;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 试卷传输模型
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Data
public class ExamPaperDto implements Serializable {
    protected Long id;
    private String paperId;                      // 试卷ID
    private String paperName;                    // 试卷名称
    private String source;                       // 试卷来源
    private String type;                         // 类型
    private List<Integer> regionCodes;           // 所属区域编码列表
    private String region;                       // 所属区域
    private String bookId;                       // 教材ID
    private String bookName;                     // 教材名称
    private String status;                       // 状态
    private ExamPlanEnums.Subject subject;
    private Integer topicNum;                    // 题数
    private Integer totalScore;                  // 总分
    private Integer examTimes;                   // 考试时间
    private Date createDatetime;                 // 创建时间
    private Date updateDatetime;                 // 最后更新时间
    private String isPublic;                     // 是否公开
    private String disable;                      // 逻辑删除
    private Integer planTimes;                   // 试卷测评次数
    private String creator;
    private String planForm;                    //测评形式
    private List<ExamPaperEnums.PartType> partTypes;   //模块类型
    private List<String> bookIds;               //所属教材id列表

    public static class Builder {
    }
}
