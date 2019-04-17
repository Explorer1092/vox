<#import "module.ftl" as center>
<@center.studentCenter currentPage='information'>
<div class="t-center-box w-fl-right">
    <div class="t-center-data">
        <div class="t-center-title">我的资料</div>
        <div class="t-center-information">
            <div class="tc-person-single">
                <div class="baseinfo-box">
                    <p class="label">我的姓名</p>
                    <p class="info">${(currentUser.profile.realname)!''}</p>
                    <p class="tip">* 如果姓名有错误请联系你的老师进行修改</p>
                </div>
            </div>
            <div class="tc-person-single">
                <div class="baseinfo-box">
                    <p class="label">所在学校</p>
                    <p class="info">${(currentStudentDetail.studentSchoolName)!}</p>
                    <p class="tip">* 学校信息不能随意修改，有问题请致电400-160-1717</p>
                </div>
            </div>
            <div class="tc-person-single">
                <div class="baseinfo-box">
                    <p class="label">考试填涂号</p>
                    <p class="info">${(scanNumber)!'无填涂号'}</p>
                    <div class="edit-box accountBut" data-box_type="scannumber">
                        <span>编辑</span>
                        <i></i>
                    </div>
                </div>
                <div class="modify-box accountBox" data-box_type="scannumber" style="display: none">
                    <div class="modify-onebox validateScanNumber">
                        <label for="">填涂号：</label>
                        <input id="input-scan-number" class="single-input w-int require" type="text" data-label="填涂号" value="${(scanNumber)!''}" placeholder="请输入填涂号">
                        <span class="w-form-misInfo w-form-info-error"></span>
                    </div>
                    <div class="sure-box" id="submit_information_update_scannumber">确定</div>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        //根据updateType  展开对应的修改/设置选择框
        <#if updateType?has_content >
            var button = $(".accountBut[data-box_type=${updateType}]");
            var buttonTop = button.offset().top;
            $('html, body').animate({scrollTop: buttonTop - 50}, 1000);
            setTimeout(function(){button.trigger('click');},1100);
        </#if>

        //数据提交
        $("#submit_information_update_scannumber").on('click', function () {
            // 极算只传填涂号即可
            var scanNumber = $.trim($('#input-scan-number').val());
            var success = validate("div[data-box_type='scannumber'] .validateScanNumber");

            if (success) {
                $.post('/student/center/saveprofiledata.vpage', {
                    scanNumber: scanNumber
                }, function (data) {
                    if (data.success) {
                        $17.tongji('个人中心-我的资料-确定');
                        $17.alert('修改成功！', function () {
                            setTimeout(function () {
                                location.reload();
                            }, 200);
                        });
                    } else {
                        $17.alert(data.info);
                    }
                });
            }
        });

        //根据选择类型  展开对应的输入框
        $(".accountBut").on('click', function(){
            var boxType = $(this).data('box_type');
            foldCard(boxType);
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