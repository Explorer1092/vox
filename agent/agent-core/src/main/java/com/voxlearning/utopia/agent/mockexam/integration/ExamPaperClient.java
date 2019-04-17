package com.voxlearning.utopia.agent.mockexam.integration;

import com.alibaba.fastjson.JSONArray;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanEntity;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPaper;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPaperProcessState;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamPlan;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPaperEnums;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamPaperQueryParams;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 试卷创建客户端
 *
 * @author xiaolei.li
 * @version 2018/8/4
 */
public interface ExamPaperClient {

    /**
     * 申请创建新试卷
     *
     * @param request 请求
     * @return 响应
     */
    CreateResponse create(CreateRequest request);

    /**
     * 试卷有效性校验
     *
     * @param plan 计划
     * @return 响应
     */
    CheckResponse check(ExamPlan plan);

    /**
     * 查询成绩
     *
     * @param request 请求
     * @return
     */
    PaperPageResponse queryPage(PaperRequest request);


    /**
     * 根据试卷id（多个）获取试卷详情
     *
     * @param subject  学科
     * @param paperIds 试卷id列表
     * @return (paperId, paperInfo)
     */
    Map<String, PaperInfo> queryByIds(ExamPlanEnums.Subject subject, List<String> paperIds);

    /**
     * 请求
     */
    @Data
    class CreateRequest implements Serializable {

        /**
         * 测评计划id
         */
        private String businessId;

        /**
         * 测评计划名称
         */
        private String name;

        /**
         * 创建人名称
         */
        private String creatorName;

        /**
         * 学科，例如：小学数学、小学语文、小学英语
         */
        private Integer subject;

        /**
         * 年级，例如：一年级、二年级。。。六年级
         */
        private Integer grade;

        /**
         * 测评类型,例如：普通、口语、听力
         */
        private String type;

        /**
         * 申请时间，格式yyyyMMdd HH:mm:ss
         */
        private String createDate;

        /**
         * 考试开始时间，格式yyyyMMdd HH:mm:ss
         */
        private String examStartDate;

        /**
         * 考试结束时间，格式yyyyMMdd HH:mm:ss
         */
        private String examStopDate;

        /**
         * 成绩发布时间，格式yyyyMMdd HH:mm:ss
         */
        private String scorePublishDate;

        /**
         * 关联教材id
         */
        private String[] bookIds;

        /**
         * 总分
         */
        private Integer score;


        /**
         * 试卷
         */
        private List<Paper> papers;

        /**
         * 区域
         */
        private List<String> regions;

        /**
         * 试卷文档模型
         */
        @Data
        public static class Paper implements Serializable {

            /**
             * 试卷id
             */
            private String paperId;

            /**
             * 试卷名称
             */
            private String title;

            /**
             * 试卷文档url
             */
            private String docUrl;
        }

        public static class Builder {

            public static CreateRequest build(ExamPlanEntity plan) {
                CreateRequest request = new CreateRequest();
                request.setBusinessId(plan.getId().toString());
                request.setName(plan.getName());
                request.setCreatorName(plan.getCreatorName());
                request.setGrade(ExamPlanEnums.Grade.valueOf(plan.getGrade()).clazzLevel.getLevel());
                request.setSubject(ExamPlanEnums.Subject.valueOf(plan.getSubject()).subject.getId());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                Date now = new Date();
                request.setCreateDate(sdf.format(now));
                request.setExamStartDate(sdf.format(plan.getStartTime()));
                request.setExamStopDate(sdf.format(plan.getEndTime()));
                request.setScorePublishDate(sdf.format(plan.getScorePublishTime()));
                request.setBookIds(new String[]{plan.getBookId()});
                request.setScore(plan.getTotalScore());
                List<ExamPlan.Paper> papers = JSONArray.parseArray(plan.getPapers(), ExamPlan.Paper.class);
                request.setPapers(papers.stream().map(i -> {
                    Paper j = new Paper();
                    j.setPaperId(i.getPaperId());
                    j.setDocUrl(i.getDocUrl());
                    j.setTitle(i.getDocTitle());
                    return j;
                }).collect(Collectors.toList()));

                request.setRegions(Arrays.asList(plan.getRegionCodes().split(",")));
                return request;
            }

