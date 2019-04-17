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
public class CategoryClazzHandlerContext implements Serializable {
    private static final long serialVersionUID = -1735881202055367157L;
    private List<Map<String, Object>> result = new LinkedList<>();
    private Map<String, NewQuestion> newQuestionMap;
    private Map<Long, Sentence> sentenceMap;
    private Map<Long, User> userMap;
    private PracticeType practiceType;
    private Map<String, List<NewHomeworkProcessResult>> newHomeworkProcessResultMap;

    public CategoryClazzHandlerContext(Map<String, List<NewHomeworkProcessResult>> newHomeworkProcessResultMap,
                                       Map<String, NewQuestion> newQuestionMap,
                                       Map<Long, Sentence> sentenceMap,
                                       Map<Long, User> userMap,
                                       PracticeType practiceType) {
        this.newQuestionMap = newQuestionMap;
        this.newHomeworkProcessResultMap = newHomeworkProcessResultMap;
        this.userMap = userMap;
        this.practiceType = practiceType;
        this.sentenceMap = sentenceMap;
    }

}
