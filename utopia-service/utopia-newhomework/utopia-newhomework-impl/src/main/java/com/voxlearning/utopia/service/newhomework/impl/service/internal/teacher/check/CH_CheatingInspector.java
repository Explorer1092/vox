/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.mutable.MutableLong;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.homework.api.mapper.CheckHomeworkIntegralDetail;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.PossibleCheatingHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.PossibleCheatingHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.PopupCategory.LOWER_RIGHT;
import static com.voxlearning.utopia.api.constant.PopupType.TEACHER_HOMEWORK_CHEATING;

/**
 * 作弊检查
 * <p>
 * 1. 满足(1)或者(2)，扣除金币，记录作弊
 * (1) 完成作业人数大于8，且有50%的学生作业完成时间少于10秒
 * (2) 有30%以上的学生在非在校时间完成作业，且IP数少于作业人数的10%，且平均作业完成时间在1分钟以内
 * <p>
 * 2. 如果作业中练习形式不存在PHOTO_OBJECTIVE和VOICE_OBJECTIVE，并且总题量少于三道题，扣除金币，但不记录作弊
 *
 * @author Ruib
 * @version 0.1
 * @since 2016/1/25
 */
@Named
public class CH_CheatingInspector extends SpringContainerSupport implements CheckHomeworkTask {

    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject
    private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject
    private PossibleCheatingHomeworkDao possibleCheatingHomeworkDao;

    private static final List<String> schoolHour = Arrays.asList("07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17");

    @Override
    public void execute(CheckHomeworkContext context) {
        if (context.getAccomplishment() == null || context.getAccomplishment().size() <= 0) return;
        if (context.isHomeworkQuantityNotEnough()) return;

        try {
            boolean few = checkIpDuplication(context);
            boolean fast = tooFast(context);

            if (few || fast) {
                context.setCheated(true);
                PossibleCheatingHomework possibleCheatingHomework = new PossibleCheatingHomework();
                possibleCheatingHomework.setHomeworkId(context.getHomeworkId());
                possibleCheatingHomework.setClazzId(context.getClazzId());
                possibleCheatingHomework.setTeacherId(context.getTeacherId());
                possibleCheatingHomework.setReason(getReason(fast, few));
                possibleCheatingHomework.setHomeworkType(context.getHomeworkType());
                possibleCheatingHomework.setRecordOnly(false);
                possibleCheatingHomework.setIsAddIntegral(false);
                CheckHomeworkIntegralDetail detail = context.getDetail();
                Map<Long, Object> teacherIntegral = new HashMap<>();
                teacherIntegral.put(context.getTeacherId(), detail.getTeacherIntegral());
                teacherIntegral.put(context.getClazzId(), detail.getClazzIntegral());
                possibleCheatingHomework.setTeacherIntegral(teacherIntegral);
                possibleCheatingHomework.setCreateDatetime(new Date());
                possibleCheatingHomeworkDao.insert(possibleCheatingHomework);

                String pattern = "亲爱的老师：系统检测到您{0}{1}的作业存在异常，本次作业的园丁豆被冻结，如有疑问，请致电客服400-160-1717";
                String text = MessageFormat.format(pattern,
                        DateUtils.dateToString(context.getHomework().getCreateAt(), "yyyy年MM月dd日"),
                        context.getClazz().formalizeClazzName());
                savePopup(text, context.getTeacherId());

                // 重置积分计算结果
                context.setDetail(new CheckHomeworkIntegralDetail(context.getHomeworkId(), 0, 0D, 0, 0));
            }
        } catch (Exception ex) {
            logger.error("作弊检查出错" + ex.getMessage(), ex);
        }
    }

