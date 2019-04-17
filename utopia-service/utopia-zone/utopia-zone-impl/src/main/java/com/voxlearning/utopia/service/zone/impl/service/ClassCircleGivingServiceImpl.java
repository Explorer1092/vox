/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.zone.api.BrainActivityService;
import com.voxlearning.utopia.service.zone.api.ChickenWeightService;
import com.voxlearning.utopia.service.zone.api.ClassCircleGivingService;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRecord;
import com.voxlearning.utopia.service.zone.api.entity.ClazzCircleRewardNotice;
import com.voxlearning.utopia.service.zone.api.entity.boss.AwardDetail;
import com.voxlearning.utopia.service.zone.api.entity.boss.ClazzBossAward;
import com.voxlearning.utopia.service.zone.api.entity.boss.ClazzBossUserAward;
import com.voxlearning.utopia.service.zone.api.entity.boss.StudentInfo;
import com.voxlearning.utopia.service.zone.api.entity.giving.*;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotRewardConfig;
import com.voxlearning.utopia.service.zone.impl.manager.ActivityClazzRankCacheManager;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzActivityRecordPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzCircleRewardNoticePersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.boss.ClazzBossAwardPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.giving.ChickenClazzProgressPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.giving.ChickenHelpPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.giving.ChickenStudentRecordPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.giving.SelfActivityHelpPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.plot.PlotRewardConfigPersistence;
import com.voxlearning.utopia.service.zone.impl.queue.ClazzBossRewardQueueProducer;
import com.voxlearning.utopia.service.zone.impl.support.ClazzActivityCacheManager;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Named("com.voxlearning.utopia.service.zone.impl.service.ClassCircleGivingServiceImpl")
@ExposeService(interfaceClass = ClassCircleGivingService.class, version = @ServiceVersion(version = "20181114"))
@Slf4j
public class ClassCircleGivingServiceImpl implements ClassCircleGivingService {

    @Resource
    private ChickenHelpPersistence chickenHelpPersistence;
    @Resource
    private SelfActivityHelpPersistence selfActivityHelpPersistence;
    @Resource
    private ClazzActivityRecordPersistence clazzActivityRecordPersistence;
    @Resource
    private StudentLoaderClient studentLoaderClient;
    @Resource
    private ClazzActivityCacheManager clazzActivityCacheManager;
    @Resource
    private PlotRewardConfigPersistence plotRewardConfigPersistence;
    @Resource
    private ClazzBossAwardPersistence clazzBossAwardPersistence;

    private static final int TOTAL_COMPOSE_PROBABILITY=100, COMPOSE_PROBABILITY = 10;
    @Resource
    private ChickenStudentRecordPersistence chickenStudentRecordPersistence;
    @Resource
    private ChickenClazzProgressPersistence chickenClazzProgressPersistence;
    @Resource
    private ChickenWeightService chickenWeightService;
    @Inject
    private ActivityClazzRankCacheManager activityClazzRankCacheManager;
    @Resource
    private ClazzBossRewardQueueProducer clazzBossRewardQueueProducer;

    protected final AtomicLockManager atomicLockManager = AtomicLockManager.instance();

    @Resource
    private ClazzCircleRewardNoticePersistence clazzCircleRewardNoticePersistence;
    @Resource
    private BrainActivityService brainActivityService;

    //查询 烤箱 托盘 火鸡用户list
    @Override
    public Map<Integer, List<ChickenHelpResponse>> findClazzStudentList(Integer activityId, Long userId, Long schoolId, Long clazzId) {
        List<ChickenHelp> clazzList = chickenHelpPersistence.findByClazzId(activityId, schoolId, clazzId);

        Map<Integer, List<ChickenHelpResponse>> resultMap = new HashMap<>();
        if (clazzList != null && clazzList.size() > 0 ){
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
            if (studentDetail != null){
                //烤箱
                List<ChickenHelpResponse> ovenList = chickenHelpCovert(clazzList, 1, userId);
                resultMap.put(1,ovenList);
                //托盘
                List<ChickenHelpResponse> trayList = chickenHelpCovert(clazzList, 2, userId);
                resultMap.put(2,trayList);
                //火鸡
                List<ChickenHelpResponse> chickenList = chickenHelpCovert(clazzList, 3, userId);
                resultMap.put(3,chickenList);
            }
        }
        return resultMap;
    }

