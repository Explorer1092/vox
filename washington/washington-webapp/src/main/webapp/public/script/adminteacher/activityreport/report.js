/**
 * @author:
 * @description: "一起测教研员报告"
 * @createdDate: 2018/9/4
 * @lastModifyDate: 2018/9/4
 */
// YQ: 通用方法(public/script/YQ.js), impromptu: 通用弹窗（http://trentrichardson.com/Impromptu/）, voxLogs: 打点
define(["jquery", "vue", "YQ", "echarts", "html2canvas","echarts-report", "echarts-adminteacher", "impromptu", "voxLogs"],function($, Vue, YQ, echarts, html2canvas) {
    // 注：该项目无编译机制，不要使用es6语法
    var databaseLogs = "tianshu_logs"; // 打点的表
    var EChartsTheme = 'walden';// echart配色主题，假设配色文件为echarts-adminteacher
    var downloadPdfFlag = false; // 下载padf flag（防止下载中再触发）
    var moduleName = "m_HxGe3lFlEX";
    var breakupDownloadNum = 2; // 下载报告拆分（15个一组传给后端）
    // 获取链接参数
    /*
     * 简单弹窗提示
     */
    var alertTip = function (content, title, callback) {
        var title = title || '系统提示';
        $.prompt(content, {
            title: title,
            buttons: {'确定': true},
            focus : 0,
            position: {width: 500},
            submit : function(e, v){
                if(v){
                    e.preventDefault();
                    if (callback) {
                        callback();
                    } else {
                        $.prompt.close();
                    }
                }
            }
        });
    };
    var vum= new Vue({
        el: '#activityReport',
        data: {
            noData:'',
            examFullName:YQ.getQuery('reportType')||'',
            reportName:YQ.getQuery('reportType'),
            reportTitle:[],
            navi:false,
            downloadPop:false,
            base64ObjectArr:[],
            main01:{},
            main02:{},
            main02Grid:{},
            main02barMaps:[],
            main02WholeScoreMap:{},
            main03Grid:{},
            main03barMaps:[],
            main03WholeGrid:{},
            main03pieMap:{},
            main03:{},
            main04:{},
            main04WholeGrid:{},
            main04Grid:{},
            main04barMaps:[],
            main04wholeBarMap:{},
            main04lastOneData:{},
            main04topOneData:{},
            grid:{},
            loadingPopUp:false,
            downloadPdfFlag: false,
            barMaps:[],
            activityType:'',
            noData4:true,
            noData3:true,
            noData2:true,
            noData1:true,
            breakupNum:40,
        },
        updated:function (){
            this.pagenum();
            // this.scoreBarGraph(this.main02barMaps);
            this.studyBarGraph(this.main03barMaps);
            this.pieBarGraph(this.main03pieMap);
            this.answerSpeedEcharts(this.main04barMaps)
            this.answerGridEcharts(this.main04wholeBarMap)
        },
        mounted: function () {
            var _this=this
            _this.reportName=YQ.getQuery('reportType')
        },
        created:function(){
            var _this=this
            _this.loadActivityReportSurvey()
            _this.loadActivityAnswerSpeed()
        },
        methods: {
            // 页码
            pagenum:function(){
                for(var i=0;i<$('.loadSection').length;i++){
                    if($('.downloadInner').children('.loadSection').eq(i).find('.num').length>0){
                        $('.downloadInner').children('.loadSection').eq(i).find('.num')[0].innerHTML=i
                    }
                }
            },
            //1.参与概况
            loadActivityReportSurvey:function(){
                var vm = this;
                $.ajax({
                    url: '/schoolmaster/activityReport/loadActivityReportSurvey.vpage',
                    type: 'GET',
                    data: {
                        regionLevel: YQ.getQuery('regionLevel'),
                        regionCode: YQ.getQuery('regionCode'),
                        id: YQ.getQuery('id'),
                    },
                    success: function (res) {
                        if(res.result ===false){
                            // vm.alertError(res.info);
                            vm.noData1===false
                        }
                        vm.main01=res
                        vm.grid=res.grid
                        vm.barMaps=res.barMaps
                        YQ.voxLogs({
                            database: databaseLogs,
                            module: moduleName,
                            op: "activity_detail_read",
                            s0: window.idType === 'schoolmaster' ? "校长" : "教研员",
                            s1: '', // 页面停留时间，不计
                            s2: YQ.getQuery('reportType'), // 年级
                            s3: '' // 学科
                        });
                    },
                    error: function () {
                        vm.noData1=false
                    },
                    complete: function () {
                        vm.loadActivityScoreState()
                    }
                })
            },
            // 2,活动得分状况
            loadActivityScoreState:function(){
                var vm = this;
                $.ajax({
                    url: '/schoolmaster/activityReport/loadActivityScoreState.vpage',
                    type: 'GET',
                    data: {
                        regionLevel: YQ.getQuery('regionLevel'),
                        regionCode: YQ.getQuery('regionCode'),
                        id: YQ.getQuery('id')
                    },
                    success: function (res) {
                        if(res.result === false){
                            vm.noData2=false
                        }
                        vm.main02 = res
                        vm.main02Grid = res.grid;
                        vm.main02barMaps = res.barMaps;
                        vm.main02WholeScoreMap=res.wholeScoreMap;
                    },
                    error: function () {
                        vm.noData2=false
                    },
                    complete: function () {
                        vm.barGraph(vm.barMaps)
                        vm.scoreBarGraph(vm.main02barMaps)
                        vm.loadActivityScoreLevel()
                    }
                })
            },
            //3,成绩分布
            loadActivityScoreLevel:function(){
                var vm = this;
                $.ajax({
                    url: '/schoolmaster/activityReport/loadActivityScoreLevel.vpage',
                    type: 'GET',
                    // async: false,
                    data: {
                        regionLevel: YQ.getQuery('regionLevel'),
                        regionCode: YQ.getQuery('regionCode'),
                        id: YQ.getQuery('id'),
                    },
                    success: function (res) {
                        if(res.result = false){
                            vm.noData3=false
                        }
                        if(res.barMaps!=undefined){
                            for(var i=0;i<res.barMaps.length;i++){
                                vm.main03barMaps=res.barMaps
                            }
                        }
                        vm.main03=res;
                        vm.main03Grid = res.grid;
                        vm.main03WholeGrid = res.wholeGrid;
                        vm.main03barMaps=res.barMaps
                        vm.main03pieMap=res.pieMap;
                    },
                    error: function () {
                        vm.noData3=false
                    },
                    complete: function (){
                        vm.scorePieBarGraph(vm.main02WholeScoreMap)
                        vm.scoreBarGraph(vm.main02barMaps)
                        vm.pieBarGraph(vm.main03pieMap)
                        vm.loadActivityAnswerSpeed()
                    }
                })
            },
            // 4,答题速度
            loadActivityAnswerSpeed:function(){
                var vm = this;
                $.ajax({
                    url: '/schoolmaster/activityReport/loadActivityAnswerSpeed.vpage',
                    type: 'GET',
                    data: {
                        regionLevel: YQ.getQuery('regionLevel'),
                        regionCode: YQ.getQuery('regionCode'),
                        id: YQ.getQuery('id')
                    },
                    success: function (res) {
                        if(res.result==false){
                            vm.noData4=false
                        }else{
                            vm.main04=res
                            vm.main04WholeGrid=res.wholeGrid
                            vm.main04lastOneData=res.wholeGrid.lastOneData
                            vm.main04topOneData=res.wholeGrid.topOneData
                            vm.main04Grid = res.grid2;
                            vm.main04barMaps=res.barMaps
                            vm.answerGridEcharts(res.wholeBarMap)
                            vm.main04wholeBarMap=res.wholeBarMap
                        }
                    },
                    error: function () {
                        vm.noData4=false
                    },
                    complete: function (){
                        // vm.answerSpeedEcharts(vm.main04barMaps)
                        vm.answerGridEcharts(vm.main04wholeBarMap)
                        vm.answerSpeedEcharts(vm.main04barMaps)
                    }
                })
            },
            //参与概况柱状图
            barGraph:function(res){
                var vm=this
                var joinBarMaps=res
                if(joinBarMaps!=undefined) {

                    for (var b = 0; b < joinBarMaps.length; b++) {
                        // 对于图表按40为一组拆分
                        if(joinBarMaps[b].length!=undefined){
                        }
                        var echart_num = Math.ceil(joinBarMaps[b].seriesData.length / vm.breakupNum); // 以wholeScoreDetail字段做拆分，40个为一组
                        // clone节点
                        for (var i = 0; i < echart_num; i++) {
                            if (i > 0) {
                                $('.echarts0000'+b).eq($('.echarts0000'+b).length - 1).after($('.echarts0000'+b).eq(0).clone(true));
                            }
                        }
                    }
                    for (var b = 0; b < joinBarMaps.length; b++) {
                        var xAxisEcharts={name:[],no:[]};
                        for(var h=0;h<joinBarMaps[b].xAxisData.length;h++){
                            xAxisEcharts.name.push(joinBarMaps[b].xAxisData[h].name)
                            xAxisEcharts.no.push(joinBarMaps[b].xAxisData[h].no)
                        }
                        var echart_num = Math.ceil(joinBarMaps[b].seriesData.length / vm.breakupNum); // 以wholeScoreDetail字段做拆分，40个为一组
                        for(var j=0;j<echart_num;j++){
                            var _className = document.getElementsByClassName('echarts0000'+b)[j];
                            if(_className!=null && joinBarMaps[b].xAxisData.length>0){
                                var myChart = echarts.init(_className, EChartsTheme, {renderer: 'svg'});
                                var a=joinBarMaps[b].xAxisData
                                // var yMax= Math.max(joinBarMaps[i].seriesData)
                                // 使用刚指定的配置项和数据显示图表。
                                myChart.setOption({
                                    color:'#1B92FF',
                                    title: {
                                        text: joinBarMaps[b].title,
                                        left:'center'
                                    },
                                    legend: {
                                        show:true,
                                        bottom:'-5',
                                        selectedMode: false,
                                        data:[
                                            {
                                                name:joinBarMaps[b].legendData[1],
                                                icon: 'rect',
                                            },
                                            {
                                                name: joinBarMaps[b].legendData[0] ,
                                                icon:'image://../../../../public/resource/activity/images/drabs.png'
                                            }
                                        ]
                                    },
                                    tooltip: {
                                        // type:'value',
                                        trigger: 'axis',
                                        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                                            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                                        },
                                        formatter: function (params) {
                                            var xName=params[0].seriesName.split(',');
                                            return  xName[params[0].dataIndex] + '的人均参与次数:' +  params[0].value + '次/人';
                                        }


                                    },
                                    xAxis: {
                                        data:xAxisEcharts.no.slice(j * vm.breakupNum, (j + 1) * vm.breakupNum)
                                    },
                                    yAxis: {

                                    },
                                    series: [
                                        {
                                            name: xAxisEcharts.name.slice(j * vm.breakupNum, (j + 1) * vm.breakupNum),
                                            type: 'bar',
                                            barMaxWidth: 20,
                                            // barWidth : 20,
                                            label: {
                                                normal: {
                                                    // show: res.gridDataList.length <= 10 ? true : false, // 10个以内的数据才展示顶部标识
                                                    formatter: '{b}%', // a表示系列名, b表示数据名, c表示数据值
                                                    position: 'top'
                                                }
                                            },
                                            markLine: {
                                                symbol: 'none', // 两端的形状
                                                lineStyle: {
                                                    normal: {
                                                        type: 'circle', // 圆虚线
                                                        width: 2, // 线宽
                                                        color: '#32C35D'
                                                    }
                                                },
                                                data: [
                                                    // {type : 'average', name: '平均值'}, // 取平均值
                                                    {
                                                        yAxis: joinBarMaps[b].wholeAvgNums,// 指定线的值
                                                        label: {
                                                            normal: {
                                                                formatter:'{c}'+ joinBarMaps[b].unit
                                                            }
                                                        }
                                                    }
                                                ]
                                            },
                                            data: joinBarMaps[b].seriesData.slice(j * vm.breakupNum, (j + 1) * vm.breakupNum),

                                        },
                                        {
                                            name: joinBarMaps[b].legendData[1],
                                            type: 'line',

                                        },
                                        {
                                            name: joinBarMaps[b].legendData[0],
                                            type: 'line',
                                        }

                                    ]
                                });
                            }
                        }
                    }
                }
            },
            //得分状况饼图
            scorePieBarGraph:function(res){
                if(res != undefined){
                    var _className = document.getElementsByClassName('scorePieEchars')[0];
                    if(_className != null){
                        // debugger
                        var myChart = echarts.init(_className, EChartsTheme);
                        myChart.setOption({
                            series: [
                                {
                                    name:'得分率',
                                    type:'pie',
                                    center: ['50%', '50%'],
                                    radius: [72, 92], // 半径
                                    avoidLabelOverlap: false,
                                    clockwise: false, // 是否顺时针
                                    // silent: true, // 是否不响应鼠标
                                    hoverAnimation: false, // hover 动画
                                    label: {
                                        normal: { // 默认提示
                                            show: true,
                                            position: 'center',
                                            textStyle: {
                                                fontSize: '20',
                                                fontWeight: 'bold',
                                            },
                                            formatter: function (params) {
                                                // '{a|这段文本采用样式a}',
                                                // '{b|这段文本采用样式b}这段用默认样式{x|这段用样式x}'
                                                return params.name === '得分率' ? ('{a|' + params.name + '}\n\n{b|' + params.value+ '}{c|' + '%}')  : '';
                                            },
                                            rich: {
                                                a: {
                                                    fontSize: 18,
                                                    color: '#555555'
                                                },
                                                b: {
                                                    fontSize: 40,
                                                    fontWeight: 'bold',
                                                    color: '#FBCD17'
                                                },
                                                c: {
                                                    fontSize: 20,
                                                    color: '#555555',
                                                    verticalAlign: 'bottom',
                                                    padding: [5, 0]
                                                }
                                            }
                                        }
                                    },
                                    labelLine: {
                                        normal: {
                                            show: false
                                        }
                                    },
                                    data:[
                                        {
                                            value: res.wholeScoreRate,
                                            name:'得分率',
                                            itemStyle: {
                                                color: '#FBCD17'
                                            }
                                        },
                                        {
                                            value: 100 - res.wholeScoreRate,
                                            name:'无',
                                            itemStyle: {
                                                normal: {
                                                    color: '#f0f2f5' // 正常颜色
                                                },
                                                emphasis: {
                                                    color: '#f0f2f5' // 鼠标滑过颜色
                                                }
                                            }
                                        }
                                    ]
                                }
                            ]
                        });
                    }
                }
            },
            //得分状况柱状图
            scoreBarGraph:function(res){
                var vm=this
                var scoreBarMaps=res
                if(scoreBarMaps != undefined){
                    if(scoreBarMaps.length>0) {
                        // 先循环创建domo
                        for (var s = 0; s < scoreBarMaps.length; s++) {
                            var echart_num = Math.ceil( scoreBarMaps[s].seriesData.length / vm.breakupNum); // 以wholeScoreDetail字段做拆分，40个为一组
                            // clone节点
                            for (var i = 0; i < echart_num; i++) {
                                if (i > 0) { // 从第二个40项开始，clone节点并插入兄弟节点的后面
                                    $('.scoreEcharts' + s).eq($('.scoreEcharts' + s).length - 1).after($('.scoreEcharts' + s).eq(0).clone(true));
                                }
                            }
                        }
                        // 再循环绘制canvas
                        for (var s = 0; s < scoreBarMaps.length; s++) {
                            var xAxisEcharts={name:[],no:[]};
                            for(var h=0;h<scoreBarMaps[s].xAxisData.length;h++){
                                xAxisEcharts.name.push(scoreBarMaps[s].xAxisData[h].name)
                                xAxisEcharts.no.push(scoreBarMaps[s].xAxisData[h].no)
                            }
                            var echart_num = Math.ceil( scoreBarMaps[s].seriesData.length / vm.breakupNum); // 以wholeScoreDetail字段做拆分，40个为一组
                            for(var j=0;j<echart_num;j++){
                                var _className = document.getElementsByClassName('scoreEcharts' + s)[j];
                                if(_className != null&&scoreBarMaps[s].xAxisData.length>0) {
                                    var myChart = echarts.init(_className, EChartsTheme, {renderer: 'svg'});
                                    // 使用刚指定的配置项和数据显示图表。
                                    myChart.setOption({
                                        color: '#1B92FF',
                                        title: {
                                            text: scoreBarMaps[s].title,
                                            left: 'center'
                                        },
                                        tooltip: {
                                            trigger: 'axis',
                                            axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                                                type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                                            },
                                            formatter: function (params) {
                                                var xName=params[0].seriesName.split(',');
                                                return xName[params[0].dataIndex] +'的最高分平均分:' +  params[0].value +'分';
                                            }
                                        },
                                        legend: {
                                            align: 'left',
                                            bottom: '-5',
                                            data:[
                                                {
                                                    name:scoreBarMaps[s].legendData[1],
                                                    icon: 'rect',
                                                },
                                                {
                                                    name: scoreBarMaps[s].legendData[0] ,
                                                    icon:'image://../../../../public/resource/activity/images/drabs.png'
                                                }
                                            ]
                                        },
                                        xAxis: {
                                            data: xAxisEcharts.no.slice(j * vm.breakupNum, (j + 1) * vm.breakupNum)
                                        },
                                        yAxis: {},
                                        series: [
                                            {
                                                name: xAxisEcharts.name.slice(j * vm.breakupNum, (j + 1) * vm.breakupNum),
                                                type: 'bar',
                                                barMaxWidth: 20,
                                                label: {
                                                    normal: {
                                                        // show: res.gridDataList.length <= 10 ? true : false, // 10个以内的数据才展示顶部标识
                                                        formatter: '{c}%', // a表示系列名, b表示数据名, c表示数据值
                                                        position: 'top'
                                                    }
                                                },
                                                markLine: {
                                                    symbol: 'none', // 两端的形状
                                                    lineStyle: {
                                                        normal: {
                                                            type: 'circle', // 圆虚线
                                                            width: 2, // 线宽
                                                            color: '#32C35D'
                                                        }
                                                    },
                                                    data: [
                                                        // {type: 'average', name: '平均值'}, // 取平均值
                                                        {
                                                            yAxis: scoreBarMaps[s].wholeAvgNums,// 指定线的值
                                                            label: {
                                                                normal: {
                                                                    formatter:'{c}'+ scoreBarMaps[s].unit
                                                                }
                                                            }
                                                        }
                                                    ]
                                                },
                                                data: scoreBarMaps[s].seriesData.slice(j * vm.breakupNum, (j + 1) * vm.breakupNum),

                                            },
                                            {
                                                name: scoreBarMaps[s].legendData[1],
                                                type: 'line',
                                            },
                                            {
                                                name: scoreBarMaps[s].legendData[0],
                                                type: 'line',
                                            }

                                        ]
                                    });
                                }
                            }
                        }
                    }
                }
            },
            // 成绩分布饼图
            pieBarGraph:function(res){
                var _className = document.getElementsByClassName('pieEcharts')[0];
                var pieSeriesData = [];
                if(res!=undefined){
                    if(_className != null){
                        if(res.pieSeriesData!=undefined){
                            for (var i = 0; i < res.pieSeriesData.length; i++) {
                                pieSeriesData.push({
                                    name: res.pieSeriesData[i].name + ': \n' + res.pieSeriesData[i].value+'%',
                                    value: res.pieSeriesData[i].value,
                                });
                            }

                            var myChart = echarts.init(_className,EChartsTheme,{renderer: 'svg'});
                            myChart.setOption({
                                tooltip : {
                                    trigger: 'item',
                                    formatter: function (params) {
                                        return params.name;
                                    }
                                },
                                series : [
                                    {
                                        type: 'pie',
                                        radius : '55%',
                                        center: ['50%', '50%'],
                                        data: pieSeriesData,
                                        itemStyle: {
                                            emphasis: {
                                                shadowBlur: 10,
                                                shadowOffsetX: 0,
                                                shadowColor: 'rgba(0, 0, 0, 0.5)'
                                            }
                                        }
                                    }
                                ]
                            });
                        }
                    }
                }
            },
            // 成绩分布柱状图
            studyBarGraph:function(res){
                var vm=this
                var studyBarMaps=res
                if(studyBarMaps != undefined&&studyBarMaps.length!=undefined){
                    if(studyBarMaps.length>0) {
                        for (var t = 0; t < studyBarMaps.length; t++) {
                            if(studyBarMaps[t].seriesData!=undefined){
                                var echart_num = Math.ceil( studyBarMaps[t].seriesData.length / vm.breakupNum); // 以wholeScoreDetail字段做拆分，40个为一组
                                // clone节点
                                for (var i = 0; i < echart_num; i++) {
                                    if (i > 0) { // 从第二个40项开始，clone节点并插入兄弟节点的后面
                                        $('.studybarEcharts'+t).eq($('.studybarEcharts'+t).length - 1).after($('.studybarEcharts'+t).eq(0).clone(true));
                                    }
                                }
                            }

                        }
                        for(var t = 0; t < studyBarMaps.length; t++){
                            if(studyBarMaps[t].seriesData!=undefined){
                                var echart_num = Math.ceil(studyBarMaps[t].seriesData.length / vm.breakupNum); // 以wholeScoreDetail字段做拆分，40个为一组

                                for (var j = 0; j < echart_num; j++) {
                                    var xAxisEcharts={name:[],no:[]};
                                    for(var h=0;h<studyBarMaps[t].xAxisData.length;h++){
                                        xAxisEcharts.name.push(studyBarMaps[t].xAxisData[h].name)
                                        xAxisEcharts.no.push(studyBarMaps[t].xAxisData[h].no)
                                    }
                                    var _className = document.getElementsByClassName('studybarEcharts' + t)[j];
                                    if (_className != null && studyBarMaps[t].xAxisData.length > 0) {
                                        var myChart = echarts.init(_className, EChartsTheme, {renderer: 'svg'});
                                        // 使用刚指定的配置项和数据显示图表。
                                        myChart.setOption({
                                            color: '#1B92FF',
                                            title: {
                                                text: studyBarMaps[t].title,
                                                left: 'center'
                                            },
                                            tooltip: {
                                                trigger: 'axis',
                                                axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                                                    type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                                                },
                                                formatter: function (params) {
                                                    var xName=params[0].seriesName.split(',');
                                                    return xName[params[0].dataIndex] +'的高分人数占比:' + params[0].value + '%';
                                                }
                                            },
                                            legend: {
                                                align: 'left',
                                                bottom: '-5',
                                                data: [
                                                    {
                                                        name: studyBarMaps[t].legendData[1],
                                                        icon: 'rect',
                                                    },
                                                    {
                                                        name: studyBarMaps[t].legendData[0],
                                                        icon: 'image://../../../../public/resource/activity/images/drabs.png'
                                                    }
                                                ]
                                            },
                                            xAxis: {
                                                data: xAxisEcharts.no.slice(j * vm.breakupNum, (j + 1) * vm.breakupNum)
                                            },
                                            yAxis: {},
                                            series: [
                                                {
                                                    name: xAxisEcharts.name.slice(j * vm.breakupNum, (j + 1) * vm.breakupNum),
                                                    type: 'bar',
                                                    barMaxWidth: 20,
                                                    // barWidth : 20,
                                                    label: {
                                                        normal: {
                                                            // show: res.gridDataList.length <= 10 ? true : false, // 10个以内的数据才展示顶部标识
                                                            formatter: '{c}%', // a表示系列名, b表示数据名, c表示数据值
                                                            position: 'top'
                                                        }
                                                    },
                                                    markLine: {
                                                        symbol: 'none', // 两端的形状
                                                        lineStyle: {
                                                            normal: {
                                                                type: 'circle', // 圆虚线
                                                                width: 2, // 线宽
                                                                color: '#32C35D'
                                                            }
                                                        },
                                                        data: [
                                                            // {type : 'average', name: '平均值'}, // 取平均值
                                                            {
                                                                yAxis: studyBarMaps[t].wholeAvgNums,// 指定线的值
                                                                label: {
                                                                    normal: {
                                                                        formatter: '{c}' + studyBarMaps[t].unit
                                                                    }
                                                                }
                                                            }
                                                        ]
                                                    },
                                                    data: studyBarMaps[t].seriesData.slice(j * vm.breakupNum, (j + 1) * vm.breakupNum),

                                                },
                                                {
                                                    name: studyBarMaps[t].legendData[0],
                                                    type: 'line',
                                                },
                                                {
                                                    name: studyBarMaps[t].legendData[1],
                                                    type: 'line',
                                                }

                                            ]
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
            },
            //4.答题速度右边小柱状图
            answerGridEcharts:function(res){
                var vm=this
                var answerBarMaps=res
                if(answerBarMaps!= undefined){
                    var _className = document.getElementsByClassName('answerBarMapEcharts')[0];
                        var myChart = echarts.init(_className, EChartsTheme, {renderer: 'svg'});
                        // 使用刚指定的配置项和数据显示图表。
                    if(answerBarMaps.legendData!=undefined){
                        var lengData1=answerBarMaps.legendData[0]
                        var lengData2=answerBarMaps.legendData[1]

                        myChart.setOption({
                            color: '#1B92FF',
                            title: {
                                text: answerBarMaps.title,
                                left: 'center'
                            },
                            tooltip: {
                                trigger: 'axis',
                                axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                                    type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                                },
                                formatter: function (params) {
                                    return params[0].name +'的答题速度' +':'+params[0].value+ '题/分钟';
                                }
                            },
                            legend: {
                                align: 'left',
                                bottom: '-5',
                                data: [
                                    {
                                        name: lengData1,
                                        icon: 'rect',
                                    },
                                    {
                                        name: lengData2,
                                        icon: 'image://../../../../public/resource/activity/images/drabs.png'
                                    }
                                ]
                            },
                            xAxis: {
                                data: answerBarMaps.xAxisData
                            },
                            yAxis: {},
                            series: [
                                {
                                name:answerBarMaps.xAxisData,
                                type: 'bar',
                                barMaxWidth: 30,
                                label: {
                                    normal: {
                                        // show: res.gridDataList.length <= 10 ? true : false, // 10个以内的数据才展示顶部标识
                                        formatter: '{c}%', // a表示系列名, b表示数据名, c表示数据值
                                        position: 'top'
                                    }
                                },
                                markLine: {
                                    symbol: 'none', // 两端的形状
                                    lineStyle: {
                                        normal: {
                                            type: 'circle', // 圆虚线
                                            width: 2, // 线宽
                                            color: '#32C35D'
                                        }
                                    },
                                    data: [
                                        // {type: 'average', name: '平均值'}, // 取平均值
                                        {
                                            yAxis: answerBarMaps.wholeAnswerSpeed,
                                            label: {
                                                normal: {
                                                    formatter: '{c}' + '题/分钟'
                                                }
                                            }
                                        }
                                    ]
                                },
                                data: answerBarMaps.seriesData,
                            },
                                {
                                    name: lengData2,
                                    type: 'line',
                                },
                                {
                                    name: lengData1,
                                    type: 'line',
                                }
                            ]
                        });

                }
                }
            },
            // 4.答题速度柱状图
            answerSpeedEcharts:function(res){
                var vm=this
                var answerSpeed=res
                if(answerSpeed != undefined){
                    if(answerSpeed.length>0) {
                        for (var n = 0; n < answerSpeed.length; n++) {
                            var echart_num = Math.ceil( answerSpeed[n].seriesData.length / vm.breakupNum); // 以wholeScoreDetail字段做拆分，40个为一组
                            // clone节点
                            for (var i = 0; i < echart_num; i++) {
                                if (i > 0) { // 从第二个40项开始，clone节点并插入兄弟节点的后面
                                    $('.answerSpeedEcharts'+n).eq($('.answerSpeedEcharts'+n).length - 1).after($('.answerSpeedEcharts'+n).eq(0).clone(true));
                                }
                            }
                        }
                        for (var n = 0; n < answerSpeed.length; n++) {
                            var xAxisEcharts={name:[],no:[]};
                            for(var h=0;h<answerSpeed[n].xAxisData.length;h++){
                                xAxisEcharts.name.push(answerSpeed[n].xAxisData[h].name)
                                xAxisEcharts.no.push(answerSpeed[n].xAxisData[h].no)
                            }
                            var echart_num = Math.ceil( answerSpeed[n].seriesData.length / vm.breakupNum); // 以wholeScoreDetail字段做拆分，40个为一组
                            for(var j=0;j<echart_num;j++){
                                var _className = document.getElementsByClassName('answerSpeedEcharts'+n)[j];
                                if(_className != null&&answerSpeed[n].xAxisData.length>0) {
                                    var myChart = echarts.init(_className, EChartsTheme);
                                    // 使用刚指定的配置项和数据显示图表。
                                    myChart.setOption({
                                        color: '#1B92FF',
                                        title: {
                                            text: answerSpeed[n].title,
                                            left: 'center'
                                        },
                                        tooltip: {
                                            trigger: 'axis',
                                            axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                                                type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                                            },
                                            formatter: function (params) {
                                                var xName=params[0].seriesName.split(',');
                                                return xName[params[0].dataIndex] + '的答题速度:' +  params[0].value + '题/每分种';
                                            }
                                        },
                                        legend: {
                                            align: 'left',
                                            bottom: '-5',
                                            data:[
                                                {
                                                    name:answerSpeed[n].legendData[1],
                                                    icon: 'rect',
                                                },
                                                {
                                                    name: answerSpeed[n].legendData[0] ,
                                                    icon:'image://../../../../public/resource/activity/images/drabs.png'
                                                }
                                            ]
                                        },
                                        xAxis: {
                                            data: xAxisEcharts.no.slice(j * vm.breakupNum, (j + 1) * vm.breakupNum)
                                        },
                                        yAxis: {},
                                        series: [
                                            {
                                                name: xAxisEcharts.name.slice(j * vm.breakupNum, (j + 1) * vm.breakupNum),
                                                type: 'bar',
                                                barMaxWidth: 20,
                                                label: {
                                                    normal: {
                                                        // show: res.gridDataList.length <= 10 ? true : false, // 10个以内的数据才展示顶部标识
                                                        formatter: '{c}%', // a表示系列名, b表示数据名, c表示数据值
                                                        position: 'top'
                                                    }
                                                },
                                                markLine: {
                                                    symbol: 'none', // 两端的形状
                                                    lineStyle: {
                                                        normal: {
                                                            type: 'circle', // 圆虚线
                                                            width: 2, // 线宽
                                                            color: '#32C35D'
                                                        }
                                                    },
                                                    data: [{
                                                        yAxis: answerSpeed[n].wholeAvgNums,// 指定线的值
                                                        label: {
                                                            normal: {
                                                                formatter:'{c}'+ answerSpeed[n].unit
                                                            }
                                                        }
                                                    }]
                                                },
                                                data: answerSpeed[n].seriesData.slice(j * vm.breakupNum, (j + 1) * vm.breakupNum),
                                            },
                                            {
                                                name: answerSpeed[n].legendData[0],
                                                type: 'line',
                                            },
                                            {
                                                name: answerSpeed[n].legendData[1],
                                                type: 'line',
                                            }
                                        ]
                                    });
                                }
                            }

                        }
                    }
                }
            },
            produceImgAndDownload: function () {
                // 下载pdf流程：前端使用html2canvas将html生成canvas，然后转成base64 url，再传给后端，由后端生成pdf并下载
                var downloadSections = $('.loadSection'); // 只操作显示的loadSection
                var _length = $('.loadSection').length;
                var covertNum = 0; // html2canvas是个异步流程，需要统计异步完成所有的操作才请求后端
                var that = this;
                that.base64ObjectArr = [];
                for (var i = 0; i < downloadSections.length; i++) {
                    (function(i) {
                        var width = downloadSections[i].offsetWidth;
                        var height = downloadSections[i].offsetHeight;
                        var canvas = document.createElement("canvas");
                        var scale = 2;
                        canvas.width = width * scale;
                        canvas.height = height * scale;
                        canvas.getContext("2d").scale(scale, scale);
                        html2canvas(downloadSections[i], {
                            scale: scale,
                            canvas: canvas,
                            width: width,
                            height: height,
                            logging: false, // debug模式
                            useCORS: true // 允许跨域
                        }).then(function (canvas) {
                            covertNum++; // 计数
                            var base64Url = canvas.toDataURL('image/jpeg', 1.0); // toBase64
                            if(base64Url.split(',')[1] != ''){
                                var row = {
                                    base64Url: base64Url.split(',')[1],
                                    index: i
                                };
                                that.base64ObjectArr.push(row);
                            }
                            if (covertNum === downloadSections.length) {
                                that.downLoadPdfByBackEnd();
                            }
                        });
                    })(i);
                }
            },
            //调用后端接口
            downLoadPdfByBackEnd: function () {
                var that=this
                // 排序：解决i顺序错乱bug
                that.base64ObjectArr = that.base64ObjectArr.sort(function (a, b) {
                    return a.index - b.index;
                });
                // 收集base64Url组成数组
                var base64Arr = [];
                var indexArr = [];
                for (var j = 0; j < that.base64ObjectArr.length; j++) {
                    base64Arr.push(that.base64ObjectArr[j].base64Url);
                    indexArr.push(that.base64ObjectArr[j].index);
                }

                var dir = 'image' + new Date().getTime();  // 存放路径
                var filePaths = []; //收集每次返回的文件路径
                var loopTime = Math.ceil(that.base64ObjectArr.length / breakupDownloadNum);
                for (var m = 0; m < loopTime; m++) {
                    (function(m) {
                        // 开始请求下载pdf
                        var noList = indexArr.slice(m * breakupDownloadNum, (m + 1) * breakupDownloadNum);
                        var contentList = base64Arr.slice(m * breakupDownloadNum, (m + 1) * breakupDownloadNum);

                        var formData = new FormData();
                        formData.append("nos", noList); // 编号数组，对应content
                        formData.append("contents", contentList); // 地址数组
                        formData.append("fileName", that.examFullName);
                        formData.append("dir", dir); // 服务端图片路径
                        formData.append("filePaths", filePaths); // n - 1次图片地址
                        formData.append("endflag", m === loopTime - 1 ? 'true' : '');
                        $.ajax({
                            url: '/report/createReports.vpage',
                            type: 'POST',
                            processData: false,
                            contentType: false,
                            data: formData,
                            async: false,
                            success: function (res) {
                                if (res.result) {
                                    filePaths.push(res.filePathTemp);
                                    if(m === loopTime - 1) { // 最后一次
                                        $('#loadWindow').css('display','none')
                                        that.downloadPdf(res.reportPath, that.examFullName);
                                    }
                                } else {
                                    $('#loadWindow').css('display','none')
                                    that.alertError(res.info || '下载出错，请稍后重试');
                                }
                            },
                            error: function () {
                                $('#loadWindow').css('display','none')
                                that.alertError('下载出错，请稍后重试');
                            },
                            complete: function () {
                                if(m === loopTime - 1){
                                    downloadPdfFlag = false; // 重置下载按钮
                                }
                            }
                        });
                    })(m);
                }
            },
            //下载报告
            downloadPdf: function(reportPath, examFullName){
                var requestUrl = "/report/downReport.vpage?filePath=" + reportPath + "&fileName=" + examFullName + '.pdf';
                var downloadIframe = "<iframe style='display:none;' src=" + requestUrl + "/>";
                $("body").append(downloadIframe);
            },
            //判断是否直接下载报告
            downloadReport: function () {
                $('#loadWindow').css('display','block')
                var that = this;
                // 正在下载中时不再触发
                if (that.downloadPdfFlag) return ;
                that.downloadPdfFlag = true;

                // 异步原因：防止走预处理消耗浏览器资源导致弹窗不能出现
                setTimeout(function () {
                    if (that.base64ObjectArr.length) { // 已经生成过，则直接掉接口下载
                        that.downLoadPdfByBackEnd();
                    } else { // 未生成过，先走前端生成流程再下载
                        that.produceImgAndDownload();
                    }
                }, 1000);
                YQ.voxLogs({
                    database: databaseLogs,
                    module: moduleName,
                    op: "activity_detail_download_click",
                    s0: window.idType === 'schoolmaster' ? "校长" : "教研员",
                    s1: '', // 年级
                    s2: this.main01.activityType, // 活动类型
                });
            },
            //置顶
            bindGlobalEvent :function () {
                $(window).on('scroll', function() {
                    if ($(window).scrollTop() >= 500) { // 滚动了500之后显示
                        $('#gotoTop').show();
                    } else {
                        $('#gotoTop').hide();
                    }
                });

                $('#gotoTop').on('click', function(){ // 点击置顶
                    $('html').animate({
                        scrollTop: '0px'
                    }, 300);
                });
            },
            //错误提示
            alertError: function (content, title, callback) {
                var title = title || '系统提示';
                $.prompt(content, {
                    title: title,
                    buttons: {'确定': true},
                    focus : 0,
                    position: {width: 500},
                    submit : function(e, v){
                        if(v){
                            e.preventDefault();
                            if (callback) {
                                callback();
                            } else {
                                $.prompt.close();
                            }
                        }
                    }
                });
            },
        }
    })
})