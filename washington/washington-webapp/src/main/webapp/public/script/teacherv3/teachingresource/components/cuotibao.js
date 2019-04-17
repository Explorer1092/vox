$(function(){
    /**
     *
     * 错题宝
     */
    var cuoTiBao = {
        template : template("T:CUO_TI_BAO",{}),
        data : function(){
            return {
                wordContentList : []
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
            },
            clazzLevel : {
                type : Number,
                default : ""
            },
            termType : {
                type : Number,
                default : ""
            }
        },
        watch : {
            unitId : function(newUnitId,oldUnitId){
                (newUnitId !== oldUnitId) && this.wordContent()
            }
        },
        methods : {
            getMessageObject : function(success,noResources,noNetWork,isLoading){
                return {
                    success : success,
                    noResources : noResources || false,
                    noNetWork : noNetWork || false,
                    isLoading : isLoading || false
                }
            },
            wordContent : function(){
                var vm = this;
                var noNetWork,noResources;
                var isLoading = true;
                $.get("/teacher/teachingresource/content.vpage",{
                    bookId  : vm.bookId,
                    unitId  : vm.unitId,
                    sectionId : vm.sectionId,
                    type    : vm.type,
                    subject : vm.subject,
                    clazzLevel : vm.clazzLevel,
                    termType : vm.termType
                }).done(function(res){
                    var content = res.content || [];
                    vm.wordContentList = content;
                   /* var info = "";
                    if(res.success){
                        info = content.length > 0 ? "" : "暂无内容数据";
                    }else{
                        info =  res.info || "接口请求错误";
                    }*/
                    noNetWork = false;
                    if(res.success){
                        isLoading = false;
                        if(!content.length > 0 ){
                            noResources = true;
                        }
                    }else{
                        $17.info("接口请求错误");
                    }
                    // var result = vm.getMessageObject(res.success && content.length > 0,info);
                    var result = vm.getMessageObject(res.success && content.length > 0,noResources,noNetWork,isLoading);

                    $17.info(result);
                    vm.$emit("content-message",result);
                }).fail(function(e){
                    vm.wordContentList = [];
                    noResources = false;
                    isLoading = false;
                    noNetWork = true;
                    vm.$emit("content-message",vm.getMessageObject(false,noResources,noNetWork,isLoading));
                    // vm.$emit("content-message",vm.getMessageObject(false,e.message));
                });
            },
            playVideo : function(id,videoUrl,coverUrl){
                var vm = this;
                vm.$emit("preview-video",{
                    id :id,
                    videoUrl : videoUrl,
                    coverUrl : coverUrl
                });
            }
        },
        created : function(){
            var vm = this;
            vm.wordContent();
            $17.voxLog({
                module: "m_yvkU37oY9J",
                op : "pc_home_one_form_page_load",
                s0 : vm.subject,
                s1 : vm.bookId,
                s2 : vm.unitId,
                s3 : {sectionId:vm.sectionId,type:vm.type}
            });
            $17.info("levelReadings created....");
        },
        mounted : function(){
            $17.info("levelReadings mounted....");
        },
        beforeDestroy : function(){
            $17.info("levelReadings beforeDestroy....");
        },
        destroyed : function () {
            $17.info("levelReadings destroyed....");
        }
    };

    $17.teachingresource = $17.teachingresource || {};
    $17.extend($17.teachingresource, {
        cuoTiBao   : cuoTiBao
    });
});