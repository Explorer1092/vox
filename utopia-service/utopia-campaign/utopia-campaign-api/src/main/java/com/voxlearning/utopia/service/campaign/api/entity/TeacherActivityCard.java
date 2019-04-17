package com.voxlearning.utopia.service.campaign.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.campaign.api.enums.ActivityCardEnum;
import com.voxlearning.utopia.service.campaign.api.mapper.CardMapper;
import lombok.*;

import java.util.*;

@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-activity")
@DocumentCollection(collection = "vox_teacher_activity_card")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181210")
public class TeacherActivityCard implements java.io.Serializable, CacheDimensionDocument {

    @DocumentId
    private Long userId;
    private List<Card> cards;
    private Integer mottoOffset;
    private Boolean assign;
    private Date assignDate;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                CacheKeyGenerator.generateCacheKey(TeacherActivityCard.class, this.userId)
        };
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Card implements java.io.Serializable {
        private String type;
        private Integer mottoIndex;
        private Boolean disabled;
        private Date createTime;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode(of = "type")
    public static class CardCount implements java.io.Serializable {
        private String type;
        private Integer count;
    }

    public List<CardMapper> converntCardCount() {
        List<Card> cards = this.cards;

        List<CardMapper> list = new ArrayList<>();
        list.add(new CardMapper(ActivityCardEnum.guan.name(), 0));
        list.add(new CardMapper(ActivityCardEnum.hai.name(), 0));
        list.add(new CardMapper(ActivityCardEnum.de.name(), 0));
        list.add(new CardMapper(ActivityCardEnum.shen.name(), 0));
        list.add(new CardMapper(ActivityCardEnum.zhan.name(), 0));
        list.add(new CardMapper(ActivityCardEnum.tian.name(), 0));
        list.add(new CardMapper(ActivityCardEnum.jian.name(), 0));
        list.add(new CardMapper(ActivityCardEnum.da.name(), 0));

        for (CardMapper mapper : list) {
            for (Card card : cards) {
                if (card.getDisabled()) continue;
                if (Objects.equals(card.getType(), mapper.getType())) {
                    mapper.setCount(mapper.getCount() + 1);
                    mapper.getDetails().add(new CardMapper.CardDetail(card.getType(), card.getMottoIndex(), card.getCreateTime()));
                }
            }
        }

        return list;
    }

    public static TeacherActivityCard newInstance(Long teacherId) {
        TeacherActivityCard teacherActivityCard = new TeacherActivityCard();
        teacherActivityCard.setUserId(teacherId);
        teacherActivityCard.setMottoOffset(0);
        teacherActivityCard.setAssign(false);
        teacherActivityCard.setCards(new ArrayList<>());
        return teacherActivityCard;
    }

    // 这里不用下标, 用序号, 前端适配-1

    public Integer fetchNextMottoOffset() {
        if (this.mottoOffset == null || this.mottoOffset > 47) mottoOffset = 0;
        return this.getMottoOffset() + 1;
    }

    public boolean contentCard(ActivityCardEnum... enumList) {
        Set<String> set = new HashSet<>();

        for (Card card : this.cards) {
            if (card.getDisabled()) continue;
            set.add(card.getType());
        }

        int sum=0;

        for (ActivityCardEnum activityCardEnum : enumList) {
            if(set.contains(activityCardEnum.name())){
                sum++;
            }
        }

        return enumList.length == sum;
    }
}
