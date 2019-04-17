package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareConstants;
import com.voxlearning.utopia.service.campaign.cache.TeacherCourseCache;
import com.voxlearning.utopia.service.campaign.client.TeacherCoursewareContestServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.Pager;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.utopia.service.campaign.api.constant.TeacherCoursewareConstants.RANKING_START_DATE;

/**
 * 课件排行榜信息接口
 *
 * @Author: peng.zhang
 * @Date: 2018/10/11
 */
@Controller
@RequestMapping("/courseware/rank")
public class TeacherCourseRankController extends AbstractController {

    @Inject
    private TeacherCoursewareContestServiceClient teacherCoursewareContestServiceClient;

    public static final Integer SUNDAY = 0;

    public static final String POPULARITY_CACHE_KEY = "POPULARITY";

    public static final String TALENT_CACHE_KEY = "TALENT";

    public static final Integer DEFAULT_PAGE_NUM = 0;

    public static final Integer DEFAULT_PAGE_SIZE = 8;

    /**
     * 人气作品榜
     * @return 课件综合评价信息
     */
    @RequestMapping(value = "popularityRank.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage popularityRank(@RequestParam(name = "type",required = false) Integer type,
                                     @RequestParam(name = "period",required = false) Integer period,
                                     @RequestParam(name = "subject",required = false) String subject,
                                     @RequestParam(name = "date",required = false) String date,
                                     @RequestParam(name = "topThree",required = false) Boolean topThree,
                                     @RequestParam(name = "pageNum",required = false) Integer pageNum,
                                     @RequestParam(name = "pageSize",required = false) Integer pageSize){
        TeacherDetail teacherDetail = currentTeacherDetail();
        pageNum = pageNum == null ? DEFAULT_PAGE_NUM + 1 : pageNum + 1;
        pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
        // 0：日榜、1：周榜、2：月榜、3：总榜
        MapMessage message = new MapMessage().setSuccess(true);
        switch (type){
            case 0:
                message = teacherCoursewareContestServiceClient.loadDailyPopularityRanking(subject,date);
                break;
            case 1:
                message = teacherCoursewareContestServiceClient.loadWeeklyPopularityRanking(subject,period);
                break;
            case 3:
                message = teacherCoursewareContestServiceClient.loadTotalPopularityRanking(subject);
                break;
        }
        if (message.isSuccess()){
            List<Map<String, Object>> retValue = (List<Map<String, Object>>)message.get("data");

            if ( null != topThree && topThree ){
                retValue = retValue.size() > 3 ? retValue.subList(0,2) : retValue;
            }
            if (CollectionUtils.isNotEmpty(retValue)){
                retValue = teacherCoursewareContestServiceClient.updateCourseInfo(retValue);
                Pager<Map<String, Object>> pager = Pager.create(retValue, pageSize);
                message.set("data", pager.getPagedList(pageNum)).
                        set("pageNum",pageNum).set("pageSize",pageSize).set("total",retValue.size());
            } else {
                message.set("data", new ArrayList<>()).set("pageNum",pageNum).set("pageSize",pageSize).
                        set("total",retValue.size());
            }
            return message.set("userId", currentUserId());
        } else {
            return MapMessage.errorMessage().setInfo(message.getInfo());
        }
    }

    /**
     * 优秀作品榜
     * @param subject 学科
     * @return 课件的个人评价信息
     */
    @RequestMapping(value = "excellentRank.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage excellentRank(@RequestParam(name = "type",required = false) Integer type,
                                    @RequestParam(name = "subject",required = false) String subject,
                                    @RequestParam(name = "period",required = false) Integer period,
                                    @RequestParam(name = "topThree",required = false) Boolean topThree,
                                    @RequestParam(name = "pageNum",required = false) Integer pageNum,
                                    @RequestParam(name = "pageSize",required = false) Integer pageSize){

        pageNum = pageNum == null ? DEFAULT_PAGE_NUM + 1 : pageNum + 1;
        pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
        // 0：日榜、1：周榜、2：月榜、3：总榜
        MapMessage message = new MapMessage().setSuccess(true);
        List<Map<String, Object>> result = new ArrayList<>();

        //String today = DateUtils.dateToString(DateUtils.addDays(new Date(), -1), DateUtils.FORMAT_SQL_DATE);

        switch (type){
            case 1:
                result = TeacherCourseCache.loadExcellentWeeklyRankData(subject,period);
                break;
            case 2:
                result = TeacherCourseCache.loadExcellentMonthlyRankData(subject,period);
                break;
            case 3:
                result = TeacherCourseCache.loadExcellentTotalRankData(subject, null);
                break;
        }
        if (CollectionUtils.isNotEmpty(result)){
            if ( null != topThree && topThree ){
                result = result.size() > 3 ? result.subList(0,2) : result;
            }
            if (CollectionUtils.isNotEmpty(result)){
                result = teacherCoursewareContestServiceClient.updateCourseInfo(result);

                Pager<Map<String, Object>> pager = Pager.create(result, pageSize);
                message.set("data", pager.getPagedList(pageNum)).
                        set("pageNum",pageNum).set("pageSize",pageSize).set("total",result.size());
            } else {
                message.set("data", new ArrayList<>()).set("pageNum",pageNum).set("pageSize",pageSize).
                        set("total",result.size());
            }
        } else {
            message.set("data", new ArrayList<>());
        }
        return message.set("userId", currentUserId());
    }

