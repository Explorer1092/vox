package com.voxlearning.washington.controller.mobile.student.babyeagle;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wonderland.api.constant.babyeagle.BabyEagleSubject;
import com.voxlearning.utopia.service.wonderland.api.constant.babyeagle.CourseStatus;
import com.voxlearning.utopia.service.wonderland.api.constant.babyeagle.LiveType;
import com.voxlearning.utopia.service.wonderland.api.data.WonderlandResult;
import com.voxlearning.washington.controller.mobile.AbstractMobileWonderlandActivityController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

/**
 * @author liu jingchao
 * @since 2017/7/4
 */
@Controller
@RequestMapping(value = "/studentMobile/babyeagle")
public class MobileStudentBabyEagleController extends AbstractMobileWonderlandActivityController {

    //首页
    @RequestMapping(value = "index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage index() {
        if (studentUnLogin()) {
            return WonderlandResult.ErrorType.NEED_LOGIN.result();
        }
        try {
            AlpsFuture<MapMessage> indexAlps = babyEagleLoaderClient.getBabyEagleLoader().fetchIndex(currentUserId());
            AlpsFuture<MapMessage> levelAndTermAlps = babyEagleLoaderClient.getBabyEagleLoader().fetchLevelAndTermList(currentUserId());
            AlpsFuture<MapMessage> recommendCourseIndexAlps = babyEagleLoaderClient.getBabyEagleLoader().loadRecommendCourseIndex(currentUserId());
            MapMessage indexResult = indexAlps.getUninterruptibly();
            MapMessage levelAndTermResult = levelAndTermAlps.getUninterruptibly();
            MapMessage recommendCourseResult = recommendCourseIndexAlps.getUninterruptibly();
            if (!indexResult.isSuccess()) {
                return indexResult;
            }
            if (levelAndTermResult.isSuccess() && levelAndTermResult.containsKey("levelTermList"))
                indexResult.add("levelTermList", levelAndTermResult.get("levelTermList"));
            else
                indexResult.add("levelTermList", new ArrayList<>());
            if (recommendCourseResult.isSuccess() && recommendCourseResult.containsKey("recommendCourseList"))
                indexResult.add("recommendCourseList", recommendCourseResult.get("recommendCourseList"));
            else
                indexResult.add("recommendCourseList", new ArrayList<>());

            return indexResult;
        } catch (Exception e) {
            return WonderlandResult.ErrorType.DEFAULT.result();
        }
    }

    // 更换年级学期
    @RequestMapping(value = "changelevelterm.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage changLevelAndTerm() {
        if (studentUnLogin()) {
            return WonderlandResult.ErrorType.NEED_LOGIN.result();
        }
        try {
            ClazzLevel clazzLevel = null;
            Term term = null;

            String levelAndTerm = getRequestString("levelTermType");
            if (levelAndTerm.contains("_")) {
                String[] values = levelAndTerm.split("_");
                if (values.length == 2) {
                    clazzLevel = ClazzLevel.parse(SafeConverter.toInt(values[0]));
                    term = Term.of(SafeConverter.toInt(values[1]));
                }
            }

            if (clazzLevel == null || term == null)
                return WonderlandResult.ErrorType.PARAM_ERROR.result();

            return babyEagleServiceClient.getRemoteReference().updateStudentLevelAndTermRecord(currentUserId(), clazzLevel, term);
        } catch (Exception e) {
            return WonderlandResult.ErrorType.DEFAULT.result();
        }
    }

    //课程列表页
    @RequestMapping(value = "course/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage courseIndex() {
        if (studentUnLogin()) {
            return WonderlandResult.ErrorType.NEED_LOGIN.result();
        }
        try {
            BabyEagleSubject babyEagleSubject = BabyEagleSubject.safeParse(getRequestString("subjectType"));
            if (babyEagleSubject == null) {
                return WonderlandResult.ErrorType.PARAM_ERROR.result();
            }
            MapMessage result = babyEagleLoaderClient.getBabyEagleLoader().fetchCourseIndex(currentUserId(), babyEagleSubject).getUninterruptibly();
            return result;
        } catch (Exception e) {
            return WonderlandResult.ErrorType.DEFAULT.result();
        }
    }

