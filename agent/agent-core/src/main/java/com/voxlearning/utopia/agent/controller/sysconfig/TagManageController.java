package com.voxlearning.utopia.agent.controller.sysconfig;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.bean.tag.AgentTagTargetData;
import com.voxlearning.utopia.agent.constants.AgentTagSubType;
import com.voxlearning.utopia.agent.constants.AgentTagType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.AgentTagService;
import com.voxlearning.utopia.agent.service.common.BaseExcelService;
import com.voxlearning.utopia.agent.service.mobile.resource.TeacherResourceService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import lombok.Cleanup;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * 标签管理
 * @author deliang.che
 * @since 2019/3/19
 */
@Controller
@RequestMapping("/sysconfig/tag")
public class TagManageController extends AbstractAgentController {
    private static final int BYTES_BUFFER_SIZE = 1024 * 8;
    private final static String IMPORT_TAG_TEMPLATE = "/config/templates/import_tag_template.xlsx";

    @Inject
    private AgentTagService agentTagService;
    @Inject
    private BaseExcelService baseExcelService;
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private TeacherResourceService teacherResourceService;

    @RequestMapping(value = "index.vpage",method = RequestMethod.GET)
    @OperationCode("15cc130b4c714b8a")
    public String index(Model model){
        return "/sysconfig/tag_manage/index";
    }

    @RequestMapping(value = "detail.vpage",method = RequestMethod.GET)
    @OperationCode("795544a8f2f7402c")
    public String detail(Model model){
        model.addAttribute("provinces", agentTagService.getAllProvincePinYin());
        return "sysconfig/tag_manage/look_detail";
    }

    /**
     * 标签列表
     * @return
     */
    @RequestMapping(value = "tag_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    MapMessage tagList(){
        return MapMessage.successMessage().add("dataList",agentTagService.getAllTagList());
    }

    /**
     * 编辑标签
     * @return
     */
    @RequestMapping(value = "edit_tag.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage editTag(){
        Long id = getRequestLong("id");
        String tagType = getRequestString("tagType");
        Integer tagSubTypeCode = getRequestInt("tagSubTypeCode");
        String name = getRequestString("name");
        boolean isVisible = getRequestBool("isVisible");
        int sortNum = getRequestInt("sortNum");
        AgentTagType agentTagType = AgentTagType.nameOf(tagType);
        if (agentTagType == null){
            return MapMessage.errorMessage("标签类型不正确！");
        }
        if (StringUtils.isBlank(name)){
            return MapMessage.errorMessage("标签名称不为空！");
        }
        name = StringUtils.trim(name);

        AgentTagSubType tagSubType = null;
        if (tagSubTypeCode > 0){
            tagSubType = AgentTagSubType.codeOf(tagSubTypeCode);
        }
        if (id == 0L){
            return agentTagService.addTag(agentTagType,tagSubType,name,isVisible,sortNum);
        }else {
            return agentTagService.editTag(id,agentTagType,tagSubType,name,isVisible,sortNum);
        }
    }

    /**
     * 删除标签
     * @return
     */
    @RequestMapping(value = "delete_tag.vpage", method = RequestMethod.GET)
    @ResponseBody
    MapMessage deleteTag(){
        Long id = getRequestLong("id");
        if (id == 0L){
            return MapMessage.errorMessage("标签ID不正确！");
        }
        return agentTagService.deleteTag(id);
    }

    /**
     * 标签详情
     * @return
     */
    @RequestMapping(value = "tag_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    MapMessage tagDetail(){
        Long id = getRequestLong("id");
        if (id == 0L){
            return MapMessage.errorMessage("标签ID不正确！");
        }
        return MapMessage.successMessage().add("dataMap",agentTagService.tagDetail(id));
    }

