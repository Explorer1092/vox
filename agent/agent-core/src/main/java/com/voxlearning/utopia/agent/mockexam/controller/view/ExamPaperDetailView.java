package com.voxlearning.utopia.agent.mockexam.controller.view;

import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPaperDto;
import org.springframework.beans.BeanUtils;

import java.util.Date;

/**
 * 试卷详情
 *
 * @Author: peng.zhang
 * @Date: 2018/8/24
 */
public class ExamPaperDetailView {

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
    private String topicNum;                     // 题数
    private Integer totalScore;                  // 总分
    private Integer examTimes;                   // 考试时间
    private Date createDatetime;                 // 创建时间
    private Date updateDatetime;                 // 最后更新时间
    private String isPublic;                     // 是否公开
    private String disable;                      // 逻辑删除

    public static class Builder {

        public static ExamPaperDetailView build(ExamPaperDto dto){
            ExamPaperDetailView view = new ExamPaperDetailView();
            BeanUtils.copyProperties(dto,view);
            return view;
        }
    }
}
