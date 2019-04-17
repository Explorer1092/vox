/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.support;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.mutable.MutableLong;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

abstract public class AbstractGameSupportController extends AbstractController {
    @Getter
    protected enum SecureKeyType {
        PK通天塔通用("PK_BABEL_SECURE_KEY", 86400, 3, 60),
        沃克跑酷("WAlKER_PARKOUR_SECURE_KEY", 86400, 3, 60);
        private String cacheKey;
        private int cacheExpireSeconds;
        private int maxErrorCount;
        private int errorSuspendTime;

        private SecureKeyType(String cacheKey, int cacheExpireSeconds, int maxErrorCount, int errorSuspendTime) {
            this.cacheKey = cacheKey;
            this.cacheExpireSeconds = cacheExpireSeconds;
            this.maxErrorCount = maxErrorCount;
            this.errorSuspendTime = errorSuspendTime;
        }

    }

    protected String genSecureKeyForCurrentUser(SecureKeyType secureType) {
        return this.genSecureKey(currentUserId(), secureType);
    }

    private String genSecureKey(Long userId, SecureKeyType secureType) {
        if (null == userId) {
            return "";
        }
        Map<String, String> cont = new HashMap<>();
        cont.put("userId", String.valueOf(userId));
        String key = DigestSignUtils.signMd5(cont, String.valueOf(System.currentTimeMillis()));
        String cacheKey = secureType.getCacheKey() + userId;
        boolean setSuccess = false;
        int errorCounter = 0;
        while (!setSuccess) {
            Boolean ret = washingtonCacheSystem.CBS.unflushable.set(cacheKey, secureType.cacheExpireSeconds, key);
            setSuccess = Boolean.TRUE.equals(ret);
            if (setSuccess) {
                logger.debug("USERID:" + userId + ".type:" + secureType + ".genKey:" + DigestUtils.md5Hex(userId + key));
            } else {
                errorCounter++;
                if (errorCounter > 10) {
                    logger.error("Gen secure key {} failed for {} times.give up.retry", cacheKey, errorCounter);
                    break;
                }
                logger.error("Gen secure key failed for {}.retry", cacheKey);
            }
        }
        return key;
    }

    protected String getCurrentKey(Long userId, SecureKeyType secureType) {
        if (null == userId) {
            return "";
        }
        String cacheKey = secureType.getCacheKey() + userId;

        CacheObject<String> cacheObject = washingtonCacheSystem.CBS.unflushable.get(cacheKey);
        if (cacheObject == null) {
            logger.error("get secure key failed for {}", cacheKey);
            return null;
        }

        String keyGet = cacheObject.getValue();
        if (null == keyGet) {
            keyGet = genSecureKey(userId, secureType);
        } else {
            logger.debug("USERID:" + userId + ".type:" + secureType + ".queryForKey:" + DigestUtils.md5Hex(userId + keyGet));
            // FIXME: touch not supported?
            // couchbase.touch(cacheKey, 86400);
            washingtonCacheSystem.CBS.unflushable.set(cacheKey, 86400, keyGet);
        }
        return keyGet;
    }

    protected String checkKeyForCurrentUser(String tokenIn, SecureKeyType secureType) {
        return checkSecureKey(tokenIn, secureType, currentUserId());
    }

    private String checkSecureKey(String tokenIn, SecureKeyType secureType, Long userId) {
        CacheObject<Long> errorCountResponse = washingtonCacheSystem.CBS.flushable.get(secureType.getCacheKey() + "_error_count_" + userId);
        if (errorCountResponse == null) {
            logger.error("token check failed failed {}", secureType);
            return "";
        }
        Long errorCount = errorCountResponse.getValue();
        if (null == errorCount) {
            errorCount = 0L;
        }
        if (secureType.maxErrorCount <= errorCount) {
            increErrorSecureCount(secureType, userId);
            return userId + "WRONG TOKEN TIME >=" + secureType.maxErrorCount;
        }

        String cacheKey = secureType.getCacheKey() + userId;
        CacheObject<String> serverKeyResponse = washingtonCacheSystem.CBS.unflushable.get(cacheKey);
        if (serverKeyResponse == null) {
            logger.error("token check failed failed {}", secureType);
            return "";
        }

        String serverKey = serverKeyResponse.getValue();
        if (null == serverKey) {
            return "";
        }

        String md5Rs = DigestUtils.md5Hex(userId + serverKey);
        if (StringUtils.equals(md5Rs, tokenIn)) {
            return "";
        } else {
            increErrorSecureCount(secureType, userId);
            return "USERID:" + userId + ".type:" + secureType + "WRONG TOKEN,STORED:" + md5Rs + ",IN:" + tokenIn;
        }
    }