    /**
     * 达人榜
     * @return
     */
    @RequestMapping(value = "talentRank.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage talentRank(@RequestParam(name = "type",required = false) Integer type,
                                 @RequestParam(name = "period",required = false) Integer period,
                                 @RequestParam(name = "date",required = false) String date,
                                 @RequestParam(name = "topFive",required = false) Boolean topFive,
                                 @RequestParam(name = "pageNum",required = false) Integer pageNum,
                                 @RequestParam(name = "pageSize",required = false) Integer pageSize){

        pageNum = pageNum == null ? DEFAULT_PAGE_NUM + 1 : pageNum + 1;
        pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
        // 0：日榜、1：周榜、2：月榜、3：总榜
        MapMessage message = new MapMessage();
        switch (type){
            case 0:
                message = teacherCoursewareContestServiceClient.loadDailyTalentRanking(date);
                break;
            case 1:
                message = teacherCoursewareContestServiceClient.loadWeeklyTalentRanking(period);
                break;
            case 3:
                message = teacherCoursewareContestServiceClient.loadTotalTalentRanking();
                break;
        }
        if (message.isSuccess()){
            List<Map<String, Object>> retValue = (List<Map<String, Object>>)message.get("data");

            if ( null != topFive && topFive ){
                retValue = retValue.size() > 3 ? retValue.subList(0, 4) : retValue;
            }
            if (CollectionUtils.isNotEmpty(retValue)){
                Pager<Map<String, Object>> pager = Pager.create(retValue, pageSize);
                message.set("data", pager.getPagedList(pageNum)).
                        set("pageNum",pageNum).set("pageSize",pageSize).set("total",retValue.size());

                // 第一页判断用户自己在不在榜单内
                Long curUid = currentUserId();
                if (curUid != null && Objects.equals(pageNum, 1)) {
                    Map<String, Object> userData = retValue.stream()
                            .filter(p -> Objects.equals(curUid, SafeConverter.toLong(p.get("teacherId"))))
                            .findFirst().orElse(null);
                    message.set("userData", userData);
                } else {
                    message.set("userData", null);
                }
            } else {
                message.set("data", new ArrayList<>()).set("pageNum",pageNum).set("pageSize",pageSize)
                        .set("total",retValue.size())
                        .set("userData", null);
            }
            return message.set("userId", currentUserId());
        } else {
            return MapMessage.errorMessage().setInfo(message.getInfo());
        }
    }

    @RequestMapping(value = "top3.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage top3(){
        return teacherCoursewareContestServiceClient.loadTop3Ranking();
    }

    @RequestMapping(value = "poularityshow.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage poularityShowInfo(){
        return teacherCoursewareContestServiceClient.loadPopularityShowInfo();
    }

    @RequestMapping(value = "talentshow.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage talentShowInfo(){
        return teacherCoursewareContestServiceClient.loadTalentShowInfo();
    }

    @RequestMapping(value = "excellentshow.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage excellentShowInfo(){
        return teacherCoursewareContestServiceClient.loadExcellentShowInfo();
    }

    /**
     * 查询周榜的期数和时间的对应关系,并返回
     * @return
     */
    @RequestMapping(value = "weeklyTimes.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchTime(){
        List<WeekInfo> result = new ArrayList<>();
        Date curTime = TeacherCoursewareConstants.WEEKLY_TIMES_END;
        Integer week = (int) DateUtils.dayDiff(curTime, RANKING_START_DATE) / 7;

        for (int i = 1; i <= week; i++) {
            WeekInfo weekInfo = new WeekInfo();
            weekInfo.setPeriod(i);
            WeekRange range = WeekRange.newInstance(DateUtils.addWeeks(RANKING_START_DATE, i - 1).getTime());
            weekInfo.set_startTime(DateUtils.dateToString(range.getStartDate(), DateUtils.FORMAT_SQL_DATE));
            weekInfo.set_endTime(DateUtils.dateToString(range.getEndDate(), DateUtils.FORMAT_SQL_DATE));
            weekInfo.setStartTime(range.getStartDate());
            weekInfo.setEndTime(range.getEndDate());
            result.add(0, weekInfo);
        }

        return MapMessage.successMessage().set("data",result);
    }

    /**
     * 周信息
     */
    @Data
    public static class WeekInfo{
        public Integer period;
        public Date startTime;
        public Date endTime;
        public String _startTime;
        public String _endTime;
    }

}
