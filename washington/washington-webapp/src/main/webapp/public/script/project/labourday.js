define(['jquery', 'knockout','YQ','voxLogs',"impromptu"], function($, ko,YQ){

    var databaseLogs = "web_teacher_logs";

    function LabourDayMode() {
        var $this = this;
        $this.isLoaded = ko.observable(false);
        $this.participate = ko.observable();
        $this.participateDate = ko.observable();
        $this.homeworkList = ko.observableArray([]);
        $this.activDetail = ko.observable(false);
        $this.progressNumber = ko.observable(0);
        $this.awardMoney = ko.observable();

        YQ.voxLogs({
            database: databaseLogs,
            module: 'm_4zIy3lcI',
            op: 'o_Cryo2BFt'
        });


        $this.AssignmentBtn = function () {
            setTimeout(function () {
                window.open('/teacher/new/homework/batchassignhomework.vpage?subject=MATH');
            },200);
        };

        $this.openActivDetail = function ($data,event) {
            event.stopPropagation();
            $this.activDetail(true);
        };

        $this.closeDialog = function(){
            $this.activDetail(false);
        };

        $this.joinActiv = function () {
            YQ.voxLogs({
                database: databaseLogs,
                module: 'm_4zIy3lcI',
                op: 'o_Jdxo733K'
            });
            $this.particActiv();
        };
        $this.scheduleButton = function ($data, event) {
            var $element = $(event.currentTarget);
            if ($element.parent(".scheduleBox").hasClass("hideMn")) {
                $element.parent(".scheduleBox").removeClass("hideMn");
            } else {
                $element.parent(".scheduleBox").addClass("hideMn");
            }
        };

        $this.particActiv = function () {
            $.ajax({
                url: '/teacher/activity/term2017/acttwo/participate.vpage',
                type: 'POST',
                dataType: 'json',
                success: function (res) {
                    if (res.success) {
                        $this.participate(true);
                        $.prompt("<div style='text-align: center'>参与成功</div>", {
                            title: "系统提示",
                            position: {width: 500},
                            buttons: { "知道了": true }
                        });
                    } else {
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

        $this.rankList = function () {
            $.ajax({
                url: '/teacher/activity/term2017/acttwo/init.vpage',
                type: 'GET',
                dataType: 'json',
                success: function (res) {
                    if (res.success) {
                        $this.participate(res.participate);
                        $this.participateDate(res.participateDate);
                        $this.progressNumber(res.progress);
                        $this.awardMoney(res.awardMoney);
                        $this.isLoaded(true);
                        $this.homeworkList(res.groupHomeworkList);
                    } else {
                        $this.participate(false);
                        $this.isLoaded(true);
                        $this.awardMoney('最高50');
                        $.prompt("<div style='text-align: center'>"+res.info+"</div>", {
                            title: "系统提示",
                            position: {width: 500},
                            buttons: { "知道了": true }
                        });
                    }
                }
            });
        };
        $this.rankList();
    }

    ko.applyBindings(new LabourDayMode());
});
