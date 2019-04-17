package com.voxlearning.utopia.service.newhomework.api.mapper;


import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

//假期作业每个班级的统计信息
@Setter
@Getter
public class NewVacationHomeworkHistory implements Serializable {

    private static final long serialVersionUID = 4916085381583097845L;

    private String className;          // 班级名称

    private Subject subject;           //学科

    private String createTime;         //布置时间

    private String startTime;          //开始时间

    private String endTime;            //结束时间

    private Integer totalNum;          //一共人数

    private Integer beginNum;          //开始作业人数

    private Integer finishNum;         //完成人数

    private String packageId;          //假期作业包ID

    private String subjectName;        //学科中文名字

    private boolean ableToDelete;      //是否可以删除

    private boolean disabled;          //是否已删除

    private String bookId;

    private String bookName;


}
