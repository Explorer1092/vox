package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.zone.api.ClassCircleBossService;
import com.voxlearning.utopia.service.zone.api.ClazzActivityService;
import com.voxlearning.utopia.service.zone.api.entity.ClassCircleCouchBaseKey;
import com.voxlearning.utopia.service.zone.api.entity.ClassCircleCouchBaseRecord;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivity;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRecord;
import com.voxlearning.utopia.service.zone.api.entity.boss.ClazzBossUserAward;
import com.voxlearning.utopia.service.zone.api.entity.giving.ChickenStudent;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotActivityBizObject;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotInfo;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotRewardConfig;
import com.voxlearning.utopia.service.zone.impl.persistence.ClassCircleCouchBaseKeyPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClassCircleCouchBasePersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzActivityPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzActivityRecordPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.plot.PlotRewardConfigPersistence;
import com.voxlearning.utopia.service.zone.impl.support.ClazzActivityCacheManager;
import com.voxlearning.utopia.vo.StudentVO;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 班级圈活动
 *
 * @author chensn
 * @date 2018-10-30 12:06
 */
@Named("com.voxlearning.utopia.service.zone.impl.service.ClazzActivityServiceimpl")
@ExposeService(
    interfaceClass = ClazzActivityService.class,
    version = @ServiceVersion(version = "20181030"))
public class ClazzActivityServiceimpl implements ClazzActivityService {

    @Inject
    private ClazzActivityPersistence clazzActivityPersistence;
    @Inject
    private ClazzActivityCacheManager clazzActivityCacheManager;
    @Inject
    private ClazzActivityRecordPersistence clazzActivityRecordPersistence;

    @Inject
    private ClassCircleCouchBasePersistence classCircleCouchBasePersistence;

    @Inject
    private ClassCircleCouchBaseKeyPersistence classCircleCouchBaseKeyPersistence;

    @Resource
    private ClassCircleBossService classCircleBossService;
    @Resource
    private PlotRewardConfigPersistence plotRewardConfigPersistence;
    @Inject
    private GrayFunctionManagerClient grayFunctionManagerClient;
    @Resource
    private StudentLoaderClient studentLoaderClient;

