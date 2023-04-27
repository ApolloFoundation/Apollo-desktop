# Apollo-desktop
Apollo desktop wallet applications

At the moment we have Java-FX based desktop application. It uses the same code that Web browser based Apollo wallet from [Apollo-web-ui](https://github.com/ApolloFoundation/Apollo-web-ui) sub-project with some additional functionality that is impossible in browser.

To run desktop wallet, ___apollo-blockchain___ and ___apollo-web-ui__ packages should be installed.
Please refer to following sub-projects:
[Apollo](https://github.com/ApolloFoundation/Apollo) Apollo-blockchain node
[Apollo-web-ui](https://github.com/ApolloFoundation/Apollo-web-ui) Apollo UI 

## How to build

### Requirements

	Any JDK 11 distribution, the mainstream openjdk 11.0.9 or later is prefferred.
	
### Build commands

Clone repository, then run following commands
<pre>
	cd apl-desktop
	./mvnw clean install
</pre>	
Maven wrapper downloads Apache Maven and then builds package. Final package is zip file in "___target___" directory, e.g. ___apollo-desktop-"1.51.3"-NoOS-NoArch.zip___ that contains all required components and libraries.

## How to run

### Requirements

Any JRE 11 distribution, the mainstream openjdk 11.0.9 or later is prefferred.

### Run desktop wallet

At the moment Apollo-desktop requires ___Apollo-blockchain package___ running on the local computer and ___Apollo-web-ui___ package is installed. We plan to change this in the nearest future and allow Apollo-desktop to work with trusted remote Apollo nodes.

When Apollo blockchain node is up and running, you can start Apollo-desktop wallet by scripts in ApolloWallet/apollo-dekstop/bin directory.

### Debugging JavaScript

Apollo desktop application supports __Google Chrome Devtools__
 
Launch steps:
* Build the repository
* Run the Apollo Blockchain Node with Apollo Web UI installed
* Use _bin/apl-run-desktop.bat_ for Windows and _bin/apl-run-desktop.sh_ scripts for Linux and Mac OS
with adding `-d` parameter to launch Apollo Desktop App
* Observe message in logs: 
 `Chrome Debugger URL is devtools://devtools/bundled/inspector.html?ws=localhost:32322/`
* Open the Chrome Browser and go to  URL given in the message above
* Perform any action on Apollo Desktop App and see all the stats at Chrome devtools
## GIT branches

We follow GIT FLOW procedure in our developemnt and use following branhces:

__master__ branch contains stable code of the latest release. It is also tagged for each public release. Please see "Tags" in the "barcnh" dropdown menu. Plea
se use this branch to compile Apollo components.

__develop__ branch contains latest development version. Use this branch if you are developer/contributor.

__stage__ branch contains release preparation work of the last release. Do not use this branch if you are not release engineer


fix/*, feature/*, bugfix/** - temporary branches used by developers. Ususaly those branmches get merged to ___develop___ and deleted after work is done




