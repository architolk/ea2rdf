#java -jar ../rdf2rdf/target/rdf2rdf*.jar output/zvg.ttl output/zvg-mim.ttl ea2mim.yaml
#java -jar ../rdf2rdf/target/rdf2rdf*.jar output/zvg-mim.ttl output/zvg-onto.ttl mim2onto.yaml
#java -jar ../rdf2rdf/target/rdf2rdf*.jar output/zvg-onto.ttl output/zvg-onto.xml
#java -jar ../rdf2xml/target/rdf2xml*.jar output/zvg-onto.ttl output/zvg.graphml ../rdf2xml/rdf2graphml.xsl
#java -jar ../rdf2xml/target/rdf2xml*.jar output/zvg-onto.ttl output/zvg.graphml ../rdf2xml/rdf2graphml.xsl follow output/zvg-model-edited.graphml
#java -jar target/ea2rdf-*-jar-with-dependencies.jar -ea -e local/zvg.EAP > local/zvg.ttl
java -jar ../rdf2rdf/target/rdf2rdf*.jar -i local/zvg.ttl -o local/zvg-mim.ttl -c ../mimtools/ea2mim.yaml
