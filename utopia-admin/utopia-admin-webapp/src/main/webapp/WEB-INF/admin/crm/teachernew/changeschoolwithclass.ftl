<#-- created by pengmin.chen 2017.08.04 -->

<#import "../../layout_default.ftl" as layout_default/>
<#import "../headsearch.ftl" as headsearch>
<@layout_default.page page_title="学校查询" page_num=3>

<div class="span10">
  <@headsearch.headSearch/>
  <link href="${requestContext.webAppContextPath}/public/css/changeschoolwithclass/changeschoolwithclass.css" rel="stylesheet">
  <script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
  <div id="classgo" v-cloak="v-cloak">
    <h2>带班转校（<a target="_blank" class="hover-underline" :href="'/crm/teachernew/teacherdetail.vpage?teacherId=' + operateTeacherId">{{ operateTeacherId }}</a>）</h2>
    
    <!-- s title -->
    <div class="s-title">
      <p class="s-title-teacher">选择要转校的老师</p>
      <p class="s-title-group">选择要带走的班级</p>
    </div>
    
    <!-- main container -->
    <div class="main-container">

      <!-- 横向单条数据 -->
      <div class="single-container" v-for="(pageData, outerIndex) in pageDatas">
        <!-- 左侧老师 -->
        <div class="inline-b vertical-t left-teacher">
          <div class="checkbox">
            <label class="inline-b vertical-m" :id="'teacher-'+pageData.teacherInfo.teacherId" :data-from-groupid="pageData._fromGroupId" @click="selectTeacher(outerIndex, pageData.teacherInfo, $event)">
              <input type="checkbox" :checked="pageData._isChecked">
              <span>{{ pageData.teacherInfo.teacherName }}</span>
              <span>{{ pageData.teacherInfo.subject }}</span>
            </label>
            <p class="inline-b vertical-m">
              <span class="vertical-m">(</span>
              <a target="_blank"  class="vertical-m hover-underline" :href="'/crm/teachernew/teacherdetail.vpage?teacherId=' + pageData.teacherInfo.teacherId">{{ pageData.teacherInfo.teacherId }}</a>
              <span class="vertical-m">)</span>
              <span class="vertical-m canclick hover-underline" :id="'teacherphone-'+pageData.teacherInfo.teacherId" @click="showPhone(pageData.teacherInfo)">手机号</span>
            </p>
          </div>
        </div>
        <!-- 右侧学生 -->
        <div class="inline-b vertical-t right-student">
          <div class="checkbox inline-b student-list" v-for="(groupInfo, innerIndex) in pageData.groupList">
            <label class="inline-b vertical-m" :id="'group-'+groupInfo.groupId" @click="selectGroup(outerIndex, innerIndex, groupInfo, $event)">
              <input type="checkbox" :checked="groupInfo._isChecked" :disabled="groupInfo._disabledClick">
              <span>{{ groupInfo.clazzName }}</span>
            </label>
            <p class="inline-b vertical-m">
              <span>(</span>
              <a target="_blank"  class="hover-underline" :href="'/crm/clazz/groupinfo.vpage?groupId=' + groupInfo.groupId">{{ groupInfo.groupId }}</a>
              <span>)</span>
            </p>
          </div>
        </div>
      </div>
      <!-- 新增数据 -->
      <div class="add-data">
        <div class="inline-b canclick" id="add-data-tipbox" @click="addTeacher()">
          <img class="inline-b vertical-m add-icon" src="${requestContext.webAppContextPath}/public/img/add.png" alt="">
          <span class="inline-b vertical-m add-span" style="display: inline-block;">新增老师</span>
        </div>
        <div class="inline-b display-none" id="add-data-inputbox">
          <input class="vertical-m add-input" id="add-data-input" type="text" placeholder="请输入老师ID" @keyup.enter="sureAdd()">
          <button class="btn btn-primary vertical-m add-btn" @click="sureAdd()">确定</button>
          <button class="btn btn-default vertical-m add-btn" @click="cancelAdd()">取消</button>
        </div>
      </div>
      <!-- 提交按钮 -->
      <button class="btn btn-primary submit-btn" @click="beforeCommit()">提 交</button>
    </div>

    <!-- 错误弹窗 -->
    <div id="alert_dialog" class="modal fade">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>提示</h3>
      </div>
      <div class="modal-body">
        <p id="errorInfo"></p>
      </div>
      <div class="modal-footer">
        <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">确 定</button>
      </div>
    </div>
    
    <!-- 错误数据弹窗 -->
    <div id="alert_dialog2" class="modal fade">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>提示</h3>
      </div>
      <div class="modal-body">
        <p id="errorInfo2"></p>
        <div id="errorListInfo" class="errordata-box">
          <table class="errordata-table">
            <tr>
              <td class="teacher-info">关联老师</th>
              <td  class="students-info">关联学生</th>
            </tr>
            <tr v-for="errorData in errorDatas">
              <td class="teacher-info">
                <span>{{errorData.teacherInfo.teacherName}}</span>
                <span>{{errorData.teacherInfo.subject}}</span>
                <span>(</span>
                <a target="_blank" :href="'/crm/teachernew/teacherdetail.vpage?teacherId=' + errorData.teacherInfo.teacherId" class="canclick hover-underline">{{errorData.teacherInfo.teacherId}}</a>
                <span>)</span>
              </td>
              <td class="students-info">
                <span class="inline-b" style="width: 50%;" v-for="(studentInfo, index) in errorData.students">
                  <span>{{studentInfo.studentName}}</span>
                  <span>(</span>
                  <a target="_blank" :href="'/crm/student/studenthomepage.vpage?studentId=' + studentInfo.studentId" class="canclick hover-underline">{{ studentInfo.studentId }}</a>
                  <span>)</span>
                </span>
              </td>
            </tr>
          </table>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">确 定</button>
      </div>
    </div>

    <!-- 确定后输入新学校信息弹窗 -->
    <div id="alert_dialog3" class="modal fade">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>更改老师学校</h3>
      </div>

      <div class="modal-body">
          <div class="single-data-box">
            <label class="label inline-b vertical-t notclick">老师信息：</label>
            <label id="teacherNames" class="data-box inline-b vertical-t notclick show-text"></label>
          </div>

          <div class="single-data-box">
            <label class="label inline-b vertical-t notclick">记录类型：</label>
            <label class="data-box inline-b vertical-t notclick show-text">老师操作</label>
          </div>

          <div class="single-data-box">
            <label for="inputSchoolId" class="label inline-b vertical-t">学校ID：</label>
            <input type="text" id="inputSchoolId" class="inline-b vertical-t" placeholder="请输入学校ID">
          </div> 

          <div class="single-data-box">
            <label for="inputDesc" class="label">问题描述：</label>
            <textarea type="textarea" id="inputDesc" class="inline-b vertical-t" placeholder="输入描述信息"></textarea>
          </div>

          <div class="single-data-box">
            <label class="label inline-b vertical-t">所做操作：</label>
            <label class="data-box inline-b vertical-t notclick show-text">老师带班更改学校</label>
          </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-default" data-dismiss="modal" aria-hidden="true">取 消</button>
        <button class="btn btn-primary" id="lastSubmitBtn">确 定</button>
      </div>
    </div>
  </div>
