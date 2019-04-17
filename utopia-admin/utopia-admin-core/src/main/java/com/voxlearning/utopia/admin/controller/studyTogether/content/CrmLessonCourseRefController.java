package com.voxlearning.utopia.admin.controller.studyTogether.content;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.galaxy.service.coin.api.entity.CoinImportHistory;
import com.voxlearning.galaxy.service.studycourse.api.CrmLessonCourseContentService;
import com.voxlearning.galaxy.service.studycourse.api.CrmLessonTemplateLoader;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.galaxy.service.studycourse.api.entity.LessonCourseContentRef;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructLesson;
import com.voxlearning.galaxy.service.studycourse.api.entity.template.ChineseReadingLessonTemplate;
import com.voxlearning.galaxy.service.studycourse.api.entity.template.ClassicalChineseLessonTemplate;
import com.voxlearning.galaxy.service.studycourse.api.entity.template.PictureBookLessonTemplate;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.util.ExcelUtil;
import com.voxlearning.utopia.admin.util.XssfUtils;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherService;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wei.jiang
 * @since 2018/8/14
 */
@Controller
@RequestMapping(value = "opmanager/studyTogether/lessonCourseRef/")
@Slf4j
public class CrmLessonCourseRefController extends AbstractAdminSystemController {


    @ImportService(interfaceClass = CrmStudyTogetherService.class)
    private CrmStudyTogetherService crmStudyTogetherService;
    @ImportService(interfaceClass = CrmLessonTemplateLoader.class)
    private CrmLessonTemplateLoader crmLessonTemplateLoader;
    @ImportService(interfaceClass = CrmLessonCourseContentService.class)
    private CrmLessonCourseContentService crmLessonCourseContentService;
    @Inject
    private StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    /**
     * sku和type的对应列表
     */
    @RequestMapping(value = "get_sku_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSkuList() {
        int type = getRequestInt("type", 1);
        List<StudyLesson> allStudyLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getAllStudyLesson();

        List<String> lessonIds = allStudyLesson
                .stream()
                .filter(e -> e.getCourseType().equals(type))
                .sorted(Comparator.comparing(StudyLesson::getLessonId))
                .map(t -> SafeConverter.toString(t.getLessonId()))
                .collect(Collectors.toList());

        return MapMessage.successMessage().add("lessonIds", lessonIds);
    }

