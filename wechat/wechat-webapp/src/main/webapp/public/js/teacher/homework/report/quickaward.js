/**
 * Created by chen meng on 2016/2/23
 */
define(["$17","jbox","logger"], function ($17,jbox,logger) {
    "use strict";
    var stuList = {};
    var quickAward = function () {
        /*$.post("/signup/teacher/login.vpage",{token :126316,pwd:'222222'},function(data){
         console.info(data);
         });*/
        this.initData();
        this.initEvent();
    };
    quickAward.prototype = {
        constructor: quickAward,
        initData: function () {
            var that = this;
            var homeworkId = $17.getQuery("homeworkId");
            $.post("/teacher/homework/report/completion.vpage", {
                homeworkId: homeworkId
            }, function (res) {
                if (res.success) {
                    stuList = res.studentReportList;
                    that.initDom(0);
                } else {
                    var text = res.info || "学生信息获取失败，请稍后再试";
                    $(".J_stuList").html('<p class="txt-tips">' + text + '</p>');
                }
            });
        },
        initDom: function (type) {
            var stu = '', typeList = ["全部", "已完成", "未完成"];
            $.each(stuList, function () {
                var isFinish = this.finishAt == null ? 2 : 1;
                if (type == 0 || type == isFinish) {
                    stu += '<li class="active" uid="' + this.userId + '">' + this.userName + '</li>';
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
            });
            $(".J_stuList").on("click", "li", function () {
                if ($(this).hasClass("active")) {
                    $(this).removeClass("active")
                } else {
                    $(this).addClass("active")
                }
            });
            $(".J_addBean").on("click","span",function(){
                var count = $(".J_sendBean").html();
                if($(this).hasClass("add")) {
                    $(".J_sendBean").html(parseInt(count)+1);
                }else{
                    if(parseInt(count)==0)return;
                    $(".J_sendBean").html(parseInt(count)-1);
                }
            });
            $(".J_submit").on("click", function () {
                var rewardDetail = [],sendBean = $(".J_sendBean").html();
                $(".J_stuList li[class='active']").each(function () {
                    rewardDetail.push({
                        studentId : $(this).attr("uid"),
                        count     : sendBean
                    });
                });
                var param = {
                    clazzId : $17.getQuery("clazzId"),
                    details : rewardDetail,
                    homeworkId: $17.getQuery("homeworkId")
                };
                if (!that._validate(param))return;
                $.ajax({
                    type : "post",
                    dataType   : "json",
                    url : "/teacher/homework/report/batchsendintegral.vpage",
                    data : JSON.stringify(param),
                    success : function(res){
                        if(res.success){
                            that._sendlog({
                                op: "page_onekey_award_confirm_click",
                                s2: sendBean,
                                s3: $(".J_filter li.active").text(),
                                s4: $17.getQuery("homeworkId")
                            });
                            $17.msgTip(res.info || '一键奖励发送成功',function(){
                                window.location.href = "/teacher/homework/report/detail.vpage?homeworkId="+$17.getQuery("homeworkId");
                            });
                        }else{
                            $17.msgTip(res.info || '一键奖励发送失败，请重试');
                        }
                    },
                    contentType: 'application/json;charset=UTF-8'
                });
            });
        },
        _validate: function (param) {
            if(!param.clazzId){
                $17.msgTip("学生班级获取失败");
                return false;
            }
            if (param.details.length==0) {
                $17.msgTip("请选择需要奖励的学生");
                return false;
            }
            if (param.details[0] && param.details[0].count==0) {
                $17.msgTip("请添加奖励的学豆");
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
    return new quickAward();
});