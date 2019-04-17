<#import "module.ftl" as temp>
<@temp.page title="资源" level="上传资源">
	<div id="first_page_box" style="padding: 20px 30px;">
        <div style="width: 200px; padding-top: 50px; float: left;">
            <#if (currentTeacherDetail.subject == "MATH")!false>
                <p style="padding: 5px 0;" class="text_center"><a onclick="statistics();" class="w-btn w-btn-orange" href="/teacher/resource/uploadmathfalliblequestion.vpage" style="position: relative">上传难题、易错题</a></p>
                <p style="padding: 5px 0;" class="text_center"><a class="w-btn" href="/teacher/resource/uploadmathexampaper.vpage">上传数学试卷</a></p>
            </#if>
            <#--<p style="padding: 5px 0;" class="text_center"><a class="public_b orange_b" href="/teacher/resource/uploadenglishkebiao.vpage" style="position: relative"><i><span style="padding: 0 8px 0 5px">上传2013期末试卷</span></i><span class="icon_general icon_general_67" style="position: absolute; right: -6px;; top: -10px;"></span></a></p>-->
            <#if (currentTeacherDetail.subject == "ENGLISH")!false>
                <p style="padding: 5px 0;" class="text_center">
                    <a class="w-btn" href="/teacher/resource/uploadenglishspokenpaper.vpage" style="position: relative;">
                        上传口语试卷
                    </a>
                </p>
                <p style="padding: 5px 0;" class="text_center">
                    <a class="w-btn" href="/teacher/resource/uploadenglishexampaper.vpage" style="position: relative;">
                        上传英语试卷
                    </a>
                </p>
            </#if>
        </div>
        <div style=" margin: 0 0 0 210px;">
            <strong>上传规则：</strong>
            <p style="margin-bottom: 10px;">
                1.上传的试卷审核周期为10个工作日，审核通过后根据上传质量给予相应的奖励
                2.违反上传规定或不符合要求的内容将被移除且不能获得奖励
                3.上传文件格式：上传的文件不得大于20M，支持的格式类型：
            </p>
            <p><img src="<@app.link href="public/skin/teacherv3/images/publicbanner/makeup.png"/>" width="500"></p>
        </div>
        <div class="edge_vox">
            ${pageBlockContentGenerator.getPageBlockContentHtml('TeacherIndex', 'uploadResourcesBoxItems')}
        </div>
	</div>
    <script type="text/javascript">
        function statistics(){
            $17.tongji("数学老师_上传难题，易错题", "");
        }

        $(function(){
            $("input:checkbox[name=sure]").click(function(){
                var _check = $("input:checkbox[name=sure]").is(':checked');
                var _color = $(".align_center a");
                if( _check == false){
                    _color.removeClass("blue_b").addClass("gray_b");
                }else{
                    _color.removeClass("gray_b").addClass("blue_b");
                }
            });
        });
    </script>
</@temp.page>
