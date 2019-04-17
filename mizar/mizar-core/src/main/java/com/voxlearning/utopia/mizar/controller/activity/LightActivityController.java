package com.voxlearning.utopia.mizar.controller.activity;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.DigestUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.entity.activity.XqbSignUp;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.utils.MizarOssManageUtils;
import com.voxlearning.utopia.service.campaign.client.StudentActivityServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.helper.Validate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;
import static com.voxlearning.alps.calendar.DateUtils.dateToString;

@Controller
@RequestMapping("/activity/")
public class LightActivityController extends AbstractMizarController {

    private static final long XQB_MAX_UPLOAD_FILE_SIZE = 10 * 1024 * 1024;

    @Inject private RaikouSystem raikouSystem;
    @Inject private StudentActivityServiceClient stuActSrvCli;

    /**
     * 小小铅笔 - 获取区域列表接口
     */
    @RequestMapping(value = "/xqb/get_region.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getChildrenRegion() {
        List<Map<String, Object>> regionList = new LinkedList<>();
        Integer pcode = getRequestInt("region_pcode");
        List<ExRegion> regionAll = raikouSystem.getRegionBuffer().loadChildRegions(pcode);
        if (CollectionUtils.isNotEmpty(regionAll)) {
            for (ExRegion exRegion : regionAll) {
                Map<String, Object> region = new HashMap<>();
                region.put("region_code", exRegion.getCode());
                region.put("region_name", exRegion.getName());
                regionList.add(region);
            }
        }
        return MapMessage.successMessage().add("regionList", regionList);
    }

    /**
     * 小小铅笔 - 判断地区是否属于线下范围
     *
     * @return
     */
    @RequestMapping(value = "/xqb/check_region.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage xqbCheckRegion() {
        try {
            Integer regionCode = getRequestInt("code");
            Validate.isTrue(regionCode > 0, "参数错误!");

            boolean isOfflineRegion = raikouSystem.getRegionBuffer()
                    .findByTag("XqbOfflineRegions")
                    .contains(regionCode);

            return MapMessage.successMessage().add("isOfflineRegion", isOfflineRegion);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 小小铅笔 - 提交报名
     *
     * @return
     */
    @RequestMapping(value = "/xqb/sign_up.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage xqbSignUp(@RequestBody(required = false) XqbSignUp signUpInfo) {
        try {
            Validate.notNull(signUpInfo, "报名信息为空!");
            Validate.notEmpty(signUpInfo.getStudentName(), "学生姓名为空!");
            Validate.notEmpty(signUpInfo.getParentName(), "家长姓名为空!");
            Validate.notEmpty(signUpInfo.getWorksName(), "作品名称为空!");
            Validate.notEmpty(signUpInfo.getWorksUrl(), "作品地址为空!");
            Validate.notEmpty(signUpInfo.getPhone(), "联系方式为空!");

            // 初始化下数据
            signUpInfo.setOnline(SafeConverter.toBoolean(signUpInfo.getOnline()));
            return stuActSrvCli.submitXqbSignUp(signUpInfo);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 小小铅笔 - 上传作品
     *
     * @param file
     * @return
     */
    @RequestMapping(value = "/xqb/upload_works.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadArticleImg(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return MapMessage.errorMessage("请选择上传的素材！");
        }

        try {
            if (file.getSize() > XQB_MAX_UPLOAD_FILE_SIZE) {
                return MapMessage.errorMessage("作品大小超出10M，请调整后重新上传!");
            }

            String time = getRequestString("time");
            String sig = getRequestString("sig");
            String expectedSig = DigestUtils.md5Hex(Long.toString(file.getSize()) + "yz71" + time);
            Validate.isTrue(Objects.equals(expectedSig, sig), "上传失败!50098");

            String fileName = MizarOssManageUtils.upload(file);
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("作品上传失败！");
            }
            if (MizarOssManageUtils.invalidFile.equals(fileName)) {
                return MapMessage.errorMessage("无效的文件类型！");
            }
            return MapMessage.successMessage().add("fileName", fileName);
        } catch (Exception ex) {
            logger.error("Failed to upload img, ex={}", ex);
            return MapMessage.errorMessage("上传素材失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "/xqb/export_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public void exportXqbData(HttpServletResponse resp) {
        try {
            Integer pageSize = getRequestInt("pageSize");
            Integer pageNum = getRequestInt("pageNum");
            String sig = getRequestString("sig");
            Validate.isTrue(pageSize > 0, "页大小参数为空!");
            Validate.isTrue(pageNum > 0, "页码为空!");
            Validate.notEmpty(sig, "参数错误!");

            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("pageSize", String.valueOf(pageSize));
            paramMap.put("pageNum", String.valueOf(pageNum));
            String expectedSig = DigestSignUtils.signMd5(paramMap, "46CDE87EEBC040DA938CC9FB34F90DA8");
            Validate.isTrue(Objects.equals(expectedSig, sig), "sig错误!");

            Date endDate = DateUtils.truncate(new Date(), Calendar.DATE);
            List<XqbSignUp> signUpList = stuActSrvCli.loadXQBSignUpForExport(endDate, pageSize, pageNum);
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();

            int rowIndex = 0;
            int colIndex = 0;
            XSSFRow row;
            int regionCode;
            for (XqbSignUp signUpInfo : signUpList) {
                row = sheet.createRow(rowIndex++);

                row.createCell(colIndex++).setCellValue(signUpInfo.getStudentName());
                row.createCell(colIndex++).setCellValue(signUpInfo.getParentName());
                row.createCell(colIndex++).setCellValue(signUpInfo.getPhone());
                row.createCell(colIndex++).setCellValue(signUpInfo.getWorksName());

                regionCode = SafeConverter.toInt(signUpInfo.getRegionCode());
                String cityName = Optional.ofNullable(raikouSystem.loadRegion(regionCode))
                        .map(ExRegion::getCityName)
                        .orElse(null);

                row.createCell(colIndex++).setCellValue(cityName);
                row.createCell(colIndex++).setCellValue(signUpInfo.getAgentcode());
                row.createCell(colIndex++).setCellValue(DateUtils.dateToString(signUpInfo.getCreateDatetime()));
                row.createCell(colIndex++).setCellValue(signUpInfo.getWorksUrl());

                colIndex = 0;
            }

            @Cleanup ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    "xqb_sign_up_" + dateToString(endDate, FORMAT_SQL_DATE) + "_" + pageNum + ".xlsx",
                    "application/vnd.ms-excel",
                    outputStream.toByteArray());

        } catch (Exception e) {
            try {
                resp.setHeader("Content-type", "text/html;charset=UTF-8");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(e.getMessage());
                // resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            } catch (IOException e1) {
            }
        }
    }

    @RequestMapping(value = "/xqb/get_wechat_config.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getWechatConfig() {
        try {
            String srcUrl = getRequestString("url");
            Validate.notEmpty(srcUrl, "参数错误!");

            String type = Optional.of(getRequestString("t"))
                    .filter(StringUtils::isNotBlank)
                    .orElse("0");

            String domain;
            if (RuntimeMode.isUsingTestData()) {
                domain = "http://wechat.test.17zuoye.net";
            } else
                domain = "http://wechat.17zuoye.com";

            String url = domain + "/others/getjsapiconfig.vpage";
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                    .post(url)
                    .contentCharset(Charset.forName("UTF-8"))
                    .addParameter("url", srcUrl)
                    .addParameter("t", type)
                    .execute();

            MapMessage result = JsonUtils.fromJson(response.getResponseString(), MapMessage.class);
            Validate.notNull(result, "error!");
            return result;
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
    }

}
