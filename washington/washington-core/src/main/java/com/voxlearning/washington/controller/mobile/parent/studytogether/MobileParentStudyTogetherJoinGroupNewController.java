package com.voxlearning.washington.controller.mobile.parent.studytogether;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.parent.api.StudyTogetherAssistShareService;
import com.voxlearning.utopia.service.parent.api.StudyTogetherJoinGroupV2Service;
import com.voxlearning.utopia.service.parent.api.constants.StudyTogetherJoinActiveConstants;
import com.voxlearning.utopia.service.parent.api.constants.StudyTogetherSummerPackageActivity;
import com.voxlearning.utopia.service.parent.api.constants.StudyTogetherXBPayConstants;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.ParentJoinGroup;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.ParentJoinLessonRef;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.AssistInfoMapper;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.CardLessonMapper;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.ShareInfoConfigMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author jiangpeng
 * @since 2018-06-15 上午3:18
 **/
@Controller
@RequestMapping(value = "/parentMobile/study_together/join_group_v2")
public class MobileParentStudyTogetherJoinGroupNewController extends AbstractMobileParentStudyTogetherController {


    @ImportService(interfaceClass = StudyTogetherJoinGroupV2Service.class)
    private StudyTogetherJoinGroupV2Service studyTogetherJoinGroupV2Service;

    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;

    @ImportService(interfaceClass = StudyTogetherAssistShareService.class)
    private StudyTogetherAssistShareService studyTogetherAssistShareService;


