package com.voxlearning.utopia.agent.mockexam.integration;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPlan;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamStudentScore;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.BooleanEnum;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamScoreQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.output.ExamPlanDto;
import com.voxlearning.utopia.service.question.api.entity.XxBaseRegion;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试系统客户端
 *
 * @author xiaolei.li
 * @version 2018/8/6
 * @see <a href="http://wiki.17zuoye.net/pages/viewpage.action?pageId=31942507">模考-题库供天玑系统调用接口applyExam</a>
 */
public interface ExamClient {

    /**
     * 申请考试
     *
     * @param plan 测评计划
     * @return 响应
     */
    ApplyResponse apply(ExamPlan plan);

    /**
     * 上线
     *
     * @param request 请求
     * @return 响应
     */
    OnlineResponse online(OnlineRequest request);

    /**
     * 下线
     *
     * @param request 请求
     * @return 响应
     */
    OfflineResponse offline(OfflineRequest request);

    /**
     * 撤销
     *
     * @param request 请求
     * @return 响应
     */
    WithdrawResponse withdraw(WithdrawRequest request);

    /**
     * 重考或者补考
     *
     * @param request 请求
     * @return 响应，包含没有处理成功的学生名单和对应的错误信息，格式(studentId, errorMessage)，如果处理成功，此map为空
     */
    Map<Long, String> retry(RetryRequest request);

    /**
     * 查询学生成绩
     *
     * @param params 请求参数
     * @return 学生成绩
     */
    ExamStudentScore queryScore(ExamScoreQueryParams params);

    /**
     * 查询某次考试的学生人数
     *
     * @param examId 学生id
     * @return 学生人数
     */
    int countExamStudent(String examId);

