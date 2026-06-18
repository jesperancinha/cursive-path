# Project Build History

Note: This file tracks the history of project builds and configurations.

## Last prompts:

---
- can you make a full application using kotlin multiplatform to create a website consisting of backend and front end, where in the GUI we type some word in english and on pressing enter we see the word translated to another language? The webinterface can be simple---
---
- I would like the full project using libretranslate please
---
- can you zip the whole thing so that I can download and test locally?
---
- can you add to the docker-compose the best images to run the jars in the containerised form, so that I can run them easily? I should have 3 running containers in the end.
---
- Can you please make sure to use Hazelcast as a cache system for the backend of this project so that we can cache all responses given by librertranslate. That way we can save requests to the service. Make sure to cache exactly per request. Please also log whenever a real request is made to the libretranslate so that we can track those requests.
---
- can you fix this error? "> Task :backend:compileKotlin FAILED
  e: java.lang.IllegalArgumentException: 25.0.3
  at org.jetbrains.kotlin.com.intellij.util.lang.JavaVersion.parse(JavaVersion.java:307)
  at org.jetbrains.kotlin.com.intellij.util.lang.JavaVersion.current(JavaVersion.java:176)
  at org.jetbrains.kotlin.cli.jvm.modules.JavaVersionUtilsKt.isAtLeastJava9(javaVersionUtils.kt:11)"
---
- can you make sure that in the docker-compose file, the backend and frontend only start when the locks of the libretranslate image show the text "Starting gunicorn"?