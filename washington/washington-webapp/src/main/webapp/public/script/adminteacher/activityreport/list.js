/**
 * @author: wang.chengxian.chen
 * @description: "校长账号-模考统测"
 * @createdDate: 2018.06.19
 * @lastModifyDate: 2018.08.01
 */

// YQ: 通用方法(public/script/YQ.js), knockout-switch-case: knock switch插件, impromptu: 通用弹窗
define(["jquery", "vue", "YQ", "echarts-4.2.0", "html2canvas","echarts-report", "impromptu", "voxLogs"],function($, Vue, YQ, echarts, html2canvas) {
    var databaseLogs = "tianshu_logs"; // 打点的表
    var EChartsTheme = "report"; // echart配色主题，假设配色文件为echarts-adminteacher
    var downloadPdfFlag = false; // 下载padf flag（防止下载中再触发）
    var myDate = new Date();
    var year=myDate.getFullYear()
    var month=myDate.getMonth()+1
   var newData=year+'年'+month+'月'

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
    var vum =new Vue({
        el: '#activitylist',
        data: {
            activityReportList:[],
            activityType:'',
            activityDate:'',
            noDate:false,
            arrowUp1: false,
            allActivity:'',
            activityList:[],
            aind:'',
            tind:'',
            activityTime:'',
            arrowUp2: false,
            activityTimeList:[]

        },
        created:function(){
            var _this=this
            _this.getReportCondition()
            // _this.getReportList()
        },
        methods: {
            closeList:function(){
                if(this.arrowUp1===true){
                    this.arrowUp1=false
                }
            },
            closeTime:function(){
                if(this.arrowUp2===true){
                    this.arrowUp2=false}

            },
            arrowDirectionType: function(){
                if(this.arrowUp1===false){
                    this.arrowUp1=true
                }else if(this.arrowUp1===true){
                    this.arrowUp1=false
                }
            },
            arrowDirectionTime:function(){
                if(this.arrowUp2===false){
                    this.arrowUp2=true
                }else if(this.arrowUp2===true){
                    this.arrowUp2=false
                }
            },
            choiceActive: function(alist,aindex){
                this.aind=aindex
                this.allActivity=alist.name
                this.activityType=alist.activityType
                this.arrowUp1=false
                this.getReportList()
            },
            choiceTime: function(tlist,tindex){
                this.tind=tindex
                this.activityTime=tlist.name
                this.activityDate=tlist.value
                this.arrowUp2=false
                this.getReportList()
            },
            // 活动列表
            getReportList:function(res){
                var vm = this;
                $.ajax({
                    url: '/schoolmaster/activityReport/getActivityReportList.vpage',
                    type: 'POST',
                    data: {
                        activityType:vm.activityType,
                        date:vm.activityDate
                    },
                    success: function (res) {
                        if(res.result===false){
                            vm.noDate === true
                            vm.alertError(res.info);
                            return
                        }
                        vm.activityReportList=res.activityReportList
                    },
                    error: function () {
                        vm.alertError("获取数据失败，请刷新页面重试~");
                    }
                })
            },
            //时间，活动名字列表
            getReportCondition:function(){
                var vm = this;
                $.ajax({
                    url: '/schoolmaster/activityReport/getActivityReportCondition.vpage',
                    type: 'POST',
                    data: {},
                    success: function (res) {
                        if(res.result == 'false'){
                            vm.alertError(res.info);
                            return
                        }
                        vm.activityTimeList=res.dateList
                        vm. activityList=res.activityTypes
                        vm.allActivity=res.activityTypes[0].name
                        vm.activityTime=res.dateList[res.dateList.length-1].name
                        vm.activityDate=res.dateList[res.dateList.length-1].value
                    },
                    error: function () {
                        vm.alertError("获取数据失败，请刷新页面重试~");
                    },
                    complete: function () {
                        vm.getReportList(vm.activityDate)
                    }
                })
            },
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

