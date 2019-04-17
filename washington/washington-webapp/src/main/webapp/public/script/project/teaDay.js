define(['jquery','template',"voxLogs","jqcookie"],function($,template){
    var getUrlParam=function(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
        var r = window.location.search.substr(1).match(reg);  //匹配目标参数
        if (r != null) return unescape(r[2]); return null; //返回参数值
    };

    /*--老师tab--*/
    var teaHeader = $(".teaDay-header"),
        teaBanner = $(".teaDay-banner"),
        tabMain   = $(".tab-main"),
        initIndex = 0;
    initIndex = parseInt($.cookie('n_tid_status')) || 0;
    $.cookie('n_tid_status',null);

    $(".js-teacher").on("click",function(){
        var $this=$(this);
        $this.addClass("active").siblings().removeClass("active");
        if($this.data().bless == 0){
            teaHeader.removeClass("bg");
            teaBanner.show();
        }else{
            teaHeader.addClass("bg");
            teaBanner.hide();
        }
        tabMain.children().eq($this.index()).show().siblings().hide();
    }).eq(initIndex).click();

    var blessBox = $("#js-box"),
        imgUrl = 'aaa';
    //获取App版本
    function getAppVersion() {
        var native_version = "";

        if (window["external"] && window.external["getInitParams"]) {
            var $params = window.external.getInitParams();

            if ($params) {
                $params = $.parseJSON($params);
                native_version = $params.native_version;
            }
        }

        return native_version;
    }

    var native_version = getAppVersion(),
        version = native_version.split('.'),
        part1 = parseInt(version[0]),
        part2 = parseInt(version[1]),
        part3 = parseInt(version[2]);

    //App版本>=2.6.0.0
    function versionValidate() {
        if (part1 > 2) {
            return true;
        }
        else if (part1 == 2 && part2 > 6) {
            return true;
        }
        else if (part1 == 2 && part2 == 6 && part3 >= 0) {
            return true;
        }
        return false;
    }

    function versionValidate270() {
        return (part1 == 2 && part2 == 6);
    }

    /*--发送祝福--*/
    $(".js-send").on("click",function(){
        var current=$(".js-teacher.active");
        var $isVersion = versionValidate();

        setTimeout(function(){
            if($isVersion){
                $.cookie('n_tid_status',current.index());
                location.href="/studentMobile/teacherDay/bless/send.vpage?teacher_id="+current.data().tid;
            }else{
                location.href="/studentMobile/teacherDay/bless/update.vpage";
            }
        }, 200);
    });

    $(document).on("click",".js-send-bless",function(){
        var data={
            img_url:imgUrl,
            teacher_id:$("#js-teacher").data().tid
        };
        $.post("/studentMobile/teacherDay/bless/send.vpage",data,function(res){
            if(res.success){
                blessBox.html(template("T:发送祝福成功",{imgUrl:imgUrl}));
                history.back();
            }else{
                $.alert(res.info);
            }
        });
    });

    /*--拍照--*/
    $(document).on("click",".js-photo",function(){
        if(window.external && ('getImageByHtml' in window.external)){
            window.external.getImageByHtml(JSON.stringify( {
                "uploadUrlPath":"/v1/user/file/upload.vpage",
                "NeedAlbum":false,
                "NeedCamera":true,
                "uploadPara": {
                    "activity":"teacherday2016"
                }
            }));
        }else{
            var u = navigator.userAgent;
            var isiOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/);
            if(isiOS){
                $.alert("一起作业无法获取您的相机权限<br />请在 设置-隐私-相机 中打开");
            }else{
                $.alert("一起作业无法获取您的相机权限<br />请在权限管理中打开");
            }

        }
    });
    //拍照回调函数
    window.vox={
        task:{
            setImageToHtml:function(res){
                res = JSON.parse(res);
                if(res.errorCode == 0){     //上传成功
                    imgUrl = res.files.IMAGE[0];
                    blessBox.html(template("T:已拍照",{imgUrl:imgUrl}));
                    $(".upload-tip").fadeIn("slow");
                }else{
                    $.alert("图片上传失败!");
                }
            }
        }
    };
    //删除拍的照片按钮
    $(document).on("click",".js-delete",function(){
        blessBox.html(template("T:未拍照",{imgUrl:imgUrl}));
    });

    /*--调起家长通App--*/
    $(".js-invite").on("click",function(){
        YQ.voxLogs({
            module:"m_eNEFjA5U",
            op:"o_RS5m2xCD"
        });

        var $isVersion = versionValidate270();

        var u = navigator.userAgent;
        var isiOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/);

        if(!isiOS){
            setTimeout(function(){
                if($isVersion){
                    location.href="a17parent://platform.open.api/parent_main";
                }else{
                    if(window.external && ('openparent' in window.external)){
                        window.external.openparent("");
                    }else{
                        $.alert("调起家长通失败!");
                    }
                }
            }, 200);
        }else{
            if(window.external && ('openparent' in window.external)){
                window.external.openparent("");
            }else{
                $.alert("调起家长通失败!");
            }
        }
    });

    /*--弹窗--*/
    var popupWrapper=$("#popup-wrapper");
    $(".js-student").on("click",function(){
        var $this=$(this).children(".name").first();
        var data={
            sname : $this.hasClass("js-my-bless") ? "我" : $this.html(),
            showParent : $this.hasClass("js-my-bless"),
            imgUrl : $this.data().url,
            flowerCount: $this.hasClass("js-my-bless") ? $this.data().count : 0
        };
        popupWrapper.html(template("T:我的祝福", data));
    });

    $(".js-rule").on("click",function(){
        popupWrapper.html(template("T:活动规则",{}));
    });
    $(document).on("click",".close",function(){
        popupWrapper.html('');
    });

    if(location.pathname == "/studentMobile/teacherDay/bless/index.vpage"){
        YQ.voxLogs({
            module:"m_eNEFjA5U",
            op:"o_8D4UALh5",
            s0:getUrlParam("s0")
        });
    }

    if(location.pathname == "/studentMobile/teacherDay/bless/send.vpage"){
        YQ.voxLogs({
            module:"m_eNEFjA5U",
            op:"o_iRWe8NaS",
            s0:getUrlParam("s0")
        });
    }
});