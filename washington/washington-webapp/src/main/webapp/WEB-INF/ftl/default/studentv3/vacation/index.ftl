<#import '../layout/layout.ftl' as temp>
<@temp.page>
    <@sugar.capsule js=["ko","jplayer", "studentv3.vacation"]/>

<div id="app-root">
    <vh-2016-primary-pc-app></vh-2016-primary-pc-app>
</div>
<script type="text/javascript">
    $(function(){

        try{
            var appPkg = window["VacationHomework2016PrimaryPC"],
            app = appPkg["startup"],
            bootstrap = app["bootstrap"];
            $.isFunction(bootstrap) && bootstrap({
                domain     : '${requestContext.webAppBaseUrl}/',
                img_domain : '<@app.link_shared href='' />',
                env        : <@ftlmacro.getCurrentProductDevelopment />,
                user_id    : "${(currentUser.id)!}",
                package_id : $17.getQuery("packageId") || "",
                fowardCallCb  : function(homeworkId){
                    if($17.isBlank(homeworkId)){
                        $17.alert("未获取到作业ID");
                    }else{
                        window.location.href = "/student/vacation/homework/index.vpage?from=vacationIndex&homeworkId=" + homeworkId;
                    }
                }
            });
        }catch(exception){
            $17.voxLog({
                module: 'vacation_homework_package_list',
                op: '2016PrimaryPC_Js_error',
                errMsg: exception.message,
                userAgent: (navigator && navigator.userAgent) ? navigator.userAgent : "No browser information"
            }, "student");
        }
    });
</script>
</@temp.page>

