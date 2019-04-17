package com.voxlearning.washington.controller.open.v1.student;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.cache.UserCache;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageShareType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNews;
import com.voxlearning.washington.controller.open.AbstractStudentApiController;
import com.voxlearning.washington.controller.open.v1.util.JxtNewsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_SUCCESS;

/**
 * 护眼相关
 */
@Controller
@RequestMapping(value = "/v1")
@Slf4j
public class StudentEyeController extends AbstractStudentApiController {
    private static List<String> timeOverNewsIdList = new ArrayList<>();

    private static final List<Map<String, Object>> musics = Arrays.asList(
            MapUtils.m("name", "乘着歌声的翅膀（门德尔松）", "music", "http://cdn.17zuoye.com/fs-resource/5b75547ae8ddcab9de91b8a7.sy3"),
            MapUtils.m("name", "卡农（巴格贝尔）", "music", "http://cdn.17zuoye.com/fs-resource/5b75563b498ca49808aa9131.sy3"),
            MapUtils.m("name", "月光奏鸣曲（贝多芬）", "music", "http://cdn.17zuoye.com/fs-resource/5b75577c498ca4994c0b4cb2.sy3"),
            MapUtils.m("name", "小星星变奏曲（李斯特）", "music", "http://cdn.17zuoye.com/fs-resource/5b7571cfe8ddca4e5569ceb3.sy3"),
            MapUtils.m("name", "钟表店（安德松）", "music", "http://cdn.17zuoye.com/fs-resource/5b757231498ca46ffdaa22b7.sy3"),
            MapUtils.m("name", "春之圆舞曲（斯特劳斯）", "music", "http://cdn.17zuoye.com/fs-resource/5b756cabe8ddca4e5164b084.sy3"),
            MapUtils.m("name", "月光曲（德彪西）", "music", "http://cdn.17zuoye.com/fs-resource/5b755744e8ddcab9d5660acd.sy3")
//            MapUtils.m("name", "F大调浪漫曲（贝多芬）", "music", "http://cdn.17zuoye.com/fs-resource/5b755295e8ddcab9dde08f26.sy3"),
//            MapUtils.m("name", "F大调夜曲（肖邦）", "music", "http://cdn.17zuoye.com/fs-resource/5b7553d1e8ddcab9dce79aea.sy3"),
//            MapUtils.m("name", "爱的喜悦（克莱斯勒）", "music", "http://cdn.17zuoye.com/fs-resource/5b755435e8ddcab9dde0922c.sy3"),
//            MapUtils.m("name", "春之歌（门德尔松）", "music", "http://cdn.17zuoye.com/fs-resource/5b7554ee498ca4994d62e5e8.sy3"),
//            MapUtils.m("name", "华尔兹（柴科夫斯基）", "music", "http://cdn.17zuoye.com/fs-resource/5b755560e8ddcab9d56609a6.sy3"),
//            MapUtils.m("name", "欢乐颂（贝多芬）", "music", "http://cdn.17zuoye.com/fs-resource/5b7555f1498ca499506135e5.sy3"),
//            MapUtils.m("name", "蓝色多瑙河舞曲（斯特劳斯）", "music", "http://cdn.17zuoye.com/fs-resource/5b755669e8ddcac12c76f8e9.sy3"),
//            MapUtils.m("name", "致爱丽丝（贝多芬）", "music", "http://cdn.17zuoye.com/fs-resource/5b7557dee8ddcab9dde096af.sy3"),
//            MapUtils.m("name", "土耳其进行曲（莫扎特）", "music", "http://cdn.17zuoye.com/fs-resource/5b756d61498ca46e7d85dc52.sy3"),
//            MapUtils.m("name", "罗密欧与朱丽叶（洛塔）", "music", "http://cdn.17zuoye.com/fs-resource/5b756edf498ca46e79803401.sy3"),
//            MapUtils.m("name", "维也纳森林故事（斯特劳斯）", "music", "http://cdn.17zuoye.com/fs-resource/5b7570ebe8ddca4e57cebe18.sy3"),
//            MapUtils.m("name", "四季（威尔第）", "music", "http://cdn.17zuoye.com/fs-resource/5b757152e8ddca4e5569ce26.sy3"),
//            MapUtils.m("name", "F调旋律（鲁宾斯坦）", "music", "http://cdn.17zuoye.com/fs-resource/5b75734de8ddca4e503bb31d.sy3"),
//            MapUtils.m("name", "横笛协奏曲1号（莫扎特）", "music", "http://cdn.17zuoye.com/fs-resource/5b7573b2498ca46e7d85e143.sy3"),
//            MapUtils.m("name", "小夜曲1（莫扎特）", "music", "http://cdn.17zuoye.com/fs-resource/5b75750c498ca46e79803753.sy3"),
//            MapUtils.m("name", "小夜曲2（莫扎特）", "music", "http://cdn.17zuoye.com/fs-resource/5b757593e8ddca4e5ba6ec54.sy3"),
//            MapUtils.m("name", "小夜曲3（莫扎特）", "music", "http://cdn.17zuoye.com/fs-resource/5b7575b4498ca46e783cc842.sy3")
    );

