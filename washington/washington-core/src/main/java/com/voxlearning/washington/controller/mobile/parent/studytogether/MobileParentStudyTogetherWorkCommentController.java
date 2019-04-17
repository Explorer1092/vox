package com.voxlearning.washington.controller.mobile.parent.studytogether;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.galaxy.service.coin.api.DPCoinLoader;
import com.voxlearning.galaxy.service.coin.api.DPCoinService;
import com.voxlearning.galaxy.service.coin.api.entity.Coin;
import com.voxlearning.galaxy.service.coin.api.entity.CoinHistory;
import com.voxlearning.galaxy.service.coin.api.support.CoinHistoryBuilder;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherWorkCommentService;
import com.voxlearning.utopia.service.parent.api.StudyTogetherWorkCommentService;
import com.voxlearning.utopia.service.parent.api.cache.ParentCache;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyTogetherCommentCourse;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyTogetherStudentFeedback;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyTogetherWorkComment;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.wonderland.api.data.WonderlandResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 一起学-作业点评
 *
 * @author xuerui.zhang
 * @since 2018-07-09 15:41
 **/
@Controller
@RequestMapping(value = "/parentmobile/studytogether/workcomment")
public class MobileParentStudyTogetherWorkCommentController extends AbstractMobileParentStudyTogetherController {

    @ImportService(interfaceClass = StudyTogetherWorkCommentService.class)
    private StudyTogetherWorkCommentService workCommentService;

    @ImportService(interfaceClass = CrmStudyTogetherWorkCommentService.class)
    private CrmStudyTogetherWorkCommentService crmWorkCommentService;

    @ImportService(interfaceClass = DPCoinService.class)
    private DPCoinService dpCoinService;
    @ImportService(interfaceClass = DPCoinLoader.class)
    private DPCoinLoader dpCoinLoader;

    /**
     * 点评课课程内容
     */
    @ResponseBody
    @RequestMapping(value = "/poemcontent.vpage", method = RequestMethod.GET)
    public MapMessage getPoemContent() {
        User parent = currentParent();
        if (null == parent) {
            return noLoginResult;
        }
        Long studentId = getRequestLong("sid");
        String courseId = getRequestString("course_id");
        if (StringUtils.isBlank(courseId) || 0L == studentId) {
            return MapMessage.errorMessage("参数错误");
        }
        return workCommentService.getPoemContent(courseId, studentId);
    }