    /**
     * 申请请求
     */
    @Data
    class ApplyRequest implements Serializable {
        private String id;
        private String name;
        private String examType;
        private String regionLevel;
        private List<XxBaseRegion> regions;
        private List<Integer> schoolIds;
        private String schoolLevel;
        private int subjectId;
        private List<String> clazzLevels;
        private int submitAfterMinutes;
        private Date resultIssueAt;
        private String agentName;
        private long agentId;
        private int oralRepeatCount;
        private int teacherVisible;
        private int studentVisible;
        private Date examStopAt;
        private int durationMinutes;
        private Date correctStopAt;
        private Date examStartAt;
        private String status;
        private String score;
        private String bookCategoryId;
        private List<Paper> papers;
        private List<Rank> ranks;
        private List<String> contentTypes;
        private int distribution;
        private int testScene;
        private int gradeType;
        private int isEvaluation;
        private Date applyStartAt;//报名开始时间
        private Date applyStopAt;//报名截止时间

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Paper implements Serializable {
            private String paperId;
            private String fileUrl;
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        static class Rank implements Serializable {
            private String rankName;
            private String top;
            private String bottom;
        }

        @Service
        public static class Builder {

            @Inject private RaikouSystem raikouSystem;

            public ApplyRequest build(ExamPlan plan) {
                ApplyRequest request = new ApplyRequest();
                request.setId(plan.getId().toString());
                request.setName(plan.getName());
                switch (plan.getPattern()) {
                    case GENERAL:
                        request.setExamType("unify");
                        break;
                    case REGISTER:
                        request.setExamType("apply");
                        request.setApplyStartAt(Optional.ofNullable(plan.getCreateDatetime()).orElse(new Date()));//默认取创建时间
                        request.setApplyStopAt(plan.getRegistrationDeadlineTime());
                        break;
                    default:
                        request.setExamType("unify");
                        break;
                }
                request.setSubjectId(plan.getSubject().subject.getId());
                switch (plan.getRegionLevel()) {
                    case PROVINCE:
                        request.setRegionLevel("province");
                        break;
                    case CITY:
                        request.setRegionLevel("city");
                        break;
                    case COUNTY:
                        request.setRegionLevel("country");
                        break;
                    case SCHOOL:
                        request.setRegionLevel("school");
                        break;
                    default:
                        throw new BusinessException(ErrorCode.EXAM_CREATE,
                                String.format("测评[id=%s]的区域级别[regionLevel=%s]不正确", plan.getId(), plan.getRegionLevel()));
                }
                List<ExamPlan.Region> regions = plan.getRegions();
                if (null != regions && !regions.isEmpty()) {

                    List<Integer> regionIds = regions.stream().map(ExamPlan.Region::getCode).collect(Collectors.toList());
                    Map<Integer, ExRegion> regionMap = raikouSystem.getRegionBuffer().loadRegions(regionIds);

                    List<XxBaseRegion> _regions = regions.stream().map(i -> {
                        XxBaseRegion regionObject = new XxBaseRegion();
                        switch (i.getType()) {
                            case UNKNOWN:
                                break;
                            case PROVINCE:
                                regionObject.setProvinceId(i.getCode());
                                break;
                            case CITY:
                                regionObject.setCityId(i.getCode());
                                if (regionMap.containsKey(i.getCode())) {
                                    regionObject.setProvinceId(regionMap.get(i.getCode()).getProvinceCode());
                                }
                                break;
                            case COUNTY:
                                regionObject.setRegionId(i.getCode());
                                if (regionMap.containsKey(i.getCode())) {
                                    regionObject.setProvinceId(regionMap.get(i.getCode()).getProvinceCode());
                                    regionObject.setCityId(regionMap.get(i.getCode()).getCityCode());
                                }
                        }
                        return regionObject;
                    }).collect(Collectors.toList());
                    request.setRegions(_regions);
                }
                List<ExamPlan.School> schools = plan.getSchools();
                if (null != schools && !schools.isEmpty()) {
                    request.setSchoolIds(schools.stream().map(ExamPlan.School::getId).map(Integer::valueOf).collect(Collectors.toList()));
                }
                request.setSchoolLevel("JUNIOR");
                request.setClazzLevels(Lists.newArrayList(Integer.valueOf(plan.getGrade().clazzLevel.getLevel()).toString()));
                request.setSubmitAfterMinutes(plan.getFinishExamTime());
                request.setResultIssueAt(plan.getScorePublishTime());
                request.setAgentId(plan.getCreatorId());
                request.setAgentName(plan.getCreatorName());
                ExamPlanEnums.SpokenAnswerTimes spokenAnswerTimes = plan.getSpokenAnswerTimes();
                if (null != spokenAnswerTimes) {
                    switch (spokenAnswerTimes) {
                        case ONE:
                            request.setOralRepeatCount(1);
                            break;
                        case TWO:
                            request.setOralRepeatCount(2);
                            break;
                        case TREE:
                            request.setOralRepeatCount(3);
                            break;
                        default:
                            request.setOralRepeatCount(-1);
                            break;
                    }
                }
                if (BooleanEnum.Y == plan.getAllowTeacherQuery())
                    request.setTeacherVisible(1);
                else
                    request.setTeacherVisible(0);
                if (BooleanEnum.Y == plan.getAllowStudentQuery())
                    request.setStudentVisible(1);
                else
                    request.setStudentVisible(0);
                request.setExamStopAt(plan.getEndTime());
                request.setDurationMinutes(plan.getExamTotalTime());
                request.setCorrectStopAt(plan.getTeacherMarkDeadline());
                request.setExamStartAt(plan.getStartTime());
//                request.setScore(plan.getTotalScore().toString());
                request.setBookCategoryId(plan.getBook().getId());
                if (ExamPlanEnums.ScoreRuleType.GRADE == plan.getScoreRuleType())
                    request.setGradeType(1);
                else
                    request.setGradeType(0);
                List<ExamPlanDto.Rule> scoreRule = plan.getScoreRule();
                if (null != scoreRule && !scoreRule.isEmpty()) {
                    request.setRanks(scoreRule.stream().map(i -> {
                        Rank rank = new Rank();
                        rank.setRankName(i.getRankName());
                        rank.setTop(Integer.toString(i.getTop().intValue()));
                        rank.setBottom(Integer.toString(i.getBottom().intValue()));
                        return rank;
                    }).collect(Collectors.toList()));
                }
                if (ExamPlanEnums.DistributeType.RANDOM == plan.getDistributeType())
                    request.setDistribution(1);
                else
                    request.setDistribution(0);
                List<ExamPlan.Paper> papers = plan.getPapers();
                if (null != papers && !papers.isEmpty()) {
                    request.setPapers(papers.stream().map(i -> new Paper(i.getPaperId(), i.getDocUrl())).collect(Collectors.toList()));
                }
                List<ExamPlanEnums.Type> types = plan.getType();
                if (null != types && !types.isEmpty()) {
                    request.setContentTypes(types.stream().map(i -> {
                        if (ExamPlanEnums.Type.GENERAL == i) {
                            return "normal";
                        } else if (ExamPlanEnums.Type.SPOKEN == i) {
                            return "oral";
                        } else if (ExamPlanEnums.Type.AUDITION == i) {
                            return "listening";
                        } else if (ExamPlanEnums.Type.CALCULATION == i) {
                            return "calculation";
                        }
                        return "";
                    }).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
                }
                // 申请考试就上线，无需审批
                request.setStatus("ONLINE");
                request.setIsEvaluation(1);
                return request;
            }

            /**
             * 请求参数转map
             *
             * @param request 请求模型
             * @return map格式
             */
            public static Map<Object, Object> build(ApplyRequest request) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                    Map<Object, Object> map = Maps.newHashMap();
                    map.put("id", request.getId());
                    map.put("name", request.getName());
                    map.put("examType", request.getExamType());
                    map.put("regionLevel", request.getRegionLevel());
                    List<XxBaseRegion> regions = request.getRegions();
                    if (null != regions && !regions.isEmpty())
                        map.put("regions", regions.toString());
                    List<Integer> schoolIds = request.getSchoolIds();
                    if (null != schoolIds && !schoolIds.isEmpty())
                        map.put("schoolIds", JSON.toJSONString(schoolIds));
                    map.put("schoolLevel", request.getSchoolLevel());
                    map.put("subjectId", Integer.toString(request.getSubjectId()));
                    map.put("clazzLevels", request.getClazzLevels());
                    map.put("submitAfterMinutes", Integer.toString(request.getSubmitAfterMinutes()));
                    map.put("agentName", request.getAgentName());
                    map.put("agentId", Long.toString(request.getAgentId()));
                    map.put("oralRepeatCount", Integer.toString(request.getOralRepeatCount()));
                    map.put("teacherVisible", Integer.toString(request.getTeacherVisible()));
                    map.put("studentVisible", Integer.toString(request.getStudentVisible()));
                    map.put("durationMinutes", Integer.toString(request.getDurationMinutes()));
                    if (null != request.getResultIssueAt())
                        map.put("resultIssueAt", sdf.format(request.getResultIssueAt()));
                    if (null != request.getCorrectStopAt())
                        map.put("correctStopAt", sdf.format(request.getCorrectStopAt()));
                    if (null != request.getExamStartAt())
                        map.put("examStartAt", sdf.format(request.getExamStartAt()));
                    if (null != request.getExamStopAt())
                        map.put("examStopAt", sdf.format(request.getExamStopAt()));
                    map.put("status", request.getStatus());
                    map.put("score", request.getScore());
                    map.put("bookCatalogId", request.getBookCategoryId());
                    map.put("papers", JSON.toJSONString(request.getPapers()));
                    map.put("ranks", JSON.toJSONString(request.getRanks()));
                    map.put("contentTypes", JSON.toJSONString(request.getContentTypes()));
                    map.put("distribution", Integer.toString(request.getDistribution()));
                    map.put("testScene", Integer.toString(request.getTestScene()));
                    map.put("gradeType", Integer.toString(request.getGradeType()));
                    map.put("isEvaluation", Integer.toString(request.isEvaluation));
                    if (null != request.getApplyStartAt())
                        map.put("applyStartAt", sdf.format(request.getApplyStartAt()));
                    if (null != request.getApplyStopAt())
                        map.put("applyStopAt", sdf.format(request.getApplyStopAt()));

                    // 去空
                    Map<Object, Object> _map = Maps.newHashMap();
                    map.entrySet().stream()
                            .filter(i -> null != i.getValue() && null != i.getKey())
                            .forEach(i -> _map.put(i.getKey(), i.getValue()));
                    return _map;
                } catch (Exception e) {
                    throw new BusinessException(ErrorCode.EXAM_CREATE, e);
                }
            }
        }
    }

