package com.voxlearning.utopia.agent.service.exam;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.athena.bean.bigexam.*;
import com.voxlearning.utopia.agent.bean.AgentSchoolDictData;
import com.voxlearning.utopia.agent.bean.exam.*;
import com.voxlearning.utopia.agent.constants.AgentLargeExamContractType;
import com.voxlearning.utopia.agent.constants.AgentUserOperationType;
import com.voxlearning.utopia.agent.dao.mongo.AgentUserOperationRecordDao;
import com.voxlearning.utopia.agent.dao.mongo.exam.AgentExamContractExtendDao;
import com.voxlearning.utopia.agent.persist.entity.AgentUserOperationRecord;
import com.voxlearning.utopia.agent.persist.entity.exam.*;
import com.voxlearning.utopia.agent.persist.exam.*;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.mobile.resource.SchoolResourceService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.service.useroperationrecord.AgentUserOperationRecordService;
import com.voxlearning.utopia.agent.support.AgentGroupSupport;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPopularityType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceRange;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserSchoolLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.ArtScienceType;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * 大考相关的的服务
 *
 * @author chunlin.yu
 * @create 2018-03-13 13:27
 **/

@Named
public class AgentLargeExamService extends AbstractAgentService {
    @Inject
    private AgentExamContractPersistence agentExamContractPersistence;

    @Inject
    private AgentExamSchoolPersistence agentExamSchoolPersistence;

    @Inject
    private AgentExamSubjectPersistence agentExamSubjectPersistence;

    @Inject
    private AgentExamPaperPersistence agentExamPaperPersistence;

    @Inject
    private AgentExamGradePersistence agentExamGradePersistence;

    @Inject
    private AgentDictSchoolService agentDictSchoolService;

    @Inject
    private BaseOrgService baseOrgService;

    @Inject
    private SchoolResourceService schoolResourceService;

    @Inject
    private SchoolLoaderClient schoolLoaderClient;

    @Inject
    private AgentKlxScanPaperPersistence agentKlxScanPaperPersistence;

    @Inject
    private BaseUserService baseUserService;


    @Inject
    private AgentGroupUserLoaderClient agentGroupUserLoaderClient;

    @Inject
    private AgentUserSchoolLoaderClient agentUserSchoolLoaderClient;

    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;

    @Inject
    private AgentExamContractExtendDao agentExamContractExtendDao;

    @Inject
    private AgentUserLoaderClient agentUserLoaderClient;

    @Inject
    private AgentExamContractPaybackPersistence agentExamContractPaybackPersistence;

    @Inject
    private AgentUserOperationRecordDao agentUserOperationRecordDao;
    @Inject
    private SearchService searchService;
    @Inject
    private AgentGroupSupport agentGroupSupport;
    @Inject
    private AgentUserOperationRecordService agentUserOperationRecordService;

    public Long addOrUpdateLargeExamContract(AgentExamContractVO agentLargeExamContractVO) {
        AgentExamContract contract = agentLargeExamContractVO.toAgentLargeExamContract();
        if (contract.getId() != null) {
            //操作日志
            addOrUpdateExamContractOperationRecord(contract,"edit");
            agentExamContractPersistence.replace(contract);
        } else {
            AgentExamContract agentExamContract = agentExamContractPersistence.loadBySchoolId(contract.getSchoolId());
            if (null != agentExamContract) {
                deleteExamContract(agentExamContract.getId());
            }
            agentExamContractPersistence.insert(contract);
            //操作日志
            addOrUpdateExamContractOperationRecord(contract,"add");
        }

        return contract.getId();
    }



    /**
     * 学校检索,只查询初高中的学校
     *
     * @param userId
     * @param schoolKey
     * @return
     */
    public Collection<School> searchSchool(Long userId, String schoolKey) {
//        List<Long> searchSchoolIds = schoolResourceService.loadBusinessSchoolByScene(userId, schoolKey, "space");
        List<Long> searchSchoolIds =  searchService.searchSchoolsForSceneWithNew(userId, schoolKey, SearchService.SCENE_DICT);
        List<AgentDictSchool> dictSchoolList = agentDictSchoolService.loadSchoolDictDataBySchool(searchSchoolIds);
        searchSchoolIds = dictSchoolList.stream().filter(AgentDictSchool::isKlxModeSchool).limit(30).map(AgentDictSchool::getSchoolId).collect(toList());

        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(searchSchoolIds)
                .getUninterruptibly();
        return schoolMap.values();
    }

    /**
     * 检索合同范围内的学校
     *
     * @param userId
     * @param schoolKey
     * @return
     */
    public Collection<School> searchSchoolWithinContract(Long userId, String schoolKey) {
        List<Long> searchSchoolIds = new ArrayList<>();
        if (StringUtils.isEmpty(schoolKey)) {
            searchSchoolIds = baseOrgService.getManagedSchoolList(userId);
        } else {

//            List<Long> searchSchoolIdsTemp = schoolResourceService.loadBusinessSchoolByScene(userId, schoolKey, "space");
//            List<Long> managedSchoolList = baseOrgService.getManagedSchoolList(userId);
//            searchSchoolIds = searchSchoolIdsTemp.stream().filter(item -> managedSchoolList.contains(item)).collect(toList());
            searchSchoolIds = searchService.searchSchoolsForSceneWithNew(userId, schoolKey, SearchService.SCENE_DICT);
        }
        List<AgentExamContract> agentExamContracts = agentExamContractPersistence.loadBySchoolIds(searchSchoolIds);
        searchSchoolIds = agentExamContracts.stream().limit(30).map(AgentExamContract::getSchoolId).collect(toList());
        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(searchSchoolIds)
                .getUninterruptibly();
        return schoolMap.values();
    }


    /**
     * 删除exam
     *
     * @param contractId
     */
    public void deleteExamContract(Long contractId) {
        //删除合同扩展信息
        AgentExamContractExtend examContractExtend = agentExamContractExtendDao.loadByContractId(contractId);
        if (null != examContractExtend){
            examContractExtend.setDisabled(true);
            agentExamContractExtendDao.replace(examContractExtend);
        }

        //删除合同回款信息
        agentExamContractPaybackPersistence.deleteByContractId(contractId);

        //删除合同信息
        AgentExamContract examContract = agentExamContractPersistence.load(contractId);
        if (null != examContract) {
            examContract.setDisabled(true);
            agentExamContractPersistence.replace(examContract);
        }
    }

    public AgentExamContractVO getAgentExamContract(Long contractId) {
        AgentExamContract agentExamContract = agentExamContractPersistence.load(contractId);
        if (null != agentExamContract) {
            return toAgentExamContractVO(Collections.singleton(agentExamContract)).stream().findFirst().orElse(null);
        }
        return null;
    }

    public AgentExamContractVO getAgentExamContractBySchoolId(Long schoolId) {
        AgentExamContract agentExamContract = agentExamContractPersistence.loadBySchoolId(schoolId);
        if (null != agentExamContract) {
            return toAgentExamContractVO(Collections.singleton(agentExamContract)).stream().findFirst().orElse(null);
        }
        return null;
    }

    private List<AgentExamContractVO> toAgentExamContractVO(Collection<AgentExamContract> agentExamContract) {
        List<AgentExamContractVO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(agentExamContract)) {
            return resultList;
        }
        Map<Long, CrmSchoolSummary> crmSchoolSummaryMap = agentDictSchoolService.batchLoadCrmSchoolSummaryAndSchool(agentExamContract.stream().filter(p -> null != p.getSchoolId()).map(AgentExamContract::getSchoolId).collect(Collectors.toSet()));


        List<AgentDictSchool> dictSchoolList = agentDictSchoolService.loadSchoolDictDataBySchool(agentExamContract.stream().filter(p -> null != p.getSchoolId()).map(AgentExamContract::getSchoolId).collect(Collectors.toSet()));
        Map<Long, AgentDictSchool> dictSchoolMap = dictSchoolList.stream().collect(Collectors.toMap(AgentDictSchool::getSchoolId, Function.identity()));

        Set<Long> contractIds = agentExamContract.stream().map(AgentExamContract::getId).collect(Collectors.toSet());
        Map<Long, AgentExamContractExtend> contractExtendMap = agentExamContractExtendDao.loadByContractIds(contractIds);

        //拼装签约人信息
        List<AgentExamContractSplitSetting> contractSplitSettingList = new ArrayList<>();
        contractExtendMap.values().forEach(item -> {
            if (null != item){
                contractSplitSettingList.addAll(item.getSplitSettingList());
            }
        });
        //过滤出主签约人信息
        Set<Long> contractorIds = contractSplitSettingList.stream().filter(item -> null != item && Objects.equals(item.getContractorFlag(), AgentExamContractSplitSetting.MAIN_CONTRACTOR)).map(AgentExamContractSplitSetting::getContractorId).collect(Collectors.toSet());
        List<AgentUser> agentUserList = baseOrgService.getUsers(contractorIds);
        Map<Long, AgentUser> agentUserMap = agentUserList.stream().collect(Collectors.toMap(AgentUser::getId, Function.identity()));

