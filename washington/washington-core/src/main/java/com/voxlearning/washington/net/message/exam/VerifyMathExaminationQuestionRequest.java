package com.voxlearning.washington.net.message.exam;

import java.io.Serializable;


/**
 * TODO
 */
public class VerifyMathExaminationQuestionRequest implements Serializable
{
	private static final long serialVersionUID = 0L ;
	
	/**  */
	public String uid;
	/**  */
	public String id;
	/**  */
	public boolean verify;

}