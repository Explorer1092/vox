/**
 * Created by 17ZY-HPYKFD2 on 2017/8/23.
 */
define(['jquery', 'knockout','YQ','voxLogs',"impromptu"], function($, ko,YQ){
    function Weektask() {
        var $this = this;
        $this.isLoaded = ko.observable(false);
        $this.participate = ko.observable();
        $this.participateDate = ko.observable();
        $this.nickName = ko.observable();
        $this.progressAll = ko.observableArray([]);
        $this.progressList = ko.observableArray([]);
        $this.activDetail = ko.observable(false);
        $this.activDetailBtn = ko.observable(true);
        $this.authFlag =  ko.observable();

        var databaseLogs = "web_teacher_logs";

        YQ.voxLogs({
            database: databaseLogs,
            module: 'm_GSCn8k1V',
            op : 'o_DSY9dgEc'
        });

        $this.openActivDetail = function ($data,event) {
            event.stopPropagation();
            $this.activDetail(true);
        };

        $this.stopPropagation = function ($data,event) {
            event.stopPropagation();
        };

        $this.closeActivDetail = function () {
            $this.activDetail(false);
        };

        $this.AssignmentBtn = function () {
            YQ.voxLogs({
                database: databaseLogs,
                module: 'm_GSCn8k1V',
                op : 'o_kHbSx8ad'
            });
            setTimeout(function () {
                window.open('/teacher/new/homework/batchassignhomework.vpage?subject=MATH');
            },200);
        };

        $this.joinActiv = function () {
            YQ.voxLogs({
                database: databaseLogs,
                module: 'm_GSCn8k1V',
                op : 'o_1PdkvXoi'
            });
            $this.particActiv();
        };

        $this.scheduleButton = function ($data,event) {
            var $element = $(event.currentTarget);
            if ($element.parent(".scheduleBox").hasClass("hideMn")){
                $element.parent(".scheduleBox").removeClass("hideMn");
            }else{
                $element.parent(".scheduleBox").addClass("hideMn");
            }

        };

        $this.particActiv = function () {
            if ($this.authFlag()){
                $.ajax({
                    url: '/teacher/activity/term2017/acttwo/participate.vpage',
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
            }else{
                $.prompt("<div style='text-align: center'>该活动暂时仅限部分学校老师参与，请您关注其他活动哦~</div>", {
                    title: "系统提示",
                    position: {width: 500},
                    buttons: { "知道了": true }
                });
            }
        };

        $this.rankList = function () {
            $.ajax({
                url: '/teacher/activity/term2017/acttwo/init.vpage',
                type: 'GET',
                dataType: 'json',
                success: function (res) {
                    if(res.success){
                        $this.authFlag(res.auth);
                        $this.isLoaded(true);
                        $this.progressAll(res.progress);
                        $this.participate(res.participate);
                        $this.participateDate(res.participateDate);
                        var details = [];
                        $.each(res.groupHomeworkList, function (name, value) {
                            var finishNum = 0;
                            $.each(value.homeworkList, function (index, value) {
                                if (value.accomplished) {
                                    finishNum++;
                                }
                            });
                            value.status=finishNum;
                        });
                        $this.progressList(res.groupHomeworkList);
                        if (res.title){
                            $this.nickName(res.title);
                        }else{
                            $this.nickName("暂无称号");
                        }
                    }else{
                        $this.isLoaded(true);
                        $this.participate(false);
                        /*$.prompt("<div style='text-align: center'>"+res.info+"</div>", {
                            title: "系统提示",
                            position: {width: 500},
                            buttons: { "知道了": true },
                            submit: function(e, v) {
                                if (v) {
                                    window.close();
                                }
                            }
                        });*/
                    }
                }
            })
        };
        $this.rankList();

        $(document).on("click",function () {
            $this.activDetail(false);
        });
    }
    ko.applyBindings(new Weektask());
});
