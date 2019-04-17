package com.voxlearning.utopia.service.piclisten.impl.handler;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.piclisten.impl.dao.FollowReadSentenceResultDao;
import com.voxlearning.utopia.service.piclisten.impl.dao.StudentFollowReadReportDao;
import com.voxlearning.utopia.service.question.api.entity.PicListen;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.FollowReadSentenceResult;
import com.voxlearning.utopia.service.vendor.api.entity.StudentFollowReadReport;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生的作业报告-点读数据生成器
 * 只生成30天跟读部分,不涉及任何上边的分数
 *
 * @author jiangpeng
 * @since 2017-03-21 下午3:40
 **/
@Named
public class StudentFollowReadReportGenerator extends SpringContainerSupport {

    @Inject
    private FollowReadSentenceResultDao followReadSentenceResultDao;

    @Inject
    private QuestionLoaderClient questionLoaderClient;

    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @Inject
    private StudentFollowReadReportDao studentFollowReadReportDao;

    public void generateStudentFollowReadReport(Long studentId){
        List<FollowReadSentenceResult> followReadSentenceResults = followReadSentenceResultDao.loadByStudentId(studentId);
        if (CollectionUtils.isEmpty(followReadSentenceResults))
            return;
        // a. 超过30天前的不要
        // c. 同时得到每个句子的最新一次跟读记录。
        // d. 得到单元对应的已读句子结果列表
        Map<UnitSentenceMapper, FollowReadSentenceResult> unitSentence2LastResultMap = new HashMap<>();
        Map<String, List<FollowReadSentenceResult>> unit2ResultListMap = new HashMap<>();
        for (FollowReadSentenceResult sentenceResult : followReadSentenceResults) {
            //a
            Date createTime = sentenceResult.getCreateTime();
            if (DateUtils.calculateDateDay(createTime, 30).before(DayRange.current().getStartDate()))
                continue;
            //c
            UnitSentenceMapper mapper = UnitSentenceMapper.fromSentenceResult(sentenceResult);
            if (mapper == null)
                continue;
            FollowReadSentenceResult readSentenceResult = unitSentence2LastResultMap.get(mapper);
            if (readSentenceResult == null || readSentenceResult.getCreateTime().before(sentenceResult.getCreateTime()))
                unitSentence2LastResultMap.put(mapper, sentenceResult);
            //d
            List<FollowReadSentenceResult> readSentenceResultList = unit2ResultListMap.get(sentenceResult.getUnitId());
            if (CollectionUtils.isEmpty(readSentenceResultList))
                readSentenceResultList = new ArrayList<>();
            readSentenceResultList.add(sentenceResult);
            unit2ResultListMap.put(sentenceResult.getUnitId(), readSentenceResultList);

        }
//        //a&b
//        picListenReportCacheManager.asyncUpdateScoreResult(studentId, yesterdayRange.toString(), recordTimeCount, null,
//                (long) yesterdaySentenceIdSet.size(), null);

        //计算并保存结果。
        Map<String, NewBookCatalog> unitMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unit2ResultListMap.keySet());
        if (MapUtils.isEmpty(unitMap))
            return;
        Set<String> bookIdSet = unitMap.values().stream().map(this::getBookId).collect(Collectors.toSet());
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdSet);

        Map<String, String> unit2ModuleIdMap = unit2ModuleIdMap(unitMap.values());
        Map<String, NewBookCatalog> moduleMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unit2ModuleIdMap.values());
        Map<String, List<PicListen>> unit2PicListenListMap = questionLoaderClient.loadPicListenByNewUnitIds(unit2ResultListMap.keySet());

        StudentFollowReadReport studentFollowReadReport = StudentFollowReadReport.newInstance(studentId);
        for (Map.Entry<String, List<FollowReadSentenceResult>> entry : unit2ResultListMap.entrySet()) {
            String unitId = entry.getKey();
            List<FollowReadSentenceResult> readSentenceResults = entry.getValue();
            Set<Long> readSentenceSet = readSentenceResults.stream().map(FollowReadSentenceResult::getSentenceId).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(readSentenceSet))
                continue;
            List<PicListen> picListens = unit2PicListenListMap.get(unitId);
            NewBookCatalog unitNode = unitMap.get(unitId);
            if (unitNode == null)
                continue;
            String bookId = getBookId(unitNode);
            if (bookId == null)
                continue;
            NewBookProfile newBookProfile = bookProfileMap.get(bookId);
            if (newBookProfile == null)
                continue;
            StudentFollowReadReport.UnitResult unitResult = new StudentFollowReadReport.UnitResult();
            Long sentenceCount = getAllSentenceCount(picListens);
            unitResult.setUnitId(unitId);
            unitResult.setUnitName(unitNode.getName());
            unitResult.setUnitRank(unitNode.getRank());
            String moduleId = unit2ModuleIdMap.get(unitId);
            if (StringUtils.isNotBlank(moduleId)){
                NewBookCatalog module = moduleMap.get(moduleId);
                if ( module != null) {
                    unitResult.setModuleId(moduleId);
                    unitResult.setModuleName(module.getName());
                    unitResult.setModuleRank(module.getRank());
                }
            }

            unitResult.setTotalSentenceCount(sentenceCount);
            unitResult.setReadSentenceCount((long) readSentenceSet.size());
            unitResult.setBookId(bookId);
            unitResult.setBookName(newBookProfile.getName());

            //算平均分
            Double totalScore = 0D;
            List<String> lastReadSentenceResultIdList = new ArrayList<>();
            for (Long readSentenceId : readSentenceSet) {
                UnitSentenceMapper mapper = UnitSentenceMapper.newInstance(readSentenceId, unitId);
                FollowReadSentenceResult lastReadResult = unitSentence2LastResultMap.get(mapper);
                if (lastReadResult == null) {
                    continue; //这按说不可能。
                }
                lastReadSentenceResultIdList.add(lastReadResult.getId());
                totalScore += lastReadResult.fetchSentenceScore();
            }
            Integer averageScore = new BigDecimal(totalScore).divide(new BigDecimal(readSentenceSet.size()), 0, RoundingMode.HALF_UP).intValue();
            unitResult.setAverageScore(averageScore);
            unitResult.setLastReadSentenceResultIds(lastReadSentenceResultIdList);
            studentFollowReadReport.getUnitResultList().add(unitResult);
        }
        studentFollowReadReportDao.upsert(studentFollowReadReport);

    }

    private Map<String, String> unit2ModuleIdMap(Collection<NewBookCatalog> values) {
        Map<String, String> map = new HashMap<>();
        values.forEach(t -> {
            String moduleId = getModuleId(t);
            if (moduleId != null)
                map.put(t.getId(), moduleId);
        });
        return map;
    }

    private String getBookId(NewBookCatalog unit){
        NewBookCatalogAncestor book = unit.getAncestors().stream().filter(t -> BookCatalogType.BOOK.name().equals(t.getNodeType())).findFirst().orElse(null);
        return book == null ? null : book.getId();
    }

    private String getModuleId(NewBookCatalog unit){
        NewBookCatalogAncestor book = unit.getAncestors().stream().filter(t -> BookCatalogType.MODULE.name().equals(t.getNodeType())).findFirst().orElse(null);
        return book == null ? null : book.getId();
    }


    private Long getAllSentenceCount(List<PicListen> picListens){
        if (CollectionUtils.isEmpty(picListens))
            return 0L;
        Long sentenceCount = 0L;
        for (PicListen picListen : picListens) {
            long count = picListen.getAllSentenceIds().stream().filter(x -> x != 0).count();
            sentenceCount += count;
        }
        return sentenceCount;
    }



    @Getter
    @Setter
    @EqualsAndHashCode
    private static class UnitSentenceMapper{
        private String unitId;
        private Long sentenceId;

       public static UnitSentenceMapper fromSentenceResult(FollowReadSentenceResult sentenceResult){
           Objects.requireNonNull(sentenceResult);
           return newInstance(sentenceResult.getSentenceId(), sentenceResult.getUnitId());
       }
        public static UnitSentenceMapper newInstance(Long sentenceId, String unitId){
            UnitSentenceMapper mapper = new UnitSentenceMapper();
            mapper.setSentenceId(sentenceId);
            mapper.setUnitId(unitId);
            return mapper;
        }

    }
}
