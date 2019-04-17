package com.voxlearning.utopia.business.api.entity;


import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author fugui.chang
 * @since 2016/9/19
 */
@Getter
@Setter
@DocumentDatabase(database = "vox-schoolmaster")
@DocumentCollection(collection = "vox_class_study_situtation")
@DocumentIndexes({
        @DocumentIndex(def = "{'school_id':1,'dt':1,'subject':1}", background = true),
})
@UtopiaCacheExpiration
@UtopiaCacheRevision("20161121")
@DocumentConnection(configName = "mongo-schoolmaster")
public class ClassStudySitutation implements Serializable{
    private static final long serialVersionUID = -7257390939331773656L;

    @DocumentId private String id;
    @DocumentField("dt") private Long yearmonth; //日期 年月
    @DocumentField("province_id") private Long provinceId; //省id
    @DocumentField("province_name") private String provinceName;
    @DocumentField("city_id") private Long cityId; //市id
    @DocumentField("city_name") private String cityName;
    @DocumentField("county_id") private Long countyId; //区id
    @DocumentField("county_name") private String countyName;
    @DocumentField("school_id") private Long schoolId; //学校id
    @DocumentField("school_level") private Integer schoolLevel; //学校级别（1小学,2中学)
    @DocumentField("school_name") private String schoolName; //学校名字
    @DocumentField("grade") private String grade; //年级
    @DocumentField("group_id") private Long classId; //班级id
    @DocumentField("group_name") private String className;
    @DocumentField("subject") private String subject; //学科
    @DocumentField("teacher_id") private Long teacherId; //老师id
    @DocumentField("teacher_name") private String teacherName;
    @DocumentField("accomplish_stu_num") private Integer accomplish_stu_num;
    @DocumentField("teacher_assgin_num") private Long teacher_assgin_num; //老师布置作业老师使用个数
    @DocumentField("accomplish_hw_rate") private Double accomplish_hw_rate; //作业完成率(完成作业的学生总个数/收到作业的学生总个数)
    @DocumentField("group_accomplish_hw_avgscore") private Double group_accomplish_hw_avgscore; //平均分(完成作业的学生总分数/完成作业的学生总个数)
    @DocumentField("stu_accomplish_hw_avgscore_85_up_stu_num") private Long stu_accomplish_hw_avgscore_85_up_stu_num; //85-100人数
    @DocumentField("stu_accomplish_hw_avgscore_60_up_stu_num") private Long stu_accomplish_hw_avgscore_60_up_stu_num;//60-85人数
    @DocumentField("stu_accomplish_hw_avgscore_60_down_stu_num") private Long stu_accomplish_hw_avgscore_60_down_stu_num; //<60人数
    @DocumentCreateTimestamp @DocumentField("createtime") protected Date createTime;//创建时间

    public static String generateCacheKey(Long schoolId,Long yearmonth,String subject) {
        return CacheKeyGenerator.generateCacheKey(ClassStudySitutation.class,new String[]{"SCHOOLID","YEARCHMONTH","SUBJECT"},new Object[]{schoolId,yearmonth,subject});
    }
}
