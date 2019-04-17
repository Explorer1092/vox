package com.voxlearning.washington.net.message.exam;

import java.io.Serializable;


/**
 * 保存答题结果
 */
public class SaveQuizResultRequest implements Serializable
{
	private static final long serialVersionUID = 0L ;
	
	/** 答题结果列表 */
	public QuizExamResultRequest result;

}