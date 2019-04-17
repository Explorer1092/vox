<@sugar.capsule js=["datepicker"] css=["plugin.datepicker"] />
<script type="text/html" id="T:收集寒假时间来源渠道POPUP">
    <style>
        .gatherWinVacation{margin-top: -40px;}
        .gatherWinVacation .title{height: 34px; font-size: 18px;line-height: 34px;background-color: #f2eaa4;text-align: center; width: 500px;margin-left: -20px;}
        .gatherWinVacation .tip{text-align: center; margin-top: 10px; }
        .gatherWinVacation .w-form-table{ margin-left: 19px;}
        .gatherWinVacation .w-form-table dt{width:35%; text-align: left;}
        .gatherWinVacation .alertInfo{color: red; font-size: 10px;padding-left: 35%;display: none;}
        .jqidefaultbutton{ margin-top: -35px !important;}
    </style>
    <div class = "gatherWinVacation">
        <p class="title">寒假时间小调查</p>
        <p class="tip">诚邀您参与调查，如您暂不清楚假期时间，可点击右上角关闭</p>
        <div class="w-form-table">
            <div id="applt-hd">
                <dl style="margin-bottom: 30px; margin-top: 10px;" >
                    <dt style="display: ;">1、本学期期末考时间</dt>
                    <dd style="display:;">
                        <input id="datepicker1" name="birthday" value="" class="w-int"/>
                    </dd>
                </dl>
                <dl>
                    <dt style="display: ;">2、下学期开学时间</dt>
                    <dd style="display: ;">
                        <input id="datepicker2" name="birthday" value="" class="w-int"/>
                    </dd>
                </dl>
            </div>
            <div class="alertInfo">请选择时间！</div>
        </div>
    </div>
</script>
<script type="text/javascript">
    (function($){
        function gatherWinVacation(){
            if(!$17.getCookieWithDefault("winvac")){
                $.prompt(template("T:收集寒假时间来源渠道POPUP", {}),{
                    title  : '系统提示',
                    buttons : {"确定" : true},
                    close : function(){
                        $17.setCookieOneDay("winvac", "7" , 7);
                    },
                    loaded : function(){
                        $(".jqidefaultbutton").addClass("w-btn-disabled");
                        $( "#datepicker1" ).datepicker({
                            dateFormat: 'yy-mm-dd',
                            yearRange: '1900:2050',
                            monthNamesShort:['01','02','03','04','05','06','07','08','09','10','11','12'],
                            changeMonth: true,
                            changeYear: true
                        });

                        $( "#datepicker2" ).datepicker({
                            dateFormat: 'yy-mm-dd',
                            yearRange: '1900:2050',
                            monthNamesShort:['01','02','03','04','05','06','07','08','09','10','11','12'],
                            changeMonth: true,
                            changeYear: true
                        });
                        $(document).on("change", "#datepicker1, #datepicker2", function(){
                            if($("#datepicker1").val()!="" && $("#datepicker2").val()!=""){
                                $(".alertInfo").hide();
                                $(".jqidefaultbutton").removeClass("w-btn-disabled");
                            }
                        });
                    },
                    submit: function(e, v){
                        if(v){
                            if($("#datepicker1").val() == "" || $("#datepicker2").val() == ""){
                                $(".alertInfo").show();
                                return false;
                            }

                            $17.setCookieOneDay("winvac", "30" , 30);
                            $17.voxLog({
                                app : "teacher",
                                module : "schoolTimeToGather",
                                op : "gather",
                                thisSemesterTime : $("#datepicker1").val(),
                                nextSemesterTime : $("#datepicker2").val()
                            });
                            setTimeout(function(){
                                $17.alert("感谢参与，一起作业将为您做到更好");
                            }, 200);
                        }
                    }
                });
            }
        }

        $.extend($, {
            gatherWinVacation : gatherWinVacation
        });
    }($));
</script>