<#import "../../layout/project.module.ftl" as temp />
<@temp.page header="true" title="一起教育科技">
<@app.css href="public/skin/project/common/emptytip/skin.css"/>
<@sugar.capsule js=[ "alert"]/>
<#include "../../layout/project.header.ftl"/>
<div class="wrapper"></div>
<script>
    // 注：该页面为通用页面，路由为:/project/common/emptytip.vpage?type=1&returnUrl=***&auto=false
    // 用途：页面下线后的跳转提示，跳转到此空页面，弹窗提示，3s返回到某一页
    // @params: returnUrl，非必传，返回的页面地址(encode之后带过来)，不传默认回首页
    // @params: type，非必传，弹窗文案由于太长，就不通过参数传递，通过type来觉得显示对应的文案，默认文案为：你访问的页面已下线哦！
    // @params: autoJump，非必传，3s后自动跳转returnUrl，默认为跳转，不需要跳转传false

    var returnUrl = window.decodeURIComponent($17.getQuery('returnUrl'));
    var type = $17.getQuery('type');
    var auto = $17.getQuery('auto');
    var tipText = '你访问的页面已下线哦！';

    if (+type === 1) { // 学生用品中心下线抽奖页面提示
        tipText = '啊哦～这个功能已经下线了，去首页看看还有什么其他学习功能吧';
    }

    $17.alert(tipText, function(){
        jumpPage();
    });

    // 3s自动跳转
    if (auto !== 'false') {
        setTimeout(function () {
            jumpPage();
        }, 3000);
    }

    // 跳转页面
    function jumpPage() {
        if (returnUrl) {
            window.location.href = returnUrl;
        } else {
            window.location.href = '/';
        }
    }
</script>
</@temp.page>
<#include "../../layout/project.footer.ftl"/>