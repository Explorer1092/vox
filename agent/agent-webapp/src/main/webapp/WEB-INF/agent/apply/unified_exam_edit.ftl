<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='修改考试控制' page_num=3>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fileuploader/SimpleAjaxUploader.min.js"></script>
<style>
    body{text-shadow:none;}
    select.s_time{width: 60px;;}
    .radio input[type="radio"],
    .checkbox input[type="checkbox"]{float: left;margin-left: -7px;}
    .form-horizontal .controls{padding-top: 5px;}
    .apply_input_time{width: 80px;}
    .show{display: none;}
    .achievement td{margin:0 6px 5px 6px;}
</style>
<#macro forOption start=0 end=0 defaultVal=0>
    <#list start..end as index>
    <option value="<#if index lt 10>0</#if>${index}" <#if defaultVal == index>selected</#if>><#if index lt 10>0</#if>${index}</option>
    </#list>
</#macro>
<div class="row-fluid">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i>修改考试控制</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <#if apply?? >
            <div class="box-content">
                <div class="form-horizontal">
                    <input type="hidden" id="targetSchoolId" name="targetSchoolId" />
                    <fieldset>
                        <input id="id" value="${apply.id}" type ="hidden">
                        <div class="control-group" disabled="true">
                            <div style="display: inline-block">
                                <label class="control-label">考试开始时间:</label>
                                <div class="controls">
                                    <input type="text" id="unifiedExamBeginTime" name ="unifiedExamBeginTime" value = "${apply.unifiedExamBeginTime?string('yyyy-MM-dd')}"   class="apply_input_time">
                                    <select class="s_time" name="unifiedExamBeginTimeHour" id="unifiedExamBeginTimeHour">
                                        <#assign beginTime_HH = apply.unifiedExamBeginTime?string("HH")>
                                            <#assign beginTime_mm = apply.unifiedExamBeginTime?string('mm')>
                                            <@forOption start=0 end=23 defaultVal= beginTime_HH?number />
                                    </select> 时
                                    <select class="s_time" name="unifiedExamBeginTimeMin" id="unifiedExamBeginTimeMin">
                                        <@forOption start=0 end=59 defaultVal= beginTime_mm?number />
                                    </select> 分
                                </div>
                            </div>
                            <div style="display: inline-block">
                                <label class="control-label"> 考试截止时间：</label>
                                <div class="controls">
                                    <input type="text" id="unifiedExamEndTime" name ="unifiedExamEndTime" value = "${apply.unifiedExamEndTime?string('yyyy-MM-dd')}" class="apply_input_time">
                                    <select class="s_time" name="unifiedExamEndTimeHour" id="unifiedExamEndTimeHour">
                                        <#assign endTime_HH = apply.unifiedExamEndTime?string('HH')>
                                            <#assign beginTime_mm = apply.unifiedExamEndTime?string('mm')>
                                            <@forOption start=0 end=23 defaultVal= endTime_HH?number />
                                    </select> 时
                                    <select class="s_time" name="unifiedExamEndTimeMin" id="unifiedExamEndTimeMin">
                                        <@forOption start=0 end=59 defaultVal= beginTime_mm?number />
                                    </select> 分
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">老师批改截止时间</label>
                            <div class="controls">
                                <input type="text" id="correctingTestPaper" name ="correctingTestPaper" value = "${apply.correctingTestPaper?string('yyyy-MM-dd')}"  class="apply_input_time">
                                <select class="s_time" name="correctingTestPaperHour" id="correctingTestPaperHour">
                                    <#assign testPaper_HH = apply.correctingTestPaper?string('HH')>
                                    <#assign testPaper_mm = apply.correctingTestPaper?string('mm')>
                                    <@forOption start=0 end=23 defaultVal= testPaper_HH?number />
                                </select> 时
                                <select class="s_time" name="correctingTestPaperMin" id="correctingTestPaperMin">
                                    <@forOption start=0 end=59 defaultVal=testPaper_mm?number />
                                </select> 分
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">学生端成绩发布时间</label>
                            <div class="controls">
                                <input type="text" id="achievementReleaseTime" name ="achievementReleaseTime" value = "${apply.achievementReleaseTime?string('yyyy-MM-dd')}" class="apply_input_time">
                                <select class="s_time" name="achievementReleaseTimeHour" id="achievementReleaseTimeHour">
                                    <#assign releaseTime_HH = apply.achievementReleaseTime?string('HH')>
                                    <#assign releaseTime_mm = apply.achievementReleaseTime?string('mm')>
                                    <@forOption start=0 end=23 defaultVal= releaseTime_HH?number />
                                </select> 时
                                <select class="s_time" name="achievementReleaseTimeMin" id="achievementReleaseTimeMin">
                                    <@forOption start=0 end=59 defaultVal= releaseTime_mm?number />
                                </select> 分
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">考试开始后</label>
                            <div class="controls">
                                <input type="text" id="minSubmittedTestPaper" name ="minSubmittedTestPaper" value = "${apply.minSubmittedTestPaper}"> 分钟后方能交卷
                            </div>
                        </div>
                        <div id = "oralLanguageFrequency_div" class="control-group" <#if apply.testPaperType??><#list apply.testPaperType?split(",") as type><#if type == "ORAL"><#else>style="display:none;" </#if></#list></#if>>
                            <label class="control-label">口语可答题次数</label>
                            <div class="controls">
                                <select id ="oralLanguageFrequency">
                                    <option name="oralLanguageFrequency" value="1" <#if apply.oralLanguageFrequency?? && apply.oralLanguageFrequency == 1>selected</#if>  >一次</option>
                                    <option name="oralLanguageFrequency" value="2" <#if apply.oralLanguageFrequency?? && apply.oralLanguageFrequency == 2>selected</#if> >两次</option>
                                    <option name="oralLanguageFrequency" value="3" <#if apply.oralLanguageFrequency?? && apply.oralLanguageFrequency == 3>selected</#if> >三次</option>
                                    <option name="oralLanguageFrequency" value="-1" <#if apply.oralLanguageFrequency?? && apply.oralLanguageFrequency == -1>selected</#if> >无限次</option>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">答题时长</label>
                            <div class="controls">
                                <input type="text" id="maxSubmittedTestPaper" name ="maxSubmittedTestPaper" value = "${apply.maxSubmittedTestPaper}"> 分钟
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">成绩分制</label>
                            <div class="controls">
                                <input type="radio"  name ="grading" value="0" <#if apply.gradeType?? && apply.gradeType == 0>checked</#if>> 分数制
                                <input type="radio"  name ="grading" value="1" <#if apply.gradeType?? && apply.gradeType == 1>checked</#if> > 等第制
                            </div>
                        </div>
                        <div id="achievement" class="control-group grading">
                            <label class="control-label">等第设置</label>
                            <div class="controls">
                                <#if apply.ranks??>
                                    <table id="levelContainer" class="achievement">
                                    </table>
                                <#else>
                                <table class="achievement">
                                    <tr class="achievementGrading">
                                        <td>等第名称</td>
                                        <td><input class="achievement_name" value="优" type="text" style="width:50px"></td>
                                        <td><input class="initialScore" type="number" value="85" style="width:50px"> %</td>
                                        <td><=分数区间<=</td>
                                        <td><input class="endResults" type="number" value="100" style="width:50px"> %</td>
                                        <td class="achievement_add btn btn-primary">添加</td>
                                    </tr>
                                    <tr class="achievementGrading">
                                        <td>等第名称</td>
                                        <td><input class="achievement_name" value="良" type="text" style="width:50px"></td>
                                        <td><input class="initialScore" type="number" value="75" style="width:50px"> %</td>
                                        <td><=分数区间<</td>
                                        <td><input class="endResults" type="number" value="85" style="width:50px"> %</td>
                                        <td class="achievement_add btn btn-primary">添加</td>
                                        <td class="achievement_remove btn btn-primary">删除</td>
                                    </tr>
                                    <tr class="achievementGrading">
                                        <td>等第名称</td>
                                        <td><input class="achievement_name" value="合格" type="text" style="width:50px"></td>
                                        <td><input class="initialScore" type="number" value="60" style="width:50px"> %</td>
                                        <td><=分数区间<</td>
                                        <td><input class="endResults" type="number" value="75" style="width:50px"> %</td>
                                        <td class="achievement_add btn btn-primary">添加</td>
                                        <td class="achievement_remove btn btn-primary">删除</td>
                                    </tr>
                                    <tr class="achievementGrading">
                                        <td>等第名称</td>
                                        <td><input class="achievement_name" value="待合格" type="text" style="width:50px"></td>
                                        <td><input class="initialScore" type="number" value="0" style="width:50px"> %</td>
                                        <td><=分数区间<</td>
                                        <td><input class="endResults" type="number" value="60" style="width:50px"> %</td>
                                        <td class="achievement_add btn btn-primary">添加</td>
                                        <td class="achievement_remove btn btn-primary">删除</td>
                                    </tr>
                                    <tr class="trd show">
                                        <td>等第名称</td>
                                        <td><input class="achievement_name" value="" type="text" style="width:50px"></td>
                                        <td><input class="initialScore" type="number" value="" style="width:50px"> %</td>
                                        <td><=分数区间<</td>
                                        <td><input class="endResults" type="number" value="" style="width:50px"> %</td>
                                        <td class="achievement_add btn btn-primary">添加</td>
                                        <td class="achievement_remove btn btn-primary">删除</td>
                                    </tr>
                                </#if>
                            </table>
                            </div>
                        </div>
                        <div class="form-actions" style="text-align: center">
                            <button id="submitBtn" type="button" class="btn btn-success need_auditing" style=" margin-right: 20px">确认修改</button>
                            <button id="cancelBtn" type="button" class="btn btn-success" style=" margin-left: 20px">取消</button>
                        </div>
                    </fieldset>
                </div>
            </div>
        </#if>
    </div>
