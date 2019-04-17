package com.voxlearning.utopia.service.vendor.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author jiangpeng
 * http://wiki.17zuoye.net/pages/viewpage.action?pageId=41142603
 * @since 2018-09-21 下午6:45
 **/
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "livecast_course")
@UtopiaCacheRevision("20180920")
public class LiveCastCourse implements CacheDimensionDocument {

    private static final long serialVersionUID = 2661542697681529878L;
    @DocumentId
    private String courseId;

    private String casterAvatarUrl;

    private String casterName;

    private String courseName;

    private String detailUrl;

    private Integer grade;

    private List<Segment> lessonSegments;

    private Integer rank;


    @JsonProperty("subject")
    private Integer subjectId;

    private String tag;

    private Long subscribeNum;


    @DocumentFieldIgnore
    private Date startDate;

    @DocumentFieldIgnore
    private Date endDate;

    @JsonIgnore
    @DocumentFieldIgnore
    private Subject subject;

    /**
     * 类型（1：公开课，2：付费课）
     */
    private Integer type;

    /**
     * 价格
     */
    private String realPrice;

    /**
     * 说明
     */
    private String note;


    public boolean safeIsPublicCourse(){
        return SafeConverter.toInt(type, 1) == 1;
    }

    public boolean safeIsPayCourse(){
        return SafeConverter.toInt(type, 1) == 2;
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }

    @Data
    public static class Segment implements Serializable {
        private static final long serialVersionUID = 7122657875583840802L;
        private Date startTime;
        private Date endTime;

        private static long buf30Min = 15 *60* 1000;

        /**
         * 在上课中 前后30分钟
         * @return
         */
        public boolean in15MinPeriod(){
            Objects.requireNonNull(startTime);
            Objects.requireNonNull(endTime);
            long timeMillis = System.currentTimeMillis();
            return this.getStartTime().getTime() - buf30Min <= timeMillis && this.getEndTime().getTime() + buf30Min >= timeMillis;
        }

        /**
         *  上课前30分钟之前
         * @return
         */
        public boolean before15Min(){
            Objects.requireNonNull(startTime);
            Objects.requireNonNull(endTime);
            long timeMillis = System.currentTimeMillis();
            return timeMillis < this.getStartTime().getTime() - buf30Min;
        }

        /**
         *  下课后30分钟之后
         * @return
         */
        public boolean after30Min(){
            Objects.requireNonNull(startTime);
            Objects.requireNonNull(endTime);
            long timeMillis = System.currentTimeMillis();
            return timeMillis > this.getEndTime().getTime() + buf30Min;
        }
    }
}
