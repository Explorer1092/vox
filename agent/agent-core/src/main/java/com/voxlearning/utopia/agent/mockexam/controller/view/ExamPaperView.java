package com.voxlearning.utopia.agent.mockexam.controller.view;

import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPaperDto;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 考卷视图模型
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Data
public class ExamPaperView implements Serializable {

    protected Long id;
    private String paperId;                      // 试卷ID
    private String paperName;                    // 试卷名称
    private String source;                       // 试卷来源
    private String type;                         // 类型
    private String region;                       // 所属区域
    private String bookId;                // 教材ID
    private String bookName;              // 教材名称
    private String status;                       // 状态
    private String subject;                      // 学科
    private String _subject;                      // 学科
    private String topicNum;                     // 题数
    private Integer totalScore;                  // 总分
    private Integer examTimes;                   // 考试时间
    private Date createDatetime;                 // 创建时间
    private Date updateDatetime;                 // 最后更新时间
    private String isPublic;                     // 是否公开
    private String disable;                      // 逻辑删除

    public static class Builder {
        public static ExamPaperView build(ExamPaperDto dto){
            ExamPaperView view = new ExamPaperView();
            BeanUtils.copyProperties(dto,view);
            view._subject = dto.getSubject().desc;
            view.subject = dto.getSubject().name();
            return view;
        }
    }
}
