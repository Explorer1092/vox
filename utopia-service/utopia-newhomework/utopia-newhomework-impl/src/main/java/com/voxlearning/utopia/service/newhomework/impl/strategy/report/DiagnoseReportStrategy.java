package com.voxlearning.utopia.service.newhomework.impl.strategy.report;

import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching.DiagnoseReportDetailResp;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.List;
import java.util.Map;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/7/26
 */
public interface DiagnoseReportStrategy {

    /**
     * 获取不同作业类型报告详情
     * @param courseId 老师布置题包的课程(和学生实际做的课程可能不一样)
     * @return {@link DiagnoseReportDetailResp}
     */
    DiagnoseReportDetailResp getDiagnoseReportDetail(String courseId, String questionBoxId, List<String> questionBoxQids, Map<String, NewQuestion> newQuestionMap,
                                                     Map<Long, User> studentMap, Map<Long, NewHomeworkResult> newHomeworkResultMap, Map<String, SubHomeworkProcessResult> processResultMap,
                                                     Map<String, SelfStudyHomework> selfStudyHomeworkMap, Map<String, SelfStudyHomeworkResult> studyHomeworkResultMap);
}
