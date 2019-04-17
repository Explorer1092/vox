package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 纸质口算答题详情数据结构
 */
@Getter
@Setter
public class OcrMentalImageDetail implements Serializable {
    private static final long serialVersionUID = -272947298830822108L;

    private Integer code;   // error code.0 is correct response, others will be set when error happened.
    private String message; // error message here
    private Integer srv_type; //int 0为口算 1为教辅
    private String version; // service version
    private String img_id; // image id, used to distinct each image
    private String img_url; // image url store on alibaba OSS
    private Integer img_height; // image height, describe in pixels
    private Integer img_width; // image width, describe in pixels
    private Integer number;
    private List<Form> forms; // formula list on the image
    private OcrMentalArithmeticDiagnosis omads; //错题诊断返回的诊断结果
    private Map<String, List<String>> kpSymptoms; //知识点id -> 错因列表


    @Getter
    @Setter
    public static class Form implements Serializable {
        private static final long serialVersionUID = 1822715124960031563L;

        private Box box; // formula position on image
        private String text; // latex result for the formula which will be used for juge result and give the value of field "judge"
        private Integer judge; // 答案框批改结果，0为错，1为对，2为无法批改，3为没有填写答案.
        private String usertext; // correct result for the formula.
        private String keypoint; // key point about the formula.
        private Date correctAt; //改判时间(报错时间)

        private List<Coordinate> coordinate; // 小题整个区域坐标信息
        private List<Form> answers;
        private OcrMentalArithmeticSymptomAnalysis symptomAnalysis; // 错因分析

        @JsonIgnore
        public Boolean isCorrect(){
            return correctAt != null;
        }
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class Coordinate implements Serializable {
        private static final long serialVersionUID = -8603950550821991910L;

        private Integer x; // x dimension of this formula on image
        private Integer y; // y dimension of this formula on image
    }

    @Getter
    @Setter
    public static class Box implements Serializable {
        private static final long serialVersionUID = -1970776485084036659L;

        private Integer width; // width of the formula on the image
        private Integer top; // top dimension of this formula on image
        private Integer height; // height of the formula on the image
        private Integer left; // left dimension of this formula on image
    }

    @Getter
    @Setter
    public static class OcrMentalArithmeticDiagnosis implements Serializable  {
        private static final long serialVersionUID = -4458459774192292013L;
        String imgUrl; //图片地址用来确定对应关系
        List<ItemPoint> itemPoints;//图片知识点列表
    }

    @Getter
    @Setter
    public static class OcrMentalArithmeticSymptomAnalysis implements Serializable {
        private static final long serialVersionUID = -7671287615906001062L;

        private String symptom;//错因
        private String analysis;//分析
        private String right_pic;//正确图片
        private String wrong_pic;//错误图片
    }

    @Getter
    @Setter
    public static class ItemPoint implements Serializable  {
        private static final long serialVersionUID = 719558397746898866L;
        private String itemContent;//公式
        private List<Point> points;//知识点
    }

    @Getter
    @Setter
    public static class Point implements Serializable {
        private static final long serialVersionUID = -8475783266374494042L;
        private String pointId;//知识点id
        private String pointName;//知识点名称
        private String errorCause; //错因
        private String courseId;//课程id
        private String courseName;//课程名称
    }
}
