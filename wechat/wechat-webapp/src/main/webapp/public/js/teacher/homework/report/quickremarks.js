/**
 * Created by chen meng on 2016/2/19
 */
define(["$17","jbox","logger"], function ($17,jbox,logger) {
    "use strict";
    var stuList = {};
    var _from = {top: "顶部", bottom : "一键"}[$17.getQuery("from")];
    var TPL = {
        math : ['<li class="txt-overflow active"><i class="w-icon w-icon-radio"></i>做得太棒了！</li>',
                '<li class="txt-overflow"><i class="w-icon w-icon-radio"></i>你的作业质量比以前有了很大的进步！</li>',
                '<li class="txt-overflow"><i class="w-icon w-icon-radio"></i>你是一个很有数学才能的学生！</li>',
                '<li class="txt-overflow"><i class="w-icon w-icon-radio"></i>你的计算能力有了很大提高！</li>',
                '<li class="txt-overflow"><i class="w-icon w-icon-radio"></i>对于计算题，也要注意留心观察与思考！</li>',
                '<li class="txt-overflow"><i class="w-icon w-icon-radio"></i>多想一想前后知识的联系，你就会变得更聪明！</li>',
                '<li class="txt-overflow"><i class="w-icon w-icon-radio"></i>你的目标，应该是在数学方面成为同学们的榜样！</li>',
                '<li class="txt-overflow"><i class="w-icon w-icon-radio"></i>有的题目如果你能再认真读下已知条件，就一定能做对！</li>'],
        chinese : ['<li class="txt-overflow active"><i class="w-icon w-icon-radio"></i>做得太棒了！</li>',
            '<li class="txt-overflow"><i class="w-icon w-icon-radio"></i>恭喜你，你已经取得了很大的进步！</li>',
            '<li class="txt-overflow"><i class="w-icon w-icon-radio"></i>有些小错误，下次要多加注意。</li>',
            '<li class="txt-overflow"><i class="w-icon w-icon-radio"></i>如果你更加努力的话，我相信你会做得更好！</li>',
            '<li class="txt-overflow"><i class="w-icon w-icon-radio"></i>如果能把所有作业都按时完成，你会进步得很快！</li>',
            '<li class="txt-overflow"><i class="w-icon w-icon-radio"></i>你的作业质量比以前有了很大的进步！</li>']
    };
    var quickReports = function () {
        /*$.post("/signup/teacher/login.vpage",{token :126316,pwd:'222222'},function(data){
            console.info(data);
        });*/
        this.initData();
        this.initEvent();
    };
    quickReports.prototype = {
        constructor: quickReports,
        initData: function () {
            var that = this;
            var homeworkId = $17.getQuery("homeworkId");
            if($17.getQuery('subject') =="MATH"){
                $(".J_material").html(TPL.math);
            }else{
                $(".J_material").html(TPL.chinese);
            }
            $.post("/teacher/homework/comments.vpage", {
                homeworkId: homeworkId
            }, function (res) {
                if (res.success) {
                    stuList = res;
                    that.initDom(0);
                } else {
                    var text = res.info || "学生信息获取失败，请稍后再试";
                    $(".J_stuList").html('<p class="txt-tips">' + text + '</p>');
                }
            });
        },
        initDom: function (type) {
            var stu = '', typeList = ["全部", "已完成", "未完成"];
            $.each(stuList.states, function () {
                var isFinish = this.finished ? 1 : 2;
                if (type == 0 || type == isFinish) {
                    var uid = this.userId;
                    stu += '<li class="active" uid="' + this.userId + '">' + this.userName;
                    $.each(stuList.comments,function(){
                        if(this.studentId==uid){
                            stu += "<span class='state'>已评</span>";
                            return false;
                        }
                    });
                    stu += "</li>";
                }
            });
            if (stu) {
                $(".J_stuList").html('<div class="msf-stuInfo-label"> <ul class="classInfo">' + stu + '</div>');
            } else {
                $(".J_stuList").html('<p class="txt-tips">没有' + typeList[type] + '学生</p>');
            }
        },
        initEvent: function () {
            var that = this;
            $(".J_filter").on("click", "li", function () {
                that.initDom($(this).attr("type"));
                $(this).addClass("active").siblings().removeClass("active");

                that._sendlog({
                    op: "page_write_comments_score_click",
                    s2: _from
                });

            });
            $(".J_stuList").on("click", "li", function () {
                if ($(this).hasClass("active")) {
                    $(this).removeClass("active")
                } else {
                    $(this).addClass("active")
                }
            });
            $(".J_feedbak").on("input propertychange", function () {
                $(".num-tips").html($(this).val().length + "/100");
            });
            $(".J_submit").on("click", function () {
                var uids = "";
                $(".J_stuList li[class='active']").each(function () {
                    uids += $(this).attr("uid") + ",";
                });
                var param = {
                    homeworkId: $17.getQuery("homeworkId"),
                    userIds: uids.substring(0, uids.length - 1),
                    comment: $(".J_feedbak").val()
                };
                if (!that._validate(param))return;
                $.post("/teacher/homework/report/comment.vpage", param, function (res) {
                    if (res.success) {
                        that._sendlog({
                            op: 'page_write_comments_confirm_click',
                            s2: _from,
                            s3: $17.getQuery("homeworkId"),
                            s4: $(".J_filter li.active").text()
                        });
                        $17.msgTip("评语添加成功",function(){
                            window.location.href = "/teacher/homework/report/detail.vpage?homeworkId="+$17.getQuery("homeworkId");
                        });
                    } else {
                        var text = res.info || "评语添加失败，请稍后再试";
                        $17.alert(text);
                    }
                });
            });
            $(".J_templateText").on("click", function () {
                $(".J_slideBox").show();

                that._sendlog({
                    op: "page_write_comments_mould_click",
                    s2: _from
                });
            });
            $(".J_slideBox .close").on("click", function () {
                $(".J_slideBox").hide();
            });
            $(".J_slideBox li").on("click", function () {
                $(this).addClass("active").siblings().removeClass("active");
            });
            $(".J_slideBox .J_selText").on("click", function () {
                var text = $(".J_slideBox li[class='txt-overflow active']").text();
                $(".J_feedbak").val($(".J_feedbak").val() + text);
                $(".num-tips").html($(".J_feedbak").val().length + "/100");
                $(".J_slideBox .close").trigger("click");
            });
        },
        _validate: function (param) {
            if (!param.userIds) {
                $17.msgTip("请选择需要添加评语的学生");
                return false;
            }
            if (param.comment.length == 0 || param.comment.length > 100) {
                $17.msgTip("请填写少于100字评语");
                return false;
            }
            return true;
        },

        _sendlog: function (obj) {
            var def = {
                app: "teacher",
                module: 'm_pGqNIEG2',
                s0: $17.getQuery('subject') || LoggerProxy.subject
            };
            $.extend(def, obj);
            logger.log(def);
        }
    };
    return new quickReports();
});