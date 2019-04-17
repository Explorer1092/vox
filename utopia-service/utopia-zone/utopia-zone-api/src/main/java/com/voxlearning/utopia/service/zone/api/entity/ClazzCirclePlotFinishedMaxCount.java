package com.voxlearning.utopia.service.zone.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import lombok.Setter;

/**
 * 剧情问题
 * @author yulong.ma
 * @date 2018-11-9 19:19
 */
@Getter
@Setter
@DocumentConnection(configName = "mongod-columb")
@DocumentDatabase(database = "vox-class-circle")
@DocumentCollection(collection = "vox_class_circle_plot_question")
public class ClazzCirclePlotFinishedMaxCount implements Serializable {
    private static final long serialVersionUID = -7191218722149632677L;
    @DocumentId
    private Integer id;//剧情id

    private Integer finishedMaxCount;//最大通关完成数量

}
