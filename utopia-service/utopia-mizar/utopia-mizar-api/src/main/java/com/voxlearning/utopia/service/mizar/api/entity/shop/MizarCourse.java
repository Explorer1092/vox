package com.voxlearning.utopia.service.mizar.api.entity.shop;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Summer Yang on 2016/9/20.
 * 去上课 中间页 课程实体
 */
@Setter
@Getter
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "vox_course")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20170101")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MizarCourse implements Serializable {

    private static final long serialVersionUID = -840408456086144705L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID) private String id;
    private String title;             // 主标题， 对应到微课堂【课程/课时ID】
    private String subTitle;          // 副标题， 对应到微课堂【标签】
    private String background;        // 背景图片，对应到微课堂【颜色】
    private String description;       // 课程简介，对应到微课堂【描述】
    private String redirectUrl;       // 跳转地址
    private Boolean top;              // 置顶
    private Status status;            // 状态，对应到微课堂【状态】
    private String category;          // 类别，MizarCourseCategory，对应到微课堂【类别】
    private String keynoteSpeaker;    // 主讲人姓名
    private List<String> tags;        // 标签
    private List<String> clazzLevels; // 适配年级，对应到微课堂【适配年级】
    private Boolean indexShow;        // 显示到首页
    private Long activityId;          // 通用支付/预约的活动ID
    private Integer priority;         // 显示优先级，对应到微课堂【优先级】
    private String speakerAvatar;     // 主讲人头像，对应到微课堂【图片】
    private String price;             // 课程价格
    private String classTime;         // 上课时间
    private Integer type;             // 对应到微课堂【类型，1-课程，2-课时】

    @DocumentCreateTimestamp private Date createAt;
    @DocumentUpdateTimestamp private Date updateAt;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(MizarCourse.class, id);
    }

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(MizarCourse.class, "All");
    }


    public enum Status {
        ONLINE, OFFLINE
    }

    public static int sortCourse(MizarCourse c1, MizarCourse c2) {
        if (c1 == null || c2 == null) return 0;
        Boolean s1 = c1.getStatus() == MizarCourse.Status.ONLINE;
        Boolean s2 = c2.getStatus() == MizarCourse.Status.ONLINE;
        if (!s2.equals(s1)) {
            return s2.compareTo(s1);
        }
        Boolean b1 = SafeConverter.toBoolean(c1.getTop());
        Boolean b2 = SafeConverter.toBoolean(c2.getTop());
        if (!b2.equals(b1)) {
            return b2.compareTo(b1);
        }
        Integer p1 = c1.getPriority() == null ? 0 : c1.getPriority();
        Integer p2 = c2.getPriority() == null ? 0 : c2.getPriority();
        if (!p1.equals(p2)) {
            return p2.compareTo(p1);
        }
        Date d1 = c1.getCreateAt();
        Date d2 = c2.getCreateAt();
        return d2.compareTo(d1);
    }

}
