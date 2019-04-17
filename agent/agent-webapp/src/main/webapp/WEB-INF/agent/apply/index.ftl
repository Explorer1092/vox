<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的账户' page_num=3>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 选择申请类型</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content">
            <@apptag.pageElement elementCode="b59013d8b4134595">
                <div class="control-group span1">
                    <div class="controls">
                        <a href="dictschool_apply.vpage" class="btn btn-success">字典表调整</a>
                    </div>
                </div>
            </@apptag.pageElement>
            <@apptag.pageElement elementCode="5a81282a47454b59">
                <div class="control-group span1" >
                    <div class="controls">
                        <a href="unified_exam_apply_view.vpage" class="btn btn-success">统考申请</a>
                    </div>
                </div>
            </@apptag.pageElement>
            <@apptag.pageElement elementCode="4c167a57f4ec498e">
                <div class="control-group span2" >
                    <div class="controls">
                        <a href="/apply/data_report/data_report.vpage" class="btn btn-success">大数据报告申请</a>
                    </div>
                </div>
            </@apptag.pageElement>
            <#--<div class="control-group span3">-->
                <#--<div class="controls">-->
                    <#--<a href="#" class="btn btn-success">物料</a>-->
                <#--</div>-->
            <#--</div>-->
            <#--<div class="control-group span3">-->
                <#--<div class="controls">-->
                    <#--<a href="#" class="btn btn-success">统考</a>-->
                <#--</div>-->
            <#--</div>-->
        </div>
    </div>
</div>

</@layout_default.page>
