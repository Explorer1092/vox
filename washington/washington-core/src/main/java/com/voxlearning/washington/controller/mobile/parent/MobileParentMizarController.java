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

package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.MizarOrderType;
import com.voxlearning.utopia.core.helper.BaiduMapApi;
import com.voxlearning.utopia.core.helper.GEOUtils;
import com.voxlearning.utopia.entity.o2o.TrusteeOrderRecord;
import com.voxlearning.utopia.entity.o2o.TrusteeReserveRecord;
import com.voxlearning.utopia.service.config.client.BusinessActivityManagerClient;
import com.voxlearning.utopia.service.mizar.api.constants.MizarCourseCategory;
import com.voxlearning.utopia.service.mizar.api.constants.MizarGoodsStatus;
import com.voxlearning.utopia.service.mizar.api.constants.MizarRatingActivity;
import com.voxlearning.utopia.service.mizar.api.constants.MizarRatingStatus;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCourse;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.MicroCoursePeriod;
import com.voxlearning.utopia.service.mizar.api.entity.shop.*;
import com.voxlearning.utopia.service.mizar.api.mapper.LoadMizarShopContext;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarCourseMapper;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarShopMapper;
import com.voxlearning.utopia.service.mizar.api.mapper.TradeAreaMapper;
import com.voxlearning.utopia.service.mizar.api.utils.MicroCourseMsgTemplate;
import com.voxlearning.utopia.service.mizar.client.AsyncMizarCacheServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.MicroCourseLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarServiceClient;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.TrusteeOrderServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.AsyncStudentServiceClient;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.service.LoadFlashGameContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2016/8/16.
 * 机构导流
 */
