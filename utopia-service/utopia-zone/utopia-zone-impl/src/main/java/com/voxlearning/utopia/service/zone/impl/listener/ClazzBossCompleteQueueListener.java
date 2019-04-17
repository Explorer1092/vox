package com.voxlearning.utopia.service.zone.impl.listener;

import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.zone.api.*;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRecord;
import com.voxlearning.utopia.service.zone.api.entity.boss.AttackMonsterResult;
import com.voxlearning.utopia.service.zone.api.entity.boss.ClazzBossAward;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotActivityBizObject;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzActivityRecordPersistence;

import javax.annotation.Resource;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 *  消灭怪兽消息处理监听类
 * @author xuedongfeng
 * @since 2018.11.5
 */
@Named("com.voxlearning.utopia.service.zone.impl.listener.ClazzBossCompleteQueueListener")
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "main", queue = "gaia.va.behavior.topic")
        },
        maxPermits = 8
)
public class ClazzBossCompleteQueueListener extends SpringContainerSupport implements MessageListener {
    @Resource
    private StudentLoaderClient studentLoaderClient;
    @Resource
    private ClassCircleBossService classCircleBossService;
    @Resource
    private ClazzActivityService clazzActivityService;
    @Resource
    private ClazzActivityRecordPersistence clazzActivityRecordPersistence;

    @Resource
    private ClassCircleGivingService classCircleGivingService;

    @Resource
    private ChickenWeightService chickenWeightService;
    @Resource
    private ClassCirclePlotService classCirclePlotService;

    @Override
    public void onMessage(Message message) {
        try {
            if (message == null) {
                logger.error("clazz circle boss queue no message");
                return;
            }
            Object body = message.decodeBody();

            if (body != null && body instanceof String) {
                String json = (String) body;
                AttackMonsterResult attackMonsterResult = JsonUtils.fromJson(json, AttackMonsterResult.class);
                if (attackMonsterResult == null) {
                    logger.error("ClazzBossCompleteQueueListener error. message:{}", body);
                    return;
                }
                //返回错题boss
                if(attackMonsterResult.getType().equals("revise_boss")){

                    //打怪成功 处理逻辑
                    if ((Boolean) attackMonsterResult.getAttributes().get("isSuccess")){
                        List<ClazzBossAward> clazzBossAwardList = classCircleBossService.getClazzBossAwardList(2);
                        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(attackMonsterResult.getStudentId());

                        //班级完成数加 1
                        classCircleBossService.increaseCountByClazzIdAndType(studentDetail.getClazz().getId());

                        //给该用户消灭怪兽加 1
                        ClazzActivityRecord userRecord = clazzActivityService.findUserRecord(attackMonsterResult.getStudentId(), studentDetail.getClazz().getSchoolId(), studentDetail.getClazz().getId(), 2);

                        if(userRecord==null){
                            return;
                        }
                        if (userRecord.getCondition() == null || userRecord.getCondition().get("currentProgress") == null) {
                            return;
                        }
                        int currentProgress = (int)userRecord.getCondition().get("currentProgress");
                        userRecord.getCondition().put("currentProgress",currentProgress + 1);
                        clazzActivityRecordPersistence.updateOrSave(userRecord);

                        for (ClazzBossAward clazzBossAward : clazzBossAwardList) {
                            //判断给指定的等级需要加 1
                            if (currentProgress == clazzBossAward.getTargetValue().intValue() - 1 && clazzBossAward.getSelfOrClazz().intValue()==0){
                                classCircleBossService.increaseCountBySchoolIdAndType(studentDetail.getClazz().getSchoolId(),clazzBossAward.getType());
                            }
                        }

                    }
                }
                //火鸡活动
                if (attackMonsterResult.getType().equals("app_practice")) {
                    StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(attackMonsterResult.getStudentId());

                    if(attackMonsterResult.getAttributes()==null||attackMonsterResult.getAttributes().get("is_first_practiced")==null){
                        return;
                    }

                    if(!(Boolean)attackMonsterResult.getAttributes().get("is_first_practiced")){
                        return;
                    }

                    if(attackMonsterResult.getAttributes().get("score")==null||(Integer)attackMonsterResult.getAttributes().get("score")<1){
                        return;
                    }
                    if(attackMonsterResult.getAttributes().get("grade")==null){
                       return;
                    }
                    //判断是否本年级
                     if(!attackMonsterResult.getAttributes().get("grade").equals(studentDetail.getClazz().getClazzLevel().getLevel())){
                        return;
                     }

                    if(attackMonsterResult.getAttributes().get("term")==null
                        ||Term.ofMonth( new Date().getMonth()+1).getKey()!=(Integer) attackMonsterResult.getAttributes().get("term")){
                        return;
                    }
                    if(attackMonsterResult.getAttributes().get("appKey")==null){
                        return;
                    }

                    //小王子剧情加贡献值
                    ClazzActivityRecord clazzActivityRecord = clazzActivityService.
                            findUserRecord(attackMonsterResult.getStudentId(), studentDetail.getClazz().getSchoolId(), studentDetail.getClazz().getId(), 3);
                    if(clazzActivityRecord !=null && clazzActivityRecord.getBizObject() != null && attackMonsterResult.getAttributes()!=null){
//                        if (SafeConverter.toBoolean(attackMonsterResult.getAttributes().get("is_vip"))){//是vip
                            classCirclePlotService.addClazzContribution(3,studentDetail.getId(),6);
                            classCirclePlotService.addStudentContribution(3,studentDetail.getId(),6);
//                        }else {
//                            classCirclePlotService.addClazzContribution(3,studentDetail.getId(),3);
//                            classCirclePlotService.addStudentContribution(3,studentDetail.getId(),3);
//                        }
                    }

                    /*//火鸡活动
                    clazzActivityRecord = clazzActivityService.
                        findUserRecord(attackMonsterResult.getStudentId(), studentDetail.getClazz().getSchoolId(), studentDetail.getClazz().getId(), 4);
                    if(clazzActivityRecord ==null){
                        return;
                    }
                    if(clazzActivityRecord.getBizObject()!=null){
                        ChickenStudent chickenStudent = JsonUtils.fromJson(JsonUtils.toJson(clazzActivityRecord.getBizObject()),ChickenStudent.class);
                        if(!chickenStudent.getJoinClass()){
                            classCircleGivingService.addClassCount(studentDetail.getClazz().getId().toString());
                            chickenStudent.setJoinClass(true);
                            clazzActivityRecord.setBizObject(chickenStudent);
                            clazzActivityService.updateRecord(clazzActivityRecord);
                        }
//                        int weightValue =chickenWeightService.weightType();
                        int weightValue =1;
                        if(attackMonsterResult.getAttributes().get("appKey").equals("AfentiExam")){
                            weightValue=3;
                        }
                        if(attackMonsterResult.getAttributes().get("appKey").equals("AfentiMath")){
                            weightValue=2;
                        }
                        if(attackMonsterResult.getAttributes().get("appKey").equals("AfentiChinese")){
                            weightValue=1;
                        }
                        classCircleGivingService.
                                upsetClazzCircleRewardNotice(4,attackMonsterResult.getStudentId(),false,weightValue);
                    }*/
                }

            }
        } catch (Exception ex) {
            return;
        }
    }

}
