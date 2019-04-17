package com.voxlearning.utopia.service.psr.impl.examcn;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.dao.PsrAboveLevelBookEidsNewMathDao;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnData;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnFilter;
import com.voxlearning.utopia.service.psr.impl.util.PsrIrtPredictEx;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Chaoli Lee on 14-7-7.
 * Psr 核心类
 * 用之前 需要对 内部变量进行初始化
 */

@Slf4j
@Named
public class PsrExamCnCore implements Serializable {
    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrExamEnData psrExamEnData;
    @Inject private PsrExamEnFilter psrExamEnFilter;
    @Inject PsrAboveLevelBookEidsNewMathDao psrAboveLevelBookEidsNewMathDao;

    public EkEidListContent doCore(PsrExamContext psrExamContext) {
        EkEidListContent ekEidListContent = new EkEidListContent();
        if (psrExamContext == null)
            return ekEidListContent;

        if (ekCouchbaseDao == null)
            return ekEidListContent;

        PsrIrtPredictEx.setEkCouchbaseDao(ekCouchbaseDao); // p002 版本算法
        PsrIrtPredictEx.setG_predict_weight(psrExamContext.getGPredictWeight());

        PsrBookPersistenceNew psrBookPersistenceNew = psrExamEnData.getPsrBookPersistence(psrExamContext);
        if (psrBookPersistenceNew == null)
            return ekEidListContent;

        Integer maxUnitRankId = psrBookPersistenceNew.getMaxRankId();
        PsrUnitPersistenceNew unit = psrBookPersistenceNew.getUnitPersistenceByUnitId(psrExamContext.getUnitId());
        if (unit == null)
            unit = psrBookPersistenceNew.getUnitPersistenceByUnitRankId(maxUnitRankId);  // 默认最后一单元,可以保证充足的题量

        if (unit == null)
            return ekEidListContent; // 该课本没有单元信息

        UserExamContent userExamContent = psrExamContext.getUserExamContentId();
        if (userExamContent == null) {
            userExamContent = new UserExamContent();
            userExamContent.setIrtTheta(1.5D);
        }
        PsrUnitPersistenceNew pUnit = null;

        List<Integer> unitRanks = new LinkedList<>();

        // 1 向前单元取题,记录lesson集合
        for (int i=unit.getRank(); i > 0; i--) {
            unitRanks.add(i);
        }
        // 2 向后单元取题
        for (int i=unit.getRank()+1; i <= psrBookPersistenceNew.getMaxRankId(); i++) {
            unitRanks.add(i);
        }

        for (Integer rank : unitRanks) {
            Integer curCount = CollectionUtils.isEmpty(ekEidListContent.getEkList())
                    ? 0
                    : CollectionUtils.isEmpty(ekEidListContent.getEkList().get(0).getEidList())
                        ? 0
                        : ekEidListContent.getEkList().get(0).getEidList().size();
            Integer reqEidCount = psrExamContext.getECount() - curCount;
            if (reqEidCount <= 0)
                break;

            pUnit = psrBookPersistenceNew.getUnitPersistenceByUnitRankId(rank);
            if (pUnit == null || MapUtils.isEmpty(pUnit.getLessonPersistenceMap()))
                continue;

            List<String> lessons = new ArrayList<>(pUnit.getLessonPersistenceMap().keySet());

            EkToEidContent item = getEidsByLessons(lessons);

            if (item != null && CollectionUtils.isNotEmpty(item.getEidList())) {
                for (EidItem eid : item.getEidList()) {
                    eid.setAccuracyRate(1.0D);
                }
            }

            List<EidItem> eids = getEids(psrExamContext, userExamContent, item, reqEidCount);
            if (CollectionUtils.isEmpty(eids))
                continue;
            eids.stream().forEach(p -> ekEidListContent.addItemByEk("chinese", p));
        }

        return ekEidListContent;
    }

