package com.voxlearning.utopia.service.ai.impl.service.processor.v2.questionresult;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishPageContentConfig;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishUserSignRecord;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishUserSignRecordDao;
import com.voxlearning.utopia.service.ai.impl.service.AiChipsEnglishConfigServiceImpl;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.coupon.api.service.CouponService;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Named
public class CQR_SendCoupon extends AbstractAiSupport implements IAITask<ChipsQuestionResultContext> {

    @ImportService(interfaceClass = CouponService.class)
    private CouponService couponService;

    @Inject
    private ChipsEnglishUserSignRecordDao chipsEnglishUserSignRecordDao;

    @Inject
    private AiChipsEnglishConfigServiceImpl aiChipsEnglishConfigService;

    private static final UtopiaCache persistent = CacheSystem.CBS.getCache("persistence");

    private static final String COUPON_CACHE_PREFIX = "ChipsEnglishUserCoupon:User:";

    @Override
    public void execute(ChipsQuestionResultContext context) {
        String bookId = Optional.ofNullable(context.getChipsQuestionResultRequest().getBookId())
                .orElse("");

        // Book id list
        List<String> bookList=currentChipsEnglishBookIds();

        // 不是这两课的不发优惠券
        if (!bookList.contains(bookId)) {
            return ;
        }

        ChipsEnglishProductTimetable chipsEnglishProductTimetable = chipsUserService.loadTimetableByUserAndBook(context.getUserId(), bookId);
        if (chipsEnglishProductTimetable == null || CollectionUtils.isEmpty(chipsEnglishProductTimetable.getCourses())) {
            return;
        }
        //比较是不是今天的课程
        Date now = new Date();
        ChipsEnglishProductTimetable.Course course = chipsEnglishProductTimetable.getCourses().stream().filter(e -> context.getChipsQuestionResultRequest().getUnitId().equals(e.getUnitId())).findFirst().orElse(null);
        if (course == null || course.getBeginDate() == null || !DateUtils.dateToString(now, "yyyy-MM-dd")
                .equals(DateUtils.dateToString(course.getBeginDate(), "yyyy-MM-dd"))) {
            return;
        }

        ChipsEnglishPageContentConfig obj = aiChipsEnglishConfigService.loadChipsConfigByName(COUPON_SEND_UNIT_IDX_KEY);
        int unitIdx = SafeConverter.toInt(Optional.ofNullable(obj).map(ChipsEnglishPageContentConfig::getValue).orElse("0"));

        int courseIdx = chipsEnglishProductTimetable.getCourses().indexOf(course);

        if (courseIdx != unitIdx - 1) {
            return;
        }

        //看看是不是已经发过券了
        String cacheKey = COUPON_CACHE_PREFIX + context.getUserId();
        String coupon = SafeConverter.toString(persistent.load(cacheKey));
        if (StringUtils.isNotBlank(coupon)) {
            return;
        }

        List<ChipsEnglishUserSignRecord> recordList = chipsEnglishUserSignRecordDao.loadByUserId(context.getUserId());
        EnumSet weekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        // 当天打卡，不包括周六周日
        int unitSign = recordList.stream().filter(e -> e.getBookId().equals(bookId) &&
                e.getCurrent() && e.getCreateTime() != null &&
                !weekend.contains(e.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfWeek()))
                .map(ChipsEnglishUserSignRecord::getUnitId).collect(Collectors.toSet()).size() + 1;

        if (unitSign <= TEST_DAILY_SHARE_COUPONS.size() && unitSign <= ONLINE_DAILY_SHARE_COUPONS.size()) {
            String couponId = RuntimeMode.le(Mode.TEST) ? TEST_DAILY_SHARE_COUPONS.get(unitSign - 1) : ONLINE_DAILY_SHARE_COUPONS.get(unitSign - 1);
            couponService.sendCoupon(couponId, context.getUserId(), SHARE_RECORD_CHANNEL, "system");
            persistent.add(cacheKey, 60 * 60 * 24 * 3, couponId);
        }
    }

}
