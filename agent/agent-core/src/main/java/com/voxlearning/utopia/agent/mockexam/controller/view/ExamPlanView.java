package com.voxlearning.utopia.agent.mockexam.controller.view;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.mockexam.integration.StringUtil;
import com.voxlearning.utopia.agent.mockexam.service.dto.OperateRequest;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.BooleanEnum;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums.*;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPlanDto;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPlanDto.OperationLog;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试计划视图模型
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExamPlanView extends OperateRequest implements Serializable {
    private Long id;                           // ID
    private String name;                           // 名称
    private String subject;                        // 学科
    private String type;                           // 类型
    private String grade;                          // 年级
    private String form;                           // 形式
    private Date teacherQueryTime;                 // 教师查看试卷时间
    private String allowTeacherQuery;             // 是否允许教师查看成绩
    private String allowTeacherModify;             // 是否允许教师修改成绩
    private Date teacherMarkDeadline;              // 教师判分截止时间
    private String allowStudentQuery;              // 是否允许学生查看成绩
    private Date scorePublishTime;               // 成绩发布时间
    private Integer finishExamTime;                // 允许交卷时间
    private Integer examTotalTime;                    // 答卷时长
//    private Integer totalScore;                       // 试卷总分
    private String spokenScoreType;                // 口语算分类型
    private String spokenAnswerTimes;             // 口语可答题次数
    private String bookId;                           // 教材ID
    private String bookName;                         // 教材名称
    private String paperType;                      // 试卷类型
    private String paperId;                        // 试卷ID
    private String paperDocUrls;                   // 试卷文档地址，多个逗号分隔
    private String paperDocNames;                  // 试卷名称，多个逗号分隔
    /**
     * 考试id
     */
    private String examId;
    private String distributeType;                 // 获取试卷方式
    private String scene;                          // 场景
    private String regionLevel;                    // 区域级别,0:省级,1:市级,2:区级,3:校级
    private String regionCodes;                    // 所属区域ID
    private String regionNames;                    // 所属区域
    private String schoolIds;                        // 学校ID
    private String schoolNames;                    // 学校名称
    private Date startTime;                        // 开始时间
    private Date endTime;                          // 结束时间
    private Date createDatetime;                       // 创建时间
    private Date updateDatetime;                       // 更新时间
    private Long creatorId;                        // 申请人ID
    private String creatorName;                    // 申请人名称
    private String status;                         // 状态
    private String scoreRuleType;                      // 成绩类型
    private List<ExamPlanDto.Rule> scoreRule;                      // 等级制内容
    private List<OperationLog> logs;                // 审核意见

    private List<Builder.AttachmentView> attachmentFiles;     // 附件信息
    private String comment;                          // 备注内容
    private String pattern;                             //测评模式
    private Date registrationDeadlineTime;              //报名截止时间

    public static class Builder {
        public static ExamPlanDto build(ExamPlanView view) {
            ExamPlanDto dto = new ExamPlanDto();
            BeanUtils.copyProperties(view, dto);
            if (StringUtils.isNotBlank(view.getSubject()))
                dto.setSubject(Subject.valueOf(view.getSubject()));
            if (StringUtils.isNotBlank(view.getGrade()))
                dto.setGrade(Grade.valueOf(view.getGrade()));
            if (StringUtils.isNotBlank(view.getForm()))
                dto.setForm(Form.valueOf(view.getForm()));
            if (StringUtils.isNotBlank(view.getDistributeType()))
                dto.setDistributeType(DistributeType.valueOf(view.getDistributeType()));
            if (StringUtils.isNotBlank(view.getScene()))
                dto.setScene(Scene.valueOf(view.getScene()));
            if (StringUtils.isNotBlank(view.getPaperType()))
                dto.setPaperType(PaperType.valueOf(view.getPaperType()));
            if (StringUtils.isNotBlank(view.getRegionLevel()))
                dto.setRegionLevel(RegionLevel.valueOf(view.getRegionLevel()));
            if (StringUtils.isNotBlank(view.getScoreRuleType()))
                dto.setScoreRuleType(ScoreRuleType.valueOf(view.getScoreRuleType()));
            if (StringUtils.isNotBlank(view.getAllowStudentQuery()))
                dto.setAllowStudentQuery(BooleanEnum.valueOf(view.getAllowStudentQuery()));
            if (StringUtils.isNotBlank(view.getAllowTeacherModify()))
                dto.setAllowTeacherModify(BooleanEnum.valueOf(view.getAllowTeacherModify()));
            if (StringUtils.isNotBlank(view.getAllowTeacherQuery()))
                dto.setAllowTeacherQuery(BooleanEnum.valueOf(view.getAllowTeacherQuery()));
            if (StringUtils.isNotBlank(view.getSpokenScoreType()))
                dto.setSpokenScoreType(SpokenScoreType.valueOf(view.getSpokenScoreType()));
            if (StringUtils.isNotBlank(view.getSpokenAnswerTimes()))
                dto.setSpokenAnswerTimes(SpokenAnswerTimes.valueOf(view.getSpokenAnswerTimes()));
            if (StringUtils.isNotBlank(view.getType())) {
                List<Type> typeList = Arrays.asList(view.getType().split(","))
                        .stream()
                        .map(String::trim)
                        .map(Type::valueOf).collect(Collectors.toList());
                dto.setType(typeList);
            }
            if(CollectionUtils.isNotEmpty(view.getAttachmentFiles())){
                dto.setAttachment(JSON.toJSONString(view.getAttachmentFiles()));
            }else {
                dto.setAttachment(JSON.toJSONString(new ArrayList<>()));
            }
            dto.setPattern(Pattern.valueOf(Optional.ofNullable(view.getPattern()).orElse(Pattern.GENERAL.name())));
            return dto;
        }

        public static ExamPlanView build(ExamPlanDto dto) {
            ExamPlanView view = new ExamPlanView();
            BeanUtils.copyProperties(dto, view);
            if (null != dto.getSubject())
                view.setSubject(dto.getSubject().name());
            if (null != dto.getType())
                view.setType(StringUtils.join(dto.getType().stream().map(Enum::name).toArray(String[]::new), ","));
            if (null != dto.getGrade())
                view.setGrade(dto.getGrade().name());
            if (null != dto.getForm())
                view.setForm(dto.getForm().name());
            if (null != dto.getDistributeType())
                view.setDistributeType(dto.getDistributeType().name());
            if (null != dto.getScene())
                view.setScene(dto.getScene().name());
            if (null != dto.getRegionLevel())
                view.setRegionLevel(dto.getRegionLevel().name());
            if (null != dto.getSpokenScoreType())
                view.setSpokenScoreType(dto.getSpokenScoreType().name());
            if (null != dto.getScoreRuleType())
                view.setScoreRuleType(dto.getScoreRuleType().name());
            if (null != dto.getAllowStudentQuery())
                view.setAllowStudentQuery(dto.getAllowStudentQuery().name());
            if (null != dto.getAllowTeacherModify())
                view.setAllowTeacherModify(dto.getAllowTeacherModify().name());
            if (null != dto.getAllowTeacherQuery())
                view.setAllowTeacherQuery(dto.getAllowTeacherQuery().name());
            if (null != dto.getSpokenAnswerTimes())
                view.setSpokenAnswerTimes(dto.getSpokenAnswerTimes().name());
            if (null != dto.getPaperType())
                view.setPaperType(dto.getPaperType().name());
            if (null != dto.getStatus())
                view.setStatus(dto.getStatus().name());
            if (null != dto.getLogs())
                view.setLogs(dto.getLogs());
            if(StringUtils.isNotBlank(dto.getAttachment())){
                List<Builder.AttachmentView> attachmentViews = JSONArray.parseArray(dto.getAttachment(), Builder.AttachmentView.class);
                view.setAttachmentFiles(attachmentViews);
            }
            if (Objects.nonNull(dto.getPattern())) {
                view.setPattern(dto.getPattern().name());
            }
            return view;
        }

        public static List<RegionView> buildRegionInfo(ExamPlanDto dto){
            List<String> regionCodes = new ArrayList<>();
            List<String> regionNames = new ArrayList<>();
            List<String> schoolIds = new ArrayList<>();
            List<String> schoolNames = new ArrayList<>();
            if (null != dto.getRegionNames()){
                regionNames = Arrays.asList(dto.getRegionNames().split(","));
            }
            if (null != dto.getRegionCodes()){
                regionCodes = Arrays.asList(dto.getRegionCodes().split(","));
            }
            if (null != dto.getSchoolIds()){
                schoolIds = Arrays.asList(dto.getSchoolIds().split(","));
            }
            if (null != dto.getSchoolNames()){
                schoolNames = Arrays.asList(dto.getSchoolNames().split(","));
            }
            List<RegionView> regionViewList = new ArrayList<>();
            if (RegionLevel.SCHOOL == dto.getRegionLevel()){
                if (CollectionUtils.isNotEmpty(schoolIds)
                        && CollectionUtils.isNotEmpty(schoolNames) && schoolIds.size() == schoolNames.size()){
                    for (int i = 0 ; i < schoolIds.size() ; i++ ){
                        RegionView view = new RegionView();
                        view.setRegionCode(schoolIds.get(i));
                        view.setRegionName(schoolNames.get(i));
                        regionViewList.add(view);
                    }
                }
            } else {
                if (CollectionUtils.isNotEmpty(regionCodes)
                        && CollectionUtils.isNotEmpty(regionNames) && regionCodes.size() == regionNames.size()){
                    for (int i = 0 ; i < regionCodes.size() ; i++ ){
                        RegionView view = new RegionView();
                        view.setRegionCode(regionCodes.get(i));
                        view.setRegionName(regionNames.get(i));
                        regionViewList.add(view);
                    }
                }
            }
            return regionViewList;
        }

        @Data
        public static class RegionView {
            private String regionName;
            private String regionCode;
        }

        @Data
        public static class AttachmentView{
            private String fileName;
            private String fileUrl;
        }

    }
}
