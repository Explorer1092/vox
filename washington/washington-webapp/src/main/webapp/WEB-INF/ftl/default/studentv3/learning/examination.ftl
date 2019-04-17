<#import "module.ftl" as module>
<@module.learningCenter menuName="examination">
<div class="t-his-container w-fl-right">
    <h2 class="leaf"></h2>
    <div class="t-his-inner">
        <div class="t-his-inner bot-leaf"></div>
        <div id="J_examList"></div>
    </div>
</div>
<script type="text/javascript">
    var constantObj = {
        userId    : ${(currentUser.id)!0}
    };
</script>
    <@sugar.capsule js=["homeworkv3.studentexam"] />
</@module.learningCenter>
