<@sugar.capsule js=["vue"] css=["new_teacher.poetryactivity"] />
<style type="text/css">
    [v-cloak]{
        display: none;
    }
</style>
<div class="poetry-container" id="poetry-20190228153840" v-cloak v-if="clazzList.length > 0" style="margin: 0 0 20px;">
    <div class="title">亲子诗词大会</div>
    <div class="s-content-box">
        <div class="content-box">
            <div class="float-left content-left">
                <div class="left-title">选择班级</div>
                <ul class="clazz-list">
                    <li v-for="(item,index) in clazzList" v-bind:class="{'active':item.groupId == clazz.groupId}" v-on:click="switchClazz(item)" class="clazz-item" v-text="item.fullName">&nbsp;</li>
                </ul>
            </div>
            <div class="content-right">
                <p class="pre-tip">
                    老师选择班群和活动报名，报名后学生即可参加，同时活动不可取消
                </p>
                <div class="activities-list">
                    <div class="float-left activity-item" v-for="(item,index) in activityList" v-bind:key="item.activityId">
                        <div class="float-left a-left">
                            <div class="img-box">
                                <img v-bind:src="item.coverImgUrl">
                            </div>
                        </div>
                        <div class="float-right a-right" v-on:click="activityRegister(item)">
                            <a class="a-btn a-btn-blue" v-if="item.status == 'NOT_REGISTER'">立即参加</a>
                            <a class="a-btn a-btn-disabled" v-if="item.status == 'NOT_START' || item.status == 'REGISTERED'" v-text="item.status == 'NOT_START' ? '敬请期待' : '已报名'"></a>
                        </div>
                        <div class="activity-desc">
                            <p class="a-title" v-text="item.activityName"></p>
                            <p class="a-tag">
                                <span v-for="(labelObj,zIndex) in item.labels" v-text="labelObj">&bnsp;</span>
                            </p>
                            <p class="a-result" v-if="item.joinCount >= 1000">已有<i class="a-num" v-text="item.joinCount">0</i>人参与</p>
                            <p class="a-result" v-if="item.joinCount < 1000">火热进行中</p>
                        </div>
                    </div>
                </div>
                <p class="post-tip">如有需要，请前往一起小学老师APP中体验活动或查看已报名班级的古诗学习报告</p>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        new Vue({
            el : "#poetry-20190228153840",
            data : {
                clazzList : [],
                clazz : null,
                activityList : [],
                clazzActivityMap : {}
            },
            watch : {
                clazz : function(){
                    this.getActivityList();
                }
            },
            methods : {
                getClazzList : function(){
                    var vm = this;
                    $.get("/teacher/outside/reading/report/clazzlist.vpage",{

                    }).done(function(data){
                        if(data.success && data.clazzList.length > 0){
                            vm.switchClazz(data.clazzList[0]);
                            vm.clazzList = data.clazzList;
                        }else{
                            vm.clazzList = [];
                            vm.clazz = null;
                        }
                    }).fail(function(){
                        vm.clazzList = [];
                        vm.clazz = null;
                    });
                },
                switchClazz :  function(clazz){
                    var vm = this;
                    vm.clazz = clazz || null;
                    $17.voxLog({
                        module : "m_FeJejhY7pq",
                        op : "o_vm5ZBuyHWR",
                        s0 : clazz.groupId
                    });
                },
                getActivityList:function(){
                    var vm = this;
                    if(!vm.clazz){
                        return false;
                    }
                    var clazz = vm.clazz;
                    var clazzGroupId = clazz.groupId;
                    if(vm.clazzActivityMap.hasOwnProperty(clazzGroupId)){
                        vm.activityList = vm.clazzActivityMap[clazzGroupId] || [];
                    }else{
                        $.get("/ancient/poetry/activity/list.vpage",{
                            clazzGroupId : clazz.groupId,
                            clazzLevel : clazz.clazzLevel
                        }).done(function(data){
                            if(data.success){
                                var result = data.result || [];
                                var clazzGroupId = data.clazzGroupId;
                                // (clazz.groupId === clazzGroupId) && (vm.activityList = result);
                                vm.activityList = result;
                                !vm.clazzActivityMap.hasOwnProperty(clazzGroupId) && (vm.clazzActivityMap[clazzGroupId] = result);
                            }
                        }).fail(function(){

                        });
                    }
                },
                activityRegister : function(activityObj){
                    var vm = this;
                    if(!vm.clazz || activityObj.status !== "NOT_REGISTER"){
                        return false;
                    }

                    $17.voxLog({
                        module : "m_FeJejhY7pq",
                        op : "o_3fiVBsh9Vk",
                        s0 : vm.clazz.groupId,
                        s1 : activityObj.activityId
                    });

                    $.prompt("<div class='w-ag-center'><p>欢迎报名亲子诗词大会</p><p>请督促学生完成</p></div>", {
                        title   : "提示",
                        buttons : {"稍后参加":false,"确定参加":true},
                        position: { width: 400 },
                        loaded : function(){
                            $17.voxLog({
                                module : "m_FeJejhY7pq",
                                op : "o_X9Xokt8GpC"
                            });
                        },
                        submit : function(e,v,f,m){
                            $17.voxLog({
                                module : "m_FeJejhY7pq",
                                op : "o_t0BeLe2Ytf",
                                s0 : v ? "确定参加" : "稍后参加"
                            });
                            if(v){
                                var groupId = vm.clazz.groupId;
                                $.post("/ancient/poetry/activity/register.vpage",{
                                    activityId : activityObj.activityId,
                                    clazzGroupId : groupId
                                }).done(function(data){
                                    if(data.success){
                                        vm.$set(activityObj,'status',"REGISTERED");
                                        /*var groupActivityList = vm.clazzActivityMap[groupId];
                                        for(var m = 0,mLen = groupActivityList.length; m < mLen; m++){
                                            var activityTemp = groupActivityList[m];
                                            if(activityTemp.activityId === activityObj.activityId){
                                                vm.$set(activityTemp,"status","REGISTERED");
                                                break;
                                            }
                                        }*/
                                    }else{
                                        $17.alert(data.info || "报名失败");
                                    }
                                }).fail(function(){
                                    $17.alert("网络错误，刷新重试");
                                });
                            }else{
                                $.prompt.close();
                            }
                        },
                        close   : function(){}
                    });
                }
            },
            created : function(){
                this.getClazzList();
            },
            mounted : function(){

            }
        });
    });
</script>