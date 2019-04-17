package com.voxlearning.utopia.admin.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author guangqing
 * @since 2018/8/16
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-madrid")
@DocumentDatabase(database = "vox-diagnosis")
@DocumentCollection(collection = "book_course_analysis_result")
public class BookCourseAnalysisResult implements Serializable{
    //系列id+教材id
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    //系列id
    @DocumentField("series_id")
    private String seriesId;
    //教材id
    @DocumentField("book_id")
    private String bookId;
    //系列名称
    @DocumentField("series_name")
    private String seriesName;
    //教材名称
    @DocumentField("book_name")
    private String bookName;
    //线上课程个数
    @DocumentField("course_num_online")
    private Integer courseNumOnline;
    //已使用课程个数
    @DocumentField("course_num_used")
    private Integer courseNumUsed;
    //平均课程完成率
    @DocumentField("course_complete_rate_avg")
    private Double courseCompleteRateAvg;
    //平均课程纠错率
    @DocumentField("course_right_rate_avg")
    private Double courseRightRateAvg;
}
