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

import com.google.common.base.Strings;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.zone.api.ClassCircleBossService;
import com.voxlearning.utopia.service.zone.api.ClazzActivityService;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRecord;
import com.voxlearning.utopia.service.zone.api.entity.boss.*;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzActivityRecordPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.boss.ClazzBossAwardPersistence;
import com.voxlearning.utopia.service.zone.impl.queue.ClazzBossRewardQueueProducer;
import com.voxlearning.utopia.service.zone.impl.support.ClazzActivityCacheManager;
import com.voxlearning.utopia.service.zone.impl.support.ClazzBossCountCache;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;

@Named("com.voxlearning.utopia.service.zone.impl.service.ClassCircleBossServiceImpl")
@ExposeService(interfaceClass = ClassCircleBossService.class, version = @ServiceVersion(version = "20181105"))
@Slf4j
public class ClassCircleBossServiceImpl implements ClassCircleBossService {

    /**
     * 班级boss活动基础奖励配置Persistence
     */
    @Resource
    private ClazzBossAwardPersistence clazzBossAwardPersistence;

    /**
     * 缓存管理器
     */
    @Resource
    private ClazzActivityCacheManager clazzActivityCacheManager;

    @Resource
    private ClazzActivityRecordPersistence clazzActivityRecordPersistence;
    @Resource
    private ClazzBossRewardQueueProducer clazzBossRewardQueueProducer;
    @Resource
    private StudentLoaderClient studentLoaderClient;
    @Resource
    private ClazzActivityService clazzActivityService;

    public Map<String,String> dataMap = new HashMap<>();

    public static String dataGap1 = "2018-11-09_2018-11-12";
    public static String dataGap2="2018-11-12_2018-11-14";
    public static String dataGap3="2018-11-14_2018-11.16";


    /**
     * 获取奖励配置表
     */
    @Override
    public List<ClazzBossAward> getClazzBossAwardList(Integer activityId) {
        return clazzBossAwardPersistence.getList(activityId);
    }

    @Override
    public boolean deleteClazzBossAwardById(String id){
        return clazzBossAwardPersistence.deleteById(id);
    }

    @Override
    public ClazzBossAward detail(Integer activityId, Integer selfOrClazz, Integer type) {
        String id = activityId + "_" + selfOrClazz + "_" + type;
        return clazzBossAwardPersistence.detail(id);
    }

    /**
     * 更新或者插入奖励配置表
     */
    @Override
    public ClazzBossAward updateOrInsert(Integer activityId, Integer selfOrClazz, Integer type, Double targetValue,String boxPic,String name, String awardDetails) {
        List<AwardDetail> awardDetailList = new ArrayList<>();
        if(!Strings.isNullOrEmpty(awardDetails)){
            try {
                String[] str = awardDetails.split(",");
                for (String s : str) {
                    AwardDetail awardDetail = new AwardDetail();
                    String[] fields = s.split("_");
                    awardDetail.setName(fields[0]);
                    awardDetail.setPic(fields[1]);
                    awardDetail.setNum(fields[2]);
                    awardDetail.setType(Integer.valueOf(fields[3]));
                    awardDetailList.add(awardDetail);
                }
            } catch (Exception ex) {
                return null;
            }
        }
        ClazzBossAward clazzBossAward = new ClazzBossAward();
        clazzBossAward.setAwards(awardDetailList);
        clazzBossAward.setSelfOrClazz(selfOrClazz);
        clazzBossAward.setType(type);
        clazzBossAward.setActivityId(activityId);
        clazzBossAward.setTargetValue(targetValue);
        clazzBossAward.setBoxPic(boxPic);
        clazzBossAward.setName(name);
        return clazzBossAwardPersistence.updateOrInsert(clazzBossAward);
    }

    /**
     * 缓存值加1
     */
    @Override
    public Long increaseCountBySchoolIdAndType(Long schoolId, Integer type) {
        String cacheKey = ClazzBossCountCache.generatorSchoolCountCacheKey(schoolId, type);
        return clazzActivityCacheManager.increaseClazzBossCountByKey(cacheKey);
    }

    //用户id
    public Long increaseCountByStudentId(Long userId) {
        return clazzActivityCacheManager.increaseClazzBossCountByKey(userId.toString()+"clazz_circle_boss");
    }

    public Long loadCountByStudentId(Long userId) {
        return clazzActivityCacheManager.loadClazzBossCountByKey(userId.toString()+"clazz_circle_boss");
    }

