/**
 * @author pengmin.chen
 * @description "2018春季期末复习赢双重奖学金活动"
 * @createDate 2018/05/23
 */
window.define(['jquery', 'knockout', 'YQ', 'voxLogs'], function($, ko, YQ){
    var defaultInitMode = '';
    var requestAddress = '/teacher/activity/scholarship/';
    var trackModule = 'm_ayivz7zp';
    var trackDataBase = 'web_teacher_logs';

    /*
     * indexModeSplit: 拆分indexModeSplit（eslint会检测函数语句过长）
     * @_this: IndexMode中ko对象
     */
    var indexModeSplit = function(_this){
        _this.requestDetailInfo = function () {
            // 获取主屏信息
            $.ajax({
                url: requestAddress + 'detail.vpage',
                type: 'GET',
                success: function (res) {
                    if (res.success){
                        _this.subject(res.subject);
                        _this.lastDayWinner(res.dailyWinner);
                        _this.finalAttendNum(res.finalAttendNum);
                        _this.dailyList(res.dailyList || []);
                        _this.detailInfo(res.detail || '');
                    } else {
                        _this.showErrorTip(res.info || '出错了，稍后重试');
                    }
                },
                error: function () {
                    _this.showErrorTip('出错了，稍后重试');
                }
            });
        };

        // 昨日获奖者
        _this.clickLasyDayWinnerBox = function(){
            if (!_this.lastDayWinner()) return;
            _this.isShowTipDialog(true);
            _this.tipDialogData({
                type: 0,
                dialogTitle: '布置期末复习，即可参与奖学金',
                dialogContent: '昨日教育奖学金获得者：' + _this.lastDayWinner().name + '老师',
                btnText: '去布置',
                btnCallback: function () {
                    _this.gotoHomework();
                }
            });
        };

        // 每日复习奖学金 申领
        _this.clickDailyBtn = function(flag, event){
            YQ.voxLogs({
                database: trackDataBase,
                module: trackModule,
                op: 'o_8bvv2loG'
            });

            var $this = $(event.currentTarget);

            if (flag || $this.hasClass('disabled')) return;

            $.ajax({
                url: requestAddress + 'apply_daily.vpage',
                type: 'GET',
                success: function(res){
                    if (res.success){
                        _this.isShowTipDialog(true);
                        _this.tipDialogData({
                            type: 3,
                            dialogTitle: '已进入评奖名单，并获得1园丁豆奖励！',
                            dialogContent: '督促学生复习，将有机会获得今日教育基金',
                            btnText: '确定',
                            btnCallback: function () {
                                _this.isShowTipDialog(false);
                            }
                        });

                        _this.requestDetailInfo();
                    } else if(res.authState === false){ // 未认证
                        _this.isShowTipDialog(true);
                        _this.tipDialogData({
                            type: 1,
                            dialogTitle: '你还不是认证老师',
                            dialogContent: '先去布置期末复习，通过认证即可参与',
                            btnText: '去布置',
                            btnCallback: function () {
                                _this.gotoHomework();
                            }
                        });
                    } else if (res.records === false){ // 未布置
                        _this.isShowTipDialog(true);
                        _this.tipDialogData({
                            type: 2,
                            dialogTitle: '你还没布置期末作业',
                            dialogContent: '每天布置，即可获得当日教育基金机会',
                            btnText: '去布置',
                            btnCallback: function () {
                                _this.gotoHomework();
                            }
                        });
                    } else {
                        _this.showErrorTip(res.info || '出错了，稍后重试');
                    }
                },
                error: function (){
                    _this.showErrorTip('出错了，稍后重试');
                }
            });
        };

        // 总复习奖学金 申领
        _this.clickFinalBtn = function(event){
            var reachStatus = 2; // 未达标
            if ((_this.subject() === 'CHINESE' && _this.detailInfo().termReviewNum >= 3) ||
                (_this.subject() !== 'CHINESE' && _this.detailInfo().basicReviewNum > 0 && _this.detailInfo().termReviewNum >= 3)){
                reachStatus = 1; // 已达标
            }
            YQ.voxLogs({
                database: trackDataBase,
                module: trackModule,
                op: 'o_aaAFRArR',
                s0: reachStatus
            });

            // 尚未达标（已置灰）、待评选（高亮）均不可点击
            var $this = $(event.currentTarget);

            if ($this.hasClass('disabled')) return ;
            if (_this.detailInfo().finalLottery && _this.subject() === 'CHINESE' && _this.detailInfo().termReviewNum >= 3) return ;
            if (_this.detailInfo().finalLottery && _this.subject() !== 'CHINESE' && _this.detailInfo().basicReviewNum > 0 && _this.detailInfo().termReviewNum >= 3) return ;

            $.ajax({
                url: requestAddress + 'apply_final.vpage',
                type: 'GET',
                success: function (res) {
                    if (res.success){
                        _this.isShowTipDialog(true);
                        _this.tipDialogData({
                            type: 4,
                            dialogTitle: '申领成功！',
                            dialogContent: '布置复习内容包括基础必过<br>且布置3次以上复习作业可自动参与评奖',
                            btnText: '去布置',
                            btnCallback: function () {
                                _this.gotoHomework();
                            }
                        });

                        _this.requestDetailInfo();
                    } else {
                        _this.showErrorTip(res.info || '出错了，稍后重试');
                    }
                },
                error:function () {
                    _this.showErrorTip('出错了，稍后重试');
                }
            });
        };

        // 查看名单
        _this.clickShowNameList = function(data){
            _this.isShowNameListDialog(true);
            _this.finalWinnerDialogInfo(data);
        };

        // 点击弹窗布置作业按钮
        _this.gotoHomework = function(){
            window.location.href = "/teacher/termreview/index.vpage?subject=" + _this.subject() + "&log=finalreview_activity";
        };

        // 显示报错弹窗
        _this.showErrorTip = function (errInfo) {
            _this.isShowErrorDialog(true);
            _this.errorDialogData({
                dialogContent: errInfo
            });
        };

        _this.requestDetailInfo();
        YQ.voxLogs({
            database: trackDataBase,
            module: trackModule,
            op: 'o_2EVLf2Zd',
            s0: YQ.getQuery('track')
        });
    };

    /*
     * IndexMode: 首页js
     */
    var IndexMode = function(){
        var _this = this;

        _this.dailyBtnActive = ko.observable(false); // 每日奖学金按钮状态
        _this.dailyBtnText = ko.observable(''); // 每日奖学金按钮文案
        _this.finalBtnActive = ko.observable(false); // 师生奖学金按钮状态
        _this.finalBtnText = ko.observable(''); // 师生奖学金按钮文案

        _this.lastDayWinner = ko.observable('');
        _this.subject = ko.observable('');
        _this.dailyList = ko.observableArray([]); // 每日复习list
        _this.finalList = ko.observableArray([
            {
                "stuList": "闫家瑞、李亦凡、许文悦、王梓萌、张天瑞、谢知佑",
                "userName": "宋立春",
                "schoolName": "北京小学(通州分校)",
                "userId": 1748223
            }, {
                "stuList": "侯硕、王浩羽、李嘉诺、张杨新、钱宇轩、罗欣妍",
                "userName": "杜以慧",
                "schoolName": "上海市松江区新桥小学",
                "userId": 13372325
            }, {
                "stuList": "葛泽天、谢昕予、周珈夷、陈思西、陈柯颖、魏烨骅壹",
                "userName": "周璐",
                "schoolName": "诸暨市枫桥镇小",
                "userId": 12343462
            }, {
                "stuList": "郭云帆小、武思阅、曾凡芮、赵宸浩、吴佩妮、韩立行",
                "userName": "刘凌红",
                "schoolName": "南山区南山实验学校(麒麟)",
                "userId": 1607908
            }, {
                "stuList": "李明朗、宋思彤、余良钊、许文森、苏柏维、许文林",
                "userName": "高琳",
                "schoolName": "大连市金州区湾里小学",
                "userId": 1998313
            }, {
                "stuList": "曾凡隆、林昊泽、孙俊杰、周格庸、张秋晨、伊雯",
                "userName": "娄畹苑",
                "schoolName": "姑苏实验小学",
                "userId": 13231765
            }, {
                "stuList": "卢子涵、张愉涵、王垚鑫、杨镇源、秦康浩、杨梓瑞",
                "userName": "张笑笑",
                "schoolName": "郑州高新区实验小学",
                "userId": 12556529
            }, {
                "stuList": "宋玉晴、杨贝琳、武文一、张雯涵、孙乐健、张峰源",
                "userName": "郭子菡",
                "schoolName": "张店区人民东路小学",
                "userId": 13075226
            }, {
                "stuList": "张子菡、陈奕伶、张子淇、祁雨萱、刘晋希、伍坪洋",
                "userName": "李金蓉",
                "schoolName": "绵阳市江油市中坝镇胜利街小学校",
                "userId": 1956583
            }, {
                "stuList": "孙珮珺、周雅惠、姚竣瀚、边恒一、王天屹、姜瑞琪",
                "userName": "朱慧萍",
                "schoolName": "淄博市张店区和平小区小学",
                "userId": 13300764
            }
        ]); // 总复习list
        _this.finalAttendNum = ko.observable(0); // 达标人数
        _this.detailInfo = ko.observable('');

        _this.isShowTipDialog = ko.observable(false); // 公共提示弹窗
        _this.tipDialogData = ko.observable({
            type: 0, // 0表示昨日获奖者弹窗，1表示非认证老师弹窗，2表示未不布置弹窗，3表示进入评奖名单弹窗，4表示申领成功弹窗
            dialogTitle: '',
            dialogContent: '',
            btnText: '',
            btnCallback: ''
        }); // 提示弹窗数据
        _this.isShowErrorDialog = ko.observable(false); // 报错弹窗
        _this.errorDialogData = ko.observable({
            dialogContent: ''
        }); // 报错弹窗数据
        _this.isShowNameListDialog = ko.observable(false); // 获奖名单弹窗
        _this.finalWinnerDialogInfo = ko.observable(''); // 查看名单弹窗信息

        indexModeSplit(_this);
    };

    /*
     * RuleMode: 评奖细则页js
     */
    var RuleMode = function(){
        YQ.voxLogs({
            database: trackDataBase,
            module: trackModule,
            op: 'o_TSVhq7ZY'
        });
    };

    if (typeof (window.initMode) === 'string'){
        switch (window.initMode){
            case 'indexMode':
                defaultInitMode = new IndexMode();
                break;
            case 'ruleMode':
                defaultInitMode = new RuleMode();
                break;
            default:
        }
    }

    if (defaultInitMode){
        defaultInitMode.nullContent = ko.observable();
        ko.applyBindings(defaultInitMode);
    }
});
