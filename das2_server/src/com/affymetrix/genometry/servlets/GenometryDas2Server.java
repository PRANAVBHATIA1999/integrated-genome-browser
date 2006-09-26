package com.affymetrix.genometry.servlets;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.mortbay.http.*;
import org.mortbay.jetty.servlet.*;

/**
 *  Pure java web server wrapper around GenometryDas2Servlet
 */
public class GenometryDas2Server {

  static int default_server_port = 9092;
  static boolean SHOW_GUI = false;

  public static void main (String[] args) throws Exception {
    final HttpServer server=new HttpServer();
    int server_port = default_server_port;
    if (args.length > 0) {
      server_port = Integer.parseInt(args[0]);
    }
    if (args.length > 1) {
      String data_path = args[1];
      System.setProperty("das2_genometry_server_dir", data_path);
    }
    if (args.length > 2) {
      System.setProperty("das2_maintainer_email", args[2]);
    }

    if (SHOW_GUI) {
      final JFrame frm = new JFrame("Genometry Server");
      java.awt.Container cpane = frm.getContentPane();
      cpane.setLayout(new BorderLayout());
      JButton exitB = new JButton("Quit Server");
      exitB.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            try {
              server.stop(); // should call servlet.destroy() for every servlet that's been init()ed
            }
            catch (Exception ex) { ex.printStackTrace(); }
            frm.hide();
            System.exit(0);
          }
        }
      );
      cpane.add("Center", exitB);
      frm.setSize(300, 80);
      //      frm.pack();
      frm.show();
    }

    // Create a port listener
    SocketListener listener = new SocketListener();
    listener.setPort(server_port);
    server.addListener(listener);

    // Create a context
    HttpContext context = new HttpContext();
    //    context.setContextPath("/mystuff/*");
    context.setContextPath("/");

    // Create a servlet container
    ServletHandler servlets = new ServletHandler();
    context.addHandler(servlets);

    // Map a servlet onto the container
    ServletHolder das_holder =
      servlets.addServlet("GenometryDas2Servlet", "/das2/*",
                          "com.affymetrix.genometry.servlets.GenometryDas2Servlet");
    das_holder.setInitOrder(1);  // ensure servlet init() is called on startup

    // Serve static content from the context
    //    String home = System.getProperty("jetty.home",".");
    //    context.setResourceBase(home+"/demo/webapps/jetty/tut/");
    //    context.setResourceBase("C:/JavaExtras/jetty/Jetty-4.1.0/demo/webapps/jetty/tut/");
    //    context.addHandler(new ResourceHandler());
    server.addContext(context);

    // Start the http server
    server.start();

    GenometryDas2Servlet das_servlet = (GenometryDas2Servlet)das_holder.getServlet();
    //    das_servlet.addCommandPlugin("psd_query", "com.affymetrix.genometry.servlets.ProbeSetDisplayPlugin");
    //    das_servlet.addCommandPlugin("proximity", "com.affymetrix.genometry.servlets.ProximityQueryPlugin");
    if (args.length > 3) {
      String xml_base = args[3];
      das_servlet.setXmlBase(xml_base);
    }

  }
}


