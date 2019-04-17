package com.voxlearning.utopia.service.psr.impl.appen;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.service.PsrConfig;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

@Slf4j
@Named
public class PsrAppEnMatchController implements Serializable {

    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrConfig psrConfig;
    @Inject private PsrAppEnNewController psrAppEnNewController;
    @Inject private PsrBooksSentencesNew psrBooksSentencesNew;

    public PsrPrimaryAppEnMatchContent deal(String product, Long userId,
                                       int regionCode, String bookId, String unitId, int eCount,
                                       String eType, int matchCount) {

        Date dtB = new Date();

        PsrExamContext psrExamContext = new PsrExamContext(psrConfig, product, "student", userId,
                                                           regionCode, bookId, unitId, eCount, 0.0F, 0.0F);
        psrExamContext.setEType(eType);
        psrExamContext.setMatchCount(matchCount);
        psrExamContext.setWriteLog(false);

        PsrPrimaryAppEnMatchContent retPrimaryAppEnMatchContent = new PsrPrimaryAppEnMatchContent();

        // 日志 及返回值
        if (ekCouchbaseDao == null || psrBooksSentencesNew == null) {
            return logContent(retPrimaryAppEnMatchContent, psrExamContext, "Can not connect databases.", dtB, "error");
        }

        // 获取用户需要考察的知识点列表:调用英语应用接口
        PsrPrimaryAppEnContent psrPrimaryAppEnContent = psrAppEnNewController.deal(psrExamContext);
        if (!psrPrimaryAppEnContent.getErrorContent().equals("success")
                || psrPrimaryAppEnContent.getAppEnList().size() < eCount) {
            return logContent(retPrimaryAppEnMatchContent, psrExamContext, "not found enough eks from psrAppEnNewController.", dtB, "warn");
        }

        // 如果没有配错项,则相互配错
        retPrimaryAppEnMatchContent = matchContentEachOther(retPrimaryAppEnMatchContent, psrPrimaryAppEnContent, psrExamContext);

        if (retPrimaryAppEnMatchContent.getAppEnMatchList().size() < eCount) {
            String errMsg = "not found enough eks but why," + retPrimaryAppEnMatchContent.getAppEnMatchList().size() + " < " + eCount;
            return logContent(retPrimaryAppEnMatchContent, psrExamContext, errMsg, dtB, "warn");
        }

        return logContent(retPrimaryAppEnMatchContent, psrExamContext, "success", dtB, "info");
    }


