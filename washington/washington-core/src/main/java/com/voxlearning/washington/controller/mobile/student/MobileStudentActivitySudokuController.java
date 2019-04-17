package com.voxlearning.washington.controller.mobile.student;


import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.data.ActivityBaseRule;
import com.voxlearning.utopia.entity.activity.SudokuUserRecord;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import com.voxlearning.utopia.service.campaign.mapper.StudentParticipated;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.Validate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Controller
@NoArgsConstructor
@RequestMapping("/studentMobile/activity/sudoku/")
public class MobileStudentActivitySudokuController extends BaseStudentActivityController {

    /**
     * 数独 - 得分模式下记录用户倒计时剩余时长
     */
    @RequestMapping(value = "/countdown.vpage")
    @ResponseBody
    public MapMessage sudoHeartbeat() {
        return MapMessage.successMessage();
        /*try {
            if (isExperience()) {
                return MapMessage.successMessage();
            }
            StudentDetail stuDetail = currentStudentDetail();
            Validate.notNull(stuDetail, "未登录，不能操作!");

            String activityId = getRequestString(ACTIVITY_ID);
            Validate.notEmpty(activityId, "活动 id 不能为空");

            String countdown = getRequestString("countdown");
            Validate.notEmpty(countdown, "剩余时长不能为空");

            return activityServiceClient.updateSudoCountdown(stuDetail.getId(), activityId, SafeConverter.toInt(countdown));
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }*/
    }

