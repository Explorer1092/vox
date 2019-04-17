define(['jquery', 'knockout', '$17', 'unitImportant', 'unitPrepare', 'unitWrong', 'menu'], function($, ko, $17, important, prepare, wrong) {
  $('#loading').hide();
  $('#reportDetail').show();
  $("#reportTitle").html(result.unitName);
  prepare.setPrepares(result.nextPoints || []);
  prepare.title = result.unitName;
  wrong.setWrongs(result.wql || []);
  wrong.title = result.unitName;
  if (wrong.showWrongs.length === 0) {
    $('#noWrong').show();
  }
  important.setImportants(result.points || []);
  important.title = result.unitName;
  ko.applyBindings(important, document.getElementById('important'));
  ko.applyBindings(prepare, document.getElementById('prepare'));

  /*测试 */

  /*testConfig={
  		config:{
  			debug: true
  			appId: 'appId'
  			timestamp: 'timestamp'
  			nonceStr: 'nonceStr'
  			signature: 'signature'
  		}
  		title: info.title
  		desc: info.title
  		link: location.href
  		 * imgUrl:'imgUrl'
  		success:()->
  			$17.tongji('微信','家长-单元报告','分享电子报告')
  		cancel:()->
  	}
  	share.init testConfig
   */
});
