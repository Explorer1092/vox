<#import "../layout.ftl" as temp >
<@temp.page>
<div class="wr">
    <div class="class-rules">
        <div class="box">
            <div class="hd">1、学豆榜</div>
            <p class="title">
                按时完成老师布置的作业，平均分越高，获得学豆越多，学豆榜名次越高。
                <#if (currentStudentWebGrayFunction.isAvailable("IntegralRank", "week"))!false>
                    付费产品获得的学豆不计入排行榜。
                </#if>
            </p>
            <p>满分10学豆时，按时完成作业，且：</p>
            <ul>
                <li>作业平均分=100，奖励10学豆</li>
                <li>80≤作业平均分＜100，奖励8学豆</li>
                <li>60≤作业平均分＜80，奖励5学豆</li>
                <li>40≤作业平均分＜60，奖励3学豆</li>
                <li>20≤作业平均分＜40，奖励1学豆</li>
                <li>作业平均分＜20，无学豆奖励</li>
            </ul>
            <p>满分5学豆时，按时完成作业，且：</p>
            <ul>
                <li>作业平均分=100，奖励5学豆</li>
                <li>80≤作业平均分＜100，奖励4学豆</li>
                <li>60≤作业平均分＜80，奖励3学豆</li>
                <li>40≤作业平均分＜60，奖励2学豆</li>
                <li>20≤作业平均分＜40，奖励1学豆</li>
                <li>作业平均分＜20，无学豆奖励</li>
            </ul>
        </div>
        <div class="box">
            <div class="hd">2、奖励榜</div>
            <p class="title">
                表现越好，老师奖励的学豆越多哦~<br>
                获取途径：
            </p>
            <ul>
                <li>老师检查作业时，可奖励学生学豆</li>
                <li>老师使用智慧课堂，可奖励学生学豆</li>
                <li>家长查看学业报告，可为学生领取学豆奖励</li>
            </ul>
        </div>
        <div class="box">
            <div class="hd">3、学霸榜</div>
            <p class="title">
                每次作业平均分是班级第1名，即可获得1次学霸。获得的学霸次数越多，学霸榜名次越高。
            </p>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $M.appLog('homework',{
            app: "17homework_my",
            type: "log_normal",
            module: "class",
            operation: "page_class_rules"
        });
    });
</script>
</@temp.page>