    static {
        if (RuntimeMode.lt(Mode.STAGING)) { //测试
            timeOverNewsIdList.add("5821e8854d944bb37e415aea");
            timeOverNewsIdList.add("584cc21d8c72cb947245871c");
        } else { //staging 线上
            timeOverNewsIdList.add("5988518cb3acabc7c977e9a7");
            timeOverNewsIdList.add("598852bbb3acabc7c97809c7");
            timeOverNewsIdList.add("584cc21d8c72cb947245871c");
            timeOverNewsIdList.add("5821e8854d944bb37e415aea");
            timeOverNewsIdList.add("585265e24d944b6d43ce40e0");
        }
    }

    /**
     * 相关提醒时间
     * @return
     */
    @RequestMapping(value = "/student/eye/times.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage eyeTimes() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_MESSAGE, RES_RESULT_BAD_REQUEST_MSG);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            return resultMap;
        }
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(currentUserId());
        if (studentExtAttribute == null) {
            studentExtAttribute = new StudentExtAttribute();
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS)
                .add("rest_short_time", 5)
                .add("rest_short_time_number", 2)
                .add("rest_long_time", 10)
                .add("musics", musics)
                .add("interval_time",  studentExtAttribute.fetchAppUseOnceTimeLimit())
                .add("max_time", studentExtAttribute.fetchAppUseTimeLimit())
                .add("skip_time", new int[] {60, 60, -1, 60, 60, -1, 60, 60, -1, 60});
        return resultMap;
    }

    /**
     * 给家长发push
     * @return
     */
    @RequestMapping(value = "/student/eye/notifyovertime.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage sendUseTimeOverNotify2() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_USE_TIME, "使用时间");
            validateNumber(REQ_USE_TIME, "使用时间");
            validateRequired(REQ_NOTIFY_TYPE);
            validateEnum(REQ_NOTIFY_TYPE, "通知类型", "OVER_FORCE", "OVER_COUNT");
            validateRequest(REQ_USE_TIME, REQ_NOTIFY_TYPE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_MESSAGE, RES_RESULT_BAD_REQUEST_MSG);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            return resultMap;
        }
        Student currentStudent = getCurrentStudent();
        if (currentStudent == null)
            return failMessage(RES_RESULT_STUDENT_ID_ERROR_MSG);
        String key;
        String content;
        if (Objects.equals("OVER_FORCE", getRequestString(REQ_NOTIFY_TYPE))) {
            key = "STUDNET_NOTIFY_OVER_FORCE_" + currentStudent.getId();
            content = String.format("家长您好，您的孩子 %s 已经连续学习 %s 分钟了，请让孩子适度休息，注意用眼健康哦！", currentStudent.fetchRealname(), getRequestString(REQ_USE_TIME));
        } else {
            key = "STUDNET_NOTIFY_OVER_COUNT_" + currentStudent.getId();
            content = String.format("家长您好，您的孩子 %s 今天已经累计学习 %s 分钟了，为了保护孩子眼睛，APP停止使用了哦！", currentStudent.fetchRealname(), getRequestString(REQ_USE_TIME));
        }
        return sendNotifyToParent(key, content);
    }

    /**
     * 旧版给家长发push
     * @return
     */
    @RequestMapping(value = "/student/notifyovertime.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage sendUseTimeOverNotify() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_NOTIFY_TYPE);
            validateEnum(REQ_NOTIFY_TYPE, "通知类型", "OVER_CONTINUE", "OVER_COUNT");
            validateRequest(REQ_NOTIFY_TYPE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_MESSAGE, RES_RESULT_BAD_REQUEST_MSG);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            return resultMap;
        }
        if (!StringUtils.equals("OVER_CONTINUE", getRequestString(REQ_NOTIFY_TYPE)))
            return successMessage();

        Student currentStudent = getCurrentStudent();
        if (currentStudent == null) {
            return failMessage(RES_RESULT_STUDENT_ID_ERROR_MSG);
        }
        String key = "STUDNET_NOTIFYOVERTIME_" + currentStudent.getId();
        String content = "家长你好，你的孩子" + currentStudent.fetchRealname() + "持续使用手机超过40分钟。请让孩子适度休息，并注意用眼健康！";
        return sendNotifyToParent(key, content);
    }

    /**
     * 给家长发通知
     * @param key 缓存key， 一天只发一次
     * @param content 消息内容
     * @return
     */
    private MapMessage sendNotifyToParent(String key, String content) {
        Student currentStudent = getCurrentStudent();
        if (currentStudent == null)
            return failMessage(RES_RESULT_STUDENT_ID_ERROR_MSG);

        CacheObject<Object> objectCacheObject = UserCache.getUserCache().get(key);
        if (objectCacheObject != null && objectCacheObject.getValue() != null) {
            boolean isSent = SafeConverter.toBoolean(objectCacheObject.getValue());
            if (isSent)
                return successMessage();
        }
        List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(currentStudent.getId());

        if (CollectionUtils.isEmpty(studentParentRefs))
            return successMessage();
        List<Long> parentIds = studentParentRefs.stream().map(StudentParentRef::getParentId).collect(Collectors.toList());
        String newId = randomPickOneNewId();
        JxtNews jxtNews = jxtNewsLoaderClient.getJxtNews(newId);
        if (jxtNews == null) {
            logger.warn("指定的资讯 id 不存在 ！！！newsId = {}", newId);
            return successMessage();
        }
        String newsUrl = JxtNewsUtil.generateJxtNewsDetailViewForPushWihtoutHost(jxtNews, "notifyovertime", false);
        String messageTag = ParentMessageTag.通知.name();
        parentIds.forEach(parentId -> {
            AppMessage message = new AppMessage();
            message.setUserId(parentId);
            message.setMessageType(ParentMessageType.REMINDER.getType());
            message.setTitle("通知");
            message.setContent(content);
            message.setLinkUrl(newsUrl);
            message.setLinkType(1);
            Map<String, Object> extInfo = new HashMap<>();
            extInfo.put("tag", messageTag);
            message.setExtInfo(extInfo);
            messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
        });

        // 发送push
        Map<String, Object> jpushExtInfo = new HashMap<>();
        jpushExtInfo.put("studentId", "");
        jpushExtInfo.put("url", newsUrl);
        jpushExtInfo.put("tag", messageTag);
        jpushExtInfo.put("shareType", ParentMessageShareType.NO_SHARE_VIEW.name());
        jpushExtInfo.put("shareContent", "");
        jpushExtInfo.put("shareUrl", "");
        jpushExtInfo.put("s", ParentAppPushType.NOTICE.name());
        appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PARENT, parentIds, jpushExtInfo);

        UserCache.getUserCache().set(key, DateUtils.getCurrentToDayEndSecond(), "true");
        return MapMessage.successMessage();
    }

    private String randomPickOneNewId() {
        return RandomUtils.pickRandomElementFromList(timeOverNewsIdList);
    }
}
