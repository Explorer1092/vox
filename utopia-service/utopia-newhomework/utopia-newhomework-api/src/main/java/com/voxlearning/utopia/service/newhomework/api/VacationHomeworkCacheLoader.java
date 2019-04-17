package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkWinterPlanCacheMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xuesong.zhang
 * @since 2016/12/1
 */
@ServiceVersion(version = "20180727")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 0)
@CyclopsMonitor("utopia")
public interface VacationHomeworkCacheLoader extends IPingable {

    /**
     * 给缓存中插入一条作业
     * 有就覆盖，没有就新增
     *
     * @param vacationHomework 假期作业
     * @return VacationHomeworkCacheMapper
     */
    VacationHomeworkCacheMapper addOrModifyVacationHomeworkCacheMapper(VacationHomework vacationHomework);

    /**
     * 移除缓存
     *
     * @param clazzGroupId 班组id
     * @param studentId    学生id
     */
    void removeVacationHomeworkCacheMapper(Long clazzGroupId, Long studentId);

    /**
     * 移除缓存
     *
     * @param clazzGroupId 班组id
     */
    void removeVacationHomeworkCacheMapper(Long clazzGroupId);

    /**
     * 从缓存中获取信息
     *
     * @param clazzGroupId 班组id
     * @param studentId    学生id
     * @return VacationHomeworkCacheMapper
     */
    VacationHomeworkCacheMapper loadVacationHomeworkCacheMapper(Long clazzGroupId, Long studentId);

    /**
     * 组下面所有做过作业的学生
     *
     * @param clazzGroupId 组id
     * @return List
     */
    List<VacationHomeworkCacheMapper> loadVacationHomeworkCacheMappers(Long clazzGroupId);

    /**
     * 假期作业课本计划内容
     *
     * @param bookId
     * @return
     */
    VacationHomeworkWinterPlanCacheMapper loadVacationHomeworkWinterPlanCacheMapper(String bookId);


}
