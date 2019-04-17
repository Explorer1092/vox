package com.voxlearning.utopia.admin.entity;

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author guangqing
 * @since 2018/7/17
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-madrid")
@DocumentDatabase(database = "vox-diagnosis")
@DocumentCollection(collection = "user_course_behavior")
public class UserCourseBehavior implements Serializable{
    // 唯一id eq: expGroupId + "_ + expId + "_" + courseId + "_" + page
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    //实验组Id
    private String expGroupId;
    //实验Id
    private String expId;
    //课程Id
    private String courseId;
    //页面
    private Integer page;
    //加载人数
    private Integer loadNum;
    //退出人数
    private Integer quitNum;
    //向前人数
    private Integer preBrowse;
    //向后人数
    private Integer postBrowse;
    //平均停留时长
    private Double avgStayTime;

    private Double defStayTime;

    @DocumentCreateTimestamp
    private Date createDate;

    @DocumentUpdateTimestamp
    private Date updateDate;

    private Boolean disabled;
}
