package com.voxlearning.utopia.service.business.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.common.FieldValueSerializer;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.business.api.constant.TeachingResourceUserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 教学资源
 * Created by haitian.gan 2017/8/1.
 */
@Setter
@Getter
@DocumentDatabase(database = "vox_o2o")
@DocumentCollection(collection = "vox_teaching_resource")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180911")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class TeachingResource implements Serializable, Cloneable {

    private static final long serialVersionUID = 7993281011107703887L;
    public static final Integer SOURCE_PLATFORM = 0;  // 普通平台版
    public static final Integer SOURCE_JIANGXI = 1;   // 江西版

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID)
    private String id;
    @DocumentField("name") private String name;                         // 名称
    @DocumentField("desc") private String desc;                         // 资源简介
    @DocumentField("subject") private String subject;                   // 学科
    @DocumentField("category") private String category;                 // 资源分类
    @DocumentField("label") private Label label;                        // 资源标签
    @DocumentField("workType") private WorkType workType;               // 布置作业类型
    @DocumentField("task") private String task;                         // 关联任务
    @DocumentField("grade") private String grade;                       // 年级
    @DocumentField("image") private String image;                       // 题图
    @DocumentField("appImage") private String appImage;                 // 老师app首页图
    @DocumentField("featuring") private Boolean featuring;              // 首页推荐
    @DocumentField("displayOrder") private Integer displayOrder;        // 排序
    @DocumentField("online") private Boolean online;                    // 上线状态
    @DocumentField("validityPeriod")private Integer validityPeriod;     // 任务有效期
    @DocumentField("fileUrl")private String fileUrl;                    // 文件URL
    @DocumentField("subHead")private String subHead;                    // 副标题
    @DocumentField("visitLimited")private TeachingResourceUserType visitLimited;          //访问限制
    @DocumentField("receiveLimited")private TeachingResourceUserType receiveLimited;      //领取限制

    @DocumentField("readCount") private Long readCount;            // 阅读次数
    @DocumentField("collectCount") private Long collectCount;      // 收藏次数
    @DocumentField("participateNum") private Long participateNum;  // 每周活动的参与人数
    @DocumentField("finishNum") private Long finishNum;            // 每周活动的完成人数

    @DocumentField("source") private Integer source;            // 归属来源 0 教学助手 1 江西教学助手

    @DocumentCreateTimestamp private Date createAt;
    @DocumentUpdateTimestamp private Date updateAt;

    @DocumentField("onlineAt")
    @FieldValueSerializer(serializer = "com.voxlearning.alps.lang.mapper.json.StringDateSerializer")
    private Date onlineAt;                                              // 上线时间

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TeachingResource.class, id);
    }

    public static String ck_all(){
        return CacheKeyGenerator.generateCacheKey(TeachingResource.class,"ALL");
    }


    public String getOnlineDate() {
        if (this.onlineAt == null) {
            return "";
        }
        return DateFormatUtils.format(this.onlineAt, "yyyy-MM-dd");
    }

    // 学科
    @Getter
    @AllArgsConstructor
    public enum Subject{
        MATH("数学"),
        CHINESE("语文"),
        ENGLISH("英语");

        private String desc;

        public static Subject parse(String name){
            try {
                return Subject.valueOf(name);
            }catch (Exception e){
                return Subject.MATH;
            }
        }
    }

    // 资源分类
    @Getter
    @AllArgsConstructor
    public enum Category {
        WEEK_WELFARE("每周福利"),
        TEACHING_SPECIAL("教学专题"),
        YIQI_JIANGTANG("一起新讲堂"),
        SYNC_COURSEWARE("同步课件"),

        NEW_COURSEWARE("新课件"),

        IMPORTANT_CASE("关键课例"),
        GROW_UP("成长心语"),
        ACTIVITY_NOTICE("活动公告"),
        OTHER_STONE("他山之石"),

        COURSE_WARE("废弃_优质课件"),
        OUTSIDE_READING("废弃_课外拓展"),
        TEST_PAPER("废弃_精品试题"),
        LECTURE("废弃_教研师训");


        private String desc;

        public static Category parse(String name) {
            try {
                return Category.valueOf(name);
            } catch (Exception e) {
                return Category.COURSE_WARE;
            }
        }
    }

    // 标签
    @Getter
    @AllArgsConstructor
    public enum Label {
        // 限时 / 热门 / 资源 / 活动 / 课件 / 专题 / 充电 / 专家 / 绘本 / 自然拼读 / 口算 / 节日
        限时("限时"),
        热门("热门"),
        资源("资源"),
        活动("活动"),
        课件("课件"),
        专题("专题"),
        充电("充电"),
        专家("专家"),
        绘本("绘本"),
        自然拼读("自然拼读"),
        口算("口算"),
        节日("节日"),

        锦囊("锦囊"),
        节气("节气"),
        传统文化("传统文化"),
        听力("听力 "),
        口语("口语 "),
        阅读("阅读"),
        写作("写作"),
        复习("复习 "),
        学生管理("学生管理"),
        ;

        private String desc;

        public static Label parse(String name) {
            try {
                return Label.valueOf(name);
            } catch (Exception e) {
                return null;
            }
        }
    }

    // (布置作业，布置趣配音，布置自然拼读作业，布置绘本)
    @Getter
    @AllArgsConstructor
    public enum WorkType {

        无("不布置作业"),

        布置作业("布置作业"),
        布置趣配音("布置趣配音"),
        布置自然拼读("布置自然拼读"),
        布置绘本("布置绘本"),

        布置假期作业("布置假期作业"),
        布置期末复习("布置期末复习"),

        @Deprecated
        推荐练习("推荐练习"),
        @Deprecated
        推荐趣配音("推荐趣配音"),
        @Deprecated
        推荐自然拼读("推荐自然拼读"),
        @Deprecated
        推荐绘本("推荐绘本"),
        @Deprecated
        推荐口语交际("推荐口语交际"),

        ;

        private String desc;

        public static WorkType parse(String name) {
            try {
                return WorkType.valueOf(name);
            } catch (Exception e) {
                return WorkType.推荐练习;
            }
        }
    }
    @Override
    public TeachingResource clone() {
        try {
            return (TeachingResource) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException();
        }
    }

}

