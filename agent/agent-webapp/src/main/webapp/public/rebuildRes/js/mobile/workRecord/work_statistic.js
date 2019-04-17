

    //重组数据
    var dataMap = {
        day:{
            currentDate:new Date(),
            data:{}
        },
        week:{
            currentDate:new Date(),
            data:{}
        },
        month:{
            currentDate:new Date(),
            data:{}
        }
    };
    template.helper('Math', Math);
    var showPieEcharts = false;
    var callBack = function (data,requestAjax) {
        if(requestAjax || false){
            $(".statistic.active").removeClass("requestAjax");
        }
        $(".statistic_date").html(template("statistic_date", data));
        $(".js-chooseRole div.active").click();
        checkDateFn();
        showEchartsFn(showPieEcharts,ajaxUrl);
    };
    var showEchartsFn = function (showPieEcharts,ajaxUrl) {
        if(showPieEcharts && ajaxUrl == "summary.vpage" && isAgent){
            showPieEcharts = false ;
            var pieData1 = dataMap["day"].data.workRecordStatisticsSummary.fillInWorkRecordUserCount || 0;
            var pieData2 = dataMap["day"].data.workRecordStatisticsSummary.groupUserCount || 0;
            var pieData = pieData1 + "/" + pieData2 + "\r\n" + "已录入/应录入";
            callBachEcharts(pieData1,pieData2-pieData1,pieData);
        }
    };
    //获取ajax数据
    var desc = function(x,y){
        var colId="workload";
        return (x[colId] < y[colId]) ? 1 : -1
    };
    var inschool_desc = function(x,y){
        var colId="perCapitaIntoSchool";
        return (x[colId] < y[colId]) ? 1 : -1
    };
    var getSummary = function (ajaxUrl,dataJson,info,callBack,requestAjax) {
        $.get(ajaxUrl,dataJson,function (res) {
            if(res.success){
                if(ajaxUrl != 'statistics_detai.vpage'){
                    if(ajaxUrl == 'into_school.vpage'){
                        for(var i in res){
                            if(is_array(res[i]) && res[i].length > 0){
                                res[i] = res[i].sort(inschool_desc);
                            }
                        }
                    }else{
                        for(var i in res){
                            if(is_array(res[i]) && res[i].length > 0){
                                res[i] = res[i].sort(desc);
                            }
                        }
                    }
                }
                dataMap[info].data  = res ;
                callBack({res: dataMap},requestAjax);
            }else{
                if(requestAjax || false){
                    $(".statistic.active").addClass("requestAjax");
                }
                AT.alert(res.info);
            }
        })
    };
    $(document).on("click",".statistic",function () {
        var _this = $(this),
            _dateType = $(this).data("type");
        if(_dateType == 1){
            showPieEcharts = true;
        }else{
            showPieEcharts = false;
        }
        _this.addClass("active").siblings().removeClass("active");
        dataMap.dateType = _this.data("info");
        var date = dataMap[dataMap.dateType].currentDate.Format("yyyyMMdd");
        if(_this.hasClass("requestAjax")){
            var requestAjax = true;
            var dataJson = {
                dateType:_dateType,
                date:date,
                userId:getUrlParam('userId'),
                groupId:getUrlParam('groupId')
            };
            getSummary(ajaxUrl,dataJson,dataMap.dateType,callBack,requestAjax);
        }else{
            $(".statistic_date").html(template("statistic_date", {res: dataMap}));
            checkDateFn();
            showEchartsFn(showPieEcharts,ajaxUrl);
            $(".js-chooseRole div.active").click();
        }
        if(_dateType == 0){

        }
    });
    $('.statistic.active').click();
    $(document).on("click",".js-next",function () {
        var _info = $(this).data("info");
        var dateType = $(this).data("type");
        if(_info == "day"){
            dataMap["day"].currentDate = new Date(dataMap["day"].currentDate.getTime() + 24*60*60*1000);
        }else if(_info == "week"){
            dataMap["week"].currentDate = new Date(dataMap["week"].currentDate.getTime() + 24*60*60*1000*7);
        }else if(_info == "month"){
            var getRestDays = getRestOfMonthDay (dataMap["month"].currentDate);
            dataMap["month"].currentDate = new Date(dataMap["month"].currentDate.getTime() + 24*60*60*1000*(getRestDays+1));
        }
        var date = dataMap[_info].currentDate.Format("yyyyMMdd");
        var dataJson = {
            dateType:dateType,
            date:date,
            userId:getUrlParam('userId'),
            groupId:getUrlParam('groupId')
        };
        getSummary(ajaxUrl,dataJson,dataMap.dateType,callBack);
    });
    $(document).on("click",".js-prev",function () {
        var _info = $(this).data("info");
        var dateType = $(this).data("type");
        if(_info == "day"){
            dataMap[_info].currentDate = new Date(dataMap[_info].currentDate.getTime() - 24*60*60*1000);
        }else if(_info == "week"){
            dataMap[_info].currentDate = new Date(dataMap[_info].currentDate.getTime() - 24*60*60*1000*7);
        }else if(_info == "month"){
            dataMap[_info].currentDate = new Date(dataMap[_info].currentDate.getTime() - (dataMap[_info].currentDate.getDate()+1)*24*60*60*1000);
        }
        var date = dataMap[_info].currentDate.Format("yyyyMMdd");
        var dataJson = {
            dateType:dateType,
            date:date,
            userId:getUrlParam('userId'),
            groupId:getUrlParam('groupId')
        };
        getSummary(ajaxUrl,dataJson,dataMap.dateType,callBack);
    });
    var team_workload_summary = function (_this) {
        // window.location.href = "team_workload_list.vpage?groupId=" + $(_this).data("id");
        openSecond("/mobile/work_record/statistics/team_workload_list.vpage?groupId=" + $(_this).data("id"))
    };
    var into_school_list = function (_this) {
        // window.location.href = "into_school_list.vpage?groupId=" + $(_this).data("id");
        openSecond("/mobile/work_record/statistics/into_school_list.vpage?groupId=" + $(_this).data("id"))
    };
    $(document).on("click",".js_showStatistics",function () {
       openSecond($(this).data("url"));
    });
    $(document).on("click",".js-chooseRole div",function(){
        $(this).addClass("active").siblings().removeClass("active");
        $("."+$(this).data("info")).show().siblings().hide();
    });
    var callBachEcharts = function (pieData1,pieData2,pieData) {
        var myChart = echarts.init(document.getElementById("workSituationChart"));
        var option = {
            series: [
                {
                    type:'pie',
                    radius: ['68%', '80%'],
                    avoidLabelOverlap: false,
                    tooltip : {
                        enterable:false
                    },
                    label: {
                        normal: {
                            show: true,
                            position: 'center',
                            testStyle:{
                                color:"black"
                            }
                        },
                        emphasis: {
                            show: true,
                            textStyle: {
                                fontSize: '30',
                                fontWeight: 'bold'
                            }
                        }
                    },
                    labelLine: {
                        normal: {
                            show: false
                        }
                    },
                    data:[
                        {value:pieData1,itemStyle:{normal:{color:'#ff7d5a'}}},
                        {value:pieData2, name:pieData,itemStyle:{normal:{color:'#636880'}}}
                    ]
                }
            ]
        };
        myChart.setOption(option);
    };

    var checkDateFn = function () {
        var dateTime = dataMap[$(".statistic.active").data("info")].currentDate.Format("yyyyMMdd");
        var nowDate = new Date().Format("yyyyMMdd");
        if(dateTime >= nowDate){
            $(".next").hide();
        }else{
            $(".next").show();
        }
    };