    /**
     * 数独 - 首页
     */
    @RequestMapping(value = "/home.vpage")
    @ResponseBody
    public MapMessage sudokuHome() {
        try {
            if (isExperience()) {
                MapMessage resultMsg = MapMessage.successMessage();
                resultMsg.add("pattern", "LIMIT_TIME");
                resultMsg.add("time", "10");
                resultMsg.add("countdown", "10");
                resultMsg.add("correctCount", "0");
                resultMsg.add("startTime", "2018/1/1");
                resultMsg.add("endTime", "2020/1/1");
                resultMsg.add("clazzLevel", "3");
                resultMsg.put("allow", true);
                return resultMsg;
            }

            StudentDetail stuDetail = currentStudentDetail();
            Validate.notNull(stuDetail, "未登录，不能操作!");

            String activityId = getRequestString(ACTIVITY_ID);
            Validate.notEmpty(activityId, "活动 id 不能为空");

            Clazz clazz = stuDetail.getClazz();
            if (clazz == null || clazz.getClassLevel() == null || clazz.getSchoolId() == null) {
                return MapMessage.errorMessage("学校或班级异常");
            }

            ActivityConfig config = activityConfigService.load(activityId);
            if (config.getType() != ActivityTypeEnum.SUDOKU) {
                return MapMessage.errorMessage("活动类型错误");
            }
            if (config.getDisabled()) {
                return MapMessage.errorMessage("活动已被删除");
            }
            Date now = new Date();
            if (config.getStartTime().after(now)) {
                return MapMessage.errorMessage("活动未开始");
            }
            if (config.getEndTime().before(now)) {
                return MapMessage.errorMessage("活动已结束");
            }

            checkTeacherSignUpThrowException(config, stuDetail.getClazzId());

            if (CollectionUtils.isNotEmpty(config.getAreaIds())) {
                School schoolInfo = schoolLoaderClient.getSchoolLoader().loadSchool(clazz.getSchoolId()).getUninterruptibly();
                Long areaCode = SafeConverter.toLong(schoolInfo.getRegionCode());
                if (!config.getAreaIds().contains(areaCode)) {
                    return MapMessage.errorMessage("学校区域不在活动范围");
                }
            }

            if (CollectionUtils.isNotEmpty(config.getSchoolIds())) {
                if (!config.getSchoolIds().contains(stuDetail.getClazz().getSchoolId())) {
                    return MapMessage.errorMessage("学校不在活动范围");
                }
            }

            if (CollectionUtils.isNotEmpty(config.getClazzLevels())) {
                Integer clazzLevelAsInteger = stuDetail.getClazzLevelAsInteger();
                if (!config.getClazzLevels().contains(clazzLevelAsInteger)) {
                    return MapMessage.errorMessage("年级不在活动范围");
                }
            }

            Long studentClazzId = stuDetail.getClazzId();
            if (CollectionUtils.isNotEmpty(config.getClazzIds())
                    && !config.getClazzIds().contains(studentClazzId)) {
                return MapMessage.errorMessage("班级不在活动范围");
            }

            ActivityBaseRule rules = config.getRules();
            Validate.notNull(rules, "活动配置为空");

            String curDate = DateFormatUtils.format(now, "yyyyMMdd");
            SudokuUserRecord sudokuUserRecord = activityServiceClient.loadSudokuRecordByUidAidDate(stuDetail.getId(), activityId, curDate);

            MapMessage resultMsg = MapMessage.successMessage();
            resultMsg.add("pattern", config.getRules().getPattern().name());
            resultMsg.add("time", config.getRules().getLimitTime());
            resultMsg.add("correctCount", sudokuUserRecord == null ? 0 : sudokuUserRecord.getCorrectCount());
            resultMsg.add("startTime", DateFormatUtils.format(config.getStartTime(), "yyyy/M/d"));
            resultMsg.add("endTime", DateFormatUtils.format(config.getEndTime(), "yyyy/M/d"));
            resultMsg.add("clazzLevel", clazz.getClazzLevel().getLevel());

            // 得分模式下倒计时(考虑到重新进入的情况)
            Integer countdown = config.getRules().getLimitTime();
            if (sudokuUserRecord != null && sudokuUserRecord.getCountdown() != null) {
                countdown = sudokuUserRecord.getCountdown();
            }
            resultMsg.add("countdown", countdown);

            StudentParticipated isProhibit = activityServiceClient.allowParticipatedSudoku(stuDetail.getId(), activityId,
                    config.getRules().getPattern().name(),
                    config.getRules().getLimitTime(),
                    config.getRules().getLimitAmount());
            boolean allow = SafeConverter.toBoolean(isProhibit.getAllow(), true);
            if (!allow) {
                return MapMessage.errorMessage(isProhibit.getInfo());
            }

            resultMsg.add("allow", allow);
            return resultMsg;
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 数独 - 进场
     */
    @RequestMapping(value = "/fight.vpage")
    @ResponseBody
    public MapMessage sudokuFight() {
        try {
            if (isExperience()) {
                MapMessage mapMessage = MapMessage.successMessage().add("progress", 0);
                randomExperienceQuestion(mapMessage);
                return mapMessage;
            }

            StudentDetail studentDetail = currentStudentDetail();
            Validate.notNull(studentDetail, "未登录，不能操作!");

            String activityId = getRequestString(ACTIVITY_ID);
            Validate.notEmpty(activityId, "活动 id 不能为空");

            MapMessage mapMessage = activityServiceClient.enterSudoku(studentDetail.getId(), activityId);
            return mapMessage;
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 数独 - 提交分数
     */
    @RequestMapping(value = "/submit_score.vpage")
    @ResponseBody
    public MapMessage sudokuSubmitScore() {
        try {
            if (isExperience()) {
                return MapMessage.successMessage();
            }

            StudentDetail studentDetail = currentStudentDetail();
            Validate.notNull(studentDetail, "未登录，不能操作!");

            String activityId = getRequestString(ACTIVITY_ID);
            Validate.notEmpty(activityId, "活动 id 不能为空");

            String time = getRequestString("time");
            Validate.notEmpty(time, "time 不能为空");

            String index = getRequestString("index");
            Validate.notEmpty(index, "index 不能为空");

            String timestamp = getRequestString("timestamp");
            Validate.notEmpty(timestamp, "timestamp 不能为空");

            String cacheKey = genApiRetryCacheKey(studentDetail, timestamp);
            CacheObject<Object> cacheObject = flushable.get(cacheKey);
            if (cacheObject.containsValue()) {
                return MapMessage.errorMessage("提交分数失败");
            } else {
                flushable.set(cacheKey, 10 * 60, "1");
            }

            String sign = getRequestString("sign");
            Validate.notEmpty(sign, "sign 不能为空");

            Map<String, String> map = new HashMap<>();
            map.put("time", time);
            map.put(ACTIVITY_ID, activityId);
            map.put("index", index);
            map.put("timestamp", timestamp);
            String signMd5 = DigestSignUtils.signMd5(map, "");
            Validate.isTrue(Objects.equals(sign, signMd5), "提交分数失败");

            MapMessage mapMessage = activityServiceClient.submitSudokuScore(studentDetail.getId(), activityId, time, SafeConverter.toInt(index));
            return mapMessage;
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 数独 - 查看历史成绩
     */
    @RequestMapping(value = "/history.vpage")
    @ResponseBody
    public MapMessage sudokuHistory() {
        try {
            if (isExperience()) {
                return MapMessage.successMessage();
            }

            StudentDetail studentDetail = currentStudentDetail();
            Validate.notNull(studentDetail, "未登录，不能操作!");

            String activityId = getRequestString(ACTIVITY_ID);
            Validate.notEmpty(activityId, "活动 id 不能为空");

            MapMessage mapMessage = activityServiceClient.loadSudokuHistory(studentDetail.getId(), activityId);
            return mapMessage;
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    private void randomExperienceQuestion(MapMessage mapMessage) {
        int x = RandomUtils.nextInt(3);

        if (x == 1) {
            mapMessage.add("question", new String[]{
                    "0040200003000004",
                    "0100000300404000",
                    "1020000000000401"
            }).add("answer", new String[]{
                    "3142241343211234",
                    "3124241313424231",
                    "1324421331422431"
            });
        } else if (x == 2) {
            mapMessage.add("question", new String[]{
                    "030400046000200006300005000650003010",
                    "401030000004000540045000200000050206",
                    "056000000620600004400003032000000130"
            }).add("answer", new String[]{
                    "532461146532215346364125421653653214",
                    "421635563124632541145362216453354216",
                    "256341341625623514415263132456564132"
            });
        } else {
            mapMessage.add("question", new String[]{
                    "040060587010457000753000060300905402109000805504708006030000649000389020291070050",
                    "087000140900427003600198005096802430078000250042301980700964008800215004051000690",
                    "007194600064030710530807042403000806190000037608000104380902075049050360005643200"
            }).add("answer", new String[]{
                    "942163587618457293753892164386915472179246835524738916837521649465389721291674358",
                    "287536149915427863634198725196852437378649251542371986723964518869215374451783692",
                    "827194653964235718531867942473521896192486537658379124386912475249758361715643289"
            });
        }
    }

    private boolean isExperience() {
        String activityId = getRequestString(ACTIVITY_ID);
        return Objects.equals(activityId, "0");
    }
}
