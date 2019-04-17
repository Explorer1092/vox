/*
* create by chunbao.cai on 2018-6-22
* 薯条英语公众号
* -- 电子教材详情
*
* */
define(["jquery","logger","../../public/lib/vue/vue.js","../../public/lib/echarts/4.0.4/echarts.min.js"],function($,logger,Vue,echarts){
    var vm = new Vue({
        el:'#report',
        data:{
            description_status:false,
            translate_status:false,
            txt:'原文',
            size:0,
            data:{
                enSummary:'',
                enSummary:'',
                levelName:'',
                task:[],
                warmUp:[],
                diaglogue:[]
            },

        },
        computed:{
            keys:function(){
                var _this = this;
                var allKey = {
                    task:[
                        {name:'CS',isActive:false},
                        {name:'G',isActive:false},
                        {name:'L',isActive:false},
                        {name:'P',isActive:false}
                    ],
                    warmUp:[
                        {name:'CS',isActive:false},
                        {name:'G',isActive:false},
                        {name:'L',isActive:false},
                        {name:'P',isActive:false}
                    ],
                    diaglogue:[
                        {name:'CS',isActive:false},
                        {name:'G',isActive:false},
                        {name:'L',isActive:false},
                        {name:'P',isActive:false}
                    ]
                };

                for(key in allKey){
                    allKey[key].forEach(function(item){
                        if(_this.data[key].indexOf(item.name) > -1){
                            item.isActive = true
                        }
                    })
                }

                return allKey;
            }
        },
        methods:{
            getParams:function(name){
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]); return null;
            },
            translate:function(){
                var _this = this;
                if(_this.translate_status){
                    _this.translate_status = false;
                    _this.txt = '翻译'
                }else{
                    _this.translate_status = true;
                    _this.txt = '原文'
                }
            },
            show_description:function(){
                var _this = this;
                _this.description_status = true
            },
            close_description:function(){
                var _this = this;
                _this.description_status = false
            },
            to_robinnormal:function(){
                var _this = this;
                var sign = 1;
                switch(_this.data.levelName){
                    case "一级":
                        sign = 1;
                        break;
                    case "二级":
                        sign = 2;
                        break;
                    case "三级":
                        sign = 3;
                        break;
                    default:
                        break;
                }
                window.location.href = "/chips/center/robinnormal.vpage?levelName=" + sign
            }
        },
        created:function(){
            var _this = this;
            $.get('/chips/finish/summaryV2.vpage',{
                bookId:_this.getParams('bookId'),
                userId:_this.getParams('userId')
            }).then(function(res){
                if(res.success){
                    _this.data = res;
                    _this.size = res.lessonHistory != null && res.lessonHistory instanceof Array ? res.lessonHistory.length : 0;
                }
            });
        },
        mounted:function(){
            var _this = this;
            var dom2 = document.getElementById('container2'),
                myChart2 = echarts.init(dom2);
            setTimeout(function(){

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