    /* 1. 根据 应用英语推荐接口获取需要的知识点A
     * 2. 对获取的知识点A列表遍历,对每个知识点进行标准配错项(从配置文件中查,from Couchbase)查询
     * 3. 如果查到足够的标准配错项则返回,如果查不到或者个数不够,则用知识点A中的其他知识点作为配错项
     * 4. 返回
     *    for ek in AppEnContent
     *        get ek's matchEks from couchbase
     *           否 | 否               是 | 是
     *              |                    |
     *    AppEnContent 知识点互配     配置获取到的配错项
     *              |                    |
     *              |          配错项不够  | 有足够的配错项 -> 返回
     *              |              |                       ^
     *              |              |                       |
     *              |        添加匹配AppEnContent中的知识点--> |
     *              |                                      |
     *              |---------------------------------------
     *
     */
    private PsrPrimaryAppEnMatchContent matchContentEachOther(PsrPrimaryAppEnMatchContent matchContent, PsrPrimaryAppEnContent appEnContent, PsrExamContext psrExamContext) {
        if (matchContent == null)
            matchContent = new PsrPrimaryAppEnMatchContent();
        if (appEnContent == null || psrExamContext == null) {
            matchContent.setErrorContent("matchContentEachOther para appEnContent or psrExamContext is null");
            return matchContent;
        }

        // 根据bookId查找对应的LessonIds
        List<String> lessonIds = new ArrayList<>();
        PsrBookPersistenceNew psrBookPersistenceNew = psrBooksSentencesNew.getBookPersistenceByBookId(psrExamContext.getBookId());
        if (psrBookPersistenceNew != null)
            lessonIds.addAll(psrBookPersistenceNew.getLessonIdsByBookUnit(psrExamContext.getUnitId()));

        // 1.寻找标准配错项; 2.对知识点进行相互配错
        List<String> uniqMap = new ArrayList<>();
        for (int i=0; i<psrExamContext.getECount(); i++) {
            PsrPrimaryAppEnMatchItem matchItem = new PsrPrimaryAppEnMatchItem();

            // appEnContent psr 推荐列表
            PsrPrimaryAppEnItem appEnItem = null;
            for (PsrPrimaryAppEnItem item : appEnContent.getAppEnList()) {
                if (appEnItem == null && !uniqMap.contains(item.getEid())) {
                    appEnItem = item;
                    matchItem.setMatchItem(item);
                    uniqMap.add(item.getEid());

                    // 针对考察知识点列表查找对应的配错项, fixme 后续优化为从缓存中取数据
                    String matchEks = ekCouchbaseDao.getCouchbaseDataByKey("appenmatchalgo_" + appEnItem.getEid());
                    if (StringUtils.isEmpty(matchEks))
                        continue;

                    Map<String/*eid*/, String/*algo*/> matchEksBack = new HashMap<>();
                    // 保存之前配置的 配错项(其他知识点),如果标准配错项不够的话,则添加此中配置项
                    if (matchItem.getMatchEidsMap().size() > 0) {
                        matchEksBack.putAll(matchItem.getMatchEidsMap());
                        matchItem.getMatchEidsMap().clear();
                    }

                    // 1.寻找标准配错项 from couchbase
                    PsrPrimaryAppEnMatchEks appEnMatchEks = new PsrPrimaryAppEnMatchEks();
                    appEnMatchEks.setEk(appEnItem.getEid());
                    appEnMatchEks.decodeAndSet(matchEks);
                    List<String> matchEksTmp = appEnMatchEks.getMatchEksByLessonIds(lessonIds);
                    for (String matchEk : matchEksTmp) {
                        if (matchItem.getMatchEidsMap().size() >= psrExamContext.getMatchCount())
                            break;
                        if (!matchItem.getMatchEidsMap().containsKey(matchEk))
                            matchItem.getMatchEidsMap().put(matchEk, "algomatch");
                    }

                    // (把之前保存的配错项 重新加入到返回列表中)
                    for (Map.Entry<String,String> matchEkEntry : matchEksBack.entrySet()) {
                        if (matchItem.getMatchEidsMap().size() >= psrExamContext.getMatchCount())
                            break;
                        if (!matchItem.getMatchEidsMap().containsKey(matchEkEntry.getKey()))
                            matchItem.getMatchEidsMap().put(matchEkEntry.getKey(), matchEkEntry.getValue());
                    }
                } else {
                    // 2.对知识点进行相互配错
                    if (!matchItem.getMatchEidsMap().containsKey(item.getEid())
                            && matchItem.getMatchEidsMap().size() < psrExamContext.getMatchCount())
                        matchItem.getMatchEidsMap().put(item.getEid(), "algopsr");
                }
                // 配错项 达到 需要个数,则适配下一个 ek
                if (appEnItem != null && matchItem.getMatchEidsMap().size() >= psrExamContext.getMatchCount())
                    break;
            }

            // eCount < matchCount
            if (appEnItem == null)
                continue;

            // 遗留问题, 复制 matchEidsMap 到 matchEids
            matchItem.getMatchEids().addAll(matchItem.getMatchEidsMap().keySet());

            // 判断配错项个数,不够的情况下填写'oops'
            int count = psrExamContext.getMatchCount()-matchItem.getMatchEidsMap().size();
            for (int j=0; j<count; j++) {
                matchItem.getMatchEidsMap().put("oops", "algooops");
                matchItem.getMatchEids().add("oops");
            }

            matchContent.getAppEnMatchList().add(matchItem);
        }

        return matchContent;
    }

    private PsrPrimaryAppEnMatchContent logContent(PsrPrimaryAppEnMatchContent retPrimaryAppEnMatchContent,
                                                    PsrExamContext psrExamContext, String errorMsg,
                                                    Date dtB, String logLevel) {
        if (retPrimaryAppEnMatchContent == null)
            retPrimaryAppEnMatchContent = new PsrPrimaryAppEnMatchContent();
        if (StringUtils.isEmpty(errorMsg))
            errorMsg = "success";
        if (StringUtils.isEmpty(logLevel))
            logLevel = "info";

        retPrimaryAppEnMatchContent.setErrorContent(errorMsg);

        Date dtE = new Date();
        Long uTAll = dtE.getTime() - dtB.getTime();
        String strLog = formatToString(retPrimaryAppEnMatchContent, psrExamContext, uTAll);
        switch (logLevel) {
            case "info":
                log.info(strLog);
                break;
            case "error":
                log.error(strLog);
                break;
            case "warn":
                log.warn(strLog);
                break;
            default:
                log.info(strLog);
        }

        return retPrimaryAppEnMatchContent;
    }

    private String formatToString(PsrPrimaryAppEnMatchContent retPrimaryAppEnMatchContent, PsrExamContext psrExamContext,
                                  Long spendTime) {
        String strLog = retPrimaryAppEnMatchContent.formatList();
        strLog += "[product:" + psrExamContext.getProduct() + " userId:" + psrExamContext.getUserId().toString();
        strLog += " region:" + Integer.valueOf(psrExamContext.getRegionCode()).toString();
        strLog += " book:" + psrExamContext.getBookId() + " unit:" + psrExamContext.getUnitId();
        strLog += " eCount:" + Integer.valueOf(psrExamContext.getECount()).toString();
        strLog += " eType:" + psrExamContext.getEType() + " matchCount:" + psrExamContext.getMatchCount();
        strLog += "]";
        strLog += "[TotalTime:" + spendTime.toString() + "]";

        return strLog;
    }


}
