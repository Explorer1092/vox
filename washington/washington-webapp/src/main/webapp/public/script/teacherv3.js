function addHoverEvent(){
    if(!$17.isBlank(ko)){
        ko.bindingHandlers.hover = {
            init: function(element, valueAccessor){
                $(element).hover(
                    function(){
                        $(element).addClass(valueAccessor());
                    },
                    function(){
                        $(element).removeClass(valueAccessor());
                    }
                );
            }
        };
    }
}

function addTextcutEvent(){
    if(!$17.isBlank(ko)){
        ko.bindingHandlers.textcut = {
            init: function(element, valueAccessor){
                var options = ko.utils.unwrapObservable(valueAccessor());

                $(element).text(options.text.length > options.length ? options.text.substring(0, options.length) + "..." : options.text);
            },
            update: function(element, valueAccessor){
                var options = ko.utils.unwrapObservable(valueAccessor());

                $(element).text(options.text.length > options.length ? options.text.substring(0, options.length) + "..." : options.text);
            }
        };
    }
}

function addMyColspanEvent(){
    if(!$17.isBlank(ko)){
        ko.bindingHandlers.mycolspan = {
            init: function(element, valueAccessor){
                $(element).attr("colspan", valueAccessor());
            },
            update: function(element, valueAccessor){
                $(element).attr("colspan", valueAccessor());
            }
        };
    }
}

$(function(){
    //主菜单经过浮动条效果
    $(".v-menu-hover").hover(function(){ $(this).addClass("active"); }, function(){ $(this).removeClass("active"); });
    //主菜单当前项效果
    $(".v-menu-click").on("click", function(){ $(this).radioClass("current"); });
    //退出按钮
    $("a.sign-out").on("click", function(){
        $17.tongji("教师端-菜单-退出");

        setTimeout(function(){ location.href = "/ucenter/logout.vpage"; }, 200);
        return false;
    });

    //获取未读消息
    var loadmeg = new $17.Model({
        time : $17.setDebugValue(60 * 1000, 6000 * 1000)
    });
    loadmeg.extend({
        checkMsg: function(){
            $.get("/teacher/bubbles.vpage", function(data){
                if(data.success){
                    var count = (data.unreadNoticeCount || 0) + (data.unreadLetterAndReplyCount || 0);
                    if(count <= 0){
                        $(".v-msg-count").hide();
                    }else{
                        if(count < 100) {
                            $(".v-msg-count").html(count).show();
                        }
                        else {
                            $(".v-msg-count").html('').addClass('w-icon-plentyMessages').show();
                        }
                    }
                }
            });
        },
        init: function(){
            var $this = this;

            $this.checkMsg();

//            setInterval(function(){
//                $this.checkMsg();
//            }, $this.time);
        }
    }).init();

    //老师右下角弹窗
    var userPopups = new $17.Model({
        messageUrl      : "/userpopup/getuserpopups.vpage",
        continueTime    : $17.sdv(40 * 1000, 6000 * 1000),
        timer           : $17.sdv(60 * 1000, 20 * 1000)
    });
    userPopups.extend({
        getBubbles: function(){
            var $this = this;
            $.get($this.messageUrl, function(data){
                if(data.success){
                    $this.setBubbles(data.htmlList);
                }
            });
        },
        setBubbles: function(htmlList){
            var $this   = this;
            var html    = null;

            if(htmlList.length != 0){
                html = template("t:右下角新消息", {
                    msgList : htmlList
                });

                var _k = false;
                for(var i = 0, l = htmlList.length; i < l; i++){
                    if(htmlList[i].indexOf("布置作业") >= 0){
                        _k = true;
                    }
                }

                if(_k){
                    $17.tongji("小英教研员试卷通知_曝光次数");
                }

                $.eBox({
                    title       : { html : "消息助手" },
                    content     : {
                        html : html
                    },
                    effect      : { type : "slide", speed : 200 },
                    openOnce    : false
                });

                setTimeout(function(){$("#eBoxWrap").remove();}, $this.continueTime);
            }
        },
        init: function(){
            var $this = this;
            $this.getBubbles();

            $(document).on("click", ".v-systemHelpClazzPopup", function(){
                var $ts = $(this);
                if($ts.attr("data-type") == "1"){
                    $17.voxLog({
                        module : "systemHelpClazzPopup",
                        op : "ambHelp"
                    });
                }

                if($ts.attr("data-type") == "2"){
                    $17.voxLog({
                        module : "systemHelpClazzPopup",
                        op : "ownComplete"
                    });
                }
                $("#eBoxWrap").remove();
            });
        }
    }).init();
});