</div>
<script type="text/html" id="ranksContainer">
    <table class="achievement">
        <%for(var i=0;i< res.length;i++){%>
        <tr class="achievementGrading">
            <td>等第名称</td>
            <td><input class="achievement_name" value="<%=res[i].rankName%>" type="text" style="width:50px"></td>
            <td><input class="initialScore" type="number" value="<%=res[i].bottom%>" style="width:50px"> %</td>
            <td><=分数区间<=</td>
            <td><input class="endResults" type="number" value="<%=res[i].top%>" style="width:50px"> %</td>
            <td class="achievement_add btn btn-primary">添加</td>
            <td class="achievement_remove btn btn-primary">删除</td>
        </tr>
        <%}%>
        <tr class="trd show">
            <td>等第名称</td>
            <td><input class="achievement_name" value="" type="text" style="width:50px"></td>
            <td><input class="initialScore" type="number" value="" style="width:50px"> %</td>
            <td><=分数区间<</td>
            <td><input class="endResults" type="number" value="" style="width:50px"> %</td>
            <td class="achievement_add btn btn-primary">添加</td>
            <td class="achievement_remove btn btn-primary">删除</td>
        </tr>
    </table>
</script>
<script type="text/javascript">
$(function () {
    <#if apply.ranks?? && apply.ranks != "">
        var ranks = ${apply.ranks!""};
        $('#levelContainer').html(template("ranksContainer",{res:ranks}));
        $('.achievementGrading:first-child').find('.achievement_remove').remove();
    </#if>
    $(document).on("click",".achievement_add",function(){
        if($(".trd").hasClass("show")){
            $(".trd").removeClass("show").addClass("achievementGrading");
        }else{
            var trd = $(".trd")[0];
            var trd_html = trd.outerHTML;
            var lastTr = $(".achievement").find("tr:last");
            lastTr.after(trd_html);
            if($(".achievement tr").length>9){
                $('.achievement_add').hide();
            }
        }
    });
    $(document).on('click', '.achievement_remove', function () {
        if($('.achievement_remove').length == 1){
            $(this).parent().addClass("show");
        }else{
            $(this).parent().remove();
        }
        if($('.achievement_add').length < 10){
            $('.achievement_add').show();
        }
    });
    //日期控件绑定
    $("#unifiedExamBeginTime").datepicker({
        dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
        monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : 'new Date()',
        minDate:'new Date()',
        numberOfMonths  : 1,
        changeMonth: false,
        changeYear: false,
        onSelect : function (selectedDate){
            var $unifiedExamEndTime_val =  $("#unifiedExamEndTime").val();
            if($unifiedExamEndTime_val ){
                var unifiedExamEndTime = new Date($unifiedExamEndTime_val.replace(/-/g,"/"));
                var $selectedDate = new Date(selectedDate.replace(/-/g,"/"));
                if($selectedDate > unifiedExamEndTime){
                    alert("考试开始时间不能小于考试结束日期");
                    $("#unifiedExamEndTime").val(null);
                }
            }
            return true;
        }
    });
    $("#unifiedExamEndTime").datepicker({
        dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
        monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : 'new Date()',
        minDate:'new Date()',
        numberOfMonths  : 1,
        changeMonth: false,
        changeYear: false,
        onSelect : function (selectedDate){
            var $unifiedExamBeginTime_val =  $("#unifiedExamBeginTime").val();
            if($unifiedExamBeginTime_val ){
                var $unifiedExamBeginTime = new Date($unifiedExamBeginTime_val.replace(/-/g,"/"));
                var $selectedDate = new Date(selectedDate.replace(/-/g,"/"));
                if($selectedDate < $unifiedExamBeginTime){
                    alert("考试截止时间不能小于考试开始日期");
                    $("#unifiedExamEndTime").val(null);
                }
            }
            return true;
        }
    });
    $("#correctingTestPaper").datepicker({
        dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
        monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : 'new Date()',
        minDate:'new Date()',
        numberOfMonths  : 1,
        changeMonth: false,
        changeYear: false,
        onSelect : function (selectedDate){
            var $unifiedExamEndTime_val =  $("#unifiedExamEndTime").val();
            if($unifiedExamEndTime_val ){
                var $unifiedExamEndTime = new Date($unifiedExamEndTime_val.replace(/-/g,"/"));
                var $selectedDate = new Date(selectedDate.replace(/-/g,"/"));
                if($selectedDate < $unifiedExamEndTime){
                    alert("老师批改试卷时间不能小于考试结束日期");
                    $("#correctingTestPaper").val(null);
                }
            }
            return true;
        }
    });
    $("#achievementReleaseTime").datepicker({
        dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
        monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
        dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate     : 'new Date()',
        minDate:'new Date()',
        numberOfMonths  : 1,
        changeMonth: false,
        changeYear: false,
        onSelect : function (selectedDate){
            var $correctingTestPaper_val =  $("#correctingTestPaper").val();
            if($correctingTestPaper_val ){
                var $correctingTestPaper = new Date($correctingTestPaper_val.replace(/-/g,"/"));
                var $selectedDate = new Date(selectedDate.replace(/-/g,"/"));
                if($selectedDate < $correctingTestPaper){
                    alert("成绩发布日期不能小于老师批改试卷时间");
                    $("#achievementReleaseTime").val(null);
                }
            }
            return true;
        }
    });
    $('#cancelBtn').on('click',function(){
        window.history.back();
    });

    //提交数据 数据校验和数据整合
    $('#submitBtn').on('click',function(){
        // 等第设置
        var _this = $(this);
        if ($("input[name='grading']:checked").val() == "1") {
            var achievement = $('.achievementGrading');
            var len = achievement.length;
            var achievement_mix = 0;
            if((achievement.eq(0).find(".endResults").val() - achievement.eq(len-1).find(".initialScore").val()) != 100){
                layer.alert("等第设置中成绩区间必须涵盖0-100%");
                return false;
            }
            for(var i = 0; i < achievement.length; i++){
                if(achievement.eq(i).find(".achievement_name").val() == "" || achievement.eq(i).find(".achievement_name").val() == achievement.eq(i+1).find(".achievement_name").val()){
                    alert("等第名称不能为空或不能重复");
                    return false;
                }
                var achievement_sub = achievement.eq(i).find(".endResults").val() - achievement.eq(i).find(".initialScore").val();
                achievement_mix += achievement_sub ;
                if( i+1 < len && parseFloat(achievement.eq(i).find(".initialScore").val() != achievement.eq(i+1).find(".endResults").val())){
                    alert("等第下限不能大于上限");
                    return false;
                }else{
                    if(i==0){
                        if(  parseFloat(achievement.eq(0).find(".initialScore").val()) > parseFloat(achievement.eq(0).find(".endResults").val()) ){
                            alert("等第下限不能大于上限");
                            return false;
                        }
                    }else{
                        if(  parseFloat(achievement.eq(i).find(".initialScore").val()) >= parseFloat(achievement.eq(i).find(".endResults").val()) ){
                            alert("等第下限不能大于上限");
                            return false;
                        }
                    }
                }

            }
            if(achievement_mix != 100){
                layer.alert("等第上限或下限设置错误");
                return false;
            }
        }
        var tdArr = [];
        for(var i =0;i< $('.achievementGrading').length;i++){
            tdArr.push({"rankName":$('.achievementGrading .achievement_name').eq(i).val(),"bottom":$('.achievementGrading .initialScore').eq(i).val(),"top":$('.achievementGrading .endResults').eq(i).val()})
        }

        var $unifiedExamBeginTime_val = $("#unifiedExamBeginTime").val();
        if(!$unifiedExamBeginTime_val){
            layer.alert("考试开始时间不能为空");
            return false;
        }
        $unifiedExamBeginTime_val =  $unifiedExamBeginTime_val + " " + $("#unifiedExamBeginTimeHour").val() + ":" + $("#unifiedExamBeginTimeMin").val() + ":00";
        $unifiedExamBeginTime_val = new Date($unifiedExamBeginTime_val.replace(/-/g,"/"));

        var $unifiedExamEndTime_val =$("#unifiedExamEndTime").val();
        if(!$unifiedExamEndTime_val){
            layer.alert("考试结束时间不能为空且不能早于考试开始时间");
            return false;
        }
        $unifiedExamEndTime_val =  $unifiedExamEndTime_val + " " + $("#unifiedExamEndTimeHour").val() + ":" + $("#unifiedExamEndTimeMin").val() + ":00";
        $unifiedExamEndTime_val = new Date( $unifiedExamEndTime_val.replace(/-/g,"/"));
        if($unifiedExamEndTime_val <  $unifiedExamBeginTime_val){
            layer.alert("考试结束时间不能为空且不能早于考试开始时间");
            return false;
        }
        var $correctingTestPaper_val = $("#correctingTestPaper").val();
        if(!$correctingTestPaper_val){
            layer.alert("老师批改试卷时间不能为空且不能早于考试结束时间");
            return false;
        }
        $correctingTestPaper_val = $correctingTestPaper_val + " " + $("#correctingTestPaperHour").val() + ":" + $("#correctingTestPaperMin").val() + ":00";
        $correctingTestPaper_val = new Date( $correctingTestPaper_val.replace(/-/g,"/"));
        if($correctingTestPaper_val <  $unifiedExamEndTime_val){
            layer.alert("老师批改试卷时间不能为空且不能早于考试结束时间");
            return false;
        }
        var $achievementReleaseTime_val = $("#achievementReleaseTime").val();
        if(!$achievementReleaseTime_val){
            layer.alert("学生端成绩发布时间不能为空且不能早于老师批改试卷时间");
            return false;
        }
        $achievementReleaseTime_val = $achievementReleaseTime_val + " " + $("#achievementReleaseTimeHour").val() + ":" + $("#achievementReleaseTimeMin").val() + ":00";
        $achievementReleaseTime_val = new Date($achievementReleaseTime_val.replace(/-/g,"/"));
        if($achievementReleaseTime_val <  $correctingTestPaper_val){
            layer.alert("学生端成绩发布时间不能为空且不能早于老师批改试卷时间");
            return false;
        }
        var  $minSubmittedTestPaper_val =$.trim( $("#minSubmittedTestPaper").val())-0;
        if(!$minSubmittedTestPaper_val || $minSubmittedTestPaper_val <= 1){
            layer.alert("最短答题时长不能为空且需大于一分钟");
            return false;
        }
        var  $maxSubmittedTestPaper_val =$.trim( $("#maxSubmittedTestPaper").val())-0;
        if(!$maxSubmittedTestPaper_val || $maxSubmittedTestPaper_val <  $minSubmittedTestPaper_val){
            layer.alert("答题时长不能为空且不能小于最短答题时长");
            return false;
        }
        var $oralLanguageFrequency_val = $("#oralLanguageFrequency").val();
        var $grading_val = $("input[name='grading']:checked").val();
        var unifiedExamApply = {};
        unifiedExamApply.applyId = $('#id').val();

        unifiedExamApply.unifiedExamBeginTime = $unifiedExamBeginTime_val;
        unifiedExamApply.unifiedExamEndTime = $unifiedExamEndTime_val;
        unifiedExamApply.correctingTestPaper = $correctingTestPaper_val;
        unifiedExamApply.achievementReleaseTime = $achievementReleaseTime_val;
        unifiedExamApply.minSubmittedTestPaper = $minSubmittedTestPaper_val;
        unifiedExamApply.oralLanguageFrequency = $oralLanguageFrequency_val;
        unifiedExamApply.maxSubmittedTestPaper = $maxSubmittedTestPaper_val;

        unifiedExamApply.gradeType = $grading_val;
        unifiedExamApply.ranks = JSON.stringify(tdArr);
        var unifiedExamApplyJson = JSON.stringify(unifiedExamApply);
        if(_this.hasClass('need_auditing')){
            layer.alert('请再次核实申请信息无误！任何信息错误都将导致考试无法进行!',{
                btn:['前往确认']
            },function () {
                layer.close(layer.index);
                _this.removeClass('need_auditing');
            });
        }else {
            $.ajax({
                type: 'POST',
                url: "update_exam_control.vpage",
                dataType: 'json',
                contentType: 'application/json;charset=UTF-8',
                data: unifiedExamApplyJson,
                success: function () {
                    if (confirm("提交成功") == true) {
                        window.location.href = "/apply/view/list.vpage";
                    }
                },
                error: function () {
                    layer.alert(data.info);
                }
            });
        }
    });
});
</script>

</@layout_default.page>
