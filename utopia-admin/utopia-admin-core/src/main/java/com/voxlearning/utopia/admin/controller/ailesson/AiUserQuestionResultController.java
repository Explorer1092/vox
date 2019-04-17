package com.voxlearning.utopia.admin.controller.ailesson;


import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishUserLoader;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.data.ExpendLessonConfigData;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;
import static com.voxlearning.alps.calendar.DateUtils.dateToString;

@Controller
@RequestMapping("/chips/user/question")
public class AiUserQuestionResultController extends AbstractAdminSystemController {


    @ImportService(interfaceClass = ChipsEnglishUserLoader.class)
    private ChipsEnglishUserLoader chipsEnglishUserLoader;


    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        String userId = getRequestString("userId");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        String lessonId = getRequestString("lessonId");
        if (StringUtils.isNotBlank(userId)) {
            model.addAttribute("id", userId);
            model.addAttribute("bookId", bookId);
            model.addAttribute("unitId", unitId);
            model.addAttribute("lessonId", lessonId);
        }
        return "ailesson/question_index";
    }


    @RequestMapping(value = "/lessons.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadLessonIds() {
        Long userId = getRequestLong("userId");
        String bookId = getRequestString("bookId");
        String unitType = getRequestString("unitType");
        String unitId = getRequestString("unitId");
        return wrapper(mm -> {
            mm.putAll(chipsEnglishUserLoader.loadUserChipsLessonIds(userId, bookId, ChipsUnitType.safeOf(unitType), unitId));
        });
    }


    @RequestMapping(value = "/books.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadBookIdss() {
        Long userId = getRequestLong("userId");
        return wrapper(mm -> {
            mm.putAll(chipsEnglishUserLoader.loadUserChipsBookIds(userId));
        });
    }

    @RequestMapping(value = "/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage loadResult() {
        Long userId = getRequestLong("userId");
        String lessonId = getRequestString("lessonId");
        return wrapper(mm -> {
            mm.putAll(chipsEnglishUserLoader.loadQuestionResult4Crm(userId, lessonId));
        });
    }

    @RequestMapping(value = "/userAnswerV2.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage loadResultV2() {
        Long userId = getRequestLong("userId");
        String bookId = getRequestString("bookId");
        String unitId = getRequestString("unitId");
        return wrapper(mm -> {
            mm.putAll(chipsEnglishUserLoader.loadQuestionResultByUnit4Crm(userId, bookId, unitId));
        });
    }

    @RequestMapping(value = "/indexV2.vpage", method = {RequestMethod.GET})
    public String questionIndexV2() {
        return "ailesson/questionIndexV2";
    }


    @RequestMapping(value = "/download_lesson_config.vpage", method = {RequestMethod.GET})
    public void downloadLessonConfig() {

        // Not use production env
        if (RuntimeMode.isProduction()) {
            return;
        }

        MapMessage mm = chipsEnglishUserLoader.loadLessonConfigExpend();

        if (mm.isSuccess()) {
            List<ExpendLessonConfigData> dialogue = Optional.ofNullable(mm.get("dialogue")).map(x -> (List<ExpendLessonConfigData>) x).orElse(Collections.emptyList());
            List<ExpendLessonConfigData> task = Optional.ofNullable(mm.get("task")).map(x -> (List<ExpendLessonConfigData>) x).orElse(Collections.emptyList());

            HSSFWorkbook data = exportExcel(dialogue, task);
            String filename = "薯条英语" + "_" + dateToString(new Date(), FORMAT_SQL_DATE) + ".xls";
            try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
                data.write(outStream);
                HttpRequestContextUtils.currentRequestContext().downloadFile(filename, "application/vnd.ms-excel", outStream.toByteArray());
            } catch (Exception e) {
                logger.error("exportData error.", e);
            }
        }

    }


    private HSSFWorkbook exportExcel(List<ExpendLessonConfigData> dialogue, List<ExpendLessonConfigData> task) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFCellStyle borderStyle = getHssfCellStyle(workbook);
        HSSFSheet sheet1 = workbook.createSheet("情景对话");
        HSSFSheet sheet2 = workbook.createSheet("任务对话");


        sheet1.setDefaultColumnWidth(60);
        sheet2.setDefaultColumnWidth(60);


        drawTable(sheet1, borderStyle, dialogue);
        drawTable(sheet2, borderStyle, task);

        return workbook;

    }


    private void drawTable(HSSFSheet sheet, HSSFCellStyle style, List<ExpendLessonConfigData> list) {
        int rownum = 0;
        for (int i = 0; i < list.size(); i++) {
            ExpendLessonConfigData lessonData = list.get(i);


            String title = lessonData.getTitle();
            List<ExpendLessonConfigData.ExpendLessonConfigJsgf> jsgfList = lessonData.getJsgfList();


            for (int j = 0; j < jsgfList.size(); j++) {
                ExpendLessonConfigData.ExpendLessonConfigJsgf data = jsgfList.get(j);
                List<String> expand = data.getData();

                for (int k = 0; k < expand.size(); k++) {

                    HSSFRow row = sheet.createRow(rownum++);

                    row.createCell(0).setCellValue(title);
                    row.createCell(1).setCellValue(data.getLevel());
                    row.createCell(2).setCellValue(data.getJsgf());
                    row.createCell(3).setCellValue(expand.get(k));
                    row.setRowStyle(style);
                }
            }
        }
    }

    private HSSFCellStyle getHssfCellStyle(HSSFWorkbook hssfWorkbook) {
        HSSFCellStyle borderStyle = hssfWorkbook.createCellStyle();
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);
        borderStyle.setAlignment(HorizontalAlignment.CENTER);
        HSSFFont titleFont = hssfWorkbook.createFont();
        titleFont.setFontHeightInPoints((short) 12);
        borderStyle.setFont(titleFont);
        borderStyle.setWrapText(false);
        return borderStyle;
    }


    private MapMessage wrapper(Consumer<MapMessage> wrapper) {

        MapMessage mm = MapMessage.successMessage();
        try {
            wrapper.accept(mm);
        } catch (Exception e) {
            mm = MapMessage.errorMessage(e.getMessage());
            logger.error(e.getMessage());

        }
        return mm;
    }


}