    // 完成作业人数大于8，且有50%的学生作业完成时间少于10秒
    private boolean tooFast(CheckHomeworkContext context) {
        NewAccomplishment accomplishment = context.getAccomplishment();
        if (accomplishment.size() <= 8) return false;

        Map<Long, MutableLong> sid_tft_map = duration(context.getHomework(), accomplishment);
        BigDecimal total = new BigDecimal(sid_tft_map.size());
        BigDecimal below = new BigDecimal(sid_tft_map.values().stream().filter(ml -> ml.longValue() <= 10).count());
        return below.multiply(new BigDecimal(100)).divide(total, 0, BigDecimal.ROUND_HALF_UP).intValue() >= 50;
    }

    // 有30%以上的学生在非在校时间完成作业，且IP数少于作业人数的10%，且平均作业完成时间在1分钟以内
    private boolean checkIpDuplication(CheckHomeworkContext context) {
        NewAccomplishment accomplishment = context.getAccomplishment();
        int sc = context.getStudents().size();
        if (sc <= 0) return false;
        if (atSchool(accomplishment)) return false;

        // 完成作业人数占全班人数10%以上的才计算
        boolean enough = new BigDecimal(accomplishment.size()).divide(new BigDecimal(sc), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue() >= 10;
        if (!enough) return false;

        Set<String> ips = accomplishment.getDetails().values().stream()
                .filter(source -> StringUtils.isNotBlank(source.getIp()))
                .map(NewAccomplishment.Detail::getIp).collect(Collectors.toSet());
        // 如果ip数为0，则不处理，防止数据异常现象
        if (CollectionUtils.isEmpty(ips)) return false;

        boolean ipFlag = new BigDecimal(ips.size()).divide(new BigDecimal(accomplishment.size()), 2, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100)).intValue() < 10;
        return ipFlag && finishAvgSeconds(context.getHomework(), accomplishment) < 60;
    }

    // 如果有大于等于70%的作业时在学校完成的，返回true，否则返回false
    private boolean atSchool(NewAccomplishment accomplishment) {
        long count = accomplishment.getDetails().values().stream().filter(detail -> RuntimeMode.ge(Mode.STAGING) && schoolHour.contains(DateUtils.dateToString(detail.getAccomplishTime(), "HH"))).count();
        return new BigDecimal(count).divide(new BigDecimal(accomplishment.size()), 2, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100)).intValue() > 70;
    }

    private Integer finishAvgSeconds(NewHomework newHomework, NewAccomplishment accomplishment) {
        Map<Long, MutableLong> sid_tft_map = duration(newHomework, accomplishment);

        BigDecimal total = new BigDecimal(sid_tft_map.size());
        long totalTime = 0L;
        for (MutableLong time : sid_tft_map.values()) {
            totalTime = totalTime + time.longValue();
        }
        BigDecimal tt = new BigDecimal(totalTime);
        return tt.divide(total, 0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    // 计算完成作业的同学的总时长
    private Map<Long, MutableLong> duration(NewHomework newHomework, NewAccomplishment accomplishment) {
        Set<Long> sids = accomplishment.getDetails().keySet().stream().map(SafeConverter::toLong).collect(Collectors.toSet());
        Map<Long, MutableLong> sid_tft_map = sids.stream().collect(Collectors.toMap(o -> o, o -> new MutableLong(0)));
        Map<Long, NewHomeworkResult> results = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), sids, false);
        for (NewHomeworkResult result : results.values()) {
            if (!sid_tft_map.containsKey(result.getUserId())) continue;
            for (NewHomeworkResultAnswer answer : result.getPractices().values()) {
                sid_tft_map.get(result.getUserId()).add(answer.processDuration() != null ? answer.processDuration() : 0);
            }
        }
        return sid_tft_map;
    }

    private String getReason(boolean fast, boolean few) {
        StringBuilder reason = new StringBuilder();
        if (fast) reason.append("作业非教学目的 "); // 50%完成时间少于10秒
        if (few) reason.append("作业非学生本人完成 "); // IP数与完成作业人数比小于10%
        return reason.toString();
    }

    private void savePopup(String text, Long userId) {
        userPopupServiceClient.createPopup(userId).content(text).type(TEACHER_HOMEWORK_CHEATING).category(LOWER_RIGHT).create();
    }
}
