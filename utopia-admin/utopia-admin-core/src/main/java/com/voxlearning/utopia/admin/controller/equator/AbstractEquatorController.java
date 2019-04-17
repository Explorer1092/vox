package com.voxlearning.utopia.admin.controller.equator;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.ResolveExcelResult;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.service.wonderland.api.data.WonderlandResult;
import jxl.Cell;
import jxl.CellView;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author peng.zhang.a
 * @since 17-2-4
 */
public abstract class AbstractEquatorController extends AbstractAdminSystemController {

    private static final String FILE_ROOT_PATH = "wonderland";

    protected void validateParamNotNull(String paramKey, String paramName) {
        String paramValue = getRequest().getParameter(paramKey);
        if (StringUtils.isEmpty(paramValue)) {
            throw new IllegalArgumentException(paramName + "不能为空");
        }
    }

    protected void validateParamNotNullAndLength(String paramKey, String paramName, int maxLength) {
        validateParamNotNull(paramKey, paramName);
        validateParamLength(paramKey, paramName, maxLength);
    }

    protected void validateParamLength(String paramKey, String paramName, int maxLength) {
        String paramValue = getRequest().getParameter(paramKey);
        if (StringUtils.isNotEmpty(paramValue) && paramValue.length() > maxLength) {
            throw new IllegalArgumentException(paramName + "长度应该小于" + maxLength);
        }
    }


    //默认读取fileKey的图片，若没有图片则读取fileKey+"Url"的参数
    public String uploadImage(String fileKey) {
        return uploadFile(fileKey);
    }

    //默认读取fileKey的图片，若没有图片则读取fileKey+"Url"的参数
    public String uploadFile(String fileKey) {

        if ((getRequest() instanceof MultipartHttpServletRequest)) {
            try {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                MultipartFile inputFile = multipartRequest.getFile(fileKey);
                return uploadFile(inputFile);
            } catch (Exception ignored) {
                logger.error("upload img error");
            }
        }
        return getRequestString(fileKey + "Url");
    }

    //默认读取fileKey的图片，若没有图片则读取fileKey+"Url"的参数
    public String uploadFile(File inputFile) {

        try {
            if (inputFile != null) {
                return AdminOssManageUtils.upload(inputFile, FILE_ROOT_PATH);
            }
        } catch (Exception ignored) {
            logger.error("upload img error");
        }
        return null;
    }

    //默认读取fileKey的图片，若没有图片则读取fileKey+"Url"的参数
    public String uploadFile(MultipartFile inputFile) {

        try {
            if (inputFile != null && !inputFile.isEmpty() && inputFile.getSize() != 0) {
                return AdminOssManageUtils.upload(inputFile, FILE_ROOT_PATH);
            }
        } catch (Exception ignored) {
            logger.error("upload img error");
        }
        return null;
    }

