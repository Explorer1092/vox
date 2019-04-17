package com.voxlearning.utopia.service.psr.impl.midtermreview.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.psr.entity.midtermreview.*;
import com.voxlearning.utopia.service.psr.impl.dao.midtermreview.EnglishStatisticsResultDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2016/10/10.
 */

@Slf4j
@Named
public class PsrPackageService  extends SpringContainerSupport {
    @Inject private EnglishStatisticsResultDao englishStatisticsResultDao;
    private static final Integer SIMILAR_QUESTIONS_SIZE = 20;
    private static final Integer KNOWLEDGE_POINTS_SIZE_THRESHOLD = 4;

    public List<EnglishPackage> recomPsrPackageByGroupAndBook(String bookId, Integer groupId){
        List<EnglishStatisticsResult> englishStatisticsResultList = englishStatisticsResultDao.getEnglishStatisticsResultByGroupAndBook(bookId, groupId);
        if (CollectionUtils.isEmpty(englishStatisticsResultList)) return Collections.emptyList();

        Set<String> allKnowledgePoints = Sets.newHashSet();
        allKnowledgePoints.addAll(englishStatisticsResultList.stream().map(EnglishStatisticsResult::getKpInfos).flatMap(Collection::stream).map(KnowledgePointInfo::getKnowledgePointId).collect(Collectors.toList()));
        if (allKnowledgePoints.size() <= KNOWLEDGE_POINTS_SIZE_THRESHOLD)
            return Collections.emptyList();

        List<EnglishPackage> englishPackages = Lists.newArrayList();

        for (EnglishStatisticsResult e : englishStatisticsResultList){
            EnglishPackage englishPackage = new EnglishPackage();
            String questionType = e.getQuestionType();
            String kpType = e.getKpType();
            if (StringUtils.equals(questionType, "wrong")) {
                if (StringUtils.equals(kpType, "primary")){
                    englishPackage.setPakType(0);
                    englishPackage.setPakName("主知识点错题");
                } else if(StringUtils.equals(kpType, "secondary")) {
                    englishPackage.setPakType(1);
                    englishPackage.setPakName("次知识点错题");
                }
            } else if(StringUtils.equals(questionType, "similar")){
                if (StringUtils.equals(kpType, "primary")){
                    englishPackage.setPakType(2);
                    englishPackage.setPakName("主知识点类题");
                } else if(StringUtils.equals(kpType, "secondary")) {
                    englishPackage.setPakType(3);
                    englishPackage.setPakName("次知识点类题");
                }
            }

            List<String> knowledgePoints = Lists.newArrayList();
            List<EnglishQuestion> englishQuestions = Lists.newArrayList();
            List<String> existDocIds = Lists.newArrayList();

            List<KnowledgePointInfo> knowledgePointInfos = e.getKpInfos();
            if (englishPackage.getPakType() <= 1) {
                for (KnowledgePointInfo k : knowledgePointInfos) {
                    if (!knowledgePoints.contains(k.getKnowledgePointId())) {
                        knowledgePoints.add(k.getKnowledgePointId());
                    }

                    List<QuestionInfo> questionInfos = k.getRightRateInfos();
                    for (QuestionInfo q : questionInfos) {
                        if (q.getRightRate() < 0.75 && !existDocIds.contains(q.getDocId())) {
                            EnglishQuestion englishQuestion = new EnglishQuestion();
                            englishQuestion.setDocId(q.getDocId());
                            englishQuestion.setAccurate(q.getRightRate());

                            englishQuestions.add(englishQuestion);
                            existDocIds.add(q.getDocId());
                        }
                    }
                }
            } else {
                Integer current = 0;
                while (englishQuestions.size() < SIMILAR_QUESTIONS_SIZE) {
                    boolean hasNew = false;
                    for (KnowledgePointInfo k : knowledgePointInfos) {
                        if(englishQuestions.size() >= SIMILAR_QUESTIONS_SIZE) break;
                        if (!knowledgePoints.contains(k.getKnowledgePointId()))
                            knowledgePoints.add(k.getKnowledgePointId());
                        if (k.getRightRateInfos().size() > current) {
                            QuestionInfo questionInfo = k.getRightRateInfos().get(current);
                            if (!existDocIds.contains(questionInfo.getDocId())) {
                                EnglishQuestion englishQuestion = new EnglishQuestion();
                                englishQuestion.setDocId(questionInfo.getDocId());
                                englishQuestion.setAccurate(questionInfo.getRightRate());

                                englishQuestions.add(englishQuestion);
                                existDocIds.add(questionInfo.getDocId());

                                hasNew = true;
                            }
                        }
                    }
                    if (!hasNew) break;
                    current++;
                }
            }

            englishPackage.setPakId(e.getId());
            englishPackage.setBookId(e.getBookId());
            englishPackage.setCatalogId(e.getCatalogId());
            englishPackage.setKnowledgePoints(knowledgePoints);
            englishPackage.setQuestions(englishQuestions);

            englishPackages.add(englishPackage);
        }

        return  englishPackages;
    }



