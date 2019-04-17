package com.voxlearning.utopia.service.newhomework.impl.dao.outside;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReading;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReadingMissionResult;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReadingResult;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@CacheBean(type = OutsideReadingResult.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class OutsideReadingResultDao extends StaticMongoShardPersistence<OutsideReadingResult, String> {
    @Override
    protected void calculateCacheDimensions(OutsideReadingResult document, Collection<String> dimensions) {
        dimensions.add(OutsideReadingResult.ck_id(document.getId()));
    }

    /**
     * 获取课外阅读中间结果
     * @param readingId 阅读任务id
     * @param studentId 学生id
     * @return
     */
    public OutsideReadingResult load(String readingId, Long studentId) {
        if (StringUtils.isBlank(readingId) || studentId == null) {
            return null;
        }
        return load(OutsideReadingResult.generateId(readingId, studentId));
    }

    /**
     * 获取课外阅读中间结果
     * @param readingId 阅读任务id
     * @param studentIds 学生id
     * @return
     */
    public Map<String, OutsideReadingResult> loads(String readingId, Collection<Long> studentIds) {
        if (StringUtils.isEmpty(readingId) || CollectionUtils.isEmpty(studentIds)) {
            return new LinkedHashMap<>();
        }
        List<String> readingResultIds = new LinkedList<>();
        studentIds.forEach(studentId -> readingResultIds.add(OutsideReadingResult.generateId(readingId, studentId)));
        return loads(readingResultIds);
    }

    /**
     * 获取课外阅读中间结果
     * @param readingIds 阅读任务id
     * @param studentId 学生id
     * @return
     */
    public Map<String, OutsideReadingResult> loads(Collection<String> readingIds, Long studentId) {
        if (studentId ==null || CollectionUtils.isEmpty(readingIds)) {
            return new LinkedHashMap<>();
        }
        List<String> readingResultIds = new LinkedList<>();
        readingIds.forEach(readingId -> readingResultIds.add(OutsideReadingResult.generateId(readingId, studentId)));
        return loads(readingResultIds);
    }

    /**
     * 获取课外阅读中间结果
     * @param readings 阅读任务id
     * @param studentIdsMap 学生id
     * @return
     */
    public Map<String, OutsideReadingResult> loads(Collection<OutsideReading> readings, Map<Long, List<Long>> studentIdsMap) {
        if (CollectionUtils.isEmpty(readings) || MapUtils.isEmpty(studentIdsMap)) {
            return new LinkedHashMap<>();
        }
        List<String> readingResultIds = new LinkedList<>();
        readings.forEach(reading ->
                readingResultIds.addAll(
                        studentIdsMap.get(reading.getClazzGroupId())
                                .stream()
                                .map(studentId -> OutsideReadingResult.generateId(reading.getId(), studentId))
                                .collect(Collectors.toList())
                ));

        return loads(readingResultIds);
    }

    /**
     * 初始化课外阅读中间结果表
     *
     * @param readingId  阅读任务id
     * @param bookId  图书id
     * @param studentId 学生id
     */
    public OutsideReadingResult initOutsideReadingResult(String readingId, String bookId, Long studentId) {
        if (StringUtils.isBlank(readingId) || StringUtils.isBlank(bookId) || studentId == null) {
            return null;
        }
        String readingResultId = OutsideReadingResult.generateId(readingId, studentId);
        OutsideReadingResult outsideReadingResult = load(readingResultId);
        if (outsideReadingResult != null) {
            return outsideReadingResult;
        }
        Date d = new Date();
        outsideReadingResult = new OutsideReadingResult();
        outsideReadingResult.setId(readingResultId);
        outsideReadingResult.setBookId(bookId);
        outsideReadingResult.setReadingId(readingId);
        outsideReadingResult.setStudentId(studentId);
        outsideReadingResult.setCreateAt(d);
        outsideReadingResult.setUpdateAt(d);
        OutsideReadingResult modified = insertIfAbsent(readingResultId, outsideReadingResult);
        if (modified != null) {
            getCache().createCacheValueModifier()
                    .key(OutsideReadingResult.ck_id(readingResultId))
                    .expiration(getDefaultCacheExpirationInSeconds())
                    .modifier(currentValue -> modified)
                    .execute();
        }
        return modified;
    }

    /**
     * 完成结果数据处理
     *
     * @param outsideReadingResult
     * @param missionId
     * @param missionAnswers 更新后的missionAnswers
     * @param star
     * @param allFinished
     * @param missionFinished
     * @return
     */
    public OutsideReadingResult finishOutsideReading(OutsideReadingResult outsideReadingResult, String missionId, LinkedHashMap<String, String> missionAnswers, Integer star, Boolean allFinished, Boolean missionFinished){
        Date currentDate = new Date();
        if(outsideReadingResult == null){
            return null;
        }
        Map<String, OutsideReadingMissionResult> missionResults = outsideReadingResult.getNotNullMissionResults();
        OutsideReadingMissionResult ormr = missionResults.getOrDefault(missionId, new OutsideReadingMissionResult());
        ormr.setAnswers(missionAnswers);
        //当某个关卡星星有变化的时候设置最新星星数
        if(SafeConverter.toInt(ormr.getStar()) < star){
            ormr.setStar(star);
        }
        //当整个关卡完成的时候设置关卡完成时间
        if(missionFinished){
            ormr.setFinishAt(currentDate);
        }
        //当整个图书完成的时候设置完成时间
        if(allFinished){
            outsideReadingResult.setFinishAt(currentDate);
        }
        ormr.setUpdateAt(currentDate);
        missionResults.put(missionId, ormr);
        outsideReadingResult.setMissionResults(missionResults);
        outsideReadingResult.setUpdateAt(currentDate);
        return upsert(outsideReadingResult);
    }
}
