package com.voxlearning.utopia.agent.controller.sysconfig;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.bean.AgentPaymentsData;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.sysconfig.AgentPerformanceConfigService;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

/**
 * Agent 指标控制器
 * Created by yaguang.wang on 2016/9/23.
 */
@Controller
@RequestMapping("/sysconfig/payments")
@Slf4j
public class AgentPerformanceConfigController extends AbstractAgentController {
    private static final int BYTES_BUFFER_SIZE = 1024 * 8;
    private static final String AGENT_URL = "/config/templates/import_performance_config_template2.xlsx";
    private static final String REGION_URL = "/config/templates/import_performance_config_template1.xlsx";
    @Inject private AgentPerformanceConfigService agentPerformanceConfigService;

    private static void write(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[BYTES_BUFFER_SIZE];
        int size;
        while ((size = in.read(buffer, 0, buffer.length)) > 0) {
            out.write(buffer, 0, size);
        }
        out.flush();
    }

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String agentPaymentsIndex(Model model) {
        String error = getRequestString("error");
        Integer type = getRequestInt("type");
        if (type == 0) {
            type = 1;
        }
        List<AgentPaymentsData> allData = agentPerformanceConfigService.loadAgentPaymentsDataByType(type);
        model.addAttribute("payments", allData);
        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("error", error);
        }
        model.addAttribute("type", type);
        return "/sysconfig/payments/agentpaymentsconfig";
    }

    /**
     * 移除单个业绩
     */
    @RequestMapping(value = "remove_agent_payments.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeSchoolDictInfo(@RequestParam String id) {
        try {
            int row = agentPerformanceConfigService.removeSchoolDictData(id);
            if (row > 0) {
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage("删除结算数据失败: {}", id);
            }
        } catch (Exception ex) {
            logger.error(String.format("remove agent payments info is failed id=%s", id), ex);
            return MapMessage.errorMessage("删除结算数据失败: {}", id);
        }
    }

    @RequestMapping(value = "import_agent_payments.vpage", method = RequestMethod.GET)
    public String importAgentPayments(Model model) {
        Integer type = getRequestInt("type");
        if (type == 0) {
            type = 1;
        }
        model.addAttribute("type", type);
        return "/sysconfig/payments/importagentpayments";
    }


    @RequestMapping(value = "import_agent_payments_template.vpage", method = RequestMethod.GET)
    public void importSchoolDictTemplate() {
        Integer type = getRequestInt("type");
        try {
            Resource resource = null;
            if (Objects.equals(type, 2)) {
                resource = new ClassPathResource(AGENT_URL);
            }
            if (Objects.equals(type, 1)) {
                resource = new ClassPathResource(REGION_URL);
            }

            if (resource == null) {
                logger.error("download import agent payments template - key is error ");
                return;
            }
            if (!resource.exists()) {
                logger.error("download import agent payments template - template not exists ");
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(in, out);
            String fileName = "业绩结算数导入模版.xlsx";
            if (Objects.equals(type, 2)) {
                fileName = "代理结算数导入模版.xlsx";
            }
            if (Objects.equals(type, 1)) {
                fileName = "大区经理计算数导入模版.xlsx";
            }
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("download import school dict Template - Excp : {};", e);
        }
    }

    /**
     * 批量导入结算信息
     */
    @RequestMapping(value = "import_agent_payments.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bulkImportSchoolDictInfo() {
        XSSFWorkbook workbook = readRequestWorkbook("sourceFile");
        Integer type = getRequestInt("type");
        return agentPerformanceConfigService.importAgentPayments(workbook, type);
    }

    private XSSFWorkbook readRequestWorkbook(String name) {
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
}
