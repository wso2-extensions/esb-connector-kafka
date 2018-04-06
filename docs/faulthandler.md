## Configuring the Kafka Fault Handler Sequence


Given below is a sample proxy service and fault handler sequence that you can use as a starting point for handling faults when integrating with Kafka. Customize this sample to match your requirements.

**Sample Proxy**
````xml
<proxy xmlns="http://ws.apache.org/ns/synapse" name="KafkaProducer_sampleProxy" transports="https,http" statistics="disable" trace="disable" startOnLoad="true">
  <target>
    <inSequence onError="faultHandlerSeq">     
      <init>       
      </init>     
      <filter source="$axis2:HTTP_SC" regex="^[^2][0-9][0-9]">
            <then>
               <switch source="$axis2:HTTP_SC">
                  <case regex="401">
                  <!--Fill with your error code value and expression-->
                     <property name="ERROR_CODE" value=""/>          
                     <property name="ERROR_MESSAGE" value="" expression=""/>
                  </case>
                  <case regex="422">
                     <property name="ERROR_CODE" value=""/>          
                     <property name="ERROR_MESSAGE" value=""  expression=""/>
                  </case>
                  <case regex="404">
                     <property name="ERROR_CODE" value=""/>          
                     <property name="ERROR_MESSAGE" value=""  expression=""/>
                  </case>
                  <case regex="403">
                     <property name="ERROR_CODE" value=""/>          
                     <property name="ERROR_MESSAGE" value=""  expression=""/>
                  </case>
                  <case regex="400">
                     <property name="ERROR_CODE" value=""/>          
                     <property name="ERROR_MESSAGE" value=""  expression=""/>
                  </case>
                  <case regex="500">
                     <property name="ERROR_CODE" value=""/> 
                    <property name="ERROR_MESSAGE" value=""  expression=""/>
                  </case>
                  <default>
                     <property name="ERROR_CODE" expression="$axis2:HTTP_SC"/>
                     <property name="ERROR_MESSAGE" value="" expression=""/>
                  </default>
               </switch>
               <sequence key="faultHandlerSeq" />
            </then>
         </filter>
      <respond />
    </inSequence>
    <outSequence>
     <send/>
    </outSequence>
  </target>
</proxy>
````

**Fault Handler Sequence**

````xml
<sequence xmlns="http://ws.apache.org/ns/synapse" name="faultHandlerSeq">
<property xmln:ns="http://org.apache.synapse/xsd" name="contentTypeValue" expression="get-property('transport', 'Content-Type')"/>
   <filter xmln:ns="http://org.apache.synapse/xsd" xpath="get-property('contentTypeValue') = 'application/json' or 
   get-property('contentTypeValue') = 'text/json'">
      <then>        
          <payloadFactory media-type="json">
               <format> {"error_code":"$1", "error_message":"$2"}
          </format>
                  <args>
                     <arg expression="get-property('ERROR_CODE')" evaluator="xml"/>
                     <arg expression="get-property('ERROR_MESSAGE')" evaluator="xml"/>
                  </args>
               </payloadFactory>
               <property name="messageType" value="application/json" scope="axis2"/>
        </then>
      </filter>
      <filter xmln:ns="http://org.apache.synapse/xsd" xpath="get-property('contentTypeValue') = 'application/xml' or get-property('contentTypeValue') = 'text/xml'">
          <then>
          <payloadFactory media-type="xml">
                  <format>
                     <error_info>
                        <error_code>$1</error_code>
                        <error_message>$2</error_message>
                     </error_info>
                  </format>
                  <args>
                     <arg expression="get-property('ERROR_CODE')" evaluator="xml"/>
                     <arg expression="get-property('ERROR_MESSAGE')" evaluator="xml"/>
                  </args>
               </payloadFactory>
               <property name="messageType" value="text/xml" scope="axis2"/>
        </then>                            
   </filter>
   <respond/>
</sequence>
````
