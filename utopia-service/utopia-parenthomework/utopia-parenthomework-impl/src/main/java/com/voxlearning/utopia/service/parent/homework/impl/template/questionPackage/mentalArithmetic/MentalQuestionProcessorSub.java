package com.voxlearning.utopia.service.parent.homework.impl.template.questionPackage.mentalArithmetic;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.MentalArithmeticTimeLimit;
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
 * 在线口算过滤
 * @author chongfeng.qi
 * @data 20190116
 */
@Named
@SubType({
        ObjectiveConfigType.MENTAL_ARITHMETIC
})

public class MentalQuestionProcessorSub implements HomeworkProcessor {

    @Inject
    private HomeworkUserRefLoaderImpl homeworkUserRefLoader;
    @Inject
    private HomeworkLoaderImpl homeworkLoader;

    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        if (CollectionUtils.isEmpty(param.getUnitIds())) {
            hc.setMapMessage(MapMessage.successMessage());
            return;
        }
        // 单元知识点题
        Map<String, Map<String, List<String>>> unitPointQuestionMap = new LinkedHashMap<>();
        // 根据当前单元找前一单元的题目
        Map<String, String> preUnits = new HashMap<>();
        wrapperUnitQuestion(hc.getBookQuestionNode(), unitPointQuestionMap, preUnits);
        // 请求单元
        List<String> unitIds = param.getUnitIds();
        List<QuestionPackage> questionPackages = new ArrayList<>();
        // 查询7天内布置过的题
        Collection<HomeworkUserRef> homeworkUserRefs = homeworkUserRefLoader.lastTime(param.getStudentId(), DateUtils.addDays(new Date(), -7));
        Set<String> userDid = new HashSet<>();
        Set<String> hIds = homeworkLoader.loadHomeworks(homeworkUserRefs.stream().map(HomeworkUserRef::getHomeworkId).collect(Collectors.toSet()))
                .values().stream().filter(h -> Objects.equals(h.getBizType(), ObjectiveConfigType.MENTAL_ARITHMETIC.name()))
                .map(Homework::getId).collect(Collectors.toSet());
        for (HomeworkPractice homeworkPractice : homeworkLoader.loadHomeworkPractices(hIds).values()) {
            for (Practices practice : homeworkPractice.getPractices()) {
                userDid.addAll(practice.getQuestions().stream().map(Questions::getDocId).collect(Collectors.toSet()));
            }
        }
        for (String unitId : unitIds) {
            Set<String> realDocId= new HashSet<>();
            // 取题
            qQuestion(realDocId, unitPointQuestionMap, userDid, unitId, preUnits);
            // 组题包
            QuestionPackage questionPackage = new QuestionPackage();
            // 题包id 唯一性 前缀"MENTAL_ARITHMETIC", 学生id, 单元id, 名称"BASE"
            questionPackage.setId(HomeworkUtil.generatorID(param.getBizType(), param.getStudentId(), unitId, "BASE"));
            // 题包名
            questionPackage.setName("BASE");
            long duration = 300;
            questionPackage.setDocIds(new ArrayList<>(realDocId));
            // 题包时长
            questionPackage.setDuration(duration);
            questionPackage.setBizType(ObjectiveConfigType.MENTAL_ARITHMETIC.name());
            questionPackage.setUnitId(unitId);
            questionPackage.setData(MapUtils.map("timeLimit", MentalArithmeticTimeLimit.FIVE.getTime()));
            questionPackages.add(questionPackage);
        }
        hc.setQuestionPackages(questionPackages);
    }

    /**
     * 单元知识点题
     * @param bookQuestionNode 教材题
     * @param unitPointQuestionMap 单元知识点题Map
     */
    private void wrapperUnitQuestion(BookQuestionNode bookQuestionNode, Map<String, Map<String, List<String>>> unitPointQuestionMap, Map<String, String> preUnits) {
        List<String> bookUnitIds = new ArrayList<>();
        bookQuestionNode.getChildNodes().forEach(unitNode -> {
            Map<String, List<String>> pointQuestionMap = new LinkedHashMap<>();
            List<BookQuestionNode> sections = unitNode.getChildNodes();
            if (CollectionUtils.isEmpty(sections)) {
                LoggerUtils.info("MentalQuestionProcessorSub.wrapperUnitQuestion", unitNode);
                return;
            }
            sections.forEach(sectionNode -> {
                // sectionNode可能没有知识点
                if (sectionNode.getChildNodes() == null) {
                    return;
                }
                sectionNode.getChildNodes().forEach(pointNode -> {
                    if (pointNode == null || pointNode.getChildNodes() == null) {
                        return;
                    }
                    List<String> qIds = pointNode.getChildNodes().stream().map(BookQuestionNode::getId).collect(Collectors.toList());
                    if (qIds.size() > 0) {
                        if (pointQuestionMap.containsKey(pointNode.getId())) {
                            return;
                        }
                        pointQuestionMap.put(pointNode.getId(), qIds);
                    }
                });
            });
            unitPointQuestionMap.put(unitNode.getId(), pointQuestionMap);
            bookUnitIds.add(unitNode.getId());
        });
        for (int i = 0; i < bookUnitIds.size() - 1; i++) {
            preUnits.put(bookUnitIds.get(i+1), bookUnitIds.get(i));
        }
    }
    /**
     * 取题
     * @param realDocId
     */
    private void qQuestion(Set<String> realDocId, Map<String, Map<String, List<String>>> unitPointQuestionMap, Set<String> userDid, String unitId, Map<String, String> preUnit) {
        Map<String, List<String>> pointQuestionMap = unitPointQuestionMap.get(unitId);
        // 课时下面没有知识点的去补其他课时的题
        while (pointQuestionMap != null && pointQuestionMap.size() > 0) {
            if (realDocId.size() >= 20) {
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
                if (realDocId.size() >= 20) {
                    return;
                }
                int index = 0;
                if (qList.size() > 1) {
                    index = (int) (Math.random() * qList.size());
                }
                // 7天内
                if (!userDid.contains(qList.get(index))) {
                    realDocId.add(qList.get(index));
                }
                qList.remove(index);
            }
        }
        // 补题逻辑
        if (realDocId.size() <= 16) {
            // 取上一单元
            String preUnitId = preUnit.get(unitId);
            // 取到第一单元退出
            if (preUnitId == null) {
                // 如果题还不够，就取之前布置过的题
                if (!userDid.isEmpty() && realDocId.size() <= 16) {
                    for (String did : userDid) {
                        if (realDocId.size() >= 16) {
                            break;
                        }
                        realDocId.add(did);
                    }
                }
                return;
            }
            qQuestion(realDocId, unitPointQuestionMap, userDid, preUnitId, preUnit);
        }
    }
}
