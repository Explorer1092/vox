package com.voxlearning.utopia.agent.service.mobile.workrecord;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.workrecord.WorkRecordData;
import com.voxlearning.utopia.agent.constants.ResearchersJobType;
import com.voxlearning.utopia.agent.dao.mongo.organization.AgentOuterResourceExtendDao;
import com.voxlearning.utopia.agent.dao.mongo.workload.AgentRecordWorkloadDao;
import com.voxlearning.utopia.agent.persist.AgentOrganizationPersistence;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOrganization;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOuterResourceExtend;
import com.voxlearning.utopia.agent.persist.entity.workload.AgentRecordWorkload;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.organization.AgentOuterResourceService;
import com.voxlearning.utopia.service.crm.api.constants.agent.*;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmTeacherVisitInfo;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmVisitResearcherInfo;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.api.entities.agent.WorkRecordVisitUserInfo;
import com.voxlearning.utopia.service.crm.api.entities.agent.evaluate.EvaluationRecord;
import com.voxlearning.utopia.service.crm.api.entities.agent.signin.SignInRecord;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.CrmWorkRecordLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.evaluate.EvaluationRecordLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.signin.SignInRecordLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.work.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 工作记录数据兼容service
 * @author deliang.che
 * @since 2019/2/25
 */
@Named
public class WorkRecordDataCompatibilityService extends AbstractAgentService {
    @Inject
    private CrmWorkRecordLoaderClient crmWorkRecordLoaderClient;
    @Inject
    private WorkRecordSchoolLoaderClient workRecordSchoolLoaderClient;
    @Inject
    private WorkRecordMeetingLoaderClient workRecordMeetingLoaderClient;
    @Inject
    private WorkRecordResourceExtensionLoaderClient workRecordResourceExtensionLoaderClient;
    @Inject
    private WorkRecordAccompanyLoaderClient workRecordAccompanyLoaderClient;
    @Inject
    private WorkRecordService workRecordService;
    @Inject
    private WorkRecordTeacherLoaderClient workRecordTeacherLoaderClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private WorkSupporterLoaderClient workSupporterLoaderClient;
    @Inject
    private AgentOuterResourceExtendDao agentOuterResourceExtendDao;
    @Inject
    private WorkRecordOuterResourceLoaderClient workRecordOuterResourceLoaderClient;
    @Inject
    private EvaluationRecordLoaderClient evaluationRecordLoaderClient;
    @Inject
    private SignInRecordLoaderClient signInRecordLoaderClient;
    @Inject
    private AgentRecordWorkloadDao agentRecordWorkloadDao;
    @Inject
    private AgentOuterResourceService agentOuterResourceService;
    @Inject
    private AgentOrganizationPersistence agentOrganizationPersistence;

    /**
     * 获取工作记录（兼容新旧数据）
     * @param workRecordId
     * @param workRecordType
     * @return
     */
    public WorkRecordData getWorkRecordDataByIdAndType(String workRecordId, AgentWorkRecordType workRecordType){
        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        CrmWorkRecord workRecord = crmWorkRecordLoaderClient.load(workRecordId);
        //旧数据
        if (workRecord != null){
            workRecordDataList.addAll(transformOldToWorkRecordDataList(Collections.singletonList(workRecord)));
            //新数据
        }else {
            if (workRecordType == AgentWorkRecordType.SCHOOL){
                WorkRecordSchool workRecordSchool = workRecordSchoolLoaderClient.load(workRecordId);
                if (workRecordSchool != null){
                    workRecordDataList.addAll(transformNewIntoSchoolToWorkRecordDataList(Collections.singletonList(workRecordSchool)));
                }
            }else if (workRecordType == AgentWorkRecordType.MEETING){
                WorkRecordMeeting workRecordMeeting = workRecordMeetingLoaderClient.load(workRecordId);
                if (workRecordMeeting != null){
                    workRecordDataList.addAll(transformNewMeetingToWorkRecordDataList(Collections.singletonList(workRecordMeeting)));
                }
            }else if (workRecordType == AgentWorkRecordType.RESOURCE_EXTENSION){
                WorkRecordResourceExtension workRecordResourceExtension = workRecordResourceExtensionLoaderClient.load(workRecordId);
                if (workRecordResourceExtension != null){
                    workRecordDataList.addAll(transformNewResourceExtensionToWorkRecordDataList(Collections.singletonList(workRecordResourceExtension)));
                }
            }else if (workRecordType == AgentWorkRecordType.ACCOMPANY){
                WorkRecordAccompany workRecordAccompany = workRecordAccompanyLoaderClient.load(workRecordId);
                if (workRecordAccompany != null){
                    workRecordDataList.addAll(transformNewAccompanyToWorkRecordDataList(Collections.singletonList(workRecordAccompany)));
                }
            }
        }
        return workRecordDataList.stream().findFirst().orElse(null);
    }

    /**
     * 获取工作记录列表（兼容新旧数据）
     * @param userIds
     * @param startDate
     * @param endDate
     * @return
     */
    public List<WorkRecordData> getWorkRecordDataListByUserTypeTime(Collection<Long> userIds, AgentWorkRecordType workRecordType, Date startDate, Date endDate){
        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        //分界时间
        Date demarcationDate = DateUtils.stringToDate(WorkRecordData.demarcationDate, DateUtils.FORMAT_SQL_DATETIME);
        //组装旧数据
        if (startDate.before(demarcationDate) && endDate.before(demarcationDate)){
            workRecordDataList.addAll(getOldWorkRecordDataListByUserTypeTime(userIds,workRecordType,startDate,endDate));
            //新数据与旧数据组合
        }else if (startDate.before(demarcationDate) && endDate.after(demarcationDate)){
            workRecordDataList.addAll(getNewWorkRecordDataListByUserTypeTime(userIds,workRecordType,demarcationDate,endDate));
            workRecordDataList.addAll(getOldWorkRecordDataListByUserTypeTime(userIds,workRecordType,startDate,demarcationDate));
            //查询新数据
        }else {
            workRecordDataList.addAll(getNewWorkRecordDataListByUserTypeTime(userIds,workRecordType,startDate,endDate));
        }
        return workRecordDataList;
    }

