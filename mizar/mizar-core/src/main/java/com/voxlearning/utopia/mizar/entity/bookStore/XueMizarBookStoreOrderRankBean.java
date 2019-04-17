package com.voxlearning.utopia.mizar.entity.bookStore;


import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class XueMizarBookStoreOrderRankBean implements Serializable {

   private static final long serialVersionUID = 7265593550934546332L;

   private Integer orderTotalNum;
   private Integer yesterdayOrderNum;
   private String recentDaysOrderNum;
   private Integer storeTotalNum;
   private Integer yesterdayStoreNum;
   private List<XueMizarBookStoreOrderRank> orderNumRanks;
   private List<XueMizarBookStoreOrderRank> referralRankList;
   private Page<XueMizarBookStoreOrderRank> referralRanks;

   @Getter
   @Setter
   public static class XueMizarBookStoreOrderRank implements Serializable {


      private static final long serialVersionUID = 4021412218804295168L;
      private String bookStoreName;
      private Long bookStoreId;
      private Integer orderNum;
      private String mizarUserId;
      private Long marketId;

   }

   public static XueMizarBookStoreOrderRankBean initRankBean(){
      XueMizarBookStoreOrderRankBean bean = new XueMizarBookStoreOrderRankBean();
      bean.setOrderTotalNum(0);
      bean.setYesterdayOrderNum(0);
      bean.setRecentDaysOrderNum("");
      bean.setStoreTotalNum(0);
      bean.setYesterdayStoreNum(0);
      bean.setOrderNumRanks(new ArrayList<>());
      bean.setReferralRankList(new ArrayList<>());
      return bean;
   }


}