            public static CreateRequest build(ExamPaperProcessState paper) {
                CreateRequest request = new CreateRequest();
                request.setBusinessId(paper.getId().toString());
                request.setPapers(paper.getPapers().stream().map(i -> {
                    Paper j = new Paper();
                    j.setDocUrl(i.getDocUrl());
                    j.setPaperId(i.getPaperId());
                    j.setTitle(i.getTitle());
                    return j;
                }).collect(Collectors.toList()));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                Date now = new Date();
                String dateStr = sdf.format(now);
                request.setCreateDate(dateStr);
                return request;
            }

            public static CreateRequest buildBoth(ExamPlanEntity oldPlan,ExamPlanEntity newPlan) {
                CreateRequest request = new CreateRequest();
                request.setBusinessId(oldPlan.getId().toString());
                request.setName(newPlan.getName());
                request.setCreatorName(newPlan.getCreatorName());
                request.setGrade(ExamPlanEnums.Grade.valueOf(newPlan.getGrade()).clazzLevel.getLevel());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                Date now = new Date();
                request.setCreateDate(sdf.format(now));
                request.setExamStartDate(sdf.format(newPlan.getStartTime()));
                request.setExamStopDate(sdf.format(newPlan.getEndTime()));
                request.setScorePublishDate(sdf.format(newPlan.getScorePublishTime()));
                request.setBookIds(new String[]{newPlan.getBookId()});
                request.setRegions(Arrays.asList(newPlan.getRegionCodes().split(",")));


                // 以下字段不能修改,使用初始值
                request.setSubject(ExamPlanEnums.Subject.valueOf(oldPlan.getSubject()).subject.getId());
                request.setType(oldPlan.getType());
                request.setScore(oldPlan.getTotalScore());

                // 试卷审核中【只有采用新试卷的情况下才会有该状态】，允许修改试卷文档及总分数
                List<ExamPlan.Paper> papers = JSONArray.parseArray(oldPlan.getPapers(), ExamPlan.Paper.class);
                if(ExamPlanEnums.Status.valueOf(oldPlan.getStatus()) == ExamPlanEnums.Status.PAPER_CHECKING){
//                    papers = JSONArray.parseArray(newPlan.getPapers(), ExamPlan.Paper.class);
                    request.setScore(newPlan.getTotalScore());
                }
                request.setPapers(papers.stream().map(i -> {
                    Paper j = new Paper();
                    j.setPaperId(i.getPaperId());
                    j.setDocUrl(i.getDocUrl());
                    j.setTitle(i.getDocTitle());
                    return j;
                }).collect(Collectors.toList()));
                return request;
            }
        }
    }

    /**
     * 响应
     */
    @Data
    class CreateResponse implements Serializable {

        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 错误码
         */
        private String errorCode;

        /**
         * 错误信息
         */
        private String errorMessage;

        /**
         * 数据
         */
        private ProcessState data;

        public static CreateResponse success(ProcessState state) {
            CreateResponse response = new CreateResponse();
            response.success = true;
            response.data = state;
            return response;
        }

        public static CreateResponse error(BusinessException e) {
            CreateResponse response = new CreateResponse();
            response.success = false;
            response.errorCode = e.getErrorCode().code;
            response.errorMessage = e.getErrorCode().desc;
            return response;
        }

        public static CreateResponse error(Exception e) {
            CreateResponse response = new CreateResponse();
            response.success = false;
            response.errorCode = ErrorCode.UNKNOWN.code;
            response.errorMessage = e.getMessage();
            return response;
        }

    }

