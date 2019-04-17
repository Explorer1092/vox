/* global $ : true */

(function(){
	"use strict";

	var ioPromise = window.io || $.noop;

	$.iosOnClick(".doTestPay", function(){
		var $self = $(this),
			selfData = $self.data(),
			oid = "" + selfData.order_id,
			fee = selfData.order_price * 100,
			orderProductType = selfData.order_type;
		//pay action from wechat
		if(window.isFromWeChat()){
			var url = window.location.protocol + '//' + wechatUrlHeader + '/parent/wxpay/pay-'+orderProductType+'.vpage?oid='+oid;
			location.href = url;
		}else{

			var buildXmlChild = function(key, value, noAddCDATA){
				return [
					"<", key , ">",
					noAddCDATA ? value : "<![CDATA["+ value +"]]>",
					"</", key , ">"
				].join("");
			};

			var payUrl = "/payment/notify/trustee/" + orderProductType  + "/wechatpay_parent-notify.vpage";

			var xmlStr =
				"<xml>" +
				[
					["return_code", "SUCCESS"],
					["return_msg", "OK"],
					["appid", "wx050577ff56b1c4a3"],
					["mch_id", "1274342001"],
					["nonce_str", "PeBTS6uAb5ZIuXyT"],
					["sign", "test"],
					["result_code", "SUCCESS"],
					["openid", "o4oIEj8WUwd6h68U1lq9mvI3O_u4"],
					["is_subscribe", "N"],
					["trade_type", "APP"],
					["bank_type", "CFT"],
					["total_fee", fee, true],
					["fee_type", "CNY"],
					["transaction_id", "1002060336201512011852000125"],
					["out_trade_no", oid],
					["attach", ""],
					["time_end", new Date().getTime()],
					["trade_state", "SUCCESS"],
					["cash_fee", fee, true]
				].map(function(value){
					return buildXmlChild.apply(null, value);
				}).join("")
				+ "</xml>";

			ioPromise(
				payUrl,
				xmlStr,
				"POST",
				{
					processData: false,
					contentType : 'application/json',
					dataType: "text"  // TODO Andorid do not support xml
				}
			)
			.done(function(res){
				var $res = $($.parseXML(res)),
					isSuccess = $res.find("return_code").text().toUpperCase() === "SUCCESS";

				if(!isSuccess){
					return $.alert($res.find("return_msg").text());
				}

				var payResultData = {
					oid : oid,
					from : "web",
					orderProductType : orderProductType
				};

				ioPromise("/v1/parent/order/orderTail.vpage", payResultData, "POST")
				.done(function(orderTailRes){
					if(orderTailRes.result !== "success"){
						return $.alert(orderTailRes.message);
					}

					orderTailRes.isPay && $.alert("支付成功");

					setTimeout(function(){
						window.location.reload(true);
					}, 800);
				});

			})
			.fail(function(res, textStatus, errorThrown){
				if(textStatus !== "success"){
					$.alert('支付错误 : ' + textStatus + errorThrown.toString());
					return ;
				}
			});
		}
	});
})();

