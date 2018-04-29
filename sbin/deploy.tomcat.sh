#!/bin/sh
#

# Force shell to fail on any errors.
set -e

#export NS="140528"
export NS="150319"


# hint
echo "Deploying $NS"

# stop tomcat
service tomcat6 stop

# rotate logs
postfix=`date '+%F-%H.%M.%S'`
cp -v /usr/share/tomcat6/logs/catalina.out /usr/share/tomcat6/logs/catalina-$postfix.out
rm -f /usr/share/tomcat6/logs/catalina.out

# remove existing files
rm -rf /oy/testvisor/tomcat_root/*

# deploy new files
unzip /oy/setup/webapps-$NS/webapps/admin.war -d /oy/testvisor/tomcat_root/admin
unzip /oy/setup/webapps-$NS/webapps/test.war -d /oy/testvisor/tomcat_root/test
unzip /oy/setup/webapps-$NS/webapps/ml.war -d /oy/testvisor/tomcat_root/ml
unzip /oy/setup/webapps-$NS/webapps/api.war -d /oy/testvisor/tomcat_root/api

# copy web xml files
cp /oy/sbin/tomcat/*.xml /usr/share/tomcat6/conf/Catalina/localhost

# start tomcat
service tomcat6 start
	