    private long increErrorSecureCount(SecureKeyType secureType, Long userId) {
        String cacheKey = secureType.getCacheKey() + "_error_count_" + userId;

        CacheObject<Long> response = washingtonCacheSystem.CBS.flushable.get(cacheKey);
        if (response == null) {
            logger.error("inc failed {}", cacheKey);
            return 0;
        }

        if (response.getValue() == null) {
            Boolean ret = washingtonCacheSystem.CBS.flushable.add(cacheKey, secureType.errorSuspendTime, 1L);
            if (!Boolean.TRUE.equals(ret)) {
                logger.error("inc failed {}", cacheKey);
                return 0;
            }
            return 1;
        } else {
            final MutableLong count = new MutableLong(0);
            Boolean ret = washingtonCacheSystem.CBS.flushable.cas(cacheKey, secureType.errorSuspendTime, response, new ChangeCacheObject<Long>() {
                @Override
                public Long changeCacheObject(Long currentValue) {
                    count.setValue(currentValue + 1);
                    return count.getValue();
                }
            });
            if (!Boolean.TRUE.equals(ret)) {
                logger.error("inc failed {}", cacheKey);
                return 0;
            }
            return count.longValue();
        }
    }

    @Getter
    public enum GameVitalityType {
        //@Deprecated
        //通天塔活力(5, 86400, 0, 120 * 60 * 1000, "VOX_BABEL_PLAYER_VITALITY", 0),

        // FIXME: 增加revision，修正缓存类型不匹配（因为KeyValuePair）
        热血跑酷活力(5, 86400, 0, 60 * 60 * 1000, "VOX_NEKKETSU_PARKOUR_PLAYER_VITALITY:20160112:", 1),
        WALKER_ELF(3, 86400, 0, 0, "VOX_WALKER_ELF_VITALITY:20160112:", 0);
        private int maxVitality;
        private int cacheExpire;
        private int refillHour;
        private int refillIncrement;
        private long increaseIntevalMilliSecond;
        private String cacheKey;

        private GameVitalityType(int maxVitality, int cacheExpire, int refillHour, long increaseIntevalMilliSecond, String cacheKey, int refillIncrement) {
            this.cacheExpire = cacheExpire;
            this.cacheKey = cacheKey;
            this.refillHour = refillHour;
            this.increaseIntevalMilliSecond = increaseIntevalMilliSecond;
            this.maxVitality = maxVitality;
            this.refillIncrement = refillIncrement;
        }

        private String genCacheKeyForUser(long userId) {
            return cacheKey + userId;
        }

