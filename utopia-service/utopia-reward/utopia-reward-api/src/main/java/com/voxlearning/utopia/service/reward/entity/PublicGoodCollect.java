package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 公益活动 - 收集实体
 * @author haitian.gan
 */
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-public-good")
@DocumentCollection(collection = "vox_public_good_collect_{}",dynamic = true)
@UtopiaCacheRevision("20180612")
public class PublicGoodCollect implements CacheDimensionDocument {

    private static final long serialVersionUID = 7426131009686814378L;

    @DocumentId private String id;
    private Long userId;
    private Long activityId;
    private Long styleId;
    private Status status;
    private List<Element> elementList;                          // 物件列表
    private Long finishTime;                                    // 完成时间
    private List<Reward> rewardList;                            // 奖励列表

    @DocumentCreateTimestamp private Long createTime;
    @DocumentUpdateTimestamp private Long updateTime;

    @DocumentFieldIgnore private String userName;                // 查看同班教室时需要展示名称
    @DocumentFieldIgnore private Boolean liked;                  // 是否已经点过赞

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                "USER_ID", String.valueOf(this.userId)
        };
    }

    public PublicGoodCollect generateId() {
        Objects.requireNonNull(userId);
        id = userId + "-" + RandomUtils.nextObjectId();
        return this;
    }

    @DocumentFieldIgnore
    public boolean isFinished(){
        return Objects.equals(this.status,Status.FINISHED);
    }

    public void finish(){
        this.status = Status.FINISHED;
        this.finishTime = new Date().getTime();
    }

    public enum Status{
        ONGOING,    // 进行中
        FINISHED    // 完成了
    }

    @Getter
    @Setter
    public static class Element implements Serializable {
        private static final long serialVersionUID = -6014730375721969306L;
        private Long id;
        private String code;
        private Long count;
        private Map<String,Object> attr;
    }

    @Getter
    @Setter
    public static class Reward implements Serializable{
        private static final long serialVersionUID = 4763594625934703573L;
        private Long id;
        private Integer num;
        private Map<String,Object> attr;

        public Object getAttrVal(String key){
            if(attr == null)
                return null;

            return attr.get(key);
        }

        public Map<String,Object> setAttrVal(String key,Object val){
            if(attr == null) attr = new HashMap<>();

            attr.put(key,val);
            return attr;
        }
    }

    public Reward addReward(Long rewardId,int num, boolean enable){
        if(rewardId == null || num <= 0)
            return null;

        Reward newReward = new Reward();
        newReward.id = rewardId;
        newReward.num = num;

        if(rewardList == null)
            rewardList = new ArrayList<>();

        if(enable){
            newReward.attr = new HashMap<>();
            newReward.attr.put("enable", false);
        }

        rewardList.add(newReward);
        return newReward;
    }

    public PublicGoodCollect putElementValueOf(PublicGoodElementType elements) {
        Set<String> coded = Optional.ofNullable(elementList)
                .orElse(Collections.emptyList())
                .stream()
                .map(Element::getCode)
                .collect(Collectors.toSet());

        if (!coded.contains(elements.getCode())) {
            Element element = new Element();
            element.setId(elements.getId());
            element.setCode(elements.getCode());
            element.setCount(1L);
            if (elementList == null) {
                elementList = new ArrayList<>();
            }
            elementList.add(element);
        } else {
            for (Element element : elementList) {
                if (Objects.equals(element.getCode(), elements.getCode())) {
                    Long count = Optional.ofNullable(element.getCount()).orElse(1L);
                    element.setCount(++count);
                }
            }
        }
        return this;
    }

    @DocumentFieldIgnore
    public long getEnabledEleNum() {
        return Optional.ofNullable(this.elementList).orElse(Collections.emptyList()).size();
    }

    @DocumentFieldIgnore
    public List<String> getEnabledCode() {
        return Optional.ofNullable(this.elementList)
                .orElse(Collections.emptyList())
                .stream()
                .map(Element::getCode)
                .collect(Collectors.toList());
    }

    public Map<String,Element> getElementMap(){
        return Optional.ofNullable(elementList)
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(k -> k.getCode(), v -> v));
    }

}
