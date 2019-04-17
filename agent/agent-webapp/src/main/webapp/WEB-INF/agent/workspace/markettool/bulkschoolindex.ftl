<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='批量创建学校' page_num=9>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-edit"></i> 批量创建学校</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <#include '../../widget_alert_messages.ftl'/>
            <form method="post" action="/workspace/markettool/bulkschoolconfirm.vpage" enctype="multipart/form-data">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">选择EXCEL数据文件，格式如下图所示。<br/>
                        注意事项:<br/>
                        &nbsp;&nbsp;1. EXCEL文件需要为97－2003版本格式(后缀名为.xls)<br/>
                        &nbsp;&nbsp;2. 需要保留第一行表头<br/>
                        &nbsp;&nbsp;3. 学校级别必须为小学/中学。<br/>
                        &nbsp;&nbsp;4. 学校类型必须是公立制学校。<br/>
                        &nbsp;&nbsp;5. 鉴定状态必须是已鉴定/等待鉴定。<br/>
                        &nbsp;&nbsp;6. 鉴定来源必须是市场/AB类学校。<br/>
                        <img src="${requestContext.webAppContextPath}/public/img/bulkschool.png"/><br/>
                    </label>
                    <br>
                    <div class="controls">
                        <input type="file" name="sourceExcelFile">
                    </div>
                </div>

                <ul class="inline">
                    <li>
                        <input class="btn" type="submit" value="提交" />
                    </li>
                </ul>
            </form>
        </div>
    </div>
</div>

</@layout_default.page>