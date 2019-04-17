<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='统考管理' page_num=8>
<style type="text/css">
    .form-horizontal .control-label-large{
        text-align: left;
        border: 2px solid #cccccc;
    }
    .form-horizontal .control-ext a{
        display: inline-block; padding: 0 17px;
    }
    .form-horizontal .control-sub{
        width: 260px;
        height:50px;
        line-height: 53px;
    }
    .form-horizontal .control-ext{
        width: 460px;
        margin-left: 280px;
        height:50px;
        line-height: 53px;
    }
    fieldset{
        padding-left : 120px;
    }
</style>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-user"></i> 统考流程一览</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <form class="form-horizontal" method="POST">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label control-sub control-label-large" for="focusedInput">1、【市场人员】沟通、演示统考测评</label>
                        <div class="controls control-sub control-ext">
                            <a href="//cdn.17zuoye.com/static/project/marketing/oral_introduce.ppt" target="_blank">口语测试介绍PPT</a>
                            <a href="//cdn.17zuoye.com/static/project/marketing/speech_engine.doc" target="_blank">语音打分说明</a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label control-sub control-label-large" for="focusedInput">2、【教研员】出试卷、提供word文档</label>
                        <div class="controls control-sub control-ext">
                            <a href="//cdn.17zuoye.com/static/project/marketing/oral_demo.doc" target="_blank">口语测试样题模板</a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label control-sub control-label-large" for="focusedInput">3、【市场人员】登记、上传试卷平台</label>
                        <div class="controls control-sub control-ext">

                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label control-sub control-label-large" for="focusedInput">4、【内容管理员】试卷录入</label>
                        <div class="controls control-sub control-ext">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label control-sub control-label-large" for="focusedInput">5、【学校教师】布置、检查测试</label>
                        <div class="controls control-sub control-ext">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label control-sub control-label-large" for="focusedInput">6、【教研员】查看数据报告</label>
                        <div class="controls control-sub control-ext">
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>

</div>


<script type="text/javascript">
    var isBlank = function(str){
        return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == ''
    };
</script>
</@layout_default.page>
