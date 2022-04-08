package nl.architolk.ea2rdf;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import java.io.File;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Convert {

  private static final Logger LOG = LoggerFactory.getLogger(Convert.class);

  private static Database db;

  private static void readTables() throws Exception {
    Set<String> tables = db.getTableNames();
    for (String tablename : tables) {
      Table table = db.getTable(tablename);
      if (table!=null) {
        System.out.println(table.getName() + " (" + table.getRowCount() + ")");
      } else {
        System.out.println(tablename + " NOT FOUND");
      }
    }
  }

  private static void scanTable(String tablename) throws Exception {
    Table table = db.getTable(tablename);
    for(Column column : table.getColumns()) {
      System.out.println(column.getName()+" ("+column.getType()+")");
    }
  }

  private static void readTable(String tablename) throws Exception {
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

  private static void printTable(String tablename) throws Exception {
    System.out.println("@prefix ea: <http://www.sparxsystems.eu/def/ea#>.");
    Table table = db.getTable(tablename);
    for(Row row : table) {
      System.out.println("<urn:uuid:"+tablename+":"+row.getId()+"> a ea:"+tablename+";");
      for(Column column : table.getColumns()) {
        String columnName = column.getName();
        Object value = row.get(columnName);
        if (value!=null) {
          System.out.println("  ea:"+columnName+" '''"+value+"''';");
        }
      }
      System.out.println(".");
    }
  }

  private static void exportTables() throws Exception {
    System.out.println("@prefix ea: <http://www.sparxsystems.eu/def/ea#>.");
    System.out.println("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.");
    exportPackages();
    exportObjects();
    exportAttributes();
    exportConnectors();
    exportAttributeTags();
    exportObjectProperties();
  }

  private static void exportValue(String name, Object value) {
    if (value!=null) {
      if (value.getClass().equals(java.lang.Boolean.class)) {
        System.out.println("  " + name + " " + value + ";");
      } else {
        System.out.println("  " + name + " '''" + value + "''';");
      }
    }
  }

  private static void exportGUID(String name, Object guid) {
    if (guid!=null) {
      exportValue(name,((String)guid).replaceAll("^\\{(.*)\\}$","$1"));
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
      exportValue("rdfs:comment",row.get("Notes"));
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
      exportObjectRef("ea:package","package",row.get("Package_ID"));
      exportValue("rdfs:comment",row.get("Note"));
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
      exportValue("ea:type",row.get("Type"));
      exportObjectRef("ea:classifier","object",row.get("Classifier")); //Reference to the object that represents the type
      exportValue("ea:stereotype",row.get("Stereotype"));
      exportObjectRef("ea:object","object",row.get("Object_ID"));
      exportValue("rdfs:comment",row.get("Notes"));
      exportValue("ea:lowerBound",row.get("LowerBound"));
      exportValue("ea.upperBound",row.get("UpperBound"));
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
        System.out.println(".");
      }
    }
  }

  public static void main(String[] args) {

    if (args.length>1) {

      LOG.info("Start reading the database");
      try {
        db = DatabaseBuilder.open(new File(args[1]));
        LOG.info("Database version: " + db.getFileFormat());

        if ("-t".equals(args[0])) {
          readTables();
        }
        if ("-s".equals(args[0])) {
          if (args.length>2) {
            scanTable(args[2]);
          }
        }
        if ("-r".equals(args[0])) {
          if (args.length>2) {
            readTable(args[2]);
          }
        }
        if ("-p".equals(args[0])) {
          if (args.length>2) {
            printTable(args[2]);
          }
        }
        if ("-e".equals(args[0])) {
          exportTables();
        }

      } catch (Exception e) {
        LOG.error(e.getMessage());
      }
    }
  }

}
