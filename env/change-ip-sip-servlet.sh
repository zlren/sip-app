echo "enter ip :"
read ip

cd standalone/configuration/
sed -i "s/127.0.0.1/$ip/g" standalone-sip.xml standalone.xml
cd ../../

cd mobicents-media-server/mms-server/deploy/
sed -i "s/127.0.0.1/$ip/g" server-beans.xml
