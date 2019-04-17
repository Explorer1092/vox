package com.voxlearning.utopia.service.campaign.api.entity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DocumentConnection(configName = "hs_misc")
@DocumentTable(table = "VOX_17JT_CHOICE_NOTE")
@CacheBean(type = YiqiJTChoiceNote.class)
@UtopiaCacheRevision("20180710")
public class YiqiJTChoiceNote extends AbstractDatabaseEntity{
    @UtopiaSqlColumn(name = "COURSE_ID") private Long courseId;
    @UtopiaSqlColumn(name = "QUESTIONER_USER_ID") private Long questionerUserId;
    @UtopiaSqlColumn(name = "QUESTIONER_USERNAME") private String questionerUserName;
    @UtopiaSqlColumn(name = "QUESTIONER_PICTURE_URL") private String questionerPictureUrl;
    @UtopiaSqlColumn(name = "QUESTION") private String question;
    @UtopiaSqlColumn(name = "ANSWER_USER_ID") private Long answerUserId;
    @UtopiaSqlColumn(name = "ANSWER_USERNAME") private String answerUserName;
    @UtopiaSqlColumn(name = "ANSWER_PICTURE_URL") private String answerPictureUrl;
    @UtopiaSqlColumn(name = "ANSWER")  private String answer;
}
