package com.voxlearning.washington.controller.mobile.student.babyeagle;

import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wonderland.api.constant.babyeagle.CourseStatus;
import com.voxlearning.utopia.service.wonderland.api.data.WonderlandResult;
import com.voxlearning.washington.controller.mobile.AbstractMobileWonderlandActivityController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

/**
 * 小鹰国学堂
 *
 * @author liu jingchao
 * @since 2017/9/5
 */
@Controller
@RequestMapping(value = "/studentMobile/babyeagle/sinology")
public class MobileStudentBabyEagleChinaCultureController extends AbstractMobileWonderlandActivityController {

    // 首页
    @RequestMapping(value = "index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage index() {
        if (studentUnLogin()) {
            return WonderlandResult.ErrorType.NEED_LOGIN.result();
        }
        try {

            AlpsFuture<MapMessage> indexAlps = babyEagleChinaCultureLoaderClient.getRemoteReference().fetchIndex(currentUserId());
            AlpsFuture<MapMessage> bannerRecommendCourseIndexAlps = babyEagleChinaCultureLoaderClient.getRemoteReference().fetchIndexBannerRecommendCourse(currentUserId());

            MapMessage indexResult = indexAlps.getUninterruptibly();
            MapMessage bannerRecommendCourseResult = bannerRecommendCourseIndexAlps.getUninterruptibly();
            if (!indexResult.isSuccess()) {
                return indexResult;
            }
            if (bannerRecommendCourseResult.isSuccess() && bannerRecommendCourseResult.containsKey("bannerRecommendCourseList"))
                indexResult.add("bannerRecommendCourseList", bannerRecommendCourseResult.get("bannerRecommendCourseList"));
            else

            indexResult.add("bannerRecommendCourseList", new ArrayList<>());

            return indexResult;
        } catch (Exception e) {
            return WonderlandResult.ErrorType.DEFAULT.result();
        }
    }

    // 课程列表页
    @RequestMapping(value = "courselist.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage courseList() {
        if (studentUnLogin()) {
            return WonderlandResult.ErrorType.NEED_LOGIN.result();
        }
        try {
            AlpsFuture<MapMessage> courseListAlps = babyEagleChinaCultureLoaderClient.getRemoteReference().fetchCourseList(currentUserId());
            MapMessage courseListResult = courseListAlps.getUninterruptibly();
            return courseListResult;
        } catch (Exception e) {
            return WonderlandResult.ErrorType.DEFAULT.result();
        }
    }

    // 问题向导列表
    @RequestMapping(value = "questionguidelist.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage questionGuideList() {
        try {
            AlpsFuture<MapMessage> questionGuideListAlps = babyEagleChinaCultureLoaderClient.getRemoteReference().fetchQuestionGuideList();
            return questionGuideListAlps.getUninterruptibly();
        } catch (Exception e) {
            return WonderlandResult.ErrorType.DEFAULT.result();
        }
    }

    // 进入课程
    @RequestMapping(value = "entry.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage courseEntry() {
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
        CacheObject<Boolean> sign = washingtonCacheSystem.CBS.flushable.get("baby_eagle_cul_talk_fun_api_timeout_sign");
        if (sign.getValue() != null && sign.getValue())
            return WonderlandResult.ErrorType.COMMON_MSG.result();

        try {
            MapMessage resultMap = babyEagleChinaCultureServiceClient.getRemoteReference().getCoursePlayResource(student.getId(), courseId);
            // 这里因为需要用户头像生成accessKey，所以要再调用一次
            if (resultMap.isSuccess()) {
                CourseStatus status = CourseStatus.valueOf(String.valueOf(resultMap.get("status")));
                Boolean canPlay = Boolean.valueOf(String.valueOf(resultMap.get("canPlay")));
                String talkFunCourseId = String.valueOf(resultMap.get("talkFunCourseId"));
                String imgUrl = StringUtils.isEmpty(student.fetchImageUrl()) ? "" : getUserAvatarImgUrl(student.fetchImageUrl());
                if (canPlay && (status == CourseStatus.PLAY || status == CourseStatus.OVER)) {
                    MapMessage accessKeyMap = babyEagleServiceClient.getRemoteReference().getCourseAccessKey(student.getId(), student.fetchRealname(), imgUrl, talkFunCourseId);
                    if (accessKeyMap.isSuccess() && accessKeyMap.containsKey("accessKey")) {
                        if (status == CourseStatus.PLAY)
                            resultMap.set("playUrl", "http://open.talk-fun.com/room.php?accessAuth=" + accessKeyMap.get("accessKey"));
                        else
                            resultMap.set("playUrl", "http://open.talk-fun.com/player.php?accessAuth=" + accessKeyMap.get("accessKey"));

                        resultMap.set("accessKey", accessKeyMap.get("accessKey"));
                    }

                }
            }
            return resultMap;
        } catch (Exception e) {
            // 60秒内报错超过10次开关置为true
            Long count = washingtonCacheSystem.CBS.flushable.incr("baby_eagle_cul_talk_fun_api_timeout_count", 1, 1, 60);
            if (count != null && count.intValue() >= 10) {
                washingtonCacheSystem.CBS.flushable.add("baby_eagle_cul_talk_fun_api_timeout_sign", 60, true);
            }
            return WonderlandResult.ErrorType.COMMON_MSG.result();
        }
    }