    /**
     * 下载标签导入模版
     */
    @RequestMapping(value = "download_import_tag_template.vpage", method = RequestMethod.GET)
    public void downloadImportTagTemplate() {
        try {
            Resource resource = new ClassPathResource(IMPORT_TAG_TEMPLATE);
            if (!resource.exists()) {
                logger.error("download import tag template - template not exists ");
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(in, out);
            String  fileName = "标签导入模版.xlsx";
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("download import tag Template - Excp : {};", e);
        }
    }

    private static void write(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[BYTES_BUFFER_SIZE];
        int size;
        while ((size = in.read(buffer, 0, buffer.length)) > 0) {
            out.write(buffer, 0, size);
        }
        out.flush();
    }

    /**
     * 标签导入
     * @param request
     * @return
     */
    @RequestMapping(value = "import_tag_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importTagInfo(HttpServletRequest request) {
        Long tagId = getRequestLong("tagId");
        if (tagId == 0L){
            return MapMessage.errorMessage("标签ID不正确！");
        }
        XSSFWorkbook workbook = baseExcelService.readRequestWorkbook(request,"sourceFile");
        return agentTagService.importTagInfo(workbook,tagId);
    }

    /**
     * 导出标签信息
     * @param response
     */
    @RequestMapping(value = "export_tag_info.vpage", method = RequestMethod.GET)
    public void exportTagInfo(HttpServletResponse response) {
        Long tagId = getRequestLong("tagId");
        try {
            String filename = "标签-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";

            List<AgentTagTargetData> dataList = agentTagService.getTagTargetDataList(tagId);
            //导出Excel文件
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            agentTagService.exportTagData(workbook,dataList);
            @Cleanup org.apache.commons.io.output.ByteArrayOutputStream outStream = new org.apache.commons.io.output.ByteArrayOutputStream();
            workbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
            outStream.close();
            workbook.dispose();
        }catch (Exception e){
            logger.error("error info: ",e);
            emailServiceClient.createPlainEmail()
                    .body("error info: "+e)
                    .subject("标签导出异常【" + RuntimeMode.current().getStageMode() + "】")
                    .to("deliang.che@17zuoye.com")
                    .send();
        }
    }

    /**
     * 标签关联对象列表
     * @return
     */
    @RequestMapping(value = "tag_target_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage tagTargetList() {
        Long tagId = getRequestLong("tagId");
        Integer provinceCode = getRequestInt("provinceCode");
        Integer cityCode = getRequestInt("cityCode");
        Integer countyCode = getRequestInt("countyCode");

        return MapMessage.successMessage().add("dataList",agentTagService.tagTargetDataList(tagId,provinceCode,cityCode,countyCode));
    }

    /**
     * 删除标签关联对象
     * @return
     */
    @RequestMapping(value = "delete_tag_target.vpage", method = RequestMethod.GET)
    @ResponseBody
    MapMessage deleteTagTarget(){
        Long tagId = getRequestLong("tagId");
        List<String> ids = Arrays.asList(StringUtils.split(getRequestString("ids"), ","));
        if (CollectionUtils.isEmpty(ids)){
            return MapMessage.errorMessage("请选择要删除的ID!");
        }
        return agentTagService.deleteTagTarget(tagId,ids);
    }

    /**
     * 标签子类别列表
     * @return
     */
    @RequestMapping(value = "tag_sub_type_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    MapMessage tagSubTypeList(){
        List<AgentTagSubType> tagSubTypes = AgentTagSubType.fetchTagSubTypes();
        List<Map<String,Object>> tagSubTypeList = new ArrayList<>();
        tagSubTypes.forEach(item -> {
            Map<String,Object> dataMap = new HashMap<>();
            dataMap.put("code",item.getCode());
            dataMap.put("desc",item.getDesc());
            tagSubTypeList.add(dataMap);
        });
        return MapMessage.successMessage().add("tagSubTypeList",tagSubTypeList);
    }


    /**
     * 老师职务标签
     * @return
     */
    @RequestMapping("teacher_position_tag_info.vpage")
    @ResponseBody
    public MapMessage teacherPositionTagInfo(){
        Long teacherId = getRequestLong("teacherId");
        return agentTagService.getTeacherPositionTagInfo(teacherId);
    }

    /**
     * 保存老师标签
     * @return
     */
    @RequestMapping(value = "save_teacher_tags.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveTeacherTags(){
        Long teacherId = getRequestLong("teacherId");
        List<String> tagStrIds = Arrays.asList(getRequestString("tagIds").split(","));
        List<Long> tagIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(tagStrIds)){
            tagStrIds.forEach(p -> tagIds.add(SafeConverter.toLong(p)));
        }
        //公私海场景，判断该用户是否有权限操作，若无权限，返回老师负责人员
        MapMessage mapMessage = teacherResourceService.teacherAuthorityMessage(getCurrentUserId(), teacherId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()){
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("teacherManager")))){
                return MapMessage.errorMessage(StringUtils.formatMessage("该老师由{}负责，暂无操作权限",mapMessage.get("teacherManager")));
            }else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }
        return agentTagService.saveTeacherTags(teacherId,tagIds);
    }

//    /**
//     * 迁移历史数据
//     * @return
//     */
//    @RequestMapping(value = "move_history_data.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage moveHistoryData(){
//        agentTagService.moveHistoryData();
//        return MapMessage.successMessage();
//    }
}
