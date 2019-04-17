package com.voxlearning.utopia.agent.bean.export;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 用户导出 XSSFWorkbook 对象
 * Created by yaguang.wang on 2017/2/15.
 */
public interface XSSFWorkbookExportService {

    /**
     * 废弃，写入相当的慢，请使用convertToSXSSFWorkbook
     * @return
     * @throws DataIsEmptyException
     */
    @Deprecated
    Workbook convertToXSSFWorkbook() throws DataIsEmptyException;

    Workbook convertToSXSSFWorkbook() throws Exception;
}
