# Anime Server Keystore
This directory should contain the anime server's `anime.p12`, `anime.truststore`, and `keystore.properties` files. If
these files exist, they are used by the server to allow users to make https requests.

## Keystore Setup
In order for the anime server's https to work correctly, you must specify a keystore, truststore, and properties file
containing information about that keystore including its password, otherwise the server will fall back to plain http.

### Generating the anime.p12 file
Java contains a utility for generating keystore files which is the recommended way of generating a keystore for the
anime server. In order to generate the keystore, open a terminal in this directory and run the command:
```shell script
keytool -genkeypair -alias anime -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore anime.p12 -validity 3650
```
The command will ask you for a password, name, and other information. When specifying the name, be sure to use the name
of the domain you intend to run the anime server under (e.g. `localhost`). Once finished, the command will generate a
file called `anime.p12`.

### Generating the anime.trustore file
If the server is lacking a truststore file, it will encounter annoying errors when loading the keystore. In order to
generate the truststore, open a terminal in this directory and run these commands:
```shell script
keytool -export -alias anime -keystore anime.p12 -file anime_cert
keytool -importcert -keystore anime.truststore -alias anime -file anime_cert
```

### Writing the keystore.properties file
The anime server also requires a file containing info about the keystore generated. Create a new file called
`keystore.properties` with the content:
```properties
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:keystore/anime.p12
server.ssl.key-store-password=<password>
server.ssl.key-store-alias=anime
server.ssl.trust-store=classpath:keystore/anime.truststore
server.ssl.trust-store-password=<password>
server.ssl.trust-store-type=PKCS12
security.require-ssl=true
```
Remember to replace the `<password>` with the password you typed when generating the `anime.p12` and `anime.truststore`
files.