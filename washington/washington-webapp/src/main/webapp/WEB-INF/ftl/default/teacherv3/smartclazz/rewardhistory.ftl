<#import "module.ftl" as temp />
<@temp.pagecontent mainmenu="rewardhistory_reward" submenu="rewardhistory">
    <@sugar.capsule js=["datepicker"] css=["plugin.datepicker"] />
    <div class="s-dateTip-box" style="margin-top: 20px;">
        <ul class="s-date-box s-fl-left">
            <li><a data-tab="today" data-tab_name="今天" data-start_date="${todayStartDate!''}" data-end_date="${todayEndDate!''}" href="javascript:void(0);" class="rewardListBut active date">今天</a></li>
            <li><a data-tab="yesterday" data-tab_name="昨天" data-start_date="${yesterdayStartDate!''}" data-end_date="${yesterdayEndDate!''}" href="javascript:void(0);" class="rewardListBut active date">昨天</a></li>
            <li><a data-tab="currentWeek" data-tab_name="本周" data-start_date="${currentWeekStartDate!''}" data-end_date="${currentWeekEndDate!''}" href="javascript:void(0);" class="rewardListBut active date">本周</a></li>
            <li><a data-tab="currentMonth" data-tab_name="本月" data-start_date="${currentMonthStartDate!''}" data-end_date="${currentMonthEndDate!''}" href="javascript:void(0);" class="rewardListBut active date">本月</a></li>
            <li><input type="text" readonly="readonly" value="${currentWeekStartDate!''}" title="起始时间" id="startDate" class="time"></li>
            <li> <span style="width: 5px; margin:9px 15px 5px 5px; float: left">至</span></li>
            <li> <input type="text" readonly="readonly" value="${currentWeekEndDate!''}" title="结束时间" id="endDate" class="time"></li>
            <li> <a data-tab="query_list" href="javascript:void(0);" id="query_but" class="time search">搜索</a></li>
            <li class="s-clear"></li>
        </ul>
        <a class="s-fl-right s-magT-10" href="/teacher/smartclazz/clazzdetail.vpage?clazzId=${clazz.id}">返回</a>
        <div class="s-clear"></div>
    </div>
    <#-- 发放记录列表 -->
    <div style="border: 1px solid #ccc; margin: 0 0 20px; padding: 20px 0 0;">
        <div id="content_list_box">
            <div class="today" style="display: none"></div>
            <div class="yesterday" style="display: none"></div>
            <div class="currentWeek"></div>
            <div class="currentMonth" style="display: none"></div>
            <div class="query_list" style="display: none"></div>
        </div>
        <div class="w-clear"></div>
    </div>
    <script type="text/javascript">

        function getRewardList($this,startDate,endDate,tab,tabName){
            //已加载过的页面 直接显示(搜索除外)
            if($this.hasClass('hasContent') && tab != 'query_list'){
                $('.'+ tab).show().siblings().hide();
                return false;
            }

            if($this.hasClass("loading")){return false;}
            $this.addClass("loading");
            $('.'+tab).html('<div class="text_center" style="padding: 50px 0;"><img class="throbber" src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>').show().siblings().hide();

            $.post('/teacher/smartclazz/rewardhistory.vpage',{startDate: startDate , endDate : endDate , clazzId : "${(clazz.id)!'0'}", tabName : tabName},function(data){
                $('.'+tab).html(data).show().siblings().hide();
                $('a[data-tab='+ tab +']').addClass('hasContent');
                $this.removeClass("loading");

                //查看全部
                $('.'+tab).find('.viewMoreInfo').on("click",function(){
                    var $this = $(this);
                    var refId = $this.attr("ref-id");
                    if($17.isBlank(refId)){
                      return false;
                    }
                    $('.'+tab).find(refId).removeClass("smart_scroll");
                    $this.hide();
                });
            });

        }

        $(function(){
            $17.tongji("课堂-发放记录");
            //默认获取本周数据
            getRewardList($(".rewardListBut"),"${currentWeekStartDate!''}","${currentWeekEndDate!''}",'currentWeek','本周');

            //今天 昨天 本周 本月
            $(".rewardListBut").on('click', function(){
                var $this = $(this);
                $17.tongji("课堂-发放记录-" + $this.data("tab_name"));
                var startDate = $this.data('start_date');
                var endDate = $this.data('end_date');
                var tab = $this.data('tab');
                $this.parent().addClass("active").siblings().removeClass("active");
                getRewardList($this,startDate, endDate,tab,$this.data("tab_name"));
                $("#startDate").val(startDate);
                $('#endDate').val(endDate);
            });

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

            //搜索
            $("#query_but").on('click', function(){
                $17.tongji('老师-课堂-发放记录-搜索');
                var st = $("#startDate").val();
                var et = $('#endDate').val();
                if($17.DateDiff(st,et,"d") < 0){
                    $17.alert("结束时间不能小于开始时间");
                    return false;
                }
                getRewardList($(this),st, et,'query_list','本时间段');
                //清除按钮选中状态
                $(".rewardListBut").parent().removeClass("active");
            });
        });
    </script>
</@temp.pagecontent>