package com.voxlearning.utopia.admin.controller.equator.newwonderland.rank;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.equator.common.api.data.mailbox.MaterialBody;
import com.voxlearning.equator.common.api.enums.partner.SchoolYearAndTermType;
import com.voxlearning.equator.service.configuration.api.annotation.RankFieldDesc;
import com.voxlearning.equator.service.configuration.api.entity.activity.ActivityConfigInfo;
import com.voxlearning.equator.service.configuration.api.entity.material.Material;
import com.voxlearning.equator.service.configuration.client.GeneralConfigServiceClient;
import com.voxlearning.equator.service.configuration.client.ResourceConfigServiceClient;
import com.voxlearning.equator.service.configuration.resourcetablemanage.entity.ResourceStaticFileInfo;
import com.voxlearning.equator.service.mailbox.api.client.GrowingWorldMailClient;
import com.voxlearning.equator.service.material.api.data.MaterialModificationContext;
import com.voxlearning.equator.service.material.client.MaterialServiceClient;
import com.voxlearning.equator.service.rank.api.constant.RankRewardType;
import com.voxlearning.equator.service.rank.api.constant.RedisRankCacheKeyType;
import com.voxlearning.equator.service.rank.api.data.base.RankBase;
import com.voxlearning.equator.service.rank.api.vo.response.RankRewardResponse;
import com.voxlearning.equator.service.rank.client.RankLoaderClient;
import com.voxlearning.equator.service.rank.client.RankServiceClient;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 新成长世界 通用排行榜
 *
 * @author liu jingchao
 * @version 18/08/14
 */
@Controller
@RequestMapping(value = "equator/newwonderland/common/rank")
public class CommonRankManagerController extends AbstractEquatorController {

    private static String DEFAULT_USER_IMAGE_NAME = "https://cdn-portrait.17zuoye.cn/upload/images/avatar/avatar_normal.png";

    private static String TEST_HOST = "https://cdn-portrait.test.17zuoye.net/gridfs/";

    private static String ONLINE_HOST = "https://cdn-portrait.17zuoye.cn/gridfs/";

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private RankLoaderClient rankLoaderClient;
    @Inject
    private RankServiceClient rankServiceClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private ResourceConfigServiceClient resourceConfigServiceClient;
    @Inject
    private GeneralConfigServiceClient generalConfigServiceClient;
    @Inject
    private GrowingWorldMailClient growingWorldMailClient;
    @Inject
    private MaterialServiceClient materialServiceClient;

