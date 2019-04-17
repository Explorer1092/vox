<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="学校基础数据变更">
    <@sugar.capsule css=['school','photo_pic']/>
    <style>
    .school_length input{
        width:10%;
        font-size:.75rem;
        text-align:center;
    }
    .school_length span{
        margin-left:13%;
    }
    .school_length ul li {
        width:100%;
        text-align:center;
    }
    .nav.tab-head.c-flex.c-flex-5{
        display: none;
    }
</style>
<#if diffData??>
    <#list diffData?keys as key >
        <#assign data = diffData[key]>
        <#if (data.eduSystem)?? >
            <#if key == "newData">
                <div style="font-size:.65rem;padding:.5rem .5rem 0 .5rem ;">
                    操作人：${data.recorderName!""}<div style="float:right">变更时间：${data.updateTime}</div>
                    <div style="clear: both;">变更后：</div>
                </div>
            </#if>
            <#if key == "oldData">
                <div style="font-size:.65rem;padding:.5rem .5rem 0 .5rem ;">
                    变更前：
                </div>
            </#if>
            <div class="mobileCRM-V2-list schoolParticular-edit contain_${key}">
                <div class="edit-title">年级分布 <span>（请准确填写各年级班人数和班级数）</span></div>
                <div class="school_length edit-list">
                    <#if data.eduSystem =="P5">
                        <ul class="showLength01 schoolGrade">
                            <li><div class="level" data_value="1">小学一年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade1ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade1StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="2">小学二年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade2ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade2StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="3">小学三年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade3ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade3StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="4">小学四年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade4ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade4StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="5">小学五年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade5ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade5StudentCount!''}"/>人</div></li>
                            <li><div>合计</div><div>共<input value="${schoolClass1!''}" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled" value="${schoolMain1!''}">人</div></li>
                        </ul>
                    <#elseif data.eduSystem =="P6">
                        <ul class="showLength02 schoolGrade">
                            <li><div class="level" data_value="1">小学一年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade1ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade1StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="2">小学二年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade2ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade2StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="3">小学三年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade3ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade3StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="4">小学四年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade4ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade4StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="5">小学五年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade5ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade5StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="6">小学六年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade6ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade6StudentCount!''}"/>人</div></li>
                            <li><div>合计</div><div>共<input value="${schoolClass2!''}" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled" value="${schoolMain2!''}">人</div></li>
                        </ul>
                    </#if>
                    <#if data.eduSystem =="J4">
                        <ul class="showLength04 schoolGrade">
                            <li><div class="level" data_value="6">六年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade6ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade6StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="7">七年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade7ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade7StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="8">八年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade8ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade8StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="9">九年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade9ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade9StudentCount!''}"/>人</div></li>
                            <li><div>合计</div><div>共<input value="${schoolClass3!''}" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled" value="${schoolMain3!''}">人</div></li>
                        </ul>
                    <#elseif data.eduSystem =="J3">
                        <ul class="showLength03 schoolGrade">
                            <li><div class="level" data_value="7">七年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade7ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade7StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="8">八年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade8ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade8StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="9">九年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade9ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade9StudentCount!''}"/>人</div></li>
                            <li><div>合计</div><div>共<input value="${schoolClass4!''}" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled" value="${schoolMain4!''}">人</div></li>
                        </ul>
                    </#if>
                    <#if data.eduSystem =="S3">
                        <ul class="showLength05 schoolGrade">
                            <li><div class="level" data_value="11">高一</div><div>共<input disabled="disabled" class="banClass" type="tel" value="${data.newGrade11ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" type="tel" value="${data.grade11StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="12">高二</div><div>共<input disabled="disabled" class="banClass" type="tel" value="${data.newGrade12ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" type="tel" value="${data.grade12StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="13">高三</div><div>共<input disabled="disabled" class="banClass" type="tel" value="${data.newGrade13ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" type="tel" value="${data.grade13StudentCount!''}"/>人</div></li>
                            <li><div>合计</div><div>共<input value="${schoolClass5!''}" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled" value="${schoolMain5!''}">人</div></li>
                        </ul>
                    <#elseif data.eduSystem =="S4">
                        <ul class="showLength05 schoolGrade">
                            <li><div class="level" data_value="9">九年级</div><div>共<input disabled="disabled" class="banClass" value="${data.newGrade9ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" value="${data.grade9StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="11">高一</div><div>共<input disabled="disabled" class="banClass" type="tel" value="${data.newGrade11ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" type="tel" value="${data.grade11StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="12">高二</div><div>共<input disabled="disabled" class="banClass" type="tel" value="${data.newGrade12ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" type="tel" value="${data.grade12StudentCount!''}"/>人</div></li>
                            <li><div class="level" data_value="13">高三</div><div>共<input disabled="disabled" class="banClass" type="tel" value="${data.newGrade13ClassCount!''}"/>班</div><div>共<input disabled="disabled" class="allMan" type="tel" value="${data.grade13StudentCount!''}"/>人</div></li>
                            <li><div>合计</div><div>共<input value="${schoolClass7!''}" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled" value="${schoolMain7!''}">人</div></li>
                        </ul>
                    </#if>
                    <#if data.eduSystem =="I4">
                        <#assign schoolMain6 = (data.studentCount51!0)?int + (data.studentCount52!0)?int + (data.studentCount53!0)?int+ (data.studentCount54!0)?int>
                        <#assign schoolClass6 =  (data.classCount51!0)?int + (data.classCount52!0)?int + (data.classCount53!0)?int + (data.classCount54!0)?int >
                        <ul class="showLength05 schoolGrade">
                            <li><div class="level" data_value="51">小班</div><div>共<input disabled="disabled" class="banClass" type="tel" value="${data.classCount51!''}"/>班</div><div>共<input disabled="disabled" class="allMan" type="tel" value="${data.studentCount51!''}"/>人</div></li>
                            <li><div class="level" data_value="52">中班</div><div>共<input disabled="disabled" class="banClass" type="tel" value="${data.classCount52!''}"/>班</div><div>共<input disabled="disabled" class="allMan" type="tel" value="${data.studentCount52!''}"/>人</div></li>
                            <li><div class="level" data_value="53">大班</div><div>共<input disabled="disabled" class="banClass" type="tel" value="${data.classCount53!''}"/>班</div><div>共<input disabled="disabled" class="allMan" type="tel" value="${data.studentCount53!''}"/>人</div></li>
                            <li><div class="level" data_value="54">学前班</div><div>共<input disabled="disabled" class="banClass" type="tel" value="${data.classCount54!''}"/>班</div><div>共<input disabled="disabled" class="allMan" type="tel" value="${data.studentCount54!''}"/>人</div></li>
                            <li><div>合计</div><div>共<input value="${schoolClass6!''}" class="classGrade" disabled="disabled">班</div><div>共<input class="gradeNum" disabled="disabled" value="${schoolMain6!''}">人</div></li>
                        </ul>
                    </#if>
                </div>
            </div>
        </#if>
    </#list>
</#if>
<script>
    $(document).ready(function(){
        var banClass1 = 0;
        for(var i=0;i<$('.contain_oldData .banClass').length;i++){
            if($('.contain_oldData .banClass').eq(i).val() != ""){
                banClass1 += parseFloat($('.contain_oldData .banClass').eq(i).val());
            }
        }
       $('.contain_oldData .classGrade').val(banClass1);
        var banClass2 = 0;
        for(var i=0;i<$('.contain_oldData .allMan').length;i++){
            if($('.contain_oldData .allMan').eq(i).val() != ""){
                banClass2 += parseFloat($('.contain_oldData .allMan').eq(i).val());
            }
        }
        $('.contain_oldData .gradeNum').val(banClass2);

        var banClass3 = 0;
        for(var i=0;i<$('.contain_newData .banClass').length;i++){
            if($('.contain_newData .banClass').eq(i).val() != ""){
                banClass3 += parseFloat($('.contain_newData .banClass').eq(i).val());
            }
        }
        $('.contain_newData .classGrade').val(banClass3);

        var banClass4 = 0;
        for(var i=0;i<$('.contain_newData .allMan').length;i++){
            if($('.contain_newData .allMan').eq(i).val() != ""){
                banClass4 += parseFloat($('.contain_newData .allMan').eq(i).val());
            }
        }
        $('.contain_newData .gradeNum').val(banClass4);
    })
</script>
</@layout.page>
