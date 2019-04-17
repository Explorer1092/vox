package com.voxlearning.utopia.admin.controller.studyTogether;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.galaxy.service.coin.api.CoinTypeBufferLoaderClient;
import com.voxlearning.galaxy.service.coin.api.DPCoinLoader;
import com.voxlearning.galaxy.service.coin.api.DPCoinService;
import com.voxlearning.galaxy.service.coin.api.constant.BatchAddCoinType;
import com.voxlearning.galaxy.service.coin.api.constant.CoinOperationType;
import com.voxlearning.galaxy.service.coin.api.entity.Coin;
import com.voxlearning.galaxy.service.coin.api.entity.CoinHistory;
import com.voxlearning.galaxy.service.coin.api.entity.CoinImportHistory;
import com.voxlearning.galaxy.service.coin.api.entity.CoinType;
import com.voxlearning.galaxy.service.coin.api.support.CoinHistoryBuilder;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.galaxy.service.studycourse.api.entity.credit.UserCredit;
import com.voxlearning.galaxy.service.studycourse.api.entity.credit.UserCreditHistory;
import com.voxlearning.galaxy.service.studycourse.api.user.StudyCourseUserDataService;
import com.voxlearning.galaxy.service.studycourse.api.user.StudyCourseUserStatisticsDataLoader;
import com.voxlearning.utopia.admin.data.CoinHistoryMapper;
import com.voxlearning.utopia.admin.data.CoinImportHistoryMapper;
import com.voxlearning.utopia.admin.data.ParentJoinData;
import com.voxlearning.utopia.admin.util.ExcelUtil;
import com.voxlearning.utopia.admin.util.XssfUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.core.helper.ShortUrlGenerator;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherService;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.*;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.CrmStudyTogetherDataMapper;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.StudyTogetherJobText;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/4/12
 */
@Controller
@RequestMapping("opmanager/studyTogether")
@Slf4j
public class CrmStudyTogetherController extends AbstractStudyTogetherController {

    @ImportService(interfaceClass = CrmStudyTogetherService.class)
    private CrmStudyTogetherService crmStudyTogetherService;

    @ImportService(interfaceClass = StudyCourseUserStatisticsDataLoader.class)
    private StudyCourseUserStatisticsDataLoader studyCourseUserStatisticsDataLoader;

    @ImportService(interfaceClass = StudyCourseUserDataService.class)
    private StudyCourseUserDataService studyCourseUserDataService;

    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storageClient;

    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @ImportService(interfaceClass = DPCoinLoader.class)
    private DPCoinLoader dpCoinLoader;

    @ImportService(interfaceClass = DPCoinService.class)
    private DPCoinService dpCoinService;

    @Inject
    private CoinTypeBufferLoaderClient coinTypeBufferLoaderClient;

    @Inject
    private StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    /**
     * 进入班级管理
     */
    @RequestMapping(value = "/backdoor/parentJoin.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage backDoorParentJoin() {
        String pids = getRequestString("pids");
        String lessonId = getRequestString("lessonId");
        String sourceType = getRequestString("sourceType");
        List<Long> pidList = JsonUtils.fromJsonToList(pids, Long.class);
        Map<Long, MapMessage> resultMap = new HashMap<>();
        if (sourceType.equals(ParentJoinLessonRef.JoinSource.BUY.name())) {
            for (Long pid : pidList) {
                List<UserOrder> userOrders = userOrderLoaderClient.loadUserOrderList(pid);
                UserOrder userOrder = userOrders.stream().filter(t -> OrderProductServiceType.safeParse(t.getOrderProductServiceType()) == OrderProductServiceType.StudyMates).findFirst().orElse(null);
                if (userOrder != null) {
                    MapMessage mapMessage = studyTogetherServiceClient.parentSignUpLesson(lessonId, pid, false, ParentJoinLessonRef.JoinSource.BUY, userOrder.getId());
                    resultMap.put(pid, mapMessage);
                } else {
                    resultMap.put(pid, MapMessage.errorMessage("不存在对应订单"));
                }

            }
        } else if (sourceType.equals(ParentJoinLessonRef.JoinSource.GROUP.name())) {
            for (Long pid : pidList) {
                MapMessage mapMessage = studyTogetherServiceClient.parentSignUpLesson(lessonId, pid, true, ParentJoinLessonRef.JoinSource.GROUP, "repair");
                resultMap.put(pid, mapMessage);
            }
        } else if (sourceType.equals(ParentJoinLessonRef.JoinSource.PACKAGE.name())) {
            for (Long pid : pidList) {
                MapMessage mapMessage = studyTogetherServiceClient.parentSignUpLesson(lessonId, pid, false, ParentJoinLessonRef.JoinSource.FREE, "repair");
                resultMap.put(pid, mapMessage);
            }
        }
        return MapMessage.successMessage().add("result", resultMap);
    }


    /**
     * 进入班级管理
     * 这里需要添加一列：管理员
     */
    @RequestMapping(value = "/studyGroupList.vpage", method = RequestMethod.GET)
    public String studyGroupList(Model model) {
        String lessonId = getRequestString("selectLessonId");
        Integer pageNum = getRequestInt("page", 1);
        String wechat = getRequestString("wechat");
        String wechatName = getRequestString("wechatName");
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        List<String> lessonIds = getAllLessonId();

        if (CollectionUtils.isNotEmpty(lessonIds)) {
            model.addAttribute("lessonIds", lessonIds);
        }
        if (StringUtils.isBlank(lessonId) && CollectionUtils.isNotEmpty(lessonIds)) {
            lessonId = lessonIds.get(0);
        }

        String wechatId = "";
        List<Map<String, Object>> returnList = new ArrayList<>();
        if (StringUtils.isNotBlank(wechat)) {
            StudyOpWechatAccount wechatAccount = crmStudyTogetherService.getWechatInfoByWechatNumber(wechat, lessonId);
            if (wechatAccount != null) {
                wechatId = wechatAccount.getId();
            }
        }

        //添加微信群名称搜索条件
        Page<StudyGroup> studyGroupsPage = crmStudyTogetherService.loadLessonStudyGroupList(lessonId, wechatId, pageRequest, wechatName);
        List<StudyGroup> studyGroups = studyGroupsPage.getContent();
        Map<String, StudyOpWechatAccount> wechatInfos = new HashMap<>();
        if (CollectionUtils.isNotEmpty(studyGroups)) {
            List<String> ids = studyGroups.stream().filter(e -> StringUtils.isNotBlank(e.getWechatId())).map(StudyGroup::getWechatId).collect(Collectors.toList());
            wechatInfos = crmStudyTogetherService.$getOpWechatAccountByIds(ids);
        }

        for (StudyGroup studyGroup : studyGroups) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", studyGroup.getId());
            map.put("wechateGroupName", studyGroup.getWechatGroupName() == null ? "" : studyGroup.getWechatGroupName());
            if (MapUtils.isNotEmpty(wechatInfos) && StringUtils.isNotBlank(studyGroup.getWechatId())) {
                map.put("wechatId", studyGroup.getWechatId());
                if (MapUtils.isNotEmpty(wechatInfos) && wechatInfos.get(studyGroup.getWechatId()) != null) {
                    map.put("wechatNumber", wechatInfos.get(studyGroup.getWechatId()).getWechatNumber());
                }
            }
            map.put("type", studyGroup.safeGetGroupType().name());
            map.put("code", studyGroup.getVerifyCode());
            map.put("active_url", studyGroup.getActiveUrl());
            if (StringUtils.isNotBlank(studyGroup.getGroupAreaId())) {
                GroupArea groupAreaById = crmStudyTogetherService.getGroupAreaById(studyGroup.getGroupAreaId());
                if (groupAreaById != null) {
                    map.put("areaId", groupAreaById.getId());
                    map.put("areaName", groupAreaById.getGroupAreaName());
                }
            }
            returnList.add(map);
        }

