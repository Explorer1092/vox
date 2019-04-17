package com.voxlearning.utopia.admin.controller.opmanager;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.galaxy.service.activity.api.DPMoonQaService;
import com.voxlearning.galaxy.service.activity.api.entity.festival.MoonQa;
import com.voxlearning.utopia.service.newhomework.api.constant.ModelType;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryActivity;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMission;
import com.voxlearning.utopia.service.newhomework.consumer.AncientPoetryLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.AncientPoetryServiceClient;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping(value = "/opmanager/poetry")
public class CrmMoonPoetryController extends OpManagerAbstractController {

    @ImportService(interfaceClass = DPMoonQaService.class)
    private DPMoonQaService dpMoonQaService;
    @Inject
    private AncientPoetryLoaderClient ancientPoetryLoaderClient;
    @Inject
    private AncientPoetryServiceClient ancientPoetryServiceClient;
    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storage;

    @RequestMapping(value = "/import.vpage", method = RequestMethod.GET)
    public String list() {
        return "opmanager/poetry/import";
    }

    @RequestMapping(value = "/batch.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batch(MultipartFile inputFile) {
        try {
            InputStream stream = inputFile.getInputStream();
            Workbook workbook = new XSSFWorkbook(stream);
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getLastRowNum();
            if (rowCount <= 1) {
                return MapMessage.successMessage();
            }
            List<MoonQa> moonQas = new LinkedList<>();
            for (int i = 1; i <= rowCount; i++) {
                Row row = sheet.getRow(i);
                MoonQa qa = new MoonQa();
                qa.setClassify(getCellValue(row, 0));
                qa.setQu(getCellValue(row, 1));
                qa.setA(getCellValue(row, 2));
                qa.setB(getCellValue(row, 3));
                qa.setC(getCellValue(row, 4));
                qa.setD(getCellValue(row, 5));
                qa.setAns(getCellValue(row, 6));
                qa.setAnls(getCellValue(row, 7));
                qa.setMotto(getCellValue(row, 8));
                moonQas.add(qa);
            }
            dpMoonQaService.batchImport(moonQas);

            return MapMessage.successMessage();
        } catch (Exception e) {
            return MapMessage.errorMessage("上传文件错误");
        }
    }

    private String getCellValue(Row row, int index) {
        if (row == null || index < 0) {
            return null;
        }
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }

        if (cell.getCellTypeEnum() == CellType.STRING) {
            return cell.getStringCellValue();
        }

