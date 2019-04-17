package com.voxlearning.washington.net.message.exam;

import java.io.Serializable;
import java.util.Map;


/**
 * 保存数学子题
 */
public class SaveMathExaminationQuestionItemsRequest implements Serializable
{
	private static final long serialVersionUID = 0L ;
	
	/**父题ID*/
	public String id;
	/**子题ID*/
	public Map<Integer, String> itemIds;

}