        model.addAttribute("content", returnList);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", studyGroupsPage.getTotalPages());
        model.addAttribute("hasPrev", studyGroupsPage.hasPrevious());
        model.addAttribute("hasNext", studyGroupsPage.hasNext());
        model.addAttribute("selectedLessonId", lessonId);
        model.addAttribute("wechat", wechat);
        model.addAttribute("wechatName", wechatName);
        return "opmanager/studyTogether/studyGroupListPage";
    }

    /**
     * 通过课程ID批量获取微信号
     */
    @RequestMapping(value = "getWechatNumberByLessonId.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getWechatNumberByLessonId() {
        String lessonId = getRequestString("lessonId");
        if (StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage();
        }
        List<StudyOpWechatAccount> studyOpWechatAccounts = crmStudyTogetherService.$getOpWechatListByLessonId(lessonId);

        return MapMessage.successMessage().add("wechatList", studyOpWechatAccounts);
    }

    /**
     * 修改课程班级管理信息
     */
    @RequestMapping(value = "updateWechatGroupName.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateWechatGroupName() {
        String id = getRequestString("id");
        String newName = getRequestString("newName");
        String wechatId = getRequestString("wechatId");
        String wechatName = getRequestString("wechatName");
        String opWechatName = getRequestString("opWechatName");
        String areaId = getRequestString("areaId");

        if (StringUtils.isNotBlank(wechatId) && StringUtils.isNotBlank(wechatName)) {
            StudyOpWechatAccount studyOpWechatAccount = crmStudyTogetherService.$getOpWechatAccountById(wechatId);
            studyOpWechatAccount.setWechatNumber(wechatName);
            crmStudyTogetherService.$saveOpWechatList(studyOpWechatAccount);
        }
        if (StringUtils.isNotBlank(areaId)) {
            crmStudyTogetherService.updateStudyGroupArea(id, areaId);
        }

        //添加个人微信号修改
        crmStudyTogetherService.updateStudyGroupWechatGroupName(id, newName, opWechatName);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "createStudGroup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createStudGroup() {
        String id = getRequestString("lessonId");
        String name = getRequestString("name");
        String wechatId = getRequestString("wechatId");
        String areaId = getRequestString("areaId");
        if (StringUtils.isNotBlank(name)) {
            StudyGroup groupByGroupNameAndLessonId = crmStudyTogetherService.getGroupByGroupNameAndLessonId(name, id);
            if (groupByGroupNameAndLessonId != null) {
                return MapMessage.errorMessage("同一课程下微信群名称不能重复");
            }
        }
        StudyGroup newStudyGroup = crmStudyTogetherService.createNewStudyGroup(id, name, wechatId);
        if (newStudyGroup != null) {
            String activeShortUrl = generateActiveShortUrl(newStudyGroup.getLessonId(), newStudyGroup.getVerifyCode());
            if (StringUtils.isNotBlank(activeShortUrl)) {
                String realUrl = ShortUrlGenerator.getShortUrlSiteUrl() + "/" + activeShortUrl;
                realUrl = realUrl.replace("http://", "https://");
                crmStudyTogetherService.updateStudyGroupActiveUrl(newStudyGroup.getId(), realUrl);
            }
            if (StringUtils.isNotBlank(areaId)) {
                crmStudyTogetherService.updateStudyGroupArea(newStudyGroup.getId(), areaId);
            }
            return MapMessage.successMessage().add("code", newStudyGroup.getVerifyCode()).add("activeUrl", activeShortUrl);
        } else {
            return MapMessage.errorMessage();
        }
    }


    /**
     * 进入个人微信号管理页
     */
    @RequestMapping(value = "/opWechatEditPage.vpage", method = RequestMethod.GET)
    public String opWechatEditPage(Model model) {
        String lessonId = getRequestString("selectLessonId");
        String accountType = getRequestString("accountType");
        Integer pageNum = getRequestInt("page", 1);
        String wechat = getRequestString("wechat");
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);

        List<String> lessonIds = getAllLessonId();
        if (CollectionUtils.isNotEmpty(lessonIds)) {
            model.addAttribute("lessonIds", lessonIds);
        }
        if (StringUtils.isBlank(lessonId) && CollectionUtils.isNotEmpty(lessonIds)) {
            lessonId = lessonIds.get(0);
        }

        List<Map<String, Object>> returnList = new ArrayList<>();
        if (StringUtils.isNotBlank(lessonId)) {
            List<StudyOpWechatAccount> studyOpWechatAccounts = crmStudyTogetherService.$getOpWechatListByLessonId(lessonId);
            if (StringUtils.isNotBlank(wechat)) {
                studyOpWechatAccounts = studyOpWechatAccounts.stream().filter(e -> StringUtils.equals(wechat, e.getWechatNumber())).collect(Collectors.toList());
            }
            List<StudyOpWechatAccount> finalStudyOpWechatAccounts = studyOpWechatAccounts;
            studyOpWechatAccounts
                    .stream()
                    .filter(e -> StringUtils.isBlank(accountType) || StringUtils.equals(e.getAccountType().name(), accountType))
                    .sorted(Comparator.comparing(StudyOpWechatAccount::getCreateTime))
                    .forEach(e -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", e.getId());
                        map.put("index", finalStudyOpWechatAccounts.indexOf(e) + 1);
                        map.put("self_wechat", e.getWechatNumber());
                        map.put("join_count", e.getJoinCount());
                        map.put("class_area", e.getClassArea());
                        map.put("wechat_code", StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + e.getQrCodeUrl());
                        map.put("accountType", e.getAccountType());
                        returnList.add(map);
                    });
        }
        Page<Map<String, Object>> listToPage = PageableUtils.listToPage(returnList, pageRequest);
        model.addAttribute("content", listToPage.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", listToPage.getTotalPages());
        model.addAttribute("hasPrev", listToPage.hasPrevious());
        model.addAttribute("hasNext", listToPage.hasNext());
        model.addAttribute("selectLessonId", lessonId);
        model.addAttribute("accountType", accountType);
        model.addAttribute("wechat", wechat);
        return "opmanager/studyTogether/opWechatEditPage";
    }


    /**
     * 通过id查微信号相关信息
     */
    @RequestMapping(value = "/getOpWechatAccountById.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getOpWechatAccountById() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage();
        }
        StudyOpWechatAccount studyOpWechatAccount = crmStudyTogetherService.$getOpWechatAccountById(id);
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("id", studyOpWechatAccount.getId());
        returnMap.put("lessonId", studyOpWechatAccount.getStudyLessonId());
        returnMap.put("self_wechat", studyOpWechatAccount.getWechatNumber());
        returnMap.put("wechat_code", StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + studyOpWechatAccount.getQrCodeUrl());
        returnMap.put("wechat_code_file", studyOpWechatAccount.getQrCodeUrl());
        returnMap.put("accountType", studyOpWechatAccount.getAccountType().name());

        return MapMessage.successMessage().add("opWechatAccount", returnMap);
    }

    /**
     * 保存微信信息
     */
    @RequestMapping(value = "/saveOpWechatAccount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveOpWechatAccount() {
        String id = getRequestString("id");
        String weChatNum = getRequestString("weChatNum");
        String codeUrl = getRequestString("codeUrl");
        String accountType = getRequestString("accountType");
        String lessonId = getRequestString("lessonId");
        String classArea = getRequestString("classArea");

        StudyOpWechatAccount studyOpWechatAccount = null;

        if (StringUtils.isNotBlank(id)) {
            studyOpWechatAccount = crmStudyTogetherService.$getOpWechatAccountById(id);
        }

        if (studyOpWechatAccount == null) {
            studyOpWechatAccount = new StudyOpWechatAccount();
        }

        studyOpWechatAccount.setAccountType(StudyOpWechatAccount.AccountType.valueOf(accountType));
        studyOpWechatAccount.setWechatNumber(weChatNum);
        studyOpWechatAccount.setQrCodeUrl(codeUrl);
        studyOpWechatAccount.setStudyLessonId(lessonId);
        studyOpWechatAccount.setClassArea(classArea);

        crmStudyTogetherService.$saveOpWechatList(studyOpWechatAccount);

        return MapMessage.successMessage();
    }

    /**
     * 上传微信二维码
     */
    @RequestMapping(value = "/uploadQrCode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadFileTo17pmcOss(MultipartFile inputFile) throws IOException {
        if (inputFile == null) {
            return MapMessage.errorMessage("没有可上传的文件");
        }
        String suffix = StringUtils.substringAfterLast(inputFile.getOriginalFilename(), ".");
        if (StringUtils.isBlank(suffix)) {
            suffix = "jpg";
        }
        StorageMetadata storageMetadata = new StorageMetadata();
        storageMetadata.setContentLength(inputFile.getSize());
        String env = "weChatQrCode/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "weChatQrCode/test/";
        }
        String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
        String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
        String realName = storageClient.upload(inputFile.getInputStream(), fileName, path, storageMetadata);
        String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
        return MapMessage.successMessage().add("imgName", realName).add("imgUrl", fileUrl);
    }


    /**
     * 进入用户管理页面
     * 2018-12-05：需要添加两列：学分&学分修改
     */
    @RequestMapping(value = "/studentLessonRefPage.vpage", method = RequestMethod.GET)
    public String studentLessonRefPage(Model model) {
        Long sid = getRequestLong("sid");
        String clazzId = getRequestString("clazzId");
        String lessonId = getRequestString("lessonId");
        Integer pageNum = getRequestInt("page", 1);
        String clazzLevelStr = getRequestString("clazzLevel");
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        if (StringUtils.isBlank(lessonId) && StringUtils.isBlank(clazzId) && sid == 0L) {
            model.addAttribute("content", new ArrayList<>());
            model.addAttribute("clazzMap", new HashMap<>());
            model.addAttribute("currentPage", pageNum);
            model.addAttribute("totalPage", 0);
            model.addAttribute("hasPrev", false);
            model.addAttribute("hasNext", false);
            model.addAttribute("clazzLevelError", "年级筛选必须与其他条件结合使用");
            return "opmanager/studyTogether/studentLessonRefPage";
        }
        Integer clazzLevel = SafeConverter.toInt(clazzLevelStr);
        Page<StudyGroupStudentRef> groupStudentRefs = crmStudyTogetherService.$loadByStudentLessonAndClazz(sid, clazzId, lessonId, clazzLevel, pageRequest);
        Map<String, StudyGroup> clazzMap = new HashMap<>();
        Map<String, String> studentNameMap = new HashMap<>();
        Map<String, Integer> coinCountMap = new HashMap<>();
        Map<String, Long> scoreCountMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(groupStudentRefs.getContent())) {
            List<StudyGroupStudentRef> content = groupStudentRefs.getContent();
            List<String> clazzIds = content.stream().map(StudyGroupStudentRef::getStudyGroupId).collect(Collectors.toList());
            clazzMap = crmStudyTogetherService.$getStudyGroupByIds(clazzIds);
            List<Long> sids = content.stream().map(StudyGroupStudentRef::getStudentId).collect(Collectors.toList());
            studentNameMap = studentLoaderClient.loadStudents(sids).values().stream().collect(Collectors.toMap(e -> SafeConverter.toString(e.getId()), Student::fetchRealname));
            Map<Long, Coin> coinMap = dpCoinLoader.loadCoins(sids);
            coinCountMap = coinMap.values().stream()
                    .collect(Collectors.toMap(coin -> SafeConverter.toString(coin.getId()), Coin::getTotalCount));

            //添加学分记录
            Set<String> ids = new HashSet<>();
            sids.forEach(e -> ids.add(UserCredit.generateId(e, SafeConverter.toLong(lessonId))));
            Map<String, UserCredit> creditMap = studyCourseUserStatisticsDataLoader.loadUserCreditByIds(ids);
            if (MapUtils.isNotEmpty(creditMap)) {
                scoreCountMap = creditMap.values().stream().collect(Collectors.toMap(UserCredit::getId, UserCredit::getCredit));
            }
        }

        List<CoinType> types = coinTypeBufferLoaderClient.getCoinTypes().stream().filter(CoinType::getManual).collect(Collectors.toList());
        Map<String, String> coinTypeMap = types.stream().collect(Collectors.toMap(coinType -> SafeConverter.toString(coinType.getId()), CoinType::getName));
        Map<String, String> typeCountMap = new HashMap<>();
        for (CoinType coinType : types) {
            String id = SafeConverter.toString(coinType.getId());
            String count = coinType.getOpType() == CoinOperationType.increase ? SafeConverter.toString(coinType.getCount()) : "-" + coinType.getCount();
            typeCountMap.put(id, count);
        }

        model.addAttribute("content", groupStudentRefs.getContent());
        model.addAttribute("clazzMap", clazzMap);
        model.addAttribute("studentNameMap", studentNameMap);
        model.addAttribute("coinCountMap", coinCountMap);
        model.addAttribute("coinTypeMap", coinTypeMap);
        model.addAttribute("scoreMap", scoreCountMap);
        model.addAttribute("typeCountMap", typeCountMap);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", groupStudentRefs.getTotalPages());
        model.addAttribute("hasPrev", groupStudentRefs.hasPrevious());
        model.addAttribute("hasNext", groupStudentRefs.hasNext());
        model.addAttribute("sid", sid != 0L ? sid : "");
        model.addAttribute("clazzId", clazzId);
        model.addAttribute("lessonId", lessonId);
        model.addAttribute("clazzLevel", clazzLevel);
        return "opmanager/studyTogether/studentLessonRefPage";
    }

    /**
     * 换班操作
     */
    @RequestMapping(value = "upsertStudyGroup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage upsertStudyGroup() {
        String ids = getRequestString("ids");
        String clazzId = getRequestString("clazzId");
        String lessonId = getRequestString("lessonId");
        if (StringUtils.isBlank(ids) || StringUtils.isBlank(clazzId)) {
            return MapMessage.errorMessage("班级id或学生id为空");
        }
        String[] idArray = ids.split(",");
        List<String> idList = Arrays.asList(idArray);
        List<StudyGroup> studyGroups = crmStudyTogetherService.$getStudyGroupByLessonId(lessonId);
        List<String> clazzIds = studyGroups.stream().map(StudyGroup::getId).collect(Collectors.toList());
        if (!clazzIds.contains(clazzId)) {
            return MapMessage.errorMessage("所换班级ID不属于原课程");
        }
        for (String id : idList) {
            String oldClazzId;
            StudyGroupStudentRef studyGroupStudentRef = crmStudyTogetherService.loadStudyGroupStudentRefById(id);
            if (studyGroupStudentRef == null) {
                continue;
            }
            oldClazzId = studyGroupStudentRef.getStudyGroupId();
            studyGroupStudentRef.setStudyGroupId(clazzId);
            StudyGroupStudentRef upsert = crmStudyTogetherService.upsertStudyGroupStudentRef(studyGroupStudentRef);
            if (upsert != null) {
                crmStudyTogetherService.sendChangeClazzMessage(lessonId, oldClazzId, upsert.getStudyGroupId(), upsert.getStudentId());
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 单个学生加币
     */
    @RequestMapping(value = "addCoin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addCoin() {
        Long studentId = getRequestLong("studentId");
        if (studentId == 0) {
            return MapMessage.errorMessage("学生id错误");
        }
        Integer coinType = getRequestInt("coinType");
        CoinType type = coinTypeBufferLoaderClient.getCoinType(coinType);
        if (type == null) {
            return MapMessage.errorMessage("学币类型不存在");
        }
        CoinHistory history = new CoinHistoryBuilder().withUserId(studentId)
                .withType(coinType)
                .withOperator(getCurrentAdminUser().getAdminUserName())
                .build();
        return dpCoinService.changeCoin(history);
    }

    /**
     * 一键班级加币
     */
    @RequestMapping(value = "clazzAddCoin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clazzAddCoin() {
        String clazzId = getRequestString("clazzId");
        String lessonId = getRequestString("lessonId");
        Integer coinType = getRequestInt("clazzCoinType");
        CoinType type = coinTypeBufferLoaderClient.getCoinType(coinType);
        if (type == null) {
            return MapMessage.errorMessage("学习币类型错误");
        }

        if (StringUtils.isBlank(clazzId)) {
            return MapMessage.errorMessage("班级id不能为空");
        }
        if (StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage("课程id不能为空");
        }
        PageRequest pageRequest = new PageRequest(0, 1000);
        Page<StudyGroupStudentRef> refPage = crmStudyTogetherService.$loadByStudentLessonAndClazz(null, clazzId, lessonId, pageRequest);
        if (refPage == null || CollectionUtils.isEmpty(refPage.getContent())) {
            return MapMessage.errorMessage("未查询到学生信息");
        }
        Set<Long> studentIds = refPage.getContent().stream().map(StudyGroupStudentRef::getStudentId).collect(Collectors.toSet());
        String message = "一键班级加币成功";
        List<Map<String, Object>> failList = new ArrayList<>();
        Date startDate = new Date();
        String operator = getCurrentAdminUser().getAdminUserName();
        for (Long studentId : studentIds) {
            CoinHistory history = new CoinHistoryBuilder().withUserId(studentId)
                    .withType(coinType)
                    .withOperator(operator)
                    .build();
            MapMessage mapMessage = dpCoinService.changeCoin(history);
            if (!mapMessage.isSuccess()) {
                Map<String, Object> failInfo = new HashMap<>();
                failInfo.put("studentId", studentId);
                failInfo.put("coinType", coinType);
                failInfo.put("reason", mapMessage.getInfo());
                failList.add(failInfo);
                message = "一键加币有失败数据，请在加币进度中查看";
            }
        }
        Date endDate = new Date();
        String fileUrl = "";
        if (CollectionUtils.isNotEmpty(failList)) {
            fileUrl = generateExcel(failList);
        }
        //批量加币进度记录
        CoinImportHistory importHistory = new CoinImportHistory();
        importHistory.setId(RandomUtils.nextObjectId());
        importHistory.setStartDate(startDate);
        importHistory.setEndDate(endDate);
        importHistory.setAddType(BatchAddCoinType.BATCH_CLAZZ_IMPORT);
        importHistory.setOperator(operator);
        importHistory.setUrl(fileUrl);
        dpCoinService.saveImportHistory(importHistory);

        return MapMessage.successMessage(message);
    }

    /**
     * 批量导入加币
     */
    @RequestMapping(value = "batchAddCoin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchAddCoin() {
        //批量加币进度记录
        CoinImportHistory importHistory = new CoinImportHistory();
        XSSFWorkbook xssfWorkbook = readExcel("coin_file", importHistory);
        if (xssfWorkbook == null) {
            return MapMessage.errorMessage("不支持的文件类型或者上传失败");
        }
        List<Map<String, Object>> coinInfoMap = getCoinInfoList(xssfWorkbook);
        String message = "批量加币成功";
        List<Map<String, Object>> failList = new ArrayList<>();
        Map<String, Object> failMap;
        Date startDate = new Date();
        String operator = getCurrentAdminUser().getAdminUserName();
        for (Map<String, Object> map : coinInfoMap) {
            Long studentId = SafeConverter.toLong(map.get("studentId"));
            Integer coinType = SafeConverter.toInt(map.get("coinType"));
            CoinType type = coinTypeBufferLoaderClient.getCoinType(coinType);
            if (type == null) {
                failMap = new HashMap<>();
                failMap.put("studentId", studentId);
                failMap.put("coinType", coinType);
                failMap.put("reason", "学币类型错误");
                failList.add(failMap);
                continue;
            }
            CoinHistory history = new CoinHistoryBuilder().withUserId(studentId)
                    .withType(coinType)
                    .withOperator(operator)
                    .build();
            MapMessage mapMessage = dpCoinService.changeCoin(history);
            if (!mapMessage.isSuccess()) {
                Map<String, Object> failInfo = new HashMap<>();
                failInfo.put("studentId", studentId);
                failInfo.put("coinType", coinType);
                failInfo.put("reason", mapMessage.getInfo());
                failList.add(failInfo);
                message = "批量加币有失败数据，请在加币进度中查看";
            }
        }
        Date endDate = new Date();
        String fileUrl = "";
        if (CollectionUtils.isNotEmpty(failList)) {
            fileUrl = generateExcel(failList);
        }
        importHistory.setId(RandomUtils.nextObjectId());
        importHistory.setStartDate(startDate);
        importHistory.setEndDate(endDate);
        importHistory.setAddType(BatchAddCoinType.BATCH_IMPORT);
        importHistory.setOperator(operator);
        importHistory.setUrl(fileUrl);
        dpCoinService.saveImportHistory(importHistory);
        return MapMessage.successMessage(message);
    }

    @RequestMapping(value = "templateExcel.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage generateTemplateFile() {
        String templateUrl;
        String cacheKey = "COIN_TEMPLATE_FILE";
        CacheObject<String> cacheObject = CacheSystem.CBS.getCache("persistence").get(cacheKey);
        if (cacheObject != null && cacheObject.getValue() != null) {
            templateUrl = SafeConverter.toString(cacheObject.getValue());
            return MapMessage.successMessage().add("templateUrl", templateUrl);
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("学生ID");
        row.createCell(1).setCellValue("学习币类型");
        row = sheet.createRow(6);

        XSSFFont font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        Cell cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue("备注：1.导入去掉表头和备注");

        row = sheet.createRow(7);
        cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellValue("2.学习币类型说明：");
        row.createCell(1).setCellValue("学习币类型ID");
        row.createCell(2).setCellValue("学习币类型");
        row.createCell(3).setCellValue("学习币数值");
        int rowNum = 8;
        List<CoinType> coinTypes = coinTypeBufferLoaderClient.getCoinTypes()
                .stream()
                .filter(CoinType::getManual)
                .sorted(Comparator.comparingInt(CoinType::getId))
                .collect(Collectors.toList());
        for (CoinType coinType : coinTypes) {
            row = sheet.createRow(rowNum);
            row.createCell(1).setCellValue(coinType.getId());
            row.createCell(2).setCellValue(coinType.getName());
            String count = CoinOperationType.increase == coinType.getOpType() ? SafeConverter.toString(coinType.getCount()) : "-" + coinType.getCount();
            row.createCell(3).setCellValue(count);
            rowNum++;
        }

        try {
            @Cleanup ByteArrayOutputStream os = new ByteArrayOutputStream();
            workbook.write(os);
            byte[] content = os.toByteArray();
            @Cleanup InputStream is = new ByteArrayInputStream(content);
            String env = "coin/";
            if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
                env = "coin/test/";
            }
            String fileName = "学习币导入模板.xls";
            String realName = storageClient.upload(is, fileName, env);
            templateUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
            CacheSystem.CBS.getCache("persistence").set(cacheKey, 0, templateUrl);
            return MapMessage.successMessage().add("templateUrl", templateUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return MapMessage.errorMessage("获取模板文件失败, excp:{}", e);
        }
    }

    /**
     * 加币进度
     */
    @RequestMapping(value = "coinImportHistory.vpage", method = RequestMethod.GET)
    public String importHistory(Model model) {
        int page = getRequestInt("page", 1);
        Pageable pageable = new PageRequest(page - 1, 10);
        List<CoinImportHistory> histories = dpCoinLoader.loadImportHistories().stream()
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());
        List<CoinImportHistoryMapper> mappers = new ArrayList<>();
        histories.forEach(history -> mappers.add(convert(history)));
        Page<CoinImportHistoryMapper> mapperPage = PageableUtils.listToPage(mappers, pageable);
        model.addAttribute("mapperPage", mapperPage);
        model.addAttribute("currentPage", mapperPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", mapperPage.getTotalPages());
        model.addAttribute("hasPrev", mapperPage.hasPrevious());
        model.addAttribute("hasNext", mapperPage.hasNext());
        return "opmanager/studyTogether/coinImportHistory";
    }

    /**
     * 学币历史
     */
    @RequestMapping(value = "coinHistory.vpage", method = RequestMethod.GET)
    public String coinHistory(Model model) {
        int page = getRequestInt("page", 1);
        Long studentId = getRequestLong("studentId");
        Pageable pageable = new PageRequest(page - 1, 10);
        List<CoinHistory> histories = dpCoinLoader.loadHistories(studentId).stream()
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());
        List<CoinHistoryMapper> mappers = new ArrayList<>();
        histories.forEach(history -> mappers.add(convert(history)));
        Page<CoinHistoryMapper> mapperPage = PageableUtils.listToPage(mappers, pageable);
        model.addAttribute("mapperPage", mapperPage);
        model.addAttribute("currentPage", mapperPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", mapperPage.getTotalPages());
        model.addAttribute("hasPrev", mapperPage.hasPrevious());
        model.addAttribute("hasNext", mapperPage.hasNext());
        model.addAttribute("studentId", studentId);
        return "opmanager/studyTogether/coinHistory";
    }

    /**
     * 学分历史
     */
    @RequestMapping(value = "scoreHistory.vpage", method = RequestMethod.GET)
    public String scoreHistory(Model model) {
        int page = getRequestInt("page", 1);
        Long studentId = getRequestLong("studentId");
        Long skuId = getRequestLong("skuId");
        Pageable pageable = new PageRequest(page - 1, 10);

        List<UserCreditHistory> histories = studyCourseUserStatisticsDataLoader
                .loadUserCreditHistoriesByUserIdAndSkuId(studentId, skuId).stream()
                .sorted((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate()))
                .collect(Collectors.toList());
        Page<UserCreditHistory> mapperPage = PageableUtils.listToPage(histories, pageable);

        model.addAttribute("mapperPage", mapperPage);
        model.addAttribute("currentPage", mapperPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", mapperPage.getTotalPages());
        model.addAttribute("hasPrev", mapperPage.hasPrevious());
        model.addAttribute("hasNext", mapperPage.hasNext());
        model.addAttribute("studentId", studentId);
        model.addAttribute("skuId", skuId);
        return "opmanager/studyTogether/scoreHistory";
    }

    private CoinImportHistoryMapper convert(CoinImportHistory history) {
        CoinImportHistoryMapper mapper = new CoinImportHistoryMapper();
        mapper.setOpType(history.getAddType().getDesc());
        mapper.setFileName(SafeConverter.toString(history.getFileName(), ""));
        mapper.setStartDate(DateUtils.dateToString(history.getStartDate(), "yyyy-MM-dd HH:mm:ss"));
        mapper.setEndDate(DateUtils.dateToString(history.getEndDate(), "yyyy-MM-dd HH:mm:ss"));
        mapper.setOperator(history.getOperator());
        mapper.setUrl(history.getUrl());
        return mapper;
    }

    private CoinHistoryMapper convert(CoinHistory history) {
        CoinHistoryMapper mapper = new CoinHistoryMapper();
        Integer typeCode = history.getType();
        CoinType coinType = coinTypeBufferLoaderClient.getCoinType(typeCode);
        if (coinType != null) {
            mapper.setCoinType(coinType.getName());
            mapper.setCount(coinType.getOpType() == CoinOperationType.increase ? SafeConverter.toString(history.getCount()) : "-" + history.getCount());
        }
        mapper.setCreateTime(DateUtils.dateToString(history.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        mapper.setOperator(history.getOperator());
        return mapper;
    }

    /**
     * 进入数据管理页面
     */
    @RequestMapping(value = "/getStudyData.vpage", method = RequestMethod.GET)
    public String getStudyData(Model model) {
        String lessonId = getRequestString("selectLessonId");
        String wechat = getRequestString("wechat");
        String startDate = getRequestString("startTime");
        String endDate = getRequestString("endTime");
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        List<String> lessonIds = getAllLessonId();
        if (CollectionUtils.isNotEmpty(lessonIds)) {
            model.addAttribute("lessonIds", lessonIds);
        }
        if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)) {
            model.addAttribute("content", new ArrayList<>());
            model.addAttribute("currentPage", pageNum);
            model.addAttribute("totalPage", 0);
            model.addAttribute("hasPrev", false);
            model.addAttribute("hasNext", false);
            return "opmanager/studyTogether/studyDataPage";
        }
        Date startTime = DateUtils.stringToDate(startDate);
        Date endTime = DateUtils.stringToDate(endDate);
        Page<CrmStudyTogetherDataMapper> studyTogetherData = crmStudyTogetherService.getStudyTogetherData(lessonId, wechat, startTime, endTime, pageRequest);
        model.addAttribute("content", studyTogetherData.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", studyTogetherData.getTotalPages());
        model.addAttribute("hasPrev", studyTogetherData.hasPrevious());
        model.addAttribute("hasNext", studyTogetherData.hasNext());
        model.addAttribute("selectLessonId", lessonId);
        model.addAttribute("wechat", wechat);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "opmanager/studyTogether/studyDataPage";
    }

    /**
     * 测试时添加课程Id用
     */
    @RequestMapping(value = "/addLessonId.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage addLessonIdForTest() {
        String lessonId = getRequestString("lessonId");
        if (RuntimeMode.le(Mode.TEST)) {
            crmStudyTogetherService.addLessonId(lessonId);
        }
        return MapMessage.successMessage();
    }

    /**
     * 获取push推送文案的配置
     */
    @RequestMapping(value = "/getPushText.vpage", method = RequestMethod.GET)
    public String getPushText(Model model) {
        List<String> lessonIds = getAllLessonId();
        if (CollectionUtils.isEmpty(lessonIds)) {
            return "opmanager/studyTogether/pushTextEdit";
        }
        Map<String, StudyTogetherJobText> jobTextMap = new HashMap<>();
        List<StudyTogetherJobText> jobText = crmStudyTogetherService.getJobText();
        if (CollectionUtils.isNotEmpty(jobText)) {
            jobTextMap = jobText.stream().collect(Collectors.toMap(StudyTogetherJobText::getLessonId, Function.identity()));
        }
        model.addAttribute("content", jobTextMap);
        model.addAttribute("lessonIds", lessonIds);
        return "opmanager/studyTogether/pushTextEdit";

    }

    /**
     * 获取push推送文案的配置
     */
    @RequestMapping(value = "/savePushText.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage savePushText() {
        String jobTextList = getRequestString("jobTextList");
        if (StringUtils.isBlank(jobTextList)) {
            return MapMessage.errorMessage();
        }
        List<StudyTogetherJobText> studyTogetherJobTexts = JsonUtils.fromJsonToList(jobTextList, StudyTogetherJobText.class);
        if (CollectionUtils.isEmpty(studyTogetherJobTexts)) {
            return MapMessage.errorMessage();
        }
        crmStudyTogetherService.saveJobText(studyTogetherJobTexts);
        return MapMessage.successMessage();
    }

    /**
     * 导出个人微信号信息
     */
    @RequestMapping(value = "/exportWechatData.vpage", method = RequestMethod.GET)
    public void exportWechatData() throws Exception {
        String lessonId = getRequestString("selectLessonId");
        String accountType = getRequestString("accountType");
        String wechat = getRequestString("wechat");
        if (StringUtils.isBlank(lessonId)) {
            return;
        }
        String fileName = "个人微信号信息-" + DateUtils.dateToString(new Date(), "yyyy-MM-dd-HH-mm-ss") + ".xlsx";
        List<List<String>> dataList = generateWechatExportData(lessonId, accountType, wechat);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        if (CollectionUtils.isNotEmpty(dataList)) {
            String[] dateDataTitle = new String[]{
                    "序号", "个人微信号", "被报名次数", "个人微信二维码",
                    "组别", "班级区"
            };
            int[] dateDataWidth = new int[]{
                    5000, 5000, 5000, 5000,
                    5000, 5000
            };
            try {
                xssfWorkbook = XssfUtils.convertToXSSFWorkbook(dateDataTitle, dateDataWidth, dataList, "没有数据");
            } catch (Exception e) {
                logger.error("generate studyTogether wechat info xlsx error!", e);
            }
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try {
                xssfWorkbook.write(outStream);
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        fileName,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException e) {
                logger.error("download studyTogether wechat info error!", e);
            }
        }
    }

    /**
     * 导出学生、班级关系数据
     */
    @RequestMapping(value = "/exportStudentLessonData.vpage", method = RequestMethod.GET)
    public void exportStudentLessonData() throws Exception {
        Long sid = getRequestLong("sid");
        String clazzId = getRequestString("clazzId");
        String lessonId = getRequestString("lessonId");
        String clazzLevelStr = getRequestString("clazzLevel");
        Pageable pageRequest = new PageRequest(0, 100);
        if (StringUtils.isBlank(lessonId) && StringUtils.isBlank(clazzId) && sid == 0L) {
            return;
        }
        Integer clazzLevel = SafeConverter.toInt(clazzLevelStr);
        List<StudyGroupStudentRef> studyGroupStudentRefList = new ArrayList<>();
        Map<String, StudyGroup> clazzMap = new HashMap<>();
        Map<String, String> studentNameMap = new HashMap<>();
        Page<StudyGroupStudentRef> groupStudentRefs = crmStudyTogetherService.$loadByStudentLessonAndClazz(sid, clazzId, lessonId, clazzLevel, pageRequest);
        if (groupStudentRefs.getTotalElements() == 0) {
            return;
        }
        List<StudyGroupStudentRef> content = groupStudentRefs.getContent();
        studyGroupStudentRefList.addAll(content);
        List<String> clazzIds = content.stream().map(StudyGroupStudentRef::getStudyGroupId).collect(Collectors.toList());
        clazzMap.putAll(crmStudyTogetherService.$getStudyGroupByIds(clazzIds));
        List<Long> sids = content.stream().map(StudyGroupStudentRef::getStudentId).collect(Collectors.toList());
        studentNameMap.putAll(studentLoaderClient.loadStudents(sids).values().stream().collect(Collectors.toMap(e -> SafeConverter.toString(e.getId()), Student::fetchRealname)));
        while (groupStudentRefs.hasNext()) {
            pageRequest = groupStudentRefs.nextPageable();
            groupStudentRefs = crmStudyTogetherService.$loadByStudentLessonAndClazz(sid, clazzId, lessonId, clazzLevel, pageRequest);
            if (CollectionUtils.isNotEmpty(groupStudentRefs.getContent())) {
                content = groupStudentRefs.getContent();
                studyGroupStudentRefList.addAll(content);
                clazzIds = content.stream().map(StudyGroupStudentRef::getStudyGroupId).collect(Collectors.toList());
                clazzMap.putAll(crmStudyTogetherService.$getStudyGroupByIds(clazzIds));
                sids = content.stream().map(StudyGroupStudentRef::getStudentId).collect(Collectors.toList());
                studentNameMap.putAll(studentLoaderClient.loadStudents(sids).values().stream().collect(Collectors.toMap(e -> SafeConverter.toString(e.getId()), Student::fetchRealname)));
            }
        }
        if (CollectionUtils.isEmpty(studyGroupStudentRefList)) {
            return;
        }
        String fileName = "学生班级信息-" + DateUtils.dateToString(new Date(), "yyyy-MM-dd-HH-mm-ss") + ".xlsx";
        List<List<String>> exportData = generateStudentLessonExportData(studyGroupStudentRefList, clazzMap, studentNameMap);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        if (CollectionUtils.isNotEmpty(exportData)) {
            String[] dateDataTitle = new String[]{
                    "序号", "家长Id", "学生Id", "学生姓名",
                    "年级", "课程Id", "班级Id", "微信群名称", "激活时间"
            };
            int[] dateDataWidth = new int[]{
                    5000, 5000, 5000, 5000,
                    5000, 5000, 5000, 5000, 5000
            };
            try {
                xssfWorkbook = XssfUtils.convertToXSSFWorkbook(dateDataTitle, dateDataWidth, exportData, "没有数据");
            } catch (Exception e) {
                logger.error("generate studyTogether studentClazz info xlsx error!", e);
            }
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try {
                xssfWorkbook.write(outStream);
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        fileName,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException e) {
                logger.error("download studyTogether studentClazz info error!", e);
            }
        }

    }

    /**
     * 导出班级信息
     */
    @RequestMapping(value = "/exportClazzData.vpage", method = RequestMethod.GET)
    public void exportClazzData() throws Exception {
        String lessonId = getRequestString("selectLessonId");
        String wechat = getRequestString("wechat");
        PageRequest pageRequest = new PageRequest(0, 100);
        String wechatName = getRequestString("wechatName");
        String wechatId = "";
        if (StringUtils.isNotBlank(wechat)) {
            StudyOpWechatAccount wechatAccount = crmStudyTogetherService.getWechatInfoByWechatNumber(wechat, lessonId);
            if (wechatAccount != null) {
                wechatId = wechatAccount.getId();
            }
        }
        List<StudyGroup> studyGroups = new ArrayList<>();
        Map<String, StudyOpWechatAccount> wechatInfos = new HashMap<>();
        Page<StudyGroup> studyGroupsPage = crmStudyTogetherService.loadLessonStudyGroupList(lessonId, wechatId, pageRequest, wechatName);
        if (studyGroupsPage.getTotalElements() == 0) {
            return;
        }
        Map<String, GroupArea> areaMap = new HashMap<>();
        List<GroupArea> groupAreaList = crmStudyTogetherService.getGroupAreaFromBufferByLessonId(lessonId);
        areaMap = groupAreaList.stream().collect(Collectors.toMap(GroupArea::getId, Function.identity()));
        studyGroups.addAll(studyGroupsPage.getContent());
        List<String> ids = studyGroups.stream().filter(e -> StringUtils.isNotBlank(e.getWechatId())).map(StudyGroup::getWechatId).collect(Collectors.toList());
        wechatInfos.putAll(crmStudyTogetherService.$getOpWechatAccountByIds(ids));
        while (studyGroupsPage.hasNext()) {
            pageRequest = new PageRequest(studyGroupsPage.nextPageable().getPageNumber(), studyGroupsPage.nextPageable().getPageSize());
            studyGroupsPage = crmStudyTogetherService.loadLessonStudyGroupList(lessonId, wechatId, pageRequest, wechatName);
            if (CollectionUtils.isNotEmpty(studyGroupsPage.getContent())) {
                studyGroups.addAll(studyGroupsPage.getContent());
                ids = studyGroupsPage.getContent().stream().filter(e -> StringUtils.isNotBlank(e.getWechatId())).map(StudyGroup::getWechatId).collect(Collectors.toList());
                wechatInfos.putAll(crmStudyTogetherService.$getOpWechatAccountByIds(ids));
            }
        }
        if (CollectionUtils.isEmpty(studyGroups)) {
            return;
        }
        String fileName = "班级信息-" + DateUtils.dateToString(new Date(), "yyyy-MM-dd-HH-mm-ss") + ".xlsx";
        List<List<String>> exportData = generateStudyLessonExportData(studyGroups, wechatInfos, areaMap);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        if (CollectionUtils.isNotEmpty(exportData)) {
            String[] dateDataTitle = new String[]{
                    "班级Id", "微信群名称", "个人微信号", "课程激活码", "课程激活链接", "班级区"
            };
            int[] dateDataWidth = new int[]{
                    5000, 5000, 5000, 5000, 5000, 5000
            };
            try {
                xssfWorkbook = XssfUtils.convertToXSSFWorkbook(dateDataTitle, dateDataWidth, exportData, "没有数据");
            } catch (Exception e) {
                logger.error("generate studyTogether clazz info xlsx error!", e);
            }
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try {
                xssfWorkbook.write(outStream);
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        fileName,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException e) {
                logger.error("download studyTogether clazz info error!", e);
            }
        }
    }


    /**
     * 导出统计数据
     */
    @RequestMapping(value = "/exportData.vpage", method = RequestMethod.GET)
    public void exportData() throws Exception {
        String lessonId = getRequestString("selectLessonId");
        String wechat = getRequestString("wechat");
        String startDate = getRequestString("startTime");
        String endDate = getRequestString("endTime");
        Pageable pageRequest = new PageRequest(0, 100);
        if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)) {
            return;
        }
        Date startTime = DateUtils.stringToDate(startDate);
        Date endTime = DateUtils.stringToDate(endDate);
        List<CrmStudyTogetherDataMapper> studyTogetherDataMapperList = new ArrayList<>();
        Page<CrmStudyTogetherDataMapper> studyTogetherData = crmStudyTogetherService.getStudyTogetherData(lessonId, wechat, startTime, endTime, pageRequest);
        if (studyTogetherData.getTotalElements() == 0) {
            return;
        }
        studyTogetherDataMapperList.addAll(studyTogetherData.getContent());
        while (studyTogetherData.hasNext()) {
            pageRequest = studyTogetherData.nextPageable();
            studyTogetherData = crmStudyTogetherService.getStudyTogetherData(lessonId, wechat, startTime, endTime, pageRequest);
            studyTogetherDataMapperList.addAll(studyTogetherData.getContent());
        }
        if (CollectionUtils.isEmpty(studyTogetherDataMapperList)) {
            return;
        }
        String fileName = "统计数据信息-" + DateUtils.dateToString(new Date(), "yyyy-MM-dd-HH-mm-ss") + ".xlsx";
        List<List<String>> exportData = generateDataList(studyTogetherDataMapperList);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        if (CollectionUtils.isNotEmpty(exportData)) {
            String[] dateDataTitle = new String[]{
                    "个人微信号", "被报名次数", "微信群名称", "班级Id",
                    "课程激活码", "激活人数"
            };
            int[] dateDataWidth = new int[]{
                    5000, 5000, 5000, 5000,
                    5000, 5000
            };
            try {
                xssfWorkbook = XssfUtils.convertToXSSFWorkbook(dateDataTitle, dateDataWidth, exportData, "没有数据");
            } catch (Exception e) {
                logger.error("generate studyTogether data info xlsx error!", e);
            }
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try {
                xssfWorkbook.write(outStream);
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        fileName,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException e) {
                logger.error("download studyTogether data info error!", e);
            }
        }

    }

    private StudyLesson getStudyLesson(String lessonId){
        return studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLesson(SafeConverter.toLong(lessonId));
    }
    /**
     * 导出班级区数据
     */
    @RequestMapping(value = "/exportgroupAreaData.vpage", method = RequestMethod.GET)
    public void exportgroupAreaData() throws Exception {
        String lessonId = getRequestString("selectLessonId");
        if (StringUtils.isBlank(lessonId)) {
            return;
        }
        String fileName = "班级区信息-" + DateUtils.dateToString(new Date(), "yyyy-MM-dd-HH-mm-ss") + ".xlsx";
        StudyLesson lessonById = getStudyLesson(lessonId);
        List<List<String>> dataList = generateGroupAreaExportData(lessonById);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        if (CollectionUtils.isNotEmpty(dataList)) {
            String[] dateDataTitle = new String[]{
                    "序号", "课程ID", "课程名称", "期数",
                    "班级区ID", "班级区名称"
            };
            int[] dateDataWidth = new int[]{
                    5000, 5000, 5000, 5000,
                    5000, 5000
            };
            try {
                xssfWorkbook = XssfUtils.convertToXSSFWorkbook(dateDataTitle, dateDataWidth, dataList, "没有数据");
            } catch (Exception e) {
                logger.error("generate studyTogether area info xlsx error!", e);
            }
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try {
                xssfWorkbook.write(outStream);
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        fileName,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException e) {
                logger.error("download studyTogether wechat info error!", e);
            }
        }
    }

    /**
     * 导入班级数据
     */
    @RequestMapping(value = "/importClazzData.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importClazzData() {
        XSSFWorkbook workbook = readExcel("source_file", null);
        if (workbook == null) {
            return MapMessage.errorMessage("不支持的文件类型或者上传失败");
        }
        String validateMessage = validateImportClazzData(workbook);
        if (!SafeConverter.toBoolean(validateMessage)) {
            return MapMessage.errorMessage(validateMessage);
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return MapMessage.errorMessage().add("error", "sheet读取失败");
        }
        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            String wechatGroupName = "";
            String wechatId = "";
            String lessonId = "";
            String wechatnumber = "";
            for (Cell cell : row) {
                if (cell.getColumnIndex() == 0) {
                    wechatGroupName = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(wechatGroupName)) {
                        break;
                    }
                }
                if (cell.getColumnIndex() == 1) {
                    wechatnumber = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(wechatnumber)) {
                        break;
                    }
                }
                if (cell.getColumnIndex() == 2) {
                    lessonId = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(lessonId)) {
                        break;
                    }
                }
            }
            StudyOpWechatAccount opWechatAccount = crmStudyTogetherService.getWechatInfoByWechatNumber(wechatnumber, lessonId);
            if (opWechatAccount == null) {
                break;
            }
            wechatId = opWechatAccount.getId();
            StudyGroup newStudyGroup = crmStudyTogetherService.createNewStudyGroup(lessonId, wechatGroupName, wechatId);
            if (newStudyGroup != null) {
                String activeShortUrl = generateActiveShortUrl(newStudyGroup.getLessonId(), newStudyGroup.getVerifyCode());
                if (StringUtils.isNotBlank(activeShortUrl)) {
                    String realUrl = ShortUrlGenerator.getShortUrlSiteUrl() + "/" + activeShortUrl;
                    realUrl = realUrl.replace("http://", "https://");
                    crmStudyTogetherService.updateStudyGroupActiveUrl(newStudyGroup.getId(), realUrl);
                }
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 导入班级区数据
     */
    @RequestMapping(value = "/importGroupAreaData.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importGroupAreaData() {
        XSSFWorkbook workbook = readExcel("source_file", null);
        if (workbook == null) {
            return MapMessage.errorMessage("不支持的文件类型或者上传失败");
        }
        String validateMessage = validateImportGroupAreaData(workbook);
        if (!SafeConverter.toBoolean(validateMessage)) {
            return MapMessage.errorMessage(validateMessage);
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return MapMessage.errorMessage().add("error", "sheet读取失败");
        }
        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            String lessonId = "";
            String areaName = "";
            for (Cell cell : row) {
                if (cell.getColumnIndex() == 0) {
                    lessonId = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(lessonId)) {
                        break;
                    }
                }
                if (cell.getColumnIndex() == 1) {
                    areaName = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(areaName)) {
                        break;
                    }
                }
            }
            StudyLesson lessonById = getStudyLesson(lessonId);
            if (lessonById == null) {
                break;
            }
            GroupArea groupArea = new GroupArea();
            groupArea.setLessonId(lessonId);
            groupArea.setGroupAreaName(areaName);
            crmStudyTogetherService.upsertGroupArea(groupArea);
        }
        return MapMessage.successMessage();
    }

    /**
     * 批量更新班级管理的班级区数据
     */
    @RequestMapping(value = "/updateGroupAreaData.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateGroupAreaData() {
        XSSFWorkbook workbook = readExcel("source_file", null);
        if (workbook == null) {
            return MapMessage.errorMessage("不支持的文件类型或者上传失败");
        }
        String validateMessage = validateUpdateGroupAreaData(workbook);
        if (!SafeConverter.toBoolean(validateMessage)) {
            return MapMessage.errorMessage(validateMessage);
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return MapMessage.errorMessage().add("error", "sheet读取失败");
        }
        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            String groupId = "";
            String areaId = "";
            for (Cell cell : row) {
                if (cell.getColumnIndex() == 0) {
                    groupId = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(groupId)) {
                        break;
                    }
                }
                if (cell.getColumnIndex() == 2) {
                    areaId = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(areaId)) {
                        break;
                    }
                }
            }
            GroupArea groupAreaById = crmStudyTogetherService.getGroupAreaById(areaId);
            StudyGroup studyGroup = crmStudyTogetherService.$getStudyGroupByIds(Collections.singletonList(groupId)).get(groupId);
            if (groupAreaById == null || studyGroup == null) {
                break;
            }
            crmStudyTogetherService.updateStudyGroupArea(groupId, areaId);
        }
        return MapMessage.successMessage();
    }

    /**
     * 查询家长是否报名，学生是否激活
     */
    @RequestMapping(value = "/findJoinInfo.vpage", method = RequestMethod.GET)
    public String findJoinInfo(Model model) {
        List<String> lessonIds = getAllLessonId();
        if (CollectionUtils.isNotEmpty(lessonIds)) {
            model.addAttribute("lessonIds", lessonIds);
        }

        String lessonId = getRequestString("selectLessonId");
        model.addAttribute("selectLessonId", lessonId);
        String parentMobile = getRequestString("parentMobile");
        if (StringUtils.isBlank(parentMobile) || StringUtils.isBlank(lessonId)) {
            return "opmanager/studyTogether/parentJoinRecord";
        }
        parentMobile = StringUtils.trim(parentMobile);
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(parentMobile, UserType.PARENT);
        if (userAuthentication == null) {
            return "opmanager/studyTogether/parentJoinRecord";
        }

        List<ParentJoinLessonRef> parentJoinLessonRefs = new ArrayList<>();
        Long userId = userAuthentication.getId();
        if (lessonId.equals("all")) {
            Map<String, ParentJoinLessonRef> parentJoinLessonRefMap = studyTogetherServiceClient.loadParentJoinLessonRefs(userId);
            if (MapUtils.isEmpty(parentJoinLessonRefMap)) {
                return "opmanager/studyTogether/parentJoinRecord";
            }
            parentJoinLessonRefs.addAll(parentJoinLessonRefMap.values());
        } else {
            ParentJoinLessonRef joinRecordById = crmStudyTogetherService.getJoinRecordById(ParentJoinLessonRef.generateId(userId, lessonId));
            if (null != joinRecordById) {
                parentJoinLessonRefs.add(joinRecordById);
            }
        }

        model.addAttribute("parentMobile", parentMobile);
        if (CollectionUtils.isEmpty(parentJoinLessonRefs)) {
            model.addAttribute("info", "未找到报名信息");
            return "opmanager/studyTogether/parentJoinRecord";
        }
        List<ParentJoinData> result = new ArrayList<>();
        Set<String> lessonIdSet = parentJoinLessonRefs.stream().map(ParentJoinLessonRef::getStudyLessonId).collect(Collectors.toSet());
        Map<String, StudyLesson> lessonMap = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getAllStudyLesson()
                .stream()
                .filter(e -> lessonIdSet.contains(SafeConverter.toString(e.getLessonId())))
                .collect(Collectors.toMap(x -> SafeConverter.toString(x.getLessonId()), Function.identity(), (u1, u2) -> u1));
        parentJoinLessonRefs.forEach(e -> {
            ParentJoinData bean = new ParentJoinData();
            bean.setParentId(e.getParentId());
            bean.setStudyLessonId(e.getStudyLessonId());
            bean.setCreateDate(DateUtils.dateToString(e.getCreateDate()));
            StudyLesson studyLesson = lessonMap.get(e.getStudyLessonId());
            bean.setLessonName(studyLesson != null ? studyLesson.getTitle() : "");
            StudyOpWechatAccount wechatAccount = crmStudyTogetherService.$getOpWechatAccountById(e.getSourceOpWechatId());
            if (wechatAccount != null) {
                bean.setWechatNumber(wechatAccount.getWechatNumber());
            }
            result.add(bean);
        });
        model.addAttribute("joinRecord", result);
        return "opmanager/studyTogether/parentJoinRecord";
    }

    //生成个人微信号导出数据
    private List<List<String>> generateWechatExportData(String lessonId, String accountType, String wechat) {

        List<List<String>> returnList = new ArrayList<>();
        List<StudyOpWechatAccount> studyOpWechatAccounts = crmStudyTogetherService.$getOpWechatListByLessonId(lessonId);
        if (StringUtils.isNotBlank(wechat)) {
            studyOpWechatAccounts = studyOpWechatAccounts.stream().filter(e -> StringUtils.equals(wechat, e.getWechatNumber())).collect(Collectors.toList());
        }
        List<StudyOpWechatAccount> finalStudyOpWechatAccounts = studyOpWechatAccounts;
        studyOpWechatAccounts
                .stream()
                .filter(e -> StringUtils.isBlank(accountType) || StringUtils.equals(e.getAccountType().name(), accountType))
                .sorted(Comparator.comparing(StudyOpWechatAccount::getCreateTime))
                .forEach(e -> {
                    List<String> list = new ArrayList<>();
                    list.add(SafeConverter.toString(finalStudyOpWechatAccounts.indexOf(e) + 1));
                    list.add(e.getWechatNumber());
                    list.add(SafeConverter.toString(e.getJoinCount()));
                    list.add(StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + e.getQrCodeUrl());
                    if (StudyOpWechatAccount.AccountType.NORMAL == e.getAccountType()) {
                        list.add("B端");
                    } else if (StudyOpWechatAccount.AccountType.CHANNELC == e.getAccountType()) {
                        list.add("C端");
                    }
                    list.add(e.getClassArea());
                    returnList.add(list);
                });

        return returnList;
    }

    //生成班级区导出数据
    private List<List<String>> generateGroupAreaExportData(StudyLesson lesson) {

        List<List<String>> returnList = new ArrayList<>();
        Pageable pageRequest = new PageRequest(0, 100);
        String lessonId = SafeConverter.toString(lesson.getLessonId());

        Page<GroupArea> lessonIdAndAreaName = crmStudyTogetherService.getGroupAreaByLessonIdAndAreaName(lessonId, "", pageRequest);
        if (lessonIdAndAreaName.getTotalElements() == 0) {
            return Collections.emptyList();
        }
        List<GroupArea> groupAreas = new ArrayList<>(lessonIdAndAreaName.getContent());
        while (lessonIdAndAreaName.hasNext()) {
            pageRequest = lessonIdAndAreaName.nextPageable();
            lessonIdAndAreaName = crmStudyTogetherService.getGroupAreaByLessonIdAndAreaName(lessonId, "", pageRequest);
            groupAreas.addAll(lessonIdAndAreaName.getContent());
        }
        groupAreas
                .stream()
                .sorted(Comparator.comparing(GroupArea::getCreateDate))
                .forEach(e -> {
                    List<String> list = new ArrayList<>();
                    list.add(SafeConverter.toString(groupAreas.indexOf(e) + 1));
                    list.add(lessonId);
                    list.add(lesson.getTitle());
                    list.add(SafeConverter.toString(lesson.getPhase()));
                    list.add(e.getId());
                    list.add(e.getGroupAreaName());
                    returnList.add(list);
                });

        return returnList;
    }

    //生成学生、班级关系数据
    private List<List<String>> generateStudentLessonExportData(List<StudyGroupStudentRef> studyGroupStudentRefs, Map<String, StudyGroup> clazzMap, Map<String, String> studentNameMap) {
        List<List<String>> returnList = new ArrayList<>();
        for (StudyGroupStudentRef studentRef : studyGroupStudentRefs) {
            List<String> refList = new ArrayList<>();
            refList.add(SafeConverter.toString(studyGroupStudentRefs.indexOf(studentRef) + 1));
            refList.add(SafeConverter.toString(studentRef.getParentId()));
            refList.add(SafeConverter.toString(studentRef.getStudentId()));
            refList.add(
                    StringUtils.isNotBlank(studentNameMap.get(SafeConverter.toString(studentRef.getStudentId())))
                            ? studentNameMap.get(SafeConverter.toString(studentRef.getStudentId()))
                            : "");
            refList.add(SafeConverter.toString(studentRef.getClazzLevel()));
            refList.add(SafeConverter.toString(studentRef.getStudyLessonId()));
            refList.add(SafeConverter.toString(studentRef.getStudyGroupId()));
            refList.add(clazzMap.get(studentRef.getStudyGroupId()) != null
                    ? clazzMap.get(studentRef.getStudyGroupId()).getWechatGroupName() == null ? "" : clazzMap.get(studentRef.getStudyGroupId()).getWechatGroupName()
                    : "");
            refList.add(DateUtils.dateToString(studentRef.getCreateDate()));
            returnList.add(refList);
        }

        return returnList;
    }

    //导出班级数据
    private List<List<String>> generateStudyLessonExportData(List<StudyGroup> studyGroups, Map<String, StudyOpWechatAccount> wechatInfos, Map<String, GroupArea> areaMap) {
        List<List<String>> returnList = new ArrayList<>();
        for (StudyGroup studyGroup : studyGroups) {
            List<String> groupList = new ArrayList<>();
            groupList.add(studyGroup.getId());
            groupList.add(studyGroup.getWechatGroupName() == null ? "" : studyGroup.getWechatGroupName());
            if (MapUtils.isNotEmpty(wechatInfos) && StringUtils.isNotBlank(studyGroup.getWechatId())) {
                if (wechatInfos.get(studyGroup.getWechatId()) != null) {
                    groupList.add(wechatInfos.get(studyGroup.getWechatId()).getWechatNumber());
                }
            } else {
                groupList.add("");
            }
            groupList.add(studyGroup.getVerifyCode());
            groupList.add(studyGroup.getActiveUrl());
            if (StringUtils.isNotBlank(studyGroup.getGroupAreaId())) {
                GroupArea groupArea = areaMap.get(studyGroup.getGroupAreaId());
                if (groupArea != null) {
                    groupList.add(groupArea.getGroupAreaName());
                } else {
                    groupList.add("");
                }
            } else {
                groupList.add("");
            }
            returnList.add(groupList);
        }
        return returnList;
    }

    //导出班级数据
    private List<List<String>> generateDataList(List<CrmStudyTogetherDataMapper> studyTogetherDataMapperList) {
        List<List<String>> returnList = new ArrayList<>();
        for (CrmStudyTogetherDataMapper mapper : studyTogetherDataMapperList) {
            List<String> list = new ArrayList<>();
            list.add(SafeConverter.toString(mapper.getWechatNumber()));
            list.add(SafeConverter.toString(mapper.getJoinCount()));
            list.add(SafeConverter.toString(mapper.getWechatGroupName()));
            list.add(SafeConverter.toString(mapper.getStudyGroupId()));
            list.add(SafeConverter.toString(mapper.getVerifyCode()));
            list.add(SafeConverter.toString(mapper.getVerifyCount()));
            returnList.add(list);
        }
        return returnList;
    }

    //读excel数据
    private XSSFWorkbook readExcel(String name, CoinImportHistory history) {
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
            if (history != null) {
                history.setFileName(fileName);
            }
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

    //校验导入数据
    private String validateImportClazzData(XSSFWorkbook workbook) {
        if (workbook == null) {
            return "文件读取失败";
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return "sheet读取失败";
        }
        Set<String> wechatGroupNames = new HashSet<>();
        Set<String> lessonIds = new HashSet<>();
//        Map<String, List<String>> wechatGroupMap = new HashMap<>();
        Row title = sheet.getRow(0);
        if (title == null) {
            return "表头错误";
        }
        for (Cell cell : title) {
            if (cell.getColumnIndex() > 2) {
                return "表格式错误";
            }
            if (cell.getColumnIndex() == 0 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "微信群名称")) {
                return "表头错误";
            }
            if (cell.getColumnIndex() == 1 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "绑定微信号")) {
                return "表头错误";
            }
            if (cell.getColumnIndex() == 2 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "课程ID")) {
                return "表头错误";
            }

        }
        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            String lessonId = "";
            String accountName = "";
            if (row == null) {
                return "表格式错误";
            }
            for (Cell cell : row) {
                if (cell.getColumnIndex() > 2) {
                    return "表格式错误";
                }
                if (cell.getColumnIndex() == 0) {
                    String cellValue = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(cellValue)) {
                        return "微信群名称不能为空!";
                    }
                    boolean add = wechatGroupNames.add(cellValue);
                    if (!add) {
                        return "微信群名称不能重复!";
                    }

                }
                if (cell.getColumnIndex() == 2) {
                    String cellValue = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(cellValue)) {
                        return "课程Id不能为空";
                    }
                    lessonIds.add(cellValue);
                    lessonId = cellValue;
                }

                if (cell.getColumnIndex() == 1) {
                    String cellValue = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(cellValue)) {
                        return "个人微信号不能为空！";
                    }
                    accountName = cellValue;
                }
            }
            StudyOpWechatAccount opWechatAccount = crmStudyTogetherService.getWechatInfoByWechatNumber(accountName, lessonId);
            if (opWechatAccount == null) {
                return "未找到对应的个人微信号！";
            }
        }
        if (CollectionUtils.isEmpty(wechatGroupNames)) {
            return "列-微信群名称-不能为空";
        }
        if (CollectionUtils.isEmpty(lessonIds)) {
            return "列-课程Id-不能为空";
        }
        if (lessonIds.size() > 1) {
            return "不支持多课程多班级导入";
        }
        List<String> currentLessonIds = getAllLessonId();
        if (!currentLessonIds.containsAll(lessonIds)) {
            return "课程Id有误，请检查";
        }
        for (String lessonId : lessonIds) {
            List<String> currentWechatGroupNames = crmStudyTogetherService.getStudyTogetherGroupNamesByLessonId(lessonId);
            //目前就是支持一个表里只有一个LessonId
            List<String> repeatNames = wechatGroupNames.stream().filter(currentWechatGroupNames::contains).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(repeatNames)) {
                return "微信群名称重复，请检查,重复名称为：" + JsonUtils.toJson(repeatNames);
            }
        }

        return "true";
    }

    private String validateImportGroupAreaData(XSSFWorkbook workbook) {
        if (workbook == null) {
            return "文件读取失败";
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return "sheet读取失败";
        }
        Set<String> areaGroupNames = new HashSet<>();
        Set<String> lessonIds = new HashSet<>();
//        Map<String, List<String>> wechatGroupMap = new HashMap<>();
        Row title = sheet.getRow(0);
        if (title == null) {
            return "表头错误";
        }
        for (Cell cell : title) {
            if (cell.getColumnIndex() > 1) {
                return "表格式错误";
            }
            if (cell.getColumnIndex() == 0 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "课程ID")) {
                return "表头错误";
            }
            if (cell.getColumnIndex() == 1 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "班级区名称")) {
                return "表头错误";
            }
        }
        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                return "表格式错误";
            }
            for (Cell cell : row) {
                if (cell.getColumnIndex() > 1) {
                    return "表格式错误";
                }
                if (cell.getColumnIndex() == 0) {
                    String cellValue = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(cellValue)) {
                        return "课程ID不能为空!";
                    }
                    lessonIds.add(cellValue);
                }
                if (cell.getColumnIndex() == 1) {
                    String cellValue = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(cellValue)) {
                        return "班级区名称不能为空！";
                    }
                    boolean add = areaGroupNames.add(cellValue);
                    if (!add) {
                        return "班级区名称不能重复!";
                    }
                }
            }
        }
        if (lessonIds.size() > 1) {
            return "不支持多课程多班级导入";
        }
        List<String> currentLessonIds = getAllLessonId();
        if (!lessonIds.stream().allMatch(currentLessonIds::contains)) {
            return "课程Id有误，请检查";
        }
        for (String lessonId : lessonIds) {
            List<GroupArea> groupAreaFromBufferByLessonId = crmStudyTogetherService.getGroupAreaFromBufferByLessonId(lessonId);
            if (CollectionUtils.isEmpty(groupAreaFromBufferByLessonId)) {
                break;
            }
            //目前就是支持一个表里只有一个LessonId
            List<String> areaNames = groupAreaFromBufferByLessonId.stream().map(GroupArea::getGroupAreaName).collect(Collectors.toList());
            List<String> repeatNames = areaNames.stream().filter(areaGroupNames::contains).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(repeatNames)) {
                return "班级区名称重复，请检查,重复名称为：" + JsonUtils.toJson(repeatNames);
            }
        }
        return "true";
    }

    private String validateUpdateGroupAreaData(XSSFWorkbook workbook) {
        if (workbook == null) {
            return "文件读取失败";
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return "sheet读取失败";
        }
        Set<String> areaGroupIds = new HashSet<>();
        Set<String> groupIds = new HashSet<>();
//        Map<String, List<String>> wechatGroupMap = new HashMap<>();
        Row title = sheet.getRow(0);
        if (title == null) {
            return "表头错误";
        }
        for (Cell cell : title) {
            if (cell.getColumnIndex() > 2) {
                return "表格式错误";
            }
            if (cell.getColumnIndex() == 0 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "班级ID")) {
                return "表头错误";
            }
            if (cell.getColumnIndex() == 1 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "群名称")) {
                return "表头错误";
            }
            if (cell.getColumnIndex() == 2 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "班级区ID")) {
                return "表头错误";
            }
        }
        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                return "表格式错误";
            }
            for (Cell cell : row) {
                if (cell.getColumnIndex() > 2) {
                    return "表格式错误";
                }
                if (cell.getColumnIndex() == 0) {
                    String cellValue = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(cellValue)) {
                        return "班级ID不能为空!";
                    }
                    groupIds.add(cellValue);
                }
                if (cell.getColumnIndex() == 2) {
                    String cellValue = ExcelUtil.getCellValue(cell);
                    if (StringUtils.isBlank(cellValue)) {
                        return "班级区ID不能为空！";
                    }
                    areaGroupIds.add(cellValue);
                }
            }
        }
        Set<String> groupLessonIds = crmStudyTogetherService.$getStudyGroupByIds(groupIds)
                .values()
                .stream().filter(e -> StringUtils.isNotBlank(e.getLessonId()))
                .map(StudyGroup::getLessonId)
                .collect(Collectors.toSet());
        if (groupLessonIds.size() != 1) {
            return "仅支持同一个课程下的班级进行班级区修改";
        }
        Set<String> areaLessonIds = new HashSet<>();
        for (String areaId : areaGroupIds) {
            GroupArea groupAreaById = crmStudyTogetherService.getGroupAreaById(areaId);
            if (groupAreaById != null) {
                areaLessonIds.add(groupAreaById.getLessonId());
            }
        }
        if (areaLessonIds.size() != 1) {
            return "仅支持将不同班级分配到同一班级区的修改";
        }
        String groupLessonId = groupLessonIds.stream().findFirst().orElse("");
        String areaLessonId = areaLessonIds.stream().findFirst().orElse("");
        if (!StringUtils.equals(groupLessonId, areaLessonId)) {
            return "班级区的课程Id与班级所在课程Id不一致，请检查";
        }
        return "true";
    }


    /**
     * 查询家长是否报名，学生是否激活
     */
    @RequestMapping(value = "/repairJoin.vpage", method = RequestMethod.GET)
    public String repairJoin(Model model) {
        List<String> lessonIds = getAllLessonId();
        if (CollectionUtils.isNotEmpty(lessonIds)) {
            model.addAttribute("lessonIds", lessonIds);
        }
        return "opmanager/studyTogether/repairJoin";
    }

    @RequestMapping(value = "/repairParentJoin.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage repairParentJoin() {
        String pids = getRequestString("pids");
        String lessonId = getRequestString("lessonId");

        List<Long> pidList = new ArrayList<>();
        Arrays.stream(pids.split(",")).forEach(t -> {
            boolean mobile = MobileRule.isMobile(t);
            if (mobile) {
                List<UserAuthentication> userAuthentications = userLoaderClient.loadMobileAuthentications(t);
                if (CollectionUtils.isNotEmpty(userAuthentications)) {
                    userAuthentications.stream().filter(s -> s.getUserType() == UserType.PARENT).findAny().ifPresent(userAuthentication -> pidList.add(userAuthentication.getId()));
                }
            } else {
                User user = userLoaderClient.loadUser(SafeConverter.toLong(t));
                if (user != null && user.isParent())
                    pidList.add(user.getId());
            }
        });

        Map<Long, MapMessage> resultMap = new HashMap<>();
        for (Long pid : pidList) {
            MapMessage mapMessage = studyTogetherServiceClient.parentSignUpLesson(lessonId, pid, false, ParentJoinLessonRef.JoinSource.FREE, "repair");
            resultMap.put(pid, mapMessage);
        }

        return MapMessage.successMessage().add("result", resultMap);
    }


    @RequestMapping(value = "/getGroupAreaInfos.vpage", method = RequestMethod.GET)
    public String getGroupAreaInfos(Model model) {
        String lessonId = getRequestString("selectLessonId");
        String areaName = getRequestString("areaName");
        Integer pageNum = getRequestInt("page", 1);
        PageRequest pageRequest = new PageRequest(pageNum - 1, 10);
        List<String> lessonIds = getAllLessonId();
        if (CollectionUtils.isNotEmpty(lessonIds)) {
            model.addAttribute("lessonIds", lessonIds);
        }
        if (StringUtils.isBlank(lessonId) && CollectionUtils.isNotEmpty(lessonIds)) {
            lessonId = lessonIds.get(0);
        }

        Page<GroupArea> groupAreas = crmStudyTogetherService.getGroupAreaByLessonIdAndAreaName(lessonId, areaName, pageRequest);

        StudyLesson lessonById = getStudyLesson(lessonId);

        model.addAttribute("content", groupAreas.getContent());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", groupAreas.getTotalPages());
        model.addAttribute("hasPrev", groupAreas.hasPrevious());
        model.addAttribute("hasNext", groupAreas.hasNext());
        model.addAttribute("selectedLessonId", lessonId);
        model.addAttribute("areaName", areaName);
        model.addAttribute("lesson", lessonById);
        return "opmanager/studyTogether/groupAreaList";
    }

    @RequestMapping(value = "/getGroupAreaInfoById.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getGroupAreaInfoById() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("id不能为空");
        }
        GroupArea groupArea = crmStudyTogetherService.getGroupAreaById(id);
        if (groupArea == null) {
            return MapMessage.errorMessage("未找到相应的班级区");
        }
        return MapMessage.successMessage().add("group_area", groupArea);
    }

    @RequestMapping(value = "/saveGroupArea.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveGroupArea() {
        String id = getRequestString("id");
        String lessonId = getRequestString("lessonId");
        String areaName = getRequestString("areaName");
        if (StringUtils.isBlank(lessonId) || StringUtils.isBlank(areaName)) {
            return MapMessage.errorMessage("区名称不能为空");
        }
        GroupArea groupArea = new GroupArea();
        if (StringUtils.isNotBlank(id)) {
            groupArea.setId(id);
        }
        groupArea.setLessonId(lessonId);
        groupArea.setGroupAreaName(areaName);
        GroupArea area = crmStudyTogetherService.upsertGroupArea(groupArea);
        if (area != null) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }

    @RequestMapping(value = "/getGroupAreaByLessonId.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getGroupAreaByLessonId() {
        String lessonId = getRequestString("lessonId");
        if (StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage();
        }
        List<GroupArea> groupAreas = crmStudyTogetherService.getGroupAreaFromBufferByLessonId(lessonId);
        return MapMessage.successMessage().add("groupAreas", groupAreas);
    }

    /**
     * 修改学分
     */
    @ResponseBody
    @RequestMapping(value = "addScore.vpage", method = RequestMethod.POST)
    public MapMessage addScore() {
        long stuentId = getRequestLong("studentId");
        long skuId = getRequestLong("skuId");
        if (stuentId == 0L || skuId == 0L) {
            return MapMessage.errorMessage("参数错误，操作失败");
        }
        long scoreCount = getRequestLong("scoreCount");
        long lessonId = getRequestLong("lessonId");
        String operatorType = getRequestString("operatorType");

        UserCreditHistory history = new UserCreditHistory();
        String id = UserCreditHistory.generateId(stuentId, skuId);
        history.setId(id);
        history.setStudentId(stuentId);
        history.setSkuId(skuId);
        history.setLessonId(lessonId);
        history.setCredit(scoreCount);
        history.setMessage(operatorType);
        return studyCourseUserDataService.changeUserCredit(history);
    }

    private String generateExcel(List<Map<String, Object>> failList) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        int rowNum = 0;
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("学生ID");
        row.createCell(1).setCellValue("学习币类型");
        row.createCell(2).setCellValue("错误原因");
        for (Map<String, Object> failInfo : failList) {
            Long studentId = SafeConverter.toLong(failInfo.get("studentId"));
            Integer coinType = SafeConverter.toInt(failInfo.get("coinType"));
            String reason = SafeConverter.toString(failInfo.get("reason"), "");
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(studentId);
            row.createCell(1).setCellValue(coinType);
            row.createCell(2).setCellValue(reason);
        }
        return uploadExcel(workbook);
    }

    private String uploadExcel(XSSFWorkbook workbook) {
        try {
            @Cleanup ByteArrayOutputStream os = new ByteArrayOutputStream();
            workbook.write(os);
            byte[] content = os.toByteArray();
            @Cleanup InputStream is = new ByteArrayInputStream(content);
            String env = "coin/";
            if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
                env = "coin/test/";
            }
            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
            String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + ".xls";
            String realName = storageClient.upload(is, fileName, path);
            return StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private List<Map<String, Object>> getCoinInfoList(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.getSheetAt(0);
        List<Map<String, Object>> list = new ArrayList<>();
        int rowIndex = 0;
        while (true) {
            XSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                break;
            }
            Map<String, Object> map = new HashMap<>();
            String studentId = XssfUtils.getStringCellValue(row.getCell(0));
            String coinType = XssfUtils.getStringCellValue(row.getCell(1));
            if (StringUtils.isBlank(studentId) || StringUtils.isBlank(coinType)) {
                rowIndex++;
                continue;
            }
            //简单判断是不是学生id
            if (!studentId.startsWith("3")) {
                rowIndex++;
                continue;
            }
            map.put("studentId", XssfUtils.getStringCellValue(row.getCell(0)));
            map.put("coinType", XssfUtils.getStringCellValue(row.getCell(1)));
            list.add(map);
            rowIndex++;
        }
        return list;
    }

    private String generateActiveShortUrl(String lessonId, String vCode) {
        if (StringUtils.isBlank(lessonId) || StringUtils.isBlank(vCode)) {
            return "";
        }
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("lesson_id", lessonId);
        paramMap.put("vCode", vCode);
        String longUrl = getMainHostBaseUrl() + "/view/mobile/parent/17xue_activate/activate_lessons.vpage";
        longUrl = longUrl.replace("http://", "https://");
        String activeUrl = UrlUtils.buildUrlQuery(longUrl, paramMap);
        Optional<String> shortUrl = ShortUrlGenerator.generateShortUrl(activeUrl, true);
        return shortUrl.orElse("");
    }
}
