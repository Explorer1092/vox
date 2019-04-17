package com.voxlearning.utopia.service.afenti.api.data;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Data;

import java.io.Serializable;

/**
 * @author peng.zhang.a
 * @since 2016/5/29
 */
@Data
public class AfentiUserRankInfo implements Serializable{
	private long userId;
	private int starNum;
	private int integralNum;
	private int starNationalRank;
	private int starSchoolRank;
	private int integralNationalRank;
	private int integralSchoolRank;
	private String imgUrl;
	private String stuName;
	private Subject subject;

}
