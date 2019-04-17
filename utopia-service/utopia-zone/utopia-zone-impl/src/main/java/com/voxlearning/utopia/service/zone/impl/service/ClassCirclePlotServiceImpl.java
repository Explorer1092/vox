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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.zone.api.BrainActivityService;
import com.voxlearning.utopia.service.zone.api.ClassCirclePlotService;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRecord;
import com.voxlearning.utopia.service.zone.api.entity.ZoneClazzRewardNotice;
import com.voxlearning.utopia.service.zone.api.entity.boss.AwardDetail;
import com.voxlearning.utopia.service.zone.api.entity.boss.ClazzBossAward;
import com.voxlearning.utopia.service.zone.api.entity.boss.ClazzBossUserAward;
import com.voxlearning.utopia.service.zone.api.entity.giving.ChickenClazzProgress;
import com.voxlearning.utopia.service.zone.api.entity.giving.ChickenHelp;
import com.voxlearning.utopia.service.zone.api.entity.plot.*;
import com.voxlearning.utopia.service.zone.api.plot.PlotActivityService;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzActivityRecordPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ZoneClazzRewardPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.boss.ClazzBossAwardPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.giving.ChickenClazzProgressPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.plot.PlotReceiveOtherRecordPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.plot.PlotRewardConfigPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.plot.PlotStudentRecordPersistence;
import com.voxlearning.utopia.service.zone.impl.queue.ClazzBossRewardQueueProducer;
import com.voxlearning.utopia.vo.ActivityRank;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Named("com.voxlearning.utopia.service.zone.impl.service.ClassCirclePlotServiceImpl")
@ExposeService(interfaceClass = ClassCirclePlotService.class, version = @ServiceVersion(version = "20181110"))
@Slf4j
public class ClassCirclePlotServiceImpl implements ClassCirclePlotService {

    @Resource
    private ClazzBossRewardQueueProducer clazzBossRewardQueueProducer;
    @Resource
    private PlotRewardConfigPersistence plotRewardConfigPersistence;
    @Resource
    private BrainActivityService brainActivityService;
    @Resource
    private StudentLoaderClient studentLoaderClient;
    @Resource
    private ClazzActivityRecordPersistence clazzActivityRecordPersistence;
    @Resource
    private ClazzBossAwardPersistence clazzBossAwardPersistence;
    @Resource
    private ChickenClazzProgressPersistence chickenClazzProgressPersistence;
    @Resource
    private ZoneClazzRewardPersistence zoneClazzRewardPersistence;
    @Resource
    private PlotStudentRecordPersistence plotStudentRecordPersistence;
    @Resource
    private PlotReceiveOtherRecordPersistence plotReceiveOtherRecordPersistence;
    @Resource
    private PlotActivityService plotActivityService;

    private final AtomicLockManager atomicLockManager = AtomicLockManager.instance();

    public void insertOrder(ZoneClazzRewardNotice zoneClazzRewardNotice){
        zoneClazzRewardNotice.setId(zoneClazzRewardNotice.getActivityId() + "_" + RandomUtils.nextObjectId());
        zoneClazzRewardPersistence.insert(zoneClazzRewardNotice);
    }

    //获取奖励接口 每日排名奖励
    private Boolean isReceiveRank(Integer activityId, Long userId) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        ClazzActivityRecord record = clazzActivityRecordPersistence.load(activityId + "_" + studentDetail.getClazz().getSchoolId() + "_" + studentDetail.getClazz().getId() + "_" + userId);