    /**
     * 获取排行榜列表详情
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String fetchRankDetail(Model model) {

        model.addAttribute("rankTypeList", Arrays.stream(RedisRankCacheKeyType.values())
                .map(p -> new MapMessage().set("type", p.name()).set("name", p.getDesc()).set("expiration", p.fetchExpirationTime()).set("range", p.fetchRankRange()).set("keys", Arrays.asList(p.getSupplementKeys())))
                .collect(Collectors.toList()));

        model.addAttribute("supplementKeyTypeList", Arrays.stream(RedisRankCacheKeyType.SupplementKeyType.values())
                .map(p -> new MapMessage().set("name", p.name()).set("desc", p.getDesc()))
                .collect(Collectors.toList()));

        Long studentId = getRequestLong("studentId");
        if (studentId == 0) {
            return "equator/rank/ranklist";
        }
        model.addAttribute("studentId", studentId);

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            model.addAttribute("error", "用户信息不存在！");
            return "equator/rank/ranklist";
        }

        // 指定的榜单种类
        String targetRankType = getRequestString("targetRankType");
        RedisRankCacheKeyType currentRankType = RedisRankCacheKeyType.safeParse(targetRankType);

        // 是否是变参查询（手动修改了参数）
        Boolean handSelect = getRequestString("selectType").equals("hand");

        // 获取所有省份信息
        List<ExRegion> exRegionList = raikouSystem.getRegionBuffer().loadProvinces().stream().filter(region -> region.getProvinceCode() < 700000).collect(Collectors.toList());
        model.addAttribute("exRegionList", exRegionList);

        // 获取活动列表
        List<ActivityConfigInfo> activityConfigInfoList = (List<ActivityConfigInfo>) resourceConfigServiceClient.getBuffer(ActivityConfigInfo.class).loadDataList();
        activityConfigInfoList = activityConfigInfoList.stream()
                .filter(a -> StringUtils.isNotBlank(a.getId()) && StringUtils.isNotBlank(a.getActivityType()) && a.fetchStartDate() != null && a.fetchEndDate() != null)
                .filter(a -> a.fetchEnvMode() != null && RuntimeMode.current().le(a.fetchEnvMode()))
                .sorted(Comparator.comparingLong(ActivityConfigInfo::getEndDate).reversed())
                .collect(Collectors.toList());

        List<MapMessage> activityInfoListMap = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(activityConfigInfoList)) {
            activityConfigInfoList.stream().sorted(Comparator.comparing(ActivityConfigInfo::fetchStartDate).reversed()).forEach(activity -> {
                if (activityInfoListMap.size() < 10) {
                    activityInfoListMap.add(new MapMessage()
                            .add("id", activity.getId())
                            .add("projectName", activity.getProjectName())
                            .add("startDate", DateUtils.dateToString(new Date(activity.getStartDate()), "MM-dd"))
                            .add("endDate", DateUtils.dateToString(new Date(activity.getEndDate()), "MM-dd")));
                }
            });

        }
        model.addAttribute("activityConfigInfoList", activityInfoListMap);
        // 获取第一个活动
        String firstActivityId = CollectionUtils.isNotEmpty(activityConfigInfoList) ? activityConfigInfoList.get(0).getId() : "";

        // 排行榜附加信息集整理
        // 原始默认值
        Map<String, String> supplementOriginalInfoMap = new HashMap<>();
        // 最终值
        Map<String, String> supplementFinalInfoMap = new HashMap<>();
        // 附加key种类列表
        List<RedisRankCacheKeyType.SupplementKeyType> rankSupplementKeyList = Arrays.asList(RedisRankCacheKeyType.SupplementKeyType.values());
        rankSupplementKeyList.forEach(supplementKeyType -> {
            String currentValue = "";

            switch (supplementKeyType) {
                case CID:
                    currentValue = studentDetail.getClazzId() == null ? "" : studentDetail.getClazzId().toString();
                    break;
                case SCHID:
                    if (studentDetail.getClazzId() != null) {
                        Clazz clazz = raikouSystem.loadClazz(studentDetail.getClazzId());
                        currentValue = clazz != null ? clazz.getSchoolId().toString() : "";
                    }
                    break;
                case ATTID:
                    currentValue = firstActivityId;
                    break;
                case PCD:
                    currentValue = studentDetail.getRootRegionCode() == null ? "" : studentDetail.getRootRegionCode().toString();
                    break;
                case SCHY:
                    currentValue = currentRankType == RedisRankCacheKeyType.CR_PARTNER_COMPETITION_STUDENT_PROVINCE_RANK
                            ? String.valueOf(SchoolYearAndTermType.fetchCurrentTypeValue())     // 伙伴榜单使用自定义的学年方式
                            : String.valueOf(SchoolYear.newInstance().year());
                    break;
                case LVL:
                    currentValue = studentDetail.getClazzLevelAsInteger() == null ? "" : studentDetail.getClazzLevelAsInteger().toString();
                    break;
                case FMD:
                    currentValue = DateUtils.dateToString(WeekRange.current().getStartDate(), "yyyyMMdd");  // 默认显示本周一
                    break;
            }

            supplementOriginalInfoMap.put(supplementKeyType.name(), currentValue);

            // 获取通过URL接收到的参数，如果有值，则覆盖掉默认值
            if (handSelect && StringUtils.isNotBlank(getRequestString(supplementKeyType.name()))) {
                supplementFinalInfoMap.put(supplementKeyType.name(), getRequestString(supplementKeyType.name()));
            } else {
                supplementFinalInfoMap.put(supplementKeyType.name(), currentValue);
            }
        });

        model.addAttribute("supplementOriginalInfoMap", supplementOriginalInfoMap);
        model.addAttribute("supplementFinalInfoMap", supplementFinalInfoMap);

        if (currentRankType == null) {
            return "equator/rank/ranklist";
        }
        model.addAttribute("targetRankType", currentRankType.name());
        model.addAttribute("currentRankType", currentRankType);

        // 排行榜显示字段描述信息
        Map<String, String> rankFieldDescInfo = new HashMap<>();
        Arrays.stream(currentRankType.getRankClass().getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .filter(field -> field.getAnnotation(RankFieldDesc.class) != null)
                .forEach(field -> {
                    rankFieldDescInfo.put(field.getName(), field.getAnnotation(RankFieldDesc.class).value());
                });

        String[] supplementValues = new String[currentRankType.getSupplementKeys().length + 1];
        for (int index = 0; index < currentRankType.getSupplementKeys().length; index++) {
            supplementValues[index] = supplementFinalInfoMap.getOrDefault(currentRankType.getSupplementKeys()[index], "");
        }

        // 追加其他查询变量
        String otherSupplementValue = handSelect ? getRequestString("OTHER") : "";
        model.addAttribute("OTHER", otherSupplementValue);
        supplementValues[currentRankType.getSupplementKeys().length] = otherSupplementValue;

        // 查询排行榜列表详情
        List<Object> rankInfoList = (List<Object>) rankLoaderClient.getRankLoader().loadMemberRankInfoList(currentRankType.name(), supplementValues).getUninterruptibly();
        List<Map<String, Object>> rankInfoMapList = rankInfoList.stream().map(rankInfo -> {
            Map<String, Object> currentObject = JsonUtils.safeConvertObjectToMap(rankInfo);
            RankBase currentRankBase = (RankBase) rankInfo;
            currentObject.put("profileUrl", fetchImageUrlByDefault((String) currentObject.getOrDefault("profileUrl", "")));
            currentObject.put("lastUpdateRankTime", currentRankBase.fetchLastUpdateTime());
            return currentObject;
        }).collect(Collectors.toList());

        // 查询当前用户排名信息
        RankBase userRankBase = (RankBase) rankLoaderClient.getRankLoader().fetchTargetMemberRank(currentRankType.name(), supplementValues, studentId.toString()).getUninterruptibly();
        if (userRankBase == null) {
            // 用班级ID查询
            userRankBase = (RankBase) rankLoaderClient.getRankLoader().fetchTargetMemberRank(currentRankType.name(), supplementValues, studentDetail.getClazzId().toString()).getUninterruptibly();
        }
        if (userRankBase == null) {
            // 如果都查不到就初始一个
            userRankBase = new RankBase(studentId.toString());
            userRankBase.setRanking(0);
        }
        userRankBase.setStudentName(studentDetail.fetchRealname());
        userRankBase.setProfileUrl(fetchImageUrlByDefault(studentDetail.fetchImageUrl()));
        userRankBase.setClassName(studentDetail.getClazz() != null ? studentDetail.getClazz().formalizeClazzName() : "");
        userRankBase.setSchoolName(studentDetail.getStudentSchoolName());

        Long rankCount = (Long) rankLoaderClient.getRankLoader().fetchMemberCountByScoreRange(currentRankType.name(), supplementValues, 0D, 100000000D).getUninterruptibly();
        Long surplusTTL = (Long) rankLoaderClient.getRankLoader().fetchRankTTL(currentRankType.name(), supplementValues).getUninterruptibly();
        model.addAttribute("rankFieldDescInfo", rankFieldDescInfo);
        model.addAttribute("rankInfoMapList", rankInfoMapList);
        model.addAttribute("userRankBase", userRankBase);
        model.addAttribute("rankCount", rankCount);
        model.addAttribute("surplusTTL", (int) (surplusTTL / 3600));
        model.addAttribute("defaultTTL", (int) (currentRankType.fetchExpirationTime() / 3600));
        // 查询备份key的剩余有效期
        if (!"".equals(supplementValues[supplementValues.length - 1])) {
            supplementValues = Arrays.copyOf(supplementValues, supplementValues.length + 1);
        }
        supplementValues[supplementValues.length - 1] = currentRankType.fetchRankBackupKeyFlag();
        Long surplusTTLForBackKey = (Long) rankLoaderClient.getRankLoader().fetchRankTTL(currentRankType.name(), supplementValues).getUninterruptibly();
        model.addAttribute("surplusTTLForBackKey", (int) (surplusTTLForBackKey / 3600));
        String adminUserName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("isSuperAdmin", isSuperAdmin(adminUserName));

        // 当前榜种所有奖励种类列表
        List<RankRewardType> currRankRewardType = RankRewardType.loadTargetRankRewardTypeList(currentRankType);
        model.addAttribute("rankRewardTypeList", currRankRewardType.stream()
                .map(p -> new MapMessage().set("type", p.name()).set("name", p.getDesc()))
                .collect(Collectors.toList()));

        Map<String, RankRewardResponse> rewardResponseMap = (Map<String, RankRewardResponse>) rankLoaderClient.getRankLoader().loadTargetRankTypeListReward(currRankRewardType.stream().map(RankRewardType::name).collect(Collectors.toList())).getUninterruptibly();
        model.addAttribute("rewardResponseMap", rewardResponseMap);

        // 获取道具资源
        Map<String, String> resourceIconMap = resourceConfigServiceClient.getResourceStaticFileInfoFromBuffer()
                .stream()
                .filter(Objects::nonNull)
                .filter(e -> StringUtils.equals(e.getFirstCategory(), "NewGrowingWorld"))
                .filter(e -> StringUtils.equals(e.getSecondCategory(), "Resource"))
                .collect(Collectors.toMap(ResourceStaticFileInfo::getResourceName, ResourceStaticFileInfo::getUrl));
        List<Material> materialInfoList = resourceConfigServiceClient.getBuffer(Material.class).loadDataList();
        materialInfoList.forEach(materialInfo -> materialInfo.setIcon(resourceIconMap.getOrDefault(materialInfo.getIcon(), "")));
        Map<String, Material> materialIdInfoCfgMap = materialInfoList.stream().collect(Collectors.toMap(Material::getId, materialInfo -> materialInfo));
        model.addAttribute("materialIdInfoCfgMap", materialIdInfoCfgMap);

        return "equator/rank/ranklist";
    }

    /**
     * 重置指定榜单的缓存有效时间
     */
    @RequestMapping(value = "resetrankttl.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetRankTTL() {
        // 指定的榜单种类
        String targetRankType = getRequestString("targetRankType");
        RedisRankCacheKeyType currentRankType = RedisRankCacheKeyType.safeParse(targetRankType);

        if (currentRankType == null) {
            return MapMessage.errorMessage("缺少指定榜单种类！");
        }

        // 排行榜附加信息集整理
        Map<String, String> supplementFinalInfoMap = new HashMap<>();
        List<RedisRankCacheKeyType.SupplementKeyType> rankSupplementKeyList = Arrays.asList(RedisRankCacheKeyType.SupplementKeyType.values());
        rankSupplementKeyList.forEach(supplementKeyType -> {
            // 获取通过URL接收到的参数，如果有值，则覆盖掉默认值
            if (StringUtils.isNotBlank(getRequestString(supplementKeyType.name()))) {
                supplementFinalInfoMap.put(supplementKeyType.name(), getRequestString(supplementKeyType.name()));
            } else {
                supplementFinalInfoMap.put(supplementKeyType.name(), "");
            }
        });

        // 追加其他查询变量
        String otherSupplementValue = getRequestString("OTHER");
        int supplementValuesLength = StringUtils.isBlank(otherSupplementValue) ? currentRankType.getSupplementKeys().length : currentRankType.getSupplementKeys().length + 1;
        String[] supplementValues = new String[supplementValuesLength];
        for (int index = 0; index < currentRankType.getSupplementKeys().length; index++) {
            supplementValues[index] = supplementFinalInfoMap.getOrDefault(currentRankType.getSupplementKeys()[index], "");
        }

        if (StringUtils.isNotBlank(otherSupplementValue)) {
            supplementValues[currentRankType.getSupplementKeys().length] = otherSupplementValue;
        }

        Boolean result = rankServiceClient.getRankService().resetRankTTL(currentRankType.name(), supplementValues);
        return result ? MapMessage.successMessage() : MapMessage.errorMessage("操作失败！");
    }

