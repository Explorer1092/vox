# !/usr/bin/python
# -*- coding: utf-8 -*-

import os
import sys
import glob
import shutil

#scriptDir = os.path.dirname(os.path.realpath(__file__))

stage = sys.argv[1] if len(sys.argv) >= 2 else 'dev'
a = stage.split('-')
stage_mode = a[0]
stage_codename = "" if len(a) <= 1 else a[1]

base_dir = os.path.realpath('./') + '/'
output_base_dir = base_dir + "build-repo/build-" + stage + '/'

print "output base dir: " + output_base_dir


projectParams = {
    #parent的
    'andromeda' : {'portPrefix' : '64', 'webApps': {'/': 'andromeda-webapp'}, },
    'galaxy' : {'portPrefix' : '70', 'webApps': {'/':'galaxy-webapp'},},

    #equator的
    'somalia' : {'portPrefix' : '71', 'webApps': {'/': 'somalia-webapp'}, },

    'washington': {'portPrefix': '67', 'webApps': {'/': 'washington-webapp'}, },
    'admin': {'portPrefix': '69', 'webApps': {'/utopia-admin': 'utopia-admin-webapp', }, 'useSession': True, },
    'agent': {'portPrefix': '65', 'webApps': {'/': 'agent-webapp', }, },
    'wechat': {'portPrefix': '63', 'webApps': {'/': 'wechat-webapp', }, },
    'ucenter': {'portPrefix': '61', 'webApps': {'/': 'ucenter-webapp', }, },
	'mizar': {'portPrefix': '68', 'webApps': {'/': 'mizar-webapp', }, },
    'luffy': {'portPrefix': '66', 'webApps': {'/': 'luffy-webapp', }, },
}


