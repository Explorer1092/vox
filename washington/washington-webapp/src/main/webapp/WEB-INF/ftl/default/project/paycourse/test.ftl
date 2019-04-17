<#import '../../parentmobile/layout.ftl' as layout>
<@layout.page>
<@sugar.capsule js=['jquery'] />
<a href="javascript:void(0)" class="homework">去布置作业</a><br />
<a href="javascript:void(0)" class="clazz">去班级管理</a>
<a href="a17parent://platform.open.api/parent_main">跳转</a>
<script>
    $(".homework").on("click",function(){
        try {
            if(window['external'] && window.external['goArrangeHW']){
                window.external.goArrangeHW();
            }else{
                alert('Fail!');
            }
        }catch(e) {
            alert(JSON.stringify(e));
        }
    });
    $(".clazz").on("click",function(){
        try {
            if(window['external'] && window.external['goClazzManage']){
                window.external.goClazzManage();
            }else{
                alert('Fail!');
            }
        }catch(e) {
            alert(JSON.stringify(e));
        }
    });
</script>
</@layout.page>