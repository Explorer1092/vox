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

package com.voxlearning.utopia.mizar.service.reserve;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.extension.sensitive.codec.SensitiveLib;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.raikou.service.region.api.RaikouRegionClient;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.service.AbstractMizarService;
import com.voxlearning.utopia.mizar.utils.XssfUtils;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarReserveRecord;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopGoods;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 预约信息相关Service.
 * <p>
 * Created by Yuechen.Wang on 16-9-19.
 */
@Named
public class MizarReserveService extends AbstractMizarService {

    @Inject private RaikouSystem raikouSystem;

    private static final String EXCEL_DATE_FORMAT = "yyyy年MM月dd日 hh:mm:ss";

    private String[] titles = new String[]{
            "预约ID", "预约时间", "最后操作时间"
            , "家长ID", "家长姓名", "称呼"
            , "学生ID", "学生姓名", "预约手机号", "年龄", "年级", "学校", "校区", "地区", "预约邮箱"
            , "学校ID", "学校名称", "学校地区编码", "学校所在省", "学校所在市", "学校所在区"
            , "机构ID", "机构名称", "机构地区编码", "机构所在省", "机构所在市", "机构所在区"
            , "课程ID", "课程名称"
            , "预约状态", "预约备注"
    };

    private String[] titles_customer = new String[]{
            "预约ID", "预约时间", "最后操作时间", "学生姓名", "预约手机号", "年龄", "年级", "学校", "校区", "地区"
            ,"机构ID", "机构名称", "课程ID", "课程名称", "预约状态", "预约备注"
    };

    private int[] width = new int[]{
            4000, 8000, 6000, 4000, 4000
            , 4000, 6000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000
            , 6000, 9000, 3000, 3000, 3000, 3000
            , 6000, 9000
            , 4000, 4000
    };

    private int[] width_customer = new int[]{
            4000, 8000, 6000, 4000, 4000, 3000, 3000, 6000, 6000,3000,
            4000, 4000, 6000, 9000, 4000, 4000
    };

    public List<Map<String, Object>> loadShopReservations(Collection<MizarShop> shopList, Map<String, Object> filterMap) {
        if (CollectionUtils.isEmpty(shopList)) {
            return Collections.emptyList();
        }
        Map<String, MizarShop> shopMap = shopList.stream().collect(Collectors.toMap(MizarShop::getId, Function.identity()));
        Map<String, List<MizarReserveRecord>> reservations = mizarLoaderClient.loadShopReservations(shopMap.keySet());
        return mapReserveInfo(shopMap, reservations, filterMap);
    }

    public List<Map<String, Object>> loadRecentShopReservations(Date start, Date end, Map<String, Object> filterMap) {
        if (start == null || end == null || start.after(end)) {
            return Collections.emptyList();
        }
        Map<String, List<MizarReserveRecord>> reservations = mizarLoaderClient.loadReservationsByPeriod(start, end)
                .stream()
                .collect(Collectors.groupingBy(MizarReserveRecord::getShopId));
        Map<String, MizarShop> shopMap = mizarLoaderClient.loadShopByIds(reservations.keySet());
        return mapReserveInfo(shopMap, reservations, filterMap);
    }

    public XSSFWorkbook getDownloadExcel(MizarAuthUser user, Date startDate, Date endDate) throws Exception {
        Date now = new Date();
        if (startDate == null) {
            startDate = DateUtils.calculateDateDay(now, -30);
        }
        if (endDate == null) {
            endDate = now;
        }
        List<MizarReserveRecord> reservations = mizarLoaderClient.loadReservationsByPeriod(startDate, endDate);

        // 进行过滤
        if (!user.isOperator()) {
            List<String> shopList = user.getShopList();
            reservations = reservations.stream()
                    .filter(p -> shopList.contains(p.getShopId()))
                    .collect(Collectors.toList());
        }

        List<List<String>> dataList = mapReserveInfo(user, reservations);
        if (user.isOperator()) {
            return XssfUtils.convertToXSSFWorkbook(titles, width, dataList);
        } else {
            return XssfUtils.convertToXSSFWorkbook(titles_customer, width_customer, dataList);
        }

    }

