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
        _this.authState = ko.observable(false);
        _this.pageTipTitle = ko.observable('');
        _this.pageTipContent = ko.observable('');
        _this.isAssign = ko.observable(false);
        _this.assignBtnText = ko.observable('一键布置');
        _this.assignFlag = ko.observable(false);
        _this.subject = ko.observable('');
        _this.showPopup = ko.observable(false);
        _this.popupType = ko.observable(0); // 0:错误info；1:未认证；2:没有抽奖次数-未布置；3:没有抽奖次数-已布置；4:获得教育基金；5:获得kindle；6:获得园丁豆；7:再接在励；
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
                module: "m_HBpBfjb6",
                op: "o_pEdytoNK",
                s0: _this.subject()
            });
            if (_this.isAssign()) {
                return false;
            } else {
                if (_this.assignFlag()) return false;
                _this.assignFlag(true);
                // 请求，一键布置作业
                $.ajax({
                    url: '/teacher/activity/summer/2018/onekey_assign.vpage',
                    type: 'GET',
                    dataType: 'json',
                    success: function (data) {
                        _this.assignFlag(false);
                        if (data.success) {
                            // 先toast提示,2s后跳转到作业布置首页
                            _this.showPopup(true);
                            _this.setAwardInfo(0, '温馨提示', '假期作业布置成功', '知道了');
                            setTimeout(function () {
                                getDetailInfo();
                            }, 500);
                        } else {
                            _this.showPopup(true);
                            _this.setAwardInfo(0, '温馨提示', data.info || '出错了，稍后重试！', '知道了');
                        }
                    }
                });
            }
        };

        // 根据当前时间显示对应的提示语
        function getTime (date) {
            if (date) {
                return new Date(date).getTime();
            } else {
                return new Date().getTime();
            }
        }
        if (getTime() < getTime('06/24/2018 23:59:59')) {
            _this.pageTipTitle('早布置福利');
            _this.pageTipContent('6月24日前布置的老师中必出<span class="txtRed">1个3000元教育基金</span>，' +
                '<span class="txtRed">3个1000元教育基金</span>，<span class="txtRed">5个500元教育基金</span>，' +
                '<span class="txtRed">15个100元教育基金</span>，等你领取哦~');
        } else if (getTime() > getTime('06/25/2018 00:00:00') && getTime() < getTime('07/08/2018 23:59:59')) {
            _this.pageTipTitle('每周大奖');
            _this.pageTipContent('现在布置，7月8日前每周必出<span class="txtRed">4个3000元教育基金</span>，' +
                '<span class="txtRed">6个1000元教育基金</span>，<span class="txtRed">10个500元教育基金</span>，' +
                '<span class="txtRed">40个100元教育基金</span>，等你领取~');
        } else if (getTime() > getTime('07/09/2018 00:00:00') && getTime() < getTime('07/22/2018 23:59:59')) {
            _this.pageTipTitle('最后一周');
            _this.pageTipContent('活动截止前布置，仍有机会领取<span class="txtRed">1个3000元教育基金</span>，' +
                '<span class="txtRed">1个1000元教育基金</span>，<span class="txtRed">5个500元教育基金</span>，' +
                '<span class="txtRed">15个100元教育基金</span>~');
        } else if (getTime() > getTime('07/22/2018 00:00:00')) {
            _this.pageTipTitle('活动已结束');
            _this.pageTipContent('当前活动已结束<br>请关注下次抽奖活动哦~');
        }

        getDetailInfo();
        getHistoryInfo();
        //获取当前用户信息
        function getDetailInfo () {
            $.ajax({
                url: '/teacher/activity/summer/2018/detail.vpage',
                type: 'GET',
                dataType: 'json',
                success: function (data) {
                    if (data.success) {
                        _this.numTime(data.drawTime); // 剩余次数
                        _this.bigAwardList(data.bigAward);
                        _this.subject(data.subject);
                        _this.isAssign(data.assigned);
                        if (_this.isAssign()) { // 已布置
                            _this.assignBtnText('已布置');
                        } else {
                            _this.assignBtnText('一键布置');
                        }
                        _this.authState(data.authState);
                        // 进入页面打点
                        YQ.voxLogs({
                            database: databaseLogs,
                            module: "m_HBpBfjb6",
                            op: "o_xBKG9X6w",
                            s0: _this.subject(),
                            s1: YQ.getQuery('track')
                        });
                    } else {
                        // if (data.errorCode === '101') { // 未登录
                        //     window.open('/teacher/index.vpage');
                        // }
                        _this.showPopup(true);
                        _this.setAwardInfo(0, '温馨提示', data.info || '出错了，稍后重试！', '知道了');
                    }
                }
            });
        }

        // 获取历史记录信息
        function getHistoryInfo () {
            $.ajax({
                url: '/teacher/activity/summer/2018/history.vpage',
                type: 'GET',
                dataType: 'json',
                success: function (data) {
                    if (data.success) {
                        _this.historyList(data.history);
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
                module: "m_HBpBfjb6",
                op: "o_bmPBcJul",
                s0: _this.subject(),
                s1: _this.numTime()
            });
            var $this = $(this);

            // 未认证
            if (!_this.authState()) {
                _this.showPopup(true);
                _this.setAwardInfo(1, '认证老师才能参与活动哦，快去达成认证吧~', '认证条件：<br>1.设置姓名并绑定手机<br>2.8名学生完成3次作业<br>3.3名学生绑定手机', '知道了');
                return false;
            }
            // 次数用完(无论多少都走请求)
            if (_this.numTime() <= 0){
                if (!_this.isAssign()) { // 未布置
                    _this.showPopup(true);
                    _this.setAwardInfo(2, '当前剩余抽奖次数0次！', '布置假期作业，每天得3次抽奖机会~<br>假期作业默认开始时间7月1日，<br>不用担心学生提前开始。', '一键布置');
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
                url: "/teacher/activity/summer/2018/draw.vpage",
                type: "POST",
                dataType: "json",
                data:{
                    // campaignId: _this.campaignId(),
                    // clientType: "APP"
                },
                success: function (data) {
                    if (data.success) {
                        if (data.awardId) {
                            winningId = data.awardId;
                        }

                        lotteryAnimate = setInterval(function () {
                            startRotational(function () {
                                // setAwardInfo()第一个参数对应 0:错误info；1:未认证；2:没有抽奖次数-未布置；3:没有抽奖次数-已布置；4:获得教育基金；5:获得kindle；6:获得园丁豆；7:再接在励；
                                // data.awardId对应 1:3000元教育基金；2:1000元教育基金；3:kindle；4:100元教育基金；5:1个园丁豆；6：谢谢参与
                                switch (winningId) {
                                    case 1:
                                        _this.setAwardInfo(4, '恭喜你获得3000元教育基金！', '教育基金将以京东卡的形式发放，在9月10日后统一寄送，请注意查收电话通知哦~', '知道了');
                                        break;
                                    case 2:
                                        _this.setAwardInfo(4, '恭喜你获得1000元教育基金！', '教育基金将以京东卡的形式发放，在9月10日后统一寄送，请注意查收电话通知哦~', '知道了');
                                        break;
                                    case 3:
                                        _this.setAwardInfo(5, '恭喜你获得500元教育基金！', '教育基金将以京东卡的形式发放，在9月10日后统一寄送，请注意查收电话通知哦~', '知道了');
                                        break;
                                    case 4:
                                        _this.setAwardInfo(4, '恭喜你获得100元教育基金！', '教育基金将以京东卡的形式发放，在9月10日后统一寄送，请注意查收电话通知哦~', '知道了');
                                        break;
                                    case 5:
                                        _this.setAwardInfo(6, '中奖啦！', '恭喜你获得1园丁豆', '知道了');
                                        break;
                                    case 6:
                                        _this.setAwardInfo(7, '再接再厉！', '很遗憾，奖品溜走了~', '知道了');
                                        break;
                                }
                                // 延迟500ms出现弹窗
                                setTimeout(function () {
                                    _this.showPopup(true);
                                    _this.switchFlag(true);
                                }, 500);
                            },0);
                        }, speed);

                        // if(_this.numTime() >= 1){
                        //     _this.numTime(num-=1); // 剩余次数-1
                        // }else{
                        //     _this.numTime(0);
                        // }
                        _this.numTime(data.leftChance);
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