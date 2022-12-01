#java -jar "target/ea2rdf-0.1.0-jar-with-dependencies.jar" -t input/example.eap
#java -jar "target/ea2rdf-0.1.0-jar-with-dependencies.jar" -s input/example.eap t_object
#java -jar target/ea2rdf-*-jar-with-dependencies.jar -e input/example.eap > output/export.ttl
#java -jar "target/ea2rdf-0.1.0-jar-with-dependencies.jar" -e local/example.eap > output/export1.ttl
#java -jar "target/ea2rdf-0.1.0-jar-with-dependencies.jar" -e local/example.eapx > output/export2.ttl
java -jar target/ea2rdf*.jar -e local/zvg.EAP > output/zvg.ttl
