package com.voxlearning.utopia.service.zone.api.plot;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotInfo;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotInfoDate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181109")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface PlotActivityService {

    PlotInfo savePlotInfo(Integer activityId, Integer plotGroup, Integer plotNum,
                          String title, String text, String audioUrl,
                          String dialogString, Boolean needUnlock, Integer lastPlotGroup, Integer lastPlotNum,
                          Integer nextPlotGroup, Integer nextPlotNum);

    PlotInfo updatePlotInfo(String id, String unlockCoverImg, String bgm);

    List<PlotInfo> queryPlotInfoList(Integer activityId);

    MapMessage getPlotInfoGroupList(Integer activityId, Long schoolId, Long clazzId, Long userId);

    MapMessage getUserCurrentPlotInfo(Integer activityId, Long schoolId, Long clazzId, Long userId);

    MapMessage getPlotInfoListById(Integer activityId, Long schoolId, Long clazzId, Long userId, String plotInfoId);

    MapMessage updateUserCurrentPlot(Integer activityId, Long schoolId, Long clazzId, Long userId, String plotInfoId);

    void plotInfoInit(Integer activityId);

    MapMessage updateClazzActivityRecordFirstBuy(Integer activityId, Long schoolId, Long clazzId, Long userId);

    Map getPlotInfoDateList(Integer activityId);

    PlotInfoDate upsertPlotInfoDate(Integer activityId, Integer plotGroup, String date, String endDate);

    Integer getPlotGroupDateId(Integer activityId, Long time);

    MapMessage selectPlotActivityPopup(Integer activityId, Long userId, Integer plotGroup, Integer common);

    Boolean hasSelectPlotPopup(Integer activityId, Long userId, Integer plotGroup);

    MapMessage getPlotActivityPopupSelectResult(Integer activityId, Integer plotGroup);

    MapMessage getPlotPopupSelectAward(Integer activityId, Long userId);
}
