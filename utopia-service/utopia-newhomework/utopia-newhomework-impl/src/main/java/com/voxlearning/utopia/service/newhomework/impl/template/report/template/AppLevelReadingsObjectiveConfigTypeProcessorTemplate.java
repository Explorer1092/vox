package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.ReadingAppTypePart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class AppLevelReadingsObjectiveConfigTypeProcessorTemplate extends AppReadingObjectiveConfigTypeProcessorTemplate {

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.LEVEL_READINGS;
    }


    @Override
    public void fetchTypePart(TypePartContext typePartContext) {

        //*********** begin 初始化数据准备 *********** //
        /**
         * 1:type =》 类型
         * 2:result =》 返回数据
         * 3:newHomeworkResults =》 完成该类型的中间表数据
         * 4:newHomework
         */
        ObjectiveConfigType type = typePartContext.getType();
        Map<ObjectiveConfigType, Object> result = typePartContext.getResult();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = typePartContext.getNewHomeworkResultMap();
        List<NewHomeworkResult> newHomeworkResults = newHomeworkResultMap.values().stream().filter(o -> o.isFinishedOfObjectiveConfigType(type)).collect(Collectors.toList());
        NewHomework newHomework = typePartContext.getNewHomework();
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        List<String> pictureBookIds = target
                .getApps()
                .stream()
                .map(NewHomeworkApp::getPictureBookId)
                .collect(Collectors.toList());
        //*********** end 初始化数据准备 *********** //

        ReadingAppTypePart readingAppTypePart = new ReadingAppTypePart();
        readingAppTypePart.setType(type);
        readingAppTypePart.setTypeName(type.getValue());
        readingAppTypePart.setShowScore(true);
        if (CollectionUtils.isEmpty(newHomeworkResults)) {
            readingAppTypePart.setSubContent("班平均分-- 平均用时--");
            result.put(type, readingAppTypePart);
            return;
        }

        //pictureBookMap 数据准备，避免没必要查询
        Map<String, PictureBookPlus> pictureBookPlusMap = pictureBookPlusServiceClient.loadByIds(pictureBookIds);



        // *********** begin 初始化ReadingAppTypePart.Book 是绘本每个绘本的结构****//
        List<ReadingAppTypePart.Book> books = target.getApps()
                .stream()
                .map(o -> {
                    ReadingAppTypePart.Book book = new ReadingAppTypePart.Book();
                    book.setReadingId(o.getPictureBookId());
                    if (pictureBookPlusMap.containsKey(o.getPictureBookId())) {
                        PictureBookPlus pictureBookPlus = pictureBookPlusMap.get(o.getPictureBookId());
                        book.setTabName(pictureBookPlus.getEname());
                    }
                    ObjectiveConfigTypeParameter param = new ObjectiveConfigTypeParameter();
                    param.setPictureBookId(o.getPictureBookId());
                    String url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/levelreadingsdetail",
                            MapUtils.m(
                                    "homeworkId", newHomework.getId(),
                                    "type", type,
                                    "subject", newHomework.getSubject(),
                                    "param", JsonUtils.toJson(param)));
                    book.setUrl(url);

                    return book;
                }).collect(Collectors.toList());
        // *********** end 初始化ReadingAppTypePart.Book 是绘本每个绘本的结构****//


        Map<String, ReadingAppTypePart.Book> bookMap = books.stream().collect(Collectors.toMap(ReadingAppTypePart.Book::getReadingId, Function.identity()));

        int totalScore = 0;
        long totalDuration = 0;


        //******** begin newHomeworkResults 数据处理 *******//
        //bookMap 的key 特殊对于着
        for (NewHomeworkResult r : newHomeworkResults) {
            NewHomeworkResultAnswer newHomeworkResultAnswer = r.getPractices().get(type);
            totalScore += SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
            totalDuration += SafeConverter.toLong(newHomeworkResultAnswer.processDuration());
            for (Map.Entry<String, NewHomeworkResultAppAnswer> entry : newHomeworkResultAnswer.getAppAnswers().entrySet()) {
                if (bookMap.containsKey(entry.getKey())) {
                    ReadingAppTypePart.Book book = bookMap.get(entry.getKey());
                    book.setNum(1 + book.getNum());
                    book.setTotalDuration(SafeConverter.toLong(entry.getValue().processDuration()) + book.getTotalDuration());
                    book.setTotalScore(SafeConverter.toDouble(entry.getValue().getScore()) + book.getTotalScore());
                }
            }
        }
        //******** end newHomeworkResults 数据处理 *******//

        // ************* begin 绘本后处理（分数和时间）************//
        books.stream()
                .filter(o -> o.getNum() > 0)
                .forEach(o -> {
                    long averDuration1 = new BigDecimal(o.getTotalDuration()).divide(new BigDecimal(o.getNum() * 1000), 0, BigDecimal.ROUND_HALF_UP).longValue();
                    o.setAverDuration(averDuration1);
                    int averScore1 = new BigDecimal(o.getTotalScore()).divide(new BigDecimal(o.getNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                    o.setAverScore(averScore1);
                    o.setTabValue(averScore1 + "分," + NewHomeworkUtils.handlerTime((int) averDuration1));
                });
        // ************* end 绘本后处理（分数和时间）************//
        int averScore = new BigDecimal(totalScore).divide(new BigDecimal(newHomeworkResults.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
        long averDuration = new BigDecimal(totalDuration).divide(new BigDecimal(60 * newHomeworkResults.size()), 0, BigDecimal.ROUND_UP).longValue();
        readingAppTypePart.setTabs(books);
        readingAppTypePart.setAverScore(averScore);
        readingAppTypePart.setAverDuration(averDuration);
        readingAppTypePart.setSubContent("班平均分" + averScore + " 平均用时" + averDuration + "min");
        readingAppTypePart.setHasFinishUser(true);
        result.put(type, readingAppTypePart);
    }
}
