package com.voxlearning.washington.controller.mobile.parent;

import com.google.common.collect.*;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.MapUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.athena.api.AppUsingNumService;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.ProductIdentifyType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.user.api.ClazzLoader;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.piclisten.client.TextBookManagementLoaderClient;
import com.voxlearning.utopia.service.school.api.SchoolLoader;
import com.voxlearning.utopia.service.user.api.UserAggregationLoader;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyService;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import com.voxlearning.utopia.service.vendor.consumer.MySelfStudyGlobalMsgServiceClient;
import com.voxlearning.washington.constant.StudyResourcesType;
import com.voxlearning.washington.service.parent.ParentSelfStudyPublicHelper;
import com.voxlearning.washington.support.StudyEntry;
import com.voxlearning.washington.support.StudyResourceEntry;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author malong
 * @since 2018/09/03
 */
@Slf4j
@Controller
@RequestMapping(value = "/parentMobile/studyIndex")
public class MobileParentStudyIndexController extends AbstractMobileParentController {
    @ImportService(interfaceClass = MySelfStudyService.class)
    protected MySelfStudyService mySelfStudyService;
    @Inject
    private MySelfStudyGlobalMsgServiceClient mySelfStudyGlobalMsgServiceClient;
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    private ParentSelfStudyPublicHelper parentSelfStudyPublicHelper;
    @Inject
    private TextBookManagementLoaderClient textBookManagementLoaderClient;
    @ImportService(interfaceClass = AppUsingNumService.class)
    private AppUsingNumService appUsingNumService;
    @ImportService(interfaceClass = UserAggregationLoader.class)
    protected UserAggregationLoader userAggregationLoader;
    @ImportService(interfaceClass = ClazzLoader.class)
    private ClazzLoader clazzLoader;
    @ImportService(interfaceClass = SchoolLoader.class)
    private SchoolLoader schoolLoader;

    private static final String indexChannel = "330094";

    private static List<SelfStudyType> allShelfSSTList;
    private static final Map<SelfStudyType, Integer> SSTOrderMap;
    private static List<SelfStudyType> xiaoUSSTList;
    private static final Map<SelfStudyType, String> SSTDescMap;

    private static Map<SelfStudyType, Map<String, Boolean>> mathTypeMap;
    private static Map<SelfStudyType, Map<String, Boolean>> englishTypeMap;
    private static Map<SelfStudyType, Map<String, Boolean>> chineseTypeMap;
    private static Map<SelfStudyType, Map<String, Boolean>> otherTypeMap;

    static {
        englishTypeMap = new LinkedHashMap<>();
        englishTypeMap.put(SelfStudyType.AFENTI_ENGLISH,
                ImmutableMap.<String, Boolean>builder().put("special", true).put("blacklist", false).build());
        englishTypeMap.put(SelfStudyType.PICLISTEN_ENGLISH,
                ImmutableMap.<String, Boolean>builder().put("special", true).put("blacklist", true).build());
        englishTypeMap.put(SelfStudyType.DUBBING,
                ImmutableMap.<String, Boolean>builder().put("special", true).put("blacklist", true).build());
        englishTypeMap.put(SelfStudyType.WORD_BUILDER,
                ImmutableMap.<String, Boolean>builder().put("special", false).put("blacklist", false).build());
        englishTypeMap.put(SelfStudyType.SYNCHRONOUS_EXERCISE,
                ImmutableMap.<String, Boolean>builder().put("special", true).put("blacklist", true).build());
        englishTypeMap.put(SelfStudyType.ZOUMEI_ENGLISH,
                ImmutableMap.<String, Boolean>builder().put("special", true).put("blacklist", true).build());
        englishTypeMap.put(SelfStudyType.LISTEN_WORLD,
                ImmutableMap.<String, Boolean>builder().put("special", false).put("blacklist", false).build());
        englishTypeMap.put(SelfStudyType.ELEVEL_READING,
                ImmutableMap.<String, Boolean>builder().put("special", false).put("blacklist", false).build());
        chineseTypeMap = new LinkedHashMap<>();
        chineseTypeMap.put(SelfStudyType.AFENTI_CHINESE,
                ImmutableMap.<String, Boolean>builder().put("special", true).put("blacklist", false).build());
        chineseTypeMap.put(SelfStudyType.CHINESE_PILOT,
                ImmutableMap.<String, Boolean>builder().put("special", false).put("blacklist", false).build());
        chineseTypeMap.put(SelfStudyType.CLEVEL_READING,
                ImmutableMap.<String, Boolean>builder().put("special", false).put("blacklist", false).build());
        mathTypeMap = new LinkedHashMap<>();
        mathTypeMap.put(SelfStudyType.AFENTI_MATH,
                ImmutableMap.<String, Boolean>builder().put("special", true).put("blacklist", false).build());
        mathTypeMap.put(SelfStudyType.MATH_PRACTICE,
                ImmutableMap.<String, Boolean>builder().put("special", true).put("blacklist", true).build());
        mathTypeMap.put(SelfStudyType.QUICK_ORAL_ARITHMETIC,
                ImmutableMap.<String, Boolean>builder().put("special", true).put("blacklist", true).build());
        mathTypeMap.put(SelfStudyType.ORAL_ARITHMETIC_EXERCISE,
                ImmutableMap.<String, Boolean>builder().put("special", true).put("blacklist", true).build());
        mathTypeMap.put(SelfStudyType.MATH_GARDEN,
                ImmutableMap.<String, Boolean>builder().put("special", false).put("blacklist", false).build());
        otherTypeMap = new LinkedHashMap<>();
        otherTypeMap.put(SelfStudyType.ALBUM,
                ImmutableMap.<String, Boolean>builder().put("special", true).put("blacklist", true).build());
        otherTypeMap.put(SelfStudyType.ENCYCLOPEDIA_CHALLENGE_UNKUNOW,
                ImmutableMap.<String, Boolean>builder().put("special", false).put("blacklist", false).build());
        otherTypeMap.put(SelfStudyType.DINOSAUR_LAND,
                ImmutableMap.<String, Boolean>builder().put("special", false).put("blacklist", false).build());
    }

    private static final Map<SelfStudyType, Boolean> allmathTypeMap;
    private static final Map<SelfStudyType, Boolean> allotherTypeMap;
    private static final Map<SelfStudyType, Boolean> allenglishTypeMap;
    private static final Map<SelfStudyType, Boolean> allchineseTypeMap;

    static {
        allenglishTypeMap = new LinkedHashMap<>();
        allenglishTypeMap.put(SelfStudyType.AFENTI_ENGLISH, false);
        allenglishTypeMap.put(SelfStudyType.PICLISTEN_ENGLISH, true);
        allenglishTypeMap.put(SelfStudyType.READING_ENGLISH, true);
        allenglishTypeMap.put(SelfStudyType.CHIPS_ENGLISH, true);
        allenglishTypeMap.put(SelfStudyType.DUBBING, true);
        allenglishTypeMap.put(SelfStudyType.ZOUMEI_ENGLISH, false);
        allenglishTypeMap.put(SelfStudyType.WORD_BUILDER, false);
        allchineseTypeMap = new LinkedHashMap<>();
        allchineseTypeMap.put(SelfStudyType.AFENTI_CHINESE, false);
        allchineseTypeMap.put(SelfStudyType.PICLISTEN_ENGLISH, false);
        allchineseTypeMap.put(SelfStudyType.CHINESE_PILOT, false);
        allmathTypeMap = new LinkedHashMap<>();
        allmathTypeMap.put(SelfStudyType.AFENTI_MATH, false);
        allmathTypeMap.put(SelfStudyType.MATH_GARDEN, false);
        allotherTypeMap = new LinkedHashMap<>();
        allotherTypeMap.put(SelfStudyType.ALBUM, true);
        allotherTypeMap.put(SelfStudyType.ENCYCLOPEDIA_CHALLENGE_UNKUNOW, false);
        allotherTypeMap.put(SelfStudyType.DINOSAUR_LAND, false);
        allotherTypeMap.put(SelfStudyType.SCIENCE_LAND, false);
    }

    private static final List<String> useNumServices;
    private static final List<String> numOfUsers;
    private static final List<String> filterList;

    private static final List<StudyResourcesType> resourcesRecommendList;

