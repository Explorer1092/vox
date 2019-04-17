package com.voxlearning.utopia.service.campaign.mapper;

import com.voxlearning.utopia.entity.activity.SudokuDayQuestion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SudokuHistory implements java.io.Serializable {

    private String mark;        // 描述 例如 未完成 未参加
    private String time;        // 耗时 例如 00:08:00 未完成和未加时为空
    private String date;        // 日期 开始时间05月05日 18:09, 如果未参加则只显示05月05日
    private String endDate;     // 结束日期 如果完成了题目,这里展示答完题的那个时间
    private SudokuDayQuestion.DayQuestion question;

}