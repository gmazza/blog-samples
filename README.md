blog-samples
============

This download holds the source code for many of the tutorials in my blog listed at http://web-gmazza.rhcloud.com/blog/entry/blog-index.

Important notes:

1.)  After downloading, regardless of sample you will be working with, be sure to first run "mvn clean install" from the root folder to ensure the required parent POM (blog-samples-1.0-SNAPSHOT.pom) will be installed in your local Maven repository.  You will normally incur compilation/deployment errors with submodules if the parent POM hasn't been installed yet.

2.)  To set up a non-embedded Tomcat instance so it will work with Maven commands such as mvn tomcat:deploy, tomcat:redeploy, etc., see here: http://web-gmazza.rhcloud.com/blog/entry/web-service-tutorial#maventomcat.

3.)  The code samples may inadvertently have errors within them and are use-at-your-own-risk without any guarantees of security or reliability.  Thoroughly test all work for security problems before moving to production and of course don't use any sample supplied tutorial keys or passwords in production.

