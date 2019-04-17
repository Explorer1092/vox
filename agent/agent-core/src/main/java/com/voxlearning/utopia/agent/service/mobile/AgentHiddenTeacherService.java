package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentUserOperationType;
import com.voxlearning.utopia.agent.dao.mongo.AgentHiddenTeacherDao;
import com.voxlearning.utopia.agent.dao.mongo.AgentUserOperationRecordDao;
import com.voxlearning.utopia.agent.persist.entity.AgentHiddenTeacher;
import com.voxlearning.utopia.agent.persist.entity.AgentUserOperationRecord;
import com.voxlearning.utopia.agent.service.common.BaseDictService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AgentHiddenTeacher服务
 *
 * @author chunlin.yu
 * @create 2018-01-11 12:26
 **/
@Named
public class AgentHiddenTeacherService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private AgentHiddenTeacherDao agentHiddenTeacherDao;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private AgentUserOperationRecordDao agentUserOperationRecordDao;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private BaseDictService baseDictService;

    @Inject
    private EmailServiceClient emailServiceClient;

    public void hideAndShowTeacher(Collection<Long> teacherIds){
        emailServiceClient.createPlainEmail()
                .body("处理隐藏老师方法开始")
                .subject("处理隐藏老师开始【" + RuntimeMode.current().getStageMode() + "】")
                .to("xianlong.zhang@17zuoye.com;deliang.che@17zuoye.com")
                .send();
        try{
            Set<Long> allRemoveIds = new HashSet<>();
            Set<Long> allInsertIds = new HashSet<>();

            //人工取消隐藏老师记录
            List<AgentUserOperationRecord> userOperationRecords = agentUserOperationRecordDao.findByType(AgentUserOperationType.SHOW_TEACHER);
            Map<Long, AgentUserOperationRecord> teacherOperationRecordMap = userOperationRecords.stream().collect(Collectors.toMap(AgentUserOperationRecord::getTeacherId, Function.identity(), (o1, o2) -> {
                if(o1.getCreateTime().after(o2.getCreateTime())){
                    return o1;
                }
                return o2;
            }));

            //对指定老师进行隐藏或者显示操作
            if (CollectionUtils.isNotEmpty(teacherIds)){

                Map<Long, CrmTeacherSummary> teacherMap = crmSummaryLoaderClient.loadTeacherSummary(teacherIds);
                //隐藏与显示老师操作
                hideAndShowTeacher(teacherMap,allRemoveIds,allInsertIds,teacherOperationRecordMap);
            }else {
                // 获取字典表小学学校
                List<AgentDictSchool> dictSchoolList = baseDictService.loadAllSchoolDictData();
                List<Long> schoolIds = dictSchoolList.stream().filter(p -> Objects.equals(p.getSchoolLevel(), SchoolLevel.JUNIOR.getLevel())).map(AgentDictSchool::getSchoolId).collect(Collectors.toList());

                Map<Integer, List<Long>> map = schoolIds.stream().collect(Collectors.groupingBy(p -> schoolIds.indexOf(p) % 200, Collectors.toList()));
                map.values().forEach(p -> {

                    // 获取TeacherSummary数据
                    Map<Long, List<CrmTeacherSummary>> schoolTeacherMap = crmSummaryLoaderClient.loadSchoolTeachers(p);
                    Map<Long, CrmTeacherSummary> teacherMap = schoolTeacherMap.values().stream().flatMap(List::stream).collect(Collectors.toMap(CrmTeacherSummary::getTeacherId, Function.identity(), (o1, o2) -> o1));

                    Set<Long> removeIds = new HashSet<>();
                    Set<Long> teacherIdSet = new HashSet<>();
                    //隐藏与显示老师操作
                    hideAndShowTeacher(teacherMap,removeIds,teacherIdSet,teacherOperationRecordMap);
                    allInsertIds.addAll(teacherIdSet);
                    allRemoveIds.addAll(removeIds);
                });
            }
            emailServiceClient.createPlainEmail()
                    .body("需要隐藏老师（insertDataList）总数："+allInsertIds.size()+"；需要取消隐藏老师（removeIds）总数："+allRemoveIds.size()+"；需要隐藏老师（insertDataList）：" + JsonUtils.toJson(allInsertIds)+"；需要取消隐藏老师（removeIds）："+JsonUtils.toJson(allRemoveIds))
                    .subject("隐藏与取消隐藏老师【" + RuntimeMode.current().getStageMode() + "】")
                    .to("deliang.che@17zuoye.com")
                    .send();
        }catch (Exception e){
            logger.error("处理隐藏老师异常 : " , e);
            emailServiceClient.createPlainEmail()
                    .body("处理隐藏老师异常")
                    .subject("处理隐藏老师异常【" + RuntimeMode.current().getStageMode() + "】")
                    .to("xianlong.zhang@17zuoye.com;deliang.che@17zuoye.com")
                    .send();
        }
        emailServiceClient.createPlainEmail()
                .body("处理隐藏老师结束")
                .subject("处理隐藏老师结束【" + RuntimeMode.current().getStageMode() + "】")
                .to("xianlong.zhang@17zuoye.com;deliang.che@17zuoye.com")
                .send();
    }

    private boolean judgeNeedHideBySummary(CrmTeacherSummary teacherSummary){
        if(teacherSummary == null){
            return false;
        }

        return teacherSummary.getManualFakeTeacher()
                && teacherSummary.getAuthState() != 1
                && SafeConverter.toLong(teacherSummary.fetchRegisterTimeStamp()) < DateUtils.addDays(new Date(), -14).getTime()
                && SafeConverter.toInt(teacherSummary.getLast30DaysHwSc()) == 0
                && (SafeConverter.toInt(teacherSummary.getGroupCount()) == 0 || MathUtils.doubleDivide(SafeConverter.toDouble(teacherSummary.getAllStudentCount()),SafeConverter.toDouble(teacherSummary.getGroupCount())) < 2)
                && (SafeConverter.toInt(teacherSummary.getGroupCount()) == 0 || SafeConverter.toInt(teacherSummary.getMaxSameSubjAuTeaCountInClass()) > 0);
    }

    /**
     * 隐藏与显示老师具体操作
     * @param teacherMap
     * @param removeIds
     * @param teacherIdSet
     * @param teacherOperationRecordMap
     */
    public void hideAndShowTeacher(Map<Long, CrmTeacherSummary> teacherMap, Set<Long> removeIds,Set<Long> teacherIdSet,Map<Long, AgentUserOperationRecord> teacherOperationRecordMap){
        // 根据老师ids获取老师的隐藏数据
        Map<Long, AgentHiddenTeacher> hiddenTeacherMap = agentHiddenTeacherDao.loads(teacherMap.keySet());

        List<AgentHiddenTeacher> insertDataList = new ArrayList<>();

        teacherMap.values().forEach(t -> {
            // 已经是隐藏老师
            if(hiddenTeacherMap.containsKey(t.getTeacherId())){
                // 判断是否要取消隐藏
                if(!judgeNeedHideBySummary(t)){
                    //判断是否是包班制老师
                    Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(t.getTeacherId());
                    if (relTeacherIds.size() > 1){
                        removeIds.addAll(relTeacherIds);
                    }else {
                        removeIds.add(t.getTeacherId());
                    }
                }
            }else { // 没有隐藏的老师
                //判断是否是包班制老师
                Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(t.getTeacherId());
                //如果是包班制老师，需要所有主副账号都满足隐藏条件，才可被隐藏
                if (relTeacherIds.size() > 1){
                    Map<Long, AgentHiddenTeacher> relHiddenTeacherMap = agentHiddenTeacherDao.loads(relTeacherIds);
                    List<Long> teacherIdList = new ArrayList<>();
                    relTeacherIds.forEach(teacherId -> {
                        if(judgeNeedHideBySummary(teacherMap.get(teacherId)) && !teacherOperationRecordMap.containsKey(teacherId) && !relHiddenTeacherMap.containsKey(teacherId)){
                            teacherIdList.add(teacherId);
                        }
                    });
                    //如果所有主副账号都满足隐藏条件
                    if (teacherIdList.size() == relTeacherIds.size()){
                        relTeacherIds.forEach(teacherId->{
                            if(!relHiddenTeacherMap.containsKey(teacherId)){
                                teacherIdSet.add(teacherId);
                            }
                        });
                    }
                }else{
                    // 需要隐藏，并且在近6个月内未被人工取消隐藏过
                    if(judgeNeedHideBySummary(t) && !teacherOperationRecordMap.containsKey(t.getTeacherId())){
                        teacherIdSet.add(t.getTeacherId());
                    }
                }
            }
        });
        agentHiddenTeacherDao.removes(removeIds);
        teacherIdSet.forEach(tId->{
            AgentHiddenTeacher hiddenTeacher = new AgentHiddenTeacher();
            hiddenTeacher.setId(tId);
            insertDataList.add(hiddenTeacher);
        });
        if(CollectionUtils.isNotEmpty(insertDataList)){
            agentHiddenTeacherDao.inserts(insertDataList);
        }
    }

    /**
     * 取消隐藏老师
     * @param teacherId
     * @param currentUser
     * @return
     */
    public MapMessage cancelHideTeacher(Long teacherId, AuthCurrentUser currentUser){
        if (null == teacherId || teacherId <= 0){
            return MapMessage.errorMessage("老师ID错误");
        }

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (null == teacherDetail){
            return MapMessage.errorMessage("老师不存在");
        }

        agentHiddenTeacherDao.remove(teacherId);
        AgentUserOperationRecord operationRecord = new AgentUserOperationRecord();
        operationRecord.setOperationType(AgentUserOperationType.SHOW_TEACHER);
        operationRecord.setOperatorId(currentUser.getUserId());
        operationRecord.setOperatorName(currentUser.getRealName());
        operationRecord.setTeacherId(teacherId);
        operationRecord.setTeacherName(teacherDetail.fetchRealname());
        operationRecord.setSchoolId(teacherDetail.getTeacherSchoolId());
        operationRecord.setSchoolName(teacherDetail.getTeacherSchoolName());
        agentUserOperationRecordDao.insert(operationRecord);
        return MapMessage.successMessage();
    }

    /**
     * 根据老师ID查询隐藏老师
     * @param teacherIds
     * @return
     */
    public Map<Long, AgentHiddenTeacher> getAgentHiddenTeachers(Collection<Long> teacherIds){
        if (CollectionUtils.isEmpty(teacherIds)){
            return new HashMap<>();
        }
        return agentHiddenTeacherDao.loads(teacherIds);
    }
}
