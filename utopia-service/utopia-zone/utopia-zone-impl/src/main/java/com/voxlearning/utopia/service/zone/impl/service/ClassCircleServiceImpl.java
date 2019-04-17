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

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.service.question.api.DubbingLoader;
import com.voxlearning.utopia.service.question.api.constant.ApplyToType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.zone.api.ClassCircleService;
import com.voxlearning.utopia.service.zone.api.entity.*;
import com.voxlearning.utopia.service.zone.api.entity.plot.DailySertenceModifySignConfig;
import com.voxlearning.utopia.service.zone.data.DiscussDetail;
import com.voxlearning.utopia.service.zone.data.DiscussResult;
import com.voxlearning.utopia.service.zone.impl.persistence.*;
import com.voxlearning.utopia.service.zone.impl.support.ClazzActivityCacheManager;
import com.voxlearning.utopia.service.zone.impl.support.DiscussZoneCacheManager;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Named("com.voxlearning.utopia.service.zone.impl.service.ClassCircleServiceImpl")
@ExposeService(interfaceClass = ClassCircleService.class, version = @ServiceVersion(version = "20181023"))
@Slf4j
public class ClassCircleServiceImpl implements ClassCircleService {

  @Inject
  private PictureBookPlusServiceClient pictureBookPlusServiceClient;
  @Inject
  private DiscussZonePersistence discussZonePersistence;
  @Inject
  private DiscussZoneCacheManager discussZoneCacheManager;
  @Resource
  private DailySertenceDao dailySertenceDao;
  @Resource
  private DailySentenceWithMySqlDao dailySertenceWithMySqlDao;
  @Resource
  private UserDailySentenceDao userDailySertenceDao;

  @Inject
  private DiscussRecordPersistence discussRecordPersistence;

  @Inject
  private UserPicRecordDao userPicRecordDao;

  @Resource
  private DailySertenceModifySignDao dailySertenceModifySignDao;

  @Resource
  private ClazzActivityPersistence clazzActivityPersistence;

  @Resource
  private ClazzActivityCacheManager clazzActivityCacheManager;

  private static final String TOKEN = "17zuoyecircle";
  @ImportService(interfaceClass = DubbingLoader.class)
  private DubbingLoader dubbingLoader;
  @Inject
  private GrayFunctionManagerClient grayFunctionManagerClient;
  @Resource
  private StudentLoaderClient studentLoaderClient;


  @Override
  public WeekDailySentence getDailySentence(Long userId) {
    List <WeekDailySentence> weekDailySentences = dailySertenceDao.queryAll();
    /**获取起始周**/
    WeekDailySentence weekDailySentenceBegin = weekDailySentences.get(0);

    int currentWeek = getCurrentWeekOfYear();
    /**上线是第43周**/
    int beginWeek =weekDailySentenceBegin.getBeginWeek();
    String dateStr =DateUtils.dateToString(new Date(),"yyyyMMdd");
    if(getCurrentYear()==2019||dateStr.equals("20181231")){
      beginWeek=-8;
    }
    int preCurrentWeek = currentWeek - beginWeek;
    /**对每日签到取余获取每个月不同的句子**/
    int value = preCurrentWeek % weekDailySentences.size();

    WeekDailySentence weekDailySentence = weekDailySentences.get(value);

    UserPicRecord userPicRecord =userPicRecordDao.load(UserPicRecord.generateId(userId,getCurrentYear(),getCurrentWeekOfYear()));
    if (userPicRecord == null) {
      return weekDailySentence;
    }
    for (int k = 0; k <userPicRecord.getWeekRecord().size(); k++) {
      if (userPicRecord.getWeekRecord().get(k)) {
        weekDailySentence.getWeekDailySentence().get(k).setFinished(true);
      }
    }
    return weekDailySentence;
  }

    @Override
    public List<WeekDailySentence> queryAllWeekDailySentence() {
        return  dailySertenceDao.queryAll();
    }

    public static int getIndex() {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.setFirstDayOfWeek(Calendar.MONDAY);
      int day = cal.get(Calendar.DAY_OF_WEEK);
      if(day==1){
        day =6;
      }else{
        day =day-2;
      }
      return day;
  }


