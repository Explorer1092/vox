package com.voxlearning.utopia.admin.entity;

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author guangqing
 * @since 2018/7/17
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-madrid")
@DocumentDatabase(database = "vox-diagnosis")
@DocumentCollection(collection = "user_question_behavior")
public class UserQuestionBehavior implements Serializable{
    //唯一id  eq: expGroupId + "_ + expId + "_" + courseId + "_" + page
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
    //平均答题时间
    private Double avgAnswerTime;
    //
    private List<UserQuestionBehaviorAnswer> answer;

    @DocumentCreateTimestamp
    private Date createDate;

    @DocumentUpdateTimestamp
    private Date updateDate;

    private Boolean disabled;

    @Getter
    @Setter
    public static class UserQuestionBehaviorAnswer implements Serializable {
        //用户答案
        private Integer userAnswer;
        //对错
        private Boolean result;
        //占比
        private String rate;
    }
}