package com.voxlearning.utopia.service.parent.homework.impl.template.questionPackage.orcMentalArithmetic;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.parent.homework.api.entity.*;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.mapper.QuestionPackage;
import com.voxlearning.utopia.service.parent.homework.impl.HomeworkLoaderImpl;
import com.voxlearning.utopia.service.parent.homework.impl.HomeworkUserRefLoaderImpl;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SubType;
import com.voxlearning.utopia.service.parent.homework.impl.model.BookQuestionNode;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 纸质口算题包
 *
 * @author wenlong meng
 * @since Jan 17, 2019
 */
@Named
@SubType({
        ObjectiveConfigType.OCR_MENTAL_ARITHMETIC
})
public class OcrMentalArithmeticQuestionProcessorSub implements HomeworkProcessor {

    @Inject
    private HomeworkUserRefLoaderImpl homeworkUserRefLoader;
    @Inject
    private HomeworkLoaderImpl homeworkLoader;
    /**
     * 题包逻辑
     * @param hc args
     */
    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        // 课时知识点题
        Map<String, Map<String, List<String>>> sectionPointQuestionMap = new LinkedHashMap<>();
        // 根据当前课时找前一课时的题目
        Map<String, String> preSections = new HashMap<>();
        wrapperUnitQuestion(hc.getBookQuestionNode(), sectionPointQuestionMap, preSections);
        // 查询7天内布置过的题
        Collection<HomeworkUserRef> homeworkUserRefs = homeworkUserRefLoader.lastTime(param.getStudentId(), DateUtils.addDays(new Date(), -7));
        Set<String> userDid = new HashSet<>();
        Set<String> hIds = homeworkLoader.loadHomeworks(homeworkUserRefs.stream().map(HomeworkUserRef::getHomeworkId).collect(Collectors.toSet()))
                .values().stream().filter(h -> Objects.equals(h.getBizType(), ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.name()))
                .map(Homework::getId).collect(Collectors.toSet());
        for (HomeworkPractice homeworkPractice : homeworkLoader.loadHomeworkPractices(hIds).values()) {
            for (Practices practice : homeworkPractice.getPractices()) {
                userDid.addAll(practice.getQuestions().stream().map(Questions::getDocId).collect(Collectors.toSet()));
            }
        }
        List<QuestionPackage> questionPackages = new ArrayList<>();
        String unitId = hc.getUnitId();
        int count = SafeConverter.toInt(param.getData().get("count"), 20);
        String packageName = "BASE_" + count;
        Set<String> alreadyQuestions = new HashSet<>();
        Set<String> preUserQuestions = new LinkedHashSet<>();
        for (String sectionId : param.getSectionIds()) {
            Set<String> realQid = new LinkedHashSet<>();
            // 取
            qQuestion(realQid, count, sectionPointQuestionMap, userDid, alreadyQuestions, preUserQuestions, sectionId, preSections);
            // 组题包
            QuestionPackage questionPackage = new QuestionPackage();
            // 题包id 唯一性, 学生id, sectionId, 名称packageName
            questionPackage.setId(HomeworkUtil.generatorID(param.getBizType(), param.getStudentId(), sectionId, packageName));
            // 题包名
            questionPackage.setName(packageName);
            long duration = 300;
            questionPackage.setDocIds(new ArrayList<>(realQid));
            // 题包时长
            questionPackage.setDuration(duration);
            questionPackage.setBizType(ObjectiveConfigType.OCR_MENTAL_ARITHMETIC.name());
            questionPackage.setUnitId(unitId);
            questionPackage.setSection(sectionId);
            questionPackages.add(questionPackage);
        }
        hc.setQuestionPackages(questionPackages);
    }

    /**
     * 课时知识点题
     * @param bookQuestionNode 教材题
     * @param sectionPointQuestionMap 课时知识点题Map
     */
    private void wrapperUnitQuestion(BookQuestionNode bookQuestionNode, Map<String, Map<String, List<String>>> sectionPointQuestionMap, Map<String, String> preSections) {
        List<String> sectionIds = new ArrayList<>();
        for (BookQuestionNode unitNode : bookQuestionNode.getChildNodes()) {
            // 获取当前单元所有知识点的题
            List<BookQuestionNode> sections = unitNode.getChildNodes();
            if (CollectionUtils.isEmpty(sections)) {
                LoggerUtils.info("OcrMentalArithmeticQuestionProcessorSub.wrapperUnitQuestion", unitNode);
                continue;
            }
            for (BookQuestionNode sectionNode : sections) {
                if (sectionNode.getChildNodes() != null) {
                    Map<String, List<String>> pointQuestionMap = new LinkedHashMap<>();
                    for (BookQuestionNode pointNode : sectionNode.getChildNodes()) {
                        if (CollectionUtils.isEmpty(pointNode.getChildNodes())) {
                            LoggerUtils.info("OcrMentalArithmeticQuestionProcessorSub.wrapperUnitQuestion", pointNode);
                            continue;
                        }
                        List<String> qIds = pointNode.getChildNodes().stream().filter(BookQuestionNode::getSupportForAi).map(BookQuestionNode::getId).collect(Collectors.toList());
                        if (qIds.size() == 0) {
                            continue;
                        }
                        pointQuestionMap.put(pointNode.getId(), qIds);
                    }
                    sectionPointQuestionMap.put(sectionNode.getId(), pointQuestionMap);
                }
                sectionIds.add(sectionNode.getId());
            }
        }
        for (int i = 0; i < sectionIds.size() - 1; i++) {
            preSections.put(sectionIds.get(i+1), sectionIds.get(i));
        }
    }

    /**
     * 取题
     * @param realQid
     */
    private void qQuestion(Set<String> realQid, int count, Map<String, Map<String, List<String>>> sectionPointQuestionMap, Set<String> userDid, Set<String> alreadyQuestions, Set<String> preUserQuestions, String sectionId, Map<String, String> preSection) {
        Map<String, List<String>> pointQuestionMap = sectionPointQuestionMap.get(sectionId);
        Set<String> sectionQuestions = new HashSet<>();
        // 课时下面没有知识点的去补其他课时的题
        while (pointQuestionMap != null && pointQuestionMap.size() > 0) {
            if (realQid.size() >= count) {
                return;
            }
            Iterator<String> keyIterator = pointQuestionMap.keySet().iterator();
            while(keyIterator.hasNext()) {
                // 当前知识点下的所有题
                List<String> qList = pointQuestionMap.get(keyIterator.next());
                if (qList.size() == 0) {
                    keyIterator.remove();
                    continue;
                }
                if (realQid.size() >= count) {
                    return;
                }
                int index = 0;
                if (qList.size() > 1) {
                    index = (int) (Math.random() * qList.size());
                }
                // 7天内
                if (!userDid.contains(qList.get(index)) && !alreadyQuestions.contains(qList.get(index))) {
                    realQid.add(qList.get(index));
                    alreadyQuestions.add(qList.get(index));
                } else {
                    sectionQuestions.add(qList.get(index));
                }
                preUserQuestions.add(qList.get(index));
                qList.remove(index);
            }
        }

        // 补题逻辑
        if (realQid.size() <= count * 0.8) {
            // 先取当前section下的题，再去补其他section的题
            for (String did : sectionQuestions) {
                if (realQid.size() >= count) {
                    return;
                }
                if (alreadyQuestions.contains(did)) {
                    continue;
                }
                realQid.add(did);
                alreadyQuestions.add(did);
            }
            // 取上一单元
            String preSectionId = preSection.get(sectionId);
            // 已经是第一单元第一课时，就从用户已经布置过题的里面取题
            if (preSectionId == null) {
                if (!preUserQuestions.isEmpty()) {
                    for (String did : preUserQuestions) {
                        if (realQid.size() >= count) {
                            return;
                        }
                        if (alreadyQuestions.contains(did)) {
                            continue;
                        }
                        realQid.add(did);
                        alreadyQuestions.add(did);
                    }
                }
                return;
            }
            qQuestion(realQid, count, sectionPointQuestionMap, userDid, alreadyQuestions, preUserQuestions, preSectionId, preSection);
        }
    }
}