  @Override
  public void modifySign(Long userId,Integer modifySign){
    UserPicRecord userPicRecord =userPicRecordDao.load(UserPicRecord.generateId(userId,getCurrentYear(),getCurrentWeekOfYear()));
    if (userPicRecord == null) {
      return ;
    }
    List <Boolean> lists = userPicRecord.getWeekRecord();
    Boolean flag =false;
    for (int j = 0; j < getIndex(); j++) {
      if(!lists.get(j)){
        flag=true;
        lists.set(j,true);
        break;
      }
    }
    if(!flag){
      return;
    }
    userPicRecord.setUpdateAt(new Date());
    userPicRecordDao.upsert(userPicRecord);
    List<SignRecord> signRecords = new ArrayList <>();
    SignRecord signRecord = new SignRecord();
    signRecord.setSignType(modifySign);
    signRecords.add(signRecord);
    clazzActivityCacheManager.setModifySignUserCountCache(userId,signRecords);
  }


  @Override
  public String saveWeekDailySenate(List <DailySentence> dailySentences, String token,String wholePic,Integer beginWeek) {
    if (!TOKEN.equals(token)) {
      return "error";
    }
    WeekDailySentence weekDailySentence = new WeekDailySentence();
    weekDailySentence.setId(WeekDailySentence.generateId());
    weekDailySentence.setWeekDailySentence(dailySentences);
    weekDailySentence.setCreateAt(new Date());
    weekDailySentence.setUpdateAt(new Date());
    weekDailySentence.setDelete(false);
    weekDailySentence.setWholePic(wholePic);
    weekDailySentence.setBeginWeek(beginWeek);
    dailySertenceDao.insert(weekDailySentence);
    return "success";
  }

  @Override
  public MapMessage saveUserSignIn(Long userId) {
    int currentWeek = getCurrentWeekOfYear();
    Integer currentYear =getCurrentYear();
    UserPicRecord userPicRecord = userPicRecordDao.load(UserPicRecord.generateId(userId,currentYear,currentWeek));
    WeekDailySentence weekDailySentence =getDailySentence(userId);
    if(userPicRecord==null){
      List<Boolean> list =Lists.newArrayList(false,false,false,false,false,false,false);
      for(int i = 0; i<list.size();i++){
        if(i==getIndex()){
          list.set(i,true);
          break;
        }
      }
      userPicRecord = new UserPicRecord();
      userPicRecord.setCreateAt(new Date());
      userPicRecord.setUpdateAt(new Date());
      userPicRecord.setUserId(userId);
      userPicRecord.setId(UserPicRecord.generateId(userId,currentYear,currentWeek));
      userPicRecord.setPicPath(weekDailySentence.getWholePic());
      userPicRecord.setWeek(currentWeek);
      userPicRecord.setWeekDailySentenceId(weekDailySentence.getId());
      userPicRecord.setYear(currentYear);
      userPicRecord.setWeekRecord(list);
    }else{
      if(userPicRecord.getWeekRecord().get(getIndex())){
        return MapMessage.errorMessage().set("msg", "今天您已签到");
      }
      userPicRecord.getWeekRecord().set(getIndex(),true);
      userPicRecord.setUpdateAt(new Date());
    }
    userPicRecordDao.upsert(userPicRecord);
    return MapMessage.successMessage().set("msg", "今天您签到成功").set("info",getDailySentence(userId));
  }


