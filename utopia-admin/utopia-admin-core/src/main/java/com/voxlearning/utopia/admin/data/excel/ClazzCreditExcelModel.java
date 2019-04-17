package com.voxlearning.utopia.admin.data.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author feng.guo
 * @since 2019-02-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClazzCreditExcelModel extends BaseRowModel {
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

    @ExcelProperty(value = "本周学分", index = 6)
    private Double proCredit;

    @ExcelProperty(value = "本期学分", index = 7)
    private Double totalCredit;

    @ExcelProperty(value = "本周英语学分", index = 8)
    private Double proEngCredit;

    @ExcelProperty(value = "本周数学学分", index = 9)
    private Double proMathCredit;

    @ExcelProperty(value = "本周语文学分", index = 10)
    private Double proChineseCredit;
}
