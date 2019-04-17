!function() {
    "use strict";
    Date.prototype.Format = function(fmt) { var o = { "M+" : this.getMonth()+1, "d+" : this.getDate(), "h+" : this.getHours(),  "m+" : this.getMinutes(),  "s+" : this.getSeconds(),  "q+" : Math.floor((this.getMonth()+3)/3),  "S" : this.getMilliseconds()  }; if(/(y+)/.test(fmt)) fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length)); for(var k in o) if(new RegExp("("+ k +")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length))); return fmt; }
    var listDate = {};
    var TPL = {
        mainContent:['<li>',
            '<p class="intro-title"><i class="icon-circle"></i>{startTime}</p>',
            '<div class="intro-content">',
            '<div class="contentBox">',
            '<div class="content-intro"><div class="ci-title">内容 :{books}</div>{isRepair}</div>',
            '<div class="hWork-btns">{transfer}</div>',
            '</div>',
            '<div class="content-review" style="display: {commentDisplay}">{comment}</div>',
            '</div>',
            '</li>'].join("")
    };
    var stuList = function(){
        this.initialise();
    };
    //日期
    var _date = new Date();
    var endDate = _date.toLocaleDateString().split('/').join('-');
    $( "#beginDateInput").datepicker({
        dateFormat      : 'yy-mm-dd',
        maxDate		  : endDate
    });
    $( "#beginDateInput").val(constantObj.defaultStartDate);
    stuList.prototype = {
        constructor : stuList,
        isInitPage : true,
        currentPage : 0,
        subject:$17.getQuery("subject"),
        initialise : function(){
            this.initDom();
            this.sendlog("stu-reportlist-"+this.subject,"reportlist-load");
        },
        substitute: function(str, object, regexp){
            return String(str).replace(regexp || (/\\?\{([^{}]+)\}/g), function(match, name){ if (match.charAt(0) == '\\') return match.slice(1); return (object[name] != null) ? object[name] : ''; })
        },
        initDom : function(){
            var that = this,paramData = {
                subject:$17.getQuery("subject"),
                page: that.currentPage,
                begin: $("#beginDateInput").val()
            };
            $('.searchBtn').unbind().on('click',function () {
                var checkDate = /^(\d{4})-(\d{2})-(\d{2})$/;
                var dateVal = $("#beginDateInput").val();
                if(dateVal.trim()==""){
                    $17.alert("请输入查询日期!");
                    return false;
                }
                if (!checkDate.test(dateVal)) {
                    $17.alert("日期格式不正确! 请按照 xxxx-xx-xx 格式输入!");
                    return false;
                 }
                that.currentPage = 0;
                that.isInitPage = true;
                that.initDom()
            })
            $(".left-text").html((that.subject == "MATH" ? "数学" : that.subject == "CHINESE" ? "语文" : "英语") + "作业历史");
            $.post("/student/learning/history/newhomework/timelimitlist.vpage",paramData,function(res){
                listDate = res;
                if(res.success){
                    that.mainContent();
                    that.initEvent();
                    if(that.isInitPage && listDate.history && listDate.history.content && listDate.history.content.length>0){
                        that.isInitPage = false;
                        $("#sharingPage").page({
                            total: listDate.history && listDate.history.totalPages,
                            current: 1,
                            showTotalPage: true,
                            jumpCallBack: function (index) {
                                that.currentPage = index;
                                that.initDom();
                            }
                        });
                    }
                }else{
                    $(".J_mainContent").html("<div class='data-exception'></div>");
                    $("#sharingPage").html("");
                    that.isInitPage = true;
                    $17.voxLog({
                        module : "API_REQUEST_ERROR",
                        op     : "API_STATE_ERROR",
                        s0     : "/student/learning/history/newhomework/list.vpage",
                        s1     : $.toJSON(res),
                        s2     : $.toJSON(paramData),
                        s3     : $homeworkReportList.env
                    });
                    $17.alert(res.info)
                }
            });
        },
        mainContent: function(){
            var that = this,str = "<ul class=\"h-mainTimeAxis\">";
            if(listDate.history && listDate.history.content && listDate.history.content.length>0){
                $.each(listDate.history.content,function(){
                    var books="",comment="",transfer="";
                    if(this.comments && this.comments.length > 0){
                        $.each(this.comments,function(){
                            comment +='<div class="reviewList">'+this.teacherName+'老师：'+this.comment+'<span class="time">'+new Date(this.createDate).Format("yyyy-MM-dd hh:mm:ss")+'</span></div>';
                        });
                    }
                    switch (this.homeworkStatus){
                        case 0:
                            transfer = '<a href="javascript:void(0);" type="0" url="/student/learning/history/newhomework/homeworkreport.vpage?homeworkId='+this.homeworkId+'&subject='+that.subject+'&navLink=history" class="hw-btn hw-btn-green">查看作业详情</a>';
                            break;
                        case 1:
                            transfer = '<a href="javascript:void(0);" type="1" url="/student/homework/index.vpage?from=history&homeworkId=' + this.homeworkId + '" class="hw-btn hw-btn-yellow">立即补做1</a>';
                            break;
                        case 2:
                            transfer = '<a href="javascript:void(0);" type="2" url="/student/homework/index.vpage?from=history&homeworkId=' + this.homeworkId + '" class="hw-btn hw-btn-yellow">开始作业</a>';
                            break;
                        case 3:
                            transfer = '<a href="javascript:void(0);" type="3" class="hw-btn hw-btn-disabled">已过期</a>';
                            break;
                        default:
                            transfer = '<a href="javascript:void(0);" type="0" url="/student/learning/history/newhomework/homeworkreport.vpage?homeworkId='+this.homeworkId+'&subject='+that.subject+'&navLink=history" class="hw-btn hw-btn-green">查看作业详情</a>';
                            break;
                    }
                    $.each(this.books,function(){
                        books += this.unitName + "；";
                    });
                    str += that.substitute(TPL.mainContent,{
                        books:books.substring(0,books.length-1),
                        startTime:new Date(this.startTime).Format("yyyy-MM-dd"),
                        comment:comment,
                        commentDisplay : !!comment ? "block;" : "none;",
                        isRepair:this.homeworkStatus==0?'<div class="ci-complete"><i class="state-icon"></i>已完成</div>':'<div class="ci-completeNo"><i class="state-icon"></i>未完成</div>',
                        transfer:transfer
                    });
                });
                $(".J_mainContent").html(str+"</ul>");
            }else{
                $(".J_mainContent").html("<div class='no-record'></div>");
                $("#sharingPage").html("");
                that.isInitPage = true;
            }
        },
        sendlog : function(module,op){
            $17.voxLog({
                module : module,
                op : op
            });
        },
        initEvent : function(){
            var that = this;
            $(".J_mainContent").on("click","a",function(){
                var $this = $(this);
                var type = +$this.attr("type");
                var tempUrl = $this.attr("url");
                if($17.isBlank(tempUrl)){
                    return false;
                }
                switch(type){
                    case 0 :
                        that.sendlog("stu-reportlist-"+that.subject,"reportlist-viewdetail");
                        break;
                    case 1 :
                        $17.voxLog({
                            module : "m_9vFa5c0g",
                            op : "homework_history_records_click",
                            s0 : that.subject,
                            s1 : "repair"
                        });
                        break;
                    case 2 :
                        $17.voxLog({
                            module : "m_9vFa5c0g",
                            op : "homework_history_records_click",
                            s0 : that.subject,
                            s1 : "start"
                        });
                        break;
                    default:
                        that.sendlog("stu-reportlist-"+that.subject,"reportlist-viewdetail");
                }
                setTimeout(function(){
                    window.location.href = $this.attr("url");
                },200);
            });
        }
    };
    return new stuList();
}();
