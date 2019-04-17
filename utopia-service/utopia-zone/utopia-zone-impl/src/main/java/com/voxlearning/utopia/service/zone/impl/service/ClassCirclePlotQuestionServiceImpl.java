package com.voxlearning.utopia.service.zone.impl.service;

import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.service.question.api.QuestionLoader;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.zone.api.ClassCirclePlotQuestionService;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRecord;
import com.voxlearning.utopia.service.zone.api.entity.ClazzCirclePlotFinishedMaxCount;
import com.voxlearning.utopia.service.zone.api.entity.ClazzCirclePlotQuestion;
import com.voxlearning.utopia.service.zone.api.entity.DifficultyQuestion;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotActivityBizObject;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzActivityRecordPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzCirclePlotFinishedMaxCountPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzCirclePlotQuestionPersistence;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * @Author yulong.ma
 * @Date 2018/11/10 1147
 * @Version1.0
 **/
@Named("com.voxlearning.utopia.service.zone.impl.service.ClassCirclePlotQuestionServiceImpl")
@ExposeService(interfaceClass = ClassCirclePlotQuestionService.class, version = @ServiceVersion(version = "20181110"))
@Slf4j
public class ClassCirclePlotQuestionServiceImpl implements ClassCirclePlotQuestionService {

  @Inject
  private ClazzCirclePlotQuestionPersistence clazzCirclePlotQuestionPersistence;

  @Inject
  private ClazzCirclePlotFinishedMaxCountPersistence clazzCirclePlotFinishedMaxCountPersistence;

  @Inject
  private ClazzActivityRecordPersistence clazzActivityRecordPersistence;

  @Inject
  private QuestionLoader questionLoader;

  @Override
  public void saveOrUpdateQuestion(ClazzCirclePlotQuestion clazzCirclePlotQuestion) {
    clazzCirclePlotQuestionPersistence.upsert(clazzCirclePlotQuestion);
  }

  @Override
  public NewQuestion getByNodeIdAndDiff(Integer grade, Integer nodeId, Integer diff) {

    ClazzCirclePlotQuestion clazzCirclePlotQuestion = clazzCirclePlotQuestionPersistence
        .load(grade + "_" + nodeId);

    List <DifficultyQuestion> difficultyQuestions = clazzCirclePlotQuestion.getDiff();
    String questionId = "";
    for (int i = 0; i < difficultyQuestions.size(); i++) {
      DifficultyQuestion difficultyQuestion = difficultyQuestions.get(i);
      if (diff.equals(difficultyQuestion.getDifficult())) {
        List <String> stringList = difficultyQuestion.getQuestionIds();
        int index = RandomUtils.nextInt(stringList.size());
        questionId = stringList.get(index);
        break;
      }
    }
    try {
      NewQuestion newQuestion =getQuestionFormContentLibraryByQuestonId(questionId);
      if(newQuestion!=null){
        return newQuestion;
      }
      log.error("班级圈调用内容库获取题目:+"+questionId+"返回为空");
    } catch (Exception e) {
     log.error("班级圈调用内容库获取题目:"+questionId+"失败：",e);
    }
    return null;
  }

  @Override
  public List <ClazzCirclePlotQuestion> getListByNodeIdAndDiff(Integer grade, Integer nodeId,
      Integer diff) {

    return null;
  }

  @Override
  public NewQuestion getQuestionFormContentLibraryByQuestonId(String questionId) {
    return questionLoader.loadQuestionByDocId(questionId);
  }

  @Override
  public MapMessage answerQuestion( Integer activityId, Long schoolId,Long clazzId,Long userId,Integer plotGroup,Integer questionNo,Boolean  finished){

//   if(!finished){
//    Long count =  classPlotQuestionsErrorCache.loadUserCount(activityId,userId);
//    if(count!=null&&count==3){
//      MapMessage.errorMessage("今天您已累积三次打错机会");
//    }
//     classPlotQuestionsErrorCache.incrErrorCount(activityId,userId);
//     return MapMessage.successMessage();
//   }
    ClazzCirclePlotFinishedMaxCount clazzCirclePlotFinishedMaxCount =clazzCirclePlotFinishedMaxCountPersistence
        .load(plotGroup);
    //每日所有剧情完成计次
//    if(plotGroup==4&&questionNo.equals(clazzCirclePlotFinishedMaxCount.getFinishedMaxCount())){
//      Long wholePlot=classPlotFinishedCountCache.loadUserCount(activityId,userId);
//      if(wholePlot==3){
//       return MapMessage.errorMessage("今天您已累积三次通关机会");
//      }
//      classPlotFinishedCountCache.incrErrorCount(activityId,userId);
//    }

    ClazzActivityRecord clazzActivityRecord =clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activityId,schoolId,clazzId,userId));
    if(clazzActivityRecord==null){
      PlotActivityBizObject plotActivityBizObject = new PlotActivityBizObject();
      clazzActivityRecord = new ClazzActivityRecord();
      clazzActivityRecord.setStatus(1);
      clazzActivityRecord.setClazzId(clazzId);
      clazzActivityRecord.setSchoolId(schoolId);
      clazzActivityRecord.setActivityId(activityId);
      clazzActivityRecord.setUserId(userId);
      Map<Integer,Integer> nodeRightCount = Maps.newHashMap();
      nodeRightCount.put(plotGroup,questionNo);
//      plotActivityBizObject.setNodeRightCount(nodeRightCount);
      plotActivityBizObject.setCurrentHighestDiffiCult(plotGroup*questionNo);
      clazzActivityRecordPersistence.upsert(clazzActivityRecord);
      return MapMessage.successMessage("答题成功");
    }
    PlotActivityBizObject plotActivityBizObject1 = JsonUtils.fromJson(JsonUtils.toJson(clazzActivityRecord.getBizObject()), PlotActivityBizObject.class);
//    Map<Integer,Integer> nodeRightCount = plotActivityBizObject1.getNodeRightCount();
//    if(nodeRightCount!=null){
//      nodeRightCount.put(plotGroup,questionNo);
//    }else{
//      nodeRightCount =  Maps.newHashMap();
//      nodeRightCount.put(plotGroup,questionNo);
//    }
//    plotActivityBizObject1.setNodeRightCount(nodeRightCount);
    plotActivityBizObject1.setCurrentHighestDiffiCult(plotGroup*questionNo);
    clazzActivityRecordPersistence.upsert(clazzActivityRecord);

    //每个剧情完成扣次
//    if(clazzCirclePlotFinishedMaxCount.getFinishedMaxCount().equals(questionNo)){
//      classPlotQuestionsErrorCache.incrErrorCount(activityId,userId);
//    }
     return MapMessage.successMessage("答题成功");




  }


}
