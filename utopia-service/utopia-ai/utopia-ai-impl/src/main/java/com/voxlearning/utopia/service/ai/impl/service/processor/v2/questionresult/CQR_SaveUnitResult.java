package com.voxlearning.utopia.service.ai.impl.service.processor.v2.questionresult;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.ai.cache.manager.ChipsGradeRankCacheManager;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.StoneBookData;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserLessonBookRefPersistence;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.internal.result.UnitResultProcessor;
import com.voxlearning.utopia.service.ai.support.MessageConfig;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class CQR_SaveUnitResult extends AbstractAiSupport implements IAITask<ChipsQuestionResultContext> {

    @Inject
    private UnitResultProcessor unitResultProcessor;

    @Inject
    private AIUserLessonBookRefPersistence aiUserLessonBookRefPersistence;

    @Inject
    private ChipsGradeRankCacheManager chipsGradeRankCacheManager;

    @Override
    public void execute(ChipsQuestionResultContext context) {
        Long userId = context.getUserId();
        // 课程结束 判断是否已有单元结果
        AIUserUnitResultHistory unitResultHistory = aiUserUnitResultHistoryDao.load(context.getUserId(), context.getUnit().getId());
        if ((unitResultHistory != null) || (context.getChipsQuestionResultRequest().getUnitLast())) {
            unitResultHistory = unitResultProcessor.process(userId, context.getUnit(), context.getChipsQuestionResultRequest().getBookId(), context.getLessonResultHistoryList(), context.getUnitQuestionResultList());
        }

        if (!context.getChipsQuestionResultRequest().getUnitLast()) {
            context.terminateTask();
            return;
        }

        if (context.getUnit().getJsonData().getUnit_type() != ChipsUnitType.short_lesson || unitResultHistory == null) {
            return;
        }

        String unitId = context.getUnit().getId();
        AIUserUnitResultPlan aiUserUnitResultPlan = aiUserUnitResultPlanDao.load(AIUserUnitResultPlan.generateId(userId, unitId));
        List<AIUserUnitResultPlan> unitResultPlans = aiUserUnitResultPlanDao.loadByUserId(userId).stream()
                .filter(e -> !chipsContentService.isTrailUnit(e.getUnitId())).collect(Collectors.toList());
        if (aiUserUnitResultPlan == null) {
            aiUserUnitResultPlan = AIUserUnitResultPlan.initPlan(unitResultHistory);
            aiUserUnitResultPlan.setGrade(grade(aiUserUnitResultPlan.getScore()));
            aiUserUnitResultPlan.setPointAbility(pointAbility(unitResultPlans, aiUserUnitResultPlan));
            aiUserUnitResultPlan.setStudyPlan(summary(aiUserUnitResultPlan.getPointAbility()));
            Integer rank = 1;
            StoneBookData bookData = Optional.ofNullable(stoneDataLoaderClient.getRemoteReference()
                    .loadStoneDataIncludeDisabled(Collections.singletonList(context.getChipsQuestionResultRequest().getBookId())))
                    .filter(e -> MapUtils.isNotEmpty(e))
                    .map(e -> e.get(context.getChipsQuestionResultRequest().getBookId()))
                    .map(StoneBookData::newInstance)
                    .filter(e -> e.getJsonData() != null && CollectionUtils.isNotEmpty(e.getJsonData().getChildren()))
                    .orElse(null);
            if (bookData == null) {
                context.errorResponse("没有教材");
                return;
            }
            for(StoneBookData.Node node : bookData.getJsonData().getChildren()) {
                if (node.getStone_data_id().equals(unitId)) {
                    break;
                }
                rank ++;
            }
            aiUserUnitResultPlan.setRank(rank);
            aiUserUnitResultPlanDao.insert(aiUserUnitResultPlan);
            context.getAiUserUnitResultPlans().add(aiUserUnitResultPlan);
            sendWechatTemplateMessageNotify(aiUserUnitResultPlan);
            String bookId = unitResultHistory.getBookId();
            Optional.ofNullable(bookId)
                    .map(book -> chipsContentService.loadGradeReportConfig().stream().filter(e -> e.getBook().equals(book)).findFirst().orElse(null))
                    .filter(cfg -> cfg != null && CollectionUtils.isNotEmpty(cfg.getUnits()))
                    .map(cfg -> cfg.getUnits().subList(0, Math.min(cfg.getUnits().size(), 4)).stream().collect(Collectors.toSet()))
                    .filter(se -> se.contains(unitId))
                    .map(se -> {
                        ChipsEnglishClass clazz = chipsUserService.loadClazzByUserAndBook(context.getUserId(), bookId);
                        if (clazz == null) {
                            return false;
                        }
                        int score = chipsContentService.processScoreByLessonType(context.getUserId(), Collections.singleton(unitId), LessonType.video_conversation, LessonType.task_conversation, LessonType.Dialogue, LessonType.Task);
                        chipsGradeRankCacheManager.updateRank(userId, clazz.getId(), score);
                        return true;
                    }).orElse(false);
        }

        if (CollectionUtils.isNotEmpty(unitResultPlans)) {
            context.getAiUserUnitResultPlans().addAll(unitResultPlans);
        }

    }

    private AIUserUnitResultPlan.Grade grade(Integer score) {
        if (score == null) {
            if (RuntimeMode.lt(Mode.PRODUCTION)) {
                logger.error("score is null");
            }
            return AIUserUnitResultPlan.Grade.C;
        }
        if (score >= 90) {
            return AIUserUnitResultPlan.Grade.A;
        }
        if (score > 40) {
            return AIUserUnitResultPlan.Grade.B;
        }
        return AIUserUnitResultPlan.Grade.C;
    }

    private AIUserUnitResultPlan.Ability pointAbility(List<AIUserUnitResultPlan> unitResultPlans, AIUserUnitResultPlan aiUserUnitResultPlan) {
        List<AbilityBean> abilityBeans = new ArrayList<>();
        abilityBeans.add(new AbilityBean(AIUserUnitResultPlan.Ability.Pronunciation, aiUserUnitResultPlan.getPronunciation()));
        abilityBeans.add(new AbilityBean(AIUserUnitResultPlan.Ability.Fluency, aiUserUnitResultPlan.getFluency()));
        abilityBeans.add(new AbilityBean(AIUserUnitResultPlan.Ability.Independent, aiUserUnitResultPlan.getIndependent()));
        abilityBeans.add(new AbilityBean(AIUserUnitResultPlan.Ability.Express, aiUserUnitResultPlan.getExpress()));
        abilityBeans.add(new AbilityBean(AIUserUnitResultPlan.Ability.Listening, aiUserUnitResultPlan.getListening()));
        abilityBeans.sort((e1, e2) -> e1.score.compareTo(e2.getScore()));
        if (CollectionUtils.isEmpty(unitResultPlans)) {
            return abilityBeans.get(0).getPointAbility();
        }
        unitResultPlans.sort((e1, e2) -> e2.getCreateDate().compareTo(e1.getCreateDate()));

        AbilityBean bean = abilityBeans.stream().filter(e -> unitResultPlans.get(0).getPointAbility() != e.getPointAbility() &&
                (unitResultPlans.size() == 1 || (unitResultPlans.size() > 1 && unitResultPlans.get(1).getPointAbility() != e.getPointAbility())))
                .findFirst().orElse(null);
        return bean == null ? abilityBeans.get(0).getPointAbility() : bean.getPointAbility();
    }


    private String summary(AIUserUnitResultPlan.Ability ability) {
        Random random = new Random();
        int index = random.nextInt(2);
        switch (ability) {
            case Listening:
                return MessageConfig.unit_plan_listening[index];
            case Pronunciation:
                return MessageConfig.unit_plan_production[index];
            case Express:
                return MessageConfig.unit_plan_express[index];
            case Fluency:
                return MessageConfig.unit_plan_fluency[index];
            case Independent:
                return MessageConfig.unit_plan_independent[index];

        }
        return "";
    }

    private void sendWechatTemplateMessageNotify(AIUserUnitResultPlan aiUserUnitResultPlan) {
        Date now = new Date();

        //1、根据book找课表
        ChipsEnglishProductTimetable timetable = Optional.ofNullable(aiUserLessonBookRefPersistence.loadByUserId(aiUserUnitResultPlan.getUserId()))
                .map(p -> {
                    for(AIUserLessonBookRef bookRef : p) {
                        if (!bookRef.getBookId().equalsIgnoreCase(aiUserUnitResultPlan.getBookId())) {
                            continue;
                        }
                        ChipsEnglishProductTimetable tim = chipsEnglishProductTimetableDao.load(bookRef.getProductId());
                        if (tim != null && tim.getBeginDate() != null && now.after(tim.getBeginDate())) {
                            return tim;
                        }
                    }
                    return null;
                }).orElse(null);
        if (timetable == null|| CollectionUtils.isEmpty(timetable.getCourses())) {
            return;
        }

        //2、根据课表找单元是否是今天完成的
        String patten = "yyyy-MM-dd";
        String nowStr = DateUtils.dateToString(now, patten);
        ChipsEnglishProductTimetable.Course course = timetable.getCourses().stream()
                .filter(e -> aiUserUnitResultPlan.getUnitId().equals(e.getUnitId()))
                .filter(e -> e.getBeginDate() != null && DateUtils.dateToString(e.getBeginDate(), patten).equals(nowStr))
                .findFirst().orElse(null);
        if (course == null) {
            return;
        }

        StoneUnitData unitData = Optional.ofNullable(stoneDataLoaderClient.getRemoteReference().loadStoneDataIncludeDisabled(Collections.singleton(aiUserUnitResultPlan.getUnitId())))
                .map(e -> e.get(aiUserUnitResultPlan.getUnitId()))
                .map(StoneUnitData::newInstance)
                .orElse(null);

        if (unitData == null) {
            return;
        }
        String cnInfo = Optional.ofNullable(unitData)
                .map(StoneUnitData::getJsonData)
                .map(StoneUnitData.Unit::getImage_discription)
                .orElse("");

        String enInfo = Optional.ofNullable(unitData)
                .map(StoneUnitData::getJsonData)
                .map(StoneUnitData.Unit::getImage_title)
                .orElse("");

        chipsContentService.sendDailySummaryTemplateMessage(enInfo, cnInfo, aiUserUnitResultPlan.getUserId(), unitData.getId(), aiUserUnitResultPlan.getBookId());
    }

    @Getter
    @Setter
    private class AbilityBean {
        private AIUserUnitResultPlan.Ability pointAbility;
        private Integer score;

        public AbilityBean(AIUserUnitResultPlan.Ability pointAbility, Integer score) {
            this.score = score;
            this.pointAbility = pointAbility;
        }
    }
}