  private void saveOrUpdateUserPicRecord(Long userId){
    int currentWeek = getCurrentWeekOfYear();
    Integer currentYear =getCurrentYear();
    UserPicRecord userPicRecord = userPicRecordDao.load(UserPicRecord.generateId(userId,currentYear,currentWeek));
    WeekDailySentence weekDailySentence =getDailySentence(userId);
    List<Boolean> list =Lists.newArrayList();
    List<DailySentence> list1 =weekDailySentence.getWeekDailySentence();
    for (int i = 0; i <list1.size() ; i++) {
      list.add(list1.get(i).getFinished());
    }
    if(userPicRecord==null){
      userPicRecord = new UserPicRecord();
      userPicRecord.setCreateAt(new Date());
      userPicRecord.setUpdateAt(new Date());
      userPicRecord.setUserId(userId);
      userPicRecord.setId(UserPicRecord.generateId(userId,currentYear,currentWeek));
      userPicRecord.setPicPath(weekDailySentence.getWholePic());
      userPicRecord.setWeek(currentWeek);
      userPicRecord.setWeekDailySentenceId(weekDailySentence.getId());
      userPicRecord.setYear(currentYear);
      userPicRecord.setWeekRecord(list);
    }else{
      userPicRecord.setWeekRecord(list);
      userPicRecord.setUpdateAt(new Date());
    }
    userPicRecordDao.upsert(userPicRecord);
  }

  @Override
  public UserDailySentenceRecord getRecordByUserId(Long userId) {
    UserDailySentenceRecord userDailySentenceRecords =
        userDailySertenceDao.queryRecord(getCurrentYear(), userId);
    if (userDailySentenceRecords == null) {
      return null;
    }
    return userDailySentenceRecords;
  }

  @Override
  public List <DiscussZone> findUsedDiscuss() {
    List <DiscussZone> usedDiscuss = discussZoneCacheManager.findUsedDiscussZoneCache();
    if (CollectionUtils.isNotEmpty(usedDiscuss)) {
      return usedDiscuss;
    } else {
      usedDiscuss = discussZonePersistence.findUsedDiscuss();
      if (CollectionUtils.isNotEmpty(usedDiscuss)) {
        discussZoneCacheManager.save(updateDiscussZone(usedDiscuss));
      }
    }
    return usedDiscuss;
  }


  private static int getCurrentWeekOfYear() {
    Date date = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setFirstDayOfWeek(Calendar.MONDAY);
    calendar.setTime(date);
    return calendar.get(Calendar.WEEK_OF_YEAR);
  }