    /**
     * 试卷结果响应
     */
    @Data
    class PaperPageResponse implements Serializable {
        public static final Integer EMPTY = 0;
        public static final String NOT_PUBLIC = "N";
        public static final String NO_REGION = "无";
        private List<PaperInfo> items;
        private Integer page;
        private Integer per_page;
        private boolean success;
        private Integer total;

        public static class Builder {
            public static List<ExamPaper> build(PaperPageResponse response) {
                if (response.isSuccess() && null != response.getItems()) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return response.getItems().stream().map(i -> {
                        ExamPaper paper = new ExamPaper();
                        paper.setPaperId(i.get_id());
                        paper.setPaperName(i.getTitle());
                        JSONArray jsonArray = JSONArray.parseArray(i.getRegions());
                        List<Integer> regionCodes = new ArrayList<>();
                        List<String> regionName = new ArrayList<>();
                        if (jsonArray.size() != 0) {
                            jsonArray.forEach(p -> {
                                JSONArray item = (JSONArray)p;
                                Integer regionCode = SafeConverter.toInt(item.get(0));
                                if(regionCode > 0){
                                    regionCodes.add(regionCode);
                                    regionName.add(SafeConverter.toString(item.get(1), ""));
                                }
                            });
                            paper.setRegionCodes(regionCodes);
                            paper.setRegion(StringUtils.join(regionName, ","));
                        } else {
                            paper.setRegionCodes(regionCodes);
                            paper.setRegion(NO_REGION);
                        }
                        paper.setSource(ExamPaperClient.PaperInfo.UsageType.typeOf(i.getUsage()).desc);
                        paper.setTopicNum(i.getTotal_num());
                        paper.setTotalScore(i.getTotal_score());
                        paper.setStatus(i.getStatus_text());
                        if (null != i.getBook_nodes() && !i.getBook_nodes().isEmpty()) {
                            // 取第一个教材
                            PaperInfo.BookNode bookNode = i.getBook_nodes().get(0);
                            Integer subject_id = bookNode.getSubject_id();
                            paper.setSubject(ExamPlanEnums.Subject.of(subject_id));
                            paper.setBookId(bookNode.getSeries_id());
                            paper.setBookName(bookNode.getFull_name());
                        }
                        try {
                            paper.setCreateDatetime(formatter.parse(i.getCreated_at()));
                        } catch (ParseException e) {
                        }
                        paper.setType(i.getOl_status_text());
                        if (i.getCreator() != null) {
                            paper.setCreator(i.getCreator().getName());
                        }
                        paper.setPartTypes(ExamPaperEnums.PartType.of(Optional.ofNullable(i.getParts()).orElse(Collections.emptyList())
                                .stream()
                                .map(PaperInfo.PartNote::getPart_type)
                                .collect(Collectors.toList())));
                        paper.setBookIds(Optional.ofNullable(i.getBook_nodes()).orElse(new ArrayList<>())
                                .stream()
                                .map(PaperInfo.BookNode::getBook_id)
                                .collect(Collectors.toList()));
                        return paper;
                    }).collect(Collectors.toList());
                } else {
                    throw new BusinessException(ErrorCode.PAPER_QUERY_ERROR, "查询试卷报错");
                }
            }
        }
    }

    @Data
    class PaperInfo implements Serializable {

        private String _id;                            // 试卷id
        private Integer attachments_num;
        private boolean could_delete;
        private boolean could_kick_question;
        private boolean could_lock;
        private boolean could_offline;
        private boolean could_online;
        private boolean could_unlock;
        private String created_at;                     // 创建时间
        private Creator creator;                       // 创建人信息
        private Integer current_num;
        private String date;
        private String from_researcher;
        private boolean is_default;
        private LastEditor lastEditor;
        private Integer minutes;
        private Integer ol_status;
        private String ol_status_text;                 // 测评状态
        private String paper_types;
        private String paper_types_text;
        private String provider_id;
        private String regions;                        // 区域
        private String status_text;                    // 试卷状态
        private String title;                          // 试卷名称
        private Integer total_num;                     // 题数
        private Integer total_score;                   // 总分
        private Integer usage;                         // 试卷来源
        private List<BookNode> book_nodes;
        private List<PartNote> parts;                //模块类型

        @Data
        class LastEditor implements Serializable {
            private String _id;
            private String name;
        }

        @Data
        class Creator implements Serializable {
            private String _id;
            private String name;
        }

        @Data
        class BookNode implements Serializable {
            private String book_id;
            private String series_id;
            private Integer subject_id;
            private Integer class_level;
            private String full_name;
        }

        @Data
        class PartNote implements Serializable {
            private String part_type;
        }

        @AllArgsConstructor
        public enum UsageType {
            General(0, "通用"),
            LOCAL(1, "本地化"),
            O2O(2, "O2O"),
            SELF_MAKE(3, "自制试卷");

            public final Integer code;
            public final String desc;

            private static Map<Integer, UsageType> subjectMap;

            static {
                subjectMap = new HashMap<>();
                for (UsageType type : UsageType.values()) {
                    subjectMap.put(type.code, type);
                }
            }

            public static UsageType typeOf(Integer id) {
                if (id == null) {
                    return null;
                }
                return subjectMap.get(id);
            }
        }

    }

    /**
     * 试卷请求
     */
    @Data
    class PaperRequest implements Serializable {
        private String paperId;                 // 试卷ID
        private String paperName;               // 试卷名称
        private ExamPlanEnums.Subject subject;               // 所属学科
        private String bookId;                  // 教材ID
        private String partType;                //模块类型
        private ExamPaperEnums.Source resource;                // 试卷来源
        private String status;                  // 试卷状态
        private String regionCode;              // 所属区域code
        private Integer page;                   // 页数
        private String leftTime;                //创建时间左查询区间 %Y-%m-%d（2018-9-20）
        private String rightTime;               //创建时间右查询区间（2018-9-21）

        interface DefaultValue {

            /**
             * 一起测考试类型为模考,固定为 18
             */
            String DEFAULT_PAPER_TYPE = "18";
            /**
             * 题库默认 page 数,从 1 开始
             */
            Integer DEFAULT_PAGE = 1;

            /**
             * 30 代表已发布(题库端草稿库状态)
             */
            String DEFAULT_DRAFT_STATUS = "30";

            /**
             * 1 代表已上线(题库端线上库状态)
             */
            String DEFAULT_PAPER_STATUS = "1";

        }

        public static class Builder {

            public static Map<Object, Object> build(PaperRequest request) {
                Map<Object, Object> map = new HashMap<>();
                map.put("paper_id", setDefault(request.getPaperId()));
                map.put("title", setDefault(request.getPaperName()));
                if (null != request.getResource())
                    map.put("usage", request.getResource().code);
                map.put("region_node_id", setDefault(request.getRegionCode()));
                if (null != request.getSubject()) {
                    map.put("subject_id", Integer.toString(request.getSubject().subject.getId()));
                    if(StringUtils.isBlank(request.getPaperId())){
                        if(request.getSubject() == ExamPlanEnums.Subject.MATH){
                            map.put("left_time", "2018-09-20");
                        }else if(request.getSubject() == ExamPlanEnums.Subject.ENGLISH || request.getSubject() == ExamPlanEnums.Subject.CHINESE){
                            map.put("left_time", "2017-12-01");
                        }
                    }
                }
                map.put("book_node_id", setDefault(request.getBookId()));
                map.put("paper_status", DefaultValue.DEFAULT_DRAFT_STATUS);
                map.put("page", request.getPage() == null ? DefaultValue.DEFAULT_PAGE : request.getPage());
                map.put("paper_type", DefaultValue.DEFAULT_PAPER_TYPE);
                map.put("ol_status", DefaultValue.DEFAULT_PAPER_STATUS);
                map.put("part_type", Objects.isNull(request.getPartType()) ? StringUtils.EMPTY: request.getPartType());
                if (StringUtils.isNotBlank(request.getLeftTime())) {
                    map.put("left_time", request.getLeftTime());
                }
                if (StringUtils.isNotBlank(request.getRightTime())) {
                    map.put("right_time", request.getRightTime());
                }
                return map;
            }

            private static String setDefault(String string) {
                return StringUtils.isEmpty(string) ? "" : string;
            }

            public static PaperRequest build(ExamPaperQueryParams params) {
                PaperRequest request = new PaperRequest();
                BeanUtils.copyProperties(params, request);
                request.setResource(ExamPaperEnums.Source.nameOf(params.getSource()));
                request.setSubject(ExamPlanEnums.Subject.of(params.getSubject()));
                if (StringUtils.isNotBlank(params.getUsageMonth())) {
                    Long monthLong = DateUtils.stringToDate(params.getUsageMonth(), "yyyy-MM").getTime();
                    String leftTime = DateUtils.dateToString(MonthRange.newInstance(monthLong).getStartDate(), DateUtils.FORMAT_SQL_DATE);
                    String rightTime = DateUtils.dateToString(MonthRange.newInstance(monthLong).getEndDate(), DateUtils.FORMAT_SQL_DATE);
                    request.setLeftTime(leftTime);
                    request.setRightTime(rightTime);
                }
                return request;
            }
        }
    }

    /**
     * 流程信息
     */
    @Data
    class ProcessState implements Serializable {

        /**
         * 考试计划id
         */
        private String businessId;

        /**
         * 流程id
         */
        private String processId;

        /**
         * 当前状态
         */
        private String state;

        /**
         * 流程状态
         */
        @AllArgsConstructor
        public enum Status {
            CHECKING("待审核"),
            REJECT("驳回"),
            PROCESSING("录入中"),
            DONE("完成"),;
            public final String desc;
        }
    }

    /**
     * 检查请求
     */
    @Data
    class CheckRequest implements Serializable {
        /**
         * 测评系统业务主键，用于会话关联
         */
        private String businessId;
        /**
         * 年级，小学1 ~ 6年级：1、2、3、4、5、6
         *
         * @see com.voxlearning.alps.annotation.meta.Grade
         */
        private int grade;
        /**
         * 学科,小学数学：102，小学语文：101，小学英语：103,
         *
         * @see com.voxlearning.alps.annotation.meta.Subject
         */
        private int subject;
        /**
         * 教材
         */
        private String bookIds;
        /**
         * 试卷
         */
        private List<String> paperIds;

        /**
         * 试卷分数
         */
        private int score;

        public static class Builder {
            public static CheckRequest build(ExamPlan plan) {
                CheckRequest request = new CheckRequest();
//                request.setScore(plan.getTotalScore());
                if (null != plan.getId())
                    request.setBusinessId(plan.getId().toString());
                else
                    request.setBusinessId(UUID.randomUUID().toString().replaceAll("-", "").toLowerCase());
                if (null != plan.getBook())
                    request.setBookIds(plan.getBook().getId());
                if (null != plan.getSubject())
                    request.setSubject(plan.getSubject().subject.getId());
                if (null != plan.getGrade())
                    request.setGrade(plan.getGrade().clazzLevel.getLevel());
                if (null != plan.getPapers())
                    request.setPaperIds(plan.getPapers().stream().map(ExamPlan.Paper::getPaperId).collect(Collectors.toList()));
                return request;
            }
        }
    }

    @Data
    class CheckResponse implements Serializable {
        /**
         * 检查结果
         */
        private boolean success;
        /**
         * 测评系统业务主键，用于会话关联
         */
        private String businessId;
        /**
         * 试卷
         */
        private List<String> paperIds;
        /**
         * 错误信息
         */
        private String errorMessage;

        public static CheckResponse error(CheckRequest request, Exception e) {
            CheckResponse response = new CheckResponse();
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage());
            return response;
        }
    }
}
