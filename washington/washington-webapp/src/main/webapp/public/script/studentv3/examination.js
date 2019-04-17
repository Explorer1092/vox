!function () {
    "use strict";
    var template = {
        examList: ['<div class="t-his-list">',
            '<span class="leaves leaves-big">{examStartAt}</span>',
            '<span class="leaves leaves-small"></span>',
            '<div class="t-his-dag">',
            '<span class="his-arrow"></span>',
            '<div class="his-con">',
            '<span class="tag w-fl-left w-ag-center w-tab w-tab-1">{subjectName}</span>',
            '<div class="info w-fl-left">',
            '<p>{name}</p>',
            '<p>{timeSpan}</p>',
            '</div>',
            '{status}',
            '<div class="w-clear"></div>',
            '</div>',
            '</div>',
            '</div>'].join("")
    };
    var examStatus = {
        EXPIRED: "已过期",
        REGISTRABLE: "可报名",
        REGISTERED: "开始测试",//已报名
        BEGIN: "开始测试",
        CONTINUE: "继续测试",
        ABSENT: "缺考",
        ISSUING: "待发布成绩",
        END: "已完结",
        NOT_ALLOW_VIEW_SCORE : "成绩不开放"
    };
    /*
     对Date的扩展，将 Date 转化为指定格式的String
     月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
     年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
     (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
     (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18
     */
    Date.prototype.Format = function (fmt) {
        var o = {
            "M+": this.getMonth() + 1,                 //月份
            "d+": this.getDate(),                    //日
            "h+": this.getHours(),                   //小时
            "m+": this.getMinutes(),                 //分
            "s+": this.getSeconds(),                 //秒
            "q+": Math.floor((this.getMonth() + 3) / 3), //季度
            "S": this.getMilliseconds()             //毫秒
        };
        if (/(y+)/.test(fmt))
            fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
        for (var k in o)
            if (new RegExp("(" + k + ")").test(fmt))
                fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        return fmt;
    };

    var subMenuValue = $17.getQuery("submenu").toLocaleLowerCase();

    var exam = function () {
        this.initialise();
    };
    exam.prototype = {
        constructor: exam,
        initialise: function () {
            this.initDom();
        },
        substitute: function (str, object, regexp) {
            return String(str).replace(regexp || (/\\?\{([^{}]+)\}/g), function (match, name) {
                if (match.charAt(0) == '\\') return match.slice(1);
                return (object[name] != null) ? object[name] : '';
            })
        },
        initDom: function () {
            var that = this, html = "";
            var url = "/student/newexam/list.vpage?from=pc";
            if("unittest" === subMenuValue){
                url = "/student/newexam/unit/test/list.vpage?from=pc"
            }
            $.get(url, function (res) {
                if (res.success) {
                    $.each(res.newExamList, function () {
                        html += that.substitute(template.examList, {
                            name: this.name,
                            subjectName: this.subjectName,
                            examStartAt: new Date(this.examStartAt).Format("MM-dd"),
                            timeSpan: that.timeSpan(this),
                            status: that.getStatus(this)
                        });
                    });
                    $("#J_examList").html(html);

                    $17.voxLog({
                        module : "m_86DDZQCl",
                        op     : "o_yvtJ0DSS",
                        s0     : ("unittest" === subMenuValue ? "单元检测" : "地区检测")
                    },"student");

                    setTimeout(function(){
                        $(document).on("click","a[data-href]",function(){
                            var $this = $(this);
                            $17.voxLog({
                                module : "m_86DDZQCl",
                                op     : "o_Ns4NgUB3",
                                s0     : $this.attr("data-examid"),
                                s1     : $this.text(),
                                s2     : ("unittest" === subMenuValue ? "单元检测" : "地区检测")
                            },"student");

                            setTimeout(function(){
                                location.href = $this.attr("data-href");
                            },200);
                        });
                    },200);
                } else {
                    $17.alert(res.info || "数据获取失败，请稍后再试");
                }
            });
        },
        timeSpan: function (data) {
            var str = "";
            switch (data.newExamStudentStatus) {
                case "EXPIRED":
                case "REGISTRABLE":
                case "REGISTERED":
                    str = "报名截止时间: " + new Date(data.applyStopAt).Format("MM月dd日 hh:mm");
                    break;
                case "BEGIN":
                case "CONTINUE":
                case "ABSENT":
                    str = "测试截止时间: " + new Date(data.examStopAt).Format("MM月dd日 hh:mm");
                    break;
                case "ISSUING":
                    str = "成绩发布时间: " + new Date(data.resultIssueAt).Format("MM月dd日 hh:mm");
                    break;
                case "END":
                    str = "测试时间: " + new Date(data.examStartAt).Format("MM月dd日 hh:mm") + " 至 " + new Date(data.examStopAt).Format("MM月d日 hh:mm");
                    break;
                default:
                    str = "测试时间: " + new Date(data.examStartAt).Format("MM月dd日 hh:mm") + " 至 " + new Date(data.examStopAt).Format("MM月dd日 hh:mm");
            }
            return str;
        },
        getStatus: function (examObj) {
            // this.newExamStudentStatus, this.score, this.id,this.fullScore
            var status = examObj.newExamStudentStatus,
                score = examObj.score,
                id = examObj.id,
                fullScore = examObj.fullScore;
            var str = "";
            switch (status) {
                case "EXPIRED":
                    str = '<div class="his-count"><a href="javascript:void(0);" class="w-btnRelease w-btnRelease-disable">' + examStatus[status] + '</a></div>';
                    break;
                case "REGISTRABLE":
                    str = '<div class="his-count"><a href="javascript:void(0);" data-examid="' + id + '" data-href="/student/newexam/apply.vpage?from=history&id=' + id + '" class="w-btnRelease w-btnRelease-green">' + examStatus[status] + '</a></div>';
                    break;
                case "REGISTERED":
                case "BEGIN":
                case "CONTINUE":
                    str = '<div class="his-count"><a href="javascript:void(0);" data-examid="' + id + '" data-href="/student/newexam/begin.vpage?from=history&id=' + id + '" class="w-btnRelease w-btnRelease-green">' + examStatus[status] + '</a></div>';
                    break;
                case "ABSENT":
                    str = '<span class="his-img absent"></span>';
                    break;
                case "ISSUING":
                    str = '<div class="his-count"><a href="javascript:void(0);" class="w-btnRelease w-btnRelease-disable">' + examStatus[status] + '</a></div>';
                    break;
                case "END":
                    var viewStudentDetailUrl;
                    if(examObj.oldNewExam){
                        viewStudentDetailUrl = '/student/newexam/result.vpage?' + $.param({
                                id : id
                            });
                    }else{
                        viewStudentDetailUrl = '/newexamv2/viewstudent.vpage?' + $.param({
                                examId : id,
                                userId : constantObj.userId,
                                from : "student_history"
                            });
                    }
                    var gradeType = examObj.gradeType || 0,scoreStr;
                    if(gradeType == 1){
                        scoreStr = examObj.scoreLevel;
                    }else{
                        scoreStr = score + '<span>分（共' + (fullScore ? fullScore : 0) + '分）</span>';
                    }
                    str = '<div class="detail w-fl-left" style="position: absolute;top: -5px;right: 18px;"><div class="score" style="line-height: 100%;width:152px;">' + scoreStr + '</div><a href="javascript:void(0);" data-examid="' + id + '" data-href="' + viewStudentDetailUrl + '" style="margin-top:49px;width:152px;">查看详情</a></div>';
                    break;
                default:
                    str = '<div class="his-count"><a href="javascript:void(0);" class="w-btnRelease w-btnRelease-disable">' + examStatus[status] + '</a></div>';
            }
            return str;
        }
    };
    return new exam();
}();
