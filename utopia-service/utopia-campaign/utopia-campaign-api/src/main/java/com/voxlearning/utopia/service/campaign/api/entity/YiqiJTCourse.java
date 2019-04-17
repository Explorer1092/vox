package com.voxlearning.utopia.service.campaign.api.entity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 一起讲堂活动 - 课程Mapper
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_17JT_COURSE")
@CacheBean(type = YiqiJTCourse.class)
@UtopiaCacheRevision("20180918")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class YiqiJTCourse extends AbstractDatabaseEntity{

    private static final long serialVersionUID = -2591257583186377173L;

    @UtopiaSqlColumn(name = "TITLE") private String title;
    @UtopiaSqlColumn(name = "ACTIVE_TIME") private Integer activeTime;
    @UtopiaSqlColumn(name = "OPEN_TIME") private Date openTime;
    @UtopiaSqlColumn(name = "VIDEO_URL") private String url;
    @UtopiaSqlColumn(name = "ATTEND_NUM") private Long attendNum;//参与人数
    @UtopiaSqlColumn(name = "PRICE") private Integer price;
    @UtopiaSqlColumn(name = "TOP_NUM") private Integer topNum;//置顶
    @UtopiaSqlColumn(name = "STATUS") private Integer status;//上线下线状态
    @UtopiaSqlColumn(name = "VIDEO_PICTURE_URL") private String videoPictureUrl;
    @UtopiaSqlColumn(name = "TITLE_PICTURE_URL") private String titlePictureUrl;
    @UtopiaSqlColumn(name = "APP_PICTURE_URL") private String appPictureUrl;
    @UtopiaSqlColumn(name = "LECTURER_USER_NAME") private String lecturerUserName;//讲师
    @UtopiaSqlColumn(name = "LECTURER_INTRODUCTION") private String lecturerIntroduction;//讲师简介
    @UtopiaSqlColumn(name = "TEXT_CONTENT") private String textContent;//文本内容
    @UtopiaSqlColumn(name = "FEATURING") private Boolean featuring;              // 首页推荐
    @UtopiaSqlColumn(name = "LABEL") private String label; // 标签

    @UtopiaSqlColumn(name = "READ_COUNT") private Long readCount;            //阅读次数
    @UtopiaSqlColumn(name = "COLLECT_COUNT") private Long collectCount;      //收藏次数

    @UtopiaSqlColumn(name = "SOURCE") private Integer source;            // 归属来源 0 教学助手 1 江西教学助手
    @UtopiaSqlColumn(name = "CATEGORY") private String category;         // 资源分类

    public static String ck_all(){
        return CacheKeyGenerator.generateCacheKey(
                YiqiJTCourse.class,
                new Object[]{"All"});
    }

    // 资源分类
    @Getter
    @AllArgsConstructor
    public enum Category {
        YIQI_JIANGTANG("一起新讲堂"),

        IMPORTANT_CASE("关键课例"),
        GROW_UP("成长心语"),
        ACTIVITY_NOTICE("活动公告"),
        OTHER_STONE("他山之石");

        private String desc;

        public static Category parse(String name) {
            try {
                return Category.valueOf(name);
            } catch (Exception e) {
                return Category.YIQI_JIANGTANG;
            }
        }
    }
}