def generate_resin_conf(name, params):
    config = {}

    jvm_tuning_args = ""

    # "fast" version doesn't increment the invocation counter for empty methods and accessor methods
    #jvm_tuning_args += " -XX:+UseFastAccessorMethods"


    '''
    Use G1GC
    '''
    jvm_tuning_args += " -XX:+UseG1GC"

    '''
    Java Support for Large Memory Pages
    http://www.oracle.com/technetwork/java/javase/tech/largememory-jsp-137182.html
    '''
    #jvm_tuning_args += " -XX:+UseLargePages"

    '''
    https://code.google.com/p/spymemcached/issues/detail?id=136
    '''
    #jvm_tuning_args += " -XX:MaxGCPauseMillis=850"


    '''
    Dump heap to file when java.lang.OutOfMemoryError is thrown. Manageable. (Introduced in 1.4.2 update 12, 5.0 update 7.)
    http://www.oracle.com/technetwork/java/javase/tech/vmoptions-jsp-140102.html
    '''
    jvm_tuning_args += " -XX:+HeapDumpOnOutOfMemoryError"

    '''
    The heap dump and the jvm crash log are two separate things.
    To change the destination of the jvm crash log run java with this option
    '''
    jvm_tuning_args += " -XX:ErrorFile=/tmp/jvm-crash-" + name + ".log"

    '''
    By default the heap dump is created in a file called java_pidpid.hprof in the working directory of the VM.
    You can specify an alternative file name or directory with the -XX:HeapDumpPath=/disk2/dumps option.
    '''
    jvm_tuning_args += " -XX:HeapDumpPath=/tmp"

    '''
    The compiler in the server VM now provides correct stack backtraces for all "cold" built-in exceptions.
    For performance purposes, when such an exception is thrown a few times, the method may be recompiled.
    After recompilation, the compiler may choose a faster tactic using preallocated exceptions that do not
    provide a stack trace. To disable completely the use of preallocated exceptions, use this new flag:
    -XX:-OmitStackTraceInFastThrow.
    http://www.oracle.com/technetwork/java/index.html
    '''
    jvm_tuning_args += " -XX:-OmitStackTraceInFastThrow"

    '''
    Enable jmxremote
    '''
    jvm_tuning_args += " -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=0"
    jvm_tuning_args += " -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

    '''
    https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html
    use IPv4 only sockets
    '''
    jvm_tuning_args += " -Djava.net.preferIPv4Stack=true"

    '''
     * Fast remoting methods leads to java.net.NoRouteToHostException: Cannot assign requested address
     * The actual resolution is quite simple: Java has everything what ever you need -Dhttp.maxConnections=100 allows us to put much pressure to the fast Hessian service methods. Java runtime already has HTTP1.1 client supports keep-alive, which utilized by Hessian underneath. So, we just need to allocate enough pool to prevent sockets closing and the following TIME_WAIT pollution.
    '''
    jvm_tuning_args += " -Dhttp.maxConnections=500"

    '''
    -XX:+PrintGCDetails
            [GC [DefNew: 8614K->8614K(9088K), 0.0000665 secs][Tenured: 112761K->10414K(121024K), 0.0433488 secs] 121376K->10414K(130112K), 0.0436268 secs]
    -XX:+PrintGCTimeStamps
            11.851: [GC 98328K->93620K(130112K), 0.0082960 secs]
    -XX:+PrintGCApplicationConcurrentTime
            Application time: 0.5291524 seconds
    -XX:+PrintGCApplicationStoppedTime
            Total time for which application threads were stopped: 0.0468229 seconds
    '''
    jvm_tuning_args += " -Xloggc:/dev/shm/webapp-gclog/jvmgc-" + stage + "-" + name + ".log -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime"

    # http://stackoverflow.com/questions/137212/how-to-solve-performance-problem-with-java-securerandom
    jvm_tuning_args += " -Djava.security.egd=file:/dev/./urandom"

    # http://stackoverflow.com/questions/27105004/what-means-javax-net-ssl-sslhandshakeexception-server-certificate-change-is-re
    jvm_tuning_args += " -Djdk.tls.allowUnsafeServerCertChange=true -Dsun.security.ssl.allowUnsafeRenegotiation=true"

    config['clusterSystemKey'] = 'resin-cluster-key-' + stage
    config['name'] = name
    config['portPrefix'] = params['portPrefix']

    if name == 'washington':
        config['appJvmArgLine'] = "-server -d64 -Xms16g -Xmx16g " + jvm_tuning_args
    elif name == 'somalia':
        config['appJvmArgLine'] = "-server -d64 -Xms12g -Xmx12g " + jvm_tuning_args
    else:
        config['appJvmArgLine'] = "-server -d64 -Xmx6g " + jvm_tuning_args

    config['appThreadMax'] = 4096

    config['adminPassword'] = "{SSHA}EFpZuQwCOVxsfGuywOZm4rpPwaqqPIzJ"
    config['clusterDefaultEx'] = ''
    config['proxyCacheMemorySize'] = '512m'
    config['portThreadMax'] = 4096
    config['acceptThreadMin'] = 128
    config['acceptThreadMax'] = 256

    if 'useSession' in params and params['useSession']:
        #use-persistent-store 会导致 resin db 的异常，暂时不用
        #Caused by: java.lang.ArrayIndexOutOfBoundsException: 35060
        #at com.caucho.db.block.BlockStore.getAllocationByAddress(BlockStore.java:917)
        config['sessionConfig'] = '''
                <session-config>
                    <!-- <use-persistent-store>true</use-persistent-store> -->
                    <enable-url-rewriting>false</enable-url-rewriting>
                    <session-timeout>240</session-timeout>
                    <session-max>200000</session-max>
                </session-config>
'''

        #遇到奇怪的session同步bug，先注释掉
        config['sessionPersistConfig'] = '''
<!--
        <persistent-store type="cluster">
            <init>
                <triplicate>true</triplicate>
            </init>
        </persistent-store>
-->
'''
    else:
        config['sessionConfig'] = ''
        config['sessionPersistConfig'] = ''

    if stage_mode == 'test' or stage_mode == 'staging':

        if stage_mode == 'staging':
            config['portPrefix'] = '1' + config['portPrefix']

            config['appJvmArgLine'] = "-server -d64 -ea -Xmx2048m " + jvm_tuning_args
            config['appJvmArgLine'] += " -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0"
        elif stage_mode == 'test':
            config['appJvmArgLine'] = "-server -d64 -ea -Xmx2048m " + jvm_tuning_args
            config['appJvmArgLine'] += " -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0"
            config['adminPassword'] = "{SSHA}zk+4iP5VaiBbL9Ci5a0UbVFjCKgg1Uy7" # 123456
        else:
            raise Exception('stage must be in (test, staging)')

        config['appThreadMax'] = 256
        config['clusterDefaultEx'] = '<development-mode-error-page/>'
        config['proxyCacheMemorySize'] = '1m'
        config['portThreadMax'] = 128
        config['acceptThreadMin'] = 32
        config['acceptThreadMax'] = 64

    config['appServersConfig'] = "<server id='app-%s-single' address='127.0.0.1' port='%s01'></server>" % (name, config['portPrefix'])

    config['webAppsConfig'] = ''

    if not '/' in params['webApps']:
        params['webApps']['/'] = 'utopia-crossdomain-webapp'

    for contextName in params['webApps']:
        config['webAppsConfig'] += """
            <web-app id="%s" root-directory="${resin.root}/../../../%s/webroot">
                %s
<!--
resin + gzip with Content-Length in response header will hang forever (before 4.0.40 at least)
-->
                <filter filter-name="gzip" filter-class="com.caucho.filters.GzipFilter"/>
                <filter-mapping url-pattern="*.vpage" filter-name="gzip"/>
                <filter-mapping url-pattern="*.api" filter-name="gzip"/>
                <redeploy-mode>manual</redeploy-mode>

            </web-app>

""" % (contextName, params['webApps'][contextName], config['sessionConfig'])

    config_template = """
<!-- Resin 4.0 configuration file. -->
<resin xmlns="http://caucho.com/ns/resin"
       xmlns:resin="urn:java:com.caucho.resin">

    <!-- Logging configuration for the JDK logging API -->
    <log-handler name="" level="all" path="stdout:" timestamp="[%y-%m-%d %H:%M:%S.%s]" format=" {${thread}} ${log.message}"/>

    <logger name="" level="${log_level?:'info'}"/>
    <logger name="com.caucho.java" level="config"/>
    <logger name="com.caucho.loader" level="config"/>

    <resin:import path="${resin.home}/conf/health.xml" />

    <resin:AdminAuthenticator>
        <user name="admin" password="${config.adminPassword}"/>
    </resin:AdminAuthenticator>

    <cluster-system-key>${config.clusterSystemKey}</cluster-system-key>
    <dependency-check-interval>1h</dependency-check-interval>

    <cluster-default>
        <resin:import path="classpath:META-INF/caucho/app-default.xml"/>

        ${config.clusterDefaultEx}

        <resin:AdminServices/>

        <proxy-cache memory-size="${config.proxyCacheMemorySize}">
            <!-- Vary header rewriting for IE -->
            <rewrite-vary-as-private/>
        </proxy-cache>

        <server-default>
            <port-default>
                <port-thread-max>${config.portThreadMax}</port-thread-max>
                <accept-thread-min>${config.acceptThreadMin}</accept-thread-min>
                <accept-thread-max>${config.acceptThreadMax}</accept-thread-max>

                <tcp-cork>true</tcp-cork>
            </port-default>

            <sendfile-enable>true</sendfile-enable>
            <watchdog-port>${config.portPrefix}00</watchdog-port>
        </server-default>

        <host-default>
            <web-app-default>
                <prologue>
                    <allow-servlet-el/>
                </prologue>

                <cache-mapping url-pattern="/" max-age="5s"/>
                <cache-mapping url-pattern="*.gif" max-age="60s"/>
                <cache-mapping url-pattern="*.jpg" max-age="60s"/>
                <cache-mapping url-pattern="*.png" max-age="60s"/>
                <cache-mapping url-pattern="*.css" max-age="60s"/>
                <cache-mapping url-pattern="*.js" max-age="60s"/>

            </web-app-default>
        </host-default>
    </cluster-default>


    <cluster id="app-${config.name}">
        <!-- define the servers in the cluster -->
        <server-default>
            <jvm-arg-line>-Dfile.encoding=UTF-8 ${config.appJvmArgLine}</jvm-arg-line>
            <thread-max>${config.appThreadMax}</thread-max>
            <socket-timeout>120s</socket-timeout>
            <http address="*" port="${config.portPrefix}90"/>

            <keepalive-max>8192</keepalive-max>
            <keepalive-timeout>600s</keepalive-timeout>
        </server-default>

        ${config.appServersConfig}

        ${config.sessionPersistConfig}

        <host id="" root-directory=".">

            ${config.webAppsConfig}

            <web-app id="/server-admin" root-directory="${resin.root}/doc/admin">
                <prologue>
                    <resin:set var="resin_admin_external" value="false" />
                    <resin:set var="resin_admin_insecure" value="true" />
                </prologue>
            </web-app>
        </host>
    </cluster>
</resin>
"""

    config_xml = config_template
    for k in config:
        config_xml = config_xml.replace('${config.' + k + '}', str(config[k]))

    dst_filename = output_base_dir + "env/stage/" + stage + "/resin-" + stage + "-" + name + ".xml"
    dst_dir = os.path.dirname(os.path.realpath(dst_filename))
    if not os.path.exists(dst_dir):
        os.makedirs(dst_dir, 0755)

    with open(dst_filename, "w") as f:
        f.write(config_xml)

    print "Generated: " + dst_filename


for name in projectParams:
    generate_resin_conf(name, projectParams[name])




def process_file_version(subdir, filename):
    exts = [".min.js", ".swf", ".css", ".js"]
    for ext in exts:
        if filename.endswith(ext) and not "-V201" in filename:
            fn_main = filename[0:-len(ext)]
            fn_ext = ext
            fn_full = os.path.join(subdir, filename)
            print ' File', subdir, fn_main, fn_ext, os.path.getmtime(fn_full)

            versioned_files = glob.glob(os.path.join(subdir, fn_main + "-V201*"))
            expired_files = sorted(versioned_files)[0:-4]
            for s in expired_files:
                print ' removing expired file: ' + s
                #os.remove(s)


            #shutil.copy(fn_full, )
            return True

    return False

def collect_filetimes(dir):
    print "scanning: " + dir
    for subdir, dirnames, filenames in os.walk(dir):
        for filename in filenames:
            process_file_version(subdir, filename)

#collect_filetimes(output_base_dir + 'washington-webapp/resources')
#collect_filetimes(output_base_dir + 'washington-webapp/public')

