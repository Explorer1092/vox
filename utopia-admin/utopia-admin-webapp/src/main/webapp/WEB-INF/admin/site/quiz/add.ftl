<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div class="span9">
    <#if info?has_content>
        <div class="alert <#if success?? && success>alert-success<#else>alert-danger</#if>">${info}</div>
    </#if>

    <form id="pushForm" action="savepaperregioninfo.vpage" method="post">
        <ul class="inline">
            <li><input type="radio" value="NO_PAPER_ID" checked="checked" name="paperFlag">无试卷ID</li>
            <li><input type="radio" value="HAVE_PAPER_ID" name="paperFlag">有试卷ID</li>
        </ul>
        <div id="noPaper">
            <fieldset>
                <legend>添加试卷，填写相关信息</legend>
            </fieldset>
            <ul class="inline">
                <li>
                    课&nbsp;&nbsp;&nbsp;本&nbsp;&nbsp;&nbsp;ID
                    <input id="bookId" name="bookId" value="${pushForm.bookId!}" type="text"/>
                </li>
                <li>
                    试卷名称
                    <input id="paperName" name="paperName" value="${pushForm.paperName!}" type="text"/>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    教研员账号
                    <textarea id="authorId" cols="10" rows="3" name="authorId">${pushForm.authorId!}</textarea>
                </li>
            <#--<li>-->
            <#--出卷人名称-->
            <#--<input id="authorName" name="authorName" value="${pushForm.authorName!}" type="text"/>-->
            <#--</li>-->
            </ul>
            <ul class="inline">
                <li>
                    单&nbsp;&nbsp;&nbsp;元&nbsp;&nbsp;&nbsp;ID
                    <textarea id="unitIds" cols="10" rows="3" name="unitIds">${pushForm.unitIds!}</textarea>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    试&nbsp;&nbsp;&nbsp;题&nbsp;&nbsp;&nbsp;ID
                    <textarea id="questionIds" cols="15" rows="10" name="questionIds">${pushForm.questionIds!}</textarea>
                </li>
            </ul>
        </div>
        <div id="hasPaper" style="display: none;">
            <fieldset>
                <legend>填写试卷ID</legend>
            </fieldset>

            <ul class="inline">
                <li>
                    试&nbsp;&nbsp;&nbsp;卷&nbsp;&nbsp;&nbsp;ID
                    <input id="paperId" name="paperId" value="${pushForm.paperId!}" type="text"/>
                </li>
            </ul>
        </div>
        <fieldset style="display: none;">
            <legend>填写上述试卷推送的区域\年级\有效时间(时间格式为yyyy-MM-dd HH:mm:ss)</legend>
        </fieldset>
        <ul class="inline" style="display: none;">
            <li>
                推送区域code
                <input id="pushRegionCode" value="${pushForm.pushRegionCode!}" name="pushRegionCode" type="text"/>
            </li>
            <li>
                年级
                <input id="clazzLevel" value="${pushForm.clazzLevel!}" name="clazzLevel" type="text"/>
            </li>

            <li>
                开始时间
                <input id="beginDateTimeStr" value="${pushForm.beginDateTimeStr!}" name="beginDateTimeStr" placeholder="格式：2012-12-22 00:00:00" type="text"/>
            </li>
            <li>
                结束时间
                <input id="endDateTimeStr" value="${pushForm.endDateTimeStr!}" name="endDateTimeStr" placeholder="格式：2012-12-22 00:00:00" type="text"/>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <input type="submit" value="提交" class="btn btn-primary" id="searchSubmit"/>
                <a class="btn btn-primary" href="list.vpage"/>返回</a>
            </li>
        </ul>
    </form>
</div>
<script type="text/javascript">
    $(function(){
        $.fn.datetimepicker.dates['zh-CN'] = {
            days        : ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"],
            daysShort   : ["周日", "周一", "周二", "周三", "周四", "周五", "周六", "周日"],
            daysMin     : ["日", "一", "二", "三", "四", "五", "六", "日"],
            months      : ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
            monthsShort : ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
            today       : "今日",
            suffix      : [],
            meridiem    : []
        };
        $('[name="beginDateTimeStr"]').datetimepicker({
            language:  'zh-CN',
            weekStart: 1,
            autoclose: 1,
            format: "yyyy-mm-dd hh:ii:ss",
            todayHighlight: 1,
            startView: 2,
            forceParse: 0,
            showMeridian: 1
        });
        $('[name="endDateTimeStr"]').datetimepicker({
            language:  'zh-CN',
            weekStart: 1,
            autoclose: 1,
            todayHighlight: 1,
            format: "yyyy-mm-dd hh:ii:ss",
            startView: 2,
            forceParse: 0,
            showMeridian: 1
        });


        $("input[name='paperFlag']").on("click",function(){
            var $this = $(this);
            if($this.val() == "NO_PAPER_ID"){
                $("#noPaper").css("display","");
                $("#hasPaper").css("display","none");
            }else{
                $("#noPaper").css("display","none");
                $("#hasPaper").css("display","");
            }
        });
    });
</script>
</@layout_default.page>