package com.voxlearning.utopia.service.business.impl.service;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.ChargeType;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilege;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilegeTpl;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilegeUseLog;
import com.voxlearning.utopia.service.business.api.TeacherTaskPrivilegeService;
import com.voxlearning.utopia.service.business.impl.dao.TeacherTaskPrivilegeDao;
import com.voxlearning.utopia.service.business.impl.dao.TeacherTaskPrivilegeTplDao;
import com.voxlearning.utopia.service.business.impl.dao.TeacherTaskPrivilegeUseLogDao;
import com.voxlearning.utopia.service.coupon.api.constants.CouponUserStatus;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@Slf4j
@ExposeService(interfaceClass = TeacherTaskPrivilegeService.class)
public class TeacherTaskPrivilegeServiceImpl implements TeacherTaskPrivilegeService {

    @Inject
    private TeacherTaskPrivilegeDao teacherTaskPrivilegeDao;

    @Inject
    private CouponLoaderClient couponLoaderClient;

    @Inject
    private CouponServiceClient couponServiceClient;

    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Inject
    private TeacherTaskPrivilegeTplDao teacherTaskPrivilegeTplDao;

    @Inject
    private TeacherTaskPrivilegeUseLogDao teacherTaskPrivilegeUseLogDao;

    @AlpsPubsubPublisher(topic = "utopia.teacher.task.privilege.coupon.delete.topic")
    private MessagePublisher messagePublisherCoupon;

    /**
     * 获取老师特权的方法都用这个方法
     */
    @Override
    public MapMessage getTeacherALLPrivilege(Long teacherId) {
        return initPrivilegeAndGet(teacherId);
    }