    private List<Map<String, Object>> mapReserveInfo(Map<String, MizarShop> shopMap,
                                                     Map<String, List<MizarReserveRecord>> reservations,
                                                     Map<String, Object> filterMap) {
        if (MapUtils.isEmpty(shopMap) || MapUtils.isEmpty(reservations)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        Map<Long, StudentDetail> studentMap = new HashMap<>();
        for (Map.Entry<String, MizarShop> entry : shopMap.entrySet()) {
            MizarShop shop = entry.getValue();
            List<MizarReserveRecord> reserveRecords = reservations.get(entry.getKey());
            if (CollectionUtils.isEmpty(reserveRecords)) continue;
            // 根据搜索条件过滤
            reserveRecords = reserveRecords.stream()
                    .filter(record -> filterReserveRecord(record, filterMap))
//                    .sorted((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(reserveRecords)) continue;
            Map<String, MizarShopGoods> shopGoodsMap = mizarLoaderClient.loadShopGoodsByIds(reserveRecords.stream().map(MizarReserveRecord::getShopGoodsId).collect(Collectors.toSet()));
            reserveRecords.forEach(record -> {
                Map<String, Object> info = new HashMap<>();
                info.putAll(shop.simpleInfo());
                info.put("recordId", record.getId());
                info.put("reserveTime", record.getCreateDatetime());
//                Long studentId = record.getStudentId();
//                info.put("studentId", studentId);
//                StudentDetail student;
//                if (studentMap.containsKey(studentId)) {
//                    student = studentMap.get(studentId);
//                } else {
//                    student = studentLoaderClient.loadStudentDetail(record.getStudentId());
//                    studentMap.put(studentId, student);
//                }
//                ClazzLevel clazzLevel = student == null ? null : student.getClazzLevel();
//                info.put("clazzLevel", clazzLevel == null ? "" : clazzLevel.getDescription());
                info.put("studentName", record.getStudentName());
                info.put("callName", record.getCallName());
                info.put("mobile", SensitiveLib.decodeMobile(record.getMobile()));
                info.put("status", record.fetchStatus());
                info.put("age", SafeConverter.toInt(record.getAge()) < 0 ? "" : record.getAge());
                info.put("clazzLevel", SafeConverter.toInt(record.getClazzLevel()) < 0 ? "" : record.getClazzLevel());
                info.put("school", record.getSchool());
                info.put("schoolArea", record.getSchoolArea());
                info.put("notes", record.getNotes());
                MizarShopGoods mizarShopGoods = shopGoodsMap.get(record.getShopGoodsId());
                if (mizarShopGoods != null) {
                    info.put("dealSuccess", mizarShopGoods.featureDealSuccess());
                }
                result.add(info);
            });
        }
        Collections.sort(result, (s1, s2) -> {
            Long time1 = ((Date) s1.get("reserveTime")).getTime();
            Long time2 = ((Date) s2.get("reserveTime")).getTime();
            return Long.compare(time2, time1);
        });
        return result;
    }

    private List<List<String>> mapReserveInfo(MizarAuthUser user, List<MizarReserveRecord> reservations) {
        if (CollectionUtils.isEmpty(reservations)) {
            return Collections.emptyList();
        }
        List<List<String>> result = new ArrayList<>();
        Map<String, MizarShop> shopMap = new HashMap<>();
        MizarShop shop;
        Map<String, MizarShopGoods> goodsMap = new HashMap<>();
        MizarShopGoods goods;
        Map<Long, User> userMap = new HashMap<>();
        User parent;
        Map<Long, School> schoolMap = new HashMap<>();
        School school;
        ExRegion region;

        for (MizarReserveRecord record : reservations) {
            List<String> line = new ArrayList<>();
            line.add(record.getId().toString()); // ID
            line.add(DateUtils.dateToString(record.getCreateDatetime(), EXCEL_DATE_FORMAT)); // 创建时间
            line.add(DateUtils.dateToString(record.getUpdateDatetime(), EXCEL_DATE_FORMAT)); // 更新时间

            if (user.isOperator()) {
                Long parentId = SafeConverter.toLong(record.getParentId());
                line.add(parentId.toString()); // 家长ID
                if (userMap.containsKey(parentId)) {
                    parent = userMap.get(parentId);
                } else {
                    parent = raikouSystem.loadUser(parentId);
                    userMap.put(parentId, parent);
                }
                line.add(parent == null ? "-" : parent.getProfile().getRealname());// 家长姓名
                line.add(record.getCallName());// 称呼

                line.add(record.getStudentId().toString()); // 学生ID
            }
            line.add(record.getStudentName()); // 学生姓名
            line.add(SensitiveLib.decodeMobile(record.getMobile())); // 预约手机号
            line.add(SafeConverter.toString(record.getAge()));    // 年龄
            line.add(SafeConverter.toString(record.getClazzLevel()));
            line.add(record.getSchool());
            line.add(record.getSchoolArea());
            if (record.getRegionId() != null && record.getRegionId() > 0) {
                ExRegion exRegion = raikouSystem.loadRegion(record.getRegionId());
                if (exRegion != null) {
                    line.add(exRegion.getCityName() + ":" + exRegion.getName());
                }
            } else {
                line.add("");
            }
            if (user.isOperator()) {
                line.add(record.getEmail()); // 预约邮箱

                Long schoolId = SafeConverter.toLong(record.getSchoolId());
                line.add(schoolId.toString()); // 学校ID
                if (schoolMap.containsKey(schoolId)) {
                    school = schoolMap.get(schoolId);
                } else {
                    school = raikouSystem.loadSchool(schoolId);
                    schoolMap.put(schoolId, school);
                }
                region = null;
                if (school != null) {
                    region = raikouSystem.loadRegion(school.getRegionCode());
                }
                line.add(school == null ? "-" : school.getCname()); // 学校名称
                line.add(school == null ? "-" : SafeConverter.toString(school.getRegionCode())); // 学校RegionCode
                line.add(region == null ? "-" : region.getProvinceName()); // 学校所在省
                line.add(region == null ? "-" : region.getCityName()); // 学校所在市
                line.add(region == null ? "-" : region.getCountyName()); // 学校所在区
            }

            String shopId = SafeConverter.toString(record.getShopId());
            line.add(shopId); // 机构ID
            if (shopMap.containsKey(shopId)) {
                shop = shopMap.get(shopId);
            } else {
                shop = mizarLoaderClient.loadShopById(shopId);
                shopMap.put(shopId, shop);
            }
            region = null;
            if (shop != null) {
                region = raikouSystem.loadRegion(shop.getRegionCode());
            }
            line.add(shop == null ? "-" : shop.getFullName()); // 机构名称

            if (user.isOperator()) {
                line.add(shop == null ? "-" : SafeConverter.toString(shop.getRegionCode())); // 机构RegionCode
                line.add(region == null ? "-" : region.getProvinceName()); // 机构所在省
                line.add(region == null ? "-" : region.getCityName()); // 机构所在市
                line.add(region == null ? "-" : region.getCountyName()); // 机构所在区
            }

            String goodsId = SafeConverter.toString(record.getShopGoodsId());
            line.add(goodsId); // 课程ID
            if (goodsMap.containsKey(goodsId)) {
                goods = goodsMap.get(goodsId);
            } else {
                goods = mizarLoaderClient.loadShopGoodsById(goodsId);
                goodsMap.put(goodsId, goods);
            }

            // 机构业主看到预约信息的话，如果有支付，那么不展示未支付的预约数据
            if (!user.isOperator() && goods != null && goods.getPrice() > 0) {
                if (record.getStatus() == MizarReserveRecord.Status.New) {
                    continue;
                }
            }

            line.add(goods == null ? "-" : goods.getGoodsName()); // 课程名称
            line.add(record.fetchStatus()); // 预约状态
            line.add(record.getNotes()); // 商家备注

            result.add(line);
        }
        return result;
    }

    private boolean filterReserveRecord(MizarReserveRecord record, Map<String, Object> filterMap) {
        if (record == null) return false;
        if (record.getStatus() == MizarReserveRecord.Status.New) return false;
        if (MapUtils.isEmpty(filterMap)) return true;
        String studentFilter = SafeConverter.toString(filterMap.get("studentName"));
        if (StringUtils.isNotBlank(studentFilter) && StringUtils.isNotBlank(record.getStudentName()) && !record.getStudentName().contains(studentFilter)) {
            return false;
        }
        String mobileFilter = SafeConverter.toString(filterMap.get("mobile"));
        if (StringUtils.isNotBlank(mobileFilter) && !StringUtils.equals(mobileFilter, SensitiveLib.decodeMobile(record.getMobile()))) {
            return false;
        }
        String statusFilter = SafeConverter.toString(filterMap.get("status"));
        return StringUtils.isBlank(statusFilter) || StringUtils.equals(statusFilter, record.getStatus().name());
    }

    public static void main(String[] args) {
        System.out.println(SensitiveLib.decodeMobile("13240239065"));
    }
}
