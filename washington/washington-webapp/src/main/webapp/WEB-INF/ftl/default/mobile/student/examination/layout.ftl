<#assign env = "student_examination">
<#include "../../common/config.ftl">

<#import '../../common/layout.ftl' as layout>
<#assign layout = layout>

<#assign headBlock>
    ${buildLoadStaticFileTag(CONFIG.SKINCSSPATH, "css")}
</#assign>

<#assign bottomBlock>
	<#assign bottomBlock>
        ${buildLoadStaticFileTagByList(
            [
                {
                    "path" : "${DEFAULT_CONFIG.js.reqwest}",
                    "type" : "js",
                    "addKid" : false
                },
                {
                    "path" : "${DEFAULT_CONFIG.js.vue}",
                    "type" : "js",
                    "addKid" : false
                },
                {
                    "path" : "${CONFIG.MAINJS}",
                    "type" : "js"
                }
            ]
        )}
	</#assign>
</#assign>