        agentExamContract.forEach(item -> {
            AgentExamContractVO vo = new AgentExamContractVO();
            vo.setId(item.getId());
            vo.setSchoolId(item.getSchoolId());
            vo.setContractType(item.getContractType());
            vo.setContractAmount(item.getContractAmount());
            vo.setBeginDate(item.getBeginDate());
            vo.setEndDate(item.getEndDate());
            //获取主签约人ID
            if (contractExtendMap.containsKey(item.getId())){
                AgentExamContractExtend contractExtend = contractExtendMap.get(item.getId());
                if (null != contractExtend){
                    AgentExamContractSplitSetting contractSplitSetting = contractExtend.getSplitSettingList().stream().filter(p -> null != p && Objects.equals(p.getContractorFlag(), AgentExamContractSplitSetting.MAIN_CONTRACTOR)).findFirst().orElse(null);
                    if (null != contractSplitSetting){
                        vo.setContractorId(contractSplitSetting.getContractorId());
                    }
                }
            }
            vo.setContractDate(item.getContractDate());
            vo.setHardwareCost(item.getHardwareCost());
            vo.setMachinesNum(item.getMachinesNum());
            vo.setMachinesType(item.getMachinesType());
            vo.setRemark(item.getRemark());
            if (null != item.getSchoolId()) {
                CrmSchoolSummary crmSchoolSummary = crmSchoolSummaryMap.get(item.getSchoolId());
                if (null != crmSchoolSummary) {
                    vo.setSchoolName(crmSchoolSummary.getSchoolName());
                }

                //获取学校等级
                AgentDictSchool agentDictSchool = dictSchoolMap.get(item.getSchoolId());
                if (null != agentDictSchool){
                    AgentSchoolPopularityType schoolPopularity = agentDictSchool.getSchoolPopularity();
                    if (null != schoolPopularity){
                        vo.setSchoolPopularityType(schoolPopularity.getLevel());
                    }
                }
                //获取负责学校的专员
                AgentUser agentUser = baseOrgService.getSchoolManager(item.getSchoolId()).stream().findFirst().orElse(null);
                if (null != agentUser){
                    vo.setUserName(agentUser.getRealName());
                }
            }
            if (null != vo.getContractorId()) {
                AgentUser agentUser = agentUserMap.get(vo.getContractorId());
                if (null != agentUser) {
                    vo.setContractorName(agentUser.getRealName());
                }
            }
            vo.setThirdPartyProductCost(item.getThirdPartyProductCost());
            vo.setServiceRange(item.getServiceRange());
            resultList.add(vo);
        });
        resultList.sort((o1, o2) -> {
            return -o1.getContractDate().compareTo(o2.getContractDate());
        });
        return resultList;
    }


    /**
     * 检索合同
     *
     * @param id
     * @param schoolId
     * @param contractorId
     * @param contractType
     * @param beginDate
     * @param endDate
     * @return
     */
    public List<AgentExamContractVO> searchContract(Long id, Long schoolId, Long contractorId, AgentLargeExamContractType contractType, Date beginDate, Date endDate) {
        List<AgentExamContract> agentExamContracts = agentExamContractPersistence.searchContract(id, schoolId, contractorId, contractType, beginDate, endDate);
        return toAgentExamContractVO(agentExamContracts);
    }

    public Map<Integer, String> getSchoolGradeMap(Long schoolId) {
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        Map<Integer, String> gradeMap = new LinkedHashMap<>();
        if (null != school) {
            String eduSystem = schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly();
            EduSystemType eduSystemType = EduSystemType.of(eduSystem);
            if (null != eduSystem) {
                String[] grades = eduSystemType.getCandidateClazzLevel().split(",");
                for (int i = 0; i < grades.length; i++) {
                    int grade = SafeConverter.toInt(grades[i]);
                    ClazzLevel clazzLevel = ClazzLevel.parse(grade);
                    if (null != clazzLevel) {
                        gradeMap.put(clazzLevel.getLevel(), clazzLevel.getDescription());
                    }
                }
            }
        }
        return gradeMap;
    }


    /**
     * 大考按月份查询
     *
     * @param month
     * @return
     */
    public List<AgentExamSchoolVO> getAgentExamSchoolByMonth(int month, Long userId) {
        List<AgentExamSchool> agentExamSchoolList = agentExamSchoolPersistence.loadByMonth(month);
        List<Long> managedSchoolList = baseOrgService.getManagedSchoolList(userId);
        agentExamSchoolList = agentExamSchoolList.stream().filter(item -> managedSchoolList.contains(item.getSchoolId())).collect(toList());
        return toAgentExamSchoolVOList(agentExamSchoolList);
    }

    private List<AgentExamSchoolVO> toAgentExamSchoolVOList(List<AgentExamSchool> agentExamSchoolList) {
        if (CollectionUtils.isEmpty(agentExamSchoolList)) {
            return Collections.emptyList();
        }
        Map<Integer, List<AgentExamSchool>> examSchoolMonthMap = agentExamSchoolList.stream().collect(Collectors.groupingBy(AgentExamSchool::getMonth));

        List<AgentExamGradeVO> agentExamGradeVOList = new ArrayList<>();
        examSchoolMonthMap.forEach((month, list) -> {
            if (CollectionUtils.isNotEmpty(list)) {
                Set<Long> examSchoolIdSet = list.stream().map(AgentExamSchool::getId).collect(Collectors.toSet());
                List<AgentExamGrade> examGradeList = agentExamGradePersistence.loadByExamSchoolIds(examSchoolIdSet);
                List<AgentExamGradeVO> examGradeVOS = toAgentExamGradeVOList(examGradeList, month);
                agentExamGradeVOList.addAll(examGradeVOS);
            }
        });

        Map<Long, List<AgentExamGradeVO>> examSchoolGradeMap = agentExamGradeVOList.stream().collect(Collectors.groupingBy(AgentExamGradeVO::getAgentExamSchoolId, Collectors.toList()));
        List<AgentExamSchoolVO> agentExamSchoolVOList = new ArrayList<>();
        agentExamSchoolList.forEach(item -> {
            AgentExamSchoolVO vo = AgentExamSchoolVO.fromAgentExamSchool(item);
            if (null != vo) {
                List<AgentExamGradeVO> examGradeVOS = examSchoolGradeMap.get(item.getId());
                if (CollectionUtils.isNotEmpty(examGradeVOS)) {
                    examGradeVOS.sort((o1, o2) -> {
                        return o1.getGrade().compareTo(o2.getGrade());
                    });
                    vo.setExamGradeVOList(examGradeVOS);
                }
                agentExamSchoolVOList.add(vo);
            }
        });
        return agentExamSchoolVOList;
    }

    private List<AgentExamGradeVO> toAgentExamGradeVOList(List<AgentExamGrade> examGradeList, Integer month) {
        List<AgentExamGradeVO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(examGradeList)) {
            return Collections.emptyList();
        }
        Set<Long> examGradeIdSet = examGradeList.stream().map(AgentExamGrade::getId).collect(Collectors.toSet());
        Set<String> examGradeIdSetStr = examGradeList.stream().map(item -> String.valueOf(item.getId())).collect(Collectors.toSet());
        List<AgentExamSubject> examSubjectList = agentExamSubjectPersistence.loadByExamGradeIds(examGradeIdSet);
        List<BigExamPostData> bigExamPostData = batchLoadBigExamPostData(examGradeIdSetStr);
        Map<String, BigExamPostData> bigExamPostDataMap = bigExamPostData.stream().collect(Collectors.toMap(BigExamPostData::getExamId, Function.identity(), (o1, o2) -> o2));
        List<AgentExamSubjectVO> examSubjectVOList = toAgentExamSubjectVOList(examSubjectList, bigExamPostDataMap);
        Map<Long, List<AgentExamSubjectVO>> examGradeSubjectMap = examSubjectVOList.stream().collect(Collectors.groupingBy(AgentExamSubjectVO::getAgentExamGradeId));
        examGradeList.forEach(item -> {
            AgentExamGradeVO vo = AgentExamGradeVO.fromAgentExamGrade(item);
            if (null != vo) {
                List<AgentExamSubjectVO> examSubjectVOS = examGradeSubjectMap.get(vo.getId());
                if (CollectionUtils.isNotEmpty(examSubjectVOS)) {
                    examSubjectVOS.forEach(p -> {
                        List<GroupStatistics> groupStatistics = p.getGroupStatistics();
                        if (CollectionUtils.isNotEmpty(groupStatistics)){
                            groupStatistics.forEach(g -> {
                                if (item.getGrade() != null){
                                    ClazzLevel clazzLevel = ClazzLevel.parse(item.getGrade());
                                    if (null != clazzLevel){
                                        g.setClazzName(clazzLevel.getDescription()+g.getClazzName());
                                    }
                                }
                            });
                        }
                    });
                    vo.setExamSubjectVOList(examSubjectVOS);
                }
                fillAgentGradeDetailsData(vo, bigExamPostDataMap.get(String.valueOf(vo.getId())), month);
                resultList.add(vo);
            }
        });
        resultList.sort((o1, o2) -> {
            return o1.getGrade().compareTo(o2.getGrade());
        });
        return resultList;
    }


    private void fillAgentGradeDetailsData(AgentExamGradeVO vo, BigExamPostData postData, int month) {
        Date date = DateUtils.stringToDate(String.valueOf(month), "yyyyMM");
        final String showDateStr;
        if (null != date) {
            showDateStr = DateUtils.dateToString(date, "yyyy-MM");
        } else {
            showDateStr = null;
        }
        if (null != postData) {
            vo.setBgExamGte3StuCount(postData.getBgExamGte3StuCount());
            vo.setBgExamGte6StuCount(postData.getBgExamGte6StuCount());
        }
        AgentGradeDetails agentGradeDetails = new AgentGradeDetails();
        ArtScienceCondition artScienceCondition = new ArtScienceCondition();
        if (null != postData) {
            artScienceCondition.setIsMeetClassArtsci(postData.isIsMeetClassArtsci());
            artScienceCondition.setIsMeetGradeArtsci(postData.isIsMeetGradeArtsci());
        }
        agentGradeDetails.setAgentArtScienceCondition(artScienceCondition);
        List<AgentExamSubjectVO> examSubjectVOList = vo.getExamSubjectVOList();

        List<AgentScanDetails> agentScanDetailsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(examSubjectVOList) && null != postData) {
            examSubjectVOList.forEach(item -> {
                AgentScanDetails details = new AgentScanDetails();
                List<AgentExamPaperVO> examPaperVOList = item.getExamPaperVOList();
                if (CollectionUtils.isNotEmpty(examPaperVOList)) {
                    List<String> paperNames = examPaperVOList.stream().map(AgentExamPaperVO::getPaperName).collect(toList());
                    details.setPaperName(StringUtils.join(paperNames, ","));
                }
                SubjectStatistic subjectStatistic = postData.getSubjectStatistics().stream().filter(p -> Objects.equals(p.getSubject(), item.getSubject().name())).findFirst().orElse(null);
                if (null != subjectStatistic) {
                    List<ExamStatistic> examStatistics = subjectStatistic.getExamStatistics();
                    if (CollectionUtils.isNotEmpty(examStatistics)) {
                        examStatistics.forEach(s -> {
                            ScanDetails sd = new ScanDetails();
                            sd.setArtScienceType(s.getArtScienceType());
                            ExamIndicators id = s.getIndicators();
                            if (null != id) {
                                sd.setClassMinPermeability(id.getClassMinPermeability());
                                sd.setClassPermeability(id.getClassPermeability());
                                sd.setExamStuCount(id.getParticipateCount());
                                sd.setIsMeetQuestion(subjectStatistic.isIsMeetQuestion());
                                sd.setIsSignContract(true);
                                sd.setMonth(showDateStr);
                                sd.setStuPermeability(id.getStuPermeability());
                                sd.setType(1L);
                                sd.setSubject(subjectStatistic.getSubject());
                            }
                            try {
                                AgentScanDetails scanDetailsTemp = (AgentScanDetails) BeanUtils.cloneBean(details);
                                scanDetailsTemp.setScanDetails(sd);
                                agentScanDetailsList.add(scanDetailsTemp);
                            } catch (Exception e) {
                            }
                        });
                    }
                }
            });
        }
        agentGradeDetails.setAgentScanDetails(agentScanDetailsList);
        vo.setAgentGradeDetails(agentGradeDetails);
    }

    private List<AgentExamSubjectVO> toAgentExamSubjectVOList(List<AgentExamSubject> examSubjectList, Map<String, BigExamPostData> bigExamPostDataMap) {
        List<AgentExamSubjectVO> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(examSubjectList)) {

            Set<Long> examSubjectIdSet = examSubjectList.stream().map(AgentExamSubject::getId).collect(Collectors.toSet());
            List<AgentExamPaper> examPaperList = agentExamPaperPersistence.loadByExamSubjectIds(examSubjectIdSet);
            List<AgentExamPaperVO> agentExamPaperVOList = toAgentExamPaperVOList(examPaperList);
            Map<Long, List<AgentExamPaperVO>> examSubjectPaperMap = agentExamPaperVOList.stream().collect(Collectors.groupingBy(AgentExamPaperVO::getAgentExamSubjectId, Collectors.toList()));

            examSubjectList.forEach(item -> {
                AgentExamSubjectVO vo = AgentExamSubjectVO.fromAgentExamSubject(item);
                if (null != vo) {
                    List<AgentExamPaperVO> examPaperVOList = examSubjectPaperMap.get(vo.getId());
                    if (CollectionUtils.isNotEmpty(examPaperVOList)) {
                        vo.setExamPaperVOList(examPaperVOList);
                    }
                    vo.setPaperCount(examPaperVOList.size());
                    BigExamPostData bigExamPostData = bigExamPostDataMap.get(String.valueOf(item.getAgentExamGradeId()));
                    dealStatistics(vo, bigExamPostData);
                    resultList.add(vo);
                }
            });
        }
        return resultList;
    }

    private void dealStatistics(AgentExamSubjectVO vo, BigExamPostData bigExamPostData) {
        if (null != bigExamPostData) {
            Map<String, Boolean> examRequirement = new HashMap<>();
            SubjectStatistic subjectStatistic = bigExamPostData.getSubjectStatistics().stream().filter(item -> Objects.equals(item.getSubject(), vo.getSubject().name())).findFirst().orElse(null);
            boolean is_meet_que_requirement = false;
            if (null != subjectStatistic) {
                is_meet_que_requirement = subjectStatistic.isIsMeetQuestion();
            }
            boolean art_science_standard = bigExamPostData.isIsMeetClassArtsci();
            boolean two_sence_class_num = bigExamPostData.isIsMeetClassArtsci();
            examRequirement.put("is_meet_que_requirement", is_meet_que_requirement);
            examRequirement.put("art_science_standard", art_science_standard);
            examRequirement.put("two_sence_class_num", two_sence_class_num);
            vo.setExamRequirement(examRequirement);
            if (null != subjectStatistic) {
                List<ExamStatistic> examStatisticList = subjectStatistic.getExamStatistics();
                List<ExamStatistics> examStatistics = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(examStatisticList)) {
                    examStatisticList.forEach(item -> {
                        ExamStatistics statistics = new ExamStatistics();
                        String artScienceTypeStr = item.getArtScienceType() != null ? item.getArtScienceType().replace("_", "") : null;
                        ArtScienceType artScienceType = ArtScienceType.of(artScienceTypeStr);
                        if (artScienceType != ArtScienceType.UNKNOWN) {
                            statistics.setArtScienceType(artScienceType);
                            ExamIndicators examIndicators = item.getIndicators();
                            statistics.setStuPermeability(examIndicators.getStuPermeability());
                            statistics.setMeetStuPermeability(examIndicators.isIsMeetStuPermeability());
                            statistics.setGroupPermeability(examIndicators.getClassPermeability());
                            statistics.setMeetGroupPermeability(examIndicators.isIsMeetClassPermeability());
                            statistics.setParticipateCount(examIndicators.getParticipateCount());
                            statistics.setMeetParticipateCount(examIndicators.isIsMeetParticipateCount());
                            statistics.setGroupMinCompletionRate(examIndicators.getClassMinPermeability());
                            statistics.setMeetGroupMinCompletionRate(examIndicators.isIsMeetClassMinPermeability());
                            examStatistics.add(statistics);
                        }

                    });
                }
                vo.setExamStatistics(examStatistics);
                List<GroupStatistic> groupStatisticList = subjectStatistic.getGroupStatistics();
                List<GroupStatistics> groupStatisticsList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(groupStatisticList)) {
                    groupStatisticList.forEach(item -> {
                        String artScienceTypeStr = item.getArtScienceType() != null ? item.getArtScienceType().replace("_", "") : null;
                        ArtScienceType artScienceType = ArtScienceType.of(artScienceTypeStr);
                        if (artScienceType != ArtScienceType.UNKNOWN) {
                            GroupStatistics groupStatistics = new GroupStatistics();
                            groupStatistics.setClazzName(item.getClassName());
                            groupStatistics.setArtScienceType(artScienceType);
                            groupStatistics.setGroupId(item.getGroupId());
                            groupStatistics.setParticipateCount(item.getParticipateCount());
                            groupStatistics.setPermeability(item.getPermeability());
                            groupStatistics.setStuCount(item.getStuCount());
                            groupStatisticsList.add(groupStatistics);
                        }
                    });
                }
                vo.setGroupStatistics(groupStatisticsList);
            }
        }
    }

    private List<AgentExamPaperVO> toAgentExamPaperVOList(List<AgentExamPaper> examPaperList) {
        List<AgentExamPaperVO> agentExamPaperVOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(examPaperList)) {
            return Collections.emptyList();
        }
        examPaperList.forEach(item -> {
            AgentExamPaperVO agentExamPaperVO = AgentExamPaperVO.fromAgentExamPaper(item);
            if (null != agentExamPaperVO) {
                agentExamPaperVOList.add(agentExamPaperVO);
            }
        });
        return agentExamPaperVOList;
    }


    /**
     * 大考按月份和学校ID查询
     *
     * @param month
     * @return
     */
    public AgentExamSchoolVO getAgentExamSchoolByMonthAndSchoolId(int month, long schoolId) {
        AgentExamSchool agentExamSchool = agentExamSchoolPersistence.loadBySchoolIdAndMonth(schoolId, month);
        if (null != agentExamSchool) {
            return toAgentExamSchoolVOList(Collections.singletonList(agentExamSchool)).stream().findFirst().orElse(null);
        }
        return null;
    }

    /**
     * 大考按学校ID查询
     *
     * @return
     */
    public List<AgentExamSchoolVO> getAgentExamSchoolBySchoolId(long schoolId) {
        List<AgentExamSchool> agentExamSchools = agentExamSchoolPersistence.loadBySchoolId(schoolId);
        if (CollectionUtils.isNotEmpty(agentExamSchools)) {
            return toAgentExamSchoolVOList(agentExamSchools);
        }
        return Collections.emptyList();
    }

    /**
     * 按照ID查询大考
     *
     * @param examSubjectId 大考ID
     * @return
     */
    public AgentExamSubjectVO getAgentExamSubject(Long examSubjectId) {
        AgentExamSubject examSubject = agentExamSubjectPersistence.load(examSubjectId);
        if (examSubject != null) {
            AgentExamGrade agentExamGrade = agentExamGradePersistence.load(examSubject.getAgentExamGradeId());
            if (agentExamGrade != null) {
                AgentExamSchool agentExamSchool = agentExamSchoolPersistence.load(agentExamGrade.getAgentExamSchoolId());
                if (null != agentExamSchool) {
                    List<AgentExamGradeVO> examGradeVOS = toAgentExamGradeVOList(Collections.singletonList(agentExamGrade), agentExamSchool.getMonth());
                    AgentExamGradeVO gradeVO = examGradeVOS.stream().findFirst().orElse(null);
                    if (null != gradeVO) {
                        return gradeVO.getExamSubjectVOList().stream().filter(item -> item.getId().equals(examSubjectId)).findFirst().orElse(null);
                    }
                }
            }
        }
        return null;
    }

    private MapMessage deleteAgentExamPaper(Long examPaperId) {
        AgentExamPaper examPaper = agentExamPaperPersistence.load(examPaperId);
        if (examPaper != null) {
            examPaper.setDisabled(true);
            agentExamPaperPersistence.replace(examPaper);
            AgentExamSubject agentExamSubject = agentExamSubjectPersistence.load(examPaper.getAgentExamSubjectId());
            if (null != agentExamSubject) {
                List<AgentExamPaper> agentExamPapers = agentExamPaperPersistence.loadByExamSubjectIds(Collections.singleton(agentExamSubject.getId()));
                if (CollectionUtils.isEmpty(agentExamPapers)) {
                    deleteAgentExamSubject(agentExamSubject.getId());
                }
            }
        }
        return MapMessage.successMessage();
    }

    private MapMessage deleteAgentExamGrade(Long examGradeId) {
        AgentExamGrade agentExamGrade = agentExamGradePersistence.load(examGradeId);
        if (null != agentExamGrade) {
            List<AgentExamSubject> examSubjectList = agentExamSubjectPersistence.loadByExamGradeIds(Collections.singleton(agentExamGrade.getId()));
            examSubjectList.forEach(item -> {
                deleteAgentExamSubject(item.getId());
            });
        }
        agentExamGrade.setDisabled(true);
        agentExamGradePersistence.replace(agentExamGrade);
        return MapMessage.successMessage();
    }

    /**
     * 删除大考，同时删除该大考对应的试卷
     * 如果某个学校没有对应的试卷，则删除对应的学校
     *
     * @param examSubjectId 大考ID
     * @return
     */
    public MapMessage deleteAgentExamSubject(Long examSubjectId) {
        AgentExamSubject agentExamSubject = agentExamSubjectPersistence.load(examSubjectId);
        if (null != agentExamSubject) {
            List<AgentExamPaper> agentExamPapers = agentExamPaperPersistence.loadByExamSubjectIds(Collections.singleton(agentExamSubject.getId()));
            agentExamPapers.forEach(item -> {
                item.setDisabled(true);
                agentExamPaperPersistence.replace(item);
            });
            agentExamSubject.setDisabled(true);
            agentExamSubjectPersistence.replace(agentExamSubject);

            Long agentExamGradeId = agentExamSubject.getAgentExamGradeId();
            AgentExamGrade agentExamGrade = agentExamGradePersistence.load(agentExamGradeId);
            List<AgentExamSubject> examSubjectList = agentExamSubjectPersistence.loadByExamGradeIds(Collections.singleton(agentExamGradeId));
            if (CollectionUtils.isEmpty(examSubjectList)) {
                agentExamGrade.setDisabled(true);
                agentExamGradePersistence.replace(agentExamGrade);
            }
            Long agentExamSchoolId = agentExamGrade.getAgentExamSchoolId();
            AgentExamSchool agentExamSchool = agentExamSchoolPersistence.load(agentExamSchoolId);
            List<AgentExamGrade> examGradeList = agentExamGradePersistence.loadByExamSchoolIds(Collections.singleton(agentExamSchoolId));
            if (null != agentExamSchool && CollectionUtils.isEmpty(examGradeList)) {
                agentExamSchool.setDisabled(true);
                agentExamSchoolPersistence.replace(agentExamSchool);
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 检索
     *
     * @param schoolId
     * @param nameKey
     * @return
     */
    public List<AgentKlxScanPaperVO> searchPaper(Long schoolId, Integer grade, String nameKey) {
        List<AgentKlxScanPaper> agentKlxScanPaperList = agentKlxScanPaperPersistence.searchPaper(schoolId, grade, nameKey, null, null);
        List<AgentKlxScanPaperVO> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(agentKlxScanPaperList)) {
            agentKlxScanPaperList.forEach(item -> {
                AgentKlxScanPaperVO vo = AgentKlxScanPaperVO.fromAgentKlxScanPaper(item);
                if (null != vo) {
                    voList.add(vo);
                }
            });
        }
        return voList;
    }

    public MapMessage upsertAgentExamSchool(AgentExamSchoolVO agentExamSchoolVO) {
        MapMessage mapMessage = checkAndFillAgentExamSchool(agentExamSchoolVO);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        AgentExamSchool examSchool = agentExamSchoolVO.toAgentExamSchool();
        if (examSchool.getId() == null) {
            AgentExamSchool examSchoolTemp = agentExamSchoolPersistence.loadBySchoolIdAndMonth(examSchool.getSchoolId(), examSchool.getMonth());
            if (null == examSchoolTemp) {
                examSchool = agentExamSchoolPersistence.upsert(examSchool);
            } else {
                examSchool.setId(examSchoolTemp.getId());
                examSchool = agentExamSchoolPersistence.replace(examSchool);
            }
        } else {
            examSchool = agentExamSchoolPersistence.replace(examSchool);
        }

        List<AgentExamGradeVO> examGradeVOList = agentExamSchoolVO.getExamGradeVOList();
        List<AgentExamGrade> oldExamGradeList = agentExamGradePersistence.loadByExamSchoolIds(Collections.singleton(examSchool.getId()));
        List<Long> newExamGradeIds = new ArrayList<>();
        for (AgentExamGradeVO gradeVO : examGradeVOList) {
            AgentExamGrade agentExamGrade = gradeVO.toAgentExamGrade();
            agentExamGrade.setAgentExamSchoolId(examSchool.getId());
            if (agentExamGrade.getId() == null) {
                List<AgentExamGrade> agentExamGrades = agentExamGradePersistence.loads(agentExamGrade.getAgentExamSchoolId(), agentExamGrade.getGrade());
                if (CollectionUtils.isNotEmpty(agentExamGrades)) {
                    AgentExamGrade examGradeTemp = agentExamGrades.get(0);
                    agentExamGrade.setId(examGradeTemp.getId());
                    agentExamGrade = agentExamGradePersistence.replace(agentExamGrade);
                } else {
                    agentExamGrade = agentExamGradePersistence.upsert(agentExamGrade);
                }
            } else {
                agentExamGrade = agentExamGradePersistence.upsert(agentExamGrade);
            }
            newExamGradeIds.add(agentExamGrade.getId());

            List<AgentExamSubjectVO> examSubjectVOList = gradeVO.getExamSubjectVOList();
            List<AgentExamSubject> oldExamSubjectList = agentExamSubjectPersistence.loadByExamGradeIds(Collections.singleton(agentExamGrade.getId()));
            List<Long> newExamSubjectIds = new ArrayList<>();
            for (AgentExamSubjectVO item : examSubjectVOList) {
                AgentExamSubject examSubject = item.toAgentExamSubject();
                examSubject.setAgentExamGradeId(agentExamGrade.getId());
                if (null == examSubject.getId()) {
                    List<AgentExamSubject> agentExamSubjects = agentExamSubjectPersistence.loads(agentExamGrade.getId(), examSubject.getSubject());
                    if (CollectionUtils.isNotEmpty(agentExamSubjects)) {
                        AgentExamSubject examSubjectTemp = agentExamSubjects.get(0);
                        examSubject.setId(examSubjectTemp.getId());
                        examSubject = agentExamSubjectPersistence.replace(examSubject);
                    } else {
                        examSubject = agentExamSubjectPersistence.upsert(examSubject);
                    }
                } else {
                    examSubject = agentExamSubjectPersistence.upsert(examSubject);
                }
                newExamSubjectIds.add(examSubject.getId());
                List<AgentExamPaper> oldAgentExamPapers = agentExamPaperPersistence.loadByExamSubjectIds(Collections.singleton(examSubject.getId()));
                List<Long> newAgentExamPapersIds = new ArrayList<>();
                List<AgentExamPaperVO> examPaperVOList = item.getExamPaperVOList();
                if (CollectionUtils.isNotEmpty(examPaperVOList)) {
                    for (AgentExamPaperVO p : examPaperVOList) {
                        AgentExamPaper examPaper = p.toAgentExamPaper();
                        examPaper.setAgentExamSubjectId(examSubject.getId());
                        if (p.getId() == null) {
                            AgentExamPaper examPaperTemp = agentExamPaperPersistence.load(p.getAgentExamSubjectId(), p.getPaperId());
                            if (null == examPaperTemp) {
                                examPaper = agentExamPaperPersistence.upsert(examPaper);
                            } else {
                                examPaper.setId(examPaperTemp.getId());
                                examPaper = agentExamPaperPersistence.replace(examPaper);
                            }
                        } else {
                            examPaper = agentExamPaperPersistence.upsert(examPaper);
                        }
                        newAgentExamPapersIds.add(examPaper.getId());
                    }
                }
                //删除未包含的
                oldAgentExamPapers.stream().filter(p -> !newAgentExamPapersIds.contains(p.getId())).forEach(p -> {
                    deleteAgentExamPaper(p.getId());
                });
            }
            //删除未包含的
            oldExamSubjectList.stream().filter(item -> !newExamSubjectIds.contains(item.getId())).forEach(item -> {
                deleteAgentExamSubject(item.getId());
            });
        }
        //删除未包含的
        oldExamGradeList.stream().filter(item -> !newExamGradeIds.contains(item.getId())).forEach(item -> {
            deleteAgentExamGrade(item.getId());
        });
        return MapMessage.successMessage();
    }

    /**
     * 校验数据完整性以及合法性,并填充其他数据
     *
     * @param agentExamSchoolVO
     * @return
     */
    private MapMessage checkAndFillAgentExamSchool(AgentExamSchoolVO agentExamSchoolVO) {
        if (null == agentExamSchoolVO || agentExamSchoolVO.getSchoolId() == null || agentExamSchoolVO.getMonth() == null || agentExamSchoolVO.toAgentExamSchool() == null) {
            return MapMessage.errorMessage("学校信息不完整");
        }
        List<AgentSchoolDictData> dictSchools = agentDictSchoolService.getWrappedSchoolDictDataBySchool(agentExamSchoolVO.getSchoolId());
        if (CollectionUtils.isEmpty(dictSchools)) {
            return MapMessage.errorMessage("学校不是字典表学校，无法操作");
        }
        AgentSchoolDictData dictData = dictSchools.get(0);
        agentExamSchoolVO.setSchoolName(dictData.getSchoolName());
        agentExamSchoolVO.setSchoolLevel(dictData.getSchoolLevelEnum());
        agentExamSchoolVO.setProvinceCode(dictData.getProvinceCode());
        agentExamSchoolVO.setProvinceName(dictData.getProvinceName());
        agentExamSchoolVO.setCityCode(dictData.getCityCode());
        agentExamSchoolVO.setCityName(dictData.getCityName());

        List<AgentExamContractVO> agentExamContractVOS = searchContract(null, agentExamSchoolVO.getSchoolId(), null, null, null, null);
        if (CollectionUtils.isEmpty(agentExamContractVOS)) {
            return MapMessage.errorMessage("该学校没有有效的合同，无法操作");
        }

        List<AgentExamGradeVO> examGradeVOList = agentExamSchoolVO.getExamGradeVOList();
        if (CollectionUtils.isEmpty(examGradeVOList)) {
            return MapMessage.errorMessage("没有大考信息，无法操作");
        }
        for (AgentExamGradeVO agentExamGradeVO : examGradeVOList) {
            if (agentExamGradeVO.getGrade() == null || agentExamGradeVO.toAgentExamGrade() == null) {
                return MapMessage.errorMessage("大考信息没有年级，无法操作");
            }
            List<AgentExamSubjectVO> examSubjectVOList = agentExamGradeVO.getExamSubjectVOList();
            for (AgentExamSubjectVO agentExamSubjectVO : examSubjectVOList) {
                if (agentExamSubjectVO.getSubject() == null || agentExamSubjectVO.toAgentExamSubject() == null) {
                    return MapMessage.errorMessage("大考信息没有学科，无法操作");
                }
                List<AgentExamPaperVO> examPaperVOList = agentExamSubjectVO.getExamPaperVOList();
                if (CollectionUtils.isEmpty(examPaperVOList)) {
                    return MapMessage.errorMessage(agentExamGradeVO.getGradeDes() + agentExamSubjectVO.getSubjectDes() + "大考没有试卷，无法操作");
                }
                for (AgentExamPaperVO agentExamPaperVO : examPaperVOList) {
                    if (agentExamPaperVO.getSubject() == null || agentExamPaperVO.getPaperId() == null || agentExamPaperVO.getPaperName() == null || agentExamPaperVO.toAgentExamPaper() == null) {
                        return MapMessage.errorMessage(agentExamGradeVO.getGradeDes() + agentExamSubjectVO.getSubjectDes() + "大试卷信息不全，无法操作");
                    }
                }

            }
        }
        return MapMessage.successMessage();
    }

    public List<AgentExamSchoolVO> searchExamStatistics(Integer cityCode, Long userId, Long schoolId, String month) {
        List<AgentExamSchoolVO> resultList = new ArrayList<>();
        List<Long> searchSchoolIds = new ArrayList<>();
        List<SchoolLevel> schoolLevels = new ArrayList<>();
        schoolLevels.add(SchoolLevel.MIDDLE);
        schoolLevels.add(SchoolLevel.HIGH);
        List<Long> managedSchoolList = baseOrgService.getSchoolListByLevels(schoolLevels);
        searchSchoolIds.addAll(managedSchoolList);
        if (schoolId != null) {
            searchSchoolIds = searchSchoolIds.stream().filter(item -> item.equals(schoolId)).collect(toList());
        }
        if (null != userId) {
            List<Long> managedSchoolListTemp = baseOrgService.getManagedSchoolList(userId, schoolLevels);
            if (CollectionUtils.isNotEmpty(searchSchoolIds)) {
                searchSchoolIds = searchSchoolIds.stream().filter(item -> managedSchoolListTemp.contains(item)).collect(Collectors.toList());
            }
        }
        if (cityCode != null) {
            Set<Long> schoolIds = baseOrgService.getSchoolIdsByCityCode(cityCode);
            if (CollectionUtils.isNotEmpty(searchSchoolIds)) {
                searchSchoolIds = searchSchoolIds.stream().filter(item -> schoolIds.contains(item)).collect(Collectors.toList());
            }
        }

        if (CollectionUtils.isNotEmpty(searchSchoolIds)) {
            Map<Long, List<AgentUserSchool>> userSchoolBySchools = baseOrgService.getUserSchoolBySchools(searchSchoolIds);
            Map<Long, AgentUser> agentUserMap = baseOrgService.findAllAgentUsers().stream().collect(Collectors.toMap(AgentUser::getId, Function.identity(), (o1, o2) -> o2));
            List<BigExamManagerData> bigExamManagerDataList = batchLoadBigExamManagerData(month, searchSchoolIds);
            Map<Long, CrmSchoolSummary> crmSchoolSummaryMap = agentDictSchoolService.batchLoadCrmSchoolSummary(searchSchoolIds);
            Date date = DateUtils.stringToDate(month, "yyyy-MM");
            MonthRange monthRange = MonthRange.newInstance(date.getTime());
            Date previousDate = monthRange.previous().getStartDate();
            List<AgentExamSchool> agentExamSchools = agentExamSchoolPersistence.loads(searchSchoolIds, null, SafeConverter.toInt(DateUtils.dateToString(previousDate, "yyyyMM")));
            List<AgentExamSchoolVO> agentExamSchoolVOList = toAgentExamSchoolVOList(agentExamSchools);
            if (CollectionUtils.isNotEmpty(bigExamManagerDataList)) {
                bigExamManagerDataList.forEach(item -> {
                    AgentExamSchoolVO vo = new AgentExamSchoolVO();
                    vo.setSchoolId(item.getSchoolId());
                    vo.setBgExamGte3StuCount(item.getBgExamGte3StuCount());
                    vo.setBgExamGte6StuCount(item.getBgExamGte6StuCount());
                    vo.setMonthStr(item.getMonth());
                    List<AgentUserSchool> agentUserSchools = userSchoolBySchools.get(vo.getSchoolId());
                    if (CollectionUtils.isNotEmpty(agentUserSchools)) {
                        AgentUser agentUser = agentUserMap.get(agentUserSchools.get(0).getUserId());
                        if (null != agentUser) {
                            vo.setOwnerId(agentUser.getId());
                            vo.setOwnerName(agentUser.getRealName());
                        }
                    }
                    CrmSchoolSummary crmSchoolSummary = crmSchoolSummaryMap.get(item.getSchoolId());
                    if (null != crmSchoolSummary) {
                        vo.setSchoolName(crmSchoolSummary.getSchoolName());
                        vo.setCityName(crmSchoolSummary.getCityName());
                        vo.setCityCode(crmSchoolSummary.getCityCode());
                        vo.setProvinceCode(crmSchoolSummary.getProvinceCode());
                        vo.setProvinceName(crmSchoolSummary.getProvinceName());
                    }
                    List<AgentExamGradeVO> examGradeVOS = new ArrayList<>();
                    List<GradeStatistic> gradeStatistics = item.getGradeStatistics();
                    if (CollectionUtils.isNotEmpty(gradeStatistics)) {
                        gradeStatistics.forEach(gradeStatistic -> {
                            AgentExamGradeVO gradeVO = new AgentExamGradeVO();
                            gradeVO.setBgExamGte3StuCount(gradeStatistic.getBgExamGte3StuCount());
                            gradeVO.setBgExamGte6StuCount(gradeStatistic.getBgExamGte6StuCount());
                            gradeVO.setGrade(Long.valueOf(gradeStatistic.getGradeLevel()).intValue());
                            AgentGradeDetails agentGradeDetails = new AgentGradeDetails();
                            agentGradeDetails.setAgentArtScienceCondition(gradeStatistic.getGradeDetails().getArtScienceCondition());
                            List<AgentAutoApplyStatistic> autoApplyStatistics = new ArrayList<>();
                            gradeStatistic.getGradeDetails().getAutoApplyStatistics().forEach(item1 -> {
                                AgentAutoApplyStatistic p = new AgentAutoApplyStatistic();
                                p.setAutoApplyStatistic(item1);
                                autoApplyStatistics.add(p);
                            });
                            agentGradeDetails.setAgentAutoApplyStatistics(autoApplyStatistics);
                            List<AgentScanDetails> agentScanDetails = new ArrayList<>();
                            gradeStatistic.getGradeDetails().getScanDetails().forEach(item1 -> {
                                AgentScanDetails p = new AgentScanDetails();
                                p.setScanDetails(item1);
                                if (item1.getType() == 0) {
                                    AgentKlxScanPaper agentKlxScanPaper = agentKlxScanPaperPersistence.getPaperByPaperId(item1.getPaperId());
                                    if (null != agentKlxScanPaper) {
                                        p.setPaperName(agentKlxScanPaper.getPaperTitle());
                                    }
                                }
                                if (item1.getType() == 1) {
                                    AgentExamSchoolVO examSchoolVO = agentExamSchoolVOList.stream().filter(temp -> temp.getSchoolId().equals(item.getSchoolId())).findFirst().orElse(null);
                                    if (null != examSchoolVO && CollectionUtils.isNotEmpty(examSchoolVO.getExamGradeVOList())) {
                                        List<AgentExamGradeVO> examGradeVOList = examSchoolVO.getExamGradeVOList();
                                        AgentExamGradeVO gradeVOTemp = examGradeVOList.stream().filter(temp -> temp.getGrade().equals(Long.valueOf(gradeStatistic.getGradeLevel()).intValue())).findFirst().orElse(null);
                                        if (null != gradeVOTemp && CollectionUtils.isNotEmpty(gradeVOTemp.getExamSubjectVOList())) {
                                            List<AgentExamSubjectVO> examSubjectVOList = gradeVOTemp.getExamSubjectVOList();
                                            AgentExamSubjectVO agentExamSubjectVO = examSubjectVOList.stream().filter(temp -> temp.getSubject().getValue().equals(item1.getSubject())).findFirst().orElse(null);
                                            if (agentExamSubjectVO != null && CollectionUtils.isNotEmpty(agentExamSubjectVO.getExamPaperVOList())) {
                                                List<String> paperNames = agentExamSubjectVO.getExamPaperVOList().stream().map(AgentExamPaperVO::getPaperName).collect(toList());
                                                p.setPaperName(StringUtils.join(paperNames, ","));
                                            }
                                        }
                                    }
                                }
                                agentScanDetails.add(p);
                            });
                            agentGradeDetails.setAgentScanDetails(agentScanDetails);
                            gradeVO.setAgentGradeDetails(agentGradeDetails);
                            examGradeVOS.add(gradeVO);
                        });
                    }
                    vo.setExamGradeVOList(examGradeVOS);
                    resultList.add(vo);
                });
            }
        }
        return resultList;
    }


    /**
     * 检索
     *
     * @param cityCode
     * @param userId
     * @param schoolId
     * @param month
     * @return
     */
    public List<AgentExamSchoolVO> searchExamPost(Integer cityCode, Long userId, Long schoolId, String month) {
        List<Long> schoolIds = new ArrayList<>();
        if (schoolId != null) {
            schoolIds.add(schoolId);
        }
        Date monthDate = DateUtils.stringToDate(month, "yyyy-MM");
        if (null == monthDate) {
            return Collections.emptyList();
        }
        int dateInt = SafeConverter.toInt(DateUtils.dateToString(monthDate, "yyyyMM"));
        List<AgentExamSchool> examSchools = agentExamSchoolPersistence.loads(schoolIds, cityCode, dateInt);
        if (null != userId) {
            List<SchoolLevel> schoolLevels = new ArrayList<>();
            schoolLevels.add(SchoolLevel.MIDDLE);
            schoolLevels.add(SchoolLevel.HIGH);
            List<Long> managedSchoolList = baseOrgService.getManagedSchoolList(userId, schoolLevels);
            if (CollectionUtils.isNotEmpty(managedSchoolList)) {
                examSchools = examSchools.stream().filter(item -> managedSchoolList.contains(item.getSchoolId())).collect(toList());
            } else {
                examSchools.clear();
            }
        }
        if (CollectionUtils.isNotEmpty(examSchools)) {
            Map<String, AgentUser> allUsersMap = baseUserService.getAllAgentUsers();
            List<AgentGroupUser> allGroupUsers = agentGroupUserLoaderClient.findAll();

            // 获取市经理信息
            List<AgentGroupUser> cityManageUsers = allGroupUsers.stream().filter(p -> Objects.equals(p.getUserRoleType(), AgentRoleType.CityManager)).collect(Collectors.toList());
            Map<Long, Long> cityManageUsersMap = cityManageUsers.stream().collect(Collectors.toMap(AgentGroupUser::getGroupId, AgentGroupUser::getUserId, (o1, o2) -> o1));

            // 获取所有专员信息
            List<AgentGroupUser> businessDeveloperUsers = allGroupUsers.stream().filter(p -> Objects.equals(p.getUserRoleType(), AgentRoleType.BusinessDeveloper)).collect(Collectors.toList());
            Map<Long, AgentGroupUser> businessDeveloperUserMap = businessDeveloperUsers.stream().collect(Collectors.toMap(AgentGroupUser::getUserId, Function.identity(), (o1, o2) -> o1));


            List<AgentUserSchool> agentUserSchools = agentUserSchoolLoaderClient.findBySchoolIds(examSchools.stream().map(AgentExamSchool::getSchoolId).collect(Collectors.toSet()));
            if (null == agentUserSchools) {
                agentUserSchools = Collections.emptyList();
            }
            Map<Long, List<AgentUserSchool>> agentUserSchoolMap = agentUserSchools.stream().collect(Collectors.groupingBy(AgentUserSchool::getSchoolId));
            List<AgentExamSchoolVO> agentExamSchoolVOList = toAgentExamSchoolVOList(examSchools);
            if (CollectionUtils.isNotEmpty(agentExamSchoolVOList)) {
                agentExamSchoolVOList.forEach(item -> {
                    // 设置负责人信息
                    {
                        List<AgentUserSchool> agentUserSchoolsTemp = agentUserSchoolMap.get(item.getSchoolId());
                        if (CollectionUtils.isNotEmpty(agentUserSchoolsTemp)) {
                            agentUserSchoolsTemp.forEach(p -> {
                                Long userIdTemp = p.getUserId();
                                AgentGroupUser businessDeveloperUser = businessDeveloperUserMap.get(userIdTemp);
                                if (null != businessDeveloperUser && allUsersMap.get(String.valueOf(userIdTemp)) != null) {
                                    AgentUser user = allUsersMap.get(String.valueOf(userIdTemp));
                                    item.setOwnerName(user.getRealName());
                                    item.setOwnerId(user.getId());
                                }
                            });
                        }
                        if (null == item.getOwnerId()) {
                            List<Long> schoolGroupIds = agentGroupSupport.getGroupIdsBySchool(item.getSchoolId(), Collections.singletonList(AgentGroupRoleType.City));
                            if (CollectionUtils.isNotEmpty(schoolGroupIds)) {
                                // 设置部门信息
                                Long groupId = schoolGroupIds.get(0);
                                // 设置市经理信息
                                if (cityManageUsersMap.get(groupId) != null) {
                                    Long cityManageId = cityManageUsersMap.get(groupId);
                                    if (allUsersMap.get(String.valueOf(cityManageId)) != null) {
                                        AgentUser user = allUsersMap.get(String.valueOf(cityManageId));
                                        item.setOwnerName(user.getRealName());
                                        item.setOwnerId(user.getId());
                                    }
                                }
                            }
                        }
                    }
                });
            }
            return agentExamSchoolVOList;
        }
        return Collections.emptyList();
    }

    private List<BigExamManagerData> batchLoadBigExamManagerData(final String month,Collection<Long> schoolIds){
        List<BigExamManagerData> resultList = new ArrayList<>();
        return resultList;
    }


    private List<BigExamPostData> batchLoadBigExamPostData(Set<String> examGradeIdSetStr) {
        List<BigExamPostData> resultList = new ArrayList<>();
        return resultList;
    }

    /**
     * 添加/编辑合同扩展信息
     * @param examContractExtend
     */
    public void addOrUpdateExamContractExtend(AgentExamContractExtend examContractExtend) {
        Long contractId = examContractExtend.getContractId();
        AgentExamContractExtend agentExamContractExtend = agentExamContractExtendDao.loadByContractId(contractId);
        if (null != agentExamContractExtend) {
            //操作日志
            addOrUpdateExamContractExtendOperationRecord(agentExamContractExtend,examContractExtend);
            examContractExtend.setId(agentExamContractExtend.getId());
            agentExamContractExtendDao.replace(examContractExtend);
        } else {
            agentExamContractExtendDao.insert(examContractExtend);
            //操作日志
            addOrUpdateExamContractExtendOperationRecord(null,examContractExtend);
        }
    }



    /**
     * 获取合同扩展信息
     * @param agentExamContractVO
     * @param contractId
     * @return
     */
    public AgentExamContractVO getExamContractExtend(AgentExamContractVO agentExamContractVO,Long contractId) {
        AgentExamContractExtend agentExamContractExtend = agentExamContractExtendDao.loadByContractId(contractId);
        if (null != agentExamContractExtend) {
            List<AgentExamContractSplitSetting> splitSettingList = agentExamContractExtend.getSplitSettingList();
            splitSettingList.stream().forEach(p->{
                if (null != p){
                    //获取专员姓名
                    AgentUser agentUser = agentUserLoaderClient.load(p.getContractorId());
                    if (null != agentUser){
                        p.setContractorName(agentUser.getRealName());
                    }
                }
            });

            List<String> imageUrlList = agentExamContractExtend.getImageUrlList();
            agentExamContractVO.setSplitSettingList(splitSettingList);
            agentExamContractVO.setImageUrlList(imageUrlList);
        }
        return agentExamContractVO;
    }

    /**
     * 根据合同编号获取回款记录
     * @param contractId
     * @return
     */
    public List<AgentExamContractPayback> getPaybackByContractId(Long contractId) {
        return agentExamContractPaybackPersistence.loadByContractId(contractId);
    }

    /**
     * 获取合同回款信息
     * @param contractId
     * @return
     */
    public Map<String,Object> getContractPaybackInfo(Long contractId) {
        Map<String,Object> dataMap = new HashMap<>();
        AgentExamContractVO agentExamContract = getAgentExamContract(contractId);
        if (null != agentExamContract){
            dataMap.put("contractId",contractId);
            //合同金额
            dataMap.put("contractAmount",agentExamContract.getContractAmount());
            //已回款金额
            Integer havaPaybackAmount = getContractHavaPaybackAmount(contractId);
            dataMap.put("havaPaybackAmount",havaPaybackAmount);
            //待回款金额
            dataMap.put("waitPaybackAmount", MathUtils.doubleSub(agentExamContract.getContractAmount(),havaPaybackAmount));
            List<AgentExamContractPayback> agentExamContractPaybackList = agentExamContractPaybackPersistence.loadByContractId(contractId);
            List<AgentExamContractPaybackVO> agentExamContractPaybackVOList = toAgentExamContractPaybackVO(agentExamContractPaybackList);
            dataMap.put("contractPaybackList",agentExamContractPaybackVOList);
        }
        return dataMap;
    }

    /**
     * 获取合同已回款金额
     * @param contractId
     * @return
     */
    public Integer getContractHavaPaybackAmount(Long contractId){
        Integer havaPaybackAmount = 0;//已回款金额
        List<AgentExamContractPayback> agentExamContractPaybackList = agentExamContractPaybackPersistence.loadByContractId(contractId);
        if (CollectionUtils.isNotEmpty(agentExamContractPaybackList)){
            for (int i = 0; i < agentExamContractPaybackList.size(); i++){
                havaPaybackAmount += agentExamContractPaybackList.get(i).getPaybackAmount();
            }
        }
        return havaPaybackAmount;
    }

    /**
     * 新增合同回款信息
     * @param examContractPayback
     */
    public void addContractPayback(AgentExamContractPayback examContractPayback) {
        if (null != examContractPayback){
            List<AgentExamContractPayback> agentExamContractPaybackList = agentExamContractPaybackPersistence.loadByContractId(examContractPayback.getContractId());
            //设置期数
            if (CollectionUtils.isNotEmpty(agentExamContractPaybackList)){
                examContractPayback.setPeriod(agentExamContractPaybackList.size() + 1);
            }else {
                examContractPayback.setPeriod(1);
            }
        }
        agentExamContractPaybackPersistence.insert(examContractPayback);
        //操作日志
        addContractPaybackOperationRecord(examContractPayback);
    }



    /**
     * 合同回款信息数据转换
     * @param agentExamContractPaybackList
     * @return
     */
    private List<AgentExamContractPaybackVO> toAgentExamContractPaybackVO(Collection<AgentExamContractPayback> agentExamContractPaybackList) {
        List<AgentExamContractPaybackVO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(agentExamContractPaybackList)) {
            return resultList;
        }
        Set<Long> userIds = agentExamContractPaybackList.stream().map(AgentExamContractPayback::getOperatorId).collect(Collectors.toSet());
        Map<Long, AgentUser> agentUserMap = baseOrgService.getUsers(userIds).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity(), (o1, o2) -> o1));
        agentExamContractPaybackList.forEach(item -> {
            AgentExamContractPaybackVO vo = new AgentExamContractPaybackVO();
            vo.setId(item.getId());
            vo.setPeriod(item.getPeriod());
            vo.setContractId(item.getContractId());
            vo.setPaybackDate(item.getPaybackDate());
            vo.setOperatorId(item.getOperatorId());
            vo.setPaybackAmount(item.getPaybackAmount());

            if (agentUserMap.containsKey(item.getOperatorId())){
                AgentUser agentUser = agentUserMap.get(item.getOperatorId());
                if (null != agentUser){
                    vo.setOperatorName(agentUser.getRealName());
                }
            }
            resultList.add(vo);
        });
        resultList.sort((o1, o2) -> {
            return -o1.getPeriod().compareTo(o2.getPeriod());
        });
        return resultList;
    }



    /**
     * 获取合同回款导出数据
     * @param beginDate
     * @param endDate
     * @return
     */
    public List<AgentExamContractPaybackReportData> getContractPaybackReportData(Date beginDate,Date endDate) {
        List<AgentExamContractPaybackReportData> dataList = new ArrayList<>();
        //获取回款信息
        List<AgentExamContractPayback> agentExamContractPaybackList = agentExamContractPaybackPersistence.searchContractPayback(null,null,beginDate, endDate);
        List<AgentExamContractPaybackVO> agentExamContractPaybackVOList = toAgentExamContractPaybackVO(agentExamContractPaybackList);
        //获取合同信息
        Set<Long> contractIds = agentExamContractPaybackList.stream().map(AgentExamContractPayback::getContractId).collect(Collectors.toSet());
        List<AgentExamContract> agentExamContractList = agentExamContractPersistence.loadByIds(contractIds);
        Map<Long, AgentExamContractVO> agentExamContractVOMap = toAgentExamContractVO(agentExamContractList).stream().collect(Collectors.toMap(AgentExamContractVO::getId, Function.identity(), (o1, o2) -> o1));
        agentExamContractPaybackVOList.forEach(item->{
            AgentExamContractPaybackReportData agentExamContractPaybackReportData = new AgentExamContractPaybackReportData();
            if (null != item){
                if (agentExamContractVOMap.containsKey(item.getContractId())){
                    //获取合同信息
                    AgentExamContractVO agentExamContractVO = agentExamContractVOMap.get(item.getContractId());
                    agentExamContractPaybackReportData.setContractNumber(agentExamContractVO.getContractNumber());
                    agentExamContractPaybackReportData.setPaybackNumber(item.getPaybackNumber());
                    agentExamContractPaybackReportData.setSchoolId(agentExamContractVO.getSchoolId());
                    agentExamContractPaybackReportData.setSchoolName(agentExamContractVO.getSchoolName());
                    agentExamContractPaybackReportData.setSchoolPopularityType(agentExamContractVO.getSchoolPopularityType());
                    agentExamContractPaybackReportData.setContractType(agentExamContractVO.getContractTypeDesc());
                    agentExamContractPaybackReportData.setContractorName(agentExamContractVO.getContractorName());
                    agentExamContractPaybackReportData.setContractAmount(agentExamContractVO.getContractAmount());
                    agentExamContractPaybackReportData.setHardwareCost(agentExamContractVO.getHardwareCost());
                    agentExamContractPaybackReportData.setPaybackDate(item.getPaybackDate());
                    agentExamContractPaybackReportData.setPaybackAmount(item.getPaybackAmount());
                    List<AgentExamContractPayback> examContractPaybackList = agentExamContractPaybackPersistence.searchContractPayback(item.getContractId(), item.getPeriod(), null, null);
                    Integer havePaybackAmount = 0;
                    if (CollectionUtils.isNotEmpty(examContractPaybackList)){
                        for (int i=0;i<examContractPaybackList.size();i++){
                            AgentExamContractPayback agentExamContractPayback = examContractPaybackList.get(i);
                            if (null != agentExamContractPayback){
                                havePaybackAmount += agentExamContractPayback.getPaybackAmount();
                            }
                        }
                    }
                    agentExamContractPaybackReportData.setHavaPaybackAmount(havePaybackAmount);
                    dataList.add(agentExamContractPaybackReportData);
                }
            }
        });
        return dataList;
    }

