define(['jquery', 'knockout', 'voxLogs'], function ($, ko) {
    function FinalreviewMode() {
        var $this = this;

        $this.dialogShow = ko.observable(false);
        $this.detail = ko.observable({
            basicReviewNum: 0,
            termReviewNum: 0,
            dailyLottery:"",
            finalLottery:""
        });
        $this.dailyText = ko.observable();
        $this.finalText = ko.observable();
        $this.templateName = ko.observable();
        $this.dailyWinnerData = ko.observable();
        $this.dailyList = ko.observableArray();
        $this.finalAttendNum = ko.observable();
        $this.finalSubject = ko.observable();
        $this.dialogData = ko.observable({
            type: "",
            dialogTitle: "",
            dialogContent: "",
            button: "",
            buttonText:""
        });
        $this.finalReviewListShow = ko.observable(false);
        $this.templateBox = ko.observable();
        $this.finalreviewData = ko.observableArray([
            {
                "stuList": "张博超、何欣语、黄薛媛、严妮文、徐凯杰、卫郗雅、聂元晟、萨拉露丁、蔡哲妍、李欣怡",
                "userName": "张凤",
                "schoolName": "上海市宝山区杨泰实验学校",
                "userId": 13215268,
                "parentList": "王思睿、徐凯杰、李皓轩、钱程逸、黄薛媛"
            }, {
                "stuList": "姜文慧、姜奎彦、王新云、石阿芳、王晓彤、黄浩荣、王梓彤、郭其宜、路雨虹、付子馨",
                "userName": "付继香",
                "schoolName": "泰安市肥城市王瓜店办事处中心小学",
                "userId": 1853600,
                "parentList": "王霆圳、王骁庆、孙靖淼、王嘉琦、黄一哲"
            }, {
                "stuList": "顾真瑜、李馨钰、陆鎏澄、宋迪菲、毛佳应、李熠涵、唐佳烁、陈冯恩、杭严润、李高延",
                "userName": "程艳",
                "schoolName": "常熟市实验小学",
                "userId": 1954402,
                "parentList": "毛佳应、姜稼洲、丁奕之、邵铠晨、吴秋逸"
            }, {
                "stuList": "贺煊雅、邓安时、党笑然、张浩禹、吴思语、孙懿轩、王梓天、李宜聪、许梦瑶、李其实",
                "userName": "杨浩美",
                "schoolName": "西安航天小学",
                "userId": 1772334,
                "parentList": "吴思语、关泽旭、惠伽木、杨博清、陈钰祥"
            }, {
                "stuList": "张艺馨、牛玥焱、刘奕君、吴锦源、江昊泽、师海淼、刘子轩、贾哲翔、魏子阳、张艺涵",
                "userName": "陈伟霞",
                "schoolName": "河南省实验中学思达外国语小学",
                "userId": 1998960,
                "parentList": "赵艺霖、刘奕君、陈科名、江昊泽、黄馨月"
            }, {
                "stuList": "尤峻逸、金雨欣、吴晏宇、张帆、徐瑾诗、范思忆、张相逸、吴奕琦、吕张贺、祝嘉",
                "userName": "曹丹萍",
                "schoolName": "望亭中心小学",
                "userId": 12201029,
                "parentList": "吕淑兰、吴晏宇、殷语熙、凌徐丞、张帆"
            }, {
                "stuList": "宋玉晴、白婉莹、许若琳、韩婧睿、张晋诚、孙乐健、张家豪、李润东、罗紫萱、裴子谦",
                "userName": "郭子菡",
                "schoolName": "张店区人民东路小学",
                "userId": 13075226,
                "parentList": "李梓琪、张佳艺、匡致远、杨尚尚、高思儒"
            }, {
                "stuList": "陈浣宇、刘旭辰、薛添秦、叶锦轩、樊今山、李昭穆、张正熙、马望钭、周锦渝、张梓祺",
                "userName": "李德琴",
                "schoolName": "南坪实验小学(学府路校区)",
                "userId": 12695366,
                "parentList": "张梓祺、蒋欣洋、冉诗筠、黄炫博、杨晨曦"
            }, {
                "stuList": "王梓、李雨航、周亚琳、曹源、李欣铮、陈思涵、彭宣博、陶炫彤、蔡尚轩、李欣铮",
                "userName": "张静",
                "schoolName": "北京市怀柔区第三小学",
                "userId": 16748,
                "parentList": "许颂、贾梓琪、肖乘承、曹源、杜知晓"
            }, {
                "stuList": "胡少骞、李嘉骏、田翀、吴梓宸、勾明宇、祝佳睿、朱梓鹏、苏康宇、母泽琳、张颢译",
                "userName": "童畅",
                "schoolName": "盘龙区东华小学",
                "userId": 13488527,
                "parentList": "刘雨枘、王梓鉴、王睿、李姝娴、勾明宇"
            }
        ]);
        $this.finalreviewList = ko.observable();
        $this.scholarshipRules = function () {
            $this.dialogShow(true);
            $this.dialogData({
                type: "rules",
                dialogTitle: "",
                dialogContent: "",
                button: "",
                buttonText:"去布置"
            });
        };

        $this.lookFinalReview = function (data) {
            $this.finalreviewList(data);
            $this.templateBox("finalReviewMode");
            $this.finalReviewListShow(true);
        };
        $this.closeListShow = function () {
            $this.finalReviewListShow(false);
        };
        $this.getListInit = function () {
            $.ajax({
                url: "/teacher/activity/scholarship/detail.vpage",
                type: "GET",
                dataType: "json",
                success: function (data) {
                    if (data.success) {
                        $this.finalSubject(data.subject);
                        YQ.voxLogs({
                            database: "web_teacher_logs",
                            module: 'm_8NOEdAtE',
                            op : 'scholarship_final_review_load',
                            s0 : data.subject
                        });
                        $this.finalAttendNum(data.finalAttendNum);
                        if(data.dailyWinner == null){
                            $this.templateName("dailyWinnerNull");
                        }else{
                            $this.dailyWinnerData(data.dailyWinner);
                            $this.templateName("dailyWinnerMode");
                        }
                        if(data.dailyList && data.dailyList.length > 0){
                            $this.dailyList(data.dailyList);
                        }
                        if (data.detail != null){
                            $this.detail(data.detail);
                            if (data.detail.dailyLottery){
                                $this.dailyText("已申领");
                                $(".JS-daily").removeClass("current").addClass("disabled");
                            }else{
                                $this.dailyText("申领");
                                $(".JS-daily").removeClass("disabled").addClass("current");
                            }
                            if (data.detail.finalLottery){
                                if (data.subject == "CHINESE"){
                                    if (data.detail.termReviewNum >= 5){
                                        $this.finalText("待评选");
                                        $(".JS-final").removeClass("disabled").addClass("current").addClass("dis");
                                    }else{
                                        $this.finalText("尚未达标");
                                        $(".JS-final").removeClass("current").addClass("disabled");
                                    }
                                }else{
                                    if (data.detail.basicReviewNum >= 1 && data.detail.termReviewNum >= 5){
                                        $this.finalText("待评选");
                                        $(".JS-final").removeClass("disabled").addClass("current").addClass("dis");
                                    }else{
                                        $this.finalText("尚未达标");
                                        $(".JS-final").removeClass("current").addClass("disabled");
                                    }
                                }
                            }else{
                                $this.finalText("申领");
                                $(".JS-final").removeClass("disabled").addClass("current");
                            }
                        }else{
                            $this.dailyText("申领");
                            $this.finalText("申领");
                            $(".JS-daily").removeClass("disabled").addClass("current");
                            $(".JS-final").removeClass("disabled").addClass("current");
                        }
                    }else{
                        location.href = "/";
                    }
                }
            });
        };
        $this.getListInit();

        $this.batchAssignHomework = function () {
            YQ.voxLogs({
                database: "web_teacher_logs",
                module: 'm_8NOEdAtE',
                op : 'scholarship_final_review_assignbutton_click',
                s0 : $this.finalSubject()
            });
            location.href = "/teacher/termreview/index.vpage?log=finalreview";
        };

        $this.dialogButton = function () {
            $this.dialogShow(false);
            var button = $this.dialogData().button;
            if (button != "receivedBeans"){
                YQ.voxLogs({
                    database: "web_teacher_logs",
                    module: 'm_8NOEdAtE',
                    op : 'scholarship_final_review_popup_assignbutton_click',
                    s0 : $this.finalSubject()
                });
                location.href = "/teacher/termreview/index.vpage?log=finalreview";
            }
        };

        $this.closeDialog = function () {
            $this.dialogShow(false);
        };

        $this.dailyWinnerButton = function () {
            if($this.dailyWinnerData()){
                $this.dialogShow(true);
                $this.dialogData({
                    type: "dialog",
                    dialogTitle: "布置期末复习，即可参与奖学金",
                    dialogContent: "昨日kindle奖学金获得者："+$this.dailyWinnerData().name+"老师",
                    button: "authState",
                    buttonText:"去布置"
                });
            }
        };

        $this.dailyScholarship = function () {
            if ($(".JS-daily").hasClass("disabled")){
                return ;
            }
            YQ.voxLogs({
                database: "web_teacher_logs",
                module: 'm_8NOEdAtE',
                op : 'scholarship_final_review_apply_click',
                s0 : $this.finalSubject()
            });
            $.ajax({
                url: "/teacher/activity/scholarship/apply_daily.vpage",
                type: "GET",
                dataType: "json",
                success: function (data) {
                    if (data.success) {
                        $this.dialogShow(true);
                        $this.dialogData({
                            type: "dialog",
                            dialogTitle: "已进入评选名单，并获得1园丁豆奖励！",
                            dialogContent: "督促学生复习，将有机会获得今日kindle",
                            button: "receivedBeans",
                            buttonText:"确定"
                        });
                        $(".JS-daily").removeClass("current").addClass("disabled");
                        $this.dailyText("已申领");
                    }else{
                        if(data.authState == false){
                            $this.dialogShow(true);
                            $this.dialogData({
                                type: "dialog",
                                dialogTitle: "你还不是认证老师~",
                                dialogContent: "先去布置期末复习，通过认证即可参与",
                                button: "authState",
                                buttonText:"去布置"
                            });
                        }
                        if (data.records == false){
                            $this.dialogShow(true);
                            $this.dialogData({
                                type: "dialog",
                                dialogTitle: "你还没有布置期末复习~",
                                dialogContent: "每天布置，即可获得当日kindle奖学金机会",
                                button: "noAssign",
                                buttonText:"去布置"
                            });
                        }
                    }
                }
            });
        };

        $this.finalScholarship = function () {
            if ($(".JS-final").hasClass("disabled") || $(".JS-final").hasClass("dis")){
                return ;
            }
            YQ.voxLogs({
                database: "web_teacher_logs",
                module: 'm_8NOEdAtE',
                op : 'scholarship_final_review_apply_click',
                s0 : $this.finalSubject()
            });
            $.ajax({
                url: "/teacher/activity/scholarship/apply_final.vpage",
                type: "GET",
                dataType: "json",
                success: function (data) {
                    if (data.success) {
                        $this.dialogShow(true);
                        $this.dialogData({
                            type: "dialog",
                            dialogTitle: "申领成功！",
                            dialogContent: "布置复习内容包括基础必过<br/>且布置5次以上复习作业即可自动参与评奖",
                            button: "finalSuccess",
                            buttonText:"去布置"
                        });
                        $.ajax({
                            url: "/teacher/activity/scholarship/detail.vpage",
                            type: "GET",
                            dataType: "json",
                            success: function (data) {
                                if (data.success) {
                                    if (data.detail != null){
                                        if (data.detail.finalLottery){
                                            if (data.detail.basicReviewNum >= 1 && data.detail.termReviewNum >= 5){
                                                $this.finalText("待评选");
                                                $(".JS-final").removeClass("disabled").addClass("current").addClass("dis");
                                            }else{
                                                $this.finalText("尚未达标");
                                                $(".JS-final").removeClass("current").addClass("disabled");
                                            }
                                        }
                                    }
                                }else{
                                    $.alert(data.info);
                                }
                            }
                        });
                    }else{
                        $this.dialogShow(true);
                        $this.dialogData({
                            type: "dialog",
                            dialogTitle: "系统提示",
                            dialogContent: data.info,
                            button: "authState",
                            buttonText:"知道了"
                        });
                    }
                }
            });
        };

        $this.pressImage = function(link, w){
            var defW = 200;
            if(w){
                defW = w;
            }

            if(link && link != "" && link.indexOf('oss-image.17zuoye.com') > -1 ){
                return link + '@' + defW + 'w_1o_75q';
            }else{
                return link;
            }
        };
    }

    ko.applyBindings(new FinalreviewMode());
});