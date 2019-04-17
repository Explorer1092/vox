<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page>
    <@sugar.capsule js=["countdown"] css=["project.examspike"] />
<div class="main">
    <div class="main_in">
        <div class="main_in2">
            <div class="main_in1">
                <p class="p01">${(currentStudentDetail.clazzLevel.description)!}${(currentStudentDetail.clazz.className)!}</p>
                <p class="p02">${(currentStudentDetail.studentSchoolName)!}</p>
                <p class="p03">
                    ${showDate}
                </p>
                <p class="p04">免费获得阿分题7天使用权，每班仅一位</p>
                <div class="btnBoxMiaosha">
                    <#if available == "SUCCESS_ME">
                        <a class="btn2" style="display: block;" title="已获得免费名额"></a>
                    <#elseif available == "SUCCESS_OTHER">
                        <a style="display: block;" class="btn1" title="名额已经被抢走了"></a>
                    <#elseif available == "SECKILL_ON">
                        <form action="/student/activity/seckill.vpage" method="post">
                            <input type="submit" value="" class="start" name="submit"/>
                        </form>
                    <#elseif available == 'SECKILL_OFF'>
                        <a style="display: block;" class="unstart" title="秒杀还没开始"></a>
                    </#if>

                    <p class="p05">阿分题由北京敦煌教育科技有限责任公司提供</p>
                </div>
                <div class="number">
                    <p class="p06" id="afenti_timeBox"></p>
                    <p class="p07">${advanceDate}</p>
                </div>
            </div>
            <div class="images">
                <div class="im02"></div>
                <div class="im03"></div>
                <div class="im04"></div>
                <div class="im05"></div>
            </div>
            <@ftlmacro.chargeinfo name="all" game="1" />
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $17.tongji('学生-阿分题秒杀-宣传页');
        $('#afenti_timeBox').countdown( "${(startDate)!''}", function(event) {
            $(this).html(event.strftime(
                '<span class="span01 span001">%D</span> '
                + '<span class="span01 span002">%H</span> '
                + '<span class="span01 span003">%M</span> '
                + '<span class="span01 span004">%S</span>')
            );
        }).on('finish.countdown', function(event){
            var obj = $(".unstart");
            if('${.now}' < '${(startDate)!''}'){
                obj.hide();
                obj.after('<form action="/student/activity/seckill.vpage" method="post"><input type="submit" value="" class="start" name="submit"/></a></form>');
            }
        });
    });
</script>
</@temp.page>