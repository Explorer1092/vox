<#import "../module.ftl" as temp />
<@temp.pagecontent mainmenu="classroom_ask" submenu="myreport">
<@sugar.capsule js=["datepicker"] css=["plugin.datepicker"] />
<div class="s-dateTip-box">
    <ul class="s-date-box s-fl-left">
        <li><a data-tab="today" data-tab_name="今天" data-start_date="${todayStartDate!''}" data-end_date="${todayEndDate!''}" href="javascript:void (0);" class="active date" data-title="今天">今天</a></li>
        <li><a data-tab="yesterday" data-tab_name="昨天" data-start_date="${yesterdayStartDate!''}" data-end_date="${yesterdayEndDate!''}" href="javascript:void (0);" class="active date" data-title="昨天">昨天</a></li>
        <li><a data-tab="currentWeek" data-tab_name="本周" data-start_date="${currentWeekStartDate!''}" data-end_date="${currentWeekEndDate!''}" href="javascript:void (0);" class="active date" data-title="本周">本周</a></li>
        <li><a data-tab="currentMonth" data-tab_name="本月" data-start_date="${currentMonthStartDate!''}" data-end_date="${currentMonthEndDate!''}"href="javascript:void (0);" class="active date" data-title="本月">本月</a></li>
        <li>
            <label>
                <input readonly="readonly" value="${currentWeekStartDate!''}" title="起始时间" id="startDate" type="text" style="margin-left: 12px" class="time" />
            </label>
        </li>
        <li><span style="width: 5px; margin:9px 15px 5px 5px; float: left">至</span></li>
        <li>
            <label>
                <input readonly="readonly" value="${currentWeekEndDate!''}" title="结束时间" id="endDate" type="text" class="time" />
            </label>
        </li>
        <li> <a id="searchBtn" href="javascript:void (0);" class="time search">搜索</a></li>
        <li class="s-clear"></li>

    </ul>
    <#--<a class="s-fl-right s-magT-10" href="javascript:void (0)">返回</a>-->
    <div class="s-clear"></div>
</div>

<#-- 发放记录列表 -->
<div id="content_list_box">
    <div class="today" style="display: none"></div>
    <div class="yesterday" style="display: none"></div>
    <div class="currentWeek"></div>
    <div class="currentMonth" style="display: none"></div>
    <div class="query_list" style="display: none"></div>
</div>

<script type="text/html" id="t:rewardstudent">
    <div class="historyReward historyAllTableBox">
        <p class="jqicontent" style="text-align: center; font-size:14px;">
            奖励对象：<%=rewardNames%></p>
        <div class="" style="text-align: center;">
            <div class="w-addSub-int">
                奖励学豆数量：
                <a class="w-btn w-btn-mini minusBtn" href="javascript:void (0)">-</a>
                <input class="w-int tempNum" id="tempNumBox" name="tempNumBox" maxlength="2" type="text" value="1" style="width: 140px;" >
                <a class="w-btn w-btn-mini plusBtn" href="javascript:void (0)">+</a>
            </div>
        </div>
        <p class="silverInfo jqicontent" style="text-align: center; font-size:14px;">
            说明：每名学生发放<strong class="w-blue silverCountBox">1</strong>学豆，会消耗您<strong class="w-red goldCountBox" data-studentcnt="<%=studentCnt%>"><%=studentCnt%></strong>学豆，您确定要发放吗？
        </p>
        <p class="errorInfo jqicontent" style="display:none;">园丁豆不足!</p>
    </div>
</script>

