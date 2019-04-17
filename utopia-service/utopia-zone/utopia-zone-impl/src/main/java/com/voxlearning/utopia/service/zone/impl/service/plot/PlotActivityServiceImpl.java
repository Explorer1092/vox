package com.voxlearning.utopia.service.zone.impl.service.plot;

import com.google.common.base.Strings;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.zone.api.ClassCirclePlotService;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivity;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRecord;
import com.voxlearning.utopia.service.zone.api.entity.plot.*;
import com.voxlearning.utopia.service.zone.api.plot.PlotActivityService;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzActivityPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzActivityRecordPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ZoneClazzRewardPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.plot.PlotActivitySelectRecordPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.plot.PlotInfoDatePersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.plot.PlotInfoPersistence;
import com.voxlearning.utopia.service.zone.impl.support.ClazzActivityCacheManager;
import com.voxlearning.utopia.service.zone.impl.support.ClazzPlotActivitySelectCache;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Named("com.voxlearning.utopia.service.zone.impl.service.PlotActivityServiceImpl")
@ExposeService(interfaceClass = PlotActivityService.class, version = @ServiceVersion(version = "20181109"))
@Slf4j
public class PlotActivityServiceImpl implements PlotActivityService {

    @Resource
    private PlotInfoPersistence plotInfoPersistence;

    @Resource
    private ClazzActivityRecordPersistence clazzActivityRecordPersistence;

    @Resource
    private PlotInfoDatePersistence plotInfoDatePersistence;

    @Resource
    private PlotInfoInit plotInfoInit;

    @Resource
    private PlotActivitySelectRecordPersistence plotActivitySelectRecordPersistence;

    @Resource
    private ClazzActivityCacheManager clazzActivityCacheManager;

    @Resource
    private ClassCirclePlotService classCirclePlotService;

    @Resource
    private ZoneClazzRewardPersistence zoneClazzRewardPersistence;

    @Resource
    private ClazzActivityPersistence clazzActivityPersistence;

    private static final String PLOT_LIST = "plotList", DATA = "data", VIP_USER = "vip";

    private static long BASE_POPUP_USER_MAY = 56986,BASE_POPUP_USER_IMPOSSIBLE=39601;

    private static final int CONTRIBUTE = 10;

    private static final List<String> APP_KEY = new ArrayList<>();

    static {
        APP_KEY.add("AfentiMath");
        APP_KEY.add("AfentiExam");
        APP_KEY.add("AfentiChinese");
    }

