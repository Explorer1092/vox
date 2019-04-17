package com.voxlearning.washington.controller.mobile.student;


import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.entity.activity.TangramEntryRecord;
import com.voxlearning.utopia.entity.activity.TwoFourPointEntityRecord;
import com.voxlearning.utopia.service.campaign.mapper.StudentParticipated;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.Validate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

import static com.voxlearning.alps.calendar.DateUtils.dateToString;

/**
 * 学生app 活动页
 * 学生app 各种活动接口
 *
 * @author xinqiang.wang
 * @author haitian.gan
 */
@Controller
@RequestMapping("/studentMobile/activity")
@NoArgsConstructor
@Slf4j
public class MobileStudentActivityController extends BaseStudentActivityController {

    @RequestMapping(value = "{folder}/{page}.vpage", method = RequestMethod.GET)
    public String page(@PathVariable("folder") String folder, @PathVariable("page") String page) {
        return "studentmobilev3/activity/" + folder + "/" + page;
    }

    /**
     * 七巧板 - 首页
     * @return
     */
    @RequestMapping(value = "/tangram/home.vpage")
    @ResponseBody
    public MapMessage tangramHomePage(){
        try{
            String code = getRequestParameter("code", "0");
            if (Objects.equals(code, "0")) {
                MapMessage resultMsg = MapMessage.successMessage();
                resultMsg.add("maxAttendNum", 1);
                resultMsg.add("attendNum", 1);
                resultMsg.add("time", 10);
                return resultMsg;
            }

            StudentDetail stuDetail = currentStudentDetail();
            Validate.notNull(stuDetail, "未登录，不能操作!");

            StudentParticipated studentParticipated = stuActSrvCli.allowParticipatedTangram(stuDetail.getId(), code);
            if (studentParticipated.isDeny()) {
                return MapMessage.errorMessage(studentParticipated.getInfo());
            }

            MapMessage resultMsg = MapMessage.successMessage();
            resultMsg.add("maxAttendNum",1);
            resultMsg.add("attendNum", studentParticipated.getAllow() ? 1 : 0);
            resultMsg.add("time", getActivityLimitTime(code, 10));

            checkTeacherSignUpThrowException(code, stuDetail.getClazzId());
            return resultMsg;
        }catch (Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 七巧板 - 开始比赛
     * http://wiki.17zuoye.net/pages/viewpage.action?pageId=37403812
     * @return
     */
    @RequestMapping(value = "/tangram/fight.vpage")
    @ResponseBody
    public MapMessage fight(){
        try{
            String code = getRequestParameter("code", "0");
            if (Objects.equals(code, "0")) {
                MapMessage resultMsg = MapMessage.successMessage();
                resultMsg.add("questionIndex", RandomUtils.nextInt(1, 199));
                resultMsg.add("time", 10);
                return resultMsg;
            }

            StudentDetail stuDetail = currentStudentDetail();
            Validate.notNull(stuDetail, "未登录，不能操作!");
            return stuActSrvCli.enterTangramArena(stuDetail.getId(), code);
        }catch (Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 七巧板 - 提交分数
     * @return
     */
    @RequestMapping(value = "/tangram/submit_score.vpage")
    @ResponseBody
    public MapMessage submitTangramScore(){
        try{
            String code = getRequestParameter("code", "0");
            if (Objects.equals(code, "0")) {
                return MapMessage.successMessage().add("questionIndex", RandomUtils.nextInt(1, 199));
            }

            StudentDetail stuDetail = currentStudentDetail();
            Validate.notNull(stuDetail,"未登录，不能操作!");

            int score = getRequestInt("score");
            Validate.isTrue(score >= 0);

            String timestamp = getRequestString("timestamp");
            Validate.notEmpty(timestamp, "timestamp 不能为空");

            String cacheKey = genApiRetryCacheKey(stuDetail, timestamp);
            CacheObject<Object> cacheObject = flushable.get(cacheKey);
            if (cacheObject.containsValue()) {
                return MapMessage.errorMessage("提交分数失败");
            } else {
                flushable.set(cacheKey, 10 * 60, "1");
            }

            String sign = getRequestString("sign");
            Validate.notEmpty(sign, "sign 不能为空");

            Map<String, String> map = new HashMap<>();
            map.put("code", code);
            map.put("score", score + "");
            map.put("timestamp", timestamp);
            String signMd5 = DigestSignUtils.signMd5(map, "");
            Validate.isTrue(Objects.equals(sign, signMd5), "提交分数失败");

            return stuActSrvCli.submitTangramScore(stuDetail.getId(), score, code);
        }catch (Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 七巧板 - 查看历史成绩
     * @return
     */
    @RequestMapping(value = "/tangram/history.vpage")
    @ResponseBody
    public MapMessage history(){
        try{
            String code = getRequestParameter("code", "0");
            if (Objects.equals(code, "0")) {
                return MapMessage.successMessage();
            }

            StudentDetail stuDetail = currentStudentDetail();
            Validate.notNull(stuDetail,"未登录，不能操作!");

            TangramEntryRecord record = stuActSrvCli.loadTangramRecord(stuDetail.getId(), code);
            List<Map<String,Object>> scoreList = new ArrayList<>();

            Optional.ofNullable(record.getScoreMap())
                    .orElse(new HashMap<>())
                    .forEach((time, score) -> {
                        String dateExp = dateToString(new Date(time), "MM-dd HH:mm");
                        scoreList.add(MapUtils.m("date", dateExp, "score", score));
                    });

            return MapMessage.successMessage().add("result",scoreList);
        }catch (Exception e){
            return MapMessage.errorMessage(e.getMessage());
        }
    }


    @RequestMapping(value = "/twofour/home.vpage")
    @ResponseBody
    public MapMessage twoFourHomePage() {
        try {
            String code = getRequestString("code");
            Validate.notEmpty(code, "活动code不能为空");

            if (isExperience()) {
                MapMessage resultMsg = MapMessage.successMessage();
                resultMsg.add("maxAttendNum", 1);
                resultMsg.add("attendNum", 1);
                resultMsg.add("time", 10);
                return resultMsg;
            }

            StudentDetail studentDetail = currentStudentDetail();
            Validate.notNull(studentDetail, "未登录，不能操作!");

            StudentParticipated studentParticipated = stuActSrvCli.allowParticipatedTwentyFour(studentDetail.getId(), code);
            if (studentParticipated.isDeny()) {
                return MapMessage.errorMessage(studentParticipated.getInfo());
            }

            MapMessage resultMsg = MapMessage.successMessage();
            resultMsg.add("maxAttendNum", 1);
            resultMsg.add("attendNum", studentParticipated.getAllow() ? 1 : 0);
            resultMsg.add("time", getActivityLimitTime(code, 10));

            checkTeacherSignUpThrowException(code, studentDetail.getClazzId());
            return resultMsg;
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 24点游戏 - 开始比赛
     * http://wiki.17zuoye.net/pages/viewpage.action?pageId=38179742
     */
    @RequestMapping(value = "/twofour/fight.vpage")
    @ResponseBody
    public MapMessage twofourFight() {
        try {
            String code = getRequestString("code");
            Validate.notEmpty(code, "活动code不能为空");

            User studentDetail = currentUser();

            if (isExperience()) {
                Long studentId = studentDetail == null ? null : studentDetail.getId();
                Integer max = Integer.valueOf(code.split("_")[1]);
                return stuActSrvCli.randomExpQuestion(studentId, max);
            }

            Validate.notNull(studentDetail, "未登录，不能操作!");

            StudentDetail studentInfo = currentStudentDetail();
            Integer clazzLevelAsInteger = studentInfo.getClazzLevelAsInteger();

            return stuActSrvCli.enterTwoPoint(studentDetail.getId(), code)
                    .add("clazzLevel", clazzLevelAsInteger);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/twofour/exit.vpage")
    @ResponseBody
    public MapMessage exit() {
        try {
            String code = getRequestString("code");
            Validate.notEmpty(code, "活动code不能为空");

            if (isExperience()) {
                return MapMessage.successMessage();
            }

            StudentDetail studentDetail = currentStudentDetail();
            Validate.notNull(studentDetail, "未登录，不能操作!");

            return stuActSrvCli.exitTwofour(studentDetail.getId(), code);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/twofour/getQuestion.vpage")
    @ResponseBody
    public MapMessage pullMoreQuestion() {
        try {
            String code = getRequestString("code");
            Validate.notEmpty(code, "活动code不能为空");

            User studentDetail = currentUser();

            if (isExperience()) {
                Long studentId = studentDetail == null ? null : studentDetail.getId();
                Integer max = Integer.valueOf(code.split("_")[1]);
                return stuActSrvCli.randomExpQuestion(studentId, max);
            }

            Validate.notNull(studentDetail, "未登录，不能操作!");

            return stuActSrvCli.pullMoreQuestion(studentDetail.getId(), code);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 24点游戏 - 提交分数
     *
     * @return
     */
    @RequestMapping(value = "/twofour/submit_score.vpage")
    @ResponseBody
    public MapMessage submitTwofourScore() {
        try {
            String code = getRequestString("code");
            Validate.notEmpty(code, "活动code不能为空");

            if (isExperience()) {
                return MapMessage.successMessage();
            }

            StudentDetail studentDetail = currentStudentDetail();
            Validate.notNull(studentDetail, "未登录，不能操作!");

            String timestamp = getRequestString("timestamp");
            Validate.notEmpty(timestamp, "timestamp 不能为空");

            String cacheKey = genApiRetryCacheKey(studentDetail, timestamp);
            CacheObject<Object> cacheObject = flushable.get(cacheKey);
            if (cacheObject.containsValue()) {
                return MapMessage.errorMessage("提交分数失败");
            } else {
                flushable.set(cacheKey, 10 * 60, "1");
            }

            int score = getRequestInt("score");
            Validate.isTrue(score >= 0);

            String sign = getRequestString("sign");

            Map<String, String> map = new HashMap<>();
            map.put("code", code);
            map.put("score", score + "");
            map.put("timestamp", timestamp);
            String signMd5 = DigestSignUtils.signMd5(map, "");
            Validate.isTrue(Objects.equals(sign, signMd5), "提交分数失败");

            return stuActSrvCli.submitTwoPointScore(studentDetail.getId(), score, code);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 24点游戏 - 查看历史成绩
     */
    @RequestMapping(value = "/twofour/history.vpage")
    @ResponseBody
    public MapMessage twofourHistory() {
        try {
            String code = getRequestString("code");
            Validate.notEmpty(code, "活动code不能为空");

            if (isExperience()) {
                return MapMessage.successMessage();
            }

            StudentDetail studentDetail = currentStudentDetail();
            Validate.notNull(studentDetail, "未登录，不能操作!");

            TwoFourPointEntityRecord record = stuActSrvCli.loadTwofourPointRecord(studentDetail.getId(), code);
            List<Map<String, Object>> scoreList = new ArrayList<>();

            Optional.ofNullable(record.getScoreMap())
                    .orElse(new HashMap<>())
                    .forEach((time, score) -> {
                        String dateExp = dateToString(new Date(time), "MM-dd HH:mm");
                        scoreList.add(MapUtils.m("date", dateExp, "score", score));
                    });

            return MapMessage.successMessage().add("result", scoreList);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/twofour/addreset.vpage")
    @ResponseBody
    public MapMessage resetCountTwofour() {
        try {
            String code = getRequestString("code");
            Validate.notEmpty(code, "活动code不能为空");

            if (isExperience()) {
                return MapMessage.successMessage();
            }

            StudentDetail studentDetail = currentStudentDetail();
            Validate.notNull(studentDetail, "未登录，不能操作!");

            return stuActSrvCli.resetCountTwofour(studentDetail.getId(), code);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/twofour/addskip.vpage")
    @ResponseBody
    public MapMessage skipCountTwofour() {
        try {
            String code = getRequestString("code");
            Validate.notEmpty(code, "活动code不能为空");

            if (isExperience()) {
                return MapMessage.successMessage();
            }

            StudentDetail studentDetail = currentStudentDetail();
            Validate.notNull(studentDetail, "未登录，不能操作!");

            return stuActSrvCli.skipCountTwofour(studentDetail.getId(), code);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    private boolean isExperience() {
        String code = getRequestString("code");
        return Objects.equals(code, "0_10") || Objects.equals(code, "0_13");
    }

    @RequestMapping(value = "/file/download.vpage")
    public void downloadFile() {
        try {
            String fileUrl = getRequestString("url");
            if (StringUtils.isEmpty(fileUrl)) {
                fileUrl = "https://cdn-cnc.17zuoye.cn/s17/commons/mobile/teacher/activity/newforum/images/listHeader-new.jpg";
            }

            AlpsHttpResponse resp = HttpRequestExecutor.defaultInstance()
                    .get(fileUrl)
                    .execute();

            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    "test.jpg",
                    "image/jpeg",
                    resp.getOriginalResponse());

        } catch (Exception e) {
        }
    }

}
