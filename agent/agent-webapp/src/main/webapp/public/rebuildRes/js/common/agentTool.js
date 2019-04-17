/**
 *Tianji App 基础工具包
 */
var agentTool = function(){

    var version = "0.0.1";

    var agentTool = {};

    function extend(child, parent) {
        var key;
        for (key in parent) {
            if (parent.hasOwnProperty(key)) {
                child[key] = parent[key];
            }
        }
    }

    //验证是否未定义或null或空字符串
    function isBlank(str) {
        return str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
    }

    //验证是否手机号
    function isMobile(value) {
        value = value + "";
        //严格判定
        var _reg = /^0{0,1}(13[4-9]|15[7-9]|15[0-2]|18[7-8])[0-9]{8}$/;
        //简单判定
        var reg = /^1[0-9]{10}$/;
        if (!value || value.length != 11 || !reg.test(value)) {
            return false;
        }
        return true;
    }

    //验证是否邮箱
    function isEmail(value){
        var req = /^[-_.A-Za-z0-9]+@[-_.A-Za-z0-9]+(\.[-_.A-Za-z0-9]+)+$/;
        return value && req.test(value);
    }

    //获得地址栏参数
    function getQuery(item){
        var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    }

    /**
     * 提示信息
     * option:{
     *    type: 类型
     *    msg: 提示的文本信息
     *    sureBtn: 确认按钮
     *    cancelBtn :取消按钮
     *    fadeTime: 停顿的毫秒数
     *    sure: 确认回调
     *    cancel: 取消回调
     *
     * }
     * @param msg
     */
    //提示
    function alertMsg(option){
        var _this = this,
            modelHtml = "";

        //清除之前的弹窗,保证当前只有一个
        if($(".alertDialog")){
            $(".alertDialog").remove();
        }

        _this.type = option.type || "tip"; //默认是提示消息
        _this.msg = option.msg || "提示";
        _this.sureBtn = option.sureBtn || "确定";
        _this.cancelBtn = option.cancelBtn || "取消";
        _this.sure = option.sure || function(){};
        _this.cancel = option.cancel || function(){};

        switch(_this.type)
        {
            case "modal"://确认msg按钮,只包含一个按钮
                modelHtml = '<div class="alertDialog">'+
                    '<div class="clazz-popup">'+
                    '<div class="text">'+_this.msg+'</div>'+
                    '<div class="popup-btn">'+
                    '<a href="javascript:void(0);" class="alert-btn js-agentSureBtn">'+_this.sureBtn+'</a>'+
                    '</div>'+
                    '</div>'+
                    '<div class="popup-mask js-remove"></div>'+
                    '</div>';
                break;
            case "confirm":
                modelHtml = '<div class="alertDialog">'+
                    '<div class="clazz-popup">'+
                    '<div class="text">'+_this.msg+'</div>'+
                    '<div class="popup-btn">'+
                        '<a href="javascript:void(0);" class="js-agentSureBtn">'+_this.cancelBtn+'</a>'+
                        '<a href="javascript:void(0);" class="js-cancelBtn" style="background-color:#ff7d5a;color: white;">'+_this.sureBtn+'</a>'+
                    '</div>'+
                    '</div>'+
                    '<div class="popup-mask js-remove"></div>'+
                    '</div>';
                break;
            case "tip"://提示信息
                _this.fadeTime = option.fadeTime || 1800; // 默认1秒消息
                var content = "";
                if(typeof (option) == "object"){
                    content = _this.msg
                }else if(typeof (option) == "string"){
                    content = option
                }
                modelHtml = '<div class="alertDialog">'+
                    '<div class="clazz-popup tip">'+
                    '<div class="text">'+content+'</div>'+
                    '</div>'+
                    '<div class="popup-mask js-remove"></div>'+
                    '</div>';
                break;
        }

        $(document.body).append(modelHtml);

        if(_this.type == "tip"){
            setTimeout(function(){
                $(".alertDialog").remove();
            },_this.fadeTime);
        }else{
            //确定事件
            $(document).on("click",".js-agentSureBtn",function(){
                _this.sure();
                $(this).parents(".alertDialog").remove();
            });

            //取消事件
            $(document).on("click",".js-cancelBtn",function(){
                _this.cancel();
                $(this).parents(".alertDialog").remove();
            });
        }
    }


    function setCookie(name, value, day){
        var date = new Date();
        date.setTime(date.getTime() + ((day ? day : 1) * 24 * 60 * 60 * 1000));
        document.cookie=name + "=" + value +";expires="+date.toGMTString()+";path=/"; //path 设置cookie适用位置
    }

    //取得cookie
    function getCookie(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';'); //把cookie分割成组
        for(var i=0;i < ca.length;i++) {
            var c = ca[i]; //取得字符串
            while (c.charAt(0)==' ') { //判断一下字符串有没有前导空格
                c = c.substring(1,c.length); //有的话，从第二位开始取
            }
            if (c.indexOf(nameEQ) == 0) { //如果含有我们要的name
                return unescape(c.substring(nameEQ.length,c.length)); //解码并截取我们要值
            }
        }
        return false;
    }

    //清除cookie
    function clearCookie(name) {
        setCookie(name, "", -1);
    }

    function getCookiedList() {
        return ["localTeacherSid","localTeacherSubjType","schMult","schSortKey","currentSid","slv","odby","stkw"] /*暂存的临时cookie列表*/
    }
    
    function cleanAllCookie() {
        var cookiedList = this.getCookiedList();
        for(var i=0;i<cookiedList.length;i++){this.clearCookie(cookiedList[i])}
    }

    extend(agentTool, {
        version: version,
        extend: extend,
        isBlank : isBlank,
        isMobile : isMobile,
        isEmail : isEmail,
        getQuery : getQuery,
        alert : alertMsg,
        setCookie : setCookie,
        getCookie : getCookie,
        clearCookie : clearCookie,
        getCookiedList : getCookiedList,
        cleanAllCookie : cleanAllCookie
    });

    return agentTool

};