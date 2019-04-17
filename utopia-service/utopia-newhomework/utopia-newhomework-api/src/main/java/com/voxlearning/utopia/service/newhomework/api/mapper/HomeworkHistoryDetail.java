package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生历史详情页
 *
 * @author xuesong.zhang
 * @since 2016-03-22
 */
@Getter
@Setter
public class HomeworkHistoryDetail extends HomeworkHistoryMapper {

    private static final long serialVersionUID = -4161094861584393657L;

    private LinkedHashMap<ObjectiveConfigType, HomeworkHistoryPractice> practices;
    private Map<String, String> objectiveConfigTypes;
    private List<String> objectiveConfigTypeRanks;

}
