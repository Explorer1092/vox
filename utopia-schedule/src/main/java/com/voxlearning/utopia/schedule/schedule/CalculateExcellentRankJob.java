package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareConstants;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import com.voxlearning.utopia.service.campaign.cache.TeacherCourseCache;
import com.voxlearning.utopia.service.campaign.client.TeacherCoursewareContestServiceClient;
import com.voxlearning.utopia.service.campaign.helper.DynamicRankingHelper;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareConstants.RANKING_START_DATE;

/**
 * @Author: peng.zhang
 * @Date: 2018/10/30
 */
@Named
@ScheduledJobDefinition(
        jobName = "计算优秀作品排行榜任务",
        jobDescription = "计算分数存入缓存",
        disabled = {Mode.TEST, Mode.STAGING},
        cronExpression = "0 1 0 * * ? ")
public class CalculateExcellentRankJob extends ScheduledJobWithJournalSupport {

    @Inject
    private TeacherCoursewareContestServiceClient teacherCoursewareContestServiceClient;

    @Inject protected TeacherLoaderClient teacherLoaderClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        Date curTime = new Date();
        Date endDate = DateUtils.stringToDate("2018-12-23 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
        if (curTime.after(endDate)) {
            return;
        }

        List<TeacherCourseware> allCourseList = teacherCoursewareContestServiceClient.loadTeacherCoursewareAll();

        boolean force = SafeConverter.toBoolean(parameters.get("force"));
        Date runDate = null;
        if (StringUtils.isNoneBlank(SafeConverter.toString(parameters.get("date")))) {
            runDate = DateUtils.stringToDate(SafeConverter.toString(parameters.get("date")), "yyyyMMdd");
        }

        // 课件大赛的优秀作品榜-总榜
        processTotalRank(allCourseList, runDate);

        // 课件大赛的优秀作品榜-周榜
        //processWeeklyRank(allCourseList, force);

        // 课件大赛的优秀作品榜-月榜
        //processMonthlyRank(allCourseList, force);

        // 数据预热，免得第一次加载缓慢
        teacherCoursewareContestServiceClient.loadTop3Ranking();

        teacherCoursewareContestServiceClient.loadPopularityShowInfo();

        teacherCoursewareContestServiceClient.loadTalentShowInfo();
    }

