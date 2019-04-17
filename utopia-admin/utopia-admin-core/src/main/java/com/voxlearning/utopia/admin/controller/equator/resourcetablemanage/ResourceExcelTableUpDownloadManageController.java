package com.voxlearning.utopia.admin.controller.equator.resourcetablemanage;

import com.google.zxing.common.detector.MathUtils;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.FileUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.equator.service.configuration.api.annotation.XmlComment;
import com.voxlearning.equator.service.configuration.client.ResourceExcelTableServiceClient;
import com.voxlearning.equator.service.configuration.resourcetablemanage.annotation.ResourceDownloadType;
import com.voxlearning.equator.service.configuration.resourcetablemanage.entity.ResourceTableDigest;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import jxl.Cell;
import jxl.CellView;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2018/6/28.
 */
@Controller
@RequestMapping("/equator/config/resourcetablemanage/")
public class ResourceExcelTableUpDownloadManageController extends AbstractEquatorController {
    @Inject
    private ResourceExcelTableServiceClient resourceExcelTableServiceClient;

    //上传excel文件
    @RequestMapping(value = "uploadexceldata.vpage", method = {RequestMethod.POST})
    public String uploadExcelData(Model model) {
        String path = "redirect:/equator/config/resourcetablemanage/index.vpage";
        String digestIdForTableExcelName = getRequestString("digestIdForTableExcelName").trim();

        //校验
        if (StringUtils.isBlank(digestIdForTableExcelName)) {
            model.addAttribute("error", "digest id is empty");
            return path;
        }
        ResourceTableDigest resourceTableDigest = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableDigestByIdFromDb(digestIdForTableExcelName).getUninterruptibly();
        if (resourceTableDigest == null) {
            model.addAttribute("error", "找不到配置");
            return path;
        }
        path = path + "?category=" + resourceTableDigest.getCategory();

        //校验request
        if (!(getRequest() instanceof MultipartHttpServletRequest)) {
            model.addAttribute("error", "request不是MultipartHttpServletRequest类型");
            return path;
        }
        MultipartHttpServletRequest request = (MultipartHttpServletRequest) getRequest();
        MultipartFile excelFile = request.getFile("excelFile");
        if (excelFile == null || excelFile.isEmpty()) {
            model.addAttribute("error", "没有上传的excelFile");
            return path;
        }

        //校验文件名和版本号
        String excelFileName = excelFile.getOriginalFilename();
        String rightExcelName = resourceTableDigest.getTableExcelName() + "_" + resourceTableDigest.getVersion() + ".xls";
        if (!StringUtils.equals(excelFileName, rightExcelName)) {
            model.addAttribute("error", "文件名和版本不匹配,选择的文件是" + rightExcelName + ",上传的文件是" + excelFileName);
            return path;
        }

        if (!Arrays.asList(ResourceDownloadType.CDN.name(), ResourceDownloadType.DATABASE.name()).contains(resourceTableDigest.getResourceType())) {
            model.addAttribute("error", "文件类型有误，不是CDN和DATABASE");
            return path;
        }


        String lockKey = "ResourceExcelTableUpDownloadManageController_uploadExcelData_" + rightExcelName;
        try {
            AtomicLockManager.getInstance().acquireLock(lockKey);

            //填充excel文件的数据到excelDataList
            List<List<String>> excelDataList = new ArrayList<>();
            Workbook workbook = Workbook.getWorkbook(excelFile.getInputStream());// 得到Excel文件
            Sheet sheet = workbook.getSheet(0);// Excel中的工作表 下标从0开始
            int row = sheet.getRows(); // 工作表共有的行
            for (int i = 0; i < row; i++) {
                List<String> rowData = new ArrayList<>();
                Cell[] cells = sheet.getRow(i);
                for (Cell cell : cells) {
                    String value = SafeConverter.toString(cell.getContents()).trim();
                    if (StringUtils.isBlank(value)) {
                        value = "";
                    } else if ("NULL".equalsIgnoreCase(value)) {
                        value = "";
                    }
                    rowData.add(value);
                }
                if (rowData.stream().filter(StringUtils::isNotBlank).findFirst().orElse(null) == null) {
                    throw new Exception("不能包含空行，请先删除底部空行");
                }
                excelDataList.add(rowData);
            }
            if (excelDataList.size() < 2) {
                model.addAttribute("error", "数据不齐");
                return path;
            }

            //校验字段和字段描述
            List<String> header = excelDataList.get(0);//字段描述
            List<String> keys = excelDataList.get(1);//字段name
            if (new HashSet<>(keys).size() != keys.size()) {
                model.addAttribute("error", "字段标识列不能有重复的");
                return path;
            }
            if (keys.stream().filter(StringUtils::isBlank).findFirst().orElse(null) != null) {
                model.addAttribute("error", "唯一标识不能为空");
                return path;
            }
            if (excelDataList.stream().filter(p -> p.size() > keys.size()).findFirst().orElse(null) != null) {
                model.addAttribute("error", "数据行大小不能超过字段行大小");
                return path;
            }

            Map<String, String> fieldDesc = new HashMap<>();
            for (int i = 0; i < keys.size(); i++) {
                fieldDesc.put(keys.get(i), header.get(i));
            }

            //获取excel中的正文数据
            List<Map<String, Object>> resultData = excelDataList.subList(2, excelDataList.size())
                    .stream()
                    .map(oneRecord -> {
                        Map<String, Object> result = new HashMap<>();
                        for (int i = 0; i < oneRecord.size(); i++) {
                            result.put(keys.get(i), oneRecord.get(i));
                        }
                        return result;
                    }).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(resultData)) {
                model.addAttribute("error", "没有数据，无需上传");
                return path;
            }

            if (StringUtils.equals(resourceTableDigest.getResourceType(), ResourceDownloadType.DATABASE.name())) {
                //保存的db中
                MapMessage mapMessage = resourceExcelTableServiceClient.getResourceExcelTableService().upsertExcelDataList(digestIdForTableExcelName, keys, fieldDesc, resultData);
                if (!mapMessage.isSuccess()) {
                    model.addAttribute("error", mapMessage.getInfo());
                    return path;
                }
                return path;
            } else {
                //上传文件到cdn
                File file = null;
                String url;
                try {
                    file = new File(System.currentTimeMillis() + ".json");
                    FileUtils.writeStringToFile(file, JsonUtils.toJson(resultData), "UTF-8", false);
                    url = uploadFile(file);
                } catch (Exception e) {
                    model.addAttribute("error", "上传文件" + excelFileName + "到cdn失败");
                    return path;
                } finally {
                    if (file != null) {
                        file.delete();
                    }
                }
                if (StringUtils.isBlank(url)) {
                    model.addAttribute("error", "上传文件" + excelFileName + "到cdn失败");
                    return path;
                }


                //更新表的摘要信息
                resourceTableDigest.setVersion(SafeConverter.toLong(resourceTableDigest.getVersion()) + 1);
                resourceTableDigest.setUrl(url);
                resourceTableDigest.setFieldDesc(fieldDesc);
                resourceTableDigest.setTableKeys(keys);
                MapMessage mapMessage = resourceExcelTableServiceClient.getResourceExcelTableService().replaceResourceTableDigest(resourceTableDigest);
                if (!mapMessage.isSuccess()) {
                    model.addAttribute("error", mapMessage.getInfo());
                    return path;
                }
                return path;
            }
        } catch (CannotAcquireLockException e) {
            model.addAttribute("error", "其他人正在操作，请稍后重试");
            return path;
        } catch (Exception e) {
            if (e instanceof BiffException) {
                model.addAttribute("error", "请将文档转换为2003xls格式");
            } else {
                model.addAttribute("error", e.getMessage());
            }
            return path;
        } finally {
            AtomicLockManager.getInstance().releaseLock(lockKey);
        }
    }


    //下载excel文件
    @RequestMapping(value = "downloadexcel.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public void downloadExcel() throws Exception {
        String resourceTypeStr = getRequestString("resourceType").trim();
        String digestId = getRequestString("digestId").trim();


        ResourceTableDigest resourceTableDigest = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableDigestByIdFromDb(digestId).getUninterruptibly();
        if (resourceTableDigest == null || SafeConverter.toBoolean(resourceTableDigest.getDisabled())) {
            getResponse().setContentType("text/html;charset=utf-8");
            getResponse().setCharacterEncoding("UTF-8");
            getResponse().getWriter().println("找不到对应配置");
            return;
        }


        if (!Arrays.asList(ResourceDownloadType.CDN.name(), ResourceDownloadType.DATABASE.name()).contains(resourceTableDigest.getResourceType())) {
            getResponse().setContentType("text/html;charset=utf-8");
            getResponse().setCharacterEncoding("UTF-8");
            getResponse().getWriter().println("资源类型有误");
            return;
        }

        if (StringUtils.equals(resourceTypeStr, ResourceDownloadType.CDN.name()) && StringUtils.isBlank(resourceTableDigest.getUrl())) {
            getResponse().setContentType("text/html;charset=utf-8");
            getResponse().setCharacterEncoding("UTF-8");
            getResponse().getWriter().println("CDN文件需要初始化上传");
            return;
        }

        if (StringUtils.equals(resourceTypeStr, ResourceDownloadType.DATABASE.name()) && StringUtils.isBlank(resourceTableDigest.getClassTypeName())) {
            getResponse().setContentType("text/html;charset=utf-8");
            getResponse().setCharacterEncoding("UTF-8");
            getResponse().getWriter().println("读取配置错误，classTypeName不能为空");
            return;
        }

        if (StringUtils.equals(resourceTypeStr, ResourceDownloadType.CDN.name())) {
            List<List<String>> excelDataList = wrapCdnExcelDataList(resourceTableDigest);
            downloadWrappedExcel(resourceTableDigest.getTableExcelName() + "_" + resourceTableDigest.getVersion() + ".xls", excelDataList);
        } else if (StringUtils.equals(resourceTypeStr, ResourceDownloadType.DATABASE.name())) {
            String classTypeName = resourceTableDigest.getClassTypeName();
            Class configClass = null;
            try {
                configClass = resourceExcelTableServiceClient.fetchAllowedConfigClasses()
                        .stream()
                        .filter(aClass -> StringUtils.equals(aClass.getTypeName(), classTypeName))
                        .findFirst().orElse(null);
            } catch (Exception e) {
                getResponse().setContentType("text/html;charset=utf-8");
                getResponse().setCharacterEncoding("UTF-8");
                getResponse().getWriter().println("获取配置有误，请升级依赖的jar版本");
                return;
            }

            if (configClass == null) {
                getResponse().setContentType("text/html;charset=utf-8");
                getResponse().setCharacterEncoding("UTF-8");
                getResponse().getWriter().println("读取配置错误，根据classTypeName=" + classTypeName + "定位不到Class");
                return;
            }

            String identifyingFieldName = "id";
            for (Field field : configClass.getDeclaredFields()) {
                if (field.getAnnotation(DocumentId.class) != null) {
                    identifyingFieldName = field.getName();
                }
            }
            //configClass对应的数据
            String finalIdentifyingFieldName = identifyingFieldName;
            List<Map<String, Object>> recordsList = resourceExcelTableServiceClient.getResourceExcelTableService()
                    .findAllRecordsByConfigClassFromDb(configClass)
                    .getUninterruptibly();


            List<List<String>> excelDataList = wrapDbExcelDataList(configClass, resourceTableDigest, recordsList,identifyingFieldName);

            downloadWrappedExcel(resourceTableDigest.getTableExcelName() + "_" + resourceTableDigest.getVersion() + ".xls", excelDataList);
        }

    }

    private List<Map<String, Object>> getExcelTableBySort(List<Map<String, Object>> resourceList,String sortField) {

        return resourceList.stream()
                .sorted((o1, o2) -> {
                    Object o1Obj = o1.get(sortField);
                    Object o2Obj = o2.get(sortField);
                    if (o1Obj == null || o2Obj == null) {
                        return 0;
                    }
                    if ((o1Obj instanceof Integer && o2Obj instanceof Integer)
                            || (o1Obj instanceof Double && o2Obj instanceof Double)
                            || (o1Obj instanceof Float && o2Obj instanceof Float)
                            || (o1Obj instanceof Long && o2Obj instanceof Long)) {
                        return SafeConverter.toInt(o1Obj) - SafeConverter.toInt(o2Obj);
                    }
                    String o1Str = SafeConverter.toString(o1Obj);
                    String o2Str = SafeConverter.toString(o2Obj);
                    //数字_数字_数字拼接的字符串，按数字来排序
                    if (StringUtils.isNumeric(o1Str.replaceAll("_", "")) && StringUtils.isNumeric(o2Str.replaceAll("_", ""))) {
                        String[] arr1 = o1Str.split("_");
                        String[] arr2 = o2Str.split("_");
                        double sortNum1 = 0, sortNum2 = 0;
                        for (int i = 0; i < arr1.length; i++) {
                            sortNum1 += SafeConverter.toInt(arr1[i]) * Math.pow(10, 10 - i * 2);
                        }
                        for (int i = 0; i < arr2.length; i++) {
                            sortNum2 += SafeConverter.toInt(arr2[i]) * Math.pow(10, 10 - i * 2);
                        }
                        return (int) (sortNum1 - sortNum2);
                    }
                    return o1Str.compareTo(o2Str);
                }).collect(Collectors.toList());
    }


    //查看资源表的数据
    @RequestMapping(value = "tabledatainfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String tableDateInfo(Model model) {
        String path = "equator/config/resourcetablemanage/tabledatainfo";
        String digestId = getRequestString("digestId").trim();
        if (!StringUtils.isBlank(getRequestString("error"))) {
            model.addAttribute("error", getRequestString("error"));
            return path;
        }
        if (StringUtils.isBlank(digestId)) {
            model.addAttribute("error", "digestId参数为空");
            return path;
        }

        ResourceTableDigest resourceTableDigest = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableDigestByIdFromDb(digestId).getUninterruptibly();
        if (resourceTableDigest == null) {
            model.addAttribute("error", "找不到对应配置" + digestId);
            return path;
        }
        if (!Arrays.asList(ResourceDownloadType.CDN.name(), ResourceDownloadType.DATABASE.name()).contains(resourceTableDigest.getResourceType())) {
            model.addAttribute("error", "资源类型有误,digestId=" + digestId);
            return path;
        }
        if (StringUtils.equals(resourceTableDigest.getResourceType(), ResourceDownloadType.CDN.name()) && StringUtils.isBlank(resourceTableDigest.getUrl())) {
            model.addAttribute("error", "CDN文件还未初始化上传,暂时没有数据,digestId=" + digestId);
            return path;
        }
        if (StringUtils.equals(resourceTableDigest.getResourceType(), ResourceDownloadType.DATABASE.name()) && StringUtils.isBlank(resourceTableDigest.getClassTypeName())) {
            model.addAttribute("error", "读取配置错误，classTypeName不能为空,digestId=" + digestId);
            return path;
        }

        List<List<String>> excelDataListList;
        if (StringUtils.equals(resourceTableDigest.getResourceType(), ResourceDownloadType.CDN.name())) {
            excelDataListList = wrapCdnExcelDataList(resourceTableDigest);
        } else {
            String classTypeName = resourceTableDigest.getClassTypeName();
            Class configClass;
            try {
                configClass = resourceExcelTableServiceClient.fetchAllowedConfigClasses()
                        .stream()
                        .filter(aClass -> StringUtils.equals(aClass.getTypeName(), classTypeName))
                        .findFirst().orElse(null);
            } catch (Exception e) {
                model.addAttribute("error", "获取配置有误，请升级依赖的jar版本,digestId=" + digestId);
                return path;
            }

            if (configClass == null) {
                model.addAttribute("error", "读取配置错误，根据classTypeName=" + classTypeName + "定位不到Class");
                return path;
            }
            String identifyingFieldName = "id";
            for (Field field : configClass.getDeclaredFields()) {
                if (field.getAnnotation(DocumentId.class) != null) {
                    identifyingFieldName = field.getName();
                }
            }
            List<Map<String, Object>> recordsList = resourceExcelTableServiceClient.getResourceExcelTableService().findAllRecordsByConfigClassFromDb(configClass).getUninterruptibly();
            excelDataListList = wrapDbExcelDataList(configClass, resourceTableDigest, recordsList,identifyingFieldName);
        }

        model.addAttribute("resourceTableDigest", resourceTableDigest);

        if (excelDataListList.size() >= 1) {
            model.addAttribute("filedKeyDescList", excelDataListList.get(0));//字段描述
        }
        model.addAttribute("excelDataListList", excelDataListList);

        return path;
    }

    private List<List<String>> wrapCdnExcelDataList(ResourceTableDigest resourceTableDigest) {
        String httpResult = HttpRequestExecutor.defaultInstance()
                .get(resourceTableDigest.getUrl())
                .execute()
                .getResponseString();
        List<List<String>> excelDataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(resourceTableDigest.getTableKeys())) {
            //excel前两行的字段的描述和name
            List<String> excelKeys = resourceTableDigest.getTableKeys();
            Map<String, String> filedDesc = resourceTableDigest.getFieldDesc();
            List<String> keyDescs = excelKeys.stream()
                    .map(p -> filedDesc != null && filedDesc.containsKey(p) ? filedDesc.get(p) : "无描述")
                    .collect(Collectors.toList());


            List<Map> listData = JsonUtils.fromJsonToList(httpResult, Map.class);
            excelDataList.add(keyDescs);
            excelDataList.add(excelKeys);
            for (Map map : listData) {
                List<String> midList = excelKeys.stream()
                        .map(p -> SafeConverter.toString(map.get(p)))
                        .collect(Collectors.toList());
                excelDataList.add(midList);
            }
        }

        return excelDataList;
    }

    private List<List<String>> wrapDbExcelDataList(Class configClass, ResourceTableDigest resourceTableDigest, List<Map<String, Object>> recordsList,String sortField) {
        //keys 和表头 排序等
        List<Field> fieldList = Arrays.stream(configClass.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .filter(field -> field.getAnnotation(DocumentCreateTimestamp.class) == null)
                .filter(field -> field.getAnnotation(DocumentUpdateTimestamp.class) == null)
                .filter(field -> !"serialVersionUID".equals(field.getName()))
                .filter(field -> !"disabled".equalsIgnoreCase(field.getName()))
                .collect(Collectors.toList());
        List<String> excelKeys = fieldList.stream().map(Field::getName).collect(Collectors.toList());
        excelKeys.add("disabled");

        Map<String, String> filedDesc = new HashMap<>();
        if (resourceTableDigest.getFieldDesc() != null) {
            filedDesc.putAll(resourceTableDigest.getFieldDesc());
        }
        for (Field field : fieldList) {
            XmlComment xmlComment = field.getAnnotation(XmlComment.class);
            if (xmlComment != null && StringUtils.isNotBlank(xmlComment.value()) && !filedDesc.containsKey(field.getName())) {
                filedDesc.put(field.getName(), xmlComment.value());
            }
        }

        List<String> keyDescs = excelKeys.stream()
                .map(p -> {
                    if (p.equals("disabled") && !filedDesc.containsKey("disabled")) {
                        return "是否伪删除,true表示删除不在使用";
                    }
                    return filedDesc.getOrDefault(p, "无描述");
                })
                .collect(Collectors.toList());

        List<List<String>> excelDataList = new ArrayList<>();
        excelDataList.add(0, excelKeys);
        excelDataList.add(0, keyDescs);
        List<Map<String, Object>> sortList = getExcelTableBySort(recordsList, sortField);
        sortList.forEach(record -> {
            List<String> rowData = new ArrayList<>();
            excelKeys.forEach(key -> rowData.add(SafeConverter.toString(record.get(key), "")));
            excelDataList.add(rowData);
        });

        return excelDataList;
    }

    private void downloadWrappedExcel(String excelName, List<List<String>> excelDataList) {
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

            for (int i = 0; i < excelDataList.size(); i++) {
                List<String> rowData = excelDataList.get(i);
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
            response.setHeader("Content-disposition", "attachment; filename=" + attachmentFilenameEncoding(excelName, getRequest()));
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


    @RequestMapping(value = "difftabledatainfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String diffTableDateInfo(Model model) {
        String path = "equator/config/resourcetablemanage/difftabledatainfo";
        String digestId = getRequestString("digestId").trim();
        if (!StringUtils.isBlank(getRequestString("error"))) {
            model.addAttribute("error", getRequestString("error"));
            return path;
        }


        String identifyingFieldName = "id";

        ResourceTableDigest resourceTableDigest = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableDigestByIdFromDb(digestId).getUninterruptibly();
        if (resourceTableDigest == null) {
            model.addAttribute("error", "resourceTableDigest不存在,digestId=" + digestId);
            return path;
        }
        List<List<String>> excelDataListList;
        if (StringUtils.equals(resourceTableDigest.getResourceType(), ResourceDownloadType.CDN.name())) {
            excelDataListList = wrapCdnExcelDataList(resourceTableDigest);
        } else {
            String classTypeName = resourceTableDigest.getClassTypeName();
            Class configClass;
            try {
                configClass = resourceExcelTableServiceClient.fetchAllowedConfigClasses()
                        .stream()
                        .filter(aClass -> StringUtils.equals(aClass.getTypeName(), classTypeName))
                        .findFirst().orElse(null);
            } catch (Exception e) {
                model.addAttribute("error", "读取配置错误，根据classTypeName=" + classTypeName + "定位不到Class ,digestId=" + digestId);
                return path;
            }


            if (configClass == null) {
                model.addAttribute("error", "读取配置错误，根据classTypeName=" + classTypeName + "定位不到Class ,digestId=" + digestId);
                return path;
            }
            for (Field field : configClass.getDeclaredFields()) {
                if (field.getAnnotation(DocumentId.class) != null) {
                    identifyingFieldName = field.getName();
                }
            }

            List<Map<String, Object>> recordsList = resourceExcelTableServiceClient.getResourceExcelTableService().findAllRecordsByConfigClassFromDb(configClass).getUninterruptibly();
            excelDataListList = wrapDbExcelDataList(configClass, resourceTableDigest, recordsList,identifyingFieldName);
        }


        List<List> testExcelDataList = RuntimeMode.current().le(Mode.TEST) ? new ArrayList<>(excelDataListList) : null;
        List<List> stagingExcelDataList = RuntimeMode.current().ge(Mode.STAGING) ? new ArrayList<>(excelDataListList) : null;

        String category = resourceTableDigest.getCategory();
        String tableName = resourceTableDigest.getTableName();
        if (testExcelDataList == null) {
            String testResponseString = HttpRequestExecutor.defaultInstance()
                    .get("http://e.test.17zuoye.net/student/newgroingworld/equator/config/resourcetablemanage/remotetabledatainfo.vpage?category=" + category + "&tableName=" + tableName)
                    .execute()
                    .getResponseString();
            Map<String, Object> testResultMap = JsonUtils.fromJson(testResponseString);
            if (testResultMap == null || !MapMessage.of(testResultMap).isSuccess()) {
                model.addAttribute("error", "远程获取Test环境的数据错误");
                return path;
            }
            testExcelDataList = (List<List>) testResultMap.get("excelDataListList");
        }

        if (stagingExcelDataList == null) {
            String url = "http://e.staging.17zuoye.net/student/newgroingworld/equator/config/resourcetablemanage/remotetabledatainfo.vpage?category=" + category + "&tableName=" + tableName;
            String stagingResponseString = HttpRequestExecutor.defaultInstance()
                    .get(url)
                    .execute()
                    .getResponseString();
            Map<String, Object> stagingResultMap = JsonUtils.fromJson(stagingResponseString);
            if (stagingResultMap == null || !MapMessage.of(stagingResultMap).isSuccess()) {
                model.addAttribute("error", "远程获取Staging环境的数据错误");
                return path;
            }
            stagingExcelDataList = (List<List>) stagingResultMap.get("excelDataListList");
        }


        Map<String, Map<String, String>> testDataMap = convertExcelData(identifyingFieldName, testExcelDataList);
        Map<String, Map<String, String>> stagingDataMap = convertExcelData(identifyingFieldName, stagingExcelDataList);
        if (MapUtils.isEmpty(testDataMap) || MapUtils.isEmpty(stagingDataMap)) {
            model.addAttribute("error", "数据缺失,数据格式错误(没有" + identifyingFieldName + "列),或者数据有待初始化");
            return path;
        }


        List allFieldNames = new ArrayList(testExcelDataList.get(1));
        for (Object name : stagingExcelDataList.get(1)) {
            if (!allFieldNames.contains(name)) {
                allFieldNames.add(name);
            }
        }


        //改变的数据
        List<Map<String, List<String>>> changeDataList = new ArrayList<>();
        String finalIdentifyingFieldName = identifyingFieldName;
        testDataMap.forEach((identifyingName, testData) -> {
            Map<String, String> stagingData = stagingDataMap.get(identifyingName);
            if (testData != null && stagingData != null) {
                List<String> keyList = new ArrayList<>();
                List<String> testDiffList = new ArrayList<>();
                List<String> stagingDiffList = new ArrayList<>();

                boolean hasDiff = false;
                keyList.add(finalIdentifyingFieldName);
                testDiffList.add(testData.get(finalIdentifyingFieldName));
                stagingDiffList.add(stagingData.get(finalIdentifyingFieldName));

                for (Object fieldName : allFieldNames) {
                    String key = SafeConverter.toString(fieldName);
                    testData.get(key);
                    stagingData.get(key);
                    if (!Objects.equals(testData.get(key), stagingData.get(key))) {
                        hasDiff = true;
                        keyList.add(key);
                        testDiffList.add(testData.get(key));
                        stagingDiffList.add(stagingData.get(key));
                    }
                }
                if (hasDiff) {
                    Map<String, List<String>> changeData = new HashMap<>();
                    changeData.put("keys", keyList);
                    changeData.put("testCol", testDiffList);
                    changeData.put("onlineCol", stagingDiffList);
                    changeDataList.add(changeData);
                }
            }
        });


        //测试环境增加了数据线上没有
        List<Map<String, String>> newDataList = testDataMap.keySet().stream()
                .filter(p -> !stagingDataMap.keySet().contains(p))
                .map(testDataMap::get)
                .collect(Collectors.toList());

        //测试环境没有线上增加了数据
        List<Map<String, String>> exceptionDataList = stagingDataMap.keySet().stream()
                .filter(p -> !testDataMap.keySet().contains(p))
                .map(stagingDataMap::get)
                .collect(Collectors.toList());


        model.addAttribute("resourceTableDigest", resourceTableDigest);

        model.addAttribute("changeDataList", changeDataList);

        if (newDataList.size() > 0) {
            model.addAttribute("newData", JsonUtils.toJson(newDataList));
        }
        if (exceptionDataList.size() > 0) {
            model.addAttribute("exceptionData", JsonUtils.toJson(exceptionDataList));
        }

        return path;
    }

    private Map<String, Map<String, String>> convertExcelData(String identifyingFieldName, List<List> excelDataList) {
        if (excelDataList == null || excelDataList.size() <= 2) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, String>> result = new HashMap<>();
        List filedNameList = excelDataList.get(1);
        for (List dataList : excelDataList.subList(2, excelDataList.size())) {
            Map<String, String> tempDataMap = new HashMap<>();
            for (int index = 0; index < filedNameList.size(); index++) {
                tempDataMap.put(SafeConverter.toString(filedNameList.get(index)), SafeConverter.toString(dataList.get(index)));
            }

            if (tempDataMap.containsKey(identifyingFieldName)) {
                result.put(tempDataMap.get(identifyingFieldName), tempDataMap);
            }
        }


        return result;
    }
}
