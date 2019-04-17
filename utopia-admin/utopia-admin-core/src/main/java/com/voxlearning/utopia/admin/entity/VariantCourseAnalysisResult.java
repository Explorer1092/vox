package com.voxlearning.utopia.admin.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author guangqing
 * @since 2018/8/16
 */
@Data
@NoArgsConstructor
@DocumentConnection(configName = "mongo-madrid")
@DocumentDatabase(database = "vox-diagnosis")
@DocumentCollection(collection = "variant_course_analysis_result")
public class VariantCourseAnalysisResult implements Serializable{
    //课时id+变式id+课程id+前测id+后测id
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    //系列id
    @DocumentField("series_id")
    private String seriesId;
    //教材id
    @DocumentField("book_id")
    private String bookId;
    //单元id
    @DocumentField("unit_id")
    private String unitId;
    //课时id
    @DocumentField("section_id")
    private String sectionId;
    //变式id
    @DocumentField("variant_id")
    private String variantId;
    //前测题id
    @DocumentField("pre_id")
    private String preId;
    //后测题id
    @DocumentField("post_id")
    private String postId;
    //课程id
    @DocumentField("course_id")
    private String courseId;
    //课程名称
    @DocumentField("course_name")
    private String courseName;
    //课程命中数
    @DocumentField("course_target_num")
    private Integer courseTargetNum;
    //打开课程数
    @DocumentField("course_begin_num")
    private Integer courseBeginNum;
    //完成课程数
    @DocumentField("course_finish_num")
    private Integer courseFinishNum;
    //课程完成率
    @DocumentField("course_complete_rate")
    private Double courseCompleteRate;
    //后侧题做题人数
    @DocumentField("post_do_num")
    private Integer postDoNum;
    //过滤后后测题做题人数
    @DocumentField("post_do_num_filter")
    private Integer postDoNumFilter;
    //后测完成数率
    @DocumentField("post_complete_rate")
    private Double postCompleteRate;
    //后测正确率
    @DocumentField("post_right_rate")
    private Double postRightRate;
    //开始统计时间
    @DocumentField("create_time")
    private Date createTime;
    //结束统计时间
    @DocumentField("update_time")
    private Date updateTime;
    //后侧题做对的人所用的总时间
    @DocumentField("post_right_time_sum")
    private String postRightTimeSum;
    //后侧题做对的总人数
    @DocumentField("post_right_sum")
    private Integer postRightSum;

}