    //获取学科
    public String getStudentSubject(Long userId){
        String chinese =  "chinese";
        String english = "english";
        String math =  "math";
        Long aLong = loadCountByStudentId(userId);
        if (aLong!=null){
            int count = loadCountByStudentId(userId).intValue();
            int value = count % 3;
            if (value==1){
                return english;
            }else if (value==2){
                return math;
            }else {
                return chinese;
            }
        }else {
            return english;
        }
    }


    /**
     * 查询缓存值
     */
    @Override
    public Long loadCountBySchoolIdAndType(Long schoolId, Integer type) {
        String cacheKey = ClazzBossCountCache.generatorSchoolCountCacheKey(schoolId, type);
        return clazzActivityCacheManager.loadClazzBossCountByKey(cacheKey);
    }

    /**
     * 缓存值加1
     */
    @Override
    public Long increaseCountByClazzIdAndType(Long clazzId) {
        String cacheKey = ClazzBossCountCache.generatorClazzCountCacheKey(clazzId);
        return clazzActivityCacheManager.increaseClazzBossCountByKey(cacheKey);
    }

    /**
     * 查询缓存值
     */
    @Override
    public Long loadCountByClazzIdAndType(Long clazzId) {
        String cacheKey = ClazzBossCountCache.generatorClazzCountCacheKey(clazzId);
        return clazzActivityCacheManager.loadClazzBossCountByKey(cacheKey);
    }

    @Override
    public Boolean setClazzBossCountByKey(String key, Long value){
        return clazzActivityCacheManager.setClazzBossCountByKey(key,value);
    }

    public Boolean setSchoolBossCountByKey(String key, Long value){
        return clazzActivityCacheManager.setClazzBossCountByKey(key,value);
    }

    @Override
    public Boolean deleteClazzBossCountByKey(String key){
        return clazzActivityCacheManager.deleteClazzBossCountByKey(key);
    }

