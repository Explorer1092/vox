/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.psr.impl.examen;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;

/*
 * EK 数量 在 3000 个左右, 常住 内存,减少数据库访问
 * 必须初始化 ekCouchbaseDao
 */

@Slf4j
@Named
@Deprecated // 2015.08.10
public class PsrExamEnGlobalWrongCache implements InitializingBean {
// fixme 2015.09.01 之后暂不维护
/*
    private Map<String/ *eid* /, ExamEnGlobalWrongItem> examEnGlobalWrongItemMap;
    private Map<Long/ *bookId* /, Map<String/ *eid* /, ExamEnGlobalWrongItem>> bookIdExamEnGlobalWrongMap;
    private Map<Long/ *bookId* /, Map<Integer/ *0-100* /, Integer/ *max* />> partMaxWrongCountPersonMap;    // 正确率在某个范围内的 最大 做错人数, 相同正确率 推荐 做错人数较多的 eid

    //@Getter @Setter
    @Inject private EkCouchbaseDao ekCouchbaseDao;

    public PsrExamEnGlobalWrongCache() {
        if (examEnGlobalWrongItemMap == null)
            examEnGlobalWrongItemMap = new ConcurrentHashMap<>();
        if (bookIdExamEnGlobalWrongMap == null)
            bookIdExamEnGlobalWrongMap = new ConcurrentHashMap<>();
        if (partMaxWrongCountPersonMap == null)
            partMaxWrongCountPersonMap = new ConcurrentHashMap<>();

        initMap();
    }

    public boolean isMapNull() {
        return (examEnGlobalWrongItemMap == null);
    }

    public void initMap() {
        if (ekCouchbaseDao == null || examEnGlobalWrongItemMap.size() > 0)
            return;
        List<ExamEnGlobalWrongItem> examEnGlobalWrongItems = ekCouchbaseDao.getCouchbaseUserExamEnGlobalWrong();
        if (examEnGlobalWrongItems == null || examEnGlobalWrongItems.size() <= 0)
            return;

        for (ExamEnGlobalWrongItem item : examEnGlobalWrongItems) {
            if (examEnGlobalWrongItemMap.containsKey(item.getEid()))
                continue;
            examEnGlobalWrongItemMap.put(item.getEid(), item);
        }
    }

    public Map<String, ExamEnGlobalWrongItem> getGlobalWrongMapByBookEids(Long bookId, List<String/ *eid* /> eids) {
        if (bookId == null)
            return null;
        if (eids == null || eids.size() <= 0)
            return null;

        if (bookIdExamEnGlobalWrongMap.containsKey(bookId))
            return bookIdExamEnGlobalWrongMap.get(bookId);

        Map<String, ExamEnGlobalWrongItem> retMap = new HashMap<>();

        Map<Integer/ *0-100* /, Integer/ *max* /> partMaxWrongCountPerson = null;
        if (partMaxWrongCountPersonMap.containsKey(bookId))
            partMaxWrongCountPerson = partMaxWrongCountPersonMap.get(bookId);
        if (partMaxWrongCountPerson == null)
            partMaxWrongCountPerson = new HashMap<>();

        Double weightSum = 0.0;

        for (String eid : eids) {
            if (examEnGlobalWrongItemMap.size() <= 0)
                break;
            if (!examEnGlobalWrongItemMap.containsKey(eid))
                continue;

            if (retMap.containsKey(eid))
                continue;

            ExamEnGlobalWrongItem item = examEnGlobalWrongItemMap.get(eid);

            Integer index = Double.valueOf(item.getRate() * 100).intValue();
            if (!partMaxWrongCountPerson.containsKey(index)) {
                partMaxWrongCountPerson.put(index, item.getWrongCountPerson());
            } else {
                if (partMaxWrongCountPerson.get(index) < item.getWrongCountPerson())
                    partMaxWrongCountPerson.put(index, item.getWrongCountPerson());
            }

            retMap.put(eid, item);
        }

        Double sum = 0.0;

        // 计算排序权重
        for (String key : retMap.keySet()) {
            Integer index = Double.valueOf(retMap.get(key).getRate() * 100).intValue();
            Integer max = 0;
            if (partMaxWrongCountPerson.containsKey(index))
                max = partMaxWrongCountPerson.get(index);

            // 根据 正确率 和 做错人次 计算出 权重, 正确率相同的情况下，优先推 做错次数较多的人
            retMap.get(key).setWeight(retMap.get(key).getRate() * 0.7);
            if (max > 0)
                retMap.get(key).setWeight(retMap.get(key).getWrongCountPerson() / max * 0.3);

            sum += retMap.get(key).getWeight();
        }

        // 计算一下 归一化权重, 即 所有 weightPer 加和 = 100%
        for (String key : retMap.keySet()) {
            if (sum <= 0)
                break;

            retMap.get(key).setWeightPer(retMap.get(key).getWeight() / sum * 100);
        }

        partMaxWrongCountPersonMap.put(bookId, partMaxWrongCountPerson);
        bookIdExamEnGlobalWrongMap.put(bookId, retMap);

        return retMap;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrExamEnGlobalWrongCache-Loader") {
            @Override
            public void runSafe() {
                if (examEnGlobalWrongItemMap == null)
                    examEnGlobalWrongItemMap = new ConcurrentHashMap<>();
                if (bookIdExamEnGlobalWrongMap == null)
                    bookIdExamEnGlobalWrongMap = new ConcurrentHashMap<>();
                if (partMaxWrongCountPersonMap == null)
                    partMaxWrongCountPersonMap = new ConcurrentHashMap<>();

                examEnGlobalWrongItemMap.clear();
                bookIdExamEnGlobalWrongMap.clear();
                partMaxWrongCountPersonMap.clear();

                initMap();
                log.info("PsrExamEnGlobalWrongCache userExamEnGlobalWrongItemMap.clear on the timer");
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 86400*1000, 86400*1000);
    }

*/
// fixme 2015.09.01 之后暂不维护

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