    /**
     * 获取sku下所有lesson
     */
    @RequestMapping(value = "get_lesson_list.vpage", method = RequestMethod.GET)
    public String getLessonList(Model model) {
        String skuId = getRequestString("skuId");
        int type = getRequestInt("type", 1);
        List<StudyLesson> allStudyLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getAllStudyLesson();
        List<String> lessonIds = allStudyLesson
                .stream()
                .filter(e -> e.getCourseType().equals(type))
                .sorted(Comparator.comparing(StudyLesson::getLessonId))
                .map(t -> SafeConverter.toString(t.getLessonId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(lessonIds)) {
            return "/opmanager/studyTogether/lessoncourseref/lessoncoursereflist";
        }
        model.addAttribute("lessonIds", lessonIds);
        model.addAttribute("type", type);
        if (StringUtils.isBlank(skuId)) {
            skuId = lessonIds.get(0);
        }
        model.addAttribute("skuId", skuId);
        StudyLesson lessonById = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLesson(Long.valueOf(skuId));
        if (lessonById == null) {
            return "/opmanager/studyTogether/lessoncourseref/lessoncoursereflist";
        }
        List<CourseStructLesson> courseLessonList = lessonById.getCourseLessonList();
        if (CollectionUtils.isEmpty(courseLessonList)) {
            return "/opmanager/studyTogether/lessoncourseref/lessoncoursereflist";
        }
        courseLessonList = courseLessonList.stream().sorted(Comparator.comparing(CourseStructLesson::getId)).collect(Collectors.toList());
        List<Long> ids = courseLessonList.stream().map(CourseStructLesson::getId).collect(Collectors.toList());
        Map<Long, LessonCourseContentRef> refInfoByIds = crmLessonCourseContentService.getRefInfoByIds(ids);
        Map<String, LessonCourseContentRef> refInfosByIdsStringKey = new HashMap<>();
        refInfoByIds.forEach((k, v) -> {
            String id = SafeConverter.toString(k);
            refInfosByIdsStringKey.put(id, v);
        });
        model.addAttribute("courseLessonList", courseLessonList);
        model.addAttribute("refInfoByIds", refInfosByIdsStringKey);
        return "/opmanager/studyTogether/lessoncourseref/lessoncoursereflist";
    }


    /**
     * template列表
     */
    @RequestMapping(value = "get_template_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTemplateList() {
        int type = getRequestInt("type", 1);
        List<Long> ids = new ArrayList<>();
        switch (type) {
            case 1:
                ids = crmLessonTemplateLoader.loadAllClassicalChineseTemplate()
                        .stream()
                        .map(ClassicalChineseLessonTemplate::getId)
                        .collect(Collectors.toList());
                break;
            case 2:
                ids = crmLessonTemplateLoader.loadAllPictureBookTemplate()
                        .stream()
                        .map(PictureBookLessonTemplate::getId)
                        .collect(Collectors.toList());
                break;
            case 3:
                ids = crmLessonTemplateLoader.loadAllChineseReadLessonTemplate()
                        .stream()
                        .map(ChineseReadingLessonTemplate::getId)
                        .collect(Collectors.toList());
            default:
                break;
        }

        return MapMessage.successMessage().add("templateIds", ids);
    }

    /**
     * 保存修改
     */
    @RequestMapping(value = "save_record.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveRecord() {
        long id = getRequestLong("id");
        long moduleId = getRequestLong("moduleId");
        long skuId = getRequestLong("skuId");
        long templateId = getRequestLong("templateId");
        int contentType = getRequestInt("contentType");
        LessonCourseContentRef lessonCourseContentRef = new LessonCourseContentRef();
        lessonCourseContentRef.setId(id);
        lessonCourseContentRef.setSkuId(skuId);
        lessonCourseContentRef.setModuleId(moduleId);
        lessonCourseContentRef.setTemplateId(templateId);
        lessonCourseContentRef.setContentType(contentType);
        LessonCourseContentRef upsert = crmLessonCourseContentService.saveRefInfo(lessonCourseContentRef);
        return new MapMessage().setSuccess(upsert != null);
    }

    /**
     * 导出sku中的Lesson信息
     */
    @RequestMapping(value = "/exportLessonInfo.vpage", method = RequestMethod.GET)
    public void exportLessonInfo() throws Exception {
        String skuId = getRequestString("skuId");
        int type = getRequestInt("type");
        if (StringUtils.isBlank(skuId)) {
            return;
        }

        String fileName = "导出课节信息-skuId:" + skuId + "-" + DateUtils.dateToString(new Date(), "yyyy-MM-dd-HH-mm-ss") + ".xlsx";
        List<List<String>> dataList = generateLessonInfoData(skuId, type);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        if (CollectionUtils.isNotEmpty(dataList)) {
            String[] dateDataTitle = new String[]{
                    "课节ID", "章节ID", "skuId", "课程类型", "模板ID"
            };
            int[] dateDataWidth = new int[]{
                    5000, 5000, 5000, 5000, 5000
            };
            try {
                xssfWorkbook = XssfUtils.convertToXSSFWorkbook(dateDataTitle, dateDataWidth, dataList, "没有数据");
            } catch (Exception e) {
                logger.error("generate studyTogether wechat info xlsx error!", e);
            }
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try {
                xssfWorkbook.write(outStream);
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        fileName,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException e) {
                logger.error("download studyTogether wechat info error!", e);
            }
        }
    }

    /**
     * 导入课程数据
     */
    @RequestMapping(value = "/importData.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importData() {
        XSSFWorkbook workbook = readExcel("source_file", null);
        String selectedSkuId = getRequestString("sku_id");
        if (workbook == null) {
            return MapMessage.errorMessage("不支持的文件类型或者上传失败");
        }
        String validateMessage = validateImportData(workbook, selectedSkuId);
        if (!SafeConverter.toBoolean(validateMessage)) {
            return MapMessage.errorMessage(validateMessage);
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return MapMessage.errorMessage().add("error", "sheet读取失败");
        }
        int successCount = 0;
        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            long id = 0L;
            long moduleId = 0L;
            long skuId = 0L;
            long templateId = 0L;
            int contentType = 0;
            for (Cell cell : row) {
                if (cell.getColumnIndex() == 0) {
                    id = SafeConverter.toLong(ExcelUtil.getCellValue(cell));
                    if (id == 0L) {
                        break;
                    }
                }
                if (cell.getColumnIndex() == 1) {
                    moduleId = SafeConverter.toLong(ExcelUtil.getCellValue(cell));
                    if (moduleId == 0L) {
                        break;
                    }
                }
                if (cell.getColumnIndex() == 2) {
                    skuId = SafeConverter.toLong(ExcelUtil.getCellValue(cell));
                    if (skuId == 0L) {
                        break;
                    }
                }
                if (cell.getColumnIndex() == 3) {
                    contentType = SafeConverter.toInt(ExcelUtil.getCellValue(cell));
                    if (contentType == 0L) {
                        break;
                    }
                }
                if (cell.getColumnIndex() == 4) {
                    templateId = SafeConverter.toLong(ExcelUtil.getCellValue(cell));
                    if (templateId == 0) {
                        break;
                    }
                }
            }
            LessonCourseContentRef lessonCourseContentRef = new LessonCourseContentRef();
            lessonCourseContentRef.setId(id);
            lessonCourseContentRef.setSkuId(skuId);
            lessonCourseContentRef.setModuleId(moduleId);
            lessonCourseContentRef.setTemplateId(templateId);
            lessonCourseContentRef.setContentType(contentType);
            LessonCourseContentRef upsert = crmLessonCourseContentService.saveRefInfo(lessonCourseContentRef);
            if (upsert != null) {
                successCount++;
            }
        }
        return MapMessage.successMessage().add("count", successCount);
    }

