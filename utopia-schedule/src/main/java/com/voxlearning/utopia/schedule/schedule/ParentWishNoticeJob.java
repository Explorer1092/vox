package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.parent.api.ParentWishLoader;
import com.voxlearning.utopia.service.parent.api.ParentWishService;
import com.voxlearning.utopia.service.parent.api.entity.WishStudentId;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.client.DPStudentLoaderClient;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.common.Mode.STAGING;
import static com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static com.voxlearning.alps.spi.core.RuntimeModeLoader.getInstance;


@Named
@ScheduledJobDefinition(
        jobName = "心愿单通知孩子家长72小时未登录",
        jobDescription = "心愿单通知孩子家长72小时未登录，每天3:15执行执行。",
        disabled = {STAGING, Mode.TEST},
        cronExpression = "0 15 3 * * ?",
        ENABLED = true
)
public class ParentWishNoticeJob extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = ParentWishLoader.class)
    private ParentWishLoader parentWishLoader;
    @ImportService(interfaceClass = ParentWishService.class)
    private ParentWishService parentWishService;
    @Inject
    private UserLoginServiceClient userLoginServiceClient;
    @Inject
    private DPStudentLoaderClient studentLoaderClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        Integer limit = 1000;
        Long studentId = 0L;

        List<WishStudentId> wishStudentIds = parentWishLoader.loadWishStudentIdForNotice(studentId, limit);
        while (isNotEmpty(wishStudentIds)) {
            Date date = DateUtils.addHours(new Date(), -72);
            if (getInstance().current().le(STAGING)) {
                date = DateUtils.addHours(new Date(), -2);
            }

            Date finalDate = date;
            wishStudentIds = wishStudentIds.stream().filter(wsi -> null == wsi.getCreateDatetime() || wsi.getCreateDatetime().before(finalDate)).collect(Collectors.toList());

            if (isNotEmpty(wishStudentIds)) {
                for (WishStudentId wishStudentId : wishStudentIds) {
                    List<StudentParent> parents = studentLoaderClient.loadStudentParent(wishStudentId.getId());
                    if (CollectionUtils.isEmpty(parents)) {
                        continue;
                    }

                    List<Long> parentIds = parents.stream().map(x -> x.getParentUser().getId()).collect(Collectors.toList());
                    Map<Long, Date> times = userLoginServiceClient.findUserLastLoginTime(parentIds);
                    if (MapUtils.isEmpty(times)) {
                        continue;
                    }

                    Date parentLastLoginTime = times.values().stream().max(Date::compareTo).orElse(null);
                    if (null == parentLastLoginTime) {
                        continue;
                    }

                    if (parentLastLoginTime.before(date)) {
                        parentWishService.sendNoticeToStudent(wishStudentId.getId());
                    }
                }

                Long sid = wishStudentIds.stream().map(WishStudentId::getId).max(Comparator.naturalOrder()).orElse(null);
                if (null == sid) {
                    break;
                }
                wishStudentIds = parentWishLoader.loadWishStudentIdForNotice(sid, limit);
            }
        }
    }
}