    //发送奖励
    public void sendAward(List<Integer> selfList,List<Integer> clazzList,Long studentId,String subject) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", true);
        resultMap.put("event", "reward_new");
        resultMap.put("subject", subject);
        resultMap.put("clazz", clazzList);
        resultMap.put("self", selfList);
        resultMap.put("studentId", studentId);
        Message message = Message.newMessage();
        String json = JsonUtils.toJson(resultMap);
        message.withPlainTextBody(json);
        clazzBossRewardQueueProducer.getRewordProducer().produce(message);
    }

    @Override
    public Map<String, Object> userClazzActivityRecord(Integer activityId, Long userId, Long schoolId, Long clazzId){
        increaseCountByStudentId(userId);
        List<ClazzActivityRecord> clazzActivityRecords = clazzActivityRecordPersistence.findBySchooldId(activityId, schoolId);
        int clazzStudentNum=studentLoaderClient.loadClazzStudentIds(clazzId).size();
        Map<String,Object> map = new HashMap<>();
        List<ClazzBossSelfRecord> selfList = new ArrayList<>();
        List<ClazzBossGroupRecord> clazzList = new ArrayList<>();
        map.put("selfList",selfList);
        map.put("clazzList",clazzList);
        ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activityId,schoolId,clazzId,userId));
        if(clazzActivityRecord==null) return map;

        List<ClazzBossUserAward> clazzBossUserAwards=JsonUtils.fromJsonToList(JsonUtils.toJson(clazzActivityRecord.getCondition().get(ClazzActivityRecord.CLAZZ_BOSS_USER_AWARD)),ClazzBossUserAward.class);
        List<ClazzBossAward> clazzBossAwardList = getClazzBossAwardList(activityId);
        Integer currentProgress = (Integer) clazzActivityRecord.getCondition().get("currentProgress");
        List<ClazzBossAward> cbaList= clazzBossAwardList.stream().filter(item->
            item.getSelfOrClazz()==ClazzBossAward.SELF
        ).collect(Collectors.toList());
        for(ClazzBossAward cba : cbaList){
            ClazzBossSelfRecord clazzBossSelfRecord = new ClazzBossSelfRecord();
            clazzBossSelfRecord.setType(cba.getType());
            List<ClazzActivityRecord> currentList = clazzActivityRecords.stream().filter(c -> {
                if (c.getCondition() != null && c.getCondition().get("currentProgress") != null) {
                    return ((Integer) c.getCondition().get("currentProgress") >= cba.getTargetValue().intValue()) && !clazzActivityRecord.getId().equals(c.getId());
                }
                return false;
            }).collect(Collectors.toList());
            clazzBossSelfRecord.setCurrentProgress(currentProgress);
            clazzBossSelfRecord.setSchoolProviderNum(loadCountBySchoolIdAndType(schoolId,cba.getType()));
            clazzBossSelfRecord.setSelfTarget(cba.getTargetValue().intValue());
            if (CollectionUtils.isNotEmpty(clazzBossUserAwards)) {
                clazzBossUserAwards
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(cbu -> cbu.getClazzBossAwardId().equals(cba.getId()))
                        .findFirst()
                        .ifPresent(clazzBossUserAward -> clazzBossSelfRecord.setReceive(clazzBossUserAward.getReceive()));
            } else {
                clazzBossSelfRecord.setReceive(false);
            }
            clazzBossSelfRecord.setAwards(cba.getAwards());
            clazzBossSelfRecord.setStuList(new ArrayList<>());
            Student student;
            StudentInfo studentInfo;
            int stuSize = 5;
            if (currentProgress != null && cba.getTargetValue() != null) {
                if (currentProgress >= cba.getTargetValue().intValue()) {
                    stuSize = 4;
                    student = studentLoaderClient.loadStudent(userId);
                    studentInfo = new StudentInfo();
                    studentInfo.setPic(student.fetchImageUrl());
                    studentInfo.setName(student.fetchRealname());
                    clazzBossSelfRecord.getStuList().add(studentInfo);
                }
            }

            int[] indexArray =  randomSet(currentList.size(),currentList.size()>stuSize?stuSize:currentList.size());
            for(int index : indexArray){
                student=studentLoaderClient.loadStudent(currentList.get(index).getUserId());
                studentInfo = new StudentInfo();
                studentInfo.setName(student.fetchRealname());
                studentInfo.setPic(student.fetchImageUrl());
                clazzBossSelfRecord.getStuList().add(studentInfo);
            }
            selfList.add(clazzBossSelfRecord);
        }
        map.put("selfList",selfList);

        Integer participateNum= clazzActivityRecordPersistence.findByClazzId(activityId, schoolId, clazzId).size(); //班级参加活动人数
        List<ClazzBossAward> cbaList2=clazzBossAwardList.stream().filter(item->
                item.getSelfOrClazz()==ClazzBossAward.CLAZZ
        ).collect(Collectors.toList());
        for(ClazzBossAward cba : cbaList2){
            ClazzBossGroupRecord clazzBossGroupRecord = new ClazzBossGroupRecord();
            clazzBossGroupRecord.setType(cba.getType());
            clazzBossGroupRecord.setParticipateNum(participateNum);
            clazzBossGroupRecord.setClazzStudentNum(clazzStudentNum);
            clazzBossGroupRecord.setCurrentProgress(loadCountByClazzIdAndType(clazzId).intValue());
            clazzBossGroupRecord.setClazzTarget((int) Math.floor(cba.getTargetValue()*clazzStudentNum));
            clazzBossGroupRecord.setSelfProviderNum(currentProgress);
            if (CollectionUtils.isNotEmpty(clazzBossUserAwards)) {
                clazzBossUserAwards
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(cbu -> cbu.getClazzBossAwardId().equals(cba.getId()))
                        .findFirst()
                        .ifPresent(clazzBossUserAward -> clazzBossGroupRecord.setReceive(clazzBossUserAward.getReceive()));
            } else {
                clazzBossGroupRecord.setReceive(false);
            }
            clazzBossGroupRecord.setAwards(cba.getAwards());
            clazzList.add(clazzBossGroupRecord);
        }
        map.put("clazzList",clazzList);

        map.put("subject",getStudentSubject(userId)!=null?getStudentSubject(userId):"math");

        return map;
    }

    @Override
    public boolean setClazzBossSubject(String key,String value){
        return clazzActivityCacheManager.setClazzBossSubject(key,value);
    }

    @Override
    public String getClazzBossSubject(String key){
        return clazzActivityCacheManager.getClazzBossSubject(key);
    }

    public Map.Entry<String, String> caculateDate(){
        String chineseTime =  getClazzBossSubject("chinese");
        String englishTime =  getClazzBossSubject("english");
        String mathTime =  getClazzBossSubject("math");
        dataMap.put("chinese",chineseTime!=null?chineseTime:dataGap1);
        dataMap.put("english",englishTime!=null?englishTime:dataGap2);
        dataMap.put("math",mathTime!=null?mathTime:dataGap3);
        long currentTime = System.currentTimeMillis();
        Map.Entry<String, String> m = dataMap.entrySet().stream().filter(item-> {
                String[] strings = item.getValue().split("_");
                Date dateMin =  DateUtils.stringToDate(strings[0],FORMAT_SQL_DATE);
                Date dateMax = DateUtils.stringToDate(strings[1],FORMAT_SQL_DATE);
                if(currentTime>=dateMin.getTime()&&currentTime<dateMax.getTime())
                    return true;
                else return false;
            }).findFirst().orElse(null);
        return m;
    }

   //查询个人奖励列表
   public List<RewordResponse> findRewardList(Integer activity,Long studentId,Long clazzId,Long schoolId){
       String subject = getStudentSubject(studentId);
       List<RewordResponse> list = new ArrayList<>();
       List<Integer> selfList = new ArrayList<>();
       List<Integer> clazzList = new ArrayList<>();

       ClazzActivityRecord activityRecord = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activity, schoolId, clazzId, studentId));
       if (activityRecord != null){
           List<ClazzBossUserAward> awardList = JsonUtils.fromJsonToList(JsonUtils.toJson(activityRecord.getCondition().get(ClazzActivityRecord.CLAZZ_BOSS_USER_AWARD)), ClazzBossUserAward.class);

           if (awardList != null && awardList.size() > 0){
               List <Long> userIds = studentLoaderClient.loadClazzStudentIds(clazzId);
               for (ClazzBossUserAward clazzBossUserAward : awardList) {
                   if (!clazzBossUserAward.getReceive()){//没有领取的奖励
                       ClazzBossAward detail = clazzBossAwardPersistence.detail(clazzBossUserAward.getClazzBossAwardId());
                       int currentProgress = (int) activityRecord.getCondition().get("currentProgress");
                       if (detail.getSelfOrClazz().intValue() == 0){//个人
                           if(currentProgress >= detail.getTargetValue().intValue()){
                               RewordResponse rr = new RewordResponse();
                               rr.setAwards(detail.getAwards());
                               rr.setClazzBossAwardId(detail.getId());
                               rr.setCount(detail.getTargetValue().intValue());
                               rr.setType(1);
                               list.add(rr);
                               //添加奖励
                               selfList.add(detail.getType());
//                               sendAward(detail.getSelfOrClazz(),detail.getType(),studentId,subject);
                               clazzBossUserAward.setReceive(true);
                           }
                       }else {//班级
                           Double targetCount = detail.getTargetValue() * userIds.size();//班级目标打怪数
                           Long clazzCount = loadCountByClazzIdAndType(clazzId);  //班级总打怪数
                           if(clazzCount.intValue() >= targetCount.intValue() && currentProgress >= 1){
                               RewordResponse rr = new RewordResponse();
                               rr.setClazzBossAwardId(detail.getId());
                               rr.setAwards(detail.getAwards());
                               rr.setCount(targetCount.intValue());
                               rr.setType(2);
                               list.add(rr);
                               //添加奖励
                               clazzList.add(detail.getType());
//                               sendAward(detail.getSelfOrClazz(),detail.getType(),studentId,subject);
                               clazzBossUserAward.setReceive(true);
                           }
                       }
                   }
               }
               activityRecord.getCondition().put(ClazzActivityRecord.CLAZZ_BOSS_USER_AWARD,awardList);
               clazzActivityRecordPersistence.updateOrSave(activityRecord);
           }
       }

       if (selfList.size() > 0 || clazzList.size() > 0 ){
           //发送奖励
           sendAward(selfList,clazzList,studentId,subject);
       }
       return list;
   }

   //修改学科配置
   @Override
   public boolean setSubjectTime(String chinese,String english,String math){
        if(english != null && !"".equals(english)){
            setClazzBossSubject("english",english);
        }
        if(math != null && !"".equals(math)){
            setClazzBossSubject("math",math);
        }
        if(chinese != null && !"".equals(chinese)){
            setClazzBossSubject("chinese",chinese);
        }
        return true;
   }

    private static int[] randomSet(int max, int n) {
      Random random = new Random();
        Set<Integer> set = new HashSet<>();
        int[] array = new int[n];
        if(max<=0||n<=0) return new int[0];
        for (; true;) {
            // 调用Math.random()方法
            int num = random.nextInt(max);
            set.add(num);
            if (set.size() >= n) {
                break;
            }
        }
        int i = 0;
        for (int a : set) {
            array[i] = a;
            i++;
        }
        return array;
    }

}

