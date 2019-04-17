<#if skillMonthMapper?has_content>
<div class="mb-con" id="listening_chart" style="width: 700px;height: 400px;" >

</div>
<div class="mb-con" id="speaking_chart" style="width: 700px;height: 400px;" >

</div>
<div class="mb-con" id="reading_chart" style="width: 700px;height: 400px;" >

</div>
<div class="mb-con" id="written_chart" style="width: 700px;height: 400px;" >

</div>
<script type="text/javascript">
    $(function(){
        function dataFormatter(pList,r,s){
            var _ = [];
//            var pList = ["长沙市雨花区五一小学", "长沙市雨花区砂子塘万境水岸小学", "长沙市雨花区黎郡小学", "长沙市雨花区和平小学", "长沙市雨花区麻园湾小学", "长沙市雨花区潭阳小学", "长沙市雨花区砂子塘第二小学", "长沙市雨花区劳动中路小学", "长沙市雨花区树木岭小学", "长沙市雨花区砂子塘泰禹小学", "长沙市雨花区砂子塘新世界小学", "长沙市雨花区长塘里小学", "长沙市雨花区砂子塘天华学校", "长沙市雨花区育新小学", "长沙市雨花区桂花树小学", "长沙市雨花区雅塘村小学", "长沙市雨花区培蕾小学", "长沙市雨花区德馨园小学", "长沙市雨花区中建五局子弟小学", "长沙市雨花区砂子塘小学东校区", "长沙市雨花区砂子塘小学", "长沙市雨花区同升湖国际实际学校", "长沙市雨花区井圭路小学", "长沙市雨花区东方红小学", "长沙市雨花区新世界小学", "长沙市雨花区曹家坡小学", "长沙市雨花区长郡雨花外国语学校", "长沙市雨花区合丰小学", "长沙市雨花区枫树山东南海小学", "长沙市雨花区红花坡小学", "长沙市雨花区红星小学", "长沙市雨花区枫树山大桥小学", "长沙市雨花区劳动东路小学", "长沙市雨花区香樟小学", "长沙市雨花区枫树山小学", "长沙市雨花区边山小学", "长沙市雨花区自然岭小学", "长沙市雨花区燕子岭小学", "长沙市雨花区枫树山鸿铭小学"];
//            var r = rate[date];
//            var s = sum[date];
            for(var i = 0; i < pList.length; i++){
                _.push({
                    name : pList[i],
                    value: [s[i], r[i]]
                });
            }
            return _;
        }

        function generateOptionsParam(pList,rate,sum,type){
            var _options = {};
            _options = {
                timeline: {
                    data        : [<#list dateSet as date>'${date}-01'<#if date_has_next>,</#if></#list>],
                    autoPlay    : true,
                    label       : {
                        formatter: function(s){
                            return s.slice(0, 7);
                        }
                    },
                    playInterval: 2000
                },
                options : [
                    <#list dateSet as date>
                        <#if date_index == 0>
                            {
                                backgroundColor: "white",
                                title          : {
                                    text   : '${date} ${(currentUser.formatManagedRegionStr())!}各校语言技能('+type+')统计表',
                                    subtext: '数据来自一起作业'
                                },
                                grid           : {'y': 80, 'y2': 100},
                                xAxis          : [{
                                    type     : 'value'
                                }],
                                yAxis          : [{
                                    type: 'value',
                                    max : 100,
                                    boundaryGap: true,
                                    scale: true
                                }],
                                tooltip        : {
                                    show     : true,
                                    formatter: function(params){
                                        if(params.name.substring(0, 2) == "做题"){
                                            return params.name + '：' + params.value;
                                        }else{

                                            return params.name + '<br/>做题正确率：' + params.value[1].toFixed(2) + '%<br/>做题总量&nbsp;&nbsp;&nbsp;：' + params.value[0];
                                        }
                                    }
                                },
                                series         : [
                                    {
                                        'type'    : 'scatter',
                                        markPoint : {
                                            symbolSize: 0,
                                            itemStyle: {
                                                normal: {
                                                    borderColor: '#87cefa',
                                                    label: {
                                                        formatter: function (p, n, v) {    //n name   v->value
                                                            return n;
                                                        },
                                                        textStyle: {
                                                            color: '#09f'
                                                        }
                                                    },
                                                    show: true
                                                }

                                            },
                                            data:[
                                                {name: '做题正确率(%)', x: 70, y: 55},
                                                {name: '做题总量', x: 670, y: 300}
                                            ]
                                        },
                                        markLine  : {
                                            data: [
                                                {
                                                    type     : 'average',
                                                    name     : '做题正确率平均值',
                                                    itemStyle: {
                                                        normal: {
                                                            borderColor: 'red'
                                                        }
                                                    }
                                                },
                                                {
                                                    type      : 'average',
                                                    name      : '做题总量平均值',
                                                    valueIndex: 0,
                                                    itemStyle : {
                                                        normal: {
                                                            borderColor: 'red'
                                                        }
                                                    }
                                                }
                                            ]
                                        },
                                        symbolSize: 8,
                                        'data'    : dataFormatter(pList,rate['${date}'],sum['${date}'])
                                    }
                                ]
                            }
                        <#else>
                            {
                                title : {'text': '${date} ${(currentUser.formatManagedRegionStr())!}各校语言技能('+type+')统计表'},
                                series: [
                                    {'data': dataFormatter(pList,rate['${date}'],sum['${date}'])}
                                ]
                            }
                        </#if>
                        <#if date_has_next>,</#if>
                    </#list>
                ]
            };
            return _options;
        }

        <#if skillMonthMapper.listening?has_content>
        var listen = (function(){
            var listen_chart = echarts.init(document.getElementById('listening_chart'));
            var pList = ${json_encode(skillMonthMapper.listening.names)};
            var rate = ${json_encode(skillMonthMapper.listening.monthlyRate)};

            var sum = ${json_encode(skillMonthMapper.listening.monthlySum)};

            var option = generateOptionsParam(pList,rate,sum,'听');
            listen_chart.setOption(option);
        })();
        </#if>

        <#if skillMonthMapper.speaking?has_content>
        var speaking = (function(){
            var speaking_chart = echarts.init(document.getElementById('speaking_chart'));
            var pList = ${json_encode(skillMonthMapper.speaking.names)};
            var rate = ${json_encode(skillMonthMapper.speaking.monthlyRate)};
            var sum = ${json_encode(skillMonthMapper.speaking.monthlySum)};

            var option = generateOptionsParam(pList,rate,sum,'说');
            speaking_chart.setOption(option);
        })();
        </#if>
        <#if skillMonthMapper.reading?has_content>
        var reading = (function(){
            var reading_chart = echarts.init(document.getElementById('reading_chart'));
            var pList = ${json_encode(skillMonthMapper.reading.names)};
            var rate = ${json_encode(skillMonthMapper.reading.monthlyRate)};

            var sum = ${json_encode(skillMonthMapper.reading.monthlySum)};

            var option = generateOptionsParam(pList,rate,sum,'读');
            reading_chart.setOption(option);
        })();
        </#if>
        <#if skillMonthMapper.written?has_content>
        var written = (function(){
            var written_chart = echarts.init(document.getElementById('written_chart'));
            var pList = ${json_encode(skillMonthMapper.written.names)};
            var rate = ${json_encode(skillMonthMapper.written.monthlyRate)};

            var sum = ${json_encode(skillMonthMapper.written.monthlySum)};

            var option = generateOptionsParam(pList,rate,sum,'写');
            written_chart.setOption(option);
        })();
        </#if>
    });
</script>
<#else>
暂无相关数据
</#if>