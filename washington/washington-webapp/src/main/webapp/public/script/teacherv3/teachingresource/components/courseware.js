$(function(){
    /**
     *
     * 课件
     */
    var classCourseWare = {
        template : template("T:CLASS_COURSE_WARE",{}),
        data : function(){
            return {
                courseInfoList : [],
                isOpen: false,
                pageNum : 1,
                pageSize : 18,
                totalPages: 0,
                loadData: false,
                isCourse : false
            };
        },
        props : {
            domain : {
                type : String,
                default : ""
            },
            imgDomain : {
                type : String,
                default : ""
            },
            env : {
                type : String,
                default : ""
            },
            bookId : {
                type : String,
                default : ""
            },
            unitId : {
                type : String,
                default : ""
            },
            sectionId : {
                type : String,
                default : ""
            },
            type : {
                type : String,
                default : ""
            },
            subject : {
                type : String,
                default : ""
            }
        },
        watch : {
            unitId : function(newUnitId,oldUnitId){
                if(this.subject === "ENGLISH"){
                    if(newUnitId !== oldUnitId){
                        this.courseContent(1);
                        this.courseInfoList = [];
                    }
                }
            },
            sectionId : function(newsectionId,oldsectionId){
                if(this.subject === "CHINESE" || this.subject === "MATH"){
                    if(newsectionId !== oldsectionId){
                        this.courseContent(1);
                        this.courseInfoList = [];
                    }
                }
            }
        },
        methods : {
            /*getMessageObject : function(success,info){
                return {
                    success : success,
                    info    : info || ""
                }
            },*/
            getMessageObject : function(success,noResources,noNetWork,isLoading){
                return {
                    success : success,
                    noResources : noResources || false,
                    noNetWork : noNetWork || false,
                    isLoading : isLoading || false
                }
            },
            courseContent: function (pageNum) {
                var vm = this;
                var noNetWork,noResources;
                var isLoading = true;
                vm.pageNum = pageNum;
                $.get("/teacher/teachingresource/content.vpage", {
                    bookId  : vm.bookId,
                    unitId  : vm.unitId,
                    type    : vm.type,
                    pageNum : vm.pageNum,
                    pageSize : vm.pageSize,
                    subject : vm.subject
                }).done(function (res) {
                    var content = res.content.content || [];
                    vm.courseInfoList = vm.courseInfoList.concat(content);
                    vm.totalPages = res.content.totalPages || 0;
                    vm.loadData = true;
                    // var info = "";
                    // if(res.success){
                    //     info = vm.courseInfoList.length > 0 ? "" : "暂无内容数据";
                    // }else{
                    //     info =  res.info || "接口请求错误";
                    // }

                    // var result = vm.getMessageObject(res.success && vm.courseInfoList.length > 0,info);
                    noNetWork = false;
                    if(res.success){
                        isLoading = false;
                        if(!content.length > 0 ){
                            noResources = true;
                        }
                    }else{
                        $17.info("接口请求错误");
                    }
                    var result = vm.getMessageObject(res.success && content.length > 0,noResources,noNetWork,isLoading);
                    vm.$emit("content-message",result);
                }).fail(function (e) {
                    vm.courseInfoList = [];
                    $17.error(e.message);
                    noResources = false;
                    isLoading = false;
                    noNetWork = true;
                    vm.$emit("content-message",vm.getMessageObject(false,noResources,noNetWork,isLoading));
                    // vm.$emit("content-message",vm.getMessageObject(false,"网络错误，请退出页面重试"));
                });
            },
            openOrDown : function(){
                this.isOpen = !this.isOpen;
            },
            loadPage: function() {
                var vm = this;
                var $coursewareBox = $('.coursewareBox');
                var $contentLeafSpring = $(".contentLeafSpring");
                var $window = $(window);
                $(".containerCon").scroll($17.throttle(function () {
                    //接近底部50像素时，请求接口
                    if ($coursewareBox.height() + $contentLeafSpring.height() <= ($window.height() + $(this).scrollTop() + 50) && vm.loadData) {
                        vm.loadData = false;
                        if (vm.pageNum === vm.totalPages) {
                            vm.isCourse = true;
                            setTimeout(function () {
                                vm.loadData = true;
                            }, 2000);
                        } else {
                            vm.isCourse = false;
                            vm.courseContent(vm.pageNum + 1);
                        }
                    }
                }));
            },
            previewCourseware : function(coursewareUrl){
                if(!coursewareUrl){
                    $17.alert("课件地址不存在，请联系管理员");
                    return false;
                }
                var vm = this;
                vm.$emit("previewtype",{
                    type : vm.type,
                    params : {
                        coursewareUrl : coursewareUrl  //ppt或pptx课件的绝对地址
                    }
                });
            }
        },
        created : function(){
            var vm = this;
            vm.courseContent(vm.pageNum);
            $17.voxLog({
                module: "m_yvkU37oY9J",
                op : "pc_home_one_form_page_load",
                s0 : vm.subject,
                s1 : vm.bookId,
                s2 : vm.unitId,
                s3 : {sectionId:vm.sectionId,type:vm.type}
            });
            $17.info("courseware created....");
        },
        mounted : function(){
            var vm = this;
            $17.info("courseware mounted....");
            vm.loadPage();
        },
        beforeDestroy : function(){
            $17.info("courseware beforeDestroy....");
        },
        destroyed : function () {
            $17.info("courseware destroyed....");
        }
    };

    $17.teachingresource = $17.teachingresource || {};
    $17.extend($17.teachingresource, {
        classCourseWare   : classCourseWare
    });
});