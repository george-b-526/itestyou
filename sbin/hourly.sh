echo START
date

mysql <<< "SHOW DATABASES;"

STATUS=$?
if [ $STATUS -ne 0 ]; then
  echo NOT RUNNING
  /etc/init.d/mysql stop 2>&1
  /etc/init.d/mysql start 2>&1
  /etc/init.d/tomcat6 stop 2>&1
  /etc/init.d/tomcat6 start 2>&1
else
  echo RUNNING OK!
fi

echo DONE

