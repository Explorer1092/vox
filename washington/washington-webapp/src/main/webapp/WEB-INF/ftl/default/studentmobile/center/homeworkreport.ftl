<#import "../layout.ftl" as temp >
<@temp.page dpi="">
    <@app.css href="public/skin/mobile/pc/css/homeworkreport.css" />
<style>
    html, body{ background-color: #92d145;}
</style>

    <div class="student-project-homeworkPresent-module1">
        <div class="mod-1">
            <div class="text">各科能力等级</div>
        </div>
        <div class="mod-2">
            <div class="text">作业成绩总结</div>
        </div>
        <div class="mod-3">
            <div class="text">每周词汇统计</div>
        </div>
    </div>
    <div class="student-project-homeworkPresent-module2">
        <div class="mod">
            <div class="text">
                <p>1.与老师互动，看看谁给老师送花了!</p>
                <p>2.查收单元报告，了解班级高频错题!</p>
            </div>
        </div>
    </div>
    <div class="student-project-homeworkPresent-module3">
        <div class="box">
            <div class="prom">安装一起作业官方“家长通”</div>
            <a href="javascript:void(0);" class="btn js-clickOpenParent">立即查看</a>
        </div>
    </div>

<script type="text/javascript">
    $(function(){
        $(document).on("click", ".js-clickOpenParent",function(){
            if(window.external && ('openparent' in window.external)){
                window.external.openparent("");
                $M.appLog('reward',{
                    app : "openParent",
                    module : "beanReward",
                    op : "open_success"
                });
            }else{
                $M.appLog('reward',{
                    app : "openParent",
                    module : "beanReward",
                    op : "open_error"
                });
            }
        })
    });
</script>
</@temp.page>
