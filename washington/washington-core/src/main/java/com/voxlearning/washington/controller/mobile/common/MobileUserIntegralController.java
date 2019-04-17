package com.voxlearning.washington.controller.mobile.common;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.mapper.json.StringDateSerializer;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegralHistoryPagination;
import com.voxlearning.utopia.service.integral.client.IntegralHistoryLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.controller.open.v1.util.InternalOffRewardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_USER_INTEGRAL_HISTORY;

/**
 * @author jiangpeng
 * @since 16/5/27
 */
@Controller
@RequestMapping(value = "/usermobile/integral")
@Slf4j
public class MobileUserIntegralController extends AbstractMobileController {
    @Inject private IntegralHistoryLoaderClient integralHistoryLoaderClient;
    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @Inject private InternalOffRewardService internalOffRewardService;

    /**
     * 学豆记录
     * 老师端用
     * 中学学生奖品中心在在用
     */
    @RequestMapping(value = "/history.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage integralHistory() {
        User user = currentUser();
        if (user == null)
            return noLoginResult;
        MapMessage resultMap = new MapMessage();

        int pageNumber = getRequestInt("pageNumber", 1);
        int size = getRequestInt("pageSize", 10);
        // 获取金币前三个月的历史数据
        UserIntegralHistoryPagination pagination = userLoaderClient
                .loadUserIntegralHistories(user, 3, pageNumber - 1, size, null);
        List<IntegralHistory> historyList = pagination.getContent();
        resultMap.setSuccess(true);
        resultMap.add("unit", fetchUnit(user));
        if (CollectionUtils.isEmpty(historyList)) {
            return resultMap.add(RES_USER_INTEGRAL_HISTORY, new ArrayList<>()).add("usable", 0);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        historyList.forEach(p -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("integral", p.getIntegral());
            map.put("comment", p.getComment());
            map.put("date", StringDateSerializer.format(p.getCreatetime(), "yyyy年M月d日"));
            list.add(map);
        });
        resultMap.add(RES_USER_INTEGRAL_HISTORY, list);
        resultMap.add("currentPage", pageNumber);

        long usable = 0L;
        boolean offline = false;
        if (user.isTeacher()) {
            TeacherDetail teacherDetail = currentTeacherDetail();
            usable = teacherDetail.getUserIntegral().getUsable();
            offline = internalOffRewardService.offline(teacherDetail);
        } else if (user.isStudent()) {
            StudentDetail studentDetail = currentStudentDetail();
            usable = studentDetail.getUserIntegral().getUsable();
            offline = internalOffRewardService.offline(studentDetail);
        }
        resultMap.add("usable", usable);
        resultMap.add("offline", offline);
        return resultMap;

    }

    /**
     * 获取积分过期相关信息
     * 调用端：老师APP积分历史页面
     *
     * @return
     */
    @RequestMapping(value = "/loadexpireinfo.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage loadExpireInfo() {
        User user = currentUser();
        if (user == null || user.fetchUserType() != UserType.TEACHER) {
            return MapMessage.errorMessage("用户信息错误");
        }
        TeacherDetail teacher = (TeacherDetail) user;
        MapMessage result = MapMessage.successMessage();
        boolean show = !Arrays.asList("1", "2", "7", "8").contains(DateUtils.dateToString(new Date(), "M"));
        result.add("showTips", show);
        if (show) {
            // 过期积分 = 当前剩余积分 - 最近12个月或的积分（当月算一个月，奖品中心退款不算）
            long expired = 0;
            UserIntegral ui = teacher.getUserIntegral();
            if (ui != null && ui.getUsable() > 0) {
                long sum = integralHistoryLoaderClient.getIntegralHistoryLoader()
                        .sumLatestTwelveMonthsIntegralHistoriesExcludeNegativeAnd45(teacher.getId());
                if (teacher.isPrimarySchool() || teacher.isInfantTeacher()) {
                    expired = Math.max(0, ui.getUsable() - sum / 10);
                } else {
                    expired = Math.max(0, ui.getUsable() - sum);
                }
            }
            result.add("expired", expired); // 本月过期积分数量
            result.add("showDate", DateUtils.dateToString(MonthRange.current().getEndDate(), FORMAT_SQL_DATE));

            MonthRange mr = MonthRange.current();
            String month = DateUtils.dateToString(mr.getStartDate(), DateUtils.FORMAT_SQL_DATE);

            // 免过期福利完成次数
            result.add("finishCount", asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .TeacherExpiredIntegralFreeCacheManager_fetchCount(teacher.getId(), month)
                    .getUninterruptibly());
        }
        return result;
    }
}
