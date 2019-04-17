<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="申请管理" page_num=21>
<div class="span9">
    <fieldset><legend>AppPush发送申请</legend></fieldset>
    <div class="form-horizontal">
        <#if applyData?has_content && applyData.apply?has_content>
            <div class="modal-body" style="height: auto; overflow: visible;">
                <input type="hidden" id="schoolId" name="schoolId" value="${applyData.apply.schoolId!}"/>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>调整类别</strong></label>
                    <div class="controls">
                        <label class="control-label" id="modifyType" style="text-align: left;"><#if applyData.apply.modifyType == 1>添加学校<#elseif applyData.apply.modifyType == 2>删除学校</#if></label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>学校ID</strong></label>
                    <div class="controls">
                        <label class="control-label" id="schoolIdLabel" style="text-align: left;">${applyData.apply.schoolId!}</label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>学校名称</strong></label>
                    <div class="controls">
                        <label class="control-label" id="schoolName" style="text-align: left;">${applyData.apply.schoolName!}</label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>所属地区</strong></label>
                    <div class="controls">
                        <label class="control-label" id="regionName" style="text-align: left;">${applyData.apply.regionName!}</label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>学校阶段</strong></label>
                    <div class="controls">
                        <label class="control-label" id="schoolLevel" style="text-align: left;"><#if applyData.apply.schoolLevel == 1>小学<#elseif applyData.apply.schoolLevel == 2>初中<#elseif applyData.apply.schoolLevel == 4>高中</#if></label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>相关信息</strong></label>
                    <div class="controls">
                        <label class="control-label" id="regionName" style="text-align: left;"><#if applyData.apply.modifyDesc?has_content>${applyData.apply.modifyDesc?replace('\r\n', '<br/>')}</#if></label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>调整原因</strong></label>
                    <div class="controls">
                        <label class="control-label" id="schoolLevel" style="text-align: left;">${applyData.apply.comment!}</label>
                    </div>
                </div>

                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>处理意见区</strong></label>
                    <div class="controls">
                        <table class="table table-striped table-bordered" style="font-size: 14px;">
                            <thead>
                            <tr>
                                <th>审核日期</th>
                                <th>审核人</th>
                                <th>处理结果</th>
                                <th>处理意见</th>
                            </tr>
                            </thead>
                            <#if applyData.processResultList?has_content>
                                <#list applyData.processResultList as processResult>
                                    <tr>
                                        <td><#if processResult.processDate?has_content>${processResult.processDate?string("yyyy-MM-dd")}</#if></td>
                                        <td>${processResult.accountName!}</td>
                                        <td>${processResult.result!}</td>
                                        <td>${processResult.processNotes!}</td>
                                    </tr>
                                </#list>
                            </#if>
                        </table>
                    </div>
                </div>
            </div>
        </#if>
    </div>

</div>


<script type="text/javascript">

</script>
</@layout_default.page>