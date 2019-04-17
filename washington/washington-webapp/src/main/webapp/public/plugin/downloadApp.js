/* global location: true, $:true */

/**
 *  @date 2016/1/6
 *  @auto liluwei
 *  @description 该模块主要负责下载家长通  弥补了点击下载造成的返回页面适配问题
 */

(function(){
	"use strict";

	var WIN = window,
        UA = WIN.navigator.userAgent.toLowerCase(),
		weChatReg = /micromessenger/,
		SHORT_URL_PREFIX = "http://www.17zyw.cn/";

	var studentAppPublicHref = "http://fusion.qq.com/cgi-bin/qzapps/unified_jump?appid=12112494";

    var downloadSource = {
        JZT : {
            androidCidInfo : {
                202009 : "RFBjE3",
                100110 : "2AR3Mb",
                202011 : "7jE7ru",
                202015 : "maiyyu",
                102013 : "nE3iIz",
                100206 : "rAja6b",
                100312 : "JJFR3i",
                100307 : "BbmUbq"
            },
            //isNotNeedMicroCids : [ ],
            androidWx : "http://fusion.qq.com/cgi-bin/qzapps/unified_jump?appid=11083719",
            iosWx : "http://a.app.qq.com/o/simple.jsp?pkgname=com.yiqizuoye.jzt",
            appSotre : "https://itunes.apple.com/cn/app/jia-zhang-tong-bang-jia-zhang/id1167509810?l=zh&ls=1&mt=8 "
        },
        STUDENT : {
			androidCidInfo : {
				"default" : studentAppPublicHref
			},
			androidWx : studentAppPublicHref,
			iosWx : studentAppPublicHref,
			appSotre : "https://itunes.apple.com/us/app/yi-qi-zuo-ye-xue-sheng-duan/id1004963943?l=zh&ls=1&mt="
        },
		TEACHER : {
			androidCidInfo : {
				300127 : "uMnUb2"
			},
			androidWx : "http://fusion.qq.com/cgi-bin/qzapps/unified_jump?appid=11840217",
			iosWx : "http://a.app.qq.com/o/simple.jsp?pkgname=com.yiqizuoye.teacher",
			appSotre : 'https://itunes.apple.com/cn/app/yi-qi-zuo-ye-lao-shi-duan/id961582881?l=en&mt=8'
		}
    };

	var getAndroidMicroDownload = function(cid, androidCidInfo){
			var url = androidCidInfo[cid];
			return url ? (SHORT_URL_PREFIX + url) : (androidCidInfo["default"] || "");
		},
		isNotNeedMicroDownloadCid = function(cid, isNotNeedMicroDownloadCids){
			return isNotNeedMicroDownloadCids.indexOf(cid) > -1;
		},
		buildDownloadInfoBySource = function(cid, downloadInfo){
			var androidCidInfo = downloadInfo.androidCidInfo,
                 androidFullDownloadUrlByCid = getAndroidMicroDownload(cid, androidCidInfo);

			return [
				//  Android  TODO do not change the order . make sure it is in the first
				{
					reg : /android/,
					wx  : downloadInfo.androidWx,
					fullDownload : androidFullDownloadUrlByCid,
					microDownload : androidFullDownloadUrlByCid
				},
				// ios
				{
					reg : /ipad|iphone|ipod/,
					microDownload : downloadInfo.appSotre,
					wx            : downloadInfo.iosWx,
					fullDownload : downloadInfo.iosWx
				}
			];
		};

	var downloadParentApp = function(source, cid){

		var downloadInfo = downloadSource[source.trim().toUpperCase()];

		if( downloadInfo === undefined ){
			throw Error("必须传入合法来源");
		}

		cid = (cid||"").trim();

		if(cid === ""){
			throw Error("必须传入合法cid");
		}

		var hrefInfos = buildDownloadInfoBySource(cid, downloadInfo),
			defaultHref = "defaultHref" in downloadInfo ? downloadInfo.defaultHref : hrefInfos[0].microDownload;

		for(var index =0; index < hrefInfos.length; index++){

			var hrefInfo =  hrefInfos[index];

			if(hrefInfo.reg.test(UA)){

				var href ;

				if(
					isNotNeedMicroDownloadCid(
						cid,
						(downloadInfo.isNotNeedMicroCids || [])
					)
				){
					href = hrefInfo.fullDownload;
				}else{
					href = weChatReg.test(UA) ? hrefInfo.wx : hrefInfo.microDownload;
				}

				location.href =  href;

				return ;

			}
		}

		location.href = defaultHref;

	};

	var encapsulation = function(){
		return downloadParentApp;
	};

	if (typeof define === 'function' && define.amd) {
		// AMD
		define([], encapsulation);
	} else if (typeof exports === 'object') {
		// CMD, CommonJS之类的
		module.exports = encapsulation();
	}else{
		WIN.downloadParentApp = encapsulation();
	}

})();
