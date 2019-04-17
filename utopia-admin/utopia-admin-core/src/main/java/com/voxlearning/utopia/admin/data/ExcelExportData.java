package com.voxlearning.utopia.admin.data;

import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 导出Excel的数据
 *
 * @author yuechen.wang
 * @since 2017-7-11
 **/
@Getter
public class ExcelExportData {

    private String sheet;
    private String[] title;
    private int[] width;
    private List<List<String>> data;
    private int columns;

    private Set<Integer> highlightTitle;

    public ExcelExportData(String sheet, String[] title, int[] width, List<List<String>> data, int columns) {
        this.sheet = sheet;
        this.title = title;
        this.width = width;
        this.data = data;
        this.columns = columns;
        this.highlightTitle = new HashSet<>();
    }

    /**
     * 设置表头特殊背景色
     */
    public void specialTitleStyle(int colIndex) {
        highlightTitle.add(colIndex);
    }

    public boolean highlight(int colIndex) {
        return highlightTitle.contains(colIndex);
    }
}
