package com.voxlearning.washington.net.message.exam;

import java.io.Serializable;


/**
 * 保存答题结果
 */
public class SaveAfentiResultRequest implements Serializable
{
	private static final long serialVersionUID = -1868525194876648402L;
	
	/** 答题结果列表 */
	public AfentiExamResultRequest result;

}