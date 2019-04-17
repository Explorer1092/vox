package com.voxlearning.utopia.service.psr.impl.termreport.service;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.psr.entity.termreport.*;
import com.voxlearning.utopia.service.psr.impl.dao.termreport.termreportDailyDao;
import com.voxlearning.utopia.service.psr.impl.dao.termreport.termreportDailyDao_before20170901;
import com.voxlearning.utopia.service.psr.impl.dao.termreport.termreportMonthDao;
import com.voxlearning.utopia.service.psr.impl.dao.termreport.termreportMonthDao_before20170901;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by mingming.zhao on 2016/10/20.
 */
@Slf4j
@Named
public class PsrTermReportIPackageService extends SpringContainerSupport {
    @Inject private termreportDailyDao termreportdailyDao;
    @Inject private termreportMonthDao termreportmonthDao;
    @Inject private termreportDailyDao_before20170901 termreportDailyDao_before20170901;
    @Inject private termreportMonthDao_before20170901 termreportMonthDao_before20170901;
    private static final  GroupUnitReportPackage defaultRes = new GroupUnitReportPackage();


    private List<termReportDaily> loadEachGroupUnitReportData(Integer groupId, String unitId) {
        // <groupId, unitId> is the key of table 'term_report_daily'
        // If we can find it in termreportdailyDao, we return result
        // Or we search it in termreportDaily_before_20170901
        List<termReportDaily> termreportDaily = termreportdailyDao.getStatisticsResultByGroupAndUnitid(unitId, groupId);
        if (! CollectionUtils.isEmpty(termreportDaily)) {
            return termreportDaily;
        }
        List<termReportDaily> termreportDaily_before_20170901 = termreportDailyDao_before20170901.getStatisticsResultByGroupAndUnitid(unitId, groupId);
        return termreportDaily_before_20170901;
    }
    public GroupUnitReportPackage loadGroupUnitReportPackage(Integer groupId, String unitId) {
        // List<termReportDaily> termreportResultList = termreportdailyDao.getStatisticsResultByGroupAndUnitid(unitId, groupId);
        List<termReportDaily> termreportResultList = loadEachGroupUnitReportData( groupId, unitId);
        if (CollectionUtils.isEmpty(termreportResultList)) return defaultRes;
        int index = 0;
        for (int i = 0; i < termreportResultList.size(); i++) {
            if (termreportResultList.get(i).getUpdatedAt().getTime() > termreportResultList.get(index).getUpdatedAt().getTime()) {
                index = i;
            }
        }
        GroupUnitReportPackage res = new GroupUnitReportPackage();
        res.setLayoutHomeworkTimes(Integer.parseInt(termreportResultList.get(index).getLayout_times()));
        res.setStudentGroupUnitReport(termreportResultList.get(index).getStudent_infos());
        return res;
    }

