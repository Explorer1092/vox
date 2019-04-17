package com.voxlearning.utopia.agent.service.common;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * BaseExcelService
 *
 * @author song.wang
 * @date 2016/9/13
 */
@Slf4j
@Named
public class BaseExcelService extends AbstractAgentService {

    private static final int BYTES_BUFFER_SIZE = 1024 * 8;

    public void downloadTemplate(String template, String fileName){
        try {
            Resource resource = new ClassPathResource(template);
            if (!resource.exists()) {
                logger.error("downloadTemplate - template not exists for template = {}", template);
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(in, out);
            fileName = fileName + ".xlsx";
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("downloadTemplate - Excp : {}; template = {}", e, template);
        }
    }

    private void write(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[BYTES_BUFFER_SIZE];
        int size;
        while ((size = in.read(buffer, 0, buffer.length)) > 0) {
            out.write(buffer, 0, size);
        }
        out.flush();
    }

    public XSSFWorkbook readRequestWorkbook(HttpServletRequest request, String name) {

        if (!(request instanceof MultipartHttpServletRequest)) {
            logger.error("readRequestWorkbook - Not MultipartHttpServletRequest");
            return null;
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        try {
            MultipartFile file = multipartRequest.getFile(name);
            if (file == null || file.isEmpty()) {
                logger.error("readRequestWorkbook - Empty MultipartFile with name = {}", name);
                return null;
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                logger.error("readRequestWorkbook - Not a SupportedFileType with fileName = {}", fileName);
                return null;
            }
            @Cleanup InputStream in = file.getInputStream();
            return new XSSFWorkbook(in);
        } catch (Exception e) {
            logger.error("readRequestWorkbook - Excp : {}", e);
            return null;
        }
    }

    public XSSFWorkbook readWorkBookFromTemplate(String template){
        try {
            Resource resource = new ClassPathResource(template);
            if (!resource.exists()) {
                logger.error("template not exists for template = {}", template);
                return null;
            }
            @Cleanup InputStream in = resource.getInputStream();
            return new XSSFWorkbook(in);
        } catch (Exception e) {
            logger.error("template - Excp : {}; template = {}", e, template);
        }
        return null;
    }

    // 单元格默认格式
    public CellStyle createCellStyle(Workbook workbook){
        Font font = workbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        return cellStyle;
    }

    /**
     * 设置单元格数值
     * @param row
     * @param column
     * @param style
     * @param value
     */
    public void setCellValue(Row row, int column, CellStyle style, Object value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        if (null != style){
            cell.setCellStyle(style);
        }
        String info = value == null ? "" : String.valueOf(value).trim();
        if (!NumberUtils.isDigits(info)) {
            cell.setCellValue(info);
        } else {
            cell.setCellValue(SafeConverter.toLong(info));
        }
    }
}
