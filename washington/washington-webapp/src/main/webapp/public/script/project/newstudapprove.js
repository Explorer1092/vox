/**
 * Created by 17ZY-HPYKFD2 on 2017/8/23.
 */
define(['jquery', 'knockout','YQ','voxLogs',"impromptu"], function($, ko,YQ){
function newstudapprove() {
    var $this = this;
    $this.newStudNum = ko.observable();
    $this.isPartic = ko.observable();
    $this.participate = ko.observable();
    $this.particTime = ko.observable();
    $this.rankListAll = ko.observableArray([]);
    $this.activDetail = ko.observable(false);
    $this.isLoaded = ko.observable(false);
    $this.isShowProg = ko.observable(false);
    $this.activDetailBtn = ko.observable(true);
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
            op : 'o_5WjNTb6v'
        });
            $this.activDetail(true);
    };
    $this.closeActivDetail = function () {
        $this.activDetail(false);
    };
    YQ.voxLogs({
        database: databaseLogs,
        module: 'm_lIHTCSqE',
        op : 'o_20DMEO3e'
    });
    $this.AssignmentBtn = function (index) {
        YQ.voxLogs({
            database: databaseLogs,
            module: 'm_lIHTCSqE',
            op : 'o_ZszEvso9',
            s0 :  index,
            s1 : '去布置作业'
        });
        setTimeout(function () {
            window.open('/teacher/new/homework/batchassignhomework.vpage?subject=MATH');
        },200);
    };
    $this.joinActiv = function () {
        YQ.voxLogs({
            database: databaseLogs,
            module: 'm_lIHTCSqE',
            op : 'o_rjOf1ev0'
        });
        $this.particActiv();
    };

    $this.particActiv = function () {
        $.ajax({
            url: '/teacher/activity/term2017/actone/participate.vpage',
            type: 'POST',
            dataType: 'json',
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
                        op : 'o_IrC0sr8F'
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
            url: '/teacher/activity/term2017/actone/init.vpage',
            type: 'GET',
            dataType: 'json',
            success: function (res) {
                if(res.success){
                    $this.newStudNum(res.students);
                    $this.activLevel({
                        schoolLevel : res.schoolLevel
                    });
                    $this.dataStudent({
                        auth : res.auth,
                        schoolLevel : res.schoolLevel
                    });
                    if(res.participateDate == null){
                        $this.particTime('无');
                    }else{
                        $this.particTime(res.participateDate);
                    }
                    if(res.schoolLevel == null){
                        $('.JS-joinActivBtn').parent().hide();
                        $this.activDetailBtn(false);
                    }
                    switch ($this.dataStudent().schoolLevel){
                        case "A":
                            $this.database({
                                num1: ko.observableArray([15,30,45]),
                                lengt: ko.observable(2)
                            });
                            break;
                        case "B":
                            $this.database({
                                num1: ko.observableArray([20,40,60]),
                                lengt: ko.observable(2)
                            });
                            break;
                        case "C":
                            $this.database({
                                num1: ko.observableArray([30,60,90]),
                                lengt: ko.observable(2)
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
                    $.prompt("<div style='text-align: center'>"+res.info+"</div>", {
                        title: "系统提示",
                        position: {width: 500},
                        buttons: { "知道了": true }
                    });
                }
            }
        });
    };
    $this.initAjax();
    $this.rankList = function () {
        $.ajax({
            url: '/teacher/activity/term2017/actone/rank.vpage',
            type: 'GET',
            dataType: 'json',
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
