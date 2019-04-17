package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.vendor.api.constant.TextBookSdkType;
import com.voxlearning.utopia.service.vendor.api.constant.TextBookSourceType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by jiang wei on 2017/4/5.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "vox_textbook_manager")
public class TextBookManagement implements Serializable, CacheDimensionDocument {

    private static final long serialVersionUID = -6962534404799427824L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String bookId;    //自研Id
    private TextBookSourceType sourceType;   //来源
    @Deprecated
    private String refBookId;      //NamiId
    private Boolean isFollowRead;     //是否跟读
    private Boolean hasWordList;      //是否有单词表
    private PicListenConfig picListenConfig; //点读机相关配置
    private WalkManConfig walkManConfig;     //随身听相关配置
    private TextReadConfig textReadConfig;   //语文朗读相关配置
    private String comment;           //备注
    private String operateUser;       //操作人

    private String bookListName; //教材列表上显示的教材名称

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @DocumentFieldIgnore
    private String shortPublisherName; //出版社简称
    @DocumentFieldIgnore
    private Integer clazzLevel;    //教材年级
    @DocumentFieldIgnore
    private Integer termType;      //教材学期
    private Integer subjectId; // 这里冗余一个subjectId
    private Boolean chineseWordSupport; //语文生词表

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }

    /**
     * 点读机配置
     */
    @Getter
    @Setter
    public static class PicListenConfig implements Serializable {

        private static final long serialVersionUID = -7999538603590467306L;
        private Boolean isAndroidOnline;
        private Boolean isIOSOnline;
        private Boolean isAuthUserOnline;
        private Boolean isFree;
        private Boolean isPreview;
        private Boolean isMiniProgramOnline; // 默认是Null，null 是下线
        @Deprecated
        private String sdkBookId;
        private SdkInfo sdkInfo;

        @Override
        public String toString() {
            return "PicListenConfig{" +
                    "isAndroidOnline=" + isAndroidOnline +
                    ", isIOSOnline=" + isIOSOnline +
                    ", isAuthUserOnline=" + isAuthUserOnline +
                    ", isFree=" + isFree +
                    ", isPreview=" + isPreview +
                    ", isMiniProgramOnline=" + isMiniProgramOnline +
                    ", sdkBookId='" + sdkBookId + '\'' +
                    ", sdkInfo=" + sdkInfo +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PicListenConfig that = (PicListenConfig) o;

            if (isAndroidOnline != null ? !isAndroidOnline.equals(that.isAndroidOnline) : that.isAndroidOnline != null)
                return false;
            if (isIOSOnline != null ? !isIOSOnline.equals(that.isIOSOnline) : that.isIOSOnline != null) return false;
            if (isAuthUserOnline != null ? !isAuthUserOnline.equals(that.isAuthUserOnline) : that.isAuthUserOnline != null)
                return false;
            if (isFree != null ? !isFree.equals(that.isFree) : that.isFree != null) return false;
            if (isPreview != null ? !isPreview.equals(that.isPreview) : that.isPreview != null) return false;
            if (isMiniProgramOnline != null ? !isMiniProgramOnline.equals(that.isMiniProgramOnline) : that.isMiniProgramOnline != null)
                return false;
            if (sdkBookId != null ? !sdkBookId.equals(that.sdkBookId) : that.sdkBookId != null) return false;
            return sdkInfo != null ? sdkInfo.equals(that.sdkInfo) : that.sdkInfo == null;
        }

        @Override
        public int hashCode() {
            int result = isAndroidOnline != null ? isAndroidOnline.hashCode() : 0;
            result = 31 * result + (isIOSOnline != null ? isIOSOnline.hashCode() : 0);
            result = 31 * result + (isAuthUserOnline != null ? isAuthUserOnline.hashCode() : 0);
            result = 31 * result + (isFree != null ? isFree.hashCode() : 0);
            result = 31 * result + (isPreview != null ? isPreview.hashCode() : 0);
            result = 31 * result + (isMiniProgramOnline != null ? isMiniProgramOnline.hashCode() : 0);
            result = 31 * result + (sdkBookId != null ? sdkBookId.hashCode() : 0);
            result = 31 * result + (sdkInfo != null ? sdkInfo.hashCode() : 0);
            return result;
        }
    }

    public Boolean isStagingData() {
        return this.bookId.contains("_staging");
    }

    public TextBookManagement convertStagingId2OnlineId() {
        this.setBookId(this.getBookId().replace("_staging", ""));
        return this;
    }


    /**
     * 随身听配置
     */
    @Getter
    @Setter
    public static class WalkManConfig implements Serializable {

        private static final long serialVersionUID = 181081067140121062L;
        private Boolean isAndroidOnline;
        private Boolean isIOSOnline;
        private Boolean isFree;  //随身听是否免费
        private String leastVersion;    //付费随身听最小支持版本
        private Boolean isMiniProgramOnline; // 默认是Null，null 是下线

        @Override
        public String toString() {
            return "WalkManConfig{" +
                    "isAndroidOnline=" + isAndroidOnline +
                    ", isIOSOnline=" + isIOSOnline +
                    ", isFree=" + isFree +
                    ", isMiniProgramOnline=" + isMiniProgramOnline +
                    ", leastVersion=" + leastVersion +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WalkManConfig that = (WalkManConfig) o;

            if (isAndroidOnline != null ? !isAndroidOnline.equals(that.isAndroidOnline) : that.isAndroidOnline != null)
                return false;
            if (isFree != null ? !isFree.equals(that.isFree) : that.isFree != null) return false;
            if (isMiniProgramOnline != null ? !isMiniProgramOnline.equals(that.isMiniProgramOnline) : that.isMiniProgramOnline != null)
                return false;
            return isIOSOnline != null ? isIOSOnline.equals(that.isIOSOnline) : that.isIOSOnline == null;
        }

        @Override
        public int hashCode() {
            int result = isAndroidOnline != null ? isAndroidOnline.hashCode() : 0;
            result = 31 * result + (isIOSOnline != null ? isIOSOnline.hashCode() : 0);
            result = 31 * result + (isMiniProgramOnline != null ? isMiniProgramOnline.hashCode() : 0);
            return result;
        }
    }


    /**
     * 语文朗读配置
     */
    @Getter
    @Setter
    public static class TextReadConfig implements Serializable {

        private static final long serialVersionUID = -4280738140958509571L;
        private Boolean isAndroidOnline;
        private Boolean isIOSOnline;

        @Override
        public String toString() {
            return "TextReadConfig{" +
                    "isAndroidOnline=" + isAndroidOnline +
                    ", isIOSOnline=" + isIOSOnline +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TextReadConfig that = (TextReadConfig) o;

            if (isAndroidOnline != null ? !isAndroidOnline.equals(that.isAndroidOnline) : that.isAndroidOnline != null)
                return false;
            return isIOSOnline != null ? isIOSOnline.equals(that.isIOSOnline) : that.isIOSOnline == null;
        }

        @Override
        public int hashCode() {
            int result = isAndroidOnline != null ? isAndroidOnline.hashCode() : 0;
            result = 31 * result + (isIOSOnline != null ? isIOSOnline.hashCode() : 0);
            return result;
        }
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class SdkInfo implements Serializable {
        private static final long serialVersionUID = -1628763941203122234L;
        private TextBookSdkType sdkType;
        private String sdkBookId;
        private String renjiaoNewSdkBookId;

        public String getSdkBookIdV2() {
            if (sdkType == TextBookSdkType.renjiao) {
                return StringUtils.isNotBlank(renjiaoNewSdkBookId) ? renjiaoNewSdkBookId : sdkBookId;
            }
            return sdkBookId;
        }

        public String getSdkBookIdV2(String version) {
            if (StringUtils.isBlank(version)) {
                return sdkBookId;
            }
            if (sdkType == TextBookSdkType.renjiao && VersionUtil.compareVersion(version, "2.8.8.0") > 0) {
                return StringUtils.isNotBlank(renjiaoNewSdkBookId) ? renjiaoNewSdkBookId : sdkBookId;
            }
            return sdkBookId;
        }



        public static SdkInfo NONE_SDK;

        static {
            NONE_SDK = new SdkInfo();
            NONE_SDK.sdkBookId = "";
            NONE_SDK.renjiaoNewSdkBookId = "";
            NONE_SDK.sdkType = TextBookSdkType.none;
        }

    }

    @DocumentFieldIgnore
    public Subject fetchBookSubject() {
        return Subject.fromSubjectId(subjectId);
    }


    //点读机相关方法
    @DocumentFieldIgnore
    public Boolean picListenSysSupport(String sys) {
        if (picListenConfig == null)
            return false;
        if ("ios".equalsIgnoreCase(sys))
            return SafeConverter.toBoolean(picListenConfig.getIsIOSOnline());
        else if ("mini_program".equalsIgnoreCase(sys)) {
            return SafeConverter.toBoolean(picListenConfig.getIsMiniProgramOnline());
        }
        else
            return SafeConverter.toBoolean(picListenConfig.getIsAndroidOnline());
    }

    /**
     * instead of picListenSdkInfo
     *
     * @return
     */
    @Deprecated
    @DocumentFieldIgnore
    public String picListenSdkBookId() {
        if (picListenConfig == null)
            return null;
        return picListenConfig.getSdkBookId();
    }

    @DocumentFieldIgnore
    public SdkInfo picListenSdkInfo() {
        if (picListenConfig == null || picListenConfig.getSdkInfo() == null)
            return SdkInfo.NONE_SDK;
        return picListenConfig.getSdkInfo();
    }

    @DocumentFieldIgnore
    public Boolean picListenBookNeedPay() {
        if (picListenConfig == null)
            return false;
        return !SafeConverter.toBoolean(picListenConfig.getIsFree(), true);
    }

    @DocumentFieldIgnore
    public Boolean picListenIsPreview() {
        if (picListenConfig == null)
            return false;
        return SafeConverter.toBoolean(picListenConfig.getIsPreview());
    }

    @DocumentFieldIgnore
    public Boolean picListenAuthOnline() {
        if (picListenConfig == null)
            return false;
        return SafeConverter.toBoolean(picListenConfig.getIsAuthUserOnline());
    }

    @DocumentFieldIgnore
    public Boolean picListenMiniProgramOnline() {
        if (picListenConfig == null)
            return true;
        return SafeConverter.toBoolean(picListenConfig.getIsMiniProgramOnline(), false);
    }

    @DocumentFieldIgnore
    public Boolean walkManMiniProgramOnline() {
        if (walkManConfig == null)
            return true;
        return SafeConverter.toBoolean(walkManConfig.getIsMiniProgramOnline(), false);
    }


    //跟读
    @DocumentFieldIgnore
    public Boolean followReadSupport() {
        return SafeConverter.toBoolean(isFollowRead);
    }

    @DocumentFieldIgnore
    public Boolean englishWordSupport() {
        return SafeConverter.toBoolean(hasWordList);
    }

    @DocumentFieldIgnore
    public Boolean chineseWordSupport() {
        return SafeConverter.toBoolean(chineseWordSupport);
    }

    @DocumentFieldIgnore
    public Boolean readingSupport() {
        return false; // 48745 下掉所有绘本入口
    }


    //语文朗读
    @DocumentFieldIgnore
    public Boolean textReadSysSupport(String sys) {
        if (textReadConfig == null)
            return false;
        if ("ios".equalsIgnoreCase(sys))
            return SafeConverter.toBoolean(textReadConfig.getIsIOSOnline());
        else
            return SafeConverter.toBoolean(textReadConfig.getIsAndroidOnline());
    }

    @DocumentFieldIgnore
    public Boolean textReadBookNeedPay() {
        return false; //目前语文朗读没有要付费的
    }

    //随身听
    @DocumentFieldIgnore
    public Boolean walkManSysSupport(String sys) {
        if (walkManConfig == null)
            return false;
        if ("ios".equalsIgnoreCase(sys))
            return SafeConverter.toBoolean(walkManConfig.getIsIOSOnline());
        else if ("mini_program".equalsIgnoreCase(sys)) {
            return SafeConverter.toBoolean(walkManConfig.getIsMiniProgramOnline());
        } else {
        }
            return SafeConverter.toBoolean(walkManConfig.getIsAndroidOnline());
    }

    //随身听
    @DocumentFieldIgnore
    public Boolean walkManVersionSupport(String version) {
        return walkManConfig != null && VersionUtil.compareVersion(version, walkManConfig.getLeastVersion()) >= 0;
    }

    public Boolean walkManNeedPay() {
        return walkManConfig != null && !SafeConverter.toBoolean(walkManConfig.getIsFree(), true);
    }
}
