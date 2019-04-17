/**
 * Created by pengmin.chen on 2017/12/13.
 */
define(['jquery', 'knockout', 'YQ', 'voxLogs'], function ($, ko) {
    var defaultInitMode;
    var databaseLogs = "web_teacher_logs";
    // 当多个页面引入该一个脚本时，可针对不同页面设置的全局变量来决定实例化不同的ko viewmodel
    if (typeof(initMode) == "string") {
        switch (initMode) {
            case 'teacherAwardMode':
                defaultInitMode = new teacherAwardMode();// 布置作业抽奖活动
                break;
            default:
            //intiMode null
        }
    }

    function teacherAwardMode () {
        var _this = this;

        // 大转盘变量（currentIndex表示li索引：，winningId表示后台返回获奖id）
        var currentIndex = 0, winningId = 8, speed = 200, lotteryAnimate;
        _this.isAssign = ko.observable(false); // 是否布置作业
        _this.assignBtnText = ko.observable('去布置');
        _this.showPopup = ko.observable(false);
        _this.popupType = ko.observable(0);
        _this.popupTitle = ko.observable('');
        _this.popupContent = ko.observable('');
        _this.popupBtnText = ko.observable('');
        _this.numTime = ko.observable(0); // 剩余抽奖次数
        _this.bigAwardList = ko.observableArray([]); // 大奖用户列表
        _this.historyList = ko.observableArray([]); // 抽奖记录列表
        _this.switchFlag = ko.observable(true); // 是否可以点击抽奖（flag）

        _this.setAwardInfo = function (type, title, content, btnText) {
            _this.popupType(type);
            _this.popupTitle(title);
            _this.popupContent(content);
            _this.popupBtnText(btnText);
        };

        _this.clickAlertBtn = function () {
            if (_this.popupType() === 2) {
                _this.toAssign();
            } else {
                _this.showPopup(false);
            }
        };

        _this.clickAlertClose = function () {
            _this.showPopup(false);
        };

        _this.toAssign = function () {
            // 点击布置作业按钮打点
            YQ.voxLogs({
                database: databaseLogs,
                module: "m_lIHTCSqE",
                op: "o_Ajmyn2sz",
                s0: '中学'
            });
            if (_this.isAssign()) {
                return false;
            } else {
                // 跳转到布置作业地址
                window.open(assignLink);
            }
        };

        getHomeworkState();
        getChance();
        getDetailInfo();
        //获取布置作业初始状态
        function getHomeworkState () {
            $.ajax({
                url: '/teacher/activity/term2018/hadArranged.vpage',
                type: 'GET',
                success: function (data) {
                    if (data.success) { // 已布置
                        _this.isAssign(true);
                        _this.assignBtnText('已布置');
                    } else { // 未布置
                        _this.isAssign(false);
                        _this.assignBtnText('去布置');
                    }
                }
            })
        }
        //获取剩余次数
        function getChance () {
            $.ajax({
                url: '/teacher/activity/term2018/getchance.vpage',
                type: 'GET',
                success: function (data) {
                    if (data.success) {
                        _this.numTime(data.chanceCount); // 剩余次数
                    } else {
                        _this.showPopup(true);
                        _this.setAwardInfo(0, '温馨提示', data.info || '出错了，稍后重试！', '知道了');
                    }
                }
            });
        }
        //获取当前用户信息
        function getDetailInfo () {
            $.ajax({
                url: '/teacher/activity/term2018/lotteryhistories.vpage',
                type: 'GET',
                dataType: 'json',
                success: function (data) {
                    if (data.success) {
                        _this.bigAwardList(data.bigLotteryHistories);
                        var lotteryHistories = data.lotteryHistories;
                        for (var i = 0, len = lotteryHistories.length; i < len; i++) {
                            lotteryHistories[i]._no = '第' + (lotteryHistories.length - i) + '次';
                        }
                        _this.historyList(lotteryHistories);
                        // 进入页面打点
                        YQ.voxLogs({
                            database: databaseLogs,
                            module: "m_lIHTCSqE",
                            op: "o_8RP3y4aB",
                            s0: '中学'
                        });
                    } else {
                        _this.showPopup(true);
                        _this.setAwardInfo(0, '温馨提示', data.info || '出错了，稍后重试！', '知道了');
                    }
                }
            });
        }

        /*抽奖功能*/
        $("#lotterySubmit").on('click', function () {
            // 点击抽奖按钮打点
            YQ.voxLogs({
                database: databaseLogs,
                module: "m_lIHTCSqE",
                op: "o_PNmXtfIh",
                s0: '中学'
            });
            var $this = $(this);

            // 次数用完(无论多少都走请求)
            if (_this.numTime() <= 0){
                if (!_this.isAssign()) { // 未布置
                    _this.showPopup(true);
                    _this.setAwardInfo(2, '当前剩余抽奖次数0次！', '布置作业，每天可以获得抽奖机会', '去布置');
                } else { // 已布置
                    _this.showPopup(true);
                    _this.setAwardInfo(3, '温馨提示', '很遗憾，您的剩余抽奖次数为0次', '知道了');
                }
            	return false;
            }

            // 抽奖完毕后才能 点击抽奖按钮 和 减少抽奖次数
            if(_this.switchFlag()){
                if(_this.numTime() >= 0){
                    lotteryPost($this);
                }
                _this.switchFlag(false);
            }
        });

        function lotteryPost($this){
            $.ajax({
                url: "/teacher/activity/term2018/dolottery.vpage",
                type: "POST",
                dataType: "json",
                success: function (data) {
                    if (data.success) {
                        if (data.result.awardId) {
                            winningId = data.result.awardId;
                        }

                        lotteryAnimate = setInterval(function () {
                            startRotational(function () {
                                // setAwardInfo()第一个参数对应：
                                // 0:错误info；1:未认证；2:没有抽奖次数-未布置；3:没有抽奖次数-已布置；
                                // 4:获得iPhone 7；5:获得kindle；6:获得JBL音响；7:获得蓝牙耳机；8:获得100学豆；9:获得10学豆；10:再接在励；
                                // data.result.awardId对应 1:iPhone；2:kindle；3:JBL音响；4:蓝牙耳机；5:100学豆；6：10学豆；7:再接在励；
                                switch (winningId) {
                                    case 1:
                                        _this.setAwardInfo(4, '恭喜你获得iPhone7一台！', '实物奖品4月9日之后统一寄送，请注意查收电话哦~', '知道了');
                                        break;
                                    case 2:
                                        _this.setAwardInfo(5, '恭喜你获得Kindle一台！', '实物奖品4月9日之后统一寄送，请注意查收电话哦~', '知道了');
                                        break;
                                    case 3:
                                        _this.setAwardInfo(6, '恭喜你获得JBL音响一台！', '实物奖品4月9日之后统一寄送，请注意查收电话哦~', '知道了');
                                        break;
                                    case 4:
                                        _this.setAwardInfo(7, '恭喜你获得蓝牙耳机一个！', '实物奖品4月9日之后统一寄送，请注意查收电话哦~', '知道了');
                                        break;
                                    case 5:
                                        _this.setAwardInfo(8, '中奖啦！', '恭喜你获得100学豆', '知道了');
                                        break;
                                    case 6:
                                        _this.setAwardInfo(9, '中奖啦！', '恭喜你获得10学豆', '知道了');
                                        break;
                                    case 7:
                                        _this.setAwardInfo(10, '再接再厉！', '很遗憾，奖品溜走了~', '知道了');
                                        break;
                                }
                                // 延迟500ms出现弹窗
                                setTimeout(function () {
                                    _this.showPopup(true);
                                    _this.switchFlag(true);
                                    getDetailInfo();
                                }, 500);
                            },0);
                        }, speed);
                        _this.numTime(_this.numTime()-1);
                    }else{
                        _this.showPopup(true);
                        _this.switchFlag(true);
                        _this.setAwardInfo(0, '温馨提示', data.info || '出错了，稍后重试！', '知道了');
                    }
                }
            });
        };

        function startLottery(index, maxNum){
            if(index >= maxNum){
                index = 0;
            }else{
                index++;
            }
            return index;
        };

        //转动
        function startRotational(callback,rotaNumber){
            var $lotteryBox = $("#lottery li").eq(currentIndex);
            currentIndex = startLottery(currentIndex, 8);
            $lotteryBox.addClass("active").siblings().removeClass("active");

            //最后转动
            if(rotaNumber >= 5){
                clearInterval(lotteryAnimate);

                if(currentIndex == 0){
                    rotaNumber += 1;
                }

                if(rotaNumber >= 6 && (winningId > 8 || winningId <= 0)){
                    winningId = 5;
                }

                if(rotaNumber >= 6 && $lotteryBox.data("type") == winningId){
                    clearInterval(lotteryAnimate);
                    rotaNumber = 0;
                    if(callback){
                        callback();
                    }
                    return false;
                }

                lotteryAnimate = setInterval(function(){
                    startRotational(callback,rotaNumber);
                }, 200);
            }else{
                if(currentIndex == 0){
                    rotaNumber += 1;
                }

                if(speed > 50){
                    speed -= 50;
                }

                //第二次转动
                if(speed <= 50){
                    clearInterval(lotteryAnimate);
                    lotteryAnimate = setInterval(function(){
                        startRotational(callback,rotaNumber);
                    }, speed);
                }
            }
        }
    }

    if (defaultInitMode) {
        // defaultInitMode.nullContent = ko.observable();
        ko.applyBindings(defaultInitMode);
    }
});