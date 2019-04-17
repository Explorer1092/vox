package com.voxlearning.utopia.enanalyze.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlPrimaryKeyGeneratorType;
import com.voxlearning.utopia.enanalyze.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
@DocumentConnection(configName = "main")
@DocumentTable(table = "EA_USER")
@UtopiaCacheExpiration
public class UserEntity implements Serializable {

    @UtopiaSqlColumn(name = "ID", primaryKey = true, primaryKeyGeneratorType = UtopiaSqlPrimaryKeyGeneratorType.AUTO_INC)
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    private Long id;

    @UtopiaSqlColumn(name = "OPEN_ID")
    private String openId;

    @UtopiaSqlColumn(name = "NICK_NAME")
    private String nickName;

    @UtopiaSqlColumn(name = "GENDER")
    private String gender;

    @UtopiaSqlColumn(name = "CITY")
    private String city;

    @UtopiaSqlColumn(name = "PROVINCE")
    private String province;

    @UtopiaSqlColumn(name = "AVATAR_URL")
    private String avatarUrl;

    @UtopiaSqlColumn(name = "UNION_ID")
    private String unionId;

    @UtopiaSqlColumn(name = "CREATE_DATE")
    private Date createDate;

    @UtopiaSqlColumn(name = "UPDATE_DATE")
    private Date updateDate;

    @UtopiaSqlColumn(name = "SESSION_KEY")
    private String sessionKey;

    /**
     * 类型
     *
     * @see Type
     */
    @UtopiaSqlColumn(name = "TYPE")
    private String type;

    /**
     * 类型
     */
    @AllArgsConstructor
    public enum Type {
        EN_ANALYZE("英语作文批改");
        public final String desc;
    }

    public static class Builder {
        public static UserEntity build(User user) {
            UserEntity entity = new UserEntity();
            entity.setOpenId(user.getOpenId());
            entity.setNickName(user.getNickName());
            entity.setAvatarUrl(user.getAvatarUrl());
            entity.setGender(user.getGender());
            entity.setCity(user.getCity());
            entity.setProvince(user.getProvince());
            return entity;
        }
    }

}
