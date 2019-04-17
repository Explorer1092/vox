package com.voxlearning.utopia.service.newhomework.impl.template.internal;


import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.entity.SemesterStudentReport;
import com.voxlearning.utopia.service.newhomework.api.entity.SemesterStudentReportFormData;
import com.voxlearning.utopia.service.newhomework.api.mapper.SemesterReport;
import com.voxlearning.utopia.service.newhomework.impl.athena.TermReviewLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.dao.SemesterStudentReportDao;
import com.voxlearning.utopia.service.newhomework.impl.template.FetchStudentSemesterReportTemple;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

abstract class FetchStudentSemesterReportBaseTemple implements FetchStudentSemesterReportTemple {

    @Inject private TermReviewLoaderClient termReviewLoaderClient;

    @Inject
    private SemesterStudentReportDao semesterStudentReportDao;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @Override
    public SemesterReport doFetchSemesterReport(Long studentId, String subject) {

        SemesterReport semesterReport = new SemesterReport();
        String id = studentId + "_" + subject;
        SemesterStudentReport semesterStudentReport = semesterStudentReportDao.load(id);
        if (semesterStudentReport == null) {
            return semesterReport;
        } else {
            if (semesterStudentReport.getAssign_hw_num() != null) {
                doSemesterHomeworkInformationPart(semesterReport, semesterStudentReport);//作业部分
            }
            doSemesterStudentReportFormDataPart(semesterReport, semesterStudentReport, Subject.of(subject));//作业模块部分
            if (!StringUtils.isBlank(semesterStudentReport.getBook_id())) {
                doStudentBookInfoPart(semesterReport, semesterStudentReport);//课本信息部分
                doWrongQuestionStatisticsForUnitDataPart(semesterReport, semesterStudentReport, studentId, subject);//错题信息
            }
        }
        return semesterReport;
    }


