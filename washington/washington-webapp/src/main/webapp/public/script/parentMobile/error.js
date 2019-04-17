/* global define : true */
/**
 *  @date 2016-06-01 10:24:24
 *  @auto liluwei
 *  @description 该模块主要负责 统一的报错页面的信息
 */


define([], function(){

    'use strict';

    var WIN = window,
		PM = WIN.PM;

	PM.doTrack(
		"errorPage",
		WIN.error_info.join('_')
	);

});

