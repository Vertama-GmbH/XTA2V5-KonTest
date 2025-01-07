# Fork of the KoSIT XTA2V5 Konformitätstester

This is a fork of the KoSIT repo at: https://projekte.kosit.org/transport-public/XTA2V5-KonTest.

This fork will be very likely to go nowhere. It is not meant to be used. Its purpose is to allow refactoring, testing and general tinkering with the existing code in oder to understand what is going on here and how this XTA2 is supposed to be working. 

In order to do so, I need to refactor and clean-up varioius things to dig for the origins of decisions concerning usage and implementation of XTA features from a client perspektive. Honestly, I find it extremly difficult to understand this whole java mess, and this repo might help (me) understanding how one might possibly use XTA2. 

Starting with jotting useful information (README with how to build this), add missing pieces (./mvn, mvnw) and deleting auto-generated IDE code (where it makes absolutely no sense), like catching exceptions in test just to rethrow them again...

```java
    try {
        ...
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
```
