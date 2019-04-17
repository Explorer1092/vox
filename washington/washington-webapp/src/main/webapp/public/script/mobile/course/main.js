define(['jquery', 'knockout', 'weui', 'flexSlider', 'voxSpread', 'voxLogs', 'template', 'external'], function($, ko){
    (function(){
        check_external('webDidLoad', function (exist) {
            if (exist) {
                do_external('webDidLoad', {
                    time : Date.now() + '',
                    url: location.protocol + '//' + location.hostname + location.pathname.replace(/(\/){2,}/g, '/')
                });
            }
        });
    })();

    function GoClassMode(){
        var $this = this;

        //header banner
        $this.headerBanner = ko.observableArray();
        $this.imgDoMain = ko.observable();
        $this.goLink = ko.observable();
        YQ.voxSpread({
            keyId: 220702
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
            }
        });

        //打开链接
        $this.GotoLink = function(link, name, tag){
            if(link != ""){
                if(tag){
                    YQ.voxLogs({
                        database: 'parent',
                        module: 'm_SK5wQZLl',
                        op: 'o_3xsrvu8S',
                        s0: name ? encodeURI(name) : 'null',
                        target: link
                    });
                }else{
                    YQ.voxLogs({
                        database: 'parent',
                        module: 'm_SK5wQZLl',
                        op: 'o_xQ1g2aF0',
                        s0: name ? encodeURI(name) : 'null',
                        target: link
                    });
                }

                check_external('openSecondWebview', function (exist) {
                    if (exist) {
                        do_external('openSecondWebview', {
                            url: link,
                            useNewCore : "wk"
                        });
                    } else {
                        location.href = link;
                    }
                });
            }else{
                YQ.voxLogs({
                    database: 'parent',
                    module: 'm_SK5wQZLl',
                    op: 'link_null'
                });
            }
        };
    }

    function CourseListMode(){
        var $this = this;
        var originalData = [];

        $this.templateBox = ko.observable("T:loading");
        $this.database = ko.observable();
        $this.isShowLoading = ko.observable(true);
        $this.showPageSize = 4;
        $this.flagIndex = true;

        $this.getDataContent = function(num){
            $.post("/mizar/course/loadcoursepage.vpage", {
                category: pageCategory,
                pageSize: $this.showPageSize,
                pageNum: num
            }, function(data){
                if(data.success){
                    $.each(data.rows, function(i, item){
                        if(typeof(item.soldOut) != 'boolean'){
                            item["soldOut"] = false;
                        }

                        item["offLineTips"] = false;
                        if(item.status == "OFFLINE"){
                            if($this.flagIndex){
                                item["offLineTips"] = true;
                                $this.flagIndex = false;
                            }
                        }
                    });

                    originalData = originalData.concat(data.rows);

                    if(originalData.length > 0){
                        $this.database({
                            rows: originalData
                        });
                        $this.templateBox("T:listContent");

                        if(data.rows.length < $this.showPageSize){
                            $this.isShowLoading(false);
                        }
                    }else{
                        $this.database({info: data.info || ""});
                        $this.templateBox("T:nullContent");
                    }
                }else{
                    $this.database({info: data.info || ""});
                    $this.templateBox("T:nullContent");
                }
            });
        };

        $this.getDataContent(1);

        var loading = false;//状态标记;
        var pageNumber = 1;//page

        $(document.body).infinite().on("infinite", function() {
            if(loading || !$this.isShowLoading()) return;

            loading = true;
            setTimeout(function() {
                $this.getDataContent(pageNumber += 1);
                loading = false;
            }, 800);   //模拟延迟
        });
    }

    //微课堂
    function MicroCourseMode() {
        var $this = this;
        var originalData = [];
        $this.templateBox = ko.observable("T:loading");
        $this.database = ko.observable();
        $this.dataTag = ko.observable(true);
        $this.tag = ko.observable();
        $this.loading = ko.observable(false);
        $this.pageNumber = ko.observable(1);
        $this.indexTag = ko.observable();
        $this.isShowLoading = ko.observable(true);
        $this.showPageSize = 10;
        $this.hasTopBanner = ko.observable(false);
        $this.hasNormalBanner = ko.observable(false);
        $this.imagesDetail = ko.observableArray([]);
        $this.nomalimagesDetail = ko.observableArray([]);
        $this.getQuery = function (item){
            var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
            return svalue ? decodeURIComponent(svalue[1]) : '';
        };
        $this.is_android_device = function (){
            if(navigator && navigator.userAgent && navigator.userAgent.toUpperCase().indexOf('ANDROID') != -1){
                return true;
            }
            return false;
        };

        if($this.getQuery("topb") == "show"){
            $("#noTopBar").hide();
            $("#tabList").removeClass("fixTab02");
            //安卓下适配头部

            if($this.is_android_device()){
                $("#hasTopBar").addClass("android-header");
                $("#tabList").addClass("fixTab03");
            }

            $("#hasTopBar").show();
        }else{
            $("#hasTopBar").hide();
            $("#noTopBar").show();
            $("#tabList").addClass("fixTab02");
        };

        $this.tagChange = function(){
            var $thisNode = $(event.currentTarget);
            $this.indexTag($thisNode.data("index"));
            $this.tag($thisNode.text().trim());
            $this.dataTag(true);
            $(window).scrollTop(0);
            $this.loading(false);//状态标记;
            $this.pageNumber(1);//page;
            $thisNode.addClass("active").siblings("li").removeClass("active");
            if($this.indexTag() != 0){
                $this.getDataContent(1,$this.tag());
            }else{
                $this.getDataContent(1,"");
            }
        };

        $this.getDataContent = function(num,tag){
            $.post("/mizar/course/loadcoursepage.vpage", {
                category: pageCategory,
                pageSize: $this.showPageSize,
                pageNum: num,
                tag: tag
            }, function(data){
                if(data.success){
                    var rows = data.rows ? data.rows : [];
                    if(rows.length > 0){
                        $.each(rows,function(i,item){
                            rows[i]["hasTag"] = false;
                            rows[i]["tagClass"] = "";
                            if(rows[i].tags.length > 0){
                                rows[i]["hasTag"] = true;
                            }
                            if(rows[i].color){
                                rows[i]["tagClass"] = "l-label "+rows[i].color;
                            }
                        });
                        if ($this.dataTag()){
                            originalData = rows;
                            $this.dataTag(false);
                            $this.isShowLoading(true)
                        }else{
                            originalData = originalData.concat(rows);
                        }
                        $this.database({
                            rows: originalData,
                            info: "",
                            hasData: true
                        });
                        if(rows.length < $this.showPageSize ){
                            $this.isShowLoading(false);
                        }
                        $this.templateBox("T:listContent");
                        $("#noContent").remove();
                    }else if (rows.length == 0){
                        $this.isShowLoading(false);
                        if ($this.dataTag()){
                            $this.database({
                                rows: [],
                                info: "",
                                hasData: false
                            });
                            $this.templateBox("T:listContent");
                        }
                    }
                }else{
                    $this.database({info: data.info ? data.info : "暂时还没有课程哦",hasData:false});
                    $this.templateBox("T:nullContent");
                }
            });
        };

        $this.getDataContent(1,"");

        $(document.body).infinite().on("infinite", function() {
            if($this.loading() || !$this.isShowLoading()) return;
            $this.loading(true);
            setTimeout(function() {
                $this.pageNumber($this.pageNumber()+1);
                if ($this.indexTag()==0){
                    $this.getDataContent($this.pageNumber());
                }else{
                    $this.getDataContent($this.pageNumber(),$this.tag());
                }
                $this.loading(false);
            }, 400);   //模拟延迟
        });

        //图片广告位
        $this.getImageAdp = function (id) {
            $.post('/be/newinfo.vpage', {p: id}, function (data) {
                if (data.success) {
                    if(data.data.length !=0) {
                        $this.hasTopBanner(true);
                        ko.utils.arrayForEach(data.data, function (value) {
                            value.img = data.imgDoMain + 'gridfs/' + value.img;
                        });
                        $this.imagesDetail(data.data);
                        setTimeout(function () {
                            //顶部导航广告。
                            $("#bannerContentBox").flexslider({
                                animation: "fade",
                                directionNav: false,
                                pauseOnAction: true,
                                after: function (slider) {
                                    if (!slider.playing) {
                                        slider.play();
                                    }
                                },
                                slideshowSpeed: 4000,
                                animationSpeed: 400
                            });
                        }, 0);
                    }else{
                        $this.hasTopBanner(false);
                    }
                }
            });
        };
        $this.getImageAdp(220903);


        //长期班广告位
        $this.getNormalBanner = function (id) {
            $.post('/be/newinfo.vpage', {p: id}, function (data) {
                if (data.success) {
                    if(data.data.length !=0){
                        $this.hasNormalBanner(true);
                        ko.utils.arrayForEach(data.data, function (value) {
                            value.img = data.imgDoMain + 'gridfs/' + value.img;
                        });
                        $this.nomalimagesDetail(data.data);
                    }else{
                        $this.hasNormalBanner(false);
                    }
                }
            });
        };
        $this.getNormalBanner(220904);

        //广告点击
        $this.topImgBtn = function (index) {
            var that = this;
            if(that.hasUrl){
                setTimeout(function () {
                    openLink('/be/london.vpage?aid='+that.id+"&index="+index);
                }, 200);
            }
        };

        $this.myCourseBtn = function () {
            var link = '/mizar/course/mycourse.vpage';
            openLink(link);
        };

        $this.normalBtn =function(){
            openLink('/mizar/course/microcourse-normal.vpage');
        }

    }

    var openLink = function(link){
        check_external('openSecondWebview', function (exist) {
            if (exist) {
                do_external('openSecondWebview', {
                    url: link,
                    useNewCore : "wk"
                });
            } else {
                location.href = link;
            }
        });
    };

    var isFromWeChat = function () {
        return (window.navigator.userAgent.toLowerCase().indexOf("micromessenger") > -1);
    };

    var hiddenWechatShare = function () {
        if(isFromWeChat()){ //微课堂支持微信打开，屏蔽分享
            function onBridgeReady(){
                WeixinJSBridge.call('hideOptionMenu');
            }

            if (typeof WeixinJSBridge == "undefined"){
                if( document.addEventListener ){
                    document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
                }else if (document.attachEvent){
                    document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
                    document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
                }
            }else{
                onBridgeReady();
            }
        }
    };

    if(typeof(initMode) == 'string' && initMode != ""){
        switch (initMode){
            case "MicroCourseMode":

                $(document).on("click",".js-courseItem",function () {
                    var link = $(this).data("link");
                    if(link){
                        openLink(link);
                    }
                });

                hiddenWechatShare();

                ko.applyBindings(new MicroCourseMode());

                YQ.voxLogs({
                    database: 'parent',
                    module:'m_SK5wQZLl',
                    op:'o_EpyJSIUu'
                });

                break;
            case "GoClassMode":
                //好课试听
                $("#GoodCourseBox").flexslider({
                    animation: "slide",
                    animationLoop: false,
                    slideshow: false,
                    directionNav: false,
                    controlNav: false,
                    itemWidth: 450,
                    minItems: 1.2,
                    maxItems: 1.2
                });

                ko.applyBindings(new GoClassMode());

                $(document).on("click", ".JS-logs", function(){
                    YQ.voxLogs({
                        database: 'parent',
                        module: 'm_SK5wQZLl',
                        op: 'o_3xsrvu8S',
                        s0: encodeURI($.trim($(this).text()))
                    });
                });

                YQ.voxLogs({
                    database: 'parent',
                    module: 'm_SK5wQZLl',
                    op: 'o_EpyJSIUu'
                });
                break;
            case "CourseListMode":
                if(typeof(pageCategory) == 'string' && pageCategory != ""){
                    //每日一课
                    if(pageCategory == "DAY_COURSE"){
                        YQ.voxLogs({
                            database: 'parent',
                            module: 'm_SK5wQZLl',
                            op: 'o_qbYzF0Np'
                        });
                    }

                    //好课试听
                    if(pageCategory == "GOOD_COURSE"){
                        YQ.voxLogs({
                            database: 'parent',
                            module: 'm_SK5wQZLl',
                            op: 'o_tTu5nMMq'
                        });
                    }

                    //精品视频课程
                    if(pageCategory == "VIDEO_COURSE"){
                        YQ.voxLogs({
                            database: 'parent',
                            module: 'm_SK5wQZLl',
                            op: 'o_jmEgwCup'
                        });
                    }

                    //亲子活动
                    if(pageCategory == "PARENTAL_ACTIVITY"){
                        YQ.voxLogs({
                            database: 'parent',
                            module: 'm_PXQwOTTc',
                            op: 'o_tq3j2qx8'
                        });
                    }

                    ko.applyBindings(new CourseListMode());
                }
                break;
            case "MyCourse":
                YQ.voxLogs({
                    database: 'parent',
                    module: 'm_SK5wQZLl',
                    op: 'o_CqIGKYQV'
                });

                $(document).on("click", ".JS-logs", function(){
                    YQ.voxLogs({
                        database: 'parent',
                        module: 'm_SK5wQZLl',
                        op: 'o_1tBJjCB0',
                        s0: $(this).attr("data-id")
                    });
                });

                hiddenWechatShare();
                break;
            case "GoClassNew":
                $(document).on("click", ".JS-clickGotoLink", function(){
                    var $self = $(this);
                    check_external('openSecondWebview', function (exist) {
                        if (exist) {
                            do_external('openSecondWebview', {
                                url: $self.attr("data-link"),
                                useNewCore : "wk"
                            });
                        } else {
                            location.href =  $self.attr("data-link");
                        }
                    });
                });
                //ActivityBannerBox 220703  :test 220801
                YQ.voxSpread({
                    keyId: 220703
                }, function (result) {
                    if (result.success && result.data &&  result.data.length > 0) {
                        $("#ActivityBannerBox").html( template("T:ActivityBannerBox", {result: result}) );
                    }
                });

                //外部 jQuery flexSlider 实现效果
                $("#ParentalActivityBox").flexslider({
                    animation: "slide",
                    animationLoop: true,
                    direction: "vertical",
                    slideshowSpeed: 1500,
                    directionNav: false,
                    controlNav: false,
                    touch: true //是否支持触屏滑动
                });

                ko.applyBindings(new GoClassMode());

                YQ.voxLogs({
                    database: 'parent',
                    module: 'm_SK5wQZLl',
                    op: 'GoClassNew'
                });
        }
    }

    window.onload = function(){
        check_external('closeLoading', function (exist) {
            if (exist) {
                do_external('closeLoading');
            }
        });
    };
});