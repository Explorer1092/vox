<#--动手做一做-->
<script id="t:PHOTO_OBJECTIVE" type="text/html">
<div class="w-base" style="border: 0;">
    <div class="usePrompt">
        <div class="title">
            <div class="close">
                <!--ko if:isShowExplain()-->
                <a href="javascript:void(0);" class="btn-close" data-bind="click: $root.showExplain">我知道怎么用，关闭提示</a>
                <!--/ko-->
                <!--ko ifnot:isShowExplain()-->
                <a href="javascript:void(0);" class="btn-close" data-bind="click: $root.showExplain">教学生怎么用</a>
                <!--/ko-->
            </div>
            <p class="info">使用提示</p>
        </div>
        <div class="w-clear"></div>
        <ul data-bind="css:{'hidden': !isShowExplain()}" style="overflow: hidden;">
            <li>
                <div class="step">
                    <i>step.1</i>
                    <p>选入1个或多个配置好的题目</p>
                </div>
                <div class="arrow"></div>
            </li>
            <li>
                <div class="step">
                    <i>step.2</i>
                    <p>学生收到练习后，拍摄照片或录制声音来提交练习</p>
                </div>
                <div class="arrow"></div>
            </li>
            <li>
                <div class="step">
                    <i>step.3</i>
                    <p>老师在检查作业时可看到学生上传的练习照片或练习录音，并可以在课堂上进行展示</p>
                </div>
            </li>
        </ul>
    </div>
    <!--ko if:content().length > 0-->
    <!--ko foreach:content-->
    <div class="w-base examTopicBox" style="margin:15px;">
        <div class="w-base-title" data-bind="style: { display: assignTimes() <= 0 ? 'none' : 'block' }">
            <h3><span class="sub" style="border:none;" data-bind="text: '共被使用'+assignTimes()+'次'"></span></h3>
        </div>
        <div class="homeworkBox">
            <div data-bind="attr: { id: 'subjective_'+$index()}"></div>
            <div class="h-set-homework" style="border:none;padding: 15px 0 5px 0;margin:10px 0 0 0;">
                <div class="testPaper-info">
                    <div class="btnGroup">
                        <!--ko ifnot:isSelected()-->
                        <a href="javascript:void(0)" class="btn" data-bind="click: $parent.addSubjective"><i class="h-set-icon h-set-icon-add"></i>选入</a>
                        <!--/ko-->
                        <!--ko if:isSelected()-->
                        <a href="javascript:void(0)" class="btn cancel" data-bind="click: $parent.addSubjective"><i class="h-set-icon h-set-icon-cancel"></i>移除</a>
                        <!--/ko-->
                    </div>
                </div>
            </div>
        </div>
    </div>
    <!--/ko-->
    <!--/ko-->
    <!--ko if:content().length == 0-->
    <div class="J_dataLoading" style="text-align: center;height: 30px;line-height: 30px;font-size: 18px;margin-top: 10px;">数据加载中...</div>
    <!--/ko-->
</div>
</script>