    public List<EnglishPackage> recomArtificialPsrPackageByGroupAndBook(String bookId, Integer groupId){

        List<EnglishPackage> englishPackages = Lists.newArrayList();

/////////////////////////////////////
        EnglishPackage englishPackage = new EnglishPackage();

        englishPackage.setPakType(0);
        englishPackage.setBookId(bookId);
        englishPackage.setCatalogId("BKC_20300076261886");

        List<String> knowledgePoints = Lists.newArrayList();
        knowledgePoints.add("KP_20300000952812");
        knowledgePoints.add("KP_20300001351065");
        englishPackage.setKnowledgePoints(knowledgePoints);

        List<EnglishQuestion> englishQuestions = Lists.newArrayList();
        EnglishQuestion englishQuestion = new EnglishQuestion();
        englishQuestion.setDocId("Q_20300818620736");
        englishQuestion.setAccurate(0.5);
        englishQuestions.add(englishQuestion);
        EnglishQuestion englishQuestiona = new EnglishQuestion();
        englishQuestiona.setDocId("Q_20300818662497");
        englishQuestiona.setAccurate(0.35);
        englishQuestions.add(englishQuestiona);

        englishPackage.setQuestions(englishQuestions);
        englishPackage.setPakId("000000000000000");
        englishPackage.setPakName("主知识点错题");

        englishPackages.add(englishPackage);
/////////////////////////////////////////////
        EnglishPackage englishPackage1 = new EnglishPackage();

        englishPackage1.setPakType(1);
        englishPackage1.setBookId(bookId);
        englishPackage1.setCatalogId("BKC_20300076261886");

        List<String> knowledgePoints1 = Lists.newArrayList();
        knowledgePoints1.add("KP_20300010117121");
        knowledgePoints1.add("KP_20300001546889");
        englishPackage1.setKnowledgePoints(knowledgePoints1);

        List<EnglishQuestion> englishQuestions1 = Lists.newArrayList();
        EnglishQuestion englishQuestion1 = new EnglishQuestion();
        englishQuestion1.setDocId("Q_20300831260206");
        englishQuestion1.setAccurate(0.25);
        englishQuestions1.add(englishQuestion1);
        EnglishQuestion englishQuestion1a = new EnglishQuestion();
        englishQuestion1a.setDocId("Q_20300784997151");
        englishQuestion1a.setAccurate(0.54);
        englishQuestions1.add(englishQuestion1a);

        englishPackage1.setQuestions(englishQuestions1);
        englishPackage1.setPakId("1111111111111111");
        englishPackage1.setPakName("次知识点错题");

        englishPackages.add(englishPackage1);
/////////////////////////////////////////////
        EnglishPackage englishPackage2 = new EnglishPackage();

        englishPackage2.setPakType(2);
        englishPackage2.setBookId(bookId);
        englishPackage2.setCatalogId("BKC_20300076261886");

        List<String> knowledgePoints2 = Lists.newArrayList();
        knowledgePoints2.add("KP_20300006949682");
        knowledgePoints2.add("KP_20300014137627");
        englishPackage2.setKnowledgePoints(knowledgePoints2);

        List<EnglishQuestion> englishQuestions2 = Lists.newArrayList();
        EnglishQuestion englishQuestion2 = new EnglishQuestion();
        englishQuestion2.setDocId("Q_20300831256564");
        englishQuestion2.setAccurate(0.0);
        englishQuestions2.add(englishQuestion2);
        EnglishQuestion englishQuestion2a = new EnglishQuestion();
        englishQuestion2a.setDocId("Q_20300785077053");
        englishQuestion2a.setAccurate(0.0);
        englishQuestions2.add(englishQuestion2a);

        englishPackage2.setQuestions(englishQuestions2);
        englishPackage2.setPakId("222222222222222");
        englishPackage2.setPakName("主知识点类题");

        englishPackages.add(englishPackage2);
/////////////////////////////////////////////
        EnglishPackage englishPackage3 = new EnglishPackage();

        englishPackage3.setPakType(3);
        englishPackage3.setBookId(bookId);
        englishPackage3.setCatalogId("BKC_20300076261886");

        List<String> knowledgePoints3 = Lists.newArrayList();
        knowledgePoints3.add("KP_20300001143101");
        knowledgePoints3.add("KP_20300008743416");
        englishPackage3.setKnowledgePoints(knowledgePoints3);

        List<EnglishQuestion> englishQuestions3 = Lists.newArrayList();
        EnglishQuestion englishQuestion3 = new EnglishQuestion();
        englishQuestion3.setDocId("Q_20300785032264");
        englishQuestion3.setAccurate(0.0);
        englishQuestions3.add(englishQuestion3);
        EnglishQuestion englishQuestion3a = new EnglishQuestion();
        englishQuestion3a.setDocId("Q_20300831243167");
        englishQuestion3a.setAccurate(0.0);
        englishQuestions3.add(englishQuestion3a);

        englishPackage3.setQuestions(englishQuestions3);
        englishPackage3.setPakId("33333333333333333");
        englishPackage3.setPakName("次知识点类题");

        englishPackages.add(englishPackage3);

/////////////////////////////////////////////
        return  englishPackages;
    }
}
