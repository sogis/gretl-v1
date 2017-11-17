#!/bin/bash

echo "=============================================================="
echo "Tests the GRETL runtime with a GRETL job"
echo "=============================================================="

# TODO: no absolute path

# run GRETL job by GRETL runtime
./start-gretl.sh << EOF
/home/craaflaub/git/sogis/gretljobs/afu_altlasten_pub
transferAfuAltlasten
localhost:1234
sdbu
xyz
localhost:1234
tdbu
xyz
EOF
