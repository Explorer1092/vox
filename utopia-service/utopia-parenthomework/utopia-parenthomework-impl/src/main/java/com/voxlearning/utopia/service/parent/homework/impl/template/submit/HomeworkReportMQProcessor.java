package com.voxlearning.utopia.service.parent.homework.impl.template.submit;

import com.google.common.collect.Lists;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.core.utils.MQUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.entity.Homework;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.Map;

import static com.voxlearning.utopia.service.parent.homework.impl.util.Constants.HOMEWORK_TOPIC;

/**
 * 作业报告MQ
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-15
 */
@Named
@Slf4j
public class HomeworkReportMQProcessor implements HomeworkProcessor {

    //Local variables
    private final static String REPORT_DETAIL_URL = "/view/mobile/parent/homework_parent/report_detail.vpage?referrer=1&homeworkId=";

    //Logic
    /**
     * 作业报告发送消息
     *
     * @param hc args
     * @return result
     */
    public void process(HomeworkContext hc) {
        HomeworkResult homeworkResult = hc.getHomeworkResult();
        //已完成发送消息
        if(ObjectUtils.get(()->homeworkResult.getFinished(), Boolean.FALSE)){
            //作业报告消息
            report(hc);
            //作业报告列表report消息
            report2ReportList(hc);
        }
    }

    /**
     * 作业报告消息
     *
     * @param hc
     */
    private void report(HomeworkContext hc){
        HomeworkParam p = hc.getHomeworkParam();
        Homework homework = hc.getHomework();
        //作业入口report消息
        Map<String, Object> message = MapUtils.m("messageType", "report",
                "bizType", homework.getBizType(),
                "studentId", p.getStudentId(),
                "subject", homework.getSubject(),
                "startTime", DateUtils.dateToString(homework.getStartTime()),
                "endTime", DateUtils.dateToString(homework.getEndTime()),
                "homeworkIds", Lists.newArrayList(homework.getId()),
                "desc", "作业完成，报告已出");
        MQUtils.send(HOMEWORK_TOPIC, message);
    }

    /**
     * 作业报告消息：家长通客户端作业报告列表专用
     *
     * @param hc
     */
    private void report2ReportList(HomeworkContext hc){
        HomeworkParam p = hc.getHomeworkParam();
        Homework homework = hc.getHomework();
        HomeworkResult homeworkResult = hc.getHomeworkResult();
        //获取学生班组
        Long groupId = hc.getGroupId();
        //List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(homeworkResult.getUserId());
        String publisher = "家长";//hc.getStudentInfo().getStudentName() + ObjectUtils.get(()->studentParents.stream().filter(s->s.getParentUser().getId().equals(homework.getFromUserId())).findFirst().get().getCallName(), "家长");
        Long publishUserId = ObjectUtils.get(()->Long.valueOf(homework.getPublisherId()), homework.getFromUserId());
        //组装消息
        Map<String, Object> message = MapUtils.m("messageType", "finished",
                "groupCircleType", "ELITE_HOMEWORK",
                "bizType", homework.getBizType(),
                "groupId", groupId,
                "userId", p.getStudentId(),
                "createDate", DateUtils.dateToString(homeworkResult.getCreateTime()),
                "typeId", p.getHomeworkId(),
                "linkUrl",REPORT_DETAIL_URL + p.getHomeworkId(),
                "unitName", homework.getAdditions().get("unitName"),
                "subject", homework.getSubject(),
                "content", "练习内容：" + homework.getAdditions().get("unitName"),
                "publisher", publisher,
                "publishUserId", publishUserId,
                "extInfoList", Lists.newArrayList(MapUtils.m("extType", "EXPIRE_DATE", "content","完成 " + DateUtils.dateToString(homeworkResult.getEndTime(), "M月dd日 HH:mm"))),
                "desc", "作业完成，报告已出");
        MQUtils.send(HOMEWORK_TOPIC, message);
    }



}