    static {

        allShelfSSTList = new ArrayList<>();
        allShelfSSTList.add(SelfStudyType.ENCYCLOPEDIA_CHALLENGE_UNKUNOW);
        allShelfSSTList.add(SelfStudyType.DINOSAUR_LAND);
        allShelfSSTList.add(SelfStudyType.SCIENCE_LAND);
        allShelfSSTList.add(SelfStudyType.ZOUMEI_ENGLISH);
        allShelfSSTList.add(SelfStudyType.ARITHMETIC);
        allShelfSSTList.add(SelfStudyType.SYN_PRACTICE_CHINESE);
        allShelfSSTList.add(SelfStudyType.CHINESE_HERO);
        allShelfSSTList.add(SelfStudyType.AFENTI_ENGLISH);
        allShelfSSTList.add(SelfStudyType.AFENTI_MATH);
        allShelfSSTList.add(SelfStudyType.AFENTI_CHINESE);
        allShelfSSTList.add(SelfStudyType.LISTEN_WORLD);
        SSTOrderMap = new HashMap<>();
        SSTOrderMap.put(SelfStudyType.ZUOYECUOITI, 5);
        SSTOrderMap.put(SelfStudyType.PICLISTEN_ENGLISH, 10);
        SSTOrderMap.put(SelfStudyType.DUBBING, 20);
        SSTOrderMap.put(SelfStudyType.ALBUM, 30);
        SSTOrderMap.put(SelfStudyType.READING_ENGLISH, 40);
        SSTOrderMap.put(SelfStudyType.LIVECAST, 60);
        SSTOrderMap.put(SelfStudyType.AFENTI_ENGLISH, 70);
        SSTOrderMap.put(SelfStudyType.AFENTI_MATH, 80);
        SSTOrderMap.put(SelfStudyType.AFENTI_CHINESE, 90);
        SSTOrderMap.put(SelfStudyType.ZOUMEI_ENGLISH, 100);
        SSTOrderMap.put(SelfStudyType.KUPAOWORD_ENGLISH, 110);
        SSTOrderMap.put(SelfStudyType.ARITHMETIC, 120);
        SSTOrderMap.put(SelfStudyType.CHINESE_HERO, 130);
        SSTOrderMap.put(SelfStudyType.SYN_PRACTICE_CHINESE, 140);
        SSTOrderMap.put(SelfStudyType.ENCYCLOPEDIA_CHALLENGE_UNKUNOW, 150);
        SSTOrderMap.put(SelfStudyType.ANIMAL_LAND, 160);
        SSTOrderMap.put(SelfStudyType.DINOSAUR_LAND, 170);
        SSTOrderMap.put(SelfStudyType.SCIENCE_LAND, 180);
        SSTOrderMap.put(SelfStudyType.MATH_GARDEN, 190);
        xiaoUSSTList = new ArrayList<>();
        xiaoUSSTList.add(SelfStudyType.AFENTI_ENGLISH);
        xiaoUSSTList.add(SelfStudyType.AFENTI_MATH);
        xiaoUSSTList.add(SelfStudyType.AFENTI_CHINESE);
        SSTDescMap = new HashMap<>();
        SSTDescMap.put(SelfStudyType.PICLISTEN_ENGLISH, "会发音的电子课本");
        SSTDescMap.put(SelfStudyType.ALBUM, "超千集学习资源");
        SSTDescMap.put(SelfStudyType.DUBBING, "动画配音趣练口语");
        SSTDescMap.put(SelfStudyType.READING_ENGLISH, "海量绘本提升阅读");
        SSTDescMap.put(SelfStudyType.ZOUMEI_ENGLISH, "单词专练");
        SSTDescMap.put(SelfStudyType.ARITHMETIC, "速算能力");
        SSTDescMap.put(SelfStudyType.CHINESE_HERO, "丰富语文词汇");
        SSTDescMap.put(SelfStudyType.SYN_PRACTICE_CHINESE, "语文基础字词");
        SSTDescMap.put(SelfStudyType.ENCYCLOPEDIA_CHALLENGE_UNKUNOW, "儿童百科全书");
        SSTDescMap.put(SelfStudyType.ANIMAL_LAND, "动物世界知识挖掘");
        SSTDescMap.put(SelfStudyType.DINOSAUR_LAND, "恐龙帝国探秘");
        SSTDescMap.put(SelfStudyType.SCIENCE_LAND, "身边科技常识");
        SSTDescMap.put(SelfStudyType.CHIPS_ENGLISH, "让英语脱口而出");
        useNumServices = new ArrayList<>();
        useNumServices.add(OrderProductServiceType.AfentiExam.name());
        useNumServices.add(OrderProductServiceType.AfentiChinese.name());
        useNumServices.add(OrderProductServiceType.AfentiMath.name());
        numOfUsers = Lists.newLinkedList();
        numOfUsers.add(OrderProductServiceType.AfentiExam.name());
        numOfUsers.add(OrderProductServiceType.PicListenBook.name());
        numOfUsers.add(OrderProductServiceType.WordBuilder.name());
        numOfUsers.add(OrderProductServiceType.AfentiChinese.name());
        numOfUsers.add(OrderProductServiceType.ChineseSynPractice.name());
        numOfUsers.add(OrderProductServiceType.ChinesePilot.name());
        numOfUsers.add(OrderProductServiceType.AfentiMath.name());
        numOfUsers.add(OrderProductServiceType.MathGarden.name());
        numOfUsers.add(OrderProductServiceType.EncyclopediaChallenge.name());
        numOfUsers.add(OrderProductServiceType.DinosaurLand.name());
        numOfUsers.add(OrderProductServiceType.ListenWorld.name());
        numOfUsers.add(OrderProductServiceType.ELevelReading.name());
        numOfUsers.add(OrderProductServiceType.CLevelReading.name());
        numOfUsers.add(OrderProductServiceType.UsaAdventure.name());
        numOfUsers.add(ProductIdentifyType.parent_EXAM.name());
        numOfUsers.add(ProductIdentifyType.parent_INTELLIGENT_TEACHING.name());
        numOfUsers.add(ProductIdentifyType.parent_MENTAL_ARITHMETIC.name());
        numOfUsers.add(ProductIdentifyType.parent_OCR_MENTAL_ARITHMETIC.name());

        filterList = Lists.newLinkedList();
        filterList.add(SelfStudyType.AFENTI_ENGLISH.name());
        filterList.add(SelfStudyType.AFENTI_CHINESE.name());
        filterList.add(SelfStudyType.AFENTI_MATH.name());

        resourcesRecommendList = Lists.newLinkedList();
        resourcesRecommendList.add(StudyResourcesType.SYNCHRONOUS_EXERCISE);
        resourcesRecommendList.add(StudyResourcesType.QUICK_ORAL_ARITHMETIC);
        resourcesRecommendList.add(StudyResourcesType.ORAL_ARITHMETIC_EXERCISE);
        /*resourcesRecommendList.add(StudyResourcesType.MATH_PRACTICE);*/
    }

    private static final String iconImageHeader = "https://oss-image.17zuoye.com";
    public static final Map<StudyResourcesType, Map<String, String>> resourceIconUrlMap;

    static {
        resourceIconUrlMap = Maps.newLinkedHashMap();
        resourceIconUrlMap.put(StudyResourcesType.SYNCHRONOUS_EXERCISE,
                ImmutableMap.<String, String>builder()
                        .put("product", "/jiazhangbuzhilianxiyunying/2019/01/23/20190123104908119445.png")
                        .put("test", "/jiazhangbuzhilianxiyunying/test/2019/01/23/20190123104641337798.png")
                        .build());
        resourceIconUrlMap.put(StudyResourcesType.QUICK_ORAL_ARITHMETIC,
                ImmutableMap.<String, String>builder()
                        .put("product", "/kousuanlianxiyunying/2019/01/23/20190123175738609741.png")
                        .put("test", "/kousuansusuanyunying/test/2019/01/23/20190123175653943818.png")
                        .build());
        resourceIconUrlMap.put(StudyResourcesType.ORAL_ARITHMETIC_EXERCISE,
                ImmutableMap.<String, String>builder()
                        .put("product", "/dayinkousuanyunying/2019/01/23/20190123175812843626.png")
                        .put("test", "/zhizhidayinyunying/test/2019/01/23/20190123175624112277.png")
                        .build());
        resourceIconUrlMap.put(StudyResourcesType.MATH_PRACTICE,
                ImmutableMap.<String, String>builder()
                        .put("product", "/shuxuezhongnandianfudao/2019/02/21/20190221162700101715.png")
                        .put("test", "/shuxuezhongnandianfudao/test/2019/02/21/20190221162419634217.png")
                        .build());
    }

    public static final List<SelfStudyType> afentiList;
    static {
        afentiList = Lists.newLinkedList();
        afentiList.add(SelfStudyType.AFENTI_ENGLISH);
        afentiList.add(SelfStudyType.AFENTI_CHINESE);
        afentiList.add(SelfStudyType.AFENTI_MATH);
    }

