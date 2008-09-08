proguard \
-injars dist/ScriptMobile.jar \
-outjars  ScriptMobile.jar \
-libraryjars /usr/local/netbeans-6.1/mobility8/WTK2.5.2/lib/midpapi10.jar \
-libraryjars /usr/local/netbeans-6.1/mobility8/WTK2.5.2/lib/cldcapi10.jar \
-overloadaggressively \
-allowaccessmodification \
-keep public class Main \