    public EkToEidContent getEidsByLessons(List<String> cnLessons) {
        if (CollectionUtils.isEmpty(cnLessons))
            return null;

        EkEidListContent ekEidListContentEx = ekCouchbaseDao.getEkEidListContentFromCouchbaseByCnLessons(cnLessons);
        if (ekEidListContentEx == null || ekEidListContentEx.isEkListNull() || ekEidListContentEx.getEkList().size() <= 0) {
            // 该知识点 没有取到 题, 重新获取知识点 , continue for {get ek, get eids}
            return null;
        }

        EkToEidContent ekToEidContent = new EkToEidContent();
        ekToEidContent.setEidList(new ArrayList<>());
        for (EkToEidContent item : ekEidListContentEx.getEkList()) {
            ekToEidContent.getEidList().addAll(item.getEidList());
        }
        if (ekToEidContent.isEidListNull())
            return null;

        return ekToEidContent;
    }

    private List<EidItem> getEids(PsrExamContext psrExamContext, UserExamContent userExamContent, EkToEidContent ekToEidContent, Integer reqEidCount) {

        // 计算预估通过率, 并根据预估通过率 和 题型热度排序
        List<Map.Entry<Double, EidItem>> listEids = psrExamEnFilter.getEidsPredictRate(psrExamContext, ekToEidContent, reqEidCount, Subject.CHINESE);
        if (listEids == null)
            return null;

        Integer curEidCount = 0;
        List<EidItem> retList = new ArrayList<>();

        if (userExamContent.getIrtTheta() >= psrExamContext.getLowIrtTheta() && userExamContent.getIrtTheta() <= psrExamContext.getHighIrtTheta()) {
            // 中能力的学生 从中间开始取题
            List<Integer> posList = getDataFromMiddleToSide(listEids.size());

            for (int j = 0; posList != null && j < posList.size() && curEidCount < reqEidCount; j++) {
                int n = posList.get(j);

                if (n < 0 || n > listEids.size())
                    continue;

                EidItem eidItem = listEids.get(n).getValue();

                if (eidItem == null)
                    continue;
                // fixme ---------------------------------------------
                if (!psrExamEnFilter.isPsrFromOnlineQuestionTable(psrExamContext, eidItem.getEid(), Subject.CHINESE)) {
                    //log.error("FindAnBadEid:" + eidItem.getEid());
                    continue;
                }

                if (psrExamContext.getRecommendEids() == null)
                    psrExamContext.setRecommendEids(new ArrayList<String>());
                if (psrExamContext.getRecommendEids().contains(eidItem.getEid()))
                    continue;
                psrExamContext.getRecommendEids().add(eidItem.getEid());
                curEidCount++;
                retList.add(eidItem);
            }
        } else {
            // 高能力 和 低能力的学生 按队列顺序取题
            for (int j = 0; j < listEids.size() && curEidCount < reqEidCount; j++) {
                EidItem eidItem = listEids.get(j).getValue();

                if (eidItem == null)
                    continue;

                // fixme ---------------------------------------------
                if (!psrExamEnFilter.isPsrFromOnlineQuestionTable(psrExamContext, eidItem.getEid(), Subject.CHINESE)) {
                    //log.error("FindAnBadEid:" + eidItem.getEid());
                    continue;
                }

                if (psrExamContext.getRecommendEids() == null)
                    psrExamContext.setRecommendEids(new ArrayList<String>());
                if (psrExamContext.getRecommendEids().contains(eidItem.getEid()))
                    continue;
                psrExamContext.getRecommendEids().add(eidItem.getEid());
                curEidCount++;
                retList.add(eidItem);
            }
        }  // end if 按学生能力取题

        return retList;
    }

    /*
     * 从队列中间位置 左右摆动
     * 返回 列表 下标
     */
    private List<Integer> getDataFromMiddleToSide(int size) {
        if (size <= 0)
            return null;

        List<Integer> list = new ArrayList<>();

        int middlePos = (size + 1) / 2 - 1;  // 队列的中间位置

        int pos = 0;
        int index = 0;
        for (int i = 0; i < size; i++) {
            index = i + 1;
            if (index % 2 != 0)
                pos = middlePos - ((index - 1) / 2);
            else
                pos = middlePos + (index / 2);

            list.add(pos);
        }

        return list;
    }
}
