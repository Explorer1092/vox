package com.voxlearning.washington.controller.mobile.parent.studytogether;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.service.parent.api.StudyTogetherActiveCardService;
import com.voxlearning.utopia.service.parent.api.constants.CardTerm;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.CardLessonMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2018-09-03 下午9:41
 **/
@Controller
@RequestMapping(value = "/parentMobile/study_together/card_code")
public class MobileParentStudyTogetherCardCodeController extends AbstractMobileParentStudyTogetherController {

    @ImportService(interfaceClass = StudyTogetherActiveCardService.class)
    private StudyTogetherActiveCardService studyTogetherActiveCardService;

    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;


    @RequestMapping(value = "active.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage actvie() {
        User parent = currentParent();
        if (parent == null){
            return noLoginResult;
        }
        long studentId = currentRequestStudentId();
        if (studentId == 0L){
            return noLoginResult;
        }
        String code = getRequestString("code");
        if (StringUtils.isBlank(code)){
            return MapMessage.errorMessage("木有输入激活码");
        }
        try {
            return AtomicLockManager.getInstance().wrapAtomic(studyTogetherActiveCardService)
                    .keyPrefix("StudyTogetherCardCodeActive")
                    .keys(parent.getId())
                    .proxy()
                    .consumeCardCode(code, parent.getId(), studentId);
        }catch (DuplicatedOperationException e){
            return MapMessage.errorMessage("你操作太快了，请稍候重试");
        }
    }

    @RequestMapping(value = "lessons.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lessons() {
        User parent = currentParent();
        if (parent == null){
            return noLoginResult;
        }
        long studentId = currentRequestStudentId();
        if (studentId == 0L){
            return noLoginResult;
        }
        List<Integer> spuIdList = Arrays.stream(CardTerm.values()).map(CardTerm::getSpuId).collect(Collectors.toList());
        Map<Integer, List<StudyGroup>> studentSkuGroups = studyTogetherServiceClient.loadStudentSkuGroups(studentId, spuIdList);
        Set<String> lessonIdSet = studentSkuGroups.values().stream().flatMap(Collection::stream).map(StudyGroup::getLessonId).collect(Collectors.toSet());
        Map<String, Integer> progressMap = studyTogetherServiceClient.loadStudentLessonFinishProgress(studentId, lessonIdSet);
        List<CardLessonMapper> cardLessonMapperList = new ArrayList<>();
        studentSkuGroups.forEach((spuId, groups) -> {
            for (StudyGroup group : groups) {
                String lessonId = group.getLessonId();
                StudyLesson studyLesson = getStudyLesson(lessonId);
                if (studyLesson == null){
                    continue;
                }
                AlpsFuture<Long> longAlpsFuture = studyTogetherServiceClient.loadLessonJoinCount(lessonId);
                Map<String, Integer> finishInfoMap = studyTogetherServiceClient.loadStudentTodayFinishInfo(lessonId, studentId).getUninterruptibly();
                CardLessonMapper mapper = processMapper(studyLesson, true, true, progressMap.get(lessonId), finishInfoMap, longAlpsFuture.getUninterruptibly());
                if (mapper != null){
                    cardLessonMapperList.add(mapper);
                }
            }
        });
        return MapMessage.successMessage().add("lessons", cardLessonMapperList);
    }

}
