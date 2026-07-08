package nl.architolk.ea2rdf;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvertSQLight {

  private static final Logger LOG = LoggerFactory.getLogger(ConvertSQLight.class);

  private static Connection db;

  public static void openDB(String filename) throws SQLException {
    db = DriverManager.getConnection("jdbc:sqlite:" + filename);
    DatabaseMetaData databaseMetaData = db.getMetaData();

    LOG.info("Database product: " + databaseMetaData.getDatabaseProductName());
    LOG.info("Database version: " + databaseMetaData.getDatabaseProductVersion());
  }

  private static void printTableNameCount(String tablename, int index) throws Exception {
    System.out.println(index + " " + tablename);
    /*
    Table table = db.getTable(tablename);
    if (table!=null) {
      System.out.println(index + " " + table.getName() + " (" + table.getRowCount() + ")");
    } else {
      System.out.println(index + " " + tablename + " NOT FOUND");
    }
    */
  }

  public static void readTables() throws Exception {
    DatabaseMetaData databaseMetaData = db.getMetaData();
    ResultSet resultSet = databaseMetaData.getTables(null, null, null, new String[]{"TABLE"});
    int index = 0;
    while (resultSet.next()) {
      index++;
      String tablename = resultSet.getString("TABLE_NAME");
      //String remarks = resultSet.getString("REMARKS");
      printTableNameCount(tablename,index);
    }
  }

  public static void exportTables() throws Exception {
    DatabaseMetaData databaseMetaData = db.getMetaData();
    ResultSet resultSet = databaseMetaData.getTables(null, null, null, new String[]{"TABLE"});
    Boolean printPrefix = true;
    while (resultSet.next()) {
      String tablename = resultSet.getString("TABLE_NAME");
      //printTable(tablename, printPrefix);
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
    exportDiagramLinks();
  }

  private static void exportValue(String name, Object value) {
    if (value!=null) {
      if (value.getClass().equals(java.lang.Boolean.class)) {
        System.out.println("  " + name + " " + value + ";");
      } else if (value.getClass().equals(java.lang.Integer.class)) {
        System.out.println("  " + name + " " + value + ";");
      } else if (value.getClass().equals(java.lang.Double.class)) {
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

  private static String filterNote(Object note) {
    if (note!=null) {
      byte[] winquotebytes = {-62,-110};
      String winquote = new String(winquotebytes);
      return ((String)note).replace("&#235;","ë").replace("&#233;","é").replace(winquote,"'");
    } else {
      return null;
    }
  }

  private static void exportObjectDef(String type, Object value) {
    System.out.println("<urn:" + type.toLowerCase() + ":" + value + "> a ea:" + type + ";");
  }

  private static void exportPackages() throws Exception {
    //Note: all packages are objects of type "Package". Their guids will be the same!
    Statement stmt = db.createStatement();
    ResultSet rs = stmt.executeQuery("select * from t_package");
    while (rs.next()) {
      exportObjectDef("Package",rs.getObject("Package_ID"));
      exportGUID("ea:guid",rs.getObject("ea_guid"));
      exportValue("rdfs:label",rs.getObject("Name"));
      if (rs.getInt("Parent_ID")!=0) {
        exportObjectRef("ea:parent","package",rs.getObject("Parent_ID"));
      }
      exportValue("rdfs:comment",filterNote(rs.getObject("Notes")));
      System.out.println(".");
    }
  }

  private static void exportObjects() throws Exception {
    Statement stmt = db.createStatement();
    ResultSet rs = stmt.executeQuery("select * from t_object");
    while (rs.next()) {
      exportObjectDef("Object",rs.getObject("Object_ID"));
      exportGUID("ea:guid",rs.getObject("ea_guid"));
      exportValue("ea:type",rs.getObject("Object_Type"));
      exportValue("ea:stereotype",rs.getObject("Stereotype"));
      exportValue("rdfs:label",rs.getObject("Name"));
      exportValue("ea:alias",rs.getObject("Alias"));
      if ("0".equals(rs.getObject("Abstract"))) {
        exportValue("ea:abstract",false);
      }
      if ("1".equals(rs.getObject("Abstract"))) {
        exportValue("ea:abstract",true);
      }
      exportObjectRef("ea:package","package",rs.getObject("Package_ID"));
      exportValue("rdfs:comment",filterNote(rs.getObject("Note")));
      if (rs.getInt("Classifier")!=0) {
        exportObjectRef("ea:classifier","connector",rs.getObject("Classifier")); //Used with ProxyConnector
      }
      System.out.println(".");
    }
  }

  private static void exportAttributes() throws Exception {
    Statement stmt = db.createStatement();
    ResultSet rs = stmt.executeQuery("select * from t_attribute");
    while (rs.next()) {
      exportObjectDef("Attribute",rs.getObject("ID"));
      exportGUID("ea:guid",rs.getObject("ea_guid"));
      exportValue("rdfs:label",rs.getObject("Name"));
      exportValue("ea:alias",rs.getObject("Style")); //In EA the style column seems to be used as Alias for attributes (!)
      exportValue("ea:type",rs.getObject("Type"));
      exportObjectRef("ea:classifier","object",rs.getObject("Classifier")); //Reference to the object that represents the type
      exportValue("ea:stereotype",rs.getObject("Stereotype"));
      exportObjectRef("ea:object","object",rs.getObject("Object_ID"));
      exportValue("rdfs:comment",filterNote(rs.getObject("Notes")));
      exportValue("ea:lowerBound",rs.getObject("LowerBound"));
      exportValue("ea:upperBound",rs.getObject("UpperBound"));
      System.out.println(".");
    }
  }

  private static void exportConnectors() throws Exception {
    Statement stmt = db.createStatement();
    ResultSet rs = stmt.executeQuery("select * from t_connector");
    while (rs.next()) {
      exportObjectDef("Connector",rs.getObject("Connector_ID"));
      exportGUID("ea:guid",rs.getObject("ea_guid"));
      exportValue("ea:type",rs.getObject("Connector_Type"));
      exportValue("ea:stereotype",rs.getObject("Stereotype"));
      exportValue("rdfs:label",rs.getObject("Name"));
      exportObjectRef("ea:start","object",rs.getObject("Start_Object_ID"));
      exportObjectRef("ea:end","object",rs.getObject("End_Object_ID"));
      exportObjectRef("ea:pdata1","object",rs.getObject("PDATA1")); //Pretty obscure way to link an associationclass
      exportValue("ea:direction",rs.getObject("Direction"));
      exportValue("ea:sourceRole",rs.getObject("SourceRole"));
      exportValue("ea:destRole",rs.getObject("DestRole"));
      exportValue("ea:sourceCard",rs.getObject("SourceCard"));
      exportValue("ea:destCard",rs.getObject("DestCard"));
      exportValue("ea:sourceIsNavigable",rs.getObject("SourceIsNavigable"));
      exportValue("ea:destIsNavigable",rs.getObject("DestIsNavigable"));
      exportValue("ea:sourceIsAggregate",rs.getObject("SourceIsAggregate"));
      exportValue("ea:destIsAggregate",rs.getObject("DestIsAggregate"));
      System.out.println(".");
    }
  }

  private static void exportAttributeTags() throws Exception {
    Statement stmt = db.createStatement();
    ResultSet rs = stmt.executeQuery("select * from t_attributetag");
    while (rs.next()) {
      if (rs.getObject("VALUE")!=null) {
        exportObjectDef("Attributetag",rs.getObject("PropertyID"));
        exportGUID("ea:guid",rs.getObject("ea_guid"));
        exportObjectRef("ea:element","attribute",rs.getObject("ElementID"));
        exportValue("ea:property",rs.getObject("Property"));
        exportValue("ea:value",rs.getObject("VALUE"));
        exportValue("ea:notes",filterNote(rs.getObject("NOTES")));
        System.out.println(".");
      }
    }
  }

  private static void exportConnectorTags() throws Exception {
    Statement stmt = db.createStatement();
    ResultSet rs = stmt.executeQuery("select * from t_connectortag");
    while (rs.next()) {
      if (rs.getObject("VALUE")!=null) {
        exportObjectDef("Connectortag",rs.getObject("PropertyID"));
        exportGUID("ea:guid",rs.getObject("ea_guid"));
        exportObjectRef("ea:element","connector",rs.getObject("ElementID"));
        exportValue("ea:property",rs.getObject("Property"));
        exportValue("ea:value",rs.getObject("VALUE"));
        exportValue("ea:notes",filterNote(rs.getObject("NOTES")));
        System.out.println(".");
      }
    }
  }

  private static void exportObjectProperties() throws Exception {
    Statement stmt = db.createStatement();
    ResultSet rs = stmt.executeQuery("select * from t_objectproperties");
    while (rs.next()) {
      if (rs.getObject("Value")!=null) {
        exportObjectDef("ObjectProperty",rs.getObject("PropertyID"));
        exportGUID("ea:guid",rs.getObject("ea_guid"));
        exportObjectRef("ea:element","object",rs.getObject("Object_ID"));
        exportValue("ea:property",rs.getObject("Property"));
        exportValue("ea:value",rs.getObject("Value"));
        exportValue("ea:notes",filterNote(rs.getObject("Notes")));
        System.out.println(".");
      }
    }
  }

  private static void exportXRefs() throws Exception {
    Statement stmt = db.createStatement();
    ResultSet rs = stmt.executeQuery("select * from t_xref");
    while (rs.next()) {
      if ("CustomProperties".equals(rs.getObject("Name"))) {
        exportObjectDef("XRef",rs.getRow());
        exportGUID("ea:client",rs.getObject("Client"));
        String description = (String)rs.getObject("Description");
        if (description!=null) {
          if (description.contains("@NAME")) {
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
        }
        System.out.println(".");
      }
      if ("Stereotypes".equals(rs.getObject("Name"))) {
        exportObjectDef("XRef",rs.getRow());
        exportGUID("ea:client",rs.getObject("Client"));
        String description = (String)rs.getObject("Description");
        if (description!=null) {
          if (description.contains("Name")) {
            String[] stereotypes = description.split("@ENDSTEREO;");
            for (String stereotype : stereotypes) {
              String name = stereotype.replaceAll("^(.*)@STEREO;Name=([^;]*);(.*)$","$2");
              if (!name.isEmpty()) {
                exportValue("ea:stereotype",name);
              }
            }
          }
        }
        System.out.println(".");
      }
    }
  }

  private static void exportDiagrams() throws Exception {
    Statement stmt = db.createStatement();
    ResultSet rs = stmt.executeQuery("select * from t_diagram");
    while (rs.next()) {
      exportObjectDef("Diagram",rs.getObject("Diagram_ID"));
      exportGUID("ea:guid",rs.getObject("ea_guid"));
      exportValue("rdfs:label",rs.getObject("Name"));
      exportObjectRef("ea:package","package",rs.getObject("Package_ID"));
      System.out.println(".");
    }
  }

  private static String colorRefToHex(long colorRef) {
      int r = (int)(colorRef & 0xFF);
      int g = (int)((colorRef >> 8) & 0xFF);
      int b = (int)((colorRef >> 16) & 0xFF);
      return String.format("#%02X%02X%02X", r, g, b);
  }

  private static void exportDiagramObjects() throws Exception {
    Statement stmt = db.createStatement();
    ResultSet rs = stmt.executeQuery("select * from t_diagramobjects");
    while (rs.next()) {
      exportObjectDef("DiagramObject",rs.getRow());
      exportObjectRef("ea:diagram","diagram",rs.getObject("Diagram_ID"));
      exportObjectRef("ea:object","object",rs.getObject("Object_ID"));
      exportValue("ea:rectTop",rs.getObject("RectTop"));
      exportValue("ea:rectLeft",rs.getObject("RectLeft"));
      exportValue("ea:rectRight",rs.getObject("RectRight"));
      exportValue("ea:rectBottom",rs.getObject("RectBottom"));
      String style = (String)rs.getObject("ObjectStyle");
      if (style.contains("BCol")) {
        String bcolstr = style.replaceAll("^(.*)BCol=([^;]*);(.*)$","$2");
        if (bcolstr.length()>2) {
          long bcol = Long.parseLong(bcolstr);
          exportValue("ea:backgroundColor",colorRefToHex(bcol));
        }
      }
      System.out.println(".");
    }
  }

  private static void exportDiagramLinks() throws Exception {
    Statement stmt = db.createStatement();
    ResultSet rs = stmt.executeQuery("select * from t_diagramlinks");
    while (rs.next()) {
      exportObjectDef("DiagramLink",rs.getRow());
      exportObjectRef("ea:diagram","diagram",rs.getObject("DiagramID"));
      exportObjectRef("ea:connector","connector",rs.getObject("ConnectorID"));
      String path = (String)rs.getObject("Path");
      if (path!=null) {
        Boolean needsComma = false;
        System.out.print("  ea:path \"LINESTRING(");
        String[] points = path.split(";");
        for (String point : points) {
          String[] xy = point.split(":");
          if (xy.length==2) {
            if (needsComma) {
              System.out.print(",");
            } else {
              needsComma = true;
            }
            System.out.printf("%.1f %.1f",Double.parseDouble(xy[0]),Double.parseDouble(xy[1]));
          }
        }
        System.out.println(")\";");
      }
      String geometry = (String)rs.getObject("Geometry");
      if (geometry!=null) {
        if (geometry.contains("SX=") && geometry.contains("SY=") && geometry.contains("EX=") && geometry.contains("EY")) {
          String sx = geometry.replaceAll("^(.*)SX=([^;]*);(.*)$","$2");
          String sy = geometry.replaceAll("^(.*)SY=([^;]*);(.*)$","$2");
          String ex = geometry.replaceAll("^(.*)EX=([^;]*);(.*)$","$2");
          String ey = geometry.replaceAll("^(.*)EY=([^;]*);(.*)$","$2");
          if ((sx!="") && (sy!="") && (ex!="") && (ey!="")) {
            exportValue("ea:startX",Double.parseDouble(sx));
            exportValue("ea:startY",Double.parseDouble(sy));
            exportValue("ea:endX",Double.parseDouble(ex));
            exportValue("ea:endY",Double.parseDouble(ey));
          }
        }
      }
      System.out.println(".");
    }
  }

}
