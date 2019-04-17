<#import "../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
<!--//module box 1 start-->
<div id="tipHaven" style="padding:60px 0; text-align:center; background-color: #fff; border: 1px solid #ddd; position: relative; z-index: 151;">
    <#switch type>
        <#case "hasApplication">
            <#--<p style="padding: 0 0 40px; font-size: 14px;">加入班级申请已发送，正在等待对方同意！您可以</p>-->
            <h5 data-showtip="false" class="text_gray_6">
                <a href="/teacher/clazz/createclazz.vpage" class="w-btn w-btn-green">继续添加班级</a> 或 <a href="/teacher/clazz/alteration/unprocessedapplication.vpage" class="w-btn w-btn-blue">查看申请记录</a>
            </h5>
            <#break />
        <#case "noApplication">
            <p style="padding: 0 0 40px; font-size: 14px;"> 您还没有添加自己任教的班级哦！</p>
            <h5 data-showtip="true" class="text_gray_6">
                <a id="addClassTongji" href="/teacher/clazz/createclazz.vpage?step=showtip" class="w-btn">添加班级</a>
            </h5>
            <#break />
    </#switch>
</div>
<#-- 如果老师名字为空，弹窗输入姓名。 -->
<#if (currentUser.profile.realname) == ''>
    <#include "name.ftl"/>
</#if>
<script type="text/javascript">
    <#--提示显示-->
    $(function(){
        //加入学校后-进入首页
        $17.voxLog({
            module: "reg",
            op : "showtip-load",
            step : 12
        });

        $("#addClassTongji").live("click", function(){
            //加入学校后-点击添加班级
            $17.voxLog({
                module: "reg",
                op : "showtip-click-addClass",
                step : 13
            });
        });

        LeftMenu.focus("main");

        $("#showTipOptBack").show();
    });

</script>
</@shell.page>