package com.voxlearning.utopia.service.psr.impl.examen;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.io.Serializable;


@Slf4j
@Named
@Deprecated // 2015.08.10
public class PsrExamEnAfentiQuizController implements Serializable {

// fixme 2015.09.01 之后暂不维护
/*
    @Inject private PsrConfig psrConfig;
    @Inject private PsrBooksSentences psrBooksSentences;
    @Inject private PsrExamEnController psrExamEnController;
    @Inject private PsrExamEnAfentiQuizCache psrExamEnAfentiQuizCache;
    @Inject private EkCouchbaseDao ekCouchbaseDao;


    public PsrExamContent deal(String product, String uType,
                               Long userId, int regionCode, Long bookId, Long unitId, int eCount,
                               float minP, float maxP) {
        PsrExamContext psrExamContext = new PsrExamContext(psrConfig,
                product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP);

        if (!psrExamContext.isExamEnAfentiQuiz())
            return psrExamEnController.dealAll(psrExamContext);

        return deal( psrExamContext );
    }

    // 1. fixme unitId可能为组id,需要转换为对应的unitId列表
    // 2. fixme 没有标准试卷的 直接使用psr推荐
    // 3. fixme 优先推荐标准试卷, 1> 切分标准试卷 eidcount/12 = n, eidcount/n = m, eidcount%m = i, i相同的为一张试卷的一个任务包
    // 4. fixme 标准试卷的一次任务包够12道题 直接推荐, 不够的题 由psr推荐 补够, 补的题不能与之前的标准试卷的题重复 (1:标准试卷包内不用处理重复的问题, 2:psr推送的题目按照现有逻辑就可以了, 3:psr推题不能喝试卷包中的重复)
    // 5. fixme 记录推荐的试卷任务包,推荐过的不在推荐

    public PsrExamContent deal(PsrExamContext psrExamContext) {
        Date dtB = new Date();
        PsrExamContent retExamContent = new PsrExamContent();

        if (psrExamContext == null) {
            return logContent(retExamContent, null, "PsrExamEnAfentiQuizController deal psrExamContext is null.", dtB, "error");
        }
        if (psrExamEnAfentiQuizCache == null)
            return logContent(retExamContent, psrExamContext, "psrExamEnAfentiQuizCache is null.", dtB, "error");
        if (psrBooksSentences == null)
            return logContent(retExamContent, psrExamContext, "psrBooksSentences is null.", dtB, "error");

        // 获取课本信息,转换UnitGrouId 为 unitIds
        List<Long> units = null;
        PsrBookPersistence psrBookPersistence = psrBooksSentences.getBookPersistenceByBookId(psrExamContext.getBookId());
        if (psrBookPersistence != null && psrBookPersistence.getBookStructure() == 1) {
            units = psrBookPersistence.getUnitIdsByGroupId(psrExamContext.getUnitId());
        } else {
            units = new ArrayList<>();
            units.add(psrExamContext.getUnitId());
        }
        if (units == null)
            units = new ArrayList<>();
        if (units.size() <= 0)
            units.add(psrExamContext.getUnitId());

        // 获取该课本的某些单元下的标准试卷
        List<ExamEnQuizPackageAfenti> quizPackageAfentis = psrExamEnAfentiQuizCache.getExamEnQuizByBookIdAndUnitId(psrExamContext.getBookId(), units);

        // 没有该课本的该单元对应的标准试卷,使用psr推荐
        if (quizPackageAfentis == null || quizPackageAfentis.size() <= 0) {
            psrExamContext.setWriteLog(false);
            retExamContent = psrExamEnController.dealAll(psrExamContext);
            psrExamContext.setWriteLog(true);
            return logContent(retExamContent, psrExamContext, retExamContent.getErrorContent(), dtB, "info");
        }

        // 获取推荐过的标准试卷eids
        Map<String, Long> userAfentiQuizPsr = getUserAfentiQuizPsr(psrExamContext.getUserId());  // afenti quiz 推荐过的试卷

        List<ExamEnAfentiQuizEidItem> quizPackageAfentiEidsOut = null;

        for (ExamEnQuizPackageAfenti tmpQuiz : quizPackageAfentis) {
            // 根据 quizId 切分任务(taskId),如果任务个数不够12个 则启用psr推荐补题

            Map<Integer/ *taskId* /, List<ExamEnAfentiQuizEidItem>> taskGroup = getQuizTaskGroup(tmpQuiz);
            if (taskGroup == null && taskGroup.size() <= 0)
                continue;

            // 根据Quiz顺序开始推荐(无序),已经推荐过的不在推荐
            for (Map.Entry<Integer/ *taskId* /, List<ExamEnAfentiQuizEidItem>> entry : taskGroup.entrySet()) {
                // bug 了吧
                if (entry.getValue() == null)
                    continue;

                // 查看该组任务是否已经推荐过,是则不推 - 继续推荐下个任务
                quizPackageAfentiEidsOut = new ArrayList<>();
                for (ExamEnAfentiQuizEidItem tmpItem : entry.getValue()) {
                    // 判断是否推荐过
                    if (userAfentiQuizPsr.containsKey(tmpItem.getEid()))
                        continue;
                    quizPackageAfentiEidsOut.add(tmpItem);
                }
                if (quizPackageAfentiEidsOut.size() > 0)
                    break;
            }

            if (quizPackageAfentiEidsOut != null && quizPackageAfentiEidsOut.size() > 0)
                break;
        }

        Integer afentiQuizCount = 0;
        if (quizPackageAfentiEidsOut != null && quizPackageAfentiEidsOut.size() > 0) {
            List<PsrExamItem> examList = new ArrayList<>();
            for (ExamEnAfentiQuizEidItem quizEidItem : quizPackageAfentiEidsOut) {
                PsrExamItem examItem = new PsrExamItem();
                examItem.setEk(quizEidItem.getEk());
                examItem.setEt(quizEidItem.getEt());
                examItem.setEid(quizEidItem.getEid());
                examItem.setAlogv("afenti01");
                examItem.setWeight(0.0);
                examItem.setPsrExamType("afentiquiz_std");
                if (psrExamContext.getRecommendEids() == null) {
                    List<String> recommendEids = new ArrayList<>();
                    psrExamContext.setRecommendEids(recommendEids);
                }
                psrExamContext.getRecommendEids().add(quizEidItem.getEid());
                afentiQuizCount += 1;
                examList.add(examItem);
            }
            retExamContent.setExamList(examList);
        }

        // 保存推荐的标准试卷eids
        setUserAfentiQuizPsr(userAfentiQuizPsr, retExamContent, psrExamContext.getUserId());

        // 如果题不够 则psr补题
        if (afentiQuizCount < 12) {
            Integer eCount = psrExamContext.getECount();
            psrExamContext.setECount(12 - afentiQuizCount);
            psrExamContext.setWriteLog(false);
            PsrExamContent tmpContent = psrExamEnController.dealAll(psrExamContext);
            psrExamContext.setWriteLog(true);
            psrExamContext.setECount(eCount);
            if (tmpContent.isSuccess() && tmpContent.getExamList().size() > 0) {
                retExamContent.addToExamList(tmpContent);
            }
        }

        return logContent(retExamContent, psrExamContext, "success", dtB, "info");
    }

    / *
     * ver \t eid:date,eid:date,
     * /
    public Map<String, Long> getUserAfentiQuizPsr(Long userId) {
        Map<String, Long> retMap = new HashMap<>();
        if (userId == null || ekCouchbaseDao == null)
            return retMap;

        String strKey = "afentiquizpsr_" + userId;
        String strValue = ekCouchbaseDao.getCouchbaseDataByKey(strKey);
        if (StringUtils.isEmpty(strValue))
            return retMap;

        String[] sArr = strValue.split("\t");
        if (sArr.length < 2 || StringUtils.isEmpty(sArr[1]))
            return retMap;

        String[] eidArr = sArr[1].split(",");
        if (eidArr.length <= 0)
            return retMap;

        for (String line : eidArr) {
            if (StringUtils.isEmpty(line))
                continue;
            String[] tmpArr = line.split(":");
            if (tmpArr.length < 2)
                continue;
            String eid = tmpArr[0];
            Long date = PsrTools.stringToLong(tmpArr[1]);
            retMap.put(eid, date);
        }

        return retMap;
    }

    public boolean setUserAfentiQuizPsr(Map<String, Long> eids, PsrExamContent retExamContent, Long userId) {
        if (eids == null || userId == null || ekCouchbaseDao == null)
            return false;
        if (retExamContent == null || retExamContent.getExamList().size() <= 0)
            return false;

        Date dt = new Date();
        for (PsrExamItem item : retExamContent.getExamList()) {
            eids.put(item.getEid(), dt.getTime());
        }

        String ver = "1";
        String strKey = "afentiquizpsr_" + userId;
        String strValue = ver + "\t";

        for (Map.Entry<String, Long> entry : eids.entrySet()) {
            strValue += entry.getKey() + ":" + entry.getValue() + ",";
        }

        if (StringUtils.isEmpty(strValue))
            return false;

        return ekCouchbaseDao.setCouchbaseData(strKey, strValue);
    }

    private Map<Integer/ *taskId* /, List<ExamEnAfentiQuizEidItem>> getQuizTaskGroup(ExamEnQuizPackageAfenti quizPackageAfenti) {
        Map<Integer/ *taskId* /, List<ExamEnAfentiQuizEidItem>> taskGroup = new HashMap<>();
        if (quizPackageAfenti == null || quizPackageAfenti.getQuizEidMap() == null || quizPackageAfenti.getQuizEidMap().size() <= 0)
            return taskGroup;

        // fixme 试卷分任务,每个任务12个包
        Integer quizEidCount = quizPackageAfenti.getQuizEidMap().size();
        Map<Integer/ *id* /, Integer/ *taskId* /> taskGroupPos = getQuizTaskIdPos(quizEidCount);
        if (taskGroupPos == null)
            taskGroupPos = new HashMap<>();
        if (taskGroupPos.size() <= 0) {
            for (int i = 0; i < 12; i++)
                taskGroupPos.put(i, 0);
        }

        Integer index = 0;
        for (Map.Entry<Integer/ *eid_pos* /, ExamEnAfentiQuizEidItem> entry : quizPackageAfenti.getQuizEidMap().entrySet()) {
            Integer taskId = taskGroupPos.containsKey(index) ? taskGroupPos.get(index) : 0;
            List<ExamEnAfentiQuizEidItem> tmpItem = null;
            if (taskGroup.containsKey(taskId)) {
                tmpItem = taskGroup.get(taskId);
            } else {
                tmpItem = new ArrayList<>();
            }
            tmpItem.add(entry.getValue());
            taskGroup.put(taskId, tmpItem);
            index += 1;
        }

        return taskGroup;
    }

    private Map<Integer/ *pos* /, Integer/ *taskgroup* /> getQuizTaskIdPos(Integer quizEidCount) {
        Map<Integer, Integer> retMap = new HashMap<>();
        Integer step = quizEidCount / 12;
        step = step <= 0 ? 1 : step;

        for (int i = 0; i < quizEidCount; i++) {
            Integer taskId = i % step;
            if (i > 12 * step - 1)
                taskId = step;
            retMap.put(i, taskId);
        }

        return retMap;
    }

    private PsrExamContent logContent(PsrExamContent retExamContent,
                                      PsrExamContext psrExamContext, String errorMsg,
                                      Date dtB, String logLevel) {
        if (retExamContent == null)
            retExamContent = new PsrExamContent();
        if (StringUtils.isEmpty(errorMsg))
            errorMsg = "success";

        retExamContent.setErrorContent(errorMsg);
        if (!errorMsg.equals("success"))
            retExamContent.getExamList().clear();

        if (psrExamContext != null && psrExamContext.isWriteLog()) {
            if (StringUtils.isEmpty(logLevel))
                logLevel = "info";
            Date dtE = new Date();
            Long uTAll = dtE.getTime() - dtB.getTime();
            String strLog = formatReturnLog(retExamContent, psrExamContext, uTAll);
            switch (logLevel) {
                case "info":
                    PsrExamEnAfentiQuizController.log.info(strLog);
                    break;
                case "error":
                    PsrExamEnAfentiQuizController.log.error(strLog);
                    break;
                case "warn":
                    PsrExamEnAfentiQuizController.log.warn(strLog);
                    break;
                default:
                    PsrExamEnAfentiQuizController.log.info(strLog);
            }
        }

        return retExamContent;
    }

    private String formatReturnLog(PsrExamContent retExamContent, PsrExamContext psrExamContext, Long totalTime) {
        String strLog = retExamContent.formatList("ExamEnAfentiQuiz");
        strLog += "[product:" + psrExamContext.getProduct() + " uType:" + psrExamContext.getUType() + " userId:" + psrExamContext.getUserId().toString();
        strLog += " region:" + Integer.valueOf(psrExamContext.getRegionCode()).toString() + " book:" + psrExamContext.getBookId().toString() + " unit:" + psrExamContext.getUnitId().toString();
        strLog += " eCount:" + Integer.valueOf(psrExamContext.getECount()).toString();
        strLog += " minP:" + Float.valueOf(psrExamContext.getMinP()).toString() + " maxP:" + Float.valueOf(psrExamContext.getMaxP()).toString() + "]";
        strLog += "[TotalTime:" + totalTime.toString() + "]";

        return strLog;
    }

*/
    // fixme 2015.09.01 之后暂不维护

}