define(['jquery', 'flexSlider', 'stellar', 'impromptu', 'template'], function ($) {
    var locationPathName = location.pathname;
    var listsLen = false;
    if (locationPathName == "/help/downloadApp.vpage") {
        var startAtCount = 1;

        var getCount = parseInt(getQueryString("count"));

        if(typeof(getCount) == "number" && getCount <= 2){
            startAtCount = getCount;
        }

        $(document).on('click', '.JS-carousel li', function () {
            var $self = $(this);
            var $currentBox = $(".JS-slides-banner[data-type='"+ startAtCount +"']");

            $currentBox.find(".JS-currentImg ul").animate({'margin-left': (-244 * $self.index() + 'px')});
            $currentBox.find(".JS-textInfo p").eq($self.index()).show().siblings().hide();

            $self.addClass("flex-active").siblings().removeClass("flex-active");
        });

        $(".JS-animation").flexslider({
            animation: "slide",
            startAt : startAtCount,
            slideshow: false,
            slideshowSpeed: 3000,
            directionNav: false,
            animationLoop: false,
            manualControls: ".JS-downloadType",
            start: function (slider) {
                $('.JS-flex-prev').on("click", function () {
                    slider.flexAnimate(slider.getTarget("previous"), true);
                });

                $('.JS-flex-next').on("click", function () {
                    slider.flexAnimate(slider.getTarget("next"), true);
                });

                flexInit(slider.currentSlide);
            },
            after:function(slider){
                flexInit(slider.currentSlide);
            }
        });

        function flexInit(count){
            startAtCount = count;

            $(".JS-carousel li:first").addClass("flex-active").siblings().removeClass("flex-active");
        }
    }

    if (locationPathName == "/help/uservoice.vpage") {
        $(".JS-uservoice-banner").flexslider({
            animation: "slide",
            direction: "vertical",
            slideshow: true,
            slideshowSpeed: 3000,
            directionNav: false,
            animationLoop: false,
            manualControls: ".JS-carousel-us span",
            start: function (slider) {
                $('.JS-flex-previous').on("click", function () {
                    slider.flexAnimate(slider.getTarget("previous"), true);
                });

                $('.JS-flex-next').on("click", function () {
                    slider.flexAnimate(slider.getTarget("next"), true);
                });

                //滚轮事件
                $(".JS-uservoice-banner").on("mousewheel DOMMouseScroll", function (e) {
                    var delta = (e.originalEvent.wheelDelta && (e.originalEvent.wheelDelta > 0 ? 1 : -1)) ||  // chrome & ie
                        (e.originalEvent.detail && (e.originalEvent.detail > 0 ? -1 : 1));              // firefox

                    if(delta > 0){
                        slider.flexAnimate(slider.getTarget("previous"), true);
                    }else{
                        slider.flexAnimate(slider.getTarget("next"), true);
                    }
                });
            },
            after: function(slider){
                if(slider.currentSlide == 4){
                    $(".JS-flex-next").slideUp(100);
                }else{
                    $(".JS-flex-next").slideDown(100);
                }
            }
        });

        setModeHeight(".JS-uservoice-banner li, .JS-uservoice-banner", 800);

        $(window).resize(function () {
            setModeHeight(".JS-uservoice-banner li, .JS-uservoice-banner", 800);
        });
    }

    if (locationPathName == "/help/news/index.vpage") {
        $(".JS-newIndex-banner").flexslider({
            animation: "slide",
            slideshow: true,
            slideshowSpeed: 3000,
            after: function (slider) {
                if (!slider.playing) {
                    slider.play();
                }
            },
            directionNav: false,
            animationLoop: false
        });

        //新闻数据，添加时，请按时间顺序逐条添加s
        var data = [
                {"time":"2018-01-16","title":"一起作业未来小嘉宾团亮相《艾问人物》","url":"newscontent_87.vpage"},
                {"time":"2018-01-10","title":"一起作业27位选手亮相《最强大脑》 百名学霸脑力燃烧","url":"newscontent_86.vpage"},
                {"time":"2017-01-08","title":"一起作业联合新浪教育，发布全国中小学生课外培训调查报告","url":"newscontent_85.vpage"},
                {"time":"2017-12-29","title":"一起作业与厦门教育界大咖齐聚，共话大数据与教育国际化","url":"newscontent_84.vpage"},
                {"time":"2017-12-28","title":"一起作业200万小用户与王俊凯同批捐赠张掖公益图书馆","url":"newscontent_88.vpage"},
                {"time":"2017-12-18","title":"一起作业亮相中韩经贸合作交流会 牵手韩国教育公司e-future","url":"newscontent_83.vpage"},
                {"time":"2017-12-13","title":"28年教龄校长使用一起作业探索教学新模式","url":"newscontent_82.vpage"},
                {"time":"2017-12-06","title":"一起作业刘畅：互联网教育做到今天也是刚开始","url":"newscontent_79.vpage"},
                {"time":"2017-12-05","title":"乌镇会刊专访 刘畅：在线教育促进教育均衡","url":"newscontent_78.vpage"},
                {"time":"2017-12-04","title":"再登乌镇世界互联网大会，刘畅谈网络文化共建共享","url":"newscontent_77.vpage"},
                {"time":"2017-12-01","title":"一起作业亮相云南省互联网大会 大数据开启教育新篇章","url":"newscontent_76.vpage"},
                {"time":"2017-11-29","title":"一起作业肖盾GES大会现场：这是教育信息化最好的时代","url":"newscontent_75.vpage"},
                {"time":"2017-11-24","title":"一起作业荣登“2017德勤-华兴中国明日之星”榜单","url":"newscontent_81.vpage"},
                {"time":"2017-11-23","title":"一起作业与大连教育学院联合开展十三五重点课题研究","url":"newscontent_74.vpage"},
                {"time":"2017-11-23","title":"一起作业联合艾瑞咨询 发布史上第一份中小学在线作业用户洞察报告","url":"newscontent_73.vpage"},
                {"time":"2017-11-15","title":"刘畅GET大会演讲：一起作业是谁，为了谁，要去哪里？","url":"newscontent_72.vpage"},
                {"time":"2017-11-04","title":"关爱孤残儿童 一起作业连续四年“为爱行走”","url":"newscontent_71.vpage"},
                {"time":"2017-11-03","title":"一起作业携手“小小铅笔”走进河南 让留守儿童感知现代教育","url":"newscontent_70.vpage"},
                {"time":"2017-10-29","title":"一起作业少年科学团对话全球顶级科学家","url":"newscontent_68.vpage"},
                {"time":"2017-10-26","title":"一起作业“17师训讲堂”广州开讲“绘本教学”广获好评","url":"newscontent_69.vpage"},
                {"time":"2017-10-24","title":"太原乡村小学连线北京　孩子们看着屏幕跟外教学口语","url":"newscontent_67.vpage"},
                {"time":"2017-10-21","title":"一起作业联合北师大在京召开混合式作业课题研讨会","url":"newscontent_66.vpage"},
                {"time":"2017-10-09","title":"温度教育就是“眼中有人 心中有爱”","url":"newscontent_80.vpage"},
                {"time":"2017-09-20","title":"教育改革多元化　一起作业助力青岛信息化教学卓有成效","url":"newscontent_65.vpage"},
                {"time":"2017-09-19","title":"“小小铅笔”走进黑龙江 一起作业驱动教育公平","url":"newscontent_64.vpage"},
                {"time":"2017-09-16","title":"实践培养学生六大核心素养 一起作业上线“成长世界”","url":"newscontent_63.vpage"},
                {"time":"2017-09-14","title":"一起作业引领“互联网+英语教育” 引资深教研员关注","url":"newscontent_62.vpage"},
                {"time":"2017-09-07","title":"教育大数据时代 一起作业如何引领潮流","url":"newscontent_60.vpage"},
                {"time":"2017-09-01","title":"一起作业牵手《最强大脑》，全球搜索“青春大脑”","url":"newscontent_59.vpage"},
                {"time":"2017-08-25","title":"教育公益圈现最年轻“CEO” 现身甘肃秦安助力教育公平","url":"newscontent_58.vpage"},
                {"time":"2017-08-22","title":"一起作业牵手剑桥大学出版社 加速布局Global教育资源","url":"newscontent_57.vpage"},
                {"time":"2017-08-04","title":"《经济学人》7月聚焦智能教育，一起作业为中国唯一代表案例","url":"newscontent_56.vpage"},
                {"time":"2017-07-15","title":"一起作业亮相第七届数博会 大数据引擎催生教育新业态","url":"newscontent_54.vpage"},
                {"time":"2017-07-07","title":"一起作业和11位中小学名校校长畅谈“互联网+教育”","url":"newscontent_53.vpage"},
                {"time":"2017-07-06","title":"教育大数据领域顶级会议EDM首次登陆中国，一起作业受邀分享探索经验！","url":"newscontent_52.vpage"},
                {"time":"2017-06-30","title":"一封有4000万个收件人的特殊感谢信","url":"newscontent_55.vpage"},
                {"time":"2017-06-29","title":"巴蜀中学、珊瑚小学与一起作业联合开展十三五重点课题研究","url":"newscontent_51.vpage"},
                {"time":"2017-06-14","title":"一起作业·蒲公英图书馆正式开馆 儿童公益推动教育公平","url":"newscontent_50.vpage"},
                {"time":"2017-05-15","title":"一起作业助力天津十四中探索信息化教学","url":"newscontent_49.vpage"},
                {"time":"2017-05-05","title":"全国高中数学特色课堂研修会 一起作业备受名师们青睐","url":"newscontent_47.vpage"},
                {"time":"2017-04-26","title":"互联网+教育时代下 一起作业促进学生核心素养落地","url":"newscontent_46.vpage"},
                {"time":"2017-04-20","title":"南开中学与一起作业联合开展十三五重点课题研究","url":"newscontent_45.vpage"},
                {"time":"2017-03-16","title":"印度在线教育领导品牌Fliplearn专访一起作业","url":"newscontent_42.vpage"},
                {"time":"2017-03-08","title":"一起作业CEO刘畅对话两会代表，共话教育大数据的未来","url":"newscontent_41.vpage"},
                {"time":"2017-03-07","title":"一起作业联合北师大开展混合式作业课题研究","url":"newscontent_44.vpage"},
                {"time":"2017-02-04","title":"央视走进一起作业，聚焦智能教育的中国探索","url":"newscontent_40.vpage"},
                {"time":"2017-01-18","title":"五年用户留存率高出行业十倍 一起作业是怎么做到的？","url":"newscontent_39.vpage"},
                {"time":"2017-01-15","title":"一起作业董事长王强捐赠未来科学大奖","url":"newscontent_38.vpage"},
                {"time":"2016-11-29","title":"一起作业出席国际教育高峰论坛 引领个性化教育新理念","url":"newscontent_43.vpage"},
                {"time":"2016-11-18","title":"一起作业获“家长学生喜爱在线教育产品”大奖","url":"newscontent_37.vpage"},
                {"time":"2016-11-17","title":"一起作业CEO刘畅登上乌镇世界互联网大会演讲台","url":"newscontent_36.vpage"},
                {"time":"2016-11-17","title":"一起作业宣布收购快乐学，强强联合探索大数据智能教学","url":"newscontent_35.vpage"},
                {"time":"2016-11-04","title":"一起作业家长通品牌升级，助力千万家长与孩子一起成长","url":"newscontent_34.vpage"},
                {"time":"2016-11-02","title":"一起作业调查显示：互联网让更多父亲融入家庭教育","url":"newscontent_48.vpage"},
                {"time":"2016-09-07","title":"一起作业升级品牌架构，建立K12综合教育平台","url":"newscontent_32.vpage"},
                {"time":"2016-09-07","title":"一起作业刘畅：只有创造了用户价值才能有健康的商业模式","url":"newscontent_33.vpage"},
                {"time":"2016-09-01","title":"牵手明天，连线成长：一起作业新形象亮相","url":"newscontent_27.vpage"},
                {"time":"2016-08-05","title":"一起作业GMC日奥赛代表团交流归来","url":"newscontent_31.vpage"},
                {"time":"2016-07-22","title":"一起作业如何与传统出版开启在线教育共赢模式？来听听各位大佬怎么说","url":"newscontent_30.vpage"},
                {"time":"2016-07-20","title":"刘畅：移动互联背景下的作业形态及未来发展","url":"newscontent_29.vpage"},
                {"time":"2016-07-02","title":"一起作业获评2016中国企业未来之星最具成长性新兴企业","url":"newscontent_28.vpage"},
                {"time":"2016-05-23","title":"6000名教学精英云集东莞，一起作业引领信息化教学浪潮","url":"newscontent_25.vpage"},
                {"time":"2016-04-26","title":"地方教育出版社携手一起作业 共谋在线教育大业","url":"newscontent_24.vpage"},
                {"time":"2016-04-20","title":"一起作业与赖斯、比尔·盖茨同台，讲述在线教育的那些事儿！","url":"newscontent_23.vpage"},
                {"time":"2016-04-08","title":"“向雾霾宣战—2000万小学生共绘蓝天”公益画展正式开幕","url":"newscontent_22.vpage"},
                {"time":"2015-11-27","title":"华东师大出版社副总编率管理团队莅临一起作业考察交流","url":"newscontent_21.vpage"},
                {"time":"2015-10-23","title":"一起作业火爆亮相外专委第19次学术年会","url":"newscontent_17.vpage"},
                {"time":"2015-10-23","title":"龚亚夫：互联网将解决英语教育的最大瓶颈","url":"newscontent_18.vpage"},
                {"time":"2015-10-23","title":"王蔷：一起作业走在了教育发展的前沿","url":"newscontent_19.vpage"},
                {"time":"2015-10-23","title":"刘畅：教育如何拥抱“互联网+”","url":"newscontent_20.vpage"},
                {"time":"2015-10-10","title":"刘畅：致四周年","url":"newscontent_16.vpage"},
                {"time":"2015-09-24","title":"一起作业网CEO出席真格论坛","url":"newscontent_15.vpage"},
                {"time":"2015-09-15","title":"一起作业网CEO出席新浪高端访谈","url":"newscontent_14.vpage"},
                {"time":"2015-08-28","title":'一起作业进入"创业在上海"创新创业大赛总决赛',"url":"newscontent_13.vpage"},
                {"time":"2015-06-01","title":"一起作业网CEO刘畅参加全国第七次少代会并登上新闻联播","url":"newscontent_1.vpage"},
                {"time":"2015-03-28","title":"一起作业网开启平台化战略","url":"newscontent_2.vpage"},
                {"time":"2015-03-26","title":"信息化时代教师一定要向导师靠拢","url":"newscontent_3.vpage"},
                {"time":"2015-01-27","title":"在线教育从免费到盈利有多远","url":"newscontent_4.vpage"},
                {"time":"2015-01-06","title":"小学生网上写作业，你怎么看？","url":"newscontent_5.vpage"},
                {"time":"2014-06-19","title":"一起作业CEO刘畅哈佛演讲：互联网让中国教育走向世界","url":"newscontent_12.vpage"},
                {"time":"2013-02-18","title":"一起作业网完成1亿美元D轮融资","url":"newscontent_6.vpage"},
                {"time":"2013-12-12","title":"“一起作业”的一起摸索","url":"newscontent_7.vpage"},
                {"time":"2013-07-25","title":"英语网络作业 助力中小学减负","url":"newscontent_8.vpage"},
                {"time":"2013-07-19","title":"全国小学英语教研员工作论坛在京隆重举行","url":"newscontent_9.vpage"},
                {"time":"2013-05-28","title":"北京海淀借助信息化减负","url":"newscontent_10.vpage"},
                {"time":"2012-05-17","title":"“一起作业”挑战传统英语教学","url":"newscontent_11.vpage"}
            ]

        //每页显示新闻条数
        var newsLen = 7;

        //新闻数据总条数
        var maxLen = data.length;

        //判断页数
        var minLen = Math.ceil(maxLen/newsLen);

        //渲染template模板
        $("#newsContent").html(template("news_ul_temp",{data:data,pageNo:minLen,innerNo:newsLen,maxNo:maxLen}));

        //页面切换
        $(document).on("click",".JS-navList",function(){

            var index=$(this).index();

            $(".JS-news-nav").find("span").eq(index).addClass("active").siblings().removeClass("active");

            $(".JS-lists").find("ul").eq(index).show().siblings().hide();
        });
    }

    if (locationPathName == "/help/aboutus.vpage") {
        setModeHeight(".JS-aboutus-banner li");

        setModeHeight(".JS-lastBanner", 750 ,148);

        var _defType = 1;

        $('.JS-flex-next').on("click", function () {
            if(_defType == 4){
                _defType = 1;
            }else{
                _defType += 1;

                if(_defType == 4){
                    $(this).hide();
                }
            }

            var _offsetTop = $('.JS-aboutus-banner li[data-type="'+_defType+'"]').offset().top;
            $("html, body").animate({ scrollTop: _offsetTop }, 200);
        });

        $(window).scroll(function(c){
            if($(window).scrollTop() <= $('.JS-aboutus-banner li[data-type="3"]').offset().top){
                $('.JS-flex-next').show();
            }else{
                $('.JS-flex-next').hide();
            }
        });
        $(document).on("click","#js-clickPlay",function(){
            $.prompt(template("T:vdname", {}), {
                position: {width: 700},
                title: "一起成长",
                buttons: {}
            });
        })
    }


    function setModeHeight(id, h ,n) {
        var _winHeight = $(window).height();
        var _defHeight = h || 700;
        var _defLessCount = n || 0;

        if(_winHeight <= _defHeight){
            $(id).height(_defHeight - _defLessCount);
        }else{
            $(id).height( _winHeight - _defLessCount);
        }
    }

    //产品概念
    if (locationPathName == "/help/concept.vpage") {
        $(".JS-conceptSwitch-main").flexslider({
            animation: "slide",
            slideshow: true,
            slideshowSpeed: 5000,
            //startAt: 1,
            directionNav: false,
            animationLoop: true,
            manualControls: ".JS-conceptSwitch-mode li",
            touch: true //是否支持触屏滑动
        });
        setModeHeight('.JS-setHeight', 800, 300);
        setModeHeight('.JS-setHeight-header');
        setModeHeight('.homeItem');
        $(window).resize(function () {
            setModeHeight('.JS-setHeight', 800, 300);
            setModeHeight('.JS-setHeight-header');
            setModeHeight('.homeItem');
        });
        $.stellar({
            horizontalScrolling: false,
            responsive: true
        });

        $(document).on("click","#js-clickPlay1",function(){
            $.prompt(template("T:conceptMode1", {}), {
                position: {width: 700},
                title: "一道题的科技之旅",
                buttons: {}
            });
        }).on("click","#js-clickPlay2",function(){
            $.prompt(template("T:conceptMode2", {}), {
                position: {width: 700},
                title: "数学平台：数据驱动教与学",
                buttons: {}
            });
        }).on("click","#js-clickPlay3",function(){
            $.prompt(template("T:conceptMode3", {}), {
                position: {width: 700},
                title: "英语平台：换种思维看世界",
                buttons: {}
            });
        })
    }

    //Get Query
    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }
});