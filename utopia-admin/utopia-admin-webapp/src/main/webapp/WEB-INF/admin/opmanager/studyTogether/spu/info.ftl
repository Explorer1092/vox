<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        SPU详情页
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="chapterForm" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <input id="spuId" name="spuId" value="${spuId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">
                        <h4>基本信息</h4><hr style="border:0; background-color: black; height:1px;">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SPU_ID</label>
                            <div class="controls">
                                <input type="text" id="spuId" name="spuId" class="form-control" value="${content.id!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">系列ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="seriesId" name="seriesId" class="form-control js-postData" value="${content.seriesId!''}" style="width: 336px" disabled/>
                                <span id="subjectName"></span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SPU名称 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="name" name="name" class="form-control js-postData" value="${content.name!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SPU副标题(故宫用) <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="subTitle" name="subTitle" class="form-control js-postData" value="${content.subtitle!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SPU类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="type" name="type" style="width: 180px;" class="js-postData" disabled>
                                    <option value="">--请选择SPU类型--</option>
                                    <#if types??>
                                        <#list types as lels>
                                            <option <#if content?? && content.type??><#if content.type == lels> selected="selected"</#if></#if> value = ${lels!}>
                                                <#if lels?? && lels == 0>普通
                                                <#elseif lels?? && lels == 1>线下推广
                                                </#if>
                                            </option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">适合年级 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="min" name="min" placeholder="最小年级，正整数" class="form-control js-postData" value="${min!''}" style="width: 155px;" disabled/>--<input type="number" id="max" name="max" placeholder="最大年级，正整数" class="form-control js-postData" value="${max!''}" style="width: 155px;" disabled/>
                                <span style="color: red">最低年级 <= 最高年级，相等表示只有一个年级，范围[1,6]</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">学习天数 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="days" name="days" class="form-control js-postData" value="${content.days!''}" style="width: 167px;" disabled/>天
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程卡片 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="icon" name="icon" class="form-control js-postData input" value="${content.icon!''}" style="width: 336px" disabled/>
                                <a class="btn btn-success preview" data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.icon!''}"</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程描述 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="desc" name="desc" class="form-control js-postData" type="text" disabled value="<#if content??>${content.desc!''}</#if>" style="width: 336px;"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程头图(故宫用) </label>
                            <div class="controls">
                                <input type="text" id="headIcon" name="headIcon" class="form-control js-postData input" value="${content.headIcon!''}" style="width: 336px" disabled/>
                                <a class="btn btn-success preview" data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.headIcon!''}"</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程大图 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="courseBigPic" name="courseBigPic" class="form-control js-postData input" value="${content.courseBigPic!''}" style="width: 336px" disabled/>
                                <a class="btn btn-success preview" data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.courseBigPic!''}"</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程小图 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="courseLittlePic" name="courseLittlePic" class="form-control js-postData input" value="${content.courseLittlePic!''}" style="width: 336px" disabled/>
                                <a class="btn btn-success preview" data-href="<#if content?? && cdn_host??>${cdn_host!''}${content.courseLittlePic!''}"</#if>">预览</a>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">配置环境 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="envLevel" name="envLevel" style="width: 180px;" class="js-postData" disabled>
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
                    <#-- 是否套课 -->
                        <div class="control-group">
                            <label class="col-sm-2 control-label">是否套课 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="isLessonSet" name="isLessonSet" class="form-control js-postData" disabled>
                                    <option value="false" <#if content?? && content.isLessonSet?? && content.isLessonSet?string("true","false") == 'false'>selected</#if>>否</option>
                                    <option value="true" <#if content?? && content.isLessonSet?? && content.isLessonSet?string("true","false") == 'true'>selected</#if>>是</option>
                                </select>
                            </div>
                        </div>
                        <h4>其他规则设置</h4><hr style="border:0; background-color: black; height:1px;">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程能力细分描述 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input id="tags" name="tags" class="form-control js-postData" placeholder="不同能力之间使用英文逗号隔开,示例: 听,说" type="text" disabled
                                       value="<#assign index = 0><#if content?? && content.tags?? && content.tags?size gt 0><#list content.tags as tag><#if index != 0>,</#if>${tag}<#assign index = index + 1></#list></#if>" style="width: 336px;"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">是否有周复习课 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="hasReview" name="hasReview" style="width: 180px;" disabled class="js-postData">
                                    <option value="">-请选择是否有周复习课-</option>
                                    <#if reviews??>
                                        <#list reviews as lels>
                                            <option <#if content?? && content.hasReview??><#if content.hasReview?string("true","false") == lels> selected="selected"</#if></#if> value = ${lels!}>
                                                <#if lels?? && lels == "true">是
                                                <#elseif lels?? && lels == "false">否
                                                </#if>
                                            </option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">知识点数量 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="number" id="knowledgeCount" name="knowledgeCount" class="form-control js-postData" value="${content.knowledgeCount!''}" style="width: 167px;" disabled/>个
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">体验课ID </label>
                            <div class="controls">
                                <input type="text" id="templateId" name="templateId" class="form-control js-postData" value="${content.templateId!''}" style="width: 167px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">动态匹配设置 <span style="color: red">*</span></label>
                            <div class="controls">
                                <select id="dynamicAdapt" name="dynamicAdapt" style="width: 180px;" class="js-postData" disabled>
                                    <option value="">--请选择动态匹配设置--</option>
                                    <#if reviews??>
                                        <#list reviews as lels>
                                            <option <#if content?? && content.dynamicAdapt??><#if content.dynamicAdapt?string("true","false") == lels> selected="selected"</#if></#if> value = ${lels!}>
                                                <#if lels?? && lels == "true">是
                                                <#elseif lels?? && lels == "false">否
                                                </#if>
                                            </option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">SPU奖励顺序(故宫用) </label>
                            <div class="controls">
                                <input type="number" id="seq" name="seq" class="form-control js-postData" value="${content.seq!''}" style="width: 167px;" disabled/>
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
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="${content.createUser!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        $(document).on("click", "a.preview", function () {
            var link = $(this).attr("data-href");
            if (!link) {
                alert("文件上传中，请稍后预览");
                return;
            }
            window.open(link);
        });
    });
</script>
</@layout_default.page>

