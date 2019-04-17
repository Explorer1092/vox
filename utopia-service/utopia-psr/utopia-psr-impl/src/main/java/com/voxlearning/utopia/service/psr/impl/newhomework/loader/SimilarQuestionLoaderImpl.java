package com.voxlearning.utopia.service.psr.impl.newhomework.loader;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.psr.entity.newhomework.MathQuestionBox;
import com.voxlearning.utopia.service.psr.entity.newhomework.SectionProgressId;
import com.voxlearning.utopia.service.psr.impl.dao.newhomework.SectionProgressIdDao;
import com.voxlearning.utopia.service.psr.impl.newhomework.service.NewHomeWorkCacheService;
import com.voxlearning.utopia.service.psr.impl.newhomework.service.QuestionPackageService;
import com.voxlearning.utopia.service.psr.impl.newhomework.service.SimilarQuestionService;
import com.voxlearning.utopia.service.psr.newhomework.loader.SimilarQuestionLoader;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = SimilarQuestionLoader.class)
public class SimilarQuestionLoaderImpl implements SimilarQuestionLoader {

    @Inject
    private SimilarQuestionService similarQuestionService;
    @Inject
    private QuestionPackageService packageService;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private NewHomeWorkCacheService newHomeWorkCacheService;
    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private SectionProgressIdDao sectionProgressIdDao;

