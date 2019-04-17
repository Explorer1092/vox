<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="equator工具" page_num=24>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">

<style>
    #response-data {display: none}
    #response-data-beauty {color: green; margin: 0; padding: 0; border: none; font-size: smaller;}
</style>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<div id="main_container" class="span9">
    <#include "inner_header.ftl"/>
    <form class="form-horizontal" action="/equator/newwonderland/tools/workbook.vpage" method="post" id="productForm"
          enctype="multipart/form-data">
        <div class="control-group">
            <label class="col-sm-2 control-label">开始解析页</label>
            <div class="controls">
                <input type="text" name="pageNum" id="pageNum"/>
                <span class="controls-desc">从0页开始</span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">开始行</label>
            <div class="controls">
                <input type="text" name="startRowNum" id="startRowNum"/>
                <span class="controls-desc">从0开始</span>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">文件</label>
            <div class="controls">
                <input type="file" name="file" id="file"/>
                <span class="controls-desc"></span>
            </div>
        </div>
        <div class="control-group">
            <div class="controls">
                <button type="submit" id="btn_save" name="btn_save" class="btn btn-primary">解析</button>
            </div>
        </div>
    </form>
    <#if data?exists>
    <p>解析之后的数据</p>
    <hr/>
    <textarea id="response-data">${data!''}</textarea>
    <pre id="response-data-beauty"></pre>
    </#if>
</div>
<script type="text/javascript">
    $(function () {
        const responseData = $('#response-data').val();
        if (responseData) {
            try {
                const obj = JSON.parse(responseData);
                if (typeof obj === 'object' && obj) {
                    $('#response-data-beauty').text(JSON.stringify(JSON.parse(responseData), null, 2));
                }
            } catch (e) {
                // do nothing
            }
        }
    });
</script>
</@layout_default.page>