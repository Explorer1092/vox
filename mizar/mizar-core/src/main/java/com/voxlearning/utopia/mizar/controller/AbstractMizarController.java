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

package com.voxlearning.utopia.mizar.controller;


import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.mizar.auth.HbsAuthUser;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.entity.MizarTreeNode;
import com.voxlearning.utopia.mizar.interceptor.MizarCookieHelper;
import com.voxlearning.utopia.mizar.interceptor.MizarHttpRequestContext;
import com.voxlearning.utopia.mizar.utils.BeanUtils;
import com.voxlearning.utopia.mizar.utils.MizarOssManageUtils;
import com.voxlearning.utopia.service.config.consumer.BadWordCheckerClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarChangeRecordLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarNotifyLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarUserLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarChangeRecordServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarNotifyServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarUserServiceClient;
import com.voxlearning.utopia.service.region.api.entities.Region;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Alex on 2016/08/13.
 */
@Controller
public class AbstractMizarController extends SpringContainerSupport {

    @Inject private RaikouSystem raikouSystem;

    @Inject protected MizarLoaderClient mizarLoaderClient;
    @Inject protected MizarUserLoaderClient mizarUserLoaderClient;
    @Inject protected MizarServiceClient mizarServiceClient;
    @Inject protected MizarUserServiceClient mizarUserServiceClient;
    @Inject protected MizarChangeRecordLoaderClient mizarChangeRecordLoaderClient;
    @Inject protected MizarChangeRecordServiceClient mizarChangeRecordServiceClient;
    @Inject protected MizarNotifyLoaderClient mizarNotifyLoaderClient;
    @Inject protected MizarNotifyServiceClient mizarNotifyServiceClient;
    @Inject protected BadWordCheckerClient badWordCheckerClient;

    @Inject private MizarCookieHelper mizarCookieHelper;

    protected static final long MAXIMUM_UPLOAD_PHOTO_SIZE = 500 * 1024;
    protected static final long MAXIMUM_UPLOAD_FILE_SIZE = 5 * 1024 * 1024;
    protected static final int PAGE_SIZE = 10;
    protected static final String DATE_FORMAT = "yyyy-MM-dd";
    protected static final String MONTH_FORMAT = "yyyy-MM";
    protected static final String DEFAULT_LINE_SEPARATOR = "\n";
    protected static final String MONEY_PATTERN = "^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$";

    private static final String regionTreeCache = "MIZAR_REGION_TREE";

    // =============================================================
    // ===========                基础方法               ============
    // =============================================================
    protected HttpServletRequest getRequest() {
        return HttpRequestContextUtils.currentRequestContext().getRequest();
    }

    protected HttpServletResponse getResponse() {
        return HttpRequestContextUtils.currentRequestContext().getResponse();
    }

    protected boolean isRequestPost() {
        return getRequest().getMethod().equals("POST");
    }

    protected boolean isRequestGet() {
        //then URLEncodedUtils can be used to parse query-string ?
        return getRequest().getMethod().equals("GET");
    }

    protected String redirect(String s) {
        if (s.charAt(0) == '/') {
            MizarHttpRequestContext context = (MizarHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
            return "redirect:" + context.getWebAppContextPath() + s;
        }
        return "redirect:" + s;
    }

    public void setCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        cookie.setMaxAge(DateUtils.getCurrentToMonthEndSecond());
        getResponse().addCookie(cookie);
    }

    // =============================================================
    // ===========           获取String类型参数          ============
    // =============================================================
    // 默认返回 空字符串
    protected String getRequestString(String key) {
        return requestString(key, "");
    }

    // 如果传入为空/空串，返回 null
    protected String requestString(String name) {
        return requestString(name, null);
    }

    // 自定义返回值
    protected String requestString(String name, String iValue) {
        String value = getRequest().getParameter(name);
        return StringUtils.isEmpty(value) ? iValue : value;
    }

    protected List<String> requestStringList(String name) {
        return requestStringList(name, ",");
    }

