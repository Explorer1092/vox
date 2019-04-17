package com.voxlearning.utopia.service.dubbing.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by jiang wei on 2017/10/12.
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "dubbing_raw")
public class DubbingRaw implements Serializable {

    private static final long serialVersionUID = -5025072865943883367L;

    @DocumentId
    private String id;
    @DocumentField("subject_id")
    private Integer subjectId;                          // 学科
    @DocumentField("category_id")
    private String categoryId;
    private Integer rank;

    @DocumentField("video_name")
    private String videoName;                           // 视频名称
    @DocumentField("video_summary")
    private String videoSummary;                        // 视频简介
    @DocumentField("cover_url")
    private String coverUrl;
    @DocumentField("cover_thumbnail_url")
    private String coverThumbnailUrl;
    @DocumentField("video_url")
    private String videoUrl;
    @DocumentField("video_seconds")
    private Integer videoSeconds;                       // 视频时长,单位秒
    @DocumentField("background_music")
    private String backgroundMusic;                     // 配音背景音
    private Integer difficult;
    @DocumentField("video_from")
    private String videoFrom;                           // 来源
    @DocumentField("copyright_owner")
    private String copyrightOwner;                      // 版权者
    private List<DubbingSentence> sentences;
    @DocumentField("is_sync")
    private Boolean isSync;
    @DocumentField("finish_sync")
    private Boolean finishSync;

    private Integer version;
    @DocumentField("doc_id")
    private String docId;
    private Map<String, Object> extras;
    @DocumentCreateTimestamp
    private Date createdAt;
    @DocumentUpdateTimestamp
    private Date updatedAt;
    @DocumentField("deleted_at")
    private Date deletedAt;
    @DocumentField("srt_url")
    private String srtUrl;

    @JsonIgnore
    public boolean isDeleted() {
        return deletedAt != null;
    }


    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    public static class DubbingSentence implements Serializable {
        private static final long serialVersionUID = -1749871300824252783L;
        private Integer rank;
        @DocumentField("chinese_text")
        private String chineseText;
        @DocumentField("english_text")
        private String englishText;
        @DocumentField("voice_start")
        private String voiceStart;
        @DocumentField("voice_end")
        private String voiceEnd;
    }
}
