/**
 * @author:
 * @description: "一起测教研员报告"
 * @createdDate: 2018/9/4
 * @lastModifyDate: 2018/9/4
 */

// YQ: 通用方法(public/script/YQ.js), impromptu: 通用弹窗（http://trentrichardson.com/Impromptu/）, voxLogs: 打点
define(["jquery", "vue", "YQ", "echarts-4.2.0", "html2canvas","echarts-report", "impromptu", "voxLogs"],function($, Vue, YQ, echarts, html2canvas) {
    // 注：该项目无编译机制，不要使用es6语法

    var databaseLogs = "tianshu_logs"; // 打点的表
    var EChartsTheme = "report"; // echart配色主题，假设配色文件为echarts-adminteacher
    var downloadPdfFlag = false; // 下载padf flag（防止下载中再触发）
    var modulename = "m_6q3pjPVz";


    // 打点方法如下：
    YQ.voxLogs({
        database: databaseLogs, // 表名
        module: '', // module
        op: '', // op
        s0: '', // s0, 有则带上
        s1: '' // s1, 有则带上
    });
    // 获取链接参数
    // console.log(YQ.getQuery('examId'))
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
        el: '#rstaffPage',
        data: {
            subject:'',
            grade:'',
            a: '变量1',
            b: '变量2',
            isTrue: true ,
            clickIndex:'',
            activeIndex:'1',
            activeColor:0,
            sTitleActive:-1,
            blank:'blank',
            mergeRow:'',
            popUpBox:{isOpen:false,html:''},
            reportTitle:[],
            reportSubTitle:'',
            reportDesc:[],
            catalog:[],
            ulActive:'',
            base64ObjectArr:[],
            downloadPdfFlag: false,
            examFullName: '名称',
            colorArr: [
                "#4794f5","#ffc281","#ff6767","#92e0ad", "#a475ff", "#67e0e3", "#6f7fff", "#ffd02c", "#ef6dbb","#ff9f7f",
                "#4794f5","#ffc281","#ff6767","#92e0ad", "#a475ff", "#67e0e3", "#6f7fff", "#ffd02c", "#ef6dbb","#ff9f7f",
                "#4794f5","#ffc281","#ff6767","#92e0ad", "#a475ff", "#67e0e3", "#6f7fff", "#ffd02c", "#ef6dbb","#ff9f7f",
                "#4794f5","#ffc281","#ff6767","#92e0ad", "#a475ff", "#67e0e3", "#6f7fff", "#ffd02c", "#ef6dbb","#ff9f7f",
                "#4794f5","#ffc281","#ff6767","#92e0ad", "#a475ff", "#67e0e3", "#6f7fff", "#ffd02c", "#ef6dbb","#ff9f7f"
            ],
            main:[],
            nowDate: '',
            tableBox:[],
            box:[],
            table02:{table02Th:[],table02Tr:[], table01Tb:[],table02Tb:[],rowSpan:[]},
            table02Th:[],
            table02Tr:[],
            table01Tb:[],
            table02Tb:[],
            rowSpan:[],
            tb02Index:[],
            nowYear:'',
            content:[],
            regionLevel: !YQ.getQuery('regionLevel')?'':YQ.getQuery('regionLevel'),
            bgColor: '#387DC1',
            number: '02',
            downloadIsShow: false
        },
        mounted: function () {
            var _this=this
            _this.loadData(),
                //获取测评情况
                _this.loadSurvey()
            _this.getNowFormatDate()
            _this.getNowYear()
            _this.getBgColor()
        },
        computed: {
        },
        watch: {
            main:function(val, oldVal){
                // var vm = this
                // if(val.length == 4){
                //     //vm.drawChart()
                // }
                // if(val.length == 1){
                //     //获得项目实施情况
                //     vm.loadProject()
                // }
                // if(val.length == 2){
                //     //获得学生整体情况
                //     vm.loadStudentWhole()
                // }
                // if(val.length == 3){
                //     //获得学生学科表现
                //     vm.loadStudentSubject()
                // }
                // if(val.length == 4){
                //     //获得评价总结与建议
                //     vm.loadSuggest()
                // }
                // if(val.length == 5){
                //     //附表
                //     vm.loadAttachedList()
                // }

            },

        },
        methods: {
            //获取封皮封面背景颜色及编号
            getBgColor: function () {
                var _this = this;
                if(YQ.getQuery('regionLevel') == 'city'){
                    _this.bgColor = '#387DC1';
                    _this.number = '01';
                }
                if(YQ.getQuery('regionLevel') == 'county'){
                    _this.bgColor = '#39B1B3';
                    _this.number = '03';
                }
                if(YQ.getQuery('regionLevel') == 'school'){
                    _this.bgColor = '#393390';
                    _this.number = '04';
                }
            },
            // 得到初始化展开的ul
            arrowShow:function(){
                var _this=this;
                var ulShow=_this.catalog;
                for(var i=0;i<ulShow.length;i++){
                    if(ulShow[i].childTitle.length!=0){
                        break
                    }}
                this.ulActive=i
            },
            tableBox1: function(){
                for(var i=1;i<this.content.length;i++){
                    if(!this.content[i]){
                        this.content.splice(i,1)
                        boxArr.push(this.content[i])
                    }
                }
                var boxArr=this.content
                var arr=[]
                for(var j=0;j<boxArr.length;j++){
                    for(var a=0;a<boxArr[j].length;a++){
                        if(boxArr[j][a].grid.length>0){
                            for(var b=0;b<boxArr[j][a].grid.length;b++){
                                // console.log( boxArr[j][a].grid[b])
                                if(boxArr[j][a].grid[b].gridType==='0'){
                                    arr= boxArr[j][a].grid[b].gridData

                                }
                            }
                            var box=arr
                        }
                    }
                }
                this.box = box;
                this.table02.table02Tr=box;
                this.table02.table02Th=box[0]
                this.table02.table02Tr.splice(0,1);
                for(var i=0;i<this.table02.table02Tr.length;i++){
                    var row = this.table02.table02Tr[i];
                    if(row[0].rowSpan == ''){
                        this.table02.table02Tr[i].splice(0,1)
                    }
                }
            },
            // 大标题
            bigScroll:function(index){
                this.sTitleActive = -1;
                this.activeColor=index;
                this.ulActive = index;
                var bigTopHeight=$(".download_box").eq(index+3).offset().top-90;
                $('html,body').animate({
                    scrollTop: bigTopHeight
                },500)
            },
            // 小标题
            smallScroll:function(index,e){
                var _this=$(e.target)
                this.sTitleActive = index;
                this.activeColor = -1;
                var scrollIndex=$('.r_sidebar_a').children().index(_this.parents('.menu_box'))+3;
                var smallTopHeight=$(".download_box").eq(scrollIndex).children(".download_middle").eq(index).offset().top - 120;
                $('html,body').animate({
                    scrollTop:smallTopHeight
                },500)
            },
            clickReminder:function(){
                var that = this;
                that.popUpBox.isOpen=true
            },
            //发邮件
            submitBtn:function(){
                var that = this;
                var content =$('.text_content').val();
                if(content == ''){
                    that.alertError('请填写建议内容，谢谢~');
                    return
                }
                var title = that.reportTitle.join('');
                $.ajax({
                    url: '/exam/evaluationReport/sendEmail.vpage',
                    type: 'POST',
                    data: {
                        title:title + '_' + YQ.getQuery('regionLevel') + '_' + YQ.getQuery('regionCode') + '_' + YQ.getQuery('examId'),
                        content:content
                    },
                    async: false,
                    success: function (res) {
                        if (res.result) {
                            that.alertError(res.info);
                            that.popUpBox.isOpen=false
                        } else {
                            that.alertError(res.info);
                        }
                    },
                    error: function () {
                        that.alertError(res.info);
                    },
                    complete: function () {

                    }
                })
            },
            hiddenBtn:function(){
                var that = this;
                that.popUpBox.isOpen=false
            },
            //重叠柱图
            studentSituationChart: function (datas, className, index) {
                var _className = document.getElementsByClassName(className)[index];
                var that = this;
                //动态生成echarts高度
                var _yAxisDataLength = datas.yAxis.length;
                var _height = (120 + 30*_yAxisDataLength) + 'px';
                _className.style.height = _height;

                //动态计算y轴标题距刻度线距离
                var _nameGap = that.getNameGap(datas.yAxis);

                //初始化echarts,并定义echarts数据
                var myChart = echarts.init(_className, EChartsTheme, {renderer: 'svg'});
                var xCompany = typeof datas.xCompany !=="undefined" ? datas.xCompany : '';
                var yCompany = typeof datas.yCompany !=="undefined" ? datas.yCompany : '';
                var seriesData = [];
                var legendData = datas.legendData;
                var data = datas.data;
                for (var i = 0; i < legendData.length; i++) {
                    var row = {
                        name: legendData[i],
                        type: 'bar',
                        stack: '总量',
                        label: {
                            normal: {
                                show: true,
                                position: 'insideRight',
                                formatter: '{@score}' + xCompany
                            }
                        },
                        data: data[i]
                    };
                    seriesData.push(row);
                }

                myChart.setOption({
                    // title: {
                    //     text: datas.title,
                    //     x: 'center',
                    //     y: 'bottom'
                    // },
                    tooltip: {
                        trigger: 'axis',
                        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
                        },
                        formatter: function (params) {
                            //console.log(params)
                            var relVal = params[0].name;
                            for (var i = 0, l = params.length; i < l; i++) {
                                relVal += '<br/>' + params[i].marker + params[i].seriesName + ' : ' + params[i].value + xCompany;
                            }
                            return relVal;
                        }
                    },
                    legend: {
                        top: 10,
                        data: datas.legendData
                    },
                    grid: {
                        top: 50,
                        left: '5%',
                        right: '4%',
                        bottom: 30,
                        containLabel: true
                        // borderColor: '#FFFFFF',
                        // borderWidth:1
                    },
                    xAxis: {
                        name: datas.xTitle,
                        nameLocation: 'center',
                        nameGap: 35,
                        max:100,
                        type: 'value',
                        axisLabel: {
                            formatter: '{value}' + xCompany
                        },
                    },
                    yAxis: {
                        name: datas.yTitle,
                        nameLocation: 'center',
                        nameGap: _nameGap,
                        type: 'category',
                        data: datas.yAxis
                    },
                    series: seriesData
                }, this.isTrue);
            },
            //柱图1
            standardRateChart: function (datas, className, index) {
                var _className = document.getElementsByClassName(className)[index];
                if(typeof _className === 'undefined'){
                    return
                }
                var that = this;

                //动态生成echarts高度
                var _classNameLength = document.getElementsByClassName(className).length;
                var _yAxisDataLength = datas.yAxis.length;
                var _height = (120 + 30*_yAxisDataLength) + 'px';
                _className.style.height = _height;
                //console.log(_height);
                //动态计算y轴标题距刻度线距离
                var _nameGap = that.getNameGap(datas.yAxis);

                //初始化echarts,并定义echarts数据 {renderer: 'svg'}
                var myChart = echarts.init(_className, EChartsTheme, {renderer: 'svg'});

                var xCompany = typeof datas.xCompany !=="undefined" ? datas.xCompany : '';
                var yCompany = typeof datas.yCompany !=="undefined" ? datas.yCompany : '';
                var yAxisData = datas.yAxis;
                var data = datas.data;
                var avgName = datas.avgName;
                var avgNum = datas.avgNum;
                var avgArr = [];
                var title = !datas.title?'':datas.title;
                avgNum.forEach(function (value, index) {
                    var _row = {
                        name : avgName[index],
                        xAxis: value
                    }
                    avgArr.push(_row);
                });
                var colorArr = this.colorArr;
                var seriesData = [
                    {
                        type: 'bar',
                        data: [],
                        //均线
                        markLine : {
                            symbol : 'none',
                            itemStyle : {
                                normal : {
                                    color:'#FF4C4C',
                                    label : {
                                        show: true,
                                        formatter: function (params) {
                                            var name = params.name;
                                            var value = params.value;
                                            return name + ':' + value;
                                        }
                                    }
                                }
                            },
                            tooltip: {
                                formatter: function (params) {
                                    return params.marker + params.name + ':' + params.value + xCompany;
                                }
                            },
                            data : avgArr
                        },
                        label: {
                            normal: {
                                show: true,
                                position: 'right',
                                formatter:function(params)
                                {
                                    var value = params.value;
                                    return value + xCompany
                                }
                            }
                        }
                    }
                ];
                for (var i = 0; i < yAxisData.length; i++) {
                    var row = {
                        value: data[i],
                        itemStyle: {
                            normal: {color: colorArr[i]}
                        }
                    };
                    seriesData[0].data.push(row);
                }

                myChart.setOption({
                    title: {
                        text: title,
                        x: 'center',
                        y: 'top',
                        // backgroundColor:'#bebebe',
                        // textStyle:{
                        //     width:'100%'
                        // }
                    },
                    tooltip : {
                        // trigger: 'axis',
                        // axisPointer : {
                        //     type : 'shadow'
                        // },
                        // formatter:function(params)
                        // {
                        //     //console.log(params)
                        //     var relVal = params[0].name;
                        //     for (var i = 0, l = params.length; i < l; i++) {
                        //         relVal += '<br/>' + params[i].marker + params[i].seriesName + ' : ' + params[i].value+"%";
                        //     }
                        //     return relVal;
                        // }
                    },
                    grid: {
                        top:50,
                        left: '5%',
                        right: '7%',
                        bottom: 30,
                        containLabel: true
                        // borderColor: '#FFFFFF',
                        // borderWidth:1
                    },
                    xAxis: {
                        name: datas.xTitle,
                        nameLocation: 'center',
                        nameGap: 35,
                        type: 'value',
                        max:100,
                        axisLabel: {
                            formatter: '{value}'
                        }
                    },
                    yAxis: {
                        name: datas.yTitle,
                        nameLocation: 'center',
                        nameGap: _nameGap,
                        type: 'category',
                        data: datas.yAxis
                    },
                    series: seriesData
                }, this.isTrue);
            },
            //散点图
            scatterPointChartData: function (datas, className, index) {
                var _className = document.getElementsByClassName(className)[index];

                var myChart = echarts.init(_className, EChartsTheme, {renderer: 'svg'});
                var that = this;
                var _nameGap = that.getNameGap(datas.yTitle);
                var seriesData = [];
                var xCompany = typeof datas.xCompany !=="undefined" ? datas.xCompany : '';
                var yCompany = typeof datas.yCompany !=="undefined" ? datas.yCompany : '';
                var legendData = datas.legendData;
                var legendDataLength = legendData.length;
                var top = 10 + Math.ceil(legendDataLength/4) * 4;

                var data = datas.data;
                // var xMean=datas.xAvg;
                // var yMean=datas.yAvg;
                for (var i = 0; i < legendData.length; i++) {
                    var row = {
                        name:legendData[i],
                        type:'scatter',
                        data: [data[i]],
                        markLine : {
                            symbol : 'none',
                            itemStyle : {
                                normal : {
                                    color:'#FF4C4C',
                                    label : {
                                        show:true,
                                        formatter:function(params)
                                        {
                                            var name = params.name;
                                            var value = params.value;
                                            if(params.data.yAxis){
                                                return  name + ':' + value + '\n\n';
                                            }else{
                                                return  name + ':' + value;
                                            }

                                        }

                                    }
                                }
                            },
                            tooltip: {
                                formatter: function (params) {
                                    return params.marker + params.name + ':' + params.value + xCompany;
                                }
                            },
                            data : [
                                {
                                    name: datas.xAvgName,
                                    xAxis: datas.xAvg
                                },
                                {
                                    name: datas.yAvgName,
                                    yAxis: datas.yAvg,

                                }
                            ]
                        }
                    };
                    seriesData.push(row);
                }
                function axisValue(data){
                    var maxVal=data;
                    var xCost=[]
                    var yCost=[]
                    for(var i=0;i<data.length;i++){
                        xCost.push(data[i][0])
                        yCost.push(data[i][1])
                    }
                    var xMaxCost=(Math.max.apply(null,xCost )+2).toFixed(1);//最大值
                    var xMinCost=(Math.min.apply(null,xCost )-2).toFixed(1);//最小值
                    var yMaxCost=(Math.max.apply(null,yCost )+2).toFixed(1);//最大值
                    var yMinCost=(Math.min.apply(null,yCost )-2).toFixed(1);//最小值
                    var xArea=((datas.xAvg-xMinCost)/(xMaxCost-xMinCost)).toFixed(2)
                    var yArea=((datas.yAvg-yMinCost)/(yMaxCost-yMinCost)).toFixed(2)
                    var yMax=(87.5*(1-yArea)*0.65).toFixed(2)+"%"
                    var yMin=(87.5*(1-yArea*0.5)).toFixed(2)+'%'
                    var xMax=((100-5.5-0.5)*xArea/2+5).toFixed(2)+"%"
                    var xMin=(94*(1-(1-xArea)/2)).toFixed(2)+'%'
                    var fontColor='rgba(00,00,00,0.15)'
                    var a= [xMaxCost,xMinCost,yMaxCost,yMinCost,yMax,yMin,xMax,xMin,fontColor]
                    return a
                }
                if(legendData.length>10){
                    legendData=[]
                }
                // console.log(axisValue(data)[4])
                myChart.setOption({
                    title: [
                        {
                            text: 'A',
                            left: axisValue(data)[7],
                            top: axisValue(data)[5],
                            textStyle:{
                                fontSize:40,
                                color:axisValue(data)[8]
                            }
                        },
                        {
                            // text: datas.title,
                            text: 'B',
                            left:axisValue(data)[7],
                            top: axisValue(data)[4],
                            textStyle:{
                                fontSize:40,
                                color: axisValue(data)[8]
                            }
                        },
                        {
                            text: 'C',
                            left:axisValue(data)[6],
                            top: axisValue(data)[4],
                            textStyle: {
                                fontSize: 40,
                                color: axisValue(data)[8]
                            }
                        },
                        {
                            text: 'D',
                            left: axisValue(data)[6],
                            top: axisValue(data)[5],
                            textStyle:{
                                fontSize:40,
                                color:axisValue(data)[8]
                            },
                        }
                    ],
                    grid: {
                        top: top/3 + '%',
                        left: 20,
                        right: 60,
                        bottom: 30,
                        containLabel: true
                    },
                    tooltip : {
                        showDelay : 0,
                        formatter : function (params) {
                            return params.seriesName + ' <br/>'
                                + params.marker + datas.xTitle +':'+ params.value[0] + xCompany  +'<br>'
                                + params.marker + datas.yTitle +':'+params.value[1] + yCompany;

                        },
                        axisPointer:{
                            show: true,
                            type : 'cross',
                            lineStyle: {
                                type : 'dashed',
                                width : 1
                            }
                        }
                    },
                    legend: {
                        data: legendData,
                        left: 'center',

                    },
                    dataZoom:[],
                    xAxis : [
                        {
                            name: datas.xTitle,
                            nameLocation: 'center',
                            nameGap: 35,
                            type : 'value',
                            scale:true,
                            max:axisValue(data)[0],
                            min:axisValue(data)[1],
                            axisLabel : {
                                formatter: '{value} ' + xCompany
                            },
                            splitLine: {
                                show: true
                            }
                        }
                    ],
                    yAxis : [
                        {
                            name: datas.yTitle,
                            nameLocation: 'center',
                            nameGap: 35,
                            type : 'value',
                            max:axisValue(data)[2],
                            min:axisValue(data)[3],
                            scale:true,
                            axisLabel : {
                                formatter: '{value} ' + yCompany
                            },
                            splitLine: {
                                show: true
                            }
                        }
                    ],
                    series : seriesData
                }, this.isTrue);
            },
            //echart
            drawChart: function () {
                var vm = this;

                for(var m = 0;m < vm.main.length; m++) {
                    if(vm.main[m].contents !== 'undefined'){
                        var _contents = vm.main[m].contents;
                        if( Array.isArray(_contents) ){
                            //echarts
                            _contents.forEach(function (value,i) {
                                var _echart = value.echart;
                                if(Array.isArray(_echart)){
                                    _echart.forEach(function (eval,ei) {
                                        var _className = 'echarts' + m + i + ei;
                                        if(Array.isArray(eval.echartData)){
                                            eval.echartData.forEach(function (eeval,eei) {
                                                vm.chooseEchart(eval.echartType, eeval, _className, eei);
                                            });
                                        }else{
                                            vm.chooseEchart(eval.echartType, eval.echartData, _className, 0);
                                        }

                                    });
                                }
                            });

                            //grid

                        }
                    }
                }
            },
            //判断加载那个echart
            chooseEchart:function(type, data, _className, index){
                var vm = this;
                if(type == '重叠柱图'){
                    vm.studentSituationChart(data, _className, index);
                }
                if(type == '柱图'){
                    vm.standardRateChart(data, _className, index);
                }
                if(type == '散点图'){
                    vm.scatterPointChartData(data, _className, index);
                }
            },
            //计算y轴名称到刻度距离
            getNameGap: function(yArr) {
                var _nameGap = 30;
                var _length = 0;
                var _newNameGap = 0;
                if(yArr){
                    for (var n = 0;n < yArr.length; n++) {
                        _length = yArr[n].length;
                        _newNameGap = 18 + 11.5 * _length;
                        if(_newNameGap > _nameGap){
                            _nameGap = _newNameGap;
                        }
                    }
                }else{
                    _length = 0;
                    _newNameGap = 18 + 11.5 * _length;
                    if(_newNameGap > _nameGap){
                        _nameGap = _newNameGap;
                    }
                }

                return _nameGap;
            },
            //生成图片
            produceImgAndDownload: function () {
                // 下载pdf流程：前端使用html2canvas将html生成canvas，然后转成base64 url，再传给后端，由后端生成pdf并下载
                var downloadSections = $('.download_box'); // 只操作显示的loadSection
                var _length = $('.download_box').length;
                var covertNum = 0; // html2canvas是个异步流程，需要统计异步完成所有的操作才请求后端
                var that = this;
                that.base64ObjectArr = [];
                for (var i = 0; i < downloadSections.length; i++) {
                    (function(i) {
                        var width = downloadSections[i].offsetWidth;
                        var height = downloadSections[i].offsetHeight;
                        var canvas = document.createElement("canvas");
                        var scale = 1.5;
                        // $('.beginPageBox')[0].style.display = 'block';
                        // $('.catalog_wrap')[0].style.display = 'block';
                        // $('.page_wrap')[0].style.display = 'block';
                        if(i < _length - 3){
                            $('.page_header')[i].style.display = 'block';
                        }

                        canvas.width = width * scale;
                        canvas.height = height * scale;

                        if(height <= 1002) {
                            height = 1002;
                        }

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

                            var imgIndex = 0;

                            if(i < 2){
                                imgIndex = i;
                            } else if(i == 2){
                                imgIndex = _length-1;
                            } else if(i > 2  && ( i < _length)){
                                imgIndex = i - 1;
                            }

                            if(base64Url.split(',')[1] != ''){
                                var row = {
                                    base64Url: base64Url.split(',')[1],
                                    index: imgIndex
                                };
                                that.base64ObjectArr.push(row);
                            }
                            //所有图片生成完毕
                            if(i < _length - 3){
                                $('.page_header')[i].style.display = 'none';
                            }
                            if (covertNum === downloadSections.length) {
                                // $('.catalog_wrap')[0].style.display = 'none';
                                // $('.page_wrap')[0].style.display = 'none';
                                // $('.beginPageBox')[0].style.display = 'none';
                                that.downLoadPdfByBackEnd();
                            }
                        });

                    })(i);
                }
            },
            //调用后端接口
            downLoadPdfByBackEnd: function () {
                var that = this;
                var dir;
                $.ajax({
                    url: '/report/getReportDir.vpage',
                    type: 'POST',
                    processData: false,
                    contentType: false,
                    data: {},
                    async: false,
                    success: function (res) {
                        if (res.result) {
                            dir = res.dir;
                            YQ.voxLogs({
                                database: databaseLogs,
                                module: modulename,
                                op: "exam_detail_download_click",
                                s0: window.idType === 'schoolmaster' ? "校长" : "教研员",
                                s1: that.gradeName, // 年级
                                s2: that.subject, // 学科
                                S3:'ACADEMIC_ACHIEVEMENT'//报告类型
                            });
                        } else {
                            that.downloadIsShow = false;
                            that.alertError(res.info || '下载出错，请稍后重试');
                        }
                    },
                    error: function (res) {
                        that.downloadIsShow = false;
                        that.alertError(res.info || '下载出错，请稍后重试');
                    }
                });


                // 排序：解决i顺序错乱bug
                that.base64ObjectArr = that.base64ObjectArr.sort(function (a, b) {
                    return a.index - b.index;
                });

                var filePaths = []; //收集每次返回的文件路径
                for (var j = 0; j < that.base64ObjectArr.length; j++) {
                    // 开始请求下载pdf
                    var formData = new FormData();
                    var title = that.reportTitle.join('');
                    formData.append("no", j);
                    formData.append("content", that.base64ObjectArr[j].base64Url);
                    formData.append("fileName", title);
                    formData.append("dir", dir);
                    formData.append("filePaths",filePaths);
                    if(j == (that.base64ObjectArr.length-1)){
                        formData.append("endflag", 'true');
                    }else{
                        formData.append("endflag", '');
                    }

                    $.ajax({
                        url: '/report/createReport.vpage',
                        type: 'POST',
                        processData: false,
                        contentType: false,
                        data: formData,
                        async: false,
                        success: function (res) {
                            if (res.result) {
                                filePaths.push(res.filePathTemp);
                                if(j == (that.base64ObjectArr.length-1)){
                                    that.downloadPdf(res.reportPath, title);
                                }
                            } else {
                                that.downloadIsShow = false;
                                that.alertError(res.info || '下载出错，请稍后重试');
                            }
                        },
                        error: function (res) {
                            that.downloadIsShow = false;
                            that.alertError(res.info || '下载出错，请稍后重试');
                        },
                        complete: function () {
                            if(j == (that.base64ObjectArr.length-1)){
                                that.downloadPdfFlag = false; // 重置下载按钮
                            }
                        }
                    })
                }
            },
            //下载报告
            downloadPdf: function(reportPath, examFullName){
                this.downloadIsShow = false;
                var requestUrl = "/report/downReport.vpage?filePath=" + reportPath + "&fileName=" + examFullName + '.pdf';
                var downloadIframe = "<iframe style='display:none;' src=" + requestUrl + "/>";
                $("body").append(downloadIframe);
            },
            //loading
            showDownloadLoading: function () {
                this.downloadIsShow = true;
                // var title = title || '系统提示';
                // var content = '报告生成中，请稍候30-60S~';
                // $.prompt(content, {
                //     title: title,
                //     focus : 0,
                //     buttons: {},
                //     position: {width: 360},
                //     loaded: function () {
                //         $('.jqiclose').hide();
                //     }
                // });
            },
            //判断是否直接下载报告
            downloadReport: function () {
                var that = this;
                // 判断非IE环境
                // if ((!!window.ActiveXObject || "ActiveXObject" in window)) {
                //     var ChangeBrowserDialogModal = function () {};
                //     var changeBrowserDialogModal = new ChangeBrowserDialogModal();
                //     var changeBrowserDialogHtml = "<div id=\"changeBrowserDialogContent\" data-bind=\"template: { name: 'convertReportTemp', data: self}\"></div>";
                //     $.prompt(changeBrowserDialogHtml, {
                //         title: '提示',
                //         focus: 0,
                //         position: {width: 500},
                //         buttons: {"确定": true},
                //         loaded: function () {
                //             //ko.applyBindings(changeBrowserDialogModal, document.getElementById("changeBrowserDialogContent"));
                //         }
                //     });
                //     return ;
                // }
                that.showDownloadLoading(); // 显示loading弹窗
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

                // YQ.voxLogs({
                //     database: databaseLogs,
                //     module: moduleName,
                //     op: "exam_detail_download_click",
                //     s0: window.idType === 'schoolmaster' ? "校长" : "教研员",
                //     s1: self.gradeName(), // 年级
                //     s2: self.subject() // 学科
                // });
            },
            bindGlobalEvent :function () {
                $(window).on('scroll', function() {
                    if ($(window).scrollTop() >= 500) { // 滚动了500之后显示
                        $('#gotoTop').fadeIn(300);
                    } else {
                        $('#gotoTop').fadeOut(300);
                    }
                });

                $('#gotoTop').on('click', function(){ // 点击置顶
                    $('html').animate({
                        scrollTop: '0px'
                    }, 300);
                });
            },
            //获取目录
            loadData : function(){
                var vm = this;
                $.ajax({
                    url: '/exam/evaluationReport/loadEvaluationReportByMoudleName.vpage',
                    type: 'GET',
                    data: {
                        moduleName: 'EvaluationStruct',//区级
                        regionLevel: YQ.getQuery('regionLevel'),
                        regionCode: YQ.getQuery('regionCode'),
                        examId: YQ.getQuery('examId')
                    },
                    success: function (res) {
                        if(res.result == 'false'){
                            vm.alertError(res.info);
                            return
                        }
                        if(!res.dataMap.reportTitle) {
                            vm.alertError("获取数据失败，请刷新页面重试~");
                            return
                        }
                        vm.reportTitle=res.dataMap.reportTitle;
                        vm.reportSubTitle=res.dataMap.reportSubTitle;
                        vm.reportDesc=res.dataMap.reportDesc;
                        vm.catalog=res.dataMap.catalog;
                        vm.subject=res.subject||'';
                        vm.gradeName=res.grade || '';
                        YQ.voxLogs({
                            database: databaseLogs,
                            module: modulename,
                            op: "exam_detail_load",
                            s0: window.idType === 'schoolmaster' ? "校长" : "教研员",
                            s1: '', // 页面停留时间，不计
                            s2: vm.gradeName, // 年级
                            s3: vm.subject,// 学科
                            S4:'DIAGNOSTIC_EVALUATION'
                        });
                        vm.arrowShow()
                    },
                    error: function () {
                        vm.alertError("获取数据失败，请刷新页面重试~");
                    }
                })
            },
            //获得测评情况  url: '/examReport/loadEvaluationSurvey.vpage',
            loadSurvey : function(){
                var vm = this;
                $.ajax({
                    url: '/exam/evaluationReport/loadEvaluationReportByMoudleName.vpage',
                    type: 'GET',
                    data: {
                        regionLevel: YQ.getQuery('regionLevel'),
                        regionCode: YQ.getQuery('regionCode'),
                        examId: YQ.getQuery('examId'),
                        moduleName:'EvaluationSurvey'
                    },
                    success: function (res) {
                        if(res.result == 'false'){
                            if(res.info != '您好，报告数据正在统计中，请稍后查看'){
                                vm.alertError(res.info);
                                return
                            }
                        }
                        // vm.main[0] = res.dataMap;
                        var _lenght = vm.main.length;
                        if(res.dataMap!= null){
                            vm.$set(vm.main, _lenght, res.dataMap);
                            vm.content.push(vm.main[_lenght].contents);
                        }
                    },
                    error: function () {
                        vm.alertError("获取数据失败，请刷新页面重试~");
                    },
                    complete: function () {
                        vm.loadProject()
                    }
                })
            },
            //获得获得项目实施情况情况  url: '/examReport/loadProjectImplSituation.vpage',
            loadProject : function(){
                var vm = this;
                $.ajax({
                    url: '/exam/evaluationReport/loadEvaluationReportByMoudleName.vpage',
                    type: 'GET',
                    data: {
                        moduleName:'ProjectImplSituation',
                        regionLevel: YQ.getQuery('regionLevel'),
                        regionCode: YQ.getQuery('regionCode'),
                        examId: YQ.getQuery('examId')
                    },
                    success: function (res) {
                        if(res.result == 'false'){
                            if(res.info != '您好，报告数据正在统计中，请稍后查看'){
                                vm.alertError(res.info);
                                return
                            }
                        }
                        var _lenght = vm.main.length;
                        if(res.dataMap!= null){
                            vm.$set(vm.main, _lenght, res.dataMap);
                            vm.content.push(vm.main[_lenght].contents);
                            vm.tableBox1()
                        }
                    },
                    error: function () {
                        vm.alertError("获取数据失败，请刷新页面重试~");
                    },
                    complete: function () {
                        vm.loadStudentWhole()
                    }
                })
            },
            //获得学生整体情况  url: '/examReport/loadStudentWholeSituation.vpage',
            loadStudentWhole : function() {
                var vm = this;
                $.ajax({
                    url: '/exam/evaluationReport/loadEvaluationReportByMoudleName.vpage',
                    type: 'GET',
                    data: {
                        moduleName:'StudentWholeSituation',
                        regionLevel: YQ.getQuery('regionLevel'),
                        regionCode: YQ.getQuery('regionCode'),
                        examId: YQ.getQuery('examId')
                    },
                    success: function (res) {
                        if(res.result == 'false'){
                            if(res.info != '您好，报告数据正在统计中，请稍后查看'){
                                vm.alertError(res.info);
                                return
                            }
                        }
                        var _lenght = vm.main.length;
                        if(res.dataMap!= null){
                            vm.$set(vm.main, _lenght, res.dataMap);
                            vm.content.push(vm.main[_lenght].contents);
                        }
                    },
                    error: function () {
                        vm.alertError("获取数据失败，请刷新页面重试~");
                    },
                    complete: function () {
                        vm.loadStudentSubject()
                    }
                })
            },
            //获得获得学生学科表现  url: '/examReport/loadStudentSubjectSituation.vpage',
            loadStudentSubject : function (){
                var vm = this;
                $.ajax({
                    url: '/exam/evaluationReport/loadEvaluationReportByMoudleName.vpage',
                    type: 'GET',
                    data: {
                        moduleName:'StudentSubjectSituation',
                        regionLevel: YQ.getQuery('regionLevel'),
                        regionCode: YQ.getQuery('regionCode'),
                        examId: YQ.getQuery('examId')
                    },
                    success: function (res) {
                        if(res.result == 'false'){
                            if(res.info != '您好，报告数据正在统计中，请稍后查看'){
                                vm.alertError(res.info);
                                return
                            }
                        }
                        var _lenght = vm.main.length;
                        if(res.dataMap!= null){
                            vm.$set(vm.main, _lenght, res.dataMap);
                            vm.content.push(vm.main[_lenght].contents);
                        }
                    },
                    error: function () {
                        vm.alertError("获取数据失败，请刷新页面重试~");
                    },
                    complete: function () {
                        vm.loadSuggest()
                    }
                })
            },
            //获得评价总结与建议  url: '/examReport/loadSuggest.vpage',
            loadSuggest : function(){
                var vm = this;
                $.ajax({
                    url: '/exam/evaluationReport/loadEvaluationReportByMoudleName.vpage',
                    type: 'GET',
                    data: {
                        moduleName:'Suggest',
                        regionLevel: YQ.getQuery('regionLevel'),
                        regionCode: YQ.getQuery('regionCode'),
                        examId: YQ.getQuery('examId')
                    },
                    success: function (res) {
                        if(res.result == 'false'){
                            if(res.info != '您好，报告数据正在统计中，请稍后查看'){
                                vm.alertError(res.info);
                                return
                            }
                        }
                        var _lenght = vm.main.length;
                        if(res.dataMap!= null){
                            vm.$set(vm.main, _lenght, res.dataMap);
                            vm.content.push(vm.main[_lenght].contents);
                        }
                        //调用echarts加载数据
                        vm.drawChart();
                    },
                    error: function () {
                        vm.alertError("获取数据失败，请刷新页面重试~");
                    },
                    complete: function () {
                        vm.loadAttachedList()
                    }
                })
            },
            // 附表
            loadAttachedList : function(){
                var vm = this;
                $.ajax({
                    url: '/exam/evaluationReport/loadEvaluationReportByMoudleName.vpage',
                    type: 'GET',
                    data: {
                        moduleName:'AttachedList',
                        regionLevel: YQ.getQuery('regionLevel'),
                        regionCode: YQ.getQuery('regionCode'),
                        examId: YQ.getQuery('examId')
                    },
                    success: function (res) {
                        if(res.result == 'false'){
                            if(res.info != '您好，报告数据正在统计中，请稍后查看'){
                                vm.alertError(res.info);
                                return
                            }
                        }
                        var _lenght = vm.main.length;
                        if(res.dataMap!= null){
                            vm.$set(vm.main, _lenght, res.dataMap);
                            vm.content.push(vm.main[_lenght].contents);
                        }
                    },
                    error: function () {
                        vm.alertError("获取数据失败，请刷新页面重试~");
                    }
                })
            },
            //错误弹窗
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
            getNowFormatDate:function () {
                var that= this;
                var date = new Date();
                var seperator1 = "/";
                var year = date.getFullYear();
                var month = date.getMonth() + 1;
                var strDate = date.getDate();
                if (month >= 1 && month <= 9) {
                    month = "0" + month;
                }
                if (strDate >= 0 && strDate <= 9) {
                    strDate = "0" + strDate;
                }
                var currentdate = year + seperator1 + month + seperator1 + strDate;
                that.nowDate = currentdate;
            },
            getNowYear:function () {
                var that= this;
                var date = new Date();
                that.nowYear = date.getFullYear();
            }
        }
    });

});