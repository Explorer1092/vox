var App = {
	/**
	 * POST提交数据（Ajax）
	 * 
	 * @param url
	 * @param data
	 * @param success
	 * @param error
	 * @return
	 */
	postJSON : function(url, data, callback,errorfunction) {
		if ($.isFunction(data)) {
			callback = data;
			data = undefined;
		}
		if(errorfunction == null || !$.isFunction(errorfunction)){
			errorfunction = function(){
				alert("网络请求失败，请稍等重试或者联系客服人员");
			};
		}
		
		return $.ajax({
			type : 'post',
			url : url,
			data : $.toJSON(data),
			success : callback,
			error:errorfunction,
			dataType : 'json',
			contentType : 'application/json;charset=UTF-8'
		});
	},
	/**
	 * 格式化显示状态
	 * 
	 * @param value
	 * @param rec
	 * @return
	 */
	disableFormatter : function(value, rec) {
		return (value == true) ? '<span style="color:red">✕</span>' : '<span style="color:green">✔</span>';
	},
	/**
	 * 格式化显示选中
	 * 
	 * @param value
	 * @param rec
	 * @return
	 */
	checkboxFormatter : function(value, rec) {
		return (value == true) ? '<span style="color:green">✔</span>' : '<span style="color:red">✕</span>';
	}
}