<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="查询短信" page_num=3>
<div id="main_container" class="span12">
    <div>
        <form method="post" action="findMobileMessage.vpage" class="form-horizontal">
            <legend>查询短信</legend>
            <ul class="inline form_datetime">
                <li>
                    <label for="mobile">
                        手机号
                        <input name="mobile" id="mobile" type="text" maxlength="11"/>
                    </label>
                </li>
                <li>
                    <button type="submit" class="btn btn-primary">查询</button>
                </li>
            </ul>
        </form>
    </div>
    <br/>
    <div>
        <legend>查询结果：</legend>
        <#if smsMessageList??>
            <table style="border-width: 2px;" class="table table-bordered">
                <thead>
                <tr>
                    <th colspan="2">手机号：<span style="color:dodgerblue;">${queryMobilMessage_mobile!}</span></th>
                </tr>
                <tr>
                    <th style="width:100px;">创建时间</th>
                    <th>短信类型</th>
                    <th width="25%">短信内容</th>
                    <th>状态</th>
                    <th>错误原因</th>
                    <th>发送通道</th>
                </tr>
                </thead>
            <tbody>
                <#if smsMessageList?size gt 0>
                    <#list smsMessageList as SMSMessage>
                    <tr>
                        <td>${(SMSMessage.createTime)?string("yyyy-MM-dd HH:mm:ss")?replace(" ", "<br/>")}</td>
                        <td>
                            ${(SMSMessage.smsType.getDescription())!'--'}
                            <br/>
                            ${(SMSMessage.smsType.name())!'--'}
                        </td>
                        <td>
                            ${(SMSMessage.smsContent)!?html}
                        </td>
                        <td>
                            <#if (SMSMessage.status)??>
                                <#if SMSMessage.status == '1'>
                                    提交成功
                                <#elseif SMSMessage.status == '2'>
                                    发送成功
                                <#else>
                                    发送失败
                                </#if>
                            <#else>
                                --
                            </#if>
                            <#if (SMSMessage.verification)?? && (SMSMessage.verification)>
                                <br/> 是否消费: ${(SMSMessage.consumed)?string("是","否")}
                            <#else>
                                <br/>送达时间：${(SMSMessage.receiveTime)!'--'}
                            </#if>
                            <br/>发送时间：${(SMSMessage.sendTime)!'--'}
                        </td>
                        <td>${(SMSMessage.errorDesc)!'--'}(${(SMSMessage.errorCode)!'--'})</td>
                        <td>${(SMSMessage.smsChannel)!'--'}</td>
                    </tr>
                    </#list>
                </tbody>
                <#else>
                    <tbody>
                    <tr>
                        <td colspan="6" style="text-align: center;">未查询出符合条件的数据</td>
                    </tr>
                    </tbody>
                </#if>
            </table>
        </#if>
    </div>
</div>
</@layout_default.page>