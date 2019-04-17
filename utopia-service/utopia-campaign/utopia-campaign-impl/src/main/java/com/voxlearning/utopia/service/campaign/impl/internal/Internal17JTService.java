package com.voxlearning.utopia.service.campaign.impl.internal;

import com.voxlearning.alps.core.util.DigestUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTCourse;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTPuy;
import com.voxlearning.utopia.service.campaign.impl.dao.YiqiJTCourseDao;
import com.voxlearning.utopia.service.campaign.impl.dao.YiqiJTPuyDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Named
public class Internal17JTService extends SpringContainerSupport {

    private static final String URL_PARSE_REGEX = "^(http://|https://)?([^/?]+)(/[^?]*)?(\\?.*)?$";
    private static final String OSS_AUTH_PRIVATE_KEY = "17zy17jt";

    @Inject private YiqiJTCourseDao yiqiJTCourseDao;
    @Inject private YiqiJTPuyDao yiqiJTPuyDao;

    @Override
    public void afterPropertiesSet() {
    }

    public Optional<Date> loadCourseBuyTime(Long teacherId, Long courseId) {
        YiqiJTPuy yiqiJTPuy = yiqiJTPuyDao.loadOne(teacherId, courseId);

        return Optional.ofNullable(Optional.ofNullable(yiqiJTPuy).map(YiqiJTPuy :: getBuyTime)).orElse(null);
//        return loadUserCourseData(teacherId,courseId,"buyTime",t -> new Date(SafeConverter.toLong(t)));
    }

    public List<YiqiJTCourse> loadAllCourses(){
        return yiqiJTCourseDao.loadAll();
    }

    public YiqiJTCourse loadCourseById(Long id){
        return yiqiJTCourseDao.load(id);
    }

    public MapMessage fixData(Long userId) {
        MapMessage resultMsg = MapMessage.successMessage();
        return resultMsg;
    }

    public void updateBuyData(long userId, long courseId, Date buyTime) {
        YiqiJTCourse course = loadCourseById(courseId);
        if(course == null) {
            return;
        }

        YiqiJTPuy yiqiJTPuy = yiqiJTPuyDao.loadOne(userId, courseId);
        if (yiqiJTPuy == null) {
            yiqiJTPuy = new YiqiJTPuy();
            yiqiJTPuy.setBuyTime(buyTime);
            yiqiJTPuy.setCourseId(courseId);
            yiqiJTPuy.setUserId(userId);
            yiqiJTPuyDao.upsert(yiqiJTPuy);
            yiqiJTCourseDao.updateAttendNum(courseId);
        } else {
            yiqiJTPuy.setBuyTime(buyTime);
            yiqiJTPuyDao.upsert(yiqiJTPuy);
        }
    }

    public String wrapAuth(String url, Date expTime) {
        if (StringUtils.isEmpty(url) || expTime == null)
            return null;

        Pattern pattern = Pattern.compile(URL_PARSE_REGEX);
        Matcher matcher = pattern.matcher(url);
        if (!matcher.matches())
            return null;

        String exp = String.valueOf(expTime.getTime() / 1000);
        String rand = UUID.randomUUID().toString().replace("-", "");
        String path = matcher.group(3);
        String signStr = StringUtils.join(new String[]{path, exp, rand, "0", OSS_AUTH_PRIVATE_KEY}, "-");
        String authKey = StringUtils.join(new String[]{exp,rand,"0",DigestUtils.md5Hex(signStr)},"-");

        return UrlUtils.buildUrlQuery(url, MapUtils.m("auth_key", authKey));
    }
}
