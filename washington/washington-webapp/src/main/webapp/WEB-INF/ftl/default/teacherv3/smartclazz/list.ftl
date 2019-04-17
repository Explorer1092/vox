<#import "../../nuwa/teachershellv3.ftl" as temp />
<@temp.page>
<@sugar.capsule js=["DD_belatedPNG"] css=["new_teacher.smartclazz"] />
<style>
    html, body{ background-color: #ebe9e4;}
    .m-main{ width: 1000px;}
</style>
<div class="smart_clazz_list">
    <ul>
        <#if clazzList?has_content>
            <#list clazzList as cl>
                <li>
                    <div class="subject">${cl.subjectText!}</div>
                    <#if currentUser.fetchCertificationState() == "SUCCESS">
                        <a target="_blank" href="/teacher/smartclazz/clazzdetail.vpage?clazzId=${cl.clazzId!''}&subject=${cl.firstSubject!}">
                    <#else>
                        <a href="javascript:void(0);">
                    </#if>
                        <div class="ground_color ground_color_${cl_index%10}">
                            <div class="ground_tran"></div>
                            <div class="ring PNG_24"><span class="ringIcon PNG_24"></span></div>
                        </div>
                        <div class="title">
                            <h3>${cl.clazzName!''}</h3>
                            <span class="count">${cl.studentCount!0}个同学</span>
                        </div>
                    </a>
                </li>
            </#list>

            <#if clazzList?size lte 8 && (currentTeacherDetail.subjects?size lte 1)!false >
                <li>
                    <#if currentUser.fetchCertificationState() == "SUCCESS">
                        <a target="_blank" href="/teacher/clazz/createclazz.vpage">
                    <#else>
                        <a href="javascript:void(0);">
                    </#if>
                        <div class="ground_color ground_color_add">
                            <div class="ground_tran"></div>
                            <div class="ring PNG_24"><span class="ringIcon PNG_24"></span></div>
                            <div class="text">创建新班级</div>
                        </div>
                    </a>
                </li>
            </#if>
        </#if>
    </ul>
</div>
<div style="clear: both"></div>

<script type="text/javascript">
    $(function(){
        $17.tongji("课堂-首页");

        LeftMenu.focus("smartclazz");

        var smartLi = $(".smart_clazz_list li");

        smartLi.hover(function(){
            $(this).addClass("active").find(".ground_tran").show();
        }, function(){
            $(this).removeClass("active").find(".ground_tran").hide();
        });

        <#if currentUser.fetchCertificationState() != "SUCCESS">
            smartLi.on("click", function(){
                $.prompt("<div class='jqicontent'>只有认证老师才可以使用智慧课堂哦，快去认证吧！</div>",{
                    title   : '消息提醒',
                    buttons : {"暂不认证" : false , "马上认证" : true },
                    focus   : 1,
                    submit  : function(e,v,m,f){
                        if(v){
                            e.preventDefault();
                            location.href = "${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage";
                            return false;
                        }else{
                            $.prompt.close();
                        }
                    }
                });
            });
        </#if>
    });
</script>
</@temp.page>