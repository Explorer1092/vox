package com.voxlearning.utopia.service.parent.homework.impl.template.submit;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.parent.homework.api.entity.Homework;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * 作业结果上报大数据
 *
 * @author Wenlong Meng
 * @since 20190121
 */
@Named
@Slf4j
public class HomeworkReport2BigDataMQProcessor implements HomeworkProcessor {

    //Local variables
    /**
     * 做题数据上报
     */
    @Getter
    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.parent.platform.homework.result", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer messageProducer;

    //Logic
    /**
     * 作业报告发送消息
     *
     * @param hc args
     * @return result
     */
    public void process(HomeworkContext hc) {
        Homework homework = hc.getHomework();
        HomeworkParam homeworkParam = hc.getHomeworkParam();
        Long studentId = homeworkParam.getStudentId();
        Long groupId = hc.getGroupId();
        HomeworkResult homeworkResult = hc.getHomeworkResult();
        List<HomeworkProcessResult> hprs = hc.getHomeworkProcessResults();
        try{
            hprs.forEach(hpr->{
                Map<String, Object> message = MapUtils.m(
                        "studytype", homework.getSource() + "_"+hpr.getObjectiveConfigType(),
                        "processResultId", hpr.getId(),
                        "homeworkId", homework.getId(),
                        "createAt", hpr.getCreateTime(),
                        "updateAt", hpr.getUpdateTime(),
                        "clazzGroupId", groupId,
                        "homeworkTag", homework.getHomeworkTag(),
                        "type", homework.getType(),
                        "bookId", homework.getAdditions().get("bookId"),
                        "unitId", homework.getAdditions().get("unitId"),
                        "userId", studentId,
                        "questionId", hpr.getQuestionId(),
                        "questionDocId", hpr.getQuestionDocId(),
                        "questionVersion", hpr.getQuestionVersion(),
                        "standardScore", hpr.getScore(),
                        "score", hpr.getUserScore(),
                        "grasp", hpr.getRight(),
                        "subGrasp", hpr.getUserSubGrasp(),
                        "userAnswers", hpr.getUserAnswers(),
                        "duration", hpr.getDuration(),
                        "schoolLevel", "JUNIOR",
                        "finished", homeworkResult.getFinished(),
                        "subject", hpr.getSubject(),
                        "objectiveConfigType", hpr.getObjectiveConfigType(),
                        "clientType", homeworkResult.getClientType(),
                        "clientName", homeworkResult.getClientName(),
                        "env", RuntimeMode.current().name()
                );
                messageProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
            });
        }catch (Exception e){
            log.error("{}", JsonUtils.toJson(hprs), e);
        }


    }

}
