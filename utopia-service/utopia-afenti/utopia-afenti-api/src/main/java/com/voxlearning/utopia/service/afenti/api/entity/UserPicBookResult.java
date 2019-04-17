package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * 我的绘本操作(阅读、作题)历史记录 实体
 * @author haitian.gan
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-newworld")
@DocumentDatabase(database = "vox-picbook")
@DocumentCollection(collection = "vox_user_picbook_history_{}",dynamic = true)
@UtopiaCacheRevision("20180227")
public class UserPicBookResult implements CacheDimensionDocument{

    private static final long serialVersionUID = -8464405958384959168L;
    @DocumentId private String id;

    private Long userId;
    private String type;
    private Integer module;
    private String bookId;
    private String pageId;
    private String questionId;
    private Integer duration;
    private String audioUrl;
    private Integer score;
    private Boolean finish;
    private String answer;

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }

    public List<List<String>> parseAnswer(){
        return Optional.ofNullable(answer)
                .map(a -> stream(a.split(";")).map(f -> asList(f.split(","))).collect(toList()))
                .orElse(new ArrayList<>());
    }
}
