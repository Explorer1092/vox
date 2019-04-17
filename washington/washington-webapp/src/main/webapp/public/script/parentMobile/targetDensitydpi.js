/**
 * author : luwei.li
 * date : 2015/11/9
 * descrtption : Android4.0以下(其实测试发现 包括4.0也会发生)的手机系统自带浏览器中，不支持设置viewport的width  感谢@yifei.peng代码的帮助
 * use :
 *    1: TODO 为了保证html的完整性 严谨性 使用前，[必须]在你的head标签中写上你想要的viewport
 *    2: 对于js不能够正确获取到屏幕宽度的设备, 请直接在badScreenBrowerObj 中添加 TODO 只因为让大家在公用的库中添加，就是因为避免我们重复同一个坑。
 *        //Example:
 *
 *        //
 *        badScreenBrowerObj = {
 *            "GT-I9100G" : {  // 三星gt i9100g
 *                 width : 480,
 *                 height : 800
 *            }
 *        }
 *    3: 使用如下代码启动适配
 *        adaptUILayout.adapt(布局宽度);
 *
 *  TODO : 当我们项目中的 这类手机(oppo R817t vivo x  lenovo)占有率不多的时候，撤销这个代码
 */

(function(){
    "use strict";

    // 不能正确识别的手机宽高的 为了共享精神 记得都写在这
    var badScreenBrowerObj = {

    };

    var WIN = window,
        USERAGENT = WIN.navigator.userAgent.toLowerCase();


    //根据校正appVersion或userAgent校正屏幕分辨率宽度值
    var regulateScreen = (function(){

        //默认尺寸
        var screen = WIN.screen,
            defSize = {
                width  : screen.width,
                height : screen.height
            };

        var check = function(userAgent){
            return userAgent && USERAGENT.indexOf(userAgent) > -1;
        };

        return  function () {
            for (var userAgent in badScreenBrowerObj) {
                if (badScreenBrowerObj.hasOwnProperty(userAgent) && check(userAgent)) {
                    return badScreenBrowerObj[userAgent];
                }
            }

            return defSize;
        };

    })();

    var getAndroidVersion = function(ua){
        ua = (ua || window.navigator.userAgent).toLowerCase();
        var match = ua.match(/android\s([0-9\.]*)/);
        return match ? match[1] : false;
    };

    //实现缩放
    var adapt = function(uiWidth, diyContent){

        // check is pc or mobile  http://detectmobilebrowsers.com/
        if( !(/android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini/.test(USERAGENT) && ("ontouchstart" in document) ) ){
            return ;
        }

        var head = document.querySelector("head"),
            viewportMeta;

        if(head === null){
            throw "必须有head";
        }

        if(diyContent){

            viewportMeta = document.createElement("meta");
            viewportMeta.name = "viewport";

            typeof diyContent === "string" && (diyContent = function(){return diyContent});

            if (typeof diyContent !== "function") {
                throw "diyContent必须Function or String";
            }

            viewportMeta.content = diyContent();
            head.appendChild(viewportMeta);
        }

        var isiOS = USERAGENT.indexOf('ipad') > -1 || USERAGENT.indexOf('iphone') > -1;

        viewportMeta || (viewportMeta =  head.querySelector("[name='viewport']"));

        if(viewportMeta === null){
            throw "必须有viewport meta标签 并且最好有width选项";
        }

        if(!isiOS){
            uiWidth = 720;
        }

        var // [Viewport target-densitydpi no longer supported](http://developer.android.com/guide/webapps/migrating.html#TargetDensity)
            isSupportTargetDensitydpiAndroid  = getAndroidVersion(USERAGENT) < '4.4';

        var devicePixelRatio = WIN.devicePixelRatio || 1,
            deviceWidth      = regulateScreen().width,
            targetDensitydpi =  uiWidth / deviceWidth * devicePixelRatio * 160,
            // 因为有些手机不支持我们自己计算出来的device-width(uiWidth) 所以最好还是使用 width=device-width 让他自己适配
            initialContent   = 'target-densitydpi=' + targetDensitydpi + ', width=' + uiWidth + ', user-scalable=no';

        if(isiOS){
            return false;
        }

        viewportMeta.content = initialContent;
    };

    window.adaptUILayout =  adapt;

})();

