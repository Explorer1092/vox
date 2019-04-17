package com.voxlearning.utopia.service.campaign.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 2017 老师抽奖活动实体
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_TEACHER_VOCATION_LOTTERY")
@UtopiaCacheRevision("20171211")
public class TeacherVocationLottery extends AbstractDatabaseEntity{

    private static final long serialVersionUID = -8860480954689176235L;

    @UtopiaSqlColumn private Long teacherId;
    @UtopiaSqlColumn private Integer fluidTime;     // 浮动的抽奖次数
    @UtopiaSqlColumn private Integer fixedTime;     // 固定的抽奖次数
    @UtopiaSqlColumn private Integer assignTime;    // 假期作业的布置次数
    @UtopiaSqlColumn private Integer drawTime;      // 抽奖次数
    @UtopiaSqlColumn private Date activeTime;       // 活跃时间

    public static String ck_teacher_id(Long teacherId){
        return CacheKeyGenerator.generateCacheKey(TeacherVocationLottery.class,"TEACHER_ID",teacherId);
    }

    public void addFixedTime(int time){
        if(this.fixedTime == null)
            this.fixedTime = 0;

        this.fixedTime+= time;
    }

    public void addAssignTime(int time){
        if(this.assignTime == null)
            this.assignTime = 0;

        this.assignTime+= time;
    }

    public int calTotalTime(){
        int fluidT = fluidTime == null ? 0 : fluidTime;
        int fixedT = fixedTime == null ? 0 : fixedTime;
        return fluidT + fixedT;
    }

    /**
     * 判断是否为没有任何抽奖机会的记录
     * @return
     */
    public boolean isNoChance(){
        return this.fixedTime == 0 && this.fluidTime == 0;
    }

}
