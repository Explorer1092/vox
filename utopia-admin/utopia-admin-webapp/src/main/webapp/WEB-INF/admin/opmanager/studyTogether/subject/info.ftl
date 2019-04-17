<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        主题详情
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="chapterForm" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">主题ID</label>
                            <div class="controls">
                                <input type="text" id="subjectId" name="subjectId" class="form-control" value="${content.id!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">系列ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="seriesId" name="seriesId" class="form-control js-postData" value="${content.seriesId!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">主题名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="name" name="name" class="form-control js-postData" value="${content.name!''}" style="width: 336px" disabled/>
                                <span id="spuName"></span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">主题顺序 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="seq" name="seq" placeholder="整数填写" class="form-control js-postData" value="${content.seq!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">配置环境 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="envLevel" name="envLevel" style="width: 350px;" class="js-postData" disabled>
                                    <option value="">--请选择配置环境--</option>
                                <#if levels??>
                                    <#list levels as lels>
                                        <option <#if content?? && content.envLevel??><#if content.envLevel == lels> selected="selected"</#if></#if> value = ${lels!}>
                                            <#if lels?? && lels == 10>单元测试环境
                                            <#elseif lels?? && lels == 20>开发环境
                                            <#elseif lels?? && lels == 30>测试环境
                                            <#elseif lels?? && lels == 40>预发布环境
                                            <#elseif lels?? && lels == 50>生产环境
                                            </#if>
                                        </option>
                                    </#list>
                                </#if>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">备注说明</label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text" value="<#if content??>${content.remark!''}</#if>" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者</label>
                            <div class="controls">
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="${content.createUser!''}" style="width: 336px;" readonly/>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
</@layout_default.page>

