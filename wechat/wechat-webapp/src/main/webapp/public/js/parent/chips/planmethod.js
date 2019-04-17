/*
* create by chunbao.cai on 2018-6-22
* 薯条英语公众号
* -- 电子教材详情
*
* */
define(["jquery","logger","../../public/lib/vue/vue.js","../../public/lib/echarts/4.0.4/echarts.min.js"],function($,logger,Vue,echarts){
    var vm = new Vue({
        el:'#planmethod',
        data:{
            description_status:false,
            data:{
                grade:'A',
                rank:'',
                studyPlan:'',
                title:'',
                scoreRanking:'',
                pointAbilityName:''
            }
        },
        methods:{
            getParams:function(name){
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]); return null;
            },
            show_description:function(){
                var _this = this;
                _this.description_status = true
            },
            close_description:function(){
                var _this = this;
                _this.description_status = false
            },
        },
        created:function(){
            var _this = this;
            $.get('/chips/daily/study/summary.vpage',{
                unitId:_this.getParams('id'),
                bookId:_this.getParams('book')
            }).then(function(res){
                if(res.success){
                    _this.data = res
                }
            });
        },
        mounted:function(){
            var _this = this;
            var dom = document.getElementById('container'),
                dom2 = document.getElementById('container2'),
                myChart = echarts.init(dom),
                myChart2 = echarts.init(dom2),
                rdata = {};
            setTimeout(function(){
                rdata.independent = _this.data.independent;
                rdata.listening = _this.data.listening;
                rdata.fluency = _this.data.fluency;
                rdata.pronunciation = _this.data.pronunciation;
                rdata.express = _this.data.express;

                var option = {
                    title: {
                        show: false
                    },
                    tooltip: {
                        trigger: 'axis'
                    },
                    legend: {
                        x: 'center',
                        show: false,
                        data:['某软件']
                    },
                    radar: [
                        {
                            shape: 'circle',
                            indicator: [
                                {text: '独立完成', max: 100},
                                {text: '听力', max: 100},
                                {text: '流利度', max: 100},
                                {text: '发音', max: 100},
                                {text: '表达', max: 100}
                            ],
                            name: {
                                textStyle: {
                                    color:'#464646', // 独立完成等文字颜色
                                    fontSize : 13
                                }
                            },
                            center: ['50%', '50%'],
                            radius: 80,
                            axisLine: {            // 坐标轴线
                                show: true,        // 默认显示，属性show控制显示与否
                                lineStyle: {
                                    color: '#dfdfdf',
                                    width:0.5,
                                }
                            },
                            splitArea : {
                                show : true,
                                areaStyle : {
                                    color: ['rgba(135,225,44, 0.05)']  // 图表背景网格的颜色
                                }
                            },
                            splitLine : {
                                show : true,
                                lineStyle : {
                                    width : 0.5,
                                    color : '#dfdfdf' // 图表背景网格线的颜色
                                }
                            }
                        }
                    ],
                    series: [
                        {
                            name:'能力概况',
                            type: 'radar',
                            tooltip: {
                                trigger: 'item'
                            },
                            itemStyle: {
                                normal: {
                                    areaStyle:{
                                        color: '#8AD63D',
                                        type: 'default'
                                    },
                                    color: '#8AD63D',
                                    borderType: 'dashed'
                                }
                            },
                            lineStyle: {
                                width: 1,
                                color:'#8AD63D'
                            },
                            nodeStyle: {
                                borderColor:'#8AD63D'
                            },
                            data: [
                                {
                                    value: [
                                        rdata.independent,
                                        rdata.listening,
                                        rdata.fluency,
                                        rdata.pronunciation,
                                        rdata.express
                                    ]
                                }
                            ]
                        }
                    ]
                };

                if (option && typeof option === 'object'){
                    myChart.setOption(option, true);
                }

                var yAxisData = [];
                var xAxisData = [];
                _this.data.lessonHistory.forEach(function(item, i){
                    xAxisData.push('DAY' + i);
                    yAxisData.push(item.score);
                });

                var option2 = {
                    xAxis: {
                        data: xAxisData,
                        textStyle:{
                            fontSize:8
                        },
                        axisLabel:{
                            fontSize:8,
                            interval:0,
                            align:'center',
                            rotate:50,
                            margin:15
                        }
                    },
                    yAxis: {
                        splitLine: {
                            lineStyle: {
                                type: 'dashed'
                            }
                        },
                        max:100,
                        axisLabel:{
                            fontSize:8,
                            align:'center',
                            margin:12
                        },
                    },
                    grid:{
                        right:45,
                        top:10,
                        bottom:35,
                        left:30
                    },
                    legend: {
                        x: 'center',
                        show: true,
                        data:['某软件']
                    },
                    series: [{
                        symbolSize: 10,
                        data: yAxisData,
                        type: 'scatter',
                        animation:false,
                        itemStyle:{
                            color: {
                                type: 'radial',
                                x: 0.5,
                                y: 0.5,
                                r: 0.6,
                                colorStops: [{
                                    offset: 0, color: '#fff' // 0% 处的颜色
                                }, {
                                    offset: 0.8, color: '#FFB71E' // 100% 处的颜色
                                },{
                                    offset: 1, color: '#FFB71E' // 100% 处的颜色
                                }],
                                globalCoord: false // 缺省为 false
                            },
                            shadowColor:{
                                type: 'linear',
                                x: 0,
                                y: 0,
                                x2: 0,
                                y2: 1,
                                colorStops: [{
                                    offset: 0, color: 'red' // 0% 处的颜色
                                }, {
                                    offset: 2, color: 'blue' // 100% 处的颜色
                                }],
                                globalCoord: false // 缺省为 false
                            }
                        },
                        markLine: {
                            silent: true,
                            data: [{
                                yAxis: 90
                            }],
                            symbolSize:0,
                            label:{
                                formatter:'A级 90',
                                color: 'red',
                            },
                            lineStyle:{
                                color: {
                                    type: 'linear',
                                    x: 0,
                                    y: 0,
                                    x2: 0,
                                    y2: 1,
                                    colorStops: [{
                                        offset: 0, color: 'red'
                                    }, {
                                        offset: 1, color: 'red'
                                    }],
                                    globalCoord: false
                                }
                            }
                        }
                    }]
                };

                if (option2 && typeof option2 === 'object'){
                    myChart2.setOption(option2, true);
                }
            },200);


        }

    })

});