package com.voxlearning.utopia.service.psr.impl.exammath;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.entity.mathnewkp.MathQuestion;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentencesNew;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.dao.PsrAboveLevelBookEidsNewMathDao;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnData;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnFilter;
import com.voxlearning.utopia.service.psr.impl.util.PsrIrtPredictEx;
import com.voxlearning.utopia.service.psr.mathnewkp.loader.PsrQuestionsLoader;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

/**
 * Created by Chaoli Lee on 14-7-7.
 * Psr 核心类
 * 用之前 需要对 内部变量进行初始化
 */

@Slf4j
@Named
public class PsrExamMathCore implements Serializable {
    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrExamEnFilter psrExamEnFilter;
    @Inject private PsrExamEnData psrExamEnData;
    @Inject private PsrAboveLevelBookEidsNewMathDao psrAboveLevelBookEidsNewMathDao;
    @Inject private PsrBooksSentencesNew psrBooksSentencesNew;
    @Inject private PsrQuestionsLoader psrQuestionsLoader;
    @Inject private QuestionLoaderClient questionLoaderClient;

    public EkEidListContent doCore(PsrExamContext psrExamContext) {
        if (psrExamContext == null)
            return new EkEidListContent();

        EkEidListContent ekEidListContent = psrExamContext.getEkEidListContent();
        if (ekEidListContent.getEids().size() >= psrExamContext.getECount())
            return ekEidListContent;

        if (ekCouchbaseDao == null || psrExamEnFilter == null || psrAboveLevelBookEidsNewMathDao == null || psrBooksSentencesNew == null)
            return ekEidListContent;

//        Map<String,String> kpOldKpMap = psrBooksSentencesNew.getOldKnowledgePointIdByBookId(psrExamContext.getBookId());
        PsrIrtPredictEx.setEkCouchbaseDao(ekCouchbaseDao); // p002 版本算法
        PsrIrtPredictEx.setG_predict_weight(psrExamContext.getGPredictWeight());

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
        // 对 Ek 做过滤, learning_profile 中的ek 是否 超出 bookId的范围,超出的Ek 不推荐
        // 如果 配置 不启用 bookId范围过滤 知识点, 则不做过滤

        UserExamContent userExamContent = psrExamEnData.getUserExamContentId(psrExamContext, Subject.MATH);
        if (userExamContent == null)
            return ekEidListContent;

        EksTypeListContent eksTypeListContent = psrExamEnFilter.doFilter(psrExamContext, Subject.MATH);
        if (eksTypeListContent == null || eksTypeListContent.isAllListEmpty())
            return ekEidListContent;

        Integer validKnowledgePointNum = eksTypeListContent.getValidEkNum();

        // 权重归一化
        String ekType = "levelone";  // levelzero, leveltwo, down
        List<Map.Entry<Double, String>> eksIds = psrExamEnFilter.getEksSortByWeightFromMap(psrExamContext, ekType);

        if (psrExamContext.getRecommendEids() == null)
            psrExamContext.setRecommendEids(new ArrayList<String>());

        // 教材信息
        PsrBookPersistenceNew psrBookPersistenceNew = psrBooksSentencesNew.getBookPersistenceByBookId(psrExamContext.getBookId());

        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
        // 轮盘算法,获取知识点
        int reqEidCount = psrExamContext.getECount();
        int curEidCount = ekEidListContent.getEids().size();

        boolean noMoreEks = false;

        for (; !noMoreEks && curEidCount < reqEidCount; ) {
            int index = 0;
            String ek = "";
            boolean isGetEk = false;
            int ekCount = 0;

            while (!isGetEk && !noMoreEks) {
                // get ek
                /*
                 * 首先 选 基本掌握 的知识点
                 * 其次是 没有基本掌握的知识点时 根据学生的能力 选, 能力查的学生选 掌握的知识点, 能力高的学生选 未掌握的知识点
                 * 再次是 没有可选知识点时, 选择当天做对的知识点
                 * 当所以的知识点集合 都没有数据时，则跳出选题 并返回
                 */

                /*
                 * 当前类型无列表 或者列表为空,则循环列表,直到最后一个列表:type="down" 或者 无列表:type=""
                 */
                if (eksIds == null || eksIds.size() <= 0) {
                    switch (ekType) {
                        case "levelone":
                            if (userExamContent.getIrtTheta() > psrExamContext.getHighIrtTheta()) {
                                ekType = "levelzero";
                            } else {
                                ekType = "leveltwo";
                            }
                            break;
                        case "leveltwo":
                            if (userExamContent.getIrtTheta() > psrExamContext.getHighIrtTheta()) {
                                ekType = "down";
                            } else {
                                ekType = "levelzero";
                            }
                            break;
                        case "levelzero":
                            if (userExamContent.getIrtTheta() > psrExamContext.getHighIrtTheta()) {
                                ekType = "leveltwo";
                            } else {
                                ekType = "down";
                            }
                            break;
                        case "down":
                        default:
                            ekType = "";
                            noMoreEks = true;
                            break;
                    }

                    if (!StringUtils.isEmpty(ekType))
                        eksIds = psrExamEnFilter.getEksSortByWeightFromMap(psrExamContext, ekType);
                    // 循环下一个列表
                    if (eksIds == null || eksIds.size() <= 0) {
                        continue;
                    }
                }

                Random radom = psrExamContext.getRandom();
                double radomTd = radom.nextInt(1000000) + 0.0;
                double weightTmp = radomTd / 1000000 * psrExamContext.getBaseNumberForWeight();

                double weightNsum = 0.0;
                for (int i = 0; eksIds != null && i < eksIds.size(); i++) {
                    Map.Entry<Double, String> entry = eksIds.get(i);

                    weightNsum += entry.getKey();

                    index = 0;
                    boolean bGet = false;
                    if (weightTmp <= weightNsum) {
                        index = i;
                        bGet = true;
                    }
                    if (i == eksIds.size() - 1) {
                        index = i;
                        bGet = true;
                    }

                    if (bGet) {
                        isGetEk = true;
                        ek = eksIds.get(index).getValue();

                        if (psrExamContext.getBaseNumberForWeight() <= 0)
                            psrExamContext.setBaseNumberForWeight(100);

                        //ekCount = (reqEidCount - curEidCount) * eksIds.get(index).getKey().intValue() / baseNumberForWeight + 1;

                        ekCount = 1;
                        if (psrExamContext.getEidCountRatePerEk() > 0)
                            ekCount = Double.valueOf(Integer.valueOf(reqEidCount).doubleValue() * psrExamContext.getEidCountRatePerEk()).intValue();

                        ekCount = validKnowledgePointNum <= 2 ? reqEidCount : (ekCount>0 ? ekCount : 1); // 当可用的知识点数量<=2 的还是放开知识点的数量限制

                        eksIds.remove(index);

                        break;
                    }
                }
            }  // end get ek while

            int curEkCount = 0;  // 记录当前ek取题的个数
            if (isGetEk && !StringUtils.isEmpty(ek) && curEidCount < reqEidCount && curEkCount < ekCount) {
                // get eids by ekCount
                EkEidListContent ekEidListContentEx = null;

                // 数学新教材 从huihui接口获取对应的题目信息
                if (psrExamContext.getSubject().equals(Subject.MATH) && psrBookPersistenceNew.getLatestVersion() == 1) {
                    ekEidListContentEx = getMathNewKpQid(psrExamContext, ek);
                    // 如果 ek=kp:feature
                    if (ek.contains(":"))
                        ek = ek.substring(0, ek.indexOf(":"));
                } else {
                    if (ekCouchbaseDao != null)
                        ekEidListContentEx = ekCouchbaseDao.getEkEidListContentFromCouchbaseByEk(ek, psrExamContext.getGrade());
                }
                if (ekEidListContentEx == null || ekEidListContentEx.isEkListNull() || ekEidListContentEx.getEkList().size() <= 0) {
                    // 该知识点 没有取到 题, 重新获取知识点 , continue for {get ek, get eids}
                    continue;
                }
//                String oldEk = kpOldKpMap.get(ek);
                // 以列表的形式返回,但实际只有一个ek
                for (int i = 0; i < ekEidListContentEx.getEkList().size() && curEidCount < reqEidCount && curEkCount < ekCount; i++) {
                    EkToEidContent ekToEidContent = ekEidListContentEx.getEkList().get(i);
                    if (ekToEidContent == null || ekToEidContent.isEidListNull())
                        continue;

                    // 计算预估通过率, 并根据预估通过率 和 题型热度排序
                    List<Map.Entry<Double, EidItem>> listEids = psrExamEnFilter.getEidsPredictRate(psrExamContext, ekToEidContent, ekCount, Subject.MATH);
                    if (listEids == null)
                        continue;

                    // fixme 下次优化逻辑时.保存符合要求的eid信息(>ekCount 之外的),备用。当只有一个知识点的题量比较多,同时其他知识点没有题可推,这个时候就要使用备用的eid
                    if (userExamContent.getIrtTheta() >= psrExamContext.getLowIrtTheta() && userExamContent.getIrtTheta() <= psrExamContext.getHighIrtTheta()) {
                        // 中能力的学生 从中间开始取题
                        List<Integer> posList = getDataFromMiddleToSide(listEids.size());

                        for (int j = 0; posList != null && j < posList.size() && curEidCount < reqEidCount && curEkCount < ekCount; j++) {
                            int n = posList.get(j);

                            if (n < 0 || n > listEids.size())
                                continue;

                            EidItem eidItem = listEids.get(n).getValue();

                            if (eidItem == null)
                                continue;

                            String mainNKP = "";
                            // fixme ---------------------------------------------
                            NewQuestion question = questionLoaderClient.loadQuestionIncludeDisabled(eidItem.getEid());
                            if (!psrExamEnFilter.isPsrFromOnlineQuestionTable(psrExamContext, eidItem.getEid(), Subject.MATH, question)) {
                                //log.error("FindAnBadEid:" + eidItem.getEid() + ",badEk:" + ek);
                                continue;
                            }
                            mainNKP = (ek.startsWith("TM") || ek.startsWith("SM")) ? psrExamEnFilter.getMainNewKPFromQuestion(psrExamContext, question) : ek;
                            if (StringUtils.isBlank(mainNKP))
                                continue;

                            if (psrExamContext.getRecommendEids() == null)
                                psrExamContext.setRecommendEids(new ArrayList<String>());
                            if (psrExamContext.getRecommendEids().contains(eidItem.getEid()))
                                continue;
                            psrExamContext.getRecommendEids().add(eidItem.getEid());
                            curEkCount++;
                            curEidCount++;
                            ekEidListContent.addItemByEk(mainNKP, eidItem);
                        }
                    } else {
                        // 高能力 和 低能力的学生 按队列顺序取题
                        for (int j = 0; j < listEids.size() && curEidCount < reqEidCount && curEkCount < ekCount; j++) {
                            EidItem eidItem = listEids.get(j).getValue();

                            if (eidItem == null)
                                continue;

                            String mainNKP = "";
                            // fixme ---------------------------------------------
                            NewQuestion question = questionLoaderClient.loadQuestionIncludeDisabled(eidItem.getEid());
                            if (!psrExamEnFilter.isPsrFromOnlineQuestionTable(psrExamContext, eidItem.getEid(), Subject.MATH, question)) {
                                //log.error("FindAnBadEid:" + eidItem.getEid() + ",badEk:" + ek);
                                continue;
                            }
                            mainNKP = (ek.startsWith("TM") || ek.startsWith("SM")) ? psrExamEnFilter.getMainNewKPFromQuestion(psrExamContext, question) : ek;                            if (StringUtils.isBlank(mainNKP))
                                continue;

                            if (psrExamContext.getRecommendEids() == null)
                                psrExamContext.setRecommendEids(new ArrayList<String>());
                            if (psrExamContext.getRecommendEids().contains(eidItem.getEid()))
                                continue;
                            psrExamContext.getRecommendEids().add(eidItem.getEid());
                            curEkCount++;
                            curEidCount++;
                            ekEidListContent.addItemByEk(mainNKP, eidItem);
                        }
                    }  // end if 按学生能力取题
                }  // end for 按学生能力取题

            } // end while get eids while

        }  //  end for curEidCount and reqEidCount

        return ekEidListContent;
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

    public EkEidListContent getMathNewKpQid(PsrExamContext psrExamContext, String ek) {
        return getMathNewKpQids(psrExamContext, Collections.singletonList(ek), 50);
    }

    public EkEidListContent getMathNewKpQids(PsrExamContext psrExamContext, List<String> eks, Integer count) {
        EkEidListContent ekEidListContent = new EkEidListContent();
        Map<String, List<MathQuestion>> retMap = psrQuestionsLoader.loadMathQuestionsByKp(psrExamContext.getBookId(), psrExamContext.getUnitId(), eks, count);

        if (MapUtils.isEmpty(retMap))
            return ekEidListContent;

        for (String ek : eks) {
            if (!retMap.containsKey(ek))
                continue;

            if (CollectionUtils.isEmpty(retMap.get(ek))) {
                EkToEidContent item = new EkToEidContent();
                item.setEk(ek);
                item.setEidList(new ArrayList<>());
                ekEidListContent.getEkList().add(item);
            }

            for (MathQuestion mq : retMap.get(ek)) {
                EidItem item = new EidItem();
                item.setEid(mq.getQuestionId());
                item.setEt(mq.getContentTypeId() == null ? "0" : mq.getContentTypeId().toString());
                item.setIrtA(0.5D);
                item.setIrtB(0.5D);

                ekEidListContent.addItemByEk(ek, item);
            }
        }

        return ekEidListContent;
    }

}
