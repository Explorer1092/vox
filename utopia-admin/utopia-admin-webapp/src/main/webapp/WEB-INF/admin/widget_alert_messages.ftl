<#assign messages = requestContext.getAlertMessageManager().getMessages() />
<#list messages as msg>
<div class="alert alert-${msg.category}">${msg.content?html}</div>
</#list>
${requestContext.getAlertMessageManager().clearMessages()}
