package com.voxlearning.utopia.agent.mockexam.domain.model;

import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperEntity;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPaperEnums;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPaperDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考试计划领域模型
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Data
public class ExamPaper implements Serializable {
    protected Long id;
    private String paperId;                      // 试卷ID
    private String paperName;                    // 试卷名称
    private String source;                       // 试卷来源
    private String type;                         // 类型
    private List<Integer> regionCodes;           // 所属区域编码列表
    private String region;                       // 所属区域名称，中间用，分隔
    private String bookId;                // 教材ID
    private String bookName;              // 教材名称
    private String status;                       // 状态
    private ExamPlanEnums.Subject subject;
    private Integer topicNum;                          // 题数
    private Integer totalScore;                  // 总分
    private Integer examTimes;                   // 考试时间
    private Date createDatetime;                 // 创建时间
    private Date updateDatetime;                 // 最后更新时间
    private Integer planTimes;
    private String isPublic;                    // 是否公开
    private String creator;
    private String planForm;                    //测评形式
    private List<ExamPaperEnums.PartType> partTypes;   //模块类型
    private List<String> bookIds;               //所属教材id列表


    @AllArgsConstructor
    public enum Status {
        OFFLINE("已下线"),
        ONLINE("已上线");
        public final String desc;
    }

    public static class Builder {
        public static ExamPaperEntity build(ExamPaperProcessState model) {
            ExamPaperEntity entity = new ExamPaperEntity();
            BeanUtils.copyProperties(model, entity);
            return entity;
        }

        public static ExamPaperDto build(ExamPaper model) {
            ExamPaperDto dto = new ExamPaperDto();
            BeanUtils.copyProperties(model, dto);
            return dto;
        }

        public static List<ExamPaperDto> build(List<ExamPaper> models) {
            return models.stream().map(Builder::build).collect(Collectors.toList());
        }
    }
}
