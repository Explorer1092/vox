/*
package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.ChargeType;
import com.voxlearning.utopia.entity.campaign.WirelessCharging;
import com.voxlearning.utopia.entity.misc.TeacherInvitationConfig;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskLoaderClient;
import com.voxlearning.utopia.service.finance.client.WirelessChargingServiceClient;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.InviteHistory;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.ChargeType.TEACHER_INVITE_CERTIFICATE;

*/
/**
 * @author Xiaochao.Wei
 * @since 2017/8/21
 *//*

@Named
@ScheduledJobDefinition(
        jobName = "开学活动邀请老师奖励任务",
        jobDescription = "开学活动邀请老师奖励任务,每天运行一次",
        disabled = {Mode.DEVELOPMENT, Mode.UNIT_TEST, Mode.TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "0 0 4 * * ?"
)
@ProgressTotalWork(100)
public class AutoInviteTeacherRewardJob extends ScheduledJobWithJournalSupport {

    @Inject
    private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private WirelessChargingServiceClient wirelessChargingServiceClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private TeacherTaskLoaderClient teacherTaskLoaderClient;

    private int getMoney(TeacherInvitationConfig config, TeacherDetail teacherDetail) {
        Subject subject = teacherDetail.getSubject();
        if (teacherDetail.isPrimarySchool()) {
            if (subject == Subject.CHINESE) {
                return config.getChinese();
            }
            if (subject == Subject.MATH) {
                return config.getMath();
            }
            if (subject == Subject.ENGLISH) {
                return config.getEnglish();
            }
        } else if (teacherDetail.isJuniorTeacher()) {
            if (subject == Subject.MATH || subject == Subject.JMATH) {
                return config.getMiddleMath();
            }
            if (subject == Subject.ENGLISH || subject == Subject.JENGLISH) {
                return config.getMiddleEnglish();
            }
        }
        return 0;
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<InviteHistory> historyList = asyncInvitationServiceClient.getAsyncInvitationService().findTobeRewardList4().getUninterruptibly();
        List<TeacherInvitationConfig> teacherInvitationConfigs = asyncInvitationServiceClient.getAsyncInvitationService().queryTeacherInvitationConfig().getUninterruptibly();
        Map<Integer, TeacherInvitationConfig> mapConfig = teacherInvitationConfigs.stream().collect(Collectors.toMap(TeacherInvitationConfig::getCityCode, Function.identity(), (o1, o2) -> o1));

        for (InviteHistory history : historyList) {
            try {
                execute(mapConfig, history);
            } catch (Exception e) {
                logger.error(e.getMessage() + " InviteHistory id is " + history.getId(), e);
            }
        }
    }

    private void execute(Map<Integer, TeacherInvitationConfig> mapConfig, InviteHistory history) {
        if (history.getUserId() == null || history.getInviteeUserId() == null) {
            return;
        }
        // 过滤副账号
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(history.getUserId());
        if (mainTeacherId != null) {
            return;
        }
        Long inviMainTeacherId = teacherLoaderClient.loadMainTeacherId(history.getInviteeUserId());
        if (inviMainTeacherId != null) {
            return;
        }

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(history.getUserId());
        if (teacherDetail == null) {
            return;
        }
        TeacherDetail inviTeacherDetail = teacherLoaderClient.loadTeacherDetail(history.getInviteeUserId());
        if (inviTeacherDetail == null) {
            return;
        }
        // 被邀请者没有学科的话暂不处理
        if (inviTeacherDetail.getSubject() == null) {
            return;
        }

        // 中学数学只要求认证 其他需要完成新手任务
        boolean isJuniorMath = inviTeacherDetail.isJuniorTeacher() && (inviTeacherDetail.getSubject() == Subject.MATH || inviTeacherDetail.getSubject() == Subject.MMATH);
        boolean isPrimaryChineseEnglishMath = inviTeacherDetail.isPrimarySchool() &&
                (inviTeacherDetail.getSubject() == Subject.CHINESE || inviTeacherDetail.getSubject() == Subject.ENGLISH || inviTeacherDetail.getSubject() == Subject.MATH);

        if (isJuniorMath) {
            // 30 天有效, 超过没奖
            Date startDate = history.getCreateTime();
            Date endDate = DateUtils.ceiling(DateUtils.addDays(startDate, 30), Calendar.DAY_OF_MONTH);
            DateRange dateRange = new DateRange(startDate, endDate);
            if (!dateRange.contains(startDate)) {
                finish(history);
                return;
            }

            // 如果被邀请的人没认证,两边都不用发奖励, 等待下一次任务
            if ((inviTeacherDetail.getLastAuthDate() == null) || (inviTeacherDetail.fetchCertificationState() != AuthenticationState.SUCCESS)) {
                return;
            }
        } else {
            // 15 天有效, 超过没奖
            Date startDate = history.getCreateTime();
            Date endDate = DateUtils.ceiling(DateUtils.addDays(startDate, 15), Calendar.DAY_OF_MONTH);
            DateRange dateRange = new DateRange(startDate, endDate);
            if (!dateRange.contains(startDate)) {
                finish(history);
                return;
            }
            boolean b = teacherTaskLoaderClient.receiveAndFinishedRookieTask(inviTeacherDetail.getId());
            if (!b) {
                return;
            }

            // 其他学科，提前判断邀请者是不是没认证，避免更新成 9 后奖励没发
            if ((teacherDetail.getLastAuthDate() == null) || (teacherDetail.fetchCertificationState() != AuthenticationState.SUCCESS)) {
                return;
            }
        }
        boolean recipientExecuted = history.getIsChecked().equals(7); // 如果是7说明被邀请者已经发过了

        history.setIsChecked(9);
        Integer row = asyncInvitationServiceClient.getAsyncInvitationService().updateHistory(history).getUninterruptibly();
        if (row > 0) {
            TeacherInvitationConfig invitationConfig = mapConfig.get(teacherDetail.getCityCode());
            if (invitationConfig != null) {
                int money = getMoney(invitationConfig, inviTeacherDetail);

                if (money > 0) {
                    if (teacherDetail.getLastAuthDate() != null && teacherDetail.fetchCertificationState() == AuthenticationState.SUCCESS) {
                        // 邀请者加话费
                        Integer amount = money * 100;
                        UserAuthentication ua = userLoaderClient.loadUserAuthentication(history.getUserId());
                        wirelessChargingServiceClient.getWirelessChargingService()
                                .saveWirelessCharging(history.getUserId(),
                                        TEACHER_INVITE_CERTIFICATE,
                                        ua.getSensitiveMobile(),
                                        amount,
                                        "老师您好，您完成“新学期邀请新老师”活动获得的" + money + "元教学补贴充值成功，感谢您的使用！",
                                        "")
                                .awaitUninterruptibly();
                    } else {
                        // 由于没认证导致的发放失败,运营要求后期补
                        history.setIsChecked(7);
                        asyncInvitationServiceClient.getAsyncInvitationService().updateHistory(history).getUninterruptibly();
                    }
                } else {
                    // 说明地区没有配置奖励,终结掉邀请记录
                    finish(history);
                }
            }

            // 被邀请者如果是中数或者小英小语，也发奖励
            if ((isJuniorMath || isPrimaryChineseEnglishMath) && (!recipientExecuted)) {
                invitationConfig = mapConfig.get(inviTeacherDetail.getCityCode());
                if (invitationConfig == null) {
                    return;
                }
                int money = getMoney(invitationConfig, inviTeacherDetail);
                if (money <= 0) {
                    return;
                }

                // 小英&小语 如果老师新手任务已经发过奖励了，这里就不再发放
                if (isPrimaryChineseEnglishMath) {
                    UserAuthentication ua = userLoaderClient.loadUserAuthentication(history.getInviteeUserId());
                    WirelessCharging chargingItem = wirelessChargingServiceClient.getWirelessChargingService().findChargingSuccessList(ua.getSensitiveMobile())
                            .getUninterruptibly().stream()
                            .filter(p -> Objects.equals(p.getChargeType(), ChargeType.TEACHER_TASK.getType()))
                            .filter(p -> Objects.equals(p.getChargeDesc(), "老师任务奖励新手任务"))
                            .findAny().orElse(null);
                    if (chargingItem != null) {
                        return;
                    }
                }

                // 被邀请者加话费
                Integer amount = money * 100;
                UserAuthentication ua = userLoaderClient.loadUserAuthentication(history.getInviteeUserId());
                wirelessChargingServiceClient.getWirelessChargingService()
                        .saveWirelessCharging(history.getInviteeUserId(),
                                TEACHER_INVITE_CERTIFICATE,
                                ua.getSensitiveMobile(),
                                amount,
                                "老师您好，您接受“新学期邀请新老师”活动获得的" + money + "元教学补贴充值成功，感谢您的使用！",
                                "")
                        .awaitUninterruptibly();
            }
        }
    }

    private void finish(InviteHistory history) {
        history.setIsChecked(99); // 未找到配置或者已经异常终结
        asyncInvitationServiceClient.getAsyncInvitationService().updateHistory(history).getUninterruptibly();
    }

}
*/