    /**
     * 保存用户诗词录音
     */
    @ResponseBody
    @RequestMapping(value = "/uploadvoice.vpage", method = RequestMethod.POST)
    public MapMessage uploadVoice() {
        User parent = currentParent();
        if (null == parent) {
            return noLoginResult;
        }
        String url = getRequestString("url");
        Long studentId = getRequestLong("sid");
        String courseId = getRequestString("course_id");
        Long duration = getRequestLong("duration");

        if (0L == studentId || StringUtils.isBlank(courseId) || StringUtils.isBlank(url) || 0L == duration) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            return AtomicLockManager.getInstance().wrapAtomic(workCommentService)
                    .keyPrefix("uploadVoice")
                    .keys(studentId, courseId)
                    .proxy()
                    .uploadVoice(studentId, courseId, url, parent.getId(), duration);
        } catch (Exception e) {
            logger.error("lock error {}", e.getMessage(), e);
            return WonderlandResult.ErrorType.DUPLICATED_OPERATION.result();
        }
    }

    /**
     * 微信分享诗词录音
     */
    @ResponseBody
    @RequestMapping(value = "/share.vpage", method = RequestMethod.GET)
    public MapMessage share() {
        Long studentId = getRequestLong("student_id");
        String courseId = getRequestString("course_id");
        if (0L == studentId || StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("参数错误~");
        }
        return workCommentService.getWorkCommentShareMeesage(studentId, courseId);
    }

    /**
     * 免费状态
     */
    @ResponseBody
    @RequestMapping(value = "/buy.vpage", method = RequestMethod.POST)
    public MapMessage getFreeStatus() {
        User parent = currentParent();
        if (null == parent) {
            return noLoginResult;
        }
        Long studentId = getRequestLong("sid");
        String courseId = getRequestString("course_id");
        if (0L == studentId || StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("参数错误~");
        }
        StudyTogetherCommentCourse commentCourse = crmWorkCommentService.loadCommentCourse(courseId);
        if (commentCourse == null || StringUtils.isNotBlank(commentCourse.getProductId())) {
            return MapMessage.errorMessage("无此课程或此课程非免费商品");
        }
        try {
            return AtomicLockManager.getInstance().wrapAtomic(workCommentService).keyPrefix("updateWorkComment")
                    .keys(studentId, courseId.trim())
                    .proxy()
                    .updateWorkCommentWithType(studentId, courseId.trim(), 0);
        } catch (Exception e) {
            logger.error("lock error {}", e.getMessage(), e);
            return WonderlandResult.ErrorType.DUPLICATED_OPERATION.result();
        }
    }

    /**
     * 学生反馈内容
     */
    @ResponseBody
    @RequestMapping(value = "/loadfeedback.vpage", method = RequestMethod.GET)
    public MapMessage getStudentFeedback() {
        Long studentId = getRequestLong("sid");
        String courseId = getRequestString("course_id");
        if (0L == studentId || StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("参数错误~");
        }
        try {
            return workCommentService.loadStudentFeedback(studentId, courseId);
        } catch (Exception e) {
            logger.error("load feedback error {}", e.getMessage(), e);
            return MapMessage.errorMessage("server error");
        }
    }

    /**
     * 添加学生反馈内容
     */
    @ResponseBody
    @RequestMapping(value = "/addfeedback.vpage", method = RequestMethod.POST)
    public MapMessage addStudentFeedback() {
        Long studentId = getRequestLong("sid");
        String courseId = getRequestString("course_id");
        int satisfaction = getRequestInt("satisfaction");
        String desc = getRequestString("desc");
        if (0L == studentId || StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("参数错误~");
        }
        MapMessage respMsg = workCommentService.loadStudentFeedback(studentId, courseId);
        if (respMsg.isSuccess()) {
            if ((Boolean) respMsg.get("is_feedback")) {
                return MapMessage.errorMessage("已提交反馈，无法重新提交");
            }
        }
        StudyTogetherStudentFeedback bean = new StudyTogetherStudentFeedback();
        bean.setId(StudyTogetherStudentFeedback.generateId(studentId, courseId));
        bean.setSatisfaction(satisfaction);
        bean.setStudentId(studentId);
        bean.setCourseId(courseId);
        if (StringUtils.isNotBlank(desc)) {
            bean.setTalkToTeacher(desc);
        }
        try {
            return AtomicLockManager.getInstance().wrapAtomic(workCommentService).keyPrefix("addStudentFeedback")
                    .keys(studentId, courseId.trim())
                    .proxy()
                    .addStudentFeedback(bean);
        } catch (Exception e) {
            logger.error("lock error {}", e.getMessage(), e);
            return WonderlandResult.ErrorType.DUPLICATED_OPERATION.result();
        }
    }

    /**
     * 学习币购买点评课程：type = 2
     */
    @ResponseBody
    @RequestMapping(value = "coinbuy.vpage", method = RequestMethod.POST)
    public MapMessage byCourseUsingCoin() {
        User parent = currentParent();
        if (null == parent) {
            return noLoginResult;
        }

        Long studentId = getRequestLong("sid");
        String courseId = getRequestString("course_id");
        if (0L == studentId || StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("参数错误~");
        }

        StudyTogetherWorkComment workComment = workCommentService.loadWorkCommnet(studentId, courseId);
        if (null == workComment) {
            return MapMessage.errorMessage("用户信息错误");
        }
        if (workComment.getIsBuy()) {
            return MapMessage.errorMessage("您已购买过此课程");
        }

        Coin coin = dpCoinLoader.loadCoin(studentId);
        if (null == coin || coin.getTotalCount() < 100) {
            return MapMessage.successMessage("学习币不足").add("has_coin", false);
        }

        StudyTogetherCommentCourse commentCourse = crmWorkCommentService.loadCommentCourse(courseId);
        if (null == commentCourse) {
            return MapMessage.errorMessage("不存在此课程");
        }

        Integer coinType;
        if (RuntimeMode.current().le(Mode.TEST)) {
            coinType = 57;
        } else {
            coinType = 46;
        }
        Student student = studentLoaderClient.loadStudent(studentId);
        CoinHistory history = new CoinHistoryBuilder().withType(coinType).withUserId(studentId)
                .withOperator(student.fetchRealname()).build();
        MapMessage respMsg = dpCoinService.changeCoin(history);
        if (!respMsg.isSuccess()) {
            return MapMessage.errorMessage("创建订单异常");
        }

        List<UserOrder> userOrders = userOrderLoader.loadUserOrderListIncludedCanceled(studentId);
        userOrders = userOrders.stream()
                .filter(order -> order.getPaymentStatus() == PaymentStatus.Unpaid)
                .filter(order -> OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.StudyMatesReview)
                .filter(order -> order.getProductId().equals(commentCourse.getProductId()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(userOrders)) {
            for (UserOrder order : userOrders) {
                MapMessage message = userOrderServiceClient.updateUserOrderStatus(order, PaymentStatus.Unpaid, OrderStatus.Canceled);
                if (!message.isSuccess()) {
                    logger.error("Update User order status fail, id " + order.getId() + ", user " + order.getUserId());
                    return MapMessage.errorMessage("server error");
                }
            }
        }
        try {
            return AtomicLockManager.getInstance().wrapAtomic(workCommentService).keyPrefix("updateWorkComment")
                    .keys(studentId, courseId.trim())
                    .proxy()
                    .updateWorkCommentWithType(studentId, courseId.trim(), 2).add("has_coin", true);
        } catch (Exception e) {
            logger.error("lock error {}", e.getMessage(), e);
            return WonderlandResult.ErrorType.DUPLICATED_OPERATION.result();
        }
    }

    /**
     * 添加金币
     */
    @ResponseBody
    @RequestMapping(value = "addcoin.vpage", method = RequestMethod.POST)
    public MapMessage addCoin() {
        User parent = currentParent();
        if (null == parent) {
            return noLoginResult;
        }

        Long studentId = getRequestLong("sid");
        String courseId = getRequestString("course_id");
        if (0L == studentId || StringUtils.isBlank(courseId)) {
            return MapMessage.errorMessage("参数错误~");
        }

        Integer coinType;
        if (RuntimeMode.current().le(Mode.TEST)) {
            coinType = 59;
        } else {
            coinType = 47;
        }
        try {
            String key = generateWorkCommentCoinCacheKey(studentId, courseId);
            Integer value = loadWorkCommentCoinCacheValue(studentId, courseId);
            Student student = studentLoaderClient.loadStudent(studentId);
            if (value == 0) {
                CoinHistory history = new CoinHistoryBuilder().withType(coinType).withUserId(studentId).withOperator(student.fetchRealname()).build();
                MapMessage respMsg = dpCoinService.changeCoin(history);
                if (!respMsg.isSuccess()) {
                    return MapMessage.errorMessage("创建订单异常");
                }
                ParentCache.getParentPersistenceCache().set(key, 0, 1);
            }
            return MapMessage.successMessage();
        } catch (Exception e) {
            logger.error("作业点评添加金币失败，原因：{}", e.getMessage(), e);
            return MapMessage.errorMessage("Server Error");
        }
    }

    private String generateWorkCommentCoinCacheKey(Long studentId, String courseId) {
        return CacheKeyGenerator.generateCacheKey("17xue:workcomment", new String[]{"PID", "AID"}, new Object[]{studentId, courseId});
    }

    private Integer loadWorkCommentCoinCacheValue(Long studentId, String courseId) {
        String key = generateWorkCommentCoinCacheKey(studentId, courseId);
        Integer value = ParentCache.getParentPersistenceCache().load(key);
        return SafeConverter.toInt(value);
    }

}
