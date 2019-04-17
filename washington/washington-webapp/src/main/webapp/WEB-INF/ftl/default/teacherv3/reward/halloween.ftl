<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="halloween" showNav="hide">
<style>
    html,body{ background-color: #fafafa;}
    .hl-section { width: 960px; margin: 15px auto 50px;}
    .hl-section .u-1{ background: url(<@app.link href="public/skin/project/halloween/images/hl_1.jpg"/>) no-repeat; height: 399px; }
    .hl-section .u-2{ background: url(<@app.link href="public/skin/project/halloween/images/hl_2_v1.jpg"/>) no-repeat; height: 105px; overflow: hidden;}
    .hl-section .u-3{ background: url(<@app.link href="public/skin/project/halloween/images/hl_3.jpg"/>) no-repeat; height: 304px; position: relative; overflow: hidden;}
    .hl-section .u-3 .gold{ position: absolute; width: 230px; line-height: 50px; font-size: 18px; color: #663300; top: 180px; left: 75px; text-align: right;}
    .hl-section .u-3 .gold span{line-height: 50px; font-size: 48px; color: #f60; display: inline-block; vertical-align: text-bottom;}
    .hl-section .u-3 .gold h4{ font-size: 16px; clear: both; line-height: 150%; margin-bottom: 5px;}
    .hl-section .u-4-1{ background: url(<@app.link href="public/skin/project/halloween/images/hl_4_1.jpg"/>) no-repeat; height: 155px; overflow: hidden;}
    .hl-section .u-4-1 a{ display: block; width: 200px; height: 32px; margin: 86px 0 0 524px;}
    .hl-section .u-4-2{ background: url(<@app.link href="public/skin/project/halloween/images/hl_4_2.jpg"/>) no-repeat; height: 155px; overflow: hidden;}
    .hl-section .u-4-2 .inner{ background: url(<@app.link href="public/skin/project/halloween/images/studentlist.gif"/>) no-repeat; height: 27px; width: 338px; margin: 76px 0 0 270px;}
    .hl-section .u-5{ background: url(<@app.link href="public/skin/project/halloween/images/hl-bottom_1.jpg"/>) repeat-y 0 0;}
    .hl-section .u-5 .bottom{ background: url(<@app.link href="public/skin/project/halloween/images/hl-bottom_2.jpg"/>) no-repeat; height: 83px;}
    .studentContent{ border: 1px solid #d8b975; background-color: #e2e2e2; margin: 0 90px 20px 70px; position: relative; }
    .studentContent .ct-title{ background: url(<@app.link href="public/skin/project/halloween/images/title.png"/>) no-repeat; width: 416px; height: 62px; position: absolute; left: 50%; margin-left: -207px; top: -13px;}
    .studentContent .studentBox{ margin: 80px 50px 0; }
    .studentContent .studentList {position: relative; clear: both; margin-bottom: 50px; background-color: #f3f3f3; padding-bottom: 20px; }
    .studentContent .studentList h3{ background: url(<@app.link href="public/skin/project/halloween/images/clazz_title.png"/>) no-repeat; width: 221px; height: 60px; overflow: hidden; position: absolute; left: -12px; _left: -38px; top: -15px;}
    .studentContent .studentList h3 span{ color: #fff; width: 170px; white-space: nowrap; overflow: hidden; display: block; margin: 8px 0 0 25px; font-size: 18px; line-height: 30px; text-overflow: ellipsis;}
    .studentContent .studentList ul{ padding-top: 60px; margin-left: 25px; width: 700px;}
    .studentContent .studentList li{ float: left; width: 82px; margin-right: 30px; position: relative;}
    .studentContent .studentList li .avatar{ border: 1px solid #e3e3e3; padding: 3px; background-color: #fff; display: block; width: 72px; height: 72px;}
    .studentContent .studentList li .name{ display: block; font-size: 14px; text-align: center; width: 100%; white-space: nowrap; text-overflow: ellipsis; overflow: hidden; line-height: 24px;}
    .clearNull{ clear: both; width: 100%; height: 1px; overflow: hidden;}
</style>
<div class="hl-section">
    <div class="u-1"></div>
    <div class="u-2">
        <a href="#back" style="display: block; width: 270px; height: 35px; margin: 37px 0 0 198px;"></a>
    </div>
    <div class="u-3">
        <div class="gold" id="countGold">
            <h4>本次活动您共获得：</h4>
            <span>${total!'---'}</span>园丁豆
        </div>
        <a id="back"></a>
    </div>
    <div class="u-4-1">
        <a href="//cdn.17zuoye.com/static/project/StudentClassLevelIntroduction.pptx" target="_blank"></a>
    </div>
    <div class="u-4-2">
        <div class="inner"></div>
    </div>
    <div class="u-5">
        <div class="studentContent">
            <h2 class="ct-title"></h2>
            <div class="studentBox">
                <div style=" padding: 0 0 30px; color: #f00; font-size: 16px;">您只需要通知下列学生登录网站做一次作业（包含补做）即可，系统将自动为您计算。</div>
                <#if data?? && data?size gt 0>
                    <#list data?keys as dt>
                        <div class="studentList">
                            <h3><span>${dt?string}</span></h3>
                            <#if data[dt]?size gt 0>
                                <ul>
                                    <#list (data[dt]) as cl>
                                        <li data-student-id="${cl.studentId}" <#if cl.activeFlag!false>title="已唤醒"</#if>>
                                            <#if cl.activeFlag!false>
                                                <span class="w-checkbox w-checkbox-current" style="position: absolute; top: 5px; right: 5px;"></span>
                                            </#if>
                                            <span class="avatar"><img src="<@app.avatar href='${cl.imgUrl}'/>" width="72" height="72"/></span>
                                            <span class="name">${cl.studentName}</span>
                                            <span class="name">${cl.studentId}</span>
                                        </li>
                                    </#list>
                                </ul>
                                <div class="clearNull"></div>
                            <#else>
                                <div style="text-align: center; padding: 50px;">暂无数据</div>
                            </#if>
                        </div>
                    </#list>
                <#else>
                    <div class="studentList">
                        <div style="text-align: center; padding: 50px;">暂无数据</div>
                        <div class="clearNull"></div>
                    </div>
                </#if>
            </div>
        </div>
        <div class="bottom"></div>
    </div>
</div>
<#--<script type="text/javascript">
    $(function(){
        var studentBoxLength = $(".studentBox .studentList li[data-student-id]").length;
        if(studentBoxLength < 20){
            $("#countGold").hide();
        }else{
            $("#countGold span").text(studentBoxLength * 5);
        }
    });
</script>-->
</@shell.page>