    /**
     * 删除排行榜中指定成员的信息
     */
    @RequestMapping(value = "removeuserrankinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeTargetMemberRankInfo() {
        // 指定的榜单种类
        String targetRankType = getRequestString("targetRankType");
        RedisRankCacheKeyType currentRankType = RedisRankCacheKeyType.safeParse(targetRankType);
        String targetId = getRequestString("targetId");

        if (currentRankType == null || StringUtils.isBlank(targetId)) {
            return MapMessage.errorMessage("缺少指定榜单种类或成员ID！");
        }

        String adminUserName = getCurrentAdminUser().getAdminUserName();
        if (StringUtils.isBlank(adminUserName) || !isSuperAdmin(adminUserName)) {
            return MapMessage.errorMessage("暂时没有操作权限，请修改通用配置'CommonRankSuperAdminList'后再进行操作！");
        }

        // 排行榜附加信息集整理
        Map<String, String> supplementFinalInfoMap = new HashMap<>();
        List<RedisRankCacheKeyType.SupplementKeyType> rankSupplementKeyList = Arrays.asList(RedisRankCacheKeyType.SupplementKeyType.values());
        rankSupplementKeyList.forEach(supplementKeyType -> {
            // 获取通过URL接收到的参数，如果有值，则覆盖掉默认值
            if (StringUtils.isNotBlank(getRequestString(supplementKeyType.name()))) {
                supplementFinalInfoMap.put(supplementKeyType.name(), getRequestString(supplementKeyType.name()));
            } else {
                supplementFinalInfoMap.put(supplementKeyType.name(), "");
            }
        });

        // 追加其他查询变量
        String otherSupplementValue = getRequestString("OTHER");
        int supplementValuesLength = StringUtils.isBlank(otherSupplementValue) ? currentRankType.getSupplementKeys().length : currentRankType.getSupplementKeys().length + 1;
        String[] supplementValues = new String[supplementValuesLength];
        for (int index = 0; index < currentRankType.getSupplementKeys().length; index++) {
            supplementValues[index] = supplementFinalInfoMap.getOrDefault(currentRankType.getSupplementKeys()[index], "");
        }
        if (StringUtils.isNotBlank(otherSupplementValue)) {
            supplementValues[currentRankType.getSupplementKeys().length] = otherSupplementValue;
        }

        Boolean result = rankServiceClient.getRankService().removeTargetMemberRankInfo(currentRankType.name(), supplementValues, targetId);
        return result ? MapMessage.successMessage() : MapMessage.errorMessage("操作失败！");
    }

