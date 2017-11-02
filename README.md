Yoti Java SDK
=============

Welcome to the Yoti Java SDK. This repo contains the tools and step by step instructions you need to quickly integrate your Java back-end with Yoti so that your users can share their identity details with your application in a secure and trusted way.  

## Table of Contents

1) [An Architectural view](#an-architectural-view) -
High level overview of integration

1) [Requirements](#requirements)
Everything you need to get started

1) [Building From Source](#building-from-source)
Everything you need to build from source

1) [Enabling the SDK](#enabling-the-sdk)-
Description on importing your SDK 

1) [Client Initialisation](#client-initialisation)-
Description on setting up your SDK

1) [Profile Retrieval](#profile-retrieval) -
Description on setting up profile

1) [Handling Users](#handling-users) -
Description on handling user logons

1) [Connectivity Requirements](#connectivity-requirements)-
Description of network connectivity requirements

1) [Modules](#modules)- 
The Modules above explained

1) [Spring Boot Auto Configuration](#spring-boot-auto-configuration)- 
Description of utilising Spring Boot

1) [Spring Security Integration](#spring-security-integration)- 
Integrating Yoti Authentication with Spring Boot.

1) [Misc](#misc)

1) [Known Issues](#known-issues)-
Known issues using the libraries

1) [Support](#support)-
Please feel free to reach out

## An Architectural View

To integrate your application with Yoti, your back-end must expose a GET endpoint that Yoti will use to forward tokens.
The endpoint can be configured in Yoti Dashboard when you create/update your application.

The image below shows how your application back-end and Yoti integrate in the context of a Login flow.
Yoti SDK carries out steps 6 through 9 for you, including profile decryption and communication with backend services.

![alt text](login_flow.png "Login flow")


Yoti also allows you to enable user details verification from your mobile app by means of the Android (TBA) and iOS (TBA) SDKs. In that scenario, your Yoti-enabled mobile app is playing both the role of the browser and the Yoti app. By the way, your back-end doesn't need to handle these cases in a significantly different way. You might just decide to handle the `User-Agent` header in order to provide different responses for web and mobile clients.

## Requirements

* Java 1.6 or higher
* SLF4J 

## Building From Source

Building from source is generally not necessary for third parties since artifacts are published in Maven Central. However, if you want to build from source you can do so using the [Maven Wrapper](https://github.com/takari/maven-wrapper) that is bundled with this distribution. For those familiar with Gradle this is much like the Gradle Wrapper and ensures that the correct version of Maven is being used.

From the top level directory:

```bash
./mvnw clean install
```

Notable flags that you may wish to use to skip certain static analysis/code quality tools are listed below. This is only recommended if you find that these tools are taking too long during development or are flagging false positives that you are yet to exclude. **They should not be ignored when building a candidate for a release unless you are sure that the issues being raised are not a cause for concern.**

* `-Dfindbugs.skip=true`: skips findbugs and the findbugs security extension.
* `-Ddependency-check.skip=true`: skips the OWASP dependency scanner.

### Example Usage

```bash
./mvnw clean install -Dfindbugs.skip=true -Ddependency-check.skip=true
```

## Enabling the SDK

To import the Yoti SDK inside your project, you can use your favourite dependency management system.
If you are using Maven, you need to add the following dependency:

```xml
<dependency>
	<groupId>com.yoti</groupId>
	<artifactId>yoti-sdk-impl</artifactId>
	<version>1.3</version>
</dependency>
```
If you are using Gradle, here is the dependency to add:

`compile group: 'com.yoti', name: 'yoti-sdk-impl', version: '1.3'`

You will find all classes packaged under `com.yoti.api`


## Client Initialisation

The YotiClient is the SDK entry point. To initialise it you need include the following snippet inside your endpoint initialisation section:
```java
import com.yoti.api.client.YotiClient;
import com.yoti.api.client.YotiClientBuilder;
import static com.yoti.api.client.FileKeyPairSource.fromFile;

YotiClient client = YotiClientBuilder.newInstance()
    .forApplication(<YOUR_CLIENT_SDK_ID>)
    .withKeyPair(fromFile(<PATH/TO/YOUR/APPLICATION/KEY_PAIR.pem>)).build();
```
Where:
* `YOUR_CLIENT_SDK_ID` is the identifier generated by Yoti Dashboard when you create your app.
* `PATH/TO/YOUR/APPLICATION/KEY_PAIR.pem` is the path to the pem file your browser generates for you, when you create your app on Yoti Dashboard.


## Profile Retrieval

When your application receives a token via the exposed endpoint (it will be assigned to a query string parameter named `token`), you can easily retrieve the user profile by adding the following to your endpoint handler:

```java
import com.yoti.api.client.ActivityDetails;

ActivityDetails activityDetails = client.getActivityDetails(encryptedToken);
```
or, for a more detailed example:

```java
import com.yoti.api.client.YotiClient;
import com.yoti.api.client.ActivityDetails;
import com.yoti.api.client.HumanProfile;
import com.yoti.api.client.ProfileException;

try {
	final ActivityDetails activityDetails = client.getActivityDetails(token);
   	final HumanProfile profile = activityDetails.getUserProfile();
   	//use the profile to extract attributes.
} catch (ProfileException e) {
    LOG.info("Could not get profile", e);
    // do something meaningful.
}
```
 
**Important: encrypted tokens are intended for single use only. You must not invoke the client using the same token more than once.**

## Handling Users

When you retrieve the user profile, you receive a user ID generated by Yoti exclusively for your application.
This means that if the same individual logs into another app, Yoti will assign her/him a different ID.
You can use this ID to verify whether (for your application) the retrieved profile identifies a new or an existing user.
Here is an example of how this works:

```java
ActivityDetails activityDetails;
try {
	activityDetails = client.getActivityDetails(token);
  Optional<YourAppUserClass> user = yourUserSearchMethod(activityDetails.getUserId());
  if (user.isPresent()) {
		String userId = activityDetails.getUserId();
		Image selfie = profile.getSelfie();
		String givenNames = profile.getGivenNames();
		String familyName = profile.getFamilyName();
		String mobileNumber = profile.getPhoneNumber();
		String emailAddress = profile.getEmailAddress();
		Date dateOfBirth = profile.getDateOfBirth();
		Gender gender = profile.getGender();
		String nationality = profile.getNationality();
  } else {
      // handle registration
  }              
} catch (ProfileException e) {
  LOG.info("Could not get profile", e);
  return "error";
}
```
Where `yourUserSearchMethod` is a piece of logic in your app that is supposed to find a user, given a userId. 
No matter if the user is a new or an existing one, Yoti will always provide her/his profile, so you don't necessarily need to store it.

The `com.yoti.api.client.HumanProfile` class provides a set of methods to retrieve different user attributes. Whether the attributes are present or not depends on the settings you have applied to your app on Yoti Dashboard.

## Connectivity Requirements

Interacting with the `com.yoti.api.client.YotiClient` to get `com.yoti.api.client.ActivityDetails` is not an offline operation. Your application will need to be able to establish an outbound TCP connection to port 443 to the Yoti servers at `https://api.yoti.com` (by default - see the [Misc](#misc) section).

By default the Yoti Client will block indefinitely when connecting to the remote server or reading data. Consequently it is **possible that your application thread could be blocked**. 

Since version 1.1 of the `yoti-sdk-impl` you can set the following two system properties to control this behaviour:

* `yoti.client.connect.timeout.ms` - the number of milliseconds that you are prepared to wait for the connection to be established. Zero is interpreted as an infinite timeout.
* `yoti.client.read.timeout.ms` - the number of milliseconds that you are prepared to wait for data to become available to read in the response stream. Zero is interpreted as an infinite timeout.

## Modules

The SDK is split into a number of modules for easier use and future extensibility. 
### yoti-sdk-api
Being the only interface you need to explicitly couple your code to this module. Exposes the core classes:

Class | Description
----- | -----------
HumanProfile  | The set of attributes the user has configured for the transaction.
YotiClientBuilder  | Builds a YotiClient instance by automatically selecting the available implementations on the class path.
YotiClient | Allows your app to retrieve a user profile, given an encrypted token.
KeyPairSource and its implementations | A set of classes responsible for working with different sources (e.g. files, classpath resources, URLs) to load the private/public keypair.

### yoti-sdk-dummy
Dummy implementation without connectivity to any platform services. Can be used for testing purposes.
### yoti-sdk-impl
Real SDK implementation that takes care of decrypting the token, fetching the user profile from Yoti servers by issuing a signed request and finally decrypting the fetched profile.
### yoti-sdk-spring-boot-auto-config
A module that can be used in Spring Boot applications to automatically configure the YotiClient and KeyPairSource with standard application properties.
### yoti-sdk-spring-security
A module that can be used in Spring applications that use Spring Security to add Yoti authentication.

## Spring Boot Auto Configuration

As a convenience, if your application happens to use Spring Boot, you can utilise the Spring Boot auto configuration module that will take care of configuring the Yoti Client and Key Pair for you based on standard application properties.

For more information and to see an example of this in use take a look at the Spring Boot Auto Configuration module and Spring Boot example in this repository.

## Spring Security Integration

If you use Spring Security you can use the `yoti-sdk-spring-security` module to make integration easier. You are provided with some classes that fit into Spring Security's existing authentication model.

Combining this with the Spring Boot Auto Configuration can make integration very easy with very little code needing to be written.

## Misc

* By default, Yoti SDKs fetch profiles from [https://api.yoti.com/api/v1](https://api.yoti.com/api/v1).
If necessary, this can be overridden by setting the `yoti.api.url` system property.
* This SDK uses AES-256 encryption. If you are using the Oracle JDK, this key length is not enabled by default. The following stack overflow question explains how to fix this: [http://stackoverflow.com/questions/6481627/java-security-illegal-key-size-or-default-parameters](http://stackoverflow.com/questions/6481627/java-security-illegal-key-size-or-default-parameters)
* To find out how to set up your Java project in order to use this SDK, you can check the Spring Boot example in this repo.   

## Known Issues

### Loading Private Keys

#### Affects

* Version 1.1 onwards.

#### Description

There was a known issue with the encoding of RSA private key PEM files that were issued in the past by Yoti Dashboard (most likely where you downloaded the private key for your application).

Some software is more accepting that others and will have been able to cope with the incorrect encoding, whereas some stricter libraries will not accept this encoding.

At version `1.1` of this client the Java Security Provider that we use (`Bouncy Castle`) was [upgraded](https://www.bouncycastle.org/releasenotes.html) from `1.51` -> `1.57`. This upgrade appears to have made the key parser more strict in terms of encoding since it no longer accepts these incorrectly encoded keys. 

#### Symptoms

This error usually manifests itself when constructing and instance of the Yoti Client to read the private key.

Generally you'll encounter an exception with an message and stack trace as follows:

```java
com.yoti.api.client.InitialisationException: Cannot load key pair
	at com.yoti.api.client.spi.remote.SecureYotiClient.loadKeyPair(SecureYotiClient.java:99)
	at com.yoti.api.client.spi.remote.SecureYotiClient.<init>(SecureYotiClient.java:73)
	at com.yoti.api.client.spi.remote.SecureYotiClientFactory.getInstance(SecureYotiClientFactory.java:25)
	at com.yoti.api.client.ServiceLocatorYotiClientBuilder.build(ServiceLocatorYotiClientBuilder.java:40)
	at com.yoti.api.spring.YotiClientAutoConfiguration.yotiClient(YotiClientAutoConfiguration.java:48)
	
Caused by: org.bouncycastle.openssl.PEMException: problem creating RSA private key: java.lang.IllegalArgumentException: failed to construct sequence from byte[]: corrupted stream detected
	at org.bouncycastle.openssl.PEMParser$KeyPairParser.parseObject(Unknown Source)
	at org.bouncycastle.openssl.PEMParser.readObject(Unknown Source)
	at com.yoti.api.client.spi.remote.SecureYotiClient$KeyStreamVisitor.findKeyPair(SecureYotiClient.java:269)
	at com.yoti.api.client.spi.remote.SecureYotiClient$KeyStreamVisitor.accept(SecureYotiClient.java:260)
	at com.yoti.api.spring.SpringResourceKeyPairSource.getFromStream(SpringResourceKeyPairSource.java:28)
	at com.yoti.api.client.spi.remote.SecureYotiClient.loadKeyPair(SecureYotiClient.java:97)
	... 52 common frames omitted
	
Caused by: org.bouncycastle.openssl.PEMException: problem creating RSA private key: java.lang.IllegalArgumentException: failed to construct sequence from byte[]: corrupted stream detected
	at org.bouncycastle.openssl.PEMParser$RSAKeyPairParser.parse(Unknown Source)
	... 58 common frames omitted
	
Caused by: java.lang.IllegalArgumentException: failed to construct sequence from byte[]: corrupted stream detected
	at org.bouncycastle.asn1.ASN1Sequence.getInstance(Unknown Source)
	... 59 common frames omitted
```

#### How To Fix

You can re-encode the badly encoded PEM file using some software that is more accepting of the incorrect encoding and saving the new key. 
 
An example of software able to do this is `OpenSSL` versions `1.0.2g` and `1.1.0` using the command:

```bash
openssl rsa -in input-file.pem -out fixed-input-file.pem
```

Using the new (correctly encoded) file should now be compatible with versions 1.1 onwards (as well as older versions like `1.0` prior to this).

## Support

For any questions or support please email [sdksupport@yoti.com](mailto:sdksupport@yoti.com).
Please provide the following the get you up and working as quick as possible:

- Computer Type
- OS Version
- Version of Java being used
- Screenshot

# Authors
 * [Andras Bulla](https://github.com/lopihe)
 * [Radoslaw Busz](https://github.com/gitplaneta)
 * [David Goaté](https://github.com/davidgoate)
 * [Attila Kiss](https://github.com/atkiss)
 * [Quirino Zagarese](https://github.com/qzagarese) 
 
