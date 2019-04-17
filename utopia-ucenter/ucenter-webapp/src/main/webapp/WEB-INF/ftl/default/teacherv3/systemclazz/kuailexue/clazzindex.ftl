<#import "../../../nuwa/kuailexue/teachershellv3.ftl" as shell />
<@shell.page show="main">

<@sugar.capsule js=["hashchange"]/>

<#--main module-->
<div id="clazzmanagement-main" class="class-content pd-15">

</div>

<#--<iframe id="clazzmanagement-main" width="100%" frameborder="no"></iframe>-->
<script type="text/javascript">
    // 判断第三方平台（希悦）
    var isThirdParty = "${(isSeiueSchool!false)?string}";
    function isThirdPartyTip () {
        $.prompt('已接入第三方平台，暂时无法进行调整，若有疑问请联系客服。', {
            title: "系统提示",
            focus: 1,
            buttons: {"知道了": false, "联系客服": true},
            submit: function (e, v){
                if (v) {
                    window.open("${(ProductConfig.getMainSiteBaseUrl())!''}/redirector/onlinecs_new.vpage?type=teacher&question_type=question_advice_mt&origin=o2o老师首页");
                }
            }
        });
    }

    function loadMainPage(url) {
        if($17.isBlank(url) || $17.isBlank(location.hash)){
            url = "/teacher/clazz/managedclazzlist.vpage";
        }

        $.ajax({
            type: "GET",
            url: url,
            async: true,
            beforeSend: function() {
                // TODO 这里可以加载loading等东东
                $("#clazzmanagement-main").html('<div class="w-ag-center" style="padding: 50px 0;"><img src="<@app.link href="public/skin/default/images/loadding.gif"/>" alt="加载中..." /> 加载中…</div>');
            },
            success: function(data) {
                $("#clazzmanagement-main").empty().html(data);

                if($17.getHashQuery("clazzId") != ""){
                    LeftMenu.focus($17.getHashQuery("clazzId"));
                }
            },
            error : function(){
                loadMainPage("/teacher/clazz/managedclazzlist.vpage");
            }
        });
    }

    $(function(){
        // 左侧菜单
        LeftMenu.focus("clazzmanager");

        $(window).hashchange(function() {
            loadMainPage(location.hash.substr(1));
        });

        // 这里这么处理有点trick
        // 通过前进、后退，地址上会已经带了#/teacher/clazz/managedclazzlist.vpage
        // 所以如果已经有的话，因为事件之前还未绑定，所以单独触发一下
        // 如果没有则做个静默跳转，跳班级管理页面

        loadMainPage(location.hash.substr(1));
    });
</script>

</@shell.page>