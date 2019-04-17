package com.voxlearning.utopia.admin.controller.site;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.galaxy.service.achivement.api.AchivementLoader;
import com.voxlearning.galaxy.service.achivement.api.CreditItemLoader;
import com.voxlearning.galaxy.service.achivement.api.CreditItemService;
import com.voxlearning.galaxy.service.achivement.api.constant.BusinessType;
import com.voxlearning.galaxy.service.achivement.api.constant.credit.CreditItemType;
import com.voxlearning.galaxy.service.achivement.api.entity.AchivementLog;
import com.voxlearning.galaxy.service.achivement.api.support.event.CrmCreditContext;
import com.voxlearning.galaxy.service.achivement.api.entity.AchivementAction;
import com.voxlearning.galaxy.service.achivement.api.entity.credit.CreditItem;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.AchivementCreditData;
import com.voxlearning.utopia.admin.data.CreditLogsData;
import com.voxlearning.utopia.admin.data.excel.ClazzCreditExcelModel;
import com.voxlearning.utopia.admin.data.excel.CreditLogExcelModel;
import com.voxlearning.utopia.service.school.api.SchoolLoader;
import com.voxlearning.utopia.service.user.api.UserAggregationLoader;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.DPStudentLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: feng.guo
 * @Date: 2018-12-12
 * @Description: 学分体系管理
 */
