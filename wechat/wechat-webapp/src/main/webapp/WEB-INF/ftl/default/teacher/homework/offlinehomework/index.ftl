<#import "../../layout_view.ftl" as activityMain>
<@activityMain.page title="添加作业单" pageJs="offlineHomeworkIndex">
    <@sugar.capsule css=["offlinehomework"] />
<div class="jc-tips assignOffline" style="display: none;" data-bind="visible:!$root.webLoading() && $root.from() == 'SET_HOMEWORK'">
    <p class="blue"><i class="icon"></i><span>在线作业布置成功！</span></p>
    <p>您可以继续添加线下作业，生成作业单发给家长</p>
</div>
<div class="jc-listMain assignOffline" style="display: none;" data-bind="visible:!$root.webLoading()">
    <div class="jc-list title">今日作业单</div>
    <ul>
        <li class="jc-list disabled" style="display:none;" data-bind="visible:$root.from() == 'SET_HOMEWORK'"><!--disabled选中状态-->
            <div class="info">
                <i class="icon icon-l"></i><p class="name">一起作业在线作业<span class="txtGrey">（您刚刚已布置）</span></p>
            </div>
        </li>

        <!--ko foreach:{data:contentTypes,as:'contentType'}-->
        <li class="jc-list" data-bind="css:{'active':contentType.checked()},click:$root.typeChecked.bind($data,$element,$root)"><!--active当前状态-->
            <div class="info">
                <i class="icon icon-l"></i><p class="name" data-bind="text:contentType.typeName()">阅读课文</p>
            </div>
        </li>
        <!--ko template:{name:$root.displayMode,data:contentType}--><!--/ko-->
        <!--/ko-->
    </ul>
    <div class="jc-listTime">
        <p class="left">
            <span class="label">截止时间：</span>
            <input class="time" readonly="readonly" name="endDateTime" data-bind="textInput:$root.endDateTime">
        </p>
        <p class="right">
            <span class="label">家长签字</span>
            <span class="check checked" data-bind="css:{'checked' : $root.parentSign()},click:$root.parentSignClick"></span>
        </p>
    </div>
</div>
<div class="jc-footer assignOffline" style="display: none;" data-bind="visible:!$root.webLoading()">
    <div class="innerBox">
        <div class="inner inner-2">
            <a href="javascript:void(0)" data-bind="click:$root.cancelClick.bind($data)" class="w-btn w-btn-lightBlue">跳过</a>
            <a href="javascript:void(0)" data-bind="css:{disabled : $root.confirmBtnDisabled()},click:$root.confirmClick.bind($data)" class="w-btn">确认</a>
        </div>
    </div>
</div>

<div class="jc-flayer" id="unitsPopUp" style="display: none;" data-bind="visible:$root.unitsShow()">
    <div class="flayerBox">
        <div class="flayerHeader">
            <h2 class="name">编辑选项</h2>
            <span class="close" data-bind="click:$root.closeClick">×</span>
        </div>
        <div class="jc-tips-yellow" style="display: none" data-bind="visible:$root.focusType() == 'LISTEN'">温馨提示：家长和孩子可使用“点读机”功能完成此项作业</div>
        <div class="jc-operation" style="display: none;" data-bind="visible:$root.focusType() != 'DICTATION'">
            <span class="name">遍数</span>
            <div class="operation">
                <div class="label-op" data-bind="click:$root.readingClick.bind($root,-1)"><span class="label reduce"></span></div>
                <span class="label" data-bind="text:$root.displayCount">3</span>
                <div class="label-op" data-bind="click:$root.readingClick.bind($root,1)"><span class="label add"></span></div>
            </div>
        </div>
        <ul class="jc-unit">
            <!--ko foreach:{data:$root.unitList,as:'unit'}-->
            <li data-bind="css:{'active':unit.unitId == $root.focusUnitId()},text:unit.unitName,click:$root.unitClick.bind($data,$root,'MANUAL_CLICK')">&nbsp;</li>
            <!--/ko-->
        </ul>
        <div class="jc-footer">
            <div class="innerBox">
                <div class="inner">
                    <a href="javascript:void(0)" class="w-btn" data-bind="click:$root.submitClick.bind($root)">确认</a>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/html" id="t:READ">
    <li class="jc-list unit" data-bind="attr:{'data-type':type()},click:$root.typeClick.bind($data,$root)">
        <div class="info">
            <p class="name">朗读<!--ko text:count()--><!--/ko-->遍&nbsp;&nbsp;  <!--ko text:contentName()--><!--/ko--></p><i class="icon icon-r"></i>
        </div>
    </li>
</script>
<script type="text/html" id="t:LISTEN">
    <li class="jc-list unit" data-bind="attr:{'data-type':type()},click:$root.typeClick.bind($data,$root)">
        <div class="info">
            <p class="name">听<!--ko text:contentType.count()--><!--/ko-->遍&nbsp;&nbsp;  <!--ko text:contentType.contentName()--><!--/ko--></p><i class="icon icon-r"></i>
        </div>
    </li>
</script>
<script type="text/html" id="t:DICTATION">
    <li class="jc-list unit" data-bind="attr:{'data-type':type()},click:$root.typeClick.bind($data,$root)">
        <div class="info">
            <p class="name"><!--ko text:contentType.contentName()--><!--/ko--></p><i class="icon icon-r"></i>
        </div>
    </li>
</script>
<script type="text/html" id="t:CUSTOMIZE">
    <li class="jc-list unit" data-bind="attr:{'data-type':type()},click:$root.typeClick.bind($data,$root)">
        <div class="info">
            <p class="name"><!--ko text:contentType.contentName()--><!--/ko--></p><i class="icon icon-r"></i>
        </div>
    </li>
</script>
</@activityMain.page>