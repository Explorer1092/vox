<#macro page title="一起教育科技" type="index">
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=Edge, chrome=1"/>
    <meta name="keywords" content="17教育科技，一起小学下载，一起小学学生，学生APP，学生端下载，在线教育平台">
    <meta name="description" content="一起小学是一个学生、老师和家长三方互动的作业平台，老师轻松布置作业，学生快乐做作业，家长可以定期查看孩子的学习进度及报告，情景交融的学习模式，让孩子轻松搞定各科学习！一起小学，让学习成为美好体验。">
    <meta property="qc:admins" content="2501263117617257156375" />
    <title>一起教育科技-让学习成为美好体验</title>
    <@sugar.capsule js=["jquery", "core", "template", "DD_belatedPNG_class"] />
    <@sugar.site_traffic_analyzer_begin />
    <@app.css href="public/skin/helpkf/css/skin.css" />
    <script src="${scriptPath}" defer></script>
</head>
<body>
    <div class="header">
        <div class="navigation">
            <#if (type != "index")!false>
                <#if ((currentUser.userType == 1)!false) && ((currentTeacherDetail.isJuniorTeacher())!false)>
                    <a href="/help/kf/junior.vpage" style="float:right; margin:30px 0 0;">&lt;返回帮助首页</a>
                <#else>
                    <a href="/help/kf/index.vpage" style="float:right; margin:30px 0 0;">&lt;返回帮助首页</a>
                </#if>
            </#if>
            <span class="logo"><a href="/" style="text-indent: -9999px; display: block;width:117px;height:45px;border:none;" title="返回一起作业首页">一起作业</a></span><#--<b class="title">帮助与支持</b>-->
        </div>
    </div>
    <div class="main">
        <#nested>
    </div>
    <div class="footer">
        <p class="navs"><a href="/help/aboutus.vpage">关于我们</a><i>•</i> <a href="/help/jobs.vpage">诚聘英才</a><i>•</i> <a href="/help/parentsguidelines.vpage">家长须知</a><i>•</i> <a href="/help/childrenhealthonline.vpage">儿童健康上网</a></p>
        ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
    </div>
<#if (type != "index")!false>
    <script type="text/javascript">
        var detailItem;
        var publicTel;
        $(function(){
            $(document).on("click", ".js-clickLeftTypeBtn", function(){
                var $this = $(this);
                var $type = $this.attr("data-type");
                var items = detailItem;
                var getQueryCount = $17.getQuery("count");

                $("html, body").animate({ scrollTop : 0}, 100);

                $this.addClass("active").siblings().removeClass("active");
                $("#detailsItems").html( template("T:detailsItems", { items: items[$type], type : $type, count : getQueryCount, tel : publicTel}) );
            });

            $(document).on("click", ".js-useDesc", function(){
                var $this = $(this);
                $this.addClass("active").find("div.answer").slideDown(100);
                $this.siblings("li").removeClass("active").find("div.answer").slideUp("slow");
            });

            var getType = $17.getQuery("type");
            if(!$17.isBlank(getType)){
                $(".js-clickLeftTypeBtn[data-type='"+ getType +"']").click();
            }else{
                $(".js-clickLeftTypeBtn:first").click();
            }
        });
    </script>
    <script type="text/html" id="T:detailsItems">
        <div class="container">
            <div class="info">使用说明><%=type%></div>
            <div class="list">
                <ul>
                    <%for(var i = 0; i < items.length; i++){%>
                        <li class="js-useDesc <%=(count == i ? 'active' : '')%>">
                            <p><i class="arrow"></i><%=(i + 1)%>、<%==(items[i].name)%></p>
                            <div class="answer" style="display: <%=(count == i ? 'block' : 'none')%>;">
                                <%==(items[i].content)%>
                                <%if(items[i].tel){%>
                                    <div class="phone">联系我们：<%=tel%></div>
                                <%}%>
                                <%if(items[i].specialType){%>
                                    <div class="btn"><a href="javascript:void(0);" class="btn-online js-clickServerPopup" data-questiontype="<%=items[i].problem%>" data-usertype="${type!'teacher'}" data-origin="PC帮助中心-<%=items[i].name%>">在线交谈</a></div>
                                <%}else{%>
                                    <%if(items[i].server){%>
                                        <div class="btn"><a href="javascript:void(0);" class="btn-online js-clickServerPopupOne" data-questiontype="<%=items[i].problem%>" data-usertype="${type!'teacher'}" data-origin="PC帮助中心-<%=items[i].name%>">在线交谈</a></div>
                                    <%}%>
                                <%}%>
                            </div>
                        </li>
                    <%}%>
                </ul>
            </div>
        </div>
    </script>
    <#include "serverinfo.ftl"/>
</#if>
    <@sugar.site_traffic_analyzer_end />
</body>
</html>
</#macro>