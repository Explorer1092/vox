<#import '../layout.ftl' as layout>

<@layout.page className='PersonFeedback' title="意见反馈" pageJs="second" >

<#include "../constants.ftl">

<#escape x as x?html>
    <div class="parentApp-feedBack doFeedback">
        <div class="feedHead">请选择您的反馈类别</div>
        <div class="feedClass doFeedTypes">
            <#assign types = [
                "账号类",
                "作业类",
                "功能类",
                "建议类"
            ]>

            <#list types as type>
                <label class="doFeedType <#if type_index == 0 >active</#if>"><span></span>${type}</label>
            </#list>
        </div>
        <div class="feedHead">请输入意见与建议</div>
        <div class="feedText">
            <textarea name="q"  cols="30" rows="5" placeholder="我的意见与建议..."></textarea>
        </div>
        <div class="feedSent">
            <div class="doSendFeedback">发送</div>
        </div>

        <div class="doFeedbackDone" style="display : none;">
            <div class="parentApp-emptyProm parentApp-emptyProm-5">
                <div class="promIco"></div>
                <div class="promTxt">发送成功，感谢您的反馈！</div>
            </div>
        </div>
    </div>
</#escape>

</@layout.page>
