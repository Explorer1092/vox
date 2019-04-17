<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="else" showNav="show">

<@sugar.capsule js=["hashchange"]/>

<#--main module-->
<div id="teachercenter-main"></div>
<@sugar.capsule js=["17module"]/>
<#--<iframe id="clazzmanagement-main" width="100%" frameborder="no"></iframe>-->
<script type="text/javascript">
    function loadMainPage(url) {
        if($17.isBlank(url) || $17.isBlank(location.hash)){
            url = "/teacher/center/basicinfo.vpage";
        }

        $.ajax({
            type: "GET",
            url: url,
            async: true,
            beforeSend: function() {
                // TODO 这里可以加载loading等东东
                $("#teachercenter-main").html('<div class="w-ag-center" style="padding: 50px 0;"><img src="<@app.link href="public/skin/default/images/loadding.gif"/>" alt="加载中..." /> 加载中…</div>');
            },
            success: function(data) {
                $("#teachercenter-main").empty().html(data);
            },
            error : function(){
                loadMainPage("/teacher/center/basicinfo.vpage");
            }
        });
    }

    $(function(){
        // 左侧菜单
        LeftMenu.changeMenu();
        LeftMenu.focus("basicInfo");

        $(window).hashchange(function() {
            loadMainPage(location.hash.substr(1));
        });

        loadMainPage(location.hash.substr(1));
    });
</script>

</@shell.page>