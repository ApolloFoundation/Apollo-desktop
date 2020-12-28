'Start apl-blockchain in background without open console window
'Required for Windows installer

Set WshShell = CreateObject("WScript.Shell")
set fso =  CreateObject("Scripting.FileSystemObject")
rem Get Apollo root folder
Dim ApolloFolder
ApolloFolder =  fso.GetParentFolderName(fso.GetParentFolderName(fso.GetParentFolderName(WScript.ScriptFullName)))
WshShell.Run chr(34) & ApolloFolder & "\apollo-blockchain\bin\apl-run.bat" & chr(34), 0, false
WshShell.Run chr(34) & CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName) & "\apl-run-desktop.bat" & chr(34), 0, false

