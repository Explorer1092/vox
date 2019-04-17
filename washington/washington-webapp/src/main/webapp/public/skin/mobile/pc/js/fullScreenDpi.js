var adaptUILayout = (function () {
    //根据校正appVersion或userAgent校正屏幕分辨率宽度值
    var regulateScreen = (function () {
        var cache = {};

        //默认尺寸
        var defSize = {
            width: window.screen.width,
            height: window.screen.height
        };

        var ver = window.navigator.appVersion;

        var _ = null;

        var check = function (key) {
            return key.constructor == String ? ver.indexOf(key) > -1 : ver.test(key);
        };

        var add = function (name, key, size) {
            if (name && key)
                cache[name] = {
                    key: key,
                    size: size
                };
        };

        var del = function (name) {
            if (cache[name])
                delete cache[name];
        };

        var cal = function () {
            if (_ != null)
                return _;

            for (var name in cache) {
                if (check(cache[name].key)) {
                    _ = cache[name].size;
                    break;
                }
            }

            if (_ == null)
                _ = defSize;

            return _;
        };

        return {
            add: add,
            del: del,
            cal: cal
        };
    })();


    //实现缩放
    var adapt = function (uiWidth) {
        var
            deviceWidth,
            deviceHeight,
            devicePixelRatio,
            targetDensityDpi,
        //meta,
            initialContent,
            head,
            isiOS,
            viewport,
            ua;

        ua = navigator.userAgent.toLowerCase();
        //whether it is the iPhone or iPad
        isiOS = ua.indexOf('ipad') > -1 || ua.indexOf('iphone') > -1;
        //获取设备信息,并矫正参数值
        devicePixelRatio = window.devicePixelRatio;
        deviceWidth = regulateScreen.cal().width;
        deviceHeight = regulateScreen.cal().height;

        //获取最终dpi
        if(deviceWidth >= uiWidth){
            targetDensityDpi = "device-dpi";
        }else{
            targetDensityDpi = uiWidth / deviceWidth * devicePixelRatio * 160;
        }

        if (isiOS) {
            //use iphone and iPad device
            initialContent = 'target-densitydpi=device-dpi, width=' + ( deviceWidth >=768 ? 768 : 640 ) + ', user-scalable=no';
        } else {
            //use viewport Android device
            initialContent = 'target-densitydpi=' + targetDensityDpi + ', width='+uiWidth+', user-scalable=no';

            if (screen.width == "1080") {
                initialContent = 'target-densitydpi=320, width=720, user-scalable=no, minimum-scale=1.0, initial-scale=1.0, maximum-scale=1.0';
            }

            //处理-W > H
            if(deviceWidth >= 1080 && deviceWidth > deviceHeight){
                initialContent = 'width=device-width, height=device-height,inital-scale=1.0,maximum-scale=1.0,user-scalable=no';
            }
        }

        viewport = document.createElement('meta');
        viewport.name = 'viewport';
        viewport.content = initialContent;

        head = document.getElementsByTagName('head')[0];
        head.appendChild(viewport);
    };

    return {
        adapt: adapt
    };
})();

adaptUILayout.adapt(720);