        return null;
    }


    /**
     *  小学业务--活动管理 --- 活动管理
     * @return
     */
    @RequestMapping(value = "/activitymanager.vpage", method = RequestMethod.GET)
    public String activityManager(Model model) {
        Integer page = getRequestInt("page", 1);
        //活动ID
        String activityId = getRequestString("activityId");
        //活动名称
        String activityName = getRequestString("activityName");
        //活动年级
        Integer activityLevel = requestInteger("activityLevel");
        //活动状态
        String activityStatus = getRequestString("activityStatus");

        //前端输入回显
        model.addAttribute("page", page);
        model.addAttribute("activityId", activityId);
        model.addAttribute("activityName", activityName);
        model.addAttribute("activityLevel", activityLevel);
        model.addAttribute("activityStatus", activityStatus);

        List<AncientPoetryActivity> activities = new ArrayList<>();
        if (StringUtils.isNotBlank(activityId)) {
            AncientPoetryActivity ancientPoetryActivity = ancientPoetryLoaderClient.findActivityById(activityId);
            if (ancientPoetryActivity != null) {
                activities.add(ancientPoetryActivity);
            }
        } else {
            activities = ancientPoetryLoaderClient.loadAllActivity();
        }
        //活动名称
        if (StringUtils.isNotBlank(activityName)) {
            activities = activities.stream()
                    .filter(a -> StringUtils.isNotBlank(a.getName()))
                    .filter(a -> a.getName().contains(activityName))
                    .collect(Collectors.toList());
        }
        //活动年级
        if (activityLevel != null && activityLevel != 0) {
            activities = activities.stream()
                    .filter(a -> CollectionUtils.isNotEmpty(a.getClassLevel()))
                    .filter(a -> a.getClassLevel().contains(activityLevel))
                    .collect(Collectors.toList());
        }
        //活动状态
        if (StringUtils.isNotBlank(activityStatus)) {
            activities = activities.stream()
                    .filter(a -> a.getDisabled() != null)
                    .filter(a -> a.getDisabled().toString().equals(activityStatus))
                    .collect(Collectors.toList());
        }

        activities.sort(((o1, o2) -> o2.getCreateAt().compareTo(o1.getCreateAt())));
        Page<AncientPoetryActivity> activityPage = PageableUtils.listToPage(activities, new PageRequest(page - 1, 10));
        List<AncientPoetryActivity> resultActivityList = activityPage.getContent();

        List<Map> results = new LinkedList<>();
        for (AncientPoetryActivity activity : resultActivityList) {
            Map map = new HashMap();
            Set<Long> regionIds = new HashSet<>();
            if (CollectionUtils.isNotEmpty(activity.getRegions())) {
                regionIds = activity.getRegions()
                        .stream()
                        .map(AncientPoetryActivity.Region::getRegionIds)
                        .flatMap(Collection::stream)
                        .filter(r -> r != null)
                        .collect(Collectors.toSet());
            }
            map.put("regions", regionIds.toString());
            map.put("activityId", activity.getId());
            map.put("name", activity.getName());
            map.put("startDate", DateUtils.dateToString(activity.getStartDate(), "yyyy年MM月dd日"));
            map.put("endDate", DateUtils.dateToString(activity.getEndDate(), "yyyy年MM月dd日"));
            map.put("missionSize", activity.getMissions().size());
            //活动年级
            List<String> classLevels = new ArrayList<>();
            for (Integer integer : activity.getClassLevel()) {
                String classLevel;
                switch ( integer ) {
                    case 1:
                        classLevel = "一年级";
                        break;
                    case 2:
                        classLevel = "二年级";
                        break;
                    case 3:
                        classLevel = "三年级";
                        break;
                    case 4:
                        classLevel = "四年级";
                        break;
                    case 5:
                        classLevel = "五年级";
                        break;
                    case 6:
                        classLevel = "六年级";
                        break;
                    default:
                        classLevel = "";
                        return classLevel;
                }
                classLevels.add(classLevel);
            }
            map.put("classLevel", classLevels);
            map.put("status", activity.getDisabled());
            results.add(map);
        }
        model.addAttribute("totalPage", activityPage.getTotalPages());
        model.addAttribute("activityList", results);

        return "opmanager/poetry/activitymanager";
    }

    /**
     *  小学业务--活动管理
     *      ---- 新建活动
     *      ---- 活动详情
     *      ---- 活动修改
     * @return
     */
    @RequestMapping(value = "/activity_create_or_view.vpage", method = RequestMethod.GET)
    public String activityCreateOrView(Model model) {
        Integer edit = getRequestInt("edit", 0);
        //活动ID
        String activityId = getRequestString("activityId");
        if (StringUtils.isNotBlank(activityId)) {
            AncientPoetryActivity activity = ancientPoetryLoaderClient.findActivityById(activityId);
            if (activity == null) {
                getAlertMessageManager().addMessageError("您要查找的活动不存在");
                return "opmanager/poetry/activitymanager";
            }
            model.addAttribute("activityObj",activity);  //活动详情实体
            model.addAttribute("poetryMissionList",activity.getMissions()); //当前活动的古诗关卡列表
        }

        model.addAttribute("cdn_host", StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")));
        model.addAttribute("edit", edit);
        model.addAttribute("activityId", activityId);
        return "opmanager/poetry/activity_create_or_view";
    }

    /**
     * 小学业务--活动管理
     *       --活动保存
     * @return
     */
    @RequestMapping(value = "/save/ancient_poetry_activity.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveAncientPoetryActivity() {
        // 获取提交信息
        String activityInfoStr = getRequestString("activity_info");
        AncientPoetryActivity activity = JSON.parseObject(activityInfoStr, AncientPoetryActivity.class);
        //校验参数
        String activityId = activity.getId();
        if (StringUtils.isBlank(activityId)) {
            //自己生成一个活动ID
            String month = MonthRange.newInstance(System.currentTimeMillis()).toString();
            activityId = new AncientPoetryActivity.ID(month).toString();
            activity.setId(activityId);
        }
        if (StringUtils.isBlank(activity.getName())) {
            return MapMessage.errorMessage("活动名称不能为空");
        }
        if (StringUtils.isBlank(activity.getCoverImgUrl())) {
            return MapMessage.errorMessage("活动封面地址不能为空");
        }
        if (StringUtils.isBlank(activity.getBackgroundImgUrl())) {
            return MapMessage.errorMessage("活动背景图不能为空");
        }
        if (StringUtils.isBlank(activity.getBackgroundTopImgUrl())) {
            return MapMessage.errorMessage("背景头部图不能为空");
        }
        if (StringUtils.isBlank(activity.getMissionTopImgUrl())) {
            return MapMessage.errorMessage("关卡头部图不能为空");
        }
        if (CollectionUtils.isEmpty(activity.getRegions())) {
            return MapMessage.errorMessage("活动省份不能为空");
        }
        if (activity.getStartDate() == null) {
            return MapMessage.errorMessage("活动开始时间不能为空");
        }
        if (activity.getEndDate() == null) {
            return MapMessage.errorMessage("活动结束时间不能为空");
        }
        if (CollectionUtils.isEmpty(activity.getClassLevel())) {
            return MapMessage.errorMessage("活动年级不能为空");
        }
        if (CollectionUtils.isEmpty(activity.getLabel())) {
            return MapMessage.errorMessage("主题标签不能为空");
        }
        List<AncientPoetryActivity.Mission> missions = activity.getMissions();
        if (CollectionUtils.isEmpty(missions)) {
            return MapMessage.errorMessage("古诗关卡不能为空");
        }
        //校验古诗ID   是否存在
        List<String> poetryIds = missions.stream().map(AncientPoetryActivity.Mission::getMissionId).collect(Collectors.toList());
        Map<String, AncientPoetryMission> poetryMissionMap = ancientPoetryLoaderClient.fetchAncientPoetryMissionByIds(poetryIds);
        if (poetryMissionMap.size() != poetryIds.size()) {
            for (AncientPoetryActivity.Mission mission : missions) {
                if (!poetryMissionMap.keySet().contains(mission.getMissionId())) {
                    return MapMessage.errorMessage("古诗ID：" + mission.getMissionId() + "&" + "古诗名称：" + mission.getMissionName()+ ",古诗还未录入");
                }
            }
        }
        activity.setDisabled(false);
        activity.setCreateAt(new Date());
        activity.setJoinCount(0L);
        try {
            ancientPoetryServiceClient.upsertAncientPoetryActivity(activity);
        }catch (Exception e){
            logger.error("活动保存失败{}", e);
            return MapMessage.errorMessage("活动保存失败");
        }
        return MapMessage.successMessage().add("id", activity.getId());
    }

    /**
     * 更改活动状态
     */
    @RequestMapping(value = "/update_ancient_poetry_activity_status.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateActivityStatus() {
        String activityId = getRequestString("activity_id");
        if (StringUtils.isBlank(activityId)) {
            return MapMessage.errorMessage("活动ID不能为空");
        }
        String activityStatus = getRequestString("status");
        if (StringUtils.isBlank(activityStatus)) {
            return MapMessage.errorMessage("活动状态不能为空");
        }
        ancientPoetryServiceClient.updateActivityStatus(activityId, SafeConverter.toBoolean(activityStatus));
        return MapMessage.successMessage().add("id", activityId);
    }

    /**
     * 上传图片到aliyun
     */
    @RequestMapping(value = "/upload_signal_file_to_oss.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadImgToOss(MultipartFile inputFile) {
        String activityName = "study_course";
        if (inputFile == null || StringUtils.isBlank(activityName)) {
            return MapMessage.errorMessage("没有上传的文件");
        }
        try {
            return uploadFileTo17pmcOss(inputFile, activityName);
        } catch (Exception e) {
            logger.error("课程文件上传失败{}", e);
            return MapMessage.errorMessage("课程文件上传失败");
        }
    }

    private MapMessage uploadFileTo17pmcOss(MultipartFile inputFile, String activityName) throws IOException {
        if (inputFile == null) {
            return MapMessage.errorMessage("没有可上传的文件");
        }
        StorageMetadata storageMetadata = new StorageMetadata();
        storageMetadata.setContentLength(inputFile.getSize());
        String env = activityName + "/" + "ancient_poetry_activity" + "/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = activityName + "/test/" + "ancient_poetry_activity" + "/";
        }
        String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
        String fileName = inputFile.getOriginalFilename().replaceAll(" ", "");
        String realName = storage.upload(inputFile.getInputStream(), fileName, path, storageMetadata);
        String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
        return MapMessage.successMessage().add("fileName", realName).add("fileUrl", fileUrl);
    }

    /**
     * 上传音频文件
     * @return
     */
    @RequestMapping(value = "/batch_upload_file_to_oss.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchUploadFileToOss() {
        MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) getRequest();
        List<MultipartFile> inputFiles = multipartHttpServletRequest.getFiles("inputFiles");
        String activityName = "study_course";
        if (inputFiles == null) {
            return MapMessage.errorMessage("没有上传的音频文件");
        }
        try {
            List<Map<String, Object>> mapList = new ArrayList<>();
            for (MultipartFile inputFile : inputFiles) {
                MapMessage message = uploadFileTo17pmcOss(inputFile, activityName);
                if (message.isSuccess()) {
                    mapList.add(new HashMap<>(message));
                }
            }
            return MapMessage.successMessage().add("file_list", mapList);
        } catch (Exception e) {
            logger.error("音频文件上传失败{}", e);
            return MapMessage.errorMessage("音频文件上传失败");
        }
    }

    /**
     *  小学业务--活动管理 --- 古诗管理
     * @return
     */
    @RequestMapping(value = "/poetrymanager.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Integer page = getRequestInt("page", 1);
        //古诗ID
        String poetryId = getRequestString("poetryId");
        //古诗名称
        String poetryName = getRequestString("poetryName");
        //古诗描述
        String poetryNameDesc = getRequestString("poetryNameDesc");

        //前端输入回显
        model.addAttribute("page", page);
        model.addAttribute("poetryId", poetryId);
        model.addAttribute("poetryName", poetryName);
        model.addAttribute("poetryNameDesc", poetryNameDesc);

        List<AncientPoetryMission> poetryMissionList = new ArrayList<>();
        if (StringUtils.isNotEmpty(poetryId)) {
            AncientPoetryMission ancientPoetryMission = ancientPoetryLoaderClient.fetchAncientPoetryMissionByIds(Collections.singletonList(poetryId)).get(poetryId);
            if (ancientPoetryMission != null) {
                poetryMissionList.add(ancientPoetryMission);
            }
        } else {
            poetryMissionList = ancientPoetryLoaderClient.loadAllPoetry();
        }
        //古诗名称
        if (StringUtils.isNotBlank(poetryName)) {
            poetryMissionList = poetryMissionList.stream()
                    .filter(a -> StringUtils.isNotBlank(a.getTitle()))
                    .filter(a -> a.getTitle().contains(poetryName))
                    .collect(Collectors.toList());
        }
        //古诗描述
        if (StringUtils.isNotBlank(poetryNameDesc)) {
            poetryMissionList = poetryMissionList.stream()
                    .filter(a -> StringUtils.isNotBlank(a.getDescription()))
                    .filter(a -> a.getDescription().contains(poetryNameDesc))
                    .collect(Collectors.toList());
        }

        Page<AncientPoetryMission> poetryPage = PageableUtils.listToPage(poetryMissionList, new PageRequest(page - 1, 10));
        List<AncientPoetryMission> poetryList = poetryPage.getContent();

        model.addAttribute("totalPage", poetryPage.getTotalPages());
        model.addAttribute("poetryList", poetryList);

        return "opmanager/poetry/index";
    }


    /**
     *  小学业务--古诗管理
     *      ---- 新建古诗
     *      ---- 古诗详情
     *      ---- 古诗修改
     * @return
     */
    @RequestMapping(value = "/poetry_create_or_view.vpage", method = RequestMethod.GET)
    public String poetryCreateOrView(Model model) {
        //是否允许编辑
        Integer edit = getRequestInt("edit", 0);
        model.addAttribute("edit", edit);
        model.addAttribute("admin_user", getCurrentAdminUser().getAdminUserName());
        //古诗ID
        String poetryId = getRequestString("poetryId");
        if (StringUtils.isEmpty(poetryId)) {
            return "opmanager/poetry/poetry_create_or_view";
        }
        AncientPoetryMission ancientPoetryMission = ancientPoetryLoaderClient.fetchAncientPoetryMissionByIds(Collections.singletonList(poetryId)).get(poetryId);
        if (ancientPoetryMission == null) {
            getAlertMessageManager().addMessageError("古诗不存在");
            return "opmanager/poetry/poetry_create_or_view";
        }
        model.addAttribute("poetryMission",ancientPoetryMission);
        model.addAttribute("cdn_host", StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")));

        //古诗模板模块
        if (MapUtils.isNotEmpty(ancientPoetryMission.getModels())) {
            ancientPoetryMission.getModels().values().forEach(p -> {
                if (p.getModelType() == ModelType.EXPOUND) {
                    AncientPoetryMission.ExpoundContent content = p.getExpoundContent();
                    model.addAttribute("expound_content", content);
                    model.addAttribute("expound_title", p.getModelTitle());
                } else if (p.getModelType() == ModelType.APPRECIATE) {
                    AncientPoetryMission.AppreciateContent content = p.getAppreciateContent();
                    model.addAttribute("appreciate_content", content);
                    model.addAttribute("appreciate_title", p.getModelTitle());
                } else if (p.getModelType() == ModelType.RECITE) {
                    AncientPoetryMission.ReciteContent content = p.getReciteContent();
                    model.addAttribute("recite_content", content);
                    model.addAttribute("recite_title", p.getModelTitle());
                } else if (p.getModelType() == ModelType.FUN) {
                    AncientPoetryMission.FunContent content = p.getFunContent();
                    model.addAttribute("fun_content", content);
                    model.addAttribute("fun_title", p.getModelTitle());
                }
            });
        }
        return "opmanager/poetry/poetry_create_or_view";
    }

    /**
     *  小学业务--古诗管理
     *      ---- 保存古诗
     * @return
     */
    @RequestMapping(value = "/save/ancient_poetry_mission.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveAncientPoetryMission() {
        // 获取提交信息
        String poetryStr = getRequestString("poetry_info");
        AncientPoetryMission poetry = JsonUtils.fromJson(poetryStr, AncientPoetryMission.class);
        //校验参数
        if (StringUtils.isBlank(poetry.getTitle())) {
            return MapMessage.errorMessage("古诗标题不能为空");
        }
        if (StringUtils.isBlank(poetry.getAuthor())) {
            return MapMessage.errorMessage("古诗作者不能为空");
        }
        if (StringUtils.isBlank(poetry.getGoalDetail())) {
            return MapMessage.errorMessage("目标描述不能为空");
        }
        if (StringUtils.isBlank(poetry.getAudioUrl())) {
            return MapMessage.errorMessage("上传音频地址不能为空");
        }
        if (poetry.getAudioSeconds() == 0) {
            return MapMessage.errorMessage("音频时长不能为0");
        }
        if (CollectionUtils.isEmpty(poetry.getContentList())) {
            return MapMessage.errorMessage("文本内容不能为空");
        }
        LinkedHashMap<ModelType, AncientPoetryMission.Model> models = poetry.getModels();
        if (MapUtils.isEmpty(models)) {
            return MapMessage.errorMessage("模块内容不能为空");
        }

        //名师讲解
        AncientPoetryMission.Model expoundModel = models.get(ModelType.EXPOUND);
        if (models.get(ModelType.EXPOUND) == null) {
            return MapMessage.errorMessage("名师精讲信息不能为空");
        }
        if (StringUtils.isBlank(expoundModel.getModelTitle())) {
            return MapMessage.errorMessage("名师讲解模块标题不能为空");
        }
        if (expoundModel.getModelType() == null) {
            return MapMessage.errorMessage("名师讲解模块类型不能为空");
        }
        if (expoundModel.getExpoundContent() == null) {
            return MapMessage.errorMessage("名师讲解详细信息不能为空");
        }
        if (StringUtils.isBlank(expoundModel.getExpoundContent().getSubTitle())) {
            return MapMessage.errorMessage("名师讲解模块子标题不能为空");
        }
        if (StringUtils.isBlank(expoundModel.getExpoundContent().getBackgroundImgUrl())) {
            return MapMessage.errorMessage("名师讲解模块背景图不能为空");
        }
        if (StringUtils.isBlank(expoundModel.getExpoundContent().getAudioUrl())) {
            return MapMessage.errorMessage("名师讲解模块音频不能为空");
        }
        if (expoundModel.getExpoundContent().getAudioSeconds() == null) {
            return MapMessage.errorMessage("名师精讲音频时长不能为0");
        }

        //名句赏析
        AncientPoetryMission.Model appreciateModel = models.get(ModelType.APPRECIATE);
        if (appreciateModel == null) {
            return MapMessage.errorMessage("名句赏析不能为空");
        }
        if (StringUtils.isBlank(appreciateModel.getModelTitle())) {
            return MapMessage.errorMessage("名句赏析模块标题不能为空");
        }
        if (appreciateModel.getModelType() == null) {
            return MapMessage.errorMessage("名句赏析模块类型不能为空");
        }
        if (appreciateModel.getAppreciateContent() == null) {
            return MapMessage.errorMessage("名句赏析详细信息不能为空");
        }
        if (StringUtils.isBlank(appreciateModel.getAppreciateContent().getSubTitle())) {
            return MapMessage.errorMessage("名句赏析模块子标题不能为空");
        }
        if (StringUtils.isBlank(appreciateModel.getAppreciateContent().getBackgroundImgUrl())) {
            return MapMessage.errorMessage("名句赏析模块背景图不能为空");
        }
        if (StringUtils.isBlank(appreciateModel.getAppreciateContent().getAudioUrl())) {
            return MapMessage.errorMessage("名句赏析模块音频不能为空");
        }
        if (appreciateModel.getAppreciateContent().getAudioSeconds() == null || appreciateModel.getAppreciateContent().getAudioSeconds() == 0) {
            return MapMessage.errorMessage("名句赏析音频时长不能为0");
        }

        //每日朗读
        AncientPoetryMission.Model reciteModel = models.get(ModelType.RECITE);
        if (reciteModel == null) {
            return MapMessage.errorMessage("每日朗读不能为空");
        }
        if (StringUtils.isBlank(reciteModel.getModelTitle())) {
            return MapMessage.errorMessage("每日朗读模块标题不能为空");
        }
        if (reciteModel.getModelType() == null) {
            return MapMessage.errorMessage("每日朗读模块类型不能为空");
        }
        if (CollectionUtils.isEmpty(reciteModel.getReciteContent().getSentenceList())) {
            return MapMessage.errorMessage("每日朗读句子不能为空");
        }

        //趣味练习
        AncientPoetryMission.Model funModel = models.get(ModelType.FUN);
        if (funModel == null) {
            return MapMessage.errorMessage("趣味练习不能为空");
        }
        if (StringUtils.isBlank(funModel.getModelTitle())) {
            return MapMessage.errorMessage("趣味练习模块标题不能为空");
        }
        if (funModel.getModelType() == null) {
            return MapMessage.errorMessage("趣味练习模块类型不能为空");
        }
        if (CollectionUtils.isEmpty(funModel.getFunContent().getQuestionIds())) {
            return MapMessage.errorMessage("趣味练习题目ID不能为空");
        }
        Map<String, NewQuestion> questionMap = questionLoaderClient.loadLatestQuestionByDocIds(funModel.getFunContent().getQuestionIds());
        Set<String> funMissedQuestionIds = funModel.getFunContent().getQuestionIds()
                .stream()
                .filter(p -> questionMap.get(p) == null)
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(funMissedQuestionIds)) {
            return MapMessage.errorMessage("趣味练习中如下题目ID错误: " + JsonUtils.toJson(funMissedQuestionIds));
        }

        return ancientPoetryServiceClient.upsertAncientPoetryMission(poetry);
    }
}