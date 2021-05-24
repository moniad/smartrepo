In order to regenerate Java sources from WSDL, you need to do the following:

1. Choose an appropriate Project SDK in IntelliJ settings (when I tried it, it worked best with Amazon Corretto 8).
2. Run clean install from Maven tab on Sidebar

Newly generated classes will be available in the package named:
https.agh_edu_pl.smart_repo.file_extension_service

In order to perform sample testing, do the following:
1. Either change working directory to test/resources, or copy file noteTxtExtensionRequest.xml located there to
   appropriate location.
2. Run curl --header "content-type: text/xml" -d @noteTxtExtensionRequest.xml http://localhost:7777/ws/schema and verify
if you receive ```<ns2:extension>txt</ns2:extension>``` in your response.
   
In case file extension cannot be recovered, you won't receive the extension tag in response.