@Controller
@RequestMapping(value = "/mizar")
@Slf4j
public class MobileParentMizarController extends AbstractMobileController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncMizarCacheServiceClient asyncMizarCacheServiceClient;
    @Inject private AsyncStudentServiceClient asyncStudentServiceClient;
    @Inject private MizarLoaderClient mizarLoaderClient;
    @Inject private MizarServiceClient mizarServiceClient;
    @Inject private MicroCourseLoaderClient microCourseLoaderClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private BusinessActivityManagerClient businessActivityManagerClient;
    @Inject private TrusteeOrderServiceClient trusteeOrderServiceClient;

    // 线上1分钱测试家长ID账号
    private static final List<Long> freeParentIds = Arrays.asList(27398018L, 27398020L, 27398022L, 27398025L, 27398027L, 27398030L,
            27398035L, 27398036L, 27398040L, 27398042L, 27398041L, 27398038L, 27398029L, 27398032L, 27398047L, 27398049L,
            27398054L, 27398055L);

    /**
     * 首页
     */
    @RequestMapping(value = "/index.vpage", method = {RequestMethod.GET})
    public String index(Model model) {
        // 这里未来可能会加入一些首页推荐的机构列表
        return "mizar/index";
    }

    /**
     * 机构地图位置
     */
    @RequestMapping(value = "/shopmap.vpage", method = {RequestMethod.GET})
    public String map(Model model) {
        String shopId = getRequestString("shopId");
        String type = getRequestString("type");

        if (StringUtils.isBlank(shopId)) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的机构不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
        MizarShop shop = mizarLoaderClient.loadShopById(shopId);
        if (shop == null) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的机构不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
        model.addAttribute("shop", shop);
        return "mizar/map";
    }


    @RequestMapping(value = "uploadphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadRatingPhoto() {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
        try {
            MultipartFile inputFile = multipartRequest.getFile("file");
            if (inputFile != null && !inputFile.isEmpty()) {
                String suffix = StringUtils.substringAfterLast(inputFile.getOriginalFilename(), ".");
                if (StringUtils.isBlank(suffix)) {
                    suffix = "jpg";
                }
                StorageMetadata storageMetadata = new StorageMetadata();
                storageMetadata.setContentLength(inputFile.getSize());
                String env = "mizar/";
                if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
                    env = "mizar/test/";
                }
                String path = env + DateUtils.dateToString(new Date(), "yyyy/MM/dd");
                String fileName = DateUtils.dateToString(new Date(), "yyyyMMddHHmmssSSS") + RandomStringUtils.randomNumeric(3) + "." + suffix;
                String realName = storageClient.upload(inputFile.getInputStream(), fileName, path, storageMetadata);
                String photo = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_homework_image_host")) + realName;
                return MapMessage.successMessage().add("fileName", photo);
            } else {
                return MapMessage.errorMessage("请选择图片");
            }
        } catch (Exception ex) {
            logger.error("上传失败,msg:{}", ex.getMessage(), ex);
            return MapMessage.errorMessage("上传失败");
        }
    }

    /**
     * 机构详情页
     */
    @RequestMapping(value = "/shopdetail.vpage", method = {RequestMethod.GET})
    public String shopDetail(Model model) {
        String shopId = getRequestString("shopId");
        if (StringUtils.isBlank(shopId)) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的机构不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
        MizarShop shop = mizarLoaderClient.loadShopById(shopId);
        if (shop == null) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的机构不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
        // 有机构 去组装机构详情页需要展示的内容。
        Map<String, Object> shopMap = mizarLoaderClient.loadShopShowMap(shopId);
        // 获取有多少同学来过
        Long studentId = SafeConverter.toLong(getCookieManager().getCookie("sid", "0"));
        Long parentId = currentUserId();
        if (studentId == 0) {
            List<User> childList = studentLoaderClient.loadParentStudents(parentId);
            if (CollectionUtils.isNotEmpty(childList)) {
                studentId = MiscUtils.firstElement(childList).getId();
            }
        }
        School school = asyncStudentServiceClient.getAsyncStudentService()
                .loadStudentSchool(studentId)
                .getUninterruptibly();
        if (school != null) {
            Map<String, Object> reserveMap = mizarLoaderClient.loadShopReserveByShopIdAndSchoolId(school.getId(), shopId);
            if (MapUtils.isNotEmpty(reserveMap)) {
                // 过滤自己
                int sameCount = 0;
                List<Map<String, Object>> sameList = (List) reserveMap.get("sameSchoolReserveList");
                if (CollectionUtils.isNotEmpty(sameList)) {
                    final Long finalStudentId = studentId;
                    sameList = sameList.stream().filter(m -> !Objects.equals(SafeConverter.toLong(m.get("studentId")), finalStudentId)).collect(Collectors.toList());
                    sameCount = sameList == null ? 0 : sameList.size();
                }
                shopMap.put("sameReserveCount", sameCount);
            }
        }
        // 是否已经点赞
        if (parentId != null && parentId != 0) {
            boolean likedShop = mizarLoaderClient.likedShop(shopId, MizarRatingActivity.ADD_MIZAR_SHOP_LIKE.getId(), parentId);
            model.addAttribute("hasLiked", likedShop);
        }
        model.addAttribute("activityId", MizarRatingActivity.ADD_MIZAR_SHOP_LIKE.getId());
        model.addAttribute("shop", shopMap);
        model.addAttribute("showRemarkAndLike", true);

        String pageType = getRequestString("page_type");

        if (pageType.equals("online") || (shop.getType() != null && shop.getType() == 1)) {
            return "mizar/shopdetailonline";
        }

        return "mizar/shopdetail";
    }


    // 机构详情页 其他小伙伴还看了机构推荐
    @RequestMapping(value = "shoprecommend.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage shopRecommend() {
        String shopId = getRequestString("shopId");
        if (StringUtils.isBlank(shopId)) {
            return MapMessage.errorMessage("不存在推荐商家");
        }
        List<MizarShopMapper> mappers = mizarLoaderClient.loadRecommendShop(shopId);
        return MapMessage.successMessage().add("shopList", mappers);
    }

    /**
     * 来过的同学 详情页面
     */
    @RequestMapping(value = "/samereserve.vpage", method = {RequestMethod.GET})
    public String sameReserve(Model model) {
        String shopId = getRequestString("shopId");
        if (StringUtils.isBlank(shopId)) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的机构不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
        MizarShop shop = mizarLoaderClient.loadShopById(shopId);
        if (shop == null) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的机构不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
        Long studentId = SafeConverter.toLong(getCookieManager().getCookie("sid", "0"));
        Long parentId = currentUserId();
        if (studentId == 0) {
            List<User> childList = studentLoaderClient.loadParentStudents(parentId);
            if (CollectionUtils.isNotEmpty(childList)) {
                studentId = MiscUtils.firstElement(childList).getId();
            }
        }
        School school = asyncStudentServiceClient.getAsyncStudentService()
                .loadStudentSchool(studentId)
                .getUninterruptibly();
        if (school != null) {
            Map<String, Object> reserveMap = mizarLoaderClient.loadShopReserveByShopIdAndSchoolId(school.getId(), shopId);
            if (MapUtils.isNotEmpty(reserveMap)) {
                List<Map<String, Object>> sameList = (List) reserveMap.get("sameSchoolReserveList");
                if (CollectionUtils.isNotEmpty(sameList)) {
                    final Long finalStudentId = studentId;
                    sameList = sameList.stream().filter(m -> !Objects.equals(SafeConverter.toLong(m.get("studentId")), finalStudentId)).collect(Collectors.toList());
                }
                model.addAttribute("sameSchoolReserveList", sameList);
                model.addAttribute("otherSchoolReserveList", reserveMap.get("otherSchoolReserveList"));
            }
        }
        return "mizar/samereserve";
    }

    /**
     * 课程详情页
     */
    @RequestMapping(value = "/goodsdetail.vpage", method = {RequestMethod.GET})
    public String goodsDetail(Model model) {
        String goodsId = getRequestString("goodsId");
        if (StringUtils.isBlank(goodsId)) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的课程不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
        MizarShopGoods goods = mizarLoaderClient.loadShopGoodsById(goodsId);
        if (goods == null) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的课程不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/goodsdetail";
        }
        if (!MizarGoodsStatus.ONLINE.equals(goods.getStatus())) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的课程已经下线~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }

        // 获取电话
        MizarShop shop = mizarLoaderClient.loadShopById(goods.getShopId());
        if (shop == null) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的机构不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
        model.addAttribute("phone", shop.getContactPhone());
        model.addAttribute("goods", goods);
        return "mizar/goodsdetail";
    }

    /**
     * 品牌详情页
     */
    @RequestMapping(value = "/branddetail.vpage", method = {RequestMethod.GET})
    public String brandDetail(Model model) {
        String brandId = getRequestString("brandId");
        if (StringUtils.isBlank(brandId)) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的品牌不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
        MizarBrand brand = mizarLoaderClient.loadBrandById(brandId);
        if (brand == null) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的品牌不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
        model.addAttribute("brand", brand);
        // 获取最近商户
        Map<String, String> position = getUserPosition();
        Map<String, Object> shopMap = mizarLoaderClient.loadBrandNearShop(brandId, position.get("longitude"), position.get("latitude"));
        model.addAttribute("shopMap", shopMap);
        return "mizar/branddetail";
    }

    // 获取品牌最近的机构
    @RequestMapping(value = "/loadbrandnearshop.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadBrandNearShop() {
        String brandId = getRequestString("brandId");
        // 获取最近商户
        Map<String, String> position = getUserPosition();
        Map<String, Object> shopMap = mizarLoaderClient.loadBrandNearShop(brandId, position.get("longitude"), position.get("latitude"));
        return MapMessage.successMessage().add("shopMap", shopMap);
    }

    /**
     * 机构图片列表
     */
    @RequestMapping(value = "/shoppics.vpage", method = {RequestMethod.GET})
    public String shopPics(Model model) {
        String shopId = getRequestString("shopId");
        if (StringUtils.isBlank(shopId)) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的机构不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
        MizarShop shop = mizarLoaderClient.loadShopById(shopId);
        if (shop == null) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的机构不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
        // 获取图片列表
        model.addAttribute("picList", shop.getPhoto());
        return "mizar/shoppics";
    }

    /**
     * 点评列表
     */
    @RequestMapping(value = "/ratinglist.vpage", method = {RequestMethod.GET})
    public String ratingList(Model model) {
        String shopId = getRequestString("shopId");
        if (StringUtils.isBlank(shopId)) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的机构不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
        MizarShop shop = mizarLoaderClient.loadShopById(shopId);
        if (shop == null) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的机构不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
        model.addAttribute("shopId", shopId);
        return "mizar/ratinglist";
    }

    /**
     * 品牌馆或者品牌页预约入口
     */
    @RequestMapping(value = "/reserveselect.vpage", method = {RequestMethod.GET})
    public String reserveSelect(Model model) {
        String shopId = getRequestString("shopId");
        if (StringUtils.isBlank(shopId)) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的机构不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }
        MizarShop shop = mizarLoaderClient.loadShopById(shopId);
        if (shop == null) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的机构不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/errorpage";
        }

        model.addAttribute("shopId", shopId);
        return "mizar/reserveselect";
    }

    // 品牌馆或者品牌页预约数据获取
    @RequestMapping(value = "/loadreserveselect.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadReserveSelect() {
        String shopId = getRequestString("shopId");
        if (StringUtils.isBlank(shopId)) {
            return MapMessage.errorMessage().add("info", "你访问的机构不存在").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        MizarShop shop = mizarLoaderClient.loadShopById(shopId);

        if (shop == null) {
            return MapMessage.errorMessage().add("info", "你访问的机构不存在").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }

        return MapMessage.successMessage().add("shopName", shop.getFullName())
                .add("shopId", shop.getId())
                .add("shopAddress", shop.getAddress())
                .add("brandId", shop.getBrandId());
    }

    // 品牌馆预约--选择机构
    @RequestMapping(value = "/loadshopselect.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadShopSelect() {
        String brandId = getRequestString("brandId");

        // 获取最近商户
        Map<String, String> position = getUserPosition();
        List<MizarShopMapper> mappers = mizarLoaderClient.loadBrandShopsByPosition(brandId, position.get("longitude"), position.get("latitude"));

        return MapMessage.successMessage().add("mappers", mappers);
    }

    // 获取当前孩子的所有家长列表  用来预约
    @RequestMapping(value = "loadparents.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadParents() {
        Long studentId = getRequestLong("sid");
        Long parentId = currentUserId();
        // 存在不登陆的情况
        if (parentId == null || parentId == 0L) {
            return MapMessage.successMessage().add("login", false);
        }
        if (studentId == 0) {
            // 获取家长的第一个孩子
            List<User> userList = studentLoaderClient.loadParentStudents(parentId);
            if (CollectionUtils.isEmpty(userList)) {
                return MapMessage.successMessage().add("login", false);
            }
            studentId = userList.get(0).getId();
        }
        // 查找家长的手机
        User user = raikouSystem.loadUser(studentId);
        if (user == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentId);
        Map<String, Object> mobileMap = new HashMap<>();
        for (StudentParent studentParent : studentParents) {
            if (Objects.equals(studentParent.getParentUser().getId(), parentId)) {
                String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(studentParent.getParentUser().getId());
                if (StringUtils.isNotBlank(mobile)) {
                    mobileMap.put("parentId", studentParent.getParentUser().getId());
                    mobileMap.put("showName", user.fetchRealname() + studentParent.getCallName());
                    mobileMap.put("callName", studentParent.getCallName());
                    mobileMap.put("studentName", user.fetchRealname());
                    mobileMap.put("mobile", mobile);
                    break;
                }
            }

        }
        return MapMessage.successMessage().add("mobileMap", mobileMap);
    }

    // 预约机构 目前只针对机构预约
    @RequestMapping(value = "reserve.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage reserve() {
        Long studentId = getRequestLong("sid");
        Long parentId = currentUserId();
        String goodsId = getRequestString("goodsId");
        String mobile = getRequestString("mobile");
        String shopId = getRequestString("shopId");
        String callName = getRequestString("callName");
        String studentName = getRequestString("studentName");
        if (StringUtils.isBlank(shopId)) {
            return MapMessage.errorMessage("参数错误");
        }

        // 获取是否7天之内预约过
        List<MizarReserveRecord> recordList = mizarLoaderClient.loadShopReserveByMobile(mobile, shopId);
        if (CollectionUtils.isNotEmpty(recordList)) {
            List<MizarReserveRecord> records = recordList.stream().filter(r -> r.getCreateDatetime().after(DateUtils.calculateDateDay(new Date(), -7)))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(records)) {
                return MapMessage.successMessage().add("reserveFlag", true);
            }
        }
        MizarReserveRecord reserveRecord = new MizarReserveRecord();
        reserveRecord.setCallName(callName);
        reserveRecord.setStudentId(studentId);
        reserveRecord.setParentId(parentId);
        reserveRecord.setShopGoodsId(goodsId);
        reserveRecord.setShopId(shopId);
        reserveRecord.setMobile(mobile);
        reserveRecord.setStudentName(studentName);
        reserveRecord.setStatus(MizarReserveRecord.Status.New);
        // 获取schoolId
        if (studentId == 0 && parentId != null && parentId != 0) {
            List<User> childList = studentLoaderClient.loadParentStudents(parentId);
            if (CollectionUtils.isNotEmpty(childList)) {
                studentId = MiscUtils.firstElement(childList).getId();
            }
        }
        School school = asyncStudentServiceClient.getAsyncStudentService()
                .loadStudentSchool(studentId)
                .getUninterruptibly();
        if (school != null) {
            reserveRecord.setSchoolId(school.getId());
        }
        return atomicLockManager.wrapAtomic(mizarServiceClient)
                .keyPrefix("MIZAR_RESERVE")
                .keys(parentId, shopId)
                .proxy()
                .saveMizarReserve(reserveRecord);
    }

    // 机构展示一期 升学专题
    @RequestMapping(value = "/up.vpage", method = {RequestMethod.GET})
    public String up(Model model) {
        // 增加灰度判断  4个灰度
        String type = getRequestString("t");
        List<MizarShopMapper> englishList = new ArrayList<>();
        List<String> englishIds = new ArrayList<>();
        if (StringUtils.equals(type, "i1")) {
            englishIds = Arrays.asList("57bc4a382f70b11f241e9f4b", "57bc4a382f70b11f241e9f4a", "57bc4a382f70b11f241e9f4e");
        } else if (StringUtils.equals(type, "i2")) {
            englishIds = Arrays.asList("57bc4a382f70b11f241e9f4e", "57bc4a382f70b11f241e9f4b", "57bc4a372f70b11f241e9e94");
        } else if (StringUtils.equals(type, "d1")) {
            englishIds = Arrays.asList("57bc4a3a2f70b11f241ea1a6", "57bc4a372f70b11f241e9e94", "57bc4a382f70b11f241e9f4e");
        } else if (StringUtils.equals(type, "d2")) {
            englishIds = Arrays.asList("57bc4a3a2f70b11f241ea1a6", "57bc4a382f70b11f241e9f4a", "57bc4a372f70b11f241e9e94");
        } else {
            englishIds = Arrays.asList("57bc4a382f70b11f241e9f4a", "57bc4a382f70b11f241e9f4b", "57bc4a382f70b11f241e9f49",
                    "57bc4a382f70b11f241e9f4e", "57bc4a372f70b11f241e9e94", "57bc4a3a2f70b11f241ea1a6");
        }
        for (String id : englishIds) {
            MizarShop shop = mizarLoaderClient.loadShopById(id);
            if (shop != null) {
                englishList.add(getPageListMap(shop));
            }
        }
        model.addAttribute("englishList", englishList);

        List<MizarShopMapper> cyList = new ArrayList<>();
        List<String> cyIds = Arrays.asList("57c92f5d72118bdb9f80f2a5", "57c8edaa72118bfd581b20b2", "57c92e046cdb8a3b86f05b89", "57bc4a3b2f70b11f241ea3cc",
                "57bc4a3a2f70b11f241ea1d9", "57bc4a382f70b11f241ea03c", "57bc4a382f70b11f241ea03e",
                "57bc4a382f70b11f241ea03d", "57bc4a3c2f70b11f241ea51d", "57bc4a3c2f70b11f241ea51b",
                "57bc4a3c2f70b11f241ea51e");

        for (String id : cyIds) {
            MizarShop shop = mizarLoaderClient.loadShopById(id);
            if (shop != null) {
                cyList.add(getPageListMap(shop));
            }
        }
        model.addAttribute("cyList", cyList);

        List<MizarShopMapper> wlList = new ArrayList<>();
        List<String> wlIds = Arrays.asList("57bc4a382f70b11f241e9f7f", "57bc4a392f70b11f241ea13c");
        for (String id : wlIds) {
            MizarShop shop = mizarLoaderClient.loadShopById(id);
            if (shop != null) {
                wlList.add(getPageListMap(shop));
            }
        }
        model.addAttribute("wlList", wlList);
        return "mizar/up";
    }

    /**
     * 列表页
     */
    @RequestMapping(value = "/list.vpage", method = {RequestMethod.GET})
    public String list(Model model) {
        // 取区域ID 根据 用户所在城市 如果没有 则默认定位取北京的地区,1-是,0
        Long parentId = currentUserId();
        Integer cityCode = 110100;
        if (parentId != null && parentId > 0L) {
            List<User> child = studentLoaderClient.loadParentStudents(parentId);
            if (CollectionUtils.isNotEmpty(child)) {
                StudentDetail detail = studentLoaderClient.loadStudentDetail(MiscUtils.firstElement(child).getId());
                if (detail != null && detail.getCityCode() != null) {
                    cityCode = detail.getCityCode();
                }
            }
        }
        // 获取商圈数据
        List<TradeAreaMapper> areaList = mizarLoaderClient.loadAllTradeArea(cityCode);
        model.addAttribute("areaList", areaList);
        // 获取分类数据
        List<Map<String, Object>> categoryList = mizarLoaderClient.loadAllCategory();
        model.addAttribute("categoryList", categoryList);
        return "mizar/list";
    }

    // 机构展示一期 品牌馆
    @RequestMapping(value = "/ppg.vpage", method = {RequestMethod.GET})
    public String ppg(Model model) {
        // 渲染页面
        return "mizar/ppg";
    }

    // 获取品牌馆数据
    @RequestMapping(value = "/loadppgdata.vpage")
    @ResponseBody
    public MapMessage loadPpgData() {
        // 获取用户经纬度
        Map<String, String> position = getUserPosition();
        // 获取品牌馆列表数据
        Map<String, List<Map<String, Object>>> dataMap = mizarLoaderClient.loadPpgIndexList(position.get("longitude"), position.get("latitude"));
        return MapMessage.successMessage().add("dataMap", dataMap);
    }

    private Collection<Integer> getChildrenGradeListByParentId(List<User> childList) {
        if (CollectionUtils.isEmpty(childList)) {
            return Collections.emptyList();
        }
        //家长所有孩子(学生)的id
        Set<Long> studentIdSet = childList.stream().map(User::getId).collect(Collectors.toSet());
        //根据学生Id列表,查询所有对应的年级信息
        Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIdSet);
        if (MapUtils.isNotEmpty(studentDetailMap)) {
            Collection<Integer> classLevelList = new HashSet<>();//学生所在的年级id列表
            for (Map.Entry<Long, StudentDetail> entry : studentDetailMap.entrySet()) {
                StudentDetail studentDetail = entry.getValue();
                ClazzLevel clazzLevel = studentDetail.getClazzLevel();
                if (null != clazzLevel) {
                    classLevelList.add(clazzLevel.getLevel());
                }
            }
            return classLevelList;
        }
        return Collections.emptyList();
    }

    // 列表页按照条件获取机构方法
    @RequestMapping(value = "loadshops.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadShops() {
        Long parentId = currentUserId();
        if (parentId == null) {
            return MapMessage.successMessage();
        }
        String firstCategory = getRequestString("firstCategory");
        String secondCategory = getRequestString("secondCategory");
        Integer regionCode = getRequestInt("regionCode");           // 区域标识
        String tradeArea = getRequestString("tradeArea");          //商圈
        String orderBy = getRequestParameter("orderBy", "smart"); // 距离distance 或者 评分 rating 或者 smart
        String shopName = getRequestString("shopName");  // 根据名字检索
        Integer pageSize = getRequestInt("pageSize", 10);
        int pageNum = getRequestInt("pageNum", 0);
        String longitude = getRequestParameter("longitude", "0");       //用户GPS经度
        String latitude = getRequestParameter("latitude", "0");         //用户GPS纬度
        // search by request region code > user GPS pos > school GPS pos > school region code
        // 获取家长的第一个孩子的学校
        LoadMizarShopContext context = new LoadMizarShopContext();
        School school = null;
        List<User> childList = studentLoaderClient.loadParentStudents(parentId);
        User child = MiscUtils.firstElement(childList);
        if (child != null) {
            school = asyncStudentServiceClient.getAsyncStudentService()
                    .loadStudentSchool(child.getId())
                    .getUninterruptibly();
            context.studentId = child.getId();
        }
        if (school != null) {
            context.schoolId = school.getId();
        }
        //如果没有传经纬度,则取学校的经纬度
        if (StringUtils.equals(longitude, "0") || StringUtils.equals(longitude, "0")) { // no user gps pos found
            if (null != school) {
                SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService()
                        .loadSchoolExtInfo(school.getId())
                        .getUninterruptibly();
                if (null != extInfo && StringUtils.isNotBlank(extInfo.getLatitude()) && StringUtils.isNotBlank(extInfo.getLongitude())) {
                    longitude = extInfo.getLongitude();
                    latitude = extInfo.getLatitude();
                }
            }
        }
        Integer schoolRegionCode = 0;
        if (regionCode == 0) { //客户端没有传区域 则取学校的区域
            if (null != school) {
                schoolRegionCode = school.getRegionCode();
            }
        }
        // 没有GPS也没有区域
        if ((StringUtils.equals(longitude, "0") || StringUtils.equals(longitude, "0")) && regionCode == 0 && schoolRegionCode == 0) {
            return MapMessage.successMessage();
        }
        //查询该家长所有孩子的年级信息
        Collection<Integer> classLevelList = getChildrenGradeListByParentId(childList);

        context.firstCategory = firstCategory;
        context.secondCategory = secondCategory;
        context.regionCode = regionCode;
        context.schoolRegionCode = schoolRegionCode;
        context.tradeArea = tradeArea;
        context.longitude = longitude;
        context.latitude = latitude;
        context.shopName = shopName;
        context.clazzLevels = classLevelList;
        context.pageNum = pageNum;
        context.pageSize = pageSize;
        context.orderBy = orderBy;
        PageImpl<MizarShopMapper> shopPages = mizarLoaderClient.loadShopPageByParam(context);
        return MapMessage.successMessage()
                .add("pageNum", shopPages.getNumber())
                .add("pageSize", shopPages.getSize())
                .add("rows", shopPages.getContent())
                .add("totalPage", shopPages.getTotalPages())
                .add("totalSize", shopPages.getTotalElements());
    }

    // 根据经纬度获取我的地址
    @RequestMapping(value = "loadaddress.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadAddress() {
        String longitude = getRequestString("longitude");
        String latitude = getRequestString("latitude");
        if (StringUtils.isBlank(longitude) || StringUtils.isBlank(latitude)) {
            return MapMessage.successMessage().add("address", "获取位置失败");
        }
        MapMessage message = BaiduMapApi.getAddress(latitude, longitude, "wgs84");
        if (message.isSuccess()) {
            return message;
        } else {
            return MapMessage.successMessage().add("address", "获取位置失败");
        }
    }

    // 机构收集点评活动主页
    @RequestMapping(value = "/remark/index.vpage", method = {RequestMethod.GET})
    public String collectRating(Model model) {
        model.addAttribute("time", new Date().getTime());
        return "mizar/remark/index";
    }

    @RequestMapping(value = "/remark/search.vpage", method = RequestMethod.GET)
    public String remarkSearch() {
        return "mizar/remark/search";
    }

    // 机构收集点评活动 机构详情页
    @RequestMapping(value = "/remark/detail.vpage", method = {RequestMethod.GET})
    public String collectRatingShopDetail(Model model) {
        String shopId = getRequestString("shopId");
        Long userId = currentUserId();
        if (userId == null || userId == 0) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号~").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
            return "mizar/remark/detail";
        }
        if (StringUtils.isBlank(shopId)) {
            return "mizar/remark/detail";
        }
        MizarShop shop = mizarLoaderClient.loadShopById(shopId);
        if (shop == null) {
            return "mizar/remark/detail";
        }
        Map<String, Object> shopMap = new HashMap<>();
        shopMap.put("shopName", shop.getFullName());
        if (CollectionUtils.isNotEmpty(shop.getPhoto())) {
            shopMap.put("shopImg", shop.getPhoto().get(0));
        }
        // 获取活动投票排名
        List<Map<String, Object>> shopRankList = mizarLoaderClient.loadShopRankListByActivityId(MizarRatingActivity.Collect_1.getId());
        Map<String, Object> shopRankMap;
        if (CollectionUtils.isNotEmpty(shopRankList)) {
            shopRankMap = shopRankList.stream().filter(m -> StringUtils.equals(m.get("shopId").toString(), shopId)).findFirst().orElse(null);
            if (MapUtils.isNotEmpty(shopRankMap)) {
                shopMap.put("likeCount", shopRankMap.get("likeCount"));
                shopMap.put("rank", shopRankMap.get("rank"));
            }
        }
        // 本人是否已经投票
        boolean isLike = mizarLoaderClient.likedShop(shopId, MizarRatingActivity.Collect_1.getId(), userId);
        model.addAttribute("liked", isLike);
        model.addAttribute("shopMap", shopMap);
        model.addAttribute("activityId", MizarRatingActivity.Collect_1.getId());
        return "mizar/remark/detail";
    }

    // 教育机构排行页面
    @RequestMapping(value = "/remark/mechanismrank.vpage", method = {RequestMethod.GET})
    public String shopRank(Model model) {
        List<Map<String, Object>> shopRankList = mizarLoaderClient.loadShopRankListByActivityId(MizarRatingActivity.Collect_1.getId());
        model.addAttribute("shopRankList", shopRankList);
        return "mizar/remark/mechanismrank";
    }

    // 查看我附近的教育机构
    @RequestMapping(value = "/remark/nearrank.vpage", method = {RequestMethod.GET})
    public String nearRank(Model model) {
        Long parentId = currentUserId();
        Map<String, List<Map<String, Object>>> nearMap = new HashMap<>();
        if (parentId != null && parentId != 0) {
            List<User> childList = studentLoaderClient.loadParentStudents(parentId);
            if (CollectionUtils.isNotEmpty(childList)) {
                User student = childList.stream().findFirst().orElse(null);
                if (student != null) {
                    School school = asyncStudentServiceClient.getAsyncStudentService()
                            .loadStudentSchool(student.getId())
                            .getUninterruptibly();
                    if (school != null) {
                        SchoolExtInfo info = schoolExtServiceClient.getSchoolExtService()
                                .loadSchoolExtInfo(school.getId())
                                .getUninterruptibly();
                        if (info != null && StringUtils.isNotBlank(info.getLatitude()) && StringUtils.isNotBlank(info.getLongitude())) {
                            nearMap = mizarLoaderClient.loadNearRankListBySchoolId(school.getId(), MizarRatingActivity.Collect_1.getId());
                        }
                    }
                }
            }
        }
        if (MapUtils.isEmpty(nearMap)) {
            // 获取排行榜分类排名
            List<Map<String, Object>> rankList = mizarLoaderClient.loadShopRankListByActivityId(MizarRatingActivity.Collect_1.getId());
            Map<String, List<Map<String, Object>>> rankMap = rankList.stream().collect(Collectors.groupingBy(new Function<Map<String, Object>, String>() {
                @Override
                public String apply(Map<String, Object> stringObjectMap) {
                    return SafeConverter.toString(stringObjectMap.get("firstCategory"));
                }
            }));
            for (Map.Entry<String, List<Map<String, Object>>> entry : rankMap.entrySet()) {
                List<Map<String, Object>> mappers = entry.getValue();
                if (CollectionUtils.isNotEmpty(mappers) && mappers.size() > 3) {
                    mappers = mappers.subList(0, 3);
                }
                List<String> shopIds = mappers.stream().map(m -> SafeConverter.toString(m.get("shopId"))).collect(Collectors.toList());
                Map<String, MizarShop> shopMap = mizarLoaderClient.loadShopByIds(shopIds);
                for (Map<String, Object> map : mappers) {
                    MizarShop shop = shopMap.get(SafeConverter.toString(map.get("shopId")));
                    if (shop != null) {
                        map.put("shopImg", shop.getPhoto() != null ? shop.getPhoto().get(0) : "");
                        map.put("ratingStar", shop.getRatingStar());
                        map.put("ratingCount", shop.getRatingCount());
                        map.put("tradeArea", shop.getTradeArea());
                    }
                }
                nearMap.put(entry.getKey(), mappers);
            }
        }
        model.addAttribute("nearMap", nearMap);
        return "mizar/remark/nearrank";
    }

    // 教育机构 -- 写点评
    @RequestMapping(value = "/remark/remark.vpage", method = {RequestMethod.GET})
    public String remark(Model model) {
        String shopId = getRequestString("shopId");
        Integer activityId = getRequestInt("activityId");
        Long userId = currentUserId();
        if (userId == null || userId == 0) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号~").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
            return "mizar/remark/remark";
        }
        if (StringUtils.isBlank(shopId)) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的机构不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/remark/remark";
        }
        MizarShop shop = mizarLoaderClient.loadShopById(shopId);
        if (shop == null) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的机构不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "mizar/remark/remark";
        }
        model.addAttribute("shopId", shopId);
        model.addAttribute("activityId", activityId);
        model.addAttribute("shopName", getRequestString("shopName"));

        return "mizar/remark/remark";
    }

    // 写点评
    @RequestMapping(value = "rating.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage rating() {
        Long parentId = currentUserId();
        String shopId = getRequestString("shopId");
        Integer ratingStar = getRequestInt("ratingStar");
        String ratingContent = getRequestString("ratingContent");
        String photo = getRequestString("photo"); // 多张逗号分隔
        Integer activityId = getRequestInt("activityId", 0);
        Double cost = getRequestDouble("cost");
        if (parentId == null || parentId == 0) {
            return MapMessage.errorMessage("请登录家长号");
        }
        if (StringUtils.isBlank(shopId)) {
            return MapMessage.errorMessage("请选择要点评的机构");
        }
        MizarShop shop = mizarLoaderClient.loadShopById(shopId);
        if (shop == null) {
            return MapMessage.errorMessage("点评的机构不存在");
        }
        if (StringUtils.isBlank(ratingContent) || badWordCheckerClient.containsConversationBadWord(ratingContent)) {
            return MapMessage.errorMessage("对不起，包含非法字符，请检查后输入~");
        }
        long likeCount = asyncMizarCacheServiceClient.getAsyncMizarCacheService()
                .MizarLikeShopMonthCountManager_loadLikeCount(parentId)
                .getUninterruptibly();
        if (likeCount >= 10) {
            return MapMessage.errorMessage("对不起，您本月点评次数已达上限~");
        }
        // 获取第一个孩子的头像 + 显示名称
        Map<String, Object> showMap = generateParentFirstStudentShowInfo(parentId);
        if (MapUtils.isEmpty(showMap)) {
            return MapMessage.errorMessage("对不起，你还没有绑定孩子");
        }
        MizarRating rating = new MizarRating();
        rating.setUserAvatar(SafeConverter.toString(showMap.get("avatar")));
        rating.setUserName(SafeConverter.toString(showMap.get("showName")));
        rating.setUserId(parentId);
        rating.setActivityId(activityId);
        if (StringUtils.isNotBlank(photo)) {
            rating.setPhoto(Arrays.asList(StringUtils.split(photo, ",")));
        }
        rating.setRating(ratingStar);
        rating.setRatingContent(ratingContent);
        rating.setRatingTime(new Date().getTime());
        rating.setShopId(shopId);
        rating.setCost(cost);
        // 点评四星以上无图的可以直接展示上线，否则都需要审核
        rating.setStatus(MizarRatingStatus.PENDING.name());
        if (rating.getRating() >= 4 && CollectionUtils.isEmpty(rating.getPhoto())) {
            rating.setStatus(MizarRatingStatus.ONLINE.name());
        }
        // 点评
        return atomicLockManager.wrapAtomic(mizarServiceClient)
                .keyPrefix("MIZAR_RATING")
                .keys(parentId, shopId)
                .proxy()
                .saveMizarRating(rating);
    }


    // 机构点评列表页分页
    @RequestMapping(value = "loadratingpage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadRatingPage() {
        String shopId = getRequestString("shopId");
        if (StringUtils.isBlank(shopId)) {
            return MapMessage.errorMessage("机构不存在");
        }
        MizarShop shop = mizarLoaderClient.loadShopById(shopId);
        if (shop == null) {
            return MapMessage.errorMessage("机构不存在");
        }
        Integer pageSize = getRequestInt("pageSize", 5);
        Integer pageNum = getRequestInt("pageNum", 1);
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);
        PageImpl<MizarRating> dataPage = mizarLoaderClient.loadRatingPage(shopId, pageable);
        List<MizarRating> ratings = dataPage.getContent();
        List<Map<String, Object>> contentList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ratings)) {
            ratings.forEach(mizarRating -> {
                contentList.add(MizarRating.toRatingMap(mizarRating));
            });
        }
        return MapMessage.successMessage()
                .add("pageNum", dataPage.getNumber())
                .add("pageSize", dataPage.getSize())
                .add("rows", contentList)
                .add("totalPage", dataPage.getTotalPages())
                .add("totalSize", dataPage.getTotalElements());
    }


    // 机构收集点评活动列表页
    @RequestMapping(value = "loadactivityratings.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadActivityRatings() {
        Long time = getRequestLong("time");
        Integer pageSize = getRequestInt("pageSize", 5);
        Integer pageNum = getRequestInt("pageNum", 1);
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);
        PageImpl<Map<String, Object>> dataPage = mizarLoaderClient.loadCollectionRatings(MizarRatingActivity.Collect_1.getId(), time, pageable);
        return MapMessage.successMessage()
                .add("pageNum", dataPage.getNumber())
                .add("pageSize", dataPage.getSize())
                .add("rows", dataPage.getContent())
                .add("totalPage", dataPage.getTotalPages())
                .add("totalSize", dataPage.getTotalElements());
    }

    // 机构收集活动投票
    @RequestMapping(value = "likeshop.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage likeShop() {
        String shopId = getRequestString("shopId");
        Integer activityId = getRequestInt("activityId");
        Long parentId = currentUserId();
        if (StringUtils.isBlank(shopId) || parentId == null || parentId == 0 || activityId == 0) {
            return MapMessage.errorMessage("请选择要投票的店铺");
        }
        // 本人是否已经投票
        boolean isLike = mizarLoaderClient.likedShop(shopId, MizarRatingActivity.Collect_1.getId(), parentId);
        if (isLike) {
            return MapMessage.errorMessage("不能重复投票哦~");
        }
        MizarShop shop = mizarLoaderClient.loadShopById(shopId);
        if (shop == null) {
            return MapMessage.errorMessage("投票的机构不存在");
        }
        // 投票
        return atomicLockManager.wrapAtomic(mizarServiceClient)
                .keyPrefix("MIZAR_LIKED")
                .keys(parentId, shopId)
                .proxy()
                .likedShop(parentId, shopId, activityId);
    }

    // 导流中间页 （上课了）
    @RequestMapping(value = "/course/goclass.vpage", method = {RequestMethod.GET})
    public String goClass(Model model) {
        Long parentId = currentUserId();
        if (parentId == null || parentId == 0) {
            model.addAttribute("result", MapMessage.errorMessage("请登录家长号~").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE));
            return "mizar/course/goclass";
        }
        List<User> child = studentLoaderClient.loadParentStudents(parentId);
        if (CollectionUtils.isEmpty(child)) {
            model.addAttribute("result", MapMessage.errorMessage("未绑定孩子~").setErrorCode(ApiConstants.RES_RESULT_USER_UNBIND_STUDENT));
            return "mizar/course/goclass";
        }
        // 是否显示身边课程栏目  看孩子有没有在灰度内
        boolean nearClassFlag = false;
        for (User user : child) {
            StudentDetail detail = studentLoaderClient.loadStudentDetail(user.getId());
            if (detail == null) {
                continue;
            }
            if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(detail, "Mizar", "NearShop")) {
                nearClassFlag = true;
                break;
            }
        }
        model.addAttribute("nearClassFlag", nearClassFlag);
        // 根据分类获取首页显示内容
        Map<String, List<MizarCourseMapper>> courseMap = mizarLoaderClient.loadMizarCourseIndexMapByParentId(parentId);
        model.addAttribute("courseMap", courseMap);

        String pageType = getRequestString("type");

        if (StringUtils.isNotBlank(pageType) && pageType.contains("new")) {
            return "mizar/course/goclassnew";
        }

        return "mizar/course/goclass";
    }

    // （上课了） 好课试听 列表页  根据时间排序分页
    @RequestMapping(value = "/course/goodcourse.vpage", method = {RequestMethod.GET})
    public String goodCourse(Model model) {
        model.addAttribute("category", MizarCourseCategory.GOOD_COURSE.name());

        String pageType = getRequestString("type");

        if (StringUtils.isNotBlank(pageType) && pageType.contains("new")) {
            return "mizar/course/goodcoursenew";
        }

        return "mizar/course/goodcourse";
    }

    // （上课了） 亲子活动 列表页  根据时间排序分页
    @RequestMapping(value = "/course/parentalcourse.vpage", method = {RequestMethod.GET})
    public String parentalCourse(Model model) {
        model.addAttribute("category", MizarCourseCategory.PARENTAL_ACTIVITY.name());
        return "mizar/course/parentalcourse";
    }

    // （上课了） 每日一课 列表页  根据时间排序分页
    @RequestMapping(value = "/course/daycourse.vpage", method = {RequestMethod.GET})
    public String dayCourse(Model model) {
        model.addAttribute("category", MizarCourseCategory.DAY_COURSE.name());
        return "mizar/course/daycourse";
    }

    // （上课了） 精品视频课程 列表页  根据时间排序分页
    @RequestMapping(value = "/course/videocourse.vpage", method = {RequestMethod.GET})
    public String videoCourse(Model model) {
        model.addAttribute("category", MizarCourseCategory.VIDEO_COURSE.name());
        return "mizar/course/videocourse";
    }

    // （上课了） 新版上课了页面 好课试听沿用精品视频课程页面
    @RequestMapping(value = "/course/goodcoursenew.vpage", method = {RequestMethod.GET})
    public String goodCourseNew(Model model) {
        model.addAttribute("category", MizarCourseCategory.GOOD_COURSE.name());
        return "mizar/course/videocourse";
    }

    // （上课了） 获取课程列表
    @RequestMapping(value = "course/loadcoursepage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadCoursePage() {
        Integer pageSize = getRequestInt("pageSize", 5);
        Integer pageNum = getRequestInt("pageNum", 1);
        String category = getRequestString("category");
        String tag = getRequestString("tag");
        if (StringUtils.isBlank(category)) {
            return MapMessage.successMessage();
        }
        MizarCourseCategory courseCategory = MizarCourseCategory.of(category);
        if (courseCategory == null) {
            return MapMessage.errorMessage("无效的分类");
        }
        Long parentId = currentUserId();
        Pageable pageable = new PageRequest(pageNum - 1, pageSize);
        Page<MizarCourseMapper> dataPage = mizarLoaderClient.loadUserMizarCoursePageByCategory(courseCategory, tag, parentId, pageable);
        return MapMessage.successMessage()
                .add("pageNum", dataPage.getNumber())
                .add("pageSize", dataPage.getSize())
                .add("rows", dataPage.getContent())
                .add("totalPage", dataPage.getTotalPages())
                .add("totalSize", dataPage.getTotalElements());
    }

    // （上课了） 我的课程
    @RequestMapping(value = "/course/mycourse.vpage", method = {RequestMethod.GET})
    public String myCourse(Model model) {
        Long parentId = currentUserId();
        // 没登录的话去登录吧
        if (parentId == null) {
            return "redirect: /";
        }

        List<Map<String, Object>> trusteeOrderList = new ArrayList<>();
        // 获取通用预约订单
        List<TrusteeReserveRecord> reserveRecords = trusteeOrderServiceClient.loadTrusteeReserveByParentId(parentId);
        List<TrusteeReserveRecord> seattleReserveRecords = reserveRecords.stream()
                .filter(r -> r.getStatus() == TrusteeReserveRecord.Status.Success)
                .filter(r -> r.getActivityId() != null && r.getActivityId() != 0)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(seattleReserveRecords)) {
            Set<Long> activityIds = seattleReserveRecords.stream().map(TrusteeReserveRecord::getActivityId).collect(Collectors.toSet());
            Map<Long, BusinessActivity> activityMap = new LinkedHashMap<>();
            for (Long id : activityIds) {
                BusinessActivity ba = businessActivityManagerClient.getBusinessActivityBuffer().load(id);
                if (ba != null) {
                    activityMap.put(id, ba);
                }
            }
            for (TrusteeReserveRecord reserveRecord : seattleReserveRecords) {
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("createTime", DateUtils.dateToString(reserveRecord.getCreateDatetime(), "yyyy年MM月dd日 HH:mm"));
                objectMap.put("title", activityMap.get(reserveRecord.getActivityId()).getTitle());
                objectMap.put("price", 0);
                objectMap.put("redirectUrl", activityMap.get(reserveRecord.getActivityId()).getReturnUrl());
                trusteeOrderList.add(objectMap);
            }
        }

        // 将微课堂预约加入试听课列表
        List<TrusteeReserveRecord> courseReserves = reserveRecords.stream()
                .filter(r -> r.getStatus() == TrusteeReserveRecord.Status.Success)
                .filter(r -> StringUtils.isNotBlank(r.getTargetId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(courseReserves)) {
            // 按课时购买订单
            List<String> periodIds = courseReserves.stream().filter(p -> !Boolean.TRUE.equals(p.getNeedPay()))
                    .map(TrusteeReserveRecord::getTargetId).distinct().collect(Collectors.toList());
            Map<String, MicroCoursePeriod> periodMap = microCourseLoaderClient.getCourseLoader().loadCoursePeriods(periodIds);
            // 按课程购买订单
            List<String> courseIds = courseReserves.stream().filter(p -> Boolean.TRUE.equals(p.getNeedPay()))
                    .map(TrusteeReserveRecord::getTargetId).distinct().collect(Collectors.toList());
            Map<String, MicroCourse> courseMap = microCourseLoaderClient.getCourseLoader().loadMicroCourses(courseIds);
            for (TrusteeReserveRecord reserve : courseReserves) {
                if (periodMap.containsKey(reserve.getTargetId())) {
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.put("createTime", DateUtils.dateToString(reserve.getCreateDatetime(), "yyyy年MM月dd日 HH:mm"));
                    objectMap.put("title", periodMap.get(reserve.getTargetId()).getTheme());
                    objectMap.put("price", 0);
                    objectMap.put("redirectUrl", MicroCourseMsgTemplate.linkUrl(reserve.getSignPics(), reserve.getTargetId()));
                    trusteeOrderList.add(objectMap);
                } else if (courseMap.containsKey(reserve.getTargetId())) {
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap.put("createTime", DateUtils.dateToString(reserve.getCreateDatetime(), "yyyy年MM月dd日 HH:mm"));
                    objectMap.put("title", courseMap.get(reserve.getTargetId()).getName());
                    objectMap.put("price", 0);
                    objectMap.put("redirectUrl", MicroCourseMsgTemplate.linkUrl(reserve.getSignPics(), reserve.getTargetId()));
                    trusteeOrderList.add(objectMap);
                }
            }
        }

        List<UserOrder> userOrders = userOrderLoaderClient.loadUserOrderList(parentId);
        // 新的微课堂订单
        List<Map<String, Object>> courseOrderList = new LinkedList<>();
        List<UserOrder> microCourseOrders = userOrders.stream()
                .filter(o -> OrderType.micro_course == o.getOrderType())
                .filter(o -> OrderStatus.Confirmed == o.getOrderStatus())
                .filter(o -> PaymentStatus.Paid == o.getPaymentStatus())
                .sorted(Comparator.comparing(UserOrder::getUpdateDatetime))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(microCourseOrders)) {
            for (UserOrder courseOrder : microCourseOrders) {
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("createTime", DateUtils.dateToString(courseOrder.getCreateDatetime(), "yyyy年MM月dd日 HH:mm"));
                objectMap.put("title", courseOrder.getProductName());
                objectMap.put("price", courseOrder.getOrderPrice());
                objectMap.put("redirectUrl", MicroCourseMsgTemplate.linkUrl(courseOrder.getUserReferer(), courseOrder.getProductId()));
                objectMap.put("orderNo", courseOrder.getId());
                courseOrderList.add(objectMap);
            }
        }
        model.addAttribute("courseOrderList", courseOrderList);

        // 通用支付订单 fixme 兼容一段时间老的订单
