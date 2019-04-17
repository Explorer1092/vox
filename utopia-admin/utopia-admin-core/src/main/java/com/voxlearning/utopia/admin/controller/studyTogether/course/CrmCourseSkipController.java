package com.voxlearning.utopia.admin.controller.studyTogether.course;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.galaxy.service.studycourse.api.CourseQrcodeLoader;
import com.voxlearning.galaxy.service.studycourse.api.CourseQrcodeService;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseSkipLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseSkipService;
import com.voxlearning.galaxy.service.studycourse.api.entity.changelog.ContentChangeLog;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseQrcode;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseSkip;
import com.voxlearning.galaxy.service.studycourse.constant.ChangeLogType;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.CourseConstMapper;
import lombok.Cleanup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <P>跳转
 * @author xuerui.zhang
 * @since 2018/9/12 下午8:00
 */
@Controller
@RequestMapping(value = "opmanager/studytogether/skip/")
public class CrmCourseSkipController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = CrmCourseSkipService.class)
    private CrmCourseSkipService courseSkipService;

    @ImportService(interfaceClass = CrmCourseSkipLoader.class)
    private CrmCourseSkipLoader courseSkipLoader;

    @ImportService(interfaceClass = CourseQrcodeLoader.class)
    private CourseQrcodeLoader qrcodeLoader;

    @ImportService(interfaceClass = CourseQrcodeService.class)
    private CourseQrcodeService qrcodeService;

    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storageClient;

    private final static String ACTIVITY_NAME = "study_course";

    private static String url;
    static {
        Mode current = RuntimeMode.current();
        switch (current) {
            case PRODUCTION:
                url = "https://www.17zuoye.com";
                break;
            case STAGING:
                url = "https://www.staging.17zuoye.net";
                break;
            case DEVELOPMENT:
                url = "https://www.test.17zuoye.net";
                break;
            case TEST:
                url = "https://www.test.17zuoye.net";
                break;
            default:
                url = "https://www.17zuoye.com";
                break;
        }
    }

    /**
     * 课程-跳转列表
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        String skipId = getRequestString("skipId");
        Long skuId = getRequestLong("skuId");
        Integer type = getRequestInt("type", -1);
        Integer grade = getRequestInt("grade", -1);
        String createUser = getRequestString("createUser");

        List<CourseSkip> skipList = courseSkipLoader.loadAllCourseSkip();
        if (CollectionUtils.isNotEmpty(skipList)) {
            if (StringUtils.isNotBlank(skipId)) {
                skipList = skipList.stream().filter(e -> e.getId().equals(skipId.trim())).collect(Collectors.toList());
            }
            if (0L != skuId) {
                skipList = skipList.stream().filter(e -> e.getSkuId().equals(skuId)).collect(Collectors.toList());
            }
            if (-1 != type) {
                skipList = skipList.stream().filter(e -> e.getType().equals(type)).collect(Collectors.toList());
            }
            if (-1 != grade) {
                skipList = skipList.stream().filter(e -> e.getGrade().equals(grade)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(createUser)) {
                skipList = skipList.stream().filter(e -> e.getCreateUser().contains(createUser.trim())).collect(Collectors.toList());
            }
        }
        Page<CourseSkip> resultList;
        if (CollectionUtils.isEmpty(skipList)) {
            resultList = PageableUtils.listToPage(Collections.emptyList(), pageRequest);
        } else {
            skipList = skipList.stream().sorted((o1, o2) -> o2.getId().compareTo(o1.getId())).collect(Collectors.toList());
            resultList = PageableUtils.listToPage(skipList, pageRequest);
        }

        if(0L != skuId) model.addAttribute("skuId", skuId);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("skipId", skipId);
        model.addAttribute("type", type);
        model.addAttribute("grade", grade);
        model.addAttribute("createUser", createUser);
        return "/opmanager/studyTogether/skip/index";
    }

    /**
     * 跳转-修改/添加页面
     */
    @RequestMapping(value = "details.vpage", method = RequestMethod.GET)
    public String courseDetail(Model model) {
        String skipId = getRequestString("skipId");
        String qrUrl = "";
        String username = null;
        if (StringUtils.isNotBlank(skipId)) {
            CourseSkip courseSkip = courseSkipLoader.loadCourseSkipById(skipId);
            if (courseSkip != null) {
                model.addAttribute("content", courseSkip);
                username = courseSkip.getCreateUser();
                CourseQrcode qrcode = qrcodeLoader.loadCourseShareQrcodeById(CourseQrcode.generateId(courseSkip.getSkuId()));
                if (qrcode != null) {
                    qrUrl = qrcode.getQrCodeUrl();
                }
            }
        } else {
            model.addAttribute("content", new CourseSkip());
        }

        String oosStr = ConfigManager.instance().getCommonConfig().getConfigs().get(CourseConstMapper.OSS_HOST);
        model.addAttribute("cdn_host", StringUtils.defaultString(oosStr));
        model.addAttribute("skipId", skipId);
        model.addAttribute("qrUrl", qrUrl);
        model.addAttribute("types", CourseConstMapper.SKIP_TYPE);
        model.addAttribute("grades", CourseConstMapper.KID_GRADE);
        model.addAttribute("createUser", null == username ? getCurrentAdminUser().getAdminUserName() : username);
        return "opmanager/studyTogether/skip/details";
    }

    /**
     * 添加或修改通知
     */
    @ResponseBody
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    public MapMessage save() {
        String skipId = getRequestString("skipId");
        Long skuId = getRequestLong("skuId");
        int grade = getRequestInt("grade");
        String remark = getRequestString("remark");
        String createUser = getRequestString("createUser");
        String jumpUrl = getRequestString("jumpUrl");
        Long targetSkuId = getRequestLong("targetSkuId");

        //type1:直通车
        int type = getRequestInt("type");
        String appearDate = getRequestString("appearDate");
        String disappearDate = getRequestString("disappearDate");
        String buttonSign = getRequestString("buttonSign");
        String buttonNoSign = getRequestString("buttonNoSign");
        String iconTextSign = getRequestString("iconTextSign");
        String iconTextNoSign = getRequestString("iconTextNoSign");

        try {
            CourseSkip bean;
            CourseSkip newObj;
            CourseSkip oldObj = new CourseSkip();
            if (StringUtils.isBlank(skipId)) {
                bean = new CourseSkip();
            } else {
                bean = courseSkipLoader.loadCourseSkipById(skipId);
                if (bean == null) {
                    return MapMessage.errorMessage("数据不存在");
                }
                BeanUtils.copyProperties(oldObj, bean);
            }

            bean.setSkuId(skuId);
            bean.setGrade(grade);
            bean.setRemark(remark);
            bean.setType(type);
            bean.setJumpUrl(jumpUrl);
            bean.setTargetSkuId(targetSkuId <= 0L ? null : targetSkuId);

            if (1 == type) {
                if (StringUtils.isBlank(appearDate)){
                    bean.setAppearDate(null);
                } else{
                    bean.setAppearDate(CrmCourseCommonController.safeConvertDate(appearDate));
                }
                if (StringUtils.isBlank(disappearDate)){
                    bean.setAppearDate(null);
                }else {
                    bean.setDisappearDate(CrmCourseCommonController.safeConvertDate(disappearDate));
                }
                bean.setButtonSign(buttonSign);
                bean.setButtonNoSign(buttonNoSign);
                bean.setIconTextSign(iconTextSign);
                bean.setIconTextNoSign(iconTextNoSign);
            }

            String userName = getCurrentAdminUser().getAdminUserName();
            if (StringUtils.isBlank(skipId)) {
                bean.setCreateUser(createUser);
            }
            if (StringUtils.isBlank(skipId)) {
                bean.setId(CourseSkip.generateId(skuId, grade, type));
                courseSkipService.save(bean);
                studyCourseBlackWidowServiceClient.justAddChangeLog("课程-跳转", userName,
                        ChangeLogType.CourseSkip, bean.getId(), "新增跳转信息");
            } else {
                bean.setId(skipId);
                newObj = courseSkipService.save(bean);
                studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldObj, newObj, userName,
                        ChangeLogType.CourseSkip, bean.getId());
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    /**
     * 章节Info
     */
    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String courseInfo(Model model) {
        String skipId = getRequestString("skipId");
        if (StringUtils.isBlank(skipId))  {
            return CourseConstMapper.SKIP_REDIRECT;
        }
        CourseSkip courseSkip = courseSkipLoader.loadCourseSkipById(skipId);
        if (courseSkip == null) {
            return CourseConstMapper.SKIP_REDIRECT;
        }
        model.addAttribute("content", courseSkip);
        return "opmanager/studyTogether/skip/info";
    }

    /**
     * 跳转-日志信息
     * @since 日志模板类型：CourseSkip
     */
    @RequestMapping(value = "logs.vpage", method = RequestMethod.GET)
    public String getLogs(Model model) {
        String skipId = getRequestString("skipId");
        if (StringUtils.isBlank(skipId))  {
            return CourseConstMapper.SKIP_REDIRECT;
        }
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Page<ContentChangeLog> resultList = studyCourseBlackWidowServiceClient.getContentChangeLogService()
                .loadChangeLogListByPage(skipId, ChangeLogType.CourseSkip, pageRequest);

        model.addAttribute("content", resultList.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("logList", resultList.getContent());
        model.addAttribute("skipId", skipId);
        return "opmanager/studyTogether/skip/logs";
    }

    @ResponseBody
    @RequestMapping(value = "check_id.vpage", method = RequestMethod.GET)
    public MapMessage checkId() {
        Long skuId = getRequestLong("skuId");
        int grade = getRequestInt("grade");
        int type = getRequestInt("type");
        CourseSkip courseSkip = courseSkipLoader.loadCourseSkipById(CourseSkip.generateId(skuId, grade, type));
        return courseSkip == null ? MapMessage.successMessage() : MapMessage.errorMessage();
    }

    @ResponseBody
    @RequestMapping(value = "create_qrcode.vpage", method = RequestMethod.POST)
    public MapMessage createQRcode() {
        Long skuId = getRequestLong("skuId");
        String jumpUrl = getRequestString("jumpUrl");
        String qrcodeId = CourseQrcode.generateId(skuId);
        CourseQrcode qrcode = new CourseQrcode();
        qrcode.setId(qrcodeId);
        String qrcodeUrl = createQRcode(jumpUrl);
        qrcode.setQrCodeUrl(qrcodeUrl);
        CourseQrcode save = qrcodeService.save(qrcode);
        if (save == null) {
            return MapMessage.errorMessage("分享二维码生成失败");
        }
        return MapMessage.successMessage().add("newUrl", save.getQrCodeUrl());
    }

    //生成二维码
    private String createQRcode(String jumpUrl){
        if (StringUtils.isBlank(jumpUrl)) {
            return null;
        }

        String contentUrl;
        if (jumpUrl.startsWith("/")) {
            contentUrl = url + jumpUrl;
        } else {
            contentUrl = url + "/" +  jumpUrl;
        }

        String env = ACTIVITY_NAME + "/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = ACTIVITY_NAME + "/test/";
        }
        String filePath = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());

        try(ByteArrayOutputStream imageOut = new ByteArrayOutputStream()) {
            int width = 320;
            int height = 320;

            HashMap<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix byteMatrix = new MultiFormatWriter().encode(
                    new String(contentUrl.getBytes("UTF-8"), "ISO-8859-1"),
                    BarcodeFormat.QR_CODE, width, height, hints);

            BufferedImage image = toBufferedImage(byteMatrix);
            ImageIO.write(image, "png", imageOut);
            String imageName = "QRCode_" + RandomUtils.nextObjectId() + ".png";
            @Cleanup ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageOut.toByteArray());
            storageClient.upload(byteArrayInputStream, imageName, filePath);
            return filePath + "/" + imageName;
        } catch (Exception e) {
            logger.error("generate qrcode error. url:{} filePath:{}", contentUrl, filePath, e);
            return null;
        }
    }

    private BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }
}
