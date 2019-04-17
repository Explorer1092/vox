
<#assign
    CSSBASEPATH = "public/skin/mobile/"
    JSBASEPATH = "public/script/mobile/"
    COMMON_SKIN_PATH = CSSBASEPATH + "common/style/"

    _plugin_path = "public/plugin/"
>

<#function _build_module_base_path module base_path>

	<#return {
		"JS" : (JSBASEPATH + base_path + "/{{module}}/")?replace("{{module}}", module),
		"CSS" : CSSBASEPATH + base_path + "/{{module}}/style/"?replace("{{module}}", module)
	}>

</#function>

<#assign
	_base_path = {
		"student_junior"      : _build_module_base_path('junior', 'student'),
		"student_ugc"         : _build_module_base_path('ugc', 'student')
	}
>

<#assign
    ALL_CONFIG = {
        "default" : {
			"js" : {
				"dpi"       : "public/script/parentMobile/targetDensitydpi",
				"common"    : "public/script/mobile/common/main",
				"log"       : _plugin_path + "log",
				"jquery"    : _plugin_path + "jquery/jquery-1.9.1.min",
				"zepto"     : _plugin_path + "zepto/1.1.6/zepto.min",
				"requirejs" : _plugin_path + "requirejs/require.2.1.9.min",
				"vue"       : _plugin_path + "vue/1.0.16/vue.min",
				"reqwest"   : _plugin_path + "reqwest/2.0.5/reqwest.min",
				"fastClick" : _plugin_path + "fastClick"
			}
		},
        "student_junior" : {
            "CSSPRE"        : "student_junior",
			"JS_BASE_PATH"  : _base_path.student_junior.JS,
			"CSS_BASE_PATH" : _base_path.student_junior.CSS,
			"SKINCSSPATH"   : _base_path.student_junior.CSS + "skin"
        },
		"student_ugc" : {
			"CSSPRE"        : "student_ugc",
			"JS_BASE_PATH"  : _base_path.student_ugc.JS,
			"CSS_BASE_PATH" : _base_path.student_ugc.CSS,
			"SKINCSSPATH"   : _base_path.student_ugc.CSS + "skin",
			"MAINJS"        : _base_path.student_ugc.JS + "main"
		}
    }
	DEFAULT_CONFIG = ALL_CONFIG.default,
	CONFIG = ALL_CONFIG[env!"default"]
>

<#include "./constants.ftl">
