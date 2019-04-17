<#import "../layout_view.ftl" as activityMain>
<@activityMain.page title="班级管理" pageJs="clazzManage">
    <@sugar.capsule css=["clazzManage"] />
    <div class="cHeader">
        <ul>
            <!--ko ifnot: isFakeTeacher-->
            <li><a data-bind="click: createClazzBtn" href="javascript:void (0);" class="cBtn cBtn-green">创建/退出班级</a></li>
            <!--/ko-->
            <li><a data-bind="click: applicationRecordBtn" href="javascript:void (0);" class="cBtn cBtn-yellow">申请记录</a></li>
        </ul>
    </div>
    <div class="cMain" data-bind="visible: $data.clazzDetail" style="display: none;">
        <ul class="cList">
            <!-- ko foreach : {data : $data.clazzDetail, as : '_clazz'} -->
            <li>
                <a class="list-cell" href="javascript:void (0);" data-bind="attr: {'href': '/teacher/clazzmanage/editclazz.vpage?clazzId='+_clazz.clazzId()+'&clazzName='+_clazz.clazzName()}">
                    <span class="grade">
                        <!--ko text: _clazz.clazzName()--><!--/ko-->
                        <!--ko if:_clazz.subjectText()-->
                            <!--ko text:"（"+_clazz.subjectText()+"）" --><!--/ko-->
                        <!--/ko-->
                    </span>
                    <span class="count" data-bind="text: '学生'+ _clazz.studentCount() + '人' ">--</span>
                </a>
            </li>
            <!--/ko-->
        </ul>
    </div>
</@activityMain.page>