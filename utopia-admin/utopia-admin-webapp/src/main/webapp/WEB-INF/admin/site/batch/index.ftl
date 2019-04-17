<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<#import "head.ftl" as h/>
<@layout_default.page page_title='Web manage' page_num=4>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    span {
        font: "arial";
    }

    .index {
        color: #0000ff;
    }

    .index, .item {
        font-size: 18px;
        font: "arial";
    }

    .warn {
        color: red;
    }
</style>
<div class="span9">

    <@h.head/>

    <fieldset>
        <legend>批量发短信</legend>

        <form method="post" action="/site/sms/messagesend.vpage">
            <ul class="inline">
                <li>
                    <label>输入短信内容：<textarea name="content" cols="45" rows="10"
                                            placeholder="请在这里输入要发送的手机号及内容"></textarea></label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <input class="btn" type="submit" value="提交"/>
                </li>
            </ul>
        </form>
        <form method="post" action="/site/sms/batchsendsms.vpage">
            <ul class="inline">
                <li>
                    <label>输入短信内容（批量）：<textarea name="content" cols="45" rows="10"
                                                placeholder="请在这里输入要发送的内容"></textarea></label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>输入手机号或用户ID（批量）：<textarea name="receivers" cols="45" rows="10"
                                                    placeholder="请在这里输入要发送的手机号或用户ID，一行一条，不支持两种方式混合输入！"></textarea></label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>选择类型：<select name="smsType" id="smsType">
                        <option value="CRM_BATCH_BACK_FLOW">批量发短信_回流</option>
                        <option value="CRM_BATCH_AMBASSADOR">批量发短信_校园大使</option>
                        <option value="CRM_BATCH_REWARD">批量发短信_教学用品中心</option>
                        <option value="CRM_BATCH_INVITE">批量发短信_邀请</option>
                        <option value="CRM_BATCH_MARKET">批量发短信_市场</option>
                    </select>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <input class="btn" type="submit" value="提交（批量）"/>
                    <span style="color: red;">请慎重操作，三一目前不支持17号段的发送，请选择正确的短信类型</span>
                </li>
            </ul>
        </form>
        <div>
            <span style="color: red">
                <#if sendCount??><strong>总共提交了${sendCount!}个</strong></#if>
                <#if messageCount??><strong>成功发送${messageCount!}条短信</strong></#if>
            </span>
            <br><br>
            <label>失败记录：</label>
            <table class="table table-bordered">
                <#if failList??>
                    <#list failList as e>
                        <tr>
                            <td>${e}</td>
                        </tr>
                    </#list>
                </#if>
            </table>
    </fieldset>
    <br/>
</div>
</@layout_default.page>