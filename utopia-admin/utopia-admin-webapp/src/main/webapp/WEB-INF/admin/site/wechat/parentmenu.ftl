<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div class="span9">
    <legend>
        <#if type=='parent'>
            微信家长端菜单
        <#elseif type=='chips'>
            薯条英语微信菜单
        <#elseif type=='teacher'>
            微信老师端菜单
        <#else>
            微信17学菜单
        </#if>
    </legend>
    <p>当前菜单(默认菜单+个性化菜单):</p>
    <pre id="menu"></pre>
    <br/>
    <p>在此输入默认菜单:</p>
    <div>
        <textarea id="defaultmenu" name="defaultmenu" style="width:450px;height:450px;" placeholder=""></textarea>
        <input id="btn_update_default_menu" class="btn" type="button" value="更新默认菜单"/>
    </div>
    <br/>
    <p>添加个性化菜单:</p>
    <div>
        <DIV id="pd0u202" style="" data-label="State1">
            <p>用户分组:
                <INPUT id="groupId" type=text placeholder="group_id" class="u205" data-label="textfield">(可选,用户分组id，可通过用户分组管理接口获取)
            </p>
            <p>地区:
                <INPUT id="country" type=text placeholder="国家" class="u205" data-label="textfield">
                <INPUT id="province" type=text placeholder="省" class="u205" data-label="textfield">
                <INPUT id="city" type=text placeholder="市" class="u205" data-label="textfield">(可选,具体请参考微信地区信息表)
            </p>
            <p>
                性别:
                <SELECT id="sl_gender" class="u206">
                    <OPTION selected value="">未选择</OPTION>
                    <OPTION value="1">男</OPTION>
                    <OPTION value="2">女</OPTION>
                </SELECT>(可选)
            </p>
            <p>
                手机操作系统:
                <SELECT id="sl_os" class="u206">
                    <OPTION selected value="">未选择</OPTION>
                    <OPTION value="1">IOS</OPTION>
                    <OPTION value="2">Andriod</OPTION>
                    <OPTION value="3">其它</OPTION>
                </SELECT>(可选)
            </p>

        </DIV>
        <textarea id="individualmenu" name="individualmenu" style="width:450px;height:450px;" placeholder=""></textarea>
        <input id="btn_add_individual_menu" class="btn" type="button" value="添加个性化菜单"
    </div>
    <p>删除个性化菜单:</p>
    <div>
        <input id="menuId" type="text" placeholder="menuId" name="menuId"/>
        <input id="btn_del_individual_menu" class="btn" type="button" value="删除个性化菜单"/>
    </div>
    <p>测试个性化菜单匹配:</p>
    <div>
        <input id="openId" type="text" placeholder="openId或微信号"/>
        <input id="btn_test_menu" class="btn" type="button" value="测试个性化菜单匹配"/>
        <pre id="testmenu"></pre>
    </div>

    <#if serverip??>
        <p>微信服务器信息:${serverip!""}</p>
        <p>token:${token!""}</p>
    </#if>
</div>
<script type="text/javascript">
    $(function () {
        $.post('getmenu.vpage', {type: '${type}'}, function (data) {
            if (data.success) {
                $('#menu').html(JSON.stringify(data.menu, null, "\t"));
            }
        });
    });

    $('#btn_update_default_menu').on('click', function () {
        var menu = $('#defaultmenu').val();
        if (menu.trim().length == 0) {
            alert('请输入默认菜单');
            return false;
        }

        $.post('setdefaultmenu.vpage', {type: '${type}', content: menu}, function (data) {
            console.log(data);
            if (data.success) {
                alert('操作成功');
                window.location.reload();
            } else {
                alert('操作失败,' + data.info);
                return false;
            }
        });
        console.log('set default menu.');
    });

    $('#btn_add_individual_menu').on('click', function () {
        var groupId = $('#groupId').val();
        var country = $('#country').val();
        var province = $('#province').val();
        var city = $('#city').val();
        var gender = $('#sl_gender').val();
        var os = $('#sl_os').val();
        var content = $('#individualmenu').val();

        if (groupId.trim().length == 0 && country.trim().length == 0 && province.trim().length == 0 && city.trim().length == 0
                && gender.trim().length == 0 && os.trim().length == 0) {
            alert('请选择匹配条件');
            return false;
        }

        if (content.trim().length == 0) {
            alert('请输入个性化菜单');
            return false;
        }

        $.post('setindividualparentmenu.vpage', {
            type: '${type}',
            groupId: groupId,
            country: country,
            province: province,
            city: city,
            gender: gender,
            os: os,
            content: content
        }, function (data) {
            console.log(data);
            if (data.success) {
                alert('操作成功');
                window.location.reload();
            } else {
                alert('操作失败,' + data.info);
                return false;
            }
        });

    });

    $('#btn_del_individual_menu').on('click', function () {
        var menuId = $('#menuId').val();
        if (menuId.trim().length == 0) {
            alert('请输入要删除的个性化菜单ID');
            return false;
        }

        $.post('delindividualparentmenu.vpage', {type: '${type}', menuId: menuId}, function (data) {
            if (!confirm('确定要删除个性化菜单' + menuId + '吗?')) {
                return false;
            }
            if (data.success) {
                alert('操作成功');
                window.location.reload();
            } else {
                alert('操作失败,' + data.info);
                return false;
            }
        });
    });

    $('#btn_test_menu').on('click', function () {
        var openId = $('#openId').val();
        if (openId.trim().length == 0) {
            alert('请输入openId或微信号');
            return false;
        }

        $.post('testindividualparentmenu.vpage', {type: '${type}', openId: openId}, function (data) {
            if (data.success) {
                $('#testmenu').html(JSON.stringify(data.menu, null, '\t'));
            } else {
                alert('操作失败,' + data.info);
                return false;
            }
        });
    });
</script>

</@layout_default.page>
