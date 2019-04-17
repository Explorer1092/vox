<#import "module.ftl" as com>
<@com.page title="一起教育科技用户服务协议">
<style>
    .main{width:1000px!important;}
    .help-right{ float: right; width: 234px; border: 2px solid #d3d8df; border-radius: 6px; margin-bottom: 15px;}
    .help-right h2{ color: #4e5656; font-size: 18px; text-align: center; height: 36px; line-height: 36px; border-bottom: 1px solid #d3d8df;}
    .help-right li{ height: 28px; line-height: 28px; color: #838383;background: url(<@app.link href='public/skin/helpkf/images/circle.jpg'/>) 5px 7px no-repeat; padding-left: 25px;}
    .help-right li:hover{ color: #189cfb; cursor: pointer}
    .help-right li.active{color: #189cfb;}
    .shopagreement_text{display:none;}
</style>
    <#include "serviceagreement_text.ftl">
<div class="help-right">
    <h2>一起教育科技用户服务协议</h2>
    <ul>
        <li>一起小学学生端软件用户服务协议</li>
        <li>一起小学老师端软件用户服务协议</li>
        <li>一起学软件用户服务协议</li>
        <li>一起中学学生端软件用户服务协议</li>
        <li>一起中学老师端软件用户服务协议</li>
    </ul>
</div>
<!-- InstanceEndEditable -->
<!--end-->
<div class="clear"></div>
<script type="text/javascript">
    $(function(){
        function getQuery(item){
            var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
            return svalue ? decodeURIComponent(svalue[1]) : '';
        }
        var _index = getQuery('agreement');
        $(".help-right").find("li").eq(_index).addClass("active");
        $(".shopagreement_text").eq(_index).show();

        $(".help-right").on("click","li",function () {
            $(this).addClass("active").siblings().removeClass("active");
            $(".shopagreement_text").eq($(this).index()).show().siblings().hide();
        });
    });
</script>

</@com.page>