    /**
     * 初始化老师的特权信息
     */
    public MapMessage initPrivilegeAndGet(Long teacherId) {
        try {
            if (teacherId == null) {
                return MapMessage.errorMessage("请输入老师ID");
            }
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (!teacherDetail.isPrimarySchool()) {
                return MapMessage.errorMessage("只有小学老师才有特权哦");
            }
            Long mainTchId = teacherLoaderClient.loadMainTeacherId(teacherId);
            if(mainTchId != null && mainTchId > 0L) {
                return MapMessage.errorMessage("只有主账号老师才有特权哦");
            }
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("getTeacherALLPrivilege")
                    .keys(teacherId)
                    .callback(() -> {
                        try {
                            /** 当前日期信息 **/
                            Date now = new Date();
                            String nowString = DateUtils.dateToString(now, DateUtils.FORMAT_SQL_DATETIME);

                            /** 载入特权模板信息 **/
                            Map<Long, TeacherTaskPrivilegeTpl> tplMap = loadTeacherTaskPrivilegeTplMap();
                            if (tplMap.isEmpty()) {
                                log.error("老师特权信息模板为空");
                                return MapMessage.errorMessage("特权模板信息信息为空，请检查");
                            }

                            /** 载入用户的扩展信息 **/
                            TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacherId);
                            Integer extLevel = null;
                            Long extInitNewExpTime = null;
                            if (teacherExtAttribute != null) {
                                extLevel = teacherExtAttribute.getNewLevel();
                                extInitNewExpTime = teacherExtAttribute.getInitNewExpTime();
                            }

                            /** 用户的特权信息 **/
                            TeacherTaskPrivilege teacherTaskPrivilege = teacherTaskPrivilegeDao.load(teacherId);
                            if (null == teacherTaskPrivilege) {//用户没有特权信息
                                teacherTaskPrivilege = new TeacherTaskPrivilege();
                                teacherTaskPrivilege.setId(teacherId);
                                teacherTaskPrivilege.setPrivileges(new ArrayList<>());
                            }

                            /** 根据用户等级设置用户的特权权限 **/
                            List<TeacherTaskPrivilege.Privilege> privileges = teacherTaskPrivilege.getPrivileges();
                            Map<Long, TeacherTaskPrivilege.Privilege> privilegeMap = privileges.stream().collect(Collectors.toMap(TeacherTaskPrivilege.Privilege::getId, p -> p));
                            setTeacherPrivilegeByLevel(teacherTaskPrivilege, teacherId, extLevel, extInitNewExpTime, privilegeMap, tplMap, now, nowString);
                            List<TeacherTaskPrivilege.Privilege> privilegesNew = privilegeMap.values().stream().collect(Collectors.toList());
                            Collections.sort(privilegesNew, (p1, p2) -> p1.getId() == p2.getId() ? 0 : p1.getId() > p2.getId() ? 1 : - 1);
                            privileges.clear();
                            privileges.addAll(privilegesNew);

                            /** 用户拥有券的信息，为了提高性能，不每次查券的接口，判断是否需要查询 **/
                            boolean isNotUsedCoupon = isNotUsedCoupon(tplMap, privileges);
                            if (isNotUsedCoupon) {//用户拥有券的特权，则添加，因为券在其他系统，调用也不经过我们，只能通过查询去查看券是否有使用
                                List<CouponShowMapper> couponShowMappers = couponLoaderClient.loadUserCoupons(teacherId);
                                for (TeacherTaskPrivilege.Privilege privilege : privileges) {
                                    TeacherTaskPrivilegeTpl tpl = tplMap.get(privilege.getId());
                                    /**如果是关于券的特权，查找券的使用次数以及使用时间，并更新**/
                                    if (tpl.getIsCoupon() && CollectionUtils.isNotEmpty(couponShowMappers)) {
                                        setCouponUseTimes(couponShowMappers, privilege, tpl, nowString);
                                    }
                                }
                            }

                            teacherTaskPrivilegeDao.upsert(teacherTaskPrivilege);
                            return MapMessage.successMessage().add("teacherTaskPrivilege", teacherTaskPrivilege);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                            return MapMessage.errorMessage("系统错误，请重试!");
                        }
                    })
                    .build()
                    .execute();
        } catch (Exception e) {
            return MapMessage.errorMessage("正在处理中，请勿重复操作").add("error", e.getMessage()).add("lock", true);
        }
    }

    /**
     * 设置老师的特权信息
     */
    private boolean setTeacherPrivilegeByLevel(TeacherTaskPrivilege teacherTaskPrivilege, Long teacherId, Integer extLevel, Long extLevelTime, Map<Long, TeacherTaskPrivilege.Privilege> privilegeMap,
                                                    Map<Long, TeacherTaskPrivilegeTpl> tplMap, Date now, String nowString) {
        JexlEngine jexlEngine = new JexlEngine();
        JexlContext calContext = new MapContext();

        boolean isInit = false;
        if (extLevel != null && extLevelTime != null) {//如果扩展表的用户等于与升级时间都不为NULL，才判断是否需要初始化特权
            if (teacherTaskPrivilege.getLevel() != null && !Objects.equals(teacherTaskPrivilege.getLevel(), extLevel)) {//升级或者降级
                isInit = true;
            } else if (teacherTaskPrivilege.getLevelTime() != null && !Objects.equals(teacherTaskPrivilege.getLevelTime(), extLevelTime) && teacherTaskPrivilege.getLevelTime() < extLevelTime) {//等级不变，但是保级了，也要初始化
                isInit = true;
            }
        }
        Integer level = extLevel != null && extLevel > 0 ? extLevel : 1;
        Long initTime = extLevelTime != null && extLevelTime > 0 ? extLevelTime : now.getTime();

        /** 1.计算用户当前等级应该拥有的权限 **/
        List<TeacherTaskPrivilegeTpl> putOnTpl = this.getPrivilegeByTeacherId(teacherId);
        Map<Long, TeacherTaskPrivilegeTpl> putOnTplMap = putOnTpl.stream().collect(Collectors.toMap(TeacherTaskPrivilegeTpl::getId, t -> t));
        teacherTaskPrivilege.setLevel(level);
        teacherTaskPrivilege.setLevelTime(initTime);

        /** 2.清理特权信息 **/
        TeacherTaskPrivilege.Privilege birthDayPrivilege = null;//老师生日的特权，因为一年只能发一次园丁豆，拿一下信息，【后面抽取一下，否则不好管理】
        if (isInit) {
            for (TeacherTaskPrivilege.Privilege privilege : privilegeMap.values()) {
                if (tplMap.get(privilege.getId()).getIsCoupon() && CollectionUtils.isNotEmpty(privilege.getPrivilegeCoupons())) {//如果有券，则需要处理券的逻辑
                    setCouponDisabledPatch(teacherId, privilege.getId(), privilege.getPrivilegeCoupons());
                }
                if (Objects.equals(privilege.getId(), TeacherTaskPrivilegeTpl.Privilege.BIRTHDAY_INTEGRAL.getId())) {//如果是生日特权，需要保留老师是否已经发放园丁豆
                    birthDayPrivilege = privilege;
                }
            }
            privilegeMap.clear();
        } else {
            /** A.清理不在拥有的特权 **/
            List<Long> noHaveId = new ArrayList<>();
            for (TeacherTaskPrivilege.Privilege privilege : privilegeMap.values()) {
                if (putOnTplMap.containsKey(privilege.getId())) {
                    continue;
                }
                noHaveId.add(privilege.getId());
                if (tplMap.get(privilege.getId()).getIsCoupon() && CollectionUtils.isNotEmpty(privilege.getPrivilegeCoupons())) {//如果有券，则需要处理券的逻辑
                    setCouponDisabledPatch(teacherId, privilege.getId(), privilege.getPrivilegeCoupons());
                }
                if (Objects.equals(privilege.getId(), TeacherTaskPrivilegeTpl.Privilege.BIRTHDAY_INTEGRAL.getId())) {//如果是生日特权，需要保留老师是否已经发放园丁豆
                    birthDayPrivilege = privilege;
                }
            }
            noHaveId.stream().forEach(id -> privilegeMap.remove(id));

            /** B.删除过期的特权 **/
            List<Long> expireId = new ArrayList<>();
            for (TeacherTaskPrivilege.Privilege privilege : privilegeMap.values()) {
                if (privilege.getExpireTime() == null || privilege.getExpireTime() <= 0) {//非过期特权，不处理
                    continue;
                }
                if (privilege.getExpireTime() > now.getTime()) {//特权还没有过期
                    continue;
                }
                expireId.add(privilege.getId());
                if (tplMap.get(privilege.getId()).getIsCoupon() && CollectionUtils.isNotEmpty(privilege.getPrivilegeCoupons())) {//如果有券，则需要处理券的逻辑
                    setCouponDisabledPatch(teacherId, privilege.getId(), privilege.getPrivilegeCoupons());
                }
            }
            expireId.stream().forEach(id -> privilegeMap.remove(id));
        }

        /** 3.生成新的权限信息 **/
        for (TeacherTaskPrivilegeTpl tpl : putOnTpl) {
            if (privilegeMap.containsKey(tpl.getId())) {//已经包含了，则不再处理
                continue;
            }
            TeacherTaskPrivilege.Privilege newPrivilege = new TeacherTaskPrivilege.Privilege();
            newPrivilege.setId(tpl.getId());
            newPrivilege.setName(tpl.getName());
            if (tpl.getTimesLimit() != null && tpl.getTimesLimit()) {//如果限制次数，则需要限制一下，否则不处理
                newPrivilege.setTimes((Integer) jexlEngine.createExpression(tpl.getTimesExpr()).evaluate(calContext));
                newPrivilege.setUseTimes(0);

                /** 如果有使用次数，则生成每一条使用信息明细 **/
                List<TeacherTaskPrivilege.PrivilegeCoupon> privilegeCoupons = new ArrayList<>();
                newPrivilege.setPrivilegeCoupons(privilegeCoupons);
                for (int a = newPrivilege.getTimes(); a > 0; a--) {
                    TeacherTaskPrivilege.PrivilegeCoupon privilegeCoupon = new TeacherTaskPrivilege.PrivilegeCoupon();
                    privilegeCoupon.setIsUsed(false);
                    privilegeCoupon.setIsCoupon(false);
                    privilegeCoupon.setCreateDate(nowString);
                    privilegeCoupons.add(privilegeCoupon);

                    if (tpl.getIsCoupon()) {//如果是券，需要生成老师的券信息
                        /** 重点内容：生成老师的券信息 **/
                        privilegeCoupon.setIsCoupon(true);
                        MapMessage mapMessage = couponServiceClient.sendCoupon(tpl.getCouponId(), teacherId, ChargeType.TEACHER_TASK.getDescription() + tpl.getName(), "9999");
                        if (mapMessage.isSuccess()) {
                            String refId = SafeConverter.toString(mapMessage.get("refId"));
                            privilegeCoupon.setCouponUserRefId(refId);
                        } else {
                            log.error("TeacherTaskPrivilege create coupon fail. teacherId:{}, couponId:{}, tplId:{}", teacherId, tpl.getCouponId(), tpl.getId());
                        }
                    }
                }
            }
            if (Objects.equals(tpl.getId(), TeacherTaskPrivilegeTpl.Privilege.BIRTHDAY_INTEGRAL.getId())) {//如果是生日特权，需要保留老师是否已经发放园丁豆的信息
                if (birthDayPrivilege != null) {
                    newPrivilege.setExt(birthDayPrivilege.getExt());//ext: date 发豆的详细时间, year 发豆的年份
                } else {
                    newPrivilege.setExt(new HashMap<>());
                }
            }
            newPrivilege.setCreateDate(nowString);
            newPrivilege.setUpdateDate(nowString);
            if (tpl.getLoop()) {//如果是循环特权，需要设置特权的过期时间
                Long newExpireTime = teacherTaskPrivilege.getLevelTime() + 180 * 24 * 60 * 60 * 1000L; //默认半年后过期
                if (Objects.equals(tpl.getCycleUnit(), TeacherTaskPrivilegeTpl.CycleUnit.W.name())) {
                    newExpireTime = WeekRange.current().getEndDate().getTime();
                } else if (Objects.equals(tpl.getCycleUnit(), TeacherTaskPrivilegeTpl.CycleUnit.M.name())) {
                    newExpireTime = MonthRange.current().getEndDate().getTime();
                }
                newPrivilege.setExpireTime(newExpireTime);
            }
            privilegeMap.put(newPrivilege.getId(), newPrivilege);
        }
        return true;
    }

    public MapContext getMapContext(Long teacherId) {
        MapContext context = new MapContext();
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);

        //老师等级
        TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacherId);
        int level = 1;
        if (teacherExtAttribute != null && teacherExtAttribute.getNewLevel() != null && teacherExtAttribute.getNewLevel() > 0) {
            level = teacherExtAttribute.getNewLevel();
        }
        context.set("level", level);

        //老师学科
        List<Long> longs = teacherLoaderClient.loadSubTeacherIds(teacherId);
        longs.add(teacherId);
        List<String> subjects = new ArrayList<>();
        List<String> subjectsSubTeacher = teacherLoaderClient.loadTeacherDetails(longs).values().stream().filter(t -> t.getSubject() != null).map(t -> t.getSubject().name()).collect(Collectors.toList());
        subjects.addAll(subjectsSubTeacher);
        context.set("subjects", subjects);

        //LOCAL_EXPERT_TOPIC 地区
        boolean localExpertTopicRegion = TeacherTaskPrivilegeTpl.localExpertTopicConfig.contains(SafeConverter.toString(teacherDetail.getRootRegionCode()));
        context.set("LOCAL_EXPERT_TOPIC_REGION", localExpertTopicRegion);

        //JIANG_LIAN_CE 地区
        context.set("JIANG_LIAN_CE_REGION", true);//暂时全部地区都支持

        return context;
    }

    /**
     * 老师所有特权中是否有未使用的券信息
     */
    private boolean isNotUsedCoupon(Map<Long, TeacherTaskPrivilegeTpl> tplMap, List<TeacherTaskPrivilege.Privilege> privileges) {
        boolean isNotUsedCoupon = false;
        for (TeacherTaskPrivilege.Privilege privilege : privileges) {
            if (!tplMap.get(privilege.getId()).getIsCoupon()) {
                continue;
            }
            for (TeacherTaskPrivilege.PrivilegeCoupon privilegeCoupon : privilege.getPrivilegeCoupons()) {
                if (!privilegeCoupon.getIsUsed()) {
                    isNotUsedCoupon = true;
                }
            }
        }
        return isNotUsedCoupon;
    }


    /**
     * 删除特权中未使用的券
     */
    private void setCouponDisabledPatch(Long teacherId, Long tplId, List<TeacherTaskPrivilege.PrivilegeCoupon> privilegeCoupons) {
        if (CollectionUtils.isEmpty(privilegeCoupons)) {
            return;
        }
        List<String> refIds = privilegeCoupons.stream().filter(pc -> pc.getIsCoupon() != null && pc.getIsCoupon()).filter(pc -> pc.getIsUsed() != null && pc.getIsUsed() == false).map(TeacherTaskPrivilege.PrivilegeCoupon::getCouponUserRefId).collect(Collectors.toList());
        MapMessage mapMessage = couponServiceClient.removeCouponUserRefs(refIds);
        if (!mapMessage.isSuccess()) {
            log.error("老师特权清理券失败：teacherId:{}, refId:{}", teacherId, refIds);
            pushDeleteCoupon(teacherId, tplId, refIds);
        }
    }

    /**
     * 设置老师的某一个特权中券的使用情况
     */
    private boolean setCouponUseTimes(List<CouponShowMapper> couponShowMappers, TeacherTaskPrivilege.Privilege privilege, TeacherTaskPrivilegeTpl tpl, String nowString) {
        //获取该特权下绑定的券信息
        List<TeacherTaskPrivilege.PrivilegeCoupon> privilegeCoupons = privilege.getPrivilegeCoupons();
        List<String> couponUserRefIds = privilegeCoupons.stream().map(TeacherTaskPrivilege.PrivilegeCoupon::getCouponUserRefId).collect(Collectors.toList());

        int usedNum = 0;
        for (CouponShowMapper couponShowMapper : couponShowMappers) {
            if (!Objects.equals(couponShowMapper.getCouponId(), tpl.getCouponId()) || !couponUserRefIds.contains(couponShowMapper.getCouponUserRefId())){//过滤不相关的券
                continue;
            }
            if (Objects.equals(couponShowMapper.getCouponUserStatus().name(), CouponUserStatus.Used.name())) {//已经使用过的券
                usedNum = usedNum + 1;//券的使用次数+1
                String usedTime = couponShowMapper.getUsedTime();
                String refId = couponShowMapper.getCouponUserRefId();
                TeacherTaskPrivilege.PrivilegeCoupon privilegeCoupon = privilegeCoupons.stream().filter(pc -> Objects.equals(pc.getCouponUserRefId(), refId)).findAny().orElse(null);
                if (privilegeCoupon.getIsUsed() != true) {
                    privilegeCoupon.setIsUsed(true);//设置券已经使用
                    privilegeCoupon.setUseDate(usedTime);//设置券的使用时间
                }
            }
        }
        if (privilege.getUseTimes() < usedNum) {
            privilege.setUseTimes(usedNum);//设置券已经使用的次数
            privilege.setUpdateDate(nowString);
        }
        return true;
    }

    @Override
    public MapMessage cousumerPrivilege(Long teacherId, Long id, String system) {
        return cousumerPrivilege(teacherId, id, system, null);
    }

    @Override
    public MapMessage cousumerPrivilege(Long teacherId, Long id, String system, String couponRefId) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("cousumerPrivilege")
                    .keys(teacherId)
                    .callback(() -> {
                        try {
                            MapMessage mapMessage = this.getPrivilegeTimes(teacherId, id);
                            if (!mapMessage.isSuccess()) {
                                return mapMessage;
                            }
                            Integer times = (Integer) mapMessage.get("times");
                            if (times == null) {//不限次数
                                return MapMessage.successMessage();
                            } else if (times <= 0) {//次数小于0
                                return MapMessage.errorMessage("您已没有剩余的特权次数").add("times", 0);
                            } else {//次数大于0
                                Map<Long, TeacherTaskPrivilegeTpl> tplMap = loadTeacherTaskPrivilegeTplMap();
                                TeacherTaskPrivilege teacherTaskPrivilege = teacherTaskPrivilegeDao.load(teacherId);
                                TeacherTaskPrivilege.Privilege privilege = teacherTaskPrivilege.getByPrivilegeTplId(id);
                                TeacherTaskPrivilegeTpl tpl = tplMap.get(privilege.getId());
                                privilege.setUseTimes(privilege.getUseTimes() + 1);
                                privilege.setUpdateDate(DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME));
                                if (CollectionUtils.isNotEmpty(privilege.getPrivilegeCoupons()) && !tpl.getIsCoupon()) {/** 处理非券的使用记录信息 **/
                                    TeacherTaskPrivilege.PrivilegeCoupon privilegeCoupon = privilege.getPrivilegeCoupons().stream().filter(p -> p.getIsUsed() == false).findFirst().orElse(null);
                                    if (null != privilegeCoupon) {
                                        privilegeCoupon.setIsUsed(true);
                                        privilegeCoupon.setUseDate(DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME));
                                        if (StringUtils.isNotBlank(couponRefId)) {//如果有券，需要写入券信息
                                            privilegeCoupon.setCouponUserRefId(couponRefId);
                                        }
                                        if (StringUtils.isNotBlank(system)) {//写入消费次数的功能
                                            privilegeCoupon.setUserSystem(system);
                                        }
                                    } else {
                                        log.error("老师的特权使用标记异常. teacherId:{}, tplId{}, system:{}, couponRefId:{}", teacherId, id, system, couponRefId);
                                    }
                                } else {
                                    log.error("老师有使用次数限制的特权特权未生成privilegeCoupon. teacherId:{}, tplId{}, system:{}, couponRefId:{}", teacherId, id, system, couponRefId);
                                }

                                try {
                                    TeacherTaskPrivilegeUseLog teacherTaskPrivilegeUseLog = new TeacherTaskPrivilegeUseLog();
                                    teacherTaskPrivilegeUseLog.setTeacherId(teacherId);
                                    teacherTaskPrivilegeUseLog.setLevel(teacherTaskPrivilege.getLevel());
                                    teacherTaskPrivilegeUseLog.setComment("消费一次特权信息: " + system);
                                    teacherTaskPrivilegeUseLog.setPrivilegeId(tpl.getId().intValue());
                                    teacherTaskPrivilegeUseLog.setPrivilegeName(tpl.getName());
                                    teacherTaskPrivilegeUseLogDao.insert(teacherTaskPrivilegeUseLog);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                }

                                teacherTaskPrivilegeDao.upsert(teacherTaskPrivilege);
                                return MapMessage.successMessage();
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage(),e);
                            return MapMessage.errorMessage("操作失败，请重试").add("error",e.getMessage());
                        }
                    })
                    .build()
                    .execute();
        } catch (Exception e) {
            return MapMessage.errorMessage("正在处理中，请勿重复操作");
        }
    }

    @Override
    public MapMessage isHavePrivilege(Long teacherId, Long type) {
        MapMessage mapMessage = this.getTeacherALLPrivilege(teacherId);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        TeacherTaskPrivilege teacherTaskPrivilege = (TeacherTaskPrivilege)mapMessage.get("teacherTaskPrivilege");
        List<TeacherTaskPrivilege.Privilege> privileges = teacherTaskPrivilege.getPrivileges();
        TeacherTaskPrivilege.Privilege privilege = privileges.stream().filter(p -> Objects.equals(p.getId(), type)).findAny().orElse(null);
        if (null == privilege) {
            return MapMessage.successMessage().add("isHave", false);
        }
        TeacherTaskPrivilegeTpl teacherTaskPrivilegeTpl = teacherTaskPrivilegeTplDao.load(privilege.getId());
        if (null == teacherTaskPrivilegeTpl) {
            return MapMessage.successMessage().add("isHave", false);
        }
        return MapMessage.successMessage().add("isHave", true);
    }

    @Override
    public MapMessage getPrivilegeTimes(Long teacherId, Long id) {
        MapMessage mapMessage = this.getTeacherALLPrivilege(teacherId);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        TeacherTaskPrivilege teacherTaskPrivilege = (TeacherTaskPrivilege)mapMessage.get("teacherTaskPrivilege");
        List<TeacherTaskPrivilege.Privilege> privileges = teacherTaskPrivilege.getPrivileges();
        TeacherTaskPrivilege.Privilege privilege = privileges.stream().filter(p -> Objects.equals(p.getId(), id)).findAny().orElse(null);
        if (null == privilege) {
            return MapMessage.errorMessage("您没有该特权的使用权限");
        }
        return calcTimes(privilege);
    }

    /**
     * 根据特权，计算特权的总次数与剩余的使用次数
     * times = null || total = null表示不限制次数
     * time表示剩余次数，total表示总次数, Id表示特权的
     * @param privilege
     * @return
     */
    private MapMessage calcTimes(TeacherTaskPrivilege.Privilege privilege) {
        Integer times = privilege.getTimes();
        if (times == null || times == 0) {
            return MapMessage.successMessage().add("id", privilege.getId());
        } else {
            Integer useTimes = privilege.getUseTimes();
            Integer remainderTimes = times - useTimes <= 0 ? 0 : times - useTimes;
            return MapMessage.successMessage().add("times", remainderTimes).add("total", privilege.getTimes()).add("id", privilege.getId());
        }
    }

    @Override
    public MapMessage getCoursewareDownloadTimes(Long teacherId) {
        return this.getAnyTimes(teacherId, TeacherTaskPrivilegeTpl.Privilege.COURSEWARE_DOWNLOAD_INTERMEDIATE.getType());
    }

    @Override
    public MapMessage get17ClassTimes(Long teacherId) {
        return this.getAnyTimes(teacherId, TeacherTaskPrivilegeTpl.Privilege.CLASS_17_INTERMEDIATE.getType());
    }

    private MapMessage getAnyTimes(Long teacherId, String type) {
        List<Long> ids = Arrays.stream(TeacherTaskPrivilegeTpl.Privilege.values()).filter(tpl -> Objects.equals(tpl.getType(), type)).map(TeacherTaskPrivilegeTpl.Privilege::getId).collect(Collectors.toList());
        MapMessage mapMessage = this.getTeacherALLPrivilege(teacherId);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        TeacherTaskPrivilege teacherTaskPrivilege = (TeacherTaskPrivilege)mapMessage.get("teacherTaskPrivilege");
        TeacherTaskPrivilege.Privilege privilege = teacherTaskPrivilege.getPrivileges().stream().filter(p -> ids.contains(p.getId())).findFirst().orElse(null);
        if (null == privilege) {
            return MapMessage.errorMessage("您没有该特权的使用权限");
        }
        return calcTimes(privilege);
    }

    public List<TeacherTaskPrivilegeTpl> getPrivilegeByTeacherId(Long teacherId) {
        List<TeacherTaskPrivilegeTpl> teacherTaskPrivilegeTpls = teacherTaskPrivilegeTplDao.loadAll();
        JexlEngine jexlEngine = new JexlEngine();
        JexlContext calContext = getMapContext(teacherId);
        List<TeacherTaskPrivilegeTpl> getTpls = new ArrayList<>();
        teacherTaskPrivilegeTpls.stream().forEach(tpl -> {
            Boolean isPutOn = (Boolean) jexlEngine.createExpression(tpl.getPutOnExpr()).evaluate(calContext);
            if (isPutOn) {
                getTpls.add(tpl);
            }
        });
        return getTpls;
    }

    @Override
    public List<TeacherTaskPrivilegeTpl> getPrivilegeByTeacherIdExcludeLevel(Long teacherId) {
        List<TeacherTaskPrivilegeTpl> teacherTaskPrivilegeTpls = teacherTaskPrivilegeTplDao.loadAll();
        JexlEngine jexlEngine = new JexlEngine();
        JexlContext calContext = getMapContext(teacherId);
        calContext.set("level", 5);
        List<TeacherTaskPrivilegeTpl> getTpls = new ArrayList<>();
        teacherTaskPrivilegeTpls.stream().forEach(tpl -> {
            Boolean isPutOn = (Boolean) jexlEngine.createExpression(tpl.getPutOnExpr()).evaluate(calContext);
            if (isPutOn) {
                getTpls.add(tpl);
            }
        });
        return getTpls;
    }

    private Map<Long, TeacherTaskPrivilegeTpl> loadTeacherTaskPrivilegeTplMap() {
        List<TeacherTaskPrivilegeTpl> teacherTaskPrivilegeTpls = teacherTaskPrivilegeTplDao.loadAll();
        return teacherTaskPrivilegeTpls.stream().collect(Collectors.toMap(TeacherTaskPrivilegeTpl::getId, t -> t));
    }

    @Override
    public List<TeacherTaskPrivilegeTpl> getAllTeacherTaskPrivilegeTpl() {
        return teacherTaskPrivilegeTplDao.loadAll();
    }

    @Override
    public boolean isInitPrivilege(Long teacherId) {
        TeacherTaskPrivilege teacherTaskPrivilege = teacherTaskPrivilegeDao.load(teacherId);
        if (teacherTaskPrivilege == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void pushDeleteCoupon(Long teacherId, Long tplId, List<String> refIds) {
        //券删除失败了，写一个监听器，继续删除
        Map<String,Object> msgBody = new HashMap<>();
        msgBody.put("teacherId", teacherId);//老师ID
        msgBody.put("refIds", refIds);//券ID
        msgBody.put("tplId", tplId);//券ID
        msgBody.put("messageType", "deleteCoupon");
        messagePublisherCoupon.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(msgBody)));
    }

    @Override
    public void removeTeacherTaskPrivilegeUserLog(Long time) {
        teacherTaskPrivilegeUseLogDao.removeByReceiveData(new Date(time));
    }

    @Override
    public TeacherTaskPrivilege getTeacherTaskPrivilege(Long teacherId) {
        return teacherTaskPrivilegeDao.load(teacherId);
    }

    @Override
    public void upsertTeacherTaskPrivilege(TeacherTaskPrivilege teacherTaskPrivilege) {
        teacherTaskPrivilegeDao.upsert(teacherTaskPrivilege);
    }
}