    private List<List<String>> generateLessonInfoData(String skuId, int type) {

        List<List<String>> returnList = new ArrayList<>();
        StudyLesson lessonById = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLesson(SafeConverter.toLong(skuId));
        if (lessonById == null) {
            return Collections.emptyList();
        }
        List<CourseStructLesson> courseLessonList = lessonById.getCourseLessonList();
        if (CollectionUtils.isEmpty(courseLessonList)) {
            return Collections.emptyList();
        }
        courseLessonList = courseLessonList.stream().sorted(Comparator.comparing(CourseStructLesson::getId)).collect(Collectors.toList());
        List<Long> ids = courseLessonList.stream().map(CourseStructLesson::getId).collect(Collectors.toList());
        Map<Long, LessonCourseContentRef> refInfoByIds = crmLessonCourseContentService.getRefInfoByIds(ids);
        courseLessonList.forEach(e -> {
            List<String> list = new ArrayList<>();
            list.add(SafeConverter.toString(e.getId()));
            list.add(SafeConverter.toString(e.getChapterId()));
            list.add(SafeConverter.toString(e.getSkuId()));
            list.add(SafeConverter.toString(type));
            list.add(refInfoByIds.get(e.getId()) != null ? SafeConverter.toString(refInfoByIds.get(e.getId()).getTemplateId()) : "");
            returnList.add(list);
        });
        return returnList;
    }


