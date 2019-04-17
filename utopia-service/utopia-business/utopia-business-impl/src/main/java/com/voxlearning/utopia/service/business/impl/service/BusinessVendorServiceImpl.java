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

package com.voxlearning.utopia.service.business.impl.service;
import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObjectLoader;
import com.voxlearning.alps.spi.cache.KeyGenerator;
import com.voxlearning.utopia.api.constant.AppSystemType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.business.api.BusinessVendorService;
import com.voxlearning.utopia.business.api.constant.AppUseNumCalculateType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.business.impl.athena.AppUsingNumServiceClient;
import com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.VendorAppFilterContext;
import com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.VendorAppFilterManager;
import com.voxlearning.utopia.service.business.impl.support.BusinessCacheSystem;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductStatus;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.consumer.FairylandLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.utopia.temp.ApplePayParent;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.AppSystemType.ANDROID;
import static com.voxlearning.utopia.api.constant.AppSystemType.IOS;
import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;

/**
 * @author Summer Yang
 * @version 0.1
 * @since 2016/4/12
 */
@Named
@ExposeService(interfaceClass = BusinessVendorService.class)
public class BusinessVendorServiceImpl extends BusinessServiceSpringBean implements BusinessVendorService {

    @Inject private AppUsingNumServiceClient appUsingNumServiceClient;

    @Inject private BusinessCacheSystem businessCacheSystem;

    @Inject
    VendorLoaderClient vendorLoaderClient;
    @Inject
    FairylandLoaderClient fairylandLoaderClient;
    @Inject
    VendorAppFilterManager vendorAppFilterManager;

    @Override
    public List<VendorApps> getStudentPcAvailableApps(Long studentId) {
        if (studentId == null) return Collections.emptyList();

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) return Collections.emptyList();

