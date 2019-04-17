/**
 * Created by dell on 2017/10/26.
 * 试卷分析
 */
;(function(){
	var defaultOpts = {
		clazzId : null,
		examId : null
	};
	var STATUS_ENUM = {
		LOADING : "LOADING",
		SUCCESS : "SUCCESS",
		FAIL    : "FAIL"
	};
	function AnalyzePaper(opts){
		this.status = ko.observable(STATUS_ENUM.LOADING);  //参考statusEnum
		this.resText = ko.observable("");
		this.option = $.extend(true,{},defaultOpts,opts);
		this.achievementAnalysisPart = ko.observable({}); //总成绩分析
		this.papers = ko.observableArray([]); //题目模块分析
		this.focusPaperIndex = ko.observable(-1);
		this.focusPaper = ko.pureComputed(function(){
			var paper = this.focusPaperIndex() >= 0 ? this.papers()[this.focusPaperIndex()] : {};
			$17.log(paper);
			return paper;
		},this);
		this.scoreDistributionPart = ko.observable({});
		this.init();
	}
	AnalyzePaper.prototype = {
		constructor : AnalyzePaper,
		name : "AnalyzePaper",
		init : function(){
			var self = this;
			var paramObj = {
				clazzId : self.option.clazzId,
				examId  : self.option.examId
			};
			$.get("/teacher/newexam/report/newstatistics.vpage",paramObj).done(function(res){
				if(res.success){
					var result = res.newExamStatistics || {};
					if(result.issued){
						self.achievementAnalysisPart(result.achievementAnalysisPart || {});
						var papers = result.papers || [];
						if(papers.length > 0){
							self.papers(result.papers || []);
							self.focusPaperIndex(0);
							self.changePaper.call(result.papers[0],0,self);
						}
						self.scoreDistributionPart(result.scoreDistributionPart || {});
						self.status(STATUS_ENUM.SUCCESS);
						self.resText("");
						$17.voxLog({
							module : "m_yJO2o3u3",
							op     : "o_vNFnnHLx",
							s0     : self.option.examId
						});
					}else{
						self.resText(result.issueText);
						self.status(STATUS_ENUM.FAIL);
					}
				}else{
					self.resText(res.info || "查询失败");
					self.status(STATUS_ENUM.FAIL);

					res.errorCode !== "200" && $17.voxLog({
						module : "API_REQUEST_ERROR",
						op     : "API_STATE_ERROR",
						s0     : "/teacher/newexam/report/newstatistics.vpage",
						s1     : JSON.stringify(res),
						s2     : JSON.stringify(paramObj),
						s3     : $uper.env
					});
				}
			}).fail(function(jqXHR,textStatus, error){
				self.resText(error);
				self.status(STATUS_ENUM.FAIL);
			});
		},
		changePaper : function(index,self){
			self.focusPaperIndex(index);
		},
		initChartA : function(element,rate){
			//总成绩分析图
			var self = this;
			if(self.status && self.status() !== STATUS_ENUM.SUCCESS){
				return false;
			}
			$17.log("initChartA....");
			var myChart = echarts.init(element);
			rate = rate || 0;
			var option = {
				series: [
					{
						name:'得分率',
						type:'pie',
						radius: ['70%', '80%'],
						avoidLabelOverlap: false,
						hoverAnimation :false,
						legendHoverLink:false,
						label: {
							normal: {
								show: true,
								position: 'center',
								formatter:'得分率\n\n' + (rate || 0) + '%',
								textStyle:{
									color:'#4e5656',
									fontSize:18
								}
							}
						},
						labelLine: {
							normal: {
								show: false
							}
						},
						data:[
							{value:rate, name:"得分率",itemStyle:{
								normal:{
									color:'#fe5d12'
								}
							}},
							{value:100-rate, name:"失分率",itemStyle:{
								normal:{
									color:'#c5cdd1'
								}
							}}
						]
					}
				]
			};
			myChart.setOption(option);
			return "得分率概览";
		},
		initChartB : function(element){
			//成绩分布图
			var self = this;
			if(self.status && self.status() !== STATUS_ENUM.SUCCESS){
				$17.log("initChartB 拦截....");
				return "0px";
			}

			$17.log("initChartB....");
			var myChart = echarts.init(element);

			var legendData = [],seriesData = [];
			var butions = self.scoreDistributionPart().scoreDistributions || [];
			if(!$.isArray(butions) || butions.length == 0){
				$17.log("数据为空");
				return "0px";
			}
			for(var i = 0,iLen = butions.length; i < iLen; i++){
				legendData.push(butions[i].decs);
				seriesData.push({
					value : butions[i].rate,
					name  : butions[i].decs
				});
			}
			
			var option = {
				tooltip : {
					trigger: 'item',
					formatter: "{a} <br/>{b} : {d}%"
				},
				legend: {
					orient: 'horizontal',
					bottom: 'bottom',
					data: legendData
				},
				series : [
					{
						name: '成绩分布',
						type: 'pie',
						radius : '55%',
						center: ['50%', '60%'],
						data:seriesData,
						itemStyle: {
							emphasis: {
								shadowBlur: 10,
								shadowOffsetX: 0,
								shadowColor: 'rgba(0, 0, 0, 0.5)'
							}
						}
					}
				]
			};

			myChart.setOption(option);

			return "300px";
		}
	};

	$17.newexamv2 = $17.newexamv2 || {};
	$17.extend($17.newexamv2, {
		getAnalyzePaper  : function(options){
			return new AnalyzePaper(options);
		}
	});
}());