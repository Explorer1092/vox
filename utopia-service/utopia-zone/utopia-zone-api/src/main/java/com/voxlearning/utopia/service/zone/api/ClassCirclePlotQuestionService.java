package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.zone.api.entity.ClazzCirclePlotQuestion;
import com.voxlearning.utopia.service.zone.api.entity.boss.ClazzBossAward;
import com.voxlearning.utopia.service.zone.api.entity.boss.RewordResponse;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotActivityBizObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181110")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ClassCirclePlotQuestionService {

    public void saveOrUpdateQuestion(ClazzCirclePlotQuestion clazzCirclePlotQuestion);

    public NewQuestion  getByNodeIdAndDiff(Integer grade,Integer nodeId,Integer diff);

    public List<ClazzCirclePlotQuestion> getListByNodeIdAndDiff(Integer grade,Integer nodeId,Integer diff);


    public NewQuestion getQuestionFormContentLibraryByQuestonId(String questionId);

    public MapMessage answerQuestion( Integer activityId, Long schoolId,Long clazzId,Long userId,Integer plotGroup,Integer questionNo,Boolean  finished);


}
