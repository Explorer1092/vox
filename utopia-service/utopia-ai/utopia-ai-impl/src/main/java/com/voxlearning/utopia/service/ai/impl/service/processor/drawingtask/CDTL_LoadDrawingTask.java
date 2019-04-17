package com.voxlearning.utopia.service.ai.impl.service.processor.drawingtask;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.ai.constant.ChipsUserDrawingTaskStatus;
import com.voxlearning.utopia.service.ai.data.StoneBookData;
import com.voxlearning.utopia.service.ai.data.StoneQuestionData;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.ChipsUserDrawingTask;
import com.voxlearning.utopia.service.ai.entity.ChipsUserDrawingTaskJoin;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsDrawingTaskLoadContext;
import com.voxlearning.utopia.service.ai.impl.persistence.task.ChipsUserDrawingTaskJoinPersistence;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.question.api.entity.StoneData;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class CDTL_LoadDrawingTask extends AbstractAiSupport implements IAITask<ChipsDrawingTaskLoadContext> {

    @Inject
    private ChipsUserDrawingTaskJoinPersistence chipsUserDrawingTaskJoinPersistence;

    private static int DEFAULT_PAGE_SIZE = 6;

    @Override
    public void execute(ChipsDrawingTaskLoadContext context) {
        if (CollectionUtils.isEmpty(context.getBookList())) {
            context.terminateTask();
            return;
        }

        List<ChipsUserDrawingTask> userDrawingTasks = chipsUserDrawingTaskPersistence.loadByUser(context.getUserId());
        context.setUserDrawingTasks(userDrawingTasks);


        Map<String, StoneData> stoneBookData = stoneDataLoaderClient.loadStoneDataIncludeDisabled(context.getBookList());
        Map<String, StoneBookData> bookDataMap = new HashMap<>();
        List<String> unitList = new ArrayList<>();
        for (String book : context.getBookList()) {
            StoneBookData bookStone = Optional.ofNullable(stoneBookData)
                    .map(e -> e.get(book)).map(StoneBookData::newInstance)
                    .filter(e -> e.getJsonData() != null && CollectionUtils.isNotEmpty(e.getJsonData().getChildren()))
                    .orElse(null);
            if (bookStone == null) {
                continue;
            }
            bookDataMap.put(book, bookStone);
            unitList.addAll(bookStone.getJsonData().getChildren().stream().map(StoneBookData.Node::getStone_data_id).collect(Collectors.toList()));
        }

        List<Map<String, Object>> drawingList = new ArrayList<>();
        Map<String, StoneData> unitStoneMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(unitList);
        List<String> drawingIdList = new ArrayList<>();
        Map<String, Integer> unitRankMap = new HashMap<>();
        Map<String, String> drawingUnitMap = new HashMap<>();
        int rank = 0;
        for(String unitId : unitList) {
            rank ++;
            unitRankMap.put(unitId, rank);
            StoneUnitData unitData = Optional.ofNullable(unitStoneMap.get(unitId))
                    .map(StoneUnitData::newInstance)
                    .filter(un -> un.getJsonData() != null && StringUtils.isNotBlank(un.getJsonData().getReward_illust_id()))
                    .orElse(null);
            if (unitData == null) {
                continue;
            }
            drawingUnitMap.put(unitData.getJsonData().getReward_illust_id(), unitId);
            drawingIdList.add(unitData.getJsonData().getReward_illust_id());
        }

        int pageSize = context.getPageSize() <= 0 ? DEFAULT_PAGE_SIZE : context.getPageSize();
        int totalPage = new BigDecimal(drawingIdList.size()).divide(new BigDecimal(pageSize), 0, RoundingMode.UP).intValue();
        if (totalPage == 0) {
            context.terminateTask();
            return;
        }
        context.setTotalPage(totalPage);

        int page = context.getPage();
        int min = Math.min(drawingIdList.size() - 1, Math.max(0, (page - 1) * pageSize));
        int max = Math.min(drawingIdList.size(), Math.min(page, totalPage) * pageSize);
        List<String> pageDrawingList = drawingIdList.subList(min, max);
        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(pageDrawingList);
        for(String drawingId : pageDrawingList) {
            StoneQuestionData questionData = Optional.ofNullable(stoneDataMap).map(map -> map.get(drawingId))
                    .map(StoneQuestionData::newInstance)
                    .orElse(null);
            if (questionData == null) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            String unitId = drawingUnitMap.get(drawingId);
            map.put("unitId", unitId);
            map.put("unitRank", unitRankMap.get(unitId));

            ChipsUserDrawingTask task = userDrawingTasks.stream().filter(e -> questionData.getId().equals(e.getDrawingId())).findFirst().orElse(null);
            if (task != null) {
                int gain = chipsUserDrawingTaskJoinPersistence.loadByTask(task.getId()).stream().mapToInt(ChipsUserDrawingTaskJoin::getEnergy).sum();
                map.put("drawingTaskId", task.getId());
                map.put("status", task.getStatus());
                map.put("totalEnergy", task.getTotalEnergy());
                map.put("gainEnergy", gain);
                //TODO IOS的bug导致需要将升级后的内容改为升级前的
                if (task.fetchStatus() != ChipsUserDrawingTaskStatus.finished) {
                    questionData.getJsonData().put("reward_pic_after_back", SafeConverter.toString(questionData.getJsonData().get("reward_pic_before_back")));
                    questionData.getJsonData().put("reward_pic_after_front", SafeConverter.toString(questionData.getJsonData().get("reward_pic_before_front")));
                }
            } else {
                map.put("drawingTaskId", -1);
                map.put("status", ChipsUserDrawingTaskStatus.unaccessible.name());
                map.put("totalEnergy", 0);
                map.put("gainEnergy", 0);
            }
            map.put("jsonData", questionData.getJsonData());
            drawingList.add(map);
        }
        context.setDrawingList(drawingList);
    }
}