    //课时的时间列表
    @RequestMapping(value = "course/classhoursinfo.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage classHoursInfo() {
        if (studentUnLogin()) {
            return WonderlandResult.ErrorType.NEED_LOGIN.result();
        }
        try {
            String courseId = getRequestString("courseId");
            if (StringUtils.isBlank(courseId)) {
                return WonderlandResult.ErrorType.PARAM_ERROR.result();
            }

            return babyEagleLoaderClient.getBabyEagleLoader().fetchClassHoursInfo(courseId).getUninterruptibly();
        } catch (Exception e) {
            return WonderlandResult.ErrorType.DEFAULT.result();
        }
    }


    @RequestMapping(value = "course/entry.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage courseEntry() {
        if (studentUnLogin()) {
            return WonderlandResult.ErrorType.NEED_LOGIN.result();
        }
        User student = currentStudent();
        if (student == null)
            return WonderlandResult.ErrorType.NEED_LOGIN.result();

        String courseId = getRequestString("courseId");
        LiveType liveType = LiveType.safeParse(getRequestString("liveType"));

        if (StringUtils.isBlank(courseId)) {
            return WonderlandResult.ErrorType.NEED_PARAM.result();
        }

        // 判断超时降级开关是否已开启
        CacheObject<Boolean> sign = washingtonCacheSystem.CBS.flushable.get("baby_eagle_talk_fun_api_timeout_sign");
        if (sign.getValue() != null && sign.getValue())
            return WonderlandResult.ErrorType.COMMON_MSG.result();

        try {
            MapMessage resultMap = babyEagleServiceClient.getRemoteReference().getCoursePlayResource(student.getId(), courseId, liveType);
            // 这里因为需要用户头像生成accessKey，所以要再调用一次
            if (resultMap.isSuccess()) {
                CourseStatus status = CourseStatus.valueOf(String.valueOf(resultMap.get("status")));
                Boolean canPlay = Boolean.valueOf(String.valueOf(resultMap.get("canPlay")));
                String talkFunCourseId = String.valueOf(resultMap.get("talkFunCourseId"));
                String imgUrl = StringUtils.isEmpty(student.fetchImageUrl()) ? "" : getUserAvatarImgUrl(student.fetchImageUrl());
                if (canPlay && (status == CourseStatus.PLAY || status == CourseStatus.FINISH)) {
                    MapMessage accessKeyMap = babyEagleServiceClient.getRemoteReference().getCourseAccessKey(student.getId(), student.fetchRealname(), imgUrl, talkFunCourseId);
                    if (accessKeyMap.isSuccess() && accessKeyMap.containsKey("accessKey")) {
                        if (status == CourseStatus.PLAY)
                            resultMap.set("playUrl", "http://open.talk-fun.com/room.php?accessAuth=" + accessKeyMap.get("accessKey"));
                        else if (status == CourseStatus.FINISH)
                            resultMap.set("playUrl", "http://open.talk-fun.com/player.php?accessAuth=" + accessKeyMap.get("accessKey"));

                        resultMap.set("accessKey", accessKeyMap.get("accessKey"));
                    }

                }
            }
            return resultMap;
        } catch (Exception e) {
            // 60秒内报错超过10次开关置为true
            Long count = washingtonCacheSystem.CBS.flushable.incr("baby_eagle_talk_fun_api_timeout_count", 1, 1, 60);
            if (count != null && count.intValue() >= 10) {
                washingtonCacheSystem.CBS.flushable.add("baby_eagle_talk_fun_api_timeout_sign", 60, true);
            }
            return WonderlandResult.ErrorType.COMMON_MSG.result();
        }
    }

