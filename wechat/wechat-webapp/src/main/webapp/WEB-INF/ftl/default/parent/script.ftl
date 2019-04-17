<@sugar.capsule js=["fastClick","requirejs"] />

<#--根据需要加装对应js包-->
<#assign mainJs = ["jquery","$17", "vue","logger",'crossProjectShare',"${pageJs}"]>
<@sugar.requirejs names = mainJs/>
<#--todo load js -->
<#switch pageJs>
    <#case "bindMobile">
        <@sugar.requirejs names = ['smsBtn'] />
        <#break>
    <#case "mobileLogin">
        <@sugar.requirejs names = ['getVerifyCodeModal','jbox'] />
        <#break>
    <#case "loginVerify">
        <@sugar.requirejs names = ['getVerifyCodeModal','jbox'] />
        <#break>
    <#case "homework">
        <@sugar.requirejs names = ['knockout','userpopup','menu','jbox'] />
        <#break>
    <#case "homeworkDetail">
        <@sugar.requirejs names = ['audio','jbox'] />
        <#break>
    <#case "weeklyReport">
        <@sugar.requirejs names = ['knockout','userpopup','menu','jbox'] />
        <#break>
    <#case "ucenter">
        <@sugar.requirejs names = ['menu'] />
        <#break>
    <#case "ucenterOrderList">
        <@sugar.requirejs names = ['knockout'] />
        <#break>
    <#case "errordetail">
        <@sugar.requirejs names = ['knockout','examCore_new'] />
        <#break>
    <#case "wrongQuestionList">
        <@sugar.requirejs names = ['knockout','userpopup','examCore','menu','jbox'] />
        <#break>
    <#case "resetstudentpwd">
        <@sugar.requirejs names = ['knockout','userpopup','jbox'] />
        <#break>
    <#case "starreward">
        <@sugar.requirejs names = ['knockout','userpopup','jbox'] />
        <#break>
    <#case "parentWard">
        <@sugar.requirejs names = ['knockout','userpopup','menu','wx','jbox'] />
        <#break>
	<#case "setmission">
        <@sugar.requirejs names = ['knockout','jbox'] />
        <#break>
    <#case "reportIndex">
        <@sugar.requirejs names = ['userpopup','menu','jbox'] />
        <#break>
    <#case "pay">
        <@sugar.requirejs names = ['wx'] />
        <#break>
    <#case "smart">
        <@sugar.requirejs names = ['knockout','userpopup','menu','jbox'] />
        <#break>
    <#case "integralOrder">
        <@sugar.requirejs names = ['knockout'] />
        <#break>
	<#case "reserve">
        <@sugar.requirejs names = ['knockout','getVerifyCodeModal','jbox','flexslider'] />
        <#break>
    <#case "signpic">
        <@sugar.requirejs names = ['knockout','wx','jbox'] />
        <#break>
    <#case "trusteePay">
        <@sugar.requirejs names = ['jbox'] />
        <#break>
	<#case "unitReport">
        <@sugar.requirejs names = ['knockout','userpopup','menu','jbox'] />
        <#break>
    <#case "uDetail">
        <@sugar.requirejs names = ['knockout','share','wx','examCore_new','menu'] />
        <#break>
    <#case "unitExample">
        <@sugar.requirejs names = ['knockout','examCore_new','menu'] />
        <#break>
    <#case "wrongExercise">
        <@sugar.requirejs names = ['knockout','examCore_new','menu'] />
        <#break>
    <#case "confirmProduct">
        <@sugar.requirejs names = [''] />
        <#break>
    <#case "thanksgiving">
        <@sugar.requirejs names = ['knockout','komapping','jbox'] />
        <#break>
    <#case "christmas">
        <@sugar.requirejs names = ['knockout','komapping','jbox'] />
        <#break>
    <#case "onLineqaHistory">
        <@sugar.requirejs names = ['knockout','jbox'] />
        <#break>
    <#case "bookregist">
    <#case "ocreserve">
        <@sugar.requirejs names = ['knockout','getVerifyCodeModal','jbox'] />
        <#break>
    <#case "onlineqaComment">
    <#case "ocpresent">
    <#case "registtrustee">
        <@sugar.requirejs names = ['jbox'] />
        <#break>

    <#--专题区-->
    <#case "reservation">
        <@sugar.requirejs names = ['getVerifyCodeModal','jbox'] />
        <#break>
    <#case "wintercampskupay">
        <@sugar.requirejs names = ['jbox'] />
        <#break>
    <#case "mytrusteeindex">
        <@sugar.requirejs names = ['knockout','jbox','flexslider'] />
        <#break>
    <#case "mytrusteecreateorder">
        <@sugar.requirejs names = ['jbox'] />
        <#break>
    <#case "branchalbum">
        <@sugar.requirejs names = ['wx','jbox'] />
        <#break>
    <#case "branchdetail">
        <@sugar.requirejs names = ['wx'] />
        <#break>
    <#case "mytrusteeorderdetail">
        <@sugar.requirejs names = ['jbox'] />
        <#break>
    <#case "refundExplain">
        <@sugar.requirejs names = ['jbox'] />
        <#break>
    <#case "refundDetail">
        <@sugar.requirejs names = ['knockout'] />
        <#break>
    <#case "stem">
        <@sugar.requirejs names = ['jbox'] />
        <#break>
    <#case "globalmath">
        <@sugar.requirejs names = ['jbox'] />
        <#break>
    <#case "summercampskupay">
        <@sugar.requirejs names = ['jbox'] />
        <#break>
    <#case "hot">
        <@sugar.requirejs names = ['hotcss','swiper','getVerifyCodeModal','jbox'] />
        <#break>
    <#case "parentReceiveLoginReward">
        <#break>
</#switch>
<script type="text/javascript">

	(function(){
		if(window.isFromParent){
            <#-- add targetDensitydpi support -->
			var script = document.createElement('script'),
                 prior = document.getElementsByTagName('script')[0];

			script.async = 1;
			prior.parentNode.insertBefore(script, prior);

			script.onload = script.onreadystatechange = function( _, isAbort ) {
				if(isAbort || !script.readyState || /loaded|complete/.test(script.readyState) ) {
					script.onload = script.onreadystatechange = null;
					script = undefined;

					if(!isAbort) {
						window.adaptUILayout(640);
					}
				}
			};

			script.src = "http://cdn-cnc.17zuoye.cn/public/script/parentMobile/targetDensitydpi.min-V20151214100714.js";

            <#-- TODO 因为家长通App客户端的bug 导致无法正确处理 tel: 链接的请求 因此暂且将 tel: 链接给拦截住 -->
            document.body.addEventListener('click',function(e){
                var targetDom = e.target;

                if(targetDom && targetDom.nodeName.toUpperCase()=="A" && targetDom.href.trim().search("tel:") === 0){
					targetDom.href = "javascript:;";
                }

            },false);
		}
	})();

    (function () {
        //组件自动加载
        require(['${pageJs}','logger','crossProjectShare'], function () {});

        if ('addEventListener' in document) {
            document.addEventListener('DOMContentLoaded', function() {
                FastClick.attach(document.body);
            }, false);
        }
        //pageLoadFinish
        window.onload = function(){
            var pf_page_load_time_end = +new Date(); //页面加载完成时间
            if(typeof pageLog =='function'){
                pageLog()
            }
            //ga('send','event','pageLoadFinishTime',location.href, (pf_page_load_time_end - pf_time_start)+'ms');
        };
    })();
</script>
