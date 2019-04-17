package com.voxlearning.utopia.admin.controller.studyTogether;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.utopia.admin.util.ExportExcel;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherService;
import com.voxlearning.utopia.service.parent.api.StudyTogetherCompetitionService;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyTogetherCompetitionDTO;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.MobileParentStudyTogetherCompetitionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author xuerui.zhang
 * @since  2018/5/8
 **/
@Slf4j
@Controller
@RequestMapping("/opmanager/studyTogether")
public class CrmStudyTogetherCompetitionController extends AbstractStudyTogetherController {

    @ImportService(interfaceClass = StudyTogetherCompetitionService.class)
    private StudyTogetherCompetitionService competitionService;

    @ImportService(interfaceClass = CrmStudyTogetherService.class)
    private CrmStudyTogetherService crmStudyTogetherService;

    /**
     * 视频审核后台:日榜列表
     */
    @RequestMapping(value = "/videodaylist.vpage", method = RequestMethod.GET)
    public String videoDayList(Model model) {
        String type = "day";
        String lessonId = getRequestString("selectLessonId");
        Integer pageNum = getRequestInt("page", 1);
        Integer status = getRequestInt("status", 10);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);

        //获取课程ID列表
        List<String> lessonIds = getAllLessonId();
        if (CollectionUtils.isNotEmpty(lessonIds)) {
            model.addAttribute("lessonIds", lessonIds);
        }
        if (CollectionUtils.isNotEmpty(lessonIds)) {
            model.addAttribute("lessonIds", lessonIds);
        }
        if (StringUtils.isBlank(lessonId) && CollectionUtils.isNotEmpty(lessonIds)) {
            lessonId = lessonIds.get(0);
        }

        Page<MobileParentStudyTogetherCompetitionMapper> resultList = competitionService
                .findWaitCheckVideo(type, pageRequest, lessonId, status);

        model.addAttribute("content", resultList.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("status", status);
        model.addAttribute("select_lessonId", lessonId);

        return "opmanager/studyTogether/videoDayListPage";
    }

    /**
     * 视频审核后台：总榜列表
     */
    @RequestMapping(value = "/videototallist.vpage", method = RequestMethod.GET)
    public String videoList(Model model) {
        String lessonId = getRequestString("selectLessonId");
        Integer pageNum = getRequestInt("page", 1);
        Integer status = getRequestInt("status", 10);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);

        List<String> lessonIds = getAllLessonId();
        if (CollectionUtils.isNotEmpty(lessonIds)) {
            model.addAttribute("lessonIds", lessonIds);
        }
        if (CollectionUtils.isNotEmpty(lessonIds)) {
            model.addAttribute("lessonIds", lessonIds);
        }
        if (StringUtils.isBlank(lessonId) && CollectionUtils.isNotEmpty(lessonIds)) {
            lessonId = lessonIds.get(0);
        }

        Page<MobileParentStudyTogetherCompetitionMapper> resultList = competitionService
                .findWaitCheckVideo("total", pageRequest, lessonId, status);

        model.addAttribute("content", resultList.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("status", status);
        model.addAttribute("select_lessonId", lessonId);

        return "opmanager/studyTogether/videoTotalListPage";
    }

    /**
     * 更改视频审核状态
     */
    @ResponseBody
    @RequestMapping(value = "changestatus.vpage", method = RequestMethod.POST)
    public MapMessage changeGoodsStatus() {
        long studentId = getRequestLong("sid");
        int status = getRequestInt("status");
        String lessonId = getRequestString("lessonId");
        try {
            if (lessonId == null || studentId <= 0L || status <= 0) {
                return MapMessage.errorMessage("无效的参数");
            }
            return competitionService.updateStatusById(studentId, lessonId, status);
        } catch (Exception ex) {
            logger.error("Failed change status, studentId={}, lessonId={}", status, studentId, lessonId);
            return MapMessage.errorMessage("状态变更失败：" + ex.getMessage());
        }
    }

    /**
     * 审核数据 Excel 导出
     */
    @RequestMapping(value = "/export.vpage", method = RequestMethod.POST)
    public void exportExcel(HttpServletRequest request, HttpServletResponse response) {
        String lessonId = "1";
        String[] headers = { "序号", "学院ID", "姓名", "上传时间", "点赞数", "审核状态" };
        String[] cellNames = { "id", "studentId", "studentName", "createDate", "likeNum", "status"};
        List<StudyTogetherCompetitionDTO> dataList = competitionService.getStudyTogetherCompetitionDTO(lessonId);

        ExportExcel<StudyTogetherCompetitionDTO> ex = new ExportExcel<>();
        byte[] bytes = ex.exportExcel("视频竞赛参赛信息", headers, cellNames, dataList, "yyyy-MM-dd HH:mm:ss");
        DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String name1 = "视频竞赛参赛信息-" + sdf.format(new Date()) + ".xls";
        String fileName;

        try {
            String upperCase = request.getHeader("User-Agent").toUpperCase();
            if (upperCase.indexOf("MSIE") > 0 || upperCase.indexOf("EDGE") > 0 || upperCase.indexOf("RV:11.0") > 0) {
                fileName = URLEncoder.encode(name1, "UTF-8");
            } else {
                fileName = new String(name1.getBytes("UTF-8"), "ISO8859-1");
            }
            response.setContentType("application/x-msdownload");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
