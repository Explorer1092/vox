package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Description: 期末复习作业每个班级的统计信息
 * @author: Mr_VanGogh
 * @date: 2018/6/12 下午3:14
 */
@Getter
@Setter
public class BasicReviewHomeworkHistory implements Serializable{

    private static final long serialVersionUID = -4961510771344762727L;

    private String className;          // 班级名称

    private Subject subject;           //学科

    private String startTime;          //开始时间

    private String endTime;            //结束时间

    private Integer totalNum;          //一共人数

    private Integer beginNum;          //开始作业人数

    private Integer finishNum;         //完成人数

    private String packageId;          //假期作业包ID

    private String subjectName;        //学科中文名字

    private String bookId;             //教材ID

    private String bookName;           //教材名称
}