    /**
     * 转化旧工作记录为工作数据（WorkRecordData）列表
     * @param userIds
     * @param startDate
     * @param endDate
     * @return
     */
    public List<WorkRecordData> getOldWorkRecordDataListByUserTypeTime(Collection<Long> userIds, AgentWorkRecordType workRecordType, Date startDate, Date endDate){
        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        List<CrmWorkRecord> workRecordList = new ArrayList<>();
        if (workRecordType == null){
            workRecordList.addAll(crmWorkRecordLoaderClient.listByWorkersAndTime(userIds, startDate, endDate));
        }else if (workRecordType == AgentWorkRecordType.SCHOOL){
            workRecordList.addAll(crmWorkRecordLoaderClient.listByWorkersAndType(userIds,CrmWorkRecordType.SCHOOL,startDate,endDate));
        }else if (workRecordType == AgentWorkRecordType.MEETING){
            workRecordList.addAll(crmWorkRecordLoaderClient.listByWorkersAndType(userIds,CrmWorkRecordType.MEETING,startDate,endDate));
        }else if (workRecordType == AgentWorkRecordType.RESOURCE_EXTENSION){
            workRecordList.addAll(crmWorkRecordLoaderClient.listByWorkersAndType(userIds,CrmWorkRecordType.TEACHING,startDate,endDate));
        }else if (workRecordType == AgentWorkRecordType.ACCOMPANY){
            workRecordList.addAll(crmWorkRecordLoaderClient.listByWorkersAndType(userIds,CrmWorkRecordType.VISIT,startDate,endDate));
        }
        workRecordDataList.addAll(transformOldToWorkRecordDataList(workRecordList));
        return workRecordDataList;
    }