    //错题信息模块
    private void doWrongQuestionStatisticsForUnitDataPart(SemesterReport semesterReport, SemesterStudentReport semesterStudentReport, Long studentId, String subject) {
        String bookId = semesterStudentReport.getBook_id();
        Map<String, Map<String, List<Map<String, Object>>>> wrongQuestionIds
                = termReviewLoaderClient.getTermReviewLoader().loadStudentTermWrongQuestionIds(studentId, Subject.of(subject));
        if (wrongQuestionIds != null && wrongQuestionIds.containsKey(bookId)) {
            Map<String, List<Map<String, Object>>> unitWrongQuestion = wrongQuestionIds.get(bookId);
            if (unitWrongQuestion != null) {
                semesterReport.setWrongQuestionStatisticsForUnitDatas(new LinkedList<>());
                Long wrongQuestionNum = unitWrongQuestion.values().stream().flatMap(Collection::stream).count();
                semesterReport.setWrongQuestionNum(wrongQuestionNum);
                List<Map<String, Object>> da = new LinkedList<>();
                unitWrongQuestion.forEach((key, l) -> da.add(
                        MiscUtils.m("unitId", key,
                                "size", l.size()
                        )));

                Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitWrongQuestion.keySet());
                for (Map<String, Object> m : da) {
                    SemesterReport.WrongQuestionStatisticsForUnitData wrongQuestionStatisticsForUnitData = new SemesterReport.WrongQuestionStatisticsForUnitData();
                    wrongQuestionStatisticsForUnitData.setUnitId(SafeConverter.toString(m.get("unitId")));
                    wrongQuestionStatisticsForUnitData.setWrongQuestionNum(SafeConverter.toInt(m.get("size")));
                    if (newBookCatalogMap.containsKey(m.get("unitId").toString())) {
                        NewBookCatalog n = newBookCatalogMap.get(m.get("unitId").toString());
                        Integer unitRank = SafeConverter.toInt(n.getRank());
                        wrongQuestionStatisticsForUnitData.setUnitRank(unitRank);
                    }
                    semesterReport.getWrongQuestionStatisticsForUnitDatas().add(wrongQuestionStatisticsForUnitData);
                }
                semesterReport.getWrongQuestionStatisticsForUnitDatas().sort((o1, o2) -> Integer.compare(o1.getUnitRank(), o2.getUnitRank()));
            }
        }
    }

    //课本信息部分
    private void doStudentBookInfoPart(SemesterReport semesterReport, SemesterStudentReport semesterStudentReport) {

        SemesterReport.StudentBookInfo studentBookInfo = new SemesterReport.StudentBookInfo();
        String bookId = semesterStudentReport.getBook_id();//课本ID
        String bookName = null;//课本名字
        String bookBrief = semesterStudentReport.getContent();//课本知识点等简介
        NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(bookId);


        if (newBookCatalog != null) {
            bookName = getBookName(newBookCatalog);
        }
        studentBookInfo.setBookId(bookId);
        studentBookInfo.setBookBrief(bookBrief);
        studentBookInfo.setBookName(SafeConverter.toString(bookName, ""));
        semesterReport.setStudentBookInfo(studentBookInfo);//课本模块
    }

    protected abstract String getBookName(NewBookCatalog newBookCatalog);


    private static Map<String, String> englishMap = new HashMap<>();
    private static Map<String, String> mathMap = new HashMap<>();

    static {
        englishMap.put("基础练习", "听说练习");
        englishMap.put("同步习题", "读写练习");
        englishMap.put("配套试卷", "单元测试");
        mathMap.put("同步习题", "同步习题");
        mathMap.put("配套试卷", "单元测试");
    }


    //作业模块部分
    private void doSemesterStudentReportFormDataPart(SemesterReport semesterReport, SemesterStudentReport semesterStudentReport, Subject subject) {
        if (CollectionUtils.isNotEmpty(semesterStudentReport.getFormdata())) {
            List<SemesterStudentReportFormData> semesterStudentReportFormData = semesterStudentReport.getFormdata()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(o -> JsonUtils.fromJson(o, SemesterStudentReportFormData.class))
                    .filter(Objects::nonNull)
                    .filter(o -> {
                        if (subject == Subject.MATH) {
                            if (mathMap.containsKey(o.getHomework_form_name())) {
                                o.setHomework_form_name(mathMap.get(o.getHomework_form_name()));
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            if (englishMap.containsKey(o.getHomework_form_name())) {
                                o.setHomework_form_name(englishMap.get(o.getHomework_form_name()));
                                return true;
                            } else {
                                return false;
                            }
                        }
                    })
                    .collect(Collectors.toList());
            semesterReport.setSemesterStudentReportFormData(semesterStudentReportFormData);
        }
    }

    //作业部分
    private void doSemesterHomeworkInformationPart(SemesterReport semesterReport, SemesterStudentReport semesterStudentReport) {
        //作业成绩模块
        SemesterReport.SemesterHomeworkInformation semesterHomeworkInformation = new SemesterReport.SemesterHomeworkInformation();
        Integer assign_hw_num = semesterStudentReport.getAssign_hw_num();    //布置作业数
        Integer finishedHomeworkNum = SafeConverter.toInt(semesterStudentReport.getFinish_hw_num());
        Integer unFinishedHomeworkNum = assign_hw_num - finishedHomeworkNum;

        Integer avgScore = SafeConverter.toInt(semesterStudentReport.getAvg_score());//学生学期成绩
        Integer clazzAvgScore = SafeConverter.toInt(semesterStudentReport.getGrp_avg_score());//班级成绩
        Integer clazzMaxScore = SafeConverter.toInt(semesterStudentReport.getGrp_max_score());//班级最高成绩
        String description;//评语
        if (avgScore >= clazzAvgScore) {
            description = "宝贝表现不错，是否达到您心中的期望？";
        } else {
            description = "没关系，距离期末考试还有一段时间，继续努力，实现华丽逆转！";
        }
        semesterHomeworkInformation.setFinishedHomeworkNum(finishedHomeworkNum);
        semesterHomeworkInformation.setUnFinishedHomeworkNum(unFinishedHomeworkNum);
        semesterHomeworkInformation.setAvgScore(avgScore);
        semesterHomeworkInformation.setClazzAvgScore(clazzAvgScore);
        semesterHomeworkInformation.setClazzMaxScore(clazzMaxScore);
        semesterHomeworkInformation.setDescription(description);
        semesterReport.setSemesterHomeworkInformation(semesterHomeworkInformation);
    }
}
