<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="添加学校" page_num=3>
<div id="main_container" class="span9">
    <div>
        <form action="addschool.vpage" method="post" class="form-horizontal">
            <legend>添加学校</legend>
            <ul class="inline">
                <li>
                    <label for="provinces">
                        所在省份：
                        <select id="provinces" name="provinces" class="multiple district_select" next_level="citys">
                            <option value="-1">全国</option>
                            <#if provinces??>
                                <#list provinces as p>
                                    <option value="${p.key}">${p.value}</option>
                                </#list>
                            </#if>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="citys">
                        所在城市：
                        <select id="citys" data-init='false' name="citys" class="multiple district_select" next_level="countys">
                            <option value="-1">全部</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="countys">
                        所在区域：
                        <select id="countys" data-init='false' name="countys" class="multiple district_select">
                            <option value="-1">全部</option>
                        </select>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <!--
                    <li>
                        <label>
                            学校名称：
                            <input id="cname" name="cname" type="text"/>
                        </label>
                    </li>
                -->
                <li>
                    <label>
                       主干名称：
                       <input id="cmainname" name="cmainname" type="text"/>
                    </label>
                </li>
                <li>
                    <label>
                       校区名称：
                       <input id="schooldistrict" name="schooldistrict" type="text"/>
                    </label>
                </li>
                <li>
                    <label>
                        学校简称：
                        <input id="shortname" name="shortname" type="text" />
                    </label>
                </li>
            </ul>
            <#--<ul class="inline">-->
                <#--<li>-->
                    <#--<label>-->
                        <#--批量增加学校：--> <#--CrmSchoolController中的addSchool控制代码已经做过修改-->
                        <#--<input id="cname_list" name='cname_list' type="text" placeholder="各名称请以','分隔"/>-->
                    <#--</label>-->
                <#--</li>-->
            <#--</ul>-->
            <ul class="inline">
                <li>
                    <label>
                        vip等级：&nbsp;&nbsp;
                        <select id="vip" name="vip">
                            <option value="1">重点学校</option>
                            <option value="2" selected>非重点学校</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="authenticationState">
                        鉴定状态：
                        <select id="authenticationState" name="authenticationState" class="multiple">
                            <option value="0" selected>待鉴定</option>
                        </select>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label for="level">
                        学校级别：
                        <select id="level" name="level" class="multiple" onchange="syncEduSystem(this[selectedIndex].value)">
                            <option value="5">学前</option>
                            <option value="1" selected>小学</option>
                            <option value="2">初中</option>
                            <option value="4">高中</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="type">
                        学校类型：
                        <select id="type" name="type" class="multiple">
                            <option value="1" selected>公立制学校</option>
                            <option value="2">自定义学校</option>
                            <option value="3">私立学校</option>
                            <option value="5">培训机构</option>
                            <option value="4">虚拟学校</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="eduSystem">
                        学校学制：
                        <select id="eduSystem" name="eduSystem" class="multiple">
                            <option value="I4">学前四年制</option>
                            <option value="P5">小学五年制</option>
                            <option value="P6" selected>小学六年制</option>
                            <option value="J3">初中三年制</option>
                            <option value="J4">初中四年制</option>
                            <option value="S3">高中三年制</option>
                            <option value="S4">高中四年制(1+3模式)</option>
                        </select>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label for="schoolDesc">
                        问题描述：
                        <textarea id="schoolDesc" name="schoolDesc" cols="200" rows="5"></textarea>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <button type="submit" class="btn btn-primary">提 交</button>
                </li>
                <li>
                    <button type="reset" class="btn">重 置</button>
                </li>
            </ul>
        </form>
    </div>
    <div>
        <table id="schools" class="table table-hover table-striped table-bordered">
            <tr>
                <th>学校ID</th>
                <th>学校名称</th>
                <th>所在地区</th>
                <th>地区编号</th>
                <th>认证状态</th>
                <th>学校级别</th>
                <th>vip等级</th>
                <th>学校类型</th>
                <th>学校学制</th>
            </tr>
                <#if schoolSnapShotList?has_content>
                    <#list schoolSnapShotList as schoolSnapShot>
                        <tr>
                            <td>${schoolSnapShot.schoolId!''}</td>
                            <td><a href="schoolhomepage.vpage?schoolId=${schoolSnapShot.schoolId!''}">${schoolSnapShot.schoolName!''}</a></td>
                            <td>${schoolSnapShot.regionName!''}</td>
                            <td>${schoolSnapShot.regionCode!''}</td>
                            <td>${schoolSnapShot.authenticationState!''}</td>
                            <td>${schoolSnapShot.schoolLevel!''}</td>
                            <td>${schoolSnapShot.vipLevel!''}</td>
                            <td>${schoolSnapShot.schoolType!''}</td>
                            <td>${schoolSnapShot.eduSystem!''}</td>
                        </tr>
                    </#list>
                </#if>
        </table>
    </div>
