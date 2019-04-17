<#import "../../layout/webview.layout.ftl" as layout/>

<@layout.page
    title="一起-个人中心"
    pageJs=["personalcenter", "jquery"]
    pageJsFile={"personalcenter": "public/script/adminteacher/personalcenter"}
    pageCssFile={"index": ["/public/skin/adminteacher/css/common", "/public/skin/adminteacher/css/personalcenter"]}>
    <#include "../header.ftl">
    <div class="outercontainer" id="personalcenter">
        <div class="container" >
            <!-- 主体 -->
            <div class="mainBox">
                <div class="mainInner">
                    <div class="topTitle"><a href="">首页</a> > <a href="">上传试卷</a></div>
                    <div class="contentBox">

                        <#include "../nav.ftl">

                        <div class="contentMain">
                            <div data-bind="template: { name: 'centerIndexTemp' }, visible: isShowCenterIndexTemp"></div>

                            <div data-bind="template: { name: 'informationTemp' }, visible: isShowInformationTemp"></div>

                            <div data-bind="template: { name: 'accountSafeTemp' }, visible: isShowAccountSafeTemp"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <#include "../footer.ftl">
    <#include "./template.ftl">
    <script>
        var userName = "${(currentUser.profile.realname)!}";
        var idType = "${idType!'schoolmaster'}";
    </script>
</@layout.page>