    /**
     * 申请响应
     */
    @Data
    class ApplyResponse implements Serializable {
        private boolean success;
        private String info;
        private String data;
    }

    /**
     * 上线请求
     */
    @Data
    class OnlineRequest implements Serializable {
        private String examId;
        private boolean online;

        private OnlineRequest(String examId) {
            this.examId = examId;
            online = true;
        }

        public static class Builder {
            public static OnlineRequest build(String examId) {
                return new OnlineRequest(examId);
            }
        }
    }

    @Data
    class OnlineResponse implements Serializable {
        private boolean success;
        private String info;
        private String data;
    }


    /**
     * 下线请求
     */
    @Data
    class OfflineRequest implements Serializable {
        private String examId;
        private boolean online;

        private OfflineRequest(String examId) {
            this.examId = examId;
            this.online = false;
        }

        public static class Builder {
            public static OfflineRequest build(String examId) {
                return new OfflineRequest(examId);
            }
        }
    }

    /**
     * 下线响应
     */
    @Data
    class OfflineResponse implements Serializable {
        private boolean success;
        private String examId;
        private String errorMessage;
    }

    /**
     * 重考、补考请求
     */
    @Data
    class RetryRequest implements Serializable {
        private List<Long> studentIds;     // 学生ID
        private String examId;      // 考试ID
        private Boolean makeUp;     // true:补考,false:重考
    }

    @Data
    class Score implements Serializable {
        private String studentId;
        private String studentName;
        private String province;
        private String city;
        private String school;
        private String className;
        private Date submitAt;
        private Date startAt;
        private String score;
    }

    @Data
    class WithdrawRequest implements Serializable {
        private String businessId;

        private WithdrawRequest(String id) {
            this.businessId = id;
        }

        public static class Builder {
            public static WithdrawRequest build(String planId) {
                return new WithdrawRequest(planId);
            }
        }
    }

    @Data
    class WithdrawResponse implements Serializable {
        private boolean success;
        private String info;
    }

}
