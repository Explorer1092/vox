package com.voxlearning.utopia.agent.service.export;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.bean.export.DataIsEmptyException;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import com.voxlearning.utopia.agent.bean.export.XSSFWorkbookExportService;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 实际导出XSSWorkbook 对象的类
 * Created by yaguang.wang on 2017/2/15.
 */
@Setter
@Getter
public class XSSFWorkbookExportServiceImpl implements XSSFWorkbookExportService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private List<ExportAble> export;
    private String template;
    private int initLine = 1;
    private boolean allowEmpty = true;

    public XSSFWorkbookExportServiceImpl(List export, String template) {
        this.export = export;
        this.template = template;
    }

    public XSSFWorkbookExportServiceImpl(List export, String template, boolean allowEmpty) {
        this.export = export;
        this.template = template;
        this.allowEmpty = allowEmpty;
    }

    public XSSFWorkbookExportServiceImpl(List export, String template, Integer initLine) {
        if (initLine == null) {
            this.initLine = 1;
        } else {
            this.initLine = initLine;
        }
        this.export = export;
        this.template = template;
    }

    @Override
    public Workbook convertToXSSFWorkbook() throws DataIsEmptyException {
        Resource resource = new ClassPathResource(this.getTemplate());
        if (!resource.exists()) {
            logger.error("template is not exists");
            return null;
        }
        if (CollectionUtils.isEmpty(this.export) && !allowEmpty) {
            throw new DataIsEmptyException();
        }
        try {
            @Cleanup InputStream in = resource.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            return generalWorkBook(workbook,generalCellStyle(workbook));
        } catch (IOException e) {
            logger.error("convert to XSSFWorkbook is failed:{}", e);
        }
        return null;
    }


    @Override
    public Workbook convertToSXSSFWorkbook() throws Exception {
        Resource resource = new ClassPathResource(this.getTemplate());
        if (!resource.exists()) {
            logger.error("template is not exists");
            return null;
        }
        if (CollectionUtils.isEmpty(this.export) && !allowEmpty) {
            throw new DataIsEmptyException();
        }
        try {
            @Cleanup InputStream in = resource.getInputStream();
            SXSSFWorkbook workbook = new SXSSFWorkbook(new XSSFWorkbook(in));
            return generalWorkBook(workbook,generalCellStyle(workbook));
        } catch (Exception e) {
            logger.error("convert to XSSFWorkbook is failed:{}", e);
        }
        return null;
    }

    private CellStyle generalCellStyle(Workbook workbook){
        Font font = workbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        return cellStyle;
    }

    private Workbook generalWorkBook(Workbook workbook, CellStyle style){
        Sheet sheet = workbook.getSheetAt(0);
        List<ExportAble> lineDatas = this.getExport();
        if (lineDatas == null || CollectionUtils.isEmpty(lineDatas)) {
            return workbook;
        }
        for (ExportAble line : lineDatas) {
            if (line == null) {
                continue;
            }
            List<Object> colInfo = line.getExportAbleData();
            if (CollectionUtils.isEmpty(colInfo)) {
                continue;
            }
            Row row = sheet.createRow(initLine++);
            for (int i = 0; i < colInfo.size(); i++) {
                String info = format(colInfo.get(i));
                Cell cell = row.createCell(i);
                if (!NumberUtils.isDigits(info)) {
                    cell.setCellValue(info);
                } else {
                    cell.setCellValue(SafeConverter.toLong(info));
                }
                if (null != style){
                    cell.setCellStyle(style);
                }
            }
        }
        return workbook;
    }


    /**
     * 设置字符类型文字
     */
    private String format(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

}
