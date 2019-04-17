package com.voxlearning.utopia.agent.controller.activity;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.activity.ActivityCardRedeemCodeService;
import com.voxlearning.utopia.agent.service.common.BaseExcelService;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 礼品卡发送
 */

@Controller
@RequestMapping("/activity_card")
public class ActivityCardImportController extends AbstractAgentController {

    @Inject
    private ActivityCardRedeemCodeService cardRedeemCodeService;
    @Inject
    private BaseExcelService baseExcelService;

    private final static String IMPORT_KLX_STUDENT = "/config/templates/import_activity_redeem_template.xlsx";
    private static final int BYTES_BUFFER_SIZE = 1024 * 8;

    /**
     * 导入兑换码
     * @return
     */
    @RequestMapping(value = "import/import_redeem.vpage", method = RequestMethod.GET)
    public String importKLXStudentsView(){
        return "activity/importRedeemIndex";
    }

    /**
     * 下载模板
     */
    @RequestMapping(value = "import/import_redeem_model.vpage", method = RequestMethod.GET)
    public void importKLXstudentModel(){
        try {
            Resource resource = new ClassPathResource(IMPORT_KLX_STUDENT);
            if (!resource.exists()) {
                logger.error("download import school dict template - template not exists ");
                return;
            }
            @Cleanup InputStream in = resource.getInputStream();
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            write(in, out);
            String fileName = "兑换码导入模版.xlsx";
            HttpRequestContextUtils.currentRequestContext().downloadFile(fileName, "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception e) {
            logger.error("download import school dict Template - Excp : {};", e);
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

    @RequestMapping(value = "importCardAndredeemCode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importLogisticExcel() {
        XSSFWorkbook workbook = baseExcelService.readRequestWorkbook(getRequest(), "sourceFile");
        return cardRedeemCodeService.cvExcel2ActivityCardRedeemCode(workbook);
    }
}
