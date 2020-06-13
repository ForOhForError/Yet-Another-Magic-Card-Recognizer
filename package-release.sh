#!/bin/bash
mvn clean package assembly:single
rm YamCR.zip
zip -r YamCR.zip README.md LICENSE licenses res readmeImages browser-source run.bat
cd target
zip ../YamCR.zip YamCR.jar
