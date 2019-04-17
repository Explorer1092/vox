package com.voxlearning.utopia.service.afenti.impl.service;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.bean.afenti.AfentiParentReport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.afenti.api.AfentiActivityService;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiActivityType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserBookRef;
import com.voxlearning.utopia.service.afenti.impl.athena.AfentiParentReportServiceClient;
import com.voxlearning.utopia.service.afenti.impl.service.activity.AfentiActivityDataAssembler;
import com.voxlearning.utopia.service.afenti.impl.service.activity.AfentiActivityDataAssemblerManager;
import com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term.FetchTermQuizInfoProcessor;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.UtopiaAfentiSpringBean;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyService;
import com.voxlearning.utopia.service.vendor.api.entity.StudyAppData;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.alps.annotation.meta.Subject.CHINESE;
import static com.voxlearning.alps.annotation.meta.Subject.MATH;
import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.DEFAULT;
import static com.voxlearning.utopia.service.integral.api.constants.IntegralType.*;

/**
 * @author Ruib
 * @since 2016/8/15
 */
@Named
@ExposeService(interfaceClass = AfentiActivityService.class)
public class AfentiActivityServiceImpl extends UtopiaAfentiSpringBean implements AfentiActivityService {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;

    @Inject AfentiActivityDataAssemblerManager manager;
    @Inject FetchTermQuizInfoProcessor processor;
    @ImportService(interfaceClass = MySelfStudyService.class)
    private MySelfStudyService mySelfStudyService;
    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private AfentiParentReportServiceClient afentiParentReportServiceClient;

    @Override
    public Map<String, Object> fetchActivityData(StudentDetail student, AfentiActivityType type, Subject subject) {
        if (null == student || null == student.getClazz() || null == type) return Collections.emptyMap();
        AfentiActivityDataAssembler assembler = manager.getAssembler(type);
        if (null == assembler) return Collections.emptyMap();
        return assembler.assemble(student, subject);
    }

    @Override
    public MapMessage receiveLoginReward(StudentDetail student, Subject subject) {

        if (asyncAfentiCacheService.AfentiUserLoginRewardCacheManager_existRecord(student, subject).take()) {
            return MapMessage.errorMessage("已经领取");
        }

        long nowDayNum = DateUtils.dayDiff(new Date(), WeekRange.current().getStartDate()) + 1;
        Set<Integer> records = asyncAfentiCacheService.AfentiUserLoginRewardCacheManager_loadRecords(student, subject)
                .take();
        //学豆奖励
        Integer beans = 5;
        //连续六天签到双倍学豆
        if (nowDayNum == 7 && CollectionUtils.isNotEmpty(records) && !records.contains(nowDayNum) && records.size() == 6) {
            beans = beans * 2;
        }

        //每天登陆赠送5个学豆
        IntegralHistory integralHistory = new IntegralHistory();
        IntegralType integralType = subject == MATH ? AFENTI_MATH_LOGIN_REWARD
                : (subject == CHINESE ? AFENTI_CHINESE_LOGIN_REWARD : AFENTI_EXAM_LOGIN_REWARD);
        integralHistory.setUserId(student.getId());
        integralHistory.setIntegral(beans);
        integralHistory.setIntegralType(integralType.getType());
        integralHistory.setComment(integralType.getDescription());
        MapMessage message = userIntegralService.changeIntegral(student, integralHistory);
        if (message.isSuccess()) {
            asyncAfentiCacheService.AfentiUserLoginRewardCacheManager_addRecord(student, subject)
                    .awaitUninterruptibly();
        } else {
            logger.error("changeIntegral failed,message={}", message.getInfo());
            return MapMessage.errorMessage(DEFAULT.getInfo())
                    .setErrorCode(DEFAULT.getCode())
                    .set("errMsg", "增加学豆失败");
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage loadLoginReportData(Long studentId, String orderProductServiceType) {
        Map<String, Object> result = new HashMap<>();
        // 学生信息
        StudentDetail detail = studentLoaderClient.loadStudentDetail(studentId);
        if (detail == null) {
            return MapMessage.errorMessage("学生不存在");
        }
        result.put("studentName", detail.fetchRealname());
        result.put("img", detail.fetchImageUrl());
        // 获取用户afenti教材年级
        AfentiLearningPlanUserBookRef ref = afentiLearningPlanUserBookRefPersistence.findByUserIdAndSubject(studentId, AfentiUtils.getSubject(OrderProductServiceType.safeParse(orderProductServiceType)))
                .stream().filter(r -> Boolean.TRUE.equals(r.getActive()))
                .filter(r -> r.getType() == AfentiLearningType.castle)
                .findFirst().orElse(null);
        result.put("clazzLevel", detail.getClazzLevel().getDescription());
        if (ref != null) {
            NewBookProfile book = newContentLoaderClient.loadBooks(Collections.singletonList(ref.getNewBookId()))
                    .values().stream().findFirst().orElse(null);
            if (book != null) {
                ClazzLevel level = ClazzLevel.parse(book.getClazzLevel());
                if (level != null) {
                    result.put("clazzLevel", level.getDescription());
                }
            }
        }

        // 学习进度
        StudyAppData data = mySelfStudyService.loadStudyAppData(studentId, SelfStudyType.fromOrderType(OrderProductServiceType.safeParse(orderProductServiceType))).getUninterruptibly();
        if (data != null) {
            result.put("progress", data.getProgress());
        }
        // 昨日练习统计 如果没有， 统计本学期的数据 大数据提供
        String flag = "yesterday";
        AfentiParentReport report = afentiParentReportServiceClient.getAfentiParentReportService()
                .getAfentiParentReport(studentId, AfentiUtils.getSubject(OrderProductServiceType.safeParse(orderProductServiceType)), true);
        if (report == null || report.getCompletion() == null) {
            // 获取本学期
            flag = "term";
            report = afentiParentReportServiceClient.getAfentiParentReportService()
                    .getAfentiParentReport(studentId, AfentiUtils.getSubject(OrderProductServiceType.safeParse(orderProductServiceType)), false);
            if (report == null || report.getCompletion() == null) {
                flag = "";// 无数据
            }
        }
        result.put("dataFlag", flag);
        if (StringUtils.isNotBlank(flag) && report != null) {
            result.put("completion", report.getCompletion());   // 做题数
            result.put("correctRate", report.getCorrectRate()); // 正确率
            result.put("kpCount", report.getKpCount());         // 知识点数
            result.put("daysOfUse", report.getDaysOfUse());     // 累积使用天数
            result.put("wrongNumber", report.getWrongNumber()); // 错题数
        }
        // 获取大家也在用
        List<Map<String, Object>> purchaseList = asyncAfentiCacheService.AfentiPurchaseInfosCacheManager_getRecords(detail).take();
        result.put("purchaseList", purchaseList);
        // 获取剩余天数
        AppPayMapper payMapper = userOrderLoaderClient.getUserAppPaidStatus(orderProductServiceType, studentId);
        if (payMapper != null) {
            // 向上取整 天数+1 我们的订单有效期都是截止到最后一天的23:59:59
            result.put("daysToExpire", payMapper.getDayToExpire() == null ? 0 : SafeConverter.toInt(payMapper.getDayToExpire()) + 1); // 还有多少天过期
        }
        return MapMessage.successMessage().add("result", result);
    }
}