    protected ResolveExcelResult resolveExcel(String fineName, int pageNum) throws Exception {
        if (!(getRequest() instanceof MultipartHttpServletRequest)) {
            throw new Exception("请求类型错误");
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
        MultipartFile file = multipartRequest.getFile(fineName);
        List<List<String>> data = new ArrayList<>();
        String fileName = file.getOriginalFilename();
        if (!file.isEmpty()) {
            Workbook workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件

            Sheet sheet = workbook.getSheet(pageNum);// Excel中的工作表 下标从0开始
            int row = sheet.getRows(); // 工作表共有的行
            for (int i = 0; i < row; i++) {
                List<String> rowData = new ArrayList<>();
                Cell[] cells = sheet.getRow(i);
                for (Cell cell : cells) {
                    String value = SafeConverter.toString(cell.getContents());
                    if (StringUtils.isBlank(value) || StringUtils.isBlank(value.trim())) {
                        value = "";
                    } else if ("NULL".equalsIgnoreCase(value)) {
                        value = "";
                    }
                    rowData.add(value);
                }
                if (rowData.stream().filter(StringUtils::isNotBlank).findFirst().orElse(null) == null) {
                    throw new Exception("不能包含空行，请先删除底部空行");
                }
                data.add(rowData);
            }

            if (CollectionUtils.isNotEmpty(data)) {
                int colsSize = data.get(0).size();
                List<String> errRow = data.stream().filter(p -> p.size() != colsSize).findFirst().orElse(null);
                if (errRow != null) {
                    throw new Exception("数据行有错误,与其他行数据不一致," + data.get(0).get(0) + "=" + errRow.get(0));
                }
            }
        }
        return ResolveExcelResult.newInstance(fileName, data);
    }

    protected List<Map<String, String>> resolveExcel(String fineName, int pageNum, int startRowNum) throws Exception {
        if (!(getRequest() instanceof MultipartHttpServletRequest)) {
            throw new Exception("请求类型错误");
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
        MultipartFile file = multipartRequest.getFile(fineName);
        List<Map<String, String>> data = new ArrayList<>();
        if (!file.isEmpty()) {
            Workbook workbook = Workbook.getWorkbook(file.getInputStream());// 得到Excel文件

            Sheet sheet = workbook.getSheet(pageNum);// Excel中的工作表 下标从0开始
            if (sheet.getRows() <= startRowNum) {
                return null;
            }
            int row = sheet.getRows(); // 工作表共有的行
            Cell[] keyCells = sheet.getRow(startRowNum);
            for (Cell keyCell : keyCells) {
                if (StringUtils.isBlank(keyCell.getContents())) {
                    return null;
                }
            }
            for (int i = startRowNum + 1; i < row; i++) {
                HashMap<String, String> rowData = new LinkedHashMap<>();
                Cell[] cells = sheet.getRow(i);
                for (int j = 0; j < cells.length; j++) {
                    String key = keyCells[j].getContents();
                    String value = SafeConverter.toString(cells[j].getContents());
                    rowData.put(key, value);
                }
                data.add(rowData);
            }
        }
        return data;
    }


    protected void downloadExcel(String filename, List<List<String>> data) {
        WritableWorkbook writableWorkbook = null;
        try {
            HttpServletResponse response = getResponse();

            writableWorkbook = Workbook.createWorkbook(response.getOutputStream());
            WritableSheet ws = writableWorkbook.createSheet("Sheet_0", 0);
            CellView navCellView = new CellView();
            navCellView.setAutosize(true); //设置自动大小
            navCellView.setSize(40);

            // 用于正文居左
            WritableCellFormat wcf_left = new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 10));
            wcf_left.setBorder(jxl.format.Border.NONE, jxl.format.BorderLineStyle.THIN); // 线条
            wcf_left.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE); // 文字垂直对齐
            wcf_left.setAlignment(jxl.format.Alignment.CENTRE); // 文字水平对齐
            wcf_left.setWrap(false); // 文字是否换行

            for (int i = 0; i < data.size(); i++) {
                List<String> rowData = data.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    String value = rowData.get(j);
                    if (StringUtils.isBlank(value)) {
                        value = "NULL";
                    }
                    ws.addCell(new Label(j, i, value, wcf_left));
                }
            }
            ws.getSettings().setDefaultColumnWidth(15);
            ws.setColumnView(0, navCellView);
            response.reset();// 清空输出流
            response.setHeader("Content-disposition", "attachment; filename=" + attachmentFilenameEncoding(filename, getRequest()));
            response.setContentType("application/vnd.ms-excel");// 定义输出类型

            writableWorkbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writableWorkbook != null) {
                try {
                    writableWorkbook.close();
                } catch (IOException | WriteException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public String getHeaderRefer() {
        return getRequest().getHeader("Referer");
    }


    public String redirectReferWithInfo(String info) {
        return redirectReferWithInfo(info, false);
    }

    public String redirectReferWithInfo(String info, Boolean success) {
        String headerRefer = getHeaderRefer();
        try {
            if (headerRefer == null) {
                getResponse().getWriter().println(info);
                return null;
            }
            MapMessage param = success ? WonderlandResult.successMessage(info) : WonderlandResult.errorMessage(info);
            headerRefer = UrlUtils.buildUrlQuery(headerRefer, param);
            headerRefer = headerRefer.replaceAll("\\+", "%20");
            return "redirect:" + headerRefer;
        } catch (Exception e) {
            logger.error("redirectReferWithInfo error, info={}", info, e);
            return null;
        }

    }

}
