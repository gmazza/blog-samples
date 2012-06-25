blog-samples
============

This download holds the source code for many of the tutorials in my JRoller blog listed at http://www.jroller.com/gmazza/entry/blog_article_index.

Important notes:

1.)  After downloading, regardless of sample you will be working with, be sure to first run "mvn clean install" from the root folder to ensure the required parent POM (blog-samples-1.0-SNAPSHOT.pom) will be installed in your local Maven repository.  You will normally incur compilation/deployment errors with submodules if the parent POM hasn't been installed yet.

2.)  The samples are each pretty small but if you incur memory errors while building all at once you may need to expand your memory allocations for Maven (http://cxf.apache.org/building.html).

3.)  I've compiled and run the source code on JDK 7 without difficulty.  For JDK 6, CXF should be fine but the Metro samples will probably return compilation/deployment errors unless you update the jaxws-api.jar in your JDK (http://www.jroller.com/gmazza/entry/metro_usernametoken_profile#MetroUT2)

4.)  To set up Tomcat so it will work with Maven commands such as mvn tomcat:deploy, tomcat:redeploy, etc., see here: http://www.jroller.com/gmazza/entry/web_service_tutorial#maventomcat.

5.)  The code samples may inadvertently have errors within them and are use-at-your-own-risk without any guarantees of security or reliability.  Thoroughly test all work for security holes before moving to production and don't use the supplied sample keys or passwords in production.