    //邀请助力
    @Override
    public Boolean inviteHelp(Integer activityId, Long userId, Long schoolId, Long clazzId,Integer type) {
        boolean isHelp = false;
        List<ChickenHelp> userList = chickenHelpPersistence.findByUserId(activityId, schoolId, clazzId, userId);
        if (userList != null && userList.size()>0){
            userList = userList.stream().filter(item ->item.getId().contains(DateUtils.dateToString(new Date(),DateUtils.FORMAT_SQL_DATE))).collect(Collectors.toList());
        }
        ClassCircleChickenWeight classCircleChickenWeight = chickenWeightService.queryClassCircleChickenWeight("1");
        int todayMaxCount = 0;
        if (classCircleChickenWeight != null){
            if(classCircleChickenWeight.getConfigs() != null)
                todayMaxCount = classCircleChickenWeight.getConfigs().get("4");
        }
        //说明今天还没有邀请助力
        if (userList == null || userList.size() == 0){
            isHelp = true;
        }

        //判断是否超过今日邀请次数
        if (userList.size() < todayMaxCount){
            ChickenHelp chickenHelp = userList.stream().filter(item -> !item.getStatus()&&item.getType().intValue()==type.intValue()).findFirst().orElse(null);
            //还没有添加该类型的助力
            if (chickenHelp == null){
                isHelp = true;
            }
        }

        //可以邀请助力 保存数据
        if (isHelp){
            ChickenHelp chickenHelp = new ChickenHelp();
            chickenHelp.setActivityId(activityId);
            chickenHelp.setClazzId(clazzId);
            chickenHelp.setSchoolId(schoolId);
            chickenHelp.setUserId(userId);
            chickenHelp.setStatus(false);
            chickenHelp.setTipStatus(false);
            chickenHelp.setType(type);
            chickenHelp.generateId();
            chickenHelp.setUserList(new ArrayList<>());
            chickenHelpPersistence.upsert(chickenHelp);
        }

        return isHelp;
    }

