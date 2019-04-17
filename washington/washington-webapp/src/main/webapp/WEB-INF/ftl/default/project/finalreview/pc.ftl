<@app.css href="public/skin/project/finalreview/pc/css/finaldetails.css"/>
<@sugar.capsule js=['jquery', "core", "alert", "template"] css=["plugin.alert"] />
<div class="gradeSchool-box">
    <div class="g-banner">
        <div class="g-head">
            <a href="/" class="return">返回首页</a>
            <div class="logo"></div>
        </div>
    </div>
    <div class="g-main" style="position: relative; z-index: 2;">
        <div class="g-column">
            <div class="title"><p>期末复习作业时间</p></div>
            <div class="g-content">
                <p class="time">2016年5月30日-2016年6月30日</p>
                <p class="info">（视不同城市的考试时间而定）</p>
            </div>
        </div>
        <div class="g-column">
            <div class="title"><p>复习第一步   完成老师布置的期末复习作业</p></div>
            <div class="g-content">
                <p class="tag">1、完成老师布置的期末复习作业，可以获得额外的学豆奖励</p>
                <p class="tag">2、每完成一份作业，额外获得2学豆</p>
            </div>
        </div>
        <#if (currentUser.userType == 3)!false>
            <#if ( !currentStudentDetail.inPaymentBlackListRegion && ![1, 2]?seq_contains(currentStudentDetail.getClazzLevelAsInteger()) )!false>
                <div class="g-column">
                    <div class="title"><p>复习第二步 根据错题记录，自主复习</p></div>
                    <div class="g-content">
                        <a href="/afenti/api/index.vpage" class="tip js-clickVoxLog" data-op="activity_btn_pc">点击查看本学期错题记录，重做错题></a>
                    </div>
                </div>
            </#if>
        </#if>
    </div>
    <div class="g-footer"></div>
</div>
<script type="text/javascript">
    $(function(){
        $(".js-clickVoxLog").on("click",function(){
            var $thisOP = $(this).data("op");

            if( $17.isBlank($thisOP) ){
                return false;
            }

            $17.voxLog({
                module : "final_homework",
                op : $thisOP
            },"student");
        })
        $17.voxLog({
            module: "final_homework",
            op: "activity_load_pc"
        },"student")
    })
</script>