<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='托比同步课堂' page_num=9>

<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>

<div class="span9">
    <legend>
        <strong><span class="text-info">托比同步课堂</span>&nbsp;/&nbsp;<span class="text-success">错误</span></strong>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form id="courseForm" class="well form-horizontal" method="post"
                  action="/opmanager/tobbit/balloonSave.vpage">
                <input type="hidden" name="id" value="${id!''}"/>
                <input type="hidden" name="subject" value="${subject!''}"/>
                <fieldset>
                    <legend>参数错误，请关闭窗口</legend>

                </fieldset>
            </form>
        </div>
    </div>
</div>
</@layout_default.page>