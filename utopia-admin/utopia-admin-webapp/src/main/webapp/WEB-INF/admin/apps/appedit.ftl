<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="第三方应用管理" page_num=10>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<br/><br/>
<div id="main_container" class="span9">
    <form class="form-horizontal" action="updatevendorapp.vpage" method="post" id="vendor_app_edit_form">
        <input type="hidden" id="vendorAppId" name="vendorAppId" value="${vendorApp.id}"/>
        <div class="control-group">
            <label class="col-sm-2 control-label">开发商</label>
            <div class="controls">
                <select id="vendorId" name="vendorId">
                    <#list appVendorList as appVendorItem>
                        <option value="${appVendorItem.id}" <#if vendorApp.vendorId?? && vendorApp.vendorId == appVendorItem.id>selected</#if>>${appVendorItem.cname!}</option>
                    </#list>
                </select>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">应用名(中文)</label>
            <div class="controls">
                <input type="text" id="appCname" name="appCname" class="form-control" value="${vendorApp.cname!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">应用名(英文)</label>
            <div class="controls">
                <input type="text" id="appEname" name="appEname" class="form-control" value="${vendorApp.ename!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">应用名(略称)</label>
            <div class="controls">
                <input type="text" id="appSname" name="appSname" class="form-control" value="${vendorApp.shortName!}" maxlength="10"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">活力类型</label>
            <div class="controls">
                <input type="text" id="vitalityType" name="vitalityType" class="form-control" value="${vendorApp.vitalityType!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">银币类型</label>
            <div class="controls">
                <input type="text" id="integralType" name="integralType" class="form-control" value="${vendorApp.integralType!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">APP地址</label>
            <div class="controls">
                <input type="text" id="appUrl" name="appUrl" class="form-control" value="${vendorApp.appUrl!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">APP图标</label>
            <div class="controls">
                <input type="text" id="appIcon" name="appIcon" class="form-control" value="${vendorApp.appIcon!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">APPKey</label>
            <div class="controls">
                <input type="text" id="appKey" name="appKey" class="form-control" value="${vendorApp.appKey!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">SecretKey</label>
            <div class="controls">
                <input type="text" id="secretKey" name="secretKey" class="form-control" value="${vendorApp.secretKey!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">Callback URL</label>
            <div class="controls">
                <input type="text" id="callbackUrl" name="callbackUrl" class="form-control" value="${vendorApp.callBackUrl!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">购买地址</label>
            <div class="controls">
                <input type="text" id="purchaseUrl" name="purchaseUrl" class="form-control" value="${vendorApp.purchaseUrl!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">每日最大访问次数</label>
            <div class="controls">
                <input type="text" id="dayMaxAccess" name="dayMaxAccess" class="form-control" value="${vendorApp.dayMaxAccess!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">每日最大活力</label>
            <div class="controls">
                <input type="text" id="dayMaxPk" name="dayMaxPk" class="form-control" value="${vendorApp.dayMaxAddPK!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">每日最大银币</label>
            <div class="controls">
                <input type="text" id="dayMaxIntegral" name="dayMaxIntegral" class="form-control" value="${vendorApp.dayMaxAddIntegral!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">排序</label>
            <div class="controls">
                <input type="text" id="rank" name="rank" class="form-control" value="${vendorApp.rank!0}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">IP白名单</label>
            <div class="controls">
                <input type="text" id="serverIps" name="serverIps" class="form-control" value="${vendorApp.serverIps!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">运行环境</label>
            <div class="controls">
                <select id="runtimeMode"  name="runtimeMode" class="form-control">
                    <option value="30" <#if vendorApp.runtimeMode?? && vendorApp.runtimeMode == 30>selected</#if>>Testing</option>
                    <option value="40" <#if vendorApp.runtimeMode?? && vendorApp.runtimeMode == 40>selected</#if>>Staging</option>
                    <option value="50" <#if vendorApp.runtimeMode?? && vendorApp.runtimeMode == 50>selected</#if>>Production</option>
                </select>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">适配年级</label>
            <div class="controls">
                <input type="text" id="clazzLevel" name="clazzLevel" class="form-control" value="${vendorApp.clazzLevel!''}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">状态</label>
            <div class="controls">
                <select id="status" name="status" class="form-control">
                    <option value="ONLINE" <#if vendorApp.status?? && vendorApp.status == 'ONLINE'>selected</#if>>ONLINE</option>
                    <option value="OFFLINE" <#if vendorApp.status?? && vendorApp.status == 'OFFLINE'>selected</#if>>OFFLINE</option>
                    <option value="SUSPEND" <#if vendorApp.status?? && vendorApp.status == 'SUSPEND'>selected</#if>>SUSPEND</option>
                </select>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">维护提示</label>
            <div class="controls">
                <textarea id="suspendMessage" name="suspendMessage" class="form-control" rows="5">${vendorApp.suspendMessage!}</textarea>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">是否免费APP</label>
            <div class="controls">
                <select id="isPaymentFree" name="isPaymentFree" class="form-control">
                    <option value="true" <#if (vendorApp.isPaymentFree)!false>selected</#if>>是</option>
                    <option value="false" <#if (!vendorApp.isPaymentFree)!true>selected</#if>>否</option>
                </select>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">是否教育类APP</label>
            <div class="controls">
                <select id="isEduApp" name="isEduApp" class="form-control">
                    <option value="true" <#if (vendorApp.isEduApp)!false>selected</#if>>是</option>
                    <option value="false" <#if (!vendorApp.isEduApp)!true>selected</#if>>否</option>
                </select>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">学生在哪端可以使用</label>
            <div class="controls">
                <textarea id="playSources" name="playSources" class="form-control" rows="5">${vendorApp.playSources!''}</textarea>
                填写提示：pc,wechat,app，学生在哪端可以使用；如果为多个，用逗号分隔。
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">移动端应用URL</label>
            <div class="controls">
                <input type="text" id="appmUrl" name="appmUrl" class="form-control" value="${vendorApp.appmUrl!''}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">IOS壳版本号学生端(移动端应用必须提供)</label>
            <div class="controls">
                <input type="text" id="version" name="version" class="form-control" value="${vendorApp.version!''}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">移动端应用Icon地址</label>
            <div class="controls">
                <input type="text" id="appmIcon" name="appmIcon" class="form-control" value="${vendorApp.appmIcon!''}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">是否开放家长端购买（家长app和微信）</label>
            <div class="controls">
                <select id="wechatBuyFlag" name="wechatBuyFlag" class="form-control">
                    <option value="true" <#if (vendorApp.wechatBuyFlag)!false>selected</#if>>是</option>
                    <option value="false" <#if (!vendorApp.wechatBuyFlag)!true>selected</#if>>否</option>
                </select>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">副标题</label>
            <div class="controls">
                <input type="text" id="subhead" name="subhead" class="form-control" value="${vendorApp.subhead!''}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">横屏竖屏自适应</label>
            <div class="controls">
                <select id="orientation" name="orientation" class="form-control">
                    <option value="portrait" <#if vendorApp.orientation?? && vendorApp.orientation == 'portrait'>selected</#if>>竖屏</option>
                    <option value="landscape" <#if vendorApp.orientation?? && vendorApp.orientation == 'landscape'>selected</#if>>横屏</option>
                    <option value="sensor" <#if vendorApp.orientation?? && vendorApp.orientation == 'sensor'>selected</#if>>自适应</option>
                </select>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">应用描述</label>
            <div class="controls">
                <textarea id="description" name="description" class="form-control" rows="5">${vendorApp.description!''}</textarea>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">是否存在app内部购买的虚拟产品</label>
            <div class="controls">
                <select id="virtualItemExist" name="virtualItemExist" class="form-control">
                    <option value="true" <#if (vendorApp.virtualItemExist)!false>selected</#if>>是</option>
                    <option value="false" <#if (!vendorApp.virtualItemExist)!true>selected</#if>>否</option>
                </select>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">浏览器内核</label>
            <div class="controls">
                <select id="browser" name="browser" class="form-control">
                    <option value="system" <#if (vendorApp.browser == 'system')!false>selected</#if>>system</option>
                    <option value="crossWalk" <#if (vendorApp.browser == 'crossWalk')!false>selected</#if>>crossWalk</option>
                </select>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">安卓壳版本号学生端(移动端应用必须提供)</label>
            <div class="controls">
                <input type="text" id="versionAndroid" name="versionAndroid" class="form-control" value="${vendorApp.versionAndroid!''}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">IOS壳版本号家长端(如果家长端可以使用必须提供)</label>
            <div class="controls">
                <input type="text" id="iosParentVersion" name="iosParentVersion" class="form-control" value="${vendorApp.iosParentVersion!''}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">安卓壳版本号家长端(如果家长端可以使用必须提供)</label>
            <div class="controls">
                <input type="text" id="androidParentVersion" name="androidParentVersion" class="form-control" value="${vendorApp.androidParentVersion!''}"/>
            </div>
        </div>
        <div class="control-group">
            <div class="controls">
                <button type="button" id="btn_cancel" name="btn_cancel" class="btn">取消</button> &nbsp;&nbsp; <button type="submit" class="btn btn-primary">保存</button>
            </div>
        </div>

    </form>
</div>
<script type="text/javascript">

    $(function() {
        $('#btn_cancel').on('click', function () {
            $("#vendor_app_edit_form").attr("action", "appindex.vpage");
            $("#vendor_app_edit_form").submit();
        });
    });

</script>
</@layout_default.page>