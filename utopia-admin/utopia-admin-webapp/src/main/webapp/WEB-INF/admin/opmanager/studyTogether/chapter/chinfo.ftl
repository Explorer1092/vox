<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="课节详情" page_num=9 jqueryVersion ="1.7.2">
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>

<div id="main_container" class="span9">

    <legend style="font-weight: 700;">
        章节详情
        <a type="button" id="btn_cancel" href="chindex.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="clazzFestivalInfoForm" name="info_form" enctype="multipart/form-data" action="" method="post">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">章节ID</label>
                            <div class="controls">
                                <input type="text" id="chapterId" name="chapterId" class="form-control" value="${content.id!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="skuId" name="skuId" class="form-control js-postData" type="text" value="<#if content??>${content.skuId!''}</#if>" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">章节名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="chapterName" name="chapterName" class="form-control js-postData" type="text" value="<#if content??>${content.chapterName!''}</#if>" style="width: 336px;"disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">章节描述 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="chapterDesc" name="chapterDesc" class="form-control js-postData" type="text" value="<#if content??>${content.chapterDesc!''}</#if>" style="width: 336px;" maxlength="30" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">展示顺序 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="seq" name="seq" class="form-control js-postData" type="text" value="<#if content??>${content.seq!''}</#if>" style="width: 336px;" maxlength="30" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">开课日期 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="openDate" name="openDate" class="form-control js-postData" value="<#if content??>${content.openDate!''}</#if>" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">单周奖励ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="singleRewardIds" name="singleRewardIds" class="form-control js-postData"
                                       value="<#if content?? && content.singleRewardIds?? && content.singleRewardIds?size gt 0><#assign index = 0><#list content.singleRewardIds as num><#if index != 0>,</#if>${num}<#assign index = index + 1></#list></#if>"
                                       style="width: 336px;"disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">双周奖励ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="doubleRewardIds" name="doubleRewardIds" class="form-control js-postData"
                                       value="<#if content?? && content.doubleRewardIds?? && content.doubleRewardIds?size gt 0><#assign jndex = 0><#list content.doubleRewardIds as num><#if jndex != 0>,</#if>${num}<#assign jndex = jndex + 1></#list></#if>"
                                       style="width: 336px;"disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">配置环境 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="envLevel" name="envLevel" class="form-control js-postData" type="text"
                                       value="<#if content?? && content.envLevel == 10>单元测试
                                              <#elseif content?? && content.envLevel == 20>开发环境
                                              <#elseif content?? && content.envLevel == 30>测试环境
                                              <#elseif content?? && content.envLevel == 40>预发布环境
                                              <#elseif content?? && content.envLevel == 50>生产环境
                                              </#if>"
                                       style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">备注说明 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text" value="<#if content??>${content.remark!''}</#if>" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="${content.createUser!''}" readonly/>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
</@layout_default.page>