//        List<UserOrder> seattleOrderList = userOrders.stream()
//                .filter(o -> OrderType.seattle == o.getOrderType())
//                .filter(o -> OrderStatus.Confirmed == o.getOrderStatus())
//                .filter(o -> PaymentStatus.Paid == o.getPaymentStatus())
//                .collect(Collectors.toList());
//        if (CollectionUtils.isNotEmpty(seattleOrderList)) {
//            List<Long> activityIds = seattleOrderList.stream().map(o -> SafeConverter.toLong(o.getProductId())).distinct().collect(Collectors.toList());
//            Map<Long, BusinessActivity> activityMap = businessActivityServiceClient.loadBusinessActivitiesIncludeDisabled(activityIds);
//            for (UserOrder order : seattleOrderList) {
//                Map<String, Object> objectMap = new HashMap<>();
//                objectMap.put("createTime", DateUtils.dateToString(order.getCreateDatetime(), "yyyy年MM月dd日 HH:mm"));
//                objectMap.put("title", activityMap.get(SafeConverter.toLong(order.getProductId())).getTitle());
//                objectMap.put("price", order.getOrderPrice());
//                objectMap.put("redirectUrl", activityMap.get(SafeConverter.toLong(order.getProductId())).getReturnUrl());
//                objectMap.put("orderNo", order.getId());
//                trusteeOrderList.add(objectMap);
//            }
//        }
        List<TrusteeOrderRecord> orderRecords = trusteeOrderServiceClient.loadTrusteeOrderByParentId(parentId);
        List<TrusteeOrderRecord> trusteeOrders = orderRecords.stream()
                .filter(o -> SafeConverter.toInt(o.getOrderType(), 1) == MizarOrderType.COMMON_PAY.getCode())
                .filter(o -> o.getStatus() == TrusteeOrderRecord.Status.Paid)
                .filter(o -> o.getActivityId() != null && o.getActivityId() != 0)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(trusteeOrders)) {
            Set<Long> activityIds = trusteeOrders.stream().map(TrusteeOrderRecord::getActivityId).collect(Collectors.toSet());
            Map<Long, BusinessActivity> activityMap = new LinkedHashMap<>();
            for (Long id : activityIds) {
                BusinessActivity ba = businessActivityManagerClient.getBusinessActivityBuffer().load(id);
                if (ba != null) {
                    activityMap.put(id, ba);
                }
            }
            for (TrusteeOrderRecord record : trusteeOrders) {
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("createTime", DateUtils.dateToString(record.getCreateTime(), "yyyy年MM月dd日 HH:mm"));
                objectMap.put("title", activityMap.get(record.getActivityId()).getTitle());
                objectMap.put("price", activityMap.get(record.getActivityId()).getProductPrice());
                objectMap.put("redirectUrl", activityMap.get(record.getActivityId()).getReturnUrl());
                objectMap.put("orderNo", record.getId());
                trusteeOrderList.add(objectMap);
            }
        }

        // 排序
        Collections.sort(trusteeOrderList, (o1, o2) -> {
            Date d1 = DateUtils.stringToDate(SafeConverter.toString(o1.get("createTime")), "yyyy年MM月dd日 HH:mm");
            Date d2 = DateUtils.stringToDate(SafeConverter.toString(o2.get("createTime")), "yyyy年MM月dd日 HH:mm");
            return d2.compareTo(d1);
        });
        model.addAttribute("trusteeOrderList", trusteeOrderList);

        // 获取机构预约记录
        List<Map<String, Object>> mizarOrderList = new ArrayList<>();
        List<MizarReserveRecord> mizarReserves = mizarLoaderClient.loadShopReserveByParentId(parentId);
        if (CollectionUtils.isNotEmpty(mizarReserves)) {
            Set<String> shopIds = mizarReserves.stream().map(MizarReserveRecord::getShopId).collect(Collectors.toSet());
            Map<String, MizarShop> shopMap = mizarLoaderClient.loadShopByIds(shopIds);
            for (MizarReserveRecord record : mizarReserves) {
                MizarShop shop = shopMap.get(record.getShopId());
                if (shop == null) {
                    continue;
                }
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("createTime", DateUtils.dateToString(record.getCreateDatetime(), "yyyy年MM月dd日 HH:mm"));
                objectMap.put("shopName", shop.getFullName());
                mizarOrderList.add(objectMap);
            }
        }
        model.addAttribute("mizarOrderList", mizarOrderList);

        // 亲子活动订单