    // 总榜单处理
    private void processTotalRank(List<TeacherCourseware> allCourseList, Date runDate) {
        if (new Date().after(TeacherCoursewareConstants.TOTAL_RANK_END)) {
            return;
        }

        Map<Subject, List<Map<String, Object>>> rankingMap = new HashMap<>();
        rankingMap.put(Subject.ENGLISH, new ArrayList<>());
        rankingMap.put(Subject.MATH, new ArrayList<>());
        rankingMap.put(Subject.CHINESE, new ArrayList<>());

        int maxDownloadNum = 1;

        // 获取下载数据
        Date endDate = DateUtils.getDayEnd(DateUtils.addDays(new Date(), -1));
        if (runDate != null) {
            endDate = DateUtils.getDayEnd(runDate);
        }

        logger.info("running end date is " + endDate);

        List<Map<String, Object>> downloadInfo = teacherCoursewareContestServiceClient.fetchDownloadStatInfo(RANKING_START_DATE, endDate);
        Map<String, Map<String, Object>> downloadInfoMap = new HashMap<>();
        for (Map<String, Object> item : downloadInfo) {
            String courseId = SafeConverter.toString(item.get("COURSEWARE_ID"));
            downloadInfoMap.put(courseId, item);

            int downloadNum = SafeConverter.toInt(item.get("NUM"));
            if (downloadNum > maxDownloadNum) {
                maxDownloadNum = downloadNum;
            }
        }

        // 循环算分
        for (TeacherCourseware courseware : allCourseList) {
            // 过滤错误数据
            if (!rankingMap.containsKey(courseware.getSubject())) {
                continue;
            }

            // 状态
            if (courseware.getExamineStatus() != TeacherCourseware.ExamineStatus.PASSED || SafeConverter.toBoolean(courseware.getDisabled())) {
                continue;
            }

            int downloadScore = 0;
            if (downloadInfoMap.containsKey(courseware.getId())) {
                downloadScore = (int) (SafeConverter.toInt(downloadInfoMap.get(courseware.getId()).get("NUM")) * 10 / maxDownloadNum);
            }

            // 计算得分
            int newScore = SafeConverter.toInt(courseware.getTotalScore()) + downloadScore;
            Map<String, Object> item = new HashMap<>();
            item.put("coursewareId", courseware.getId());

            if (SafeConverter.toInt(courseware.getCommentNum()) >= 3) {
                item.put("score", newScore);
            } else {
                item.put("score", 0);
            }

            item.put("awardLevelName",courseware.getAwardLevelName());
            item.put("teacherId",courseware.getTeacherId());
            item.put("awardLevelId",courseware.getAwardLevelId());
            item.put("title",courseware.getTitle());
            item.put("totalScore",courseware.getTotalScore());
            item.put("coverUrl",courseware.getCoverUrl());
            item.put("createDate",DateUtils.dateToString(courseware.getUpdateTime(), DateUtils.FORMAT_SQL_DATE));
            item.put("downloadNum",courseware.getDownloadNum());
            item.put("commentNum",courseware.getCommentNum());
            item.put("visitNum",courseware.getVisitNum());
            item.put("serieName",courseware.getSerieName());

            // 提交老师信息
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(courseware.getTeacherId());
            item.put("teacherName", teacher.fetchRealname());
            item.put("schoolName", teacher.getTeacherSchoolName());

            rankingMap.get(courseware.getSubject()).add(item);
        }

        // 排序，存入持久化缓存
        for (Subject subject : rankingMap.keySet()) {
            List<Map<String, Object>> rankingData = rankingMap.get(subject);
            // 倒序排
            Collections.sort(rankingData, (o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("score")), SafeConverter.toInt(o1.get("score"))));

            List<Map<String, Object>> allData = rankingData;

            if (rankingData.size() > 200) {
                rankingData = new ArrayList<>(rankingData.subList(0, 200));
            }

            String today = DateUtils.dateToString(endDate, DateUtils.FORMAT_SQL_DATE);

            // 处理上升下降数据
            String yesterday = DateUtils.dateToString(DateUtils.addDays(endDate, -1), DateUtils.FORMAT_SQL_DATE);
            List<Map<String, Object>> prevData = TeacherCourseCache.loadExcellentTotalRankData(subject.name(), yesterday);
            if (prevData == null) {
                prevData = new ArrayList<>();
            }

            DynamicRankingHelper.calcDynamicRank(rankingData, prevData, "coursewareId");


            TeacherCourseCache.setExcellentTotalRankData(subject.name(), today, rankingData);

            // 尴尬了, 说投票帮非要凑够要求的作品数, 单独存份缓存 500条兴许够了
            if (allData.size() > 500) {
                allData = new ArrayList<>(allData.subList(0, 500));
            }

            TeacherCourseCache.setExcellentCanvassTopData(subject.name(), today, allData);
        }
    }

    // 周榜单处理
    private void processWeeklyRank(List<TeacherCourseware> allCourseList, boolean force) {
        // 每周一才处理
        Calendar calendar = Calendar.getInstance();
        if (Calendar.MONDAY != calendar.get(Calendar.DAY_OF_WEEK) && !force) {
            return;
        }

        Date curTime = new Date();
        int week = (int) DateUtils.dayDiff(curTime, RANKING_START_DATE) / 7;

        int maxDownloadNum = 1;

        // 获取上周的下载数据
        WeekRange timeRange = WeekRange.current().previous();
        List<Map<String, Object>> downloadInfo = teacherCoursewareContestServiceClient.fetchDownloadStatInfo(timeRange.getStartDate(), timeRange.getEndDate());
        Map<String, Map<String, Object>> downloadInfoMap = new HashMap<>();
        for (Map<String, Object> item : downloadInfo) {
            String courseId = SafeConverter.toString(item.get("COURSEWARE_ID"));
            downloadInfoMap.put(courseId, item);

            int downloadNum = SafeConverter.toInt(item.get("NUM"));
            if (downloadNum > maxDownloadNum) {
                maxDownloadNum = downloadNum;
            }
        }

        // 处理排行榜
        Map<Subject, List<Map<String, Object>>> rankingMap = new HashMap<>();
        rankingMap.put(Subject.ENGLISH, new ArrayList<>());
        rankingMap.put(Subject.MATH, new ArrayList<>());
        rankingMap.put(Subject.CHINESE, new ArrayList<>());

        for (TeacherCourseware courseware : allCourseList) {
            // 过滤错误数据
            if (!rankingMap.containsKey(courseware.getSubject())) {
                continue;
            }

            // 状态
            if (courseware.getExamineStatus() != TeacherCourseware.ExamineStatus.PASSED || SafeConverter.toBoolean(courseware.getDisabled())) {
                continue;
            }

            int downloadScore = 0;
            if (downloadInfoMap.containsKey(courseware.getId())) {
                downloadScore = (int) (SafeConverter.toInt(downloadInfoMap.get(courseware.getId()).get("NUM")) * 10 / maxDownloadNum);
            }

            // 计算得分
            int newScore = SafeConverter.toInt(courseware.getTotalScore()) + downloadScore;

            Map<String, Object> item = new HashMap<>();
            item.put("coursewareId", courseware.getId());
            if (SafeConverter.toInt(courseware.getCommentNum()) >= 3) {
                item.put("score", newScore);
            } else {
                item.put("score", 0);
            }

            item.put("awardLevelName",courseware.getAwardLevelName());
            item.put("teacherId",courseware.getTeacherId());
            item.put("awardLevelId",courseware.getAwardLevelId());
            item.put("title",courseware.getTitle());
            item.put("totalScore",courseware.getTotalScore());
            item.put("coverUrl",courseware.getCoverUrl());
            item.put("createDate",DateUtils.dateToString(courseware.getUpdateTime(), DateUtils.FORMAT_SQL_DATE));
            item.put("downloadNum",courseware.getDownloadNum());
            item.put("commentNum",courseware.getCommentNum());
            item.put("visitNum",courseware.getVisitNum());

            // 提交老师信息
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(courseware.getTeacherId());
            item.put("teacherName", teacher.fetchRealname());
            item.put("schoolName", teacher.getTeacherSchoolName());

            rankingMap.get(courseware.getSubject()).add(item);
        }

        // 排序，存入持久化缓存
        for (Subject subject : rankingMap.keySet()) {
            List<Map<String, Object>> rankingData = rankingMap.get(subject);
            // 倒序排
            Collections.sort(rankingData, (o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("score")), SafeConverter.toInt(o1.get("score"))));
            if (rankingData.size() > 200) {
                rankingData = new ArrayList<>(rankingData.subList(0, 200));
            }

            // 处理上升下降数据
            List<Map<String, Object>> prevData = new ArrayList<>();
            int prevWeek = week - 1;
            if (prevWeek >= 1) {
                prevData = TeacherCourseCache.loadExcellentWeeklyRankData(subject.name(), week);
            }

            DynamicRankingHelper.calcDynamicRank(rankingData, prevData, "coursewareId");

            TeacherCourseCache.setExcellentWeeklyRankData(subject.name(), week, rankingData);
        }
    }

    // 月榜单处理
    private void processMonthlyRank(List<TeacherCourseware> allCourseList, boolean force) {

        Integer month = 0;
        Date startDate, endDate;
        // 月榜11月19日，12/18处理两次
        String date = DateUtils.dateToString(new Date(), "yyyyMMdd");
        if ("20181119".equals(date)) {
            month = 1;
            startDate = DateUtils.stringToDate("2018-10-22 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
            endDate = DateUtils.stringToDate("2018-11-18 23:59:59", DateUtils.FORMAT_SQL_DATETIME);
        } else if ("20181217".equals(date)) {
            month = 2;
            startDate = DateUtils.stringToDate("2018-11-19 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
            endDate = DateUtils.stringToDate("2018-12-16 23:59:59", DateUtils.FORMAT_SQL_DATETIME);
        } else if (force){
            Date curTime = new Date();
            Date fromDate = DateUtils.stringToDate("2018-11-19 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
            if (curTime.getTime() < fromDate.getTime()) {
                month = 1;
                startDate = DateUtils.stringToDate("2018-10-22 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
                endDate = DateUtils.stringToDate("2018-11-18 23:59:59", DateUtils.FORMAT_SQL_DATETIME);
            } else {
                month = 2;
                startDate = DateUtils.stringToDate("2018-11-19 00:00:00", DateUtils.FORMAT_SQL_DATETIME);
                endDate = DateUtils.stringToDate("2018-12-16 23:59:59", DateUtils.FORMAT_SQL_DATETIME);
            }
        } else {
            return;
        }

        int maxDownloadNum = 1;

        // 获取下载数据
        List<Map<String, Object>> downloadInfo = teacherCoursewareContestServiceClient.fetchDownloadStatInfo(startDate, endDate);
        Map<String, Map<String, Object>> downloadInfoMap = new HashMap<>();
        for (Map<String, Object> item : downloadInfo) {
            String courseId = SafeConverter.toString(item.get("COURSEWARE_ID"));
            downloadInfoMap.put(courseId, item);

            int downloadNum = SafeConverter.toInt(item.get("NUM"));
            if (downloadNum > maxDownloadNum) {
                maxDownloadNum = downloadNum;
            }
        }

        // 处理排行榜
        Map<Subject, List<Map<String, Object>>> rankingMap = new HashMap<>();
        rankingMap.put(Subject.ENGLISH, new ArrayList<>());
        rankingMap.put(Subject.MATH, new ArrayList<>());
        rankingMap.put(Subject.CHINESE, new ArrayList<>());

        for (TeacherCourseware courseware : allCourseList) {
            // 过滤错误数据
            if (!rankingMap.containsKey(courseware.getSubject())) {
                continue;
            }

            // 状态
            if (courseware.getExamineStatus() != TeacherCourseware.ExamineStatus.PASSED || SafeConverter.toBoolean(courseware.getDisabled())) {
                continue;
            }

            int downloadScore = 0;
            if (downloadInfoMap.containsKey(courseware.getId())) {
                downloadScore = (int) (SafeConverter.toInt(downloadInfoMap.get(courseware.getId()).get("NUM")) * 10 / maxDownloadNum);
            }

            // 计算得分
            int newScore = SafeConverter.toInt(courseware.getTotalScore()) + downloadScore;
            Map<String, Object> item = new HashMap<>();
            item.put("coursewareId", courseware.getId());

            if (SafeConverter.toInt(courseware.getCommentNum()) >= 3) {
                item.put("score", newScore);
            } else {
                item.put("score", 0);
            }

            item.put("awardLevelName",courseware.getAwardLevelName());
            item.put("teacherId",courseware.getTeacherId());
            item.put("awardLevelId",courseware.getAwardLevelId());
            item.put("title",courseware.getTitle());
            item.put("totalScore",courseware.getTotalScore());
            item.put("coverUrl",courseware.getCoverUrl());
            item.put("createDate",DateUtils.dateToString(courseware.getUpdateTime(), DateUtils.FORMAT_SQL_DATE));
            item.put("downloadNum",courseware.getDownloadNum());
            item.put("commentNum",courseware.getCommentNum());
            item.put("visitNum",courseware.getVisitNum());

            // 提交老师信息
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(courseware.getTeacherId());
            item.put("teacherName", teacher.fetchRealname());
            item.put("schoolName", teacher.getTeacherSchoolName());

            rankingMap.get(courseware.getSubject()).add(item);
        }

        // 排序，存入持久化缓存
        for (Subject subject : rankingMap.keySet()) {
            List<Map<String, Object>> rankingData = rankingMap.get(subject);
            // 倒序排
            Collections.sort(rankingData, (o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("score")), SafeConverter.toInt(o1.get("score"))));
            if (rankingData.size() > 200) {
                rankingData = new ArrayList<>(rankingData.subList(0, 200));
            }

            // 处理上升下降数据
            List<Map<String, Object>> prevData = new ArrayList<>();
            int prevMonth = month - 1;
            if (prevMonth >= 1) {
                prevData = TeacherCourseCache.loadExcellentMonthlyRankData(subject.name(), prevMonth);
            }

            DynamicRankingHelper.calcDynamicRank(rankingData, prevData, "coursewareId");

            TeacherCourseCache.setExcellentMonthlyRankData(subject.name(), month, rankingData);
        }
    }

}
