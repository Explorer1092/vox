package com.voxlearning.utopia.admin.controller.studyTogether.course;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseStructSkuLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseStructSkuService;
import com.voxlearning.galaxy.service.studycourse.api.entity.changelog.ContentChangeLog;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructSku;
import com.voxlearning.galaxy.service.studycourse.constant.ChangeLogType;
import com.voxlearning.galaxy.service.studycourse.constant.NewTemplateType;
import com.voxlearning.galaxy.service.studycourse.constant.StudyCourseMapFaceType;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.CourseConstMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <P>sku
 *
 * @author xuerui.zhang
 * @since 2018/9/12 下午7:59
 */
@Controller
@RequestMapping(value = "opmanager/studytogether/sku/")
public class CrmCourseStructSkuController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = CrmCourseStructSkuService.class)
    private CrmCourseStructSkuService courseSkuService;

    @ImportService(interfaceClass = CrmCourseStructSkuLoader.class)
    private CrmCourseStructSkuLoader courseSkuLoader;

    /**
     * sku列表
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Long skuId = getRequestLong("skuId");
        Long spuId = getRequestLong("spuId");
        Integer phase = getRequestInt("phase");
        String openDate = getRequestString("openDate");
        String closeDate = getRequestString("closeDate");
        Integer envLevel = getRequestInt("envLevel");
        Integer skuType = getRequestInt("skuType", -1);
        Integer activeType = getRequestInt("activeType");
        String createUser = getRequestString("createUser");
        String isComponentSku = getRequestString("isComponentSku");
        String templateType = getRequestString("templateType");

        List<CourseStructSku> skuList = courseSkuLoader.loadAllCourseStructSku();
        Page<CourseStructSku> resultList = getPages(skuList, skuId, spuId, phase, openDate, closeDate,
                envLevel, createUser, pageRequest, activeType, skuType, isComponentSku, templateType);

        if (0L != skuId) model.addAttribute("skuId", skuId);
        if (0L != spuId) model.addAttribute("spuId", spuId);
        if (envLevel > 0) model.addAttribute("envLevel", envLevel);
        if (activeType > 0) model.addAttribute("activeType", activeType);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("openDate", openDate);
        model.addAttribute("openDate", openDate);
        model.addAttribute("closeDate", closeDate);
        model.addAttribute("createUser", createUser);
        model.addAttribute("skuType", skuType);
        model.addAttribute("isComponentSku", isComponentSku);
        model.addAttribute("templateType", templateType);
        return "/opmanager/studyTogether/sku/index";
    }

    /**
     * 课程修改/添加页面
     */
    @RequestMapping(value = "details.vpage", method = RequestMethod.GET)
    public String courseDetail(Model model) {
        Long skuId = getRequestLong("skuId", 0L);
        String username = null;
        if (0L != skuId) {
            CourseStructSku courseSku = courseSkuLoader.loadCourseStructSkuById(skuId);
            if (courseSku != null) {
                username = courseSku.getCreateUser();
                model.addAttribute("content", courseSku);
            }
        } else {
            model.addAttribute("content", new CourseStructSku());
        }
        String oosStr = ConfigManager.instance().getCommonConfig().getConfigs().get(CourseConstMapper.OSS_HOST);
        model.addAttribute("cdn_host", StringUtils.defaultString(oosStr));
        model.addAttribute("createUser", null == username ? getCurrentAdminUser().getAdminUserName() : username);
        model.addAttribute("skuId", skuId);
        model.addAttribute("levels", CourseConstMapper.ENV_LEVEL);
        model.addAttribute("ebooks", CourseConstMapper.EBOOK_TYPE);
        model.addAttribute("ways", CourseConstMapper.JOIN_WAY);
        model.addAttribute("codes", CourseConstMapper.CODE_TYPE);
        model.addAttribute("StudyCourseMapFaceTypeList", StudyCourseMapFaceType.values());
        return "opmanager/studyTogether/sku/details";
    }

    /**
     * 添加或修改SKU
     */
    @ResponseBody
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    public MapMessage save() {
        Long skuId = getRequestLong("skuId");
        Long spuId = getRequestLong("spuId");
        Integer phase = getRequestInt("phase");
        String openDate = getRequestString("openDate");
        String closeDate = getRequestString("closeDate");
        String price = getRequestString("price");
        String discountPrice = getRequestString("discountPrice");
        Integer personLimited = getRequestInt("personLimited");
        String productId = getRequestString("productId");
        String ebookId = getRequestString("ebookId");
        Integer cardDisplay = getRequestInt("cardDisplay");
        Integer ebookGetWay = getRequestInt("ebookGetWay");
        Integer joinWay = getRequestInt("joinWay");
        Integer activeType = getRequestInt("activeType");
        Integer qrcodeType = getRequestInt("qrcodeType");
        Integer envLevel = getRequestInt("envLevel");
        String showDate = getRequestString("showDate");
        String sighUpEndDate = getRequestString("sighUpEndDate");
        String activePagePic = getRequestString("activePagePic");
        String remark = getRequestString("remark");
        String createUser = getRequestString("createUser");
        String rankRewards = getRequestString("rankRewards");
        boolean isComponentSku = getRequestInt("isComponentSku") == 2;
        String newTemplateType = getRequestString("newTemplateType");

        /* 故宫项目新增 */
        Integer skuType = getRequestInt("skuType");
        Integer studyModel = getRequestInt("studyModel");
        boolean needReward = getRequestBool("needReward");

        /**shareConfig 内容配置*/
        String link = getRequestString("link");
        String title = getRequestString("title");
        String content = getRequestString("content");
        String pictureUrl = getRequestString("pictureUrl");


        String grouponId = getRequestString("grouponId");

        Date open = CrmCourseCommonController.safeConvertDate(openDate);
        Date close = CrmCourseCommonController.safeConvertDate(closeDate);
        Date show = CrmCourseCommonController.safeConvertDate(showDate);

        if (show.after(open) || show.after(close)) {
            return MapMessage.errorMessage("报名时间晚于开课或课程结束时间");
        }
        if (open.after(close)) {
            return MapMessage.errorMessage("开课时间晚于结课时间");
        }

        try {
            Boolean flag = false;
            CourseStructSku newObj;
            CourseStructSku oldObj = new CourseStructSku();
            CourseStructSku bean = courseSkuLoader.loadCourseStructSkuById(skuId);
            if (null != bean) {
                flag = true;
                BeanUtils.copyProperties(oldObj, bean);
            }
            if (!flag) {
                bean = new CourseStructSku();
            }

            bean.setId(skuId);
            bean.setSpuId(spuId);
            bean.setPhase(phase);
            bean.setOpenDate(open);
            bean.setCloseDate(close);
            bean.setPrice(price);
            bean.setDiscountPrice(discountPrice);
            bean.setPersonLimited(personLimited);
            if (StringUtils.isNotBlank(productId)) bean.setProductId(productId);
            bean.setCardDisplay(cardDisplay == 2);
            bean.setEbookGetWay(ebookGetWay);
            if (StringUtils.isNotBlank(ebookId)) bean.setEbookId(ebookId);

            bean.setNeedReward(needReward);
            bean.setShowDate(show);
            bean.setSighUpEndDate(CrmCourseCommonController.safeConvertDate(sighUpEndDate));
            bean.setJoinWay(joinWay);
            bean.setActiveType(activeType);

            if (StringUtils.isNotBlank(activePagePic)) bean.setActivePagePic(activePagePic);

            if (StringUtils.isNotBlank(grouponId)) bean.setGrouponId(grouponId);

            bean.setQrcodeType(qrcodeType);
            bean.setEnvLevel(envLevel);
            bean.setIsComponentSku(isComponentSku);
            if (isComponentSku && StringUtils.isNotBlank(newTemplateType)) {
                StudyCourseMapFaceType mapFaceType = StudyCourseMapFaceType.valueOf(newTemplateType);
                bean.setTemplateType(mapFaceType);
            }
            if (StringUtils.isNotBlank(remark)) bean.setRemark(remark);
            String userName = getCurrentAdminUser().getAdminUserName();

            //if (StringUtils.isBlank(rankRewards)) return MapMessage.errorMessage("奖励排行为空");
            if (StringUtils.isNotBlank(rankRewards)) {
                List<Map<String, Object>> rankRewardList = toListMap(rankRewards);
                List<CourseStructSku.RankReward> resultList = new LinkedList<>();
                rankRewardList.forEach(map -> {
                    CourseStructSku.RankReward rankReward = new CourseStructSku.RankReward();
                    rankReward.setFrom(SafeConverter.toInt(map.get("from")));
                    rankReward.setTo(SafeConverter.toInt(map.get("to")));
                    String terms = SafeConverter.toString(map.get("terms"));
                    String[] split = terms.split(",");
                    rankReward.setTerms(Arrays.asList(split));
                    resultList.add(rankReward);
                });
                bean.setRankRewards(resultList);
            }

            CourseStructSku.ShareContentConfig shareContentConfig = new CourseStructSku.ShareContentConfig();
            shareContentConfig.setContent(content);
            shareContentConfig.setLink(link);
            shareContentConfig.setPictureUrl(pictureUrl);
            shareContentConfig.setTitle(title);
            bean.setShareContentConfig(shareContentConfig);

            bean.setSkuType(skuType);
            if (3 == skuType) {
                bean.setStudyModel(studyModel);
            }
            if (!flag) {
                bean.setCreateUser(createUser);
                newObj = courseSkuService.save(bean);
                studyCourseBlackWidowServiceClient.justAddChangeLog("SKU", userName, ChangeLogType.CourseSku,
                        newObj.getId().toString(), "新增SKU信息");
            } else {
                newObj = courseSkuService.save(bean);
                studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldObj, newObj, userName,
                        ChangeLogType.CourseSku, newObj.getId().toString());
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存失败:{}", ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String courseInfo(Model model) {
        Long skuId = getRequestLong("skuId");
        if (0L == skuId) {
            return CourseConstMapper.SKU_REDIRECT;
        }
        CourseStructSku courseSku = courseSkuLoader.loadCourseStructSkuById(skuId);
        if (courseSku == null) {
            return CourseConstMapper.SKU_REDIRECT;
        }
        model.addAttribute("content", courseSku);
        String oosStr = ConfigManager.instance().getCommonConfig().getConfigs().get(CourseConstMapper.OSS_HOST);
        model.addAttribute("cdn_host", StringUtils.defaultString(oosStr));
        model.addAttribute("levels", CourseConstMapper.ENV_LEVEL);
        model.addAttribute("StudyCourseMapFaceTypeList", StudyCourseMapFaceType.values());
        return "opmanager/studyTogether/sku/info";
    }

    @RequestMapping(value = "logs.vpage", method = RequestMethod.GET)
    public String getLogs(Model model) {
        Long skuId = getRequestLong("skuId");
        if (0L == skuId) return CourseConstMapper.SKU_REDIRECT;

        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        Page<ContentChangeLog> resultList = studyCourseBlackWidowServiceClient.getContentChangeLogService()
                .loadChangeLogListByPage(skuId.toString(), ChangeLogType.CourseSku, pageRequest);

//        Page<ContentChangeLog> resultList;
//        if (CollectionUtils.isEmpty(changeLogList)) {
//            resultList = PageableUtils.listToPage(Collections.emptyList(), pageRequest);
//        } else {
//            resultList = PageableUtils.listToPage(changeLogList, pageRequest);
//        }
        model.addAttribute("content", resultList.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", resultList.getTotalPages());
        model.addAttribute("hasPrev", resultList.hasPrevious());
        model.addAttribute("hasNext", resultList.hasNext());
        model.addAttribute("logList", resultList.getContent());
        model.addAttribute("skuId", skuId);
        return "opmanager/studyTogether/sku/logs";
    }

    private Page<CourseStructSku> getPages(List<CourseStructSku> skuList, Long skuId, Long spuId,
                                           Integer phase, String openDate, String closeDate,
                                           Integer envLevel, String createUser, PageRequest pageRequest, Integer activeType, Integer skuType, String isComponent, String type) {
        Page<CourseStructSku> resultList;
        if (CollectionUtils.isNotEmpty(skuList)) {
            if (0L != skuId) {
                skuList = skuList.stream().filter(e -> e.getId().equals(skuId)).collect(Collectors.toList());
            }
            if (0L != spuId) {
                skuList = skuList.stream().filter(e -> e.getSpuId().equals(spuId)).collect(Collectors.toList());
            }
            if (phase > 0) {
                skuList = skuList.stream().filter(e -> e.getPhase().equals(phase)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(openDate)) {
                skuList = skuList.stream().filter(e -> DateUtils.dateToString(e.getOpenDate(), DateUtils.FORMAT_SQL_DATE).equals(openDate))
                        .collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(closeDate)) {
                skuList = skuList.stream().filter(e -> DateUtils.dateToString(e.getCloseDate(), DateUtils.FORMAT_SQL_DATE).equals(closeDate))
                        .collect(Collectors.toList());
            }
            if (activeType > 0) {
                skuList = skuList.stream().filter(e -> e.getActiveType().equals(activeType)).collect(Collectors.toList());
            }
            if (skuType >= 0) {
                skuList = skuList.stream().filter(e -> e.getSkuType().equals(skuType)).collect(Collectors.toList());
            }
            if (envLevel > 0) {
                skuList = skuList.stream().filter(e -> e.getEnvLevel().equals(envLevel)).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(createUser)) {
                skuList = skuList.stream().filter(e -> e.getCreateUser().contains(createUser.trim())).collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(isComponent)) {
                boolean judge = SafeConverter.toBoolean(isComponent);
                if (!judge && StringUtils.isNotBlank(type)) {
                    skuList = Collections.emptyList();
                } else {
                    skuList = skuList.stream().filter(e -> e.safeGetIsComponent() == SafeConverter.toBoolean(judge)).collect(Collectors.toList());
                }
            }
            if (StringUtils.isNotBlank(type) && SafeConverter.toBoolean(isComponent)) {
                StudyCourseMapFaceType mapFaceType = StudyCourseMapFaceType.valueOf(type);
                skuList = skuList.stream().filter(e -> e.getTemplateType() != null && e.getTemplateType() == mapFaceType).collect(Collectors.toList());
            }
            skuList = skuList.stream().sorted((o1, o2) -> o2.getId().compareTo(o1.getId())).collect(Collectors.toList());
            resultList = PageableUtils.listToPage(skuList, pageRequest);
        } else {
            resultList = PageableUtils.listToPage(Collections.emptyList(), pageRequest);
        }
        return resultList;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> toListMap(String json) {
        List<Object> list = JSON.parseArray(json);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object object : list) {
            Map<String, Object> ret = (Map<String, Object>) object;
            result.add(ret);
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "checkId.vpage", method = RequestMethod.POST)
    public MapMessage checkSubjectId() {
        Long skuId = getRequestLong("skuId");
        CourseStructSku sku = courseSkuLoader.loadCourseStructSkuById(skuId);
        if (null == sku) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("当前ID已经存在");
        }
    }

}
