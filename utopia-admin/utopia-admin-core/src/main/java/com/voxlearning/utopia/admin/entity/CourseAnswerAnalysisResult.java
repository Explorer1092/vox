package com.voxlearning.utopia.admin.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author guangqing
 * @since 2018/8/16
 */
@Data
@NoArgsConstructor
@DocumentConnection(configName = "mongo-madrid")
@DocumentDatabase(database = "vox-diagnosis")
@DocumentCollection(collection = "course_answer_analysis_result")
public class CourseAnswerAnalysisResult implements Serializable{
    //课时id+变式id+课程id+前测id+后测id+page
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
    //课程id
    @DocumentField("course_id")
    private String courseId;
    //页码
    @DocumentField("page")
    private Integer page;
    //课程答题总人数
    @DocumentField("course_answer_num")
    private Integer courseAnswerNum;
    //课程答题总时间
    @DocumentField("course_answer_time_total")
    private Double courseAnswerTimeTotal;
    //平均答题时长
    @DocumentField("course_answer_time_avg")
    private Double courseAnswerTimeAvg;

    @DocumentField("answer")
    private List<CourseAnswerAnalysisResultAnswer> answerList;

    @Getter
    @Setter
    public static class CourseAnswerAnalysisResultAnswer implements Serializable {

        private static final long serialVersionUID = -3212627023499931194L;
        //用户答案
        @DocumentField("user_answer")
        private Integer userAnswer;
        //对错
        @DocumentField("result")
        private Boolean result;
        //占比
        @DocumentField("rate")
        private Double rate;
        //该答案总人数
        @DocumentField("answer_num")
        private Integer answerNum;
    }
}
