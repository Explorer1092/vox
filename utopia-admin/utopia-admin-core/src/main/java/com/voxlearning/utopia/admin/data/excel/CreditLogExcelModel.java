package com.voxlearning.utopia.admin.data.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author feng.guo
 * @since 2019-02-14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditLogExcelModel extends BaseRowModel {
    @ExcelProperty(value = "学校ID", index = 0)
    private Long scid;

    @ExcelProperty(value = "学校名称", index = 1)
    private String schoolName;

    @ExcelProperty(value = "班级ID", index = 2)
    private Long cid;

    @ExcelProperty(value = "班级名称", index = 3)
    private String clazzName;

    @ExcelProperty(value = "学生ID", index = 4)
    private Long sid;

    @ExcelProperty(value = "学生姓名", index = 5)
    private String userName;

    @ExcelProperty(value = "学分来源", index = 6)
    private String creditSource;

    @ExcelProperty(value = "获得分值", index = 7)
    private Double credit;

    @ExcelProperty(value = "获取时间", index = 8)
    private String createTime;
}