//    /**
//     * 合同操作日志
//     * @param contractId
//     * @param agentUserOperationType
//     * @param operationContent
//     */
//    public void contractOperationRecord(Long contractId,AgentUserOperationType agentUserOperationType,String operationContent){
//        if (StringUtils.isNotBlank(operationContent)){
//            AgentUserOperationRecord operationRecord = new AgentUserOperationRecord();
//            operationRecord.setDataId(String.valueOf(contractId));
//            operationRecord.setOperationType(agentUserOperationType);
//            operationRecord.setOperatorId(getCurrentUserId());
//            operationRecord.setOperatorName(getCurrentUser().getRealName());
//            operationRecord.setNote(operationContent);
//            agentUserOperationRecordDao.insert(operationRecord);
//        }
//    }

    /**
     * 获取合同操作日志
     * @return
     */
    public List<Map<String,Object>> getContractOperationRecord(Long contractId){
        List<Map<String,Object>> dataList = new ArrayList<>();
        List<AgentUserOperationRecord> userOperationRecordList = agentUserOperationRecordDao.findByDataId(String.valueOf(contractId));
        userOperationRecordList.forEach(item -> {
            if (null != item){
                Map<String,Object> dataMap = new HashMap<>();
                dataMap.put("createTime",DateUtils.dateToString(item.getCreateTime(),"yyyy-MM-dd HH:mm"));
                dataMap.put("operatorName",item.getOperatorName());
                dataMap.put("operationType",item.getOperationType().getDesc());
                dataMap.put("operationContent",item.getNote());
                dataList.add(dataMap);
            }
        });
        return dataList;
    }

    /**
     * 新增/编辑合同基本信息操作日志
     * @param contract
     */
    public void addOrUpdateExamContractOperationRecord(AgentExamContract contract,String flag){
        //记录操作日志
        String contractOperationContent = "";
        //编辑
        if (flag.equals("edit")){
            AgentExamContract contractOld = agentExamContractPersistence.load(contract.getId());
            if (null != contractOld){
                if (!Objects.equals(contractOld.getSchoolId(), contract.getSchoolId())){
                    contractOperationContent += "学校ID，从 " + (contractOld.getSchoolId() != null ? contractOld.getSchoolId() : "空") +" 变更到 " + (contract.getSchoolId() != null ? contract.getSchoolId() : "空") +"；";
                }
                if (!Objects.equals(contractOld.getContractType(),contract.getContractType())){
                    contractOperationContent += "合同类型，从 " + (StringUtils.isNotBlank(contractOld.getContractType().getDesc()) ? contractOld.getContractType().getDesc() : "空" ) +" 变更到 " + (StringUtils.isNotBlank(contract.getContractType().getDesc()) ? contract.getContractType().getDesc() : "空" ) +"；";
                }
                if (((contractOld.getContractAmount() != null && contractOld.getContractAmount() != 0 ) || (contract.getContractAmount() != null && contract.getContractAmount() != 0))
                        && !Objects.equals(contractOld.getContractAmount(),contract.getContractAmount())){
                    contractOperationContent += "总金额，从 " + (contractOld.getContractAmount() != null ? contractOld.getContractAmount() : "空") +" 变更到 " + (contract.getContractAmount() != null ? contract.getContractAmount() : "空")+"；";
                }
                if (((contractOld.getHardwareCost() != null && contractOld.getHardwareCost() != 0 ) || (contract.getHardwareCost() != null && contract.getHardwareCost() != 0))
                        && !Objects.equals(contractOld.getHardwareCost(),contract.getHardwareCost())){
                    contractOperationContent += "硬件成本，从 " + (contractOld.getHardwareCost() != null ? contractOld.getHardwareCost() : "空") +" 变更到 " + (contract.getHardwareCost() != null ? contract.getHardwareCost() : "空") +"；";
                }
                if (!Objects.equals(DateUtils.dateToString(contractOld.getBeginDate(),DateUtils.FORMAT_SQL_DATE),DateUtils.dateToString(contract.getBeginDate(),DateUtils.FORMAT_SQL_DATE))){
                    contractOperationContent += "服务开始时间，从 "+DateUtils.dateToString(contractOld.getBeginDate(),DateUtils.FORMAT_SQL_DATE)+" 变更到 "+DateUtils.dateToString(contract.getBeginDate(),DateUtils.FORMAT_SQL_DATE)+"；";
                }
                if (!Objects.equals(DateUtils.dateToString(contractOld.getEndDate(),DateUtils.FORMAT_SQL_DATE),DateUtils.dateToString(contract.getEndDate(),DateUtils.FORMAT_SQL_DATE))){
                    contractOperationContent += "服务结束时间，从 "+DateUtils.dateToString(contractOld.getEndDate(),DateUtils.FORMAT_SQL_DATE)+" 变更到 "+DateUtils.dateToString(contract.getEndDate(),DateUtils.FORMAT_SQL_DATE)+"；";
                }
                if (!Objects.equals(DateUtils.dateToString(contractOld.getContractDate(),DateUtils.FORMAT_SQL_DATE),DateUtils.dateToString(contract.getContractDate(),DateUtils.FORMAT_SQL_DATE))){
                    contractOperationContent += "签约日期，从 "+DateUtils.dateToString(contractOld.getContractDate(),DateUtils.FORMAT_SQL_DATE)+" 变更到 "+DateUtils.dateToString(contract.getContractDate(),DateUtils.FORMAT_SQL_DATE)+"；";
                }
                if (((contractOld.getMachinesNum() != null && contractOld.getMachinesNum() != 0 ) || (contract.getMachinesNum() != null && contract.getMachinesNum() != 0))
                        && !Objects.equals(contractOld.getMachinesNum(),contract.getMachinesNum())){
                    contractOperationContent += "机器数量，从 " + (contractOld.getMachinesNum() != null ? contractOld.getMachinesNum() : "空") +" 变更到 " + (contract.getMachinesNum() != null ? contract.getMachinesNum() : "空")+"；";
                }
                if (!Objects.equals(contractOld.getMachinesType(),contract.getMachinesType())){
                    contractOperationContent += "机器型号，从 " + (StringUtils.isNotBlank(contractOld.getMachinesType()) ? contractOld.getMachinesType() : "空" ) +" 变更到 " + (StringUtils.isNotBlank(contract.getMachinesType()) ? contract.getMachinesType() : "空" ) +"；";
                }
                if (!Objects.equals(contractOld.getRemark(),contract.getRemark())){
                    contractOperationContent += "备注，从 " + (StringUtils.isNotBlank(contractOld.getRemark()) ? contractOld.getRemark() : "空" ) +" 变更到 " + (StringUtils.isNotBlank(contract.getRemark()) ? contract.getRemark() : "空" ) +"；";
                }
                if (((contractOld.getThirdPartyProductCost() != null && contractOld.getThirdPartyProductCost() != 0 ) || (contract.getThirdPartyProductCost() != null && contract.getThirdPartyProductCost() != 0))
                        && !Objects.equals(contractOld.getThirdPartyProductCost(),contract.getThirdPartyProductCost())){
                    contractOperationContent += "第三方产品成本，从 " + (contractOld.getThirdPartyProductCost() != null ? contractOld.getThirdPartyProductCost() : "空") + " 变更到 " + (contract.getThirdPartyProductCost() != null ? contract.getThirdPartyProductCost() : "空") +"；";
                }
                if (!Objects.equals(contractOld.getServiceRange(),contract.getServiceRange())){
                    List<AgentServiceRange> serviceRangeListOld = AgentServiceRange.toList(contractOld.getServiceRange());
                    List<AgentServiceRange> serviceRangeListNew = AgentServiceRange.toList(contract.getServiceRange());
                    contractOperationContent += "服务范围，从 " + (StringUtils.isNotBlank(StringUtils.join(serviceRangeListOld.stream().map(AgentServiceRange::getDesc).collect(toList()), ",")) ? StringUtils.join(serviceRangeListOld.stream().map(AgentServiceRange::getDesc).collect(toList()), ",") : "空")
                            + " 变更到 " + (StringUtils.isNotBlank(StringUtils.join(serviceRangeListNew.stream().map(AgentServiceRange::getDesc).collect(toList()), ",")) ? StringUtils.join(serviceRangeListNew.stream().map(AgentServiceRange::getDesc).collect(toList()), ",") : "空") + "；";
                }
            }
            //新增
        }else if (flag.equals("add")){
            AgentExamContractVO agentExamContractVO = new AgentExamContractVO();
            agentExamContractVO.setId(contract.getId());
            contractOperationContent += "添加合同，合同编号："+agentExamContractVO.getContractNumber()+"；";
        }
        //操作日志
//        contractOperationRecord(contract.getId(),AgentUserOperationType.CONTRACT_BASE_INFO,contractOperationContent);
        agentUserOperationRecordService.addOperationRecord(ConversionUtils.toString(contract.getId()),AgentUserOperationType.CONTRACT_BASE_INFO,contractOperationContent);
    }

    /**
     * 添加/编辑合同扩展信息操作日志
     * @param examContractExtendOld
     * @param examContractExtendNew
     */
    public void addOrUpdateExamContractExtendOperationRecord(AgentExamContractExtend examContractExtendOld,AgentExamContractExtend examContractExtendNew){
        //分成设置操作日志
        StringBuilder operationContentSplitSetting = new StringBuilder();
        //合同图片操作日志
        StringBuilder operationContentImageUrl = new StringBuilder();
        //新增
        if (null == examContractExtendOld && null != examContractExtendNew){
            /*
            分成设置
             */
            List<AgentExamContractSplitSetting> splitSettingList = examContractExtendNew.getSplitSettingList();
            splitSettingList.forEach(item->{
                if (null != item){
                    AgentUser user = baseUserService.getUser(item.getContractorId());
                    if (null != user){
                        item.setContractorName(user.getRealName());
                    }
                    Double splitProportion = null != item.getSplitProportion()? item.getSplitProportion() : 0.0;
                    operationContentSplitSetting.append("添加签约人，"+item.getContractorName()+"，分成比例"+splitProportion+"%；");
                }
            });
            /*
            合同图片
             */
            List<String> imageUrlList = examContractExtendNew.getImageUrlList();
            if (CollectionUtils.isNotEmpty(imageUrlList)){
                imageUrlList.forEach(item->{
                    operationContentImageUrl.append("添加照片，"+item);
                });
            }
            //编辑
        }else if (null != examContractExtendOld && null != examContractExtendNew){
            /*
            分成设置
             */
            List<AgentExamContractSplitSetting> splitSettingListOld = examContractExtendOld.getSplitSettingList();
            Map<Long, AgentExamContractSplitSetting> splitSettingMapOld = splitSettingListOld.stream().collect(Collectors.toMap(AgentExamContractSplitSetting::getContractorId, Function.identity(), (o1, o2) -> o1));

            List<AgentExamContractSplitSetting> splitSettingListNew = examContractExtendNew.getSplitSettingList();
            Map<Long, AgentExamContractSplitSetting> splitSettingMapNew = splitSettingListNew.stream().collect(Collectors.toMap(AgentExamContractSplitSetting::getContractorId, Function.identity(), (o1, o2) -> o1));
            //遍历分成设置新数据，与原有数据对比
            splitSettingListNew.stream().forEach(item->{
                if (null != item){
                    AgentUser user = baseUserService.getUser(item.getContractorId());
                    if (null != user){
                        item.setContractorName(user.getRealName());
                    }
                    double splitProportionNew = null != item.getSplitProportion() ? item.getSplitProportion() : 0.0;
                    if (splitSettingMapOld.containsKey(item.getContractorId())){
                        AgentExamContractSplitSetting splitSettingOld = splitSettingMapOld.get(item.getContractorId());
                        if (null != splitSettingOld){
                            double splitProportionOld = null != splitSettingOld.getSplitProportion() ? splitSettingOld.getSplitProportion() : 0.0;
                            if (!Objects.equals(splitSettingOld.getSplitProportion(), item.getSplitProportion())){
                                operationContentSplitSetting.append("修改签约人，从 "+item.getContractorName()+splitProportionOld+" 变更为 "+splitProportionNew+"；");
                            }
                        }
                    }else{
                        operationContentSplitSetting.append("添加签约人，"+item.getContractorName()+",分成比例"+splitProportionNew+"%；");
                    }
                }
            });
            //遍历分成设置原有数据，与新数据对比
            splitSettingListOld.stream().forEach(item->{
                if (null != item){
                    AgentUser user = baseUserService.getUser(item.getContractorId());
                    if (null != user){
                        item.setContractorName(user.getRealName());
                    }
                    double splitProportion = null != item.getSplitProportion() ? item.getSplitProportion() : 0.0;
                    if (!splitSettingMapNew.containsKey(item.getContractorId())){
                        operationContentSplitSetting.append("删除签约人，"+item.getContractorName()+",分成比例"+splitProportion+"%；");
                    }
                }
            });
            /*
            合同图片
             */
            List<String> imageUrlListOld = examContractExtendOld.getImageUrlList();
            List<String> imageUrlListNew = examContractExtendNew.getImageUrlList();
            //遍历合同图片URL新数据，与原有数据对比
            if (CollectionUtils.isNotEmpty(imageUrlListNew)){
                imageUrlListNew.forEach(item->{
                    if (null != item){
                        if (CollectionUtils.isEmpty(imageUrlListOld) || (CollectionUtils.isNotEmpty(imageUrlListOld) && !imageUrlListOld.contains(item))){
                            operationContentImageUrl.append("添加图片，"+item+"；");
                        }
                    }
                });
            }
            //遍历合同图片URL原有数据，与新数据对比
            if (CollectionUtils.isNotEmpty(imageUrlListOld)){
                imageUrlListOld.forEach(item->{
                    if (null != item){
                        if (CollectionUtils.isEmpty(imageUrlListNew) || (CollectionUtils.isNotEmpty(imageUrlListNew) && !imageUrlListNew.contains(item))){
                            operationContentImageUrl.append("删除照片，"+item+"；");
                        }
                    }
                });
            }
        }
//        contractOperationRecord(examContractExtendNew.getContractId(),AgentUserOperationType.CONTRACT_BASE_INFO,String.valueOf(operationContentSplitSetting));
//        contractOperationRecord(examContractExtendNew.getContractId(),AgentUserOperationType.CONTRACT_BASE_INFO,String.valueOf(operationContentImageUrl));
        agentUserOperationRecordService.addOperationRecord(ConversionUtils.toString(examContractExtendNew.getId()),AgentUserOperationType.CONTRACT_BASE_INFO,ConversionUtils.toString(operationContentSplitSetting));
        agentUserOperationRecordService.addOperationRecord(ConversionUtils.toString(examContractExtendNew.getId()),AgentUserOperationType.CONTRACT_BASE_INFO,ConversionUtils.toString(operationContentImageUrl));
    }

    /**
     * 新增合同回款信息操作日志
     * @param examContractPayback
     */
    public void addContractPaybackOperationRecord(AgentExamContractPayback examContractPayback){
        StringBuilder contractPaybackOperationContent = new StringBuilder();
        AgentExamContractPaybackVO examContractPaybackVO = new AgentExamContractPaybackVO();
        examContractPaybackVO.setId(examContractPayback.getId());
        String paybackDate = null != examContractPayback.getPaybackDate() ? DateUtils.dateToString(examContractPayback.getPaybackDate(),DateUtils.FORMAT_SQL_DATE):"";
        contractPaybackOperationContent.append("添加回款，编号"+examContractPaybackVO.getPaybackNumber()+",金额"+examContractPayback.getPaybackAmount()+"，日期"+paybackDate+"；");
//        contractOperationRecord(examContractPayback.getContractId(),AgentUserOperationType.CONTRACT_PAYBACK_INFO,String.valueOf(contractPaybackOperationContent));
        agentUserOperationRecordService.addOperationRecord(ConversionUtils.toString(examContractPayback.getContractId()),AgentUserOperationType.CONTRACT_PAYBACK_INFO,ConversionUtils.toString(contractPaybackOperationContent));
    }
}
