<#import "../layout_view.ftl" as activityMain>
<@activityMain.page title="添加老师" pageJs="addTeacher">
    <@sugar.capsule css=["clazzManage"] />
    <div class="addTeacher" data-bind="visible: subjectListDetail" style="display: none;">
        <ul class="cList">
            <!-- ko foreach : {data : subjectListDetail, as : '_list'} -->
            <!--ko if: _list.teacher-->
            <li>
                <span><!--ko text: _list.subjectName--><!--/ko--></span>
                <span class="name" data-bind="text: '('+ _list.teacher.name()+ _list.teacher.id() +')'"></span>
                <!--ko if: _list.teacher.authenticationState-->
                <i class="auth-icon"></i>
                <!--/ko-->
                <!--ko ifnot: _list.justSentApplication-->
                    <span class="span-r addState">（已添加）</span>
                <!--/ko-->
                <!--ko if: _list.justSentApplication-->
                <span class="span-r addState">（等待对方同意）</span>
                <!--/ko-->
            </li>
            <!--/ko-->

            <!--ko ifnot: _list.teacher-->
            <li>
                <span><!--ko text: _list.subjectName--><!--/ko-->老师</span>
                <span class="span-r cBtn cBtn-blue" data-bind="click: $root.addTeacherBtn">添加老师</span>
            </li>
            <!--/ko-->
            <!--/ko-->

        </ul>
    </div>
</@activityMain.page>