        VendorAppFilterContext vendorAppFilterContext = VendorAppFilterContext
                .newInstance(studentDetail, null, OperationSourceType.pc, null, null);
        vendorAppFilterManager.getChildrenProcessor().process(vendorAppFilterContext);
        return vendorAppFilterContext.getResultVendorApps();
    }

    @Override
    public List<VendorApps> getStudentMobileAvailableApps(StudentDetail studentDetail, String version, AppSystemType appSystemType) {
        if (studentDetail == null || StringUtils.isBlank(version)) return Collections.emptyList();

        VendorAppFilterContext vendorAppFilterContext = VendorAppFilterContext
                .newInstance(studentDetail, null, OperationSourceType.app, version, appSystemType);

        vendorAppFilterManager.getChildrenProcessor().process(vendorAppFilterContext);

        return vendorAppFilterContext.getResultVendorApps();
    }

    @Override
    public List<VendorApps> getParentAvailableApps(User parent, StudentDetail children) {
        if (parent == null || children == null) return Collections.emptyList();

        VendorAppFilterContext vendorAppFilterContext = VendorAppFilterContext
                .newInstance(children, parent, null, null, null);
        vendorAppFilterManager.getParentProcessor().process(vendorAppFilterContext);
        return vendorAppFilterContext.getResultVendorApps();
    }

    //这个方法返回值的结构被前端h5限定了，只能这样了
    @Override
    public List<Map<String, Object>> getShoppingInfo(Long parentId, OrderProduct product, AppSystemType appSystemType, String version) {
        if (null == product) {
            return Collections.emptyList();
        }

        VendorApps vendorApps = vendorLoaderClient.loadVendor(product.getProductType());
        if (null == vendorApps) {
            return Collections.emptyList();
        }

        String launchUrl = null;
        String orientation = null;
        String browser = null;
        boolean useAppFlag = isUseAppFlag(appSystemType, version, vendorApps);
        if (useAppFlag) {
            FairylandProduct fairylandProduct = fairylandLoaderClient.loadFairylandProducts(FairyLandPlatform.PARENT_APP, null)
                    .stream().filter(p -> Objects.equals(product.getProductType(), p.getAppKey())).findFirst().orElse(null);
            if (fairylandProduct != null
                    && StringUtils.isNotEmpty(fairylandProduct.fetchRedirectUrl(RuntimeMode.current()))
                    && StringUtils.isNotEmpty(version)) {
                launchUrl = fairylandProduct.fetchRedirectUrl(RuntimeMode.current());
                orientation = vendorApps.getOrientation();
                browser = vendorApps.getBrowser();
            }
        }

        Map<String, Object> info = new HashMap<>();
        Map<String, List<Map<String, Object>>> products = new HashMap<>();

        List<UserOrder> userOrders = userOrderLoaderClient.loadUserPaidOrders(product.getProductType(), parentId);
        int status = 0;
        if (CollectionUtils.isNotEmpty(userOrders)) {
            UserOrder userOrder = userOrders.stream().filter(o -> o.getProductId().equals(product.getId())).findFirst().orElse(null);
            if (null != userOrder) {
                status = 1;
            }
        }

        info.put("products", products);
        info.put("status", status);
        info.put("launchUrl", launchUrl);
        info.put("orientation", orientation);
        info.put("browser", browser);

        List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
        List<Map<String, Object>> properties = new ArrayList<>();
        for (OrderProductItem item : orderProductItems) {
            if (products.containsKey(product.getProductType())) {
                properties = products.get(product.getProductType());
            }

            properties.addAll(fillProductItemProperties(vendorApps, product, item, parentId, appSystemType, version));
            products.put(product.getProductType(), properties);
            if (OrderProductServiceType.safeParse(product.getProductType()) == PicListenBook) {
                break;  //只显示产品信息，不显示Item信息，只要一条就够了
            }
        }

        List<Map<String, Object>> infos = new ArrayList<>();
        infos.add(info);

        return infos;
    }

    @Override
    public List<Map<String, Object>> getShoppingInfo(Long studentId, Long parentId, String orderProductServiceType, AppSystemType appSystemType, String version) {
        if ((studentId == null && parentId == null) || orderProductServiceType == null || OrderProductServiceType.safeParse(orderProductServiceType) == Unknown) return Collections.emptyList();

        // 获取VendorApps
        VendorApps vendorApps = vendorLoaderClient.loadVendor(orderProductServiceType);
        if (vendorApps == null) return Collections.emptyList();

        // 获取所有孩子
        Set<Long> cids = new HashSet<>();
        if (studentId != null && studentId != 0L) {
            cids.add(studentId);
        } else {
            cids.addAll(studentLoaderClient.loadParentStudents(parentId).stream()
                    .map(User::getId).filter(t -> t != null).collect(Collectors.toSet()));
        }
        String launchUrl = null;
        String orientation = null;
        String browser = null;
        boolean useAppFlag = isUseAppFlag(appSystemType, version, vendorApps);
        if (useAppFlag) {
            FairylandProduct fairylandProduct = fairylandLoaderClient.loadFairylandProducts(FairyLandPlatform.PARENT_APP, null)
                    .stream().filter(p -> Objects.equals(orderProductServiceType, p.getAppKey())).findFirst().orElse(null);
            if (fairylandProduct != null
                    && StringUtils.isNotEmpty(fairylandProduct.fetchRedirectUrl(RuntimeMode.current()))
                    && StringUtils.isNotEmpty(version)) {
                launchUrl = fairylandProduct.fetchRedirectUrl(RuntimeMode.current());
                orientation = vendorApps.getOrientation();
                browser = vendorApps.getBrowser();
            }
        }
        Map<Long, StudentDetail> children = studentLoaderClient.loadStudentDetails(cids);
        List<Map<String, Object>> infos = new ArrayList<>();
        for (StudentDetail child : children.values()) {
            //毕业班学生直接返回空
            if (child.getClazz() == null || child.getClazz().isTerminalClazz() || !child.getClazz().isPrimaryClazz()) {
                continue;
            }
            Integer level = child.getClazzLevelAsInteger();
            if (level == null || level < 1 || level > 6) continue;

            Map<String, Object> info = new HashMap<>();

            AppPayMapper userAppPaidStatus = userOrderLoaderClient.getUserAppPaidStatus(orderProductServiceType, child.getId());
            int status = userAppPaidStatus == null ? 0 : SafeConverter.toInt(userAppPaidStatus.getAppStatus(), 0);

            info.put("name", child.getProfile().getRealname());
            info.put("img", child.fetchImageUrl());
            info.put("uid", child.getId().toString());
            info.put("products", new HashMap<>());
            info.put("status", status);
            info.put("launchUrl", launchUrl);
            info.put("orientation", orientation);
            info.put("browser", browser);


            if (OrderProductServiceType.safeParse(orderProductServiceType) == Stem101) {
                AppPayMapper stemOrders = userOrderLoaderClient.getUserAppPaidStatus(Stem101.name(), child.getId(), true);
                if (stemOrders != null && CollectionUtils.isNotEmpty(stemOrders.getValidProducts())) {
                    info.put("buyIds", StringUtils.join(stemOrders.getValidProducts(), ","));
                }
            }

            if (OrderProductServiceType.safeParse(orderProductServiceType) == WalkerElf) {
                AppPayMapper walkerOrders = userOrderLoaderClient.getUserAppPaidStatus(WalkerElf.name(), child.getId(), true);
                if (walkerOrders != null && CollectionUtils.isNotEmpty(walkerOrders.getValidProducts())) {
                    info.put("buyIds", StringUtils.join(walkerOrders.getValidProducts(), ","));
                }
            }


            Map<String, List<Map<String, Object>>> products = new HashMap<>();

            List<OrderProduct> productList = userOrderLoaderClient.loadAllOrderProductsByModifyPrice(child);
            productList = productList.stream()
                    .filter(p -> Objects.equals(p.getProductType(), vendorApps.getAppKey()))
                    .collect(Collectors.toList());
            Set<String> productIds = productList.stream()
                    .map(OrderProduct::getId).collect(Collectors.toSet());
            Map<String, List<OrderProductItem>> itemList = userOrderLoaderClient.loadProductItemsByProductIds(productIds);

            for (OrderProduct orderProduct : productList) {
                //错题精讲通过年级与学期进行过滤
                if (OrderProductServiceType.safeParse(orderProduct.getProductType()) == OrderProductServiceType.FeeCourse) {
                    if (StringUtils.isBlank(orderProduct.getAttributes())) {
                        continue;
                    }
                    String[] attributes = orderProduct.getAttributes().split(",");
                    if (attributes.length != 2
                            || SafeConverter.toInt(attributes[0]) != child.getClazz().getClazzLevel().getLevel()
                            || SafeConverter.toInt(attributes[1]) != SchoolYear.newInstance().currentTerm().getKey()) {
                        continue;
                    }
                }
                if (!itemList.containsKey(orderProduct.getId())) {
                    continue;
                }
                // ApplePay 账号处理
                if (isValidProductForApplePay(parentId, orderProduct, appSystemType, version)) {
                    continue;
                }

                // FIXME 暂时是1：1的关系，后期根据实际情况修改
                OrderProductItem productItem = itemList.get(orderProduct.getId()).get(0);

                List<Map<String, Object>> properties = new ArrayList<>();
                if (products.containsKey(orderProductServiceType)) properties = products.get(orderProductServiceType);

                properties.addAll(fillProductItemProperties(vendorApps, orderProduct, productItem, parentId, appSystemType, version));
                products.put(orderProduct.getProductType(), properties);
            }
            info.put("products", products);
            infos.add(info);
        }
        return infos;
    }

    private boolean isValidProductForApplePay(Long parentId, OrderProduct orderProduct, AppSystemType appSystemType, String version) {
        //// FIXME: 2017/2/17 applePay UserCounts
        if (OrderProductServiceType.safeParse(orderProduct.getProductType()) != AfentiExam) {
            return false;
        }
        if (appSystemType != IOS) {
            return false;
        }
        if (!ApplePayParent.getParentIds().contains(parentId)) {
            return false;
        }
        if (VersionUtil.compareVersion(version, "1.9.0") < 0) {
            return false;
        }
        return !Objects.equals(orderProduct.getName(), "阿分题英语30天");
    }

    private List<Map<String, Object>> fillProductItemProperties(VendorApps vendorApps,
                                                                OrderProduct orderProduct,
                                                                OrderProductItem productItem,
                                                                Long parentId,
                                                                AppSystemType appSystemType,
                                                                String version) {
        List<Map<String, Object>> properties = new ArrayList<>();

        String title = vendorApps.getCname();
        if (StringUtils.isNotBlank(title) && StringUtils.isNotBlank(vendorApps.getSubhead()))
            title += ":" + vendorApps.getSubhead();
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("price", orderProduct.getPrice().doubleValue());
        infoMap.put("period", productItem.getPeriod());
        infoMap.put("name", orderProduct.getName());
        infoMap.put("productType", orderProduct.getProductType());
        infoMap.put("productId", orderProduct.getId());
        infoMap.put("title", title);
        infoMap.put("info", vendorApps.getDescription());
        infoMap.put("categoryName", orderProduct.getCategory());
        infoMap.put("appId", productItem.getAppItemId());
        infoMap.put("orignalPrice", orderProduct.getOriginalPrice().doubleValue());
        infoMap.put("attributes", orderProduct.getAttributes());
        infoMap.put("productDesc", orderProduct.getDesc());
        // 这里处理ApplePay账号逻辑
        if (ApplePayParent.getParentIds().contains(parentId) && appSystemType == IOS
                && VersionUtil.compareVersion(version, "1.9.0") >= 0
                && Objects.equals(orderProduct.getName(), "阿分题英语30天")) {
            infoMap.put("price", 30);
            infoMap.put("orignalPrice", orderProduct.getOriginalPrice().doubleValue());
        }
        properties.add(infoMap);

        return properties;
    }

    private boolean isUseAppFlag(AppSystemType appSystemType, String version, VendorApps vendorApps) {
        boolean useAppFlag = false;
        String apVersion = vendorApps.getAndroidParentVersion();
        String iosVersion = vendorApps.getIosParentVersion();
        if (appSystemType != null && appSystemType == ANDROID && (StringUtils.isEmpty(apVersion) || VersionUtil.compareVersion(version, apVersion) >= 0)) {
            useAppFlag = true;
        }
        if (appSystemType != null && appSystemType == IOS && (StringUtils.isEmpty(iosVersion) || VersionUtil.compareVersion(version, iosVersion) >= 0)) {
            useAppFlag = true;
        }
        return useAppFlag;
    }

    // 家长端可用的FairylandProduct
    @Override
    public List<FairylandProduct> getParentAvailableFairylandProducts(User parent, StudentDetail studentDetail,
                                                                      FairyLandPlatform fairyLandPlatform, FairylandProductType fairylandProductType) {
        if (parent == null || studentDetail == null) return Collections.emptyList();

        List<FairylandProduct> fairylandProducts = fairylandLoaderClient.loadFairylandProducts(fairyLandPlatform, fairylandProductType);
        long appProductSize = fairylandProducts.stream().filter((p) -> FairylandProductType.APPS.name().equals(p.getProductType())).count();
        if (appProductSize == 0) {
            return Collections.emptyList();
        }

        List<VendorApps> availableApps = getParentAvailableApps(parent, studentDetail);

        Set<String> availableAppKeys = availableApps.stream()
                .map(VendorApps::getAppKey)
                .collect(Collectors.toSet());

        return fairylandProducts.stream()
                .filter((p) -> (FairylandProductStatus.ONLINE.name().equals(p.getStatus())))
                .filter((p) -> availableAppKeys.contains(p.getAppKey()))
                .collect(Collectors.toList());
    }


    @Override
    public Map<String, String> fetchUserUseNumDesc(List<String> serviceTypes, StudentDetail studentDetail) {
        if (CollectionUtils.isEmpty(serviceTypes) || studentDetail.getClazz() == null) {
            return Collections.emptyMap();
        }
        Map<String, Integer> clazzMap = loadUseNum(AppUseNumCalculateType.CLAZZ, serviceTypes, studentDetail);
        Map<String, Integer> gradeMap = loadUseNum(AppUseNumCalculateType.GRADE, serviceTypes, studentDetail);
        Map<String, Integer> schoolMap = loadUseNum(AppUseNumCalculateType.SCHOOL, serviceTypes, studentDetail);
        Map<String, Integer> nationalMap = loadUseNum(AppUseNumCalculateType.NATION, serviceTypes, studentDetail);

        return serviceTypes.stream()
                .collect(Collectors.toMap(t -> t, serviceType -> {
                    if (clazzMap.containsKey(serviceType) && clazzMap.getOrDefault(serviceType, 0) >= 3) {
                        return getDesc(AppUseNumCalculateType.CLAZZ, clazzMap.getOrDefault(serviceType, 0));
                    } else if (gradeMap.containsKey(serviceType) && gradeMap.getOrDefault(serviceType, 0) >= 3) {
                        return getDesc(AppUseNumCalculateType.GRADE, gradeMap.getOrDefault(serviceType, 0));
                    } else if (schoolMap.containsKey(serviceType) && schoolMap.getOrDefault(serviceType, 0) >= 3) {
                        return getDesc(AppUseNumCalculateType.SCHOOL, schoolMap.getOrDefault(serviceType, 0));
                    } else if (nationalMap.containsKey(serviceType) && nationalMap.getOrDefault(serviceType, 0) >= 3) {
                        return getDesc(AppUseNumCalculateType.NATION, nationalMap.getOrDefault(serviceType, 0));
                    }
                    return "";
                }));
    }

    private String getDesc(AppUseNumCalculateType type, Integer num) {
        String numStr = num < 10000 ? String.valueOf(num) : String.valueOf((num + 5000) / 10000) + "万";
        if (type == AppUseNumCalculateType.CLAZZ) {
            return numStr + "名同班同学在学";
        } else if (type == AppUseNumCalculateType.GRADE) {
            return numStr + "名同年级同学在学";
        } else if (type == AppUseNumCalculateType.SCHOOL) {
            return numStr + "名同校同学在学";
        } else if (type == AppUseNumCalculateType.NATION) {
            return numStr + "名同学在学";
        }
        return "";
    }

    @Override
    public Map<String, Integer> loadUseNum(AppUseNumCalculateType appUseNumCalculateType, List<String> serviceTypes, StudentDetail studentDetail) {
        if (CollectionUtils.isEmpty(serviceTypes) || studentDetail.getClazz() == null) {
            return Collections.emptyMap();
        }
        Long clazzId = studentDetail.getClazz().getId();
        Long schoolId = studentDetail.getClazz().getSchoolId();
        Integer grade = SafeConverter.toInt(studentDetail.getClazz().getClassLevel(), 0);

        CacheObjectLoader.Loader<String, Integer> loader = businessCacheSystem.CBS.flushable
                .getCacheObjectLoader().createLoader(new KeyGenerator<String>() {
                    @Override
                    public String generate(String serviceType) {
                        if (appUseNumCalculateType == AppUseNumCalculateType.CLAZZ) {
                            return CacheKeyGenerator.generateCacheKey("APP_USE_NUM",
                                    new String[]{"st", "ct", "classId"},
                                    new Object[]{serviceType, appUseNumCalculateType.name(), clazzId});
                        } else if (appUseNumCalculateType == AppUseNumCalculateType.GRADE) {
                            return CacheKeyGenerator.generateCacheKey("APP_USE_NUM",
                                    new String[]{"st", "ct", "grade", "schoolId"},
                                    new Object[]{serviceType, appUseNumCalculateType.name(), grade, schoolId});
                        } else if (appUseNumCalculateType == AppUseNumCalculateType.SCHOOL) {
                            return CacheKeyGenerator.generateCacheKey("APP_USE_NUM",
                                    new String[]{"st", "ct", "schoolId"},
                                    new Object[]{serviceType, appUseNumCalculateType.name(), schoolId});
                        } else {
                            return CacheKeyGenerator.generateCacheKey("APP_USE_NUM",
                                    new String[]{"st", "ct"},
                                    new Object[]{serviceType, appUseNumCalculateType.name()});
                        }

                    }
                });
        return loader.loads(serviceTypes)
                .loadsMissed(missedSources -> {
                    try {
                        switch (appUseNumCalculateType) {
                            case CLAZZ:
                                return appUsingNumServiceClient.getAppUsingNumService().queryClazz(new ArrayList<>(missedSources), clazzId);
                            case GRADE:
                                return appUsingNumServiceClient.getAppUsingNumService().queryGrade(new ArrayList<>(missedSources), schoolId, grade);
                            case SCHOOL:
                                return appUsingNumServiceClient.getAppUsingNumService().querySchool(new ArrayList<>(missedSources), schoolId);
                            case NATION:
                                return appUsingNumServiceClient.getAppUsingNumService().queryNational(new ArrayList<>(missedSources));
                        }
                    } catch (Exception ext) {
                    }
                    return new HashMap<>();
                }).write(DateUtils.getCurrentToDayEndSecond())
                .getResult();
    }

    @Override
    public Map<String, Integer> loadNationNum(List<String> serviceTypes) {
        CacheObjectLoader.Loader<String, Integer> loader = businessCacheSystem.CBS.flushable.getCacheObjectLoader()
                .createLoader(serviceType -> CacheKeyGenerator.generateCacheKey("APP_USE_NUM",
                        new String[]{"st", "ct"},
                        new Object[]{serviceType, AppUseNumCalculateType.NATION.name()}));
        return loader.loads(serviceTypes).loadsMissed(missedSources -> appUsingNumServiceClient.getAppUsingNumService().queryNational(new ArrayList<>(missedSources)))
                .write(DateUtils.getCurrentToDayEndSecond())
                .getResult();
    }

}
