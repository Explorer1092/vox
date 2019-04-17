package com.voxlearning.wechat.context;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.constants.MessageType;
import com.voxlearning.wechat.support.utils.MessageFields;
import lombok.NoArgsConstructor;

/**
 * @author Xin Xin
 * @since 10/16/15
 */
@NoArgsConstructor
public class MessageContext {
    //FIXME:目前属性字段的类型支持String、Long、Double、Integer、BigDecimal、MessageType
    //FIXME:如果要添加新类型，需要同时修改MessageUtils里的消息解析逻辑
    //FIXME:一定有更好的办法来解析，怎么弄呢？

    private String toUserName;
    private String fromUserName;
    private Long createTime;
    private MessageType msgType;
    private String msgId;
    private String msgID; //模板消息通知里的字段,shit!
    private String content;
    private String picUrl;
    private String mediaId;
    private String format;
    private String thumbMediaId;
    private String recognition;
    private Double location_X;
    private Double location_Y;
    private Integer scale;
    private String label;
    private String title;
    private String description;
    private String url;

    private String event;
    private String eventKey;
    private String ticket;
    private String latitude;
    private String longitude;
    private String precision;
    private String status;

    private String kfAccount;
    private String menuId; //微信忽然新增的字段,暂时没用到

    //群发结果推送专有字段
    private Integer totalCount;
    private Integer filterCount;
    private Integer sentCount;
    private Integer errorCount;

    /**
     * 获取消息体的识别标识，用于配置消息处理的handler
     * 各种标识：
     * Content    文本消息
     * PicUrl:MediaId    图片消息
     * MediaId:Format    音频消息
     * MediaId:ThumbMediaId  视频消息、小视频消息
     * Location_X:Location_Y:Scale:Label     地理位置消息
     * Title:Description:Url     链接消息
     * Event      关注、取消关注
     * Event:EventKey:Ticket   扫描二维码
     * Event:Latitude:Longitude:Precision  上报地理位置
     * Event:EventKey  菜单点击
     * Event:Status    模板消息结果通知
     * Event:KfAccount 多客服会话状态通知事件
     */
    public String getFingerprint(WechatType type) {
        StringBuilder sb = new StringBuilder();
        String delimiter = ":";

        sb.append(type.name()).append(delimiter);
        if (null != content) {
            sb.append(MessageFields.FIELD_CONTENT).append(delimiter);
        }
        if (!StringUtils.isBlank(picUrl)) {
            sb.append(MessageFields.FIELD_PIC_URL).append(delimiter);
        }
        if (!StringUtils.isBlank(mediaId)) {
            sb.append(MessageFields.FIELD_MEDIA_ID).append(delimiter);
        }
        if (!StringUtils.isBlank(format)) {
            sb.append(MessageFields.FIELD_FORMAT).append(delimiter);
        }
        if (!StringUtils.isBlank(thumbMediaId)) {
            sb.append(MessageFields.FIELD_THUMB_MEDIA_ID).append(delimiter);
        }
        if (null != location_X) {
            sb.append(MessageFields.FIELD_LOCATION_X).append(delimiter);
        }
        if (null != location_Y) {
            sb.append(MessageFields.FIELD_LOCATION_Y).append(delimiter);
        }
        if (null != scale) {
            sb.append(MessageFields.FIELD_SCALE).append(delimiter);
        }
        if (null != label) {
            sb.append(MessageFields.FIELD_LABEL).append(delimiter);
        }
        if (!StringUtils.isBlank(title)) {
            sb.append(MessageFields.FIELD_TITLE).append(delimiter);
        }
        if (!StringUtils.isBlank(description)) {
            sb.append(MessageFields.FIELD_DESCRIPTION).append(delimiter);
        }
        if (!StringUtils.isBlank(url)) {
            sb.append(MessageFields.FIELD_URL).append(delimiter);
        }
        if (!StringUtils.isBlank(event)) {
            sb.append(MessageFields.FIELD_EVENT).append(delimiter);
        }
        if (!StringUtils.isBlank(eventKey)) {
            sb.append(MessageFields.FIELD_EVENT_KEY).append(delimiter);
        }
        if (!StringUtils.isBlank(ticket)) {
            sb.append(MessageFields.FIELD_TICKET).append(delimiter);
        }
        if (!StringUtils.isBlank(latitude)) {
            sb.append(MessageFields.FIELD_LATITUDE).append(delimiter);
        }
        if (!StringUtils.isBlank(longitude)) {
            sb.append(MessageFields.FIELD_LONGITUDE).append(delimiter);
        }
        if (!StringUtils.isBlank(precision)) {
            sb.append(MessageFields.FIELD_PRECISION).append(delimiter);
        }
        if (!StringUtils.isBlank(status)) {
            sb.append(MessageFields.FIELD_STATUS).append(delimiter);
        }
        if (!StringUtils.isBlank(kfAccount)) {
            sb.append(MessageFields.FIELD_KF_ACCOUNT).append(delimiter);
        }
        if (null != totalCount) {
            sb.append(MessageFields.FILED_TOTAL_COUNT).append(delimiter);
        }
        if (null != filterCount) {
            sb.append(MessageFields.FILED_FILTER_COUNT).append(delimiter);
        }
        if (null != sentCount) {
            sb.append(MessageFields.FILED_SENT_COUNT).append(delimiter);
        }
        if (null != errorCount) {
            sb.append(MessageFields.FILED_ERROR_COUNT).append(delimiter);
        }

        if (sb.length() > 0) {
            return sb.substring(0, sb.length() - 1);
        }

        throw new IllegalStateException("create message fingerprint failed,msg:" + JsonUtils.toJson(this));
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public void setMsgType(MessageType msgType) {
        this.msgType = msgType;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getThumbMediaId() {
        return thumbMediaId;
    }

    public void setThumbMediaId(String thumbMediaId) {
        this.thumbMediaId = thumbMediaId;
    }

    public String getRecognition() {
        return recognition;
    }

    public void setRecognition(String recognition) {
        this.recognition = recognition;
    }

    public Double getLocation_X() {
        return location_X;
    }

    public void setLocation_X(Double locationX) {
        this.location_X = locationX;
    }

    public Double getLocation_Y() {
        return location_Y;
    }

    public void setLocation_Y(Double locationY) {
        this.location_Y = locationY;
    }

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKfAccount() {
        return kfAccount;
    }

    public void setKfAccount(String kfAccount) {
        this.kfAccount = kfAccount;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getFilterCount() {
        return filterCount;
    }

    public void setFilterCount(Integer filterCount) {
        this.filterCount = filterCount;
    }

    public Integer getSentCount() {
        return sentCount;
    }

    public void setSentCount(Integer sentCount) {
        this.sentCount = sentCount;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }
}
