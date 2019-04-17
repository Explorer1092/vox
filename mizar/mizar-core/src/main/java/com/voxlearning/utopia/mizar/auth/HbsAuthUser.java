package com.voxlearning.utopia.mizar.auth;

import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 华杯赛用户的权限bean
 * Created by haitian.gan on 2017/2/15.
 */
@Getter
@Setter
@NoArgsConstructor
public class HbsAuthUser implements Serializable{

    private Long userId;
    private String accountName;
    private String realName;

    public static String ck_user(Long id){
        return CacheKeyGenerator.generateCacheKey(HbsAuthUser.class,id);
    }
}
