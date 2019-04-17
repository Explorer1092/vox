/**
 * 报告 2018.11.15
 * Tsingjing
 */
;(function(){
    var moduleIdArr = new Array();
    var dataArr = new Array();
    var dataTitleArr = new Array();
    var status = false;
    var info = "";
    var that = this;
    var even_bg_color = null;
    var odd_bg_color =  null;
    var if_dot_bottom  = false;
    var loaddingData = null;
    getDataAjax(1,function (res,params) {
        if(res.success == true){
            dataTitleArr.push({
                module_id:params.module_id,
                dataMap:res.data
            });
            for(var i=0;i<res.data.catalog.length;i++){
                moduleIdArr.push(res.data.catalog[i].moduleId);
            }
            for(var j=0;j<moduleIdArr.length;j++){
                getDataAjax(moduleIdArr[j],function (res,params) {
                    dataArr.push({
                        module_id:params.module_id,
                        dataMap:res.data
                    });
                });
            }
        }else{
            status = true;
            info = res.info;
        }
    });
    function getDataAjax(module_id,func) {
        var url = "/teacher/newexam/report/detailsanalysis.vpage";
        var params = {
            class_id:$17.getQuery("clazzId"),
            exam_id:$17.getQuery("newExamId"),
            module_id:module_id
        }
        $.ajax(url,{
            method   : 'GET',
            data:params,
            dataType : 'json'
        }).done(function( res, textStatus, jqXHR ){
            func(res,params);
        }).fail(function(jqXHR, textStatus, e){

        }).always(function(){

        });
    }
    function initData(param,echart) {
        /**
         * param = 1 返回颜色
         * param = 2 返回icon data
         * param = 3 返回数据 data
         */
        if(param == 1){
            var color = [];
            for(var i=0;i<echart.echartData.length;i++){
                color.push(echart.echartData[i].color)
            }
            return color;
        }else if(param == 2){
            var objArr = [];
            for(var i=0;i<echart.echartData.length;i++){
                objArr.push({
                    name:echart.echartData[i].rank,
                    icon:"circle"
                })
            }
            return objArr;
        }else if(param == 3){
            var objArr = [];
            for(var i=0;i<echart.echartData.length;i++){
                objArr.push({
                    value:echart.echartData[i].num,
                    name: echart.echartData[i].rank,
                })
            }
            return objArr;
        }
    }
    function Ztfxreport(options){
        $17.voxLog({
            module : "m_yJO2o3u3",
            op     : "o_CZPalTTN02",
            s0     : $17.getQuery("newExamId")
        });
        loaddingData = Date.parse(new Date());
        this.initDomEvent()
        for(var i=0;i<dataArr.length;i++){ //对ajax 的数据排序
            for(var j=i+1;j<dataArr.length;j++){
                if(dataArr[i].module_id>dataArr[j].module_id){
                    var temp = dataArr[i];
                    dataArr[i] = dataArr[j];
                    dataArr[j] = temp;
                }
            }
        };
        this.ajaxTitleData = ko.observable(dataTitleArr);
        this.ajaxData = ko.observable(dataArr);
        this.status = function () {
            if(status){
                return true
            }else{
                return false
            }
        };
        this.info = function () {
            return info
        };
        this.initMap = function (param,echart){ //饼图
            $("#container"+param).html("");
            var myChart = echarts.init(document.getElementById('container'+param));
            var option = {
                title : {
                    text: '',
                    x:'center',
                    bottom:4,
                    textStyle:{
                        color:"#333333",
                        fontSize:"12",
                    }
                },
                tooltip: {
                    trigger: 'item',
                    formatter: "{b}:{d}%"//"{a} <br/>{b}: {c} ({d}%)"
                },
                color:initData(1,echart),
                legend: {
                    selectedMode:false,//取消图例上的点击事件
                    orient: 'vertical',
                    right: 40,
                    top:120,
                    data: initData(2,echart),
                },
                series: [
                    {
                        // name:'',
                        type:'pie',
                        radius: ['0%', '70%'],
                        center: ['36%', '46%'],
                        avoidLabelOverlap: false,
                        label: {
                            normal: {
                                position: 'inner',
                                formatter: '{d}%'
                            }
                        },
                        labelLine: {
                            normal: {
                                show: false
                            }
                        },
                        data:initData(3,echart),
                    }
                ]
            };
            myChart.setOption(option);
            return ""
        };
        this.itemClick = function (block,index,title) {
            $(".item-h1").removeClass("on");
            $(".item-p li").removeClass("on");
            $("#"+block+"-"+index+'xx').addClass("on")
            //大标题block 为 0 小标题是module_id
            $('html,body').animate({scrollTop: $("#"+block+"-"+index).offset().top},'slow');
            if(block == "0"){//大标题
                $17.voxLog({
                    module : "m_yJO2o3u3",
                    op     : "o_C2Dp3A1U3u",
                    s0     : $17.getQuery("newExamId"),
                    s1     : title.title,
                    s2     : "title",
                    s3     : Math.ceil((Date.parse(new Date())-loaddingData)/1000),
                });

            } else{//小标题
                $17.voxLog({
                    module : "m_yJO2o3u3",
                    op     : "o_C2Dp3A1U3u",
                    s0     : $17.getQuery("newExamId"),
                    s1     : title,
                    s2     : "childTitle",
                    s3     : Math.ceil((Date.parse(new Date())-loaddingData)/1000),
                });
            }
        };
        this.itemShowHide = function (block,index) {

            if($("#"+block+"-"+index+"xx").next(".item-p").css("display") == 'block'){
                $("#"+block+"-"+index+"xx").next(".item-p").hide("fast");
                $("#"+block+"-"+index+"xx").children(".icon_a").addClass("icon_x")
            }else{
                $("#"+block+"-"+index+"xx").next(".item-p").show("fast")
                $("#"+block+"-"+index+"xx").children(".icon_a").removeClass("icon_x")
            }
        };
        this.bgaddOReven = function (idx,gridData) {//奇偶背景颜色
            if(gridData.type == 'head'){
                even_bg_color = gridData.even_bg_color;
                odd_bg_color  = gridData.odd_bg_color
            }
            if((idx%2)==1) {
                return "background-color:"+even_bg_color;
            }
            else {
                return "background-color:"+odd_bg_color;
            }
        };
        this.NA = function (param) {
            if(param == "NA"){
                return ""
            }else{
                return param;
            }
        }
    }
    Ztfxreport.prototype = {
        constructor : Ztfxreport,
        name : "Ztfxreport",
        initDomEvent : function () {
            var self = this;
            $(document).on("click", ".teach-gotop", function () {
                $('html,body').animate({scrollTop: 0});
            });
            $(window).scroll(function () {
                var scrollTop = $(this).scrollTop(); //已经滚动到上面的页面高度
                var top = 200 - scrollTop < -0 ? -0 : 200 - scrollTop;
                $(".left").css("top", top);
                var maxHeight = $(window).height() - 100 - top;
                $(".left-fix").css("maxHeight", maxHeight + "px");

                var scrollHeight = $(document).height();//页面高度
                var windowHeight = $(this).height();//浏览器窗口高度
                if (scrollTop + windowHeight == scrollHeight) {//此处是滚动条到底部时候触发的事件，在这里写要加载的数据，或者是拉动滚动条的操作
                    if(!if_dot_bottom){
                        if_dot_bottom = true;
                        $17.voxLog({
                            module : "m_yJO2o3u3",
                            op     : "o_duZpjaJzRd",
                            s0     : $17.getQuery("newExamId")
                        });
                    }
                };
            });
        },
        mockClick:function () {
            
        }
        
    };
	$17.newexamv3 = $17.newexamv3 || {};
	$17.extend($17.newexamv3, {
		getZtfxreport  : function(options){
			return new Ztfxreport();
		}
	});
}());