    @RequestMapping(value = "xiaou.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getXiaoUList() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR);
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return MapMessage.successMessage();
        }
        List<StudyEntry> studyEntries = new ArrayList<>();
        StudyEntry parentHomeworkEntry = getParentHomeworkEntry();
        if (parentHomeworkEntry != null) {
            List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parent.getId());
            Set<Long> studentIds = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
            List<StudentDetail> studentDetails = new ArrayList<>(studentLoaderClient.loadStudentDetails(studentIds).values());
            //B端小学用户和C端用户显示家长布置作业入口
            boolean parentHomeworkGray = studentDetails.stream()
                    .anyMatch(s -> grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(s, "Parent", "Homework"));
            if (studentDetail.getClazz() == null || (studentDetail.getClazz().isPrimaryClazz() && !studentDetail.getClazz().isTerminalClazz() && parentHomeworkGray)) {
                studyEntries.add(parentHomeworkEntry);
            }
        }

        boolean inBlackList = userBlacklistServiceClient.isInUserBlackList(studentDetail);
        if ((studentDetail.getClazz() == null || (!studentDetail.getClazz().isTerminalClazz() && studentDetail.isPrimaryStudent())) && !inBlackList) {
            IndexDataContext context = getIndexDataContext(parent, studentDetail);
            studyEntries.addAll(getStudyEntries(context, xiaoUSSTList, Collections.emptyMap(), true));
        }
        return MapMessage.successMessage().add("dataList", studyEntries)
                .add("is_channelC", studentDetail.getClazz() == null);
    }

    private StudyEntry getParentHomeworkEntry() {
        String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "PARENT_HOMEWORK_ENTRY_NEW");
        if (StringUtils.isNotBlank(configValue)) {
            Map<String, Object> configMap = JsonUtils.fromJson(configValue);
            if (com.voxlearning.alps.repackaged.org.apache.commons.collections4.MapUtils.isNotEmpty(configMap)) {
                String name = SafeConverter.toString(configMap.get("name"));
                String label = SafeConverter.toString(configMap.get("label"));
                String icon = SafeConverter.toString(configMap.get("icon"));
                String url = ProductConfig.getMainSiteBaseUrl() + SafeConverter.toString(configMap.get("url"));
                if (url.contains("?")) {
                    url += "&referrer=4";
                } else {
                    url += "?referrer=4";
                }
                String labelColor = SafeConverter.toString(configMap.get("labelColor"));
                String labelTextColor = SafeConverter.toString(configMap.get("labelTextColor"));
                String bottomText = SafeConverter.toString(configMap.get("bottomText"));
                StudyEntry studyEntry = new StudyEntry()
                        .touchName(name)
                        .touchLabel(label)
                        .touchIcon(icon)
                        .touchFunctionTypeH5()
                        .touchFunctionKey(url);
                studyEntry.setLabelColor(labelColor);
                studyEntry.setLabelTextColor(labelTextColor);
                studyEntry.setBottomText(bottomText);
                return studyEntry;
            }
        }
        return null;
    }

    /**
     * 全部入口
     */
    @RequestMapping(value = "all_study_resource.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage allStudyResource() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR);
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        IndexDataContext context = getIndexDataContext(parent, studentDetail);
        //C端用户、黑名单用户
        Boolean specialUserFlag = studentDetail == null || studentDetail.getClazz() == null || userBlacklistServiceClient.isInUserBlackList(studentDetail);
        List<Map<String, Object>> dataList = new ArrayList<>();
        String tagTextConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "ALL_STUDY_RESOURCE_TAG");
        Map<String, String> tagTextMap = new HashMap<>();
        if (StringUtils.isNotBlank(tagTextConfig)) {
            tagTextMap = JsonUtils.fromJsonToMapStringString(tagTextConfig);
        }
        //获取所有可用的商品
        List<OrderProduct> products = userOrderLoaderClient.loadAvailableProduct();
        Map<SelfStudyType, DayRange> userProductMaps = parentSelfStudyPublicHelper.moneySSTLastDayMap(studentId, false);
        List<Subject> subjects = loadPaiedPicListenBookSubject(parent);

        boolean isChannelC = studentDetail != null && studentDetail.getClazz() == null;
        boolean showXiaoU = isChannelC && !userBlacklistServiceClient.isInUserBlackList(studentDetail);

        //英语列表
        List<SelfStudyType> englishTypes = new ArrayList<>();
        if (showXiaoU) {
            englishTypes.add(SelfStudyType.AFENTI_ENGLISH);
        }
        allenglishTypeMap.forEach((key, value) -> {
            if (!specialUserFlag || value) {
                englishTypes.add(key);
            }
        });
        if (CollectionUtils.isNotEmpty(englishTypes)) {
            Map<String, Object> englishMap = new HashMap<>();
            englishMap.put("title", "英语");
            List<StudyEntry> entries = getStudyEntries(context, englishTypes, tagTextMap, false);
            entries = wrapStudyEntries(entries, products, userProductMaps);

            //趣味配音
            if (VersionUtil.compareVersion(getAppVersion(), "2.8.2") >= 0) {
                StudyEntry entry = entries.stream().filter(x -> x.getSelfStudyType() == SelfStudyType.DUBBING).findFirst().orElse(null);
                if(null != entry){
                    entry.touchName("趣味配音");
                    entry.setApplcationName("趣味配音");
                }
            }

            //点读机特殊处理，购买的显示正在学，否则显示价格
            entries = wrapStudyWithListen(entries, subjects, Subject.ENGLISH);

            englishMap.put("entry_list", entries);
            dataList.add(englishMap);
        }
        //语文
        List<SelfStudyType> chineseTypes = new ArrayList<>();
        if (showXiaoU) {
            chineseTypes.add(SelfStudyType.AFENTI_CHINESE);
        }
        allchineseTypeMap.forEach((key, value) -> {
            if (!specialUserFlag || value) {
                chineseTypes.add(key);
            }
        });
        if (CollectionUtils.isNotEmpty(chineseTypes)) {
            Map<String, Object> chineseMap = new HashMap<>();
            chineseMap.put("title", "语文");
            List<StudyEntry> entries = getStudyEntries(context, chineseTypes, tagTextMap, false);
            entries = wrapStudyEntries(entries, products, userProductMaps);

            //点读机特殊处理，购买的显示正在学，否则显示价格
            entries = wrapStudyWithListen(entries, subjects, Subject.CHINESE);

            chineseMap.put("entry_list", entries);
            dataList.add(chineseMap);
        }
        //数学
        List<SelfStudyType> mathTypes = new ArrayList<>();
        if (showXiaoU) {
            mathTypes.add(SelfStudyType.AFENTI_MATH);
        }
        allmathTypeMap.forEach((key, value) -> {
            if (!specialUserFlag || value) {
                mathTypes.add(key);
            }
        });
        if (CollectionUtils.isNotEmpty(mathTypes)) {
            Map<String, Object> mathMap = new HashMap<>();
            mathMap.put("title", "数学");
            List<StudyEntry> entries = getStudyEntries(context, mathTypes, tagTextMap, false);
            entries = wrapStudyEntries(entries, products, userProductMaps);
            mathMap.put("entry_list", entries);
            dataList.add(mathMap);
        }
        //综合
        List<SelfStudyType> otherTypes = new ArrayList<>();
        allotherTypeMap.forEach((key, value) -> {
            if (!specialUserFlag || value) {
                otherTypes.add(key);
            }
        });
        if (CollectionUtils.isNotEmpty(otherTypes)) {
            Map<String, Object> otherMap = new HashMap<>();
            otherMap.put("title", "综合");
            List<StudyEntry> entries = getStudyEntries(context, otherTypes, tagTextMap, false);
            entries = wrapStudyEntries(entries, products, userProductMaps);
            otherMap.put("entry_list", entries);
            dataList.add(otherMap);
        }
        return MapMessage.successMessage().add("data_list", dataList);
    }

    /**
     * 学习资源推荐位
     *
     * @return
     */
    @RequestMapping(value = "quality_recommend_resources.vpage")
    @ResponseBody
    public MapMessage resourceRecommend() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR);
        }

        Long sid = getRequestLong(REQ_STUDENT_ID);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        IndexDataContext context = getIndexDataContext(parent, studentDetail);

        //判断是否小学或者已毕业
        boolean graduate = false;
        if (null != studentDetail && null != studentDetail.getClazz()) {
            if (!studentDetail.getClazz().isPrimaryClazz()) {
                graduate = true;
            } else {
                graduate = studentDetail.getClazz().isTerminalClazz();
            }
        }

        boolean finalGraduate = graduate;
        List<StudyResourceEntry> studyResourceEntryList = resourcesRecommendList.stream()
                .map(studyResourcesType -> {
                    StudyResourceEntry resourceEntry = new StudyResourceEntry();
                    if (null != studentDetail) {
                        if (studyResourcesType == StudyResourcesType.SYNCHRONOUS_EXERCISE) {
                            resourceEntry.touchFunctionTypeH5()
                                    .touchFunctionKey(context.getCurrentSchema() + studyResourcesType.getH5Url());
                        } else if (studyResourcesType == StudyResourcesType.QUICK_ORAL_ARITHMETIC) {
                            resourceEntry.touchFunctionTypeH5()
                                    .touchFunctionKey(context.getCurrentSchema() + StudyResourcesType.QUICK_ORAL_ARITHMETIC.getH5Url() + "?sid=" + context.getStudentDetail().getId() + "&type=mental&referrer=1");
                        } else if (studyResourcesType == StudyResourcesType.ORAL_ARITHMETIC_EXERCISE) {
                            resourceEntry.touchFunctionTypeH5()
                                    .touchFunctionKey(context.getCurrentSchema() + StudyResourcesType.ORAL_ARITHMETIC_EXERCISE.getH5Url());
                        } else {
                            resourceEntry.touchFunctionTypeH5()
                                    .touchFunctionKey(context.getCurrentSchema() + StudyResourcesType.MATH_PRACTICE.getH5Url());
                        }
                    } else {
                        resourceEntry.touchFunctionTypeNative()
                                .touchFunctionKey(StudyEntry.FunctionKey.BIND_CHILD);
                    }
                    resourceEntry.setDisplay(finalGraduate);
                    resourceEntry.setName(studyResourcesType.getName());
                    resourceEntry.setSubject(studyResourcesType.getSubject());
                    resourceEntry.setIconUrl(getIconImageUrl(studyResourcesType));
                    resourceEntry.setMainTitle(studyResourcesType.getMainTitle());
                    resourceEntry.setSubheading(studyResourcesType.getSubheading());
                    resourceEntry.setUserNum(onlineRealNumber(ProductIdentifyType.identifyType(studyResourcesType.name()), studyResourcesType, sid));
                    return resourceEntry;
                })
                .collect(Collectors.toList());

        return MapMessage.successMessage()
                .add("data", studyResourceEntryList)
                .add("have_children", null == studentDetail ? false : true);
    }

    /**
     * 获取icon地址
     *
     * @param studyResourcesType
     */
    public String getIconImageUrl(StudyResourcesType studyResourcesType) {
        if (null != studyResourcesType) {
            if (RuntimeMode.isProduction() || RuntimeMode.isStaging()) {
                return iconImageHeader + resourceIconUrlMap.get(studyResourcesType).getOrDefault("product", "");
            } else {
                return iconImageHeader + resourceIconUrlMap.get(studyResourcesType).getOrDefault("test", "");
            }
        }
        return "";
    }

    /**
     * 实时在线人数
     */
    public String onlineRealNumber(ProductIdentifyType productType, StudyResourcesType studyType, Long sid) {
        if (null != productType) {
            //在线人数
            Integer useNum = 0;
            String numStr = null;
            Map<String, Integer> numMap;
            //获取班级信息
            Clazz clazz = clazzLoader.loadStudentClazz(sid);
            if (null != clazz) {
                try {
                    numMap = appUsingNumService.queryClazz(numOfUsers, clazz.getId());
                    useNum = SafeConverter.toInt(numMap.get(productType.name()));
                    if (useNum >= 15) {
                        numStr = "同班" + useNum;
                    } else {
                        numMap = appUsingNumService.queryGrade(numOfUsers, clazz.getSchoolId(), clazz.getClazzLevel().getLevel());
                        useNum = SafeConverter.toInt(numMap.get(productType.name()));
                        if (useNum >= 60) {
                            numStr = "同年级" + useNum;
                        } else {
                            numMap = appUsingNumService.querySchool(numOfUsers, clazz.getSchoolId());
                            useNum = SafeConverter.toInt(numMap.get(productType.name()));
                            if (useNum >= 100) {
                                numStr = "同校" + useNum;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Remotely invoking large data exceptions：{}", e);
                }
            }
            //处理其他情况
            if (StringUtils.isBlank(numStr) || 0 == useNum) {
                try {
                    numMap = appUsingNumService.queryNational(numOfUsers);
                    useNum = SafeConverter.toInt(numMap.get(productType.name()));
                } catch (Exception e) {
                    log.error("Remotely invoking large data exceptions：{}", e);
                    useNum = 0;
                }
                //同步练习初始化在线人数：2000+
                numStr = 0 == useNum ? "" : (useNum < 10000 ? String.valueOf(useNum) : ((useNum + 5000) / 10000 + "万"));
            }

            if (StringUtils.isNotBlank(numStr)) {
                String useDesc;
                if (StudyResourcesType.SYNCHRONOUS_EXERCISE == studyType ||
                        StudyResourcesType.QUICK_ORAL_ARITHMETIC == studyType ||
                        StudyResourcesType.ORAL_ARITHMETIC_EXERCISE == studyType) {
                    useDesc = numStr + "名家长已布置";
                } else {
                    useDesc = numStr + "名学生正在学";
                }
                return useDesc;
            }
        }

        return "";
    }

    /**
     * 练习馆入口
     */
    @RequestMapping(value = "practice_hall_resource.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage practiceHallResource() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage(RES_RESULT_LOAD_USER_ERROR);
        }

        String allSubject = "ALL";
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String subject = getRequestParameter("subject", "ALL");
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        IndexDataContext context = getIndexDataContext(parent, studentDetail);

        //C端用户
        boolean specialUser = studentDetail == null || studentDetail.getClazz() == null;
        //黑名单用户
        boolean blacklistUser = userBlacklistServiceClient.isInUserBlackList(studentDetail);
        //已毕业的用户-小学
        boolean graduate = false;
        //学前
        boolean preschool = false;
        //中学
        boolean middle = false;
        if (null != studentDetail && null != studentDetail.getClazz()) {
            if (studentDetail.getClazz().isPrimaryClazz()) {
                graduate = studentDetail.getClazz().isTerminalClazz();
            }
            preschool = studentDetail.getClazz().isInfantClazz();
            middle = studentDetail.getClazz().isJuniorClazz();
        }
        //20001审核用户
        boolean auditUser = parent.getId().compareTo(20001L) == 0 ? true : false;

        //标签内容配置
        String tagTextConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "ALL_STUDY_RESOURCE_TAG");
        Map<String, String> tagTextMap = Maps.newHashMap();
        if (StringUtils.isNotBlank(tagTextConfig)) {
            tagTextMap = JsonUtils.fromJsonToMapStringString(tagTextConfig);
        }

        //获取所有可用的商品
        List<OrderProduct> products = userOrderLoaderClient.loadAvailableProduct();
        Map<SelfStudyType, DayRange> userProductMaps = parentSelfStudyPublicHelper.moneySSTLastDayMap(studentId, false);

        //英语
        List<SelfStudyType> englishTypes;
        Map<String, Object> englishMap = null;
        if (Subject.ENGLISH.name().equals(subject) || allSubject.equals(subject)) {
            englishTypes = getSelfStudyTypes(specialUser, blacklistUser, graduate, auditUser, preschool, middle, englishTypeMap, context);
            if (CollectionUtils.isNotEmpty(englishTypes)) {
                englishMap = Maps.newLinkedHashMap();
                englishMap.put("title", "英语");
                handleCoreBus(studentId, context, specialUser, blacklistUser, tagTextMap, products, userProductMaps, englishMap, englishTypes);
            }
        }

        //语文
        List<SelfStudyType> chineseTypes;
        Map<String, Object> chineseMap = null;
        if (Subject.CHINESE.name().equals(subject) || allSubject.equals(subject)) {
            //语文-添加课本点读
            if (allSubject.equals(subject)) {
                Iterator<Map.Entry<SelfStudyType, Map<String, Boolean>>> iterator = chineseTypeMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<SelfStudyType, Map<String, Boolean>> mapEntry = iterator.next();
                    if (mapEntry.getKey() == SelfStudyType.PICLISTEN_ENGLISH ||
                            mapEntry.getKey() == SelfStudyType.SYNCHRONOUS_EXERCISE) {
                        iterator.remove();
                    }
                }
            } else {
                chineseTypeMap.put(SelfStudyType.SYNCHRONOUS_EXERCISE, ImmutableMap.<String, Boolean>builder().put("special", true).put("blacklist", true).build());
                chineseTypeMap.put(SelfStudyType.PICLISTEN_ENGLISH, ImmutableMap.<String, Boolean>builder().put("special", true).put("blacklist", true).build());
            }
            chineseTypes = getSelfStudyTypes(specialUser, blacklistUser, graduate, auditUser, preschool, middle, chineseTypeMap, context);
            if (CollectionUtils.isNotEmpty(chineseTypes)) {
                chineseMap = Maps.newLinkedHashMap();
                chineseMap.put("title", "语文");
                handleCoreBus(studentId, context, specialUser, blacklistUser, tagTextMap, products, userProductMaps, chineseMap, chineseTypes);
            }
        }

            //数学
            List<SelfStudyType> mathTypes;
            Map<String, Object> mathMap = null;
            if (Subject.MATH.name().equals(subject) || allSubject.equals(subject)) {
                //数学-添加布置练习
                if (allSubject.equals(subject)) {
                    Iterator<Map.Entry<SelfStudyType, Map<String, Boolean>>> iterator = mathTypeMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<SelfStudyType, Map<String, Boolean>> mapEntry = iterator.next();
                        if (mapEntry.getKey() == SelfStudyType.SYNCHRONOUS_EXERCISE) {
                            iterator.remove();
                        }
                    }
                } else {
                    mathTypeMap.put(SelfStudyType.SYNCHRONOUS_EXERCISE, ImmutableMap.<String, Boolean>builder().put("special", true).put("blacklist", true).build());
                    mathTypeMap = mathTypeMap.entrySet().stream()
                            .filter(Objects::nonNull)
                            .sorted(Comparator.comparing(t -> t.getKey().getType()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
                }
                mathTypes = getSelfStudyTypes(specialUser, blacklistUser, graduate, auditUser, preschool, middle, mathTypeMap, context);
                if (CollectionUtils.isNotEmpty(mathTypes)) {
                    mathMap = Maps.newLinkedHashMap();
                    mathMap.put("title", "数学");
                    handleCoreBus(studentId, context, specialUser, blacklistUser, tagTextMap, products, userProductMaps, mathMap, mathTypes);
                }
            }

        //综合
        List<SelfStudyType> otherTypes;
        Map<String, Object> otherMap = null;
        if ("OTHERS".equals(subject) || allSubject.equals(subject)) {
            otherTypes = getSelfStudyTypes(specialUser, blacklistUser, graduate, auditUser, preschool, middle, otherTypeMap, context);
            if (CollectionUtils.isNotEmpty(otherTypes)) {
                otherMap = Maps.newLinkedHashMap();
                otherMap.put("title", "综合");
                handleCoreBus(studentId, context, specialUser, blacklistUser, tagTextMap, products, userProductMaps, otherMap, otherTypes);
            }
        }

        //整理数据
        List<Map<String, Object>> dataList = collatingStudyData(englishMap, chineseMap, mathMap, otherMap);
        return MapMessage.successMessage()
                .add("data_list", dataList)
                .add("have_children", null == studentDetail ? false : true);
    }

    private void handleCoreBus(Long studentId, IndexDataContext context, boolean specialUser, boolean blacklistUser, Map<String, String> tagTextMap,
                               List<OrderProduct> products, Map<SelfStudyType, DayRange> userProductMaps, Map<String, Object> map, List<SelfStudyType> selfStudyTypes) {

        List<StudyEntry> entries = handleStudyEntries(context, selfStudyTypes, tagTextMap, studentId, userProductMaps);
        entries = wrapXLYStudyEntries(entries, products, userProductMaps);

        //添加B、C端用户
        if (CollectionUtils.isNotEmpty(entries)) {
            entries = entries.stream().map(studyEntry -> {
                if (specialUser) {
                    //C端用户类型
                    studyEntry.setUserType(3);
                } else if (blacklistUser) {
                    //黑名单用户类型
                    studyEntry.setUserType(2);
                } else {
                    //B端用户类型
                    studyEntry.setUserType(1);
                }
                return studyEntry;
            }).collect(Collectors.toList());

            //处理已购买小U的B、C端用户跳转，黑名单用户
            handleTerminalJump(entries, context, specialUser);
        }

        map.put("entry_list", entries);
    }

    private void handleTerminalJump(List<StudyEntry> studyEntries, IndexDataContext context, boolean specialUser) {
        if (CollectionUtils.isNotEmpty(studyEntries)) {
            studyEntries.stream()
                    .map(studyEntry -> {
                        FairylandProduct fairylandProduct = context.getSst2FairyLandProductMap().get(studyEntry.getSelfStudyType());
                        if (studyEntry.getIsBuy() && null != context && null != context.getStudentDetail() && filterList.contains(studyEntry.getSelfStudyType().name())) {
                            if (!specialUser) {
                                //B端用户跳转到报告页-舍弃小U英语
                                if (studyEntry.getSelfStudyType() == SelfStudyType.AFENTI_ENGLISH) {
                                    return studyEntry.touchFunctionTypeH5()
                                            .touchFunctionKey(context.getCurrentSchema() + "/karp/commodity_details/index/list?showAppList=true&sid=" + context.getStudentDetail().getId() + "&productType=AfentiExam&order_refer=330323");
                                } else {
                                    return studyEntry.touchFunctionTypeH5()
                                            .touchFunctionKey(context.getCurrentSchema() + "/zion/nova-report?subject=" + studyEntry.getSelfStudyType().getSubject() + "&sid=" + context.getStudentDetail().getId());
                                }
                            } else {
                                //C端用户跳转到小U内部
                                if (fairylandProduct == null) {
                                    return null;
                                }
                                VendorApps vendorApps = vendorLoaderClient.loadVendor(fairylandProduct.getAppKey());
                                return studyEntry.touchFunctionTypeNative()
                                        .touchFunctionKey(StudyEntry.FunctionKey.FAIRYLAND_APP)
                                        .touchExtra(generateFairylandExtra(fairylandProduct, vendorApps));
                            }
                        } else if (studyEntry.getSelfStudyType() == SelfStudyType.QUICK_ORAL_ARITHMETIC) {
                            return studyEntry.touchIcon(iconImageHeader + SelfStudyType.QUICK_ORAL_ARITHMETIC.getIconUrl());
                        } else if (studyEntry.getSelfStudyType() == SelfStudyType.ORAL_ARITHMETIC_EXERCISE) {
                            return studyEntry.touchIcon(iconImageHeader + SelfStudyType.ORAL_ARITHMETIC_EXERCISE.getIconUrl());
                        } else if (studyEntry.getSelfStudyType() == SelfStudyType.SYNCHRONOUS_EXERCISE) {
                            return studyEntry.touchIcon(iconImageHeader + SelfStudyType.SYNCHRONOUS_EXERCISE.getIconUrl());
                        } else if (studyEntry.getSelfStudyType() == SelfStudyType.LISTEN_WORLD) {
                            return studyEntry.touchIcon(iconImageHeader + SelfStudyType.LISTEN_WORLD.getIconUrl());
                        } else if (studyEntry.getSelfStudyType() == SelfStudyType.ELEVEL_READING) {
                            return studyEntry.touchIcon(iconImageHeader + SelfStudyType.ELEVEL_READING.getIconUrl());
                        } else if (studyEntry.getSelfStudyType() == SelfStudyType.CLEVEL_READING) {
                            return studyEntry.touchIcon(iconImageHeader + SelfStudyType.CLEVEL_READING.getIconUrl());
                        } else if (studyEntry.getSelfStudyType() == SelfStudyType.MATH_PRACTICE) {
                            return studyEntry.touchIcon(iconImageHeader + SelfStudyType.MATH_PRACTICE.getIconUrl());
                        } else if(studyEntry.getSelfStudyType() == SelfStudyType.DUBBING){
                            if (VersionUtil.compareVersion(getAppVersion(), "2.8.2") >= 0) {
                                studyEntry.touchName("趣味配音");
                                studyEntry.setApplcationName("趣味配音");
                            }
                            return studyEntry;
                        } else {
                            return studyEntry;
                        }
                    }).collect(Collectors.toList());
        }
    }

    private List<StudyEntry> handleStudyEntries(IndexDataContext indexDataContext, List<SelfStudyType> selfStudyTypes, Map<String, String> tagMap, Long studentId, Map<SelfStudyType, DayRange> userProductMaps) {
        if (CollectionUtils.isEmpty(selfStudyTypes)) {
            return Collections.emptyList();
        }
        List<StudyEntry> studyEntries = selfStudyTypes.stream()
                .map(t -> {
                    StudyEntry studyEntry = newProcessEntry(t, indexDataContext, false);
                    if (studyEntry == null) {
                        return null;
                    }
                    //判断是否购买产品
                    long days = -1;
                    if (isStuding(studyEntry, userProductMaps)) {
                        DayRange range = userProductMaps.get(studyEntry.getSelfStudyType());
                        if (null != range) {
                            Date nowDay = new Date();
                            Date endDay = range.getEndDate();
                            if (nowDay.before(endDay)) {
                                days = DateUtils.dayDiff(endDay, nowDay);
                                if (days > 0 && days <= 5) {
                                    studyEntry.setBottomText("还剩" + days + "天到期");
                                }
                            }
                        }
                    }
                    //未到期显示在线使用人数
                    if (days > 5 || days < 0) {
                        if (StringUtils.isNotBlank(t.getOrderProductServiceType())) {
                            //在线人数
                            Integer useNum = 0;
                            String numStr = null;
                            Map<String, Integer> numMap;
                            //获取班级信息
                            Clazz clazz = clazzLoader.loadStudentClazz(studentId);
                            if (null != clazz) {
                                try {
                                    numMap = appUsingNumService.queryClazz(numOfUsers, clazz.getId());
                                    useNum = SafeConverter.toInt(numMap.get(t.getOrderProductServiceType()));
                                    if (useNum >= 15) {
                                        numStr = "同班" + useNum;
                                    } else {
                                        numMap = appUsingNumService.queryGrade(numOfUsers, clazz.getSchoolId(), clazz.getClazzLevel().getLevel());
                                        useNum = SafeConverter.toInt(numMap.get(t.getOrderProductServiceType()));
                                        if (useNum >= 60) {
                                            numStr = "同年级" + useNum;
                                        } else {
                                            numMap = appUsingNumService.querySchool(numOfUsers, clazz.getSchoolId());
                                            useNum = SafeConverter.toInt(numMap.get(t.getOrderProductServiceType()));
                                            if (useNum >= 100) {
                                                numStr = "同校" + useNum;
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error("Remotely invoking large data exceptions：{}", e);
                                }
                            }
                            //处理其他情况
                            if (StringUtils.isBlank(numStr) || 0 == useNum) {
                                try {
                                    numMap = appUsingNumService.queryNational(numOfUsers);
                                    useNum = SafeConverter.toInt(numMap.get(t.getOrderProductServiceType()));
                                } catch (Exception e) {
                                    log.error("Remotely invoking large data exceptions：{}", e);
                                    useNum = 0;
                                }
                                //同步练习初始化在线人数：2000+
                                numStr = 0 == useNum ? "" : (useNum < 10000 ? String.valueOf(useNum) : ((useNum + 5000) / 10000 + "万"));
                            }

                            if (StringUtils.isNotBlank(numStr)) {
                                String useDesc;
                                if (SelfStudyType.SYNCHRONOUS_EXERCISE == t ||
                                        SelfStudyType.QUICK_ORAL_ARITHMETIC == t ||
                                        SelfStudyType.ORAL_ARITHMETIC_EXERCISE == t) {
                                    useDesc = numStr + "名家长已布置";
                                } else if (SelfStudyType.MATH_PRACTICE == t) {
                                    useDesc = numStr + "名学生正在学";
                                } else {
                                    useDesc = numStr + "名家长已购买";
                                }
                                studyEntry.setBottomText(useDesc);
                            }
                        }
                    }
                    if (t == SelfStudyType.AFENTI_MATH) {
                        studyEntry.setRecommendText("北师大附小老师推荐");
                    }
                    studyEntry.setTagText(SafeConverter.toString(tagMap.get(t.name())));
                    return studyEntry;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        dealIconCdnAndGlobalMSg(studyEntries, indexDataContext);
        studyEntries.forEach(t -> dealDefaultLabel2Desc(t, indexDataContext, !xiaoUSSTList.contains(t.getSelfStudyType())));
        return studyEntries;
    }

    @NotNull
    private List<SelfStudyType> getSelfStudyTypes(boolean specialUser, boolean blacklistUser, boolean graduate, boolean auditUser, boolean preschool, boolean middle, Map<SelfStudyType, Map<String, Boolean>> typeMap, IndexDataContext context) {
        if (MapUtils.isEmpty(typeMap)) {
            return Collections.emptyList();
        }
        Set<SelfStudyType> types = Sets.newLinkedHashSet();
        typeMap.forEach((key, value) -> {
            if (blacklistUser) {
                if (value.get("blacklist")) {
                    types.add(key);
                }
            } else if (specialUser) {
                if (value.get("special")) {
                    types.add(key);
                }
            } else {
                types.add(key);
            }
        });

        List<SelfStudyType> selfStudyTypes = new LinkedList<>(types);
        //毕业生过滤
        if (graduate && CollectionUtils.isNotEmpty(selfStudyTypes)) {
            selfStudyTypes = selfStudyTypes.stream()
                    .filter(Objects::nonNull)
                    .filter(selfStudyType -> (selfStudyType != SelfStudyType.SYNCHRONOUS_EXERCISE &&
                            selfStudyType != SelfStudyType.MATH_PRACTICE &&
                            selfStudyType != SelfStudyType.QUICK_ORAL_ARITHMETIC &&
                            selfStudyType != SelfStudyType.ORAL_ARITHMETIC_EXERCISE &&
                            selfStudyType != SelfStudyType.AFENTI_MATH &&
                            selfStudyType != SelfStudyType.AFENTI_CHINESE &&
                            selfStudyType != SelfStudyType.AFENTI_ENGLISH))
                    .collect(Collectors.toList());
        }

        //用户是20001，则配置100分，小U绘本，小U语文绘本隐藏入口
        if (auditUser && CollectionUtils.isNotEmpty(selfStudyTypes)) {
            selfStudyTypes = selfStudyTypes.stream()
                    .filter(Objects::nonNull)
                    .filter(selfStudyType -> (
                            selfStudyType != SelfStudyType.LISTEN_WORLD &&
                                    selfStudyType != SelfStudyType.ELEVEL_READING &&
                                    selfStudyType != SelfStudyType.CLEVEL_READING
                    )).collect(Collectors.toList());
        }

        //version>=2.8.2版本展示小U绘本和小U语文绘本
        if (CollectionUtils.isNotEmpty(selfStudyTypes)) {
            if (StringUtils.isNotBlank(getAppVersion()) &&
                    VersionUtil.compareVersion(getAppVersion(), "2.8.2") < 0) {
                selfStudyTypes = selfStudyTypes.stream()
                        .filter(Objects::nonNull)
                        .filter(selfStudyType -> (
                                selfStudyType != SelfStudyType.ELEVEL_READING &&
                                        selfStudyType != SelfStudyType.CLEVEL_READING
                        )).collect(Collectors.toList());
            }
        }

        return selfStudyTypes;
    }

    public List<Map<String, Object>> collatingStudyData(Map<String, Object> englishMap, Map<String, Object> chineseMap, Map<String, Object> mathMap, Map<String, Object> otherMap) {
        //课本点读
        List<StudyEntry> pointReaderList = Lists.newLinkedList();
        Map<String, Object> pointReaderMap = Maps.newLinkedHashMap();
        //布置练习
        List<StudyEntry> homeworkList = Lists.newLinkedList();
        Map<String, Object> homeworkMap = Maps.newLinkedHashMap();
        //智能教辅
        List<StudyEntry> teachAssistantList = Lists.newLinkedList();
        Map<String, Object> teachAssistantMap = Maps.newLinkedHashMap();
        //专项练习
        List<StudyEntry> specialExercisesList = Lists.newLinkedList();
        Map<String, Object> specialExercisesMap = Maps.newLinkedHashMap();
        //趣味拓展
        List<StudyEntry> interestExpandList = Lists.newLinkedList();
        Map<String, Object> interestExpandMap = Maps.newLinkedHashMap();

        //整理和分组--英语
        studyMap(englishMap, pointReaderList, homeworkList, teachAssistantList, specialExercisesList, interestExpandList);

        //整理和分组--语文
        studyMap(chineseMap, pointReaderList, homeworkList, teachAssistantList, specialExercisesList, interestExpandList);

        //整理和分组--数学
        studyMap(mathMap, pointReaderList, homeworkList, teachAssistantList, specialExercisesList, interestExpandList);

        //蒸馏和分组--综合
        studyMap(otherMap, pointReaderList, homeworkList, teachAssistantList, specialExercisesList, interestExpandList);

        //整理分组后数据
        pointReaderMap.put("title", SelfStudyType.PICLISTEN_ENGLISH.getApplicationType());
        pointReaderMap.put("title_id", "textbook_list");
        pointReaderMap.put("content", pointReaderList);

        homeworkMap.put("title", SelfStudyType.SYNCHRONOUS_EXERCISE.getApplicationType());
        homeworkMap.put("title_id", "placement_list");
        homeworkMap.put("content", homeworkList);

        teachAssistantMap.put("title", SelfStudyType.AFENTI_ENGLISH.getApplicationType());
        teachAssistantMap.put("title_id", "assistant_list");
        teachAssistantMap.put("content", teachAssistantList);

        specialExercisesMap.put("title", SelfStudyType.MATH_GARDEN.getApplicationType());
        specialExercisesMap.put("title_id", "exercises_list");
        specialExercisesMap.put("content", specialExercisesList);

        interestExpandMap.put("title", SelfStudyType.ENCYCLOPEDIA_CHALLENGE_UNKUNOW.getApplicationType());
        interestExpandMap.put("title_id", "development_list");
        interestExpandMap.put("content", interestExpandList);

        return ImmutableList.<Map<String, Object>>builder()
                .add(pointReaderMap)
                .add(homeworkMap)
                .add(teachAssistantMap)
                .add(specialExercisesMap)
                .add(interestExpandMap)
                .build();
    }

    private void studyMap(Map<String, Object> params, List<StudyEntry> pointReaderList, List<StudyEntry> homeworkList,
                          List<StudyEntry> teachAssistantList, List<StudyEntry> specialExercisesList, List<StudyEntry> interestExpandList) {
        if (MapUtils.isNotEmpty(params)) {
            Object object = params.getOrDefault("entry_list", null);
            if (null != object) {
                List<StudyEntry> studyEntryList = (List<StudyEntry>) object;
                verifyStudyData(studyEntryList, pointReaderList, homeworkList, teachAssistantList, specialExercisesList, interestExpandList);
            }
        }
    }

    public void verifyStudyData(List<StudyEntry> studyEntryList, List<StudyEntry> pointReaderList, List<StudyEntry> homeworkList,
                                List<StudyEntry> teachAssistantList, List<StudyEntry> specialExercisesList, List<StudyEntry> interestExpandList) {
        //课本点读
        String pointReader = SelfStudyType.PICLISTEN_ENGLISH.getApplicationType();
        //布置练习
        String homework = SelfStudyType.SYNCHRONOUS_EXERCISE.getApplicationType();
        //智能教辅
        String teachAssistant = SelfStudyType.AFENTI_ENGLISH.getApplicationType();
        //专项练习
        String specialExercises = SelfStudyType.MATH_GARDEN.getApplicationType();
        //趣味拓展
        String interestExpand = SelfStudyType.ENCYCLOPEDIA_CHALLENGE_UNKUNOW.getApplicationType();

        if (CollectionUtils.isNotEmpty(studyEntryList)) {
            studyEntryList.stream().forEach(studyEntry -> {
                String applicationName = studyEntry.getSelfStudyType().getApplicationType();
                if (applicationName.equals(pointReader)) {
                    pointReaderList.add(studyEntry);
                } else if (applicationName.equals(homework)) {
                    homeworkList.add(studyEntry);
                } else if (applicationName.equals(teachAssistant)) {
                    teachAssistantList.add(studyEntry);
                } else if (applicationName.equals(specialExercises)) {
                    specialExercisesList.add(studyEntry);
                } else if (applicationName.equals(interestExpand)) {
                    interestExpandList.add(studyEntry);
                }
            });
        }
    }

    /**
     * 新封装数据方法，不改变原有业务逻辑，用户排序和按钮
     *
     * @param entries         应用列表
     * @param products        所有商品
     * @param userProductMaps 用户购买
     * @return 应用列表
     */
    private List<StudyEntry> wrapXLYStudyEntries(List<StudyEntry> entries, List<OrderProduct> products, Map<SelfStudyType, DayRange> userProductMaps) {
        for (StudyEntry entry : entries) {
            OrderProduct currentProduct = products.stream()
                    .filter(product -> {
                        OrderProductServiceType serviceType = OrderProductServiceType.safeParse(entry.getSelfStudyType().getOrderProductServiceType());
                        return serviceType != null && StringUtils.equals(serviceType.name(), product.getProductType());
                    })
                    .sorted(Comparator.comparing(OrderProduct::getPrice)).findFirst().orElse(null);

            if (StringUtils.isNotEmpty(entry.getTagText())) {
                if (entry.getSelfStudyType() == SelfStudyType.ELEVEL_READING ||
                        entry.getSelfStudyType() == SelfStudyType.CLEVEL_READING) {
                    if (isStuding(entry, userProductMaps)) {
                        entry.setSort(1);
                        entry.setButtonText("进入阅读");
                        entry.setIsBuy(true);
                        entry.setProductType("进入阅读");
                    } else {
                        entry.setSort(2);
                        entry.setButtonText("进入阅读");
                        entry.setIsBuy(false);
                        entry.setProductType("进入阅读");
                    }
                } else {
                    if (isStuding(entry, userProductMaps)) {
                        entry.setSort(1);
                        entry.setButtonText("已开通");
                        entry.setIsBuy(true);
                        entry.setProductType("已开通");
                    } else if (null != currentProduct && null != currentProduct.getPrice()) {
                        entry.setSort(3);
                        entry.setButtonText(currentProduct.getPrice().stripTrailingZeros().toPlainString());
                        entry.setIsBuy(false);
                        entry.setProductType("未开通");
                    } else {
                        entry.setSort(2);
                        entry.setButtonText("免费");
                        entry.setIsBuy(false);
                        entry.setProductType("免费");
                    }
                }
            } else {
                if (entry.getSelfStudyType() == SelfStudyType.ELEVEL_READING ||
                        entry.getSelfStudyType() == SelfStudyType.CLEVEL_READING) {
                    if (isStuding(entry, userProductMaps)) {
                        entry.setSort(3);
                        entry.setButtonText("进入阅读");
                        entry.setIsBuy(true);
                        entry.setProductType("进入阅读");
                    } else {
                        entry.setSort(4);
                        entry.setButtonText("进入阅读");
                        entry.setIsBuy(false);
                        entry.setProductType("进入阅读");
                    }
                } else {
                    if (isStuding(entry, userProductMaps)) {
                        entry.setSort(4);
                        entry.setButtonText("已开通");
                        entry.setIsBuy(true);
                        entry.setProductType("已开通");
                    } else if (null != currentProduct && null != currentProduct.getPrice()) {
                        entry.setSort(6);
                        entry.setButtonText(currentProduct.getPrice().stripTrailingZeros().toPlainString());
                        entry.setIsBuy(false);
                        entry.setProductType("未开通");
                    } else {
                        entry.setSort(5);
                        entry.setButtonText("免费");
                        entry.setIsBuy(false);
                        entry.setProductType("免费");
                    }
                }
            }
        }
        return entries.stream().sorted(Comparator.comparing(StudyEntry::getSort)).collect(Collectors.toList());
    }

    /**
     * 兼容爆版本
     *
     * @param entries
     * @param products
     * @param userProductMaps
     * @return
     */
    private List<StudyEntry> wrapStudyEntries(List<StudyEntry> entries, List<OrderProduct> products, Map<SelfStudyType, DayRange> userProductMaps) {
        for (StudyEntry entry : entries) {
            OrderProduct currentProduct = products.stream()
                    .filter(product -> {
                        OrderProductServiceType serviceType = OrderProductServiceType.safeParse(entry.getSelfStudyType().getOrderProductServiceType());
                        return serviceType != null && StringUtils.equals(serviceType.name(), product.getProductType());
                    })
                    .sorted(Comparator.comparing(OrderProduct::getPrice)).findFirst().orElse(null);

            if (StringUtils.isNotEmpty(entry.getTagText())) {
                if (isStuding(entry, userProductMaps)) {
                    entry.setSort(1);
                    entry.setButtonText("正在学");
                } else if (null != currentProduct && null != currentProduct.getPrice()) {
                    entry.setSort(3);
                    entry.setButtonText("￥" + currentProduct.getPrice().stripTrailingZeros().toPlainString());
                } else {
                    entry.setSort(2);
                    entry.setButtonText("免费");
                }
            } else {
                if (isStuding(entry, userProductMaps)) {
                    entry.setSort(4);
                    entry.setButtonText("正在学");
                } else if (null != currentProduct && null != currentProduct.getPrice()) {
                    entry.setSort(6);
                    entry.setButtonText("￥" + currentProduct.getPrice().stripTrailingZeros().toPlainString());
                } else {
                    entry.setSort(5);
                    entry.setButtonText("免费");
                }
            }
        }
        return entries.stream().sorted(Comparator.comparing(StudyEntry::getSort)).collect(Collectors.toList());
    }

    private List<StudyEntry> wrapStudyWithListen(List<StudyEntry> entries, List<Subject> subjects, Subject subject) {
        StudyEntry entry = entries.stream()
                .filter(x -> null != x.getSelfStudyType()
                        && OrderProductServiceType.PicListenBook.equals(x.getSelfStudyType().getOrderProductServiceType()))
                .findFirst().orElse(null);
        if (null == entry) {
            return entries;
        }

        if (subjects.contains(subject)) {
            entry.setButtonText("正在学");
            if (StringUtils.isNotEmpty(entry.getTagText())) {
                entry.setSort(1);
            } else {
                entry.setSort(4);
            }
            entry.setTag(StudyEntry.Tag.payed);
        } else {
            entry.setButtonText("￥5-15");
            if (StringUtils.isNotEmpty(entry.getTagText())) {
                entry.setSort(3);
            } else {
                entry.setSort(6);
            }
        }

        return entries.stream().sorted(Comparator.comparing(StudyEntry::getSort)).collect(Collectors.toList());
    }

    private List<StudyEntry> getStudyEntries(IndexDataContext indexDataContext, List<SelfStudyType> selfStudyTypes, Map<String, String> tagMap, boolean onlyXiaoU) {
        Map<String, Integer> numMap = appUsingNumService.queryNational(useNumServices);
        List<StudyEntry> studyEntries = selfStudyTypes.stream()
                .map(t -> {
                    StudyEntry studyEntry = newProcessEntry(t, indexDataContext, onlyXiaoU);
                    if (studyEntry == null) {
                        return null;
                    }
                    if (StringUtils.isBlank(studyEntry.getBottomText()) && t.getOrderProductServiceType() != null) {
                        Integer useNum = SafeConverter.toInt(numMap.get(t.getOrderProductServiceType()));
                        if (useNum > 0) {
                            String numStr = useNum < 10000 ? String.valueOf(useNum) : String.valueOf((useNum + 5000) / 10000) + "万";
                            String useDesc = numStr + "名学生在学";
                            studyEntry.setBottomText(useDesc);
                        }
                    }
                    if (t == SelfStudyType.AFENTI_ENGLISH) {
                        studyEntry.setRecommendText("北师大附小老师推荐");
                    }
                    studyEntry.setTagText(SafeConverter.toString(tagMap.get(t.name())));
                    return studyEntry;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        dealIconCdnAndGlobalMSg(studyEntries, indexDataContext);
        studyEntries.forEach(t -> dealDefaultLabel2Desc(t, indexDataContext, !xiaoUSSTList.contains(t.getSelfStudyType())));
        if (onlyXiaoU) {
            studyEntries.forEach(studyEntry -> {
                if (xiaoUSSTList.contains(studyEntry.getSelfStudyType())) {
                    switch (studyEntry.getSelfStudyType()) {
                        case AFENTI_ENGLISH:
                            studyEntry.setName("小U英语同步练");
                            break;
                        case AFENTI_MATH:
                            studyEntry.setName("小U数学同步练");
                            break;
                        case AFENTI_CHINESE:
                            studyEntry.setName("小U语文同步练");
                            break;
                    }
                }
            });
        }
        return studyEntries;
    }

    private StudyEntry.Tag fetchTag(SelfStudyType selfStudyType, IndexDataContext.Position position, boolean isMySelected) {
        return fetchTag(selfStudyType, position, null, isMySelected);
    }

    private StudyEntry.Tag fetchTag(SelfStudyType selfStudyType, IndexDataContext.Position position, StudyEntry.Tag specificTag, boolean isMySelected) {
        if (position == null || selfStudyType == null) {
            return null;
        }
        if (position == IndexDataContext.Position.index && isMySelected) { //首页-我的学习资源 只显示配音的 tag
            switch (selfStudyType) {
                case PICLISTEN_ENGLISH:
                    return StudyEntry.Tag.rec;
                case UNKNOWN:
                    return StudyEntry.Tag.rec;
                default:
                    return null;
            }
        } else if (position == IndexDataContext.Position.edit || !isMySelected) {
            if (specificTag != null) {
                return specificTag;
            }
            switch (selfStudyType) {
                case PICLISTEN_ENGLISH:
                    return StudyEntry.Tag.fresh;
                case ALBUM:
                    return StudyEntry.Tag.free;
                case READING_ENGLISH:
                    return StudyEntry.Tag.free;
                default:
                    return null;
            }
        }
        return null;
    }

    private String getProgressFromData(StudyAppData studyAppData) {
        if (studyAppData == null) {
            return "";
        }
        String progress = studyAppData.getProgress();
        return progress == null ? "" : progress;
    }

    private StudyEntry newProcessEntry(SelfStudyType selfStudyType, IndexDataContext context, boolean onlyXiaoU) {
        DayRange endDayRange = null;
        if (MapUtils.isNotEmpty(context.getSelfStudyTypeDayRangeMapKeepExpired())) {
            endDayRange = context.getSelfStudyTypeDayRangeMapKeepExpired().get(selfStudyType);
        }
        StudyEntry studyEntry = StudyEntry.newInstance(selfStudyType, context.getVersion());
        if (selfStudyType == SelfStudyType.PICLISTEN_ENGLISH) {
            studyEntry = studyEntry
                    .touchFunctionTypeH5()
                    .touchFunctionKey(context.getCurrentSchema() + SelfStudyType.PICLISTEN_ENGLISH.getH5Url())
                    .toucheTag(fetchTag(SelfStudyType.PICLISTEN_ENGLISH, context.getPosition(), true))
                    .touchLabelTextColor(StudyEntry.LabelColor.YELLOW_1);
        } else if (selfStudyType == SelfStudyType.ALBUM) {
            if (null == context.getStudentDetail()) {
                return studyEntry.touchFunctionTypeNative()
                        .touchFunctionKey(StudyEntry.FunctionKey.BIND_CHILD);
            } else {
                return studyEntry.touchFunctionTypeH5()
                        .touchFunctionKey(context.getCurrentSchema() + SelfStudyType.ALBUM.getH5Url() + "?sid=" + context.getStudentDetail().getId())
                        .toucheTag(fetchTag(SelfStudyType.ALBUM, context.getPosition(), true))
                        .touchLabelTextColor(StudyEntry.LabelColor.WHITE);
            }
        } else if (selfStudyType == SelfStudyType.READING_ENGLISH) {
            studyEntry = studyEntry
                    .touchFunctionTypeH5()
                    .touchFunctionKey(context.getCurrentSchema() + SelfStudyType.READING_ENGLISH.getH5Url())
                    .toucheTag(fetchTag(SelfStudyType.READING_ENGLISH, context.getPosition(), true))
                    .touchLabelTextColor(StudyEntry.LabelColor.YELLOW_1);
        } else if (selfStudyType == SelfStudyType.CHIPS_ENGLISH) {
            studyEntry = studyEntry
                    .touchFunctionTypeNative()
                    .touchFunctionKey(StudyEntry.FunctionKey.FRIESENGLISH)
                    .toucheTag(fetchTag(SelfStudyType.CHIPS_ENGLISH, context.getPosition(), true));
        } else if (selfStudyType == SelfStudyType.DUBBING) {
            if (null != context.getStudentDetail()) {
                studyEntry = studyEntry
                        .touchFunctionTypeNative()
                        .touchFunctionKey(StudyEntry.FunctionKey.DUBBING)
                        .toucheTag(fetchTag(SelfStudyType.DUBBING, context.getPosition(), false));
            } else {
                studyEntry.touchFunctionTypeNative()
                        .touchFunctionKey(StudyEntry.FunctionKey.BIND_CHILD)
                        .toucheTag(fetchTag(SelfStudyType.DUBBING, context.getPosition(), false));
            }
        } else if (selfStudyType == SelfStudyType.SYNCHRONOUS_EXERCISE) {
            if (null != context.getStudentDetail()) {
                studyEntry = studyEntry
                        .touchFunctionTypeH5()
                        .touchFunctionKey(context.getCurrentSchema() + SelfStudyType.SYNCHRONOUS_EXERCISE.getH5Url())
                        .toucheTag(fetchTag(SelfStudyType.SYNCHRONOUS_EXERCISE, context.getPosition(), false))
                        .touchLabelColor(StudyEntry.LabelColor.WHITE);
            } else {
                studyEntry.touchFunctionTypeNative()
                        .touchFunctionKey(StudyEntry.FunctionKey.BIND_CHILD);
            }
        } else if (selfStudyType == SelfStudyType.QUICK_ORAL_ARITHMETIC) {
            if (null != context.getStudentDetail()) {
                studyEntry.touchFunctionTypeH5()
                        .touchFunctionKey(context.getCurrentSchema() + SelfStudyType.QUICK_ORAL_ARITHMETIC.getH5Url() + "?sid=" + context.getStudentDetail().getId() + "&type=mental&referrer=2")
                        .toucheTag(fetchTag(SelfStudyType.QUICK_ORAL_ARITHMETIC, context.getPosition(), false))
                        .touchLabelColor(StudyEntry.LabelColor.WHITE);
            } else {
                studyEntry.touchFunctionTypeNative()
                        .touchFunctionKey(StudyEntry.FunctionKey.BIND_CHILD);
            }
        } else if (selfStudyType == SelfStudyType.ORAL_ARITHMETIC_EXERCISE) {
            if (null != context.getStudentDetail()) {
                studyEntry.touchFunctionTypeH5()
                        .touchFunctionKey(context.getCurrentSchema() + SelfStudyType.ORAL_ARITHMETIC_EXERCISE.getH5Url() + "?sid=" + context.getStudentDetail().getId() + "&referrer=2&useNewCore=wk")
                        .toucheTag(fetchTag(SelfStudyType.ORAL_ARITHMETIC_EXERCISE, context.getPosition(), false))
                        .touchLabelColor(StudyEntry.LabelColor.WHITE);
            } else {
                studyEntry.touchFunctionTypeNative()
                        .touchFunctionKey(StudyEntry.FunctionKey.BIND_CHILD);
            }
        } else if (selfStudyType == SelfStudyType.MATH_PRACTICE) {
            if (null != context.getStudentDetail()) {
                studyEntry.touchFunctionTypeH5()
                        .touchFunctionKey(context.getCurrentSchema() + SelfStudyType.MATH_PRACTICE.getH5Url())
                        .toucheTag(fetchTag(SelfStudyType.MATH_PRACTICE, context.getPosition(), false))
                        .touchLabelColor(StudyEntry.LabelColor.WHITE);
            } else {
                studyEntry.touchFunctionTypeNative()
                        .touchFunctionKey(StudyEntry.FunctionKey.BIND_CHILD);
            }
        } else if (selfStudyType == SelfStudyType.ELEVEL_READING) {
            studyEntry = studyEntry
                    .touchFunctionTypeNative()
                    .touchFunctionKey(StudyEntry.FunctionKey.ELEVEL_READING)
                    .toucheTag(fetchTag(SelfStudyType.ELEVEL_READING, context.getPosition(), true))
                    .touchLabelTextColor(StudyEntry.LabelColor.YELLOW_1);
        } else if (selfStudyType == SelfStudyType.CLEVEL_READING) {
            studyEntry = studyEntry
                    .touchFunctionTypeNative()
                    .touchFunctionKey(StudyEntry.FunctionKey.CLEVEL_READING)
                    .toucheTag(fetchTag(SelfStudyType.CLEVEL_READING, context.getPosition(), true))
                    .touchLabelTextColor(StudyEntry.LabelColor.YELLOW_1);
        } else {
            // 接下来是所有增值产品的入口
            FairylandProduct fairylandProduct = context.getSst2FairyLandProductMap().get(selfStudyType);
            if (fairylandProduct == null) {
                return null;
            }

            StudyAppData appDataAlpsFuture = null;
            if (xiaoUSSTList.contains(selfStudyType) && !onlyXiaoU) {
                studyEntry.setLabel("个性化同步练习");
            } else if (!context.getStudyAppDataFutureMap().containsKey(selfStudyType)) {
                studyEntry.touchLabel(fairylandProduct.getProductDesc());
            } else {
                appDataAlpsFuture = context.getStudyAppDataFutureMap().get(selfStudyType).getUninterruptibly();
                String progress = getProgressFromData(appDataAlpsFuture);
                if (StringUtils.isBlank(progress)) {
                    studyEntry.setLabel(fairylandProduct.getProductDesc());
                } else {
                    studyEntry.setLabel(progress);
                }
            }
            //如果是阿芬提语数英,进报告页面
            if (endDayRange != null && endDayRange.getEndDate().after(DayRange.current().getEndDate())) { //已付费，进入游戏内
                String redirectUrl = fairylandProduct.fetchRedirectUrl(RuntimeMode.current()); //如果获取不到,或者家长通不支持，则跳转到详情页。否则跳转到游戏内
                if (StringUtils.isNotBlank(redirectUrl)) {
                    VendorApps vendorApps = vendorLoaderClient.loadVendor(fairylandProduct.getAppKey());
                    studyEntry.touchFunctionTypeNative()
                            .touchFunctionKey(StudyEntry.FunctionKey.FAIRYLAND_APP)
                            .touchExtra(generateFairylandExtra(fairylandProduct, vendorApps));
                } else {
                    studyEntry.touchFunctionTypeH5()
                            .touchFunctionKey(url2ProductInfo(context.getStudentDetail().getId(), fairylandProduct, context.getCurrentSchema(), context.getChannel()));
                }
                //小U，未付费进详情页，已付费，C端去应用，B端去报告
                if (selfStudyType.isAfenti() && context.getStudentDetail() != null && context.getStudentDetail().getClazz() != null) {
                    String xiaoUReportUrl = "/zion/nova-report";
                    studyEntry.touchFunctionTypeH5()
                            .touchFunctionKey(context.getCurrentSchema() + xiaoUReportUrl + "?subject=" + selfStudyType.getSubject() + "&sid=" + context.getStudentDetail().getId());

                }
            } else {
                studyEntry.touchFunctionTypeH5()
                        .touchFunctionKey(url2ProductInfo(context.getStudentDetail().getId(), fairylandProduct, context.getCurrentSchema(), context.getChannel()));
            }

            if (endDayRange != null) {
                if (endDayRange.getEndDate().after(DayRange.current().getEndDate())) {
                    studyEntry.toucheTag(fetchTag(selfStudyType, context.getPosition(), StudyEntry.Tag.payed, false));
                } else if (DateUtils.calculateDateDay(endDayRange.getEndDate(), 10).after(DayRange.current().getEndDate())) {
                    studyEntry.toucheTag(fetchTag(selfStudyType, context.getPosition(), StudyEntry.Tag.renew, false));
                }
            }
            //如果有 notify，则把 notify 优先显示
            if (appDataAlpsFuture != null && StringUtils.isNotBlank(appDataAlpsFuture.getNotifyContent()) && StringUtils.isNotBlank(appDataAlpsFuture.getNotifyUniqueId())) {
                studyEntry.touchLabelReminder(appDataAlpsFuture.getNotifyContent(), appDataAlpsFuture.getNotifyUniqueId());
            }
            studyEntry.setIconUrl("gridfs/" + fairylandProduct.getProductIcon());
        }
        if (endDayRange != null) {
            long deltaDay = DateUtils.dayDiff(endDayRange.getEndDate(), DayRange.current().getEndDate()) + 1;
            if (deltaDay == 0) {
                studyEntry.setBottomText("今日到期");
            } else if (deltaDay > 0 && deltaDay <= 14) {
                studyEntry.setBottomText("还有" + deltaDay + "天到期");
            }
        }
        return studyEntry;
    }

    private String url2ProductInfo(Long sid, FairylandProduct product, String mainSiteSchema, String channel) {

        //修改成商品详情页，调用不存在product == null的时候，暂时原有代码先保留。
        /*if (product == null) {
            return mainSiteSchema + "/view/mobile/parent/learning_app/detail.vpage?showAppList=true" + "&sid=" + sid + "&rel=xxx";
        }
        return mainSiteSchema + "/view/mobile/parent/learning_app/detail.vpage?showAppList=true" + "&sid=" + sid + "&productType=" + product.getAppKey() + "&order_refer=" + channel;*/

        if (product == null) {
            return null;
        }
        return mainSiteSchema + "/karp/commodity_details/index/list?showAppList=true&sid=" + sid + "&productType=" + product.getAppKey() + "&order_refer=330323";
    }

    private Map<String, Object> generateFairylandExtra(FairylandProduct fairylandProduct, VendorApps vendorApps) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (fairylandProduct == null || null == vendorApps) {
            return map;
        }
        String url = fairylandProduct.fetchRedirectUrl(RuntimeMode.current());
        map.put("url", url);
        map.put("name", fairylandProduct.getProductName());
        map.put("orientation", vendorApps.getOrientation());
        map.put("browser", vendorApps.getBrowser());
        map.put("fullScreen", true);
        map.put("hideTitle", true);
        //安卓特殊要求必传
        if (fairylandProduct.getAppKey().equals(SelfStudyType.LISTEN_WORLD.getOrderProductServiceType())) {
            map.put("useNewCore", "crosswalk");
        }
        return map;
    }

    private void dealIconCdnAndGlobalMSg(List<StudyEntry> entryList, IndexDataContext indexDataContext) {
        //添加全局的消息
        Map<SelfStudyType, MySelfStudyEntryGlobalMsg> mySelfStudyEntryGlobalMsgMap = mySelfStudyGlobalMsgServiceClient.getMySelfStudyEntryGlobalMsgMap();
        String currentCdnSchema = indexDataContext.getCurrentCdnSchema();
        entryList.forEach(t -> {
            if (t == null) {
                return;
            }
            t.setIconUrl(currentCdnSchema + t.getIconUrl());
            MySelfStudyEntryGlobalMsg mySelfStudyEntryGlobalMsg = mySelfStudyEntryGlobalMsgMap.get(t.getSelfStudyType());
            if (mySelfStudyEntryGlobalMsg != null && StringUtils.isNotBlank(mySelfStudyEntryGlobalMsg.getText())) {
                t.touchGlobalMsg(mySelfStudyEntryGlobalMsg);
            }
        });
    }

    private void dealDefaultLabel2Desc(StudyEntry studyEntry, IndexDataContext indexDataContext, boolean force) {
        if (studyEntry == null) {
            return;
        }
        SelfStudyType selfStudyType = studyEntry.getSelfStudyType();
        if (selfStudyType == null) {
            return;
        }
        if (!force && StringUtils.isNotBlank(studyEntry.getLabel())) {
            return;
        }
        String defaultLabel = SSTDescMap.get(selfStudyType);
        if (StringUtils.isNotBlank(defaultLabel)) {
            studyEntry.setLabel(defaultLabel);
        } else {
            Map<SelfStudyType, FairylandProduct> sst2FairyLandProductMap = indexDataContext.getSst2FairyLandProductMap();
            if (sst2FairyLandProductMap == null) {
                return;
            }
            FairylandProduct fairylandProduct = sst2FairyLandProductMap.get(selfStudyType);
            if (fairylandProduct == null) {
                return;
            }
            studyEntry.setLabel(fairylandProduct.getProductDesc());
        }
    }

    private IndexDataContext getIndexDataContext(User parent, StudentDetail studentDetail) {
        IndexDataContext context = IndexDataContext.empty;
        if (null != studentDetail) {
            context = asyncLoadIndexDataContext(parent, studentDetail);
        }
        context.setCurrentSchema(fetchMainsiteUrlByCurrentSchema());
        context.setCurrentCdnSchema(getCdnBaseUrlStaticSharedWithSep());
        context.setChannel(indexChannel);
        context.setParent(parent);
        context.setStudentDetail(studentDetail);
        context.setLogin(true);
        context.setHasChild(null != studentDetail);
        context.setVersion(getClientVersion());
        context.setSys(getClientSys());
        context.setPosition(IndexDataContext.Position.index);

        if (null == studentDetail) {
            context.setHitAdIgnoreGray(false);
            context.setHitLiveCastAdIgnoreGray(false);
            context.setHitLiveCastImageAdGray(false);
            context.setHitShutiaoGray(false);
        } else {
            boolean hitAdIgnoreGray = grayFunctionManagerClient.getStudentGrayFunctionManager()
                    .isWebGrayFunctionAvailable(studentDetail, "jztapp", "indexAdBlack", true);
            context.setHitAdIgnoreGray(hitAdIgnoreGray);

            boolean hitLiveCastAdIgnoreGray = grayFunctionManagerClient.getStudentGrayFunctionManager()
                    .isWebGrayFunctionAvailable(studentDetail, "jztapp", "liveCastAdBlack", true);
            context.setHitLiveCastAdIgnoreGray(hitLiveCastAdIgnoreGray);

            boolean hitLiveCastImageAdGray = grayFunctionManagerClient.getStudentGrayFunctionManager()
                    .isWebGrayFunctionAvailable(studentDetail, "jztapp", "liveCastImageAd", true);
            context.setHitLiveCastImageAdGray(hitLiveCastImageAdGray);

            boolean hitShutiaoGray = grayFunctionManagerClient.getStudentGrayFunctionManager()
                    .isWebGrayFunctionAvailable(studentDetail, "jztapp", "shutiaoEntry", true);
            context.setHitShutiaoGray(hitShutiaoGray);
        }
        return context;
    }

    private IndexDataContext asyncLoadIndexDataContext(User parent, StudentDetail studentDetail) {
        IndexDataContext context = new IndexDataContext();
        context.setParent(parent);
        context.setStudentDetail(studentDetail);
        context.setVersion(getClientVersion());
        context.setSys(getClientSys());
        final CountDownLatch latch = new CountDownLatch(3);
        AlpsThreadPool.getInstance().submit(() -> {
            // sst2FairyLandProductMap
            try {
                Map<SelfStudyType, FairylandProduct> sst2FairyLandProductMap = new HashMap<>();
                if (parent != null && studentDetail != null) {
                    List<FairylandProduct> parentAvailableFairylandProducts =
                            businessVendorServiceClient.getParentAvailableFairylandProducts(parent, studentDetail, FairyLandPlatform.PARENT_APP, FairylandProductType.APPS);

                    for (FairylandProduct fairylandProduct : parentAvailableFairylandProducts) {
                        OrderProductServiceType orderProductServiceType = OrderProductServiceType.safeParse(fairylandProduct.getAppKey());
                        if (orderProductServiceType == OrderProductServiceType.Unknown) {
                            continue;
                        }
                        SelfStudyType selfStudyType = SelfStudyType.fromOrderType(OrderProductServiceType.valueOf(fairylandProduct.getAppKey()));
                        if (selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN) {
                            continue;
                        }
                        sst2FairyLandProductMap.put(selfStudyType, fairylandProduct);
                    }
                }
                context.setSst2FairyLandProductMap(sst2FairyLandProductMap);
            } catch (Exception e) {
                logger.error("generate sst2FairyLandProductMap error : ", e);
                context.setSst2FairyLandProductMap(new HashMap<>());
            } finally {
                latch.countDown();
            }

        });

        AlpsThreadPool.getInstance().submit(() -> {
            // selfStudyTypeDayRangeMap
            try {
                if (studentDetail != null) {
                    Map<SelfStudyType, DayRange> selfStudyTypeDayRangeMap = parentSelfStudyPublicHelper.moneySSTLastDayMap(studentDetail.getId(), true);
                    context.setSelfStudyTypeDayRangeMapKeepExpired(selfStudyTypeDayRangeMap);
                    Map<SelfStudyType, DayRange> map = new HashMap<>();
                    Date now = new Date();
                    selfStudyTypeDayRangeMap.forEach((k, v) -> {
                        if (v.getEndDate().after(now))
                            map.put(k, v);
                    });
                    context.setSelfStudyTypeDayRangeMa(map);
                } else {
                    context.setSelfStudyTypeDayRangeMa(new HashMap<>());
                }
                boolean inUserBlackList = parent != null && userBlacklistServiceClient.isInUserBlackList(parent)
                        || (studentDetail != null && userBlacklistServiceClient.isInUserBlackList(studentDetail));
                context.setInBlackList(inUserBlackList);
            } catch (Exception e) {
                logger.error("generate selfStudyTypeDayRangeMap error : ", e);
                context.setSelfStudyTypeDayRangeMa(new HashMap<>());
            } finally {
                latch.countDown();
            }
        });

        AlpsThreadPool.getInstance().submit(() -> {
            try {
                if (studentDetail != null) {
                    Map<String, String> appKey2DescMap =
                            businessVendorServiceClient.fetchUserUseNumDesc(
                                    allShelfSSTList.stream().filter(t -> t.getOrderProductServiceType() != null && OrderProductServiceType.safeParse(t.getOrderProductServiceType()) != OrderProductServiceType.Unknown).map(t -> t.getOrderProductServiceType()).collect(Collectors.toList()), studentDetail);
                    context.setAppKey2DescMap(appKey2DescMap);
                } else {
                    context.setAppKey2DescMap(Collections.emptyMap());
                }
            } catch (Exception e) {
                logger.error("generate appKey2DescMap error : ", e);
                context.setAppKey2DescMap(Collections.emptyMap());
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map<SelfStudyType, AlpsFuture<StudyAppData>> studyDataFutureMap
                = getUserStudyDataFutureMap(SSTOrderMap.keySet(), parent, studentDetail);
        context.setStudyAppDataFutureMap(studyDataFutureMap);
        context.setCurrentSchema(fetchMainsiteUrlByCurrentSchema());
        context.setCurrentCdnSchema(getCdnBaseUrlStaticSharedWithSep());
        context.setParent(parent);
        context.setStudentDetail(studentDetail);
        context.setLogin(parent != null);
        context.setHasChild(studentDetail != null);
        context.setShenhe(parent == null ? false : isShenhe(parent.getId()));
        context.setHasZuoYeCuoTi(studentHasZuoyecuotiEntry(studentDetail));
        return context;
    }

    private Map<SelfStudyType, AlpsFuture<StudyAppData>> getUserStudyDataFutureMap(Collection<SelfStudyType> selfStudyTypes, User parent, StudentDetail studentDetail) {
        Map<SelfStudyType, AlpsFuture<StudyAppData>> map = new HashMap<>();
        for (SelfStudyType selfStudyType : selfStudyTypes) {
            AlpsFuture<StudyAppData> appDataFuture = mySelfStudyService.loadStudyAppData(studentDetail.getId(), selfStudyType);
            map.put(selfStudyType, appDataFuture);
        }
        AlpsFuture<StudyAppData> picListenFuture = mySelfStudyService.loadStudyAppData(studentDetail.getId(), SelfStudyType.PICLISTEN_ENGLISH);
        map.put(SelfStudyType.PICLISTEN_ENGLISH, picListenFuture);
        if (selfStudyTypes.contains(SelfStudyType.ALBUM)) {
            AlpsFuture<StudyAppData> albumFuture = mySelfStudyService.loadStudyAppData(parent.getId(), SelfStudyType.ALBUM);
            map.put(SelfStudyType.ALBUM, albumFuture);
        }
        if (selfStudyTypes.contains(SelfStudyType.LIVECAST)) {
            AlpsFuture<StudyAppData> liveCastFuture = mySelfStudyService.loadStudyAppData(parent.getId(), SelfStudyType.LIVECAST);
            map.put(SelfStudyType.LIVECAST, liveCastFuture);
        }
        map.put(SelfStudyType.ZUOYECUOITI, mySelfStudyService.loadStudyAppData(studentDetail.getId(), SelfStudyType.ZUOYECUOITI));
        AlpsFuture<StudyAppData> dubbingFuture = mySelfStudyService.loadStudyAppData(studentDetail.getId(), SelfStudyType.DUBBING);
        map.put(SelfStudyType.DUBBING, dubbingFuture);
        return map;
    }

    private Boolean studentHasZuoyecuotiEntry(StudentDetail studentDetail) {
        return studentDetail != null && studentDetail.getClazz() != null && !studentDetail.getClazz().isTerminalClazz() && studentDetail.isPrimaryStudent();
    }

    private Boolean isShenhe(Long parentId) {
        return parentId != null && parentId.equals(20001L);
    }

    private String getClientVersion() {
        return getRequestString(REQ_APP_NATIVE_VERSION);
    }

    private String getClientSys() {
        return getRequestString(REQ_SYS);
    }

    private boolean isStuding(StudyEntry entry, Map<SelfStudyType, DayRange> userProductMaps) {
        if (null == entry || null == entry.getSelfStudyType() || MapUtils.isEmpty(userProductMaps)) {
            return false;
        }

        DayRange range = userProductMaps.get(entry.getSelfStudyType());
        if (range == null) {
            return false;
        }

        long time = System.currentTimeMillis();

        return time <= range.getEndTime();
    }

    private List<Subject> loadPaiedPicListenBookSubject(User parent) {
        Map<String, PicListenBookPayInfo> bookPicListen = picListenCommonService.userBuyBookPicListenLastDayMap(parent, false);
        Map<String, TextBookManagement> books = textBookManagementLoaderClient.getTextBookByIds(bookPicListen.keySet());
        List<Subject> subjects = new LinkedList<>();
        bookPicListen.keySet().forEach(key -> {
            PicListenBookPayInfo payInfo = bookPicListen.get(key);
            if (null == payInfo.getDayRange() || payInfo.getDayRange().getEndTime() <= System.currentTimeMillis()) {
                return;
            }

            TextBookManagement management = books.get(key);
            if (null == management) {
                return;
            }
            Subject subject = management.fetchBookSubject();
            subjects.add(subject);
        });

        return subjects;
    }
}
