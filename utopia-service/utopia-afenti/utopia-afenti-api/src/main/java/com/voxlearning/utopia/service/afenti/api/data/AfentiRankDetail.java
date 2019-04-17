package com.voxlearning.utopia.service.afenti.api.data;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author peng.zhang.a
 * @since 2016/5/26
 */
@Data
public class AfentiRankDetail implements Serializable {

	private Long userId;
	private String stuName;
	private Integer num;
	private String imgUrl;
	private Integer rank;

	public static AfentiRankDetail newInstance(Long userId, String stuName, Integer num, String imgUrl, Integer rank) {
		AfentiRankDetail afentiRankDetailInfo = new AfentiRankDetail();
		afentiRankDetailInfo.setUserId(userId);
		afentiRankDetailInfo.setStuName(stuName);
		afentiRankDetailInfo.setNum(num);
		afentiRankDetailInfo.setImgUrl(imgUrl);
		afentiRankDetailInfo.setRank(rank);
		return afentiRankDetailInfo;
	}

	public static List<AfentiRankDetail> init(int num) {
		List<AfentiRankDetail> afentiRankDetailInfos = new ArrayList<>();
		for(int i= 0;i<num;i++) {
			afentiRankDetailInfos.add(AfentiRankDetail.newInstance(30000L + i, "name" + i, 1000 - i, "", i + 1));
		}
		return afentiRankDetailInfos;
	}

}