    // 购买课程
    @RequestMapping(value = "buy.vpage", method = {RequestMethod.POST})
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
        CacheObject<Boolean> sign = washingtonCacheSystem.CBS.flushable.get("baby_eagle_cul_talk_fun_api_timeout_sign");
        if (sign.getValue() != null && sign.getValue())
            return WonderlandResult.ErrorType.COMMON_MSG.result();

        try {
            MapMessage resultMap = AtomicLockManager.getInstance()
                    .wrapAtomic(babyEagleChinaCultureServiceClient)
                    .keyPrefix("BabyEagleChinaCultureBuyCourse")
                    .keys(currentUserId())
                    .proxy()
                    .getRemoteReference()
                    .buyCourseClassHour(student.getId(), courseId);
            // 这里因为需要用户头像生成accessKey，所以要再调用一次
            if (resultMap.isSuccess()) {
                CourseStatus status = CourseStatus.valueOf(String.valueOf(resultMap.get("status")));
                String talkFunCourseId = String.valueOf(resultMap.get("talkFunCourseId"));
                String imgUrl = StringUtils.isEmpty(student.fetchImageUrl()) ? "" : getUserAvatarImgUrl(student.fetchImageUrl());
                if (status == CourseStatus.PLAY || status == CourseStatus.OVER) {
                    MapMessage accessKeyMap = babyEagleServiceClient.getRemoteReference().getCourseAccessKey(student.getId(), student.fetchRealname(), imgUrl, talkFunCourseId);
                    if (accessKeyMap.isSuccess() && accessKeyMap.containsKey("accessKey")) {
                        if (status == CourseStatus.PLAY)
                            resultMap.set("playUrl", "http://open.talk-fun.com/room.php?accessAuth=" + accessKeyMap.get("accessKey"));
                        else
                            resultMap.set("playUrl", "http://open.talk-fun.com/player.php?accessAuth=" + accessKeyMap.get("accessKey"));

                        resultMap.set("accessKey", accessKeyMap.get("accessKey"));
                    }

                }
            }
            return resultMap;
        } catch (Exception e) {
            // 60秒内报错超过10次开关置为true
            Long count = washingtonCacheSystem.CBS.flushable.incr("baby_eagle_cul_talk_fun_api_timeout_count", 1, 1, 60);
            if (count != null && count.intValue() >= 10) {
                washingtonCacheSystem.CBS.flushable.add("baby_eagle_cul_talk_fun_api_timeout_sign", 60, true);
            }
            return WonderlandResult.ErrorType.COMMON_MSG.result();
        }
    }

    // 领取课程奖励
    @RequestMapping(value = "receive.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage receiveGift() {
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

        try {
            return AtomicLockManager.getInstance()
                    .wrapAtomic(babyEagleChinaCultureServiceClient)
                    .keyPrefix("BabyEagleChinaCultureReceiveCourseGift")
                    .keys(currentUserId())
                    .proxy()
                    .getRemoteReference()
                    .receiveCourseGift(student.getId(), courseId);
        } catch (Exception e) {
            return WonderlandResult.ErrorType.DUPLICATED_OPERATION.result();
        }
    }

}
