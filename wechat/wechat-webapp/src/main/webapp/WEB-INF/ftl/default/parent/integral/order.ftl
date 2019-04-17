<#import "../layout.ftl" as integral>
<@integral.page title="贡献班级学豆" pageJs="integralOrder">
<style type="text/css">
    html body{background-color: #E9EAEA;}
</style>
<div class="main body_background_gray">
    <div class="sendBean_box">
        <div class="sb_up">
            <p class="text_gray font_gray" style="font-size: 24px;">贡献班级学豆后，仅用于老师奖励本班学生</p>
            <i class="icon icon_17"></i>
        </div>
        <div class="sb_down">
            <div class="price_content">
                <p id="subject_box">
                    学科：
                    <#if teachers?? && teachers?size gt 0>
                        <#list teachers as teacher>
                            <#if teacher.subject == 'ENGLISH'>
                                <span data-bind="click: $root.selectSubject.bind($data,'${teacher.id!0}','${teacher.subject!0}'), css : {'active' : $data.selectedTeacher() == '${teacher.subject!0}'}">英语</span>
                            <#elseif teacher.subject == 'MATH' >
                                <span data-bind="click: $root.selectSubject.bind($data,'${teacher.id!0}','${teacher.subject!0}'), css : {'active' : $data.selectedTeacher() == '${teacher.subject!0}'}">数学</span>
                            <#elseif teacher.subject == 'CHINESE' >
                                <span data-bind="click: $root.selectSubject.bind($data,'${teacher.id!0}','${teacher.subject!0}'), css : {'active' : $data.selectedTeacher() == '${teacher.subject!0}'}">语文</span>
                            </#if>
                        </#list>
                    </#if>
                </p>
                <p id="coin_list_box">
                    学豆：
                    <span data-bind="click : $data.selectedIntegral.bind($data,20,1), css : {'active' : $data.selectedCount() == 20}"><strong>20</strong><b>个</b></span>
                    <span data-bind="click : $data.selectedIntegral.bind($data,50,2), css : {'active' : $data.selectedCount() == 50}"><strong>50</strong><b>个</b></span>
                    <span data-bind="click : $data.selectedIntegral.bind($data,120,5), css : {'active' : $data.selectedCount() == 120}"><strong>120</strong><b>个</b></span>
                    <span data-bind="click : $data.selectedIntegral.bind($data,260,10), css : {'active' : $data.selectedCount() == 260}"><strong>260</strong><b>个</b></span>
                </p>
                <p>
                    价格：
                    <span style="border:none; cursor:default">
                        <strong style="color:#ff5955" data-bind="text: $data.integalPrice">1</strong><b> 元</b>
                    </span>
                </p>
            </div>
            <div class="inform_content">
                <p>
                    <span data-bind="click: $data.ruleBtn.bind($data,!$data.selectedRule()),css : {'icon_check_active' : $data.selectedRule()}" style="float: left; margin: 10px" class="icon icon_check"></span>
                </p>
                <p style="margin: 0; padding-left: 20px; font-size: 24px; color: #858a91; ">
                    我已阅读<span class="text_blue"> <a href="http://www.17zuoye.com/help/shopagreement.vpage">《一起作业网声明》</a> </span>，自愿送班级学豆， 用于学习。
                </p>
            </div>
        </div>
    </div>
    <form action="/parent/integral/order.vpage" method="POST">
        <div class="foot_btn_box">
            <a href="javascript:void(0);" data-bind="enable: $data.selectedRule(), css : {'btn_disable' : (!$data.selectedRule() || $data.integalPrice() == 0 || $root.selectedTeacher() == '')}, click : $data.buyBtn" class="btn_mark btn_mark_block"><span style="color: #F9F9F9;">立即购买</span></a>
        </div>
        <input type="hidden" name="tid" data-bind="attr : { value : $data.selectedTeacherId}" value=""/>
        <input type="hidden" name="sid" value="${sid!0}"/>
        <input type="hidden" name="price" data-bind="attr : { value : $data.integalPrice}" value=""/>
    </form>
</div>
</@integral.page>