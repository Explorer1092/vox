package com.voxlearning.utopia.mizar.controller.bookstore;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.entity.bookStore.*;
import com.voxlearning.utopia.mizar.utils.HydraCorsairSupport;
import com.voxlearning.utopia.mizar.utils.MizarOssManageUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarDepartment;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserDepartment;
import com.voxlearning.utopia.service.region.api.RegionLoader;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yong.he
 */
@Controller
@RequestMapping(value = "/bookstore/manager")
public class BookStoreController extends AbstractMizarController {

    private static final String DEPARTMENT_ID;
    private static final String DEFAULT_PASSWORD;

    static {
        if (RuntimeMode.current().ge(Mode.STAGING)) {
            DEPARTMENT_ID = "5c49604f0d1efacccebd3c38";
            DEFAULT_PASSWORD = "mizar@17zy";
        } else {
            DEPARTMENT_ID = "5c459f47e92b1b5cb4feacb2";
            DEFAULT_PASSWORD = "1";
        }
    }

    @Inject
    HydraCorsairSupport hydraCorsairSupport;

    @ImportService(interfaceClass = RegionLoader.class)
    RegionLoader regionLoader;


    /**
     * 查看书店列表index
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        Long totalElements = 0L;
        List<BookStoreBean> bookStoreList = new ArrayList<>();
        List<String> mizarUserIds;
        Page<BookStoreBean> bookStoreBeanPage;
        MizarAuthUser user = getCurrentUser();
        String bookStorePhone = getRequestString("bookStorePhone");
        String contactName = getRequestString("contactName");
        String bookStoreName = getRequestString("bookStoreName");
        Long bookStoreId = getRequestLong("bookStoreId");
        int page = getRequestInt("page");
        // 最小从1开始
        page = Math.max(1, page);
        Pageable pageRequest = PageableUtils.startFromOne(page, 20);
        Integer userRole;
        if (user == null) {
            return "bookstore/list";
        }
        //根据权限设置userRoleType
        userRole = getCurrentUserRole(user);
        if (userRole == 0) {
            return "bookstore/list";
        }
        String mizarUserId = "";
        if (StringUtils.isNotBlank(bookStorePhone)) {
            MizarUser mizarUser = mizarUserLoaderClient.loadUserByMobile(bookStorePhone);
            if (mizarUser == null) {
                bookStoreBeanPage = new PageImpl<>(bookStoreList, pageRequest, totalElements);
                return getReturn(model, page, bookStoreBeanPage, userRole);
            }
            mizarUserId = mizarUser.getId();
        }

        //
        if (userRole.equals(MizarUserRoleType.BusinessDevelopmentManager.getId())) {
            mizarUserIds = getAllBDUser();
        } else {
            mizarUserIds = Collections.singletonList(user.getUserId());
        }

        //通过传递的搜索参数查询门店列表
        XueMizarBookStoreBean listInfo = hydraCorsairSupport.loadPageBookStore(bookStoreName, bookStoreId, mizarUserIds, page, 20, userRole, contactName, mizarUserId);
        if (listInfo != null) {
            bookStoreList = listInfo.getBookStoreList();
            totalElements = listInfo.getTotalElements();
            bookStoreList = convertBookStoreAddMobile(bookStoreList);
        }

        bookStoreBeanPage = new PageImpl<>(bookStoreList, pageRequest, totalElements);
        return getReturn(model, page, bookStoreBeanPage, userRole);
    }

    private String getReturn(Model model, int page, Page<BookStoreBean> bookStoreBeanPage, Integer userRole) {
        model.addAttribute("page", page);
        model.addAttribute("totalPages", bookStoreBeanPage.getTotalPages());
        model.addAttribute("userRole", userRole);
        model.addAttribute("bookStoreBeanPage", bookStoreBeanPage);
        return "bookstore/list";
    }

    /**
     * 书店详情信息
     */
    @RequestMapping(value = "view.vpage", method = RequestMethod.GET)
    public String view(Model model) {
        List<String> allBDUser = new ArrayList<>();
        BookStoreBean bookStoreBean;
        Long bookStoreId = getRequestLong("id");
        bookStoreBean = hydraCorsairSupport.loadBookStoreById(bookStoreId);
        MizarAuthUser currentUser = getCurrentUser();

        if (bookStoreBean == null) {
            return "bookstore/view";
        }
        if (currentUser == null) {
            return "bookstore/view";
        }
        Integer userRole = getCurrentUserRole(currentUser);
        if (userRole.equals(MizarUserRoleType.BusinessDevelopmentManager.getId())) {
            allBDUser = getAllBDUser();
        }
        Boolean result = hydraCorsairSupport.checkViewBookStoreByRole(bookStoreId, userRole, currentUser.getUserId(), allBDUser);
        if (!result) {
            return "bookstore/view";
        }

        MizarUser user = mizarUserLoaderClient.loadUser(bookStoreBean.getMizarUserId());
        if (user == null){
            return "bookstore/view";
        }
        Map<String, Object> storeAddressMap = JsonUtils.fromJson(bookStoreBean.getStoreAddress());
        bookStoreBean.setStoreAddressMap(storeAddressMap);
        bookStoreBean.setMobile(user.getMobile());
        model.addAttribute("bookStoreBean", bookStoreBean);
        return "bookstore/view";
    }