    @Override
    public PlotInfo savePlotInfo(Integer activityId, Integer plotGroup, Integer plotNum,
                                 String title, String text, String audioUrl,
                                 String dialogString, Boolean needUnlock, Integer lastPlotGroup, Integer lastPlotNum,
                                 Integer nextPlotGroup, Integer nextPlotNum) {
        String id = PlotInfo.generatorId(activityId, plotGroup, plotNum);
        if (id == null) return null;
        PlotInfo plotInfo = new PlotInfo();
        plotInfo.setId(id);                 //剧情活动id
        plotInfo.setActivityId(activityId);
        plotInfo.setPlotGroup(plotGroup);
        plotInfo.setPlotNum(plotNum);
        plotInfo.setTitle(title);           //剧情活动标题
        plotInfo.setText(text);             //剧情活动字幕
        plotInfo.setAudioUrl(audioUrl);     //剧情活动音频
        plotInfo.setNeedUnlock(needUnlock); //剧情需不需要解锁
        plotInfo.setLastPlotId(PlotInfo.generatorId(activityId, lastPlotGroup, lastPlotNum));     //上一个剧情id
        plotInfo.setNextPlotId(PlotInfo.generatorId(activityId, nextPlotGroup, nextPlotNum));     //下一个剧情id
        try {
            if (!Strings.isNullOrEmpty(dialogString)) {       //NPC话语
                if (plotInfo.getDialog() != null)
                    plotInfo.getDialog().clear();
                else plotInfo.setDialog(new ArrayList<>());
                String[] dialogArrays = dialogString.split(",");
                for (String s : dialogArrays) {
                    String[] dialogArray = s.split("_");
                    Dialog dialog = new Dialog();
                    int len = dialogArray.length;
                    dialog.setOrder(len > 0 ? Integer.valueOf(dialogArray[0]) : null);
                    dialog.setText(len > 1 ? dialogArray[1] : null);
                    dialog.setAudio(len > 2 ? dialogArray[2] : null);
//                    Map<String, Double> coordinate = new HashMap<>();
//                    coordinate.put(Dialog.COORDINATE_X, len > 3 && !dialogArray[3].isEmpty() ? Double.valueOf(dialogArray[3]) : null);
//                    coordinate.put(Dialog.COORDINATE_Y, len > 4 && !dialogArray[4].isEmpty() ? Double.valueOf(dialogArray[4]) : null);
//                    if (coordinate.get(Dialog.COORDINATE_X) != null)
//                        dialog.setCoordinate(coordinate);
                    Popup popup = new Popup();
                    popup.setText(len > 3 && !dialogArray[3].isEmpty() ? dialogArray[3] : null);
                    popup.setButton1(len > 4 && !dialogArray[4].isEmpty() ? dialogArray[4] : null);
                    popup.setButton2(len > 5 && !dialogArray[5].isEmpty() ? dialogArray[5] : null);
                    if (popup.getText() != null)
                        dialog.setPopup(popup);
//                    PicEffect picEffect = new PicEffect();
//                    picEffect.setUrl(len > 6 && !dialogArray[6].isEmpty() ? dialogArray[6] : null);
//                    Map<String, Double> picEffectCoordinate = new HashMap<>();
//                    picEffectCoordinate.put(Dialog.COORDINATE_X, len > 7 && !dialogArray[7].isEmpty() ? Double.valueOf(dialogArray[7]) : null);
//                    picEffectCoordinate.put(Dialog.COORDINATE_Y, len > 8 && !dialogArray[8].isEmpty() ? Double.valueOf(dialogArray[8]) : null);
//                    picEffect.setCoordinate(picEffectCoordinate);
//                    if (picEffect.getUrl() != null)
//                        dialog.setPicEffect(picEffect);
                    dialog.setPic(len > 6 ? dialogArray[6] : null);
                    plotInfo.getDialog().add(dialog);
                }
            }
        } catch (Exception ex) {
            return null;
        }
        return plotInfoPersistence.upsert(plotInfo);
    }

    @Override
    public PlotInfo updatePlotInfo(String id, String unlockCoverImg, String bgm) {
        PlotInfo plotInfo = plotInfoPersistence.load(id);
        if (plotInfo == null) return null;
        plotInfo.setUnlockCoverImg(unlockCoverImg);
        plotInfo.setBgm(bgm);
        return plotInfoPersistence.upsert(plotInfo);
    }

    @Override
    public List<PlotInfo> queryPlotInfoList(Integer activityId) {
        return plotInfoPersistence.getPlotInfoList().stream().filter(Objects::nonNull)
                .sorted(Comparator.comparing(PlotInfo::getId))
                .collect(Collectors.toList());
    }