    /**
     * 删除/恢复排行榜
     */
    @RequestMapping(value = "removerank.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeTargetRank() {
        // 指定的榜单种类
        String targetRankType = getRequestString("targetRankType");
        RedisRankCacheKeyType currentRankType = RedisRankCacheKeyType.safeParse(targetRankType);

        if (currentRankType == null) {
            return MapMessage.errorMessage("缺少指定榜单种类！");
        }

        String adminUserName = getCurrentAdminUser().getAdminUserName();
        if (StringUtils.isBlank(adminUserName) || !isSuperAdmin(adminUserName)) {
            return MapMessage.errorMessage("暂时没有操作权限，请修改通用配置'CommonRankSuperAdminList'后再进行操作！");
        }

        // 排行榜附加信息集整理
        Map<String, String> supplementFinalInfoMap = new HashMap<>();
        List<RedisRankCacheKeyType.SupplementKeyType> rankSupplementKeyList = Arrays.asList(RedisRankCacheKeyType.SupplementKeyType.values());
        rankSupplementKeyList.forEach(supplementKeyType -> {
            // 获取通过URL接收到的参数，如果有值，则覆盖掉默认值
            if (StringUtils.isNotBlank(getRequestString(supplementKeyType.name()))) {
                supplementFinalInfoMap.put(supplementKeyType.name(), getRequestString(supplementKeyType.name()));
            } else {
                supplementFinalInfoMap.put(supplementKeyType.name(), "");
            }
        });

        // 追加其他查询变量
        String otherSupplementValue = getRequestString("OTHER");
        int supplementValuesLength = StringUtils.isBlank(otherSupplementValue) ? currentRankType.getSupplementKeys().length : currentRankType.getSupplementKeys().length + 1;
        String[] supplementValues = new String[supplementValuesLength];
        for (int index = 0; index < currentRankType.getSupplementKeys().length; index++) {
            supplementValues[index] = supplementFinalInfoMap.getOrDefault(currentRankType.getSupplementKeys()[index], "");
        }

        if (StringUtils.isNotBlank(otherSupplementValue)) {
            supplementValues[currentRankType.getSupplementKeys().length] = otherSupplementValue;
        }

        // recover:true时表示恢复
        Boolean recover = getRequestBool("recover");

        Boolean result = recover ? rankServiceClient.getRankService().recoverRankFromBackup(currentRankType.name(), supplementValues)
                : rankServiceClient.getRankService().removeRankWithBackup(currentRankType.name(), supplementValues);
        return result ? MapMessage.successMessage() : MapMessage.errorMessage("操作失败！");
    }

