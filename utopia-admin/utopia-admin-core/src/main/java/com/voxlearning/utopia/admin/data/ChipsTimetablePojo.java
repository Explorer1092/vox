package com.voxlearning.utopia.admin.data;

import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author guangqing
 * @since 2018/10/17
 */
@Getter
@Setter
public class ChipsTimetablePojo {

    private String productId;
    private String productName;
    private String beginDate;
    private String endDate;
//    private List<Course> courseList;
    private List<EditPojo> editPojoList;
    //该产品下总单元数量，多个教材单元数量之和
    private Integer allUnitNum;

    @Getter
    @Setter
    public static class Course {
        private Date beginDate;
        private String unitId;
        private String unitName;
        private String bookId;
        private String bookName;
    }

    @Getter
    @Setter
    public static class EditPojo{
        private String bookId;
        private String bookName;
        //单元开课时间,有序
        private String dateList;
        //该教材下单元数量
        private Integer unitNum;
    }

}