//        List<TrusteeOrderRecord> orderRecords = parentServiceClient.loadTrusteeOrderByParentId(parentId);
        List<TrusteeOrderRecord> activities = orderRecords.stream()
                .filter(o -> SafeConverter.toInt(o.getOrderType(), 1) == MizarOrderType.FAMILY_ACTIVITY.getCode())
                .filter(o -> o.getStatus() == TrusteeOrderRecord.Status.Paid)
                .filter(o -> StringUtils.isNotBlank(o.getMizarTargetId()))
                .collect(Collectors.toList());
        List<Map<String, Object>> activityList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(activities)) {
            Set<String> idSet = activities.stream().map(TrusteeOrderRecord::getMizarTargetId).collect(Collectors.toSet());
            Map<String, MizarShopGoods> activityMap = mizarLoaderClient.loadShopGoodsByIds(idSet);
            for (TrusteeOrderRecord record : activities) {
                Map<String, Object> objectMap = new HashMap<>();
                MizarShopGoods act = activityMap.get(record.getMizarTargetId());
                objectMap.put("createTime", DateUtils.dateToString(record.getCreateTime(), "yyyy年MM月dd日 HH:mm"));
                objectMap.put("title", act.getTitle());
                objectMap.put("price", record.getPrice());
                objectMap.put("orderNo", record.getId());
                activityList.add(objectMap);
            }
        }
        model.addAttribute("activityList", activityList);
        return "mizar/course/mycourse";
    }

    // （上课了） course 统一跳转路径
    @RequestMapping(value = "/course/go.vpage", method = {RequestMethod.GET})
    public String courseGo() {
        String courseId = getRequestString("id");
        if (StringUtils.isBlank(courseId)) {
            return "redirect:/mizar/course/goclass.vpage";
        }
        MizarCourse course = mizarLoaderClient.loadMizarCourseById(courseId);
        if (course == null) {
            return "redirect:/mizar/course/goclass.vpage";
        }
        String url = course.getRedirectUrl();
        if (MizarCourseCategory.MICRO_COURSE_OPENING.name().equals(course.getCategory())
                || MizarCourseCategory.MICRO_COURSE_NORMAL.name().equals(course.getCategory())) {
            url = MicroCourseMsgTemplate.linkUrl(null, course.getTitle());
        }
        if (StringUtils.isBlank(url)) {
            return "redirect:/mizar/course/goclass.vpage";
        }
        // 记录数量
        asyncMizarCacheServiceClient.getAsyncMizarCacheService()
                .MizarCourseReadCountManager_increaseCount(courseId)
                .awaitUninterruptibly();
        return "redirect:" + url;
    }


    private MizarShopMapper getPageListMap(MizarShop shop) {
        MizarShopMapper mapper = new MizarShopMapper();
        mapper.setId(shop.getId());
        mapper.setName(shop.getFullName());
        if (CollectionUtils.isNotEmpty(shop.getPhoto())) {
            mapper.setPhoto(shop.getPhoto().get(0));
        }
        mapper.setRatingCount(shop.getRatingCount() == null ? 0 : shop.getRatingCount());
        mapper.setRatingStar(shop.getRatingStar() == null ? 0 : shop.getRatingStar());
        mapper.setTradeArea(shop.getTradeArea());
        mapper.setSecondCategory(shop.getSecondCategory());
        return mapper;
    }

    @Override
    protected Map<String, Object> loadNewSelfstudyGameFlash(LoadFlashGameContext context) {
        return super.loadNewSelfstudyGameFlash(context);
    }


    //---------------------------------------------------------------------
    //-----------------         亲 子 活 动 相 关                ----------
    //---------------------------------------------------------------------

    // 亲子活动列表页
    @RequestMapping(value = "familyactivity/list.vpage", method = RequestMethod.GET)
    public String activityList(Model model) {
        return "mizar/familyactivity/list";
    }

    // 获取亲子活动列表
    @RequestMapping(value = "familyactivity/more.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage moreActivity() {
        int pageNum = getRequestInt("pageNum", 0);
        int pageSize = getRequestInt("pageSize", 5);
        Pageable page = new PageRequest(pageNum, pageSize);
        // 列表中只读取亲子活动
        List<String> types = Collections.singletonList(MizarShopGoods.familyActivityType());
        try {
            Page<MizarShopGoods> activityPage = mizarLoaderClient.loadPageByGoodsType(page, MizarGoodsStatus.ONLINE, types);
            return MapMessage.successMessage()
                    .add("pageNum", pageNum)
                    .add("pageSize", pageSize)
                    .add("activityList", mapActivityList(activityPage.getContent()))
                    .add("totalPage", activityPage.getTotalPages())
                    .add("totalSize", activityPage.getTotalElements());
        } catch (Exception ex) {
            logger.error("Failed get more family activity", ex);
            return MapMessage.errorMessage("加载失败");
        }
    }

    // 亲子活动详情页
    @RequestMapping(value = "familyactivity/detail.vpage", method = RequestMethod.GET)
    public String activityDetail(Model model) {
        String activityId = getRequestString("actId");
        MizarShopGoods activity = mizarLoaderClient.loadShopGoodsById(activityId);
        // 校验是否是亲子活动
        // 兼容USTalk
        if (activity == null || MizarGoodsStatus.ONLINE != activity.getStatus() || (!activity.isFamilyActivity() && !activity.isUSTalkActivity())) {
            return "redirect: /mizar/familyactivity/list.vpage";
        }
        // 前端传过来当请的GPS位置
        Double latitude = getRequestDouble("lat");
        Double longitude = getRequestDouble("lng");
        model.addAttribute("activity", mapActivity(activity, latitude, longitude));
        // 没有产品选择页直接跳转支付页面
        model.addAttribute("directlyPay", CollectionUtils.isEmpty(activity.getItems()));
        // 是否已经购买成功了
        List<TrusteeOrderRecord> orderRecordList = trusteeOrderServiceClient.loadTrusteeOrderByParentId(currentUserId());
        // 过滤是否已经购买过了 如果购买过， 给一个提示
        TrusteeOrderRecord paidRecord = orderRecordList.stream()
                .filter(o -> SafeConverter.toInt(o.getOrderType(), 1) == MizarOrderType.FAMILY_ACTIVITY.getCode())
                .filter(o -> Objects.equals(o.getMizarTargetId(), activityId))
                .filter(o -> o.getStatus() == TrusteeOrderRecord.Status.Paid)
                .findFirst().orElse(null);
        model.addAttribute("paid", paidRecord != null);
        return "mizar/familyactivity/detail";//活动详情页面
    }

    // 产品选择
    @RequestMapping(value = "familyactivity/apply.vpage", method = RequestMethod.GET)
    public String selectProductType(Model model) {
        String activityId = getRequestString("actId");
        MizarShopGoods activity = mizarLoaderClient.loadShopGoodsById(activityId);
        // 校验是否是亲子活动
        // 兼容USTalk
        if (activity == null || MizarGoodsStatus.ONLINE != activity.getStatus() || (!activity.isFamilyActivity() && !activity.isUSTalkActivity())) {
            return "redirect: /mizar/familyactivity/list.vpage";
        }
        if (CollectionUtils.isEmpty(activity.getItems())) {
            // 没有产品类型的时候，直接从活动中取参数
            model.addAttribute("price", activity.getPrice());
        } else {
            // 有产品类型的时候取产品类型
            Map<String, List<MizarGoodsItem>> itemMap = activity.getItems()
                    .stream()
                    .collect(Collectors.groupingBy(MizarGoodsItem::getCategoryName));
            model.addAttribute("itemMap", itemMap);
        }
        model.addAttribute("title", activity.getTitle()); // 支付页产品标题
        return "mizar/familyactivity/apply";//活动订单产品选择
    }

    // 支付中间页
    @RequestMapping(value = "familyactivity/pay.vpage", method = RequestMethod.GET)
    public String familyActivityPayment(Model model) {
        String activityId = getRequestString("actId");
        Boolean directlyPay = getRequestBool("dp");
        String itemId = getRequestString("item");
        MapMessage validMsg = validateFamilyActivity(activityId, directlyPay, itemId);
        if (!validMsg.isSuccess()) {
            return "redirect: /mizar/familyactivity/list.vpage";
        }
        Double price = SafeConverter.toDouble(validMsg.get("price"));
        MizarShopGoods activity = (MizarShopGoods) validMsg.get("activity");
        model.addAttribute("actId", activityId);
        model.addAttribute("dp", directlyPay);
        model.addAttribute("itemId", itemId);
        model.addAttribute("price", price);
        model.addAttribute("title", activity.getTitle());
        return "mizar/familyactivity/pay";
    }

    // 生成订单
    @RequestMapping(value = "familyactivity/order.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createOrder() {
        String activityId = getRequestString("activityId");
        Boolean directlyPay = getRequestBool("dp");
        String itemId = getRequestString("item");
        Long parentId = currentUserId();
        if (parentId == null || parentId == 0) {
            return MapMessage.errorMessage("参数错误");
        }
        MapMessage validMsg = validateFamilyActivity(activityId, directlyPay, itemId);
        if (!validMsg.isSuccess()) {
            return validMsg;
        }
        try {
            Double price = SafeConverter.toDouble(validMsg.get("price"));
            MizarShopGoods activity = (MizarShopGoods) validMsg.get("activity");
            List<TrusteeOrderRecord> orderRecordList = trusteeOrderServiceClient.loadTrusteeOrderByParentId(parentId);
            // 过滤是否已经购买过了 如果购买过， 给一个提示
            TrusteeOrderRecord paidRecord = orderRecordList.stream()
                    .filter(o -> SafeConverter.toInt(o.getOrderType(), 1) == MizarOrderType.FAMILY_ACTIVITY.getCode())
                    .filter(o -> Objects.equals(o.getMizarTargetId(), activityId))
                    .filter(o -> o.getStatus() == TrusteeOrderRecord.Status.Paid)
                    .findFirst().orElse(null);
            if (paidRecord != null) {
                return MapMessage.errorMessage("您已成功购买该产品，请去我的订单查看。").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }
            // 看看该类型是否有未支付订单， 如果有，直接用现有的订单
            TrusteeOrderRecord record = orderRecordList.stream()
                    .filter(o -> SafeConverter.toInt(o.getOrderType(), 1) == MizarOrderType.FAMILY_ACTIVITY.getCode())
                    .filter(o -> Objects.equals(o.getMizarTargetId(), activityId))
                    .filter(o -> directlyPay || Objects.equals(o.getMizarItemId(), itemId))
                    .filter(o -> o.getStatus() == TrusteeOrderRecord.Status.New)
                    .findFirst().orElse(null);
            if (record != null) {
                return MapMessage.successMessage().add("orderId", record.getId());
            } else {
                TrusteeOrderRecord orderRecord = TrusteeOrderRecord.newOrder();
                orderRecord.setParentId(parentId);
                // 1分钱测试
                if (RuntimeMode.ge(Mode.PRODUCTION) && freeParentIds.contains(parentId)) {
                    orderRecord.setPrice(new BigDecimal(0.01));
                } else {
                    orderRecord.setPrice(new BigDecimal(price));
                }
                orderRecord.setPayMethod("wechat_parent");
                orderRecord.setMizarTargetId(activityId); // MizarShopGoods Id
                orderRecord.setMizarItemId(itemId);
                orderRecord.setOrderType(MizarOrderType.FAMILY_ACTIVITY.getCode());
                orderRecord.setRemark(itemId); // 备注里填上选择商品的ID
                MapMessage message = AtomicLockManager.instance().wrapAtomic(trusteeOrderServiceClient)
                        .keyPrefix("ParentService:saveTrusteeOrder")
                        .keys(parentId, activity.getId())
                        .proxy().saveTrusteeOrder(orderRecord);
                if (message.isSuccess()) {
                    // 处理成功之后扣除项目的剩余数量
                    if (CollectionUtils.isNotEmpty(activity.getItems())) {
                        activity.getItems().stream()
                                .filter(item -> itemId.equals(item.getItemId()))
                                .forEach(t -> t.setRemains(t.getRemains() - 1));
                        mizarServiceClient.saveMizarShopGoods(activity);
                    }
                    return MapMessage.successMessage().add("orderId", message.get("id"));
                }
            }
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        } catch (Exception ex) {
            logger.error("Create seattle order failed,parentId:{},directlyPay:{}, itemId:{},activityId:{}", parentId, directlyPay, itemId, activityId, ex);
        }
        return MapMessage.errorMessage("生成订单失败");
    }

    //已支付页
    @RequestMapping(value = "familyactivity/paiddetail.vpage", method = RequestMethod.GET)
    public String paidDetail(Model model) {
        String orderNo = getRequestString("orderNo");
        if (StringUtils.isBlank(orderNo)) {
            return "redirect: /mizar/familyactivity/list.vpage";
        }
        // 校验订单
        TrusteeOrderRecord order = trusteeOrderServiceClient.loadTrusteeOrder(orderNo);
        if (order == null || order.isDisabledTrue()) {
            return "redirect: /mizar/familyactivity/list.vpage";
        }
        MizarShopGoods activity = mizarLoaderClient.loadShopGoodsById(order.getMizarTargetId());
        // 校验是否是亲子活动
        // 兼容USTalk
        if (activity == null || MizarGoodsStatus.ONLINE != activity.getStatus() || (!activity.isFamilyActivity() && !activity.isUSTalkActivity())) {
            return "redirect: /mizar/familyactivity/list.vpage";
        }
        model.addAttribute("actId", activity.getId()); // 活动ID
        // 商品信息
        model.addAttribute("payStatus", order.getStatus()); // 支付状态
        model.addAttribute("title", activity.getTitle()); // 标题
        model.addAttribute("cover", CollectionUtils.isEmpty(activity.getBannerPhoto()) ? null : activity.getBannerPhoto().stream().findFirst().orElse(null)); // 顶部图

        // 订单信息
        MizarGoodsItem item = CollectionUtils.isEmpty(activity.getItems()) ? null : activity.getItems().stream().filter(t -> Objects.equals(order.getRemark(), t.getItemId())).findFirst().orElse(null);
        model.addAttribute("actTime", item == null ? null : item.getItemName()); // 时间
        model.addAttribute("productType", item == null ? null : item.getCategoryName()); // 产品类型
        model.addAttribute("createTime", DateUtils.dateToString(order.getCreateTime(), "yyyy年MM月dd日 HH:mm")); // 下单时间
        model.addAttribute("orderNo", orderNo); // 订单编号

        // 活动地点
        // 前端传过来当请的GPS位置， 如果是从 OrderApi过来的话，是没有距离这个选项的
        Double lat1 = getRequestDouble("lat");
        Double lng1 = getRequestDouble("lng");
        if (StringUtils.isNotBlank(activity.getAddress())) {
            model.addAttribute("address", activity.getAddress()); // 活动地点
            // 距离
            Double lat2 = SafeConverter.toDouble(activity.getLatitude());
            Double lng2 = SafeConverter.toDouble(activity.getLongitude());
            if (lat1 > 0 && lat2 > 0 && lng1 > 0 && lng2 > 0) {
                double distance = GEOUtils.getDistance(lat1, lng1, lat2, lng2);
                model.addAttribute("distance", distance); // 距离
            }
        }

        // 联系人
        String userName = null;
        StudentParentRef studentRef = parentLoaderClient.loadParentStudentRefs(currentUserId()).stream().findFirst().orElse(null);
        if (studentRef != null) {
            Student student = studentLoaderClient.loadStudent(studentRef.getStudentId());
            if (student != null && student.getProfile() != null && student.getProfile().getRealname() != null) {
                userName = student.getProfile().getRealname() + studentRef.getCallName();
            }
        } else {
            userName = currentParent() != null ? currentParent().getProfile().getRealname() : "";
        }
        model.addAttribute("userName", userName); // 取出第一个孩子的信息系
        model.addAttribute("userPhone", sensitiveUserDataServiceClient.loadUserMobileObscured(currentUserId()));
        // 咨询电话
        model.addAttribute("tels", StringUtils.isBlank(activity.getContact()) ? Collections.emptyList() : Collections.singleton(activity.getContact()));
        model.addAttribute("successUrl", activity.getSuccessUrl());
        return "mizar/familyactivity/paiddetail";
    }

    @RequestMapping(value = "activitymap.vpage", method = RequestMethod.GET)
    public String activityMap(Model model) {
        String activityId = getRequestString("actId");
        MizarShopGoods activity = mizarLoaderClient.loadShopGoodsById(activityId);

        // 校验是否是亲子活动
        // 兼容USTalk
        if (activity == null || MizarGoodsStatus.ONLINE != activity.getStatus() || (!activity.isFamilyActivity() && !activity.isUSTalkActivity())) {
            return "redirect: /mizar/familyactivity/list.vpage";
        }
        model.addAttribute("actId", activity);
        model.addAttribute("longitude", activity.getLongitude());
        model.addAttribute("latitude", activity.getLatitude());
        model.addAttribute("title", activity.getTitle());
        model.addAttribute("address", activity.getAddress());
        model.addAttribute("tels", StringUtils.isBlank(activity.getContact()) ? Collections.emptyList() : Collections.singleton(activity.getContact()));
        return "mizar/map/map";//活动位置 公用map 所以地址不统一
    }

    private List<Map<String, Object>> mapActivityList(List<MizarShopGoods> activities) {
        if (CollectionUtils.isEmpty(activities)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> activityList = new ArrayList<>();
        for (MizarShopGoods activity : activities) {
            Map<String, Object> info = new HashMap<>();
            info.put("actId", activity.getId());
            info.put("title", activity.getTitle()); // 主标题
            info.put("desc", activity.getDesc()); // 简介
            info.put("cover", CollectionUtils.isEmpty(activity.getBannerPhoto()) ? null : activity.getBannerPhoto().stream().findFirst().orElse(null)); // 封面图
            info.put("tags", activity.getTags()); // 标签
            activityList.add(info);
        }
        return activityList;
    }

    private Map<String, Object> mapActivity(MizarShopGoods activity, Double lat, Double lng) {
        if (activity == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> info = new HashMap<>();
        info.put("actId", activity.getId());
        info.put("title", activity.getTitle()); // 主标题
        info.put("banner", CollectionUtils.isEmpty(activity.getDetail()) ? null : activity.getDetail().stream().findFirst().orElse(null)); // 顶部图
        info.put("reportDesc", activity.getReportDesc()); // 体验报告
        info.put("url", activity.getRedirectUrl()); // 跳转链接
        info.put("activityDesc", activity.getActivityDesc()); // 活动介绍
        info.put("expenseDesc", activity.getExpenseDesc()); // 费用说明
        info.put("address", activity.getAddress()); // 活动地址
        info.put("contact", activity.getContact()); // 咨询
        info.put("successUrl", activity.getSuccessUrl());
        // 距离
        Double lat1 = SafeConverter.toDouble(lat);
        Double lat2 = SafeConverter.toDouble(activity.getLatitude());
        Double lng1 = SafeConverter.toDouble(lng);
        Double lng2 = SafeConverter.toDouble(activity.getLongitude());
        if (lat1 > 0 && lat2 > 0 && lng1 > 0 && lng2 > 0) {
            double distance = GEOUtils.getDistance(lat1, lng1, lat2, lng2);
            info.put("distance", distance);
        }
        return info;
    }

    private MapMessage validateFamilyActivity(String activityId, Boolean directlyPay, String itemId) {
        MizarShopGoods activity = mizarLoaderClient.loadShopGoodsById(activityId);
        // 兼容USTalk
        if (activity == null || MizarGoodsStatus.ONLINE != activity.getStatus() || (!activity.isFamilyActivity() && !activity.isUSTalkActivity())) {
            return MapMessage.errorMessage("您访问的活动不存在或已过期~");
        }
        Double price;
        if (directlyPay) {
            price = activity.getPrice();
        } else {
            if (CollectionUtils.isEmpty(activity.getItems()) || StringUtils.isBlank(itemId)) {
                return MapMessage.errorMessage("参数错误");
            }
            MizarGoodsItem item = activity.getItems().stream().filter(t -> itemId.equals(t.getItemId())).findFirst().orElse(null);
            if (item == null) {
                return MapMessage.errorMessage("参数错误");
            }
            // 检查剩余数量
            int remains = SafeConverter.toInt(item.getRemains());
            if (remains == 0) {
                return MapMessage.errorMessage("对不起，已售罄~");
            }
            price = item.getPrice();

        }
        if (SafeConverter.toDouble(price) <= 0) {
            return MapMessage.errorMessage("参数错误");
        }
        return MapMessage.successMessage()
                .add("activity", activity)
                .add("price", price);
    }


    //---------------------------------------------------------------------
    //-----------------           直 播 课 相 关                 ----------
    //---------------------------------------------------------------------

    /**
     * 直播课--统一跳转
     */
    @RequestMapping(value = "course/live/{dispatch}.vpage", method = RequestMethod.GET)
    public String liveIndex(@PathVariable("dispatch") String dispatch) {
        return "mizar/course/live/" + dispatch;
    }

}
