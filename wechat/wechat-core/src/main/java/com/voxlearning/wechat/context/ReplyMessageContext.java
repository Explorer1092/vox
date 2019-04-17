package com.voxlearning.wechat.context;

import com.voxlearning.wechat.support.Articles;
import lombok.Getter;
import lombok.Setter;

/**
 * 被动回复消息context
 * @author Xin Xin
 * @since 10/19/15
 */

public class ReplyMessageContext extends MessageContext {
    @Getter
    @Setter
    private String image_MediaId;
    @Getter
    @Setter
    private String voice_Media_id;
    @Getter
    @Setter
    private String video_MediaId;
    @Getter
    @Setter
    private String video_Title;
    @Getter
    @Setter
    private String video_Description;
    @Getter
    @Setter
    private String music_Title;
    @Getter
    @Setter
    private String music_Description;
    @Getter
    @Setter
    private String music_MusicUrl;
    @Getter
    @Setter
    private String music_HQMusicUrl;
    @Getter
    @Setter
    private String music_ThumbMediaId;
    @Getter
    @Setter
    private Articles articles;
    @Getter
    @Setter
    private Integer articleCount;
}
