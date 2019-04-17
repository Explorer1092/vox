<#import "/common/config.ftl" as app>
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:Lesson xmlns:ns2="http://jaxb.vlesson.vlt.com">
    <result>
        <status>${status}</status>
        <errorinfo>${errorinfo!''}</errorinfo>
        <clazzSize>${clazzSize!0}</clazzSize>
        <sameClass>${sameClass!0}</sameClass>
    <#if status=='success' && user??>
        <userInfo>
            <userInfo>
                <userName>${(user.profile.realname)!}</userName>
                <userId>${user.id}</userId>
                <userRank>${userRank}</userRank>
                <userPhoto><@app.avatar href="${user.fetchImageUrl()!}"/></userPhoto>
            </userInfo>


            <#if inviteUser?exists>
                <inviteUserInfo>
                    <userName>${(inviteUser.profile.realname)!}</userName>
                    <userId>${inviteUser.id}</userId>
                    <userRank>${inviteUserRank}</userRank>
                    <userPhoto><@app.avatar href="${inviteUser.fetchImageUrl()!}"/></userPhoto>
                </inviteUserInfo>
            <#else>
                <inviteUserInfo>null</inviteUserInfo>
            </#if>


            <#if students?exists>
                <studentList>
                    <#list students as st>
                        <student>
                            <userName>${st.realname}</userName>
                            <userId>${st.id}</userId>
                            <userPhoto><@app.avatar href="${st.imgUrl!}"/></userPhoto>
                        </student>
                    </#list>
                </studentList>
            <#else>
                <studentList>null</studentList>
            </#if>


            <lastTimeAdvanceStudentList>null</lastTimeAdvanceStudentList>


        </userInfo>
    <#else>
        <userInfo>null</userInfo>
    </#if>
    </result>
</ns2:Lesson>