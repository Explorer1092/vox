package com.voxlearning.utopia.service.vendor.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author jiangpeng
 * @since 2017-10-17 下午5:36
 **/

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "livecast_index_refined_lessons")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class LiveCastIndexRefinedLessons extends LiveCastTargetAndExtra {
    private static final long serialVersionUID = 4720174959238277383L;


    @JsonProperty("lessons")
    private List<LessonInfo> lessonInfoList;



    @Data
    public static class LessonInfo implements Serializable{
        private static final long serialVersionUID = 1045374778965513543L;
        @JsonProperty("cover_url")
        private String coverUrl;
        private String title;
        private String label;
        private String desc;
        private String url;
        @JsonProperty("need_login")
        private Boolean needLogin;
        @JsonProperty("dot_id")
        private String dotId;
        @JsonProperty("watch_count")
        private String watchCount;


        @Deprecated
        public StudyEntry toStudyEntry(String mainSiteSchem){
            StudyEntry studyEntry = new StudyEntry();
            studyEntry.setSelfStudyType(SelfStudyType.LIVECAST);
            studyEntry.setLabel(label);
            studyEntry.setLabelTextColor(StudyEntry.LabelColor.WHITE.getColor());
            studyEntry.setName(title);
            studyEntry.setBottomText(desc);
            studyEntry.setIconUrl(coverUrl);
            studyEntry.touchFunctionType(StudyEntry.FunctionType.H5);
            if (SafeConverter.toBoolean(needLogin)) {
                studyEntry.touchFunctionKey(mainSiteSchem + "/redirector/goaoshu.vpage?returnURL=" + url);
            } else {
                studyEntry.touchFunctionKey(url);
            }
            studyEntry.setDotId(dotId);
            studyEntry.setWatchCount(watchCount);
            return studyEntry;
        }
    }

}
