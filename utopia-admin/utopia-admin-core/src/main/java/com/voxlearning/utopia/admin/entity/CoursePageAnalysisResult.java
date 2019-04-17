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
@DocumentCollection(collection = "course_page_analysis_result")
public class CoursePageAnalysisResult implements Serializable{
    //课时id+变式id+课程id+前测id+后测id+page
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String _id;
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
    //课程id
    @DocumentField("course_id")
    private String courseId;
    //页码
    @DocumentField("page")
    private Integer page;
    //加载人数
    @DocumentField("load_num")
    private Integer loadNum;
    //退出人数
    @DocumentField("quit_num")
    private Integer quitNum;
    //向前浏览人数
    @DocumentField("pre_num")
    private Integer preNum;
    //向后浏览人数
    @DocumentField("post_num")
    private Integer postNum;
    //停留总人数
    @DocumentField("stay_num")
    private Integer stayNum;
    //停留总时间
    @DocumentField("stay_time_total")
    private Double stayTimeTotal;
    //平均停留时长
    @DocumentField("stay_time_avg")
    private Double stayTimeAvg;
    //默认停留时长
    @DocumentField("stay_time_defult")
    private Double stayTimeDefult;
    //开始统计时间
    @DocumentField("create_time")
    private Date createTime;
    //结束统计时间
    @DocumentField("update_time")
    private Date updateTime;

}
