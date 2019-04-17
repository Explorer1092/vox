<#import "../layout.ftl" as homeworkIndex>
<@homeworkIndex.page title="布置作业" pageJs="homeworkIndex">
    <@sugar.capsule css=['homework'] />
    <div class="mhw-selected" data-bind="visible: !$data.unitsDetail() > 0">
        <div class="ms-box">
            <div class="title" id="loadingHomeworkIndexBox" style="text-align: center">数据努力加载中...</div>
        </div>
    </div>

    <div class="mhw-home"> 
        <div id="clazzBox" data-bind="visible:$data.unitsDetail().length > 0" style="display: none;">
            <div class="mh-header">
                <ul class="list">
                    <li data-bind="click: $data.changeEventClick.bind($data,'subject'),visible: $data.subjectList().length > 1">
                        <div class="left"><i class="icon icon-subject"></i><span>学科</span></div>
                        <div class="text txt-overflow" data-bind="text: $root.getSubjectValue($root.subject())"></div>
                    </li>
                    <li data-bind="click: $data.changeEventClick.bind($data,'clazz')">
                        <div class="left"><i class="icon icon-class"></i><span>班级</span></div>
                        <div class="text txt-overflow">
                            [<span data-bind="text: $data.showLevel()+'年级'"></span>]
                            <!-- ko foreach: {data : $data.showClazzList(), as : '_clazz'} -->
                            <!--ko if:_clazz.checked -->
                            <span data-bind="text: _clazz.clazzName"></span>
                            <!--/ko-->
                            <!-- /ko -->
                        </div>
                    </li>
                    <li data-bind="click: $data.changeEventClick.bind($data,'book')">
                        <div class="left"><i class="icon icon-material"></i><span>教材</span></div>
                        <div class="text txt-overflow" data-bind="text: $data.defaultBookDetail().name || '--'"></div>
                    </li>
                    <li data-bind="click: $data.changeEventClick.bind($data,'unit')">
                        <div class="left"><i class="icon icon-unit"></i><span>单元</span></div>
                        <div class="text txt-overflow">
                            <!--ko if: $root.hasUnitModules-->
                                <!--ko foreach : {data: $data.showUnitDetail(),as : '_modules'} -->
                                    <!-- ko foreach : {data : _modules.units, as : '_mu'} -->
                                        <!--ko if:_mu.isDefault -->
                                            <span data-bind="text: _mu.name || '--'"></span>
                                        <!--/ko-->
                                    <!--/ko-->
                                <!--/ko-->
                            <!--/ko-->

                            <!--ko ifnot: $root.hasUnitModules-->
                                <!--ko foreach : {data: $data.showUnitDetail(),as : '_units'} -->
                                    <!--ko if:_units.isDefault -->
                                        <span data-bind="text: _units.name || '--'"></span>
                                    <!--/ko-->
                                <!--/ko-->
                            <!--/ko-->
                        </div>
                    </li>
                </ul>
            </div>
            <div class="mh-content" data-bind="if: $data.subject() != 'ENGLISH'">
                <h3 class="title">请选择布置作业课时</h3>
                <ul>
                    <!--ko foreach : {data: $data.lessonsDetail(),as : '_lessons'} -->
                    <!--ko foreach : {data: _lessons.children,as : '_sections'} -->
                    <li data-bind="click: $root.sectionClick, css:{'active':_sections.checked}" class="txt-overflow">
                        <i class="w-icon w-icon-check"></i>
                        <span data-bind="text: _sections.name"></span>
                    </li>
                    <!--/ko-->
                    <!--/ko-->
                </ul>
            </div>
            <div class="btn-box">
                <a data-bind="visible: $root.getSelectedSectionIds().length == 0" href="javascript:void(0)" class="w-btn disabled">去布置作业</a>
                <a data-bind="visible: $root.getSelectedSectionIds().length != 0,click: $data.submitBtn" href="javascript:void(0)" style="display: none;" class="w-btn">去布置作业</a>
            </div>
        </div>
        <div id="noClazzBox" style="display: none;" class="mhw-goWarm">
            <div class="mgw-icon"></div>
            <div class="mgw-text">您需要检查班级上一次作业，才<br>可以布置新作业!</div>
            <div class="mgw-btn">
                <a href="/teacher/homework/report/history.vpage">去检查作业</a>
            </div>
        </div>
    </div>

    <#--更换提示框-->
    <div></div>
    <#include "clazzlist.ftl"/>
    <#include "book.ftl"/>
    <#include "units.ftl"/>
    <#include "subjectlist.ftl"/>

    <script type="text/javascript">
        var canBeAssignedSubjects = ${canBeAssignedSubjects![]};
    </script>
</@homeworkIndex.page>

