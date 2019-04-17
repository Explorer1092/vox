<div class="aDetail-container">
    <div class="aDet-banner"></div>
    <div class="aDet-main">
        <div class="aDet-list">
            <div class="title">期末复习作业时间</div>
            <div class="info time">
                <p class="textGreen">2016年5月30日-2016年6月30日</p>
                <p class="text">（视不同城市的考试时间而定）</p>
            </div>
        </div>
        <div class="aDet-list">
            <div class="title">复习第一步  完成老师布置的期末复习作业</div>
            <div class="info step01">
                <p class="text">1、为巩固学期知识掌握，我们通过过去您孩子的作业记录，精选了适量的期末复习内容；</p>
                <p class="text">2、老师会根据实际情况布置期末复习内容，请您配合老师协助孩子完成期末复习作业；</p>
                <p class="text">3、为鼓励学生完成期末复习作业获得更好的成绩，我们为学生准备了大量的学豆奖励。</p>
            </div>
        </div>
        <#if (afentiexam)!false>
        <div class="aDet-list">
            <div class="title">复习第二步  根据错题记录，自主复习</div>
            <div class="info step02" id="checkLink"></div>
        </div>
        </#if>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        var sid= $17.getQuery("sid");
        $("#checkLink").append('<a href="/parentMobile/ucenter/shoppinginfo.vpage?sid='+ sid +'&productType=AfentiExam" class="textGreen js-clickVoxLog" data-op="activity_btn_parentApp">点击查看本学期错题记录，重做错题></a>');
    })
</script>