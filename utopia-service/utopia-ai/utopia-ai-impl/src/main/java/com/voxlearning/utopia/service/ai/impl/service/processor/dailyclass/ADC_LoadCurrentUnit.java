package com.voxlearning.utopia.service.ai.impl.service.processor.dailyclass;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.ai.constant.AIBookStatus;
import com.voxlearning.utopia.service.ai.context.AIUserDailyClassContext;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.util.DateExtentionUtil;
import com.voxlearning.utopia.service.ai.util.StringExtUntil;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Named
public class ADC_LoadCurrentUnit extends AbstractAiSupport implements IAITask<AIUserDailyClassContext> {

    @Override
    public void execute(AIUserDailyClassContext context) {
        if (StringUtils.isNotBlank(context.getUnitId())) {
            return;
        }
        context.getExtMap().put("userEnName", Optional.ofNullable(context.getUser().getProfile())
                .filter(e -> StringUtils.isNotBlank(e.getNickName()))
                .map(UserProfile::getNickName)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(e -> StringExtUntil.getPinyinString(e)).orElse(""));
        Date current = new Date();
        if (current.before(context.getBeginDate())) {
            long dayDiff = DateUtils.dayDiff(context.getBeginDate(), current) + 1;
            context.getExtMap().put("dayDiff", dayDiff);
            context.getExtMap().put("beginDate",  DateUtils.dateToString(context.getBeginDate(), "M月d日"));
            context.setStatus(AIBookStatus.UnBegin);
            context.terminateTask();
            return;
        }

        if (current.after(context.getEndDate())) {
            // 查看完成关卡情况
            List<AIUserUnitResultHistory> unitResultHistoryList = aiUserUnitResultHistoryDao.loadByUserId(context.getUser().getId());
            if (CollectionUtils.isNotEmpty(unitResultHistoryList)) {
                List<AIUserUnitResultHistory> thisBookList = unitResultHistoryList.stream().filter(h -> StringUtils.equals(h.getBookId(), context.getBookId()))
                        .filter(AIUserUnitResultHistory::getFinished)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(thisBookList) && thisBookList.size() >= 8) {
                    context.setStatus(AIBookStatus.GetTarget);
                } else {
                    context.setStatus(AIBookStatus.UnGetTarget);
                }
            } else {
                context.setStatus(AIBookStatus.UnGetTarget);
            }
            context.terminateTask();
            return;
        }
        // 进行中 获取今日课程
        if (current.after(context.getBeginDate()) && current.before(context.getEndDate())) {
            // 获取教材课单元
            List<NewBookCatalog> units = fetchUnitListExcludeTrial(context.getBookId());
            if (CollectionUtils.isEmpty(units)) {
                context.errorResponse("no unit");
                return;
            }
            int days = DateExtentionUtil.daysDiffExcludeWeekend(context.getBeginDate(), current);
            int index = days - 1;
            NewBookCatalog todayUnit = index < units.size() ? units.get(index) : units.get(0);
            context.setUnit(todayUnit != null ? todayUnit : units.get(0));
            context.setRank(days <= units.size() ? days : 1);
            context.setStatus(AIBookStatus.InTime);
        }
    }
}
