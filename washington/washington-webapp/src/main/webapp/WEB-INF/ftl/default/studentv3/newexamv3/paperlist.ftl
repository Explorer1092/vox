<#import '../layout/layout.ftl' as temp>
<@temp.page clazzName="" pageName="newexamv3_paperlist">
    <@sugar.capsule js=["vue"] css=["new_student.newexam"]/>
    <style type="text/css">
        [v-cloak]{
            display: none;
        }
    </style>
    <div class="t-paper-container" v-cloak id="t-paper-container">
        <ul class="p-list">
            <li class="p-item" v-for="(paper,index) in paperList">
                <div class="float-l">
                    <div>
                        <i class="t-tag" v-bind:class="{'t-unit':paper.paperType == 25,'t-section' : paper.paperType == 26}" v-text="paper.typeName">课时小测</i>
                        <span class="t-title" v-text="paper.title">黄岗小状元第三单元第四节课贡网小状元第三单元第四节课</span>
                    </div>
                    <div class="t-desc"><span v-text="dateToString(paper.examStartAt) + '--' + dateToString(paper.examStopAt)">2月14日-3月18日</span> | <span v-text="'共' + paper.questionNum + '题'">共18题</span></div>
                </div>
                <div class="float-r">
                    <a class="t-btn" v-bind:class="{'disabled' : paper.status == 'undo'}" v-on:click="goExam(paper);" href="javascript:void(0);" v-text="getBtnText(paper.status)">开始检测</a>
                </div>
            </li>
        </ul>
        <div class="tips">所有考试信息可在“学习中心-检测”找到</div>
    </div>
<div class="valleyIcon01"></div>
<div class="valleyIcon02"></div>
<#if (ProductDevelopment.isDevEnv())!false>
    <#assign domain = "//www.test.17zuoye.net">
<#else>
    <#assign domain = "${requestContext.webAppBaseUrl}">
</#if>
<script type="text/javascript">
    $(function(){
        var env = <@ftlmacro.getCurrentProductDevelopment />;
        var imgDomain = "<@app.link_shared href='' />";
        var domain = "${domain}";
        new Vue({
           el : "#t-paper-container",
           data : {
               paperList : [],
               loading : true,
               info : "",
               env : env,
               imgDomain : imgDomain,
               domain : domain
           },
           methods : {
               dateToString : function(datetime){
                    return $17.dateToString(datetime,"MM月dd日");
               },
               getBtnText : function(status){
                   var btnText;
                   switch (status) {
                       case "todo":
                           btnText = "开始检测";
                            break;
                       case "doing":
                           btnText = "继续检测";
                           break;
                       default:
                           btnText = "未开始";
                           break;
                   }
                   return btnText;
               },
               fetchPaperList : function(){
                   var vm = this;
                   vm.loading = true;
                   $.get("/student/newexam/unit/test/index/list.vpage",{

                   }).done(function (res) {
                        if(res.success){
                            vm.paperList = res.unitTestList;
                            vm.loading = false;
                        }else{
                            vm.info = res.info || "请求接口失败";
                        }
                   }).fail(function(){
                        vm.loading = true;
                        vm.info = "网络出错，请刷新页面重试";
                   });
               },
               goExam : function(paper){
                   var vm = this;
                   switch (paper.status) {
                       case "todo":
                       case "doing":
                           $17.voxLog({
                               module : "m_a6xnFNmrZH",
                               op : "unittest_paperlist_start_click",
                               s1 : paper.typeName

                           });
                           if(!!navigator.userAgent.match(/AppleWebKit.*Mobile.*/)){
                               var goHome = function(){ location.href="/student/index.vpage"; return false; };
                               $17.alert("请使用电脑打开浏览器或下载一起小学学生APP完成考试哦~", goHome, goHome);
                           }else{
                               setTimeout(function(){
                                   location.replace(vm.domain + "/resources/apps/hwh5/funkyexam/V1_0_0/index.vhtml?server_type=" + vm.env + "&id=" + paper.examId + "&img_domain=" + vm.imgDomain);
                               },1);
                           }
                           break;
                       default:
                           break;
                   }
                   return false;
               }
           },
           created : function(){
               this.fetchPaperList();
               $17.voxLog({
                   module : "m_a6xnFNmrZH",
                   op : "unittest_paperlist_load"
               });
           }
        });
    });
</script>
</@temp.page>

