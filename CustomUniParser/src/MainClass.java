import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.util.NutchConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class MainClass {

	private static final int NONE = 0;
	private static final int UNI_DEFAULT = 1;
	private static final int UNI_WIWI = 2;
	private static final int UNI_NAT_FAK_I = 3;
	private static final int UNI_PHYSICS = 4;
	private static final int UNI_BIOLOGY = 5;
	private static final int UNI_ETHIKKOM = 6;
	private static final int UNI_OC_CHEMIE = 7;
	private static final int PDF = 8;
	
	private static final String[] segments = new String[]{"20140705140223", "20140705140402", "20140705141227", "20140705154615", "20140705172323", "20140705182415", "20140705183924", "20140705191039", "20140705192432", "20140705193115",
		"20140705193929", "20140705194007", "20140706013723", "20140706122905", "20140706133641", "20140706144319", "20140706195104", "20140707144127"};
	
	public static void main(String[] args) throws IOException {
		//for(int i = 0; i < segments.length; i++) {
		for(int i = 0; i < 1; i++) {	
			oldMain(segments[i]);
		}
		
	}
	
	private static void oldMain (String segmentNum) throws IOException {
		Configuration conf = NutchConfiguration.create();
        Options opts = new Options();
        GenericOptionsParser parser = new GenericOptionsParser(conf, opts, new String[]{"C:/cygwin64/home/apache-nutch-1.4-bin/runtime/local/bin/crawl44/segments/"+segmentNum});
        String[] remainingArgs = parser.getRemainingArgs();
        FileSystem fs = FileSystem.get(conf);
        String segment = remainingArgs[0];
        Path file = new Path(segment, Content.DIR_NAME + "/part-00000/data");
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, file, conf);
        Text key = new Text();
        Content content = new Content();
        
        
        File folder = null, newFile = null;
        FileWriter w = null;
        
        // Loop through sequence files
        int index = 0;
        
        MainClass bla = new MainClass();
        
        String output = "";
        
        boolean started = false;
        
        while (reader.next(key, content)) {
        	if(index == 0) {
        		if(started) {
        			w.write("\n</add>");
        			w.close();
        		}
	            folder = new File("C:/Users/Jonathan/Desktop/IRIndexXMLs");
	            newFile = new File("C:/Users/Jonathan/Desktop/IRIndexXMLs/index"+folder.listFiles().length+".xml");
	            w = new FileWriter(newFile);
	            w.write("<add>\n");
	            started=true;
	            
	        }
        	index++;
        	if(index == 100) index = 0;
            try {
            	Document doc = Jsoup.parse(new String(content.getContent(), "utf-8"));
            //   Document doc = Jsoup.parse(new URL("http://www.ur.de/index.html"), 1000);
            	String s = bla.parseDocument(doc, content.getBaseUrl());
            	if(!s.equals("")) {
            		System.out.println("Writing to file: " + newFile.getAbsolutePath());
            		w.write(s);
            	}
            } catch (Exception e) {
            }
        }

		w.write("\n</add>");
		w.close();
	}
	
	private String parseDocument(Document doc, String url) {
		if(!url.endsWith("/") && !url.endsWith("html") && !url.endsWith("htm") && !url.endsWith("php") && !url.endsWith("jsp") && !url.endsWith(".de") && !url.endsWith(".pdf")) return "";
		int cms = findOutWhichCMS(doc, url);
		String title = doc.select("title").text();
		
		doc = Jsoup.parse(removeUnnecessaryParts(doc, cms));
		
		return this.saveToDoc(title, url, doc.text())+"\n";
	}
	
	private int findOutWhichCMS(Document doc, String url) {
		if(url.endsWith(".pdf")) return PDF;
		if(url.contains("www-oc.chemie.")) return UNI_OC_CHEMIE;
		if(url.contains("www-wiwi.")) return UNI_WIWI;
		if(url.contains("www.physik.")) return UNI_PHYSICS;
		if(url.contains("www.biologie.")) return UNI_BIOLOGY;
		if(url.contains("ethikkommission.")) return UNI_ETHIKKOM;
		
		Elements meta = doc.getElementsByTag("meta");
		// UNI: MAIN
		boolean b1 = false, b2 = false;
		for(int i = 0; i < meta.size(); i++) {
			Element el = meta.get(i);
			Attributes ats = el.attributes();
			String atsString = ats.html();
			if(atsString.contains("name=\"designer\" content=\"bauer &amp; bauer medienbuero | www.headwork.de\"")) b1 = true;
			if(atsString.contains("name=\"GENERATOR\" content=\"IMPERIA 8.6.0.26\"")) b2 = true;
		}
		if(b1 && b2) return UNI_DEFAULT;
		
		
		return NONE;
	}
	
	private String removeUnnecessaryParts(Document doc, int cms) {	
		String htmlString = "";
				
		if(cms == UNI_DEFAULT) {
			htmlString += doc.getElementsByTag("head").html();

			doc.select("div.navigation").remove();
			doc.select("div.header").remove();
			
			htmlString += doc.html();
		} else {
			htmlString += doc.html();
		}
		
		return htmlString + "";
	}
	
	private String saveToDoc(String title, String url, String content) { //, String important) {
		String xmlDoc = "<doc>\n";

		xmlDoc +="<field name=\"id\">"+url+"</field>\n";
		xmlDoc +="<field name=\"title\">"+title+"</field>\n";
		xmlDoc +="<field name=\"url\">"+url+"</field>\n";
		xmlDoc +="<field name=\"content\">"+content+"</field>\n";
		
		return xmlDoc +="</doc>";
	}
}