    @Override
    public List<ClazzActivity> getList(Long userId, Long schoolId, Long clazzId) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        boolean isInBlankList = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ClassZoneActivity", "BlackList");
        if (isInBlankList) {
            return null;
        }
        List<ClazzActivity> usedActivitys = clazzActivityCacheManager.findActivityCache();
        if (CollectionUtils.isEmpty(usedActivitys)) {
            if (RuntimeMode.le(Mode.STAGING)) {
                //staging和测试环境缓存不在会查最新活动数据
                usedActivitys = clazzActivityPersistence.findUsedActivity();
                if (CollectionUtils.isEmpty(usedActivitys)) {
                    return null;
                }
                clazzActivityCacheManager.save(usedActivitys);
            } else {
                //正式环境直接返回空
                return null;
            }

        }
        //结束12小时之内都能查到
        long timestamp = new Date().getTime() - (43200L * 1000L);
        Date now = new Date(timestamp);
        usedActivitys = usedActivitys.stream()
                .filter(e -> e.getIsShow() == true)
                .filter(e -> e.getEndDate().after(now))
                .collect(Collectors.toList());
      for (ClazzActivity activity : usedActivitys) {
          // 装填参数
          String url = activity.getTargetPattern();
          if (StringUtils.isNoneBlank(url)) {
              List<String> params = patternUrl(url);
              if(params.size()!=0){
                  url = url.substring(0,url.indexOf("#"));
                  for(String param :params){
                      switch (param){
                          case "userId":
                              url = addUrlParam(url,"userId",String.valueOf(userId));
                              break;
                          case "clazzId":
                              url = addUrlParam(url,"clazzId",String.valueOf(clazzId));
                              break;
                          case "activityId":
                              url = addUrlParam(url, "activityId", String.valueOf(activity.getId()));
                              break;
                          case "schoolId":
                              url = addUrlParam(url, "schoolId", String.valueOf(schoolId));
                              break;
                          default:
                              //反射规则获取数据
                              break;
                      }
                  }
                  activity.setTargetPattern(url);
              }
          }
          //计算排名或班级人数
          switch (activity.getShowType()){
              case 1:
                  //计算活动排名
                  activity.setShowText("1");
                  break;
              case 2:
                  //计算活动参与人数
                  Long num = clazzActivityCacheManager.loadByActivity(activity.getId());
                  String result = "";
                  if (num == null) {
                      result = "0";
                  } else {
                      result = num > 99999 ? Math.round((double) num / 10000) + "万" : String.valueOf(num);
                  }
                  activity.setShowText(result);
              default:
                  break;
          }

      //计算排名或班级人数
      switch (activity.getContentType()) {
        case 1:
          //计算班级排名
          activity.setContent("当前班级里排名第1位");
          break;
        case 2:
          //计算班级人数
          activity.setContent("班级里" + clazzActivityRecordPersistence
              .findByClazzId(activity.getId(), schoolId, clazzId).size() + "人在参与");
        default:
          break;
      }
    }
    return usedActivitys;
  }

    @Override
    public ClazzActivity getActivity(Integer activityId) {
        return clazzActivityPersistence.load(activityId);
    }

    @Override
  public void addOrUpdate(ClazzActivity clazzActivity) {
      if (clazzActivity.getId() == null) {
          //暂时不支持自增主键，由于是配置表 暂时自己定义ID 让id有意义
          return;
      }
      clazzActivityPersistence.upsert(clazzActivity);
      List<ClazzActivity> list = clazzActivityPersistence.findUsedActivity();
      if (CollectionUtils.isNotEmpty(list)) {
      clazzActivityCacheManager.save(list);
      } else {
          clazzActivityCacheManager.save(Collections.emptyList());
      }
  }

  @Override
  public void addOrUpdateRecord(Long userId, Long schoolId, Long clazzId, Integer activityId,
      Integer status, Map <String, Object> condition) {

      ClazzActivityRecord cr = new ClazzActivityRecord();
        cr.setActivityId(activityId);
        cr.setClazzId(clazzId);
        cr.setSchoolId(schoolId);
        cr.setUserId(userId);
        cr.generateId();
        cr.setStatus(status);
        cr.setCondition(condition);
      ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(cr.getId());
      ClazzActivity clazzActivity = clazzActivityPersistence.load(activityId);
      if (clazzActivity != null) {
          if (clazzActivityRecord == null) {
              //如果有最低展示参与活动人数配置，设置最低活动人数
              if (clazzActivity.getLowActivityNum() != null) {
                  Long acNum = clazzActivityCacheManager.loadByActivity(activityId);
                  if (acNum == null || acNum < clazzActivity.getLowActivityNum()) {
                      clazzActivityCacheManager.setIncreaseByActivity(activityId, clazzActivity.getLowActivityNum());
                  } else {
                      clazzActivityCacheManager.increaseByActivity(activityId);
                  }
              } else {
                  clazzActivityCacheManager.increaseByActivity(activityId);
              }
              preSpecificHandle(cr);
              clazzActivityRecordPersistence.insert(cr);
          } else {
              //补偿数据逻辑
              if (clazzActivityRecord.getActivityId() == 2) {
                  Map<String, Object> objectMap = new HashMap<>();
                  objectMap.put("currentProgress", 0);
                  if (clazzActivityRecord.getCondition() == null || clazzActivityRecord.getCondition().get("currentProgress") == null) {
                      List<ClazzBossUserAward> clazzBossUserAwards = classCircleBossService.getClazzBossAwardList(activityId).stream().map(item -> {
                          ClazzBossUserAward clazzBossUserAward = new ClazzBossUserAward();
                          clazzBossUserAward.setClazzBossAwardId(item.getId());
                          clazzBossUserAward.setReceive(false);
                          return clazzBossUserAward;
                      }).collect(Collectors.toList());
                      objectMap.put(ClazzActivityRecord.CLAZZ_BOSS_USER_AWARD, clazzBossUserAwards);
                      cr.setCondition(objectMap);
                      clazzActivityRecordPersistence.upsert(clazzActivityRecord);
                      return;
                  }
              }
              //感恩节类似活动
              if (clazzActivity != null && clazzActivity.getType() == 4 && clazzActivityRecord.getBizObject() == null) {
                  ChickenStudent chickenStudent = new ChickenStudent();
                  chickenStudent.setJoinClass(false);
                  List<PlotRewardConfig> plotRewardConfigList = plotRewardConfigPersistence.findByActivityId(clazzActivityRecord.getActivityId());
                  Map<String, Integer> chickenMap = new HashMap<>();
                  plotRewardConfigList.forEach(item -> chickenMap.put(item.getId(), 0));
                  chickenStudent.setChickenMap(chickenMap);
                  Map<String, ClazzBossUserAward> userAwardMap = new HashMap<>();
                  getUserAwards(clazzActivityRecord).forEach(item -> userAwardMap.put(item.getClazzBossAwardId(), item));
                  chickenStudent.setUserAwardMap(userAwardMap);
                  clazzActivityRecord.setScore(0);
                  clazzActivityRecord.setBizObject(chickenStudent);
                  clazzActivityRecordPersistence.upsert(clazzActivityRecord);
                  return;
              }
              //传进来为1时候不再更新，初始化就为1
              if (status == 1 || clazzActivityRecord.getStatus() == 0) {
                  return;
              }
              specificHandle(clazzActivityRecord, cr);
              clazzActivityRecord.setStatus(status);
              clazzActivityRecordPersistence.upsert(clazzActivityRecord);
          }
      }

    }

    @Override
    public void updateRecord(ClazzActivityRecord clazzActivityRecord) {
        if (clazzActivityRecord != null) {
            clazzActivityRecordPersistence.upsert(clazzActivityRecord);
        }
    }

    @Override
  public ClazzActivityRecord findUserRecord(Long userId, Long schoolId, Long clazzId,
      Integer activityId) {
      ClazzActivityRecord cr = new ClazzActivityRecord();
      cr.setActivityId(activityId);
      cr.setClazzId(clazzId);
      cr.setSchoolId(schoolId);
      cr.setUserId(userId);
      cr.generateId();
      //补偿数据逻辑
      cr = clazzActivityRecordPersistence.load(cr.getId());
      if (cr != null && cr.getActivityId() == 2) {
          Map<String, Object> objectMap = new HashMap<>();
          objectMap.put("currentProgress", 0);
          if (cr.getCondition() == null || cr.getCondition().get("currentProgress") == null) {
              List<ClazzBossUserAward> clazzBossUserAwards = classCircleBossService.getClazzBossAwardList(activityId).stream().map(item -> {
                  ClazzBossUserAward clazzBossUserAward = new ClazzBossUserAward();
                  clazzBossUserAward.setClazzBossAwardId(item.getId());
                  clazzBossUserAward.setReceive(false);
                  return clazzBossUserAward;
              }).collect(Collectors.toList());
              objectMap.put(ClazzActivityRecord.CLAZZ_BOSS_USER_AWARD, clazzBossUserAwards);
              cr.setCondition(objectMap);
              clazzActivityRecordPersistence.upsert(cr);
          }
      }
      return cr;
  }

  /**
   * 万圣节缓存
   */
  @Override
  public void increase(Integer type) {
    clazzActivityCacheManager.saveHalloweenCount(type);
  }

  @Override
  public Map <String, Long> loadLikedCounts() {
    return clazzActivityCacheManager.queryHalloweenCount();
  }

  @Override
  public Map <String, Object> findBySchooldId(Long schoolId, Integer activityId) {
    Map <String, Object> map = new HashMap <>();
    List <StudentVO> studentVOList = new ArrayList <>();
    List <ClazzActivityRecord> clazzActivityRecords = clazzActivityRecordPersistence
            .findBySchooldId(activityId, schoolId);
    if (CollectionUtils.isEmpty(clazzActivityRecords)) {
      map.put("count", 0);
      map.put("studentVOList", studentVOList);
      return map;
    }

    for (int i = 0; i < 10; i++) {
      if (i < clazzActivityRecords.size()) {
        StudentVO studentVO = new StudentVO();
        ClazzActivityRecord clazzActivityRecord = clazzActivityRecords.get(i);
        studentVO.setUserId(clazzActivityRecord.getUserId());
        studentVOList.add(studentVO);
      }
    }
    map.put("count", studentVOList.size());
    map.put("studentVOList", studentVOList);
    return map;
  }


  private List <String> patternUrl(String targetPattern) {
    List <String> result = new ArrayList <>();
    if (targetPattern.contains("#")) {
      Pattern pattern = Pattern.compile("#.+?#");
      Matcher m = pattern.matcher(targetPattern);
      while (m.find()) {
        result.add(m.group());
      }
    }
    return result;
  }

  private String addUrlParam(String url, String name, String value) {
    StringBuilder sbl = new StringBuilder(url);
    if (url.contains("?")) {
      sbl.append("&").append(name).append("=").append(value);
    } else {
      sbl.append("?").append(name).append("=").append(value);
    }
    return sbl.toString();
  }

    /**
     * 活动个性化设置
     *
     * @param clazzActivityRecord
     */
    private void specificHandle(ClazzActivityRecord clazzActivityRecord, ClazzActivityRecord newClazzActivityRecord) {
        if (newClazzActivityRecord.getActivityId() == 1 && newClazzActivityRecord.getStatus() == 0) {
          //万圣节活动  8个维度的计数
            if (newClazzActivityRecord.getCondition() != null && newClazzActivityRecord.getCondition().get("resultIndex") != null) {
                clazzActivityCacheManager.saveHalloweenCount((Integer) newClazzActivityRecord.getCondition().get("resultIndex"));
            }
        }
    }

    private void preSpecificHandle(ClazzActivityRecord clazzActivityRecord){
        if (clazzActivityRecord.getActivityId() == 2) {
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("currentProgress", 0);
            if (clazzActivityRecord.getCondition() == null || clazzActivityRecord.getCondition().get("currentProgress") == null) {
                objectMap.put(ClazzActivityRecord.CLAZZ_BOSS_USER_AWARD, getUserAwards(clazzActivityRecord));
                clazzActivityRecord.setCondition(objectMap);
            }
        }
        ClazzActivity clazzActivity = clazzActivityPersistence.load(clazzActivityRecord.getActivityId());

        if (clazzActivity.getType() == 3) {
            PlotActivityBizObject plotActivityBizObject = new PlotActivityBizObject();
            plotActivityBizObject.setCurrentHighestDiffiCult(0);
            plotActivityBizObject.setVip(false);
            plotActivityBizObject.setCurrentPlot(PlotInfo.generatorId(clazzActivity.getId(), 1, 1));
            plotActivityBizObject.setFirstEntry(true);
            plotActivityBizObject.setAppkey("AfentiMath");
            plotActivityBizObject.setFirstBuy(0);
            clazzActivityRecord.setBizObject(plotActivityBizObject);
        }
        //感恩节类似活动
        if (clazzActivity.getType() == 4) {
            ChickenStudent chickenStudent = new ChickenStudent();
            chickenStudent.setJoinClass(false);
            List<PlotRewardConfig> plotRewardConfigList = plotRewardConfigPersistence.findByActivityId(clazzActivityRecord.getActivityId());
            Map<String, Integer> chickenMap = new HashMap<>();
            plotRewardConfigList.forEach(item -> chickenMap.put(item.getId(), 0));
            chickenStudent.setChickenMap(chickenMap);
            Map<String, ClazzBossUserAward> userAwardMap = new HashMap<>();
            getUserAwards(clazzActivityRecord).forEach(item -> userAwardMap.put(item.getClazzBossAwardId(), item));
            chickenStudent.setUserAwardMap(userAwardMap);
            clazzActivityRecord.setScore(0);
            clazzActivityRecord.setBizObject(chickenStudent);
        }
    }

    /**
     * 获取用户奖励配置
     */
    private List<ClazzBossUserAward> getUserAwards(ClazzActivityRecord clazzActivityRecord) {
        return classCircleBossService.getClazzBossAwardList(clazzActivityRecord.getActivityId()).stream().map(item -> {
            ClazzBossUserAward clazzBossUserAward = new ClazzBossUserAward();
            clazzBossUserAward.setClazzBossAwardId(item.getId());
            clazzBossUserAward.setReceive(false);
            return clazzBossUserAward;
        }).collect(Collectors.toList());
    }


    /**
   * 存储couchbase键值到mongo
   **/
  @Override
  public void saveOrUpdateCouchBaseToMongo(ClassCircleCouchBaseRecord classCircleCouchBaseRecord) {

    ClassCircleCouchBaseRecord classCircleCouchBaseRecord1 = classCircleCouchBasePersistence
        .load(classCircleCouchBaseRecord.getCouchBaseKey());
    if (classCircleCouchBaseRecord1 == null) {
      classCircleCouchBasePersistence.insert(classCircleCouchBaseRecord);
    } else {
      classCircleCouchBasePersistence.upsert(classCircleCouchBaseRecord);
    }
  }

  @Override
  public ClassCircleCouchBaseRecord queryCouchBase(String couchBaseKey) {
    return classCircleCouchBasePersistence.load(couchBaseKey);
  }

  /**
   * 存储couchbase的key到mongo
   **/
  @Override
  public void saveOrUpdateCouchBaseKeyToMongo(String key) {

    ClassCircleCouchBaseKey classCircleCouchBaseKey=classCircleCouchBaseKeyPersistence.load(key);
    if (classCircleCouchBaseKey!= null) {
      classCircleCouchBaseKey.setCouchBaseKey(key);
      classCircleCouchBaseKeyPersistence.upsert(classCircleCouchBaseKey);
    } else {
      classCircleCouchBaseKey= new ClassCircleCouchBaseKey();
      classCircleCouchBaseKey.setCouchBaseKey(key);
      classCircleCouchBaseKey.setId(key);
      classCircleCouchBaseKeyPersistence.insert(classCircleCouchBaseKey);
    }
  }

  @Override
  public List <ClassCircleCouchBaseKey> queryCouchBaseKey() {
    return classCircleCouchBaseKeyPersistence.query();
  }

  @Override
  public Long loadByKey(String key) {
    String str =clazzActivityCacheManager.loadByKey(key);
    return str==null?0L:Long.valueOf(str);
  }

  @Override
  public void setValueByKey(String key,String value){
    clazzActivityCacheManager.setValueByKey(key,value);
  }

  @Override
  public void deleteCache(String key) {
    clazzActivityCacheManager.deleteCache(key);
  }

    @Override
    public void deleteActivityListCache() {
        clazzActivityCacheManager.deleteActivityCache();
    }
}