        /**
         * 刷新活力 获取当前活力
         */
        public KeyValuePair<Integer, Integer> refreshVitality(long userId,
                                                              UtopiaCache cacheManager) {
            String cacheKey = genCacheKeyForUser(userId);

            // FIXME: 原代码中缓存错误此处被忽略
            CacheObject<KeyValuePair<Integer, Long>> cacheObject = cacheManager.get(cacheKey);
            if (cacheObject == null) {
                return new KeyValuePair<>(maxVitality, 0);
            }

            KeyValuePair<Integer, Long> vitalOrigin = cacheObject.getValue();
            long now = System.currentTimeMillis();
            if (null == vitalOrigin) {//cache已过期，表明用户长期未刷新，直接给满活力
                int newVital = maxVitality;
                KeyValuePair<Integer, Long> vitalNow = new KeyValuePair<>(newVital, now);
                cacheManager.add(cacheKey, cacheExpire, vitalNow);
                return new KeyValuePair<>(vitalNow.getKey(), 0);
            }

            if (refillHour >= 0) {//需要每天定时加满的类型
                long latestRefillTime = findLatestRefreshTime();
                if (vitalOrigin.getValue() < latestRefillTime && now >= latestRefillTime) {
                    int newVital = maxVitality;
                    KeyValuePair<Integer, Long> vitalNow = new KeyValuePair<>(newVital, now);
                    cacheManager.set(cacheKey, cacheExpire, vitalNow);
                    return new KeyValuePair<>(vitalNow.getKey(), 0);
                }
            }
            int oldVital = vitalOrigin.getKey();
            long timeDiff = now - vitalOrigin.getValue();//距上次自动增加活力已过去多久
            int autoIncrement = 0 == increaseIntevalMilliSecond * refillIncrement ? 0 : (int) (timeDiff / increaseIntevalMilliSecond * refillIncrement);//距上次更新活力后，需增加几点活力
            long refillCountdown = 0 == increaseIntevalMilliSecond * refillIncrement ? 0 : (increaseIntevalMilliSecond - (timeDiff % increaseIntevalMilliSecond));//倒计时
            int newVital = oldVital + autoIncrement;
            long newTime = vitalOrigin.getValue() + increaseIntevalMilliSecond * autoIncrement;
            if (newTime > now) {
                newTime = now;
            }
            if (newVital >= maxVitality) {
                newVital = maxVitality;
                refillCountdown = 0;
            }
            KeyValuePair<Integer, Long> vitalNow = new KeyValuePair<>(newVital, newTime);
            cacheManager.set(cacheKey, cacheExpire, vitalNow);
            refillCountdown = refillCountdown == increaseIntevalMilliSecond ? refillCountdown + 1 : refillCountdown;
            return new KeyValuePair<>(vitalNow.getKey(), (int) (refillCountdown / 1000));
        }

        @SuppressWarnings("unchecked")
        public KeyValuePair<Integer, Integer> incVitality(long userId,
                                                          UtopiaCache cacheManager,
                                                          int inc) {
            KeyValuePair<Integer, Integer> nowVital = refreshVitality(userId, cacheManager);
            KeyValuePair<Integer, Long> vitalOrigin = null;
            String cacheKey = genCacheKeyForUser(userId);

            // FIXME: 原代码中缓存错误此处被忽略
            CacheObject<Object> cacheObject = cacheManager.get(cacheKey);
            if (cacheObject != null) {
                vitalOrigin = (KeyValuePair<Integer, Long>) cacheObject.getValue();
            }
            if (null == vitalOrigin) {
                return new KeyValuePair<>(0, 0);
            }
            long refreshTime = vitalOrigin.getValue();
            int newVital = nowVital.getKey() + inc;
            if (newVital < 0) {
                return new KeyValuePair<>(newVital, 0);
            }
            int countDown = nowVital.getValue();
            if (newVital < nowVital.getKey() && nowVital.getKey() == maxVitality) {//刚从满活力减少
                countDown = (int) (increaseIntevalMilliSecond / 1000);
                refreshTime = System.currentTimeMillis();
            }
            nowVital.setKey(newVital);
            nowVital.setValue(countDown);

            // FIXME: 原代码中缓存错误此处被忽略
            cacheManager.set(genCacheKeyForUser(userId), cacheExpire, new KeyValuePair<>(newVital, refreshTime));
            return nowVital;
        }

        public KeyValuePair<Integer, Integer> fillVitality(long userId,
                                                           UtopiaCache cacheManager) {
            // FIXME: 原代码中缓存错误此处被忽略
            cacheManager.set(genCacheKeyForUser(userId), cacheExpire, new KeyValuePair<>(maxVitality, System.currentTimeMillis()));
            return new KeyValuePair<>(maxVitality, 0);
        }

        /**
         * 算出最近一次系统刷满活力的时间
         */
        private long findLatestRefreshTime() {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, refillHour);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            if (cal.getTimeInMillis() > System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, -1);
            }
            return cal.getTimeInMillis();
        }
    }
}
