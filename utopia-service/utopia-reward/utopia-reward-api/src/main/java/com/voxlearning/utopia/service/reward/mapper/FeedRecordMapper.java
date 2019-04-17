package com.voxlearning.utopia.service.reward.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 公益活动动态 Mapper
 */
@Getter
@Setter
public class FeedRecordMapper implements Serializable {
    private static final long serialVersionUID = -3784757236490499754L;

    private String year;
    private List<FeedRecord> day;

    @Getter
    @Setter
    public static class FeedRecord implements Serializable {
        private static final long serialVersionUID = -3784757236490499754L;

        private String date;
        List<Feed> feed;

        @Getter
        @Setter
        public static class Feed implements Serializable {
            private static final long serialVersionUID = -3784757236490499754L;

            private String type;       // 类型 点赞 留言 对应 com.voxlearning.utopia.service.reward.entity.PublicGoodFeed.Type
            private String time;       // 时间
            private String content;    // 内容
            private String comment;    // 留言
        }
    }
}
