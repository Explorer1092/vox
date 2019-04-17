package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.context.AncientPoetryProcessContext;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryActivity;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMission;

import java.util.concurrent.TimeUnit;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/2/20
 */
@ServiceVersion(version = "20190228")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface AncientPoetryService {

    /**
     * 报名亲子古诗活动
     */
    MapMessage registerPoetryActivity(Long teacherId, String activityId, Long clazzGroupId);

    /**
     * 提交作答结果
     */
    MapMessage processResult(AncientPoetryProcessContext context);

    /**
     * 老师参加亲子古诗活动(点击立即参与)
     */
    MapMessage viewActivity(Long teacherId);

    /**
     * 更新活动状态
     */
    MapMessage updateActivityStatus(String activityId, boolean status);

    /**
     * CRM
     * 保存活动信息
     */
    MapMessage upsertAncientPoetryActivity(AncientPoetryActivity ancientPoetryActivity);

    /**
     * CRM
     * 保存古诗信息
     */
    MapMessage upsertAncientPoetryMission(AncientPoetryMission ancientPoetryMission);

    /**
     * 导入古诗数据, 内部用
     */
    MapMessage insertsAncientPoetryMission(String jsonStr);

    /**
     * 生成指定学校、指定年级的排行榜
     */
    void generateGlobalRankBySchoolIdAndClazzLevel(Long schoolId, Integer clazzLevel);

    /**
     * 生成指定区、指定年级的排行榜
     */
    void generateGlobalRankByRegionIdAndClazzLevel(Integer regionId, Integer clazzLevel);
}
