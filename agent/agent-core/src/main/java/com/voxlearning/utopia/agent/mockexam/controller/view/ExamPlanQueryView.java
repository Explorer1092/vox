package com.voxlearning.utopia.agent.mockexam.controller.view;

import com.alibaba.fastjson.JSONArray;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPlanDto;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 分页查询视图模型
 *
 * @author xiaolei.li
 * @version 2018/8/21
 */
@Data
public class ExamPlanQueryView implements Serializable {
    private Long id;
    private String name;
    private String[] paperIds;
    private String creatorName;
    private String _regionLeve;
    private String _form;
    private String _grade;
    private String _subject;
    private Date createDatetime;
    private Date startTime;
    private Date endTime;
    private Date scorePublishTime;               // 成绩发布时间
    private String status;
    private String _status;
    private String examId;
    private String regionLevel;
    private List<ExamPlanView.Builder.AttachmentView> attachmentFiles;     // 附件信息
    private String paperType;               //试卷类型

    /**
     * 当前状态可用动作
     */
    private List<Actions> actions;

    /**
     * 动作定义，用于封装测评计划的状态与可用动作之间的迁移
     *
     * @author xiaolei.li
     * @version 2018/8/31
     */
    @AllArgsConstructor
    public enum Actions {
        PLAN_DETAIL("查看"),
        PLAN_MODIFY("编辑"),
        PLAN_SUBMIT("提交"),
        PLAN_WITHDRAW("撤回"),
        PLAN_COPY("复制"),
        PLAN_AUDIT("审核"),
        PLAN_ONLINE("上线"),
        PLAN_OFFLINE("下线"),
        EXAM_SCORE("成绩查询"),
        EXAM_REPLENISH("重考"),
        EXAM_MAKEUP("补考"),
        EXAM_SCORE_DONWLOAD("成绩单下载"),
        EXAM_REPORT_DONWLOAD("测评报告"),
        PLAN_DOWNLOAD_ATTACHMENT("附件下载"),
        ;
        public final String desc;
    }

    public static class Builder {
        public static ExamPlanQueryView build(ExamPlanDto dto) {
            ExamPlanQueryView view = new ExamPlanQueryView();
            view.setId(dto.getId());
            view.setName(dto.getName());
            if (StringUtils.isNotBlank(dto.getPaperId()))
                view.setPaperIds(new String[]{dto.getPaperId()});
            view.setCreatorName(dto.getCreatorName());
            view.set_regionLeve(dto.getRegionLevel().desc);
            view.set_form(dto.getForm().desc);
            view.set_grade(dto.getGrade().desc);
            view.set_subject(dto.getSubject().desc);
            view.setCreateDatetime(dto.getCreateDatetime());
            view.setStartTime(dto.getStartTime());
            view.setEndTime(dto.getEndTime());
            view.setScorePublishTime(dto.getScorePublishTime());
            view.setStatus(dto.getStatus().name());
            view.set_status(dto.getStatus().desc);
            view.setExamId(dto.getExamId());
            view.setRegionLevel(dto.getRegionLevel().regionType == RegionType.UNKNOWN
                    ? "SCHOOL" : dto.getRegionLevel().regionType.name());
            if(StringUtils.isNotBlank(dto.getAttachment())){
                List<ExamPlanView.Builder.AttachmentView> attachmentViews = JSONArray.parseArray(dto.getAttachment(), ExamPlanView.Builder.AttachmentView.class);
                view.setAttachmentFiles(attachmentViews);
            }
            if (Objects.nonNull(dto.getPaperType())) {
                view.setPaperType(dto.getPaperType().name());
            }
            return view;
        }
    }
}