    /**
     * 转化旧工作记录为工作数据（WorkRecordData）列表
     * @param workRecordList
     * @return
     */
    public List<WorkRecordData> transformOldToWorkRecordDataList(Collection<CrmWorkRecord> workRecordList){
        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        if (CollectionUtils.isEmpty(workRecordList)){
            return Collections.emptyList();
        }
        workRecordList.forEach(item -> {
            WorkRecordData workRecordData = new WorkRecordData();
            workRecordData.setId(item.getId());
            workRecordData.setUserId(item.getWorkerId());
            workRecordData.setUserName(item.getWorkerName());
            workRecordData.setWorkTime(item.getWorkTime());
            //进校
            if (item.getWorkType() == CrmWorkRecordType.SCHOOL){
                workRecordData.setWorkType(AgentWorkRecordType.SCHOOL);
                workRecordData.setSchoolId(item.getSchoolId());
                workRecordData.setSchoolName(item.getSchoolName());
                List<WorkRecordVisitUserInfo> visitUserInfoList = new ArrayList<>();
                List<CrmTeacherVisitInfo> visitTeacherList = item.getVisitTeacherList();
                if (CollectionUtils.isNotEmpty(visitTeacherList)){
                    visitTeacherList.forEach(p -> {
                        WorkRecordVisitUserInfo visitUserInfo = new WorkRecordVisitUserInfo();
                        visitUserInfo.setId(p.getTeacherId());
                        visitUserInfo.setName(p.getTeacherName());
                        visitUserInfo.setJob(WorkRecordVisitUserInfo.TEACHER_JOB);
                        visitUserInfo.setJobName(WorkRecordVisitUserInfo.TEACHER_JOB_NAME);
                        visitUserInfo.setSubject(p.getSubject());
                        visitUserInfo.setSubjectName(p.getSubject() != null ? p.getSubject().getValue() : "");
                        visitUserInfoList.add(visitUserInfo);
                    });
                }
                workRecordData.setVisitUserInfoList(visitUserInfoList);
                if(item.getVisitSchoolType() != null && item.getVisitSchoolType() == 1){
                    workRecordData.setVisitSchoolType(1);
                }else {
                    workRecordData.setVisitSchoolType(2);
                }
                workRecordData.setResult(item.getSchoolMemorandumInfo());

                workRecordData.setMeetingForm(item.getShowFrom());
                workRecordData.setLecturerName(item.getMeetingNote());
                workRecordData.setResult(item.getWorkContent());
                workRecordData.setPhotoUrls(Collections.singletonList(item.getScenePhotoUrl()));
                workRecordData.setMeetingTime(item.getMeetingTime());
                AgentSchoolWorkTitleType schoolWorkTitleType = AgentSchoolWorkTitleType.of(item.getWorkTitle());
                if (schoolWorkTitleType != null){
                    workRecordData.setWorkTitle(schoolWorkTitleType.getWorkTitle());
                }
                transformOldSignInInfo(item,workRecordData);
                //组会
            }else if (item.getWorkType() == CrmWorkRecordType.MEETING){
                workRecordData.setWorkType(AgentWorkRecordType.MEETING);
                workRecordData.setMeetingType(item.getMeetingType());
                workRecordData.setMeetingTime(item.getMeetingTime());
                workRecordData.setMeetingCount(item.getMeeteeCount());
                workRecordData.setMeetingForm(item.getShowFrom());
                workRecordData.setPhotoUrls(Collections.singletonList(item.getScenePhotoUrl()));
                workRecordData.setResult(item.getWorkContent());
                WorkRecordVisitUserInfo workRecordVisitUserInfo = new WorkRecordVisitUserInfo();
                workRecordVisitUserInfo.setId(item.getResearchersId());
                workRecordVisitUserInfo.setName(item.getResearchersName());
                workRecordVisitUserInfo.setJob(ResearchersJobType.RESEARCHER.getJobId());
                workRecordVisitUserInfo.setJobName(ResearchersJobType.RESEARCHER.getJobName());
                workRecordData.setVisitUserInfoList(Collections.singletonList(workRecordVisitUserInfo));
                workRecordData.setAgencyName(item.getAgencyName());
                workRecordData.setWorkTitle(item.getWorkTitle());
                workRecordData.setLecturerName(item.getMeetingNote());
                workRecordData.setIsPresent(item.getInstructorAttend());
                transformOldSignInInfo(item,workRecordData);
                //资源拓维
            }else if (item.getWorkType() == CrmWorkRecordType.TEACHING){
                workRecordData.setWorkType(AgentWorkRecordType.RESOURCE_EXTENSION);
                Map<Long,String> visitResearcherMap = new HashMap<>();
                List<CrmVisitResearcherInfo> visitResearcherList = item.getVisitedResearcherList();
                if (CollectionUtils.isNotEmpty(visitResearcherList)){
                    visitResearcherList.forEach(p -> {
                        visitResearcherMap.put(SafeConverter.toLong(p.getResearcherId()),p.getConclusion());
                    });
                }else {
                    visitResearcherMap.put(SafeConverter.toLong(item.getResearchersId()),"");
                }
                workRecordData.setVisitUserInfoList(workRecordService.generateResourceExtensionVisitUserInfo(visitResearcherMap));
                workRecordData.setVisitIntention(item.getVisitedIntention());
                workRecordData.setContent(item.getVisitedFlow());
                transformOldSignInInfo(item,workRecordData);
                //陪同
            }else if (item.getWorkType() == CrmWorkRecordType.VISIT){
                workRecordData.setWorkType(AgentWorkRecordType.ACCOMPANY);
                String workRecordId = item.getSchoolWorkRecordId();
                CrmWorkRecord crmWorkRecord = crmWorkRecordLoaderClient.load(workRecordId);
                if (crmWorkRecord != null){
                    CrmWorkRecordType workType = crmWorkRecord.getWorkType();
                    if (workType == CrmWorkRecordType.SCHOOL){
                        workRecordData.setBusinessType(AccompanyBusinessType.SCHOOL);
                    }else if (workType == CrmWorkRecordType.MEETING){
                        workRecordData.setBusinessType(AccompanyBusinessType.MEETING);
                    }else if (workType == CrmWorkRecordType.TEACHING){
                        workRecordData.setBusinessType(AccompanyBusinessType.RESOURCE_EXTENSION);
                    }
                    //陪访人信息
                    workRecordData.setAccompanyUserId(crmWorkRecord.getWorkerId());
                    workRecordData.setAccompanyUserName(crmWorkRecord.getWorkerName());
                }
                workRecordData.setBusinessRecordId(workRecordId);
                workRecordData.setIsOldBusinessRecordId(true);
                workRecordData.setWorkTime(item.getWorkTime());
                AgentVisitWorkTitleType visitWorkTitleType = AgentVisitWorkTitleType.of(item.getWorkTitle());
                if (visitWorkTitleType != null){
                    workRecordData.setPurpose(visitWorkTitleType.getWorkTitle());
                }
                workRecordData.setPhotoUrls(Collections.singletonList(item.getImgUrl()));
                workRecordData.setAddress(item.getAddress());
                //评价
                Map<EvaluationIndicator,Integer> evaluationMap = new HashMap<>();
                evaluationMap.put(EvaluationIndicator.PREPARATION_SCORE,item.getPreparationScore());
                evaluationMap.put(EvaluationIndicator.PRODUCT_PROFICIENCY_SCORE,item.getProductProficiencyScore());
                evaluationMap.put(EvaluationIndicator.RESULT_MEET_EXPECTED_RESULT_SCORE,item.getResultMeetExpectedResultScore());
                workRecordData.setEvaluationMap(evaluationMap);

                workRecordData.setResult(item.getPartnerSuggest());

            }
            workRecordDataList.add(workRecordData);
        });
        return workRecordDataList;
    }

