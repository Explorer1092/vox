<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:Lesson xmlns:ns2="http://jaxb.vlesson.vlt.com">
<#if lesson?exists>
    <classLevel>${classLevel!''}</classLevel>
    <gameTime>${gameTime!''}</gameTime>
    <lessonId>${lesson.id!''}</lessonId>
    <lessonName>${(lesson.ename!'')?html}</lessonName>
    <#list sentences as sent>
        <sentence>
            <sentenceId>${sent.id!''}</sentenceId>
            <Data>
                <cn>${(sent.cnText!'')?html}</cn>
                <en>${(sent.enText!'')?html}</en>
                <metadata>${sent.metadata!''}</metadata>
                <waveFileLocator><@app.wave href="${sent.waveUri!''}" /></waveFileLocator>
                <slowWaveFileLocator><@app.wave href="${sent.slowWaveUri!''}" /></slowWaveFileLocator>
                <#if (sent.sentenceImg)?exists && sent.sentenceImg != 'wordstock/wordimg/web/' >
                    <pic><@app.wave href="${sent.sentenceImg!''}" /></pic>
                </#if>
                <#if (sent.sentenceParaphrases)?exists && sent.sentenceParaphrases?size gt 0 >
                    <tips>
                        <#list sent.sentenceParaphrases as sent>
                            <tip>${sent}</tip>
                        </#list>
                    </tips>
                </#if>

            </Data>
        </sentence>
    </#list>
<#else>
    <lessonId>null</lessonId>
</#if>
</ns2:Lesson>