    private List<String> requestStringList(String name, String sep) {
        String content = getRequestString(name);
        if (StringUtils.isBlank(sep)) {
            return Collections.emptyList();
        }
        return Stream.of(content.split(sep)).map(String::trim).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    // =============================================================
    // ===========          获取Integer类型参数          ============
    // =============================================================
    protected int getRequestInt(String name, int def) {
        return ConversionUtils.toInt(getRequest().getParameter(name), def);
    }

    protected int getRequestInt(String name) {
        return ConversionUtils.toInt(getRequest().getParameter(name));
    }

    protected Integer requestInteger(String name) {
        String value = getRequest().getParameter(name);
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return null;
        }
    }

    // =============================================================
    // ===========            获取Long类型参数           ============
    // =============================================================
    // 默认返回0
    protected long getRequestLong(String name) {
        return ConversionUtils.toLong(getRequest().getParameter(name));
    }

    // 默认返回自定义值，会校验 checkIllegalJsNumber
    protected long getRequestLong(String name, long def) {
        return ConversionUtils.toLong(getRequest().getParameter(name), def);
    }

    // 直接解析，失败返回null
    protected Long requestLong(String name) {
        String value = getRequest().getParameter(name);
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return null;
        }
    }

    // 获取LongSet，默认以英文逗号分隔
    protected Set<Long> requestLongSet(String name) {
        return requestLongSet(name, ",");
    }

    protected Set<Long> requestLongSet(String name, String sep) {
        Set<Long> values = new HashSet<>();
        String[] array = getRequestString(name).split(sep);
        for (String e : array) {
            long value = SafeConverter.toLong(e);
            if (value > 0) {
                values.add(value);
            }
        }
        return values;
    }

    // =============================================================
    // ===========           获取Double类型参数          ============
    // =============================================================
    //
    protected Double getRequestDouble(String name) {
        return ConversionUtils.toDouble(getRequest().getParameter(name), 0D);
    }

    // =============================================================
    // ===========          获取Boolean类型参数          ============
    // =============================================================
    protected Boolean getRequestBool(String name) {
        return ConversionUtils.toBool(getRequest().getParameter(name));
    }

    protected Boolean getRequestBool(String name, Boolean def) {
        if (StringUtils.isNoneBlank(getRequest().getParameter(name))) {
            return ConversionUtils.toBool(getRequest().getParameter(name));
        } else {
            return def;
        }
    }

    // =============================================================
    // ===========       获取java.util.Date类型参数      ============
    // =============================================================
    protected Date getRequestDate(String name) {
        return getRequestDate(name, DATE_FORMAT, null);
    }

    protected Date getRequestMonth(String name) {
        return getRequestDate(name, MONTH_FORMAT, null);
    }

    protected Date getRequestDate(String name, String pattern) {
        return getRequestDate(name, pattern, null);
    }

    protected Date getRequestDate(String name, Date def) {
        return getRequestDate(name, DATE_FORMAT, def);
    }

    protected Date getRequestDate(String name, String pattern, Date def) {
        Date date = null;
        String value = getRequestString(name);
        if (StringUtils.isNotBlank(value)) {
            date = DateUtils.stringToDate(value, pattern);
        }
        return date != null ? date : def;
    }

    // =============================================================
    // ===========            获取用户相关参数           ============
    // =============================================================
    protected MizarAuthUser getCurrentUser() {
        MizarHttpRequestContext context = (MizarHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        return context.getMizarAuthUser();
    }

    protected HbsAuthUser getCurrentHbsUser() {
        MizarHttpRequestContext context = (MizarHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
        return context.getHbsAuthUser();
    }

    protected String currentUserId() {
        MizarAuthUser currentUser = getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUserId();
        }
        Map map = mizarCookieHelper.getCookMapFromCookie(getRequest());
        if (map == null) {
            return null;
        }
        return map.get("userId").toString();
    }

    protected List<String> currentUserShop() {
        MizarAuthUser currentUser = getCurrentUser();
        if (currentUser == null || CollectionUtils.isEmpty(currentUser.getShopList())) {
            return Collections.emptyList();
        }
        return currentUser.getShopList();
    }

    protected List<Long> currentUserSchools() {
        MizarAuthUser currentUser = getCurrentUser();
        if (currentUser == null || CollectionUtils.isEmpty(currentUser.getSchoolList())) {
            return Collections.emptyList();
        }
        return currentUser.getSchoolList();
    }

    protected void setUserToCookie(String userId) {
        Cookie cookieUser = new Cookie("userId", mizarCookieHelper.genCookie(userId));
        if (RuntimeMode.current().ge(Mode.TEST)) {
            cookieUser.setDomain(getRequest().getServerName());
        }
        cookieUser.setPath("/");
        cookieUser.setMaxAge(DateUtils.getCurrentToDayEndSecond());
        getResponse().addCookie(cookieUser);
    }

    protected void removeUserFromCookie() {
        Cookie cookieUser = new Cookie("userId", "");
        if (RuntimeMode.current().ge(Mode.TEST)) {
            cookieUser.setDomain(getRequest().getServerName());
        }
        cookieUser.setPath("/");
        cookieUser.setMaxAge(0);
        getResponse().addCookie(cookieUser);
    }

    // =============================================================
    // ===========                其他方法              ============
    // =============================================================
    protected String $uploadFile(String file) {
        if (!(getRequest() instanceof MultipartHttpServletRequest)) {
            return null;
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
        try {
            MultipartFile inputFile = multipartRequest.getFile(file);
            if (inputFile != null && !inputFile.isEmpty()) {
                return MizarOssManageUtils.upload(inputFile);
            }
        } catch (Exception ex) {
            logger.error("上传失败,msg:{}", ex.getMessage(), ex);
        }
        return null;
    }

    protected MapMessage $uploadFile(String file, long maxSize) {
        if (!(getRequest() instanceof MultipartHttpServletRequest)) {
            return MapMessage.errorMessage("无效的请求!");
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
        try {
            MultipartFile inputFile = multipartRequest.getFile(file);
            if (inputFile == null || inputFile.isEmpty()) {
                return MapMessage.errorMessage("上传文件为空");
            }
            if (inputFile.getSize() > maxSize) {
                return MapMessage.errorMessage("上传文件超出{}KB", maxSize / 1024);
            }
            // 如果有传图片尺寸的话，校验一下图片的尺寸
            int width = getRequestInt("width");
            int height = getRequestInt("height");
            if (width > 0 && height > 0) {
                BufferedImage image = ImageIO.read(inputFile.getInputStream());
                int oriHeight = image.getHeight();
                int oriWidth = image.getWidth();
                if (width != oriWidth || height != oriHeight) {
                    return MapMessage.errorMessage("请上传 {}px×{}px 大小的图片", width, height);
                }
            }
            return MapMessage.successMessage().add("fileName", MizarOssManageUtils.upload(inputFile));
        } catch (Exception ex) {
            logger.error("上传失败,msg:{}", ex.getMessage(), ex);
            return MapMessage.errorMessage("上传失败:" + ex.getMessage());
        }
    }

    protected MapMessage uploadAudioCommon(MultipartFile file) {
        if (file == null)
            return MapMessage.errorMessage("请选择上传的音频!");

        // 校验大小，不能超过10M
        long audioSize = file.getSize();
        if (audioSize > 10 * 1000 * 1000) {
            return MapMessage.errorMessage("上传的音频不能超过10M!");
        }

        try {
            String fileName = MizarOssManageUtils.upload(file);
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("素材保存失败！");
            }
            if (MizarOssManageUtils.invalidFile.equals(fileName)) {
                return MapMessage.errorMessage("仅支持mp3格式的文件上传！");
            }
            return MapMessage.successMessage().add("fileName", fileName);
        } catch (Exception ex) {
            logger.error("Failed to upload img, ex={}", ex);
            return MapMessage.errorMessage("上传素材失败：" + ex.getMessage());
        }
    }

    public <T> List<List<T>> splitList(List<T> source, int size) {
        Validate.isTrue(size > 0);
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer, List<T>> dest = new LinkedHashMap<>();
        int cursor = 0;
        for (T element : source) {
            int index = cursor / size;
            if (dest.containsKey(index)) {
                dest.get(index).add(element);
            } else {
                List<T> list = new ArrayList<>();
                list.add(element);
                dest.put(index, list);
            }
            cursor++;
        }
        return new ArrayList<>(dest.values());
    }

    protected <T> T requestEntity(Class<T> entityClass) {
        if (entityClass == null) {
            return null;
        }
        T entity = null;
        try {
            entity = entityClass.newInstance();
            BeanUtils.getInstance().copy(entity, getRequest().getParameterMap());
        } catch (Exception ignored) {
        }
        return entity;
    }

    // 获取师资力量
    protected List<Map<String, Object>> getTeachersInfo() {
        String[] tName = getRequest().getParameterValues("tName");
        String[] tSeniority = getRequest().getParameterValues("tSeniority");
        String[] tCourse = getRequest().getParameterValues("tCourse");
        String[] tIntroduction = getRequest().getParameterValues("tIntroduction");
        String[] tPhoto = getRequest().getParameterValues("tPhoto");
        List<Map<String, Object>> teacherInfoList = new ArrayList<>();
        if (null != tSeniority && tSeniority.length > 0) {
            int size = tSeniority.length;
            for (int i = 0; i < size; i++) {
                Map<String, Object> infoMap = new HashMap<>();
                infoMap.put("name", tName[i]);//名称
                infoMap.put("experience", tSeniority[i]);//教龄
                infoMap.put("course", tCourse[i]);
                infoMap.put("description", tIntroduction[i]);//描述
                infoMap.put("photo", tPhoto[i]);
                teacherInfoList.add(infoMap);
            }
        }
        return teacherInfoList;
    }

    // 获取区域树
    public List<MizarTreeNode> buildRegionTree(Collection<Integer> regions) {
        Map<String, MizarTreeNode> allRegionTree = buildAllRegionTree();
        regions = CollectionUtils.toLinkedHashSet(regions);
        if (CollectionUtils.isNotEmpty(regions)) {
            for (Integer regionCode : regions) {
                MizarTreeNode regionInfo = allRegionTree.get(String.valueOf(regionCode));
                regionInfo.setSelected(true);
                allRegionTree.put(String.valueOf(regionCode), regionInfo);
            }
        }
        List<MizarTreeNode> retList = new ArrayList<>();

        Set<String> allKeySet = allRegionTree.keySet();
        for (String regionCode : allKeySet) {
            MizarTreeNode node = allRegionTree.get(regionCode);
            if (node.getCode() == null) {
                retList.add(node);
            }
        }
        return retList;
    }

    // 构建地区树
    private Map<String, MizarTreeNode> buildAllRegionTree() {
        CacheObject<Map<String, MizarTreeNode>> cacheObject = CacheSystem.CBS.getCache("flushable").get(regionTreeCache);
        if (cacheObject != null && cacheObject.getValue() != null) {
            return cacheObject.getValue();
        }
        List<Region> regions = new ArrayList<>(raikouSystem.getRegionBuffer().loadAllRegions().values());
        Map<String, MizarTreeNode> retMap = new HashMap<>();
        for (Region region : regions) {
            MizarTreeNode node = new MizarTreeNode();
            node.setTitle(region.getName());
            node.setKey(String.valueOf(region.getCode()));
            if (region.getPcode() != 0) {
                node.setCode(String.valueOf(region.getPcode()));
            }
            node.setChildren(new ArrayList<>());
            retMap.put(String.valueOf(region.getCode()), node);
        }

        // 第二次循环，根据Id和ParentID构建父子关系
        for (Region region : regions) {
            Integer pcode = region.getPcode();
            if (pcode == 0) {
                continue;
            }
            MizarTreeNode parentNode = retMap.get(String.valueOf(pcode));
            MizarTreeNode childNode = retMap.get(String.valueOf(region.getCode()));

            // 如果父节点存在，将此结点加入到父结点的子节点中
            if (parentNode != null) {
                List<MizarTreeNode> children = parentNode.getChildren();
                if (!children.contains(childNode)) {
                    children.add(childNode);
                }
            }
        }
        CacheSystem.CBS.getCache("flushable").set(regionTreeCache, DateUtils.getCurrentToDayEndSecond(), retMap);
        return retMap;
    }
}
