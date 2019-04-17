<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page>
    <@sugar.capsule js=["countdown"] css=["project.talentspike"] />
<div class="main">
    <div class="main_in">
        <div class="main_in2">
            <div class="main_in1">
                <p class="p03">
                    ${showDate}
                </p>
                <div class="number">
                    <p class="p01">${(currentStudentDetail.clazzLevel.description)!}${(currentStudentDetail.clazz.className)!}</p>
                    <#if available == "SECKILL_ON" || available == "SECKILL_OFF">
                        <p class="p06" id="basic_timeBox"></p>
                    </#if>
                </div>
                <p class="p02">
                ${(currentStudentDetail.studentSchoolName)!}
                </p>
                <p class="p05">冒险岛由北京敦煌教育科技有限责任公司提供</p>

                <#if available == "SUCCESS_ME">
                    <span class="btn2" style="display: block;" title="已获得免费名额"></span>
                <#elseif available == "SUCCESS_OTHER">
                    <span style="display: block;" class="btn1" title="名额已经被抢走了"></span>
                <#elseif available == "SECKILL_ON">
                    <form action="/student/activity/seckill.vpage" method="post">
                        <input type="submit" class="btn" value="" name="submit" style="cursor: pointer;"/>
                    </form>
                <#elseif available == "SECKILL_OFF">
                    <span style="display: block;" class="btn3" title="秒杀还没开始"></span>
                </#if>
            </div>

            <div class="images">
                <div class="im02"></div>
                <div class="im03"></div>
                <div class="im04"></div>
                <div class="im05"></div>
            </div>
            <@ftlmacro.chargeinfo name="all" game="2" />
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $17.tongji('学生-冒险岛秒杀-宣传页');
        $('#basic_timeBox').countdown( "${(startDate)!''}", function(event) {
            $(this).html(event.strftime(
                '<span class="span01 span001">%D</span> '
                + '<span class="span01 span002">%H</span> '
                + '<span class="span01 span003">%M</span> '
                + '<span class="span01 span004">%S</span>')
            );
        }).on('finish.countdown', function(event){
            var obj = $(".btn3");
            if('${.now}' < '${(startDate)!''}'){
                obj.hide();
                obj.after('<form action="/student/activity/seckill.vpage" method="post"><input type="submit" value="" class="btn" name="submit"/></form>');
            }
        });
    });
</script>
</@temp.page>