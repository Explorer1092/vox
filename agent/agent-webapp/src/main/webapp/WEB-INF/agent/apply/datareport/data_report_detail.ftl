<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='大数据报告申请详情' page_num=3>
<script src="/public/rebuildRes/js/common/category.js"></script>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 大数据报告申请详情</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content">
            <div class="form-horizontal">
                <#if applyData?has_content && applyData.apply?has_content>
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label">学科</label>
                            <div class="controls">
                                <label class="control-label" id="modifyType" style="text-align: left;margin-left:90px;width:250px"><#if applyData.apply.subject == 1>小学英语<#elseif applyData.apply.subject == 2>小学数学</#if></label>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">区域/学校</label>
                            <div class="controls">
                                <label class="control-label" id="schoolIdLabel" style="text-align: left;margin-left:90px;width:250px">
                                    <#if applyData.apply.reportLevel == 1>${applyData.apply.cityName!}
                                    <#elseif applyData.apply.reportLevel == 2>${applyData.apply.cityName!}/${applyData.apply.countyName!}
                                    <#elseif applyData.apply.reportLevel == 3>${applyData.apply.schoolName!}（${applyData.apply.schoolId!}）
                                    </#if>
                                </label>
                            </div>
                        </div>
                        <#if applyData.apply.engStartGrade??>
                            <div class="control-group">
                                <label class="control-label">英语起始年级</label>
                                <div class="controls">
                                    <label class="control-label" id="modifyType" style="text-align: left;margin-left:90px;width:250px"><#if applyData.apply.engStartGrade == 1>小学一年级<#elseif applyData.apply.engStartGrade == 3>小学三年级</#if></label>
                                </div>
                            </div>
                        </#if>
                        <div class="control-group">
                            <label class="control-label">时间维度</label>
                            <div class="controls">
                                <label class="control-label" id="schoolName" style="text-align: left;margin-left:90px;width:250px">
                                    <#if applyData.apply.reportType == 1>学期报告
                                    <#elseif applyData.apply.reportType == 2>月度报告
                                    </#if>
                                </label>
                            </div>
                        </div>

                        <#if applyData.apply.reportType == 1>
                            <div class="control-group">
                                <label class="control-label">学期</label>
                                <div class="controls">
                                    <label class="control-label" id="regionName" style="text-align: left;margin-left:90px;width:250px">
                                        <#if applyData.apply.reportTerm == 1>2016年9-12月
                                        <#elseif applyData.apply.reportTerm == 2>2017年1-6月
                                        <#elseif applyData.apply.reportTerm == 3>2017年7-12月
                                        <#elseif applyData.apply.reportTerm == 4>2018年1-6月
                                        </#if>
                                    </label>
                                </div>
                            </div>
                        <#elseif applyData.apply.reportType == 2>
                            <div class="control-group">
                                <label class="control-label">月份</label>
                                <div class="controls">
                                    <label class="control-label" id="regionName" style="text-align: left;margin-left:90px;width:250px">${applyData.apply.reportMonth!}</label>
                                </div>
                            </div>
                        </#if>
                        <div class="control-group">
                            <label class="control-label">样本校</label>
                            <div class="controls">
                                <label class="control-label" id="schoolLevel" style="text-align: left;margin-left:90px;width:250px">${applyData.apply.sampleSchoolId!}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${applyData.apply.sampleSchoolName!}</label>
                            </div>
                        </div>
                        <#if applyData.apply.firstDocument?? &&  ((applyData.apply.firstDocument)!'')!=''>
                            <div class="control-group">
                                <label class="control-label">报告下载</label>
                                <div class="controls">
                                    <a href ="${applyData.apply.firstDocument!''}" download>${applyData.apply.firstDocumentName()}</a>
                                    <#if (applyData.apply.secondDocument??) && ((applyData.apply.secondDocument)!'')!=''>
                                        <a href ="${applyData.apply.secondDocument!''}" download>${applyData.apply.secondDocumentName()}</a>
                                    </#if>
                                </div>
                            </div>
                        </#if>
                        <div class="control-group">
                            <label class="control-label" style="text-align: left;margin-left:90px;width:250px">
                                申请人历史申请记录：共计<#if historyApplies?has_content>${historyApplies?size!0}条<#else>0条</#if>
                            </label>
                        </div>
                        <div class="dataTables_wrapper" role="grid">
                            <table class="table table-striped table-bordered bootstrap-datatable" id="historyApplyTable" style="width: 1000px;margin-left: 50px;">
                                <thead>
                                <tr>
                                    <th class="sorting" style="width: 60px;">申请日期</th>
                                    <th class="sorting" style="width: 60px;">申请学科</th>
                                    <th class="sorting" style="width: 60px;">区域/学校</th>
                                    <th class="sorting" style="width: 60px;">时间维度</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#if historyApplies?has_content>
                                        <#list historyApplies as item>
                                        <tr>
                                            <td>${item.createDatetime?string("yyyy-MM-dd")}</td>
                                            <td><#if item.subject == 1>小学英语<#elseif item.subject ==2>小学数学</#if></td>
                                            <td>
                                                <#if item.reportLevel == 1>${item.cityName!}/${item.countyName!}
                                                <#elseif item.reportLevel == 2>${item.countyName!}
                                                <#elseif item.reportLevel == 3>${item.schoolName!}（${item.schoolId!}）
                                                </#if>
                                            </td>

                                            <td>
                                                <#if item.reportType == 1>学期报告
                                                    <#if item.reportTerm == 1>2016年9-12月
                                                    <#elseif item.reportTerm == 2>2017年1-6月
                                                    <#elseif item.reportTerm == 3>2017年7-12月
                                                    <#elseif item.reportTerm == 4>2018年1-6月
                                                    </#if>
                                                <#elseif item.reportType == 2>月度报告${item.reportMonth}
                                                </#if>
                                            </td>
                                        </tr>
                                        </#list>
                                    </#if>
                                </tbody>
                            </table>
                        </div>
                        <div class="control-group">
                            <label class="control-label">申请原因</label>
                            <div class="controls">
                                <label class="control-label" id="schoolLevel" style="text-align: left;margin-left:90px;width:150px">${applyData.apply.comment!}</label>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="control-label">处理意见</label>
                        </div>

                        <div class="dataTables_wrapper" role="grid">
                            <table class="table table-striped table-bordered bootstrap-datatable" id="historyApplyTable" style="width: 1000px;margin-left: 50px;">
                                <thead>
                                <tr>
                                    <th class="sorting" style="width: 60px;">审核日期</th>
                                    <th class="sorting" style="width: 60px;">审核人</th>
                                    <th class="sorting" style="width: 60px;">处理结果</th>
                                    <th class="sorting" style="width: 60px;">处理意见</th>
                                    <th class="sorting" style="width: 60px;">备注</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#if applyData.processResultList?has_content>
                                        <#list applyData.processResultList as processResult>
                                        <tr>
                                            <td><#if processResult.processDate?has_content>${processResult.processDate}</#if></td>
                                            <td>${processResult.accountName!}</td>
                                            <td>${processResult.result!}</td>
                                            <td>${processResult.processNotes!}</td>
                                            <td></td>
                                        </tr>
                                        </#list>
                                    </#if>
                                </tbody>
                            </table>
                        </div>
                    </fieldset>
                </#if>
            </div>
        </div>
    </div>
</div>
</@layout_default.page>