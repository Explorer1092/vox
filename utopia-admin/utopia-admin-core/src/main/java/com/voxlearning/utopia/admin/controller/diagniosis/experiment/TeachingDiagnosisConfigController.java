
package com.voxlearning.utopia.admin.controller.diagniosis.experiment;

import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.opmanager.OpManagerAbstractController;
import com.voxlearning.utopia.admin.entity.CourseAnalysisResult;
import com.voxlearning.utopia.admin.service.experiment.ExperimentDiagnosisService;
import com.voxlearning.utopia.business.api.TeachingDiagnosisExperimentService;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.DiagnosisExperimentContent;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.DiagnosisExperimentGroup;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.ExperimentType;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;


@Controller
@RequestMapping("/crm/experiment")
public class TeachingDiagnosisConfigController extends OpManagerAbstractController {

    @ImportService(interfaceClass = TeachingDiagnosisExperimentService.class)
    private TeachingDiagnosisExperimentService teachingDiagnosisExperimentService;

    @Inject
    private ExperimentDiagnosisService experimentDiagnosisService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return "/";
        }
        return "experiment/index";
    }

    @RequestMapping(value = "config/index.vpage", method = RequestMethod.GET)
    public String configIndex(Model model) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return "/";
        }
        return "experiment/configIndex";
    }

    @RequestMapping(value = "config/index/data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage fetchIndexData() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        ExperimentType type = ExperimentType.safe(getRequestParameter("type", "COMMON"));
        if (type == null) {
            return MapMessage.errorMessage("类型无效");
        }

        List<DiagnosisExperimentGroup> experimentGroupList = teachingDiagnosisExperimentService.fetchAllExperimentGroup(type);
        if (CollectionUtils.isNotEmpty(experimentGroupList)) {
            experimentGroupList.forEach(e -> {
                List<CourseAnalysisResult> resultList = experimentDiagnosisService.findCourseAnalysisResultByGroupId(e.getId());
                e.setReported(CollectionUtils.isNotEmpty(resultList) ? true : false);
            });
        }
        return MapMessage.successMessage().add("data", experimentGroupList);
    }

    @RequestMapping(value = "config/group/create.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createGroup() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String name = getRequestString("name");
        ExperimentType type = ExperimentType.safe(getRequestParameter("type", "COMMON"));
        if (StringUtils.isBlank(name) || type == null) {
            return MapMessage.errorMessage("名称不能为空或者类型无效");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("createExperimentGroup")
                    .keys(adminUser.getRealName())
                    .callback(() -> teachingDiagnosisExperimentService.createExperimentGroup(name, type, adminUser.getRealName()))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed createExperimentGroup  name:{}", name,  ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }

    @RequestMapping(value = "config/group/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteGroup() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("实验组id不能为空");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("deleteExperimentGroup")
                    .keys(id)
                    .callback(() -> teachingDiagnosisExperimentService.deleteExperimentGroup(id))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed deleteExperiment  id:{}", id,  ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }


    @RequestMapping(value = "config/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delete() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("实验id名称不能为空");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("deleteExperiment")
                    .keys(id)
                    .callback(() ->  teachingDiagnosisExperimentService.deleteExperiment(id, adminUser.getRealName()))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed deleteExperiment  id:{}", id,  ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }
    @RequestMapping(value = "config/create.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage create() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }

        String name = getRequestString("name");
        String groupId = getRequestString("groupId");
        if (StringUtils.isAnyEmpty(name, groupId)) {
            return MapMessage.errorMessage("字段不能为空");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("createExperiment")
                    .keys(adminUser.getRealName())
                    .callback(() -> teachingDiagnosisExperimentService.createExperiment(name, groupId, adminUser.getRealName()))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed createExperiment  name:{}", name,  ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }

    @RequestMapping(value = "config/detail.vpage", method = RequestMethod.GET)
    public String detail(Model model) {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return "/";
        }
        String id = getRequestString("id");
        model.addAttribute("id", id);
        return "experiment/configDetail";
    }

    @RequestMapping(value = "config/detail/data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage detailData() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("id为空");
        }
        return teachingDiagnosisExperimentService.fetchExperimentInfoById(id);
    }

    @RequestMapping(value = "config/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage update() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String dataJson = getRequestString("data");
        DiagnosisExperimentContent config = JSONObject.parseObject(dataJson, DiagnosisExperimentContent.class);
        if (config == null || StringUtils.isBlank(config.getId())) {
            return MapMessage.errorMessage("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("updateExperiment")
                    .keys(config.getId())
                    .callback(() ->  teachingDiagnosisExperimentService.updateExperiment(config, adminUser.getRealName()))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed updateExperiment  config:{}", config,  ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }
    @RequestMapping(value = "config/status/change.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeStatus() {
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (adminUser == null) {
            return MapMessage.errorMessage("请先登录");
        }
        String id = getRequestString("id");
        String status = getRequestString("status");
        if (StringUtils.isAnyEmpty(id, status) || TeachingDiagnosisExperimentConfig.Status.valueOf(status) == null) {
            return MapMessage.errorMessage("参数异常");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("updateExperimentStatus")
                    .keys(id)
                    .callback(() ->  teachingDiagnosisExperimentService.updateExperimentStatus(id, TeachingDiagnosisExperimentConfig.Status.valueOf(status), adminUser.getRealName()))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage("正在处理，请不要重复提交");
        } catch (Exception ex) {
            logger.error("Failed updateExperimentStatus  id:{}", id,  ex);
            return MapMessage.errorMessage("数据异常，重试一下吧");
        }
    }
}