</div>

<script type="text/javascript">
  var vm = new Vue({
    el: "#classgo",
    data: {
      operateTeacherId: '', // 操作的老师ID
      pageDatas: [], // 页面列表数据
      postDatas: { // 提交的数据（teacherNames不提交，自用）
        teacherIds: [],
        allTeacherIds: [],
        teacherNames: [],
        groups: []
      },
      topTeacherIds: [], // 顶层的ID(第一次操作的老师ID和新增的老师ID)
      deleteTeacherIds: [], // 删除操作，产生的所有老师ID集合
      errorDatas: [] // 有错误数据处理后显示
    },
    mounted: function mounted () {
      this.getTeacherInfo(this.getQueryString('teacherId'))
      this.operateTeacherId = this.getQueryString('teacherId');
      document.title = '带班转校（' + this.operateTeacherId + ')';

      $('#lastSubmitBtn').on('click', function(){ this.commit(false) }.bind(this))
    },
    methods: {
      // 显示手机号
      showPhone: function showPhone (teacherInfo) {
        $('#teacherphone-' + teacherInfo.teacherId).removeClass('canclick hover-underline').text(teacherInfo.mobile || '无手机号');
      },
      // 点击老师处理
      selectTeacher: function selectTeacher (index, teacherInfo, event) {
        // 根据事件源阻止label事件会触发两次的bug
        var ev = event || window.event;
        var eTarget = ev.target || ev.srcElement;
        if (!$(eTarget).is('input')) return false;

        var isSelect = $('#teacher-' + teacherInfo.teacherId).find('input').is(":checked");
        // var isSelect = $('#teacher-' + teacherInfo.teacherId).find('input').prop("checked");
        if (isSelect) { // 如果是勾选
          this.pageDatas[index]._isChecked = true; // 将_isChecked改为true
          this.getGroupInfo(index, teacherInfo.teacherId);
        } else { // 如果是去除
          // this.pageDatas[index]._isChecked = false; // 将_isChecked改为false
          this.removeTeacherInfo(index, teacherInfo.teacherId)
        }
      },
      // 点击班组处理
      selectGroup: function selectGroup (outerIndex, innerIndex, groupInfo, event) {
        // 根据事件源阻止label事件会触发两次的bug
        var ev = event || window.event;
        var eTarget = ev.target || ev.srcElement;
        if (!$(eTarget).is('input')) return false;

        var isSelect = $('#group-' + groupInfo.groupId).find('input').is(":checked");
        if (isSelect) {
          this.pageDatas[outerIndex].groupList[innerIndex]._isChecked = true; // 将_isChecked改为true
          this.getGroupTeachersInfo(innerIndex, groupInfo.groupId, groupInfo._disabledClick);
        } else {
          // this.pageDatas[outerIndex].groupList[innerIndex]._isChecked = false; // 将_isChecked改为false
          this.removeGroupInfo(outerIndex, innerIndex, groupInfo.groupId);
        }
      },
      // 请求1：老师id 请求 老师信息（初始化调用 和 添加时调用）
      getTeacherInfo: function getTeacherInfo(teacherId){
        var self = this;
        $.ajax({
          url: 'getTeacher.vpage',
          type: 'GET',
          data: {
            schoolId: this.getQueryString('schoolId'),
            teacherId: teacherId
          },
          success: function(result) {
            console.log('result', result);
            if (result.success) {
              // 遍历当前列表，无则添加
              var isHas = false;
              self.pageDatas.forEach(function(pageData){
                if (pageData.teacherInfo.teacherId === teacherId) isHas = true;
              });
              // 当前列表不存在此teacherId
              if (!isHas) {
                self.topTeacherIds.push(teacherId); // 存储顶层teacherId
                // push新数据
                var singleData = {
                  teacherInfo: result.teacherInfo,
                  groupList: [],
                  _fromGroupId: '',
                  _isChecked: false
                }
                self.pageDatas.push(singleData);
                $('#add-data-tipbox').show(); // 显示添加
                $('#add-data-inputbox').hide(); // 隐藏输入框
                $('#add-data-input').val(''); // 情况输入框
              } else {
                self.showError('已存在此teacherId，请重新添加！');
              }
            } else {
              self.showError(result.info)
            }
          },
          error: function() {
            self.showError()
          }
        })
      },
      // 请求2：老师id 请求 学生信息
      getGroupInfo: function getGroupInfo (index, teacherId) {
        var self = this;
        $.ajax({
          url: 'getGroups.vpage',
          type: 'GET',
          data: {
            teacherId: teacherId
          },
          success: function (result) {
            console.log('result', result);
            if (result.success) {
              var resultDatas = result.groupList
              // 先给新增的数据统一增加一个_disabledClick = fale 和 _isChecked = false;
              resultDatas.forEach(function (resultData) {
                resultData._disabledClick = false; // 不可操作
                resultData._isChecked = false; // 选中状态
              })

              // 遍历查看groupParent是否groupParent存在一致，有则修改_disabledClick为true
              resultDatas.forEach(function (resultData, resultDataIndex) {
                self.pageDatas.forEach(function (pageData){
                  pageData.groupList.forEach(function (singleData){
                    if (resultData.groupParent && resultData.groupParent === singleData.groupParent) {
                      resultData._disabledClick = true;
                      resultData._isChecked = true;
                    }
                  });
                });
              });

              // 给对应的班组赋值
              self.pageDatas[index].groupList = resultDatas;

              // 全部自动勾选(且请求对应的老师信息)
              self.pageDatas[index].groupList.forEach(function(group, groupIndex){
                group._isChecked = true;
                self.getGroupTeachersInfo(groupIndex, group.groupId, group._disabledClick);
              })
            } else {
              self.showError(result.info)
            }
          },
          error: function(error) {
            self.showError();
          }
        })
      },
      // 请求3：班组id 请求 老师信息
      getGroupTeachersInfo: function getGroupTeachersInfo (index, groupId, disabledClick) {
        var self = this;
        $.ajax({
          url: 'getGroupTeachers.vpage',
          type: 'GET',
          data: {
            groupId: groupId
          },
          success: function (result) {
            console.log('result', result);
            if (result.success) {
              // 先遍历判断当前列表是否存在此teacherID，无则添加
              if (result.groupTeacherList.length === 0) return false;
              result.groupTeacherList.forEach(function (groupTeacher) {
                var isHas = false;
                self.pageDatas.forEach(function (pageData, pageDataIndex) {
                  // 当前列表中已存在此teacherId
                  if (pageData.teacherInfo.teacherId === groupTeacher.teacherId) { // 当前teacherId在list中已经存在
                    isHas = true;
                    if (!disabledClick) { // 如果是可点击的（过滤掉不可点击的）
                      if (pageData._fromGroupId !== '') { // 不为空
                        // 判断当前teacherId对应的_fromGroupId中是否存在groupId
                        if(!self.seeFromGroupIdHave(pageData._fromGroupId, groupId)) {
                          pageData._fromGroupId += '-' + groupId;
                        }
                      } else { // 为空
                        pageData._fromGroupId = groupId;
                      }
                    }
                  }
                });
                if (!isHas) { // 当前teacherId未在列表中存在
                  var singleData = {
                    teacherInfo: groupTeacher,
                    groupList: [],
                    _fromGroupId: groupId,
                    _isChecked: false
                  }
                  // 给老师节点绑定一个_fromGroupId作为此groupId引出该条记录的凭证
                  // 将新增的老师数据push到列表上
                  self.pageDatas.push(singleData);
                }
              });
            } else {
              self.showError(result.info);
            }
          },
          error: function (error) {
            self.showError();
          }
        })
      },
      // 删除老师信息
      removeTeacherInfo: function removeTeacherInfo(index, teacherId){
        // 将_isChecked置为false
        this.pageDatas[index]._isChecked = false;
        // 清空旗下班组产生的老师信息栏
        var groupList = this.pageDatas[index].groupList;
        for(var i = 0, len1 = groupList.length; i < len1; i++){ // 遍历删除当前groupList
          this.removeGroupInfo(index, i, groupList[i].groupId);
        }
        // 清空右侧班组信息
        this.pageDatas[index].groupList = [];
      },
      // 删除班组信息
      removeGroupInfo: function removeGroupInfo(outerIndex, innerIndex, groupId){
        // 将_isChecked置为false
        this.pageDatas[outerIndex].groupList[innerIndex]._isChecked = false;

        // 递归查找需要删除的teacherId集合
        this.deleteTeacherIds = [];
        this.recursionFindGroupChilds(groupId);

        // 删除上述递归产生的teacherId集合中的每一项
        if (this.deleteTeacherIds.length === 0) return false;
        this.removeRecursionTeacher(this.deleteTeacherIds);
      },
      // 递归遍历查找由班组产生的所有人
      recursionFindGroupChilds: function recursionFindGroupChilds(groupId){
        var pageDatas = this.pageDatas;
        // 遍历pageDatas，查看由该groupId产生的teacherId
        for (var i = 0, len = pageDatas.length; i < len; i++) {
          if (this.seeFromGroupIdHave(pageDatas[i]._fromGroupId, groupId)) { // _fromGroupId是否存在groupId（是否有引导关系）
            if (pageDatas[i]._fromGroupId.split('-').length !== 1) { // 当前_fromGroupId存在多个，即该老师由多个班组引出
              // 存在多个时，删除_fromGroupId其中的一个
              pageDatas[i]._fromGroupId = this.deleteStringOneTerm(pageDatas[i]._fromGroupId, groupId);
            } else { // 当前_fromGroupId只有一个，即该老师只由一个班组引出
              // 只有一个时，_fromGroupId与groupId相等，且teacherId是非顶层ID
              if (pageDatas[i]._fromGroupId === groupId && !this.seeArrayHaveOneTerm(this.topTeacherIds, pageDatas[i].teacherInfo.teacherId)) {
                // push此teacherId，以便删除
                this.deleteTeacherIds.push(pageDatas[i].teacherInfo.teacherId);
                // 遍历此teacherId下的group，对于勾选的group，递归调用 recursionFindGroupChilds 方法循环找teacherId
                for (var j = 0, len2 = pageDatas[i].groupList.length; j < len2; j++) {
                  if (pageDatas[i].groupList[j]._isChecked) {
                    this.recursionFindGroupChilds(pageDatas[i].groupList[j].groupId); // 递归遍历
                  }
                }
              }
            }
          }
        }
      },
      // 删除递归找到的teacher
      removeRecursionTeacher: function removeRecursionTeacher (teacherIds) {
        var pageDatas = this.pageDatas;
        for (var q = 0, qlength = teacherIds.length; q < qlength; q++) {
          for (var p = 0, plength = pageDatas.length, flag = false; p < plength; flag ? p++ : p) {
            if (pageDatas[p] && pageDatas[p].teacherInfo.teacherId === teacherIds[q]) { // 找到teacherId对应的那一项
              pageDatas.splice(p, 1);
              flag = false;
            } else {
              flag = true;
            }
          }
        }
      },
      // 新增老师
      addTeacher: function addTeacher() {
        $('#add-data-tipbox').hide();
        $('#add-data-inputbox').show();
      },
      // 确认添加
      sureAdd: function sureAdd(){
        this.getTeacherInfo($('#add-data-input').val());
      },
      // 取消添加
      cancelAdd: function cancelAdd() {
        $('#add-data-tipbox').show();
        $('#add-data-inputbox').hide();
        $('#add-data-input').val(''); // 清空输入框
      },
      // 提交前校验和获取数据
      beforeCommit: function beforeCommit(){
        // 提交
        var self = this;
        
        // 遍历pageDatas，查看是否存在勾选老师但不勾选班组的情况，有则弹窗提示
        var hasError = false;
        this.pageDatas.forEach(function (pageData) { // 遍历外层pageDatas
          if (pageData._isChecked) { // 老师已勾选
            var groupListHasChecked = false;
            pageData.groupList.forEach(function (group) { // 遍历里层groupList
              if (group._isChecked) groupListHasChecked = true;
            });
            if (!groupListHasChecked) { // 班组无勾选
              hasError = true;
              return false; // 跳出
            }
          }
        });
        if (hasError) {
          this.showError('当前提交的列表中存在已勾选老师但未勾选班组的情况，无法带班转校，请修改后提交或使用不带班转校功能！');
          return false; // 跳出
        }

        // 先清空提交的数据，遍历当前页面数据pageDatas，找到已勾选的数据，提交给后端
        self.postDatas.teacherIds = [];
        self.postDatas.allTeacherIds = [];
        self.postDatas.teacherNames = [];
        self.postDatas.groups = [];
        this.pageDatas.forEach(function (pageData) {
          // 先判断勾选的teacherIds、allTeacherIds和teacherNames是否push，无push则push
          var isHaveSameTeacherId = false;
          var isHaveSameAllTeacherId = false;
          var isHaveSameTeacherName = false;
          self.postDatas.teacherIds.forEach(function (teacherId) {
            if (teacherId === pageData.teacherInfo.teacherId) isHaveSameTeacherId = true;
          });
          self.postDatas.teacherIds.forEach(function (allteacherId) {
            if (allteacherId === pageData.teacherInfo.teacherId) isHaveSameAllTeacherId = true;
          });
          self.postDatas.teacherNames.forEach(function (teacherName) {
            if (teacherName === pageData.teacherInfo.teacherName) isHaveSameTeacherName = true;
          });
          if (pageData._isChecked && !isHaveSameTeacherId) self.postDatas.teacherIds.push(pageData.teacherInfo.teacherId);
          if (!isHaveSameAllTeacherId) self.postDatas.allTeacherIds.push(pageData.teacherInfo.teacherId);
          if (pageData._isChecked && !isHaveSameTeacherName) self.postDatas.teacherNames.push(pageData.teacherInfo.teacherName);

          // 先判断勾选的groups是否已经push，无push则push
          pageData.groupList.forEach(function (group) {
            var isHaveSameGroup = false;
            self.postDatas.groups.forEach(function (postGroup) {
              if (group.groupId === postGroup.groupId) isHaveSameGroup = true;
            });
            if (group._isChecked && !isHaveSameGroup) {
              self.postDatas.groups.push({
                groupId: group.groupId,
                groupParent: group.groupParent
              });
            }
          });
        });

        // 前端校验通过走后端校验
        this.commit(true);
      },
      // 最终提交(flag为标志，true表示后端校验数据，false表示校验成功后接受信息)
      commit: function commit(flag){
        if (this.postDatas.teacherIds.length === 0 && this.postDatas.groups.length === 0) {
          this.showError('当前没有可提交的带班转校信息！');
          return false;
        }
        var self = this;
        // 提交请求
        $.ajax({
          url: 'changeschoolpre.vpage',
          type: 'POST',
          data: {
            teacherIds: JSON.stringify(this.postDatas.teacherIds),
            allTeacherIds: JSON.stringify(this.postDatas.allTeacherIds),
            groups: JSON.stringify(this.postDatas.groups),
            schoolId: flag ? '' : $('#inputSchoolId').val(),
            changeSchoolDesc: flag ? '' : $('#inputDesc').val(),
            check: flag
          },
          success: function (result) {
            console.log('result', result);
            if (flag) { // 第一次校验
              if (!result.success && result.result) { // 第一次未通过 且 返回错误数据
                self.dealwithErrorData(result.result);
              } else { // 第一次通过
                $('#alert_dialog3').show().modal('show');
                $('#teacherNames').text(self.postDatas.teacherNames.join('、'));
              }
            } else { // 第二次提交数据
              if (result.success) { // 最终提交成功
                $('#alert_dialog3').hide().modal('hide').find('#inputSchoolId, #inputDesc').val('');
                self.showError('你已正确提交信息，带班转校已完成！');
              } else {
                $('#alert_dialog3').hide().modal('hide');
                self.showError(result.info);
              }
            }
          },
          error: function () {
            self.showError();
          }
        })
      },
      // 处理错误数据
      dealwithErrorData: function dealwithErrorData (errDatas) {
        this.errorDatas = [];
        var newDatas = this.errorDatas;

        // 遍历错误数据
        errDatas.forEach(function (errData) { 
          var isHaveTeacher = false;
          newDatas.forEach(function (newData, newDataIndex) {
            if (errData.teacherInfo.teacherId === newData.teacherInfo.teacherId) isHaveTeacher = true;
          });
          if (!isHaveTeacher) {
            newDatas.push({
              teacherInfo: errData.teacherInfo,
              students: []
            });
          }
        });
        newDatas.forEach(function (newData) {
          errDatas.forEach(function (errData) {
            if (newData.teacherInfo.teacherId === errData.teacherInfo.teacherId) {
              var isHaveStudent = false;
              newData.students.forEach(function (newDataStudent) {
                if (newDataStudent === errData.studentInfo.studentId) isHaveStudent = true;
              });
              if (!isHaveStudent) newData.students.push(errData.studentInfo);
            }
          });
        });
        $("#errorInfo2").html("所选班级中学生关联以下老师，请先将学生从老师名下删除，或手动添加老师至转校老师列表！");
        $('#alert_dialog2').show().modal('show');
      },
      // 报错弹窗
      showError: function showError(errInfo){
        $("#errorInfo").html(errInfo ? errInfo : "好像出错咯，请稍后再试！");
        $('#alert_dialog').show().modal('show');
      },
      // 判断数据结构中_fromGroupId是否包含当前groupId
      seeFromGroupIdHave: function seeFromGroupIdHave (fromGroupId, groupId) {
        var hasGroupId = false;
        fromGroupId.split('-').forEach(function (fromGid) {
          if (fromGid === groupId) hasGroupId = true;
        });
        return hasGroupId;
      },
      // 判断数组中是否存在某一项
      seeArrayHaveOneTerm: function seeArrayHaveOneTerm (topTeacherArr, teacherId) {
        var hasTeacherId = false;
        topTeacherArr.forEach(function (topTeacher) {
          if (topTeacher === teacherId) hasTeacherId = true;
        });
        return hasTeacherId;
      },
      // 删除字符串中一项（如：1234-2345-3456-4567，删除其中一个且保持连接结构）
      deleteStringOneTerm: function deleteStringOneTerm (originalString, deleteString) {
        var arr = originalString.split('-');
        for (var m = 0, mlength = arr.length, flag = false; m < mlength; flag ? m++ : m) {
          if (arr[m] === deleteString) {
            arr.splice(m, 1);
            flag = false;
          } else {
            flag = true;
          }
        }
        return arr.join('-');
      },
      // 获取链接参数
      getQueryString: function getQueryString (name) { // 解析链接参数
        const reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)');
        const r = decodeURI(window.location.search).substr(1).match(reg);
        if (r !== null) return unescape(r[2]);
        return null;
      }
    }
  });
</script>
</@layout_default.page>