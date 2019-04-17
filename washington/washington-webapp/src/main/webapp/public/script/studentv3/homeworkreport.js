!function() {
    "use strict";

    var reportDate = {};
    var categoryMap = {};
    Date.prototype.Format = function(fmt) { var o = { "M+" : this.getMonth()+1, "d+" : this.getDate(), "h+" : this.getHours(),  "m+" : this.getMinutes(),  "s+" : this.getSeconds(),  "q+" : Math.floor((this.getMonth()+3)/3),  "S" : this.getMilliseconds()  }; if(/(y+)/.test(fmt)) fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length)); for(var k in o) if(new RegExp("("+ k +")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length))); return fmt; }
    var stuReport = function(){
        this.initialise();
    };
    stuReport.prototype = {
        constructor : stuReport,
        subject :$17.getQuery("subject"),
        initialise : function(){
            this.sendlog("stu-reportdetail-"+this.subject,"reportdetail-load");
            this.initDom();
        },
        substitute: function(str, object, regexp){
            return String(str).replace(regexp || (/\\?\{([^{}]+)\}/g), function(match, name){ if (match.charAt(0) == '\\') return match.slice(1); return (object[name] != null) ? object[name] : ''; })
        },
        initDom : function(){
            var that = this,homeworkId = $17.getQuery("homeworkId")
                ,reqUrl = "/student/learning/history/newhomework/detail.vpage";
            $.get(reqUrl,{
                homeworkId:homeworkId
            },function(res){
                reportDate = res;
                if(res.success){
                    $.extend(true,categoryMap,res.detail.objectiveConfigTypes);
                    that.unitInfo();
                    that.commentInfo();
                    that.practices();
                    that.initEvent();
                }else{
                    $17.alert(res.info);
                    $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : reqUrl,
                        s1     : $.toJSON(res),
                        s2     : $.toJSON({homeworkId : homeworkId}),
                        s3     : $studentv3HomeworkReport.env
                    });
                }
            });
        },
        unitInfo: function(){
            $(".J_endTime").html("截止时间："+(new Date(reportDate.detail.endTime)).Format("yyyy-MM-dd hh:mm:ss"));
            var unitInfo='<div class="projectTitle"><h3 class="time">'+(new Date(reportDate.detail.startTime)).Format("yyyy-MM-dd")+'</h3>';
            $.each(reportDate.detail.books,function(){
                var unitName = this.unitName;
                $.each(this.sectionNames,function(){
                    unitInfo += "<p class=\"title\">" + unitName + (this||"") + "</p>";
                });
            });
            unitInfo += '</div><div class="projectTime"><h3 class="count">已完成</h3><p class="time">('+(new Date(reportDate.detail.finishAt)).Format("yyyy-MM-dd hh:mm:ss")+')</p></div>'
            $(".J_unitInfo").html(unitInfo);
        },
        commentInfo: function(){
            var commentInfo = "";
            var $commentInfo = $(".J_commentInfo");
            if(reportDate.detail.comments.length > 0){
                $.each(reportDate.detail.comments,function(){
                    commentInfo += '<div class="reviewList"><div class="rText">'+this.teacherName+'老师：'+this.comment+'</div><div class="rTime"><span>'+(new Date(this.createDate)).Format("yyyy-MM-dd hh:mm:ss")+'</span></div></div>'
                });
                $commentInfo.html(commentInfo);
            }else{
                $commentInfo.hide();
            }
        },
        practices: function(){
            var that = this,str = "";
            $.each(reportDate.detail.practices,function(index){
                var title = categoryMap[index];
                if(index == "BASIC_APP" || index == "LS_KNOWLEDGE_REVIEW" || index == "NATURAL_SPELLING"){
                    title += "（共" + this.completePracticeCount + "个练习）";
                }else if(index == "NEW_READ_RECITE"){
                    title += "（共"+ this.completePracticeCount + "篇）";
                }else if(index == "DUBBING"){
                    title += '';
                }else if(index == "ORAL_COMMUNICATION"){
                    title += "（共"+ (this.rightCount+ this.wrongCount) + "个主题）";
                }else if(index != "READING" && index != "OCR_MENTAL_ARITHMETIC"){
                    title += "（共"+ (this.rightCount+ this.wrongCount) + "题）";
                }
                str += template("t:baseTemplate",{
                    title : title,
                    rightCount : this.rightCount,
                    wrongCount : this.wrongCount,
                    rate       : this.rate,
                    time       : "用时" + this.duration + "分钟",
                    homeworkId : $17.getQuery("homeworkId"),
                    tab       : index,
                    completePracticeCount : this.completePracticeCount,
                    score : this.score,
                    subject : $17.getQuery("subject") || null,
                    state:this.state
                });
            });
            $(".J_practice").html(str);
        },
        formatSeconds: function (value) {
            var theTime = parseInt(value);// 秒
            var theTime1 = 0;// 分
            var theTime2 = 0;// 小时
            if(theTime > 60) {
                theTime1 = parseInt(theTime/60);
                theTime = parseInt(theTime%60);
                if(theTime1 > 60) {
                    theTime2 = parseInt(theTime1/60);
                    theTime1 = parseInt(theTime1%60);
                }
            }
            var result = ""+parseInt(theTime)+"秒";
            if(theTime1 > 0) {
                result = ""+parseInt(theTime1)+"分"+result;
            }
            if(theTime2 > 0) {
                result = ""+parseInt(theTime2)+"小时"+result;
            }
            return result;
        },
        sendlog : function(module,op){
            $17.voxLog({
                module : module,
                op : op
            });
        },
        initEvent : function(){
            var that = this;
            $(".J_practice").on("click","a",function(){
                that.sendlog("stu-reportdetail-"+that.subject,"reportdetail-viewquestions");
                window.location.href = $(this).attr("url");
            });
            
            $("a.goCorrect").on("click",function(){
                var $this = $(this);
                $17.voxLog({
                    module : "m_LJmU0Xeb",
                    op : "studycenter_correction_click",
                    s0 : reportDate.detail.subject
                });
                setTimeout(function(){
                    window.location.href = $this.attr("data-url");
                },200);
            });
        }

    };
    return new stuReport();
}();
