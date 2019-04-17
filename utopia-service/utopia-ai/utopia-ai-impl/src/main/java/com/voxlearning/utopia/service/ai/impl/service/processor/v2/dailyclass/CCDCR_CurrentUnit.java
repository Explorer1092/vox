package com.voxlearning.utopia.service.ai.impl.service.processor.v2.dailyclass;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.ai.constant.AIBookStatus;
import com.voxlearning.utopia.service.ai.data.StoneBookData;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsContentDailyClassContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.util.DateExtentionUtil;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class CCDCR_CurrentUnit extends AbstractAiSupport implements IAITask<ChipsContentDailyClassContext> {

    @Override
    public void execute(ChipsContentDailyClassContext context) {
        if (StringUtils.isNotBlank(context.getUnitId())) {
            return;
        }

        Date current = new Date();
        //课程开始前
        if (current.before(context.getBeginDate())) {
            context.getExtMap().put("dayDiff", DateExtentionUtil.dayDiffCeil(current, context.getBeginDate()));
            context.getExtMap().put("beginDate",  DateUtils.dateToString(context.getBeginDate(), "M月d日"));
            context.setStatus(AIBookStatus.UnBegin);
            return;
        }

        StoneBookData bookData = Optional.ofNullable(stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(context.getBookRef().getBookId())))
                .filter(MapUtils::isNotEmpty)
                .map(e -> e.get(context.getBookRef().getBookId()))
                .map(StoneBookData::newInstance)
                .filter(e -> e.getJsonData() != null && CollectionUtils.isNotEmpty(e.getJsonData().getChildren()))
                .orElse(null);
        if (bookData == null || bookData.getJsonData() == null || CollectionUtils.isEmpty(bookData.getJsonData().getChildren())) {
            context.errorResponse("没有教材");
            return;
        }

        List<AIUserUnitResultHistory> unitResultHistoryList = aiUserUnitResultHistoryDao.loadByUserId(context.getUser().getId());

        //课程结束后
        if (current.after(context.getEndDate())) {
            if (CollectionUtils.isNotEmpty(unitResultHistoryList)) {
                List<AIUserUnitResultHistory> thisBookList = unitResultHistoryList.stream()
                        .filter(h -> StringUtils.equals(h.getBookId(), context.getBookRef().getBookId()))
                        .filter(e -> !chipsContentService.isTrailUnit(e.getUnitId()))
                        .filter(AIUserUnitResultHistory::getFinished)
                        .collect(Collectors.toList());
                int total = bookData.getJsonData().getChildren().stream().filter(e -> !chipsContentService.isTrailUnit(e.getStone_data_id())).collect(Collectors.toList()).size();
                if (CollectionUtils.isNotEmpty(thisBookList) && thisBookList.size() * 5 >  total * 4) {
                    context.setStatus(AIBookStatus.GetTarget);
                } else {
                    context.setStatus(AIBookStatus.UnGetTarget);
                }
            } else {
                context.setStatus(AIBookStatus.UnGetTarget);
            }
            return;
        }

        ChipsEnglishProductTimetable chipsEnglishProductTimetable = context.getTimetable();
        if (chipsEnglishProductTimetable == null || CollectionUtils.isEmpty(chipsEnglishProductTimetable.getCourses())) {
            context.errorResponse("缺少课表配置");
            return;
        }

        String today = DateUtils.dateToString(current, "yyyy-MM-dd");
        StoneBookData.Node unitNode = chipsEnglishProductTimetable.getCourses().stream()
                .filter(e -> e.getBeginDate() != null && DateUtils.dateToString(e.getBeginDate(), "yyyy-MM-dd").equals(today))
                .findFirst()
                .map(e -> bookData.getJsonData().getChildren().stream().filter(e1 -> e.getUnitId().equals(e1.getStone_data_id())).findFirst().orElse(null))
                .orElse(null);
        if (unitNode == null) {
            int days = chipsEnglishProductTimetable.getCourses().stream()
                    .filter(e -> e.getBookId().equals(bookData.getId()))
                    .filter(e -> e.getBeginDate().before(current))
                    .collect(Collectors.toList()).size();
            int index = days - 1;
            if (index < 0) {
                Date beginDate = chipsEnglishProductTimetable.getCourses().stream()
                        .filter(e -> e.getBookId().equals(bookData.getId()))
                        .sorted(Comparator.comparing(ChipsEnglishProductTimetable.Course::getBeginDate))
                        .findFirst()
                        .map(ChipsEnglishProductTimetable.Course::getBeginDate).orElse(current);
                if (beginDate.after(current)) {
                    context.getExtMap().put("dayDiff", DateExtentionUtil.dayDiffCeil(current, beginDate));
                    context.getExtMap().put("beginDate",  DateUtils.dateToString(beginDate, "M月d日"));
                    context.setStatus(AIBookStatus.UnBegin);
                    return;
                }
            }
            unitNode = index < bookData.getJsonData().getChildren().size() && index > 0 ? bookData.getJsonData().getChildren().get(index) : bookData.getJsonData().getChildren().get(0);
        }


        StoneBookData.Node unitN = unitNode;
        StoneUnitData unitData = Optional.ofNullable(stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singletonList(unitN.getStone_data_id())))
                .filter(MapUtils::isNotEmpty)
                .map(e -> e.get(unitN.getStone_data_id()))
                .map(StoneUnitData::newInstance)
                .filter(e -> e.getJsonData() != null)
                .orElse(null);

        if (unitData == null) {
            context.errorResponse("教材没有内容");
            return;
        }
        context.setBook(bookData);
        context.setUnit(unitData);
        context.setStatus(AIBookStatus.InTime);

        int rank = 1;
        for(StoneBookData.Node node : bookData.getJsonData().getChildren()) {
            if (unitData.getId().equals(node.getStone_data_id())) {
                break;
            }
            rank ++;
        }

        context.setRank(rank);
    }
}
