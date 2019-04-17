define(['jquery','knockout','YQ','voxLogs', 'flexSlider','external'], function ($, ko, YQ) {
    // if (location.href.indexOf('https') > -1) {
    //     location.href = 'http' + location.href.substring(5);
    // }
    function FinanceMode() {
        var _this = this;
        var ua = window.navigator.userAgent.toLowerCase();
        var bindFlag = isBindParent?1:2,
            pays0 = 'userType_3:bindtype_' + bindFlag + ':payAppKey_' + YQ.getQuery("app_key");

        YQ.voxLogs({
            module: 'm_pxUqPehpsT',
            op: 'o_NvDGFEQhia',
            s0: pays0,
            s1: orderId
        });

        if (YQ.getQuery("app_key").indexOf('LevelReading') > -1 || YQ.getQuery("hideAppTitle") === "true" || YQ.getQuery("isSendAppInfo") === "true"){
            if (YQ.getExternal()['setTopBarInfo']){
                YQ.getExternal().setTopBarInfo(JSON.stringify({
                    show : false
                }));
            }
        }

        if (compare_version(YQ.getAppVersion(), '3.0.3') > -1 && ua.indexOf('student') > -1 && (YQ.getQuery("app_key").indexOf("LevelReading") > -1 || YQ.getQuery("isSendAppInfo") === "true") && ua.indexOf("ios") > -1){
            if (YQ.getExternal()['setRightCloseBtn']){
                YQ.getExternal().setRightCloseBtn(JSON.stringify({
                    show : true
                }));
            }
        }


        $("#financeContentBox").flexslider({
            animation: 'slide',
            directionNav: true,
            animationLoop: true,
            slideshowSpeed: 5000,
            animationSpeed: 400,
            after:function (slider) {
                if (!slider.playing){
                    slider.play();
                }
            },
            end: function(slider){
                if (slider.currentSlide === 1) {
                    YQ.voxLogs({
                        module: 'm_pxUqPehpsT',
                        op: 'o_FAcew9ha9d',
                        s0: pays0,
                        s1: orderId
                    });
                }
            }
        });

        _this.return_previous = function () {
            if(YQ.getExternal()['disMissView'] && (YQ.getQuery("app_key").indexOf('LevelReading') > 1 || YQ.getQuery("appPaySeccess") === "close")){
                YQ.getExternal().disMissView();
                return false;
            }

            if (YQ.getQuery("frontType") === 'h5') {
                if(YQ.getExternal()['disMissView']){
                    YQ.getExternal().disMissView();
                }
            } else {
                if (window.history.length === 1) {
                    if(YQ.getExternal()['disMissView']){
                        YQ.getExternal().disMissView();
                    }
                } else {
                    window.history.back();
                }
            }
        };
        
        _this.download_parent = function () {
            YQ.voxLogs({
                module: 'm_pxUqPehpsT',
                op: 'o_4kT4lpgRIa',
                s0: pays0,
                s1: orderId
            });
            check_external('isExistApp', function (exist) {
                if (exist) {
                    var params = ua.indexOf('android') > -1 ? 'com.yiqizuoye.jzt' : 'a17parent';

                    do_external('isExistApp', params, function (flag) {
                        if (flag) {
                            do_external('openApp',{
                                name: 'a17parent',
                                type: 'h5',
                                params: '{}',
                                url    :  location.origin + '/view/mobile/parent/17my_shell/order.vpage?useNewCore=wk'
                            });
                        } else {
                            check_external('isExistApp', function (exist) {
                                if (exist) {
                                    do_external('openSecondWebview', {
                                        url:'https://www.17zuoye.com/view/mobile/common/download?app_type=17parent'
                                    });
                                } else {
                                    window.location.href = 'https://www.17zuoye.com/view/mobile/common/download?app_type=17parent';
                                }
                            });
                        }
                    });
                }
            });
        };

        function compare_version(src, dest){
            var src_arr = src.split('.'),
                dest_arr = dest.split('.'),
                len = Math.max(src_arr.length, dest_arr.length);

            for(var index = 0; index < len; index++){
                var src_cache  = parseInt(src_arr[index]),
                    dest_cache = parseInt(dest_arr[index]);

                if((src_cache && !dest_cache && src_cache > 0) || (src_cache > dest_cache)){
                    return 1;
                }else if((dest_cache && !src_cache && dest_cache > 0) || (src_cache < dest_cache)){
                    return -1;
                }
            }

            return 0;
        }

    }
    ko.applyBindings(new FinanceMode());
});