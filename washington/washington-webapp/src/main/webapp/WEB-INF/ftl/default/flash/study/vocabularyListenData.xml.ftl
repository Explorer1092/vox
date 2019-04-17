<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:Lesson xmlns:ns2="http://jaxb.vlesson.vlt.com">
<#if lesson?exists>
    <gameTime>${gameTime!''}</gameTime>
    <lessonId>${lesson.id!''}</lessonId>
    <lessonName>${(lesson.ename!'')?html}</lessonName>
    <#list sentences as sent>
        <sentence>
            <sentenceId>${sent.id}</sentenceId>
            <dialogRole>${sent.dialogRole!''}</dialogRole>
            <engData>
                <text>${(sent.enText?default(''))?html}</text>
                <waveFileLocator><@app.wave href="${sent.waveUri?default('')}"/></waveFileLocator>
                <wavePlayTime>${sent.wavePlayTime}</wavePlayTime>
            </engData>
            <chnData>
                <text0>${sent.qa.word1}</text0>
                <text1>${sent.qa.word2}</text1>
                <text2>${sent.qa.word3}</text2>
                <text3>${sent.qa.word4}</text3>
                <answer>${sent.qa.answer}</answer>
            </chnData>
        </sentence>
    </#list>
<#else>
    <lessonId>null</lessonId>
</#if>
</ns2:Lesson>