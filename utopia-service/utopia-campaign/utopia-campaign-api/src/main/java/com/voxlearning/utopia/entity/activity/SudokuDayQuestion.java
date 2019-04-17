package com.voxlearning.utopia.entity.activity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 数独活动每天题目记录表 (可以考虑创建活动时预生成)
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-activity")
@DocumentCollection(collection = "vox_activity_sudoku_day")
@UtopiaCacheRevision("20180911")
@DocumentIndexes({
        @DocumentIndex(def = "{'activityId':1}", background = true)
})
public class SudokuDayQuestion implements CacheDimensionDocument {

    private static final long serialVersionUID = -4775869009469002151L;

    @DocumentId
    private String id;
    private String activityId;            // 活动
    private String curDate;               // 当天日期 20180911
    private List<DayQuestion> questions;  // 当天的题目

    @DocumentCreateTimestamp
    private Date createTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"AID"}, new Object[]{activityId})
        };
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DayQuestion implements java.io.Serializable {
        private static final long serialVersionUID = -4775869009469002151L;

        private String question;   // 题目
        private String answer;     // 答案

        public DayQuestion(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }
}