    private static final String SECTION = "SECTION";
    private static final String UNIT = "UNIT";
    private static final Integer SIMILAR_QUESTIONS_SIZE = 5;
    private static final String MATH_SECTION_EXPOSURE_PREFIX = "MSE#";
    private static final String EXPOSURE_NO_THRESHOLD = "ENT#";
    private static final String UNIT_PACKAGE_QUESTIONS_PREFIX = "UPQP#";
    private static final Long EXPOSURE_NO_THRESHOLD_DEFAULT = 200L;
    private static final Integer CACHE_SIZE = 10000;
    private static final Integer INITIAL_CAPACITY = (int) (CACHE_SIZE * 0.7);
    private static Cache<String, List<NewQuestion>> cacheQuestions = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).initialCapacity(INITIAL_CAPACITY).expireAfterWrite(60, TimeUnit.MINUTES).build();
    private static Cache<String, List<MathQuestionBox>> cacheQuestionPackagesOfSections = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).initialCapacity(INITIAL_CAPACITY).expireAfterWrite(60, TimeUnit.MINUTES).build();
    private static Cache<String, Long> cacheExposureNoThreshold = CacheBuilder.newBuilder().maximumSize(1).initialCapacity(1).expireAfterWrite(1, TimeUnit.DAYS).build();
    private static Cache<String, List<String>> cacheSectionQuestionsForReplacement = CacheBuilder.newBuilder().maximumSize(1000).initialCapacity(600).expireAfterAccess(1, TimeUnit.DAYS).build();

    private List<NewQuestion> insertDoc(String docId, List<NewQuestion> questions) {
        List<NewQuestion> resultNewQuestions = Lists.newArrayList();
        resultNewQuestions.addAll(questions.stream().filter(q -> !StringUtils.equals(q.getDocId(), docId)).collect(Collectors.toList()));
        List<NewQuestion> questions1 = cacheQuestions.getIfPresent(docId);
        if (CollectionUtils.isNotEmpty(questions1)) {
            resultNewQuestions.add(0, questions1.get(0));
        } else {
            NewQuestion newQuestion = questionLoaderClient.loadQuestionByDocId(docId);
            if (newQuestion != null) {
                cacheQuestions.put(docId, Lists.newArrayList(newQuestion));
                resultNewQuestions.add(0, newQuestion);
            }
        }
        return resultNewQuestions;
    }

    @Override
    public Map<String, List<NewQuestion>> loadGoalSimilarQuestions(Collection<String> sourceIds, String unitId, Teacher teacher) {
        // goal专用推类题接口，sourceId就是qid
        // TODO: 2016/10/11 @永磊
        return loadSimilarQuestions(sourceIds, unitId, teacher, UNIT);
    }

    @Override
    public Map<String, List<NewQuestion>> loadSimilarQuestions(Collection<String> sourceIds, Teacher teacher) {
        if (CollectionUtils.isEmpty(sourceIds)) return Collections.emptyMap();

        Map<String, List<String>> sectionSourceIdsMap = Maps.newHashMap();
        sourceIds.stream().forEach(s -> {
            String[] elems = StringUtils.split(s, "#");
            String sectionId = "";
            if (elems.length == 2) {
                sectionId = elems[0];
            }
            if (sectionSourceIdsMap.containsKey(sectionId)) sectionSourceIdsMap.get(sectionId).add(s);
            else sectionSourceIdsMap.put(sectionId, Lists.newArrayList(s));
        });

        Map<String, List<NewQuestion>> results = Maps.newHashMap();
        sectionSourceIdsMap.entrySet().stream().forEach(e -> {
            Map<String, List<NewQuestion>> tmpResults = loadSimilarQuestions(e.getValue(), e.getKey(), teacher, SECTION);
            if (!tmpResults.isEmpty()) results.putAll(tmpResults);
        });
        return results;
    }

    public Map<String, List<NewQuestion>> loadSimilarQuestions(Collection<String> sourceIds, String sectionId, Teacher teacher, String type) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyMap();
        }
        Map<String, List<NewQuestion>> results = Maps.newHashMap();
        List<String> qidsToBeProcessed = Lists.newArrayList();
        Map<String, String> qidSourceIdMap = Maps.newHashMap();
        for (String sourceId : sourceIds) {
            String[] elems = StringUtils.split(sourceId, "#");
            String questionId;
            if (elems.length == 1) {
                questionId = elems[0];
            } else {
                questionId = elems[1];
            }
            qidSourceIdMap.put(questionId, sourceId);
            String key = questionId + sectionId;
            List<NewQuestion> questions = cacheQuestions.getIfPresent(key);
            if (questions != null) {
                if (StringUtils.contains(questionId, "Q_10201150192746")) {
                    Long teacherId = teacher.getId();
                    if (teacherId != null) {
                        String docId = null;
                        if (1 == teacherId % 2) docId = "Q_10205390338217";
                        else docId = "Q_10205390304583";
                        if (StringUtils.isNotEmpty(docId)) questions = insertDoc(docId, questions);
                        if (questions.size() > SIMILAR_QUESTIONS_SIZE)
                            questions = questions.subList(0, SIMILAR_QUESTIONS_SIZE);
                    }
                }
                results.put(sourceId, questions);
            } else {
                qidsToBeProcessed.add(questionId);
            }
        }
        if (CollectionUtils.isNotEmpty(qidsToBeProcessed)) {
            Map<String, List<NewQuestion>> questions = similarQuestionService.recomSimilarQuestionsByQuestions(qidsToBeProcessed, sectionId, type);
            if (!questions.isEmpty()) {
                for (Map.Entry<String, List<NewQuestion>> e : questions.entrySet()) {
                    String questionId = e.getKey();
                    List<NewQuestion> newQuestions = e.getValue();
                    String key = questionId + sectionId;
                    List<NewQuestion> resultNewQuestions = Lists.newArrayList();
                    resultNewQuestions.addAll(newQuestions);
                    if (StringUtils.contains(questionId, "Q_10201150192746")) {
                        Long teacherId = teacher.getId();
                        if (teacherId != null) {
                            String docId = null;
                            if (1 == teacherId % 2) docId = "Q_10205390338217";
                            else docId = "Q_10205390304583";
                            if (StringUtils.isNotEmpty(docId)) resultNewQuestions = insertDoc(docId, newQuestions);
                            if (resultNewQuestions.size() > SIMILAR_QUESTIONS_SIZE)
                                resultNewQuestions = resultNewQuestions.subList(0, SIMILAR_QUESTIONS_SIZE);
                        }
                    }

                    if (newQuestions != null) cacheQuestions.put(key, newQuestions);

                    if (CollectionUtils.isNotEmpty(resultNewQuestions)) {
                        String sourceId = qidSourceIdMap.get(questionId);
                        results.put(sourceId, resultNewQuestions);
                    }
                }
            }
        }

        return results;
    }

    @Override
    public Map<String, List<NewQuestion>> loadMathSimilarQuestions(Collection<String> catalogIds) {
        Map<String, List<NewQuestion>> result = Maps.newHashMap();
        List<String> bkcIdsToBeProcessed = Lists.newArrayList();
        for (String catalogId : catalogIds) {
            List<NewQuestion> catalogQuestions = cacheQuestions.getIfPresent(catalogId);
            if (CollectionUtils.isNotEmpty(catalogQuestions)) {
                result.put(catalogId, catalogQuestions);
            } else {
                bkcIdsToBeProcessed.add(catalogId);
            }
        }
        if (CollectionUtils.isNotEmpty(bkcIdsToBeProcessed)) {
            Map<String, List<NewQuestion>> bkcIdQuestionsMap = similarQuestionService.recomQuestionsBySectionIds(bkcIdsToBeProcessed);
            if (!bkcIdQuestionsMap.isEmpty()) {
                for (Map.Entry<String, List<NewQuestion>> entry : bkcIdQuestionsMap.entrySet()) {
                    String catalogId = entry.getKey();
                    List<NewQuestion> catalogQuestions = entry.getValue();
                    if (CollectionUtils.isNotEmpty(catalogQuestions)) {
                        result.put(catalogId, catalogQuestions);
                        cacheQuestions.put(catalogId, catalogQuestions);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<NewQuestion> loadSimilarQuestions(String sourceQid, String correctQid, Integer correctRate, String sectionId) {
        String key = sourceQid + correctQid + sectionId;
        List<NewQuestion> similarQuestions = cacheQuestions.getIfPresent(key);
        if (CollectionUtils.isNotEmpty(similarQuestions)) {
            return similarQuestions;
        } else {
            similarQuestions = similarQuestionService.recomSimilarQuestionsByWrongQuestion(sourceQid, correctQid, sectionId);
            cacheQuestions.put(key, similarQuestions);
            return similarQuestions;
        }
    }


    private MathQuestionBox insertDoc(List<String> docIds, MathQuestionBox mathQuestionBox) {
        String docId = null;
        while (CollectionUtils.isNotEmpty(docIds)) {
            Collections.shuffle(docIds);
            docId = docIds.get(0);
            NewQuestion newQuestion = questionLoaderClient.loadQuestionByDocId(docId);
            if (newQuestion == null)
                docIds.remove(0);
            else
                break;
        }
        if (StringUtils.isNotEmpty(docId)) {
            List<String> questions = mathQuestionBox.getQuestionIds();
            Integer packageSize = questions.size() + 1;
            Integer selectedPos = RandomUtils.nextInt(packageSize / 3, 2 * packageSize / 3);
            if (selectedPos == 0) {
                questions.add(selectedPos, docId);
            } else {
                questions.add(selectedPos - 1, docId);
            }
            mathQuestionBox.setQuestionIds(questions);
        }

        return mathQuestionBox;
    }

    @Override
    public Map<String, List<MathQuestionBox>> loadQuestionPackagesOfSections(Collection<String> catalogIds) {
        if (CollectionUtils.isEmpty(catalogIds)) {
            return Collections.emptyMap();
        }
        Map<String, List<MathQuestionBox>> result = Maps.newHashMap();
        Map<String, NewBookCatalog> bookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(catalogIds);
        List<String> bkcIdsToBeProcessed = Lists.newArrayList();
        for (String catalogId : catalogIds) {
            List<MathQuestionBox> mathQuestionBoxList = cacheQuestionPackagesOfSections.getIfPresent(catalogId);
            if (CollectionUtils.isNotEmpty(mathQuestionBoxList)) {
                result.put(catalogId, mathQuestionBoxList);
            } else {
                bkcIdsToBeProcessed.add(catalogId);
            }
        }
        if (CollectionUtils.isNotEmpty(bkcIdsToBeProcessed)) {
            Map<String, Map<String, MathQuestionBox>> mathQuestionBoxMap = packageService.loadQuestionPackagesBySectionIds(bkcIdsToBeProcessed);
            if (mathQuestionBoxMap.isEmpty()) return Collections.emptyMap();

            Long exposureNoThreshold = cacheExposureNoThreshold.getIfPresent(EXPOSURE_NO_THRESHOLD);
            if (exposureNoThreshold == null) {
                exposureNoThreshold = newHomeWorkCacheService.getValueFromKey(EXPOSURE_NO_THRESHOLD);
                if (exposureNoThreshold == 0)
                    exposureNoThreshold = EXPOSURE_NO_THRESHOLD_DEFAULT;
                cacheExposureNoThreshold.put(EXPOSURE_NO_THRESHOLD, exposureNoThreshold);
            }

            List<String> bkcIdsToGetCandidates = Lists.newArrayList();
            bkcIdsToGetCandidates.addAll(bkcIdsToBeProcessed.stream().filter(e -> CollectionUtils.isEmpty(cacheSectionQuestionsForReplacement.getIfPresent(MATH_SECTION_EXPOSURE_PREFIX + e))).collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(bkcIdsToGetCandidates)) {
                Map<String, List<String>> sectionsExposureQuestions = newHomeWorkCacheService.getSectionsExposureQuestions(bkcIdsToGetCandidates);
                if (!sectionsExposureQuestions.isEmpty()) {
                    sectionsExposureQuestions.entrySet().stream().filter(e -> CollectionUtils.isNotEmpty(e.getValue())).forEach(e -> cacheSectionQuestionsForReplacement.put(MATH_SECTION_EXPOSURE_PREFIX + e.getKey(), e.getValue()));
                }
            }

            for (Map.Entry<String, Map<String, MathQuestionBox>> e : mathQuestionBoxMap.entrySet()) {
                List<MathQuestionBox> mathQuestionBoxes = Lists.newArrayList();
                String catalogId = e.getKey();
                Map<String, MathQuestionBox> boxMap = e.getValue();

                List<String> candidates = cacheSectionQuestionsForReplacement.getIfPresent(MATH_SECTION_EXPOSURE_PREFIX + catalogId);
                Map<String, Long> values = newHomeWorkCacheService.getValuesFromKeys(candidates);
                List<String> docIds = Lists.newArrayList();
                if (!values.isEmpty()) {
                    for (Map.Entry<String, Long> q : values.entrySet()) {
                        if (q.getValue() < exposureNoThreshold) {
                            docIds.add(q.getKey());
                        }
                    }
                }
                if (!boxMap.isEmpty()) {
                    if (boxMap.containsKey("base")) {
                        MathQuestionBox mathQuestionBox = boxMap.get("base");
                        if (bookCatalogMap.containsKey(catalogId)) {
                            mathQuestionBox.setName(bookCatalogMap.get(catalogId).getName());
                        }
                        mathQuestionBox.setDifficulty(1);
                        mathQuestionBox.setUsageType(ObjectiveConfig.UsageType.NEW.getValue());
                        if (CollectionUtils.isNotEmpty(docIds)) {
                            mathQuestionBox = insertDoc(docIds, mathQuestionBox);
                        }
                        if (StringUtils.equals(catalogId, "BKC_10200084710173"))
                            mathQuestionBox.getQuestionIds().add("Q_10205390349392");
                        mathQuestionBoxes.add(mathQuestionBox);
                    }
                    if (boxMap.containsKey("solidify")) {
                        MathQuestionBox mathQuestionBox = boxMap.get("solidify");
                        if (bookCatalogMap.containsKey(catalogId)) {
                            mathQuestionBox.setName(bookCatalogMap.get(catalogId).getName());
                        }
                        mathQuestionBox.setDifficulty(1);
                        mathQuestionBox.setUsageType(ObjectiveConfig.UsageType.PRACTICE.getValue());
                        if (CollectionUtils.isNotEmpty(docIds)) {
                            mathQuestionBox = insertDoc(docIds, mathQuestionBox);
                        }
                        if (StringUtils.equals(catalogId, "BKC_10200084710173"))
                            mathQuestionBox.getQuestionIds().add("Q_10205390349392");
                        mathQuestionBoxes.add(mathQuestionBox);
                    }
                    if (CollectionUtils.isNotEmpty(mathQuestionBoxes)) {
                        result.put(catalogId, mathQuestionBoxes);
                        cacheQuestionPackagesOfSections.put(catalogId, mathQuestionBoxes);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Map<String, List<NewQuestion>> loadQuestionsByKps(Collection<String> kps, String catalogId) {
        //TODO
        if (CollectionUtils.isEmpty(kps) || StringUtils.isBlank(catalogId)) {
            return Collections.emptyMap();
        }
        Map<String, List<NewQuestion>> results = Maps.newHashMap();
        List<String> kpsNew = Lists.newArrayList();
        List<String> kpsToBeProcessed = Lists.newArrayList();

        kps.stream().forEach(kp -> kpsNew.add(StringUtils.replace(kp, "|", ":", -1)));

        kpsNew.stream().forEach(q -> {
            String key = q + catalogId;
            List<NewQuestion> questions = cacheQuestions.getIfPresent(key);
            if (questions != null) {
                results.put(q, questions);
            } else {
                kpsToBeProcessed.add(q);
            }
        });

        if (CollectionUtils.isNotEmpty(kpsToBeProcessed)) {
            List<String> questionDocIds = cacheSectionQuestionsForReplacement.getIfPresent(UNIT_PACKAGE_QUESTIONS_PREFIX + catalogId);
            if (questionDocIds == null) {
                questionDocIds = packageService.loadQuestionsByUnitIds(Collections.singletonList(catalogId)).get(catalogId);
                if (questionDocIds != null)
                    cacheSectionQuestionsForReplacement.put(UNIT_PACKAGE_QUESTIONS_PREFIX + catalogId, questionDocIds);
            }
            List<SectionProgressId> sectionProgressIds = sectionProgressIdDao.getSectionProgressIdsByUnit(catalogId);
            SectionProgressId sectionProgressId = null;
            if (CollectionUtils.isNotEmpty(sectionProgressIds))
                sectionProgressId = Collections.max(sectionProgressIds, Comparator.comparing(SectionProgressId::getProgressId));
            Map<String, List<NewQuestion>> questions = similarQuestionService.recomQuestionsByKps(kpsToBeProcessed, sectionProgressId, questionDocIds);
            if (!questions.isEmpty()) {
                for (Map.Entry<String, List<NewQuestion>> e : questions.entrySet()) {
                    String kpId = e.getKey();
                    List<NewQuestion> newQuestions = e.getValue();
                    String key = kpId + catalogId;

                    if (newQuestions != null) {
                        cacheQuestions.put(key, newQuestions);
                        results.put(kpId, newQuestions);
                    }
                }
            }
        }
        return results;
    }

    @Override
    public Map<String, List<String>> testMathSimilarQuestions(Collection<String> catalogIds) {
        Map<String, List<String>> result = Maps.newHashMap();
        Map<String, List<NewQuestion>> results = loadMathSimilarQuestions(catalogIds);
        results.entrySet().stream().forEach(e -> {
            result.put(e.getKey(), e.getValue().stream().map(NewQuestion::getDocId).collect(Collectors.toList()));
        });
        return result;
    }

    @Override
    public Map<String, List<String>> testSimilarQuestion(String sourceQid, String sectionId) {
        Map<String, List<String>> result = Maps.newHashMap();
        List<NewQuestion> questions = similarQuestionService.recomSimilarQuestionsByWrongQuestion(sourceQid, "", sectionId);
        result.put(sourceQid, questions.stream().map(NewQuestion::getDocId).collect(Collectors.toList()));
        return result;
    }

    @Override
    public Map<String, List<MathQuestionBox>> testQuestionPackagesOfSection(Collection<String> catalogIds) {
        return loadQuestionPackagesOfSections(catalogIds);
    }

    @Override
    public Map<String, List<String>> testLoadSimilarQuestions(Collection<String> sourceIds, String unitId) {
        Teacher teacher = new Teacher();

        Map<String, List<NewQuestion>> tmp = this.loadSimilarQuestions(sourceIds, teacher);
        Map<String, List<String>> ret = Maps.newHashMap();

        tmp.entrySet().stream().forEach(e -> {
            String key = e.getKey();
            List<String> values = Lists.newArrayList();
            List<NewQuestion> newQuestions = e.getValue();
            values.addAll(newQuestions.stream().map(NewQuestion::getDocId).collect(Collectors.toList()));
            ret.put(key, values);
        });

        return ret;
    }

    @Override
    public Map<String, List<String>> testloadGoalSimilarQuestions(Collection<String> sourceIds, String unitId) {
        Teacher teacher = new Teacher();
        Map<String, List<NewQuestion>> tmp = this.loadGoalSimilarQuestions(sourceIds, unitId, teacher);
        Map<String, List<String>> ret = Maps.newHashMap();

        tmp.entrySet().stream().forEach(e -> {
            String key = e.getKey();
            List<String> values = Lists.newArrayList();
            List<NewQuestion> newQuestions = e.getValue();
            values.addAll(newQuestions.stream().map(NewQuestion::getDocId).collect(Collectors.toList()));
            ret.put(key, values);
        });

        return ret;
    }

    @Override
    public Map<String, List<String>> testloadQuestionsByKps(Collection<String> kps, String catalogId) {
        Map<String, List<NewQuestion>> ret = loadQuestionsByKps(kps, catalogId);
        Map<String, List<String>> result = Maps.newHashMap();
        for (Map.Entry<String, List<NewQuestion>> entry : ret.entrySet()) {
            String key = entry.getKey();
            List<NewQuestion> newQuestions = entry.getValue();
            List<String> docIds = Lists.newArrayList();
            if (newQuestions != null) {
                docIds.addAll(newQuestions.stream().map(NewQuestion::getDocId).collect(Collectors.toList()));
            }
            result.put(key, docIds);
        }
        return result;
    }
}
