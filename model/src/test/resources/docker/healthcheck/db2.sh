#!/bin/sh

set -e

retry=0

until cat <<EOF | docker exec -i --user db2inst1 jsql-db2 /bin/bash     
    
    pwd
    export DB2INSTANCE=DB2INST1                                                                                                                            
    ./database/config/db2inst1/sqllib/bin/db2 list active databases                                 
    ./database/config/db2inst1/sqllib/bin/db2 connect to testdb                              
    ./database/config/db2inst1/sqllib/bin/db2 select 1 as jsqlColumn from sysibm.sysversions                    
EOF
do
  retry=$((retry+1))
  if [ $retry -gt 60 ] ; then
    exit 1
  fi
  
  >&2 echo "Db2 is unavailable - sleeping #${retry}"
  sleep 1
done
  
>&2 echo "Db2 is up - executing command"
exec $cmd
