package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-homework")
@DocumentCollection(collection = "vox_uploader_resource_library")
@UtopiaCacheExpiration(604800)
@UtopiaCacheRevision(value = "20181130")
public class UploaderResourceLibrary implements Serializable {
    private static final long serialVersionUID = 6942641697214726720L;

    @DocumentId
    private String id;
    private String source;  //来自于
    private String fileType;   //资源类型
    private String url;    //资源地址
    private Long userId;    //用户id
    private String courseId;  //课程id
    private String homeworkId; //作业id
    private String keywork;  //关键字
    @DocumentCreateTimestamp
    private Date createAt;
    @DocumentUpdateTimestamp
    private Date updateAt;

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(UploaderResourceLibrary.class, id);
    }

}