@Slf4j
@Controller
@RequestMapping(value = "/site/achivement")
public class AchivementController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = UserAggregationLoader.class)
    private UserAggregationLoader userAggregationLoader;
    @ImportService(interfaceClass = CreditItemLoader.class)
    private CreditItemLoader creditItemLoader;
    @ImportService(interfaceClass = AchivementLoader.class)
    private AchivementLoader achivementLoader;
    @ImportService(interfaceClass = CreditItemService.class)
    private CreditItemService creditItemService;
    @ImportService(interfaceClass = SchoolLoader.class)
    private SchoolLoader schoolLoader;
    @Inject
    private DPStudentLoaderClient dpStudentLoaderClient;

    @RequestMapping(value = "/index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String achivementIndex(Model model) {
        String startDate = getRequestParameter("startDate", null);
        String endDate = getRequestParameter("endDate", null);
        String pageIndex = getRequestParameter("pageNumber", null);
        String itemType = getRequestParameter("itemTypeId", null);

        Date startTime = null;
        if (StringUtils.isNotBlank(startDate)) {
            startTime = getFormatTime(DateUtils.stringToDate(startDate, DateUtils.FORMAT_SQL_DATE), 0);
        }
        Date endTime = null;
        if (StringUtils.isNotBlank(endDate)) {
            endTime = getFormatTime(DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATE), 1);
        }

        //把业务类型转换成list
        List<String> terms = toList(CreditItemType.class).stream()
                .map(type -> CreditItemType.getCreditItemTermByName(type.name()))
                .distinct()
                .collect(Collectors.toList());
        model.addAttribute("terms", terms);

        Map<String, Object> result = queryAchivementActionPage(null==itemType?null:CreditItemType.getCreditItemName(itemType), startTime, endTime, null==pageIndex?1:Integer.parseInt(pageIndex), 10);
        model.addAttribute("achivementlist", result.get("list"));
        model.addAttribute("pageNumber", result.get("pageNumber"));
        model.addAttribute("hasNext", result.get("hasNext"));
        model.addAttribute("hasPrevious", result.get("hasPrevious"));
        model.addAttribute("totalPages", result.get("totalPages"));
        return "/site/achivement/achivementlist";
    }

    @RequestMapping(value = "/add.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String addAchivement(Model model) {
        String id = getRequestString("id");
        CrmCreditContext creditContext = new CrmCreditContext();
        if (StringUtils.isNotBlank(id)) {
            List<CreditItem> creditItemList = creditItemLoader.loadCrmCreditItem(id);
            if (CollectionUtils.isNotEmpty(creditItemList)) {
                CreditItem creditItem = creditItemList.get(0);
                creditContext.setId(creditItem.getId());
                creditContext.setDisable(creditItem.getDisabled());
                creditContext.setItemType(CreditItemType.getCreditItemDesc(creditItem.getType()));
                creditContext.setTerm(CreditItemType.getCreditItemTermByName(creditItem.getType()));
                List<AchivementAction> achivementActionList = achivementLoader.getAchivementAction(creditItem.getActionId());
                if (CollectionUtils.isNotEmpty(achivementActionList)) {
                    AchivementAction achivementAction = achivementActionList.get(0);
                    creditContext.setName(achivementAction.getName());
                    creditContext.setValue(achivementAction.getValue());
                    creditContext.setBussType(achivementAction.getType());
                    creditContext.setSubject(achivementAction.getSubject());
                }
            }
        } else {
            creditContext.setDisable(false);
        }

        //把业务类型转换成list
        List<String> itemTerms = toList(CreditItemType.class).stream()
                .map(type -> CreditItemType.getCreditItemTermByName(type.name()))
                .distinct()
                .collect(Collectors.toList());
        List<String> itemTypes;
        if (StringUtils.isNotBlank(creditContext.getItemType())) {
            itemTypes = CreditItemType.getCreditItermDescByTerm(creditContext.getTerm());
        } else {
            itemTypes = toList(CreditItemType.class).stream()
                    .map(type -> CreditItemType.getCreditItemDesc(type.name()))
                    .collect(Collectors.toList());
        }
        model.addAttribute("itemTerms", itemTerms);
        model.addAttribute("itemTypes", itemTypes);
        model.addAttribute("credit", creditContext);

        return "/site/achivement/addachivement";
    }

    /**
     * 获取行为类型
     */
    @RequestMapping(value = "/type.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<String> searchType() {
        String term = getRequestString("term");
        if (StringUtils.isBlank(term)) {
            return Collections.emptyList();
        }
        List<String> types = CreditItemType.getCreditItermDescByTerm(term);
        return types;
    }

    @RequestMapping(value = "/save.vpage", method = RequestMethod.POST)
    public @ResponseBody MapMessage saveAchivement() {
        String id = getRequestParameter("id", null);
        String name = getRequestParameter("name", null);
        String value = getRequestParameter("value", null);
        String subject = getRequestParameter("subject", null);
        String itemType = getRequestParameter("itemType", null);

        if (StringUtils.isBlank(name) ||
            StringUtils.isBlank(subject) ||
            StringUtils.isBlank(itemType) ||
            null == value) {
            return MapMessage.errorMessage("保存失败");
        }

        if (StringUtils.isBlank(id)) {
            List<CreditItem> creditItemList = creditItemLoader.loadCrmCreditItem(null).stream()
                    .filter(creditItem -> creditItem.getType().equals(CreditItemType.getCreditItemName(itemType)))
                    .collect(Collectors.toList());
            List<AchivementAction> achivementActionList = creditItemList.stream()
                    .filter(creditItem -> CollectionUtils.isNotEmpty(achivementLoader.getAchivementAction(creditItem.getActionId())))
                    .map(creditItem -> achivementLoader.getAchivementAction(creditItem.getActionId()).get(0))
                    .filter(achivementAction -> achivementAction.getSubject().equals(subject))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(achivementActionList)) {
                return MapMessage.errorMessage("保存失败");
            }
        }
        CrmCreditContext creditContext = new CrmCreditContext(id, name, BusinessType.CREDIT.name(), CreditItemType.getCreditItemName(itemType), subject, Double.parseDouble(value), false, new Date(), null);
        creditItemService.saveOrUpdateCreditItem(creditContext, StringUtils.isBlank(id)?"SAVE":"UPDATE");
        return MapMessage.successMessage("保存成功");
    }

    /**
     * 隐藏设置某条学分体系
     * @return
     */
    @RequestMapping(value = "/endisable.vpage", method = RequestMethod.POST)
    public @ResponseBody MapMessage ableAchivement() {
        String id = getRequestParameter("id", null);
        String type = getRequestParameter("type", null);
        if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(type)) {
            List<CreditItem> creditItemList = creditItemLoader.loadCrmCreditItem(id);
            if (CollectionUtils.isEmpty(creditItemList)) {
                return MapMessage.errorMessage("查询不到信息");
            }
            creditItemService.prohibitionCreditItem(id, !SafeConverter.toBoolean(type));
            return MapMessage.successMessage("操作成功");
        }
        return MapMessage.errorMessage("操作失败");
    }

    /**
     * 删除某条学分体系
     */
    @RequestMapping(value = "/del.vpage", method = RequestMethod.POST)
    public @ResponseBody MapMessage delAchivement() {
        String id = getRequestParameter("id", null);
        if (StringUtils.isNotBlank(id)) {
            List<CreditItem> creditItemList = creditItemLoader.loadCrmCreditItem(id);
            if (CollectionUtils.isEmpty(creditItemList)) {
                return MapMessage.errorMessage("查询不到信息");
            }
            creditItemService.removeCreditAchivementContact(id);
            return MapMessage.successMessage("移除成功");
        }
        return MapMessage.errorMessage("移除失败");
    }

    /**
     * 查询学生学分
     */
    @RequestMapping(value = "/search_credit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String searchCreditList(Model model) {
        //学生ID
        Long sid = getRequestLong("sid");
        //查询类型
        Integer status = getRequestInt("status", 0);
        //页码
        Integer pageNumber = getRequestInt("pageNumber", 1);

        //查询学生班级信息
        Clazz clazz = null;
        //查询学生学校信息
        School school = null;
        //查询学生信息
        StudentDetail studentDetail;
        if (null != sid && 0 != sid) {
            studentDetail = dpStudentLoaderClient.loadStudentDetail(sid);
            if (null == studentDetail) {
                return "/site/achivement/creditlist";
            }
            //查询学生班级信息
            clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(sid);
            if (null != clazz) {
                //查询学生学校信息
                school = schoolLoader.loadSchool(clazz.getSchoolId()).getUninterruptibly();
            }
        }

        List<AchivementCreditData> creditDataList = Lists.newArrayList();
        List<User> clazzInfos = userAggregationLoader.loadLinkedClassmatesForSystemClazz(sid);
        if (CollectionUtils.isNotEmpty(clazzInfos)) {
            if (null != clazz) {
                creditDataList = cardingScoreData(clazzInfos, clazz, school, status, sid);
            }
        }

        //总页数
        Integer totalPages = 1;
        List<AchivementCreditData> creditResultList = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(creditDataList)) {
            if (null != status && status == 1) {
                //总条数
                Integer totalSize = creditDataList.size();
                if (totalSize > 20) {
                    //计算多少页
                    Double pageSize = Math.ceil((totalSize / 20) + 0.4);
                    totalPages = pageSize.intValue();
                }
                //根据当前页计算需要显示的条数
                if (pageNumber <= totalPages) {
                    creditResultList = creditDataList.stream()
                            .sorted(Comparator.comparing(AchivementCreditData::getProCredit).reversed())
                            .skip((pageNumber - 1) * 20).limit(20)
                            .collect(Collectors.toList());
                }
            } else {
                creditResultList = creditDataList.stream()
                        .filter(achivementCreditData -> Objects.equals(sid, achivementCreditData.getSid()) && 0 == status)
                        .collect(Collectors.toList());
            }
        }

        model.addAttribute("creditList", creditResultList);
        model.addAttribute("status", status);
        model.addAttribute("sid", 0==sid?null:sid);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrevious", 1==pageNumber?false:true);
        model.addAttribute("hasNext", 1==totalPages?false:pageNumber<totalPages?true:false);

        return "/site/achivement/creditlist";
    }

    /**
     * 梳理学分
     * @param clazzInfos 当前学生班级人数（不包含自己）
     * @param clazz 班级信息
     * @param school 学校信息
     * @param status 0：个人 1：班级
     * @param sid 学生ID
     */
    public List<AchivementCreditData> cardingScoreData(List<User> clazzInfos, Clazz clazz, School school, Integer status, Long sid) {
        //封装学科
        List<Subject> subjectList = ImmutableList.<Subject>builder()
                .add(Subject.ENGLISH)
                .add(Subject.CHINESE)
                .add(Subject.MATH)
                .build();

        List<AchivementCreditData> creditDataList = Lists.newLinkedList();
        if (null != status || 1 == status) {
            creditDataList = clazzInfos.stream()
                    .map(user -> {
                        AchivementCreditData data = new AchivementCreditData();
                        //学生基本信息
                        data.setSid(user.getId());
                        data.setUserName(user.getProfile().getRealname());
                        //学生班级信息
                        if (null != clazz) {
                            data.setCid(clazz.getId());
                            data.setClazzName(clazz.getClassName());
                        }
                        //学生学校信息
                        if (null != school) {
                            data.setScid(school.getId());
                            data.setSchoolName(school.getCname());
                        }
                        //本周学分
                        Double proCredit = achivementLoader.getUserCurrentWeekAchivementValue(BusinessType.CREDIT, user.narrowToUser().getId());
                        Double proCreditFormat = new BigDecimal(null==proCredit?0:proCredit).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        data.setProCredit(proCreditFormat);
                        //本学期学分
                        Double totalCredit = achivementLoader.getUserAchivementValue(BusinessType.CREDIT, user.narrowToUser().getId());
                        Double totalCreditFormat = new BigDecimal(null==totalCredit?0:totalCredit).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        data.setTotalCredit(totalCreditFormat);
                        //本周根据学科分组获的各学科学分
                        subjectList.stream().forEach(subject -> {
                            Double subjectCredit = achivementLoader.getUserCurrentWeekSubjectAchivementValue(BusinessType.CREDIT, subject, user.narrowToUser().getId());
                            if (subject == Subject.ENGLISH) {
                                data.setProEngCredit(new BigDecimal(subjectCredit).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                            }
                            if (subject == Subject.CHINESE) {
                                data.setProChineseCredit(new BigDecimal(subjectCredit).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                            }
                            if (subject == Subject.MATH) {
                                data.setProMathCredit(new BigDecimal(subjectCredit).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                            }
                        });
                        return data;
                    }).collect(Collectors.toList());
        }
        //查询当前学生学分信息
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        AchivementCreditData data = new AchivementCreditData();
        //学生基本信息
        data.setSid(sid);
        data.setUserName(studentDetail.getProfile().getRealname());
        //学生班级信息
        if (null != clazz) {
            data.setCid(clazz.getId());
            data.setClazzName(clazz.getClassName());
        }
        //学生学校信息
        if (null != school) {
            data.setScid(school.getId());
            data.setSchoolName(school.getCname());
        }
        //本周学分
        Double proCredit = achivementLoader.getUserCurrentWeekAchivementValue(BusinessType.CREDIT, sid);
        Double proCreditFormat = new BigDecimal(null==proCredit?0:proCredit).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        data.setProCredit(proCreditFormat);
        //本学期学分
        Double totalCredit = achivementLoader.getUserAchivementValue(BusinessType.CREDIT, sid);
        Double totalCreditFormat = new BigDecimal(null==totalCredit?0:totalCredit).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        data.setTotalCredit(totalCreditFormat);
        //本周根据学科分组获的各学科学分
        subjectList.stream().forEach(subject -> {
            Double subjectCredit = achivementLoader.getUserCurrentWeekSubjectAchivementValue(BusinessType.CREDIT, subject, sid);
            if (subject == Subject.ENGLISH) {
                data.setProEngCredit(new BigDecimal(subjectCredit).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if (subject == Subject.CHINESE) {
                data.setProChineseCredit(new BigDecimal(subjectCredit).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if (subject == Subject.MATH) {
                data.setProMathCredit(new BigDecimal(subjectCredit).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
        });
        creditDataList.add(data);

        return creditDataList;
    }

    /**
     * 查询学生学分详情记录
     */
    @RequestMapping(value = "/credit_logs.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String srarchCreditLogDetails(Model model) {
        //学生ID
        Long sid = getRequestLong("sid");
        //开始时间
        String startDate = getRequestParameter("startDate", null);
        //结束时间
        String endDate = getRequestParameter("endDate", null);
        //页码
        Integer pageNumber = getRequestInt("pageNumber", 1);

        //对日期进行处理
        Date startTime = null;
        if (StringUtils.isNotBlank(startDate)) {
            startTime = getFormatTime(DateUtils.stringToDate(startDate, DateUtils.FORMAT_SQL_DATE), 0);
        }
        Date endTime = null;
        if (StringUtils.isNotBlank(endDate)) {
            endTime = getFormatTime(DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATE), 1);
        }

        //假如没有日期则只查询本周学分
        if (null == startTime || null == endTime || startTime.after(endTime)) {
            startTime = DateUtils.getFirstDayOfWeek(new Date());
            endTime = DateUtils.getLastDayOfWeek(new Date());
        }

        //获取学生详情
        StudentDetail studentDetail = dpStudentLoaderClient.loadStudentDetail(sid);
        //查询学生学校信息
        School school = null;
        //查询学生班级信息
        Clazz clazz = null;
        if (null != studentDetail) {
            clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(sid);
            if (null != clazz) {
                //查询学生学校信息
                school = schoolLoader.loadSchool(clazz.getSchoolId()).getUninterruptibly();
            }
        }

        //总页数
        Integer totalPage = 1;
        //对数据进行处理
        List<CreditLogsData> creditLogsDataList = Lists.newLinkedList();
        List<AchivementLog> achivementLogList = achivementLoader.exportAchivementLogs(sid, startTime, endTime);
        if (CollectionUtils.isNotEmpty(achivementLogList)) {
            //总条数
            Integer size = achivementLogList.size();
            if (size > 20) {
                //计算多少页
                Double pageSize = Math.ceil((size / 20) + 0.4);
                totalPage = pageSize.intValue();
            }
            //学分详情梳理
            Clazz finalClazz = clazz;
            School finalSchool = school;
            creditLogsDataList = achivementLogList.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(AchivementLog::getCreateDatetime).reversed())
                    .skip((pageNumber - 1) * 20).limit(20)
                    .map(achivementLog -> {
                        CreditLogsData data = new CreditLogsData();
                        //查询学生信息
                        if (null == studentDetail) {
                            return null;
                        }
                        //封装学生日志信息
                        data.setSid(sid);
                        data.setUserName(studentDetail.getProfile().getRealname());
                        data.setCid(null== finalClazz ?0: finalClazz.getId());
                        data.setClazzName(null== finalClazz ?"暂无": finalClazz.getClassName());
                        data.setScid(null== finalSchool ?0: finalSchool.getId());
                        data.setSchoolName(null== finalSchool ?"暂无": finalSchool.getCname());
                        data.setCredit(achivementLog.getValue());
                        data.setCreditSource(achivementLog.getAction());
                        data.setCreateTime(achivementLog.getCreateDatetime());

                        return data;
                    }).collect(Collectors.toList());
        }

        model.addAttribute("sid", 0==sid?null:sid);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("achivementlogs", creditLogsDataList);
        model.addAttribute("totalPages", totalPage);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("hasPrevious", 1==pageNumber?false:true);
        model.addAttribute("hasNext", 1==totalPage?false:pageNumber<totalPage?true:false);
        return "/site/achivement/creditdetail";
    }

    public Map<String, Object> queryAchivementActionPage(String itemType, Date startTime, Date endTime, Integer pageIndex, Integer pageSize) {
        //先查询学分行为信息
        Pageable pageable = new PageRequest(pageIndex-1, pageSize, Sort.Direction.DESC, "ct");
        Page<CreditItem> creditItemPage = creditItemLoader.loadCreditItemByPage(pageable, itemType, startTime, endTime);
        List<CreditItem> creditItemList = creditItemPage.getContent();
        if (CollectionUtils.isEmpty(creditItemList)) {
            return ImmutableMap.<String, Object>builder()
                    .put("pageNumber", pageIndex)
                    .put("hasNext", creditItemPage.hasNext())
                    .put("list", new ArrayList<CrmCreditContext>())
                    .put("hasPrevious", creditItemPage.hasPrevious())
                    .put("totalPages", creditItemPage.getTotalPages())
                    .build();
        }

        //查询关联关系
        List<CrmCreditContext> crmCreditContexts = creditItemList.stream()
                .sorted(Comparator.comparing(CreditItem::getCreateDatetime).reversed())
                .filter(creditItem -> CollectionUtils.isNotEmpty(achivementLoader.getAchivementAction(creditItem.getActionId())))
                .map(creditItem -> {
                    AchivementAction achivementAction = achivementLoader.getAchivementAction(creditItem.getActionId()).get(0);
                    String creditItemType = CreditItemType.getCreditItemDesc(creditItem.getType());
                    return new CrmCreditContext(creditItem.getId(), achivementAction.getName(), "学分", StringUtils.isBlank(creditItemType)?creditItem.getType():creditItemType, formatSubject(achivementAction.getSubject()), achivementAction.getValue(), creditItem.getDisabled(), creditItem.getCreateDatetime(), CreditItemType.getCreditItemTermByName(creditItem.getType()));
                }).collect(Collectors.toList());

        return ImmutableMap.<String, Object>builder()
                .put("pageNumber", pageIndex)
                .put("list", crmCreditContexts)
                .put("hasNext", creditItemPage.hasNext())
                .put("hasPrevious", creditItemPage.hasPrevious())
                .put("totalPages", creditItemPage.getTotalPages())
                .build();
    }

    public <T extends Enum> List<T> toList(Class<T> clazz) {
        return Arrays.asList(clazz.getEnumConstants());
    }

    public Date getFormatTime(Date time, Integer type) {
        if (null == time || null == type) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        if (0 == type) {
            //一天开始时间
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        } else if (1 == type) {
            //一天结束时间
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            return calendar.getTime();
        }

        return null;
    }

    public String formatSubject(String name) {
        for (Subject subject : Subject.values()) {
            if (subject.name().equals(name)) {
                return subject.getValue();
            }
        }
        return "未知";
    }

    /**
     * 导出班级所有学生学分
     */
    @RequestMapping(value = "/export/credit_excel.vpage", method = RequestMethod.GET)
    public void exportCreditExcel(HttpServletResponse response) {

        //学生ID
        Long sid = getRequestLong("sid");
        try {
            //查询学生班级信息
            Clazz clazz = null;
            //查询学生学校信息
            School school = null;
            //查询学生信息
            StudentDetail studentDetail;
            if (null != sid && 0 != sid) {
                studentDetail = dpStudentLoaderClient.loadStudentDetail(sid);
                if (null == studentDetail) {
                    return;
                }
                //查询学生班级信息
                clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(sid);
                if (null != clazz) {
                    //查询学生学校信息
                    school = schoolLoader.loadSchool(clazz.getSchoolId()).getUninterruptibly();
                }
            }

            List<AchivementCreditData> creditDataList;
            List<ClazzCreditExcelModel> clazzCreditExcelModels = Lists.newLinkedList();
            List<User> clazzInfos = userAggregationLoader.loadLinkedClassmatesForSystemClazz(sid);
            if (CollectionUtils.isNotEmpty(clazzInfos)) {
                if (null != clazz) {
                    creditDataList = cardingScoreData(clazzInfos, clazz, school, 1, sid);
                    if (CollectionUtils.isNotEmpty(creditDataList)) {
                        clazzCreditExcelModels = creditDataList.stream()
                                .filter(Objects::nonNull)
                                .sorted(Comparator.comparing(AchivementCreditData::getProCredit))
                                .map(achivementCreditData -> {
                                    ClazzCreditExcelModel model = new ClazzCreditExcelModel();
                                    try {
                                        BeanUtils.copyProperties(model, achivementCreditData);
                                    } catch (IllegalAccessException e) {
                                        log.error("Type conversion exception: {}", sid);
                                    } catch (InvocationTargetException e) {
                                        log.error("Type conversion exception: {}", sid);
                                    }
                                    return model;
                                }).collect(Collectors.toList());
                    }
                }
            }

            //班级名称
            String clazzName = "";
            String schoolName = "";
            if (CollectionUtils.isNotEmpty(clazzCreditExcelModels)) {
                clazzName = clazzCreditExcelModels.get(0).getClazzName();
                schoolName = clazzCreditExcelModels.get(0).getSchoolName();
            }
            //写入excel
            String filename = new SimpleDateFormat("yyyy.MM.dd").format(new Date()) + "-" + schoolName + clazzName + "学分详情";
            exportExcelTool(clazzCreditExcelModels, ClazzCreditExcelModel.class, response, filename);
        } catch (Exception e) {
            log.error("export credit excel exception：{}", sid);
        }
    }

    /**
     * 导出学生学分日志记录
     * @param response
     */
    @RequestMapping(value = "/export/credit_log.vpage", method = RequestMethod.GET)
    public void exportCreditLogs(HttpServletResponse response) {
        //学生ID
        Long sid = getRequestLong("sid");
        //开始时间
        String startDate = getRequestParameter("startDate", null);
        //结束时间
        String endDate = getRequestParameter("endDate", null);

        try {
            //对日期进行处理
            Date startTime = null;
            if (StringUtils.isNotBlank(startDate)) {
                startTime = getFormatTime(DateUtils.stringToDate(startDate, DateUtils.FORMAT_SQL_DATE), 0);
            }
            Date endTime = null;
            if (StringUtils.isNotBlank(endDate)) {
                endTime = getFormatTime(DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATE), 1);
            }

            //假如没有日期则只查询本周学分
            if (null == startTime || null == endTime || startTime.after(endTime)) {
                startTime = DateUtils.getFirstDayOfWeek(new Date());
                endTime = DateUtils.getLastDayOfWeek(new Date());
            }

            //获取学生详情
            StudentDetail studentDetail = dpStudentLoaderClient.loadStudentDetail(sid);
            //查询学生学校信息
            School school = null;
            //查询学生班级信息
            Clazz clazz = null;
            if (null != studentDetail) {
                clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(sid);
                if (null != clazz) {
                    //查询学生学校信息
                    school = schoolLoader.loadSchool(clazz.getSchoolId()).getUninterruptibly();
                }
            }

            //梳理导出信息
            List<CreditLogExcelModel> creditLogExcelModelList = Lists.newLinkedList();
            List<AchivementLog> achivementLogList = achivementLoader.exportAchivementLogs(sid, startTime, endTime);
            if (CollectionUtils.isNotEmpty(achivementLogList)) {
                Clazz finalClazz = clazz;
                School finalSchool = school;
                creditLogExcelModelList = achivementLogList.stream()
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(AchivementLog::getCreateDatetime).reversed())
                        .map(achivementLog -> {
                            CreditLogExcelModel model = new CreditLogExcelModel();
                            if (null == studentDetail) {
                                return null;
                            }
                            model.setSid(sid);
                            model.setUserName(studentDetail.getProfile().getRealname());
                            model.setCid(finalClazz.getId());
                            model.setClazzName(finalClazz.getClassName());
                            model.setScid(finalSchool.getId());
                            model.setSchoolName(finalSchool.getCname());
                            model.setCreditSource(achivementLog.getAction());
                            model.setCredit(achivementLog.getValue());
                            model.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(achivementLog.getCreateDatetime()));

                            return model;
                        }).collect(Collectors.toList());
            }

            //学生姓名
            String sname = "";
            if (null != studentDetail) {
                sname = studentDetail.getProfile().getRealname();
            }
            //写入excel
            String filename = new SimpleDateFormat("yyyy.MM.dd").format(new Date()) + "-" + (StringUtils.isBlank(sname)?sid:sname) + "学分记录日志";
            exportExcelTool(creditLogExcelModelList, CreditLogExcelModel.class, response, filename);
        } catch (Exception e) {
            log.error("export credit log excel exception：{}", sid);
        }
    }

    private void exportExcelTool(List<? extends BaseRowModel> list, Class<? extends BaseRowModel> t, HttpServletResponse response, String filename) {
        ExcelWriter excelWriter = null;
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            excelWriter = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX, true);
            Sheet sheet = new Sheet(1, 0, t);
            excelWriter.write(list, sheet);
            response.setContentType("multipart/form-data");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8") + ".xlsx");
        } catch (Exception e) {
            log.error("export credit excel exception: {}", e);
        } finally {
            if (null != excelWriter) {
                excelWriter.finish();
            }
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("ServletOutputStream closing exception: {}", e);
                }
            }
        }
    }

    /**
     * 比较两个日期是否在合法范围内（一个学期：185）
     */
    public static boolean diffDays(Date startDate, Date endDate) {
        try {
            if (null != startDate && null != endDate) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);

                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(endDate);

                int startDay = calendar.get(Calendar.DAY_OF_YEAR);
                int endDay = calendar1.get(Calendar.DAY_OF_YEAR);

                int startYear = calendar.get(Calendar.YEAR);
                int endYear = calendar1.get(Calendar.YEAR);

                if (startYear != endYear) {
                    int timeDiff = 0;
                    for (int i=startYear;i<endYear;i++) {
                        if (i%4==0 && i%100!=0 || i%400==0) {
                            timeDiff += 366;
                        } else {
                            timeDiff += 365;
                        }

                        int totalTimeDiff = timeDiff + (endDay - startDay);
                        if (totalTimeDiff > 185) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                } else {
                    int totalTimeDiff = endDay - startDay;
                    if (totalTimeDiff > 185) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
        }

        return false;
    }
}
