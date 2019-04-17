package com.voxlearning.utopia.agent.service;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.HssfUtils;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.bean.tag.AgentTagTargetData;
import com.voxlearning.utopia.agent.constants.AgentTagSubType;
import com.voxlearning.utopia.agent.constants.AgentTagTargetType;
import com.voxlearning.utopia.agent.constants.AgentTagType;
import com.voxlearning.utopia.agent.constants.AgentTargetType;
import com.voxlearning.utopia.agent.dao.mongo.AgentTargetTagDao;
import com.voxlearning.utopia.agent.dao.mongo.tag.AgentTagTargetDao;
import com.voxlearning.utopia.agent.persist.AgentTagPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentTargetTag;
import com.voxlearning.utopia.agent.persist.entity.tag.AgentTag;
import com.voxlearning.utopia.agent.persist.entity.tag.AgentTagTarget;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.support.AgentSchoolSupport;
import com.voxlearning.utopia.agent.support.AgentTeacherSupport;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class AgentTagService extends AbstractAgentService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final Collator pinYinComparator = Collator.getInstance(Locale.CHINA);

    @Inject private RaikouSystem raikouSystem;
    @Inject
    private AgentTagPersistence agentTagPersistence;
    @Inject
    private AgentDictSchoolService agentDictSchoolService;
    @Inject
    private AgentTeacherSupport agentTeacherSupport;
    @Inject
    private AgentTagTargetDao agentTagTargetDao;
    @Inject
    private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject
    private AgentSchoolSupport agentSchoolSupport;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private AgentTargetTagDao agentTargetTagDao;

    private static final Integer COVER_NUM_ADD = 1;
    private static final Integer COVER_NUM_SUBTRACT = 2;

    public List<Map<String, Object>> getAllTagList() {
        List<AgentTag> tagList = agentTagPersistence.loadAll();
        tagList = tagList.stream().sorted(Comparator.comparing(AgentTag::getCreateDatetime).reversed()).collect(Collectors.toList());
        return transformTagList(tagList);
    }

    public List<Map<String, Object>> transformTagList(List<AgentTag> tagList) {
        if (CollectionUtils.isEmpty(tagList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        tagList.forEach(item -> {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("id", item.getId());
            dataMap.put("tagType", item.getTagType());
            dataMap.put("tagSubTypeCode", item.getTagSubType() != null ? item.getTagSubType().getCode() : null);
            dataMap.put("tagSubTypeDesc", item.getTagSubType() != null ? item.getTagSubType().getDesc() : "");
            dataMap.put("name", item.getName());
            dataMap.put("coverNum", item.getCoverNum());
            dataMap.put("isVisible", item.getIsVisible());
            dataMap.put("sortNum", item.getSortNum());
            dataList.add(dataMap);
        });
        return dataList;
    }

    public MapMessage addTag(AgentTagType tagType, AgentTagSubType tagSubType, String name, Boolean isVisible, Integer sortNum) {
        List<AgentTag> tagList = agentTagPersistence.loadByName(name);
        if (CollectionUtils.isNotEmpty(tagList)) {
            return MapMessage.errorMessage("已存在该名称的标签！");
        }
        AgentTag agentTag = new AgentTag();
        agentTag.setName(name);
        agentTag.setTagType(tagType);
        if (tagSubType != null) {
            agentTag.setTagSubType(tagSubType);
        }
        agentTag.setIsVisible(isVisible);
        agentTag.setSortNum(sortNum);
        agentTag.setDisabled(false);
        agentTagPersistence.insert(agentTag);
        return MapMessage.successMessage();
    }


    public MapMessage editTag(Long id, AgentTagType tagType, AgentTagSubType tagSubType, String name, Boolean isVisible, Integer sortNum) {
        if (id == null) {
            return MapMessage.errorMessage("标签ID不正确！");
        }
        AgentTag agentTag = agentTagPersistence.load(id);
        if (agentTag == null) {
            return MapMessage.errorMessage("标签不存在！");
        }
        List<AgentTag> tagList = agentTagPersistence.loadByName(name);
        if (CollectionUtils.isNotEmpty(tagList)) {
            tagList = tagList.stream().filter(p -> !Objects.equals(p.getId(), id)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(tagList)) {
                return MapMessage.errorMessage("已存在该名称的标签！");
            }
        }
        agentTag.setName(name);
        agentTag.setTagType(tagType);
        if (tagSubType != null) {
            agentTag.setTagSubType(tagSubType);
        }
        agentTag.setIsVisible(isVisible);
        agentTag.setSortNum(sortNum);
        agentTag.setDisabled(false);
        agentTagPersistence.upsert(agentTag);
        return MapMessage.successMessage();
    }

    public MapMessage deleteTag(Long id) {
        AgentTag agentTag = agentTagPersistence.load(id);
        if (agentTag == null) {
            return MapMessage.errorMessage("标签不存在！");
        }
        //删除关联对象
        List<AgentTagTarget> tagTargetList = agentTagTargetDao.loadByTagId(id);
        if (CollectionUtils.isNotEmpty(tagTargetList)) {
            Set<String> tagTargetIds = tagTargetList.stream().map(AgentTagTarget::getId).collect(Collectors.toSet());
            agentTagTargetDao.removes(tagTargetIds);
        }
        agentTag.setCoverNum(0);
        agentTag.setDisabled(true);
        agentTagPersistence.upsert(agentTag);
        return MapMessage.successMessage();
    }

    public Map<String, Object> tagDetail(Long id) {
        AgentTag agentTag = agentTagPersistence.load(id);
        if (agentTag == null) {
            return MapMessage.errorMessage("标签不存在！");
        }
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String, Object>> tagList = transformTagList(Collections.singletonList(agentTag));
        if (CollectionUtils.isNotEmpty(tagList)) {
            dataMap = tagList.stream().findFirst().orElse(null);
        }
        return dataMap;
    }

    public MapMessage importTagInfo(XSSFWorkbook workbook, Long tagId) {
        AgentTag tag = agentTagPersistence.load(tagId);
        if (tag == null) {
            return MapMessage.errorMessage("标签不存在！");
        }
        AgentTagType tagType = tag.getTagType();
        List<Long> idList = convertToIdList(workbook);
        Set<Long> existIdList = new HashSet<>();
        List<AgentTagTarget> oldTagTargetList = agentTagTargetDao.loadByTagId(tagId);
        if (CollectionUtils.isNotEmpty(oldTagTargetList)) {
            existIdList.addAll(oldTagTargetList.stream().map(p -> SafeConverter.toLong(p.getTargetId())).collect(Collectors.toSet()));
        }
        MapMessage mapMessage = validateImportTagInfo(idList, existIdList, tagType);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        idList.removeAll(existIdList);
        List<AgentTagTarget> newTagTargetList = new ArrayList<>();
        idList.forEach(id -> {
            AgentTagTarget tagTarget = new AgentTagTarget();
            tagTarget.setTagId(tagId);
            tagTarget.setTargetId(SafeConverter.toString(id));
            if (tagType == AgentTagType.SCHOOL) {
                tagTarget.setTargetType(AgentTagTargetType.SCHOOL);
            } else if (tagType == AgentTagType.TEACHER) {
                tagTarget.setTargetType(AgentTagTargetType.TEACHER);
            }
            newTagTargetList.add(tagTarget);
        });
        agentTagTargetDao.inserts(newTagTargetList);
        //更新覆盖数量
        AlpsThreadPool.getInstance().submit(() -> updateTagCoverNum(Collections.singleton(tagId), SafeConverter.toInt(newTagTargetList.size()), COVER_NUM_ADD));
        return MapMessage.successMessage();
    }

    private List<Long> convertToIdList(XSSFWorkbook workbook) {
        List<Long> idList = new ArrayList<>();
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        int rows = 1;
        if (sheet != null) {
            while (true) {
                try {
                    XSSFRow row = sheet.getRow(rows++);
                    if (row == null) {
                        break;
                    }
                    Long id = XssfUtils.getLongCellValue(row.getCell(0));
                    idList.add(id);
                } catch (Exception ex) {
                    logger.error("read excel failed", ex);
                    break;
                }
            }
        }
        return idList;
    }

    private MapMessage validateImportTagInfo(Collection<Long> idList, Collection<Long> existIdList, AgentTagType tagType) {
        MapMessage resultMessage = MapMessage.errorMessage();
        List<String> errorInfoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(idList)) {
            int rows = 1;
            List<Long> tempIdList = new ArrayList<>();
            if (tagType == AgentTagType.SCHOOL) {
                Map<Long, CrmSchoolSummary> schoolSummaryMap = agentDictSchoolService.batchLoadCrmSchoolSummaryAndSchool(idList);
                for (Long id : idList) {
                    rows++;
                    if (id == null) {
                        errorInfoList.add(rows + "行学校ID错误！ID为：" + id);
                        continue;
                    }
                    if (tempIdList.contains(id)) {
                        errorInfoList.add(rows + "行学校ID重复！ID为：" + id);
                        continue;
                    } else {
                        tempIdList.add(id);
                    }
                    if (!schoolSummaryMap.containsKey(id) || schoolSummaryMap.get(id) == null) {
                        errorInfoList.add(rows + "行学校不存在！ID为：" + id);
                        continue;
                    }
                    if (existIdList.contains(id)) {
                        errorInfoList.add(rows + "行学校已关联该标签！ID为：" + id);
                        continue;
                    }
                }
            } else if (tagType == AgentTagType.TEACHER) {
                Map<Long, CrmTeacherSummary> schoolSummaryMap = agentTeacherSupport.batchLoadCrmTeacherSummaryAndTeacher(idList);
                for (Long id : idList) {
                    rows++;
                    if (id == null) {
                        errorInfoList.add(rows + "行老师ID错误！ID为：" + id);
                        continue;
                    }
                    if (tempIdList.contains(id)) {
                        errorInfoList.add(rows + "行老师ID重复！ID为：" + id);
                        continue;
                    } else {
                        tempIdList.add(id);
                    }
                    if (!schoolSummaryMap.containsKey(id) || schoolSummaryMap.get(id) == null) {
                        errorInfoList.add(rows + "行老师不存在！ID为：" + id);
                        continue;
                    }
                    if (existIdList.contains(id)) {
                        errorInfoList.add(rows + "行老师已关联该标签！ID为：" + id);
                        continue;
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(errorInfoList)) {
                resultMessage.put("errorInfoList", errorInfoList);
            } else {
                resultMessage = MapMessage.successMessage();
            }
        }
        return resultMessage;
    }


    public List<AgentTagTargetData> getTagTargetDataList(Long tagId) {
        List<AgentTagTargetData> tagTargetDataList = new ArrayList<>();
        if (tagId == 0L) {
            return Collections.emptyList();
        }
        AgentTag tag = agentTagPersistence.load(tagId);
        if (tag == null) {
            return Collections.emptyList();
        }
        List<AgentTagTarget> tagTargetList = agentTagTargetDao.loadByTagId(tagId);
        if (CollectionUtils.isEmpty(tagTargetList)) {
            return Collections.emptyList();
        }
        AgentTagType tagType = tag.getTagType();
        Map<Long, CrmTeacherSummary> teacherSummaryMap = new HashMap<>();
        Map<Long, School> teacherSchoolMap = new HashMap<>();
        Set<Long> schoolIds = new HashSet<>();
        if (tagType == AgentTagType.SCHOOL) {
            schoolIds.addAll(tagTargetList.stream().map(p -> SafeConverter.toLong(p.getTargetId())).collect(Collectors.toSet()));
        } else if (tagType == AgentTagType.TEACHER) {
            //老师信息
            Set<Long> teacherIds = tagTargetList.stream().map(p -> SafeConverter.toLong(p.getTargetId())).collect(Collectors.toSet());
            teacherSummaryMap.putAll(agentTeacherSupport.batchLoadCrmTeacherSummaryAndTeacher(teacherIds));
            //老师的学校信息
            teacherSchoolMap.putAll(asyncTeacherServiceClient.getAsyncTeacherService()
                    .loadTeacherSchools(teacherIds)
                    .getUninterruptibly());
            schoolIds.addAll(teacherSchoolMap.values().stream().map(School::getId).collect(Collectors.toSet()));
        }

        //学校信息
        Map<Long, CrmSchoolSummary> schoolSummaryMap = agentSchoolSupport.batchLoadCrmSchoolSummaryAndSchool(schoolIds);
        //组装学校区域信息
        Set<Integer> countyCodes = schoolSummaryMap.values().stream().map(CrmSchoolSummary::getCountyCode).collect(Collectors.toSet());
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(countyCodes);
        schoolSummaryMap.forEach((k, v) -> {
            ExRegion exRegion = exRegionMap.get(v.getCountyCode());
            if (exRegion != null) {
                v.setProvinceName(exRegion.getProvinceName());
                v.setCityName(exRegion.getCityName());
                v.setCountyName(exRegion.getCountyName());
            }
        });

        //学校人员信息
        Map<Long, List<AgentUserSchool>> schoolUserMap = baseOrgService.getUserSchoolBySchools(schoolIds);

        //人员信息
        Map<Long, AgentUser> userMap = new HashMap<>();
        Set<Long> userIds = schoolUserMap.values().stream().flatMap(List::stream).map(AgentUserSchool::getUserId).collect(Collectors.toSet());
        List<AgentUser> userList = baseOrgService.getUsers(userIds);
        if (CollectionUtils.isNotEmpty(userList)) {
            userMap.putAll(userList.stream().collect(Collectors.toMap(AgentUser::getId, Function.identity())));
        }

        //人员与部门信息
        Map<Long, List<AgentGroup>> userGroups = baseOrgService.getUserGroups(userIds);

        tagTargetList.forEach(item -> {
            AgentTagTargetData tagTargetData = new AgentTagTargetData();
            tagTargetData.setId(item.getId());
            tagTargetData.setTagType(tag.getTagType());
            tagTargetData.setTagName(tag.getName());

            CrmSchoolSummary schoolSummary = null;
            if (tagType == AgentTagType.SCHOOL) {
                schoolSummary = schoolSummaryMap.get(SafeConverter.toLong(item.getTargetId()));

            } else if (tagType == AgentTagType.TEACHER) {
                School school = teacherSchoolMap.get(SafeConverter.toLong(item.getTargetId()));
                if (school != null) {
                    schoolSummary = schoolSummaryMap.get(school.getId());
                }

                CrmTeacherSummary teacherSummary = teacherSummaryMap.get(SafeConverter.toLong(item.getTargetId()));
                if (teacherSummary != null) {
                    tagTargetData.setTeacherId(teacherSummary.getTeacherId());
                    tagTargetData.setTeacherName(teacherSummary.getRealName());
                }
            }

            if (schoolSummary != null) {
                tagTargetData.setSchoolId(schoolSummary.getSchoolId());
                tagTargetData.setSchoolName(schoolSummary.getSchoolName());
                tagTargetData.setSchoolLevel(schoolSummary.getSchoolLevel());
                tagTargetData.setProvinceName(schoolSummary.getProvinceName());
                tagTargetData.setCityName(schoolSummary.getCityName());
                tagTargetData.setCountyCode(schoolSummary.getCountyCode());
                tagTargetData.setCountyName(schoolSummary.getCountyName());
                List<AgentUserSchool> userSchoolList = schoolUserMap.get(schoolSummary.getSchoolId());
                if (CollectionUtils.isNotEmpty(userSchoolList)) {
                    AgentUserSchool userSchool = userSchoolList.stream().findFirst().orElse(null);
                    if (userSchool != null) {
                        AgentUser user = userMap.get(userSchool.getUserId());
                        if (user != null) {
                            tagTargetData.setUserName(user.getRealName());
                        }
                        List<AgentGroup> groupList = userGroups.get(userSchool.getUserId());
                        if (CollectionUtils.isNotEmpty(groupList)) {
                            AgentGroup group = groupList.stream().findFirst().orElse(null);
                            if (group != null) {
                                tagTargetData.setGroupName(group.getGroupName());
                            }
                        }
                    }
                }
            }

            tagTargetDataList.add(tagTargetData);
        });
        return tagTargetDataList;
    }

    public void exportTagData(SXSSFWorkbook workbook, List<AgentTagTargetData> dataList) {
        try {
            Sheet sheet = workbook.createSheet("标签");
            sheet.createFreezePane(0, 1, 0, 1);
            Font font = workbook.createFont();
            font.setFontName("宋体");
            font.setFontHeightInPoints((short) 10);
            CellStyle firstRowStyle = workbook.createCellStyle();
            firstRowStyle.setFont(font);
            firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
            Row firstRow = sheet.createRow(0);
            HssfUtils.setCellValue(firstRow, 0, firstRowStyle, "标签类别");
            HssfUtils.setCellValue(firstRow, 1, firstRowStyle, "标签名称");
            HssfUtils.setCellValue(firstRow, 2, firstRowStyle, "学校ID");
            HssfUtils.setCellValue(firstRow, 3, firstRowStyle, "学校名称");
            HssfUtils.setCellValue(firstRow, 4, firstRowStyle, "老师ID");
            HssfUtils.setCellValue(firstRow, 5, firstRowStyle, "老师姓名");
            HssfUtils.setCellValue(firstRow, 6, firstRowStyle, "阶段");
            HssfUtils.setCellValue(firstRow, 7, firstRowStyle, "省份");
            HssfUtils.setCellValue(firstRow, 8, firstRowStyle, "城市");
            HssfUtils.setCellValue(firstRow, 9, firstRowStyle, "地区");
            HssfUtils.setCellValue(firstRow, 10, firstRowStyle, "部门");
            HssfUtils.setCellValue(firstRow, 11, firstRowStyle, "负责人");

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            if (CollectionUtils.isNotEmpty(dataList)) {
                Integer index = 1;
                for (AgentTagTargetData data : dataList) {
                    Row row = sheet.createRow(index++);
                    List<Object> exportAbleData = data.getExportAbleData();
                    if (CollectionUtils.isNotEmpty(exportAbleData)) {
                        for (int i = 0; i < exportAbleData.size(); i++) {
                            Object object = exportAbleData.get(i);
                            if (SafeConverter.toLong(object) != 0L || Objects.equals(object, 0)) {
                                HssfUtils.setCellValue(row, i, cellStyle, SafeConverter.toLong(object));
                            } else {
                                HssfUtils.setCellValue(row, i, cellStyle, ConversionUtils.toString(object));
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("error info: ", ex);
            emailServiceClient.createPlainEmail()
                    .body("error info: " + ex)
                    .subject("导出标签表异常【" + RuntimeMode.current().getStageMode() + "】")
                    .to("deliang.che@17zuoye.com")
                    .send();
        }

    }


    public List<Map<String, Object>> getAllProvincePinYin() {
        List<ExRegion> regionList = raikouSystem.getRegionBuffer().loadProvinces();
        Set<ExRegion> rt = new TreeSet<>((o1, o2) -> pinYinComparator.compare(o1.getProvinceName(), o2.getProvinceName()));
        rt.addAll(regionList);
        List<Map<String, Object>> provinces = new ArrayList<>();
        for (ExRegion region : rt) {
            Map<String, Object> province = new HashMap<>();
            province.put("key", region.getCode());
            province.put("value", region.getName());
            provinces.add(province);
        }
        return provinces;
    }

    public List<AgentTagTargetData> tagTargetDataList(Long tagId, Integer provinceCode, Integer cityCode, Integer countyCode) {
        Set<Integer> countyCodeList = new HashSet<>();
        if (provinceCode != 0 && cityCode == 0) {
            List<ExRegion> cityRegions = raikouSystem.getRegionBuffer().loadChildRegions(provinceCode);
            for (Region vo : cityRegions) {
                List<ExRegion> countyRegions = raikouSystem.getRegionBuffer().loadChildRegions(vo.getCode());
                if (CollectionUtils.isNotEmpty(countyRegions)) {
                    countyCodeList.addAll(countyRegions.stream().map(ExRegion::getCountyCode).collect(Collectors.toSet()));
                }

            }
        }
        if (cityCode != 0 && countyCode == 0) {
            List<ExRegion> childRegionList = raikouSystem.getRegionBuffer().loadChildRegions(cityCode);
            if (CollectionUtils.isNotEmpty(childRegionList)) {
                countyCodeList.addAll(childRegionList.stream().map(ExRegion::getCountyCode).collect(Collectors.toSet()));
            }
        }
        if (countyCode != 0) {
            countyCodeList.add(countyCode);
        }

        List<AgentTagTargetData> tagTargetDataList = getTagTargetDataList(tagId);
        if (CollectionUtils.isNotEmpty(countyCodeList)) {
            tagTargetDataList = tagTargetDataList.stream().filter(p -> countyCodeList.contains(p.getCountyCode())).collect(Collectors.toList());
        }
        return tagTargetDataList;
    }

    public MapMessage deleteTagTarget(Long tagId, Collection<String> ids) {
        long num = agentTagTargetDao.removes(ids);
        if (num > 0) {
            //更新覆盖数量
            AlpsThreadPool.getInstance().submit(() -> updateTagCoverNum(Collections.singleton(tagId), SafeConverter.toInt(num), COVER_NUM_SUBTRACT));
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage("删除失败！");
    }

    /**
     * 更新覆盖数量
     *
     * @param tagIds
     * @param num
     * @param flag
     * @return
     */
    public MapMessage updateTagCoverNum(Collection<Long> tagIds, Integer num, Integer flag) {
        Map<Long, AgentTag> tagMap = getTagListByIds(tagIds);
        if (MapUtils.isEmpty(tagMap)) {
            return MapMessage.errorMessage("标签不存在！");
        }
        tagMap.forEach((k, v) -> {
            if (v != null) {
                int coverNum = SafeConverter.toInt(v.getCoverNum());
                int resultCoverNum = 0;
                if (flag.equals(COVER_NUM_ADD)) {
                    resultCoverNum = coverNum + num;
                } else if (flag.equals(COVER_NUM_SUBTRACT)) {
                    if (coverNum >= num) {
                        resultCoverNum = coverNum - num;
                    }
                }
                v.setCoverNum(resultCoverNum);
                agentTagPersistence.upsert(v);
            }
        });
        return MapMessage.successMessage();
    }

    public Map<String, List<AgentTag>> getTagListByTargetIdsAndType(Collection<String> targetIds, AgentTagTargetType targetType, Boolean isVisible) {
        Map<String, List<AgentTagTarget>> tagTargetMap = agentTagTargetDao.loadByTargetIdsAndType(targetIds, targetType);
        if (MapUtils.isEmpty(tagTargetMap)) {
            return Collections.emptyMap();
        }
        Set<Long> tagIds = new ArrayList<>(tagTargetMap.values()).stream().flatMap(List::stream).map(AgentTagTarget::getTagId).collect(Collectors.toSet());
        Map<Long, AgentTag> tagMap = getTagListByIds(tagIds);
        Map<Long, AgentTag> newTagMap = new HashMap<>();
        if (isVisible != null) {
            tagMap.forEach((k, v) -> {
                if (v.getIsVisible() == isVisible) {
                    newTagMap.put(k, v);
                }
            });
        } else {
            newTagMap.putAll(tagMap);
        }

        Map<String, List<AgentTag>> tagListMap = new HashMap<>();
        tagTargetMap.forEach((k, v) -> {
            if (CollectionUtils.isNotEmpty(v)) {
                List<AgentTag> tagList = new ArrayList<>();
                v.forEach(item -> {
                    AgentTag agentTag = newTagMap.get(item.getTagId());
                    if (agentTag != null && agentTag.getIsVisible()) {
                        tagList.add(agentTag);
                    }
                });
                if (CollectionUtils.isNotEmpty(tagList)) {
                    tagListMap.put(k, tagList.stream().sorted(Comparator.comparing(AgentTag::getSortNum)).collect(Collectors.toList()));
                }
            }
        });
        return tagListMap;
    }

    public MapMessage getTeacherPositionTagInfo(Long teacherId) {
        MapMessage mapMessage = MapMessage.successMessage();

        List<AgentTag> allTeacherPositionTagList = agentTagPersistence.loadByTypeAndSubType(AgentTagType.TEACHER, AgentTagSubType.POSITION);
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        // 初中数学老师和高中数学老师没有"学科组长"标签
        if (teacher.isKLXTeacher()) {
            allTeacherPositionTagList = allTeacherPositionTagList.stream().filter(p -> !Objects.equals(p.getName(), "学科组长")).collect(Collectors.toList());
        }
        //排序
        allTeacherPositionTagList = allTeacherPositionTagList.stream().sorted(Comparator.comparing(AgentTag::getSortNum)).collect(Collectors.toList());


        List<AgentTag> teacherPositionTagList = new ArrayList<>();
        Map<String, List<AgentTag>> teacherTagMap = getTagListByTargetIdsAndType(Collections.singletonList(SafeConverter.toString(teacherId)), AgentTagTargetType.TEACHER, true);
        List<AgentTag> teacherTagList = teacherTagMap.get(SafeConverter.toString(teacherId));
        if (CollectionUtils.isNotEmpty(teacherTagList)) {
            teacherPositionTagList = teacherTagList.stream().filter(p -> p.getTagSubType() == AgentTagSubType.POSITION).collect(Collectors.toList());
        }

        List<Map<String, Object>> unSelectedTagList = new ArrayList<>();
        List<Map<String, Object>> selectedTagList = new ArrayList<>();
        for (AgentTag tag : allTeacherPositionTagList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", tag.getId());
            map.put("name", tag.getName());
            if (CollectionUtils.isEmpty(teacherPositionTagList) || (CollectionUtils.isNotEmpty(teacherPositionTagList) && !teacherPositionTagList.contains(tag))) {
                unSelectedTagList.add(map);
            } else {
                selectedTagList.add(map);
            }
        }

        mapMessage.put("unSelectedTagList", unSelectedTagList);
        mapMessage.put("selectedTagList", selectedTagList);
        return mapMessage;
    }

    public MapMessage saveTeacherTags(Long teacherId, Collection<Long> tagIds) {
        Map<Long, AgentTag> tagMap = getTagListByIds(tagIds);
        if (MapUtils.isEmpty(tagMap)) {
            return MapMessage.errorMessage("请选择标签！");
        }
        //获取该老师的职务标签
        Set<Long> positionTagIds = new HashSet<>();
        Map<String, List<AgentTag>> teacherTagMap = getTagListByTargetIdsAndType(Collections.singleton(SafeConverter.toString(teacherId)), AgentTagTargetType.TEACHER, true);
        List<AgentTag> tagList = teacherTagMap.get(SafeConverter.toString(teacherId));
        if (CollectionUtils.isNotEmpty(tagList)) {
            positionTagIds.addAll(tagList.stream().filter(p -> p.getTagSubType() == AgentTagSubType.POSITION).map(AgentTag::getId).collect(Collectors.toSet()));
        }

        //需要删除的标签的对应对象记录
        Set<String> delTagTargetIds = new HashSet<>();
        Map<Long, List<AgentTagTarget>> tagTargetMap = agentTagTargetDao.loadByTagIds(positionTagIds);
        tagTargetMap.forEach((k, v) -> {
            delTagTargetIds.addAll(v.stream().filter(p -> Objects.equals(p.getTargetId(), SafeConverter.toString(teacherId))).map(AgentTagTarget::getId).collect(Collectors.toSet()));
        });

        //需要新加的标签的对应对象记录
        List<AgentTagTarget> addTagTargetList = new ArrayList<>();
        tagIds.forEach(p -> {
            AgentTagTarget tagTarget = new AgentTagTarget();
            tagTarget.setTagId(p);
            tagTarget.setTargetId(SafeConverter.toString(teacherId));
            tagTarget.setTargetType(AgentTagTargetType.TEACHER);
            addTagTargetList.add(tagTarget);
        });

        agentTagTargetDao.removes(delTagTargetIds);
        agentTagTargetDao.inserts(addTagTargetList);
        //更新覆盖数量
        AlpsThreadPool.getInstance().submit(() -> updateTagCoverNum(positionTagIds, 1, COVER_NUM_SUBTRACT));
        AlpsThreadPool.getInstance().submit(() -> updateTagCoverNum(tagIds, 1, COVER_NUM_ADD));
        return MapMessage.successMessage();
    }


    /**
     * 迁移历史数据
     */
    public void moveHistoryData() {
        //迁移标签数据
        List<AgentTag> newTagList = new ArrayList<>();
        List<com.voxlearning.utopia.agent.constants.AgentTag> oldTags = Arrays.asList(com.voxlearning.utopia.agent.constants.AgentTag.values());
        oldTags.forEach(item -> {
            AgentTag agentTag = new AgentTag();
            if (com.voxlearning.utopia.agent.constants.AgentTag.codeOf(item.getCode()) == com.voxlearning.utopia.agent.constants.AgentTag.REJECT) {
                agentTag.setTagType(AgentTagType.NOTIFY);
            } else {
                agentTag.setTagType(AgentTagType.TEACHER);
                agentTag.setTagSubType(AgentTagSubType.POSITION);
            }
            agentTag.setName(item.getDesc());
            agentTag.setCoverNum(0);
            agentTag.setIsVisible(true);
            agentTag.setSortNum(1);
            agentTag.setDisabled(false);
            newTagList.add(agentTag);
        });
        agentTagPersistence.inserts(newTagList);

        //迁移标签对应对象数据
        List<AgentTag> tagList = new ArrayList<>();
        tagList.addAll(agentTagPersistence.loadByType(AgentTagType.NOTIFY));
        tagList.addAll(agentTagPersistence.loadByTypeAndSubType(AgentTagType.TEACHER, AgentTagSubType.POSITION));
        Map<Long, AgentTag> idTagMap = tagList.stream().collect(Collectors.toMap(AgentTag::getId, Function.identity(), (o1, o2) -> o1));
        Map<String, AgentTag> nameTagMap = tagList.stream().collect(Collectors.toMap(AgentTag::getName, Function.identity(), (o1, o2) -> o1));

        List<AgentTagTarget> newTagTargetList = new ArrayList<>();
        List<AgentTargetTag> oldTargetTags = agentTargetTagDao.loadAll();
        if (CollectionUtils.isNotEmpty(oldTargetTags)) {
            oldTargetTags.forEach(item -> {
                List<com.voxlearning.utopia.agent.constants.AgentTag> tags = item.getTags();
                if (CollectionUtils.isNotEmpty(tags)) {
                    tags.forEach(p -> {
                        AgentTag agentTag = nameTagMap.get(p.getDesc());
                        if (agentTag != null) {
                            AgentTagTarget tagTarget = new AgentTagTarget();
                            tagTarget.setTagId(agentTag.getId());
                            tagTarget.setTargetId(SafeConverter.toString(item.getTargetId()));
                            AgentTargetType targetType = item.getTargetType();
                            if (targetType == AgentTargetType.NOTIFIY) {
                                tagTarget.setTargetType(AgentTagTargetType.NOTIFY);
                            } else if (targetType == AgentTargetType.TEACHER) {
                                tagTarget.setTargetType(AgentTagTargetType.TEACHER);
                            }
                            newTagTargetList.add(tagTarget);
                        }
                    });
                }
            });
        }
        if (CollectionUtils.isNotEmpty(newTagTargetList)) {
            agentTagTargetDao.inserts(newTagTargetList);

            //更新标签覆盖数量
            Map<Long, List<AgentTagTarget>> tagTargetMap = newTagTargetList.stream().collect(Collectors.groupingBy(AgentTagTarget::getTagId));
            tagTargetMap.forEach((k, v) -> {
                AgentTag agentTag = idTagMap.get(k);
                if (agentTag != null) {
                    agentTag.setCoverNum(v.size());
                    agentTagPersistence.upsert(agentTag);
                }
            });
        }
    }

    public List<Long> getNotifyTagIdsByName(String tagName) {
        List<Long> tagIds = new ArrayList<>();
        List<com.voxlearning.utopia.agent.persist.entity.tag.AgentTag> notifyTagList = agentTagPersistence.loadByType(AgentTagType.NOTIFY);
        if (CollectionUtils.isNotEmpty(notifyTagList)) {
            tagIds.addAll(notifyTagList.stream().filter(p -> Objects.equals(p.getName(), tagName)).map(com.voxlearning.utopia.agent.persist.entity.tag.AgentTag::getId).collect(Collectors.toSet()));
        }
        return tagIds;
    }

    public List<Map<String, Object>> getTagListByType(AgentTagType tagType, Boolean isVisible) {
        List<AgentTag> tagList = agentTagPersistence.loadByType(tagType);
        if (isVisible != null) {
            tagList = tagList.stream().filter(p -> p.getIsVisible() == isVisible).collect(Collectors.toList());
        }
        return convertTagList(tagList.stream().sorted(Comparator.comparing(AgentTag::getSortNum)).collect(Collectors.toList()));
    }

    public List<Map<String, Object>> convertTagList(List<AgentTag> tagList) {
        if (CollectionUtils.isEmpty(tagList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        tagList.forEach(item -> {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("id", item.getId());
            dataMap.put("name", item.getName());
            dataList.add(dataMap);
        });
        return dataList;
    }

    public Map<Long, AgentTag> getTagListByIds(Collection<Long> ids) {
        Map<Long, AgentTag> dataMap = new HashMap<>();
        Map<Long, AgentTag> tagMap = agentTagPersistence.loads(ids);
        tagMap.forEach((k, v) -> {
            if (!v.getDisabled()) {
                dataMap.put(k, v);
            }
        });
        return dataMap;
    }
}