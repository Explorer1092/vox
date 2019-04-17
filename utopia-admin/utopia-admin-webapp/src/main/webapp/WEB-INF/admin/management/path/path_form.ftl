<#--app = forms.ChoiceField()
    name_zh = forms.CharField(label=u'名称')
    name_en = forms.CharField(label=u'标识ID',help_text=u"唯一标识ID，有效值：字母 数字 - _ .", max_length=80, validators=[isNoSpace, validate_ldapid])

    def __init__(self, uid, *args, **kwargs):
        super(PathForm, self).__init__(*args, **kwargs)
        self.root_app = get_choices_app(uid)
        self.fields['app'] = forms.ChoiceField(label=u'业务系统App', choices=self.root_app)

    def clean_name_en(self):
        name_en = self.cleaned_data.get("name_en", "")
        app = self.cleaned_data.get("app", "")
        if app == '':
            self._errors["app"] = self.error_class([u'必填项'])
            return name_en

        with LDAPClient.open() as ldapClient:
            if ldapClient.hasOU(name_en, app):
                raise forms.ValidationError(u"所选 业务系统 中已存在相同的路径标识！")-->

<div class="control-group <#if errorMessage??>error</#if>">
    <label class="control-label">名称</label>
    <div class="controls">
        <input type="text" name="pathDescription" value="${pathDescription!}" required="true" />
        <span class="help-inline">${errorMessage!''}中文描述</span>
    </div>
</div>
<div class="control-group <#if errorMessage??>error</#if>">
    <label class="control-label">标识ID</label>
    <div class="controls">
        <input type="text" name="pathName" value="${pathName!}" maxlength="80" required="true" />
        <span class="help-inline">${errorMessage!''} 唯一标识ID，有效值：字母 数字 / - _ .</span>
    </div>
</div>
<div class="control-group <#if errorMessage??>error</#if>">
    <label class="control-label">业务系统App</label>
    <div class="controls">
        <#if appList??>
        <select name="appName" required="true">
            <#list appList as appItem>
            <option value="${appItem.appName!}" <#if appItem.appName==appName >selected="selected"</#if>  >${appItem.appDescription!}</option>
            </#list>
        </select>
        </#if>
        <span class="help-inline">${errorMessage!''}</span>
    </div>
</div>