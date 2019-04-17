package com.voxlearning.utopia.service.parent.homework.impl.template.assign;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.entity.Homework;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkPractice;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 布置作业上报数据
 *
 * @author Wenlong Meng
 * @since Jan 21, 2019
 */
@Named
@Slf4j
public class HomeworkAssign2BigDataMQProcessor implements HomeworkProcessor {

    /**
     * 布置作业上报
     */
    @Getter
    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.parent.platform.homework.assgin", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer messageProducer;

    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        Homework homework = hc.getHomework();
        Long studentId = param.getStudentId();
        HomeworkPractice homeworkPractice = hc.getHomeworkPractice();
        Map<String, Object> practices = new HashMap<>();
        try{
            homeworkPractice.getPractices().forEach(p ->
                    practices.put(
                            p.getType(),
                            MapUtils.m(
                                    "questions",
                                    p.getQuestions().stream().map(q ->
                                            MapUtils.m(
                                                    "questionId", q.getQuestionId(),
                                                    "questionVersion", q.getQuestionVersion(),
                                                    "score", q.getScore(),
                                                    "submitWay", q.getSubmitWay(),
                                                    "seconds", q.getSeconds(),
                                                    "bookId", homework.getAdditions().get("bookId"),
                                                    "bookName", homework.getAdditions().get("bookName"),
                                                    "unitId", homework.getAdditions().get("unitId"),
                                                    "unitName", homework.getAdditions().get("unitName")
                                            )
                                    ).collect(Collectors.toList())
                            )
                    )

            );
            String publisherId = StringUtils.isNotBlank(homework.getPublisherId()) ? homework.getPublisherId() : homework.getFromUserId() + "";
            Map<String, Object> message = MapUtils.m(
                    "env", RuntimeMode.current().name(),
                    "studytype", homework.getSource()+"_"+homework.getBizType(),
                    "homeworkId", homework.getId(),
                    "type", homework.getType(),
                    "homeworkTag", homework.getHomeworkTag(),
                    "schoolLevel", "JUNIOR",
                    "subject", homework.getSubject(),
                    "actionId", homework.getActionId(),
                    "title", "家长通-" + ObjectUtils.get(()->ObjectiveConfigType.of(homework.getBizType()), "同步习题"),
                    "packageId", homework.getActionId(),
                    "studentId", studentId,
                    "teacherId", publisherId,//该名称由大数据定义，此处为家长id
//                    "publisherId", publisherId,
                    "clazzGroupId", hc.getGroupId(),
                    "duration", homework.getDuration(),
                    "source", "App",
                    "startTime", homework.getStartTime(),
                    "endTime", homework.getEndTime(),
                    "disabled", false,
                    "includeSubjective", false,
                    "timeFiled",homework.getCreateTime(),
                    "checked",false,
                    "includeIntelligentTeaching",false,
                    "createAt", homework.getCreateTime(),
                    "updateAt", homework.getUpdateTime(),
                    "practices", practices
            );
            messageProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
        }catch (Exception e){
            log.error("{}", JsonUtils.toJson(homework), e);
        }

    }
}
