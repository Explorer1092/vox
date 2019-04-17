/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.SchoolType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.service.crm.CrmSchoolClueService;
import com.voxlearning.utopia.admin.service.crm.CrmWorkRecordService;
import com.voxlearning.utopia.admin.viewdata.SchoolCheckDetailView;
import com.voxlearning.utopia.admin.viewdata.SchoolClueDetailView;
import com.voxlearning.utopia.admin.viewdata.SchoolClueView;
import com.voxlearning.utopia.api.constant.CrmSchoolClueStatus;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.core.helper.AmapMapApi;
import com.voxlearning.utopia.entity.crm.CrmSchoolClue;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/11/9
 */
@Controller
@RequestMapping("/crm/school_clue")
public class CrmSchoolClueController extends CrmAbstractController {

    private static final String SCHOOL_AUTH_OPERATORS = "school_auth_operators";
    private static final String SCHOOL_INFO_OPERATORS = "school_info_operators";
    private static final String OP_AUTH = "op_auth";
    private static final String OP_INFO = "op_info";
    private static final String OP_CRITICAL = "op_critical";
    private static final String OP_SIGN_IN = "op_sign_in";
    private static final Integer PAGE_SIZE = 50;
    private static final List<Integer> AUTHENTICATE_LIST;

    static {
        AUTHENTICATE_LIST = new ArrayList<>();
        AUTHENTICATE_LIST.add(1);
        AUTHENTICATE_LIST.add(4);
        AUTHENTICATE_LIST.add(5);
    }

    @Inject private RaikouSystem raikouSystem;

