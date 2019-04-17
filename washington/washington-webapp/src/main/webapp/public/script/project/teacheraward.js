/**
 * Created by pengmin.chen on 2017/8/21.
 */
define(['jquery', 'knockout', 'voxLogs'], function ($, ko) {
	var defaultInitMode;
	// 打点设置，默认为学生端
	var UA = window.navigator.userAgent, databaseLogs;
	var isIOSFlash = (UA.indexOf("iPhone") > - 1  || UA.indexOf("iPad") > - 1);
	var isAndroidFlash = UA.indexOf("Android") > - 1;
	if (isIOSFlash){
		databaseLogs = "app_17teacher_ios.normal";
	}else if(isAndroidFlash){
		databaseLogs = "app_17teacher_android.normal";
	}else{
		databaseLogs = "web_teacher_logs";
	}

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

	// 进入页面打点
	YQ.voxLogs({
		database: databaseLogs,
		module: "m_lIHTCSqE",
		op: "o_8RP3y4aB"
	});

	// 定义knockout viewmodel teacherAwardMode
	function teacherAwardMode () {
		var _this = this;

		// 大转盘变量（currentIndex表示li索引：，winningId表示后台返回获奖id）
		var currentIndex = 4, winningId = 8, speed = 200, lotteryAnimate;

		_this.numTime = ko.observable(0); // 剩余抽奖次数
		_this.useTime = ko.observable(0); // 已经使用的次数
		_this.bigList = ko.observableArray([]); // 大奖用户列表
		_this.resultList = ko.observableArray([]); // 抽奖用户列表
		_this.switchFlag = ko.observable(true); // 是否可以点击抽奖（flag）
		_this.showPop = ko.observable(false); // 是否显示弹窗
		_this.showPopProductAward = ko.observable(false); // 弹窗显示奖品
		_this.showPopDousAward = ko.observable(false); // 弹窗显示奖励学豆
		_this.showPopNoAward = ko.observable(false); // 无奖弹窗
		_this.showPopNoCheck = ko.observable(false); // 弹窗显示没有认证(或其他报错弹窗)
        _this.awardName = ko.observable(''); // 奖品名称
		_this.noCheckPara = ko.observable(''); // 未认证弹窗文案
		// 关闭实物奖励弹窗
		_this.closePopProductAward = function () {
			_this.showPop(false);
			_this.showPopProductAward(false);
		};
		// 关闭学豆奖励弹窗
		_this.closePopDousAward = function () {
			_this.showPop(false);
			_this.showPopDousAward(false);
		};
		// 关闭无奖弹窗
		_this.closePopNoAward = function () {
			_this.showPop(false);
			_this.showPopNoAward(false);
		};
		// 关闭未认证弹窗
		_this.closePopNoCheck = function () {
            _this.showPop(false);
            _this.showPopNoCheck(false);
        };
		//获取当前用户信息
		$.ajax({
			url: '/activity/v2/termbegin.vpage',
			type: 'GET',
			dataType: 'json',
			success: function success(data) {
				if (data.success) {
					console.log('result', data);
					_this.numTime(data.freeChance); // 剩余次数
					_this.useTime(data.myHistory.length); // 使用次数
					var bigResultList = data.campaignLotteryResultsBig;
					var ownHistoryList = data.myHistory;
					for(var i = 0, brLength = bigResultList.length; i < brLength; i++) {
						bigResultList[i]._date = resolveUTCDate(bigResultList[i].datetime);
						bigResultList[i]._time = resolveUTCTime(bigResultList[i].datetime);
                        bigResultList[i]._teacherName = bigResultList[i].userName.substr(0, 1) + '老师';
					}
					for(var j = 0, ohLength = ownHistoryList.length; j < ohLength; j++){
						ownHistoryList[j]._countIndex = ownHistoryList.length - j;
						ownHistoryList[j]._date = resolveUTCDate(ownHistoryList[j].lotteryDate);
						ownHistoryList[j]._time = resolveUTCTime(ownHistoryList[j].lotteryDate);
						ownHistoryList[j]._awardNamePara = ownHistoryList[j].awardName === '谢谢参与' ? '未获得奖励' : ('获得了' + ownHistoryList[j].awardName);
					}
					_this.bigList(bigResultList);
					_this.resultList(ownHistoryList);
				} else {
					if (data.errorCode === '101') { // 未登录
						window.open('/teacher/index.vpage');
					}
				}
			}
		});

		// UTC时间戳转换成年月日
		function resolveUTCDate (utcData) {
			var myDate = new Date(utcData);
			var month = ('0' + (myDate.getMonth() + 1)).slice(-2);
			var day = ('0' + myDate.getDate()).slice(-2);
			return month + '-' + day;
		}
		// UTC时间戳转换成时分秒
		function resolveUTCTime(utcTime) {
			var myDate = new Date(utcTime);
			var hours = ('0' + myDate.getHours()).slice(-2);
			var minutes = ('0' + myDate.getMinutes()).slice(-2);
			return hours + ':' + minutes;
		}

		/*布置作业*/
		$('#assignBtn').on('click', function () {
			// 点击布置作业按钮打点
			YQ.voxLogs({
				database: databaseLogs,
				module: "m_lIHTCSqE",
				op: "o_Ajmyn2sz"
			});
		});
		/*抽奖功能*/
		$("#lotterySubmit").on('click', function () {
			// 点击抽奖按钮打点
			YQ.voxLogs({
				database: databaseLogs,
				module: "m_lIHTCSqE",
				op: "o_PNmXtfIh"
			});
			var $this = $(this);
			// 次数用完(无论多少都走请求)
			// if (_this.numTime() <= 0){
			// 	_this.showPop(true);
			// 	_this.showPopNoCheck(true);
			// 	_this.noCheckPara('很遗憾，您当前剩余抽奖次数为0次<br>快去布置作业获得抽奖机会吧~');
			// 	return false;
			// }
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
				url: "/activity/dolottery.vpage",
				type: "POST",
				dataType: "json",
				data:{
					// campaignId: _this.campaignId(),
					// clientType: "APP"
				},
				success: function (data) {
					console.log('result', data);
					if (data.success) {
						var num = _this.numTime();
						var lotteryResult = data.lottery;

						if (data.success && data.lottery.awardId) {
							winningId = data.lottery.awardId;
						}

						lotteryAnimate = setInterval(function () {
							startRotational(function () {
								//奖品名称
								_this.awardName(lotteryResult.awardName);
								_this.switchFlag(true);
								if (winningId >= 1 &&  winningId <= 4) { // iphone(1)、iPad(2)、kindle(3)、凌美定制钢笔(4)
									_this.showPopProductAward(true); // 显示实物弹窗
								} else if (winningId >= 5 && winningId <= 7) { // 100豆(5)、10豆(6)、1豆(7)
									_this.showPopDousAward(true); // 显示豆弹窗
								} else if (winningId === 8) { // 无奖励
									_this.showPopNoAward(true); // 谢谢参与弹窗
								}
								// 延迟500ms出现弹窗
								setTimeout(function () {
									_this.showPop(true);
								}, 500);
							},0);
						}, speed);

						if(_this.numTime() >= 1){
							_this.numTime(num-=1); // 剩余次数-1
							_this.useTime(_this.useTime()+1); // 使用次数+1
						}else{
							_this.numTime(0);
						}
					}else{
                        _this.switchFlag(true);
						_this.showPop(true);
						_this.showPopNoCheck(true);
						if (data.errorCode === '105') {
                            _this.noCheckPara('认证老师才能参与活动哦<br>快去达成认证吧~');
						} else if (data.errorCode === '104') {
                            _this.noCheckPara('本次活动只支持小学老师参与<br>请您关注其他活动哦~');
						} else if (data.errorCode === '106') {
                            _this.noCheckPara('很遗憾，您当前剩余抽奖次数为0次<br>快去布置作业获得抽奖机会吧~');
                        } else {
							_this.noCheckPara(data.info);
                        }
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