<script>

    function checkNum(){
        var maxReward = 100; //兑换学豆最多值
        var changeNum = 1; //加减学豆数差值

        if(arguments[0] > maxReward || !$17.isNumber(arguments[0])){
            return 100;
        }

        if(arguments[1] == "minus"){
            if(arguments[0] - changeNum <= 0 || arguments[0] - changeNum > maxReward){
                return arguments[0];
            }else{
                return arguments[0] - changeNum;
            }
        }else{
            if(arguments[0] + changeNum < 0 || arguments[0] + changeNum > maxReward){
                return arguments[0];
            }else{
                return arguments[0] + changeNum;
            }
        }
    }

    //兑换学豆
    function changeSilver($that){
        var totalSilver = 0;
        $(".tempNum").each(function(){
            totalSilver += $(this).val() * 1;
        });

        if(totalSilver == 0 || totalSilver == 99){
            $that.addClass("btn_disable");
            $("#tempNumBox").focus();
        }

        if(totalSilver == 0){
            $that.css({"cursor" : "default"});
            $('.silverInfo').hide().siblings(".errorInfo").show().html('请添加学豆数量');
            return false;
        }else{
            $("#tempNumBox").removeClass("btn_disable");
            $that.siblings().closest('a.minusBtn ').css({"cursor" : "pointer"});
            $('.silverInfo').show().siblings(".errorInfo").hide();
        }

        var mBox = $('.historyAllTableBox');
        var $totalSpan = mBox.find('.goldCountBox');
        $totalSpan.text(Math.ceil(totalSilver) * ($totalSpan.attr("data-studentcnt") * 1));
        mBox.find('.silverCountBox').text(totalSilver);
    }

    $(function(){
        $17.tongji("互动课堂-课堂提问-我的报告");
        $("#startDate").datepicker({
            dateFormat      : 'yy-mm-dd',
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : "${currentWeekStartDate!''}",
            minDate         : "-3m",
            maxDate         : "${currentWeekStartDate!''}",
            onSelect : function (){}
        });

        $('#endDate').datepicker({
            dateFormat          : 'yy-mm-dd',
            monthNames          : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin         : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate         : "${currentWeekEndDate!''}",
            minDate             : "-3m",
            maxDate             : "${currentWeekEndDate!''}",
            onSelect            : function(){}
        });

        var historyReport = {
            clazzId    : "${clazz.id}",
            startDate  : "${todayStartDate!''}",
            endDate    : "${todayEndDate!''}",
            currentTab : "today",
            currentEle : $("a[data-tab='today']"),
            changePage : function(pageNo){
                if(!$17.isNumber(pageNo) || pageNo < 1){
                    pageNo = 1;
                }
                $('.'+ historyReport.currentTab).load("/teacher/smartclazz/findquestionreport.vpage",{
                    clazzId  : historyReport.clazzId,
                    startDate: historyReport.startDate,
                    endDate  : historyReport.endDate,
                    pageNo   : pageNo,
                    subject  : "${curSubject!}"
                },function(data){
                    $('.'+ historyReport.currentTab).siblings().hide();
                    $('a[data-tab='+ historyReport.currentTab +']').addClass('hasContent');

                    $("a.questionPage").die().live("click",function(){
                        historyReport.changePage(historyReport.currentTab,$(this).attr("data-page"));
                    });
                });
            },
            cacheSearch : function(){
                //已加载过的页面 直接显示(搜索除外)
                if(historyReport.currentEle.hasClass('hasContent') && historyReport.currentTab != 'query_list'){
                    $('.'+ historyReport.currentTab).show().siblings().hide();
                    return false;
                }

                if(historyReport.currentEle.hasClass("loading")){return false;}
                historyReport.currentEle.addClass("loading");
                $('.' + historyReport.currentTab).html('<div class="text_center" style="padding: 50px 0;"><img class="throbber" src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>').show().siblings().hide();
                historyReport.changePage();
                return true;
            },
            init : function(){
                //默认获取今天数据
                historyReport.cacheSearch();

                ////今天 昨天 本周 本月
                $("a.date").on("click",function(){
                    var $this = $(this);
                    $17.tongji("互动课堂-课堂提问-我的报告-" + $this.attr("data-title"));
                    historyReport.startDate = $this.data('start_date');
                    historyReport.endDate = $this.data('end_date');
                    historyReport.currentTab = $this.data('tab');
                    historyReport.currentEle = $this;
                   // $this.parent().addClass("active").siblings().removeClass("active");
                    historyReport.cacheSearch();
                    //$("#startDate").val(startDate);
                    //$('#endDate').val(endDate);
                });
                //搜索按钮
                $("#searchBtn").on("click",function(){
                    $17.tongji("互动课堂-课堂提问-我的报告-搜索");
                    historyReport.startDate = $("#startDate").val();
                    historyReport.endDate = $('#endDate').val();
                    historyReport.currentTab = 'query_list';
                    historyReport.currentEle = $(this);
                    if($17.DateDiff(historyReport.startDate, historyReport.endDate,"d") < 0){
                        $17.alert("结束时间不能小于开始时间");
                        return false;
                    }
                    historyReport.cacheSearch();
                    return true;
                });

                //奖励学生
                $("i.js-reward-bean").die().live('click', function(){
                    $17.tongji('互动课堂-课堂提问-我的报告-奖励学豆');
                    var $this = $(this);
                    if(!$this.closest("li").hasClass("green")){
                        return false;
                    }
                    if($("#content_list_box").isFreezing()){
                        return false;
                    }
                    var rightStudents = $.evalJSON($.trim($this.closest("li").siblings("li.rightstudents").html()));
                    var rewardNames = [];
                    var rewardStudentIds = [];
                    if(rightStudents.length > 0){
                        var postfix = "";
                        if(rightStudents.length > 5){
                            postfix = "等" +　rightStudents.length + "名同学";
                        }
                        for(var j = 0; j < rightStudents.length; j++){
                            if(j < 5){
                                rewardNames.push(rightStudents[j].studentName);
                            }
                            rewardStudentIds.push(rightStudents[j].studentId);
                        }
                    }else{
                        rewardNames = "暂没有奖励的名单,请稍候再试";
                    }
                    $.prompt(template("t:rewardstudent",{rewardNames : rewardNames.toString(), studentCnt : rightStudents.length}),{
                        title : "提示",
                        buttons : {"取消" : false , "确定" : true },
                        submit : function(e,v,m,f){
                            if(v && rewardStudentIds.length > 0){
                                $17.tongji("互动课堂-课堂提问-我的报告-奖励学豆-确定");
                                $("#content_list_box").freezing();
                                var paramData = {
                                    userIds     : rewardStudentIds.toString(),
                                    clazzId     : ${clazz.id},
                                    integralCnt : $('#tempNumBox').val() * 1,
                                    rewardItem  : "ANSWER_BEST",
                                    subject     : "${curSubject!}"
                                };

                                App.postJSON('/teacher/smartclazz/updaterewardintegral.vpage', paramData, function(data){
                                    $17.alert(data.info);
                                    $this.removeClass('loading');
                                    $("#content_list_box").thaw();
                                });

                            }else{
                                $.prompt.close();
                            }
                        },
                        loaded : function(){
                            /*输入框只能输入数字*/
                            $('input[id="tempNumBox"]').keyup(function(){
                                if (/\D/g.test(this.value)){
                                    this.value = this.value.replace(/\D/g, '');
                                }
                            });
                        }
                    });

                });

                //减学豆
                $(".minusBtn").live('click', function(){
                    var $that = $(this);
                    var tempNum = $that.siblings('.tempNum');
                    var tempNumVal = tempNum.val() * 1;

                    tempNum.attr("value", checkNum(tempNumVal,'minus'));
                    $that.siblings().closest('a.plusBtn').removeClass("btn_disable");
                    //园丁豆学豆计数器
                    changeSilver($that);

                });

                //加学豆
                $(".plusBtn").live('click', function(){
                    var $that = $(this);
                    var tempNum = $that.siblings('.tempNum');
                    var tempNumVal = tempNum.val() * 1;

                    tempNum.attr("value", checkNum(tempNumVal,'plus'));
                    $that.siblings().closest('a.minusBtn').removeClass("btn_disable");
                    //园丁豆学豆计数器
                    changeSilver($that);
                });
            }
        };
        historyReport.init();
    });


</script>

</@temp.pagecontent>