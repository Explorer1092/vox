/*
* @datetime : 2016-06-01
* @author : yifei.peng
* @rely : jQuery
* */
(function(window){
    "use strict";
    var $WIN = window;

    if(typeof $WIN === "undefined"){
        $WIN = {};
    }

    function _info(msg){
        if( location.host.indexOf("test") > -1 || location.host.indexOf("localhost") > -1 ){
            console.info(msg);
        }
    }

    function _extend(child, parent){
        var $key;
        for($key in parent){
            if(parent.hasOwnProperty($key)){
                child[$key] = parent[$key];
            }
        }
    }

    //获取App版本
    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }

    function getAppVersion(){
        var native_params = {
            appVersion : "",
            systemType : "",
            systemVersion : ""
        };

        if(window["external"] && window.external["getInitParams"] ){
            var $params = window.external.getInitParams();

            if($params){
                $params = $.parseJSON($params);
                native_params.appVersion = $params.native_version;
                native_params.systemType = $params.system_type;
                native_params.systemVersion = $params.system_version;
            }
		}else{
            native_params.appVersion = getQueryString("app_version") || '';
            native_params.systemType = getQueryString("system_type") || '';
            native_params.systemVersion = getQueryString("system_version") || '';
        }

        return native_params;
    }

    var spreadMode = {
        showBanner : function(opt){
            /*
            * id : document #banner
            * tag : "li", 默认ul li
            * scrollStyle : "banner",//广告类型 banner : popup
            * imgNumber : 1,//展示图片个数
            * // 特殊广告逻辑
            * special : {
            *    "SanguoDmz" : true
            * }
            * */
            var $default = {
                special : {},
                boxId : null,
                hasTitle : false,
                tag : "li"
            };

            _extend($default, opt);

            var $rowBox = "";
            var tag = $default.tag;
            var modeData = $default.data;

            if(modeData && modeData.length > 0){
                if(tag == "" || tag == "li" || tag == "back"){$rowBox += "<ul class='slides'>";}

                for(var i = 0; i < modeData.length; i++){
                    var $userId = ($default.userId ? "&sid=" + $default.userId : "");
                    var $getToLink = (modeData[i].hasUrl ? goSetLink(modeData[i].id, i, $default) : 'javascript:;');
                    var $target = modeData[i].hasUrl ? "target='_blank'" : "";
                    var $imageUrl = $default.imgDoMain + "/gridfs/" + modeData[i].img;
                    var $titleContent = "";

                    if($default.hasTitle){
                        $titleContent = "<div class='title'> " + modeData[i].description + "</div>";
                    }

                    if(tag == "back"){
                        $rowBox += "<li><a href='" + $getToLink + "' "+ $target +" style='background: no-repeat center center url("+ $imageUrl +") ; width: 100%; height: 300px; display: block;'></a></li>";
                    }else if(tag == "div"){
                        $rowBox += "<div><a href='" + $getToLink + "' "+ $target +">" + $titleContent + "<img src='"+ $imageUrl +"' width='100%'/></a></div>";
                    }else if(tag == "text"){
                        $rowBox += "<a href='" + $getToLink + "' "+ $target +">" + modeData[i].description + "</a>";
                    }else{
                        $rowBox += "<li><a href='" + $getToLink + "' "+ $target +">" + $titleContent + "<img src='"+ $imageUrl +"' width='100%'/></a></li>";
                    }
                }

                if(tag == "" || tag == "li"){$rowBox += "</ul>"}

                $default.boxId.html( $rowBox );
            }else{
                _info("数据为空！");
                $default.boxId.hide();
            }
        },
        //切換效果
        switchMode : function(opt){
            var $initialValue = {
                eff : 0,
                boxId : null,
                second : 2500,
                tabCss  : "",
                currentCss  : "active",
                currentIndex : 0,
                picId : opt.boxId.find("li")
            };

            _extend($initialValue, opt);

            if($initialValue.boxId == null){
                return false;
            }

            var timeInit = setInterval(function(){
                $initialValue.currentIndex++;
                initSwitch();
            }, $initialValue.second);

            if($initialValue.picId.length > 0){
                //遍历
                $initialValue.picId.eq(0).show().siblings().hide();
                if($initialValue.picId.length > 1){
                    var $tabBox = "<div class='tabBox "+ $initialValue.tabCss +"'>";
                    $initialValue.picId.each(function(i){
                        $tabBox += "<span class='prve "+ (i==$initialValue.currentIndex ? $initialValue.currentCss : '') +"'>"+ (i+1) +"</span>";
                    });
                    $tabBox += "</div>";

                    $initialValue.boxId.append( $tabBox );
                }
            }else{
                $initialValue.boxId.hide();
            }

            //通用
            function initSwitch(){
                if( $initialValue.currentIndex >= $initialValue.picId.length){
                    $initialValue.currentIndex = 0;
                }
                if( $initialValue.currentIndex < 0){
                    $initialValue.currentIndex = $initialValue.picId.length-1;
                }
                $initialValue.boxId.find(".prve").eq($initialValue.currentIndex).addClass($initialValue.currentCss).siblings().removeClass($initialValue.currentCss);
                switch ($initialValue.eff){
                    case 1 :
                        $initialValue.picId.eq($initialValue.currentIndex).fadeIn(500).siblings().fadeOut(500);
                        break;
                    default :
                        $initialValue.picId.eq($initialValue.currentIndex).fadeIn(60).siblings().hide();
                }
            }

            //经过
            $initialValue.boxId.find(".prve, li").on("mouseover", function(){
                clearInterval(timeInit);
                $initialValue.currentIndex = $(this).prevAll().length;
                initSwitch();
            }).on("mouseout", function(){
                timeInit = setInterval(function(){
                    $initialValue.currentIndex++;
                    initSwitch();
                }, $initialValue.second);
            });

            //左点击
            $initialValue.boxId.find(".back, .next").on("click", function(){
                switch( $(this).attr("class") ){
                    case "back":
                        $initialValue.currentIndex--;
                        break;
                    case "next":
                        $initialValue.currentIndex++;
                        break;
                }
                initSwitch();
                clearInterval(time);
                time = setInterval(function(){
                    $initialValue.currentIndex++;
                    initSwitch();
                }, $initialValue.second);
            });
        }
    };

    function goSetLink(id, index, obj){
        var $userId = (obj.userId ? "&sid=" + obj.userId : "");
        return (obj.goLink + "?new_page=blank&aid=" + id + "&index=" + index + "&v=" + obj.appVersion + "&s=" + obj.systemType + "&sv=" + obj.systemVersion + $userId);
    }

    /*
     * 获取广告位 数据
     *
     * var defaultOpt = {
     * keyId : null, //10001
     * postLink : "scripts/spread.json"
     * */
    function voxSpread(opt, callback){
        var $default = {
            source : "",
            appVersion : getAppVersion().appVersion,
            systemType : getAppVersion().systemType,
            systemVersion : getAppVersion().systemVersion,
            boxId : null, //document object  or jquery selector
            keyId : null, //10001
            userId : null,
            switchMode : false, //是否添加滚动效果
            domain  : null,
            goLink : "/be/london.vpage",
            postLink : "/be/newinfo.vpage"
        };

        _extend($default, opt);

        if($default.domain){
            $default.goLink = $default.domain + "/be/london.vpage";
            $default.postLink = $default.domain + "/be/newinfo.vpage";
        }

        if(typeof $default.boxId === "string"){
            $default.boxId = $($default.boxId);
        }

        if($default.keyId != null){
            $.post($default.postLink, {
                p : $default.keyId,
                s : $default.systemType,
                sv : $default.systemVersion,
                v : $default.appVersion
            }, function(data){
                if(data.success){
                    for(var i = 0, items = data.data; i < items.length; i++){
                        if(items[i].url && items[i].url != ""){
                            items[i].goUrl = goSetLink(items[i].id, i, $default); //设置完整GO TO URL;
                        }else{
                            items[i].goUrl = 'javascript:;'
                        }
                    }

                    _extend($default, data);

                    if($default.boxId != null){
                        spreadMode.showBanner($default);

                        if($default.switchMode){
                            spreadMode.switchMode($default);
                        }
                    }
                }else{
                    _extend($default, data);

                    _info(data.errorCode);

                    if($default.boxId != null){
                        $default.boxId.hide();
                    }
                }

                if(callback){callback($default);}
            });
        }else{
            _info("keyId = null");

            if($default.boxId != null){
                $default.boxId.hide();
            }
        }
    }

    /**
     * textScroll
     * @param opt
     * @returns {boolean}
     */
    function textScroll(opt) {
        (function () {
            var ele = $(opt.ele), timerID = ele.attr("data-timerid") || "", scrollUp;
            if (!opt) {
                opt = {}
            }
            if (timerID) {
                clearInterval(timerID);
            }
            var $this = ele.eq(0).find("ul:first");

            var lineH = $this.find("li:first").outerHeight(),
                line = opt.line ? parseInt(opt.line, 10) : parseInt(ele.height() / lineH, 10),
                speed = opt.speed ? parseInt(opt.speed, 10) : 500,
                timer = opt.timer ? parseInt(opt.timer, 10) : 1000;

            if (line == 0) {
                line = 1;
            }
            if ($this.find("li").length < 2) {
                clearInterval(timerID);
                return false;
            }

            var upHeight = 0 - line * lineH;

            //滚动函数
            scrollUp = function () {
                if ($this.find("li").length > 1) {
                    $this.animate({
                        marginTop: upHeight
                    }, speed, function () {
                        for (var i = 1; i <= line; i++) {
                            $this.find("li:first").appendTo($this);
                        }
                        $this.css({marginTop: 0});
                    });
                }
            };

            //鼠标事件绑定
            $this.unbind('mouseleave');
            $this.bind({
                mouseenter: function () {
                    clearInterval(timerID);
                },
                mouseleave: function () {
                    timerID = setInterval(function () {
                        scrollUp();
                    }, timer);
                    ele.attr("data-timerid", timerID);
                }
            }).mouseleave();
        })();
    }

    var initList = {
        voxSpread : voxSpread,
        textScroll: textScroll
    };

    if(typeof $WIN['YQ'] === 'undefined'){
        $WIN.YQ = initList;
    }else{
        _extend($WIN.YQ, initList);
    }

    if(typeof define === 'function' && define.amd){
        define([], function () {
            'use strict';
            return initList;
        });
    }else if(typeof module !== 'undefined'){
        module.exports = initList;
    }
}(window));