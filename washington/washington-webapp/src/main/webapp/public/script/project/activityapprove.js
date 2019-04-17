/**
 * Created by 17ZY-HPYKFD2 on 2017/8/23.
 */
define(['jquery', 'knockout','YQ','voxLogs',"impromptu"], function($, ko,YQ){
    function newstudapprove() {
        var $this = this;
        $this.newStudNum = ko.observable(0);
        $this.isPartic = ko.observable(0);
        $this.participate = ko.observable();
        $this.particTime = ko.observable();
        $this.rankListAll = ko.observableArray([]);
        $this.activDetail = ko.observable(false);
        $this.isLoaded = ko.observable(false);
        $this.isShowProg = ko.observable(false);
        $this.activDetailBtn = ko.observable(true);
        $this.activityId = ko.observable(YQ.getQuery("activityId"));
        $this.activityNote = ko.observable();
        $this.activityTime = ko.observable("2018年3月12日到2018年4月10日");
        $this.endTime = ko.observable("2018年5月9日");
        $this.activityperiod = ko.observable('小学');
        var numArray1 = [],numArray2 = [],numArray3 = [],numArray4 = [];
        switch (YQ.getQuery("activityId")){
            case '120001':
                $this.activityNote('英语');
                var logs0 = "小学英语";
                numArray1 = [20,30,60];
                numArray2 = [30,60,90];
                numArray3 = [60,120];
                numArray4 = [60];
                break;
            case '130001':
                $this.activityNote('语文');
                $this.activityTime("2018年3月12日到2018年4月30日");
                $this.endTime("2018年5月30日");
                var logs0 = "小学语文";
                numArray1 = [15,30,45];
                numArray2 = [20,40,60];
                numArray3 = [30,60,90];
                break;
            case '150001':
                $this.activityNote('英语');
                $this.activityTime("2018年4月20日到2018年5月31日");
                $this.endTime("2018年6月1日");
                var logs0 = "中学英语";
                $this.activityperiod('中学');
                numArray2 = [30,60,90];
                numArray3 = [30,60,90];
                break;
        }

        $this.dataStudent = ko.observable({
            auth : '',
            schoolLevel : ''
        });
        $this.database =  ko.observable({
            num1 : ko.observableArray(),
            lengt : ko.observable()
        });
        $this.linkAuthPag =function () {
            var hosts;
            if (location.host.indexOf("test.17zuoye")>-1){
                hosts = "ucenter.test.17zuoye.net";
            }else if (location.host.indexOf("staging.17zuoye")>-1){
                hosts = "ucenter.staging.17zuoye.net";
            }else{
                hosts = "ucenter.17zuoye.com";
            }
            var loc = location.protocol+'//'+hosts;
            window.open(loc+'/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage');
        };
        $this.activLevel = ko.observable({
            schoolLevel : ''
        });
        var databaseLogs = "web_teacher_logs";
        $this.openActivDetail = function () {
            YQ.voxLogs({
                database: databaseLogs,
                module: 'm_lIHTCSqE',
                op : 'o_5WjNTb6v',
                s0 : logs0
            });
            $this.activDetail(true);
        };
        $this.closeActivDetail = function () {
            $this.activDetail(false);
        };
        YQ.voxLogs({
            database: databaseLogs,
            module: 'm_lIHTCSqE',
            op : 'o_20DMEO3e',
            s0 : logs0
        });
        $this.AssignmentBtn = function (index) {
            YQ.voxLogs({
                database: databaseLogs,
                module: 'm_lIHTCSqE',
                op : 'o_ZszEvso9',
                s0 :  index,
                s1 : '去布置作业',
                s2 : logs0
            });
            if (YQ.getQuery("activityId") === '150001'){
                setTimeout(function () {
                    var zxLink;
                    if (location.href.indexOf("test.17zuoye")>-1){
                        zxLink = "zx.test.17zuoye.net";
                    }else if(location.href.indexOf("staging.17zuoye")>-1){
                        zxLink = "zx.staging.17zuoye.net";
                    }else{
                        zxLink = "zx.17zuoye.com";
                    }
                    window.open(location.protocol+'//'+zxLink+'/teacher/assign/index?from=nav&previewFrom=create_assignment');
                },200);
            }else{
                setTimeout(function () {
                    var subject = "";
                    if ($this.activityNote()=='语文'){
                        subject = "CHINESE";
                    }else if($this.activityNote()=='英语'){
                        subject = "ENGLISH";
                    }
                    window.open('/teacher/new/homework/batchassignhomework.vpage?subject='+subject);
                },200);
            }
        };
        $this.joinActiv = function () {
            YQ.voxLogs({
                database: databaseLogs,
                module: 'm_lIHTCSqE',
                op : 'o_rjOf1ev0',
                s0 : logs0
            });
            $this.particActiv();
        };

        $this.particActiv = function () {
            $.ajax({
                url: '/teacher/activity/term2017/actone/participate_activity.vpage',
                type: 'POST',
                dataType: 'json',
                data:{
                    activityId:YQ.getQuery("activityId")
                },
                success: function (res) {
                    if(res.success){
                        $this.participate(true);
                        $.prompt("<div style='text-align: center'>参与成功</div>", {
                            title: "系统提示",
                            position: {width: 500},
                            buttons: { "知道了": true }
                        });
                        YQ.voxLogs({
                            database: databaseLogs,
                            module: 'm_lIHTCSqE',
                            op : 'o_IrC0sr8F',
                            s0 : logs0
                        });
                        setTimeout(function () {
                            location.reload();
                        },400);
                    }else{
                        $this.participate(false);
                        $.prompt("<div style='text-align: center'>"+res.info+"</div>", {
                            title: "系统提示",
                            position: {width: 500},
                            buttons: { "知道了": true }
                        });
                    }
                }
            });
        };
        $this.initAjax = function () {
            $.ajax({
                url: '/teacher/activity/term2017/actone/init_activity.vpage',
                type: 'GET',
                dataType: 'json',
                data:{
                    activityId:YQ.getQuery("activityId")
                },
                success: function (res) {
                    if(res.success){
                        $this.newStudNum(res.students);
                        $this.activLevel({
                            schoolLevel : res.level
                        });
                        $this.dataStudent({
                            auth : res.auth,
                            schoolLevel : res.level
                        });
                        if(res.participateDate == null){
                            $this.particTime('无');
                        }else{
                            $this.particTime(res.participateDate);
                        }
                        if(res.level == null){
                            $('.JS-joinActivBtn').parent().hide();
                            $this.activDetailBtn(false);
                        }

                        switch ($this.dataStudent().schoolLevel){
                            case 'A':
                                $this.database({
                                    num1: ko.observableArray(numArray1)
                                });
                                break;
                            case 'B':
                                $this.database({
                                    num1: ko.observableArray(numArray2)
                                });
                                break;
                            case 'C':
                                $this.database({
                                    num1: ko.observableArray(numArray3)
                                });
                                break;
                            case 'D':
                                $this.database({
                                    num1: ko.observableArray(numArray4)
                                });
                                break;
                        }
                        if(res.participate){
                            $this.participate(true);
                            if(res.rank >= 10000){
                                $this.isPartic('10000+');
                            }else{
                                $this.isPartic(res.rank);
                            }
                            $this.isShowProg(true);
                            $('.JS-joinActivBtn').parent().hide();
                        }else{
                            $this.participate(false);
                            $this.isPartic('未参与');
                        }
                        $this.isLoaded(true);
                    }else{
                        $this.activDetailBtn(false);
                        $this.isLoaded(true);
                    }
                }
            });
        };
        $this.initAjax();
        $this.rankList = function () {
            $.ajax({
                url: '/teacher/activity/term2017/actone/rank_activity.vpage',
                type: 'GET',
                dataType: 'json',
                data:{
                    activityId:YQ.getQuery("activityId")
                },
                success: function (res) {
                    if(res.success){
                        $this.rankListAll(res.rankList);
                    }
                }
            })
        };
        $this.rankList();
    }
    ko.applyBindings(new newstudapprove());
});
