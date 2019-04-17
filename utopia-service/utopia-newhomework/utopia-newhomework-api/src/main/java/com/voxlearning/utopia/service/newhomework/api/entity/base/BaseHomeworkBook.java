package com.voxlearning.utopia.service.newhomework.api.entity.base;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016/11/28
 */
@Getter
@Setter
public class BaseHomeworkBook implements Serializable {

    private static final long serialVersionUID = 5258100749294104730L;

    public SchoolLevel schoolLevel;
    public Subject subject;                                                // 学科
    public String actionId;                                                // 在批量布置的时候一定要保持这个id一致,拼接方法:"teacherId_${批量布置时间点}"
    public String title;                                                   // 作业名称
    public String des;                                                     // 作业描述

    public Long teacherId;                                                 // 老师id，此处未来可能会变为fromUserId，布置作业的用户角色不会仅仅是老师
    public Long clazzGroupId;                                              // 班组id，有问题问长远

    public LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practices;     // <作业类型，List>

    public Set<String> processUnitNameList() {
        Set<String> unitNames = new LinkedHashSet<>();
        if (practices != null) {
            for (ObjectiveConfigType objectiveConfigType : practices.keySet()) {
                for (NewHomeworkBookInfo bookInfo : practices.get(objectiveConfigType)) {
                    unitNames.add(bookInfo.getUnitName());
                }
            }
        }
        return unitNames;
    }

    public Set<String> processUnitIds() {
        if (MapUtils.isEmpty(practices)) {
            return Collections.emptySet();
        }
        return practices.values()
                .stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .map(NewHomeworkBookInfo::getUnitId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }

    public String processBookId() {
        if (MapUtils.isEmpty(practices)) {
            return null;
        }
        return practices.values()
                .stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .map(NewHomeworkBookInfo::getBookId)
                .filter(StringUtils::isNotBlank)
                .findFirst().orElse(null);
    }


    public Set<String> processBookNameList() {
        Set<String> bookNames = new LinkedHashSet<>();
        if (practices != null) {
            for (ObjectiveConfigType objectiveConfigType : practices.keySet()) {
                for (NewHomeworkBookInfo bookInfo : practices.get(objectiveConfigType)) {
                    bookNames.add(bookInfo.getBookName());
                }
            }
        }
        return bookNames;
    }

    public Set<String> processBookIdList() {
        Set<String> bookIds = new LinkedHashSet<>();
        if (MapUtils.isNotEmpty(practices)) {
            for (ObjectiveConfigType objectiveConfigType : practices.keySet()) {
                for (NewHomeworkBookInfo bookInfo : practices.get(objectiveConfigType)) {
                    bookIds.add(bookInfo.getBookId());
                }
            }
        }
        return bookIds;
    }

    /**
     * 获取不同作业形式下题目ID和unitId对应关系
     * @return
     */
    public Map<ObjectiveConfigType, Map<String, String>> processUnitIdMap() {
        if (MapUtils.isEmpty(practices)) {
            return Collections.emptyMap();
        }
        Map<ObjectiveConfigType, Map<String, String>> responseMap = new HashMap<>();
        for (Map.Entry<ObjectiveConfigType, List<NewHomeworkBookInfo>> configTypeEntry : practices.entrySet()) {

            Map<List<String>, String> questionsUnitIdMap = configTypeEntry.getValue()
                    .stream()
                    .collect(Collectors.toMap(NewHomeworkBookInfo::getQuestions, NewHomeworkBookInfo::getUnitId, (o1, o2) -> o1));
            Map<String, String> questionUnitMap = new HashMap<>();

            for(List<String> qids : questionsUnitIdMap.keySet()){
                if(CollectionUtils.isNotEmpty(qids)){
                    String unitId = questionsUnitIdMap.get(qids);
                    if(unitId != null){
                        for(String qid : qids){
                            questionUnitMap.put(qid, unitId);
                        }
                    }
                }
            }
            responseMap.put(configTypeEntry.getKey(), questionUnitMap);
        }
        return responseMap;
    }
}
