package com.voxlearning.washington.support.context;

import lombok.Data;


/**
 * 这个类严禁序列化，严禁直接用json传到前端，因为会包含很多敏感信息
 */
@Data
public class WashingtonUserDetail {

}
