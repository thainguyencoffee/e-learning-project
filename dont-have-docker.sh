#!/bin/bash

if [[ "$OSTYPE" == "darwin"* ]]; then
  SED="sed -i '' -e"
else
  SED="sed -i -e"
fi
host=$(echo $HOSTNAME | tr '[A-Z]' '[a-z]')


echo "***************************************************************************************************************************************"
echo "* To build Spring Boot native images, run with the \"native\" argument: \"sh ./build.sh native\" (images will take much longer to build). *"
echo "*                                                                                                                                     *"
echo "* This build script tries to auto-detect ARM64 (Apple Silicon) to build the appropriate Spring Boot Docker images.                    *"
echo "***************************************************************************************************************************************"
echo ""


echo "***************************************************************************************************************************************"
echo "File đang được chạy vì bạn không dùng Docker mà chỉ cần code frontend, file này chạy với MỤC ĐÍCH là setup file app.config.ts \n
Sau khi chạy xong, mở file app.config.ts để biết thêm thông tin!"
echo "***************************************************************************************************************************************"
echo ""

cd angular-ui/
rm -f "src/app/app.config.ts"
cp ../angular-ui.app.config.ts src/app/app.config.ts
$SED "s/LOCALHOST_NAME/${host}/g" src/app/app.config.ts
rm -f "src/app/app.config.ts''"
npm i
npm run build