echo off
set FOUND=false
for %%G in ("%path:;=" "%") do if exist %%G"\java.exe" set FOUND=true
if %FOUND%==true (java -Xmx5120m -jar igb_exe.jar %*) else (echo %0: could not find java)