    @RequestMapping(value = "join.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage parentJoinGroup() {
        User parent = currentParent();
        if (parent == null)
            return MapMessage.errorMessage().setErrorCode("666");
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId))
            return MapMessage.errorMessage("课程id缺失");
        String joinGroupId = getRequestString("group_id");
        if (StringUtils.isBlank(joinGroupId))
            return MapMessage.errorMessage("团拼主id缺失");
        return studyTogetherJoinGroupV2Service.parentJoinGroup(lessonId, joinGroupId, parent.getId());
    }


    @RequestMapping(value = "create.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage parentCreateGroup() {
        User parent = currentParent();
        if (parent == null)
            return MapMessage.errorMessage().setErrorCode("666");
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId))
            return MapMessage.errorMessage("课程id缺失");
        return studyTogetherJoinGroupV2Service.parentCreateGroup(parent.getId(), lessonId);
    }


    @RequestMapping(value = "wechat/group_info.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage groupInfoWeChat() {
        User parent = currentParent();
        boolean isLogin = parent != null;
        String joinGroupId = getRequestString("group_id");
        if (StringUtils.isBlank(joinGroupId))
            return MapMessage.errorMessage("拼主ID呢？");
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId))
            return MapMessage.errorMessage("课程id呢？");
        StudyLesson studyLesson = getStudyLesson(lessonId);
        if (studyLesson == null)
            return MapMessage.errorMessage("课程不存在");

        if (!studyLesson.joinNeedGroup())
            return MapMessage.errorMessage("此课程不支持拼团报名！");
        AlpsFuture<Long> joinCountFuture = studyTogetherServiceClient.loadLessonJoinCount(lessonId);
        ParentJoinLessonRef parentJoinLessonRef = isLogin ? studyTogetherServiceClient.loadParentJoinLessonRef(lessonId, parent.getId()) : null;

        long studentId = getRequestLong("sid");
        StudyGroup studyGroup = !isLogin || studentId == 0 ? null : studyTogetherServiceClient.loadStudentGroupByLessonId(studentId, lessonId);
        Map<String, Object> lessonInfoMap = lessonInfoMap(studyLesson, joinCountFuture, parentJoinLessonRef, studyGroup);

        MapMessage resultMsg = MapMessage.successMessage().add("lesson_info", lessonInfoMap);

        Map<String, Object> groupInfoMap;
        //如果没有登录，
        if (!isLogin) {
            ParentJoinGroup parentJoinGroup = studyTogetherJoinGroupV2Service.loadByJoinGroupId(joinGroupId);
            if (parentJoinGroup == null) {
                return MapMessage.errorMessage("对不起，此拼团不存在哦！");
            }
            groupInfoMap = groupInfoMap(parentJoinGroup, 0L, studyLesson);
            return resultMsg.add("group_detail", groupInfoMap);
        }
        //如果登录，则首先看当前用户的团状态
        ParentJoinGroup showJoinGroup = studyTogetherJoinGroupV2Service.loadParentJoinGroup(lessonId, parent.getId());
        //如果当前用户没有任何团，或者团已过期，则显示当前团。
        if (showJoinGroup == null ) {
            showJoinGroup = studyTogetherJoinGroupV2Service.loadByJoinGroupId(joinGroupId);
            if (showJoinGroup == null) {
                return MapMessage.errorMessage("对不起，此拼团不存在哦！");
            }
        }
        groupInfoMap = groupInfoMap(showJoinGroup, parent.getId(), studyLesson);
        boolean activeDirectly = studyTogetherServiceClient.couldActiveDirectly(parent.getId(), studentId, lessonId);
        return resultMsg.add("group_detail", groupInfoMap).add("active_directly", activeDirectly);
    }


    @RequestMapping(value = "inapp/group_info.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage groupInfo() {
        User parent = currentParent();
        if (parent == null)
            return noLoginResult;
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId))
            return MapMessage.errorMessage("课程ID错误");

        StudyLesson studyLesson = getStudyLesson(lessonId);
        if (studyLesson == null)
            return MapMessage.errorMessage("课程不存在");
        if (!studyLesson.joinNeedGroup())
            return MapMessage.errorMessage("此课程不支持拼团报名！");
        AlpsFuture<Long> joinCountFuture = studyTogetherServiceClient.loadLessonJoinCount(lessonId);
        ParentJoinLessonRef parentJoinLessonRef = studyTogetherServiceClient.loadParentJoinLessonRef(lessonId, parent.getId());

        long studentId = getRequestLong("sid");
        StudyGroup studyGroup = studentId == 0 ? null : studyTogetherServiceClient.loadStudentGroupByLessonId(studentId, lessonId);
        Map<String, Object> lessonInfoMap = lessonInfoMap(studyLesson, joinCountFuture, parentJoinLessonRef, studyGroup);

        ParentJoinGroup joinGroup = studyTogetherJoinGroupV2Service.loadParentJoinGroup(lessonId, parent.getId());
        if (joinGroup == null)
            return MapMessage.errorMessage("尚未创建拼团或者没加入拼团~");
        Map<String, Object> groupInfoMap = groupInfoMap(joinGroup, parent.getId(), studyLesson);
        boolean activeDirectly = studyTogetherServiceClient.couldActiveDirectly(parent.getId(), studentId, lessonId);
        return MapMessage.successMessage().add("lesson_info", lessonInfoMap).add("group_detail", groupInfoMap).add("active_directly", activeDirectly);

    }

    private Map<String, Object> groupInfoMap(ParentJoinGroup joinGroup, Long currentParentId, StudyLesson studyLesson) {
        Map<String, Object> groupInfoMap = new HashMap<>();
        List<Map<String, Object>> membersInfoList = generateGroupMemberMapList(joinGroup, currentParentId);
        groupInfoMap.put("is_full", joinGroup.groupSuccess(studyLesson.joinGroupLimit()));
//        groupInfoMap.put("expiry_countdown", joinGroup.expiryCountDown(studyLesson));
//        groupInfoMap.put("is_expire", joinGroup.isExpire(studyLesson));
        groupInfoMap.put("remainder", studyLesson.joinGroupLimit() - joinGroup.memberCount() - 1);
        groupInfoMap.put("members", membersInfoList);
        // TODO: 2018/6/15 确认这个 owner_Id 字段前端用来干啥了,   2018/6/19  确认了，新加一个group_id来标识团，owner_id为团主id
        groupInfoMap.put("group_id", joinGroup.getId());
        groupInfoMap.put("owner_id", joinGroup.getOwnerId());
        //如果团过期||当前用户不在团里，就返回199的productId
        List<Long> memberIdList = !CollectionUtils.isEmpty(joinGroup.getMemberIdList()) ? joinGroup.getMemberIdList() : new ArrayList<>();
        memberIdList.add(joinGroup.getOwnerId());
//        groupInfoMap.put("product_id", (joinGroup.isExpire(studyLesson) || !memberIdList.contains(currentParentId)) ? studyLesson.orignalProductId() : joinGroup.getProductId());
        OrderProduct product = StringUtils.isBlank(joinGroup.getProductId()) ? null :
                userOrderLoaderClient.loadOrderProductById(joinGroup.getProductId());
        groupInfoMap.put("price", product == null ? "199" : product.getPrice());
        return groupInfoMap;
    }


    @RequestMapping(value = "lesson_info.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage lessonInfo() {
        User parent = currentParent();
        String lessonId = getRequestString("lesson_id");
        if (StringUtils.isBlank(lessonId))
            return MapMessage.errorMessage("课程ID错误");
        Long studentId = getRequestLong("sid");
        StudyLesson studyLesson = getStudyLesson(lessonId);
        if (studyLesson == null)
            return MapMessage.errorMessage(" 课程不存在");
        AlpsFuture<Long> joinCountFuture = studyTogetherServiceClient.loadLessonJoinCount(lessonId);
        ParentJoinLessonRef parentJoinLessonRef = parent != null ? studyTogetherServiceClient.loadParentJoinLessonRef(lessonId, parent.getId()) : null;
        StudyGroup studyGroup = loadParentStudentActiveGroup(parent, studentId, lessonId);
        Map<String, Object> lessonInfoMap = lessonInfoMap(studyLesson, joinCountFuture, parentJoinLessonRef, studyGroup);
        MapMessage successMessage = MapMessage.successMessage().add("lesson_info", lessonInfoMap);
        boolean activeDirectly;
        if (StudyTogetherJoinActiveConstants.payTestSkuId.equals(lessonId)) {
            StudyGroup studyGroup1 = loadParentStudentActiveGroup(parent, studentId, StudyTogetherJoinActiveConstants.payTestOldSkuId);
            activeDirectly = studyGroup1 != null;
        } else {
            activeDirectly = parent == null ? false : studyTogetherServiceClient.couldActiveDirectly(parent.getId(), studentId, lessonId);
        }
        String productId = studyLesson.orignalProductId();
        if (studyLesson.joinNeedGroup()) {
            ParentJoinGroup parentJoinGroup = parent == null ? null : studyTogetherJoinGroupV2Service.loadParentJoinGroup(lessonId, parent.getId());
            OrderProduct product = parentJoinGroup == null || StringUtils.isBlank(parentJoinGroup.getProductId()) ? null :
                    userOrderLoaderClient.loadOrderProductById(parentJoinGroup.getProductId());
            Map<String, Object> groupMap = new HashMap<>();
            groupMap.put("has_group", parentJoinGroup != null);
//            groupMap.put("product_id", product == null || parentJoinGroup.isExpire(studyLesson) ? studyLesson.orignalProductId() : product.getId());
//            groupMap.put("price", product == null || parentJoinGroup.isExpire(studyLesson) ? 199 : product.getPrice());
            if (parentJoinGroup != null) {
                groupMap.put("group_id", parentJoinGroup.getId());
                groupMap.put("group_owner_id", parentJoinGroup.getOwnerId());
            }
            successMessage.add("group_info", groupMap);
        } else if (studyLesson.joinMustPay()) {
            long countDown = StudyTogetherXBPayConstants.countDown();
            if (countDown <= 0)
                activeDirectly = false;
            if (activeDirectly) {
                productId = studyLesson.secondProductId();
            } else {
                productId = studyLesson.orignalProductId();
            }

        } else if (studyLesson.safeGetJoinWay() == 5) {
            productId = studyLesson.orignalProductId();
        } else if (studyLesson.safeGetJoinWay() == 6){
            AssistInfoMapper assistInfo = studyTogetherAssistShareService.getAssistInfo(parent == null ? null : parent.getId(), SafeConverter.toInt(lessonId));
            successMessage.add("assist_info", assistInfo);
            productId = studyLesson.orignalProductId();
        } else if (studyLesson.safeGetJoinWay() == 9){
            Map<String, Object> map = new HashMap<>();
            Boolean shared = false;
            if (parent != null) {
                shared = studyTogetherServiceClient.getStudyTogetherShareDiscountService().parentSkuIsShared(parent.getId(), lessonId);
            }
            OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
            if (orderProduct != null){
                map.put("product_id", orderProduct.getId());
                map.put("price", orderProduct.getPrice());
                map.put("discount_price", orderProduct.getPrice().multiply(new BigDecimal("0.8")).intValue());
            }
            map.put("shared", shared == null ? false : shared);
            successMessage.add("share_discount_info", map);
        }

        if (StringUtils.isNotBlank(productId) && studyLesson.purchaseSupport()) {
            Map<String, Object> payInfo = new HashMap<>(2);
            OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
            if (orderProduct != null) {
                payInfo.put("product_id", productId);
                payInfo.put("price", orderProduct.getPrice());
            } else {
                logger.warn("没有配置对应的商品，skuId = {}", studyLesson.getLessonId());
            }
            successMessage.add("pay_info", payInfo);
        }

        return successMessage
                .add("active_directly", activeDirectly)
                .add("dynamic_adapt", SafeConverter.toBoolean(studyLesson.getDynamicAdapt()))
                .add("is_login", parent != null)
                .add("summer_activity", StudyTogetherSummerPackageActivity.isActivityLesson(lessonId));
    }

    private StudyGroup loadParentStudentActiveGroup(User parent, Long studentId, String lessonId) {
        if (parent == null)
            return null;
        if (studentId != 0L) {
            return studyTogetherServiceClient.loadStudentGroupByLessonId(studentId, lessonId);
        }
        List<User> users = studentLoaderClient.loadParentStudents(parent.getId());
        if (CollectionUtils.isEmpty(users))
            return null;
        for (User user : users) {
            StudyGroup studyGroup = studyTogetherServiceClient.loadStudentGroupByLessonId(user.getId(), lessonId);
            if (studyGroup != null)
                return studyGroup;
        }
        return null;
    }

    private Map<String, Object> lessonInfoMap(StudyLesson studyLesson, AlpsFuture<Long> joinCountFuture, ParentJoinLessonRef parentJoinLessonRef, StudyGroup studyGroup) {
        Map<String, Object> lessonInfoMap = new HashMap<>();
        lessonInfoMap.put("name", studyLesson.getTitle());
        lessonInfoMap.put("phase", studyLesson.getPhase());
        lessonInfoMap.put("start_date", DateUtils.dateToString(studyLesson.getOpenDate(), "MM.dd"));
        lessonInfoMap.put("times", studyLesson.getTimes());
        lessonInfoMap.put("clazz_level_text", studyLesson.getSuitableGradeText());
        lessonInfoMap.put("group_count", studyLesson.joinGroupLimit());
        lessonInfoMap.put("join_count", joinCountFuture.getUninterruptibly());
        lessonInfoMap.put("is_join", studyGroup != null || parentJoinLessonRef != null);
        lessonInfoMap.put("is_join_end", studyLesson.getSighUpEndDate().before(new Date()));
        lessonInfoMap.put("course_type", studyLesson.getCourseType());
        lessonInfoMap.put("is_active", studyGroup != null);
        lessonInfoMap.put("is_closed", studyLesson.isClosed());
        lessonInfoMap.put("join_way", studyLesson.safeGetJoinWay());
        lessonInfoMap.put("sku_id", studyLesson.getSpuId());
        lessonInfoMap.put("series_type", studyLesson.getSeriesType());
        lessonInfoMap.put("join_source", parentJoinLessonRef == null ? "" :parentJoinLessonRef.getJoinSource().name());
        ShareInfoConfigMapper shareInfoConfigMapper = pageBlockContentServiceClient.loadConfigObject(StudyTogetherAssistShareService.shareInfoConfigPage, StudyTogetherAssistShareService.shareInfoConfigBlock,
                ShareInfoConfigMapper.class);
        CardLessonMapper.ShareInfo shareInfo = CardLessonMapper.ShareInfo.defaultInfo;
        if (shareInfoConfigMapper != null){
            CardLessonMapper.ShareInfo shareInfo1 = shareInfoConfigMapper.get(SafeConverter.toString(studyLesson.getLessonId()));
            if (shareInfo1 != null){
                shareInfo = shareInfo1;
            }
        }
        lessonInfoMap.put("share_info", shareInfo);
        return lessonInfoMap;
    }
}
