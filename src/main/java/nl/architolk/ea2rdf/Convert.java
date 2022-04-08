package nl.architolk.ea2rdf;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableMetaData;
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
      TableMetaData metadata = db.getTableMetaData(tablename);
      if (metadata!=null) {
        LOG.info(metadata.getName());
      } else {
        LOG.info(tablename + " NOT FOUND");
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
  }

  private static void exportPackages() throws Exception {
    Table table = db.getTable("t_package");
    for(Row row : table) {
      System.out.println("<urn:package:"+row.get("Package_ID")+"> a ea:Package;");
      System.out.println("  rdfs:label '''"+row.get("Name")+"''';");
      if (row.getInt("Parent_ID")!=0) {
        System.out.println("  ea:parent <urn:package:"+row.get("Parent_ID")+">;");
      }
      System.out.println(".");
    }
  }

  private static void exportObjects() throws Exception {
    Table table = db.getTable("t_object");
    for(Row row : table) {
      System.out.println("<urn:object:"+row.get("Object_ID")+"> a ea:Object;");
      System.out.println("  ea:type ea:"+row.get("Object_Type")+";");
      if (row.get("Name")!=null) {
        System.out.println("  rdfs:label '''"+row.get("Name")+"''';");
      }
      System.out.println("  ea:package <urn:package:"+row.get("Package_ID")+">;");
      if (row.get("Note")!=null) {
        System.out.println("  rdfs:comment '''"+row.get("Note")+"''';");
      }
      System.out.println(".");
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