    /**
     * 获取排行榜奖励列表详情
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "reward.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String fetchRankRewardList(Model model) {

        model.addAttribute("rankRewardTypeList", Arrays.stream(RankRewardType.values())
                .map(p -> new MapMessage().set("type", p.name()).set("name", p.getDesc()))
                .collect(Collectors.toList()));

        Map<String, RankRewardResponse> rewardResponseMap = (Map<String, RankRewardResponse>) rankLoaderClient.getRankLoader().loadTargetRankTypeListReward(Collections.EMPTY_LIST).getUninterruptibly();
        model.addAttribute("rewardResponseMap", rewardResponseMap);

        Map<String, String> resourceIconMap = resourceConfigServiceClient.getResourceStaticFileInfoFromBuffer()
                .stream()
                .filter(Objects::nonNull)
                .filter(e -> StringUtils.equals(e.getFirstCategory(), "NewGrowingWorld"))
                .filter(e -> StringUtils.equals(e.getSecondCategory(), "Resource"))
                .collect(Collectors.toMap(ResourceStaticFileInfo::getResourceName, ResourceStaticFileInfo::getUrl));
        List<Material> materialInfoList = resourceConfigServiceClient.getBuffer(Material.class).loadDataList();
        materialInfoList.forEach(materialInfo -> materialInfo.setIcon(resourceIconMap.getOrDefault(materialInfo.getIcon(), "")));
        Map<String, Material> materialIdInfoCfgMap = materialInfoList.stream().collect(Collectors.toMap(Material::getId, materialInfo -> materialInfo));
        model.addAttribute("materialIdInfoCfgMap", materialIdInfoCfgMap);

        Long studentId = getRequestLong("studentId");
        if (studentId == 0) {
            return "equator/rank/rewardlist";
        }
        model.addAttribute("studentId", studentId);

        return "equator/rank/rewardlist";
    }

    /**
     * 发送指定排行榜奖励信息
     */
    @RequestMapping(value = "sendrankreward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendRankReward() {
        // 指定的榜单奖励种类
        String targetRankRewardType = getRequestString("targetRankRewardType");
        Integer targetRanking = getRequestInt("targetRanking");
        Long targetStudentId = getRequestLong("targetStudentId");
        String targetMailContent = getRequestString("targetMailContent");

        RankRewardType currentRankRewardType = RankRewardType.safeParse(targetRankRewardType);

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(targetStudentId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("用户信息不存在！");
        }

        if (currentRankRewardType == null) {
            return MapMessage.errorMessage("缺少指定榜单奖励种类！");
        }

        // 查询奖励
        Map<String, Integer> rewardInfoMap = (Map<String, Integer>) rankLoaderClient.getRankLoader().loadTargetRankingRewardMap(currentRankRewardType.name(), targetRanking).getUninterruptibly();
        if (MapUtils.isEmpty(rewardInfoMap)) {
            return MapMessage.errorMessage("指定的名次没有可发送的奖励！");
        }

        List<MaterialModificationContext> materialModificationContextList = new ArrayList<>();
        rewardInfoMap.forEach((materialId, materialCount) -> {
            MaterialModificationContext context = new MaterialModificationContext();
            context.setUserId(targetStudentId);
            context.setMaterialId(materialId);
            context.setQuantity(materialCount);
            materialModificationContextList.add(context);
        });
        // 更改用户道具
        MapMessage mapMessage = materialServiceClient.getMaterialService().modify(materialModificationContextList);

        if (mapMessage.isSuccess()) {
            if (StringUtils.isNotBlank(targetMailContent)) {
                // 发送邮件告知
                growingWorldMailClient.sendGrowingWorldRewardMail(targetStudentId, targetMailContent, materialModificationContextList.stream().map(info -> {
                    return new MaterialBody(info.getMaterialId(), info.getQuantity());
                }).collect(Collectors.toList()));
            }
            return MapMessage.successMessage("操作成功！");
        } else {
            return MapMessage.errorMessage("操作失败！");
        }
    }

    private Boolean isSuperAdmin(String userId) {
        if (StringUtils.isBlank(userId)) {
            return false;
        }
        try {
            // 从通用配置中获取超级管理员名单
            String key = "CommonRankSuperAdminList";
            String commonRankSuperAdminList = generalConfigServiceClient.loadConfigValueFromClientBuffer(key);
            return commonRankSuperAdminList.indexOf(userId) >= 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    // 头像拼接
    private String fetchImageUrlByDefault(String imgUrl) {
        if (StringUtils.isNotEmpty(imgUrl) && !imgUrl.contains("avatar_normal")) {
            return RuntimeMode.le(Mode.TEST) ? TEST_HOST + imgUrl.replace("gridfs/", "") : ONLINE_HOST + imgUrl.replace("gridfs/", "");
        } else {
            return DEFAULT_USER_IMAGE_NAME;
        }
    }
}