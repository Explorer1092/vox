/**
 * Created by fengwei on 2017/3/29.
 */
define(['jquery', "$17", 'impromptu', 'voxLogs','template'], function ($) {
    var canChangeLotteryPool = true; //完成抽奖,防止抽奖中途切换奖池
    //错误弹窗
    var lotteryAlertInfo = function (text) {
        var alertText = text?text:'好像出问题咯';
        $('.keyRecords-pop').hide();
        $('#lotteryErrorAlert').show();
        $('.js-errorInfo').html(alertText);
    };

    function Lottery(id,type) {
        this.index = -1; //当前位置索引
        this.count = 0; //总共位置
        this.timer = null; //
        this.speed = 60; //初始转动速度
        this.times = 0; //转动次数
        this.cycle = 60; //转动基本次数：即至少需要转动多少次再进入抽奖环节
        this.prize = 0; //中奖位
        this.prizeName = ''; //中奖名称
        this.id = id; //盒子id
        this.obj = $("#" + this.id);
        this.units = this.obj.find("div.icon");
        this.count = this.units.length;
        this.isClick = false;
        this.type = type;
    }
    Lottery.prototype = {
        //当前状态
        setState: function() {
            if(this.count > 0) {
                this.obj.find("div.icon-0" + this.index).addClass("active");
            }
        },
        //索引改变
        changeIndex: function() {
            // this.units.removeClass('active');
            $('#lotteryItems>div.icon').removeClass('active');
            this.index++;
            if(this.index > this.count) {
                this.index = 0;
            }
            this.setState();
        },
        //运动
        rotate: function() {
            var that = this;
            clearTimeout(that.timer);
            if(typeof(that.rotate) == 'function') {
                that.timer = setTimeout(function() {
                    that.rotate();
                }, that.speed);
            }
            that.changeIndex();
            that.times++;
            that.judge();
        },
        //判断加减速和是否停止
        judge: function() {

            if(this.times + 10 < this.cycle){
                this.speed -= 20;
            }else{
                var activeNodeIndex = $('div.icon.active').data('type');
                if(this.times > this.cycle+1 && activeNodeIndex == this.prize) {
                    var that = this;
                    clearTimeout(that.timer);
                    that.timer = null;
                    this.times = 0;
                    this.speed = 60;
                    this.index = -1;

                    clearTimeout(that.click);
                    that.click = setTimeout(function() {
                        that.alertPrize();
                    }, 200);
                }

                //快接近 减速
                if(this.times > this.cycle + 1 && ((this.prize == 1 && this.index == 8) || this.prize == this.index + 1)) {
                    this.speed += 200;
                }else {
                    this.speed += 20;
                }
            }
            if(this.speed < 40){
                this.speed = 40;
            }
        },
        //成功提示信息
        alertPrize: function() {
            var that = this;
            that.isClick = false;
            if(that.prize == 8 && that.getCampaignId() != 51){
                lotteryAlertInfo(that.prizeName);
            }else{
                $('.keyRecords-pop').hide();
                $('#lotterySuccessAlert').show();
                $('.js-awardName').html(that.prizeName);
            }

            canChangeLotteryPool = true;
        },
        getCampaignId:function () {
            var activeNode = $('.js-lotteryPool>li.show')[0],
                index = $(activeNode).data('index');
            return parseInt(index) + 50 ;
        },
        //中奖礼品
        getPrize: function(url, json) {
            var that = this;
            $.ajax({
                url: url,
                data: json,
                dataType: 'json',
                type: 'post',
                success: function(res) {
                    if(res.success){

                        that.prize = res.lottery.awardId;
                        that.prizeName = res.lottery.awardName;

                        var times = parseInt($('.js-freeChanceNo').html());
                        if(times >0){
                            times -= 1;
                        }
                        $('.js-freeChanceNo').html(times);
                        if(times == 0){
                            $(".js-submitLottery").addClass('gray');
                        }else{
                            $(".js-submitLottery").removeClass('gray');
                        }

                        that.rotate();//成功后转动

                    }else{
                        lotteryAlertInfo(res.info);
                        that.isClick = false;
                        canChangeLotteryPool = true;
                    }
                },
                error: function(e) {
                    lotteryAlertInfo();
                    that.isClick = false;
                    canChangeLotteryPool = true;
                }
            })
        },
        click: function(that) {
            var btn = $('.js-submitLottery');
            btn.on('click', function() {
                if(!$(this).hasClass('gray')){
                    if(that.isClick) return;
                    that.isClick = true;
                    if(canChangeLotteryPool){
                        canChangeLotteryPool = false;
                        that.getPrize('doscholarshiplottery.vpage',{
                            clientType:'PC',
                            campaignId:that.getCampaignId()
                        });
                    }
                }
            })
        },
        init: function() {
            this.setState();
            this.click(this);
        }
    };

    $(function() {
        //获取抽奖相关参数
        var getLotteryData = function () {
            $.ajax({
                url:'loadscholarshipdata.vpage',
                type:'GET',
                success:function (res) {
                    if(res.success){
                        initPageTemp(res);
                        getLotteryResult(res);
                    }else{

                    }
                },
                error:function () {

                }
            })
        };

        getLotteryData();

        var getLotteryResult = function (res) {

            if(res.campaignLotteryResultsBig.length == 0){
                $('.js-bigPrizeList').remove();
            }else{
                $('.js-bigPrizeList').html(template('bigPrizeTemp',{list:res.campaignLotteryResultsBig}));
            }
            $('.js-resultList').html(template('bigPrizeTemp',{list:res.campaignLotteryResults}));
        };

        var lottery;

        //初始化页面相关数据
        var initPageTemp = function (res) {
            if(res.freeChance != undefined){
                $('.js-freeChanceNo').html(res.freeChance);
                if(res.freeChance != 0){
                    $(".js-submitLottery").removeClass('gray');
                }
            }
            //TODO 查看记录，钥匙开启状态
            if(res.teacherKeyInfo){
                // show key
                var showKey = 0,
                    showDialogFlag = false;
                if(res.teacherKeyInfo.totalKeyNum){
                    showKey = parseInt(res.teacherKeyInfo.totalKeyNum);
                }
                if(res.teacherKeyInfo.showAuth){
                    showDialogFlag = true;
                }
                var typeObj = getLotteryType(showKey);


                $('.js-myKeyNo').html(showKey);
                setLotteryPool(typeObj); //初始化奖池内容
                setLotteryPoolTab(typeObj); //初始化奖池开关

                //获得奖品池
                lottery = new Lottery('lotteryItems',typeObj.type);
                lottery.init();
                if(showDialogFlag){
                    $('#authDialog').show();
                }
            }else{
                setLotteryPool({type:3});
                lottery = new Lottery('lotteryItems',3);
                lottery.init();

                $('.js-lotteryPool>li').addClass('dis');
                $('.js-submitLottery').addClass('gray');
            }
        };

        //获取对应奖池类型 并初始化奖池切换状态
        var getLotteryType = function (num) {
            var typeNum = 0;
            if(num >= 0 && num <5){
                $(".js-submitLottery").addClass('gray'); //无奖池可开和无次数一样
            }else if(num >=5 && num < 10){
                typeNum = 3;
            }else if(num >=10 && num < 15){
                typeNum = 2;
            }else if(num >= 15){
                typeNum = 1;
            }

            //TODO 钥匙数对应奖池类型
            return {
                type:typeNum
            }
        };

        var setLotteryPool = function (tempData) {
            if(tempData){
                $('#lotteryItems').html(template('lotteryItemTemp',tempData));
            }
        };

        //设置奖池开关
        var setLotteryPoolTab = function (tempData) {
            if(tempData && tempData.type){
                var type = tempData.type;
                switch (type){
                    case 0 :
                        $('.js-lotteryPool>li').removeClass('active');
                        break;
                    case 1 :
                        $('.js-lotteryPool>li').addClass('active').find('div.tag').removeClass('tagGray');
                        $('.js-lotteryPool>li[data-index="1"]').click();
                        break;
                    case 2 :
                        $('.js-lotteryPool>li[data-index="2"]').addClass('active').find('div.tag').removeClass('tagGray');
                        $('.js-lotteryPool>li[data-index="3"]').addClass('active').find('div.tag').removeClass('tagGray');
                        $('.js-lotteryPool>li[data-index="2"]').click();
                        break;
                    case 3 :
                        $('.js-lotteryPool>li[data-index="3"]').addClass('active').find('div.tag').removeClass('tagGray');
                        $('.js-lotteryPool>li[data-index="3"]').click();
                        break;
                }
            }
        };

        //获取记录数据
        var getRecordData = function () {
            $.ajax({
                url:'/teacher/new/homework/goal/scholarship/keyrecord.vpage',
                type:'GET',
                success:function (res) {
                    if(res.success){
                        $('.js-recordContent').html('');
                        if(res.data){
                            renderRecord(res.data);
                        }else{
                            $('.js-recordContent').html('<div style="text-align: center;">暂无记录</div>');
                        }
                    }else{
                        lotteryAlertInfo(res.info);
                        $('.js-recordContent').html('<div style="text-align: center;">暂无记录</div>');
                    }
                },
                error:function () {
                    lotteryAlertInfo();
                }
            })
        };

        getRecordData();

        //渲染dialog模板
        var renderRecord = function (data) {
            $('.js-recordContent').html(template('recordDialogTemp',{data:data}));
        };

        $(document).on('click','ul.js-lotteryPool>li',function () {
            var $this = $(this),
                type = $this.data('index'),
                hasActive = $this.hasClass('active');
            if(canChangeLotteryPool && hasActive){
                $this.addClass('show').siblings('li').removeClass('show');
                setLotteryPool({
                    type:type
                });

                lottery = new Lottery('lotteryItems',type);
                lottery.init();
            }
            var op = '',
                s0Str = 'no';
            if(hasActive){
                s0Str = 'yes';
            }
            switch (type){
                case 1:
                    op = 'gold_pool_pc';
                    break;
                case 2:
                    op = 'silver_prize_pool_pc';
                    break;
                case 3:
                    op = 'copper_pool_pc';
                    break;
            }
            YQ.voxLogs({
                database: "web_teacher_logs",
                module: 'm_ncvLVKQI',
                op: op,
                s0:s0Str
            });
        }).on('click','.js-showRecord',function () {
            $('#recordDialog').show();
            YQ.voxLogs({
                database: "web_teacher_logs",
                module: 'm_ncvLVKQI',
                op: 'key_record_pc'
            });
        }).on('click','.js-closeDialog',function () {
            $('#recordDialog').hide();
        }).on('click','.js-closeSuccessAlert',function () {
            $('#lotterySuccessAlert').hide();
        }).on('click','.js-closeErrorAlert',function () {
            $('#lotteryErrorAlert').hide();
        }).on('click','.close',function () {
            $(this).parents('.keyRecords-pop').hide();
        });

        YQ.voxLogs({
            database: "web_teacher_logs",
            module: 'm_ncvLVKQI',
            op: 'scholarship_activity_page_pc'
        });
    });


});