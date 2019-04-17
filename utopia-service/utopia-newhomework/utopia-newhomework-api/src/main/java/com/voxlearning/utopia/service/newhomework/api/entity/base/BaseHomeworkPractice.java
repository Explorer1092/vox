package com.voxlearning.utopia.service.newhomework.api.entity.base;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/1/6
 */
@Getter
@Setter
public class BaseHomeworkPractice implements Serializable {

    private static final long serialVersionUID = -5294557749063314213L;

    public List<NewHomeworkPracticeContent> practices;                     // 作业内容

    /**
     * 取某一种作业形式下的所有试题id
     *
     * @param type 作业形式
     * @return List<Qid>
     */
    public List<String> findQuestionIds(ObjectiveConfigType type, Boolean includeReadingOral) {
        NewHomeworkPracticeContent newHomeworkPracticeContent = this.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (newHomeworkPracticeContent == null)
            return Collections.emptyList();
        return newHomeworkPracticeContent.processNewHomeworkQuestion(includeReadingOral)
                .stream()
                .filter(Objects::nonNull)
                .map(NewHomeworkQuestion::getQuestionId)
                .collect(Collectors.toList());
    }

    public List<String> findAllQuestionIds() {
        List<String> result = new LinkedList<>();
        practices.stream().filter(Objects::nonNull).forEach(n -> result.addAll(findQuestionIds(n.getType(), true)));
        return result;
    }


    /**
     * 基础作业类型下的所有应用形式
     *
     * @param type 作业形式
     * @return List<NewHomeworkQuestion>
     */
    public List<NewHomeworkApp> findNewHomeworkApps(ObjectiveConfigType type) {
        return practices.stream()
                .filter(o -> Objects.equals(type, o.getType()))
                .map(NewHomeworkPracticeContent::getApps)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * 某种作业形式下的题目信息
     *
     * @param type 作业形式
     * @return List<NewHomeworkQuestion>
     */
    public List<NewHomeworkQuestion> findNewHomeworkQuestions(ObjectiveConfigType type) {
        if (practices == null)
            return Collections.emptyList();
        NewHomeworkPracticeContent newHomeworkPracticeContent = this.findTargetNewHomeworkPracticeContentByObjectiveConfigType(type);
        if (newHomeworkPracticeContent == null)
            return Collections.emptyList();
        return newHomeworkPracticeContent.processNewHomeworkQuestion(false);
    }

    /**
     * 基础应用作业形式下的题目信息
     *
     * @param type       作业形式
     * @param lessonId   lessonId
     * @param categoryId 练习类型id
     * @return List<NewHomeworkQuestion>
     */
    public List<NewHomeworkQuestion> findNewHomeworkQuestions(ObjectiveConfigType type, String lessonId, Integer categoryId) {
        return practices.stream()
                .filter(o -> Objects.equals(type, o.getType()))
                .map(NewHomeworkPracticeContent::getApps)
                .flatMap(Collection::stream)
                .filter(o -> Objects.equals(lessonId, o.getLessonId()) && Objects.equals(categoryId, o.getCategoryId()))
                .map(NewHomeworkApp::getQuestions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * 阅读绘本作业形式下的题目信息
     *
     * @param type          作业形式
     * @param pictureBookId 绘本id
     * @return List<NewHomeworkQuestion>
     */
    public List<NewHomeworkQuestion> findNewHomeworkQuestions(ObjectiveConfigType type, String pictureBookId) {
        return practices.stream()
                .filter(o -> Objects.equals(type, o.getType()))
                .map(NewHomeworkPracticeContent::getApps)
                .flatMap(Collection::stream)
                .filter(o -> Objects.equals(pictureBookId, o.getPictureBookId()))
                .map(NewHomeworkApp::getQuestions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * @param o type of ObjectiveConfigType
     * @return find NewHomeworkPracticeContent by the ObjectiveConfigType
     */
    public NewHomeworkPracticeContent findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType o) {
        for (NewHomeworkPracticeContent n : practices) {
            if (n.getType() == o) {
                return n;
            }
        }
        return null;
    }

    /**
     * KV形式的作业结构
     *
     * @return LinkedHashMap
     */
    public LinkedHashMap<ObjectiveConfigType, NewHomeworkPracticeContent> findPracticeContents() {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkPracticeContent> resultMap = new LinkedHashMap<>();
        // 每种作业形式只存在一条记录
        for (NewHomeworkPracticeContent content : practices) {
            resultMap.put(content.getType(), content);
        }
        return resultMap;
    }
}
