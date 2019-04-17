<#import "../layout_view.ftl" as activityMain>
<@activityMain.page title="创建/退出班级" pageJs="createClazz">
    <@sugar.capsule css=["clazzManage"] />
    <div id="subjectListBox">
        <div class="subjectList" data-bind="visible: subjectList().length > 1">
            <h2>请选择任教学科</h2>
            <div class="labelBox">
                <!-- ko foreach : {data : subjectList, as : '_subs'} -->
                <span class="label" data-bind="text: _subs.value, css: {'active': _subs.checked},click: $root.selectSubjectBtn">--</span>
                <!--/ko-->
            </div>
        </div>
        <#--<div class="cTitle pad30">
            <span>任教班级数量</span>
            <div class="countOperation">
                <span class="operation" data-bind="click: $root.minusClazzClick, css : {'disabled' : $root.clazzCount() == 0}"><i class="icon icon-reduce"></i></span>
                <span class="countText" data-bind="text: $root.clazzCount()">0</span>
                <span class="operation" data-bind="click: $root.plusClazzClick, css : {'disabled' : $root.clazzCount() == $root.chazzsMaxCount()}"><i class="icon icon-add"></i></span>
            </div>
        </div>-->
        <div class="cMain" data-bind="visible: $data.clazz" style="display: none;">
            <div class="cTitle">选择您所教的班级（可多选）</div>
            <div class="selectGrade">
                <div class="grade-left">
                    <ul>
                        <!-- ko foreach : {data : $data.clazz, as : '_clazz'} -->
                        <!--ko if: _clazz.show()-->
                        <li data-bind="css: {'active' : _clazz.checked}, click: $root.checkClazzBtn">
                            <div>
                                <!--ko text: _clazz.name--><!--/ko-->
                                <p class="count" data-bind="text: '已选'+_clazz.selectedCount()+'个班',visible:selectedCount()>0">--</p>
                            </div>
                        </li>
                        <!--/ko-->
                        <!--/ko-->
                    </ul>
                </div>
                <div class="grade-right">
                    <ul>
                        <!-- ko foreach : {data : $data.clazz, as : '_clazz'} -->
                        <!--ko if: _clazz.checked-->
                        <!-- ko foreach : {data : _clazz.clazzs, as : '_clazzs'} -->
                            <li data-bind="text: _clazzs.name, css:{'active': _clazzs.checked}, click: $root.selectClazzsBtn.bind($data,$parent)">--</li>
                        <!--/ko-->
                        <!--/ko-->
                        <!--/ko-->
                    </ul>
                </div>
            </div>
        </div>
        <div class="cMain" data-bind="visible: $data.clazz" style="display: none;">
            <div class="cTitle">已选班级</div>
            <div class="selectGrade">
                <div class="labelBox">
                    <!-- ko foreach : {data : $data.clazz, as : '_clazz'} -->
                    <!-- ko foreach : {data : _clazz.clazzs, as : '_clazzs'} -->
                        <!--ko if: _clazzs.checked-->
                            <span class="label" data-bind="text: _clazz.name()+_clazzs.name()"></span>
                        <!--/ko-->
                    <!--/ko-->
                    <!--/ko-->
                </div>
            </div>
        </div>
        <div class="cFooter">
            <div class="footerInner">
                <div class="btnBox">
                    <a data-bind="click: saveBtn" href="javascript:void(0)" class="btn">完成</a>
                </div>
            </div>
        </div>
    </div>

    <#--adjusttpl-->
    <script type="text/html" id="adjusttpl">
        <div class="classCreate-dialog">
            <!-- ko foreach : {data : newClazzs, as : '_newClazzs'} -->
            <div data-bind="visible: _newClazzs.show" class="scrollBox">
                <div class="text">
                    <p><!--ko text: _newClazzs.name() --><!--/ko--></p>
                    <p>已有老师和学生，点击申请加入？</p>
                </div>
                <!-- ko foreach : {data : _newClazzs.groups, as : '_groups'} -->
                <div class="info" data-bind="css: {'active': _groups.checked}, click: $root.selectClazzBtn.bind($data,$parent)">
                    <p class="name"><strong>任课老师：</strong>
                        <!-- ko foreach : {data : _groups.teachers, as : '_teachers'} -->
                        <span data-bind="text: _teachers.name"></span>
                        <!--/ko-->
                    </p>
                    <p class="name"><strong>学生名单：</strong>
                        <!-- ko foreach : {data : _groups.students, as : '_students'} -->
                        <span data-bind="text: _students.name"></span>
                        <!--/ko-->
                    </p>
                </div>
                <!--/ko-->
            </div>
            <div class="footerInner">
                <div class="btnBox btnBox-2">
                    <a href="javascript:void(0);" data-bind="click: $root.unJoinClazzBtn" class="btn btn-grey">不加入</a>
                    <a href="javascript:void(0);" data-bind="click: $root.joinClazzBtn" class="btn">加入</a>
                </div>
            </div>
            <!--/ko-->
        </div>
    </script>

    <script>
        var isP6Clazz = false;
        <#if eduSystem?? && eduSystem == 'P6'>
        isP6Clazz = true;
        </#if>
        <#--var actualTeachClazzCount=${actualTeachClazzCount!0};-->
    </script>

</@activityMain.page>