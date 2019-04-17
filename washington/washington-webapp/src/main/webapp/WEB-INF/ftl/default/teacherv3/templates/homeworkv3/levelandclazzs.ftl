<#macro levelandclazzs yqxueteacher="false">
<script id="t:年级和班级" type="text/html">
    <ul data-yqxueteacher="${yqxueteacher}">
    <!--ko if:isMoreLevel-->
    <!--ko foreach: batchclazzs-->
        <!--ko if: $data.length > 0-->
        <li data-bind="click:$root.levelClick.bind($data,$root, $index() + 1), attr:{'data-level' : $index() + 1},css:{'active' : $root.showLevel() == ($index() + 1)},text:($index() + 1) + '年级'" class="v-level"></li>
        <!--/ko-->
    <!--/ko-->
    <!--/ko-->
    <!--ko if: $root.showLevel() > 0-->
    <li class="pull-down">
        <!--ko if:$root.showClazzList().length > 1-->
        <p data-bind="<#if yqxueteacher=='false'>click:$root.chooseOrCancelAll,</#if> css:{'w-checkbox-current' : $root.isAllChecked}">
            <span class="w-checkbox"></span>
            <span class="w-icon-md textWidth">全部</span>
        </p>
        <!--/ko-->
        <!--ko foreach:$root.showClazzList()-->
        <!--ko if:$root.isMoreLevel-->
        <p data-bind="<#if yqxueteacher=='false'>click : $root.singleClazzAddOrCancel.bind($data,$root,$index()),</#if> css:{'w-checkbox-current': checked()}" class="marginL26" style="width:100px;">
            <span class="w-checkbox"></span>
            <span class="w-icon-md" data-bind="attr:{title:className},text:className"></span>
        </p>
        <!--/ko-->
        <!--ko ifnot:$root.isMoreLevel-->
        <p data-bind="<#if yqxueteacher=='false'>click : $root.singleClazzAddOrCancel.bind($data,$root,$index()),</#if> css:{'w-checkbox-current': checked()}" style="margin-left: 24px;*margin-left: 12px; width: 88px;">
            <span class="w-checkbox"></span>
            <span class="w-icon-md" style="width:70px;" data-bind="attr:{title:$root.showLevel() + '年级' + className()},text:$root.showLevel() + '年级' + className()"></span>
        </p>
        <!--/ko-->
        <!--/ko-->
    </li>
    <!--/ko-->
    </ul>
</script>
</#macro>