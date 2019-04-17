<#if data.expandHomeworkCards?has_content>
    <li class="practice-block">
        <div class="practice-content">
            <h4><span class="w-discipline-tag w-discipline-tag-1">课外拓展</span></h4>
            <div class="no-content">
                <p class="n-1"><span class="w-icon w-icon-10"></span></p>
                <p class="n-2"><strong>有课外拓展任务待完成!</strong></p>
            </div>
            <div class="pc-btn">
                <a href="javascript:void (0);" class="w-btn w-btn-blue J_expandhomeworkbtn">开始作业</a>
            </div>
        </div>
    </li>
    <script type="text/javascript">
        $(function(){
            $17.voxLog({
                module : "PROGRAMMER_EXPAND_HOMEWORK",
                op     : "TASK_CARD_SHOW"
            });
            $("a.J_expandhomeworkbtn").on("click",function(){
                $17.voxLog({
                    module : "PROGRAMMER_EXPAND_HOMEWORK",
                    op     : "TASK_CARD_CLICK"
                });
                $17.alert("课外拓展任务，含有电脑端不支持的题型和视频，为了更好的答题和录音体验，请下载一起作业手机版完成！");
                return false;
            });
        });
    </script>
</#if>