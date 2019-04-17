<#-- @ftlvariable name="productTypeList" type="java.util.List" -->
<#-- @ftlvariable name="exRegionList" type="java.util.List<com.voxlearning.utopia.service.region.api.entities.extension.ExRegion>" -->
<#-- @ftlvariable name="conditionMap" type="java.util.Map" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div class="span9">

    <ul class="breadcrumb">
        <li><a href="addglobaltags.vpage">增加Global配置</a><span class="divider">|</span></li>
        <li><span>查询Global配置</span><span class="divider">|</span></li>
    </ul>

    <fieldset>
        <legend>查询Global配置</legend>
    </fieldset>
    <form action="globaltaglist.vpage" method="post">
        <ul class="inline">
            <li>
                <label>选择Tag名称：
                    <select id="tagName" name="tagName">
                        <#if tagNames?has_content>
                            <#list tagNames as tName>
                                <option <#if tagName?? && tagName == tName>selected="selected"</#if> value="${tName!}">${tName!}</option>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>
            <li>
                <button class="btn btn-primary" id="submit" type="submit">查询</button>
                <a id="dataexport" href="javascript:void(0);" role="button" class="btn btn-inverse">导出全部</a>
            </li>
        </ul>
    </form>

    <table class="table table-hover table-striped table-bordered">
        <#if tagsList?has_content>
            <tr>
                <th style="width: 180px;">tagName</th>
                <th style="width: 550px;">tagValue</th>
                <th>备注</th>
                <th>创建时间</th>
                <th>操作</th>
            </tr>
            <#list tagsList as tags>
                <tr>
                    <td>${tags.tagName!''}</td>
                    <td>${tags.tagValue!''}</td>
                    <td>${tags.tagComment!''}</td>
                    <td>${tags.createDatetime?string("yyyy-MM-dd HH:mm:ss")}</td>
                    <td><a id="delete_tags_${tags.id!}" data-id="${tags.id!}" href="javascript:void(0)">删除</a></td>
                </tr>
            </#list>
        </#if>
    </table>
</div>
<script>
    $(function () {

        $('a[id^="delete_tags_"]').click(function () {
            if (!window.confirm("是否确认删除?")) {
                return;
            }
            var $this = $(this);
            var postData = {
                tagId: $this.data('id')
            };
            $.post("deleteglobaltag.vpage", postData, function (data) {
                if (data.success) {
                    alert("删除成功");
                    location.reload();
                }
            });
        });

        $('#dataexport').click(function(){
            var tagName = $('#tagName').val();
            location.href = "downloadtaginfo.vpage?tagName=" + tagName;
        });
    });
</script>
</@layout_default.page>