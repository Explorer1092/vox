<#import "../layout_new_no_group.ftl" as layout>
<#assign groupName="work_record">
<@layout.page title="年级分布">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back();" class="headerBack">&lt;&nbsp;返回</a>
            <a href="javascript:void(0);" class="headerBtn">保存</a>
            <div class="headerText">年级分布</div>
        </div>
    </div>
</div>

<form action="save_grade_distribution.vpage" method="POST" id="save-grade-distribution" enctype="multipart/form-data">

    <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
        <ul class="mobileCRM-V2-list">
            <li class="js-gradeItem">
                <div class="link">
                    <div class="side-fl"><span class="side-noneGrade <#if (firstYearNum!0) gte 0> done</#if>"></span>一年级
                    </div>
                    <input type="tel" placeholder="人数" maxlength="4" class="side-fr side-time" name="firstYearNum"
                           id="firstYearNum" value="<#if firstYearNum?? >${firstYearNum!''}</#if>">
                </div>
            </li>
            <li class="js-gradeItem">
                <div class="link">
                    <div class="side-fl"><span class="side-noneGrade <#if (secondYearNum!0) gte 0> done</#if>"></span>二年级
                    </div>
                    <input type="tel" placeholder="人数" maxlength="4" class="side-fr side-time" name="secondYearNum"
                           id="secondYearNum" value="<#if secondYearNum?? >${secondYearNum!''}</#if>">
                </div>
            </li>
            <li class="js-gradeItem">
                <div class="link">
                    <div class="side-fl"><span class="side-noneGrade <#if (threeYearNum!0) gte 0> done</#if>"></span>三年级
                    </div>
                    <input type="tel" placeholder="人数" maxlength="4" class="side-fr side-time" name="threeYearNum"
                           id="threeYearNum" value="<#if threeYearNum?? >${threeYearNum!''}</#if>">
                </div>
            </li>
            <li class="js-gradeItem">
                <div class="link">
                    <div class="side-fl"><span class="side-noneGrade <#if (fourYearNum!0) gte 0> done</#if>"></span>四年级
                    </div>
                    <input type="tel" placeholder="人数" maxlength="4" class="side-fr side-time" name="fourYearNum"
                           id="fourYearNum" value="<#if fourYearNum?? >${fourYearNum!''}</#if>">
                </div>
            </li>
            <li class="js-gradeItem">
                <div class="link">
                    <div class="side-fl"><span class="side-noneGrade <#if (fiveYearNum!0) gte 0> done</#if>"></span>五年级
                    </div>
                    <input type="tel" placeholder="人数" maxlength="4" class="side-fr side-time" name="fiveYearNum"
                           id="fiveYearNum" value="<#if fiveYearNum?? >${fiveYearNum!''}</#if>">
                </div>
            </li>
            <li class="js-gradeItem">
                <div class="link">
                    <div class="side-fl"><span class="side-noneGrade <#if (sixYearNum!0) gte 0> done</#if>"></span>六年级
                    </div>
                    <input type="tel" placeholder="人数" maxlength="4" class="side-fr side-time" name="sixYearNum"
                           id="sixYearNum" value="<#if sixYearNum?? >${sixYearNum!''}</#if>">
                </div>
            </li>
            <li class="js-gradeItem">
                <div class="link">
                    <div class="side-fl"><span class="side-noneGrade <#if (sevenYearNum!0) gte 0> done</#if>"></span>七年级
                    </div>
                    <input type="tel" placeholder="人数" maxlength="4" class="side-fr side-time" name="sevenYearNum"
                           id="sevenYearNum" value="<#if sevenYearNum?? >${sevenYearNum!''}</#if>">
                </div>
            </li>
            <li class="js-gradeItem">
                <div class="link">
                    <div class="side-fl"><span class="side-noneGrade <#if (eightYearNum!0) gte 0> done</#if>"></span>八年级
                    </div>
                    <input type="tel" placeholder="人数" maxlength="4" class="side-fr side-time" name="eightYearNum"
                           id="eightYearNum" value="<#if eightYearNum??>${eightYearNum!''}</#if>">
                </div>
            </li>
            <li class="js-gradeItem">
                <div class="link">
                    <div class="side-fl"><span class="side-noneGrade <#if (nineYearNum!0) gte 0> done</#if>"></span>九年级
                    </div>
                    <input type="tel" placeholder="人数" maxlength="4" class="side-fr side-time" name="nineYearNum"
                           id="nineYearNum" value="<#if nineYearNum?? >${nineYearNum!''}</#if>">
                </div>
            </li>
        </ul>
    </div>
    <input type="hidden" name="schoolingLength" id="schoolingLength" value="${schoolingLength!''}">
    <input type="hidden" name="returnUrl" id="returnUrl" value="${returnUrl!''}">
</form>
<div class="mobileCRM-V2-info mobileCRM-V2-mt">
    注意: <br>
    该学校没有的年级填写“0”人<br>
</div>
<script>


    var gradeDistribution = {};
    function getRealTimeDetail() {
        gradeDistribution = {
            "firstYearNum": $("#firstYearNum").val(),
            "secondYearNum": $("#secondYearNum").val(),
            "threeYearNum": $("#threeYearNum").val(),
            "fourYearNum": $("#fourYearNum").val(),
            "fiveYearNum": $("#fiveYearNum").val(),
            "sixYearNum": $("#sixYearNum").val(),
            "sevenYearNum": $("#sevenYearNum").val(),
            "eightYearNum": $("#eightYearNum").val(),
            "nineYearNum": $("#nineYearNum").val()
        }
    }
    $(".headerBtn").click(function () {
        getRealTimeDetail();
        if (validate()) {
            $.post("save_grade_distribution.vpage", gradeDistribution, function (result) {
                if (result.success) {
                    window.location.href = "javascript:window.history.back();";
                } else {
                    alert(result.info)
                }
            })
            /*$("#save-grade-distribution").submit();*/
        }
    });

    function validate() {
        return true;
    }

    (function () {

        var scl = getQuery("schoolingLength");
        var modal = {
            "1": 5,
            "2": 6,
            "3": 3,
            "4": 4
        };
        var sclVal = modal[scl];
        var DivList = $(".js-gradeItem");

        if (sclVal == 5) {
            for (var i = 5; i < 9; i++) {
                $(DivList[i]).hide();
            }
        } else if (sclVal == 6) {
            for (var i = 6; i < 9; i++) {
                $(DivList[i]).hide();
            }
        } else if (sclVal == 3) {
            for (var i = 0; i < 6; i++) {
                $(DivList[i]).hide();
            }
        } else if (sclVal == 4) {
            for (var i = 0; i < 5; i++) {
                $(DivList[i]).hide();
            }
        }

        $(".side-time").on("change", function () {
            var val = $(this).val();
            if ((parseInt(val) >=0) && (parseInt(val) <= 5000)) {
                $(this).siblings('div.side-fl').find('span').addClass("done");
            } else {
                $(this).val("");
                $(this).siblings('div.side-fl').find('span').removeClass("done");
                if (val != 0) {
                    alert("请输入0到5000以内的数字");
                }
            }
        });
    })();


</script>
</@layout.page>