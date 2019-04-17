/* global Vue : true, reqwest:true */

(function(){
	"use strict";

	var activeClass = "active",
		prizes = new Vue(
			{
				el : '#prizes',
				data : {
					prizes : []
				}
			}
		),
		getRewardList = function(categoryId){
			reqwest({
				url : "/studentMobile/center/rewardList.vpage",
				type : "json",
				data : {
					categoryId : categoryId
				},
				method : "post",
				success : function(res){
					if(res.success){
						prizes.prizes = res.rows;
					}
				}
			});
		};

	var categoriesVM = new Vue(
		{
			el : '#categories',
			data : {
				canShowList : false,
				categroyName : "全部"
			},
			methods : {
				toggleShowList : function(){
					this.canShowList = !this.canShowList;
				},
				changeList : function(event){
					var vm = this,
						self = event.target;

					getRewardList(self.dataset.category_id);

					var activeDom = document.querySelector("#categories .active");

					activeDom && activeDom.classList.remove(activeClass);
					self.classList.add(activeClass);

					vm.canShowList = false;
					vm.categroyName = self.textContent.trim();

				}
			}
			// https://github.com/vuejs/vue/issues/688
			//,ready : function(){
			//	this.$el.querySelector(".studentJuniorSchool-mallTop-list>a").click();
			//}
		}
	);

	var triggerClick = function(dom){
		if(!dom){
			return ;
		}

		try{
			dom.click();
		}catch(error){
			var e = document.createEvent('MouseEvent'); // https://developer.mozilla.org/en-US/docs/Web/API/Document/createEvent  http://stackoverflow.com/questions/596481/simulate-javascript-key-events

			e.initEvent('click', false, false);
			dom.dispatchEvent(e);
		}
	};


	document.addEventListener("DOMContentLoaded", function(event) {
		categoriesVM.$el && triggerClick(categoriesVM.$el.querySelector(".studentJuniorSchool-mallTop-list>a"));
	});

	var integralListVm = new Vue({
		el : "#integralList",
		data : {
			integralList : []
		}
	});

	var getIntegralList = function(){
		reqwest({
			url : "/studentMobile/center/integralchip.vpage ",
			type : "json",
			method : "post",
			success : function(res){
				if(res.success){
					integralListVm.integralList = res.pagination.content;
					return ;
				}

				window.alert(res.info);
			}
		});
	};

	getIntegralList();

})();
