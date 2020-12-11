# Apollo-desktop
Apollo desktop applications

At the moment we have Java-FX 11 based desktop application. It uses the same code that Web browser based Apollo wallet
with some additional functionality that is impossible in browser.

To run desktop wallet, ___apollo-blockchain___ and ___apollo-web-ui__ packages should be installed.
Please refer to following sub-projects:
[Apollo](https://github.com/ApolloFoundation/Apollo) Apollo-blockchain node
[Apollo-web-ui](https://github.com/ApolloFoundation/Apollo-web-ui) Apollo UI
 

## How to build

### Requirements

	Any JDK 11 distribution, the mainstream openjdk 11.0.9 or later is prefferred.
	
### Build
Clone repository, then run following commands
<pre>
	cd apl-desktop
	./mvnw clean install
</pre>	
Maven wrapper downloads Apache Maven and then builds package. Final package is zip file in "target" directory, e.g. apollo-desktop-1.47.5-NoOS-NoArch.zip that contains all required components and libraries.

## How to run

### Requirements

Any JRE 11 distribution, the mainstream openjdk 11.0.9 or later is prefferred.

### Run desctop wallet

At the moment Apollo-desktop requires Apollo-blockchain package running on the local computer. We plan to change this in the nearest future and allow Apollo-desktop to work with remote Apollo nodes

When Apollo blockchain node is up and running, you can start Apollo-desktop wallet by scripts in ApolloWallet/apollo-dekstop/bin directory

## GIT branches

We follow GIT FLOW procedure in our developemnt and use following branhces:

__master__ branch contains stable code of the latest release. It is also tagged for each public release. Please see "Tags" in the "barcnh" dropdown menu. Plea
se use this branch to compile Apollo components.

__develop__ branch contains latest development version. Use this branch if you are developer/contributor.

__stage__ branch contains release preparation work of the last release. Do not use this branch if you are not release engineer


fix/*, feature/*, bugfix/** - temporary branches used by developers. Ususaly those branmches get merged to ___develop___ and deleted after work is done