    /**
     * 编辑书店详情信息
     */
    @RequestMapping(value = "update.vpage", method = RequestMethod.GET)
    public String update() {
        return "bookstore/update";
    }

    /**
     * 新增书店详情信息
     */
    @RequestMapping(value = "insert.vpage", method = RequestMethod.GET)
    public String insert() {
        return "bookstore/insert";
    }

    /**
     * 查看当前人下书店的经营状况信息页面
     */
    @RequestMapping(value = "operation.vpage", method = RequestMethod.GET)
    public String operation(Model model) {
        int page = getRequestInt("page");
        page = Math.max(1, page); // 最小从1开始
        Pageable pageable = PageableUtils.startFromOne(page, 10);
        MizarAuthUser user = getCurrentUser();
        XueMizarBookStoreOrderRankBean bookStoreOrderRankBean;
        Integer userRole;
        Integer totalElement = 0;
        if (user == null) {
            return "bookstore/operation";
        }
        //根据权限设置userRoleType
        userRole = getCurrentUserRole(user);
        if (userRole == 0) {
            return "bookstore/operation";
        }
        List<String> userIds;
        if (user.isBDManager()) {
            userIds = getAllBDUser();
        } else {
            userIds = Collections.singletonList(user.getUserId());
        }
        bookStoreOrderRankBean = hydraCorsairSupport.loadOperationInfoByMizarUserIds(userIds, userRole);
        if (bookStoreOrderRankBean != null) {
            bookStoreOrderRankBean = convertOperationBean(bookStoreOrderRankBean, pageable);
            if (bookStoreOrderRankBean.getReferralRanks() != null) {
                totalElement = bookStoreOrderRankBean.getReferralRanks().getTotalPages();
            }

        }
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalElement);
        model.addAttribute("userRole", userRole);
        model.addAttribute("operationInfoBean", bookStoreOrderRankBean);
        return "bookstore/operation";

    }

    /**
     * 查看当前书店信息
     */
    @RequestMapping(value = "detail.vpage")
    @ResponseBody
    public MapMessage detail() {
        BookStoreBean bookStoreBean;
        List<String> allBDUser = new ArrayList<>();
        MizarAuthUser currentUser = getCurrentUser();
        Long bookStoreId = getRequestLong("id");
        bookStoreBean = hydraCorsairSupport.loadBookStoreById(bookStoreId);

        if (bookStoreBean == null) {
            return MapMessage.errorMessage("书店信息不存在");
        }
        if (currentUser == null) {
            return MapMessage.errorMessage("登录已过期");
        }
        Integer userRole = getCurrentUserRole(currentUser);

        if (userRole.equals(MizarUserRoleType.BusinessDevelopmentManager.getId())) {
            allBDUser = getAllBDUser();
        }
        Boolean result = hydraCorsairSupport.checkViewBookStoreByRole(bookStoreId, userRole, currentUser.getUserId(), allBDUser);
        if (!result) {
            return MapMessage.errorMessage("无权限查看信息");
        }

        MizarUser user = mizarUserLoaderClient.loadUser(bookStoreBean.getMizarUserId());
        Map<String, Object> storeAddressMap = JsonUtils.fromJson(bookStoreBean.getStoreAddress());
        bookStoreBean.setStoreAddressMap(storeAddressMap);
        bookStoreBean.setMobile(user.getMobile());

        return MapMessage.successMessage().add("bookStoreBean", bookStoreBean);
    }


    /**
     * 新增、修改书店信息
     */
    @RequestMapping(value = "add.vpage")
    @ResponseBody
    public MapMessage add() {
        Long id = getRequestLong("id");
        String mizarUserId;
        String bookStoreName = getRequestString("bookStoreName");
        String contactName = getRequestString("contactName");
        String mobile = getRequestString("mobile");
        String storeAddress = getRequestString("storeAddress");
        Integer storeSizeType = getRequestInt("storeSizeType");
        String surroundingSchool = getRequestString("surroundingSchool");
        String storeQrCode = getRequestString("storeQrCode");
        String createMizarUserId = getRequestString("createMizarUserId");
        String updateMizarUserId = getRequestString("updateMizarUserId");
        String identityCardNumber = getRequestString("identityCardNumber");
        String bankCardNumber = getRequestString("bankCardNumber");
        String depositBank = getRequestString("depositBank");
        String identityPic = getRequestString("identityPic");
        String sourceMizarUserId;
        String aliStoreQrCode = "";
        MizarAuthUser currentUser = getCurrentUser();
        //如果不是管理员或者BD则不能进行修改
        if (id > 0L) {
            boolean result = checkEditBookStoreOrMobile(currentUser);
            if (!result) {
                return MapMessage.errorMessage("无权限修改");
            }
        }
        if (StringUtils.isBlank(bookStoreName)) {
            return MapMessage.errorMessage("书店名称不可为空");
        }

        if (StringUtils.isBlank(contactName)) {
            return MapMessage.errorMessage("联系人姓名不可为空");
        }
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("无效的【手机号码】");
        }
        if (StringUtils.isBlank(storeAddress)) {
            return MapMessage.errorMessage("书店地址不可为空");
        }

        if (storeSizeType <= 0) {
            return MapMessage.errorMessage("店铺面积不可为空");
        }
        MizarUser user = mizarUserLoaderClient.loadUserByMobile(mobile);


        if (user != null) {
            //校验该手机号是否为市场人员 运营人员 或市场管理人员
            List<Integer> roleList = mizarUserLoaderClient.loadUserRoleByUserId(user.getId());
            if (roleList.contains(MizarUserRoleType.BusinessDevelopment.getId())
                    || roleList.contains(MizarUserRoleType.Operator.getId())
                    || roleList.contains(MizarUserRoleType.BusinessDevelopmentManager.getId())
                    || roleList.contains(MizarUserRoleType.MizarAdmin.getId())) {
                return MapMessage.errorMessage("管理人员，市场人员，运营人员手机号下不允许存在门店");
            }

            //校验该手机号下门店名称是否重复
            boolean checkStoreName = hydraCorsairSupport.checkStoreName(bookStoreName, id, user.getId());
            if (!checkStoreName) {
                return MapMessage.errorMessage("同一手机号下书店名称不允许重复");
            }
        }
        boolean mobileExist = mizarUserLoaderClient.checkAccountAndMobile(mobile, mobile, "");
        //新建、修改门店 若手机号不存在则创建账号
        if (!mobileExist) {
            //创建账号
            mizarUserId = createBookStoreMizarUser(mobile, contactName);
        } else {
            mizarUserId = mizarUserLoaderClient.loadUserByMobile(mobile).getId();
        }
        if (StringUtils.isBlank(mizarUserId)) {
            return MapMessage.errorMessage("创建用户失败");
        }
        if (id <= 0L) {
            //新增
            createMizarUserId = currentUser.getUserId();
            id = null;
            sourceMizarUserId = findSourceMizarUserId(currentUser);
        } else {
            BookStoreBean bookStoreBean = hydraCorsairSupport.loadBookStoreById(id);
            if (bookStoreBean == null) {
                return MapMessage.errorMessage("修改失败，书店不存在");
            }
            sourceMizarUserId = bookStoreBean.getSourceMizarUserId();
            aliStoreQrCode = bookStoreBean.getAliStoreQrCode();
            //修改
            updateMizarUserId = currentUser.getUserId();
            //判断联系人是否修改 是则修改联系人
            updateMizarUserName(mizarUserId, contactName);
        }
        BookStoreBean bookStoreBean = new BookStoreBean();
        bookStoreBean.setId(id);
        bookStoreBean.setMizarUserId(mizarUserId);
        bookStoreBean.setBookStoreName(bookStoreName);
        bookStoreBean.setContactName(contactName);
        bookStoreBean.setStoreAddress(storeAddress);
        bookStoreBean.setStoreSizeType(storeSizeType);
        bookStoreBean.setSurroundingSchool(surroundingSchool);
        bookStoreBean.setStoreQrCode(storeQrCode);
        bookStoreBean.setCreateMizarUserId(createMizarUserId);
        bookStoreBean.setUpdateMizarUserId(updateMizarUserId);
        bookStoreBean.setIdentityCardNumber(identityCardNumber);
        bookStoreBean.setBankCardNumber(bankCardNumber);
        bookStoreBean.setDepositBank(depositBank);
        bookStoreBean.setIdentityPic(identityPic);
        bookStoreBean.setAliStoreQrCode(aliStoreQrCode);
        bookStoreBean.setSourceMizarUserId(sourceMizarUserId);
        Boolean result = hydraCorsairSupport.addOrEditBookStore(bookStoreBean);
        if (result) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("操作失败");
        }
    }

    /**
     * 查看手机号信息
     */
    @RequestMapping(value = "checkAdd.vpage")
    @ResponseBody
    public MapMessage checkAdd() {
        Integer checkOrderNum;
        MizarAuthUser currentUser = getCurrentUser();
        boolean changeResult = checkEditBookStoreOrMobile(currentUser);
        if (changeResult){
            return MapMessage.successMessage();
        }
        List<BookStoreBean> bookStoreBeanList = hydraCorsairSupport.findBookStoreExtendBeanListByMizarUserId(currentUser.getUserId());
        if (CollectionUtils.isEmpty(bookStoreBeanList)){
            return MapMessage.errorMessage("抱歉，您还未创建书店不可转介绍其他门店");
        }
        Set<String> createMizarUserIds = bookStoreBeanList.stream().map(BookStoreBean::getCreateMizarUserId).collect(Collectors.toSet());
        List<MizarUserDepartment> mizarUserDepartmentList = mizarUserLoaderClient.loadUserByDepartmentId(DEPARTMENT_ID);
        Set<String> managerMizarUserIds = mizarUserDepartmentList.stream().filter(e -> {
            if (e.getRoles().contains(MizarUserRoleType.BusinessDevelopment.getId())
                    || e.getRoles().contains(MizarUserRoleType.Operator.getId())
                    || e.getRoles().contains(MizarUserRoleType.BusinessDevelopmentManager.getId())
                    || e.getRoles().contains(MizarUserRoleType.MizarAdmin.getId())) {
                return true;
            }
            return false;
        }).map(MizarUserDepartment::getUserId).collect(Collectors.toSet());
        createMizarUserIds.retainAll(managerMizarUserIds);
        if (CollectionUtils.isNotEmpty(createMizarUserIds)) {
            return MapMessage.successMessage();
        }
        //校验是否在白名单内
        List<Long> bookStoreIds = bookStoreBeanList.stream().map(BookStoreBean::getId).collect(Collectors.toList());
        checkOrderNum = hydraCorsairSupport.findCheckOrderNumByBookStoreIds(bookStoreIds);
        for (BookStoreBean bean : bookStoreBeanList) {
            if (bean.getOrderNum() >= checkOrderNum) {
                return MapMessage.successMessage();
            }
        }
        return MapMessage.errorMessage("抱歉，您的门店订单数达到" + checkOrderNum + "单才有权限转介绍其他门店哦");
    }

    /**
     * 查看手机号信息
     */
    @RequestMapping(value = "changeMobile.vpage")
    @ResponseBody
    public MapMessage changeMobile() {
        MizarAuthUser currentUser = getCurrentUser();
        //如果不是管理员或者BD则不能进行修改
        boolean changeResult = checkEditBookStoreOrMobile(currentUser);
        if (!changeResult) {
            return MapMessage.errorMessage("无权限修改");
        }
        String mobile = getRequestString("mobile");
        String mizarUserId = getRequestString("mizarUserId");
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("无效的【手机号码】");
        }
        MizarUser user = mizarUserLoaderClient.loadUser(mizarUserId);
        if (user == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        if (Objects.equals(user.getMobile(), mobile)) {
            return MapMessage.errorMessage("手机号未改变无须修改");
        }
        boolean mobileExist = mizarUserLoaderClient.checkAccountAndMobile(mobile, mobile, mizarUserId);
        if (!mobileExist) {
            user.setMobile(mobile);
            user.setAccountName(mobile);
            return mizarUserServiceClient.editMizarUser(user);
        } else {
            boolean result;
            MizarUser toUser = mizarUserLoaderClient.loadUserByMobile(mobile);
            if (toUser != null) {
                //校验将要修改的手机号是否为市场人员运营人员
                List<Integer> roleList = mizarUserLoaderClient.loadUserRoleByUserId(toUser.getId());
                if (roleList.contains(MizarUserRoleType.BusinessDevelopment.getId())
                        || roleList.contains(MizarUserRoleType.Operator.getId())
                        || roleList.contains(MizarUserRoleType.BusinessDevelopmentManager.getId())
                        || roleList.contains(MizarUserRoleType.MizarAdmin.getId())) {
                    return MapMessage.errorMessage("管理员,市场人员,运营人员手机号下不允许存在门店");
                }
                result = hydraCorsairSupport.changeBookStoresMizarId(mizarUserId, toUser.getId());
            } else {
                return MapMessage.errorMessage("修改出现错误");
            }
            if (result) {
                return MapMessage.successMessage("修改成功");
            } else {
                return MapMessage.errorMessage("存在重复名称的门店，修改失败");
            }
        }
    }


    public String createBookStoreMizarUser(String mobile, String contactName) {
        MizarUser user;
        MapMessage resultMsg;
        user = new MizarUser();
        user.setAccountName(mobile);
        user.setRealName(contactName);
        user.setMobile(mobile);
        user.setUserComment(contactName);
        user.setPassword(DEFAULT_PASSWORD);

        resultMsg = mizarUserServiceClient.addMizarUser(user);
        if (resultMsg.isSuccess()) {
            user.setId(resultMsg.get("newId").toString());
            Map<String, List<Integer>> groupRoles = new HashMap<>();
            groupRoles.put(DEPARTMENT_ID, Collections.singletonList(MizarUserRoleType.ShopOwner.getId()));
            mizarUserServiceClient.addDepartment(user.getId(), groupRoles);
            return user.getId();
        } else {
            return "";
        }
    }

    @RequestMapping(value = "provinces.vpage")
    @ResponseBody
    public MapMessage provinces() {
        List<ExRegion> allProvinces = regionLoader.loadProvinces();
        List<Map> provinces = allProvinces.stream().map(e -> MapUtils.m("code", e.getCode(), "name", e.getName())).collect(Collectors.toList());
        return MapMessage.successMessage().add("data", MapUtils.m("provinces", provinces));
    }

    @RequestMapping(value = "regionlist.vpage")
    @ResponseBody
    public Map<String, Object> regionList(@RequestParam Integer regionCode) {
        List<ExRegion> allRegionList = new ArrayList<>();
        if (regionCode >= 0) {
            allRegionList = regionLoader.loadChildRegions(regionCode);
        }
        List<Map> regionList = allRegionList.stream().map(e -> MapUtils.m("code", e.getCode(), "name", e.getName())).collect(Collectors.toList());
        return MapMessage.successMessage().add("data", MapUtils.m("regionList", regionList));
    }

    public List<BookStoreBean> convertBookStoreAddMobile(List<BookStoreBean> bookStoreBeanList) {
        List<BookStoreBean> tempList = new ArrayList<>();

        if (CollectionUtils.isEmpty(bookStoreBeanList)) {
            return tempList;
        }
        Set<String> userIds = bookStoreBeanList.stream().map(BookStoreBean::getMizarUserId).collect(Collectors.toSet());
        Set<String> createMizarUserIds = bookStoreBeanList.stream().map(BookStoreBean::getCreateMizarUserId).collect(Collectors.toSet());
        Set<String> sourceMizarUserIds = bookStoreBeanList.stream().map(BookStoreBean::getSourceMizarUserId).collect(Collectors.toSet());
        userIds.addAll(createMizarUserIds);
        userIds.addAll(sourceMizarUserIds);
        if (CollectionUtils.isEmpty(createMizarUserIds)) {
            return tempList;
        }
        Map<String, MizarUser> userMap = mizarUserLoaderClient.loadUsers(userIds);
        for (BookStoreBean bookStoreBean : bookStoreBeanList) {
            String realName = (userMap.get(bookStoreBean.getCreateMizarUserId()) == null) ? "" : userMap.get(bookStoreBean.getCreateMizarUserId()).getRealName();
            String sourceRealName = (userMap.get(bookStoreBean.getSourceMizarUserId()) == null) ? "" : userMap.get(bookStoreBean.getSourceMizarUserId()).getRealName();
            String createUserId = bookStoreBean.getCreateMizarUserId();
            List<MizarDepartment> departments = new ArrayList<>();
            if (StringUtils.isNotEmpty(createUserId)) {
                departments = mizarUserLoaderClient.loadUserDepartments(createUserId, MizarUserRoleType.MizarAdmin);
            }
            String createMobile;
            if (CollectionUtils.isNotEmpty(departments)) {
                createMobile = "0";
            } else {
                createMobile = "";
            }
            Map<String, Object> storeAddressMap = JsonUtils.fromJson(bookStoreBean.getStoreAddress());

            bookStoreBean.setCreateMobile(createMobile);
            bookStoreBean.setStoreAddressMap(storeAddressMap);
            bookStoreBean.setCreateUserName(realName);
            bookStoreBean.setSourceMizarUserName(sourceRealName);
            tempList.add(bookStoreBean);
        }
        return tempList;
    }

    public XueMizarBookStoreOrderRankBean convertOperationBean(XueMizarBookStoreOrderRankBean bean, Pageable pageable) {
        XueMizarBookStoreOrderRankBean rankBean = XueMizarBookStoreOrderRankBean.initRankBean();
        rankBean.setOrderTotalNum(bean.getOrderTotalNum());
        rankBean.setYesterdayOrderNum(bean.getYesterdayOrderNum());
        rankBean.setRecentDaysOrderNum(bean.getRecentDaysOrderNum());
        rankBean.setStoreTotalNum(bean.getStoreTotalNum());
        rankBean.setYesterdayStoreNum(bean.getYesterdayStoreNum());
        rankBean.setOrderNumRanks(bean.getOrderNumRanks());
        if (bean.getReferralRankList() != null) {
            Page<XueMizarBookStoreOrderRankBean.XueMizarBookStoreOrderRank> referralRanks = PageableUtils.listToPage(bean.getReferralRankList(), pageable);
            rankBean.setReferralRanks(referralRanks);
        }
        return rankBean;
    }

    /**
     * 查看手机号信息
     */
    @RequestMapping(value = "searchMobile.vpage")
    @ResponseBody
    public MapMessage searchMobile() {
        String mobile = "";
        MizarAuthUser currentUser = getCurrentUser();
        String userId = getRequestString("userId");
        if (currentUser == null) {
            return MapMessage.errorMessage("登录已过期");
        }
        MizarUser user = mizarUserLoaderClient.loadUser(userId);
        if (user != null) {
            mobile = user.getMobile();
        }
        return MapMessage.successMessage().add("mobile", mobile);
    }

    /**
     * 下载二维码到本地
     */
    @RequestMapping(value = "downloadQrCode.vpage")
    @ResponseBody
    public MapMessage downloadQrCode() {
        List<String> allBDUser = new ArrayList<>();
        MizarAuthUser currentUser = getCurrentUser();
        Long bookStoreId = getRequestLong("bookStoreId");
        if (bookStoreId <= 0L) {
            return MapMessage.errorMessage("bookStoreId不可为空");
        }
        BookStoreBean bookStore = hydraCorsairSupport.loadBookStoreById(bookStoreId);
        if (bookStore == null) {
            return MapMessage.errorMessage("门店不存在");
        }
        Integer userRole = getCurrentUserRole(currentUser);
        if (userRole.equals(MizarUserRoleType.BusinessDevelopmentManager.getId())) {
            allBDUser = getAllBDUser();
        }
        Boolean result = hydraCorsairSupport.checkViewBookStoreByRole(bookStoreId, userRole, currentUser.getUserId(), allBDUser);
        if (!result) {
            return MapMessage.errorMessage("无权限下载信息");
        }
        String qrCode = bookStore.getStoreQrCode();
        String aliStoreQrCode = bookStore.getAliStoreQrCode();
        if (StringUtils.isBlank(qrCode)) {
            return MapMessage.errorMessage("二维码不存在");
        }
        if (StringUtils.isBlank(aliStoreQrCode)){
            aliStoreQrCode = MizarOssManageUtils.uploadBookStore(qrCode);
            bookStore.setAliStoreQrCode(aliStoreQrCode);
            hydraCorsairSupport.addOrEditBookStore(bookStore);
        }
        return MapMessage.successMessage().add("data", MapUtils.m("imgUrl", aliStoreQrCode));
    }

    /**
     * 查看订单明细信息
     */
    @RequestMapping(value = "bookStoreOrderInfo.vpage")
    public String bookStoreOrderInfo(Model model) {
        List<String> allBDUser = new ArrayList();
        Long totalElements = 0L;
        MizarAuthUser user = getCurrentUser();
        List<XueMizarBookStoreOrderInfoBean> orderInfoList = new ArrayList<>();
        long bookStoreId = getRequestLong("bookStoreId");
        Integer userRole = getCurrentUserRole(user);
        if (bookStoreId <= 0L) {
            return "bookstore/bookStoreOrderInfo";
        }
        if (userRole.equals(MizarUserRoleType.BusinessDevelopmentManager.getId())) {
            allBDUser = getAllBDUser();
        }
        Boolean result = hydraCorsairSupport.checkViewBookStoreByRole(bookStoreId, userRole, user.getUserId(), allBDUser);
        if (!result) {
            return "bookstore/bookStoreOrderInfo";
        }

        int page = getRequestInt("page");
        // 最小从1开始
        page = Math.max(1, page);
        Pageable pageRequest = PageableUtils.startFromOne(page, 20);
        XueMizarBookStoreOrderBean orderBean = hydraCorsairSupport.loadOrderInfoByBookStoreId(bookStoreId, page, 20);
        if (orderBean != null) {
            orderInfoList = orderBean.getOrderInfoList();
            totalElements = orderBean.getTotalElements();
        }
        Page<XueMizarBookStoreOrderInfoBean> orderInfoBeanPage = new PageImpl<>(orderInfoList, pageRequest, totalElements);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", orderInfoBeanPage.getTotalPages());
        model.addAttribute("orderInfoBeanPage", orderInfoBeanPage);
        return "bookstore/bookStoreOrderInfo";
    }

    public List<String> getAllBDUser() {
        List<MizarUserDepartment> userDepartmentList = mizarUserLoaderClient.loadUserByDepartmentId(DEPARTMENT_ID);
        List<String> userIds = userDepartmentList.stream().filter(e -> (e.getRoles().contains(MizarUserRoleType.BusinessDevelopment.getId()) || e.getRoles().contains(MizarUserRoleType.BusinessDevelopmentManager.getId()))).map(MizarUserDepartment::getUserId).collect(Collectors.toList());
        return userIds;
    }

    public Integer getCurrentUserRole(MizarAuthUser user) {
        Integer userRole = 0;
        if (user.isAdmin()) {
            userRole = MizarUserRoleType.MizarAdmin.getId();
        } else if (user.isBDManager()) {
            userRole = MizarUserRoleType.BusinessDevelopmentManager.getId();
        } else if (user.isBD()) {
            userRole = MizarUserRoleType.BusinessDevelopment.getId();
        } else if (user.isOperator()) {
            userRole = MizarUserRoleType.Operator.getId();
        } else if (user.isShopOwner()) {
            userRole = MizarUserRoleType.ShopOwner.getId();
        }
        return userRole;
    }

    public void updateMizarUserName(String mizarUserId, String contactName) {
        MizarUser user = mizarUserLoaderClient.loadUser(mizarUserId);
        if (!user.getRealName().equals(contactName)) {
            user.setRealName(contactName);
            mizarUserServiceClient.editMizarUser(user);
        }
    }

    public String findSourceMizarUserId(MizarAuthUser user) {
        String sourceMizarUserId = "";
        if (user.isBDManager() || user.isBD() || user.isOperator() || user.isAdmin()) {
            sourceMizarUserId = user.getUserId();
        } else {
            //找到当前用户下的书店，取最早创建的书店的sourceMizarUserId
            List<BookStoreBean> bookStoreBeanList = hydraCorsairSupport.findBookStoreListByMizarUserId(user.getUserId());
            if (CollectionUtils.isNotEmpty(bookStoreBeanList)) {
                sourceMizarUserId = bookStoreBeanList.get(0).getSourceMizarUserId();
            }
        }
        return sourceMizarUserId;
    }

    public boolean checkEditBookStoreOrMobile(MizarAuthUser user) {
        if (user.isBD() || user.isBDManager() || user.isAdmin() || user.isOperator()) {
            return true;
        } else {
            return false;
        }
    }
}
