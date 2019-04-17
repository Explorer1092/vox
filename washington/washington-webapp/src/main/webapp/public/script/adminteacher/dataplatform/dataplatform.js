/**
 * @author: zhaoyi.li
 * @description: "AI智能教育监管平台"
 * @createdDate: 2019/3/1
 * @lastModifyDate: 2019/3/1
 */
// YQ: 通用方法(public/script/YQ.js),
// impromptu: 通用弹窗（http://trentrichardson.com/Impromptu/）,
// voxLogs: 打点
define(["jquery", "vue", "YQ", "echarts", "voxLogs", "screenfull"],function($, Vue, YQ, echarts) {
    // 注：该项目无编译机制，不要使用es6语法

    // 打点方法
    function doTrack () {
        var track_obj = {
            database: 'tianshu_logs',
            module: 'm_rXBC8BoT60'
        };
        for (var i = 0; i < arguments.length; i++) {
            if (i === 0) {
                track_obj['op'] = arguments[i];
            } else {
                track_obj['s' + (i - 1)] = arguments[i];
            }
        }
        YQ.voxLogs(track_obj);
    }

    var ad_subject_list = ['英语', '数学', '语文'];
    var kl_subject_list = ['英语', '数学'];
    var grade_list = ['一年级','二年级','三年级','四年级','五年级','六年级'];
    var date = new Date,
        year = date.getFullYear(),
        month = date.getMonth() + 1;
    var bindingEvent = {
        bind: function (el, binding, vnode) {
            el.clickOutsideEvent = function (event) {
                if (!(el == event.target || el.contains(event.target))) {
                    vnode.context[binding.expression](event)
                }
            };
            document.body.addEventListener('click', el.clickOutsideEvent)
        },
        unbind: function (el) {
            document.body.removeEventListener('click', el.clickOutsideEvent)
        }
    };
    Vue.directive('click-outside-sc',bindingEvent);
    Vue.directive('click-outside-kl',bindingEvent);
    var vues = new Vue({
        el: '#dataPlatform',
        data: {
            school_name: '', // 学校名称
            is_full_screen: false,
            is_edit_error: false, // 输入的总数小于活跃数时报错
            base_data_chart: {}, // 基础数据echart obj
            base_data_list: [], // 基础数据date list

            homework_swiper: null, // 作业分类 swiper
            homework_data_chart_l: {}, // 左边echart obj
            homework_data_chart_r: {}, // 右边echart obj
            homework_data_list: [], // date list
            homework_tab_index: 0, // tab切换
            homeWork_assigned_num: 0, // 布置作业总次数
            resource_download_num: 0, // 资源下载总次数
            exam_participate_num: 0, // 学科测评总次数
            exam_no_name_list: [], // 学科测评 右上角提示
            activity_articpate_num: 0, // 趣味活动总次数
            // current_date: year + '年' + month + '月', // 当前年月
            current_date: '',

            massage_list: [], // 实时动态
            timer: null, // 实时动态定时器

            ad_subject_swiper: null, // 学科能力养成 swiper obj
            ability_develop_term: '', // 学期
            ability_develop_subject: '', // 学科
            ability_develop_grade_index: 1, // 当前选中年级
            ability_develop_grade_list: grade_list, // 年级列表
            show_subject_clazzs_tool: false, // 年级列表展开收起
            ad_data_chart: {}, // echart obj
            ad_data_list: [], // data list

            knowledge_swiper: null, // 知识版块掌握 swiper obj
            knowledge_swiper_term: '', // 学期
            knowledge_subject: '', // 学科
            knowledge_grade_index: 1, // 当前选中年级
            knowledge_grade_list: grade_list, // 年级列表
            show_knowledge_grade_tool: false, // 年级列表展开收起
            knowledge_data_chart: {}, // echarts obj
            knowledge_data_list: [], // data list
        },
        methods: {
            /* 全屏显示 */
            screen_full: function () {
                if (screenfull.enabled) screenfull.toggle();
                doTrack('click_data_supervision_page_full_half_screen')
            },
            /* 切换简洁版 */
            switch_concise_version: function () {
                doTrack('click_data_supervision_page_switch_version')
                window.location.href = '/schoolmaster/generaloverview.vpage';
            },
            get_current_date: function () {
                var vm = this;
                $.ajax({
                    url: '/schoolmasterHomepage/loadCurrentInfo.vpage',
                    type: 'GET',
                    success: function (res) {
                        if (res.success) {
                            vm.current_date = res.currentDate;
                        }
                    }
                });
            },
            /* 基础数据 */
            get_base_data: function () {
                var vm = this;
                $.ajax({
                    url: '/schoolmasterHomepage/loadBaseData.vpage',
                    type: 'GET',
                    success: function (res) {
                        if (res.result) {
                            var data = res.data;
                            vm.homeWork_assigned_num = res.data.homeWorkAssignedNum;
                            vm.resource_download_num = res.data.teachResourceDownloadNum;
                            vm.exam_participate_num = res.data.examParticipateNum;
                            vm.activity_articpate_num = res.data.activityParticpateNum;
                            vm.school_name = res.data.schoolName;
                            vm.base_data_list = [{
                                title: '班级活跃率',
                                tip_name: '活跃班级/总班级数',
                                active_num: data.activeClazzNum,
                                total_num: data.clazzs,
                                ratio: Math.round((isNaN(data.activeClazzNum / data.clazzs) ? 0 : (data.activeClazzNum / data.clazzs)) * 100),
                                editing: false,
                                edit_num: data.clazzs
                            }, {
                                title: '老师活跃率',
                                tip_name: '活跃老师/老师总数',
                                active_num: data.activeTeacherNum,
                                total_num: data.totalTeacherNum,
                                ratio: Math.round((isNaN(data.activeTeacherNum / data.totalTeacherNum) ? 0 : (data.activeTeacherNum / data.totalTeacherNum)) * 100),
                                editing: false,
                                edit_num: data.totalTeacherNum
                            }, {
                                title: '学生活跃率',
                                tip_name: '活跃学生/学生总数',
                                active_num: data.activeStudentNum,
                                editing: false,

                                total_num: data.students,
                                ratio: Math.round((isNaN(data.activeStudentNum / data.students) ? 0 : (data.activeStudentNum / data.students)) * 100),
                                edit_num: data.students

                                /*total_num: 100,
                                ratio: Math.round((isNaN(data.activeStudentNum / 100) ? 0 : (data.activeStudentNum / 100)) * 100),
                                edit_num: 100*/
                            }];

                            vm.$nextTick(function () {
                                vm.draw_base_chart();
                            });
                        }
                    }
                })
            },
            // 绘制基础数据
            draw_base_chart: function () {
                var vm = this;
                for (var i = 0; i < vm.base_data_list.length; i++) {
                    vm.base_data_chart['baseDataChart' + i] = echarts.init(document.getElementsByClassName('baseDataChart')[i]);
                    if (vm.base_data_list[i].total_num === null) {
                        vm.base_data_list[i].ratio = Infinity;
                    } else if (vm.base_data_list[i].total_num === 0) {
                        vm.base_data_list[i].ratio = 0;
                    } else if (vm.base_data_list[i].active_num > vm.base_data_list[i].total_num) {
                        vm.base_data_list[i].ratio = 100;
                    }
                    vm.set_base_option(i, vm.base_data_list[i].ratio);
                }
            },
            // 设置基础数据表表option，参数index表示第几个，ratio表示占比
            set_base_option: function (index, ratio) {
                var vm = this;
                vm.base_data_chart['baseDataChart' + index].setOption({
                    tooltip: {
                        show: false,
                        trigger: 'none',
                        triggerOn: 'none'
                    },
                    graphic: {
                        type: 'text',
                        left: 'center',
                        top: 'center',
                        zlevel: 100,
                        style: {
                            text: ratio === Infinity ? '?' :  ratio + '%',
                            font: 'normal 24px "Microsoft YaHei"',
                            fill: '#fff'
                        }
                    },
                    calculable: true,
                    series: [
                        {
                            name:'访问来源',
                            type:'pie',
                            radius: ['75%', '100%'],
                            selectedOffset: 10,
                            hoverAnimation:false,
                            legendHoverLink: false,
                            clockwise: false,
                            startAngle: 90, // 开始角
                            labelLine: {
                                normal: {
                                    show: false
                                }
                            },
                            data:[
                                {
                                    value: ratio === Infinity ? 0 : ratio,
                                    itemStyle: {
                                        color: {
                                            type: 'linear',
                                            x: 0,
                                            y: 0,
                                            x2: 1,
                                            y2: 0,
                                            colorStops: [{
                                                offset: 1, color: '#00E7F6' // 0% 处的颜色
                                            }, {
                                                offset: 0, color: '#0094F1' // 100% 处的颜色
                                            }],
                                        }
                                    }
                                },
                                {
                                    value: ratio === Infinity ? 100 : 100 - ratio,
                                    itemStyle: {
                                        color: 'rgba(35,41,67,1)'
                                    }
                                },
                            ]
                        }
                    ]
                });
            },
            // 编辑总数
            edit_base_data: function (baseData) {
                var vm = this;
                vm.base_data_list.map(function (item) { return item.editing = false; });
                baseData.editing = !baseData.editing;
                if (baseData.total_num < baseData.active_num ) vm.is_edit_error = true;
            },
            // 输入时
            handle_input: function (base_data, active_num) {
                var vm = this;
                vm.$set(base_data, 'edit_num', base_data.edit_num.replace(/[^0-9]/ig, ''));
                if (base_data.edit_num && base_data.edit_num < active_num) {
                    vm.is_edit_error = true;
                } else {
                    vm.is_edit_error = false;
                }
            },
            // 确认编辑（点击确定或回车）
            edit_sure: function(baseData, index) {
                var vm = this;
                if (vm.is_edit_sure) return;
                if (+baseData.edit_num < baseData.active_num) return;
                baseData.editing = !baseData.editing;
                baseData.total_num = +baseData.edit_num;
                baseData.ratio = Math.round((baseData.active_num / baseData.total_num) * 100);
                vm.set_base_option(index, baseData.ratio);
                doTrack('click_data_supervision_page_modify_basic_data', baseData.total_num)
            },
            /* 获取作业统计数据  */
            get_homework_data: function (url) {
                var vm = this;
                $.ajax({
                    url: '/schoolmasterHomepage/'+ url +'.vpage',
                    type: 'GET',
                    success: function (res) {
                        if (res.result) {
                            if(res.data && res.data.length ) vm.homework_data_list = res.data;

                            // 筛选空数据
                            for(var f = 0; f < vm.homework_data_list.length; f += 1){
                                if (!vm.homework_data_list[f].listData){
                                    vm.$set(vm.homework_data_list[f], 'is_empty', true);
                                } else {
                                    vm.$set(vm.homework_data_list[f], 'is_empty', false);
                                }
                            }
                            vm.$nextTick(function () {
                                if (vm.homework_swiper) {
                                    vm.homework_swiper.slideTo(0);
                                    vm.homework_swiper.destroy(true);
                                    vm.homework_swiper = null;
                                }
                                vm.init_homework_swiper();
                            });
                        }
                    }
                });
            },
            /* 作业类型tab切换 */
            switch_homework_tab: function (index) {
                var vm = this;
                var url = '';
                vm.homework_tab_index = index;
                switch (index) {
                    case 0:
                        // 布置作业
                        url = 'loadHomeWorkData';
                        doTrack('click_data_supervision_page_homework');
                        break;
                    case 1:
                        // 资源下载
                        url = 'loadTeachResourceData';
                        doTrack('click_data_supervision_page_resource_download');
                        break;
                    case 2:
                        // 学科测评
                        url = 'loadExamData';
                        doTrack('click_data_supervision_page_subject_evaluation');
                        break;
                    case 3:
                        // 趣味活动
                        url = 'loadActivityData';
                        doTrack('click_data_supervision_page_activities');
                        break;
                }
                vm.homework_data_list = [];
                vm.get_homework_data(url);
            },
            /* 作业类型 swiper */
            init_homework_swiper: function () {
                var vm = this;
                // 轮播图
                vm.homework_swiper = new Swiper('.homework-swiper', {
                    loop: false,
                    autoplay: 6000,
                    observer:true,
                    observeParents:true,
                    autoplayDisableOnInteraction: false,
                    pagination: '.homework-pagination',
                    paginationClickable: true,
                    onInit: function(swiper) {
                        var pagationList = $('.homework-pagination span');
                        vm.set_ad_swiper_pagation(pagationList,ad_subject_list);
                        vm.set_homework_option(swiper.activeIndex,vm.homework_data_list[swiper.activeIndex]);
                        if (vm.homework_data_list[swiper.activeIndex].examNoNameList) {
                            vm.exam_no_name_list = vm.homework_data_list[swiper.activeIndex].examNoNameList;
                        } else {
                            vm.exam_no_name_list = [];
                        }
                    },
                    onSlideChangeStart: function(swiper) {
                        vm.set_homework_option(swiper.activeIndex,vm.homework_data_list[swiper.activeIndex]);
                        if (vm.homework_data_list[swiper.activeIndex].examNoNameList) {
                            vm.exam_no_name_list = vm.homework_data_list[swiper.activeIndex].examNoNameList;
                        } else {
                            vm.exam_no_name_list = [];
                        }
                    },
                    onAfterResize: function () {
                        var pagationList = $('.homework-pagination span');
                        vm.set_ad_swiper_pagation(pagationList,ad_subject_list);
                    }

                });
            },
            // 格式化图表数据 chart_obj
            set_homework_option: function (index, da_data) {
                var vm = this;
                if (da_data.is_empty) return;
                // 当swiper autoplay模式下已经播放过一遍后，需要将EChart清除，才会重新播放
                if (vm.homework_data_chart_l['homeworkLeftChart' + index] || vm.homework_data_chart_r['homeworkRightChart' + index] ) {
                    vm.homework_data_chart_l['homeworkLeftChart' + index].clear();
                    vm.homework_data_chart_r['homeworkRightChart' + index].clear();
                }
                vm.homework_data_chart_l['homeworkLeftChart' + index] = echarts.init(document.getElementsByClassName('homeworkLeftChart')[index]);
                vm.homework_data_chart_r['homeworkRightChart' + index] = echarts.init(document.getElementsByClassName('homeworkRightChart')[index]);
                for(var i = 0; i < da_data.listData.length; i += 1){
                    if(da_data.listData[i].type === 'pie') {
                        vm.homework_data_chart_l['homeworkLeftChart' + index].setOption(
                            vm.draw_pie_chart(
                                index,
                                da_data.listData[i].seriesData
                            )
                        );
                    } else if (da_data.listData[i].type === 'bar') {
                        vm.homework_data_chart_r['homeworkRightChart' + index].setOption(
                            vm.draw_bar_chart(
                                da_data.listData[i].xAxisData,
                                da_data.listData[i].seriesData
                            )
                        );
                    } else if (da_data.listData[i].type === 'colorBar') {
                        vm.homework_data_chart_r['homeworkRightChart' + index].setOption(
                            vm.draw_yAxis_bar_chart(
                                da_data.listData[i].legendData,
                                da_data.listData[i].yAxisData,
                                da_data.listData[i].seriesData
                            )
                        );
                    } else if (da_data.listData[i].type === 'flow') {
                        vm.homework_data_chart_r['homeworkRightChart' + index].setOption(
                            vm.draw_polyline_chart(
                                da_data.listData[i].xAxisData,
                                da_data.listData[i].yAxisData,
                                da_data.listData[i].seriesData
                            )
                        );
                    }
                }
            },
            /* 绘制饼图 */
            draw_pie_chart: function (index, data) {
                 return {
                    legend: {
                        orient: 'horizontal',
                        left: 'center',
                        top: '70%',
                        // bottom: '10',
                        itemWidth:10,
                        itemHeight:10,
                        itemGap: 8,
                        selectedMode:false,
                        textStyle:{
                            padding:[0,5],
                            lineHeight: 20,
                            fontSize:12,
                            // color:'#B6B9BE'
                            color: '#fff'
                        },
                        formatter:  function(name){
                            var total = 0, target;
                            for (var v = 0; v < data.length; v += 1) {
                                total += data[v].value;
                                if (data[v].name === name) {
                                    target = data[v].value;
                                }
                            }
                            var arr = [
                                Math.round((target/total) * 100)+'%',
                                name
                            ];
                            return arr.join('\n');
                        },
                        icon: 'circle'
                    },
                    tooltip: {
                        formatter: function(data){
                            var arr = [
                                data.name + ' : ' + (Math.round(data.percent)) + '%'
                            ];
                            return arr
                        }
                    },
                    graphic:{
                        type:'text',
                        left:'center',
                        top:'34%',
                        style:{
                            text: ad_subject_list[index],
                            textAlign:'center',
                            fill:'#fff',
                            fontSize:17
                        }
                    },
                    series : [
                        {
                            type:'pie',
                            radius : ['25%', '55%'],
                            center : ['50%', '36%'],
                            roseType : 'radius',
                            color:['#2fe8f5', '#7c4ff3','#494ed9','#a140c3'],
                            hoverAnimation:false,
                            clockwise: false,
                            startAngle: 90, // 开始角
                            label: {
                                normal: {
                                    show: false
                                }
                            },
                            data: data
                        }
                    ]
                };
            },
            /* 绘制柱状图 */
            draw_bar_chart: function (xAxisData,seriesData) {
                return {
                    color: ['#00E7F6'],
                    tooltip : {
                        trigger: 'axis',
                        confine: true,
                        axisPointer: {
                            type: 'none'
                        },
                        formatter: '{b} : {c}'
                    },
                    grid: {
                        left: '2%',
                        right: '5%',
                        top: 40,
                        bottom: '15%',
                        containLabel: true,
                        borderColor: '#fff',
                    },
                    xAxis : {
                        type : 'category',
                        data: xAxisData,
                        offset: 4,
                        axisTick: {
                            alignWithLabel: true,
                            length: 6,
                        },
                        axisLine: {
                            show: true,
                            lineStyle: {
                                color: '#B6B9BE',
                                width: 1,
                            }
                        }
                    },
                    yAxis : {
                        type : 'value',
                        nameGap: 0,
                        axisLine: {
                            show: true,
                            lineStyle: {
                                color: '#B6B9BE',
                                width: 1,
                            }
                        },
                        splitLine: {
                            show: false,
                        }
                    },
                    series : [
                        {
                            name:'直接访问',
                            type:'bar',
                            barWidth: '30',
                            // data:[10, 52, 200, 334, 390, 330]
                            data: seriesData
                        }
                    ]
                };
            },
            /* 绘制多色横向柱状图 */
            draw_yAxis_bar_chart: function (legendData,yAxisData,seriesData) {
                var data = [];
                for (var k = 0; k < seriesData.length; k++) {
                    data.push({
                        name: seriesData[k].name,
                        type: 'bar',
                        barCategoryGap:'40%',
                        stack: '总量',
                        label: {
                            normal: {
                                show: false,
                                position: 'insideRight'
                            }
                        },
                        data: seriesData[k].data,
                    });
                }
                return {
                    color:['#00E7F6', '#7760F5','#5187FF','#A23FC5','#1DD37E','#FF399B','#D688FF','#FF8E43','#FFDC43','#04CF18','#FF693F','#FF5959'],
                    /*legend: {
                        left: 'center',
                        bottom: '12%',
                        itemGap: 10,
                        textStyle: {
                            color: '#B6B9BE',
                        },
                        data: legendData,
                    },*/
                    tooltip: {
                        trigger: 'axis',
                        axisPointer : {
                            type : 'none'
                        }
                    },
                    grid: {
                        left: '2%',
                        right: '10%',
                        top: '10%',
                        bottom: '16%',
                        containLabel: true,
                        borderColor: '#fff',
                    },
                    xAxis:  {
                        type: 'value',
                        //x轴字体的样式
                        axisLabel: {
                            show: true,
                            textStyle: {
                                color: '#B6B9BE',
                                fontSize:'12'
                            }
                        },
                        //网格是否显示
                        splitLine: {
                            show: false,
                        },
                        //x轴颜色宽度
                        axisLine:{
                            lineStyle:{
                                color:'#B6B9BE',
                                width:1,
                            }
                        },
                        // max:100,
                        splitNumber:5
                    },
                    yAxis: {
                        type: 'category',
                        //y轴字体的样式
                        axisLabel: {
                            show: true,
                            textStyle: {
                                color: '#B6B9BE',
                                fontSize:'12'
                            }
                        },
                        axisTick: {
                            alignWithLabel: true
                        },
                        //y轴颜色宽度
                        axisLine:{
                            lineStyle:{
                                color:'#B6B9BE',
                                width:1
                            }
                        },
                        offset:5,  //刻度值与坐标轴的间距
                        // data: ['一年级','二年级','三年级','四年级','五年级','六年级']
                        data: yAxisData
                    },
                    series: data
                }
            },
            /* 绘制折线图 */
            draw_polyline_chart: function (xAxisData,yAxisData,seriesData) {
                return {
                    tooltip : {
                        trigger: 'axis',
                        axisPointer: {
                            type: 'cross',
                            animation: false,
                            label: {
                                backgroundColor: 'rgba(0,0,0,.5)'
                            },
                            lineStyle:{
                                color:'#fff',
                            },
                            crossStyle: {
                                color:'#fff',
                            }
                        },
                        // formatter:'{a}<br>{b} : {c}'
                    },
                    grid: {
                        left: '13%',
                        right: '13%',
                        bottom: '20%'
                    },
                    xAxis : [
                        {
                            type : 'category',
                            boundaryGap : false,
                            data: xAxisData,
                            axisLine: {
                                show: true,
                                onZero: false,
                                lineStyle: {
                                    color: '#fff',
                                    width:1
                                }
                            }
                        }
                    ],
                    yAxis: [
                        {
                            name: yAxisData[0].name,
                            type: 'value',
                            // max: 100, //刻度最大值
                            nameTextStyle:{
                                color: '#fff',
                                fontSize: '14'
                            },
                            axisLine: {
                                show: true,
                                lineStyle: {
                                    color: '#fff',
                                    width:1,
                                }
                            },
                            splitLine: {
                                show: false
                            },
                        },
                        {
                            name: yAxisData[1].name,
                            nameLocation: 'start',
                            // max: 100, //刻度最大值
                            type: 'value',
                            inverse: true, //右侧Y坐标轴刻度反向
                            nameTextStyle:{
                                color: 'fff',
                                fontSize: '14',
                            },
                            axisLine: {
                                show: true,
                                lineStyle: {
                                    color: '#fff',
                                    width:1,
                                }
                            },
                            splitLine: {
                                show: false
                            },
                        }
                    ],
                    series: [
                        {
                            name:seriesData[0].name,
                            type:'line',
                            symbol: 'none',
                            areaStyle: {
                                color: 'rgba(0,231,246,.3)',
                            },
                            lineStyle: {
                                width: 2,
                                color: '#00E7F6',
                                shadowBlur: 20,
                                shadowColor: '#00E7F6'
                            },
                            color: '#00E7F6',
                            data: seriesData[0].data
                        },
                        {
                            name:seriesData[1].name,
                            type:'line',
                            yAxisIndex:1,
                            symbol: 'none',
                            areaStyle: {
                                color: 'rgba(125,77,246,.3)',
                            },
                            lineStyle: {
                                width: 2,
                                color: '#7D4DF6',
                                shadowBlur: 20,
                                shadowColor: '#7D4DF6'
                            },
                            color: '#7D4DF6',
                            data: seriesData[1].data
                        }
                    ]
                }
            },
            /* 获取动态数据列表 */
            get_massage_data: function () {
                var vm = this;
                $.ajax({
                    url: '/schoolmasterHomepage/loadMsgList.vpage',
                    type: 'GET',
                    success: function (res) {
                        if (res.result) {
                            if(res.data && res.data.length) {
                                vm.massage_list = res.data;
                                vm.$nextTick(function () {
                                    vm.scroll_message();
                                    var dynamicList = $('#dynamicBox').find('.dynamic-list');
                                    for (var i = 0; i < dynamicList.length; i += 1) {
                                        dynamicList.eq(i*2).addClass('txt-color');
                                    }
                                });
                            }
                        }
                    }
                });
            },
            /* 实时动态 */
            scroll_message: function () {
                var vm = this;
                var listHeight = $('#dynamicList-1').height(),
                    listBox1 = $('#dynamicList-1'),
                    listBox2 = $('#dynamicList-2');
                var count = 0;

                clearInterval(vm.timer);
                vm.timer = setInterval(function () {
                    listBox1.css('top', count);
                    listBox2.css('top', count + listHeight);
                    count -= 1;
                    if ((count + listHeight) === 0 ) {
                        count = 0;
                    }
                },20);
            },
            /* 学科能力养成 */
            get_subject_ability_data: function () {
                var vm = this;
                $.ajax({
                    url: '/schoolmasterHomepage/loadLearningSkillsData.vpage',
                    type: 'GET',
                    data: {
                        grade: vm.ability_develop_grade_index
                    },
                    success: function (res) {
                        if (res.result) {
                            vm.ability_develop_term = res.currentTerm;
                            if(res.data && res.data.length) vm.ad_data_list = res.data;
                            // 筛选空数据
                            for(var f = 0; f < vm.ad_data_list.length; f += 1){
                                if (!vm.ad_data_list[f].legendData){
                                    vm.$set(vm.ad_data_list[f], 'is_empty', true);
                                } else {
                                    vm.$set(vm.ad_data_list[f], 'is_empty', false);
                                }
                            }
                            vm.$nextTick(function () {
                                if (vm.ad_subject_swiper) {
                                    vm.ad_subject_swiper.slideTo(0);
                                    vm.ad_subject_swiper.destroy(true);
                                    vm.ad_subject_swiper = null;
                                }
                                vm.init_ad_subject_swiper();
                            });
                        }
                    }
                });
            },
            /* 学科能力养成swiper */
            init_ad_subject_swiper: function () {
                var vm = this;
                // 轮播图
                vm.ad_subject_swiper = new Swiper('.ad-subject-swiper', {
                    loop: false,
                    autoplay: 3000,
                    observer:true,
                    observeParents:true,
                    autoplayDisableOnInteraction: false,
                    pagination: '.subject-pagination',
                    paginationClickable: true,
                    onInit: function(swiper) {
                        var pagationList = $('.subject-pagination span');
                        vm.set_common_option(vm.ad_data_chart, 'subjectAbilityChart', swiper.realIndex, vm.ad_data_list[swiper.realIndex]);
                        vm.set_ad_swiper_pagation(pagationList,ad_subject_list);
                        vm.ability_develop_subject = ad_subject_list[swiper.realIndex];
                    },
                    onSlideChangeStart: function(swiper) {
                        vm.set_common_option(vm.ad_data_chart, 'subjectAbilityChart', swiper.realIndex, vm.ad_data_list[swiper.realIndex]);
                        vm.ability_develop_subject = ad_subject_list[swiper.realIndex];
                    },
                    onAfterResize: function(){
                        var pagationList = $('.subject-pagination span');
                        vm.set_ad_swiper_pagation(pagationList,ad_subject_list);
                    }
                });
            },
            // 设置公共swiper pagation text
            set_ad_swiper_pagation: function (elements,arrList) {
                // 将swiper的pagation导航修改
                for (var i = 0; i < elements.length; i += 1) {
                    elements.eq(i).text(arrList[i]);
                }
            },
            // 设置（学科能力养成/知识版块掌握）表option，参数：chart_obj表示图表对象，chart_name表示图表className，index表示第几个，da_data表示当前图表需要的数据
            set_common_option: function (chart_obj, chart_name, index, da_data) {
                var vm = this;
                if (da_data.is_empty) return;
                var area_color_arr = ['#00E7F6', '#ff00e9', '#ffed00']; // 饼图区块和边框底色
                var shadow_color_arr = ['#19e9f7', '#ff81f4', '#fff56e']; // 饼图阴影
                var data = [];
                for (var k = 0; k < da_data.legendData.length; k++) {
                    data.push({
                        name: da_data.legendData[k].name,
                        value: da_data.legendData[k].data.map(function (item) {
                            return Math.round(item * 100);
                        }),
                        symbolSize: 4,
                        symbol: 'emptyCircle',
                        smooth: false,
                        itemStyle: {
                            normal: {
                                color: area_color_arr[k]
                            }
                        },
                        lineStyle: {
                            shadowBlur: 20,
                            shadowColor: shadow_color_arr[k]
                        }
                    });
                }
                // 当swiper autoplay模式下已经播放过一遍后，需要将EChart清除，才会重新播放
                if (chart_obj[chart_name + index]) {
                    chart_obj[chart_name + index].clear();
                }
                chart_obj[chart_name + index] = echarts.init(document.getElementsByClassName(chart_name)[index]);
                chart_obj[chart_name + index].setOption({
                    legend: {
                        bottom:'10',
                        right:'10',
                        orient:'vertical',
                        itemWidth:24,
                        itemHeight:1,
                        itemGap:10,
                        textStyle: {
                            color: '#B6B9BE',
                            fontSize: 12
                        },
                        selectedMode: false
                    },
                    radar: {
                        indicator: da_data.indicatorData,
                        center:['50%', '46%'],
                        radius:"60%",
                        shape: 'polygon',
                        splitNumber: 8,
                        nameGap: 13,
                        name: {
                            textStyle: {
                                color: '#B6B9BE',
                                fontSize: 12
                            },
                            formatter: function (text) {
                                text = text.replace(/\S{4}/g, function (match) {
                                    return match + '\n'
                                });
                                return text
                            }
                        },
                        splitLine: {
                            lineStyle: {
                                color: ['rgba(255, 255, 255, 0.2)']
                            }
                        },
                        splitArea: {
                            show: true,
                            areaStyle: {
                                color: ['rgba(40,41,119,.8)','rgba(30,27,100,.8)','rgba(40,41,119,.6)']
                            }
                        },
                        axisLine: {
                            lineStyle: {
                                color: 'rgba(255, 255, 255, 0.2)'
                            }
                        }
                    },
                    series: [
                        {
                            type: 'radar',
                            lineStyle: {
                                normal: {
                                    width: 1,
                                    opacity: 1
                                }
                            },
                            data: data,
                            symbol: 'none',
                            itemStyle: {
                                normal: {
                                    color: '#F9713C'
                                }
                            },
                            areaStyle: {
                                normal: {
                                    opacity: 0.2
                                }
                            }
                        },
                    ]
                });
                // window.onresize = function () {
                //     chart_obj[chart_name + index].resize();
                // }
            },
            /* 学科能力养成 年级下拉展开收起 */
            show_grade_list: function () {
                var vm = this;
                vm.show_subject_clazzs_tool = !vm.show_subject_clazzs_tool;
            },
            /* 学科能力养成 切换年级 */
            switch_grade_list: function (index) {
                var vm = this;
                vm.ability_develop_grade_index = index;
                vm.show_subject_clazzs_tool = !vm.show_subject_clazzs_tool;
                vm.get_subject_ability_data();
                doTrack('click_data_supervision_page_ability_grade')
            },
            /* 学科能力养成 点击其他区域收起下拉框 */
            click_outside_sc: function () {
                var vm = this;
                vm.show_subject_clazzs_tool = false;
            },
            /* 知识版块掌握 */
            get_knowledge_data: function () {
                var vm = this;
                $.ajax({
                    url: '/schoolmasterHomepage/loadKnowledgeModuleData.vpage',
                    type: 'GET',
                    data: {
                        grade: vm.knowledge_grade_index
                    },
                    success: function (res) {
                        if (res.result) {
                            vm.knowledge_swiper_term = res.currentTerm;
                            if (res.data && res.data.length) vm.knowledge_data_list = res.data;
                            for (var f = 0; f < vm.knowledge_data_list.length; f +=1){
                                if (!vm.knowledge_data_list[f].legendData) {
                                    vm.$set(vm.knowledge_data_list[f], 'is_empty', true);
                                } else {
                                    vm.$set(vm.knowledge_data_list[f], 'is_empty', false);
                                }
                            }
                            vm.$nextTick(function () {
                                if (vm.knowledge_swiper) {
                                    vm.knowledge_swiper.slideTo(0);
                                    vm.knowledge_swiper.destroy(true);
                                    vm.knowledge_swiper = null;
                                }
                                vm.init_knowledge_swiper();
                            });
                        }
                    }
                });
            },
            /* 知识版块掌握 swiper */
            init_knowledge_swiper: function () {
                var vm = this;
                vm.knowledge_swiper = new Swiper('.knowledge-swiper', {
                    loop: false,
                    autoplay: 3000,
                    observer:true,
                    observeParents:true,
                    autoplayDisableOnInteraction: false,
                    pagination: '.knowledge-pagination',
                    paginationClickable: true,
                    onInit: function (swiper) {
                        var pagationList = $('.knowledge-pagination span');
                        vm.set_common_option(vm.knowledge_data_chart, 'knowledgeChart', swiper.realIndex, vm.knowledge_data_list[swiper.realIndex]);
                        vm.set_ad_swiper_pagation(pagationList, kl_subject_list);
                        vm.knowledge_subject = kl_subject_list[swiper.realIndex];
                    },
                    onSlideChangeStart: function(swiper) {
                        vm.set_common_option(vm.knowledge_data_chart, 'knowledgeChart', swiper.realIndex, vm.knowledge_data_list[swiper.realIndex]);
                        vm.knowledge_subject = kl_subject_list[swiper.realIndex];
                    },
                    onAfterResize: function(){
                        var pagationList = $('.knowledge-pagination span');
                        vm.set_ad_swiper_pagation(pagationList, kl_subject_list);
                    }
                });
            },
            /* 知识版块掌握 展开年级列表 */
            show_knowledge_grade: function () {
                var vm = this;
                vm.show_knowledge_grade_tool = !vm.show_knowledge_grade_tool;
            },
            /* 知识版块掌握 切换年级列表 */
            switch_knowledge_grade: function (index) {
                var vm = this;
                vm.knowledge_grade_index = index;
                vm.show_knowledge_grade_tool = !vm.show_knowledge_grade_tool;
                vm.get_knowledge_data();
                doTrack('click_data_supervision_page_knowledge_grade')
            },
            /* 知识版块掌握 点击其他区域收起下拉框 */
            click_outside_kl: function () {
                var vm = this;
                vm.show_knowledge_grade_tool = false;
            },
            /* 监听window resize */
            linten_window_resize: function () {
                var vm = this;
                window.onresize = function () {
                    if (screenfull.isFullscreen) {
                        //恢复
                        vm.is_full_screen = true;
                    } else {
                        //全屏
                        vm.is_full_screen = false;
                    }
                    // 实时动态
                    vm.scroll_message();

                    // 基础数据
                    for (var j = 0; j < vm.base_data_list.length; j++) {
                        if (vm.base_data_chart['baseDataChart' + j]) vm.base_data_chart['baseDataChart' + j].resize();
                    }
                    // 作业类型数据
                    for (var k = 0; k < vm.homework_data_list.length; k++) {
                        if (vm.homework_data_chart_l['homeworkLeftChart' + k]) vm.homework_data_chart_l['homeworkLeftChart' + k].resize();
                        if (vm.homework_data_chart_r['homeworkRightChart' + k]) vm.homework_data_chart_r['homeworkRightChart' + k].resize();
                    }
                    // 学科能力养成
                    for (var l = 0; l < vm.ad_data_list.length; l++) {
                        if (vm.ad_data_chart['subjectAbilityChart' + l]) vm.ad_data_chart['subjectAbilityChart' + l].resize();
                    }
                    // 知识版块
                    for (var m = 0; m < vm.knowledge_data_list.length; m++) {
                        if (vm.knowledge_data_chart['knowledgeChart' + m]) vm.knowledge_data_chart['knowledgeChart' + m].resize();
                    }

                }
            },
            /* 右上角用户操作菜单 */
            nav_operating: function () {
                $(document).on("click",function () {
                    $(".switch-login ").removeClass("showUp");
                }).on("click",".switch-login",function (event) {
                    event.stopPropagation();
                    if ($(this).hasClass("showUp")){
                        $(this).removeClass("showUp").siblings("ul").hide();
                    }else{
                        $(this).addClass("showUp").siblings("ul").show();
                    }
                });
            }
        },
        mounted: function () {
            var vm = this;
            vm.get_base_data();
            vm.get_homework_data('loadHomeWorkData');
            vm.get_massage_data();
            vm.get_subject_ability_data();
            vm.get_knowledge_data();
            vm.nav_operating();
            vm.get_current_date();
            setTimeout(function () {
                vm.linten_window_resize()
            },1000);
            doTrack('eventp_data_supervision_page')
        }
    });
});