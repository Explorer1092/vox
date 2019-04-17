var vox = vox || {};
vox.task = vox.task || {};

define(['jquery', 'knockout', 'komapping', 'radialIndicator', 'weui', 'voxLogs', 'voxSpread','flexSlider'], function ($, ko, komapping) {
    var defaultInitMode;

    if (typeof(initMode) == "string") {
        switch (initMode) {
            case 'RankIndex':
                defaultInitMode = new RankIndex();//班级列表首页
                break;
            default:
            //intiMode null
        }
    }

    //班级列表首页
    function RankIndex() {
        var $this = this;

        $this.isShowPageNull = ko.observable(true);// 是否显示空内容
        $this.className = ko.observable('班级首页');
        $this.headContent = ko.observable({});
        $this.gossipShow = ko.observable(false);
        $this.gossipFlag = ko.observable();
        $this.gossipFlagFalse = ko.observable();
        $this.integralReward = ko.observable("");
        $this.hasPersonAch = ko.observable(false); //有个人成就
        $this.perAchList = ko.observableArray([]); //展示的成就列表
        $this.perAchObj = ko.observableArray(); //展示的成就对象
        $this.shareSuccess = ko.observable(false);
        $this.indexRefreshFlag = ko.observable(true); //可刷新标志
        $this.scrollAjaxFlag = ko.observable(1); //可滚动加载flag
        //设置title
        if (getExternal()["updateTitle"]) {
            getExternal().updateTitle(document.title, "ffffff", "57b000");
        }
        $this.fullPathUrl = function (url) {
            if(url){
                return location.protocol+'//'+location.host+url;
            }
        };
        $this.gotoRecord = function () {
            YQ.voxLogs({
                module: "m_pL654GlZ",
                op : "o_Sk7xHQMs"
            });
            // location.href = $this.fullPathUrl("/view/mobile/student/clazz/record?new_page=blank&track=clazz");
            if (window['external'] && window.external['openFairylandPage']) {
                window.external.openFairylandPage(JSON.stringify({
                    url: $this.fullPathUrl("/view/mobile/student/clazz/record?new_page=blank&track=clazz"),
                    closeHelp:true,
                    page_viewable:true
                }));
            }
        }
        // 爆料是否有新的提示icon
        if (ClazzRecord || Gossip){
            $.ajax({
                url:'/studentMobile/gossip/reddot.vpage',
                type:'GET',
                dataType:'json',
                success:function (res) {
                    if (res.success){
                        $this.gossipShow(res.redDot);
                        if(res.gossipStatus){
                            $this.gossipFlag(true);
                            $this.gossipFlagFalse(false);
                        }else{
                            $this.gossipFlag(false);
                            $this.gossipFlagFalse(true);
                        }
                    }else{
                        $.alert(res.info);
                    }
                },
                error:function () {
                    $this.gossipFlag(true);
                    $this.gossipFlagFalse(false);
                }
            })
        }

        //个人成就分享弹窗
        $this.personalAchievementPop = function(){
            innerAjax({
                url:'/studentMobile/clazz/achievement/popshare.vpage',
                success:function(data){
                    if (data.success) {
                        if(data.bubbleAchievement && data.bubbleAchievement.achievements){
                            var achList = data.bubbleAchievement.achievements,
                                achLength = achList.length;
                            $this.perAchObj(data.bubbleAchievement); //成就对象
                            if(achLength >0){
                                $this.perAchList(achList);
                                $this.hasPersonAch(true);
                            }
                        }
                    }
                }
            });
            YQ.voxLogs({
                module: 'm_pL654GlZ',
                op : 'o_m80QWNs1'
            });
        };

        //获取成就信息
        $this.personalAchievementPop();

        //分享个人成就
        $this.shareFlag = ko.observable(1);
        $this.shareClazzHeaderClick = function () {
            if($this.shareFlag()){
                $this.shareFlag(0);
                innerAjax({
                    type:'POST',
                    data:{achievement:JSON.stringify($this.perAchObj())}, //成就对象
                    url:'/studentMobile/clazz/achievement/share.vpage',
                    success: function(data) {
                        $this.shareFlag(1);
                        if (data.success) {
                            $this.hasPersonAch(false);
                            $this.shareSuccess(true);
                        } else {
                            $.alert(data.info);
                        }
                    },
                    error: function () {
                        $this.shareFlag(1);
                    }
                });

                YQ.voxLogs({
                    module: 'm_pL654GlZ',
                    op : 'o_VvpYFm3a'
                });
            }
        };

        //拒绝弹窗
        $this.refursePop = function(){
            innerAjax({
                url:'/studentMobile/clazz/achievement/refuseshare.vpage',
                type:'POST',
                data:{achievement:JSON.stringify($this.perAchObj())}, //成就对象
                success: function(data) {
                    if (data.success) {
                        $this.hasPersonAch(false);
                    } else {
                        $.alert(data.info);
                    }
                }
            });

            YQ.voxLogs({
                module: 'm_pL654GlZ',
                op : 'o_B6SImT0N'
            });
        };

        //刷新
        $this.refreshBtn = function () {
            window.location.reload();
        };

        $this.clazzBirthDayList = function () {
            location.href = '/view/mobile/student/clazz/birthday?new_page=blank';
        };

        $this.changeBtnAndStatus = function (node,disabledClass,text,index) {
            $(node).addClass(disabledClass).text(text);
            $this.classDatabase()[index].showBtn = 1;
            $this.classDatabase()[index].disabledBtn = 1;
            $this.classDatabase($this.classDatabase());
        };


        //生日祝福
        $this.blessFlag = ko.observable(1);
        $this.blessBirthDay = function (index,uid,vid) {
            if($this.blessFlag()){
                $this.blessFlag(0);
                var $node = (event.target || event.currentTarget || event.srcElement);

                innerAjax({
                    url:'/studentMobile/clazz/birthday/bless.vpage',
                    type: 'POST',
                    data:{
                        relevantUserId:uid,
                        vid:vid
                    },
                    success:function(data){
                        $this.blessFlag(1);
                        if (data.success) {
                            $.alert('祝福成功');
                            $this.changeBtnAndStatus($node,'disabled','已祝福',index);
                        }else{
                            $.alert(data.info);
                        }
                    },
                    error:function () {
                        $this.blessFlag(1);
                    }
                });
                YQ.voxLogs({
                    module: 'm_pL654GlZ',
                    op : 'o_SAO9IoTO'
                });
            }
        };


        //鼓励成就
        $this.encouFlag = ko.observable(1);
        $this.encourageBtn = function(index,uid,vid){
            if($this.encouFlag()){
                $this.encouFlag(0);
                var $node = (event.target || event.currentTarget || event.srcElement);
                innerAjax({
                    url:'/studentMobile/clazz/achievement/encourage.vpage',
                    type: 'POST',
                    data:{
                        relevantUserId:uid,
                        vid:vid
                    },
                    success:function(data){
                        $this.encouFlag(1);
                        if (data.success) {
                            $.alert('鼓励成功');
                            $this.changeBtnAndStatus($node,'disabled','鼓励成功',index);
                        }else{
                            $.alert(data.info);
                        }
                    },
                    error:function () {
                        $this.encouFlag(1);
                    }
                });

                YQ.voxLogs({
                    module: 'm_pL654GlZ',
                    op : 'o_b7dl5za2'
                });
            }
        };

        $this.showBlessBtn = function () {
            //todo 班级消息首页
            location.href = '/view/mobile/student/clazz/messagelist?new_page=blank';
        };

        //班级公告
        $this.isShowStaticBanner = ko.observable(false);
        $this.headerStaticBanner = ko.observable();
        YQ.voxSpread({
            keyId: 320702
        }, function(items){
            if(items.success && items.data && items.data.length > 0){
                $this.headerStaticBanner(items);
                $this.isShowStaticBanner(true);

                $this.isShowPageNull(false);
            }
        });

        //查询班级签到头条
        $this.headlineIsShow = ko.observable(false);
        $this.headlineData = ko.observable();

        if( typeof(AttendenceFlag) == "boolean" && AttendenceFlag){
            innerAjax({
                url: '/studentMobile/sign/headline.vpage',
                success: function (data) {
                    var signIn = data.signIn || {};

                    if (data.success) {
                        $this.headlineData({
                            totalCount : signIn.totalCount || '',
                            count : signIn.count || 0,
                            rank: signIn.rank || '--',
                            rate: signIn.rate || 0
                        });
                        $this.headlineIsShow(true);

                        $this.isShowPageNull(false);

                        radialIndicator($('.JS-headRadialBox'), {
                            fontFamily : 'TrebuchetMS, Rotobo, Microsoft YaHei, sans-serif',
                            barBgColor : '#e9f7ff',
                            barColor: '#ffdf49',
                            barWidth: 10,
                            initValue: signIn.rate * 100,
                            percentage: true,
                            displayNumber: false
                        });
                    }
                }
            });
        }

        //查询班级头条
        $this.classIsShow = ko.observable(true);
        $this.classTemplateBox = ko.observable("T:班级头条");
        $this.classDatabase = ko.observableArray();

        $this.headLinePageMinId = ko.observable(-1);
        $this.getHeadLineData = function (initFlag) {
            if($this.indexRefreshFlag() && $this.scrollAjaxFlag()){
                $this.scrollAjaxFlag(0);
                innerAjax({
                    url: '/studentMobile/clazz/newheadline.vpage?minId='+$this.headLinePageMinId(),
                    success: function (data) {
                        $this.scrollAjaxFlag(1);
                        if (data.success && data.headlines && data.headlines.length > 0) {
                            for (var i = 0 ;i<data.headlines.length;i++){
                                if(data.headlines[i].userInfos){
                                    for (var j = 0 ;j<data.headlines[i].userInfos.length;j++){
                                        var wearImgIndex = data.headlines[i].userInfos[j];
                                        var wearImg = wearImgIndex.headWearImg;
                                        if (wearImg == undefined){
                                            wearImgIndex["headWearImg"]="";
                                        }
                                    }
                                }
                            }
                            //插入广告数据
                            if(initFlag){ //第一页插入广告，之后的数据往上拼接
                                if(tempClassData.length > 0){ //广告数据可能没值
                                    tempClassData = returnDateArray(data.headlines, tempClassData[0]);
                                }else{
                                    tempClassData = tempClassData.concat(data.headlines);
                                }
                                $this.isShowPageNull(0);
                            }else{
                                // 最后一页不用再往上加
                                if((data.minId == $this.headLinePageMinId()) || (data.headlines.length == 0)){
                                    $this.indexRefreshFlag(false);
                                }else{
                                    tempClassData = tempClassData.concat(data.headlines);
                                }
                            }

                            $this.classDatabase(tempClassData);
                            $this.getCanvasRadial();

                            $this.headLinePageMinId(data.minId ? data.minId : -1); //minId去获取下一页避免重复

                        }else{
                            $this.scrollAjaxFlag(0);
                            if($this.isShowPageNull()){
                                $this.classTemplateBox("T:PageNull");
                            }
                        }
                    },
                    error:function () {
                        $this.scrollAjaxFlag(1);
                    }
                });
            }
        };

        $(document.body).infinite().on("infinite", function() {
            if($this.scrollAjaxFlag()){
                $this.getHeadLineData();
            }
        });

        var tempClassData = [];//请求数据关联
        if( typeof(HeadlineFlag) == "boolean" && HeadlineFlag){
            $this.getHeadLineData(1);
        }else{
            if($this.isShowPageNull()){
                $this.classTemplateBox("T:PageNull");
            }
        }

        //广告处理插入数据到班级列表 -
        function returnDateArray(_item, obj){
            var getName = function(cTime){
                return parseInt(cTime.toString().substr(0, 10));
            };

            var recordArray = [], recordTime = new Date(), recordCount = 0;
            for(var i = 0, headlines = _item; i < headlines.length; i++){
                var commonTime = Date.UTC(recordTime.getFullYear(), recordTime.getMonth(), recordTime.getDate(), 7);

                if( getName(headlines[i].timestamp) >= getName(commonTime) ){
                    recordArray.push(headlines[i]);

                    if((i == headlines.length - 1) && recordCount == 0){
                        recordArray.push(obj);
                        recordCount = 1;
                    }
                }else{
                    if(recordCount == 0){
                        recordArray.push(obj);
                        recordCount = 1;
                    }

                    recordArray.push(headlines[i]);
                }
            }

            return recordArray;
        }

        var loadDate = new Date();
        if(loadDate.getHours() >= 15){
            YQ.voxSpread({
                keyId: 320701
            }, function(items){
                if(items.success && items.data && items.data.length > 0){
                    items.type = "CRMABV";

                    if(tempClassData.length > 0){
                        tempClassData = returnDateArray(tempClassData, items);
                    }else{
                        tempClassData = [items].concat(tempClassData);
                    }

                    $this.classDatabase(tempClassData);
                    $this.classTemplateBox("T:班级头条");
                    $this.isShowPageNull(false);
                }
            });
        }

        $this.getCanvasRadial = function(data, docId){
            var $RadialBox = $(".js-RadialBox");
            if($RadialBox.length > 0){
                $RadialBox.each(function(data){
                    var $self = $(this);
                    var $color = "#57bdf7";

                    if($self.attr('data-subject') == 'MATH'){
                        $color = "#51b65d";
                    }

                    radialIndicator($self, {
                        barBgColor : '#f0f0f0',
                        barColor: $color,
                        barWidth: 10,
                        fontFamily : 'TrebuchetMS, Rotobo, Microsoft YaHei, sans-serif',
                        initValue: $self.attr('data-count'),
                        percentage: true
                    });
                });
            }
        };

        //消息显示是否有新消息
        $this.messagesCount = ko.observable(0);
        innerAjax({
            url: '/studentMobile/message/unread/count.vpage',
            success: function (data) {
                if (data.success) {
                    $this.messagesCount(data.unread_count);
                }
            }
        });

        //读取消息,消除红点提示
        $this.readMessage = function(){
            $this.messagesCount(0);
            location.href = "/view/mobile/student/clazz/messagelist?new_page=blank";
        };

        $this.logs = function(data, index, op){
            YQ.voxLogs({
                module: 'm_NFeFK2sM',
                op : op,
                s0 : data.achievementType || data.subject || '',
                s1 : data.type || '',
                s2 : index || '',
                s3 : data.homeworkId || ''
            });
        };

        $this.headlinesClick = function(url, index, op, data){
            $this.logs(data, index, op);

            location.href = url;
        };

        if (getExternal()['usePullRefresh']) {
            getExternal().usePullRefresh(true);
        }

        //返回刷新
        vox.task.refreshData = function(){
            location.reload();
        };

        /*增值活动*/
        $this.activityDetail = ko.observableArray([]);
        $this.getActivityDetail = function () {
            $.get("/studentMobile/rank/vaazstatus.vpage", {}, function (data) {
                if (data.success) {
                    $this.activityDetail([komapping.fromJS(data)]);
                    $this.isShowPageNull(false);
                    YQ.voxLogs({
                        module: 'm_8GBlaqZv',
                        op : 'o_ImzxQp4u'
                    });
                } else {
                    YQ.voxLogs({
                        module: 'm_8GBlaqZv',
                        op : 'page_classTab_activity_load_activityclazzstatus',
                        s0: data.info || ''
                    });
                }
            });
        };
        if (ValueAddedActivityZoneFlag) {
            $this.getActivityDetail();
        }

        //打开活动页面
        $this.activityBtn = function () {
            if(getExternal().openFairylandPage){
                getExternal().openFairylandPage(JSON.stringify({
                    url: window.location.origin + '/resources/apps/hwh5/adventureteam/v100/index.html',
                    name: "fairyland_app:activity",
                    useNewCore: "crossWalk",
                    orientation: "portrait"
                }));
            }
            YQ.voxLogs({
                module: 'm_8GBlaqZv',
                op : 'o_nRx7K5qK'
            });
        };

        YQ.voxLogs({
            module: 'm_NFeFK2sM',
            op : 'page_classTab_load_success'
        });
    }

    if (defaultInitMode) {
        defaultInitMode.clickLike = function (data ,event) {
            var $data = data;
            var $likeBtn = $(event.currentTarget);

            if (!$likeBtn.hasClass("praised")) {
                return;
            }

            innerAjax({
                url: '/studentMobile/rank/like.vpage',
                type: 'POST',
                data: {
                    likedUserId: $data.userId,
                    type: defaultInitMode.type
                },
                success: function (data) {
                    if (data.success) {
                        $likeBtn.removeClass("praised")
                            .text($data.likeCount += 1);
                    }
                }
            });
        };

        //成长等级
        defaultInitMode.levelMapJson = function(i){
            if(i <= 5){
                return '1';
            }else if(i > 5 && i <= 10){
                return '2';
            }else if(i > 10 && i <= 15){
                return '3';
            }else if(i > 15 && i <= 20){
                return '4';
            }else if(i > 20 && i <= 25){
                return '5';
            }else if(i > 25){
                return '6';
            }else{
                return '1';
            }
        };

        defaultInitMode.pressImage = function(link, w){
            var defW = 200;
            if(w){
                defW = w;
            }

            if(link && link != "" && (link.indexOf('oss-image.17zuoye.com') > -1 || link.indexOf('cdn-portrait.17zuoye.cn') > -1 || link.indexOf('cdn-portrait.test.17zuoye.net/') > -1) ){
                return link + '@' + defW + 'w_1o_75q';
            }else{
                return link;
            }
        };

        ko.applyBindings(defaultInitMode);
    }

    function innerAjax(opt) {
        /*opt = { url : '', data : {},   type : "GET", success : function(){}, error : function(){} };*/
        $.ajax({
            url: opt.url,
            type: opt.type || 'GET',
            data: opt.data || {},
            success: function (data) {
                if (opt.success)opt.success(data);
            },
            error: function (data) {
                if (opt.error)opt.error(data);
            }
        });
    }

    //获取QueryString
    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }

    function getExternal(){
        var _WIN = window;
        if(_WIN['yqexternal']){
            return _WIN.yqexternal;
        }else if(_WIN['external']){
            return _WIN.external;
        }else{
            return _WIN.external = function(){};
        }
    }

    //点击打点
    $(document).on("click", "[data-logs]", function(){
        try {
            var $self = $(this);
            var $logsString = $self.attr("data-logs");
            var $logsItems = {};

            if($logsString != ""){
                var $logsJson = eval("(" + $logsString + ")");
                // m : $logsJson.m, op: $logsJson.op, s0:$logsJson.s0, s1 : $logsJson.s1
                if($logsJson.m){ $logsItems.module = $logsJson.m; }
                if($logsJson.op){ $logsItems.op = $logsJson.op; }

                $logsItems.s0 = $logsJson.s0 || $self.attr('data-s0') || "";
                $logsItems.s1 = $logsJson.s1 || $self.attr('data-s1') || "";

                YQ.voxLogs($logsItems);
            }
        }catch (e){
            // console.info(e);
        }
    });
});