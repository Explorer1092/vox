define(['jquery', 'knockout', 'komapping', 'flexSlider', 'voxSpread', 'voxLogs'], function ($, ko, koMap) {

	(function(){
		var external = window.external || {};

		(external['webDidLoad'] || $.noop).call(
			external,
			JSON.stringify({
				time : Date.now() + '',
				url: location.protocol + '//' + location.hostname + location.pathname.replace(/(\/){2,}/g, '/')
			})
		);
	})();

    try{
        function DynamicSelfMode() {
            var $this = this;

            //弹出框
            $this.SID = ko.observable(getStudentId());

            $this.dialogContent = ko.observable();
            $this.dialogBtn = ko.observable('确定');

            //自学乐园
            $this.dynamicContent = ko.observableArray([]);
            $this.isSuccess = ko.observable(true);
            $this.getDynamic = function (page) {
                $.get("/parentMobile/learning/cyclejournal.vpage", {
                    sid: getStudentId(),
                    currentPage: page
                }, function (data) {
                    if (data.success) {
                        if (data.journalPage.content.length > 0) {
                            $this.dynamicContent(koMap.fromJS(data.journalPage.content));
                            $this.isSuccess(true);
                        } else {
                            $this.isSuccess(false);
                        }
                    } else {
                        $this.isSuccess(false);
                    }
                });
            };

            $this.getDynamic(1);

            //点赞
            $this.getLike = function () {
                var $self = this;

                $.post("/parentMobile/learning/like.vpage", {
                    journalId: $self.journalId,
                    clazzId: $self.clazzId,
                    relevantUserId: $self.relevantUserId,
                    studentId: getStudentId()
                }, function (data) {
                    if (data.success) {
                        dialogAlert("成功");
                    } else {
                        dialogAlert(data.info);
                    }
                });

                YQ.voxLogs({
                    database : 'parent', module: 'm_IhZmrNYg',
                    op : 'o_60oFniKK'
                });
            };


            var appItem = {
                'WALKMAN_ENGLISH': 'book_listen',//随声听
                'PICLISTEN_ENGLISH': 'point_read',//点读机
                'LOGIN': 'go_login',//登录
                'TEXTREAD_CHINESE': 'text_read'//语文朗读
            };

            //打开同步学习工具
            $this.toolRedPointItems = ko.observable(_getCookie("icItem") || '');//设置隐藏应用红点
            $(document).on("click", '.js-clickOpenApp', function () {
                var $self = $(this);
                var $name = $self.attr('data-name');

                YQ.voxLogs({
                    sid : $this.SID() || 0,
                    database : 'parent',
                    module: 'm_IhZmrNYg',
                    op : 'o_37oul7Iu',
                    s0: appItem[$name] || ""
                });

                //http://project.17zuoye.net/redmine/issues/32383
                if (isLogin($name)) {
                    return false;
                }

                if (window['external'] && window.external['innerJump']) {
                    window.external.innerJump(JSON.stringify({"name": appItem[$name] ? appItem[$name] : appItem['LOGIN']}));
                } else {
                    dialogAlert('打开失败');
                }

                //设置隐藏应用红点
                if($self.find(".new-icon").length > 0 ){
                    var iconItems = [];

                    if(_getCookie("icItem")){
                        iconItems = _getCookie("icItem").split('.');
                    }

                    iconItems.push($self.attr("data-index"));

                    _setCookie("icItem", iconItems.join('.'), 15);

                    $self.find(".new-icon").remove();
                }
            });

            //获取孩子列表
            $this.getStudentList = ko.observableArray();
            $this.getStudent = function(){
                $.get('/parentMobile/activity/getStudentList.vpage', function(data){
                    if(data.success){
                        $this.getStudentList(data.students);
                    }
                });
            };

            $this.getStudent();

            //header banner
            $this.headerBanner = ko.observableArray();
            $this.imgDoMain = ko.observable();
            $this.goLink = ko.observable();
            YQ.voxSpread({
                keyId: 220902
            }, function (result) {
                if (result.success && result.data.length > 0) {
                    $this.headerBanner(result.data);
                    $this.imgDoMain(result.imgDoMain);
                    $this.goLink(result.goLink);

                    //外部 jQuery flexSlider 实现效果
                    $("#headerBannerCrm").flexslider({
                        animation: "slide",
                        slideshow: true,
                        slideshowSpeed: 3000,
                        directionNav: false,
                        touch: true //是否支持触屏滑动
                    });
                }else{
                    $("#headerBannerCrm").hide();
                    $("#headerBannerDefault").show();
                }
            });

            //打开链接 - 必须用data-openurl 跳转，需要在壳打开新webView
            $(document).on('click', '[data-openurl]', function(){
                var $openUrl = $(this).attr("data-openurl");
                var $linkType = $(this).attr("data-linktype");

                //单个产品入口按钮_被点击
                if($linkType == 'learnApps'){
                    YQ.voxLogs({
                        sid : $this.SID() || 0,
                        database : 'parent',
                        module: 'm_IhZmrNYg',
                        op : 'o_6AME8aW7',
                        s0 : $(this).attr("data-appkey")
                    });
                }

                //查看更多按钮_被点击
                if($linkType == 'learnAppsMore'){
                    YQ.voxLogs({
                        sid : $this.SID() || 0,
                        database : 'parent',
                        module: 'm_IhZmrNYg',
                        op : 'o_eRzC4DKy'
                    });
                }

                //自学动态_去看看按钮_被点击
                if($linkType == 'qukankan'){
                    YQ.voxLogs({
                        sid : $this.SID() || 0,
                        database : 'parent',
                        module: 'm_IhZmrNYg',
                        op : 'o_YMVndJcy'
                    });
                }

                //切换孩子按钮_被点击
                if($linkType == 'switchStudent'){
                    if($(this).hasClass('active')){
                        return false;
                    }

                    YQ.voxLogs({
                        sid : $this.SID() || 0,
                        database : 'parent',
                        module: 'm_urttBjYT',
                        op : 'o_2iOGqukR',
                        s0 : document.title
                    });

                    //跟客户端交互记录当前选择的孩子
                    try{
                        $this.SID()&&window.external.switchChild(JSON.stringify({"studentId": $(this).data("studentid")||"0", "studentName": $(this).data("studentname")||""}));
                    }catch(e){
                        console.warn("调用switchChild失败！");
                    }

                    setTimeout(function(){
                        location.href = $openUrl;
                    },50);


                    return false;
                }

                if (window['external'] && window.external['openSecondWebview']) {
                    window.external.openSecondWebview( JSON.stringify({
                        url : $openUrl
                    }) );
                } else {
                    location.href = $openUrl;
                }
            });

            YQ.voxLogs({
                sid : $this.SID() || 0,
                database : 'parent',
                module: 'm_IhZmrNYg',
                op : 'o_S49fnrXx'
            });
        }

        var dsMode = new DynamicSelfMode();

        ko.applyBindings(dsMode);

        $(document).ready(function(){
            //趣味学习应用
           setTimeout(function(){
               $("#learnApps").flexslider({
                   animation: "slide",
                   animationLoop: false,
                   slideshow: false,
                   directionNav: false,
                   controlNav: false,
                   itemWidth: 300,
                   minItems: 2.3,
                   maxItems: 2.3
               });
           }, 200);
        });
    }catch(e){
        //显示异常的详细信息
        dialogAlert(" 数据异常");
    }

    if (getQueryString("source_type") === "homework_dynamic_new" || getQueryString("source_type") === "homework_dynamic_finish" || getQueryString("source_type") === "homework_dynamic_check") {
        try {
            window.external.jumpMainTab(JSON.stringify({
                "tab_index": "3"
            }));
        } catch (e) {

        }
    }

    function getStudentId() {
        if (getQueryString('sid')) {
            return getQueryString('sid');
        } else {
            if (_getCookie) {
                return _getCookie('sid');
            } else {
                return 0;
            }
        }
    }

    function _getCookie(name){
        var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
        if(arr=document.cookie.match(reg))
            return unescape(arr[2]);
        else
            return null;
    }

    function _setCookie(name, value, day) {
        var Days = day || 1;
        var exp = new Date();
        exp.setTime(exp.getTime() + Days * 24 * 60 * 60 * 1000);
        document.cookie = name + "=" + escape(value) + ";expires=" + exp.toGMTString();
    }

    //是否已经登录
    function isLogin(tools_type) {
        var msInfo = '已登录';
        var isLogin = false; // true:未登录，false:已登录
        var appMethod = 'go_login';

        if (window['external'] && window.external['getAppIsLogin']) {
            var type = JSON.parse(window.external.getAppIsLogin()).type;

            switch (type) {
                case 'type_no_login' :
                    msInfo = "添加孩子信息，我们会为您推荐专属的学习资源";
                    isLogin = true;
                    appMethod = 'go_login';
                    break;
                case 'type_login_and_no_child' :
                    if(tools_type=="TEXTREAD_CHINESE"){
                        msInfo = "添加孩子信息后，就能使用语文朗读功能";
                        isLogin = true;
                        appMethod = 'add_child';
                    }
                    break;
                case 'type_login_and_has_child' :
                    msInfo = "登录已经绑定孩子";
                    isLogin = false;
                    break;
                default:
            }
        }

        //未登录 - 调取跳到登录
        if (isLogin) {
            dialogAlert(msInfo, function () {
                if (window['external'] && window.external['innerJump']) {
                    window.external.innerJump(JSON.stringify({"name": appMethod, func_model: 'learn', loginSource: 5}));
                }
            }, '添加');
        }

        return isLogin;
    }

    //获取App版本
    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }

    //弹出框
    var recordCallback;
    function dialogAlert(msg, callback, btn) {
        dsMode.dialogContent(msg);

        if(callback) {
            recordCallback = callback;
            if(btn)dsMode.dialogBtn(btn);
        }else{
            recordCallback = null;
            dsMode.dialogBtn('确定');
        }
    }

    $('.js-dialogClose').on('click', function(){
        var $type = $(this).attr("data-type");

        dsMode.dialogContent('');

        if(recordCallback && $type == 'submit'){
            recordCallback();
        }
        return false;
    });

});
