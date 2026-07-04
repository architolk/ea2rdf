#java -jar "target/ea2rdf-0.1.0-jar-with-dependencies.jar" -t input/example.eap
#java -jar "target/ea2rdf-0.1.0-jar-with-dependencies.jar" -s input/example.eap t_object
#java -jar target/ea2rdf-*-jar-with-dependencies.jar -e input/example.eap > output/export.ttl
#java -jar "target/ea2rdf-0.1.0-jar-with-dependencies.jar" -e local/example.eap > output/export1.ttl
#java -jar "target/ea2rdf-0.1.0-jar-with-dependencies.jar" -e local/example.eapx > output/export2.ttl
#java -jar target/ea2rdf*.jar -e local/zvg.EAP > output/zvg.ttl
#java -jar target/ea2rdf-1.2.2-jar-with-dependencies.jar -ea -0 -e local/imx-geo.eapx > output/imx-geo.ttl
#java -jar target/ea2rdf-1.2.3-jar-with-dependencies.jar -ea -e input/example.eap > output/example.ttl
#java -jar target/ea2rdf-*-jar-with-dependencies.jar -ea -e input/fietsenwinkel.eap > output/fietsenwinkel.ttl
#java -jar ../rdf2rdf/target/rdf2rdf.jar output/fietsenwinkel.ttl output/fietsenwinkel-mim.ttl ../mimtools/ea2mim.yaml
#java -jar ../rdf2xml/target/rdf2xml.jar output/fietsenwinkel-mim.ttl output/fietsenwinkel-mim.graphml ../mimtools/mim2graphml.xsl
java -jar target/ea2rdf-*-jar-with-dependencies.jar -sql -ea -e input/test-sql-light.qea > output/fietsenwinkel-test.ttl
#java -jar target/ea2rdf-*-jar-with-dependencies.jar -sql -ea -e input/Model.xea > input/Model.ttl


#java -jar target/ea2rdf-*-jar-with-dependencies.jar -ea -e local/imkl_v2.0.eap > local/imkl_v2.0.ttl
#java -jar target/ea2rdf-*-jar-with-dependencies.jar -e local/imkl_v2.0.eap > local/imkl_v2.0-tables.ttl
