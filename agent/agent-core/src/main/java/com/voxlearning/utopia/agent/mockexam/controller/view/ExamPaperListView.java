package com.voxlearning.utopia.agent.mockexam.controller.view;

import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPaperDto;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 试卷列表视图模型
 *
 * @Author: peng.zhang
 * @Date: 2018/8/22
 */
@Data
public class ExamPaperListView implements Serializable {

    private String paperId;                      // 试卷ID
    private String paperName;                    // 试卷名称
    private String source;                       // 试卷来源
    private String type;                         // 类型
    private String region;                       // 所属区域
    private String _subject;                      // 学科
    private String subject;
    private Integer topicNum;                     // 题数
    private Integer totalScore;                  // 总分
    private String status;                       // 状态
    private Date createDatetime;                 // 创建时间
    private Integer planTimes;                   // 测评次数
    private String isPublic;                     // 是否公开
    private String creator;                  // 录入人
    private String bookName;                    // 教材名
    private String paperPreviewUrl;               // 试卷预览地址
    private String planForm;                    //测评形式
    private String partTypes;                    //所属模块类型
    private List<String> bookIds;               //所属教材id列表

    public static class Builder{

        public static ExamPaperListView build(ExamPaperDto dto){
            ExamPaperListView view = new ExamPaperListView();
            BeanUtils.copyProperties(dto,view);
            view.setSubject(dto.getSubject().name());
            view.set_subject(dto.getSubject().desc);

            List<String> partTypes = Optional.ofNullable(dto.getPartTypes()).orElse(new ArrayList<>())
                    .stream()
                    .map(partType -> partType.desc)
                    .collect(Collectors.toList());
            view.setPartTypes(String.join(",", partTypes));
            if (StringUtils.isBlank(view.getPlanForm())) {
                view.setPlanForm("暂无");
            }
            return view;
        }
    }
}
