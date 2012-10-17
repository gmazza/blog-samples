This folder provides modified source files for the "Compressing SOAP messages in transit" blog entry (http://www.jroller.com/gmazza/entry/compressing_soap_messages). 

These are not standalone, compilable projects but just replacement files to the basic web service tutorial (http://www.jroller.com/gmazza/entry/web_service_tutorial, source code: https://github.com/gmazza/blog-samples/tree/master/web_service_tutorial) necessary to activate Fast Infoset or GZIP on Metro or CXF.  The files potentially to be replaced (depending on web service stack and compression method) are the client's WSClient class and/or the service's DoubleItPortTypeImpl class.

