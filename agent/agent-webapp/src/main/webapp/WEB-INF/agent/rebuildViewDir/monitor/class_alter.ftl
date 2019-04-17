<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='换班任务统计' page_num=10>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i> 换班任务统计</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <form id="query_form"  action="download.vpage" method="post" class="form-horizontal">
                <fieldset>
                    <div class="control-group span3">
                        查询当前日期以前市经理和专员未处理的换班任务：
                    </div>
                    <div class="control-group span3">
                        <div class="controls">
                            <button type="submit" id="search_btn" class="btn btn-success">下载</button>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>
</@layout_default.page>