    //获取给你助力弹窗
    @Override
    public List<ChickenRewardResponse> findChickenStudentList(Integer activityId, Long userId, Long schoolId, Long clazzId) {
        List<ChickenHelp> userList = chickenHelpPersistence.findByUserId(activityId, schoolId, clazzId, userId);

        if (userList != null){
            List<ChickenHelp> newList = userList.stream().filter(item -> item.getUserList() != null && !item.getTipStatus() && item.getUserList().size() == 3 && item.getStatus()).collect(Collectors.toList());
            if (newList.size() > 0){
                return newList.stream().map(item ->{
                    //更新状态
                    item.setTipStatus(true);
                    chickenHelpPersistence.upsert(item);

                    //组装返回数据
                    ChickenRewardResponse chickenRewardResponse = new ChickenRewardResponse();
                    //获取助力学生list
                    List<ChickenStudentResponse> stuList = item.getUserList().stream().map(obj -> {
                        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(obj);
                        ChickenStudentResponse csr = new ChickenStudentResponse();
                        csr.setName(studentDetail.fetchRealname());
                        csr.setStudentPic(studentDetail.fetchImageUrl());
                        return csr;
                    }).collect(Collectors.toList());
                    chickenRewardResponse.setStudentList(stuList);

                    //获取奖励
                    Integer weightType = item.getType();
                    chickenRewardResponse.setType(weightType);
                    ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(activityId + "_" + schoolId + "_" + clazzId + "_" + userId);
                    if (clazzActivityRecord != null && clazzActivityRecord.getBizObject() != null){
                        ChickenStudent chickenStudent = JsonUtils.fromJson(JsonUtils.toJson(clazzActivityRecord.getBizObject()), ChickenStudent.class);
                        String configId = PlotRewardConfig.generateId(activityId, weightType);
                        if(chickenStudent.getChickenMap() == null) {
                            Map<String, Integer> map = new HashMap<>();
                            map.put(configId,0);
                            chickenStudent.setChickenMap(map);
                        }
                        Integer count = chickenStudent.getChickenMap().get(configId);
                        if (count != null){
                            chickenStudent.getChickenMap().put(configId,count + 1);
                        }else {
                            chickenStudent.getChickenMap().put(configId,1);
                        }
                        clazzActivityRecord.setBizObject(chickenStudent);
                        clazzActivityRecordPersistence.upsert(clazzActivityRecord);
                    }
                    return chickenRewardResponse;
                }).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    //维护chickenHelp
    @Override
    public Boolean helpClassmate(Integer activityId, Long userId, Long schoolId, Long clazzId, String chickenHelpId) {
        String lockKey = "clazz_circle_thanks_giving_lock_" + chickenHelpId;
        try {
            atomicLockManager.acquireLock(lockKey, 3);
            ChickenHelp chickenHelp = chickenHelpPersistence.load(chickenHelpId);
            if (chickenHelp != null && !chickenHelp.getStatus()){
                if (chickenHelp.getUserList() == null) chickenHelp.setUserList(new ArrayList<>());
                if (chickenHelp.getUserList().size() < 3){
                    chickenHelp.getUserList().add(userId);
                }
                if (chickenHelp.getUserList().size() == 3) chickenHelp.setStatus(true);
                chickenHelpPersistence.upsert(chickenHelp);
                return true;
            }
        }catch (Exception e){
            return false;
        }finally {
            atomicLockManager.releaseLock(lockKey);
        }
        return false;
    }

    //班级参加人数和列表
    @Override
    public ChickenClazzResponse findClassChickenList(Integer activityId, Long userId, Long schoolId, Long clazzId) {
        ChickenClazzResponse ccr = new ChickenClazzResponse();
        List<ClazzBossAward> list = clazzBossAwardPersistence.getList(activityId);
        if (list != null){
            //过滤 班级类型
            list = list.stream().filter(item -> item.getSelfOrClazz() == 1).collect(Collectors.toList());
            if (list.size() > 0){
                ccr.setJoinCount(clazzActivityCacheManager.loadChickenCountByKey(clazzId.toString()).intValue());
                ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(activityId + "_" + schoolId + "_" + clazzId + "_" + userId);
                List<ChickenClazzRewardResponse> rewardResponses = new ArrayList<>();
                List <Long> userIds = studentLoaderClient.loadClazzStudentIds(clazzId);
                list.forEach(item ->{
                    ChickenClazzRewardResponse ccrr = new ChickenClazzRewardResponse();
                    ccrr.setAwards(item.getAwards());
                    ccrr.setIsReceive(false);
                    ccrr.setId(item.getId());
                    Double targetCount = item.getTargetValue() * userIds.size();//班级目标打怪数
                    ccrr.setTargetValue(targetCount.intValue());
                    if (clazzActivityRecord != null && clazzActivityRecord.getBizObject() != null){
                        ChickenStudent chickenStudent = JsonUtils.fromJson(JsonUtils.toJson(clazzActivityRecord.getBizObject()), ChickenStudent.class);
                        if (chickenStudent.getUserAwardMap() != null){
                            ClazzBossUserAward clazzBossUserAward = chickenStudent.getUserAwardMap().get(item.getId());
                            if (clazzBossUserAward != null) ccrr.setIsReceive(clazzBossUserAward.getReceive());
                        }
                    }
                    rewardResponses.add(ccrr);
                });
                ccr.setTargetList(rewardResponses);
            }
        }
        return ccr;
    }

    //吃鸡 同学 弹消息列表
    @Override
    public List<ChickenStudentRecordResponse> findEatChickenList(Integer activityId, Long userId, Long schoolId, Long clazzId) {
        //初始化 班级 统计表
        ChickenClazzProgress clazzProgress = chickenClazzProgressPersistence.load(activityId + "_" + schoolId + "_" + clazzId);
        if (clazzProgress == null){
            clazzProgress = new ChickenClazzProgress();
            clazzProgress.setActivityId(activityId);
            clazzProgress.setClazzId(clazzId);
            clazzProgress.setSchoolId(schoolId);
            clazzProgress.generateId();
            clazzProgress.setCount(0L);
            chickenClazzProgressPersistence.upsert(clazzProgress);
        }

        //消息列表
        List<ChickenStudentRecord> chickenStudentRecords = chickenStudentRecordPersistence.findByClazzId(activityId, schoolId, clazzId);
        List<ChickenStudentRecordResponse> resultList = new ArrayList<>();
        if (chickenStudentRecords != null && chickenStudentRecords.size() > 0){
            resultList = chickenStudentRecords.stream().map(item -> {
                ChickenStudentRecordResponse recordResponse = new ChickenStudentRecordResponse();
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(item.getUserId());
                recordResponse.setName(studentDetail.fetchRealname());
                recordResponse.setType(item.getType());
                return recordResponse;
            }).collect(Collectors.toList());
        }
        return resultList.size() > 20 ? resultList.subList(0,20) : resultList;
    }

    //对象转换 加排序
    private List<ChickenHelpResponse> chickenHelpCovert(List<ChickenHelp> clazzList, Integer type, Long userId) {
        List<ChickenHelp> newClazzList = clazzList.stream().filter(Objects::nonNull).filter(item -> item.getType().intValue() == type.intValue() && !item.getStatus()).collect(Collectors.toList());
        newClazzList.sort(comparing(ChickenHelp::getCt));
        Map<Long, ChickenHelp> map = new HashMap<>();
        newClazzList.forEach(item -> {
            if (!map.containsKey(item.getUserId())) map.put(item.getUserId(), item);
        });
        newClazzList.clear();
        map.forEach((k, v) -> newClazzList.add(v));
        newClazzList.sort(comparing(ChickenHelp::getCt));
        return newClazzList.stream().map(item -> {
            StudentDetail studentDetail1 = studentLoaderClient.loadStudentDetail(item.getUserId());
            ChickenHelpResponse chr = new ChickenHelpResponse();
            chr.setUserId(studentDetail1.getId());
            chr.setName(studentDetail1.fetchRealname());
            chr.setPic(studentDetail1.fetchImageUrl());
            chr.setId(item.getId());
            List<Long> userList = item.getUserList();
            chr.setCount(userList == null ? 0 : userList.size());
            return chr;
        }).collect(Collectors.toList());
    }

    /**助力他人*/
    @Override
    public MapMessage helpOtherUser(Integer activityId, Long schoolId, Long clazzId,Long userId,String ahId){
        ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activityId,schoolId,clazzId,userId));
        if(clazzActivityRecord ==null||clazzActivityRecord.getBizObject()==null) return MapMessage.errorMessage("未创建活动数据").add("code","notInitActivityData");

        Object object = clazzActivityRecord.getBizObject();
        ChickenStudent chickenStudent = JsonUtils.fromJson(JsonUtils.toJson(object),ChickenStudent.class);
        if(chickenStudent==null) return MapMessage.errorMessage("活动属性类型错误").add("code","activityParamError");
        if(chickenStudent.getChickenMap()==null) return MapMessage.errorMessage("活动属性类型错误").add("code","activityParamError");

        ChickenHelp chickenHelp = chickenHelpPersistence.load(ahId);
        if(chickenHelp==null) return MapMessage.errorMessage("未找到对方的助力请求").add("code","notFoundOtherHelp");

        if(userId.longValue() == chickenHelp.getUserId().longValue()) return MapMessage.errorMessage("不能为自己助力哦").add("code","cantHelpSelf");

        if(chickenHelp.getClazzId().longValue()!=clazzId.longValue()) return MapMessage.errorMessage("只能帮本班同学助力哦").add("code","onlyHelpSameClazz");

        if(chickenHelp.getStatus()) return MapMessage.errorMessage("同学此次助力已经完成啦~").add("code","otherFinished");

        SelfActivityHelp selfActivityHelp = selfActivityHelpPersistence.load(SelfActivityHelp.generateId(activityId,schoolId,clazzId,userId));
        if(selfActivityHelp==null){
            selfActivityHelp = new SelfActivityHelp();
            selfActivityHelp.setNum(0);
            selfActivityHelp.setActivityId(activityId);
            selfActivityHelp.setSchoolId(schoolId);
            selfActivityHelp.setClazzId(clazzId);
            selfActivityHelp.setUserId(userId);
            selfActivityHelp.generateId();
        }
        if(selfActivityHelp.getNum()>=3) return MapMessage.errorMessage("今天已经帮同学助力3次啦~").add("code","helpEnough");
        selfActivityHelp.setNum(selfActivityHelp.getNum()+1);
        //维护chickenHelp
        Boolean helpResult = helpClassmate(activityId,userId,schoolId,clazzId,ahId);
        if(!helpResult) return MapMessage.errorMessage("助力失败啦，稍等一会才来哦~").add("code","helpFailed");
        SelfActivityHelp result = selfActivityHelpPersistence.upsert(selfActivityHelp);
        if(result!=null) {
            Student student = studentLoaderClient.loadStudent(chickenHelp.getUserId());
            StudentInfo studentInfo = new StudentInfo();
            studentInfo.setPic(student.fetchImageUrl());
            studentInfo.setName(student.fetchRealname());
            if(result.getNum()==3){
                int goodsType = chickenWeightService.weightType();
                String goodsId = PlotRewardConfig.generateId(activityId,goodsType);
                Map<String,Integer> chickenMap = chickenStudent.getChickenMap();
                if(chickenMap!=null&&chickenMap.get(goodsId)!=null){
                    chickenMap.put(goodsId,chickenMap.get(goodsId)+1);
                }
                clazzActivityRecord.setBizObject(chickenStudent);
                clazzActivityRecordPersistence.upsert(clazzActivityRecord);
                return MapMessage.successMessage("助力成功").add("code","success").add("times",result.getNum()).add("goodsType",goodsType);
            }else if(result.getNum()<3) {
                return MapMessage.successMessage("助力成功").add("code","success").add("times",result.getNum()).add("stu",studentInfo);
            }
        }
        return MapMessage.errorMessage("系统超时~").add("code","systemTimeout");

    }

    /**
     * 获取个人活动进度 （感恩节吃鸡）
     * */
    @Override
    public MapMessage getSelfActivityProgress(Integer activityId, Long schoolId, Long clazzId, Long userId){
        SelfActivityProgress selfActivityProgress = new SelfActivityProgress();
        List<SelfProgressAward> selfProgressAwardList = new ArrayList<>();
        List<ActivityGoods> activityGoodsList = new ArrayList<>();
        selfActivityProgress.setProgress(0);
        selfActivityProgress.setIsHave(haveChance(activityId, userId, schoolId, clazzId));     //是否有助力次数
        selfActivityProgress.setAwardList(selfProgressAwardList);
        selfActivityProgress.setGoodsList(activityGoodsList);
        ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activityId,schoolId,clazzId,userId));
        if(clazzActivityRecord ==null||clazzActivityRecord.getBizObject()==null) return MapMessage.errorMessage("未创建活动数据").add("code","notInitActivityData").add("data",selfActivityProgress);