    @Inject CrmSchoolClueService crmSchoolClueService;
    @Inject CrmWorkRecordService crmWorkRecordService;

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    @RequestMapping(value = "clue_list.vpage")
    public String clueList(Model model) {
        AuthCurrentAdminUser user = getCurrentAdminUser();
        // 判断用户是否有学校鉴定权限
        boolean schoolAuthOperate = false;
        String authOperators = crmConfigService.$loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), SCHOOL_AUTH_OPERATORS);
        if (authOperators.contains(user.getAdminUserName())) {
            schoolAuthOperate = true;
        }
        // 判断用户是否有信息完善权限
        boolean schoolInfoOperate = false;
        String infoOperators = crmConfigService.$loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), SCHOOL_INFO_OPERATORS);
        if (infoOperators.contains(user.getAdminUserName())) {
            schoolInfoOperate = true;
        }
        if (RuntimeMode.isDevelopment()) {
            schoolAuthOperate = schoolInfoOperate = true;
        }
        model.addAttribute("schoolAuthOperate", schoolAuthOperate);
        model.addAttribute("schoolInfoOperate", schoolInfoOperate);
        // 没有鉴定，也没有信息完善权限的人走开
        if (!schoolAuthOperate && !schoolInfoOperate) {
            return "crm/school_clue/clue_list";
        }
        // 查询条件
        String querySchoolName = getRequestString("schoolName"); // 学校名
        Integer checkStatus = getRequestInt("check_status", 1);        // 审核状态
        Integer authStatus = getRequestInt("auth_status", 1);
        String updateStart = getRequestString("updateStart");
        String updateEnd = getRequestString("updateEnd");
        Date updateStartTime = null;
        if (StringUtils.isNotBlank(updateStart)) {
            updateStartTime = DateUtils.stringToDate(updateStart + " 00:00:00");
        }
        Date updateEndTime = null;
        if (StringUtils.isNotBlank(updateEnd)) {
            updateEndTime = DateUtils.stringToDate(updateEnd + " 23:59:59");
        }

        List<CrmSchoolClue> schoolClues = crmSchoolClueService.loadLocationSchoolClues(querySchoolName, updateStartTime, updateEndTime, checkStatus);
        Map<Long, List<CrmSchoolClue>> schoolClueMap = schoolClues.stream().collect(Collectors.groupingBy(CrmSchoolClue::getSchoolId, Collectors.toList()));
        Set<Long> schoolIds = schoolClues.stream().map(CrmSchoolClue::getSchoolId).collect(Collectors.toSet());
        List<School> allSchool = new ArrayList<>();
        if (authStatus == 1) {
            allSchool = raikouSystem.loadSchools(schoolIds)
                    .values()
                    .stream()
                    .filter(p -> p.getSchoolAuthenticationState() == AuthenticationState.WAITING)
                    .collect(Collectors.toList());
        }
        if (authStatus == 2) {
            allSchool = raikouSystem.loadSchools(schoolIds)
                    .values()
                    .stream()
                    .filter(p -> p.getSchoolAuthenticationState() == AuthenticationState.SUCCESS)
                    .collect(Collectors.toList());
        }
        List<SchoolClueView> views = allSchool
                .stream()
                .map(p -> createView(p, schoolClueMap.get(p.getId())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        model.addAttribute("querySchoolName", querySchoolName);
        model.addAttribute("authStatus", authStatus);
        model.addAttribute("checkStatus", checkStatus);
        model.addAttribute("queryUpdateTimeStart", updateStart);
        model.addAttribute("queryUpdateTimeEnd", updateEnd);
        if (CollectionUtils.isNotEmpty(views)) {
            views.sort((o1, o2) -> (o1.getSchoolUpdateTime().compareTo(o2.getSchoolUpdateTime())));
            int pageSize = PAGE_SIZE;
            int totalPage;
            if (views.size() % pageSize == 0) {
                totalPage = views.size() / pageSize;
            } else {
                totalPage = views.size() / pageSize + 1;
            }
            int number = getRequestInt("PAGE") >= 0 ? getRequestInt("PAGE") : 0;
            if (number > totalPage - 1) {
                number = totalPage - 1;
            }
            int totalCount = views.size();
            int endIndex = (number + 1) * pageSize;
            if (endIndex > totalCount) {
                endIndex = totalCount;
            }
            List<SchoolClueView> retSchoolClues = views.subList(number * pageSize, endIndex);
            model.addAttribute("schoolClues", retSchoolClues);

            Map<String, Integer> pager = new HashMap<>();
            pager.put("number", number);
            pager.put("totalPages", totalPage);
            pager.put("totalElements", totalCount);
            model.addAttribute("pager", pager);
        }
        return "crm/school_clue/clue_list";
    }

    private SchoolClueView createView(School school, List<CrmSchoolClue> clues) {
        if (school == null || CollectionUtils.isEmpty(clues)) {
            return null;
        }
        SchoolClueView result = new SchoolClueView();
        result.setCmainName(school.getCmainName());
        result.setSchoolPhase(SchoolLevel.safeParse(school.getLevel()).getDescription());
        result.setShortName(school.getShortName());
        result.setSchoolDistrict(school.getSchoolDistrict());
        result.setSchoolCreateTime(school.getCreateTime());
        result.setId(school.getId());
        ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
        if (region != null) {
            result.setProvinceName(region.getProvinceName());
            result.setCityName(region.getCityName());
            result.setCountyName(region.getCountyName());
        }
        result.setAuthenticationState(school.getSchoolAuthenticationState().getDescription());
        clues.sort((o1, o2) -> (o1.getUpdateTime().compareTo(o2.getUpdateTime())));
        CrmSchoolClue clue = clues.get(0);
        result.setSchoolUpdateTime(clue.getUpdateTime());
        return result;
    }

    @RequestMapping(value = "review_detail.vpage")
    @ResponseBody
    public Map<String, Object> reviewDetail() {
        Long schoolId = getRequestLong("schoolId");
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return MapMessage.errorMessage("学校信息未找到");
        }
        SchoolCheckDetailView view = createSchoolCheckDetailView(school);
        List<CrmSchoolClue> clues = crmSchoolClueService.loadSchoolByIdIncludeDisabled(schoolId).stream()
                .filter(p -> !SafeConverter.toBoolean(p.getDisabled()))
                .filter(p -> AUTHENTICATE_LIST.contains(p.getAuthenticateType()))
                .collect(Collectors.toList());
        view.setClues(clues.stream().map(this::createSchoolClueDetailView).filter(Objects::nonNull).collect(Collectors.toList()));
        return MapMessage.successMessage().add("view", view);
    }

    private SchoolCheckDetailView createSchoolCheckDetailView(School school) {
        SchoolCheckDetailView view = new SchoolCheckDetailView();
        view.setFullName(school.loadSchoolFullName());
        view.setSchoolPhase(SchoolLevel.safeParse(school.getLevel()).getDescription());
        view.setShortName(school.getShortName());
        view.setSchoolCreateDate(school.getCreateTime());
        view.setSchoolId(school.getId());
        ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
        if (region != null) {
            view.setProvinceName(region.getProvinceName());
            view.setCityName(region.getCityName());
            view.setCountyName(region.getCountyName());
        }
        view.setAuthenticationState(school.getSchoolAuthenticationState().getDescription());
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(school.getId()).getUninterruptibly();
        if (schoolExtInfo != null) {
            view.setAddress(schoolExtInfo.getAddress());
            //view.setEnglishStartGrade();
            EduSystemType eduSystemType = schoolExtInfo.fetchEduSystem();
            view.setSchoolLength(eduSystemType == null ? "" :
                    eduSystemType.getDescription());
            view.setEnglishStartGrade(schoolExtInfo.getEnglishStartGrade() == null ? "" : ClazzLevel.getDescription(schoolExtInfo.getEnglishStartGrade()));
            view.setSchoolSize(schoolExtInfo.getSchoolSize());
        }
        return view;
    }

    private SchoolClueDetailView createSchoolClueDetailView(CrmSchoolClue clue) {
        if (clue.getAuthenticateType() == null) {
            return null;
        }
        SchoolClueDetailView view = new SchoolClueDetailView();
        view.setClueId(clue.getId());
        view.setCreateApplyTime(DateUtils.dateToString(clue.getCreateTime()));
        if (clue.getAuthenticateType() != 2) {
            view.setCheckStatus(CrmSchoolClueStatus.codeOf(clue.getStatus()) == null ? "" : CrmSchoolClueStatus.codeOf(clue.getStatus()).toString());
        } else {
            view.setCheckStatus(CrmSchoolClueStatus.codeOf(clue.getInfoStatus()) == null ? "" : CrmSchoolClueStatus.codeOf(clue.getInfoStatus()).toString());
        }
        view.setRecorderName(clue.getRecorderName());
        view.setRecorderPhone(clue.getRecorderPhone());
        view.setPhotoUrl(clue.getPhotoUrl());
        view.setLatitude(clue.getLatitude());
        view.setLongitude(clue.getLongitude());
        view.setDateTime(clue.getDateTime());
        view.setAddress(clue.getAddress());
        view.setReviewerName(clue.getReviewerName());
        view.setReviewTime(clue.getReviewTime() != null ? DateUtils.dateToString(clue.getReviewTime()) : "");
        view.setReviewNote(clue.getReviewNote());
        view.setUpdateTime(clue.getUpdateTime() != null ? clue.getUpdateTime().getTime() : 0L);
        return view;
    }


    @RequestMapping(value = "review_clue.vpage")
    @ResponseBody
    public MapMessage reviewClue() {
        String id = requestString("id");
        Long time = requestLong("updateTime");
        CrmSchoolClueStatus reviewStatus = CrmSchoolClueStatus.codeOf(requestInteger("reviewStatus"));
        String reviewNote = requestString("reviewNote");
        String longitude = requestString("longitude");
        String latitude = requestString("latitude");
        String address = requestString("address");
        return crmSchoolClueService.reviewSchoolClue(id, time, reviewStatus, reviewNote, getCurrentAdminUser(), longitude, latitude, address);
    }

    // @RequestMapping(value = "clue_list.vpage")
    public String clueListBak(Model model) {
        AuthCurrentAdminUser user = getCurrentAdminUser();

        // 判断用户是否有学校鉴定权限
        boolean schoolAuthOperate = false;
        String authOperators = crmConfigService.$loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), SCHOOL_AUTH_OPERATORS);
        if (authOperators.contains(user.getAdminUserName())) {
            schoolAuthOperate = true;
        }
        // 判断用户是否有信息完善权限
        boolean schoolInfoOperate = false;
        String infoOperators = crmConfigService.$loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), SCHOOL_INFO_OPERATORS);
        if (infoOperators.contains(user.getAdminUserName())) {
            schoolInfoOperate = true;
        }
        if (RuntimeMode.isDevelopment()) {
            schoolAuthOperate = schoolInfoOperate = true;
        }
        model.addAttribute("schoolAuthOperate", schoolAuthOperate);
        model.addAttribute("schoolInfoOperate", schoolInfoOperate);

        // 没有鉴定，也没有信息完善权限的人走开
        if (!schoolAuthOperate && !schoolInfoOperate) {
            return "crm/school_clue/clue_list";
        }

        // 查询条件
        String querySchoolName = getRequestString("schoolName"); // 学校名
        String queryCategory = getRequestString("category");     // 类别
        Integer queryStatus = getRequestInt("status", 1);        // 审核状态

        String provinceName = getRequestString("provinceName");  // 省名称
        String cityName = getRequestString("cityName");          // 市名城
        String recorderName = getRequestString("recorderName");  // 申请人
        String createStart = getRequestString("createStart");
        String createEnd = getRequestString("createEnd");
        String reviewerName = getRequestString("reviewerName");
        Date createStartTime = null;
        if (StringUtils.isNotBlank(createStart)) {
            createStartTime = DateUtils.stringToDate(createStart + " 00:00:00");
        }
        Date createEndTime = null;
        if (StringUtils.isNotBlank(createEnd)) {
            createEndTime = DateUtils.stringToDate(createEnd + " 23:59:59");
        }
        // 检查类别是否正常
        if (StringUtils.isNoneBlank(queryCategory) && !OP_AUTH.equals(queryCategory) && !OP_INFO.equals(queryCategory) && !OP_CRITICAL.equals(queryCategory) && !OP_SIGN_IN.equals(queryCategory)) {
            return "crm/school_clue/clue_list";
        } else if ((OP_AUTH.equals(queryCategory) && !schoolAuthOperate) || (OP_INFO.equals(queryCategory) && !schoolInfoOperate) || (OP_CRITICAL.equals(queryCategory) && !schoolAuthOperate) || (OP_SIGN_IN.equals(queryCategory) && !schoolInfoOperate)) {
            return "crm/school_clue/clue_list";
        }

        List<CrmSchoolClue> retSchoolClues = new ArrayList<>();
        int totalCount = 0;
        List<CrmSchoolClue> schoolClues = new ArrayList<>();
        if (schoolAuthOperate && (StringUtils.isBlank(queryCategory) || OP_AUTH.equals(queryCategory))) { // 学校鉴定队列
            queryCategory = OP_AUTH;
            schoolClues = crmSchoolClueService.loadAuthSchoolClues(queryStatus, querySchoolName, provinceName, cityName, recorderName, createStartTime, createEndTime, reviewerName);

        } else if (schoolAuthOperate && (StringUtils.isBlank(queryCategory) || OP_CRITICAL.equals(queryCategory))) {
            queryCategory = OP_CRITICAL;
            schoolClues = crmSchoolClueService.loadCriticalSchoolClues(queryStatus, querySchoolName, provinceName, cityName, recorderName, createStartTime, createEndTime, reviewerName);
        } else if (schoolInfoOperate && (OP_SIGN_IN.equals(queryCategory))) {
            queryCategory = OP_SIGN_IN;
            schoolClues = crmSchoolClueService.loadSignInSchoolClues(queryStatus, querySchoolName, provinceName, cityName, recorderName, createStartTime, createEndTime, reviewerName);
        } else { // 信息审核队列
            queryCategory = OP_INFO;
            schoolClues = crmSchoolClueService.loadInfoSchoolClues(queryStatus, querySchoolName, provinceName, cityName, recorderName, createStartTime, createEndTime, reviewerName);
        }

        if (CollectionUtils.isNotEmpty(schoolClues)) {
            totalCount = schoolClues.size();
            Collections.sort(schoolClues, (o1, o2) -> (o1.getUpdateTime().compareTo(o2.getUpdateTime())));
            retSchoolClues.addAll(schoolClues.stream().limit(50).collect(Collectors.toList()));
        }

        Set<Long> schoolIdSet = retSchoolClues.stream().map(CrmSchoolClue::getSchoolId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIdSet);

        retSchoolClues.forEach(p -> {
            p.setShowPhase(SchoolLevel.safeParse(p.getSchoolPhase()));
            p.setShowType(SchoolType.safeParse(p.getSchoolType()));
            //修改成学校的创建时间
            if (p.getSchoolId() != null && schoolMap.get(p.getSchoolId()) != null) {
                p.setCreateTime(schoolMap.get(p.getSchoolId()).getCreateTime());
            } else {
                p.setCreateTime(null);
            }
        });

        model.addAttribute("schoolClues", retSchoolClues);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("queryStatus", queryStatus);
        model.addAttribute("queryCategory", queryCategory);
        model.addAttribute("querySchoolName", querySchoolName);
        model.addAttribute("queryProvinceName", provinceName);
        model.addAttribute("queryCityName", cityName);
        model.addAttribute("queryRecorderName", recorderName);
        model.addAttribute("queryReviewerName", reviewerName);
        model.addAttribute("queryCreateStart", createStart);
        model.addAttribute("queryCreateEnd", createEnd);
        return "crm/school_clue/clue_list";
    }

    @RequestMapping(value = "save_base_clue.vpage")
    @ResponseBody
    public MapMessage saveBaseClue() {
        String id = requestString("id");
        Integer regionCode = requestInteger("regionCode");
        String cmainName = requestString("cmainName");
        String schoolDistrict = requestString("schoolDistrict");
        Integer schoolPhase = requestInteger("schoolPhase");
        String address = requestString("address");
        Long time = requestLong("updateTime");
        if (StringUtils.isBlank(id) || regionCode == null || StringUtils.isBlank(cmainName) || schoolPhase == null) {
            return MapMessage.errorMessage("请求参数有误");
        }
        CrmSchoolClue schoolClue = crmSchoolClueService.load(id);
        if (schoolClue == null) {
            return MapMessage.errorMessage("学校线索记录不存在");
        }
        if (!isTimeEqual(time, schoolClue.getUpdateTime())) {
            return MapMessage.errorMessage("学校线索已被修改，请刷新页面后再操作");
        }
        crmSchoolClueService.updateBaseClue(id, regionCode, cmainName, schoolDistrict, address, schoolPhase);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "base_clue.vpage")
    @ResponseBody
    public CrmSchoolClue baseClue() {
        String id = requestString("id");
        return crmSchoolClueService.load(id);
    }

    /* @RequestMapping(value = "review_detail.vpage")
     @ResponseBody*/
    public Map<String, Object> reviewDetailBak() {
        Map<String, Object> reviewDetail = new HashMap<>();
        String id = requestString("id");
        CrmSchoolClue schoolClue = crmSchoolClueService.load(id);
        if (schoolClue != null) {
            Long schoolId = schoolClue.getSchoolId();
            //schoolClue.setSchoolSize(CrmSchoolClueService.countSchoolSize(schoolClue));
            schoolClue.setBranchSchoolNames(loadBranchSchoolNames(schoolClue.getBranchSchoolIds()));
            School school = raikouSystem.loadSchool(schoolClue.getSchoolId());
            reviewDetail.put("school", school);
            if (school != null) {
                ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
                reviewDetail.put("schoolRegion", region);
                SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                        .loadSchoolExtInfo(school.getId())
                        .getUninterruptibly();
                if (schoolExtInfo != null) {
                    schoolExtInfo.setBranchSchoolNames(loadBranchSchoolNames(schoolExtInfo.getBranchSchoolIds()));
                }
                reviewDetail.put("schoolExtInfo", schoolExtInfo);
                List<CrmWorkRecord> records = crmWorkRecordService.loadCrmWorkRecordBySchoolId(school.getId());
                reviewDetail.put("signPoints", createSignPoint(records));
            }
            Map<String, String> photoMeta = getPhotoMeta(schoolClue);
            reviewDetail.put("photoMeta", photoMeta);
            List<CrmSchoolClue> schoolClues = crmSchoolClueService.loadSchoolByIdIncludeDisabled(schoolId).stream()
                    .filter(p -> !Objects.equals(p.getId(), id))
                    .filter(CrmSchoolClue::isApproved)
                    .filter(p -> StringUtils.isNotBlank(p.getPhotoUrl())).collect(Collectors.toList());
            if (schoolClues.size() > 4) {
                schoolClues = schoolClues.subList(0, 4);
            }
            reviewDetail.put("photoList", createPhotoList(schoolClues));
        }
        updateSchoolClueLocationInfoByCoordinateType(schoolClue);
        reviewDetail.put("schoolClue", schoolClue);
        return reviewDetail;
    }

    private void updateSchoolClueLocationInfoByCoordinateType(CrmSchoolClue schoolClue) {
        if (Objects.equals(schoolClue.getCoordinateType(), "autonavi")) {
            return;
        }
        MapMessage msg = AmapMapApi.coordinateConvert(schoolClue.getLongitude(), schoolClue.getLatitude(), schoolClue.getCoordinateType());
        if (!msg.isSuccess()) {
            return;
        }
        schoolClue.setLongitude(SafeConverter.toString(msg.get("longitude")));
        schoolClue.setLatitude(SafeConverter.toString(msg.get("latitude")));
        schoolClue.setCoordinateType("autonavi");
    }

    private List<Map<String, String>> createSignPoint(List<CrmWorkRecord> records) {
        List<Map<String, String>> result = new ArrayList<>();
        records.forEach(p -> {
            if (StringUtils.isNotBlank(p.getLongitude()) && StringUtils.isNotBlank(p.getLatitude())) {
                Map<String, String> signPoint = new HashMap<>();
                signPoint.put("longitude", p.getLongitude());        // 经度
                signPoint.put("latitude", p.getLatitude());          // 纬度
                signPoint.put("coordinateType", p.getCoordinateType());             // GPS 类型
                signPoint.put("createTime", DateUtils.dateToString(p.getCreateTime())); // 时间
                result.add(signPoint);
            }
        });
        coordinatesConvertByCoordinate(result, "wgs84ll");
        coordinatesConvertByCoordinate(result, "bd09ll");
        return result;
    }

    private List<Map<String, String>> createPhotoList(List<CrmSchoolClue> schoolClues) {
        List<Map<String, String>> result = new ArrayList<>();
        schoolClues.forEach(p -> {
            Map<String, String> photoInfo = new HashMap<>();
            photoInfo.put("photoUrl", p.getPhotoUrl());
            photoInfo.put("updateTime", DateUtils.dateToString(p.getUpdateTime()));
            photoInfo.put("longitude", p.getLongitude());        // 经度
            photoInfo.put("latitude", p.getLatitude());           // 纬度
            photoInfo.put("coordinateType", p.getCoordinateType()); // GPS 类型
            result.add(photoInfo);
        });
        coordinatesConvertByCoordinate(result, "wgs84ll");
        coordinatesConvertByCoordinate(result, "bd09ll");
        return result;
    }

    private void coordinatesConvertByCoordinate(List<Map<String, String>> coordinates, String coordinateType) {
        List<Map<String, String>> points = coordinates.stream().filter(p -> Objects.equals(p.get("coordinateType"), coordinateType)).collect(Collectors.toList());
        MapMessage msg = AmapMapApi.coordinatesConvert(AmapMapApi.createCoordinateCollection(points, "longitude", "latitude"), coordinateType);
        if (!msg.isSuccess()) {
            return;
        }
        List<List<String>> locations = (List<List<String>>) msg.get("locations");
        if (CollectionUtils.isEmpty(locations)) {
            return;
        }
        if (points.size() != locations.size()) {
            return;
        }
        for (int i = 0; i < locations.size(); i++) {
            List<String> locationInfo = locations.get(i);
            Map<String, String> pointInfo = points.get(i);
            pointInfo.put("longitude", locationInfo.get(0));
            pointInfo.put("latitude", locationInfo.get(1));
        }
    }

    private Set<String> loadBranchSchoolNames(Set<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptySet();
        }
        Map<Long, School> longSchoolMap = raikouSystem.loadSchools(schoolIds);
        if (MapUtils.isEmpty(longSchoolMap)) {
            return Collections.emptySet();
        }
        Collection<School> schools = longSchoolMap.values();
        if (CollectionUtils.isEmpty(schools)) {
            return Collections.emptySet();
        }
        return schools.stream().map(School::getCname).collect(Collectors.toSet());
    }

    private Map<String, String> getPhotoMeta(CrmSchoolClue schoolClue) {
        Map<String, String> photoMeta = new HashMap<>();
        photoMeta.put("Make", "");
        photoMeta.put("Model", "");
        photoMeta.put("Date/Time", "");
        if (schoolClue == null) {
            return photoMeta;
        }
        if (StringUtils.isNotBlank(schoolClue.getMake())) {
            photoMeta.put("Make", schoolClue.getMake());
            photoMeta.put("Model", schoolClue.getModel());
            photoMeta.put("Date/Time", schoolClue.getDateTime());
            return photoMeta;
        }
        photoMeta = crmSchoolClueService.photoMeta(schoolClue.getPhotoUrl());
        return photoMeta;
    }


    private boolean isTimeEqual(long timestamp, Date targetTime) {
        return targetTime != null && timestamp == targetTime.getTime();
    }
}
