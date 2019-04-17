package com.voxlearning.utopia.agent.mockexam.dao.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPlan;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPlanDto;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 考试计划存储模型
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Data
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_MEXAM_PLAN")
public class ExamPlanEntity implements Serializable {

    @UtopiaSqlColumn(
            name = "ID",
            primaryKey = true,
            primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC
    )
    protected Long id;
    @UtopiaSqlColumn
    private String name;                           // 名称
    @UtopiaSqlColumn
    private String subject;                        // 学科
    @UtopiaSqlColumn
    private String type;                           // 类型
    @UtopiaSqlColumn
    private String grade;                          // 年级
    @UtopiaSqlColumn
    private String form;
    // 形式
    @UtopiaSqlColumn
    private Date teacherQueryTime;                                  // 教师查看试卷时间
    @UtopiaSqlColumn
    private String allowTeacherQuery;             // 是否允许教师查看成绩
    @UtopiaSqlColumn
    private String allowTeacherModify;            // 是否允许教师修改成绩
    @UtopiaSqlColumn
    private Date teacherMarkDeadline;                               // 教师判分截止时间
    @UtopiaSqlColumn
    private String allowStudentQuery;             // 是否允许学生查看成绩
    @UtopiaSqlColumn
    private Date scorePublishTime;         // 成绩发布时间
    @UtopiaSqlColumn
    private Integer finishExamTime;                // 允许交卷时间
    @UtopiaSqlColumn
    private Integer examTotalTime;                    // 答卷时长
    @UtopiaSqlColumn
    private Integer totalScore;                       // 试卷总分
    @UtopiaSqlColumn
    private String spokenScoreType;                // 口语算分类型
    @UtopiaSqlColumn
    private String spokenAnswerTimes;             // 口语可答题次数
    @UtopiaSqlColumn
    private String scoreRuleType;                      // 成绩类型
    @UtopiaSqlColumn
    private String scoreRule;                      // 等级制内容
    @UtopiaSqlColumn
    private String bookId;                  // 教材ID
    @UtopiaSqlColumn
    private String bookName;                // 教材名称
    @UtopiaSqlColumn
    private String paperType;                      // 试卷类型
    @UtopiaSqlColumn
    private String examId;                         // 考试id
    @UtopiaSqlColumn
    private String papers;                         // 试卷
    @UtopiaSqlColumn
    private String distributeType;                 // 获取试卷方式
    @UtopiaSqlColumn
    private String scene;                          // 场景
    @UtopiaSqlColumn
    private String regionLevel;                    // 区域级别,0:省级,1:市级,2:区级,3:校级
    @UtopiaSqlColumn
    private String regionCodes;                    // 所属区域ID
    @UtopiaSqlColumn
    private String regionNames;                    // 所属区域
    @UtopiaSqlColumn
    private String schoolIds;                        // 学校ID
    @UtopiaSqlColumn
    private String schoolNames;                    // 学校名称
    @UtopiaSqlColumn
    private Date startTime;                                         // 开始时间
    @UtopiaSqlColumn
    private Date endTime;                                           // 结束时间
    @UtopiaSqlColumn
    private String status;                         // 状态
    @UtopiaSqlColumn
    @DocumentCreateTimestamp
    private Date createDatetime;                                        // 创建时间
    @UtopiaSqlColumn
    @DocumentUpdateTimestamp
    private Date updateDatetime;                   // 更新时间
    @UtopiaSqlColumn
    private Long creatorId;                        // 申请人ID
    @UtopiaSqlColumn
    private String creatorName;                    // 申请人名称

    @UtopiaSqlColumn
    private String attachment;                         // 上传的附件json, [{fileName:"", fileUrl:""},{fileName:"", fileUrl:""}]
    @UtopiaSqlColumn
    private String comment;                          // 备注内容
    @UtopiaSqlColumn
    private String pattern;                             //测评模式
    @UtopiaSqlColumn
    private Date registrationDeadlineTime;              //报名截止时间
    //生成缓存 key

//    public static String ck_eid(Long paperId) {
//        return CacheKeyGenerator.generateCacheKey(ExamPlanEntity.class, "eid", paperId);
//    }


    public static class Builder {
        public static ExamPlanEntity build(ExamPlan model) {
            ExamPlanEntity entity = new ExamPlanEntity();
            BeanUtils.copyProperties(model, entity);
            if (null != model.getSubject())
                entity.setSubject(model.getSubject().name());
            if (null != model.getType())
                entity.setType(JSONArray.toJSONString(model.getType()));
            if (null != model.getGrade())
                entity.setGrade(model.getGrade().name());
            if (null != model.getForm())
                entity.setForm(model.getForm().name());
            if (null != model.getAllowStudentQuery())
                entity.setAllowStudentQuery(model.getAllowStudentQuery().name());
            if (null != model.getAllowTeacherModify())
                entity.setAllowTeacherModify(model.getAllowTeacherModify().name());
            if (null != model.getAllowTeacherQuery())
                entity.setAllowTeacherQuery(model.getAllowTeacherQuery().name());
            if (null != model.getSpokenScoreType())
                entity.setSpokenScoreType(model.getSpokenScoreType().name());
            if (null != model.getSpokenAnswerTimes())
                entity.setSpokenAnswerTimes(model.getSpokenAnswerTimes().name());
            if (null != model.getScoreRuleType())
                entity.setScoreRuleType(model.getScoreRuleType().name());
            if (null != model.getPaperType())
                entity.setPaperType(model.getPaperType().name());
            if (null != model.getStatus())
                entity.setStatus(model.getStatus().name());
            if (null != model.getDistributeType())
                entity.setDistributeType(model.getDistributeType().name());
            if (null != model.getScene())
                entity.setScene(model.getScene().name());
            if (null != model.getRegionLevel())
                entity.setRegionLevel(model.getRegionLevel().name());
            if (Objects.nonNull(model.getPattern()))
                entity.setPattern(model.getPattern().name());
            if (Objects.nonNull(model.getRegistrationDeadlineTime()))
                entity.setRegistrationDeadlineTime(model.getRegistrationDeadlineTime());

            // 试卷
            List<ExamPlan.Paper> papers = model.getPapers();
            if (null != papers)
                entity.setPapers(JSON.toJSONString(papers));

            // 学校
            List<ExamPlan.School> schools = model.getSchools();
            if (null != schools) {
                entity.setSchoolIds(
                        StringUtils.join(
                                schools.stream().map(ExamPlan.School::getId).toArray(String[]::new), ","));
                entity.setSchoolNames(
                        StringUtils.join(
                                schools.stream().map(ExamPlan.School::getName).toArray(String[]::new), ","));
            }

            // 教材
            if (null != model.getBook()) {
                entity.setBookId(model.getBook().getId());
                entity.setBookName(model.getBook().getName());
            }

            // 区域
            List<ExamPlan.Region> regions = model.getRegions();
            if (null != regions) {
                entity.setRegionCodes(
                        StringUtils.join(
                                regions.stream().map(i -> Integer.valueOf(i.getCode()).toString()).toArray(String[]::new), ","));
                entity.setRegionNames(
                        StringUtils.join(
                                regions.stream().map(ExamPlan.Region::getName).toArray(String[]::new), ","));
            }

            // 等级制内容
            List<ExamPlanDto.Rule> scoreRule = model.getScoreRule();
            if (null != scoreRule) {
                entity.setScoreRule(JSONArray.toJSONString(scoreRule));
            }
            return entity;
        }
    }

}