        Object object = clazzActivityRecord.getBizObject();
        ChickenStudent chickenStudent = JsonUtils.fromJson(JsonUtils.toJson(object),ChickenStudent.class);
        if(chickenStudent!=null&&chickenStudent.getChickenMap()!=null){
            selfActivityProgress.setProgress(clazzActivityRecord.getScore());           //进度数
            List<ClazzBossAward> cbaList= clazzBossAwardPersistence.getList(activityId,ClazzBossAward.SELF);
            Map<String,ClazzBossUserAward> userAwardMap =  chickenStudent.getUserAwardMap();
            Map<String,Integer> chickenMap = chickenStudent.getChickenMap();
            //用户活动进度
            cbaList.stream().filter(Objects::nonNull).forEach(cba -> {
                ClazzBossUserAward clazzBossUserAward = userAwardMap.get(cba.getId());
                if(clazzBossUserAward!=null){
                    SelfProgressAward selfProgressAward = new SelfProgressAward();
                    selfProgressAward.setAwards(cba.getAwards());
                    selfProgressAward.setReceive(clazzBossUserAward.getReceive());
                    selfProgressAward.setSelfTarget(cba.getTargetValue().intValue());
                    selfProgressAward.setType(cba.getType());
                    selfProgressAward.setActivityAwardId(cba.getId());
                    selfProgressAwardList.add(selfProgressAward);
                }
            });
            //用户活动物品
            List<PlotRewardConfig> prcList = plotRewardConfigPersistence.findByActivityId(activityId);
            prcList.stream().filter(Objects::nonNull).forEach(prc->{
                ActivityGoods activityGoods = new ActivityGoods();
                activityGoods.setType(prc.getType());
                activityGoods.setName(prc.getName());
                activityGoods.setNum(chickenMap.get(prc.getId())!=null?chickenMap.get(prc.getId()):0);
                activityGoodsList.add(activityGoods);
            });
            return MapMessage.successMessage("成功").add("code","success").add("data",selfActivityProgress);
        }
        return MapMessage.errorMessage("活动属性类型错误").add("code","activityParamError").add("data",selfActivityProgress);
    }

    //班级吃鸡进度统计
    @Override
    public List<ChickenClazzProgressResponse> findClazzProgressList(Integer activityId, Integer len, Integer pageSize) {
        Integer count = chickenClazzProgressPersistence.findCountByActivityId(activityId).intValue();
        int page = count / pageSize;
        int p = count % pageSize;
        if (p > 0) page += 1;
        List<ChickenClazzProgressResponse> resultList = new ArrayList<>();
        for (int j = 0; j < page; j++) {
            List<ChickenClazzProgress> list = chickenClazzProgressPersistence.findByActivityId(activityId, j, 10000);
            if (list != null && list.size() > 0) {
                list.forEach(item -> item.setCount(clazzActivityCacheManager.loadChickenCountByKey(item.getClazzId().toString())));
                list = list.stream().filter(item -> item.getCount().intValue() > 0).collect(Collectors.toList());
                list.sort(comparing(ChickenClazzProgress::getCount).reversed());
                if (list.size() > 0) {
                    for (int i = 0; i < len; i++) {
                        ChickenClazzProgressResponse cp = new ChickenClazzProgressResponse();
                        cp.setClazzCount(0);
                        cp.setCount(i + 1);
                        for (ChickenClazzProgress clazzProgress : list) {
                            if (clazzProgress.getCount() >= i + 1) {
                                cp.setClazzCount(cp.getClazzCount() + 1);
                            }
                        }
                        resultList.add(cp);
                    }
                }

            }
        }
        resultList.sort(comparing(ChickenClazzProgressResponse::getCount));

        return resultList;
    }

    /**合成活动物品 （烹饪火鸡）*/
    @Override
    public MapMessage composeActivityGoods(Integer activityId, Long schoolId, Long clazzId, Long userId){
        ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activityId,schoolId,clazzId,userId));
        if(clazzActivityRecord ==null||clazzActivityRecord.getBizObject()==null) return MapMessage.errorMessage("未创建活动数据").add("code","notInitActivityData");

        Object object = clazzActivityRecord.getBizObject();
        ChickenStudent chickenStudent = JsonUtils.fromJson(JsonUtils.toJson(object),ChickenStudent.class);
        if(chickenStudent!=null&&chickenStudent.getChickenMap()!=null){
            Map<String,Integer> chickenMap = chickenStudent.getChickenMap();
            List<PlotRewardConfig> prcList = plotRewardConfigPersistence.findByActivityId(activityId);
            for(PlotRewardConfig prc : prcList){
                Integer num = chickenMap.get(prc.getId());
                if(num==null||num<=0)
                    return MapMessage.errorMessage("缺少"+prc.getName()).add("code","activityGoodsNotEnough");
                num = num-1;
                chickenMap.put(prc.getId(),num);
            }
            clazzActivityRecord.setScore(clazzActivityRecord.getScore()+1);
            clazzActivityRecord.setBizObject(chickenStudent);
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
            Clazz clazz = studentDetail.getClazz();

            ClazzActivityRecord result = clazzActivityRecordPersistence.upsert(clazzActivityRecord);
            if(result!=null){
                int type = 0;
                if(RandomUtils.nextInt(TOTAL_COMPOSE_PROBABILITY)<COMPOSE_PROBABILITY){   //按照几率吃鸡
                    type=1;
                }
                //记录一条合成记录
                ChickenStudentRecord chickenStudentRecord = new ChickenStudentRecord();
                chickenStudentRecord.setActivityId(activityId);
                chickenStudentRecord.setSchoolId(schoolId);
                chickenStudentRecord.setClazzId(clazzId);
                chickenStudentRecord.setType(type);
                chickenStudentRecord.setUserId(userId);
                chickenStudentRecord.generateId();
                chickenStudentRecordPersistence.upsert(chickenStudentRecord);
                brainActivityService.updateAllRank(activityId, schoolId, clazz.getClazzLevel().getLevel(), clazzId, userId, 1);

                return MapMessage.successMessage("成功").add("code","success").add("type",type);
            }
            return MapMessage.errorMessage("系统超时~").add("code","systemTimeout");
        }
        return MapMessage.errorMessage("活动属性类型错误").add("code","activityParamError");
    }
    /**增加参加班级人数**/
    @Override
    public void addClassCount(String classId) {
        clazzActivityCacheManager.increaseChickenCountByKey(classId);
    }

    @Override
    public MapMessage receiveSelfRank(String activityAwardId, Long userId,Long schooleId,Long clazzId) {
        String id = ClazzActivityRecord.generateId(4,schooleId,clazzId,userId);
        ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(id);
        ClazzBossAward clazzBossAward =clazzBossAwardPersistence.load(activityAwardId);
        if(clazzActivityRecord==null||clazzBossAward==null||clazzBossAward.getTargetValue()==null){
            return MapMessage.errorMessage("未达到个人目标值");
        }
        if(clazzBossAward.getTargetValue().intValue()>clazzActivityRecord.getScore()){
            return MapMessage.errorMessage("未达到个人目标值");
        }
        ChickenStudent chickenStudent = JsonUtils.fromJson(JsonUtils.toJson(clazzActivityRecord.getBizObject()),ChickenStudent.class);
        Map<String,ClazzBossUserAward>  clazzBossUserAwardMap =chickenStudent.getUserAwardMap();
        ClazzBossUserAward clazzBossUserAward = clazzBossUserAwardMap.get(activityAwardId);
        if(clazzBossUserAward.getReceive()){
            return MapMessage.errorMessage("您已领取个人奖励");
        }
        clazzBossUserAward.setReceive(true);
        clazzActivityRecord.setBizObject(chickenStudent);
        clazzActivityRecordPersistence.upsert(clazzActivityRecord);
        //推送PHP
        List<AwardDetail> awardDetails =clazzBossAward.getAwards();
        Map<String,Object> map = new HashMap <>();
        awardDetails.forEach(e->{
            if(e.getType()==1){
                map.put("CHEST_GENERAL",e.getNum());
            }
            if(e.getType()==2){
                map.put("CHEST_MIDDLE",e.getNum());
            }
            if(e.getType()==3){
                map.put("CHEST_ADVANCED",e.getNum());
            }
            if(e.getType()==4){
                map.put("VW00003",e.getNum());
            }
        });
        sendReward(map, userId);
        return MapMessage.successMessage();

    }

    @Override
    public MapMessage receiveClassRank(String activityAwardId, Long userId, Long schooleId,
        Long clazzId) {
        ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(4,schooleId,clazzId,userId));
        if(clazzActivityRecord.getBizObject()==null){
            return MapMessage.errorMessage("活动没有参加");
        }
        List <Long> userIds = studentLoaderClient.loadClazzStudentIds(clazzId);
        ChickenStudent chickenStudent = JsonUtils.fromJson(JsonUtils.toJson(clazzActivityRecord.getBizObject()),ChickenStudent.class);
        if(!chickenStudent.getJoinClass()){
            return MapMessage.errorMessage("你还没有加入班级，赶快点击获取物品去小U中答题吧，答题成功才可领取~");
        }
        ClazzBossAward clazzBossAward =clazzBossAwardPersistence.load(activityAwardId);
        //班级目标打怪数
        Double targetCount = clazzBossAward.getTargetValue() * userIds.size();
        if(targetCount.intValue()<=clazzActivityCacheManager.loadChickenCountByKey(clazzId.toString()).intValue()
            &&chickenStudent.getJoinClass()){
            Map<String,ClazzBossUserAward>  clazzBossUserAwardMap =chickenStudent.getUserAwardMap();
            ClazzBossUserAward clazzBossUserAward = clazzBossUserAwardMap.get(activityAwardId);
            if(clazzBossUserAward.getReceive()){
                return MapMessage.errorMessage("您已领取班级奖励");
            }
            List<AwardDetail> awardDetails =clazzBossAward.getAwards();
            awardDetails.forEach(e->{
                if(e.getType()==5){
                    Integer  score =Integer.valueOf(e.getNum());
                    clazzBossUserAward.setReceive(true);
                    clazzBossUserAwardMap.put(activityAwardId,clazzBossUserAward);
                    chickenStudent.setUserAwardMap(clazzBossUserAwardMap);
                    clazzActivityRecord.setBizObject(chickenStudent);
                    clazzActivityRecord.setScore(clazzActivityRecord.getScore()+score);
                    clazzActivityRecordPersistence.upsert(clazzActivityRecord);
                }
            });
            return MapMessage.successMessage();
        }else{
            return MapMessage.errorMessage("未达到班级目标");
        }
    }

    public void sendReward(Map<String, Object> map, Long studentId) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> info = new HashMap<>();
        resultMap.put("rewards", map);
        resultMap.put("success", true);
        resultMap.put("event", "chicken_reward");
        resultMap.put("studentId", studentId);
        resultMap.put("subject","all");
        Message message = Message.newMessage();
        String json = JsonUtils.toJson(resultMap);
        message.withPlainTextBody(json);
        clazzBossRewardQueueProducer.getRewordProducer().produce(message);
    }

    //用户id
    @Override
    public Long increaseCountByStudentId(Long userId) {
        return clazzActivityCacheManager.increaseClazzBossCountByKey(userId.toString()+"clazz_circle_chicken");
    }

    public Long loadCountByStudentId(Long userId) {
        return clazzActivityCacheManager.loadClazzBossCountByKey(userId.toString()+"clazz_circle_chicken");
    }

    //获取学科
    @Override
    public String getStudentSubject(Long userId) {
        String chinese = "chinese";
        String english = "english";
        String math = "math";
        Long aLong = loadCountByStudentId(userId);
        if (aLong != null) {
            int count = loadCountByStudentId(userId).intValue();
            int value = count % 3;
            if (value == 1) {
                return chinese;
            } else if (value == 2) {
                return math;
            } else {
                return english;
            }
        } else {
            return english;
        }
    }

    @Override
    public List<ClazzCircleRewardNotice> queryClazzCircleRewardNotice(Integer activityId, Long userId,Long schoolId,Long classId) {
        List<ClazzCircleRewardNotice> clazzCircleRewardNotices= clazzCircleRewardNoticePersistence.findByUserId(activityId,userId);
        clazzCircleRewardNotices=clazzCircleRewardNotices.stream().filter(e-> !e.getIsShow()).collect(Collectors.toList());
      if(clazzCircleRewardNotices!=null){
          ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activityId,schoolId,classId,userId));
          clazzCircleRewardNotices.forEach(e->{
            e.setIsShow(true);
            clazzCircleRewardNoticePersistence.upsert(e);
            String goodsId = PlotRewardConfig.generateId(4,e.getRewardType());
            ChickenStudent chickenStudent = JsonUtils.fromJson(JsonUtils.toJson(clazzActivityRecord.getBizObject()),ChickenStudent.class);
            Integer value =chickenStudent.getChickenMap().get(goodsId);
            chickenStudent.getChickenMap().put(String.valueOf(goodsId),value+1);
            clazzActivityRecord.setBizObject(chickenStudent);
            clazzActivityRecordPersistence.upsert(clazzActivityRecord);
        });
      }
      return clazzCircleRewardNotices;
    }
    @Override
    public void upsetClazzCircleRewardNotice(Integer activityId, Long userId, Boolean isShow, Integer rewardType) {
            ClazzCircleRewardNotice  clazzCircleRewardNotice = new ClazzCircleRewardNotice();
            clazzCircleRewardNotice.setId(ClazzCircleRewardNotice.generateId(activityId,userId,rewardType));
            clazzCircleRewardNotice.setActivityId(activityId);
            clazzCircleRewardNotice.setIsShow(isShow);
            clazzCircleRewardNotice.setUserId(userId);
            clazzCircleRewardNotice.setRewardType(rewardType);
            clazzCircleRewardNoticePersistence.upsert(clazzCircleRewardNotice);
    }

    //查询是否有邀请机会
    public Boolean haveChance(Integer activityId, Long userId, Long schoolId, Long clazzId) {
        List<ChickenHelp> userList = chickenHelpPersistence.findByUserId(activityId, schoolId, clazzId, userId);
        if (userList != null && userList.size() > 0) {
            userList = userList.stream().filter(item -> item.getId().contains(DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE))).collect(Collectors.toList());
        }
        ClassCircleChickenWeight classCircleChickenWeight = chickenWeightService.queryClassCircleChickenWeight("1");
        int todayMaxCount = 0;
        if (classCircleChickenWeight != null) {
            if (classCircleChickenWeight.getConfigs() != null)
                todayMaxCount = classCircleChickenWeight.getConfigs().get("4");
        }
        //说明今天还没有邀请助力
        if (userList == null || userList.size() == 0) {
            return true;
        }

        //判断是否超过今日邀请次数
        if (userList.size() < todayMaxCount) {
            return true;
        }
        return false;
    }
}

