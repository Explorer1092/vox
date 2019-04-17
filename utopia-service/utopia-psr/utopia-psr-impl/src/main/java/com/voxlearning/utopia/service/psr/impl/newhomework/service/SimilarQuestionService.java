/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.psr.impl.newhomework.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.content.api.entity.BaseKnowledgePointRef;
import com.voxlearning.utopia.service.content.api.entity.NewKnowledgePointRef;
import com.voxlearning.utopia.service.content.consumer.NewKnowledgePointLoaderClient;
import com.voxlearning.utopia.service.psr.entity.newhomework.*;
import com.voxlearning.utopia.service.psr.impl.dao.newhomework.MathQuestionProfileDao;
import com.voxlearning.utopia.service.psr.impl.dao.newhomework.QuestionClusterProfileDao;
import com.voxlearning.utopia.service.psr.impl.dao.newhomework.SectionProgressIdDao;
import com.voxlearning.utopia.service.psr.impl.util.Md5;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.question.consumer.SolutionMethodRefLoaderClient;
import com.voxlearning.utopia.service.question.consumer.TestMethodLoaderClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/7/12
 * Time: 9:06
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
@Named
public class SimilarQuestionService extends SpringContainerSupport {

    @Inject
    private MathQuestionProfileDao mathQuestionProfileDao;
    @Inject
    private NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;
    @Inject
    private QuestionClusterProfileDao questionClusterProfileDao;
    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private QuestionPackageService packageService;
    @Inject
    private SectionProgressIdDao sectionProgressIdDao;
    @Inject
    private TestMethodLoaderClient testMethodLoaderClient;
    @Inject
    private NewHomeWorkCacheService newHomeWorkCacheService;
    @Inject
    private SolutionMethodRefLoaderClient solutionMethodRefLoaderClient;
    private static final Integer SIMILAR_QUESTIONS_SIZE = 5;
    private static final Integer KP_QUESTIONS_SIZE = 4;
    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]*>");
    private static final Integer EXPIRATION_SECONDS = 86400;
    private static final String MATH_SIMILAR_PREFIX = "MSP$";
    private static final String SECTION = "SECTION";
    private static final String UNIT = "UNIT";
    private static final Long TWO_TERMS_PROGRESSID_DELTA = 1500000000L;

    public List<NewQuestion> mockQuestions() {
        List<NewQuestion> newQuestions = Lists.newArrayList();
        List<String> qIds = Lists.newArrayList("Q_10200776265927", "Q_10200776132636", "Q_10200776135927", "Q_10200776138309", "Q_10200776147300", "Q_10200776150865", "Q_10200776151451", "Q_10200776164891", "Q_10200776166553", "Q_10200776169875", "Q_10200776170546", "Q_10200776197405", "Q_10200776206350", "Q_10200776207572", "Q_10200776209156", "Q_10200776211824", "Q_10200776215077", "Q_10200776219348", "Q_10200776223575", "Q_10200776227255", "Q_10200776247222", "Q_10200776198130", "Q_10200776199327", "Q_10200776202697", "Q_10200776204847", "Q_10200776205258", "Q_10200776212768", "Q_10200776218063", "Q_10200776232465", "Q_10200776234290", "Q_10200777189808", "Q_10200776203966", "Q_10200776208306", "Q_10200776210863", "Q_10200776213343", "Q_10200776216670", "Q_10200776217678", "Q_10200776220878", "Q_10200776221583", "Q_10200776222689", "Q_10200776226697", "Q_10200776240258", "Q_10200777235289", "Q_10200777249170", "Q_10200777253210", "Q_10200779080448", "Q_10200779083293", "Q_10200779086366", "Q_10200779091090", "Q_10200779092128", "Q_10200779102345", "Q_10200777499875", "Q_10200347270509", "Q_10200390042166", "Q_10200393016642", "Q_10200393098041", "Q_10200403502161", "Q_10200780574839", "Q_10200780578035", "Q_10200780579887", "Q_10200780581197", "Q_10200780582697", "Q_10200270455495", "Q_10200271384958", "Q_10200349597622", "Q_10200399366919", "Q_10200403158704", "Q_10200403191594", "Q_10200403591904", "Q_10200403639274", "Q_10200660590394", "Q_10200708448486", "Q_10200710994544", "Q_10200718940760", "Q_10200782956708", "Q_10200704941244", "Q_10200743506412", "Q_10200743521940", "Q_10200782966807", "Q_10200728935523", "Q_10200730095280", "Q_10200731844761", "Q_10200782960754", "Q_10200782963085", "Q_10200782967456", "Q_10200782975651", "Q_10200783158674", "Q_10200783610854", "Q_10200783617821", "Q_10200727401474", "Q_10200730079993", "Q_10200730104252", "Q_10200735349340", "Q_10200740679852", "Q_10200783631312", "Q_10200908353377", "Q_10200910765430", "Q_10200910603486", "Q_10200910610772", "Q_10200910634298");
        newQuestions.addAll(questionLoaderClient.loadQuestionByDocIds(qIds));
        return newQuestions;
    }

    public Map<String, List<NewQuestion>> recomQuestionsByKps(List<String> kps, SectionProgressId sectionProgressId, List<String> questionDocIds) {
        Map<String, List<NewQuestion>> results = Maps.newHashMap();
//        List<SectionProgressId> sectionProgressIds = sectionProgressIdDao.getSectionProgressIdsByUnit(catalogId);
//        SectionProgressId sectionProgressId = Collections.max(sectionProgressIds, Comparator.comparing(SectionProgressId::getProgressId));

        List<String> couchKeys = Lists.newArrayList();
        kps.stream().forEach(e->couchKeys.add(MATH_SIMILAR_PREFIX + e));
        Map<String,List<MathQuestionProfile>> md5SimilarQuestionsMap = newHomeWorkCacheService.gets(couchKeys);
        Map<String,List<MathQuestionProfile>> mathQuestionProfilesMap = Maps.newHashMap();
        Map<String,List<MathQuestionProfile>> questionsToBePutIntoCouchBase = Maps.newHashMap();

        List<String> existQuestions = Lists.newArrayList();
        Comparator<MathQuestionProfile> byProgressId = (q1,q2)->getProgressIdBasedOnSeriesId(sectionProgressId, q2).compareTo(getProgressIdBasedOnSeriesId(sectionProgressId, q1));

        for (Map.Entry<String,List<MathQuestionProfile>> entry:md5SimilarQuestionsMap.entrySet()) {
            List<MathQuestionProfile> mathQuestionProfiles = entry.getValue();
            if (mathQuestionProfiles == null) {
                String md5 = StringUtils.split(entry.getKey(),"$")[1];
                if (!questionsToBePutIntoCouchBase.containsKey(MATH_SIMILAR_PREFIX + md5)) {
                    mathQuestionProfiles = mathQuestionProfileDao.getSimQuestionProfileByKp(md5);
                    if (mathQuestionProfiles != null) {
                        questionsToBePutIntoCouchBase.put(MATH_SIMILAR_PREFIX + md5, mathQuestionProfiles);
                    }
                    mathQuestionProfilesMap.put(entry.getKey(),mathQuestionProfiles);
                }
            } else {
                mathQuestionProfilesMap.put(entry.getKey(),mathQuestionProfiles);
            }

            if (CollectionUtils.isNotEmpty(mathQuestionProfiles)) {
                existQuestions.addAll(mathQuestionProfiles.stream()
                        .filter(q->!isAboveLevel(sectionProgressId,q))
                        .map(MathQuestionProfile::getDoc_id)
                        .collect(Collectors.toList()));
            }
        }
        Map<String, NewQuestion> allCandidateQuestions = Maps.newHashMap();
        allCandidateQuestions.putAll(questionLoaderClient.loadQuestionByDocIds(existQuestions).stream().collect(Collectors.toMap(NewQuestion::getDocId, Function.identity())));

        for(Map.Entry<String,List<MathQuestionProfile>> entry : mathQuestionProfilesMap.entrySet()) {
            List<MathQuestionProfile> mathQuestionProfiles = entry.getValue();

            List<String> qIds = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(mathQuestionProfiles)) {
                qIds.addAll(mathQuestionProfiles.stream()
                        .filter(q -> allCandidateQuestions.containsKey(q.getDoc_id()))
                        .sorted(byProgressId)
                        .map(MathQuestionProfile::getDoc_id)
                        .collect(Collectors.toList()));
            }

            List<String> docsInUnitPackages = Lists.newArrayList();
            List<String> docsLeft = Lists.newArrayList();
            List<String> docsChosen = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(questionDocIds)) {
                for (String docId : qIds) {
                    if (docsInUnitPackages.size() >= KP_QUESTIONS_SIZE) break;
                    if (questionDocIds.contains(docId)) {
                        docsInUnitPackages.add(docId);
                    } else {
                        docsLeft.add(docId);
                    }
                }
            } else {
                docsLeft.addAll(qIds);
            }

            docsChosen.addAll(docsInUnitPackages);

            Integer count = KP_QUESTIONS_SIZE - docsChosen.size();
            if (count > 0) docsChosen.addAll(docsLeft.stream().limit(count).collect(Collectors.toList()));

            List<NewQuestion> questions = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(docsChosen)) {
                for (String qid: docsChosen) {
                    if (allCandidateQuestions.containsKey(qid)){
                        questions.add(allCandidateQuestions.get(qid));
                    }
                }
            }
            String kp = StringUtils.split(entry.getKey(),"$")[1];
            kp = StringUtils.replace(kp,":","|",-1);
            results.put(kp,questions);
        }

        if(!questionsToBePutIntoCouchBase.isEmpty()) newHomeWorkCacheService.puts(questionsToBePutIntoCouchBase, EXPIRATION_SECONDS);

        return results;
    }

    private String getMD5(MathQuestionProfile profile) {
        List<String> kpfs = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(profile.getKnowledgePointNews())) {
            kpfs.addAll(profile.getKnowledgePointNews().stream().map(KnowledgePointNew::getKpf_id).collect(Collectors.toList()));
        }
        if (kpfs.size() >= 2) Collections.sort(kpfs);

        List<String> tms = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(profile.getTestMethods())) {
            tms.addAll(profile.getTestMethods().stream().map(EmbedTestMethod::getId).collect(Collectors.toList()));
        }
        if (tms.size() >= 2) Collections.sort(tms);

        List<String> sms = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(profile.getSolutionMethods())) {
            sms.addAll(profile.getSolutionMethods().stream().map(EmbedSolutionMethodContent::getId).collect(Collectors.toList()));
        }
        if (sms.size() >= 2) Collections.sort(sms);

        String keyKpfs = String.join("", kpfs);
        String keyTms = String.join("", tms);
        String keySms = String.join("",sms);
        String key = keyKpfs + keyTms + keySms;

        String md5 = Md5.hexdigest(key);
        if (StringUtils.isEmpty(md5)) md5 = profile.getDoc_id();
        return md5;
    }

    public Map<String, List<NewQuestion>> recomSimilarQuestionsByQuestions(List<String> sourceQids, String catalogId, String type) {
        Map<String, List<NewQuestion>> results = Maps.newHashMap();

        NewKnowledgePointRef newKnowledgePointRef = null;
        List<TestMethodRef> testMethodRefs = null;
        List<SolutionMethodRef> solutionMethodRefs = null;
        SectionProgressId sectionProgressId = null;
        if (StringUtils.equals(type, SECTION)) {
            newKnowledgePointRef = newKnowledgePointLoaderClient.loadNewKnowledgePointRefByCatalogIds(Collections.singleton(catalogId)).get(catalogId);
            testMethodRefs = testMethodLoaderClient.loadTestMethodRefByBookCatalogIds(Collections.singleton(catalogId)).get(catalogId);
            solutionMethodRefs = solutionMethodRefLoaderClient.loadSolutionMethodRefByBookCatalogIds(Collections.singleton(catalogId)).get(catalogId);
            sectionProgressId = sectionProgressIdDao.getProgressIdBySection(catalogId);
        } else if (StringUtils.equals(type, UNIT)) {
            List<SectionProgressId> sectionProgressIds = sectionProgressIdDao.getSectionProgressIdsByUnit(catalogId);
            if (CollectionUtils.isNotEmpty(sectionProgressIds)) {
                sectionProgressId = Collections.max(sectionProgressIds, Comparator.comparing(SectionProgressId::getProgressId));
                List<String> sectionIds = sectionProgressIds.stream().map(SectionProgressId::getSectionId).collect(Collectors.toList());

                Map<String, NewKnowledgePointRef> newKnowledgePointRefMap = newKnowledgePointLoaderClient.loadNewKnowledgePointRefByCatalogIds(sectionIds);
                Map<String, List<TestMethodRef>> testMethodRefMap = testMethodLoaderClient.loadTestMethodRefByBookCatalogIds(sectionIds);
                Map<String, List<SolutionMethodRef>> solutionMethodRefMap = solutionMethodRefLoaderClient.loadSolutionMethodRefByBookCatalogIds(sectionIds);

                for (Map.Entry<String, List<TestMethodRef>> entry : testMethodRefMap.entrySet()) {
                    if (testMethodRefs == null) {
                        if (CollectionUtils.isNotEmpty(entry.getValue())) {
                            testMethodRefs = entry.getValue();
                        }
                    } else {
                        if (CollectionUtils.isNotEmpty(entry.getValue())) {
                            testMethodRefs.addAll(entry.getValue());
                        }
                    }
                }

                for(Map.Entry<String, List<SolutionMethodRef>> entry : solutionMethodRefMap.entrySet()) {
                    if (solutionMethodRefs == null) {
                        if (CollectionUtils.isNotEmpty(entry.getValue())){
                            solutionMethodRefs = entry.getValue();
                        }
                    } else {
                        if (CollectionUtils.isNotEmpty(entry.getValue())) {
                            solutionMethodRefs.addAll(entry.getValue());
                        }
                    }
                }

                for (Map.Entry<String, NewKnowledgePointRef> entry : newKnowledgePointRefMap.entrySet()) {
                    NewKnowledgePointRef newKnowledgePointRef1 = entry.getValue();
                    if (newKnowledgePointRef == null) {
                        if (newKnowledgePointRef1 != null) {
                            newKnowledgePointRef = newKnowledgePointRef1;
                        }
                    } else {
                        if (newKnowledgePointRef1 != null) {
                            newKnowledgePointRef.getKnowledgePoints().addAll(newKnowledgePointRef1.getKnowledgePoints());
                        }
                    }
                }
            }

        }
        MathQuestionProfile sectionProfile = getSectionProfile(newKnowledgePointRef, testMethodRefs,solutionMethodRefs);
        Map<String, List<NewQuestion>> tmpResults =  generateSimilarQuestions(sectionProfile,sectionProgressId,sourceQids);
        results.putAll(tmpResults);
        return results;
    }

    public Map<String, List<NewQuestion>> generateSimilarQuestions(MathQuestionProfile sectionProfile,SectionProgressId sectionProgressId,List<String> sourceQids) {
        Map<String, List<NewQuestion>> results = Maps.newHashMap();

        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestions(sourceQids);
        Map<String, MathQuestionProfile> questionsToBeProcessed = Maps.newHashMap();
        Map<String, List<String>> md5SourcesMap = Maps.newHashMap();

        newQuestionMap.entrySet().stream().forEach(e->{
            if(e.getValue() == null) {
                results.put(e.getKey(), Lists.newArrayList());
            } else {
                MathQuestionProfile profile = getProfile(e.getValue());
                questionsToBeProcessed.put(e.getKey(), profile);
                String md5 = getMD5(profile);
                if (!StringUtils.isEmpty(md5)){
                    if (md5SourcesMap.containsKey(md5)){
                        md5SourcesMap.get(md5).add(e.getKey());
                    } else {
                        List<String> sources = Lists.newArrayList();
                        sources.add(e.getKey());
                        md5SourcesMap.put(md5,sources);
                    }
                }
            }
        });

        List<String> couchKeys = Lists.newArrayList();
        md5SourcesMap.keySet().stream().forEach(e->couchKeys.add(MATH_SIMILAR_PREFIX + e));
        Map<String,List<MathQuestionProfile>> md5SimilarQuestionsMap = newHomeWorkCacheService.gets(couchKeys);
        Map<String,List<MathQuestionProfile>> mathQuestionProfilesMap = Maps.newHashMap();
        Map<String,List<MathQuestionProfile>> questionsToBePutIntoCouchBase = Maps.newHashMap();

        Set<String>existQuestions = Sets.newHashSet();
        Map<String, NewQuestion> allCandidateQuestions = Maps.newHashMap();
        for (Map.Entry<String,List<MathQuestionProfile>> entry:md5SimilarQuestionsMap.entrySet()) {
            List<MathQuestionProfile> mathQuestionProfiles = entry.getValue();
            if (CollectionUtils.isEmpty(mathQuestionProfiles)) {
                String md5 = StringUtils.split(entry.getKey(),"$")[1];
                if (!questionsToBePutIntoCouchBase.containsKey(MATH_SIMILAR_PREFIX + md5)) {
                    if (!md5SourcesMap.get(md5).isEmpty() ){
                        String sourceQid = md5SourcesMap.get(md5).get(0);
                        MathQuestionProfile mathQuestionProfile = questionsToBeProcessed.get(sourceQid);
                        mathQuestionProfiles = mathQuestionProfileDao.getSimQuestionProfileByQuestion(mathQuestionProfile);
                        if (CollectionUtils.isNotEmpty(mathQuestionProfiles)) {
                            questionsToBePutIntoCouchBase.put(MATH_SIMILAR_PREFIX + md5, mathQuestionProfiles);
                        }
                        mathQuestionProfilesMap.put(entry.getKey(), mathQuestionProfiles);
                    }
                }
            } else {
                mathQuestionProfilesMap.put(entry.getKey(),mathQuestionProfiles);
            }
            if (CollectionUtils.isNotEmpty(mathQuestionProfiles)) {
                existQuestions.addAll(mathQuestionProfiles.stream()
                        .filter(q->!isAboveLevel(sectionProgressId,q))
                        .map(MathQuestionProfile::getDoc_id)
                        .collect(Collectors.toList()));
            }
        }

        allCandidateQuestions.putAll(questionLoaderClient.loadQuestionByDocIds(existQuestions).stream().collect(Collectors.toMap(NewQuestion::getDocId, Function.identity())));
        for(Map.Entry<String,List<MathQuestionProfile>> pair : mathQuestionProfilesMap.entrySet()){
            String md5 = StringUtils.split(pair.getKey(),"$")[1];
            for (String sourceQid:md5SourcesMap.get(md5)) {
                MathQuestionProfile mathQuestionProfile = questionsToBeProcessed.get(sourceQid);
                List<MathQuestionProfile> mathQuestionProfiles = pair.getValue();

                List<String> qIds = Lists.newArrayList();
                List<NewQuestion> questions = Lists.newArrayList();

                Comparator<MathQuestionProfile> byProgressId = (q1,q2)->getProgressIdBasedOnSeriesId(sectionProgressId, q2).compareTo(getProgressIdBasedOnSeriesId(sectionProgressId, q1));
                Comparator<MathQuestionProfile> bySimilarity = (q1,q2)->isSimilar(mathQuestionProfile, q2, sectionProfile).get(0).compareTo(isSimilar(mathQuestionProfile, q1, sectionProfile).get(0));
                Comparator<MathQuestionProfile> sortCriteria = bySimilarity.thenComparing(byProgressId);

                if (CollectionUtils.isNotEmpty(mathQuestionProfiles)) {
                    qIds.addAll(mathQuestionProfiles.stream()
                            .filter(q -> !StringUtils.equals(q.getQuestion_id(), sourceQid))
                            .filter(q -> allCandidateQuestions.containsKey(q.getDoc_id()))
                            .sorted(sortCriteria)
                            .limit(SIMILAR_QUESTIONS_SIZE)
                            .map(MathQuestionProfile::getDoc_id)
                            .collect(Collectors.toList()));
                }

                if (CollectionUtils.isNotEmpty(qIds)) {
                    for (String qid: qIds) {
                        if (allCandidateQuestions.containsKey(qid)){
                            questions.add(allCandidateQuestions.get(qid));
                        }
                    }
                }
                Collections.shuffle(questions);
                results.put(sourceQid, questions);
            }
        }
        if(!questionsToBePutIntoCouchBase.isEmpty()) {
            newHomeWorkCacheService.puts(questionsToBePutIntoCouchBase, EXPIRATION_SECONDS);
        }
        return results;
    }

    @Getter
    @Setter
    private class QidDifficulty {
        String qid;
        Integer difficulty;
        Integer hasFigure;
        Integer contentLength;
        Long progressId;
    }

    @Getter
    @Setter
    private class QidInfo {
        Integer size;
        List<QidDifficulty> difficultyInfo;
    }

    private List<String> sortQuestions(List<QidInfo> qids,Comparator<QidDifficulty> sortCriteria) {
        int current = 0;
        List<String> returnQids = Lists.newArrayList();
        if (CollectionUtils.isEmpty(qids)) return returnQids;
        List<QidDifficulty> qidDifficulties = Lists.newArrayList();
        while (true) {
            boolean hasNew = false;
            qidDifficulties.clear();
            for (QidInfo qidInfo : qids) {
                if (qidInfo.getSize() > current) {
                    QidDifficulty qidDifficulty = qidInfo.getDifficultyInfo().get(current);
                    if (!returnQids.contains(qidDifficulty.getQid())) {
                        qidDifficulties.add(qidDifficulty);
                        hasNew = true;
                    }
                }
            }

            returnQids.addAll(qidDifficulties.stream().sorted(sortCriteria).map(QidDifficulty::getQid).collect(Collectors.toList()));
            if (!hasNew) break;
            current++;
        }
        return returnQids;
    }

    private List<Integer> getElementsFromContent(NewQuestion newQuestion){
        Integer hasFigure = 0;
        Integer contentLength = 0;

        NewQuestionsContent content = newQuestion.getContent();
        String contents = content.getContent();
        List<NewQuestionsSubContents> newQuestionsSubContentses = content.getSubContents();
        List<String> subContents = Lists.newArrayList();
        subContents.addAll(newQuestionsSubContentses.stream().map(NewQuestionsSubContents::getContent).collect(Collectors.toList()));
        contents += StringUtils.join(subContents,"");

        if(contents.contains("<img src")) hasFigure = 1;

        Matcher matcher = HTML_PATTERN.matcher(contents);
        // 去掉html标签
        String contentsNew = matcher.replaceAll("");

        contentLength = contentsNew.length();
        return Arrays.asList(hasFigure,contentLength);
    }

    private Long getProgressIdBasedOnSeriesId(SectionProgressId sectionProgressId, MathQuestionProfile mathQuestionProfile) {
        Long progressId = 0L;
        if (sectionProgressId == null || mathQuestionProfile == null)
            return progressId;

        String seriesId = sectionProgressId.getSeriesId();
        List<SeriesProgress> seriesProgresses = mathQuestionProfile.getSeriesProgresses();
        if (CollectionUtils.isEmpty(seriesProgresses))
            return progressId;

        for (SeriesProgress seriesProgress : seriesProgresses) {
            if (StringUtils.equals(seriesProgress.getSeries_id(), seriesId)) {
                progressId = seriesProgress.getProgress_id();
                break;
            }
        }
        return progressId;
    }

    private boolean isBiggerThanTwoTermsAgoProgressId(SectionProgressId sectionProgressId, Long questionProgressId){
        if (sectionProgressId == null) return false;
        Long progressId = sectionProgressId.getProgressId();
        Long twoTermsAgoProgressId = progressId - TWO_TERMS_PROGRESSID_DELTA;
        if (questionProgressId > twoTermsAgoProgressId) return true;
        else return false;
    }

    public Map<String,List<NewQuestion>> recomQuestionsBySectionIds(List<String> sectionIds) {
        Map<String,List<NewQuestion>> result = Maps.newHashMap();
        Map<String,QuestionClusterProfile> questionClusterProfileMap = questionClusterProfileDao.getClusterProfilesBySectionIds(sectionIds);
        if (questionClusterProfileMap.isEmpty()) {
            sectionIds.stream().forEach(e->result.put(e,Lists.newArrayList()));
            return result;
        }
        Map<String,Map<String, MathQuestionBox>> mathQuestionBoxMap = packageService.loadQuestionPackagesBySectionIds(sectionIds);
        Map<String,SectionProgressId> sectionProgressIdMap = sectionProgressIdDao.getProgressIdsBySections(sectionIds);

        Set<String>existQuestions = Sets.newHashSet();
        Map<String,NewQuestion> allCandidateQuestions = Maps.newHashMap();
        for (Map.Entry<String,QuestionClusterProfile> entry:questionClusterProfileMap.entrySet()) {
            existQuestions.addAll(entry.getValue().getCluster_qids().stream().flatMap(Collection::stream).collect(Collectors.toList()));
        }
        allCandidateQuestions.putAll(questionLoaderClient.loadQuestionByDocIds(existQuestions).stream().collect(Collectors.toMap(NewQuestion::getDocId, Function.identity())));
        Map<String, MathQuestionProfile> allCandidateMathQuestionProfilesMap = mathQuestionProfileDao.getMathQuestionProfilesByDocIds(existQuestions);

        Comparator<QidDifficulty> byProgressId = (q1,q2)->q2.getProgressId().compareTo(q1.getProgressId());
        Comparator<QidDifficulty> byHasFigure = (q1,q2)->q2.getHasFigure().compareTo(q1.getHasFigure());
        Comparator<QidDifficulty> byContentLength = (q1,q2)->q1.getContentLength().compareTo(q2.getContentLength());
        Comparator<QidDifficulty> sortCriteria = byProgressId.thenComparing(byHasFigure.thenComparing(byContentLength));

        for (Map.Entry<String,QuestionClusterProfile> entry:questionClusterProfileMap.entrySet()){
            String bkcId = entry.getKey();
//            if (!mathQuestionBoxMap.containsKey(bkcId)){
//                result.put(bkcId,Lists.newArrayList());
//            } else
                SectionProgressId sectionProgressId = null;
                if (sectionProgressIdMap.containsKey(bkcId)) sectionProgressId = sectionProgressIdMap.get(bkcId);

                Set<String> filteredQIds = Sets.newHashSet();
                if (mathQuestionBoxMap.containsKey(bkcId)&&!mathQuestionBoxMap.get(bkcId).isEmpty()) {
                    mathQuestionBoxMap.get(bkcId).entrySet().stream().forEach(e -> filteredQIds.addAll(e.getValue().getQuestionIds()));
                }

                List<List<String>> clusterQuestions = entry.getValue().getCluster_qids();
                List<QidInfo> qidInfos1 = Lists.newArrayList();//题目progrossId大于当前课时所在教材2个学期的progressId 的题目
                List<QidInfo> qidInfos2 = Lists.newArrayList();//题目progrossId小于当前课时所在教材2个学期的progressId 的题目

                for (List<String> clusterQuestion : clusterQuestions) {
                    List<String> qIds = Lists.newArrayList();
                    List<NewQuestion> questions = Lists.newArrayList();
                    qIds.addAll(clusterQuestion.stream().filter(q-> !filteredQIds.contains(q)).collect(Collectors.toList()));
                    if (CollectionUtils.isNotEmpty(qIds)) {
                        questions.addAll(allCandidateQuestions.values().stream().filter(q->qIds.contains(q.getDocId())).collect(Collectors.toList()));
                    }

                    if (CollectionUtils.isNotEmpty(questions)) {
                        QidInfo qidInfo1 = new QidInfo();//题目progrossId大于当前课时所在教材2个学期的progressId 的题目
                        QidInfo qidInfo2 = new QidInfo();//题目progrossId小于当前课时所在教材2个学期的progressId 的题目
                        List<QidDifficulty> difficultyInfo1 = Lists.newArrayList(); //题目progrossId大于当前课时所在教材2个学期的progressId 的题目
                        List<QidDifficulty> difficultyInfo2 = Lists.newArrayList(); //题目progrossId小于当前课时所在教材2个学期的progressId 的题目
                        for (NewQuestion newQuestion : questions) {
                            QidDifficulty qidDifficulty = new QidDifficulty();
                            qidDifficulty.setQid(newQuestion.getDocId());
                            qidDifficulty.setDifficulty(newQuestion.getDifficultyInt());
                            List<Integer> elements = getElementsFromContent(newQuestion);
                            qidDifficulty.setHasFigure(elements.get(0));
                            qidDifficulty.setContentLength(elements.get(1));
                            MathQuestionProfile mathQuestionProfile = null;
                            if (allCandidateMathQuestionProfilesMap.containsKey(newQuestion.getDocId()))
                                mathQuestionProfile = allCandidateMathQuestionProfilesMap.get(newQuestion.getDocId());
                            qidDifficulty.setProgressId(getProgressIdBasedOnSeriesId(sectionProgressId, mathQuestionProfile));
                            if (isBiggerThanTwoTermsAgoProgressId(sectionProgressId, qidDifficulty.getProgressId())) difficultyInfo1.add(qidDifficulty);
                            else difficultyInfo2.add(qidDifficulty);
                        }

                        List<QidDifficulty> qidDifficultyList1 = Lists.newArrayList();//题目progrossId大于当前课时所在教材2个学期的progressId 的题目
                        qidDifficultyList1.addAll(difficultyInfo1.stream().sorted(sortCriteria).collect(Collectors.toList()));
                        qidInfo1.setDifficultyInfo(qidDifficultyList1);
                        qidInfo1.setSize(qidDifficultyList1.size());
                        if (qidInfo1.getSize() > 0) qidInfos1.add(qidInfo1);

                        List<QidDifficulty> qidDifficultyList2 = Lists.newArrayList();//题目progrossId小于当前课时所在教材2个学期的progressId 的题目
                        qidDifficultyList2.addAll(difficultyInfo2.stream().sorted(sortCriteria).collect(Collectors.toList()));
                        qidInfo2.setDifficultyInfo(qidDifficultyList2);
                        qidInfo2.setSize(qidDifficultyList2.size());
                        if (qidInfo2.getSize() > 0) qidInfos2.add(qidInfo2);
                    }
                }

                List<String> tmpQIds1 = sortQuestions(qidInfos1,sortCriteria);
                List<String> tmpQIds2 = sortQuestions(qidInfos2,sortCriteria);
                List<NewQuestion> tmpQuestions = Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(tmpQIds1)) {
                    for (String qid : tmpQIds1) {
                        if (allCandidateQuestions.containsKey(qid))
                            tmpQuestions.add(allCandidateQuestions.get(qid));
                    }
                }
                if (CollectionUtils.isNotEmpty(tmpQIds2)) {
                    for (String qid:tmpQIds2) {
                        if (allCandidateQuestions.containsKey(qid))
                            tmpQuestions.add(allCandidateQuestions.get(qid));
                    }
                }
                result.put(bkcId,tmpQuestions);

        }

        return result;
    }

    private boolean isAboveLevel(SectionProgressId sectionProgressId, MathQuestionProfile mathQuestionProfile) {
        if (sectionProgressId == null)
            return false;
        String seriesId = sectionProgressId.getSeriesId();
        long progressId = sectionProgressId.getProgressId();
        List<SeriesProgress> seriesProgresses = mathQuestionProfile.getSeriesProgresses();
        if (CollectionUtils.isEmpty(seriesProgresses))
            return true;
        for (SeriesProgress seriesProgress : seriesProgresses) {
            if (StringUtils.equals(seriesProgress.getSeries_id(), seriesId)) {
                return seriesProgress.getProgress_id() > progressId;
            }
        }
        return true;
    }

    private List<NewQuestion> addFirstNElements(List<NewQuestion> questions, Integer count) {
        return (questions.stream().limit(count).collect(Collectors.toList()));
    }

    private Set<String> calIntersection(Set<String> set1, Set<String> set2){
        Set<String> inter = Sets.newHashSet();
        inter.addAll(set1);
        inter.retainAll(set2);
        return inter;
    }

    private Set<String> calUnion(Set<String> set1, Set<String> set2){
        Set<String> union = Sets.newHashSet();
        union.addAll(set1);
        union.addAll(set2);
        return union;
    }
    private List<Double> isSimilar(MathQuestionProfile mathQuestionProfile, MathQuestionProfile newQuestionProfile, MathQuestionProfile sectionProfile) {
        if ((mathQuestionProfile == null) || (newQuestionProfile == null))
            return Arrays.asList(0.0,0.0);
        Double similarValue = 0.0;
        Set<String> questionKpfs = Sets.newHashSet();
        Set<String> newQuestionKpfs = Sets.newHashSet();
        Set<String> sectionKpfs = Sets.newHashSet();
        Set<String> questionMethods = Sets.newHashSet();
        Set<String> newQuestionMethods = Sets.newHashSet();
        Set<String> sectionMethods = Sets.newHashSet();
        Set<String> questionSolutions = Sets.newHashSet();
        Set<String> newQuestionSolutions = Sets.newHashSet();
        Set<String> sectionSolutions = Sets.newHashSet();

        if (CollectionUtils.isNotEmpty(mathQuestionProfile.getKnowledgePointNews()))
            questionKpfs.addAll(mathQuestionProfile.getKnowledgePointNews().stream().map(KnowledgePointNew::getKpf_id).collect(Collectors.toList()));
        if (CollectionUtils.isNotEmpty(newQuestionProfile.getKnowledgePointNews()))
            newQuestionKpfs.addAll(newQuestionProfile.getKnowledgePointNews().stream().map(KnowledgePointNew::getKpf_id).collect(Collectors.toList()));
        if (CollectionUtils.isNotEmpty(mathQuestionProfile.getTestMethods()))
            questionMethods.addAll(mathQuestionProfile.getTestMethods().stream().map(EmbedTestMethod::getId).collect(Collectors.toList()));
        if (CollectionUtils.isNotEmpty(newQuestionProfile.getTestMethods()))
            newQuestionMethods.addAll(newQuestionProfile.getTestMethods().stream().map(EmbedTestMethod::getId).collect(Collectors.toList()));
        if (CollectionUtils.isNotEmpty(mathQuestionProfile.getSolutionMethods()))
            questionSolutions.addAll(mathQuestionProfile.getSolutionMethods().stream().map(EmbedSolutionMethodContent::getId).collect(Collectors.toList()));
        if (CollectionUtils.isNotEmpty(newQuestionProfile.getSolutionMethods()))
            newQuestionSolutions.addAll(newQuestionProfile.getSolutionMethods().stream().map(EmbedSolutionMethodContent::getId).collect(Collectors.toList()));

        if (sectionProfile != null) {
            if (CollectionUtils.isNotEmpty(sectionProfile.getKnowledgePointNews()))
                sectionKpfs.addAll(sectionProfile.getKnowledgePointNews().stream().map(KnowledgePointNew::getKpf_id).collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(sectionProfile.getTestMethods()))
                sectionMethods.addAll(sectionProfile.getTestMethods().stream().map(EmbedTestMethod::getId).collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(sectionProfile.getSolutionMethods()))
                sectionSolutions.addAll(sectionProfile.getSolutionMethods().stream().map(EmbedSolutionMethodContent::getId).collect(Collectors.toList()));
        }

        Set<String> kpfsIntersection = calIntersection(questionKpfs,newQuestionKpfs);
        Set<String> methodsIntersection = calIntersection(questionMethods,newQuestionMethods);
        Set<String> solutionsIntersection = calIntersection(questionSolutions,newQuestionSolutions);
        Set<String> kpfsUnion = calUnion(questionKpfs,newQuestionKpfs);
        Set<String> methodsUnion = calUnion(questionMethods,newQuestionMethods);
        Set<String> solutionsUnion = calUnion(questionSolutions,newQuestionSolutions);
        Set<String> sectionKpsInterSection = calIntersection(newQuestionKpfs,sectionKpfs);
        Set<String> sectionMethodsInterSection = calIntersection(newQuestionMethods,sectionMethods);
        Set<String> sectionSolutionsInterSection = calIntersection(newQuestionSolutions,sectionSolutions);

        Double kfsSimilar = (kpfsIntersection.size()+sectionKpsInterSection.size()+1.0)/(kpfsUnion.size()+1.0);
        Double methodsSimilar = (methodsIntersection.size()+sectionMethodsInterSection.size()+1.0)/(methodsUnion.size()+1.0);
        Double solutionsSimilar = (solutionsIntersection.size()+sectionSolutionsInterSection.size()+1.0)/(solutionsUnion.size()+1.0);
        similarValue = kfsSimilar * methodsSimilar * solutionsSimilar;

        return Arrays.asList(similarValue,kfsSimilar);
    }

    public List<NewQuestion> recomSimilarQuestionsByWrongQuestion(String sourceQid, String correctQid, String sectionId) {
        List<NewQuestion> questions = Lists.newArrayList();
        NewQuestion newQuestion = questionLoaderClient.loadQuestionIncludeDisabled(sourceQid);
        if (newQuestion == null) return questions;

        MathQuestionProfile mathQuestionProfile = getProfile(newQuestion);
        String md5 = getMD5(mathQuestionProfile);
        List<MathQuestionProfile> mathQuestionProfiles = newHomeWorkCacheService.get(MATH_SIMILAR_PREFIX+md5);

        if (CollectionUtils.isEmpty(mathQuestionProfiles)){
            mathQuestionProfiles = mathQuestionProfileDao.getSimQuestionProfileByQuestion(mathQuestionProfile);
            if (CollectionUtils.isNotEmpty(mathQuestionProfiles))
                newHomeWorkCacheService.put(MATH_SIMILAR_PREFIX+md5,mathQuestionProfiles,EXPIRATION_SECONDS);
        }
        List<String> qIds = Lists.newArrayList();
        // 数据没准备好，不超纲暂时不用
        SectionProgressId sectionProgressId = sectionProgressIdDao.getProgressIdBySection(sectionId);
        NewKnowledgePointRef newKnowledgePointRef = newKnowledgePointLoaderClient.loadNewKnowledgePointRefByCatalogIds(Collections.singleton(sectionId)).get(sectionId);
        List<TestMethodRef> testMethodRefs = testMethodLoaderClient.loadTestMethodRefByBookCatalogIds(Collections.singleton(sectionId)).get(sectionId);
        List<SolutionMethodRef> solutionMethodRefs = solutionMethodRefLoaderClient.loadSolutionMethodRefByBookCatalogIds(Collections.singleton(sectionId)).get(sectionId);
        MathQuestionProfile sectionProfile = getSectionProfile(newKnowledgePointRef,testMethodRefs,solutionMethodRefs);

        Comparator<MathQuestionProfile> byProgressId = (q1,q2)->getProgressIdBasedOnSeriesId(sectionProgressId, q2).compareTo(getProgressIdBasedOnSeriesId(sectionProgressId, q1));
        Comparator<MathQuestionProfile> bySimilarity = (q1,q2)->isSimilar(mathQuestionProfile, q2, sectionProfile).get(0).compareTo(isSimilar(mathQuestionProfile, q1, sectionProfile).get(0));
        Comparator<MathQuestionProfile> sortCriteria = bySimilarity.thenComparing(byProgressId);

        if (CollectionUtils.isNotEmpty(mathQuestionProfiles)) {
            qIds.addAll(mathQuestionProfiles.stream()
                    .filter(q -> !StringUtils.equals(q.getQuestion_id(), sourceQid))
                    .filter(q -> !StringUtils.equals(q.getQuestion_id(), correctQid))
                    .filter(q->!isAboveLevel(sectionProgressId,q))
                    .sorted(sortCriteria)
                    .map(MathQuestionProfile::getDoc_id)
                    .collect(Collectors.toList()));
        }

        if (CollectionUtils.isNotEmpty(qIds)) {
            questions.addAll(questionLoaderClient.loadQuestionByDocIds(qIds));
        }
        questions = addFirstNElements(questions, SIMILAR_QUESTIONS_SIZE);
        Collections.shuffle(questions);
        return questions;
    }

    public  MathQuestionProfile getProfile(NewQuestion newQuestion) {
        MathQuestionProfile mathQuestionProfile = new MathQuestionProfile();
        List<KnowledgePointNew> knowledgePointNews = Lists.newArrayList();
        List<EmbedTestMethod> testMethods = Lists.newArrayList();
        List<EmbedSolutionMethodContent> solutionMethods = Lists.newArrayList();
        Set<String> existingKpfs = Sets.newHashSet();
        Set<String> existingTestMethods = Sets.newHashSet();
        Set<String> existingSolutionMethods = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(newQuestion.getKnowledgePointsNew())) {
            for (NewQuestionKnowledgePoint newQuestionKnowledgePoint : newQuestion.getKnowledgePointsNew()) {
                if (newQuestionKnowledgePoint.getMain() == 0) continue;
                KnowledgePointNew knowledgePointNew = new KnowledgePointNew();
                knowledgePointNew.setId(newQuestionKnowledgePoint.getId());
                knowledgePointNew.setFeature_ids(newQuestionKnowledgePoint.getFeatureIds());
                String kpf = newQuestionKnowledgePoint.getId();
                if (CollectionUtils.isNotEmpty(newQuestionKnowledgePoint.getFeatureIds())) {
                    List<String> featureIds = newQuestionKnowledgePoint.getFeatureIds();
                    Collections.sort(featureIds);
                    kpf += ":" + StringUtils.join(featureIds, ",");
                }
                knowledgePointNew.setKpf_id(kpf);
                if (!existingKpfs.contains(kpf)) {
                    knowledgePointNews.add(knowledgePointNew);
                    existingKpfs.add(kpf);
                }
            }
        }
        NewQuestionsContent newQuestionsContent = newQuestion.getContent();
        if (newQuestionsContent != null) {
            List<NewQuestionsSubContents> subContentses = newQuestionsContent.getSubContents();
            if (CollectionUtils.isNotEmpty(subContentses)) {
                for (NewQuestionsSubContents content : subContentses) {
                    List<NewQuestionAnswer> answers = content.getAnswers();
                    if (CollectionUtils.isNotEmpty(answers)) {
                        for (NewQuestionAnswer answer : answers) {
                            if (CollectionUtils.isNotEmpty(answer.getKnowledgePointsNew())) {
                                for (NewQuestionKnowledgePoint newQuestionKnowledgePoint : answer.getKnowledgePointsNew()) {
                                    if (newQuestionKnowledgePoint.getMain() == 0) continue;
                                    KnowledgePointNew knowledgePointNew = new KnowledgePointNew();
                                    knowledgePointNew.setId(newQuestionKnowledgePoint.getId());
                                    knowledgePointNew.setFeature_ids(newQuestionKnowledgePoint.getFeatureIds());
                                    String kpf = newQuestionKnowledgePoint.getId();
                                    if (CollectionUtils.isNotEmpty(newQuestionKnowledgePoint.getFeatureIds())) {
                                        List<String> featureIds = newQuestionKnowledgePoint.getFeatureIds();
                                        Collections.sort(featureIds);
                                        kpf += ":" + StringUtils.join(featureIds, ",");
                                    }
                                    knowledgePointNew.setKpf_id(kpf);
                                    if (!existingKpfs.contains(kpf)) {
                                        knowledgePointNews.add(knowledgePointNew);
                                        existingKpfs.add(kpf);
                                    }
                                }
                            }
                            if (CollectionUtils.isNotEmpty(answer.getTestMethods())) {
                                for (EmbedTestMethod testMethod : answer.getTestMethods()) {
                                    if (testMethod.getMain() == 0) continue;
                                    if (!existingTestMethods.contains(testMethod.getId())) {
                                        testMethods.add(testMethod);
                                        existingTestMethods.add(testMethod.getId());
                                    }
                                }
                            }
                            if (CollectionUtils.isNotEmpty(answer.getSolutionMethods())) {
                                for (EmbedSolutionMethodContent solutionMethod : answer.getSolutionMethods()) {
                                    if (solutionMethod.getMain() == 0) continue;
                                    if (!existingSolutionMethods.contains(solutionMethod.getId())) {
                                        solutionMethods.add(solutionMethod);
                                        existingSolutionMethods.add(solutionMethod.getId());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(newQuestion.getTestMethods())) {
            for (EmbedTestMethod testMethod : newQuestion.getTestMethods()) {
                if (testMethod.getMain() == 0) continue;
                if (!existingTestMethods.contains(testMethod.getId())) {
                    testMethods.add(testMethod);
                    existingTestMethods.add(testMethod.getId());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(newQuestion.getSolutionMethods())){
            for (EmbedSolutionMethodContent solutionMethod : newQuestion.getSolutionMethods()){
                if (solutionMethod.getMain() == 0) continue;
                if (!existingSolutionMethods.contains(solutionMethod.getId())) {
                    solutionMethods.add(solutionMethod);
                    existingSolutionMethods.add(solutionMethod.getId());
                }
            }
        }
        mathQuestionProfile.setKnowledgePointNews(knowledgePointNews);
        mathQuestionProfile.setTestMethods(testMethods);
        mathQuestionProfile.setSolutionMethods(solutionMethods);
        mathQuestionProfile.setQuestion_id(newQuestion.getId());
        mathQuestionProfile.setDoc_id(newQuestion.getDocId());
        return mathQuestionProfile;
    }

    public  MathQuestionProfile getSectionProfile(NewKnowledgePointRef newKnowledgePointRef,List<TestMethodRef> testMethodRefs,List<SolutionMethodRef> solutionMethodRefs) {
        MathQuestionProfile mathQuestionProfile = new MathQuestionProfile();

        List<KnowledgePointNew> knowledgePointNews = Lists.newArrayList();
        List<EmbedTestMethod> testMethods = Lists.newArrayList();
        List<EmbedSolutionMethodContent> solutionMethods = Lists.newArrayList();
        Set<String> existingKpfs = Sets.newHashSet();
        Set<String> existingTestMethods = Sets.newHashSet();
        Set<String> existingSolutionMethods = Sets.newHashSet();
        if (newKnowledgePointRef != null) {
            List<BaseKnowledgePointRef> baseKnowledgePointRefs = newKnowledgePointRef.getKnowledgePoints();
            for (BaseKnowledgePointRef baseKnowledgePointRef : baseKnowledgePointRefs) {
                if (baseKnowledgePointRef.getKeyPoint()) {
                    KnowledgePointNew knowledgePointNew = new KnowledgePointNew();
                    knowledgePointNew.setId(baseKnowledgePointRef.getId());
                    knowledgePointNew.setFeature_ids(baseKnowledgePointRef.getFeatureIds());

                    String kpf = baseKnowledgePointRef.getId();

                    if (CollectionUtils.isNotEmpty(baseKnowledgePointRef.getFeatureIds())) {
                        List<String> featureIds = baseKnowledgePointRef.getFeatureIds();
                        Collections.sort(featureIds);
                        kpf += ":" + StringUtils.join(featureIds, ",");
                    }
                    knowledgePointNew.setKpf_id(kpf);
                    if (!existingKpfs.contains(kpf)) {
                        knowledgePointNews.add(knowledgePointNew);
                        existingKpfs.add(kpf);
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(testMethodRefs)) {
            List<EmbedTestMethodRef> embedTestMethodRefs = testMethodRefs.stream().map(TestMethodRef::getTestMethods).flatMap(Collection::stream).collect(Collectors.toList());
            for (EmbedTestMethodRef embedTestMethodRef : embedTestMethodRefs) {
                if (embedTestMethodRef.getKeyTestMethod() && !existingTestMethods.contains(embedTestMethodRef.getId())) {
                    EmbedTestMethod embedTestMethod = new EmbedTestMethod();
                    embedTestMethod.setId(embedTestMethodRef.getId());
                    embedTestMethod.setMain(1);
                    testMethods.add(embedTestMethod);
                    existingTestMethods.add(embedTestMethodRef.getId());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(solutionMethodRefs)) {
            List<EmbedSolutionMethodRef> embedSolutionMethodRefs = solutionMethodRefs.stream().map(SolutionMethodRef::getSolutionMethods).flatMap(Collection::stream).collect(Collectors.toList());
            for (EmbedSolutionMethodRef embedSolutionMethodRef : embedSolutionMethodRefs) {
                if (embedSolutionMethodRef.getKeySolutionMethod() && !existingSolutionMethods.contains(embedSolutionMethodRef.getId())) {
                    EmbedSolutionMethodContent solutionMethod = new EmbedSolutionMethodContent();
                    solutionMethod.setId(embedSolutionMethodRef.getId());
                    solutionMethod.setMain(1);
                    solutionMethods.add(solutionMethod);
                    existingSolutionMethods.add(embedSolutionMethodRef.getId());
                }
            }
        }

        mathQuestionProfile.setKnowledgePointNews(knowledgePointNews);
        mathQuestionProfile.setTestMethods(testMethods);
        mathQuestionProfile.setSolutionMethods(solutionMethods);
        return mathQuestionProfile;
    }

    /**
     * 查看题目是否覆盖知识点
     *
     * @param question
     * @param knowledgePointRef
     * @return
     */
    public boolean canRecom(NewQuestion question, NewKnowledgePointRef knowledgePointRef) {
        return true;
    }

}
