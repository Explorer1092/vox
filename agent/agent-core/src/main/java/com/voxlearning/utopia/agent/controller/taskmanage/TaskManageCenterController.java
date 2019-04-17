package com.voxlearning.utopia.agent.controller.taskmanage;


import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.common.BaseExcelService;
import com.voxlearning.utopia.agent.service.taskmanage.AgentTaskManageService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Date;


/**
 * @author deliang.che
 * @since 2018-11-13
 */
@Controller
@RequestMapping(value = "/taskmanage/maintainteacher")
public class TaskManageCenterController extends AbstractAgentController {
    private final static String IMPORT_TASK_MANAGE_TEMPLATE = "/config/templates/import_task_manage_template.xlsx";
    @Inject
    private AgentTaskManageService agentTaskManageService;
    @Inject
    private BaseExcelService baseExcelService;

    @RequestMapping("task_list_page.vpage")
    public String maintainTeacherPage(){
        return "taskmanage/maintain_teacher/list";
    }

    @RequestMapping("task_edit_page.vpage")
    public String maintainTeacherEditPage(){
        return "taskmanage/maintain_teacher/edit";
    }

    @RequestMapping("task_add_page.vpage")
    public String maintainTeacherAddPage(){
        return "taskmanage/maintain_teacher/add";
    }

    /**
     * 任务导入模板
     */
    @RequestMapping(value = "task_import_template.vpage")
    public void importTemplate(){
        try {
            baseExcelService.downloadTemplate(IMPORT_TASK_MANAGE_TEMPLATE, "任务导入模版");
        } catch (Exception e) {
            logger.error("download import_task_manage_template - Excp : {};", e);
        }
    }

    /**
     * 任务新增
     * @return
     */
    @RequestMapping(value = "add_task.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addTask(){
        AuthCurrentUser currentUser = getCurrentUser();
        String title = requestString("title");
        Date endTime = requestDate("endTime","yyyy-MM-dd HH:mm");
        String comment = requestString("comment");
        if (endTime.before(new Date())){
            return MapMessage.errorMessage("任务截止时间不得早于创建时间");
        }
        XSSFWorkbook workbook = baseExcelService.readRequestWorkbook(getRequest(), "sourceExcelFile");
        if (workbook == null) {
            return MapMessage.errorMessage();
        }

        return agentTaskManageService.addTask(currentUser,title,endTime,comment,workbook);
    }

     /**
     * 主任务列表
     * @return
     */
    @RequestMapping(value = "main_task_list.vpage")
    @ResponseBody
    public MapMessage mainTaskList(){
        return MapMessage.successMessage().add("dataList",agentTaskManageService.mainTaskList());
    }

    /**
     * 主任务详情
     * @return
     */
    @RequestMapping(value = "main_task_detail.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage mainTaskDetail(){
        String id = requestString("id");
        if (StringUtils.isBlank(id)){
            return MapMessage.errorMessage("任务ID不正确");
        }
        return agentTaskManageService.mainTaskDetail(id);
    }

    /**
     * 主任务编辑
     * @return
     */
    @RequestMapping(value = "edit_main_task.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editMainTask(){
        String id = requestString("id");
        String title = requestString("title");
        Date endTime = requestDate("endTime","yyyy-MM-dd HH:mm");
        String comment = requestString("comment");
        if (StringUtils.isBlank(id)){
            return MapMessage.errorMessage("任务ID不正确！");
        }
        //修改后的截止时间不得早于当前时间
        Date currentDate = new Date();
        if (endTime != null && endTime.before(currentDate)){
            return MapMessage.errorMessage("截止时间不得早于当前时间！");
        }
        return agentTaskManageService.editMainTask(id,title,endTime,comment);
    }

    /**
     * 删除任务
     * @return
     */
    @RequestMapping(value = "delete_task.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage deleteTask(){
        String id = requestString("id");
        if (StringUtils.isBlank(id)){
            return MapMessage.errorMessage("任务ID不正确");
        }
        return agentTaskManageService.deleteTask(id);
    }
}