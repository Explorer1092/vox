package com.voxlearning.washington.controller.open.v1.util;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.athena.bean.ParentResData;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNewsNativeLabel;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNewsPushType;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNewsStyleType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNews;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 格式化各种数据的方法。
 * 由于之前h5的全部写的私有方法或者是abstarctMobiile的父类中。现在原生要用。只能抽出来放到这里了
 *
 * @author shiwei.liao
 * @since 2016-11-22
 */
public class JxtNewsUtil {

    //格式化各种统计数量
    public static String countFormat(Long count) {
        if (count < 10000) {
            return count.toString();
        }
        BigDecimal[] divideAndRemainder = new BigDecimal(count).divideAndRemainder(new BigDecimal(10000));
        Long thousand = divideAndRemainder[1].divide(new BigDecimal(1000), BigDecimal.ROUND_HALF_UP).longValue();
        String prefix = divideAndRemainder[0].toString();
        String suffix = "." + thousand.toString();
        if (thousand == 0) {
            suffix = "";
        } else if (thousand == 10) {
            suffix = "";
            prefix = divideAndRemainder[0].add(new BigDecimal(1)).toString();
        }
        return prefix + suffix + "w";
    }

    public static JxtNewsNativeLabel generateLabel(JxtNews jxtNews, List<String> onlineAlbumIds) {
        //优先级为:置顶>同步课堂>专题/专辑>本地>视频/音频
        //第三方同步课堂
        if (StringUtils.equals(jxtNews.generateStyleType(), JxtNewsStyleType.EXTERNAL_SYNC_TEACHING_MATERIAL.name())) {
            return JxtNewsNativeLabel.SYNC_MATERIAL;
        }
        //我们自己的同步课堂
        if (StringUtils.equals(jxtNews.generateStyleType(), JxtNewsStyleType.SYNC_TEACHING_MATERIAL.name())) {
            return JxtNewsNativeLabel.SYNC_MATERIAL;
        }
        //专辑
        if (StringUtils.isNotBlank(jxtNews.getAlbumId()) && onlineAlbumIds.contains(jxtNews.getAlbumId())) {
            return JxtNewsNativeLabel.ALBUM;
        }
        //本地
        if (Objects.equals(jxtNews.generatePushType(), JxtNewsPushType.REGION.getType())) {
            return JxtNewsNativeLabel.LOCAL;
        }
        //资讯类型
        return JxtNewsNativeLabel.parse(jxtNews.generateContentType());
    }

    public static JxtNewsNativeLabel generateSearchAlbumLabel(Enum contentType) {
        if (contentType != null && !Objects.equals(contentType, ParentResData.ContentType.UNKNOWN)) {
            if (Objects.equals(contentType, ParentResData.ContentType.AUDIO)) {
                return JxtNewsNativeLabel.AUDIO_ALBUM;
            }
            if (Objects.equals(contentType, ParentResData.ContentType.VIDEO)) {
                return JxtNewsNativeLabel.VIDEO_ALBUM;
            }
        }
        return JxtNewsNativeLabel.IMG_AND_TEXT_ALBUM;
    }

    //生成资讯详情页的链接
    public static String generateJxtNewsDetailView(JxtNews jxtNews, String channelId, String ver) {
        String host = ProductConfig.getMainSiteBaseUrl();
        String uri = "/view/mobile/parent/information/detail";
        String url;
        if (host.endsWith("/")) {
            url = host + uri;
        } else {
            url = host + "/" + uri;
        }
        return url + "?newsId=" + jxtNews.getId() + "&rel=" + channelId + "&ut=" + jxtNews.getUpdateTime().getTime() + "&style_type=" + jxtNews.generateStyleType() + "&content_type=" + jxtNews.generateContentType() + "&app_version=" + ver;
    }

    //生成资讯详情页的链接
    public static String generateJxtNewsDetailViewForPushWihtoutHost(JxtNews jxtNews, String channelId, boolean isForShare) {
        String uri = isForShare ? "/view/mobile/parent/information/transpond" : "/view/mobile/parent/information/detail";
        return uri + "?newsId=" + jxtNews.getId() + "&rel=" + channelId + "&ut=" + jxtNews.getUpdateTime().getTime() + "&style_type=" + jxtNews.generateStyleType() + "&content_type=" + jxtNews.generateContentType();
    }

    //生成专辑详情页的链接
    public static String generateAlbumDetailView(String albumId, String channelId, Enum contentType, String newsId, String ver) {
        if (StringUtils.isBlank(albumId) || StringUtils.isBlank(ver)) {
            return "";
        }
        String host = ProductConfig.getMainSiteBaseUrl();
        String uri = "/view/mobile/parent/information/album_detail";
        if (contentType != null && (contentType.equals(ParentResData.ContentType.AUDIO) || contentType.equals(ParentResData.ContentType.VIDEO)) && VersionUtil.checkVersionConfig(">=1.9.0", ver)) {
            uri = "/view/mobile/parent/album/detail.vpage";
        }
        String url;
        if (host.endsWith("/")) {
            url = host + uri;
        } else {
            url = host + "/" + uri;
        }
        if (contentType != null && (contentType.equals(ParentResData.ContentType.AUDIO) || contentType.equals(ParentResData.ContentType.VIDEO)) && VersionUtil.checkVersionConfig(">=1.9.0", ver)) {
            if (StringUtils.isNotBlank(newsId)) {
                return url + "?rel=list_" + channelId + "&album_id=" + albumId + "&news_id=" + newsId;
            }
            return url + "?rel=list_" + channelId + "&album_id=" + albumId;
        }
        return url + "?rel=list_" + channelId + "&albumId=" + albumId;
    }

    //格式化时间
    public static String formatTime(int secondTime) {
        String timeStr;
        int hour;
        int minute;
        int second;
        if (secondTime <= 0) {
            return "00:00";
        } else {
            minute = secondTime / 60;
            if (minute < 60) {
                second = secondTime % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99) {
                    return "99:59:59";
                }
                minute = minute % 60;
                second = secondTime - hour * 3600 - minute * 60;
                if (hour <= 0) {
                    timeStr = unitFormat(minute) + ":" + unitFormat(second);
                } else {
                    timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
                }
            }
        }
        return timeStr;
    }

    private static String unitFormat(int i) {
        String retStr;
        if (i >= 0 && i < 10) {
            retStr = "0" + Integer.toString(i);
        } else {
            retStr = "" + i;
        }
        return retStr;
    }
}