    /**
     * 转化新的工作记录为工作数据（WorkRecordData）列表
     * @param userIds
     * @param startDate
     * @param endDate
     * @return
     */
    public List<WorkRecordData> getNewWorkRecordDataListByUserTypeTime(Collection<Long> userIds, AgentWorkRecordType workRecordType, Date startDate, Date endDate){
        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        List<Future<List<WorkRecordData>>> futureList = new ArrayList<>();
        //进校
        if (workRecordType == null || workRecordType == AgentWorkRecordType.SCHOOL){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getNewIntoSchoolWorkRecordDataList(userIds,startDate,endDate)));
        }
        //组会
        if (workRecordType == null || workRecordType == AgentWorkRecordType.MEETING){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getNewMeetingWorkRecordDataList(userIds,startDate,endDate)));
        }
        //资源拓维
        if (workRecordType == null || workRecordType == AgentWorkRecordType.RESOURCE_EXTENSION){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getNewResourceExtensionWorkRecordDataList(userIds,startDate,endDate)));
        }
        //陪同
        if (workRecordType == null || workRecordType == AgentWorkRecordType.ACCOMPANY){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getNewAccompanyWorkRecordDataList(userIds,startDate,endDate)));
        }
        for(Future<List<WorkRecordData>> future : futureList) {
            try {
                List<WorkRecordData> subList = future.get();
                if(CollectionUtils.isNotEmpty(subList)){
                    workRecordDataList.addAll(subList);
                }
            }catch (Exception e){
                String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                logger.error(methodName + " error", e);
            }
        }
        Collections.sort(workRecordDataList, (o1, o2) -> o2.getWorkTime().compareTo(o1.getWorkTime()));
        return workRecordDataList;
    }


    /**
     * 获取新工作数据列表-进校
     * @param userIds
     * @param startDate
     * @param endDate
     * @return
     */
    public List<WorkRecordData> getNewIntoSchoolWorkRecordDataList(Collection<Long> userIds, Date startDate, Date endDate){
        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        List<WorkRecordSchool> workRecordSchoolList = workRecordSchoolLoaderClient.findByWorkersAndTime(userIds, startDate, endDate);
        workRecordDataList.addAll(transformNewIntoSchoolToWorkRecordDataList(workRecordSchoolList));
        return workRecordDataList;
    }

    /**
     * 转化新工作记录为工作数据（WorkRecordData）列表-进校
     * @param workRecordSchoolList
     * @return
     */
    public List<WorkRecordData> transformNewIntoSchoolToWorkRecordDataList(Collection<WorkRecordSchool> workRecordSchoolList){
        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        if (CollectionUtils.isEmpty(workRecordSchoolList)){
            return workRecordDataList;
        }
        List<List<String>> teacherRecordIdList = workRecordSchoolList.stream().filter(p -> CollectionUtils.isNotEmpty(p.getTeacherRecordList())).map(WorkRecordSchool::getTeacherRecordList).collect(Collectors.toList());
        Set<String> teacherRecordIds = teacherRecordIdList.stream().flatMap(List::stream).collect(Collectors.toSet());
        Map<String, WorkRecordTeacher> allWorkRecordTeacherMap = workRecordTeacherLoaderClient.loads(teacherRecordIds);

        Map<Long, WorkRecordTeacher> allTeacherWorkRecordMap = allWorkRecordTeacherMap.values().stream().filter(Objects::nonNull).collect(Collectors.toMap(WorkRecordTeacher::getTeacherId, Function.identity(), (o1, o2) -> o1));
        Set<Long> allMainVisitTeacherIds = allTeacherWorkRecordMap.keySet();
        Map<Long, List<Long>> allMainSubTeacherIdMap = teacherLoaderClient.loadSubTeacherIds(allMainVisitTeacherIds);

        Set<Long> allTeacherIds = new HashSet<>();
        
        //获取每个主账号对应的副账号
        Set<Long> allSubVisitTeacherIds = new HashSet<>();
        allMainSubTeacherIdMap.forEach((k,v) -> {
            allSubVisitTeacherIds.addAll(v);

            allTeacherIds.add(k);
            allTeacherIds.addAll(v);
        });
        Map<Long, Long> allSubMainTeacherIdMap = teacherLoaderClient.loadMainTeacherIds(allSubVisitTeacherIds);

        Map<Long, Teacher> allTeacherMap = teacherLoaderClient.loadTeachers(allTeacherIds);

        //上层资源
        Set<String> allOuterResourceRecordIds = workRecordSchoolList.stream().filter(p -> CollectionUtils.isNotEmpty(p.getOuterResourceRecordList())).map(WorkRecordSchool::getOuterResourceRecordList).flatMap(List::stream).collect(Collectors.toSet());
        Map<String, WorkRecordVisitUserInfo> allOuterResourceRecordMap = generateOuterResourceVisitInfo(allOuterResourceRecordIds);

        workRecordSchoolList.forEach(item -> {
            WorkRecordData workRecordData = new WorkRecordData();
            workRecordData.setId(item.getId());
            workRecordData.setUserId(item.getUserId());
            workRecordData.setUserName(item.getUserName());
            workRecordData.setWorkType(AgentWorkRecordType.SCHOOL);
            workRecordData.setSchoolId(item.getSchoolId());
            workRecordData.setSchoolName(item.getSchoolName());
            List<WorkRecordVisitUserInfo> visitUserInfoList = new ArrayList<>();
            //拜访老师记录
            List<String> schoolTeacherRecordIds = item.getTeacherRecordList();
            if (CollectionUtils.isNotEmpty(schoolTeacherRecordIds)){
                Map<String, WorkRecordTeacher> workRecordTeacherMap = new HashMap<>();
                schoolTeacherRecordIds.forEach(recordId -> {
                    WorkRecordTeacher workRecordTeacher = allWorkRecordTeacherMap.get(recordId);
                    if (workRecordTeacher != null){
                        workRecordTeacherMap.put(recordId,workRecordTeacher);
                    }
                });
                if (MapUtils.isNotEmpty(workRecordTeacherMap)){
                    Map<Long, WorkRecordTeacher> teacherWorkRecordMap = workRecordTeacherMap.values().stream().filter(Objects::nonNull).collect(Collectors.toMap(WorkRecordTeacher::getTeacherId, Function.identity(), (o1, o2) -> o1));
                    Set<Long> mainVisitTeacherIds = teacherWorkRecordMap.keySet();

                    //获取主副账号对应关系
                    Map<Long, List<Long>> mainSubTeacherIdMap = new HashMap<>();
                    mainVisitTeacherIds.forEach(p -> {
                        mainSubTeacherIdMap.put(p,allMainSubTeacherIdMap.get(p));
                    });

                    //获取每个主账号对应的副账号
                    Set<Long> subVisitTeacherIds = new HashSet<>();
                    mainSubTeacherIdMap.forEach((k,v) -> {
                        subVisitTeacherIds.addAll(v);
                    });
                    Set<Long> teacherIds = new HashSet<>();
                    teacherIds.addAll(mainVisitTeacherIds);
                    teacherIds.addAll(subVisitTeacherIds);
                    Map<Long, Long> subMainTeacherIdMap = new HashMap<>();
                    subVisitTeacherIds.forEach(p -> {
                        subMainTeacherIdMap.put(p,allSubMainTeacherIdMap.get(p));
                    });

                    List<Teacher> teacherList = new ArrayList<>();
                    teacherIds.forEach(p -> {
                        teacherList.add(allTeacherMap.get(p));
                    });
                    visitUserInfoList.addAll(workRecordService.getVisitUserList(teacherList,teacherWorkRecordMap,subMainTeacherIdMap));
                }
            }
            //拜访上层资源记录
            List<String> outerResourceRecordIds = item.getOuterResourceRecordList();
            outerResourceRecordIds.forEach(p -> {
                WorkRecordVisitUserInfo workRecordVisitUserInfo = allOuterResourceRecordMap.get(p);
                if (workRecordVisitUserInfo != null){
                    visitUserInfoList.add(workRecordVisitUserInfo);
                }
            });
            workRecordData.setVisitUserInfoList(visitUserInfoList);
            workRecordData.setVisitSchoolType(item.getVisitType());
            transformNewSignInInfo(item.getSignInRecordId(),workRecordData);
            workRecordData.setResult(item.getResult());
            workRecordData.setMeetingForm(item.getMeetingForm());
            workRecordData.setLecturerName(item.getLecturerName());
            workRecordData.setResult(item.getResult());
            workRecordData.setPhotoUrls(item.getPhotoUrls());
            workRecordData.setWorkTime(item.getWorkTime());
            AgentSchoolVisitTheme agentSchoolVisitTheme = AgentSchoolVisitTheme.nameOf(item.getTitle());
            if (agentSchoolVisitTheme != null){
                workRecordData.setWorkTitle(agentSchoolVisitTheme.getDesc());
            }
            workRecordDataList.add(workRecordData);
        });
        return workRecordDataList;
    }

    /**
     * 获取新工作数据列表-组会
     * @param userIds
     * @param startDate
     * @param endDate
     * @return
     */
    public List<WorkRecordData> getNewMeetingWorkRecordDataList(Collection<Long> userIds, Date startDate, Date endDate){
        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        List<WorkRecordMeeting> workRecordMeetingList = workRecordMeetingLoaderClient.findByWorkersAndTime(userIds, startDate, endDate);
        workRecordDataList.addAll(transformNewMeetingToWorkRecordDataList(workRecordMeetingList));
        return workRecordDataList;
    }

    /**
     * 转化新工作记录为工作数据（WorkRecordData）列表-组会
     * @param workRecordMeetingList
     * @return
     */
    public List<WorkRecordData> transformNewMeetingToWorkRecordDataList(Collection<WorkRecordMeeting> workRecordMeetingList){
        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        if (CollectionUtils.isEmpty(workRecordMeetingList)){
            return workRecordDataList;
        }
        Set<String> allSupporterRecordIds = workRecordMeetingList.stream().filter(p -> CollectionUtils.isNotEmpty(p.getSupporterRecordList())).map(WorkRecordMeeting::getSupporterRecordList).flatMap(List::stream).collect(Collectors.toSet());
        Map<String, WorkSupporter> allSupporterMap = workSupporterLoaderClient.loads(allSupporterRecordIds);

        Set<Long> allSupporterIds = new ArrayList<>(allSupporterMap.values()).stream().map(WorkSupporter::getSupporterId).collect(Collectors.toSet());

        Map<Long, Map<String, Object>> outerResourceMap = agentOuterResourceService.getResourceInfoByIds(allSupporterIds);

        workRecordMeetingList.forEach(item -> {
            WorkRecordData workRecordData = new WorkRecordData();
            workRecordData.setWorkType(AgentWorkRecordType.MEETING);
            workRecordData.setId(item.getId());
            workRecordData.setUserId(item.getUserId());
            workRecordData.setUserName(item.getUserName());
            workRecordData.setWorkTime(item.getWorkTime());
            workRecordData.setWorkTitle(item.getTitle());
            workRecordData.setMeetingType(item.getMeetingType());
            workRecordData.setMeetingTime(item.getPreachingTime());
            workRecordData.setMeetingCount(item.getAttendances());
            transformNewSignInInfo(item.getSignInRecordId(),workRecordData);
            workRecordData.setPhotoUrls(item.getPhotoUrls());
            workRecordData.setLecturerName(item.getLecturerName());
            //外部支持记录
            Map<String, WorkSupporter> workSupporterMap = new HashMap<>();
            List<String> supporterRecordList = item.getSupporterRecordList();
            if (CollectionUtils.isNotEmpty(supporterRecordList)){
                supporterRecordList.forEach(p -> {
                    WorkSupporter workSupporter = allSupporterMap.get(p);
                    if (workSupporter != null){
                        workSupporterMap.put(p,workSupporter);
                    }
                });
            }

            List<WorkRecordVisitUserInfo> workRecordVisitUserInfoList = new ArrayList<>();
            workSupporterMap.forEach((k,v) -> {
                WorkRecordVisitUserInfo workRecordVisitUserInfo = new WorkRecordVisitUserInfo();
                workRecordVisitUserInfo.setId(v.getSupporterId());
                workRecordVisitUserInfo.setName(v.getSupporterName());
                Map<String, Object> resourceInfoMap = outerResourceMap.get(v.getSupporterId());
                if (MapUtils.isNotEmpty(resourceInfoMap)){
                    workRecordVisitUserInfo.setJob(SafeConverter.toInt(resourceInfoMap.get("job")));
                    workRecordVisitUserInfo.setJobName(SafeConverter.toString(resourceInfoMap.get("jobName")));
                }
                workRecordVisitUserInfoList.add(workRecordVisitUserInfo);
                workRecordData.setIsPresent(v.getIsPresent());
            });
            workRecordData.setVisitUserInfoList(workRecordVisitUserInfoList);
            workRecordData.setResult(item.getResult());
            workRecordDataList.add(workRecordData);
        });
        return workRecordDataList;
    }

    /**
     * 获取新工作数据列表-资源拓维
     * @param userIds
     * @param startDate
     * @param endDate
     * @return
     */
    public List<WorkRecordData> getNewResourceExtensionWorkRecordDataList(Collection<Long> userIds, Date startDate, Date endDate){
        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        List<WorkRecordResourceExtension> workRecordResourceExtensionList = workRecordResourceExtensionLoaderClient.loadByWorkersAndTime(userIds, startDate, endDate);
        workRecordDataList.addAll(transformNewResourceExtensionToWorkRecordDataList(workRecordResourceExtensionList));
        return workRecordDataList;
    }

    /**
     * 转化新工作记录为工作数据（WorkRecordData）列表-资源拓维
     * @param workRecordResourceExtensionList
     * @return
     */
    public List<WorkRecordData> transformNewResourceExtensionToWorkRecordDataList(Collection<WorkRecordResourceExtension> workRecordResourceExtensionList){
        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        if (CollectionUtils.isEmpty(workRecordResourceExtensionList)){
            return workRecordDataList;
        }

        Set<String> allTeacherRecordIds = workRecordResourceExtensionList.stream().filter(p -> CollectionUtils.isNotEmpty(p.getTeacherRecordIds())).map(WorkRecordResourceExtension::getTeacherRecordIds).flatMap(List::stream).collect(Collectors.toSet());
        Map<String, WorkRecordTeacher> allWorkRecordTeacherMap = workRecordTeacherLoaderClient.loads(allTeacherRecordIds);

        Map<Long, String> allTeacherResultMap = allWorkRecordTeacherMap.values().stream().filter(Objects::nonNull).collect(Collectors.toMap(WorkRecordTeacher::getTeacherId, WorkRecordTeacher::getResult, (o1, o2) -> o1));
        //获取主副账号对应关系
        Map<Long, List<Long>> allMainSubTeacherIdMap = teacherLoaderClient.loadSubTeacherIds(allTeacherResultMap.keySet());

        Set<Long> allTeacherIds = new HashSet<>();
        allMainSubTeacherIdMap.forEach((k,v) -> {
            allTeacherIds.add(k);
            allTeacherIds.addAll(v);
        });

        Map<Long, Teacher> allTeacherMap = teacherLoaderClient.loadTeachers(allTeacherIds);

        workRecordResourceExtensionList.forEach(item -> {
            WorkRecordData workRecordData = new WorkRecordData();
            List<WorkRecordVisitUserInfo> visitUserInfoList = new ArrayList<>();
            //拜访老师记录
            List<String> teacherRecordIds = item.getTeacherRecordIds();
            Set<Long> mainTeacherIds = new HashSet<>();
            if (CollectionUtils.isNotEmpty(teacherRecordIds)){
                teacherRecordIds.forEach(p -> {
                    WorkRecordTeacher workRecordTeacher = allWorkRecordTeacherMap.get(p);
                    if (workRecordTeacher != null){
                        mainTeacherIds.add(workRecordTeacher.getTeacherId());
                    }
                });

                //获取每个主账号对应的副账号
                Set<Long> subVisitTeacherIds = new HashSet<>();
                //获取主副账号对应关系
                mainTeacherIds.forEach(p -> {
                    subVisitTeacherIds.addAll(allMainSubTeacherIdMap.get(p));
                });
                Set<Long> teacherIds = new HashSet<>();
                teacherIds.addAll(mainTeacherIds);
                teacherIds.addAll(subVisitTeacherIds);
                Map<Long, Teacher> teacherMap = new HashMap<>();
                teacherIds.forEach(p -> {
                    teacherMap.put(p,allTeacherMap.get(p));
                });
                if (MapUtils.isNotEmpty(teacherMap)){
                    teacherMap.forEach((k,v) -> {
                        WorkRecordVisitUserInfo visitUserInfo = new WorkRecordVisitUserInfo();
                        visitUserInfo.setId(k);
                        visitUserInfo.setName(v.getProfile() != null ? v.getProfile().getRealname() : "");
                        visitUserInfo.setJob(WorkRecordVisitUserInfo.TEACHER_JOB);
                        visitUserInfo.setJobName(WorkRecordVisitUserInfo.TEACHER_JOB_NAME);
                        visitUserInfo.setSubject(v.getSubject());
                        visitUserInfo.setSubjectName(v.getSubject() != null ? v.getSubject().getValue() : "");
                        visitUserInfo.setResult(SafeConverter.toString(allTeacherResultMap.get(k)));
                        visitUserInfoList.add(visitUserInfo);
                    });
                }

            }

            workRecordData.setId(item.getId());
            //拜访上层资源记录
            Map<String, WorkRecordVisitUserInfo> workRecordVisitUserInfoMap = generateOuterResourceVisitInfo(item.getOuterResourceRecordIds());
            if (MapUtils.isNotEmpty(workRecordVisitUserInfoMap)){
                visitUserInfoList.addAll(new ArrayList<>(workRecordVisitUserInfoMap.values()));
            }
            transformNewSignInInfo(item.getSignInRecordId(),workRecordData);
            workRecordData.setContent(item.getContent());
            workRecordData.setWorkType(AgentWorkRecordType.RESOURCE_EXTENSION);
            workRecordData.setVisitUserInfoList(visitUserInfoList);
            workRecordData.setVisitIntention(item.getVisitIntention());
            workRecordData.setWorkTime(item.getWorkTime());
            workRecordData.setUserId(item.getUserId());
            workRecordData.setUserName(item.getUserName());
            workRecordDataList.add(workRecordData);
        });
        return workRecordDataList;
    }

    public Map<String,WorkRecordVisitUserInfo> generateOuterResourceVisitInfo(Collection<String> outerResourceRecordIds){
        Map<String,WorkRecordVisitUserInfo> visitUserInfoMap = new HashMap<>();
        if (CollectionUtils.isEmpty(outerResourceRecordIds)){
            return visitUserInfoMap;
        }
        Map<String, WorkRecordOuterResource> workRecordOuterResourceMap = workRecordOuterResourceLoaderClient.loads(outerResourceRecordIds);
        if (MapUtils.isNotEmpty(workRecordOuterResourceMap)){
            //获取上层资源信息
            Set<Long> outerResourceIds = workRecordOuterResourceMap.values().stream().map(WorkRecordOuterResource::getOuterResourceId).collect(Collectors.toSet());
            Map<Long, AgentOuterResourceExtend> outerResourceExtendMap = agentOuterResourceExtendDao.findListByOuterResourceIds(outerResourceIds);
            //获取机构信息
            Set<Long> organizationIds = outerResourceExtendMap.values().stream().map(AgentOuterResourceExtend::getOrganizationId).collect(Collectors.toSet());
            Map<Long, AgentOrganization> organizationMap = agentOrganizationPersistence.loads(organizationIds);
            workRecordOuterResourceMap.forEach((k,v) -> {
                WorkRecordVisitUserInfo visitUserInfo = new WorkRecordVisitUserInfo();
                visitUserInfo.setId(v.getOuterResourceId());
                visitUserInfo.setName(v.getOuterResourceName());
                AgentOuterResourceExtend outerResourceExtend = outerResourceExtendMap.get(v.getOuterResourceId());
                if (outerResourceExtend != null) {
                    visitUserInfo.setJob(outerResourceExtend.getJob());
                    ResearchersJobType researchersJobType = ResearchersJobType.typeOf(outerResourceExtend.getJob());
                    if (researchersJobType != null) {
                        visitUserInfo.setJobName(researchersJobType.getJobName());
                    }
                    visitUserInfo.setSubject(outerResourceExtend.getSubject());
                    visitUserInfo.setSubjectName(outerResourceExtend.getSubject() != null ? outerResourceExtend.getSubject().getValue() :"");

                    AgentOrganization organization = organizationMap.get(outerResourceExtend.getOrganizationId());
                    if (organization != null){
                        AgentRegionRank regionRank = organization.getRegionRank();
                        if (regionRank == AgentRegionRank.PROVINCE){
                            visitUserInfo.setRegionName(organization.getProvinceName());
                        }else if (regionRank == AgentRegionRank.CITY){
                            visitUserInfo.setRegionName(organization.getCityName());
                        }else if (regionRank == AgentRegionRank.COUNTY){
                            visitUserInfo.setRegionName(organization.getCountyName());
                        }else if (regionRank == AgentRegionRank.COUNTRY){
                            visitUserInfo.setRegionName("全国");
                        }
                    }
                }
                visitUserInfo.setResult(v.getResult());
                visitUserInfoMap.put(k,visitUserInfo);
            });
        }
        return visitUserInfoMap;
    }

    /**
     * 获取新工作数据列表-陪同
     * @param userIds
     * @param startDate
     * @param endDate
     * @return
     */
    public List<WorkRecordData> getNewAccompanyWorkRecordDataList(Collection<Long> userIds, Date startDate, Date endDate){
        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        List<WorkRecordAccompany> workRecordAccompanyList = workRecordAccompanyLoaderClient.loadByWorkersAndTime(userIds, startDate, endDate);
        workRecordDataList.addAll(transformNewAccompanyToWorkRecordDataList(workRecordAccompanyList));
        return workRecordDataList;
    }

    /**
     * 转化新工作记录为工作数据（WorkRecordData）列表-陪同
     * @param workRecordAccompanyList
     * @return
     */
    public List<WorkRecordData> transformNewAccompanyToWorkRecordDataList(Collection<WorkRecordAccompany> workRecordAccompanyList){
        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        if (CollectionUtils.isEmpty(workRecordAccompanyList)){
            return workRecordDataList;
        }

        Set<String> allEvaluationRecordIds = workRecordAccompanyList.stream().filter(p -> CollectionUtils.isNotEmpty(p.getEvaluationRecordList())).map(WorkRecordAccompany::getEvaluationRecordList).flatMap(List::stream).collect(Collectors.toSet());
        Map<String, EvaluationRecord> allEvaluationRecordMap = evaluationRecordLoaderClient.loads(allEvaluationRecordIds);

        Set<String> schoolBusinessRecordIds = new HashSet<>();
        Set<String> meetingBusinessRecordIds = new HashSet<>();
        Set<String> reBusinessRecordIds = new HashSet<>();
        workRecordAccompanyList.forEach(p -> {
            if (p.getBusinessType() == AccompanyBusinessType.SCHOOL){
                schoolBusinessRecordIds.add(p.getBusinessRecordId());
            }else if (p.getBusinessType() == AccompanyBusinessType.MEETING){
                meetingBusinessRecordIds.add(p.getBusinessRecordId());
            }else if (p.getBusinessType() == AccompanyBusinessType.RESOURCE_EXTENSION){
                reBusinessRecordIds.add(p.getBusinessRecordId());
            }
        });
        Map<String, WorkRecordSchool> schoolBusinessRecordMap = workRecordSchoolLoaderClient.loads(schoolBusinessRecordIds);
        Map<String, WorkRecordMeeting> meetingBusinessRecordMap = workRecordMeetingLoaderClient.loads(meetingBusinessRecordIds);
        Map<String, WorkRecordResourceExtension> reBusinessRecordMap = workRecordResourceExtensionLoaderClient.loads(reBusinessRecordIds);

        workRecordAccompanyList.forEach(item -> {
            WorkRecordData workRecordData = new WorkRecordData();
            workRecordData.setId(item.getId());
            workRecordData.setWorkType(AgentWorkRecordType.ACCOMPANY);
            workRecordData.setUserId(item.getUserId());
            workRecordData.setUserName(item.getUserName());
            AccompanyBusinessType businessType = item.getBusinessType();
            String businessRecordId = item.getBusinessRecordId();
            workRecordData.setBusinessType(businessType);
            workRecordData.setBusinessRecordId(businessRecordId);
            workRecordData.setIsOldBusinessRecordId(false);
            workRecordData.setWorkTime(item.getWorkTime());
            AgentVisitWorkTitleType visitWorkTitleType = AgentVisitWorkTitleType.nameOf(item.getPurpose());
            if (visitWorkTitleType != null){
                workRecordData.setPurpose(visitWorkTitleType.getWorkTitle());
            }
            workRecordData.setPhotoUrls(item.getPhotoUrls());
            //评价
            Map<EvaluationIndicator,Integer> evaluationMap = new HashMap<>();
            Map<String, EvaluationRecord> evaluationRecordMap = new HashMap<>();
            List<String> evaluationRecordIds = item.getEvaluationRecordList();
            if (CollectionUtils.isNotEmpty(evaluationRecordIds)){
                evaluationRecordIds.forEach(p -> {
                    EvaluationRecord evaluationRecord = allEvaluationRecordMap.get(p);
                    if (evaluationRecord != null){
                        evaluationRecordMap.put(p,evaluationRecord);
                    }
                });
                evaluationRecordMap.forEach((k,v) -> {
                    evaluationMap.put(v.getIndicator(),v.getResult());
                });
            }
            workRecordData.setEvaluationMap(evaluationMap);
            workRecordData.setResult(item.getResult());
            workRecordData.setWorkTime(item.getWorkTime());
            //被陪同人信息
            if (businessType == AccompanyBusinessType.SCHOOL){
                WorkRecordSchool workRecordSchool = schoolBusinessRecordMap.get(businessRecordId);
                if (workRecordSchool != null){
                    workRecordData.setAccompanyUserId(workRecordSchool.getUserId());
                    workRecordData.setAccompanyUserName(workRecordSchool.getUserName());
                }
            }else if (businessType == AccompanyBusinessType.MEETING){
                WorkRecordMeeting workRecordMeeting = meetingBusinessRecordMap.get(businessRecordId);
                if (workRecordMeeting != null){
                    workRecordData.setAccompanyUserId(workRecordMeeting.getUserId());
                    workRecordData.setAccompanyUserName(workRecordMeeting.getUserName());
                }
            }else if (businessType == AccompanyBusinessType.RESOURCE_EXTENSION){
                WorkRecordResourceExtension resourceExtension = reBusinessRecordMap.get(businessRecordId);
                if (resourceExtension != null){
                    workRecordData.setAccompanyUserId(resourceExtension.getUserId());
                    workRecordData.setAccompanyUserName(resourceExtension.getUserName());
                }
            }
            //签到信息
            transformNewSignInInfo(item.getSignInRecordId(),workRecordData);
            workRecordDataList.add(workRecordData);
        });
        return workRecordDataList;
    }

    /**
     * 转化旧数据签到信息
     * @param workRecord
     * @param workRecordData
     */
    public void  transformOldSignInInfo(CrmWorkRecord workRecord,WorkRecordData workRecordData){
        Integer signType = workRecord.getSignType();
        if (signType != null){
            if (signType == 1){
                workRecordData.setSignInType(SignInType.GPS);
            }else if (signType == 2){
                workRecordData.setSignInType(SignInType.PHOTO);
            }
        }
        workRecordData.setCoordinateType(workRecord.getCoordinateType());
        workRecordData.setLatitude(workRecord.getLatitude());
        workRecordData.setLongitude(workRecord.getLongitude());
        workRecordData.setPhotoUrl(workRecord.getSchoolPhotoUrl());
        workRecordData.setAddress(workRecord.getAddress() != null ? workRecord.getAddress() : (workRecord.getVisitedPlace() != null ? workRecord.getVisitedPlace() : ""));
    }

    /**
     * 转化新签到信息
     * @param signInRecordId
     * @param workRecordData
     */
    public void transformNewSignInInfo(String signInRecordId, WorkRecordData workRecordData){
        SignInRecord signInRecord = signInRecordLoaderClient.load(signInRecordId);
        if (signInRecord != null){
            workRecordData.setSignInType(signInRecord.getSignInType());
            workRecordData.setCoordinateType(signInRecord.getCoordinateType());
            workRecordData.setLatitude(signInRecord.getLatitude());
            workRecordData.setLongitude(signInRecord.getLongitude());
            workRecordData.setPhotoUrl(signInRecord.getPhotoUrl());
            workRecordData.setAddress(signInRecord.getAddress());
        }
    }

    /**
     * 获取工作量T
     * @param workRecordId
     * @param workRecordType
     * @return
     */
    public AgentRecordWorkload getWorkload(String workRecordId, AgentWorkRecordType workRecordType) {
        //工作量T历史数据
        AgentRecordWorkload workloadOld = agentRecordWorkloadDao.load(workRecordId);
        if (workloadOld != null){
            return workloadOld;
        }else {
            //工作量T新数据
            AgentRecordWorkload workloadNew = agentRecordWorkloadDao.loadByWorkRecordIdAndType(workRecordId,workRecordType);
            if (workloadNew != null){
                return workloadNew;
            }
        }
        return null;
    }
}
