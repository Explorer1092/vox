<ul>
    <!-- ko foreach: data -->
    <!-- ko if: $data.length > 0 -->
    <li data-bind="text: $index() + 1 + '年级', css: { 'active': $parent.focusLevel() == $index() + 1 }, click: $parent.changeLevel.bind($data, $index() + 1, $parent)"></li>
    <!-- /ko -->
    <!-- /ko -->

    <!-- ko if: groupSupported() -->
    <!-- ko foreach: data -->
    <!-- ko if: $data.length > 0 -->
    <li data-bind="visible: $parent.focusLevel() == $index() + 1" class="v-parent pull-down">
        <!-- ko if: $parent.groupData()[$index()].length > 0 -->
        <div class="v-switch-arrange" style="clear: both;padding: 10px;text-align: left;">
            <label style="cursor: pointer;"
                   data-bind="css: { 'w-radio-current': $parent.showType[$index()]() == 'clazz' }, click: $parent.changeShowType.bind($data, 'clazz', $parent)"><span
                    class="w-radio"></span> <span class="w-icon-md">按班级布置</span></label>
            <label style="cursor: pointer;"
                   data-bind="css: { 'w-radio-current': $parent.showType[$index()]() == 'group' }, click: $parent.changeShowType.bind($data, 'group', $parent)"><span
                    class="w-radio"></span> <span class="w-icon-md">按分组布置</span></label>
        </div>
        <!-- /ko -->

        <!-- ko if: $parent.showType[$index()]() == 'clazz' || ($parent.showType[$index()]() == 'group' && $parent.groupData()[$index()].length == 0) -->
        <p class="v-alltarget" data-bind="click: $parent.changeAllStatus.bind($data, $parent)">
            <span class="w-checkbox" data-bind="css: { 'w-checkbox-current': $parent.selectAll[$index()] }"></span>
            <span class="w-icon-md">全部</span>
        </p>
        <!-- ko foreach: $data -->
        <p class="v-targets" data-bind="attr: { title: className }, click: $root.changeStatus.bind($data, $root)">
            <span class="w-checkbox" data-bind="css: { 'w-checkbox-current': isChecked }"></span>
            <span class="w-icon-md" data-bind="text: className"></span>
        </p>
        <!-- /ko -->
        <!-- /ko -->

        <!-- ko if: $parent.showType[$index()]() == 'group' && $parent.groupData()[$index()].length > 0 -->
        <p class="v-alltarget" data-bind="click: $parent.changeAllStatus.bind($data, $parent)">
            <span class="w-checkbox" data-bind="css: { 'w-checkbox-current': $parent.groupSelectAll[$index()] }"></span>
            <span class="w-icon-md">全部</span>
        </p>
        <!-- ko foreach: $parent.groupData()[$index()] -->
        <p class="v-targets" data-bind="attr: { title: groupName }, click: $root.changeStatus.bind($data, $root)">
            <span class="w-checkbox" data-bind="css: { 'w-checkbox-current': isChecked }"></span>
            <span class="w-icon-md" data-bind="text: groupName"></span>
        </p>
        <!-- /ko -->
        <!-- /ko -->
    </li>
    <!-- /ko -->
    <!-- /ko -->
    <!-- /ko -->

    <!-- ko ifnot: groupSupported()-->
    <!-- ko foreach: data -->
    <!-- ko if: $data.length > 0 -->
    <li data-bind="visible: $parent.focusLevel() == $index() + 1" class="v-parent pull-down">
        <p class="v-alltarget" data-bind="click: $parent.changeAllStatus.bind($data, $parent)">
            <span class="w-checkbox" data-bind="css: { 'w-checkbox-current': $parent.selectAll[$index()] }"></span>
            <span class="w-icon-md">全部</span>
        </p>
        <!-- ko foreach: $data -->
        <p class="v-targets" data-bind="attr: { title: className }, click: $root.changeStatus.bind($data, $root)">
            <span class="w-checkbox" data-bind="css: { 'w-checkbox-current': isChecked }"></span>
            <span class="w-icon-md" data-bind="text: className"></span>
        </p>
        <!-- /ko -->
    </li>
    <!-- /ko -->
    <!-- /ko -->
    <!-- /ko -->
</ul>