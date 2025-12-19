package nl.architolk.ea2rdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Convert {

  private static final Logger LOG = LoggerFactory.getLogger(Convert.class);

  public static void main(String[] args) {

    if (args.length>1) {

      LOG.info("Start reading the database");
      try {

        if ("-sql".equals(args[0])) {
          LOG.info("SQLight database");

          if (args.length>2) {
            int argroot = ("-ea".equals(args[1]) ? 2 : 1);

            ConvertSQLight.openDB(args[argroot+1]);

            if ("-t".equals(args[argroot])) {
              ConvertSQLight.readTables();
            }

            if ("-e".equals(args[argroot])) {
              if ("-ea".equals(args[1])) {
                ConvertSQLight.exportEATables();
              } else {
                ConvertSQLight.exportTables();
              }
            }
          }

        }
        else {
          LOG.info("Legacy JET database (Access)");

          int argroot = ("-ea".equals(args[0]) ? 1 : 0);

          if (args.length>argroot+1) {

            if (("-ea".equals(args[0])) && ("-0".equals(args[1]))) {
              argroot++;
            }

            ConvertJET.openDB(args[argroot+1]);

            //It seems that some EA versions use the wrong charset when entering data to the database
            //This will correct the error, by setting the database to this wrong charset
            //This behaviour can be overriden by adding a -0 option
            if (("-ea".equals(args[0]))) {
              if (!("-0".equals(args[1]))) {
                ConvertJET.fixCharset();
              }
            }

            if ("-t".equals(args[argroot])) {
              ConvertJET.readTables();
            }
            if ("-s".equals(args[argroot])) {
              if (args.length>argroot+2) {
                ConvertJET.scanTable(args[argroot+2]);
              }
            }
            if ("-s0".equals(args[argroot])) {
              if (args.length>argroot+2) {
                ConvertJET.scanTable(Integer.parseInt(args[argroot+2]));
              }
            }
            if ("-r".equals(args[argroot])) {
              if (args.length>argroot+2) {
                ConvertJET.readTable(args[argroot+2]);
              }
            }
            if ("-p".equals(args[argroot])) {
              if (args.length>argroot+2) {
                ConvertJET.printTable(args[argroot+2],true);
              }
            }
            if ("-p0".equals(args[argroot])) {
              if (args.length>argroot+2) {
                ConvertJET.printTable(Integer.parseInt(args[argroot+2]));
              }
            }
            if ("-e".equals(args[argroot])) {
              if ("-ea".equals(args[0])) {
                ConvertJET.exportEATables();
              } else {
                ConvertJET.exportTables();
              }
            }
          }
        }

      } catch (Exception e) {
        LOG.error(e.getMessage());
      }
    }
  }

}
