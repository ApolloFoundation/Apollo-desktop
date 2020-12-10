# Apollo-desktop
Apollo desktop applications

At the moment we have Java-FX 11 based desktop application. It uses the same code that Web browser based Apollo wallet
with some additional functionality that is impossible in browser.

## How to build

### Requirements

	Any JDK 11 distribution, the mainstream openjdk 11.0.9 or later is prefferred.
	
### Build
Clone repository, then run following commands
<pre>
	cd apl-desktop
	./mvnw clean install
</pre>	
Maven wrapper downloads Apache Maven and then builds package. Package is zip file, e.g. apollo-desktop-1.47.5-NoOS-NoArch.zip that contains all required components and libraries.

## How to run

### Requirements

	Any JDK 11 distribution, the mainstream openjdk 11.0.9 or later is prefferred.


At the moment Apollo-desktop requires Apollo-blockchain package running on the local computer. We plan to change this in the nearest future and allow Apollo-desktop to work with remote Apollo nodes
Please refer to instructions at https://github.com/ApolloFoundation/Apollo on assembling and setting up Apollo node locally.

When Apollo blockchain node is up and running, you can start Apollo-desktop wallet by scripts in ApolloWallet/apollo-dekstop/bin directory






