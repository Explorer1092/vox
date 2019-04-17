package com.voxlearning.utopia.agent.mockexam.domain.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanEntity;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.OperateRequest;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.BooleanEnum;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums.*;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPlanDto;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试计划领域模型
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExamPlan extends OperateRequest implements Serializable {

    /**
     * 默认值
     */
    public interface DefaultValues {

        /**
         * 默认总分
         */
        int TOTAL_SCORE = 100;

        /**
         * 默认交卷时间
         */
        int DEFAULT_FINISH_EXAM_TIME = 0;

        /**
         * 默认答卷时间
         */
        int EXAM_TIME = 10;

        /**
         * 口语答题次数
         */
        SpokenAnswerTimes SPOKEN_ANSWER_TIMES = SpokenAnswerTimes.ONE;
    }

    /**
     * id
     */
    private Long id;

    /**
     * 名称
     */
    @Length(min = 1, max = 50, message = "名称不为空并且不能多余50个字符")
    private String name;

    /**
     * 学科
     */
    @NotNull(message = "【学科】不能为空")
    private Subject subject;

    /**
     * 类型
     */
    @NotNull(message = "【类型】不能为空")
    private List<Type> type;

    /**
     * 年级
     */
    @NotNull(message = "【年级】不能为空")
    private Grade grade;

    /**
     * 形式
     */
    @NotNull(message = "【形式】不能为空")
    private Form form;

    /**
     * 教师查看试卷时间
     */
//    @NotNull(message = "【教师查看试卷时间】不能为空")
    private Date teacherQueryTime;

    /**
     * 是否允许教师查看成绩
     */
    @NotNull(message = "【是否允许教师查看成绩】不能为空")
    private BooleanEnum allowTeacherQuery;

    /**
     * 是否允许教师修改成绩
     */
//    @NotNull(message = "【是否允许教师修改成绩】不能为空")
    private BooleanEnum allowTeacherModify;

    /**
     * 教师判分截止时间
     */
    @NotNull(message = "【教师判分截止时间】不能为空")
    private Date teacherMarkDeadline;

    /**
     * 是否允许学生查看成绩
     */
    @NotNull(message = "【是否允许学生查看成绩】不能为空")
    private BooleanEnum allowStudentQuery;

    /**
     * 成绩发布时间
     */
//    @NotNull(message = "【成绩发布时间】不能为空")
    private Date scorePublishTime;

    /**
     * 允许交卷时间
     */
    @NotNull(message = "【允许交卷时间】不能为空")
    private Integer finishExamTime;

    /**
     * 答卷时长
     */
    @NotNull(message = "【试卷总答卷时长】不能为空")
    private Integer examTotalTime;

//    /**
//     * 试卷总分
//     */
//    private Integer totalScore;

    /**
     * 口语算分类型
     */
//    @NotNull(message = "【口语算分类型】不能为空")
    private SpokenScoreType spokenScoreType;

    /**
     * 口语可答题次数
     */
    private SpokenAnswerTimes spokenAnswerTimes;

    /**
     * 成绩类型
     */
    @NotNull(message = "【成绩类型】不能为空")
    private ScoreRuleType scoreRuleType;

    /**
     * 等级制内容
     */
    @NotNull(message = "【等级制内容】不能为空")
    private List<ExamPlanDto.Rule> scoreRule;

//    /**
//     * 教材ID
//     */
//    @NotNull(message = "【教材ID】不能为空")
//    private String bookId;
//
//    /**
//     * 教材名称
//     */
//    @NotNull(message = "【教材名称】不能为空")
//    private String bookName;

    @NotNull(message = "【教材】不能为空")
    private Book book;

//    /**
//     * 教材
//     */
//    private List<Book> books;

    /**
     * 试卷类型
     */
    @NotNull(message = "【试卷类型】不能为空")
    private PaperType paperType;

//    /**
//     * 试卷ID
//     */
//    @NotNull(message = "【试卷ID】不能为空")
//    private String paperId;

//    /**
//     * 试卷文档地址，多个逗号分隔
//     */
//    @NotNull(message = "【试卷文档地址】不能为空")
//    private String paperDocUrls;

//    /**
//     * 试卷名称，多个逗号分隔
//     */
//    @NotNull(message = "【试卷名称】不能为空")
//    private String paperDocNames;

//    /**
//     * 文档
//     */
//    private List<PaperDoc> docs;

    /**
     * 试卷
     */
    private List<Paper> papers;

    /**
     * 考试id
     */
    private String examId;

    /**
     * 获取试卷方式
     */
    @NotNull(message = "【获取试卷方式】不能为空")
    private DistributeType distributeType;

    /**
     * 场景
     */
    @NotNull(message = "【场景】不能为空")
    private Scene scene;

    /**
     * 区域级别
     */
    @NotNull(message = "【区域级别】不能为空")
    private RegionLevel regionLevel;


//    /**
//     * 所属区域ID
//     */
//    @NotNull(message = "【所属区域】不能为空")
//    private String regionCodes;
//
//    /**
//     * 所属区域
//     */
//    @NotNull(message = "【所属区域】不能为空")
//    private String regionNames;

    /**
     * 区域
     */
    private List<Region> regions;

//    /**
//     * 学校ID
//     */
//    @NotNull(message = "【学校】不能为空")
//    private Long schoolIds;
//
//    /**
//     * 学校名称
//     */
//    @NotNull(message = "【学校名称】不能为空")
//    private String schoolNames;


    /**
     * 学校
     */
    private List<School> schools;

    /**
     * 开始时间
     */
    @NotNull(message = "【开始时间】不能为空")
    private Date startTime;

    /**
     * 结束时间
     */
    @NotNull(message = "【结束时间】不能为空")
    private Date endTime;

    /**
     * 状态
     */
    @NotNull(message = "【状态】不能为空")
    private Status status;

    /**
     * 创建时间
     */
    @NotNull(message = "【创建时间】不能为空")
    private Date createDatetime;

    /**
     * 更新时间
     */
    @NotNull(message = "【更新时间】不能为空")
    private Date updateDatetime;

    /**
     * 申请人ID
     */
    @NotNull(message = "【申请人ID】不能为空")
    private Long creatorId;

    /**
     * 申请人名称
     */
    @NotNull(message = "【申请人名称】不能为空")
    private String creatorName;

    /**
     * 审核意见
     */
    private List<ExamPlanOperateLog> logs;

    private String attachment;                         // 上传的附件json, [{fileName:"", fileUrl:""},{fileName:"", fileUrl:""}]
    private String comment;                          // 备注内容

    /**
     * 测评模式
     */
    @NotNull(message = "【测评模式】不能为空")
    private Pattern pattern;
    /**
     * 报名截止时间
     */
    private Date registrationDeadlineTime;

    /**
     * 区域
     */
    @Data
    @AllArgsConstructor
    public static class Region implements Serializable {
        private RegionType type;
        private int code;
        private String name;
    }

//    /**
//     * 试卷文档
//     */
//    @Data
//    @AllArgsConstructor
//    public static class PaperDoc implements Serializable {
//        private String name;
//        private String url;
//    }

    /**
     * 试卷
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Paper implements Serializable {
        private String paperId;
        private String docTitle;
        private String docUrl;
    }

    /**
     * 学校
     */
    @Data
    @AllArgsConstructor
    public static class School implements Serializable {
        private String id;
        private String name;
    }

    /**
     * 教材
     */
    @Data
    @AllArgsConstructor
    public static class Book implements Serializable {
        private String id;
        private String name;
    }


    @Service
    public static class Builder {

        @Inject private RaikouSystem raikouSystem;

        public static ExamPlan buildDefault() {
            ExamPlan examPlan = new ExamPlan();
            examPlan.setSubject(Subject.MATH);
            List<Type> typeList = new ArrayList<>();
            typeList.add(Type.GENERAL);
            examPlan.setType(typeList);
            examPlan.setForm(Form.OTHER);
            examPlan.setPaperType(PaperType.OLD);
            examPlan.setDistributeType(DistributeType.RANDOM);
            examPlan.setScene(Scene.ONLINE);
            examPlan.setRegionLevel(RegionLevel.CITY);
            examPlan.setFinishExamTime(DefaultValues.DEFAULT_FINISH_EXAM_TIME);
//            examPlan.setTotalScore(DefaultValues.TOTAL_SCORE);
            examPlan.setExamTotalTime(DefaultValues.EXAM_TIME);
            examPlan.setSpokenScoreType(SpokenScoreType.ROUND);
            examPlan.setSpokenAnswerTimes(DefaultValues.SPOKEN_ANSWER_TIMES);
            examPlan.setGrade(Grade.FIRST);
            examPlan.setAllowStudentQuery(BooleanEnum.Y);
            examPlan.setAllowTeacherModify(BooleanEnum.N);
            examPlan.setAllowTeacherQuery(BooleanEnum.Y);
            examPlan.setScoreRuleType(ScoreRuleType.SCORE);
            examPlan.setPattern(Pattern.GENERAL);

            List<ExamPlanDto.Rule> defaultRules = new ArrayList<>();
            ExamPlanDto.Rule rule1 = new ExamPlanDto.Rule();
            rule1.setRankName("优");
            rule1.setBottom(85F);
            rule1.setTop(100F);
            defaultRules.add(rule1);
            ExamPlanDto.Rule rule2 = new ExamPlanDto.Rule();
            rule2.setRankName("良");
            rule2.setBottom(75F);
            rule2.setTop(85F);
            defaultRules.add(rule2);
            ExamPlanDto.Rule rule3 = new ExamPlanDto.Rule();
            rule3.setRankName("合格");
            rule3.setBottom(60F);
            rule3.setTop(75F);
            defaultRules.add(rule3);
            ExamPlanDto.Rule rule4 = new ExamPlanDto.Rule();
            rule4.setRankName("待合格");
            rule4.setBottom(0F);
            rule4.setTop(60F);
            defaultRules.add(rule4);

            examPlan.setScoreRule(defaultRules);
            return examPlan;
        }

        public static ExamPlan build(ExamPlanDto dto) {
            ExamPlan model = new ExamPlan();
            BeanUtils.copyProperties(dto, model);

            // 教材
            if (StringUtils.isNotBlank(dto.getBookId())
                    && StringUtils.isNotBlank(dto.getBookName()))
                model.setBook(new Book(dto.getBookId(), dto.getBookName()));

            // 学校
            model.schools = KeValueModelFactory.build(
                    dto.getSchoolIds(),
                    dto.getSchoolNames(),
                    School::new);

            // 区域
            if (StringUtils.isNotBlank(dto.getRegionCodes()) && StringUtils.isNotBlank(dto.getRegionNames()))
                model.regions = KeValueModelFactory.build(
                        dto.getRegionCodes(),
                        dto.getRegionNames(),
                        (key, value) -> new Region(dto.getRegionLevel().regionType, Integer.valueOf(key), value));

            // 试卷
            if (PaperType.OLD == dto.getPaperType()) {
                String paperId = dto.getPaperId();
                if (StringUtils.isNotBlank(paperId)) {
                    model.setPapers(Arrays.stream(paperId.split(","))
                            .map(String::trim)
                            .filter(StringUtils::isNotBlank)
                            .map(i -> {
                                ExamPlan.Paper paper = new ExamPlan.Paper();
                                paper.setPaperId(i);
                                return paper;
                            }).collect(Collectors.toList()));
                }
            } else {
                if (StringUtils.isBlank(dto.getPaperId())) {
                    model.papers = KeValueModelFactory.build(
                            dto.getPaperDocNames(),
                            dto.getPaperDocUrls(),
                            (key, value) -> new Paper(null, key, value));
                } else {
                    List<Paper> papers = new ArrayList<>();
                    if (StringUtils.isNotBlank(dto.getPaperDocNames())
                            && StringUtils.isNotBlank(dto.getPaperDocUrls())) {
                        String[] docNames = Arrays.stream(dto.getPaperDocNames().split(","))
                                .map(String::trim)
                                .filter(StringUtils::isNotBlank)
                                .toArray(String[]::new);
                        String[] docUrls = Arrays.stream(dto.getPaperDocUrls().split(","))
                                .map(String::trim)
                                .filter(StringUtils::isNotBlank)
                                .toArray(String[]::new);
                        String[] paperIds = Arrays.stream(dto.getPaperId().split(","))
                                .map(String::trim)
                                .filter(StringUtils::isNotBlank)
                                .toArray(String[]::new);
                        if (paperIds.length != docUrls.length || paperIds.length != docNames.length) {
                            throw new BusinessException(ErrorCode.PLAN_PAPER_UNKNOWN_STATUS,
                                    "生成的试卷ID数量与上传的试卷数不一致");
                        } else {
                            for (int i = 0; i < paperIds.length && i < docUrls.length && i < docNames.length; i++)
                                papers.add(new Paper(paperIds[i], docNames[i], docUrls[i]));
                        }
                    }
                    model.papers = papers;
                }
            }

            return model;
        }

        public static ExamPlanDto build(ExamPlan model) {
            ExamPlanDto dto = new ExamPlanDto();
            BeanUtils.copyProperties(model, dto);

            Book book = model.getBook();
            if (null != book) {
                dto.setBookId(book.getId());
                dto.setBookName(book.getName());
            }

            List<Paper> papers = model.getPapers();
            if (null != papers && !papers.isEmpty()) {
                dto.setPaperId(StringUtils.join(
                        papers.stream()
                                .filter(i -> StringUtils.isNotBlank(i.getPaperId()))
                                .map(Paper::getPaperId)
                                .toArray(String[]::new),
                        ","));
                dto.setPaperDocNames(StringUtils.join(
                        papers.stream()
                                .filter(i -> StringUtils.isNotBlank(i.getDocTitle()))
                                .map(Paper::getDocTitle)
                                .toArray(String[]::new),
                        ","));
                dto.setPaperDocUrls(StringUtils.join(
                        papers.stream()
                                .filter(i -> StringUtils.isNotBlank(i.getDocUrl()))
                                .map(Paper::getDocUrl)
                                .toArray(String[]::new),
                        ","));
            }

            List<School> schools = model.getSchools();
            if (null != schools && !schools.isEmpty()) {
                dto.setSchoolIds(StringUtils.join(
                        schools.stream()
                                .filter(i -> StringUtils.isNotBlank(i.getId()))
                                .map(School::getId)
                                .toArray(String[]::new),
                        ","));
                dto.setSchoolNames(StringUtils.join(
                        schools.stream()
                                .filter(i -> StringUtils.isNotBlank(i.getName()))
                                .map(School::getName)
                                .toArray(String[]::new),
                        ","));
            }

            List<Region> regions = model.getRegions();
            if (null != regions && !regions.isEmpty()) {
                dto.setRegionCodes(StringUtils.join(
                        regions.stream()
                                .map(Region::getCode)
                                .toArray(Integer[]::new),
                        ","));
                dto.setRegionNames(StringUtils.join(
                        regions.stream()
                                .map(Region::getName)
                                .toArray(String[]::new),
                        ","));
            }

            List<ExamPlanOperateLog> logs = model.getLogs();
            if (null != logs && !logs.isEmpty()) {
                List<ExamPlanDto.OperationLog> _logs = logs.stream().map(i -> {
                    ExamPlanDto.OperationLog log = new ExamPlanDto.OperationLog();
                    log.setDate(i.getCreateDatetime());
                    log.setOperatorName(i.getOperatorName());
                    if (StringUtils.isNotBlank(i.getNote()))
                        log.setDesc(String.format("状态：%s => %s，备注：%s", i.getPrevStatus().desc, i.getCurrentStatus().desc, i.getNote()));
                    else
                        log.setDesc(String.format("状态：%s => %s", i.getPrevStatus().desc, i.getCurrentStatus().desc));
                    return log;
                }).collect(Collectors.toList());
                dto.setLogs(_logs);
            }

            return dto;
        }

        public ExamPlan build(ExamPlanEntity entity) {
            ExamPlan model = new ExamPlan();
            BeanUtils.copyProperties(entity, model);
            if (StringUtils.isNotBlank(entity.getSubject()))
                model.setSubject(Subject.valueOf(entity.getSubject()));
            if (StringUtils.isNotBlank(entity.getType())) {
                JSONArray jsonArray = JSONArray.parseArray(entity.getType());
                List<Type> typeList = JSONObject.parseArray(jsonArray.toJSONString(), Type.class);
                model.setType(typeList);
            }
            if (StringUtils.isNotBlank(entity.getGrade()))
                model.setGrade(Grade.valueOf(entity.getGrade()));
            if (StringUtils.isNotBlank(entity.getForm()))
                model.setForm(Form.valueOf(entity.getForm()));
            if (StringUtils.isNotBlank(entity.getDistributeType()))
                model.setDistributeType(DistributeType.valueOf(entity.getDistributeType()));
            if (StringUtils.isNotBlank(entity.getScene()))
                model.setScene(Scene.valueOf(entity.getScene()));
            if (StringUtils.isNotBlank(entity.getPaperType()))
                model.setPaperType(PaperType.valueOf(entity.getPaperType()));
            if (StringUtils.isNotBlank(entity.getRegionLevel()))
                model.setRegionLevel(RegionLevel.valueOf(entity.getRegionLevel()));
            if (StringUtils.isNotBlank(entity.getScoreRuleType()))
                model.setScoreRuleType(ScoreRuleType.valueOf(entity.getScoreRuleType()));
            if (StringUtils.isNotBlank(entity.getAllowStudentQuery()))
                model.setAllowStudentQuery(BooleanEnum.valueOf(entity.getAllowStudentQuery()));
            if (StringUtils.isNotBlank(entity.getAllowTeacherModify()))
                model.setAllowTeacherModify(BooleanEnum.valueOf(entity.getAllowTeacherModify()));
            if (StringUtils.isNotBlank(entity.getAllowTeacherQuery()))
                model.setAllowTeacherQuery(BooleanEnum.valueOf(entity.getAllowTeacherQuery()));
            if (StringUtils.isNotBlank(entity.getStatus()))
                model.setStatus(Status.valueOf(entity.getStatus()));
            if (StringUtils.isNotBlank(entity.getSpokenScoreType()))
                model.setSpokenScoreType(SpokenScoreType.valueOf(entity.getSpokenScoreType()));
            if (StringUtils.isNotBlank(entity.getSpokenAnswerTimes()))
                model.setSpokenAnswerTimes(SpokenAnswerTimes.valueOf(entity.getSpokenAnswerTimes()));

            // 教材
            if (StringUtils.isNotBlank(entity.getBookId())
                    && StringUtils.isNotBlank(entity.getBookName()))
                model.setBook(new Book(entity.getBookId(), entity.getBookName()));

            // 学校
            model.schools = KeValueModelFactory.build(
                    entity.getSchoolIds(),
                    entity.getSchoolNames(),
                    School::new);

            // 区域
            if (StringUtils.isNotBlank(entity.getRegionCodes()) && StringUtils.isNotBlank(entity.getRegionNames())) {

                List<Integer> regionsCodes = Arrays.stream(entity.getRegionCodes().split(",")).map(Integer::valueOf).collect(Collectors.toList());
                Map<Integer, ExRegion> regionsMap = raikouSystem.getRegionBuffer().loadRegions(regionsCodes);

                model.regions = KeValueModelFactory.build(
                        entity.getRegionCodes(),
                        entity.getRegionNames(),
                        (key, value) -> new Region(
                                regionsMap.containsKey(Integer.valueOf(key)) ? regionsMap.get(Integer.valueOf(key)).fetchRegionType() : null,
                                Integer.valueOf(key),
                                value));
                if (null != model.getRegions() && !model.getRegions().isEmpty()) {
                    List<Integer> regionCodes = model.getRegions().stream().map(Region::getCode).collect(Collectors.toList());
                }
            }


            // 试卷
            final String papers = entity.getPapers();
            if (StringUtils.isNotBlank(papers)) {
                model.setPapers(JSONArray.parseArray(papers, Paper.class));

            }

            // 等第制规则
            String scoreRule = entity.getScoreRule();
            if (StringUtils.isNotBlank(scoreRule)) {
                model.setScoreRule(JSONArray.parseArray(scoreRule, ExamPlanDto.Rule.class));
            }
            //测评模式
            model.setPattern(Optional.ofNullable(Pattern.of(entity.getPattern())).orElse(Pattern.GENERAL));
            return model;
        }
    }
}
