package nl.architolk.ea2rdf;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvertJET {

  private static final Logger LOG = LoggerFactory.getLogger(ConvertJET.class);

  private static Database db;

  public static void openDB(String filename) throws IOException {
    db = DatabaseBuilder.open(new File(filename));
    LOG.info("Database version: " + db.getFileFormat());
    LOG.info("Database charset: " + db.getCharset());
  }

  public static void fixCharset() {
    LOG.warn("Setting database charset to ISO-8859-1");
    db.setCharset(StandardCharsets.ISO_8859_1);
  }

  private static String filterNote(Object note) {
    if (note!=null) {
      byte[] winquotebytes = {-62,-110};
      String winquote = new String(winquotebytes);
      return ((String)note).replace("&#235;","ë").replace("&#233;","é").replace(winquote,"'");
    } else {
      return null;
    }
  }

  private static void printTableNameCount(String tablename, int index) throws Exception {
    Table table = db.getTable(tablename);
    if (table!=null) {
      System.out.println(index + " " + table.getName() + " (" + table.getRowCount() + ")");
    } else {
      System.out.println(index + " " + tablename + " NOT FOUND");
    }
  }

  public static void readTables() throws Exception {
    Set<String> tables = db.getTableNames();
    int index = 0;
    for (String tablename : tables) {
      index++;
      printTableNameCount(tablename,index);
    }
  }

  public static void scanTable(String tablename) throws Exception {
    printTableNameCount(tablename,0);
    Table table = db.getTable(tablename);
    for(Column column : table.getColumns()) {
      System.out.println(column.getName()+" ("+column.getType()+")");
    }
  }

  public static void scanTable(int tableIndex) throws Exception {
    Set<String> tables = db.getTableNames();
    int index = 0;
    for (String tablename : tables) {
      index++;
      if (index==tableIndex) {
        scanTable(tablename);
      }
    }
  }

  public static void readTable(String tablename) throws Exception {
    Table table = db.getTable(tablename);
    for(Row row : table) {
      for(Column column : table.getColumns()) {
        String columnName = column.getName();
        Object value = row.get(columnName);
        System.out.println("Column " + columnName + "(" + column.getType() + "): "
                           + value + " (" + (value==null ? "" : value.getClass()) + ")");
      }
    }
  }

  public static void printTable(String tablename, Boolean prefix) throws Exception {
    if (prefix) {
      System.out.println("@prefix db: <http://microsoft.com/access/db#>.");
    }
    Table table = db.getTable(tablename);
    for(Row row : table) {
      System.out.println("<urn:name:"+tablename+":"+row.getId()+"> a db:"+tablename+";");
      for(Column column : table.getColumns()) {
        String columnName = column.getName();
        exportValue("db:"+column.getName(),row.get(columnName),column.getType());
      }
      System.out.println(".");
    }
  }

  public static void printTable(int tableIndex) throws Exception {
    Set<String> tables = db.getTableNames();
    int index = 0;
    for (String tablename : tables) {
      index++;
      if (index==tableIndex) {
        printTable(tablename, true);
      }
    }
  }

  public static void exportTables() throws Exception {
    Set<String> tables = db.getTableNames();
    Boolean printPrefix = true;
    for (String tablename : tables) {
      printTable(tablename, printPrefix);
      printPrefix = false;
    }
  }

  public static void exportEATables() throws Exception {
    System.out.println("@prefix ea: <http://www.sparxsystems.eu/def/ea#>.");
    System.out.println("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.");
    //Export of model objects
    exportPackages();
    exportObjects();
    exportAttributes();
    exportConnectors();
    exportAttributeTags();
    exportConnectorTags();
    exportObjectProperties();
    exportXRefs();
    //Export of diagram objects
    exportDiagrams();
    exportDiagramObjects();
  }

  private static void exportValue(String name, Object value, DataType datatype) {
    if (value!=null) {
      if (datatype==DataType.GUID) {
        exportGUID(name,value);
      } else {
        exportValue(name,value);
      }
    }
  }

  private static void exportValue(String name, Object value) {
    if (value!=null) {
      if (value.getClass().equals(java.lang.Boolean.class)) {
        System.out.println("  " + name + " " + value + ";");
      } else if (value.getClass().equals(java.lang.Integer.class)) {
        System.out.println("  " + name + " " + value + ";");
      } else if (value.getClass().equals(java.lang.String.class)) {
        String strvalue = (String)value;
        // If strvalue ends with ', we should add one space...
        if (strvalue.endsWith("'")) {
          strvalue = strvalue + " ";
        }
        // Escape escape character, or turtle file will not have the correct syntax
        strvalue = strvalue.replaceAll("\\\\","\\\\\\\\");
        System.out.println("  " + name + " '''" + strvalue + "''';");
      } else {
        System.out.println("  " + name + " '''" + value + "''';");
      }
    }
  }

  private static void exportGUID(String name, Object guid) {
    if (guid!=null) {
      System.out.println("  " + name + " '" + ((String)guid).replaceAll("^\\{(.*)\\}$","$1") + "';");
    }
  }

  private static void exportObjectRef(String name, String type, Object value) {
    if (value!=null) {
      System.out.println("  " + name + " <urn:" + type + ":" + value + ">;");
    }
  }

  private static void exportObjectDef(String type, Object value) {
    System.out.println("<urn:" + type.toLowerCase() + ":" + value + "> a ea:" + type + ";");
  }

  private static void exportPackages() throws Exception {
    //Note: all packages are objects of type "Package". Their guids will be the same!
    Table table = db.getTable("t_package");
    for(Row row : table) {
      exportObjectDef("Package",row.get("Package_ID"));
      exportGUID("ea:guid",row.get("ea_guid"));
      exportValue("rdfs:label",row.get("Name"));
      if (row.getInt("Parent_ID")!=0) {
        exportObjectRef("ea:parent","package",row.get("Parent_ID"));
      }
      exportValue("rdfs:comment",filterNote(row.get("Notes")));
      System.out.println(".");
    }
  }

  private static void exportObjects() throws Exception {
    Table table = db.getTable("t_object");
    for(Row row : table) {
      exportObjectDef("Object",row.get("Object_ID"));
      exportGUID("ea:guid",row.get("ea_guid"));
      exportValue("ea:type",row.get("Object_Type"));
      exportValue("ea:stereotype",row.get("Stereotype"));
      exportValue("rdfs:label",row.get("Name"));
      exportValue("ea:alias",row.get("Alias"));
      if ("0".equals(row.get("Abstract"))) {
        exportValue("ea:abstract",false);
      }
      if ("1".equals(row.get("Abstract"))) {
        exportValue("ea:abstract",true);
      }
      exportObjectRef("ea:package","package",row.get("Package_ID"));
      exportValue("rdfs:comment",filterNote(row.get("Note")));
      if (row.getInt("Classifier")!=0) {
        exportObjectRef("ea:classifier","connector",row.get("Classifier")); //Used with ProxyConnector
      }
      System.out.println(".");
    }
  }

  private static void exportAttributes() throws Exception {
    Table table = db.getTable("t_attribute");
    for(Row row : table) {
      exportObjectDef("Attribute",row.get("ID"));
      exportGUID("ea:guid",row.get("ea_guid"));
      exportValue("rdfs:label",row.get("Name"));
      exportValue("ea:alias",row.get("Style")); //In EA the style column seems to be used as Alias for attributes (!)
      exportValue("ea:type",row.get("Type"));
      exportObjectRef("ea:classifier","object",row.get("Classifier")); //Reference to the object that represents the type
      exportValue("ea:stereotype",row.get("Stereotype"));
      exportObjectRef("ea:object","object",row.get("Object_ID"));
      exportValue("rdfs:comment",filterNote(row.get("Notes")));
      exportValue("ea:lowerBound",row.get("LowerBound"));
      exportValue("ea:upperBound",row.get("UpperBound"));
      System.out.println(".");
    }
  }

  private static void exportConnectors() throws Exception {
    Table table = db.getTable("t_connector");
    for(Row row : table) {
      exportObjectDef("Connector",row.get("Connector_ID"));
      exportGUID("ea:guid",row.get("ea_guid"));
      exportValue("ea:type",row.get("Connector_Type"));
      exportValue("ea:stereotype",row.get("Stereotype"));
      exportValue("rdfs:label",row.get("Name"));
      exportObjectRef("ea:start","object",row.get("Start_Object_ID"));
      exportObjectRef("ea:end","object",row.get("End_Object_ID"));
      exportObjectRef("ea:pdata1","object",row.get("PDATA1")); //Pretty obscure way to link an associationclass
      exportValue("ea:direction",row.get("Direction"));
      exportValue("ea:sourceRole",row.get("SourceRole"));
      exportValue("ea:destRole",row.get("DestRole"));
      exportValue("ea:sourceCard",row.get("SourceCard"));
      exportValue("ea:destCard",row.get("DestCard"));
      exportValue("ea:sourceIsNavigable",row.get("SourceIsNavigable"));
      exportValue("ea:destIsNavigable",row.get("DestIsNavigable"));
      exportValue("ea:sourceIsAggregate",row.get("SourceIsAggregate"));
      exportValue("ea:destIsAggregate",row.get("DestIsAggregate"));
      System.out.println(".");
    }
  }

  private static void exportAttributeTags() throws Exception {
    Table table = db.getTable("t_attributetag");
    for(Row row : table) {
      if (row.get("VALUE")!=null) {
        exportObjectDef("Attributetag",row.get("PropertyID"));
        exportGUID("ea:guid",row.get("ea_guid"));
        exportObjectRef("ea:element","attribute",row.get("ElementID"));
        exportValue("ea:property",row.get("Property"));
        exportValue("ea:value",row.get("VALUE"));
        exportValue("ea:notes",filterNote(row.get("NOTES")));
        System.out.println(".");
      }
    }
  }

  private static void exportConnectorTags() throws Exception {
    Table table = db.getTable("t_connectortag");
    for(Row row : table) {
      if (row.get("VALUE")!=null) {
        exportObjectDef("Connectortag",row.get("PropertyID"));
        exportGUID("ea:guid",row.get("ea_guid"));
        exportObjectRef("ea:element","connector",row.get("ElementID"));
        exportValue("ea:property",row.get("Property"));
        exportValue("ea:value",row.get("VALUE"));
        exportValue("ea:notes",filterNote(row.get("NOTES")));
        System.out.println(".");
      }
    }
  }

  private static void exportObjectProperties() throws Exception {
    Table table = db.getTable("t_objectproperties");
    for(Row row : table) {
      if (row.get("Value")!=null) {
        exportObjectDef("ObjectProperty",row.get("PropertyID"));
        exportGUID("ea:guid",row.get("ea_guid"));
        exportObjectRef("ea:element","object",row.get("Object_ID"));
        exportValue("ea:property",row.get("Property"));
        exportValue("ea:value",row.get("Value"));
        exportValue("ea:notes",filterNote(row.get("Notes")));
        System.out.println(".");
      }
    }
  }

  private static void exportXRefs() throws Exception {
    Table table = db.getTable("t_xref");
    for (Row row : table) {
      if ("CustomProperties".equals(row.get("Name"))) {
        exportObjectDef("XRef",row.getId());
        exportGUID("ea:client",row.get("Client"));
        String description = (String)row.get("Description");
        if (description!=null) {
          String[] params = description.split("@ENDPROP;");
          for (String param : params) {
            String name = param.replaceAll("^(.*)@NAME=(.*)@ENDNAME(.*)$","$2");
            String value = param.replaceAll("^(.*)@VALU=(.*)@ENDVALU(.*)$","$2");
            String type = param.replaceAll("^(.*)@TYPE=(.*)@ENDTYPE(.*)$","$2");
            if (!name.isEmpty()) {
              if (type.equals("Boolean")) {
                exportValue("ea:"+name,value.equals("1"));
              } else {
                exportValue("ea:"+name,value);
              }
            }
          }
        }
        System.out.println(".");
      }
      if ("Stereotypes".equals(row.get("Name"))) {
        exportObjectDef("XRef",row.getId());
        exportGUID("ea:client",row.get("Client"));
        String description = (String)row.get("Description");
        if (description!=null) {
          String[] stereotypes = description.split("@ENDSTEREO;");
          for (String stereotype : stereotypes) {
            String name = stereotype.replaceAll("^(.*)@STEREO;Name=([^;]*);(.*)$","$2");
            if (!name.isEmpty()) {
              exportValue("ea:stereotype",name);
            }
          }
        }
        System.out.println(".");
      }
    }
  }

  private static void exportDiagrams() throws Exception {
    Table table = db.getTable("t_diagram");
    for (Row row : table) {
      exportObjectDef("Diagram",row.get("Diagram_ID"));
      exportGUID("ea:guid",row.get("ea_guid"));
      exportValue("rdfs:label",row.get("Name"));
      exportObjectRef("ea:package","package",row.get("Package_ID"));
      System.out.println(".");
    }
  }

  private static void exportDiagramObjects() throws Exception {
    Table table = db.getTable("t_diagramobjects");
    for (Row row : table) {
      exportObjectDef("DiagramObjects",row.getId());
      exportObjectRef("ea:diagram","diagram",row.get("Diagram_ID"));
      exportObjectRef("ea:object","object",row.get("Object_ID"));
      exportValue("ea:rectTop",row.get("RectTop"));
      exportValue("ea:rectLeft",row.get("RectLeft"));
      exportValue("ea:rectRight",row.get("RectRight"));
      exportValue("ea:rectBottom",row.get("RectBottom"));
      System.out.println(".");
    }
  }

}
