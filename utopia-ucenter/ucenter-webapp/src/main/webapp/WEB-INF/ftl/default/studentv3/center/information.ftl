<#import "module.ftl" as center>
<@center.studentCenter currentPage='information'>
<div class="t-center-box w-fl-right">
    <span class="center-rope"></span>
    <span class="center-rope center-rope-1"></span>

    <div class="t-center-data">
        <div class="t-center-information">
            <div class="w-form-table">
                <dl>
                    <dt>姓　　名：</dt>
                    <dd>
                        <#if (currentUser.profile.realname)?has_content>
                        ${(currentUser.profile.realname)!''}
                        <#else>
                        ${(currentUser.id)!}
                        </#if>
                        <span class="w-hook"></span><span class="w-orange">如果姓名有错误请联系你的老师进行修改</span>
                    </dd>
                    <dt>性　　别：</dt>
                    <dd id="genderBox">
                        <span data-gender_info='M' class="w-spot w-radio-1 <#if (currentUser.profile.gender) == "M"> w-radio-1-current </#if>"></span>男
                        <span data-gender_info='F' class="w-spot w-radio-1 <#if (currentUser.profile.gender) == "F"> w-radio-1-current </#if> "></span>女
                    </dd>
                    <dt>出生日期：</dt>
                    <dd id="dataDefault">
                        <select id="date_of_birth_1i" name="date_of_birth[1i]" class="w-int" style="width: 130px;">
                            <option value="0">&nbsp;&nbsp;</option>
                            <#list 2000..2013 as y>
                                <option value="${y}" <#if (currentUser.profile.year?? && currentUser.profile.year == y) || (!currentUser.profile.year?? && y == 2007)>selected</#if>>${y}</option>
                            </#list>
                        </select>
                        <select id="date_of_birth_2i" name="date_of_birth[2i]" class="w-int" style="width: 130px;">
                            <option value="0">&nbsp;&nbsp;</option>
                            <#list 1..12 as m>
                                <option value="${m}" <#if currentUser.profile.month?? && currentUser.profile.month == m>selected</#if>>${m}</option>
                            </#list>
                        </select>
                        <select id="date_of_birth_3i" class="w-int" name="date_of_birth[3i]" style="width: 130px;">
                            <option value="0">&nbsp;&nbsp;</option>
                            <#list 1..31 as d>
                                <option value="${d}" <#if currentUser.profile.day?? && currentUser.profile.day == d>selected</#if>>${d}</option>
                            </#list>
                        </select>
                    </dd>
                    <dt>QQ 号码：</dt>
                    <dd><input id="userQQ" class="w-int" type="text" placeholder="" value="${(currentUserProfileQq)!''}"></dd>
                    <dt>学　　校：</dt>
                    <dd>${(currentStudentDetail.studentSchoolName)!}</dd>
                    <dt>班　　级：</dt>
                    <dd>${(currentStudentDetail.clazzLevel.description)!}${(currentStudentDetail.clazz.className)!}</dd>
                    <dd class="form-btn center">
                        <a id="submit_information_update_but" class="w-btn-dic w-btn-green-new" href="javascript:void(0);">确定</a>
                    </dd>
                </dl>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        var oldQq = "${currentUserProfileQq!''}";

        //性别选择
        $("#genderBox span").on('click', function () {
            $(this).addClass('w-radio-1-current').siblings().removeClass('w-radio-1-current');
        });

        //数据提交
        $("#submit_information_update_but").on('click', function () {
            var gender = $("#genderBox span.w-radio-1-current").data('gender_info');
            var year = $("#date_of_birth_1i").val();
            var month = $("#date_of_birth_2i").val();
            var day = $("#date_of_birth_3i").val();
            var QQ = $('#userQQ').val();

            // 只有在对号码进行修改的基础上才校验
            if(QQ != oldQq && !$17.isNumber(QQ)){
                $17.alert("QQ号码请填写数字。");
                return false;
            }

            if(QQ.length > 50){
                $17.alert("你输入的QQ号码过长。");
                return false;
            }

            $.post('/student/center/saveprofiledata.vpage', {gender: gender, year: year, month: month, day: day, qq: QQ}, function (data) {
                if (data.success) {
                    $17.tongji('个人中心-我的资料-确定');
                    $17.alert('修改成功！', function () {
                        setTimeout(function () {
                            location.reload();
                        }, 200);
                    });
                } else {
                    $17.alert('数据修改失败，请重试。');
                }
            });

        });

    });

    /*!
    * jQuery simpledateselect Plugin v1.0.0
    */
    var DateSelect = (function () {
        var updateOptions;

        updateOptions = function (select, options, daysInMonth) {
            var diff, i, value;
            diff = options.length - daysInMonth;

            if (diff > 0) {
                options.slice(daysInMonth).remove();
            } else if (diff < 0) {
                for (i = options.length; i < daysInMonth; i += 1) {
                    value = i + 1;
                    $("<option>").attr("value", value).text(value).appendTo(select);
                }
            }
        };

        return {
            change: function () {
                $("select[id$='_1i']").each(function () {
                    var day, month, year, days, daysInMonth, options;

                    year = $("#" + this.id);
                    month = $("#" + this.id.replace(/1i$/, "2i"));
                    day = $("#" + this.id.replace(/1i$/, "3i"));
                    days = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

                    if (parseInt($(month).val(), 10)) {
                        if (parseInt($(year).val(), 10)) {
                            if ($(month).val() === "2" && ($(year).val() % 4) === 0) {
                                days[1] += 1;
                            }
                        }

                        options = $(day).children("option[value!='0']");
                        daysInMonth = days[$(month).val() - 1];
                        updateOptions(day, options, daysInMonth);
                    }
                });
            }
        };
    }());

    $(function () {
        $(document).bind("date-select", function () {
            DateSelect.change();
        });
        $(document).trigger("date-select");

        $("#dataDefault select").change(function () {
            $(document).trigger("date-select");
        });
    });
</script>

</@center.studentCenter>