    private Set<String>getMonthsInfo(Integer yearId, Integer termId) {
        Set<String>months = new HashSet<String>();
        String startMonth = "";
        if (termId == 0) {
            startMonth = yearId + "-08";
        } else if (termId == 1) {
            yearId += 1;
            startMonth = yearId +"-02";
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
        try {
            for (int i = 0; i < 7; i++) {
                Date d1 = df.parse(startMonth);
                Calendar g = Calendar.getInstance();
                g.setTime(d1);
                g.add(Calendar.MONTH,  i);
                Date d2 = g.getTime();
                months.add(df.format(d2));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return months;
    }
    private List<termReportMonth> loadEachTermReportMonthData(Integer groupId, String subjectId, Set<String>months) {
        // In case there is duplicated data in 2 tables, wo should only keep one result for each key <groupId, month, subjectId>
        List<termReportMonth> termMonths = termreportmonthDao.getStatisticsResultByMonth(groupId, subjectId, months);
        List<termReportMonth> termMonths_before_20170901 = termreportMonthDao_before20170901.getStatisticsResultByMonth(groupId, subjectId, months);
        for (termReportMonth eachReport : termMonths_before_20170901) {
            boolean isDuplicated = false;
            for (termReportMonth sourceReport : termMonths) {
                if (eachReport.getGroupId().trim().equals(sourceReport.getGroupId().trim())
                        && eachReport.getMonth().trim().equals(sourceReport.getMonth().trim())
                        && eachReport.getSubject().trim().equals(sourceReport.getSubject().trim())
                        ) {
                    isDuplicated = true;
                    break;
                }
            }
            if (isDuplicated) continue;
            termMonths.add(eachReport);
        }
        return termMonths;
    }
    //年份，学期0上学期，1下学期, group_id, subjectId
    public TermReportPackage loadTermtReportPackage(Integer yearId, Integer termId, Integer groupId, String subjectId) {
        Set<String>months = getMonthsInfo(yearId, termId);
        HashMap<String, StudentTermReport>StudentTermReports = new HashMap<String, StudentTermReport>(); //所有学生的学期报告
        HashMap<String, MonthLayoutInfo>monthLayoutInfos = new HashMap<String ,MonthLayoutInfo>();  // 学期老师每个月布置作业情况
        // List<termReportMonth> termMonths = termreportmonthDao.getStatisticsResultByMonth(groupId, subjectId, months);
        List<termReportMonth> termMonths = loadEachTermReportMonthData(groupId, subjectId,months);
        long alltimes = 0;
        for (termReportMonth info : termMonths) {
            if (months.contains(info.getMonth())) {
                if (monthLayoutInfos.containsKey(info.getMonth()) == false) {
                    monthLayoutInfos.put(info.getMonth(), new MonthLayoutInfo());
                    monthLayoutInfos.get(info.getMonth()).setMonth(info.getMonth());
                    monthLayoutInfos.get(info.getMonth()).setLayout_count(info.getLayout_times());
                    alltimes += info.getLayout_times();
                }
                for (student_month_infos student_info : info.getStudent_infos()) {
                    StudentTermReport studenttermreporot = null;
                    if (StudentTermReports.containsKey(student_info.getStudent_id())) {
                        studenttermreporot = StudentTermReports.get(student_info.getStudent_id());
                    } else {
                        studenttermreporot = new StudentTermReport();
                    }
                    studenttermreporot.setStudentId(student_info.getStudent_id());
                    List<MonthDoHomework>homeworkStatus = studenttermreporot.getHomeworkStatus();
                    if (homeworkStatus == null) {
                        homeworkStatus = Lists.newArrayList();
                    }
                    MonthDoHomework monthwork = new MonthDoHomework();
                    monthwork.setMonth(info.getMonth());
                    monthwork.setComplete_count(student_info.getCompleteNums());
                    homeworkStatus.add(monthwork);
                    studenttermreporot.setHomeworkStatus(homeworkStatus);
                    Double avescore = studenttermreporot.getAveScore() == null ? 0.0 : studenttermreporot.getAveScore() ;
                    avescore += student_info.getAvgscores();
                    studenttermreporot.setAveScore(avescore);
                    Integer attendence = studenttermreporot.getAttendTimes() == null ? 0 :studenttermreporot.getAttendTimes() ;
                    attendence += student_info.getCompleteNums();
                    studenttermreporot.setAttendTimes(attendence);
                    Integer has_score_count = studenttermreporot.getHas_score_count()  == null ? 0 : studenttermreporot.getHas_score_count() ;
                    has_score_count += student_info.getHas_score_count();
                    studenttermreporot.setHas_score_count(has_score_count);
                    StudentTermReports.put(student_info.getStudent_id(),studenttermreporot);
                }
            }
        }
        for (String key : StudentTermReports.keySet()) {
            StudentTermReport val = StudentTermReports.get(key);
            if (val.getHas_score_count() != 0.0) {
                Double avescore = val.getAveScore() / val.getHas_score_count();
                if (avescore > 100.0) {
                    avescore = 100.0;
                }
                val.setAveScore(avescore);
            }
            Double attendRate = 0.0;
            if (alltimes != 0) attendRate = ((double)val.getAttendTimes()) / alltimes;
            val.setAttendanceRate(attendRate);
            StudentTermReports.put(key, val);
        }
        TermReportPackage termreportPackage = new TermReportPackage();
        termreportPackage.setStudentTermReports(new ArrayList<StudentTermReport>(StudentTermReports.values()));
        termreportPackage.setMonthLayoutInfos(new ArrayList<MonthLayoutInfo>(monthLayoutInfos.values()));
        return termreportPackage;
    }
    public GroupUnitReportPackage testLoadGroupUnitReportPackage(Integer groupId, String unitId) {
        GroupUnitReportPackage groupUnitReportPackage = new GroupUnitReportPackage();
        groupUnitReportPackage.setLayoutHomeworkTimes(10);
        List<StudentGroupUnitReport> StudentGroupUnitReport = Lists.newArrayList();

        StudentGroupUnitReport a = new StudentGroupUnitReport();
        a.setAttendance_rate(0.92);
        a.setAvgscores(89.2);
        a.setDo_homework_duration(8.2);
        List<String> homeworkids = Lists.newArrayList();
        homeworkids.add("12234234af");
        homeworkids.add("1223423sdfsdf4af");
        homeworkids.add("sdfw2234234af");
        a.setHomeworkids(homeworkids);
        a.setMakeup_num(5);
        a.setNotdone_num(2);
        a.setOntime_num(3);
        a.setStudentId("student1");
        StudentGroupUnitReport.add(a);

        StudentGroupUnitReport b = new StudentGroupUnitReport();
        b.setAttendance_rate(0.3492);
        b.setAvgscores(829.2);
        b.setDo_homework_duration(83.2);
        homeworkids = Lists.newArrayList();
        homeworkids.add("12we234234af");
        homeworkids.add("1223re423sdfsdf4af");
        homeworkids.add("sdfw2234234a34f");
        b.setHomeworkids(homeworkids);
        b.setMakeup_num(52);
        b.setNotdone_num(23);
        b.setOntime_num(32);
        b.setStudentId("student2");
        StudentGroupUnitReport.add(b);

        StudentGroupUnitReport c = new StudentGroupUnitReport();
        c.setAttendance_rate(0.3492);
        c.setAvgscores(829.2);
        c.setDo_homework_duration(83.2);
        homeworkids = Lists.newArrayList();
        homeworkids.add("12we2sf34234af");
        homeworkids.add("1223re423sdsdfsdf4af");
        homeworkids.add("sfdfw22342f34a34f");
        c.setHomeworkids(homeworkids);
        c.setMakeup_num(52);
        c.setNotdone_num(23);
        c.setOntime_num(32);
        c.setStudentId("student2");
        StudentGroupUnitReport.add(c);
        return groupUnitReportPackage;
    }


    public TermReportPackage testLoadTermtReportPackage(Integer yearId, Integer termId, Integer groutId, String subjectId) {
        TermReportPackage termreportPackage = new TermReportPackage();
        List<StudentTermReport>StudentTermReports =  Lists.newArrayList();
        StudentTermReport a = new StudentTermReport();
        a.setAttendanceRate(0.93);
        a.setAveScore(96.2);
        a.setStudentId("student3");
        List<MonthDoHomework> month =  Lists.newArrayList();
        MonthDoHomework m1 = new MonthDoHomework();
        m1.setComplete_count(2);
        // m1.setLayout_count(10);
        m1.setMonth("9");
        month.add(m1);
        MonthDoHomework m2 = new MonthDoHomework();
        m2.setComplete_count(12);
        // m2.setLayout_count(130);
        m2.setMonth("8");
        month.add(m2);
        a.setHomeworkStatus(month);
        StudentTermReports.add(a);

        StudentTermReport b = new StudentTermReport();
        b.setAttendanceRate(0.943);
        b.setAveScore(96.22);
        b.setStudentId("student23");
        List<MonthDoHomework> month1 =  Lists.newArrayList();
        MonthDoHomework m11 = new MonthDoHomework();
        m11.setComplete_count(22);
        // m11.setLayout_count(103);
        m11.setMonth("93");
        month1.add(m11);
        MonthDoHomework m22 = new MonthDoHomework();
        m22.setComplete_count(132);
        // m22.setLayout_count(1310);
        m22.setMonth("83");
        month1.add(m22);
        b.setHomeworkStatus(month1);
        StudentTermReports.add(b);

        termreportPackage.setStudentTermReports(StudentTermReports);
        return termreportPackage;
    }
}