  private static int getCurrentDayOfWeek() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.setFirstDayOfWeek(Calendar.MONDAY);
    return cal.get(Calendar.DAY_OF_YEAR);
  }

  private static int getCurrentDayOfYear() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.setFirstDayOfWeek(Calendar.MONDAY);
    return cal.get(Calendar.DAY_OF_YEAR);
  }

  private int getCurrentYearHaveDays() {
    int year = getCurrentYear();
    int days;//某年(year)的天数
    /**闰年的判断规则**/
    if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
      days = 366;
    } else {
      days = 365;
    }
    return days;
  }


  private static int getCurrentYear() {
    Calendar date = Calendar.getInstance();
    if(getCurrentWeekOfYear()==1&&date.get(Calendar.MONTH)==11){
      return date.get(Calendar.YEAR)+1;
    }
    return date.get(Calendar.YEAR);
  }

  public static void main(String[] args) {
    System.out.println(getCurrentWeekOfYear());
  }
  public static int getFirstDayOfWeek(int year, int week) {
    Calendar cal = Calendar.getInstance();
    //设置年份
    cal.set(Calendar.YEAR, year);
    //设置周
    cal.set(Calendar.WEEK_OF_YEAR, week);
    //设置该周第一天为星期一
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    int i = getDayOfYear(cal.getTime());
    return i;
  }

  private static int getDayOfYear(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    return cal.get(Calendar.DAY_OF_YEAR);
  }

  @Override
  public void addOrUpdate(DiscussZone discussZone) {
    if (discussZone.getId() == null) {
      //暂时不支持自增主键，由于是配置表 暂时自己定义ID 让id有意义
      return;
    }
    if (discussZonePersistence.load(discussZone.getId()) != null) {
      discussZonePersistence.upsert(discussZone);
    } else {
      discussZonePersistence.insert(discussZone);
    }
    List <DiscussZone> list = discussZonePersistence.findUsedDiscuss();
    if (CollectionUtils.isNotEmpty(list)) {
      discussZoneCacheManager.save(updateDiscussZone(list));
    }
  }

  private List <DiscussZone> updateDiscussZone(List <DiscussZone> usedDiscuss) {
    // 付费的随机挑四本
    Predicate <PictureBookPlus> filterChargedPd = pb -> {
      Integer freeFlag = pb.getFreeMap().getOrDefault(ApplyToType.SELF, 0);
      return freeFlag == 2;
    };
    // 过滤免费的绘本
    Predicate <PictureBookPlus> filterFreePd = pb -> {
      Integer freeFlag = pb.getFreeMap().getOrDefault(ApplyToType.SELF, 0);
      return freeFlag == 1;
    };
    usedDiscuss = usedDiscuss.stream().map(e -> {
      List<DiscussResult> discussResults = null;
      if (e.getType() == 2) {
        //配音
        List <Dubbing> dubbs = null;
        if (CollectionUtils.isNotEmpty(e.getIds())) {
          List<String> dubbId = e.getIds().stream().map(dubbIdAndNodeId -> dubbIdAndNodeId.split("\\|")[0]).collect(Collectors.toList());
          List<String> nodeId = e.getIds().stream().map(dubbIdAndNodeId -> dubbIdAndNodeId.split("\\|")[1]).collect(Collectors.toList());
          Map<String, Dubbing> stringDubbingMap = dubbingLoader.loadDubbingByDocIds(dubbId);
          discussResults = new ArrayList<>();
          for (int i = 0; i < dubbId.size(); i++) {
            Dubbing dubbing = stringDubbingMap.get(dubbId.get(i));
            DiscussResult dr = new DiscussResult();
            DiscussDetail discussDetail = new DiscussDetail();
            discussDetail.setCoverThumbnailUrl(dubbing.getCoverThumbnailUrl());
            discussDetail.setCoverUrl(dubbing.getCoverUrl());
            discussDetail.setId(nodeId.get(i));
            dr.setDubbing(discussDetail);
            dr.setIsPay(false);
            discussResults.add(dr);
          }
        }
      } else {
        //绘本
        List <PictureBookPlus> books = null;
        if (CollectionUtils.isNotEmpty(e.getIds())) {
          books = loadPicBooksByIds(e.getIds(), pictureBookPlusServiceClient);
        }
        if (books == null || books.size() < 5) {
          if (books == null) {
            books = new ArrayList <>();
          }
          //小于5个从内容库随机出来几个 1期只筛选免费绘本
          List <PictureBookPlus> contentBooks = loadPicBooks(pictureBookPlusServiceClient).stream()
              .filter(filterFreePd).collect(Collectors.toList());
          int size = books.size();
          for (int i = 0; i < 5 - size; i++) {
            books.add(contentBooks.get(RandomUtils.nextInt(contentBooks.size() - 1)));
          }
        }
        discussResults = books.stream().map(book -> {
          DiscussResult dr = new DiscussResult();
          DiscussDetail discussDetail = new DiscussDetail();
          discussDetail.setCoverThumbnailUrl(book.getCoverThumbnailUrl());
          discussDetail.setCoverUrl(book.getCoverUrl());
          discussDetail.setId(book.getId());
          dr.setPictureBook(discussDetail);
          dr.setIsPay(false);
          //下期加判断支付 和获取产品id逻辑
          return dr;
        }).collect(Collectors.toList());
      }
      e.setDetail(discussResults);
      return e;

    }).collect(Collectors.toList());
    return usedDiscuss;
  }


  private List <PictureBookPlus> loadPicBooks(
      PictureBookPlusServiceClient pictureBookPlusServiceClient) {
    Collection <PictureBookPlus> picBookList = loadPicBooksMap(pictureBookPlusServiceClient)
        .values();

    return picBookList.stream()
        .filter(pb -> pb.pbApplyTo() != null)
        .filter(pb -> pb.pbApplyTo().contains(ApplyToType.SELF.name()))
        // 先过滤掉资源为空的那些记录
        .filter(pb -> StringUtils.isNotBlank(pb.getIosFileUrl()))
        .filter(pb -> "ONLINE".equals(pb.getStatus()))
        .collect(Collectors.toList());
  }

  private Map <String, PictureBookPlus> loadPicBooksMap(
      PictureBookPlusServiceClient pictureBookPlusServiceClient) {
    return pictureBookPlusServiceClient.toMap();
  }

  public List <PictureBookPlus> loadPicBooksByIds(Collection <String> bookIds,
      PictureBookPlusServiceClient
          pictureBookPlusServiceClient) {
    if (com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils
        .isEmpty(bookIds)) {
      return Collections.emptyList();
    }

    List <PictureBookPlus> picBooks = new ArrayList <>();
    Map <String, PictureBookPlus> pictureBookMap = loadPicBooksMap(pictureBookPlusServiceClient);
    for (String bookId : bookIds) {
      PictureBookPlus pb = pictureBookMap.get(bookId);
      if (pb == null) {
        continue;
      }

      picBooks.add(pb);
    }

    return picBooks;
  }

  @Override
  public void saveOrUpdateDiscussRecord(Long userId, Long clazzId, Integer discussId) {
    String id = discussId + "_" + clazzId + "_" + userId;
    DiscussZoneUserRecord discussZoneUserRecord = discussRecordPersistence.load(id);
    if (discussZoneUserRecord != null) {
      discussRecordPersistence.upsert(discussZoneUserRecord);
    } else {
      discussZoneUserRecord = new DiscussZoneUserRecord();
      discussZoneUserRecord.setId(discussId + "_" + clazzId + "_" + userId);
      discussRecordPersistence.insert(discussZoneUserRecord);
      //更新缓存 其实不是很要求正确性  暂时不加锁
      List<DiscussZoneUserRecord> recordCache = discussZoneCacheManager
              .findRecordCache(discussId, clazzId);
      if (CollectionUtils.isNotEmpty(recordCache)) {
        recordCache.add(discussZoneUserRecord);
      } else {
        recordCache = discussRecordPersistence.patternId(discussId, clazzId);
      }
      discussZoneCacheManager.saveRecord(discussId, clazzId, recordCache.stream().distinct().collect(Collectors.toList()));
    }

  }

  @Override
  public List <DiscussZoneUserRecord> getDiscussRecord(Long clazzId, Integer discussId) {
    List <DiscussZoneUserRecord> recordCache = discussZoneCacheManager
        .findRecordCache(discussId, clazzId);
    if (CollectionUtils.isNotEmpty(recordCache)) {
      return recordCache;
    } else {
      recordCache = discussRecordPersistence.patternId(discussId, clazzId);
      if (CollectionUtils.isNotEmpty(recordCache)) {
        discussZoneCacheManager.saveRecord(discussId, clazzId, recordCache);
      }
    }
    return recordCache;
  }

  @Override
  public void updateWeekDailySenate(List <DailySentence> dailySentences, String id,Integer beginWeek,String wholePic) {
    WeekDailySentence weekDailySentence = dailySertenceDao.load(id);
    if (weekDailySentence != null) {
      weekDailySentence.setWeekDailySentence(dailySentences);
      weekDailySentence.setBeginWeek(beginWeek);
      weekDailySentence.setWholePic(wholePic);
      dailySertenceDao.upsert(weekDailySentence);
    }

  }

  @Override
  public void deleteDiscussCache() {
    discussZoneCacheManager.deleteDiscussCache();
  }

  @Override
  public void saveOrUpdateModifySignType(DailySertenceModifySignConfig dailySertenceModifySignConfig) {
    dailySertenceModifySignDao.upsert(dailySertenceModifySignConfig);
  }

  @Override
  public List <DailySertenceModifySignConfig> queryDailySertenceModifySignConfigList(Long userId){
    StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
    boolean isInBlankList = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ClassZoneDailySentence", "BlackList");
    if (isInBlankList) {
      return null;
    }

    /**判断需要补签天数**/
    WeekDailySentence weekDailySentence = getDailySentence(userId);
    int count = 0;
    if(weekDailySentence==null){
     return null;
    }else{
      List<DailySentence> dailySentences =weekDailySentence.getWeekDailySentence();
      int index= getIndex();
      for (int i = 0; i <index; i++) {
        if(!dailySentences.get(i).getFinished()){
          count++;
        }
      }
      if(count==0){
        return null;
      }
    }

    List <DailySertenceModifySignConfig> list= dailySertenceModifySignDao.queryAll();
    /**产品定义没有配置优先级就默认查询**/
    if(CollectionUtils.isEmpty(list)){
      list =dailySertenceModifySignDao.query();
    }
    List <SignRecord> signRecords =clazzActivityCacheManager.loadModifySignUserCountCache(userId);
    if(CollectionUtils.isNotEmpty(signRecords)&&signRecords.size()==3){
      return null;
    }
    /**过滤掉已经补签的类型**/
    if(CollectionUtils.isNotEmpty(signRecords)){
      List<Integer> integerList = new ArrayList <>();
      for (SignRecord signRecord:signRecords) {
        integerList.add(signRecord.getSignType());
      }
     list= list.stream().filter(e->!integerList.contains(e.getSignType())).collect(Collectors.toList());
    }

    List<DiscussZone> discussZones =discussZonePersistence.findUsedDiscuss();
    List<ClazzActivity> clazzActivities =clazzActivityPersistence.findUsedActivity();
    if(CollectionUtils.isEmpty(clazzActivities)){
      list =list.stream().filter(e-> e.getSignType()!=1 ).collect(Collectors.toList());
    }

    if(CollectionUtils.isNotEmpty(discussZones)){
      /**目前讨论区只能配置一种讨论类型，所以过滤掉没有的类型**/
      if(discussZones.get(0).getType().equals(2)){
        list =list.stream().filter(e-> e.getSignType()!=3 ).collect(Collectors.toList());
      }
      if(discussZones.get(0).getType().equals(3)){
       list = list.stream().filter(e-> e.getSignType()!=2).collect(Collectors.toList());
      }
    }
    if(CollectionUtils.isEmpty(list)){
      return null;
    }
    if(count>list.size()){
      list=list.subList(0,list.size());
    }else{
      list=list.subList(0,count);

    }
    list.forEach(e->{
      if(e.getSignType()==2){
        discussZones.stream().filter(f->{
          if(f.getType()==2){
            e.setUrl(f.getIds().get(0));
          }
          return true;
        }).collect(Collectors.toList());
      }
      if(e.getSignType()==3){
        discussZones.stream().filter(f->{
          if(f.getType()==3){
            e.setUrl(f.getIds().get(0));
          }
          return true;
        }).collect(Collectors.toList());
      }
      if(e.getSignType()==1){
        e.setContent(e.getContent().replace("xxxxx",clazzActivities.get(0).getName()));
        e.setUrl(clazzActivities.get(0).getId().toString());
      }
      if(e.getSignType()==5){
        e.setUrl("MATH");
      }

      if(e.getSignType()==4){
        e.setUrl("CHINESE");
      }
      if(e.getSignType()==6){
        e.setUrl("ENGLISH");
      }
    });
    return list;
  }

  @Override
  public List <SignRecord> loadModifySignUserCountCache(Long userId) {
    List <SignRecord> signRecords =clazzActivityCacheManager.loadModifySignUserCountCache(userId);
    if(CollectionUtils.isEmpty(signRecords)){
      return null;
    }
    signRecords =signRecords.stream().filter(e->{
      if(!e.getFinished()){
        return true;
      }else{
        return false;
      }
    }).collect(Collectors.toList());
    if(CollectionUtils.isNotEmpty(signRecords)){
      for (SignRecord signRecord:signRecords) {
        clazzActivityCacheManager.updateModifySign(userId,signRecord.getSignType());
      }
    }
    return signRecords;
  }

  @Override
  public void updateModifySign(Long userId, Integer type) {
    clazzActivityCacheManager.updateModifySign(userId,type);
  }

  @Override
  public List<DiscussZone> getById(Integer id) {
    DiscussZone dz = discussZonePersistence.load(id);
    if (dz == null) {
      return null;
    }
    return updateDiscussZone(Collections.singletonList(dz));
  }


}