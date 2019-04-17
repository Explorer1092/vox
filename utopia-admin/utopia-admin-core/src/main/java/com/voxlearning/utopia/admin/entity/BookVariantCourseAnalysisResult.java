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
@DocumentCollection(collection = "book_variant_course_analysis_result")
public class BookVariantCourseAnalysisResult implements Serializable{

    //课时id+变式id
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
    //单元名称
    @DocumentField("unit_name")
    private String unitName;
    //课时名称
    @DocumentField("section_name")
    private String sectionName;
    //变式名称
    @DocumentField("variant_name")
    private String variantName;
    //平均课程完成率
    @DocumentField("course_complete_rate_avg")
    private Double courseCompleteRateAvg;
    //平均后侧完成率
    @DocumentField("post_complete_rate_avg")
    private Double postCompleteRateAvg;
    //对照后测完成率
    @DocumentField("control_post_complete_rate")
    private Double controlPostCompleteRate;
    //平均课程纠错率
    @DocumentField("course_right_rate_avg")
    private Double courseRightRateAvg;
    //对照课程纠错率
    @DocumentField("control_course_right_rate")
    private Double controlCourseRightRate;

}