    @RequestMapping(value = "course/buy.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage courseBuy() {
        if (studentUnLogin()) {
            return WonderlandResult.ErrorType.NEED_LOGIN.result();
        }

        User student = currentStudent();
        if (student == null)
            return WonderlandResult.ErrorType.NEED_LOGIN.result();

        String courseId = getRequestString("courseId");
        if (StringUtils.isBlank(courseId)) {
            return WonderlandResult.ErrorType.NEED_PARAM.result();
        }

        // 判断超时降级开关是否已开启
        CacheObject<Boolean> sign = washingtonCacheSystem.CBS.flushable.get("baby_eagle_talk_fun_api_timeout_sign");
        if (sign.getValue() != null && sign.getValue())
            return WonderlandResult.ErrorType.COMMON_MSG.result();

        try {
            MapMessage resultMap = AtomicLockManager.getInstance()
                    .wrapAtomic(babyEagleServiceClient)
                    .keyPrefix("BabyEagleReceiveBuyCourse")
                    .keys(currentUserId())
                    .proxy()
                    .getRemoteReference()
                    .buyCourseClassHour(student.getId(), courseId);
            // 这里因为需要用户头像生成accessKey，所以要再调用一次
            if (resultMap.isSuccess()) {
                CourseStatus status = CourseStatus.valueOf(String.valueOf(resultMap.get("status")));
                String talkFunCourseId = String.valueOf(resultMap.get("talkFunCourseId"));
                String imgUrl = StringUtils.isEmpty(student.fetchImageUrl()) ? "" : getUserAvatarImgUrl(student.fetchImageUrl());
                if (status == CourseStatus.PLAY || status == CourseStatus.FINISH) {
                    MapMessage accessKeyMap = babyEagleServiceClient.getRemoteReference().getCourseAccessKey(student.getId(), student.fetchRealname(), imgUrl, talkFunCourseId);
                    if (accessKeyMap.isSuccess() && accessKeyMap.containsKey("accessKey")) {
                        if (status == CourseStatus.PLAY)
                            resultMap.set("playUrl", "http://open.talk-fun.com/room.php?accessAuth=" + accessKeyMap.get("accessKey"));
                        else if (status == CourseStatus.FINISH)
                            resultMap.set("playUrl", "http://open.talk-fun.com/player.php?accessAuth=" + accessKeyMap.get("accessKey"));

                        resultMap.set("accessKey", accessKeyMap.get("accessKey"));
                    }

                }
            }
            return resultMap;
        } catch (Exception e) {
            // 60秒内报错超过10次开关置为true
            Long count = washingtonCacheSystem.CBS.flushable.incr("baby_eagle_talk_fun_api_timeout_count", 1, 1, 60);
            if (count != null && count.intValue() >= 10) {
                washingtonCacheSystem.CBS.flushable.add("baby_eagle_talk_fun_api_timeout_sign", 60, true);
            }
            return WonderlandResult.ErrorType.COMMON_MSG.result();
        }
    }

    @RequestMapping(value = "course/receive.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage courseReceive() {
        if (studentUnLogin()) {
            return WonderlandResult.ErrorType.NEED_LOGIN.result();
        }

        String courseId = getRequestString("courseId");
        if (StringUtils.isBlank(courseId)) {
            return WonderlandResult.ErrorType.NEED_PARAM.result();
        }

        try {
            return AtomicLockManager.getInstance()
                    .wrapAtomic(babyEagleServiceClient)
                    .keyPrefix("BabyEagleReceiveCourseGift")
                    .keys(currentUserId())
                    .proxy()
                    .getRemoteReference()
                    .receiveCourseGift(currentUserId(), courseId);
        } catch (Exception e) {
            return WonderlandResult.ErrorType.DUPLICATED_OPERATION.result();
        }
    }

    @RequestMapping(value = "rank.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage rank() {
        if (studentUnLogin()) {
            return WonderlandResult.ErrorType.NEED_LOGIN.result();
        }
        try {
            return babyEagleLoaderClient.getBabyEagleLoader().loadStudentSchoolRankList(currentUserId()).getUninterruptibly();
        } catch (Exception e) {
            return WonderlandResult.ErrorType.DEFAULT.result();
        }
    }

    @RequestMapping(value = "goodlist.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage goodList() {
        if (studentUnLogin()) {
            return WonderlandResult.ErrorType.NEED_LOGIN.result();
        }
        try {
            return babyEagleLoaderClient.getBabyEagleLoader().loadStudentGoodRecordForThisWeek(currentUserId()).getUninterruptibly();
        } catch (Exception e) {
            return WonderlandResult.ErrorType.DEFAULT.result();
        }
    }

    @RequestMapping(value = "givegood.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage giveGood() {
        if (studentUnLogin()) {
            return WonderlandResult.ErrorType.NEED_LOGIN.result();
        }

        Long targetStudentId = getRequestLong("targetStudentId");

        try {
            return babyEagleServiceClient.getRemoteReference().addGoodRecord(currentUserId(), targetStudentId);
        } catch (Exception e) {
            return WonderlandResult.ErrorType.DEFAULT.result();
        }
    }

    @RequestMapping(value = "receivesinologycard.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage receiveSinologyCard() {
        if (studentUnLogin()) {
            return WonderlandResult.ErrorType.NEED_LOGIN.result();
        }

        try {
            return babyEagleChinaCultureServiceClient.getRemoteReference().receiveSinologyCard(currentUserId());
        } catch (Exception e) {
            return WonderlandResult.ErrorType.DEFAULT.result();
        }
    }
}
