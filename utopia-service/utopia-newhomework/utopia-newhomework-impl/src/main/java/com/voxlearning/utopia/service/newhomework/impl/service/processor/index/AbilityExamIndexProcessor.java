package com.voxlearning.utopia.service.newhomework.impl.service.processor.index;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.newhomework.api.constant.AssignmentConfigType;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamBasic;
import com.voxlearning.utopia.service.newhomework.impl.service.processor.AbilityExamSpringBean;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 获取题目数据
 *
 * @author lei.liu
 * @version 18-11-1
 */
@Named
public class AbilityExamIndexProcessor extends AbilityExamSpringBean {

    @Inject private RaikouSystem raikouSystem;
    @Inject private AbilityExamQuestionCacheManager abilityExamQuestionCacheManager;

    public void index(AbilityExamIndexContext context) {

        // 学生验证
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(context.getStudentId());
        ExRegion exRegion = raikouSystem.loadRegion(studentDetail.getStudentSchoolRegionCode());
        if (exRegion == null) {
            context.errorResponse("学校地区信息错误");
            context.setErrorCode("900");
            return;
        }

        // 题目获取情况
        AbilityExamBasic basic = abilityExamBasicDao.load(String.valueOf(context.getStudentId()));
        boolean finished = false;
        if (basic != null) {
            // 已经完成了全部题目
            if (basic.fetchFinished()) {
                finished = true;
            }
        } else {
            // 尚未获取题目，获取题目列表
            basic = abilityExamQuestionCacheManager.getQuestionInfo(context.getStudentId(), studentDetail.getClazzLevel().getLevel());
            // 保存的是docId需要获取一下id
            if (basic != null && CollectionUtils.isNotEmpty(basic.getQuestionIds())) {
                Map<String, NewQuestion> newQuestions = questionLoaderClient.loadLatestQuestionByDocIds(basic.getQuestionIds());
                if (MapUtils.isNotEmpty(newQuestions)) {
                    // 题目类型白名单
                    List<Integer> allowedSubContentTypeIds = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 14, 16, 17, 18, 19, 20, 21, 24, 27, 28, 29, 30);
                    // 日志
                    List<String> wrongQuestionIds = newQuestions.values()
                            .stream()
                            .filter(o -> !CollectionUtils.containsAll(allowedSubContentTypeIds, o.findSubContentTypeIds()))
                            .map(NewQuestion::getId)
                            .collect(Collectors.toList());

                    if (CollectionUtils.isNotEmpty(wrongQuestionIds)) {
                        LogCollector.info("backend-general", MapUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getStudentId(),
                                "mod1", basic.getPaperId(),
                                "mod2", StringUtils.join(basic.getQuestionIds(), ","),
                                "op", "AbilityExamIndexProcessor"
                        ));
                    }

                    List<String> questionIds = newQuestions.values()
                            .stream()
                            .filter(o -> CollectionUtils.containsAll(allowedSubContentTypeIds, o.findSubContentTypeIds()))
                            .map(NewQuestion::getId)
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(questionIds)) {
                        basic.setQuestionIds(questionIds);
                    } else {
                        context.errorResponse("获取题目信息失败");
                        context.setErrorCode("900");
                        return;
                    }
                }
            } else {
                context.errorResponse("获取题目信息失败");
                context.setErrorCode("900");
                return;
            }
            basic.setId(String.valueOf(context.getStudentId()));
            basic.setCompletedTime(null); // 保险
            abilityExamBasicDao.upsert(basic);
        }

        // Practice
        Map<String, Object> practiceInfo = MapUtils.m(
                "objectiveConfigType", AssignmentConfigType.INTELLIGENCE_EXAM.getType(),
                "objectiveConfigTypeName", AssignmentConfigType.INTELLIGENCE_EXAM.getName(),
                "doHomeworkUrl", UrlUtils.buildUrlQuery("/bonus/ability/do.vpage", MapUtils.m("homeworkId", context.getStudentId(), "type", AssignmentConfigType.INTELLIGENCE_EXAM.getType())),
                "finished", finished
        );
        context.getDataMap().put("practices", Collections.singletonList(practiceInfo));

        // Other
        context.getDataMap().put("id", context.getStudentId());
        context.getDataMap().put("homeworkId", context.getStudentId());
        context.getDataMap().put("practiceCount", 1);
        context.getDataMap().put("finished", finished);
        context.getDataMap().put("userId", context.getStudentId());

    }

}