    @Override
    public MapMessage getPlotInfoGroupList(Integer activityId, Long schoolId, Long clazzId, Long userId) {
        List<PlotInfoGroupBo> plotInfoGroupBoList = plotInfoPersistence.getPlotInfoList()
                .stream()
                .filter(item -> item.getPlotNum() == 1)
                .sorted(Comparator.comparing(PlotInfo::getPlotGroup))
                .map(PlotInfoGroupBo::new).collect(Collectors.toList());
        Boolean vipUser = true;
        Integer firstBuy = 0;
        ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activityId, schoolId, clazzId, userId));
        if (clazzActivityRecord == null)
            return MapMessage.errorMessage("未创建活动数据").add(VIP_USER, true).add("firstBuy", firstBuy).add("APP_KEY", APP_KEY).add(DATA, plotInfoGroupBoList);
        Object object = clazzActivityRecord.getBizObject();
        PlotActivityBizObject plotActivityBizObject = JsonUtils.fromJson(JsonUtils.toJson(object), PlotActivityBizObject.class);
        if (plotActivityBizObject != null) {
            if (plotActivityBizObject.getVip() != null) vipUser = plotActivityBizObject.getVip();
            if (plotActivityBizObject.getFirstBuy() != null) firstBuy = plotActivityBizObject.getFirstBuy();
        } else {
            return MapMessage.errorMessage("未初始化活动属性").add(VIP_USER, true).add("firstBuy", firstBuy).add("APP_KEY", APP_KEY).add(DATA, plotInfoGroupBoList);
        }
        return MapMessage.successMessage("成功").add(VIP_USER, vipUser).add("firstBuy", firstBuy).add("APP_KEY", APP_KEY).add(DATA, plotInfoGroupBoList);
    }

    @Override
    public MapMessage getUserCurrentPlotInfo(Integer activityId, Long schoolId, Long clazzId, Long userId) {
        String currentPlot = PlotInfo.generatorId(activityId, 1, 1);
        Boolean openNewPlot = false;
        Boolean firstEntry = true;
        ClazzActivity clazzActivity = clazzActivityPersistence.load(activityId);
        if(clazzActivity==null||clazzActivity.getType()!=3)
            return MapMessage.errorMessage("活动不匹配").add("code", "activityNotMatch").add("openNewPlot", false).add(DATA, currentPlot);
        ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activityId, schoolId, clazzId, userId));
        if (clazzActivityRecord == null)
            return MapMessage.errorMessage("未创建活动数据").add("code", "notCreateActivityData").add("openNewPlot", false).add(DATA, currentPlot);
        Object object = clazzActivityRecord.getBizObject();
        PlotActivityBizObject plotActivityBizObject = JsonUtils.fromJson(JsonUtils.toJson(object), PlotActivityBizObject.class);
        if (plotActivityBizObject != null) {
            currentPlot = plotActivityBizObject.getCurrentPlot();
            if(plotActivityBizObject.getFirstEntry()==null||!plotActivityBizObject.getFirstEntry()) {
                firstEntry = false;
                long time = System.currentTimeMillis();
                List<PlotInfoDate> plotInfoDateList = plotInfoDatePersistence.getPlotInfoDateList(activityId);
                PlotInfoDate plotInfoDate = plotInfoDateList.stream().filter(Objects::nonNull)
                        .filter(p -> time > p.getOpenDate().getTime() && time < p.getEndDate().getTime() && time < p.getOpenDate().getTime() + DateUtils.DAY_TIME_LENGTH_IN_MILLIS)
                        .findFirst().orElse(null);
                if (plotInfoDate != null && plotInfoDate.getPlotGroup() > 1) {
                    if(plotActivityBizObject.getEntryNewPlot()==null){
                        Set<Integer> entryNewPlotSet = new HashSet<>();
                        plotActivityBizObject.setEntryNewPlot(entryNewPlotSet);
                    }
                    Set<Integer> entryNewPlotSet = plotActivityBizObject.getEntryNewPlot();
                    if(!entryNewPlotSet.contains(plotInfoDate.getPlotGroup())){
                        openNewPlot = true;
                        currentPlot = PlotInfo.generatorId(activityId, plotInfoDate.getPlotGroup(), 1);
                        entryNewPlotSet.add(plotInfoDate.getPlotGroup());
                        plotActivityBizObject.setEntryNewPlot(entryNewPlotSet);
                        clazzActivityRecord.setBizObject(plotActivityBizObject);
                        clazzActivityRecordPersistence.upsert(clazzActivityRecord);
                    }
                }
            }
        } else {
            return MapMessage.errorMessage("未初始化活动属性").add("code", "notInitActivityData").add("openNewPlot", false).add(DATA, currentPlot);
        }
        return MapMessage.successMessage("成功").add("code", "success").add("openNewPlot",openNewPlot).add("firstEntry",firstEntry).add(DATA, currentPlot);
    }

    @Override
    public MapMessage getPlotInfoListById(Integer activityId, Long schoolId, Long clazzId, Long userId, String plotInfoId) {
        List<PlotInfoBo> plotInfoBoList = new ArrayList<>();
        Boolean userUnlock = true;
        Boolean selected;
        Map<String,Object> map = new HashMap<>();
        ClazzActivity clazzActivity = clazzActivityPersistence.load(activityId);
        if(clazzActivity==null||clazzActivity.getType()!=3)
            return MapMessage.errorMessage("活动不匹配").add("code", "activityNotMatch").add(VIP_USER, true).add("userUnlock", true).add("selected",false).add(DATA, plotInfoBoList);
        ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activityId, schoolId, clazzId, userId));
        if (clazzActivityRecord == null)
            return MapMessage.errorMessage("未创建活动数据").add("code", "notCreateActivityData").add(VIP_USER, true).add("userUnlock", true).add("selected",false).add(DATA, plotInfoBoList);
        PlotInfo plotInfo = plotInfoPersistence.load(plotInfoId);
        if (plotInfo == null)
            return MapMessage.errorMessage("找不到对应的剧情").add("code", "plotInfoNotFound").add(VIP_USER, true).add("userUnlock", true).add("selected",false).add(DATA, plotInfoBoList);
        Object object = clazzActivityRecord.getBizObject();
        PlotActivityBizObject plotActivityBizObject = JsonUtils.fromJson(JsonUtils.toJson(object), PlotActivityBizObject.class);
        if (plotActivityBizObject == null)
            return MapMessage.errorMessage("未初始化活动属性").add("code", "notInitActivityData").add(VIP_USER, true).add("userUnlock", true).add("selected",false).add(DATA, plotInfoBoList);
        PlotInfoDate plotInfoDate = plotInfoDatePersistence.load(PlotInfoDate.generatorId(activityId,plotInfo.getPlotGroup()));
        if(plotInfoDate!=null){
            long currentTime = System.currentTimeMillis();
            userUnlock = currentTime > plotInfoDate.getOpenDate().getTime();
            map.put("openDate",plotInfoDate.getOpenDate());
            map.put("lastPlotId",plotInfoDate.getPlotGroup()>1?PlotInfo.generatorId(activityId,plotInfoDate.getPlotGroup()-1,1):null);
        }
        PlotInfoDate nextPlotGroupDate = plotInfoDatePersistence.load(PlotInfoDate.generatorId(activityId,plotInfo.getPlotGroup()+1));
        if(nextPlotGroupDate!=null){
            map.put("nextPlotGroupDate",nextPlotGroupDate.getOpenDate());
        }
        map.put("hasPopup",false);
        selected = hasSelectPlotPopup(activityId, userId, plotInfo.getPlotGroup());
        AtomicInteger order = new AtomicInteger(1);
        List<PlotInfo> plotInfoList = plotInfoPersistence.getPlotInfoListById(PlotInfo.db_regex(activityId, plotInfo.getPlotGroup()))
                .stream().filter(Objects::nonNull)
                .peek(p -> {
                    map.put("nextPlotId",p.getNextPlotId());
                    if(p.getBgm()!=null) map.put("bgm",p.getBgm());
                    if(p.getUnlockCoverImg()!=null) map.put("unlockCoverImg",p.getUnlockCoverImg());
                    if(p.getPlotGroup()!=null) map.put("plotGroup",p.getPlotGroup());
                    if(p.getTitle()!=null) map.put("title",p.getTitle());
                    List<PlotInfoBo> plotInfoBoList1=p.getDialog().stream().filter(Objects::nonNull).map(dialog -> {
                        PlotInfoBo plotInfoBo = new PlotInfoBo();
                        plotInfoBo.setPlotInfoId(p.getId());
                        plotInfoBo.setOrder(order.get());
                        plotInfoBo.setPlotNum(p.getPlotNum());
                        order.incrementAndGet();
                        plotInfoBo.setNeedUnlock(p.getNeedUnlock());
                        plotInfoBo.setPopup(dialog.getPopup());
                        plotInfoBo.setNpcAudio(dialog.getAudio());
                        if (dialog.getPopup() != null) map.put("hasPopup",true);
                        return plotInfoBo;
                    }).collect(Collectors.toList());
                    plotInfoBoList1.stream().findFirst().ifPresent(plotInfoBo -> {
                        plotInfoBo.setText(p.getText());
                        plotInfoBo.setAudio(p.getAudioUrl());
                    });
                    plotInfoBoList.addAll(plotInfoBoList1);
                }).collect(Collectors.toList());
        plotInfoList.stream().findFirst().ifPresent(p-> map.put("id",p.getId()));
        plotInfoBoList.sort(Comparator.comparing(PlotInfoBo::getPlotNum));
        return MapMessage.of(map).setSuccess(true).add("code", "success").add("userUnlock",userUnlock).add("selected",selected).add(VIP_USER, true).add(DATA, plotInfoBoList);
    }

    @Override
    public MapMessage updateUserCurrentPlot(Integer activityId, Long schoolId, Long clazzId, Long userId, String plotInfoId) {
        ClazzActivity clazzActivity = clazzActivityPersistence.load(activityId);
        if(clazzActivity==null||clazzActivity.getType()!=3)
            return MapMessage.errorMessage("活动不匹配").add("code", "activityNotMatch").add("plotInfoId", plotInfoId);
        ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activityId, schoolId, clazzId, userId));
        if (clazzActivityRecord == null)
            return MapMessage.errorMessage("未创建活动数据").add("code", "notCreateActivityData").add("plotInfoId", plotInfoId);
        PlotInfo plotInfo = plotInfoPersistence.load(plotInfoId);
        if (plotInfo == null)
            return MapMessage.errorMessage("找不到对应的剧情").add("code", "plotInfoNotFound").add("plotInfoId", plotInfoId);
        Object object = clazzActivityRecord.getBizObject();
        PlotActivityBizObject plotActivityBizObject = JsonUtils.fromJson(JsonUtils.toJson(object), PlotActivityBizObject.class);
        if (plotActivityBizObject != null) {
            plotActivityBizObject.setCurrentPlot(plotInfoId);
            plotActivityBizObject.setFirstEntry(false);
            clazzActivityRecord.setBizObject(plotActivityBizObject);
            ClazzActivityRecord result = clazzActivityRecordPersistence.upsert(clazzActivityRecord);
            if (result != null)
                return MapMessage.successMessage("成功").add("code", "success").add("plotInfoId", plotInfoId);
            return MapMessage.errorMessage("系统繁忙,数据更新失败").add("code", "systemBusy").add("plotInfoId", plotInfoId);
        } else
            return MapMessage.errorMessage("未初始化活动数据").add("code", "notInitActivityData").add("plotInfoId", plotInfoId);
    }

    @Override
    public void plotInfoInit(Integer activityId) {
        plotInfoInit.init(activityId);
    }

    @Override
    public MapMessage updateClazzActivityRecordFirstBuy(Integer activityId, Long schoolId, Long clazzId, Long userId) {
        ClazzActivityRecord clazzActivityRecord = clazzActivityRecordPersistence.load(ClazzActivityRecord.generateId(activityId, schoolId, clazzId, userId));
        if (clazzActivityRecord == null) return MapMessage.errorMessage("未创建活动数据").add("code", "notCreateActivityData");
        Object object = clazzActivityRecord.getBizObject();
        PlotActivityBizObject plotActivityBizObject = JsonUtils.fromJson(JsonUtils.toJson(object), PlotActivityBizObject.class);
        if (plotActivityBizObject != null) {
            if (!plotActivityBizObject.getVip()) return MapMessage.errorMessage("还不是会员").add("code", "notVip");
            if (plotActivityBizObject.getFirstBuy() != null && plotActivityBizObject.getFirstBuy() != 0)
                return MapMessage.errorMessage("已经更新过了").add("code", "hadUpdated");
            plotActivityBizObject.setFirstBuy(1);
            clazzActivityRecord.setBizObject(plotActivityBizObject);
            ClazzActivityRecord result = clazzActivityRecordPersistence.upsert(clazzActivityRecord);
            if (result != null) return MapMessage.successMessage("成功").add("code", "success");
            return MapMessage.errorMessage("系统繁忙,数据更新失败").add("code", "systemBusy");
        } else return MapMessage.errorMessage("未初始化活动数据").add("code", "notInitActivityData");
    }

    @Override
    public Map getPlotInfoDateList(Integer activityId){
        Map<Integer,Map<String,String>> map = new HashMap<>();
        plotInfoDatePersistence.getPlotInfoDateList(activityId).forEach(plotInfoDate -> {
            Map<String,String> map1 = new HashMap<>();
            map1.put("openDate",DateUtils.dateToString(plotInfoDate.getOpenDate()));
            map1.put("endDate",DateUtils.dateToString(plotInfoDate.getEndDate()));
            map.put(plotInfoDate.getPlotGroup(),map1);
        });
        return map;
    }

    @Override
    public PlotInfoDate upsertPlotInfoDate(Integer activityId, Integer plotGroup, String date, String endDate) {
        PlotInfoDate plotInfoDate = new PlotInfoDate();
        plotInfoDate.setId(PlotInfoDate.generatorId(activityId, plotGroup));
        plotInfoDate.setOpenDate(DateUtils.stringToDate(date, DateUtils.FORMAT_SQL_DATETIME));
        plotInfoDate.setEndDate(DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATETIME));
        return plotInfoDatePersistence.upsert(plotInfoDate);
    }

    @Override
    public Integer getPlotGroupDateId(Integer activityId, Long time) {
        List<PlotInfoDate> plotInfoDateList = plotInfoDatePersistence.getPlotInfoDateList(activityId);
        String plotGroupDateId = null;
        try{
            for (PlotInfoDate plotInfoDate : plotInfoDateList) {
                if (time >= plotInfoDate.getOpenDate().getTime()) plotGroupDateId = plotInfoDate.getId();
                if (time >= plotInfoDate.getOpenDate().getTime() && time < plotInfoDate.getEndDate().getTime()) {
                    return Integer.valueOf(plotInfoDate.getId().split("_")[1]);
                }
            }
            return plotGroupDateId!=null?Integer.valueOf(plotGroupDateId.split("_")[1]):null;
        }catch (Exception ex){
            return null;
        }
    }

    @Override
    public MapMessage selectPlotActivityPopup(Integer activityId, Long userId, Integer plotGroup, Integer common) {
        if (activityId == null || plotGroup == null || common == null || common > 1 || common < 0)
            return MapMessage.errorMessage("传参错误").add("code", "paramError");
        ClazzActivity clazzActivity = clazzActivityPersistence.load(activityId);
        if(clazzActivity==null||clazzActivity.getType()!=3)
            return MapMessage.errorMessage("活动不匹配").add("code", "activityNotMatch");
        if (hasSelectPlotPopup(activityId, userId, plotGroup))
            return MapMessage.errorMessage("已经选择了哦").add("code", "selected");
        PlotActivitySelectRecord plotActivitySelectRecord = new PlotActivitySelectRecord();
        plotActivitySelectRecord.setId(PlotActivitySelectRecord.generatorId(activityId, userId, plotGroup));
        plotActivitySelectRecord.setCommon(common);
        plotActivitySelectRecord.setCondition(false);
        PlotActivitySelectRecord result = plotActivitySelectRecordPersistence.upsert(plotActivitySelectRecord);
        if (result == null) return MapMessage.errorMessage("系统繁忙,数据更新失败").add("code", "systemBusy");
        String key = ClazzPlotActivitySelectCache.generatorCacheKey(activityId, plotGroup, common);
        clazzActivityCacheManager.increasePlotSelectResult(key);
        Map<String, Object> map = new HashMap<>();
        getPlotActivityPopupSelectResultMap(activityId, plotGroup, map);
        return MapMessage.of(map).setSuccess(true).setInfo("成功").add("code", "success");
    }

    @Override
    public Boolean hasSelectPlotPopup(Integer activityId, Long userId, Integer plotGroup) {
        String id = PlotActivitySelectRecord.generatorId(activityId, userId, plotGroup);
        return plotActivitySelectRecordPersistence.exists(id);
    }

    @Override
    public MapMessage getPlotActivityPopupSelectResult(Integer activityId, Integer plotGroup) {
        Map<String, Object> map = new HashMap<>();
        map.put("mayPercent", BASE_POPUP_USER_MAY/(BASE_POPUP_USER_MAY+BASE_POPUP_USER_IMPOSSIBLE));
        map.put("m_num",0);
        map.put("i_num",0);
        map.put("totalCount", BASE_POPUP_USER_MAY+BASE_POPUP_USER_IMPOSSIBLE);
        if (activityId == null || plotGroup == null)
            return MapMessage.of(map).setSuccess(false).setInfo("参数错误").add("code", "paramError").add("plotGroup", plotGroup);
        ClazzActivity clazzActivity = clazzActivityPersistence.load(activityId);
        if(clazzActivity==null||clazzActivity.getType()!=3)
            return MapMessage.of(map).setSuccess(false).setInfo("活动不匹配").add("code", "activityNotMatch").add("plotGroup", plotGroup);
        String text = null;
        List<PlotInfo> plotInfoList = plotInfoPersistence.getPlotInfoListById(PlotInfo.db_regex(activityId, plotGroup));
        for(PlotInfo plotInfo : plotInfoList){
            if(plotInfo.getDialog()==null) continue;
            for(Dialog dialog : plotInfo.getDialog()){
                if(dialog.getPopup()==null) continue;
                text = dialog.getPopup().getText();
            }
        }
        if(text==null) return MapMessage.of(map).setSuccess(false).setInfo("这个剧情没有选择弹框儿").add("code", "notFoundPopup").add("plotGroup", plotGroup);
        getPlotActivityPopupSelectResultMap(activityId, plotGroup, map);
        return MapMessage.of(map).setSuccess(true).setInfo("成功").add("code", "success").add("plotGroup", plotGroup).add("text",text);
    }

    private void getPlotActivityPopupSelectResultMap(Integer activityId, Integer plotGroup, Map<String, Object> resultMap) {
        String mayKey = ClazzPlotActivitySelectCache.generatorCacheKey(activityId, plotGroup, 1);
        Long mayCount = clazzActivityCacheManager.loadPlotSelectResult(mayKey);
        String impossibleKey = ClazzPlotActivitySelectCache.generatorCacheKey(activityId, plotGroup, 0);
        Long impossibleCount = clazzActivityCacheManager.loadPlotSelectResult(impossibleKey);
        double totalCount = mayCount+impossibleCount+BASE_POPUP_USER_MAY+BASE_POPUP_USER_IMPOSSIBLE;
        double mayPercent= (mayCount+BASE_POPUP_USER_MAY)/totalCount;
        mayPercent = (double) Math.round(mayPercent*100)/100;
        resultMap.put("mayPercent",mayPercent);
        resultMap.put("m_num",mayCount);
        resultMap.put("i_num",impossibleCount);
        resultMap.put("totalCount",totalCount);
    }

    @Override
    public MapMessage getPlotPopupSelectAward(Integer activityId, Long userId) {
        List<Integer> plotGroups = new ArrayList<>();
        if (activityId == null || userId == null)
            return MapMessage.errorMessage("参数错误").add("data", plotGroups);
        ClazzActivity clazzActivity = clazzActivityPersistence.load(activityId);
        if(clazzActivity==null||clazzActivity.getType()!=3)
            return MapMessage.errorMessage("活动不匹配").add("data", plotGroups);
        Long time = System.currentTimeMillis();
        Integer plotGroup= getPlotGroupDateId(activityId, time);
        if (plotGroup == null) return MapMessage.errorMessage("还没有奖励").add("data", plotGroups);
        plotActivitySelectRecordPersistence.getPlotActivitySelectRecordList(activityId, userId)
                .stream()
                .filter(item -> item.getPlotGroup() < plotGroup && !item.getCondition())
                .forEach(item -> {
                    //领取奖励
                    item.setCondition(true);
                    Boolean selfContribute = classCirclePlotService.addStudentContribution(activityId, userId, CONTRIBUTE);
                    classCirclePlotService.addClazzContribution(activityId, userId, CONTRIBUTE);
                    if (selfContribute){
                        plotActivitySelectRecordPersistence.upsert(item);
                        plotGroups.add(item.getPlotGroup());
                    }
                });
        int size = plotGroups.size();
        if(size>0) return MapMessage.successMessage("成功").add("data",plotGroups).add("contribute", CONTRIBUTE*size);
        return MapMessage.errorMessage("还没有奖励").add("data",plotGroups).add("contribute", CONTRIBUTE*size);
    }

}