</div>

<#-------------------existschoo dialog------------------->
<div id="dialog-confirm" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>确认相似学校</h3>
    </div>
    <div class="modal-body">
        <table>
            <tr>
                <td>学校ID</td>
                <td>学校名称</td>
            </tr>
            <#if existSchoolMap??&& existSchoolMap?has_content>
                <#list existSchoolMap?keys as schoolId>
                    <tr>
                        <td>${schoolId}</td>
                        <td><a target="_blank" href="schoolhomepage.vpage?schoolId=${schoolId!}">${existSchoolMap[schoolId]!''}</a></td>
                    </tr>
                </#list>
            </#if>
        </table>
        <form id="confirmAddSchool" name="confirmAddSchool" action="addschool.vpage" method="post">
            <input id="sessionKey" name="sessionKey" type="hidden" value="${sessionKey!''}">
            <div class="modal-footer">
                <button id="delete_class_dialog_btn_ok" type="submit" class="btn btn-primary">仍然添加</button>
                <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
            </div>
        </form>
    </div>

<script>
    function clearNextLevel(obj) {
        if (obj.attr("next_level")) {
            clearNextLevel($("#" + obj.attr("next_level")).html('<option value=""></option>'));
        }
    }

    function syncEduSystem(level) {
        var eduSystemMap = {
            "1" : [{"sys":"P6", "desc":"小学六年制"}, {"sys":"P5", "desc":"小学五年制"}],
            "2" : [{"sys":"J3", "desc":"初中三年制"}, {"sys":"J4", "desc":"初中四年制"}],
            "4" : [{"sys":"S3", "desc":"高中三年制"}, {"sys":"S4", "desc":"高中四年制(1+3模式)"}],
            "5" : [{"sys":"I4", "desc":"学前四年制"}]
        };

        var eduSystems = eduSystemMap[level];

        var html = '';
        for (var i=0; i<eduSystems.length; ++i) {
            var eduSys = eduSystems[i];
            html += ("<option value='" + eduSys["sys"] +"'>" + eduSys["desc"]+"</option>");
        }
        $('#eduSystem').html(html);
    }

    $(function () {
        $(".district_select").on("change", function(){
            var html        = null;
            var $this       = $(this);
            var next_level  = $this.attr("next_level");
            var regionCode  = $this.val();
            if(next_level){
                var codeType = next_level;
                next_level = $("#" + next_level);
                clearNextLevel($this);
                $.ajax({
                    type : "post",
                    url : "regionlist.vpage",
                    data : {
                        regionCode : regionCode
                    },
                    success : function(data){
                        html = '';
                        var regionList = data.regionList;
                        for (var i in regionList) {
                            html += '<option value="' + regionList[i]["code"] + '">' + regionList[i]["name"] + '</option>';
                        }
                        next_level.html(html);
                        <#if conditionMap?has_content>
                            if(codeType == 'citys' && !next_level.data('init')){
                                next_level.val(${conditionMap.citys!'-1'});
                                next_level.data('init', true);
                            }else if (codeType == 'countys' && !next_level.data('init')){
                                next_level.val(${conditionMap.countys!'-1'});
                                next_level.data('init', true);
                            }
                        </#if>
                         next_level.trigger('change');
                    }
                });
            }
        });
        <#if conditionMap?has_content>
            $("#provinces").val(${conditionMap.provinces!"-1"});
            $('#provinces').trigger('change');
            $('#cmainname').val("${conditionMap.cmainname!''}");
            $('#schooldistrict').val("${conditionMap.schooldistrict!''}");
            $('#cname').val("${conditionMap.cname!''}");
            $('#shortname').val('${conditionMap.shortname!''}');
            $('#cname_list').val("");
            $('#vip').val("${conditionMap.vip!''}");
            $('#authenticationState').val(${conditionMap.authenticationState!''});
            $('#level').val(${conditionMap.level!''});
            $('#level').trigger('change');
            $('#type').val(${conditionMap.type!''});
            $('#schoolDesc').val("${conditionMap.schoolDesc!''}");
            $('#eduSystem').val("${conditionMap.eduSystem!''}");
            <#if existSchoolMap?has_content>
                $("#dialog-confirm").modal("show");
            </#if>
        </#if>
    });
</script>
</@layout_default.page>