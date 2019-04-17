package com.voxlearning.utopia.service.reward.impl.internal;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.reward.api.mapper.CommentEntriesMapper;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.entity.newversion.*;
import com.voxlearning.utopia.service.reward.impl.dao.*;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class InternalTobyService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private RewardCenterTobyDao rewardCenterMyTobyDao;
    @Inject
    private TobyDressDao tobyDressDao;
    @Inject
    private TobyImageCVRecordDao tobyImageCVRecordDao;
    @Inject
    private TobyAccessoryCVRecordDao tobyAccessoryCVRecordDao;
    @Inject
    private TobyCountenanceCVRecordDao tobyCountenanceCVRecordDao;
    @Inject
    private TobyPropsCVRecordDao tobyPropsCVRecordDao;
    @Inject
    private RewardProductDao rewardProductDao;

    public RewardCenterToby loadTobyByUserId(Long userId) {
        RewardCenterToby toby = rewardCenterMyTobyDao.loadByUserId(userId);
        if (toby == null) {
            toby = RewardCenterToby.defaultBean(userId);
        }
        return toby;
    }

    public RewardCenterToby upsertToby(RewardCenterToby toby) {
        return rewardCenterMyTobyDao.upsert(toby);
    }

    public TobyDress upsertTobyDress(TobyDress tobyDress) {
        return tobyDressDao.upsert(tobyDress);
    }


    public List<RewardCenterToby> loadTobyByUserIdList(Collection<Long> userIdList) {
        if (userIdList == null || userIdList.isEmpty()) {
            return Collections.emptyList();
        }

        List<RewardCenterToby> tobyList = new ArrayList<>(rewardCenterMyTobyDao.loadByUserIdList(userIdList).values());
        //判断,如果存在没有设置过自己Toby的学生,给返回一个默认形象
        if (CollectionUtils.isEmpty(tobyList)) {
            tobyList = new ArrayList<>();
            for (Long userId : userIdList) {
                RewardCenterToby toby = RewardCenterToby.defaultBean(userId);
                tobyList.add(toby);
            }
        } else if (userIdList.size() > tobyList.size()) {
            Map<Long, String> hasTobyMap = new HashMap<>();
            for ( RewardCenterToby toby : tobyList) {
                hasTobyMap.put(toby.getUserId(), "");
            }
            for (Long userId : userIdList) {
                if (hasTobyMap.containsKey(userId)) {
                    continue;
                }
                RewardCenterToby toby = RewardCenterToby.defaultBean(userId);
                tobyList.add(toby);
            }
        }
        return tobyList;
    }

    public List<TobyDress> loadTobyDresses() {
       return tobyDressDao.loadAll();
    }

    public TobyDress loadTobyDressById(Long id) {
        return tobyDressDao.loadOne(id);
    }

    public RewardProduct loadProductById(Long id) {
        return rewardProductDao.load(id);
    }

    /**
     * key countenanceId
     * value TobyCountenanceCVRecord
     * @param userId
     * @return
     */
    public List<TobyCountenanceCVRecord> loadTobyCountenanceListByUserId(Long userId) {
        return tobyCountenanceCVRecordDao.loadByUserId(userId);
    }

    public Boolean isOwn(Long userId, Long id) {
        List<TobyCountenanceCVRecord> countenanceCVRecords = tobyCountenanceCVRecordDao.loadByUserId(userId);
        if (CollectionUtils.isNotEmpty(countenanceCVRecords)) {
            TobyCountenanceCVRecord record = countenanceCVRecords.stream()
                    .filter(t -> Objects.equals(id, t.getCountenanceId()))
                    .filter(t -> t.getExpiryTime() > new Date().getTime()).findAny().orElse(null);
            if (Objects.nonNull(record)) {
                return true;
            }
        }
        List<TobyImageCVRecord> imageCVRecords = tobyImageCVRecordDao.loadByUserId(userId);
        if (CollectionUtils.isNotEmpty(imageCVRecords)) {
            TobyImageCVRecord record = imageCVRecords.stream()
                    .filter(t -> Objects.equals(id, t.getImageId()))
                    .filter(t -> t.getExpiryTime() > new Date().getTime()).findAny().orElse(null);
            if (Objects.nonNull(record)) {
                return true;
            }
        }
        List<TobyPropsCVRecord> propsCVRecords = tobyPropsCVRecordDao.loadByUserId(userId);
        if (CollectionUtils.isNotEmpty(propsCVRecords)) {
            TobyPropsCVRecord record = propsCVRecords.stream()
                    .filter(t -> Objects.equals(id, t.getPropsId()))
                    .filter(t -> t.getExpiryTime() > new Date().getTime()).findAny().orElse(null);
            if (Objects.nonNull(record)) {
                return true;
            }
        }
        List<TobyAccessoryCVRecord> accessoryCVRecords = tobyAccessoryCVRecordDao.loadByUserId(userId);
        if (CollectionUtils.isNotEmpty(accessoryCVRecords)) {
            TobyAccessoryCVRecord record = accessoryCVRecords.stream()
                    .filter(t -> Objects.equals(id, t.getAccessoryId()))
                    .filter(t -> t.getExpiryTime() > new Date().getTime()).findAny().orElse(null);
            if (Objects.nonNull(record)) {
                return true;
            }
        }
        return false;
    }

    public List<RewardCenterToby> loadAccessoryErrExpiryTime(Long timeStamp) {
        return rewardCenterMyTobyDao.loadAccessoryErrExpiryTime(timeStamp);
    }
    public List<RewardCenterToby> loadCountenanceErrExpiryTime(Long timeStamp) {
        return rewardCenterMyTobyDao.loadCountenanceErrExpiryTime(timeStamp);
    }
    public List<RewardCenterToby> loadImageErrExpiryTime(Long timeStamp) {
        return rewardCenterMyTobyDao.loadImageErrExpiryTime(timeStamp);
    }
    public List<RewardCenterToby> loadPropsErrExpiryTime(Long timeStamp) {
        return rewardCenterMyTobyDao.loadPropsErrExpiryTime(timeStamp);
    }

    public MapMessage cancelTobyDress(Long id, Long userId) {
        RewardCenterToby toby = loadTobyByUserId(userId);
        if (Objects.equals(toby.getImageId(), id)) {
            RewardCenterToby.defaultImage(toby);
            this.upsertToby(toby);
        } else if (Objects.equals(toby.getCountenanceId(), id)) {
            RewardCenterToby.defaultCountenance(toby);
            this.upsertToby(toby);
        } else if (Objects.equals(toby.getPropsId(), id)) {
            RewardCenterToby.defaultProps(toby);
            this.upsertToby(toby);
        } else if (Objects.equals(toby.getAccessoryId(), id)) {
            RewardCenterToby.defaultAccessory(toby);
            this.upsertToby(toby);
        }
        TobyImageCVRecord imageCVRecord = loadUserNewestTobyImage(id, userId);
        if (Objects.nonNull(imageCVRecord)) {
            imageCVRecord.setExpiryTime(-1L);
            imageCVRecord.setStatus(TobyImageCVRecord.Status.CANCEL.getStatus());
            tobyImageCVRecordDao.upsert(imageCVRecord);
            return MapMessage.successMessage();
        }
        TobyCountenanceCVRecord countenanceCVRecord = loadUserNewestTobyCountenance(id, userId);
        if (Objects.nonNull(countenanceCVRecord)) {
            countenanceCVRecord.setExpiryTime(-1L);
            countenanceCVRecord.setStatus(TobyImageCVRecord.Status.CANCEL.getStatus());
            tobyCountenanceCVRecordDao.upsert(countenanceCVRecord);
            return MapMessage.successMessage();
        }
        TobyPropsCVRecord propsCVRecord = loadUserNewestTobyProps(id, userId);
        if (Objects.nonNull(propsCVRecord)) {
            propsCVRecord.setExpiryTime(-1L);
            propsCVRecord.setStatus(TobyImageCVRecord.Status.CANCEL.getStatus());
            tobyPropsCVRecordDao.upsert(propsCVRecord);
            return MapMessage.successMessage();
        }
        TobyAccessoryCVRecord accessoryCVRecord = loadUserNewestTobyAccessory(id, userId);
        if (Objects.nonNull(accessoryCVRecord)) {
            accessoryCVRecord.setExpiryTime(-1L);
            accessoryCVRecord.setStatus(TobyImageCVRecord.Status.CANCEL.getStatus());
            tobyAccessoryCVRecordDao.upsert(accessoryCVRecord);
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }

    public TobyImageCVRecord loadUserNewestTobyImage(Long imageId, Long userId) {
        if (imageId==null || imageId==0) {
            return null;
        }
        List<TobyImageCVRecord> imageCVRecordList = tobyImageCVRecordDao.loadByUserId(userId);
        if (CollectionUtils.isEmpty(imageCVRecordList)) {
            return null;
        }

        TobyImageCVRecord result = imageCVRecordList
                .stream()
                .filter(t -> t.getExpiryTime() > new Date().getTime())
                .map(t -> {
                    if (Objects.equals(imageId, t.getImageId())) {
                        t.setStatus(TobyImageCVRecord.Status.USING.getStatus());
                    }
                    return t;
                })
                .findFirst().orElse(null);
        return result;
    }

    public TobyCountenanceCVRecord loadUserNewestTobyCountenance(Long id, Long userId) {
        if (id==null || id==0) {
            return null;
        }
        List<TobyCountenanceCVRecord> countenanceCVRecords = tobyCountenanceCVRecordDao.loadByUserId(userId);
        if (CollectionUtils.isEmpty(countenanceCVRecords)) {
            return null;
        }
        TobyCountenanceCVRecord result = countenanceCVRecords
                .stream()
                .filter(t -> t.getExpiryTime() > new Date().getTime())
                .map(t -> {
                    if (Objects.equals(id, t.getCountenanceId())) {
                        t.setStatus(TobyImageCVRecord.Status.USING.getStatus());
                    }
                    return t;
                })
                .findFirst().orElse(null);
        return result;
    }

    public TobyPropsCVRecord loadUserNewestTobyProps(Long id, Long userId) {
        if (id==null || id==0) {
            return null;
        }
        List<TobyPropsCVRecord> propsCVRecords = tobyPropsCVRecordDao.loadByUserId(userId);
        if (CollectionUtils.isEmpty(propsCVRecords)) {
            return null;
        }
        TobyPropsCVRecord result = propsCVRecords
                .stream()
                .filter(t -> t.getExpiryTime() > new Date().getTime())
                .map(t -> {
                    if (Objects.equals(id, t.getPropsId())) {
                        t.setStatus(TobyImageCVRecord.Status.USING.getStatus());
                    }
                    return t;
                })
                .findFirst().orElse(null);
        return result;
    }

    public TobyAccessoryCVRecord loadUserNewestTobyAccessory(Long id, Long userId) {
        if (id==null || id==0) {
            return null;
        }
        List<TobyAccessoryCVRecord> accessoryCVRecords = tobyAccessoryCVRecordDao.loadByUserId(userId);
        if (CollectionUtils.isEmpty(accessoryCVRecords)) {
            return null;
        }
        TobyAccessoryCVRecord result = accessoryCVRecords
                .stream()
                .filter(t -> t.getExpiryTime() > new Date().getTime())
                .map(t -> {
                    if (Objects.equals(id, t.getAccessoryId())) {
                        t.setStatus(TobyImageCVRecord.Status.USING.getStatus());
                    }
                    return t;
                })
                .findFirst().orElse(null);
        return result;
    }


    public List<TobyImageCVRecord> loadTobyImageListByUserId(Long userId) {
        return tobyImageCVRecordDao.loadByUserId(userId);
    }

    public List<TobyAccessoryCVRecord> loadTobyAccessoryListByUserId(Long userId) {
        return tobyAccessoryCVRecordDao.loadByUserId(userId);
    }

    public List<TobyPropsCVRecord> loadTobyPropsListByUserId(Long userId) {
        return tobyPropsCVRecordDao.loadByUserId(userId);
    }

    public boolean cvTobyImage(Long id, Long userId, Long activeTimeMs, String url, String name) {
        TobyImageCVRecord futureRecord = null;
        List<TobyImageCVRecord>  recordList = tobyImageCVRecordDao.loadByUserId(userId);
        if (CollectionUtils.isNotEmpty(recordList)) {
            for (TobyImageCVRecord entry : recordList) {
                if (Objects.equals(id, entry.getImageId()) && entry.getExpiryTime() > new Date().getTime()) {
                    futureRecord = entry;
                    break;
                }
            }
        }

        TobyImageCVRecord  usingRecord = recordList.stream()
                .filter(p -> Objects.equals(p.getStatus(), TobyImageCVRecord.Status.USING.getStatus()))
                .findFirst().orElse(null);
        if (usingRecord != null) {
            usingRecord.setStatus(TobyImageCVRecord.Status.OWNED.getStatus());
            tobyImageCVRecordDao.upsert(usingRecord);
        }

        if (futureRecord == null) {
            futureRecord = TobyImageCVRecord.build(id, userId, activeTimeMs);
            tobyImageCVRecordDao.insert(futureRecord);
        } else {
            futureRecord.setStatus(TobyImageCVRecord.Status.USING.getStatus());
            tobyImageCVRecordDao.upsert(futureRecord);
        }

        RewardCenterToby toby = loadTobyByUserId(userId);
        toby.setImageId(id);
        toby.setImageName(name);
        toby.setImageUrl(url);
        toby.setImageExpiryTimeStamp(futureRecord.getExpiryTime());
        this.upsertToby(toby);
        return true;
    }

    public boolean ownTobyImageUnexpendIntegal(Long userId, Long imageId) {
        RewardProduct rewardProduct = rewardProductDao.load(imageId);
        if (rewardProduct == null) {
            return false;
        }
        Long activetimeMs = rewardProduct.getExpiryDate() * 24 * 60 * 60 * 1000 + new Date().getTime();
        TobyImageCVRecord record = TobyImageCVRecord.build(rewardProduct.getId(), userId, activetimeMs);
        record.setStatus(TobyImageCVRecord.Status.OWNED.getStatus());
        tobyImageCVRecordDao.insert(record);
        return true;
    }

    public boolean cvTobyAccessory(Long id, Long userId, Long activeTimeMs, String url) {
        TobyAccessoryCVRecord futureRecord = null;
        List<TobyAccessoryCVRecord>  recordList = tobyAccessoryCVRecordDao.loadByUserId(userId);
        if (CollectionUtils.isNotEmpty(recordList)) {
            for (TobyAccessoryCVRecord entry : recordList) {
                if (Objects.equals(id, entry.getAccessoryId()) && entry.getExpiryTime() > new Date().getTime()) {
                    futureRecord = entry;
                    break;
                }
            }
        }

        TobyAccessoryCVRecord  usingRecord = recordList.stream()
                .filter(p -> Objects.equals(p.getStatus(), TobyImageCVRecord.Status.USING.getStatus()))
                .findFirst().orElse(null);

        if (usingRecord != null) {
            usingRecord.setStatus(TobyImageCVRecord.Status.OWNED.getStatus());
            tobyAccessoryCVRecordDao.upsert(usingRecord);
        }

        if (futureRecord == null) {
            futureRecord = TobyAccessoryCVRecord.build(id, userId, activeTimeMs);
            tobyAccessoryCVRecordDao.insert(futureRecord);
        } else {
            futureRecord.setStatus(TobyAccessoryCVRecord.Status.USING.getStatus());
            tobyAccessoryCVRecordDao.upsert(futureRecord);
        }

        RewardCenterToby toby = loadTobyByUserId(userId);
        toby.setAccessoryId(id);
        toby.setAccessoryUrl(url);
        toby.setAccessoryExpiryTimeStamp(futureRecord.getExpiryTime());
        this.upsertToby(toby);
        return true;
    }

    public boolean cvTobyCountenance(Long id, Long userId, Long activeTimeMs, String url) {
        TobyCountenanceCVRecord futureRecord = null;
        List<TobyCountenanceCVRecord>  recordList = tobyCountenanceCVRecordDao.loadByUserId(userId);
        if (CollectionUtils.isNotEmpty(recordList)) {
            for (TobyCountenanceCVRecord entry : recordList) {
                if (Objects.equals(id, entry.getCountenanceId()) && entry.getExpiryTime() > new Date().getTime()) {
                    futureRecord = entry;
                    break;
                }
            }
        }

        TobyCountenanceCVRecord  usingRecord = recordList.stream()
                .filter(p -> Objects.equals(p.getStatus(), TobyImageCVRecord.Status.USING.getStatus()))
                .findFirst().orElse(null);

        if (usingRecord != null) {
            usingRecord.setStatus(TobyImageCVRecord.Status.OWNED.getStatus());
            tobyCountenanceCVRecordDao.upsert(usingRecord);
        }

        if (futureRecord == null) {
            futureRecord = TobyCountenanceCVRecord.build(id, userId, activeTimeMs);
            tobyCountenanceCVRecordDao.insert(futureRecord);
        } else {
            futureRecord.setStatus(TobyCountenanceCVRecord.Status.USING.getStatus());
            tobyCountenanceCVRecordDao.upsert(futureRecord);
        }

        RewardCenterToby toby = loadTobyByUserId(userId);
        toby.setCountenanceId(id);
        toby.setCountenanceUrl(url);
        toby.setCountenanceExpiryTimeStamp(futureRecord.getExpiryTime());
        this.upsertToby(toby);
        return true;
    }

    public boolean cvTobyProps(Long id, Long userId, Long activeTimeMs, String url) {
        TobyPropsCVRecord futureRecord = null;
        List<TobyPropsCVRecord>  recordList = tobyPropsCVRecordDao.loadByUserId(userId);
        if (CollectionUtils.isNotEmpty(recordList)) {
            for (TobyPropsCVRecord entry : recordList) {
                if (Objects.equals(id, entry.getPropsId()) && entry.getExpiryTime() > new Date().getTime()) {
                    futureRecord = entry;
                    break;
                }
            }
        }

        TobyPropsCVRecord  useingRecord = recordList.stream()
                .filter(p -> Objects.equals(p.getStatus(), TobyImageCVRecord.Status.USING.getStatus()))
                .findFirst().orElse(null);
        if (useingRecord != null) {
            useingRecord.setStatus(TobyPropsCVRecord.Status.OWNED.getStatus());
            tobyPropsCVRecordDao.upsert(useingRecord);
        }

        if (futureRecord == null) {
            futureRecord = TobyPropsCVRecord.build(id, userId, activeTimeMs);
            tobyPropsCVRecordDao.insert(futureRecord);
        } else {
            futureRecord.setStatus(TobyPropsCVRecord.Status.USING.getStatus());
            tobyPropsCVRecordDao.upsert(futureRecord);
        }

        RewardCenterToby toby = loadTobyByUserId(userId);
        toby.setPropsId(id);
        toby.setPropsUrl(url);
        toby.setPropsExpiryTimeStamp(futureRecord.getExpiryTime());
        this.upsertToby(toby);
        return true;
    }
}
