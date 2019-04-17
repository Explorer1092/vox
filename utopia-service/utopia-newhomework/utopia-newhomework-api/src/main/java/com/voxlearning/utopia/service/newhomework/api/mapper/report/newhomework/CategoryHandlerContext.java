package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CategoryHandlerContext implements Serializable {
    private static final long serialVersionUID = -1743951867768030309L;
    private Map<Long, Sentence> sentenceMap;
    private Map<String, NewQuestion> newQuestionMap;
    private Map<String, NewHomeworkProcessResult> processResultMap;
    private List<String> qIds;
    private PracticeType practiceType;
    private User user;
    private List<Map<String, Object>> result = new LinkedList<>();

    public CategoryHandlerContext(Map<Long, Sentence> sentenceMap,
                                  Map<String, NewQuestion> newQuestionMap,
                                  Map<String, NewHomeworkProcessResult> dataInfo,
                                  List<String> qIds, PracticeType practiceType, User user) {
        this.sentenceMap = sentenceMap;
        this.newQuestionMap = newQuestionMap;
        this.processResultMap = dataInfo;
        this.qIds = qIds;
        this.practiceType = practiceType;
        this.user = user;
    }
}