        if (record != null && record.getBizObject() != null){
            Object object = record.getBizObject();
            PlotActivityBizObject plotActivityBizObject = JsonUtils.fromJson(JsonUtils.toJson(object),PlotActivityBizObject.class);
            Date date = plotActivityBizObject.getDate();
            if (date != null){
                String nowStr = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE);
                String awardStr = DateUtils.dateToString(date, DateUtils.FORMAT_SQL_DATE);
                if(nowStr.equals(awardStr))return true;
            }
        }
        return false;
    }

    //获取用户排名奖励
    private List<AwardDetail> getRankReward(Integer activityId, Long userId){
        //获取用户排名排名
        ActivityRank selfPersonInLevelRank = brainActivityService.getSelfPersonInLevelRank(activityId, userId,1,DateUtils.nextDay(new Date(),-1));
//        ActivityRank selfPersonInLevelRank = brainActivityService.getSelfPersonInLevelRank(activityId, userId,1,null);
        int rankOn = 0;
        if (selfPersonInLevelRank != null && selfPersonInLevelRank.getIndex() != null){
            rankOn = selfPersonInLevelRank.getIndex();
        }

        //前三十名领取奖励
        if (rankOn <= 30){
            List<ClazzBossAward> list = clazzBossAwardPersistence.getList(activityId, 0);
            if (list != null && list.size() > 0){
                for (int i = 0;i < list.size(); i++){
                    int before = list.get(i).getTargetValue().intValue();
                    int after;
                    if (i == list.size()-1){
                        after = 31;
                    }else {
                        after = list.get(i+1).getTargetValue().intValue();
                    }
                    if (rankOn >= before && rankOn < after){
                        return list.get(i).getAwards();
                    }
                }
            }
        }
        return new ArrayList<>();
    }


    //获取用户最后一天的排名奖励
    public List<AwardDetail> getLastDayRankReward(Integer activityId, Long userId,Date date){
        //获取用户排名排名
        ActivityRank selfPersonInLevelRank = brainActivityService.getSelfPersonInLevelRank(activityId, userId,1,DateUtils.nextDay(date,-1));
        int rankOn = 0;
        if (selfPersonInLevelRank != null && selfPersonInLevelRank.getIndex() != null){
            rankOn = selfPersonInLevelRank.getIndex();
        }

        //前三十名领取奖励
        if (rankOn <= 30){
            List<ClazzBossAward> list = clazzBossAwardPersistence.getList(activityId, 0);
            if (list != null && list.size() > 0){
                for (int i = 0;i < list.size(); i++){
                    int before = list.get(i).getTargetValue().intValue();
                    int after;
                    if (i == list.size()-1){
                        after = 31;
                    }else {
                        after = list.get(i+1).getTargetValue().intValue();
                    }
                    if (rankOn >= before && rankOn < after){
                        return list.get(i).getAwards();
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    //发送最后一日的排名奖励
    public boolean sendEveryDayReward(Integer activityId, Long userId,Date date){
        boolean result = false;
        List<PlotRewardObject> plotRewardObjects = new ArrayList<>();
        List<AwardDetail> rankReward = getLastDayRankReward(activityId, userId,date);
        //发送奖励
        if (rankReward != null && rankReward.size() > 0){
            ActivityRank selfPersonInLevelRank = brainActivityService.getSelfPersonInLevelRank(activityId, userId,1,DateUtils.nextDay(date,-1));
            //php发奖励
            PlotRewardObject prd = new PlotRewardObject();
            prd.setSubject("all");
            prd.setType("person");
            prd.setRankIndex(selfPersonInLevelRank.getIndex()==null?0:selfPersonInLevelRank.getIndex());
            prd.setAwards(rankReward);
            plotRewardObjects.add(prd);
        }
        //给php发送奖励
        if (plotRewardObjects.size()>0) {
            installReward(plotRewardObjects,userId);
            result = true;
        }
        return result;
    }

    /**
     * 发送奖励
     */
    private void sendReward(List<Object> list,Long userId) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", true);
        resultMap.put("event", "plot_reward");
        resultMap.put("studentId", userId);
        resultMap.put("messages", list);
        Message message = Message.newMessage();
        String json = JsonUtils.toJson(resultMap);
        message.withPlainTextBody(json);
        clazzBossRewardQueueProducer.getRewordProducer().produce(message);
    }

    //组装发送奖励数据
    private void installReward(List<PlotRewardObject> plotRewardObjects,Long userId){
        List<Object> list = new ArrayList<>();
        for (PlotRewardObject plotRewardObject : plotRewardObjects) {
            Map<String, Object> info = new HashMap<>();
            Map<String, Object> resultMap = new HashMap<>();
            int gen = 0;
            int mid = 0;
            int sup = 0;
            int sport = 0;
            int key = 0;
            if (plotRewardObject.getAwards() != null){
                for (AwardDetail awardDetail : plotRewardObject.getAwards()) {

                    if(awardDetail.getType()==1){//普通奖励
                        gen = Integer.valueOf(awardDetail.getNum());
                    }
                    if(awardDetail.getType()==2){//中级奖励
                        mid = Integer.valueOf(awardDetail.getNum());
                    }
                    if(awardDetail.getType()==3){//高级奖励
                        sup = Integer.valueOf(awardDetail.getNum());
                    }
                    if(awardDetail.getType()==4){//竞技奖励
                        sport = Integer.valueOf(awardDetail.getNum());
                    }
                    if(awardDetail.getType()==5){//钥匙
                        key = Integer.valueOf(awardDetail.getNum());
                    }
                }
            }
            int pointNum = SafeConverter.toInt(plotRewardObject.getPoint());
            if(pointNum > 0) info.put("CHECKPOINT", SafeConverter.toInt(plotRewardObject.getPoint())); //关卡
            if(gen > 0) info.put("CHEST_GENERAL", gen); //普通宝箱
            if(mid > 0) info.put("CHEST_MIDDLE", mid);//中级宝箱
            if(sup > 0) info.put("CHEST_ADVANCED", sup);//高级宝箱
            if(key > 0) info.put("CHEST_KEY", key);//钥匙
            if(sport > 0) info.put("VW00003", sport);
            resultMap.put("rewards", info);
            resultMap.put("subject", plotRewardObject.getSubject());
            Map<String, Object> typeMap = new HashMap<>();
            String type = plotRewardObject.getType();
            typeMap.put("type",type);
            if (type.equals("person")) typeMap.put("rankIndex",plotRewardObject.getRankIndex());
            if (type.equals("clazz")) typeMap.put("contributionValue",plotRewardObject.getContributionValue());
            if (!SafeConverter.toString(type).equals("buy")) resultMap.put("rewardSource",typeMap);
            list.add(resultMap);
        }
        if (list.size() > 0) sendReward(list,userId);
    }

    //保存或更新配置
    public Boolean upsetRewardConfig(Integer activityId,Integer type,String name ,String pic){
        PlotRewardConfig prc = new PlotRewardConfig();
        prc.setActivityId(activityId);
        prc.setType(type);
        prc.setPic(pic);
        prc.setName(name);
        prc.generateId();
        plotRewardConfigPersistence.upsert(prc);
        return true;
    }

    //查询用户每日排名奖励 和 班级 奖励
    @Override
    public MapMessage findEveryDayReward(Integer activityId,Long userId,Long schoolId, Long clazzId) {
        MapMessage mapMessage = MapMessage.successMessage();

        ClazzActivityRecord record = clazzActivityRecordPersistence.load(activityId + "_" + schoolId + "_" + clazzId + "_" + userId);
        List<PlotRewardObject> plotRewardObjects = new ArrayList<>();
        //排名奖励
        Boolean receiveRank = isReceiveRank(activityId, userId);
        if (!receiveRank){
            List<AwardDetail> rankReward = getRankReward(activityId, userId);

            //发送奖励
            if (rankReward != null && rankReward.size() > 0){
                ActivityRank selfPersonInLevelRank = brainActivityService.getSelfPersonInLevelRank(activityId, userId,1,DateUtils.nextDay(new Date(),-1));
                //php发奖励
                PlotRewardObject prd = new PlotRewardObject();
                prd.setSubject("all");
                prd.setType("person");
                prd.setRankIndex(selfPersonInLevelRank.getIndex()==null?0:selfPersonInLevelRank.getIndex());
                prd.setAwards(rankReward);
                plotRewardObjects.add(prd);
            }
            //更新领取每日奖励时间
            if(record != null && record.getBizObject()!=null){
                Object object = record.getBizObject();
                PlotActivityBizObject plotActivityBizObject = JsonUtils.fromJson(JsonUtils.toJson(object),PlotActivityBizObject.class);
                plotActivityBizObject.setDate(new Date());
                record.setBizObject(plotActivityBizObject);
                clazzActivityRecordPersistence.upsert(record);
            }
            mapMessage.add("everyDayRewards",rankReward);
        }else {
            mapMessage.add("everyDayRewards",new ArrayList<>());
        }

        //班级奖励
        List<ClazzBossAward> list = clazzBossAwardPersistence.getList(activityId, 1);
        ChickenClazzProgress clazzProgress = chickenClazzProgressPersistence.load(ChickenClazzProgress.generateId(activityId,clazzId,schoolId));
        List<PlotClazzRewardResponse> rewardResponseList = new ArrayList<>();
        if (list != null && list.size() > 0 && clazzProgress != null){

            if (record != null && record.getBizObject() != null){
                Object object = record.getBizObject();
                PlotActivityBizObject plotActivityBizObject = JsonUtils.fromJson(JsonUtils.toJson(object),PlotActivityBizObject.class);
                Map<String, ClazzBossUserAward> userAwardMap = plotActivityBizObject.getUserAwardMap();


                for (ClazzBossAward clazzBossAward : list) {
                    PlotClazzRewardResponse rewardResponse = new PlotClazzRewardResponse();
                    if (userAwardMap != null){
                        ClazzBossUserAward clazzBossUserAward = userAwardMap.get(clazzBossAward.getId());
                        if (clazzBossUserAward != null){
                            if (clazzProgress.getCount().intValue() >= clazzBossAward.getTargetValue().intValue() && !clazzBossUserAward.getReceive()) {//领取奖励
                                rewardResponse.setAwards(clazzBossAward.getAwards());
                                rewardResponse.setContribution(clazzBossAward.getTargetValue().intValue());
                                rewardResponseList.add(rewardResponse);
                                //添加到用户记录中
                                clazzBossUserAward.setReceive(true);
                                userAwardMap.put(clazzBossAward.getId(),clazzBossUserAward);

                                //添加奖励
                                PlotRewardObject prd = new PlotRewardObject();
                                prd.setSubject("all");
                                prd.setType("clazz");
                                prd.setAwards(clazzBossAward.getAwards());
                                prd.setContributionValue(clazzBossAward.getTargetValue().intValue());
                                plotRewardObjects.add(prd);
                            }
                        }else {
                            if (clazzProgress.getCount().intValue() >= clazzBossAward.getTargetValue().intValue()) {//领取奖励
                                rewardResponse.setAwards(clazzBossAward.getAwards());
                                rewardResponse.setContribution(clazzBossAward.getTargetValue().intValue());
                                rewardResponseList.add(rewardResponse);
                                //添加到用户记录中
                                clazzBossUserAward = new ClazzBossUserAward();
                                clazzBossUserAward.setReceive(true);
                                clazzBossUserAward.setClazzBossAwardId(clazzBossAward.getId());
                                userAwardMap.put(clazzBossAward.getId(),clazzBossUserAward);
                                //添加奖励
                                PlotRewardObject prd = new PlotRewardObject();
                                prd.setSubject("all");
                                prd.setType("clazz");
                                prd.setAwards(clazzBossAward.getAwards());
                                prd.setContributionValue(clazzBossAward.getTargetValue().intValue());
                                plotRewardObjects.add(prd);
                            }
                        }
                    }else {
                        if (clazzProgress.getCount().intValue() >= clazzBossAward.getTargetValue().intValue()){//领取奖励
                            userAwardMap = new HashMap<>();
                            rewardResponse.setAwards(clazzBossAward.getAwards());
                            rewardResponse.setContribution(clazzBossAward.getTargetValue().intValue());
                            rewardResponseList.add(rewardResponse);
                            //添加到用户记录中
                            ClazzBossUserAward clazzBossUserAward = new ClazzBossUserAward();
                            clazzBossUserAward.setReceive(true);
                            clazzBossUserAward.setClazzBossAwardId(clazzBossAward.getId());
                            userAwardMap.put(clazzBossAward.getId(),clazzBossUserAward);
                            //添加奖励
                            PlotRewardObject prd = new PlotRewardObject();
                            prd.setSubject("all");
                            prd.setType("clazz");
                            prd.setAwards(clazzBossAward.getAwards());
                            prd.setContributionValue(clazzBossAward.getTargetValue().intValue());
                            plotRewardObjects.add(prd);
                        }
                    }
                }
                plotActivityBizObject.setUserAwardMap(userAwardMap);
                record.setBizObject(plotActivityBizObject);
                clazzActivityRecordPersistence.upsert(record);
            }
        }
        mapMessage.add("clazzRewards",rewardResponseList);
        //给php发送奖励
        if (plotRewardObjects.size()>0) installReward(plotRewardObjects,userId);
        return mapMessage;
    }

    //给班级添加贡献值
    public Boolean addClazzContribution(Integer activityId, Long userId,Integer value){
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        String lockKey = "clazz_circle_plot_lock_" + studentDetail.getClazz().getId();
        try {
            atomicLockManager.acquireLock(lockKey, 3);
            ChickenClazzProgress clazzProgress = chickenClazzProgressPersistence.load(ChickenClazzProgress.generateId(activityId,studentDetail.getClazz().getId(),studentDetail.getClazz().getSchoolId()));
            if (clazzProgress != null){
                clazzProgress.setCount(clazzProgress.getCount() + value);
            }else {
                clazzProgress = new ChickenClazzProgress();
                clazzProgress.setClazzId(studentDetail.getClazz().getId());
                clazzProgress.setSchoolId(studentDetail.getClazz().getSchoolId());
                clazzProgress.setActivityId(activityId);
                clazzProgress.generateId();
                clazzProgress.setCount(value.longValue());
            }
            chickenClazzProgressPersistence.upsert(clazzProgress);
        }catch (Exception e){
            return false;
        }finally {
            atomicLockManager.releaseLock(lockKey);
        }
        return true;
    }

    //给自己加贡献值
    public Boolean addStudentContribution(Integer activityId, Long userId,Integer value){
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        Clazz clazz = studentDetail.getClazz();
        String lockKey = "clazz_circle_plot_lock_" + userId;
        try {
            atomicLockManager.acquireLock(lockKey, 3);
            brainActivityService.updateAllRank(activityId,clazz.getSchoolId(),clazz.getClazzLevel().getLevel(),clazz.getId(),userId,value);
            ClazzActivityRecord record = clazzActivityRecordPersistence.load(activityId + "_" + studentDetail.getClazz().getSchoolId() + "_" + studentDetail.getClazz().getId() + "_" + userId);
            if (record != null){
                record.setScore(record.getScore()==null?value:(record.getScore() + value));
                clazzActivityRecordPersistence.upsert(record);
            }
        }catch (Exception e){
            return false;
        }finally {
            atomicLockManager.releaseLock(lockKey);
        }
        return true;
    }

    //查询班级进度list
    @Override
    public MapMessage findClazzProgressList(Integer activityId, Long userId, Long schoolId, Long clazzId) {
        MapMessage mapMessage = MapMessage.successMessage();
        List<ClazzBossAward> list = clazzBossAwardPersistence.getList(activityId, 1);

        //个人进度
        ChickenClazzProgress clazzProgress = chickenClazzProgressPersistence.load(ChickenClazzProgress.generateId(activityId,clazzId,schoolId));
        mapMessage.add("currentProgress",clazzProgress != null ? clazzProgress.getCount():0);//个人进度

        //班级进度
        List<Integer> progress = new ArrayList<>();
        if (list != null && list.size() > 0){
            list.forEach(item -> progress.add(item.getTargetValue().intValue()));
        }
        mapMessage.add("clazzProgress",progress);//班级进度
        mapMessage.add("vip",false);//是否是vip

        Integer dateId = plotActivityService.getPlotGroupDateId(activityId, System.currentTimeMillis());
        ClazzActivityRecord record = clazzActivityRecordPersistence.load(activityId + "_" + schoolId + "_" + clazzId + "_" + userId);
        if (record != null && record.getBizObject() != null){
            Object object = record.getBizObject();
            PlotActivityBizObject plotActivityBizObject = JsonUtils.fromJson(JsonUtils.toJson(object),PlotActivityBizObject.class);
            Boolean vip = plotActivityBizObject.getVip();
            if (vip != null) mapMessage.set("vip",vip);
        }
        mapMessage.add("isPop",false);//是否首次弹支付页

        List<ZoneClazzRewardNotice> rewardList = zoneClazzRewardPersistence.findByClazzId(activityId, clazzId);

        rewardList = rewardList.stream().filter(item -> SafeConverter.toInt(dateId)== item.getRewardType()).collect(Collectors.toList());

        List<String> imgList = new ArrayList<>();
        List<Map<String,Object>> resultList = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        if (rewardList.size() > 0){

            map.put("isFirst",rewardList.get(0).getIsFirst());
            StudentDetail studentDetail1 = studentLoaderClient.loadStudentDetail(rewardList.get(0).getUserId());
            map.put("name",studentDetail1.fetchRealname());
            map.put("rewardType",rewardList.get(0).getRewardType());
            //贡献榜
            for (ZoneClazzRewardNotice notice : rewardList) {
                Map<String,Object> newMap = new HashMap<>();
                //为大家解锁
                newMap.put("isFirst",notice.getIsFirst());
                StudentDetail student = studentLoaderClient.loadStudentDetail(notice.getUserId());
                newMap.put("name",student.fetchRealname());
                newMap.put("rewardType",notice.getRewardType());
                resultList.add(newMap);

                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(notice.getUserId());
                imgList.add(studentDetail.fetchImageUrl());
            }
            if (imgList.size() > 3){
                imgList = imgList.subList(0,3);
            }
        }
        mapMessage.add("lockPerson",map);//解锁人或者购买人
        mapMessage.add("contList",imgList);//贡献榜 三个人
        mapMessage.add("lockPersonList",resultList);//解锁人或者购买人
        return mapMessage;
    }

    //获取同班同学的感谢列表
    @Override
    public MapMessage findThankList(Integer activityId, Long userId, Long schoolId, Long clazzId) {
        MapMessage mapMessage = MapMessage.successMessage();
        List<PlotStudentRecord> list = plotStudentRecordPersistence.findByUserId(activityId,userId);
        List<Map<String,Object>> resultList = new ArrayList<>();
        if (list != null && list.size() > 0){
            list.forEach(item -> {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(item.getGiveUserId());
                Map<String,Object> map = new HashMap<>();
                map.put("name",studentDetail.fetchRealname());
                map.put("type",item.getType());
                resultList.add(map);
            });
        }
        return mapMessage.add("data",resultList);
    }

    //获取个人奖励弹窗
    @Override
    public MapMessage findRewardList(Integer activityId, Long userId, Long schoolId, Long clazzId) {

        MapMessage mapMessage = MapMessage.successMessage();

        Integer dateId = plotActivityService.getPlotGroupDateId(activityId, System.currentTimeMillis());
        //个人奖励 和 同班同学奖励
        List<ZoneClazzRewardNotice> rewardList = zoneClazzRewardPersistence.findByClazzId(activityId, clazzId);
        List<ZoneClazzRewardNotice> rewardClazzList = rewardList.stream().filter(item -> item.getUserId().intValue()!=userId.intValue()&&SafeConverter.toInt(dateId)== item.getRewardType()).collect(Collectors.toList());
        List<ZoneClazzRewardNotice> rewardPersionList = rewardList.stream().filter(item -> item.getUserId().intValue()==userId.intValue()&&!item.getIsReceived()).collect(Collectors.toList());

        List<ClazzBossAward> list = clazzBossAwardPersistence.getList(activityId, 2);
        List<PlotRewardObject> plotRewardObjects = new ArrayList<>();

        StudentDetail student = studentLoaderClient.loadStudentDetail(userId);
        // 个人购买奖励
        if (rewardPersionList.size() > 0 && list.size() > 0){

            List<Map<String,Object>> resultList = new ArrayList<>();
            for (ZoneClazzRewardNotice zoneClazzRewardNotice : rewardPersionList) {
                Map<String,Object> resultMap = new HashMap<>();
                String subject = getSubject(zoneClazzRewardNotice);
                PlotRewardObject prd = new PlotRewardObject();
                prd.setPoint(1);
                prd.setSubject(subject);
                prd.setType("buy");

                List<ClazzBossAward> clazzBossAwardStream = list.stream().filter(item -> item.getTargetValue().intValue() == SafeConverter.toInt(zoneClazzRewardNotice.getPeriod())).collect(Collectors.toList());
                //php 奖励 和 关卡
                if(clazzBossAwardStream.size() > 0) prd.setAwards(clazzBossAwardStream.get(0).getAwards());

                plotRewardObjects.add(prd);

                //奖励
                if (clazzBossAwardStream.size() > 0){
                    //提高版
                    if(zoneClazzRewardNotice.getPeriod()==365 && zoneClazzRewardNotice.getIsImproved()){
                        clazzBossAwardStream = clazzBossAwardStream.stream().filter(item -> item.getType() == 7).collect(Collectors.toList());
                    }

                    if(zoneClazzRewardNotice.getPeriod()==365 && !zoneClazzRewardNotice.getIsImproved()){
                        clazzBossAwardStream = clazzBossAwardStream.stream().filter(item -> item.getType() == 6).collect(Collectors.toList());
                    }
                    resultMap.put("rewards",clazzBossAwardStream.get(0).getAwards());
                }else {
                    resultMap.put("rewards",new ArrayList<>());
                }

                //贡献值
                resultMap.put("count",50);
                addStudentContribution(activityId,userId,50);
                addClazzContribution(activityId,userId,50);

                //关卡
                Map<String,Object> pointMap = new HashMap<>();
                pointMap.put("count",1);
                pointMap.put("subject",subject);
                pointMap.put("plot",zoneClazzRewardNotice.getRewardType());
                pointMap.put("isFirst",zoneClazzRewardNotice.getIsFirst());
                pointMap.put("name",student.fetchRealname());
                resultMap.put("point",pointMap);
                resultList.add(resultMap);

                //更新领取奖励状态
                zoneClazzRewardNotice.setIsReceived(true);
                zoneClazzRewardPersistence.upsert(zoneClazzRewardNotice);
            }
            mapMessage.add("personBuy",resultList);
        }else {
            mapMessage.add("personBuy",new ArrayList<>());
        }

        // 同班同学购买奖励
        if (rewardClazzList.size() > 0){
            List<Map<String,Object>> resultList = new ArrayList<>();
            List<PlotRewardConfig> configs = plotRewardConfigPersistence.findByActivityId(activityId);
            List<String>  nameList = new ArrayList<>();
            List<Long>  idList = new ArrayList<>();
            int english = 0;
            int chinese = 0;
            int math = 0;
            for (ZoneClazzRewardNotice notice : rewardClazzList) {
                Map<String,Object> resultMap = new HashMap<>();
                List<PlotReceiveOtherRecord> otherRecords = plotReceiveOtherRecordPersistence.findByUserIdAndOrder(activityId, userId, notice.getId());
                if (otherRecords == null || otherRecords.size()<=0){
                    //保存记录
                    PlotReceiveOtherRecord other = new PlotReceiveOtherRecord();
                    other.setActivityId(activityId);
                    other.setIsReceive(true);
                    other.setOrderId(notice.getId());
                    other.setUserId(userId);
                    other.generateId();
                    plotReceiveOtherRecordPersistence.upsert(other);

                    //php发奖励
                    String subject = getSubject(notice);
                    PlotRewardObject prd = new PlotRewardObject();
                    prd.setPoint(1);
                    prd.setSubject(subject);
                    prd.setType("buy");
                    plotRewardObjects.add(prd);

                    StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(notice.getUserId());
                    //返回数据 关卡
                    Map<String,Object> pointMap = new HashMap<>();
                    pointMap.put("count",1);
                    pointMap.put("subject",subject);
                    pointMap.put("buyUserId",notice.getUserId());
                    pointMap.put("plot",notice.getRewardType());
                    pointMap.put("isFirst",notice.getIsFirst());
                    pointMap.put("name",studentDetail.fetchRealname());
                    resultMap.put("point",pointMap);


                    //返回星星数据
                    Map<String,Object> starMap = new HashMap<>();
                    starMap.put("list",configs);
                    starMap.put("buyUserId",notice.getUserId());
                    resultMap.put("thanks",starMap);

                    resultList.add(resultMap);

                    //活动更改
                    nameList.add(studentDetail.fetchRealname());
                    idList.add(notice.getUserId());
                    if (notice.getSubject()==1){
                        chinese += 1;
                    }else if(notice.getSubject()==2){
                        math += 1;
                    }else {
                        english += 1;
                    }
                }

            }
            List<ZoneClazzRewardNotice> zoneList = rewardList.stream().filter(item -> SafeConverter.toInt(dateId)== item.getRewardType() && item.getIsFirst()).collect(Collectors.toList());
            Map<String,Object> resultNewMap = new HashMap<>();
            resultNewMap.put("nameList",nameList);
            resultNewMap.put("idList",idList);
            boolean isFirst = false;
            if (zoneList.size()>0){
                if (idList.contains(zoneList.get(0).getUserId())) isFirst = true;
            }

            resultNewMap.put("isFirst",isFirst);
            resultNewMap.put("plot",SafeConverter.toInt(dateId));
            resultNewMap.put("english",english);
            resultNewMap.put("chinese",chinese);
            resultNewMap.put("math",math);

            mapMessage.add("clazzBuy",resultList);
            mapMessage.add("clazzNewBuy",resultNewMap);
        }else {
            mapMessage.add("clazzBuy",new ArrayList<>());

            Map<String,Object> resultNewMap = new HashMap<>();
            resultNewMap.put("nameList",new ArrayList<>());
            resultNewMap.put("idList",new ArrayList<>());
            resultNewMap.put("isFirst",false);
            resultNewMap.put("plot",SafeConverter.toInt(dateId));
            resultNewMap.put("english",0);
            resultNewMap.put("chinese",0);
            resultNewMap.put("math",0);
            mapMessage.add("clazzNewBuy",resultNewMap);
        }
        //是否弹支付页面
        boolean isPop = false;
        ClazzActivityRecord record = clazzActivityRecordPersistence.load(activityId + "_" + schoolId + "_" + clazzId + "_" + userId);
        if (record != null && record.getBizObject() != null){
            Object object = record.getBizObject();
            PlotActivityBizObject plotActivityBizObject = JsonUtils.fromJson(JsonUtils.toJson(object),PlotActivityBizObject.class);
            Set<Integer> plotPop = plotActivityBizObject.getPlotPop();
            int plot = SafeConverter.toInt(dateId);
            if (plotPop != null){
                if (plot != 0 && !plotPop.contains(plot)) {
                    isPop = true;
                    plotPop.add(plot);
                }
            }else {
                if (plot != 0 ) {
                    plotPop = new HashSet<>();
                    plotPop.add(plot);
                    isPop = true;
                }
            }
            plotActivityBizObject.setPlotPop(plotPop);
            record.setBizObject(plotActivityBizObject);
            clazzActivityRecordPersistence.upsert(record);
        }
        mapMessage.add("isPop",isPop);//是否首次弹支付页

        if (plotRewardObjects.size() > 0)installReward(plotRewardObjects,userId);
        return mapMessage;
    }

    //获取学科
    private String getSubject(ZoneClazzRewardNotice notice){
        String subject;
        if (notice.getSubject()==1){
            subject = "chinese";
        }else if(notice.getSubject()==2){
            subject = "math";
        }else {
            subject = "english";
        }
        return subject;
    }

    //赠送礼物
    @Override
    public MapMessage gaveGift(Integer activityId, Long userId,Long gaveUserId,Integer type) {
        PlotStudentRecord record = new PlotStudentRecord();
        record.setActivityId(activityId);
        record.setGiveUserId(userId);
        record.setType(type);
        record.setUserId(gaveUserId);
        record.generateId();
        plotStudentRecordPersistence.upsert(record);
        return MapMessage.successMessage();
    }

    //批量赠送礼物
    @Override
    public MapMessage batchGaveGift(Integer activityId, Long userId,List<Long> gaveUserId,Integer type) {
        List<PlotStudentRecord> list = new ArrayList<>();
        for (Long gaveId : gaveUserId) {
            PlotStudentRecord record = new PlotStudentRecord();
            record.setActivityId(activityId);
            record.setGiveUserId(userId);
            record.setType(type);
            record.setUserId(gaveId);
            record.generateId();
            list.add(record);
        }
        if (list.size() > 0) plotStudentRecordPersistence.inserts(list);
        return MapMessage.successMessage();
    }
    @Override
    public Boolean addZoneClazzRewardNotice(Long userId, ZoneClazzRewardNotice zoneClazzRewardNotice,String appName) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        if(studentDetail == null ||studentDetail.getClazz()==null ){
            return false;
        }
        ClazzActivityRecord userRecord = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(3, studentDetail.getClazz().getSchoolId(), studentDetail.getClazz().getId(), userId));
        if (userRecord == null) {
            //没参加活动不处理
            return false;
        }
        Integer plotGroupDateId = plotActivityService.getPlotGroupDateId(3, new Date().getTime());
        if (plotGroupDateId == null) {
            //当前剧情不存在
            return false;
        }
        if (userRecord.getBizObject() != null) {
            PlotActivityBizObject bizObject = JsonUtils.fromJson(JsonUtils.toJson(userRecord.getBizObject()), PlotActivityBizObject.class);
            bizObject.setVip(true);
            bizObject.setAppkey(appName);
            userRecord.setBizObject(bizObject);
        }
        List<ZoneClazzRewardNotice> clazzList = zoneClazzRewardPersistence.findByClazzIdAndReward(3, studentDetail.getClazz().getId(), plotGroupDateId);
        zoneClazzRewardNotice.setIsFirst(CollectionUtils.isEmpty(clazzList));
        zoneClazzRewardNotice.setClazzId(studentDetail.getClazz().getId());
        zoneClazzRewardNotice.setRewardType(plotGroupDateId);
        zoneClazzRewardNotice.setIsReceived(false);
        zoneClazzRewardNotice.generateId();
        zoneClazzRewardPersistence.insert(zoneClazzRewardNotice);
        clazzActivityRecordPersistence.upsert(userRecord);
        return true;
    }
}

