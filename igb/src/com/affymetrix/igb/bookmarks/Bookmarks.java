package com.affymetrix.igb.bookmarks;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GraphSym;
import com.affymetrix.genometryImpl.SymWithProps;
import com.affymetrix.genometryImpl.das2.Das2Type;
import com.affymetrix.genometryImpl.das2.Das2VersionedSource;
import com.affymetrix.genometryImpl.das2.FormatPriorities;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.general.GenericVersion;
import com.affymetrix.genometryImpl.general.SymLoader;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.LoadUtils.ServerType;
import java.util.ArrayList;
import java.util.List;

/**
 * Inserts/modifies data for a DAS2 bookmark
 * Stack like storage of urls/graph ids
 * 
 * @author Ido M. Tamir
 */
final public class Bookmarks {
    
    private final List<SymBookmark> syms = new ArrayList<SymBookmark>();

	public boolean add(GenericFeature feature){
		if(feature == null)
			return false;

		return addToSyms(feature);
	}
	
    /**
     * adds one id
     *
	 * this is a very complicated way to go from a graph to its server
	 * I am sure a simpler way exists, but I could not find it.
	 *
     * @param id the id of the graph
     */
    void addGraph(GraphSym graph){
		if(!checkServerMatch(graph)){
			syms.add(new SymBookmark());
		}
    }

	private boolean checkServerMatch(GraphSym graph) {
		BioSeq seq = graph.getGraphSeq();
	    AnnotatedSeqGroup as = seq.getSeqGroup();
		String gid = graph.getID();
		for(GenericVersion version : as.getEnabledVersions()){
			for( GenericFeature feature : version.getFeatures()){
				if(gid.endsWith(feature.featureName)){
					return addToSyms(feature);
				}
			}
		}
		return false;
	}

	private boolean addToSyms(GenericFeature feature){
		GenericVersion version = feature.gVersion;

		if(version.gServer.serverType == ServerType.DAS2){

			Das2Type type = (Das2Type) feature.typeObj;

			String file_name = type.getID();
			String format =  FormatPriorities.getFormat(type);

			Das2VersionedSource source = (Das2VersionedSource) version.versionSourceObj;
			String server_str = source.getID().substring(0, source.getID().indexOf(version.versionID) - 1);

			syms.add(new SymBookmark(server_str, version.versionID, file_name, format, version.gServer.serverType));

			return true;
		}else if (version.gServer.serverType == ServerType.QuickLoad){
			SymLoader sym = feature.symL;

			if(sym == null)
				return false;

			String format = sym.extension;
			format = format.substring(format.indexOf(".") + 1);

			syms.add(new SymBookmark(version.gServer.URL, version.versionID, sym.uri.toString(), format, version.gServer.serverType));

			return true;
		}

		return false;
	}

    /**
    * returns the current/last parser
    *
    */ 
    private SymBookmark getLast(){
        if(syms.size() > 0){
            return syms.get(syms.size() - 1);
        }
        throw new IndexOutOfBoundsException("No parsers in bookmark");
    }
    
    /**
    * checks if graph is from DAS2 source and sets source_url to the id of the graph
    * which is the path
    * 
    *
    */
    String getSource() {
        if(getLast().isValid()){
            return getLast().getServer();
        }
        return null;
    }
    
    /*
    * returns true if valid url can be constructed
    *
    */
    boolean isValid(){
        return getLast().isValid();
    }
    
    /**
    * from url + region to das2 query
    * 
    *
    */
     static String getDas2Query(SymBookmark bookmark, int start, int end, String chr){
         String cap = bookmark.getCapability() + "?";
         String seg = "segment=" + bookmark.getServer() + "/" + bookmark.getMapping() + "/" + chr + ";";
         String over = "overlaps=" + start + ":" + end + ";";
         String type = "type=" +  bookmark.getPath() + ";" + "format=" + bookmark.getFormat();
         return GeneralUtils.URLEncode(cap + seg + over + type);
     }
	 
    /**
    * sets the das2 and quickload properties of the bookmark and deletes source_url.
    */ 
   public void set(SymWithProps mark_sym) {
        List<String> das2queries = new ArrayList<String>();
        List<String> das2servers = new ArrayList<String>();
		List<String> quickqueries = new ArrayList<String>();
		List<String> quickserver = new ArrayList<String>();
        for(SymBookmark bookmark : this.syms){
            if(bookmark.isValid()){
				if(bookmark.getServerType() == ServerType.DAS2){
					String server = bookmark.getServer();
					int start = (Integer) mark_sym.getProperty("start");
					int end = (Integer) mark_sym.getProperty("end");
					String chr = (String) mark_sym.getProperty("seqid");
					String query = getDas2Query(bookmark, start, end, chr);
					das2servers.add(server);
					das2queries.add(query);
				}else if (bookmark.getServerType() == ServerType.QuickLoad){
					String server = bookmark.getServer();
					quickserver.add(server);
					quickqueries.add(bookmark.getPath());
				}
			}
        }
        mark_sym.setProperty(Bookmark.DAS2_QUERY_URL, das2queries.toArray(new String[das2queries.size()]));
        mark_sym.setProperty(Bookmark.DAS2_SERVER_URL, das2servers.toArray(new String[das2servers.size()]));
		mark_sym.setProperty(Bookmark.QUICK_QUERY_URL, quickqueries.toArray(new String[quickqueries.size()]));
	    mark_sym.setProperty(Bookmark.QUICK_SERVER_URL, quickserver.toArray(new String[quickserver.size()]));
		
    }

   public List<SymBookmark> getSyms(){
	   return syms;
   }
}

