/* global Vue : true, reqwest:true, date_diff : true */

(function(){
	"use strict";

	var WIN = window,
		exam_id = WIN.exam.id,
		examination_vm;

	var extend = function(out) {
		out = out || {};

		for (var i = 1; i < arguments.length; i++) {
			if (!arguments[i]){
				continue;
			}

			for (var key in arguments[i]) {
				if (arguments[i].hasOwnProperty(key)){
					out[key] = arguments[i][key];
				}
			}
		}

		return out;
	};

	var send_ajax = function(url, method, data, callback){
		reqwest({
			url    : url,
			type   : "json",
			method : method,
			data : data,
			success : callback,
			fail : function(res){
				WIN.alert('报名失败' + res.info);
			}
		});
	};

	var interval_by_count_down = -1,
		count_down_by_vue = function(remaining_ms){

			WIN.clearInterval(interval_by_count_down);

			if(!remaining_ms){
				return ;
			}

			interval_by_count_down = WIN.setInterval(function () {

				if(remaining_ms < 0){
					return WIN.clearInterval(interval_by_count_down);
				}

				examination_vm.date_unit = date_diff(remaining_ms, 'd');

				remaining_ms -= 1000;

			}, 1000);

		};

	examination_vm = new Vue(
		{
			el : '#examination',
			data : {
				date_unit : {},
				name : "",
				status : "",
				REGISTRABLE : true
			},
			methods : {
				do_sign_up : function(){
					send_ajax(
						"/student/newexam/register.vpage",
						"get",
						{
							newExamId : exam_id
						},
						function(res){

							if(res.success){
								WIN.location.reload(true);
								return ;
							}

							WIN.alert('报名失败' + res.info);

						}
					);
				}
			},
			ready : function(){
				var vm = this;

				send_ajax(
					"/student/newexam/detail.vpage",
					"get",
					{
						id : WIN.exam.id
					},
					function(res){
						if(res.success){
							var status = res.newExamStudentStatus,
								REGISTRABLE = res.newExamStudentStatus === "REGISTRABLE";

							var _data = {
								name : res.name,
								status : status,
								REGISTRABLE : REGISTRABLE
							};

							count_down_by_vue(res.remainTime);

							extend(vm, _data);

							return ;
						}

						WIN.alert('获取页面信息失败');
					}
				);
			}
		}
	);


})();
