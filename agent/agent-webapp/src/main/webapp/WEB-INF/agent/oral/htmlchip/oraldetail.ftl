<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">&times;</button>
            <h4 class="modal-title">详情</h4>
        </div>
        <form class="form-horizontal">
            <fieldset>
                <#if oralMapper?has_content>
                <div class="control-group">
                    <label class="control-label">测试类型</label>
                    <div class="controls">
                        <label class="radio">
                        ${(oralMapper.rangeTypeName)!}测评
                        </label>
                    </div>
                </div>
                <div class="control-group">
                    <label for="date01" class="control-label">测试开始时间</label>
                    <div class="controls">
                    ${(oralMapper.beginDate)!}
                    </div>
                </div>
                <div class="control-group">
                    <label for="date02" class="control-label">测试结束时间</label>
                    <div class="controls">
                    ${(oralMapper.endDate)!}
                    </div>
                </div>
                <div class="control-group">
                    <label for="date02" class="control-label">教师批改截止时间</label>
                    <div class="controls">
                    ${(oralMapper.correctStopDate)!}
                    </div>
                </div>
                <div class="control-group">
                    <label for="date02" class="control-label">成绩发布时间</label>
                    <div class="controls">
                    ${(oralMapper.resultIssueDate)!}
                    </div>
                </div>
                <div class="control-group">
                    <label for="fileInput" class="control-label">文件名称</label>
                    <div class="controls">
                       ${(oralMapper.fileName)!}
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="selectError1">开放地区</label>
                    <#if oralMapper.regions?has_content>
                        <#list oralMapper.regions as region>
                            <div class="controls" style="width: 280px;">
                                <span>${region}</span>
                            </div>
                        </#list>
                    </#if>
                    <#if oralMapper.schoolIds?has_content>
                        <#list oralMapper.schoolIds as school>
                            <div class="controls" style="width: 280px;">
                                <span>${school}</span>
                            </div>
                        </#list>
                    </#if>
                </div>
                <#else>
                    没有查到相关数据，请刷新列表重新查看
                </#if>
            </fieldset>
        </form>
    </div>
</div>