    //读excel数据
    private XSSFWorkbook readExcel(String name, CoinImportHistory history) {
        HttpServletRequest request = getRequest();
        if (!(request instanceof MultipartHttpServletRequest)) {
            logger.error("readRequestWorkbook - Not MultipartHttpServletRequest");
            return null;
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        try {
            MultipartFile file = multipartRequest.getFile(name);
            if (file.isEmpty()) {
                logger.error("readRequestWorkbook - Empty MultipartFile with name = {}", name);
                return null;
            }
            String fileName = file.getOriginalFilename();
            if (history != null) {
                history.setFileName(fileName);
            }
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


    //校验导入数据
    private String validateImportData(XSSFWorkbook workbook, String selectedSkuId) {
        if (workbook == null) {
            return "文件读取失败";
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return "sheet读取失败";
        }
        Set<String> lessonIds = new HashSet<>();
        Set<String> skuIds = new HashSet<>();
//        Map<String, List<String>> wechatGroupMap = new HashMap<>();
        Row title = sheet.getRow(0);
        for (Cell cell : title) {
            if (cell.getColumnIndex() > 4) {
                return "表格式错误";
            }
            if (cell.getColumnIndex() == 0 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "课节ID")) {
                return "表头错误";
            }
            if (cell.getColumnIndex() == 1 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "章节ID")) {
                return "表头错误";
            }
            if (cell.getColumnIndex() == 2 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "skuId")) {
                return "表头错误";
            }
            if (cell.getColumnIndex() == 3 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "课程类型")) {
                return "表头错误";
            }
            if (cell.getColumnIndex() == 4 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "模板ID")) {
                return "表头错误";
            }

        }
        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            long id = 0L;
            long moduleId = 0L;
            long skuId = 0L;
            long templateId = 0L;
            int contentType = 0;
            for (Cell cell : row) {
                if (cell.getColumnIndex() > 4) {
                    return "表格式错误";
                }
                if (cell.getColumnIndex() == 0) {
                    String cellValue = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(cellValue)) {
                        return "课节ID不能为空!";
                    }
                    boolean add = lessonIds.add(cellValue);
                    if (!add) {
                        return "课节ID不能重复!";
                    }
                    id = SafeConverter.toLong(cellValue);
                }
                if (cell.getColumnIndex() == 2) {
                    String cellValue = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(cellValue)) {
                        return "skuId不能为空";
                    }

                    skuId = SafeConverter.toLong(cellValue);
                }

                if (cell.getColumnIndex() == 4) {
                    String cellValue = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(cellValue)) {
                        return "模版ID不能为空";
                    }
                    templateId = SafeConverter.toLong(cellValue);
                }

                if (cell.getColumnIndex() == 1) {
                    String cellValue = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(cellValue)) {
                        return "章节ID不能为空";
                    }
                    moduleId = SafeConverter.toLong(cellValue);
                }

                if (cell.getColumnIndex() == 3) {
                    String cellValue = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(cellValue)) {
                        return "课程类型不能为空";
                    }
                    contentType = SafeConverter.toInt(cellValue);
                }

            }
            if(skuId!=SafeConverter.toLong(selectedSkuId)){
                return "表中sku与页面选择不对应";
            }
            StudyLesson lessonById = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLesson(skuId);
            if (lessonById == null) {
                return "skuId不正确";
            }
            Map<Long, CourseStructLesson> lessonMap = lessonById.getCourseLessonList().stream().collect(Collectors.toMap(CourseStructLesson::getId, Function.identity()));
            if (!lessonMap.keySet().contains(id)) {
                return "不存在的课节ID";
            }
            if (lessonMap.get(id) != null && lessonMap.get(id).getChapterId() != SafeConverter.toInt(moduleId)) {
                return "课节ID:" + id + "的章节ID不正确";
            }
            if (lessonById.getCourseType() != contentType) {
                return "课程类型不正确";
            }
            if (contentType == 1) {
                ClassicalChineseLessonTemplate classicalChineseLessonTemplate = crmLessonTemplateLoader.loadClassicalChineseTemplate(templateId);
                if (classicalChineseLessonTemplate == null) {
                    return "模版ID错误,类型：语文古文，模版ID:" + templateId;
                }
            } else if (contentType == 2) {
                PictureBookLessonTemplate pictureBookLessonTemplate = crmLessonTemplateLoader.loadPictureBookTemplate(templateId);
                if (pictureBookLessonTemplate == null) {
                    return "模版ID错误,类型：英语绘本，模版ID:" + templateId;
                }
            } else {
                return "暂不支持的导入类型";
            }
        }
        return "true";
    }


}
