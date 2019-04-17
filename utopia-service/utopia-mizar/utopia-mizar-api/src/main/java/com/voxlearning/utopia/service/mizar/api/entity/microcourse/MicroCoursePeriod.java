package com.voxlearning.utopia.service.mizar.api.entity.microcourse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.mizar.api.utils.DateParseHelper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 微课堂-课时实体
 * Created by Wang Yuechen on 2016/12/08.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentDatabase(database = "vox_mizar")
@DocumentCollection(collection = "micro_course_period")
@UtopiaCacheRevision("20170309")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
public class MicroCoursePeriod implements CacheDimensionDocument {

    private static final long serialVersionUID = 1L;

    public static final String BTN_BEFORE_PURCHASE = "BP";  // 更改前按钮文字Key
    public static final String BTN_AFTER_PURCHASE = "AP";   // 更改后按钮文字Key

    @DocumentId private String id;
    private String theme;       // 课时主题
    private String info;        // 课时介绍
    private Date startTime;     // 上课时间
    private Date endTime;       // 下课时间
    private Double price;       // 原价
    private Double discount;    // 折扣价
    private List<String> photo; // 详情图片
    private String url;         // 课时视频
    private Map<String, String> btnContent;    // 按钮文字
    private Boolean smsNotify;  // 是否需要短信提醒
    private String liveUrl;     // 直播地址
    private String replayUrl;   // 回放地址
    private String tip;         // 备注提示文字
    private String tkCourse;    // 自助欢拓课程ID
    private String spreadText;  // 推广文字
    private String spreadUrl;   // 推广链接
    private Map<String, Object> extInfo;      // 课时状态，目前是通过欢拓反馈进行变更
    private String longClassUrl; // 配套长期班URL
    private List<String> longClassPhoto; // 配套长期班图片

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;
    private Boolean disabled;   // 删除状态

    @JsonIgnore
    public boolean isDisabledTrue() {
        return Boolean.TRUE.equals(disabled);
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }

    public boolean emptyBtnContent() {
        return btnContent == null
                || StringUtils.isBlank(btnContent.get(BTN_BEFORE_PURCHASE))
                || StringUtils.isBlank(btnContent.get(BTN_AFTER_PURCHASE));
    }

    public String fetchPeriodTime() {
        if (startTime == null || endTime == null) {
            return "时间未定";
        }
        return StringUtils.formatMessage("{} ~ {}（时长：{}）",
                DateUtils.dateToString(startTime, "yyyy-MM-dd HH:mm"),
                DateUtils.dateToString(endTime, "HH:mm"),
                DateParseHelper.calDuration(startTime, endTime, "时间未定")
        );
    }

    /**
     * 根据课时的时间排序, 开始时间早的排前面
     */
    public static int sortByTime(MicroCoursePeriod p1, MicroCoursePeriod p2) {
        if (p1 == null || p2 == null) return 0;
        if (p1.getStartTime() == null || p2.getStartTime() == null) return 0;
        int cmp = p1.getStartTime().compareTo(p2.getStartTime());
        if (cmp != 0) return cmp;
        if (p1.getEndTime() == null || p2.getEndTime() == null) return 0;
        return p1.getEndTime().compareTo(p2.endTime);
    }

}
