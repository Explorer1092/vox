/**
 * @author: pengmin.chen
 * @description: "同步课件后台列表页"
 * @createdDate: 2019/3/23
 * @lastModifyDate: 2019/3/23
 */

var subjectObj = {
    'CHINESE': '语文',
    'MATH': '数学',
    'ENGLISH': '英语'
};
new Vue({
    el: '#courseContainer',
    data: {
        subjectObj: subjectObj,
        choiceSubjectEnglishName: '', // 选择的学科
        choiceClazzLevelId: '', // 选择的年级
        choiceTermId: '', // 选择的上下册
        choiceBookId: '', // 选择的教材
        choiceOnlineStatus: '', // 选择的状态
        choiceSource: '', // 来源
        inputTitle: '', // 输入的title
        inputId: '', // 输入的ID
        bookList: [], // 教材列表
        courseList: [], // 课件列表
        pageIndex: 1,
        pageSize: 10,
        totalSize: 0,
        loadingFlag: false // 请求flag
    },
    methods: {
        // 请求教材信息
        requestBookInfo: function () {
            var vm = this;
            $.ajax({
                url: '/opmanager/teacher_resource/book_list.vpage',
                type: 'GET',
                data: {
                    term: vm.choiceTermId,
                    clazzLevel: vm.choiceClazzLevelId,
                    subject: vm.choiceSubjectEnglishName
                },
                success: function (res) {
                    if (!res.success) {
                        vm.showTip(res.info, 'error');
                        return;
                    }
                    vm.bookList = res.books;
                    // 获取到教材之后，触发一次查询
                    vm.requestListInfo(true);
                }
            });
        },
        // 请求列表信息(flag为true表示需要重置page)
        requestListInfo: function (flag) {
            var vm = this;
            if (flag) {
                vm.pageIndex = 1;
                vm.pageSize = 10;
            }
            vm.loadingFlag = false;
            $.ajax({
                url: '/opmanager/teacher_resource/list.vpage',
                type: 'GET',
                data: {
                    id: vm.inputId,
                    title: vm.inputTitle,
                    subject: vm.choiceSubjectEnglishName,
                    clazz_level: vm.choiceClazzLevelId,
                    level_term: vm.choiceTermId,
                    book_id: vm.choiceBookId,
                    online_status: vm.choiceOnlineStatus,
                    source: vm.choiceSource,
                    page: vm.pageIndex,
                    page_size: vm.pageSize,
                },
                success: function (res) {
                    vm.loadingFlag = true;
                    if (!res.success) {
                        vm.showTip(res.info, 'error');
                        return;
                    }
                    vm.courseList = res.data;
                    vm.totalSize = res.totalElements;
                }
            });
        },
        // 切换每页的size时
        handleSizeChange: function (val) {
            var vm = this;
            vm.pageIndex = 1;
            vm.pageSize = val;
            vm.requestListInfo(false);
        },
        // 点击页码时
        handleCurrentChange: function (val) {
            var vm = this;
            vm.pageIndex = val;
            vm.requestListInfo(false);
        },
        // 预览
        previewCourse: function (course) {
            var url = mainSiteBaseUrl + "/view/mobile/teacher/primary/teaching_assistant/detail_course.vpage?resourceId=" + course.id;
            window.open(url, '_top ', 'width=375,height=667');
        },
        // 编辑
        editCourse: function (course) {
            window.location.href = '/opmanager/teacher_resource/course/edit.vpage?courseId=' + course.id;
        },
        // 下线
        onlineCourse: function (course) {
            var vm = this;
            vm.$confirm("您确定要" + (course.online ? '下线' : '上线') + "该课件吗?", '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(function () {
                vm.requestDelete(course.id).done(function(res) {
                    if (!res.success) {
                        vm.showTip(res.info, 'error');
                        return;
                    }
                    vm.$message({
                        type: 'success',
                        message: (course.online ? '下线' : '上线') + '成功!'
                    });
                    course.online = !course.online;
                });
            });
        },
        // 上线或下线
        requestDelete: function(id) {
            return ($.get('/opmanager/teacher_resource/online_offline.vpage', {id: id}));
        },
        // 显示提示
        showTip: function (info, type) {
            var vm = this,
                opt = {
                    showClose: true,
                    message: info
                };
            if (type) {
                opt = $.extend(opt, {
                    type: type
                });
            }
            vm.$message(opt);
        }
    },
    mounted: function () {
        var vm = this;
        vm.requestBookInfo(); // 请求教材信息
    }
});