<div class="title_box">
    <ul id="ul_subject">
        <li data-bind="css: {'active' : focusTab() =='english'}">
            <a data-bind="click: changeTab.bind($data,'english')" href="javascript:void (0);">英语</a>
        </li>
        <li data-bind="css: {'active' : focusTab() =='math'}">
            <a data-bind="click: changeTab.bind($data,'math')" data-subject_type="math" href="javascript:void (0);">数学</a>
        </li>
        <li class="clear"></li>
    </ul>
</div>
<div class="clear"></div>