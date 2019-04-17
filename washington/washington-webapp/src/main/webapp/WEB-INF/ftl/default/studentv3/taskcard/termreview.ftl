<#if data.basicReviewHomeworkCards?has_content>
<li class="practice-block">
    <div class="practice-content">
        <h4><span class="w-discipline-tag w-discipline-tag-1">期末基础</span></h4>
        <div class="no-content">
            <p class="n-1"><span class="w-icon w-icon-10"></span></p>
            <p class="n-2"><strong>期末基础复习待完成</strong></p>
        </div>
        <div class="pc-btn">
            <a href="javascript:void (0);" class="w-btn w-btn-blue J_termreviewbtn">开始复习</a>
        </div>
    </div>
</li>
<script type="text/javascript">
    $(function(){
        $("a.J_termreviewbtn").on("click",function(){
            $17.alert("老师布置的期末基础复习作业，含有电脑端不支持的内容设计。为了更好的复习体验，请下载一起小学学生APP完成！");
            return false;
        });
    });
</script>
</#if>