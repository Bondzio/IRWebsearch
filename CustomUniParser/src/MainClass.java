import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.solr.search.Grouping.TotalCount;
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
	
	private static final String[] testURLS = new String[] {
	//	"http://wiwi-app.uni-regensburg.de/spindvergabe/","http://wiwi-app.uni-regensburg.de/spindvergabe/mieten.php","http://www.biologie.uni-regensburg.de/Zoologie/Schneuwly/Forschung/index.html","http://www.biologie.uni-regensburg.de/Zoologie/Strohm/beewolf_pheromone/index.html","http://www.physik.uni-regensburg.de/edv/infra/webdoku.phtml","http://www.uni-regensburg.de/bayhost/stipendien/","http://www.uni-regensburg.de/bayhost/stipendien/","http://www.uni-regensburg.de/bayhost/stipendien/incoming/index.html","http://www.uni-regensburg.de/chancengleichheit/familie/uni/index.html","http://www.uni-regensburg.de/chancengleichheit/frauenfoerderung/index.html","http://www.uni-regensburg.de/chancengleichheit/gleichstellung/index.html","http://www.uni-regensburg.de/chancengleichheit/index.html","http://www.uni-regensburg.de/corporate-design/vorlagen/","http://www.uni-regensburg.de/Einrichtungen/Verwaltung/Abteilung-I/Abt.I-Kr/Zwischenpr/206zp6vo.html","http://www.uni-regensburg.de/europaeum/kooperation/netzwerk/partner/krakow/index.html","http://www.uni-regensburg.de/fakultaeten/index.html","http://www.uni-regensburg.de/Fakultaeten/phil_Fak_III/Geschichte/dar-hitler-prozess1.html","http://www.uni-regensburg.de/international/","http://www.uni-regensburg.de/international/ausland-studieren/austauschprogramme-europa/bewerbung/index.html","http://www.uni-regensburg.de/international/ausland-studieren/austauschprogramme-europa/bewerbung/sprachkenntnisse/index.html","http://www.uni-regensburg.de/international/ausland-studieren/austauschprogramme-europa/erasmus-programm/index.html","http://www.uni-regensburg.de/international/ausland-studieren/austauschprogramme-europa/index.html","http://www.uni-regensburg.de/international/ausland-studieren/austauschprogramme-europa/vorbereitung-abreise/beurlaubung/index.html","http://www.uni-regensburg.de/international/ausland-studieren/austauschprogramme-europa/vorbereitung-abreise/index.html","http://www.uni-regensburg.de/international/ausland-studieren/faecherinfo/geisteswissenschaften/index.html","http://www.uni-regensburg.de/international/index.html","http://www.uni-regensburg.de/international/internationale-studierende/index.html","http://www.uni-regensburg.de/international/internationale-studierende/wohnungssuche/","http://www.uni-regensburg.de/international/internationale-studierende/wohnungssuche/wohnheime/index.html","http://www.uni-regensburg.de/international/warum-regensburg/","http://www.uni-regensburg.de/international/warum-regensburg/allgemeine-infos/index.html","http://www.uni-regensburg.de/international/warum-regensburg/kontakt/index.html","http://www.uni-regensburg.de/international/warum-regensburg/nationaler-kodex/index.html","http://www.uni-regensburg.de/international/warum-regensburg/studienangebote/index.html","http://www.uni-regensburg.de/kontakt/gebaeudeplaene/index.html","http://www.uni-regensburg.de/kontakt/lageplan/","http://www.uni-regensburg.de/kultur-freizeit/","http://www.uni-regensburg.de/law/faculty/administration/index.html","http://www.uni-regensburg.de/medizin/fakultaet/organisation-einrichtungen/index.html","http://www.uni-regensburg.de/medizin/orthopaedie/forschung/schwerpunkte/auszeichnungen/index.html","http://www.uni-regensburg.de/mensa/","http://www.uni-regensburg.de/musik/index.html","http://www.uni-regensburg.de/philosophie-kunst-geschichte-gesellschaft/fakultaet/index.html","http://www.uni-regensburg.de/philosophie-kunst-geschichte-gesellschaft/geschichte/institut/uni-raumlageplan/","http://www.uni-regensburg.de/philosophie-kunst-geschichte-gesellschaft/vergleichende-politikwissenschaft-mittel-osteuropa/kusznir/index.html","http://www.uni-regensburg.de/pressearchiv/017415.html","http://www.uni-regensburg.de/pressearchiv/pressemitteilung/302255.html","http://www.uni-regensburg.de/rechenzentrum/index.html","http://www.uni-regensburg.de/rechenzentrum/software/software-fuer-studierende/","http://www.uni-regensburg.de/rechenzentrum/software/windows/","http://www.uni-regensburg.de/rechenzentrum/support/drucken/preise/index.html","http://www.uni-regensburg.de/rechenzentrum/support/smartphone-synchronisation/blackberry/index.html","http://www.uni-regensburg.de/rechenzentrum/support/software-fuer-studierende/","http://www.uni-regensburg.de/rechenzentrum/unser-rz/stellen/index.html","http://www.uni-regensburg.de/rechtswissenschaft/buergerliches-recht/loehnig/kontakt/index.html","http://www.uni-regensburg.de/rechtswissenschaft/najur/faq/","http://www.uni-regensburg.de/sport/index.html","http://www.uni-regensburg.de/sprache-literatur-kultur/fakultaet/","http://www.uni-regensburg.de/sprache-literatur-kultur/fakultaet/institute/index.html","http://www.uni-regensburg.de/sprache-literatur-kultur/information-medien-sprache-kultur/index.html","http://www.uni-regensburg.de/sprache-literatur-kultur/informationswissenschaft/forschung/index.html","http://www.uni-regensburg.de/sprache-literatur-kultur/informationswissenschaft/fuer-studieninteressierte/bewerbung/index.html","http://www.uni-regensburg.de/sprache-literatur-kultur/informationswissenschaft/fuer-studieninteressierte/index.html","http://www.uni-regensburg.de/sprache-literatur-kultur/informationswissenschaft/fuer-studierende/index.html","http://www.uni-regensburg.de/sprache-literatur-kultur/informationswissenschaft/iiix-2014/index.html","http://www.uni-regensburg.de/sprache-literatur-kultur/informationswissenschaft/index.html","http://www.uni-regensburg.de/sprache-literatur-kultur/informationswissenschaft/kontakt/index.html","http://www.uni-regensburg.de/sprache-literatur-kultur/informationswissenschaft/mitarbeiter/florian-meier-m-a-/index.html","http://www.uni-regensburg.de/sprache-literatur-kultur/informationswissenschaft/mitarbeiter/index.html","http://www.uni-regensburg.de/sprache-literatur-kultur/informationswissenschaft/offene-stellen/index.html","http://www.uni-regensburg.de/studium/bewerbung-einschreibung/getting-started/index.html","http://www.uni-regensburg.de/studium/bewerbung-einschreibung/index.html","http://www.uni-regensburg.de/studium/deutschlandstipendium/","http://www.uni-regensburg.de/studium/pruefungsverwaltung/geisteswissenschaften/","http://www.uni-regensburg.de/studium/studentenkanzlei/","http://www.uni-regensburg.de/studium/studentenkanzlei/antraege-bescheinigungen/","http://www.uni-regensburg.de/studium/studentenkanzlei/bewerbung-einschreibung//einschreibung/index.html","http://www.uni-regensburg.de/studium/studentenkanzlei/bewerbung-einschreibung/bewerbung/index.html","http://www.uni-regensburg.de/studium/studentenkanzlei/bewerbung-einschreibung/rueckmeldung/index.html","http://www.uni-regensburg.de/studium/studienfoerderung/begabtenfoerderungswerke/index.html","http://www.uni-regensburg.de/studium/studienfoerderung/buechergeld/index.html","http://www.uni-regensburg.de/studium/studienfoerderung/foerderpreise-wettbewerbe/","http://www.uni-regensburg.de/studium/studienfoerderung/foerderpreise-wettbewerbe/index.html","http://www.uni-regensburg.de/studium/studienfoerderung/index.html","http://www.uni-regensburg.de/studium/studienfoerderung/sonstige-foerderungsmoeglichkeiten/index.html","http://www.uni-regensburg.de/studium/zentrale-studienberatung/kontakt/index.html","http://www.uni-regensburg.de/studium/zentrale-studienberatung/veranstaltungen/einfuehrungen/index.html","http://www.uni-regensburg.de/technische-zentrale/abteilung-referate/facility-management-v-4/fundbuero/index.html","http://www.uni-regensburg.de/theologie/alte-kirchengeschichte-patrologie/bafoeg/index.html","http://www.uni-regensburg.de/Universitaet/Jahresbericht/aktuell/1-2.html","http://www.uni-regensburg.de/universitaet/sim/","http://www.uni-regensburg.de/universitaet/sprecherrat/service/index.html","http://www.uni-regensburg.de/universitaet/sprecherrat/wohnungsboerse/index.html","http://www.uni-regensburg.de/universitaet/stellenausschreibungen/lehre-forschung-verwaltung/index.html","http://www.uni-regensburg.de/universitaet/stellenausschreibungen/professuren/index.html","http://www.uni-regensburg.de/verwaltung/formulare/einstellung-berufungen/index.html","http://www.uni-regensburg.de/verwaltung/organigramm/abteilung-1/pruefungssekretariat/","http://www.ur.de/","http://www-app.uni-regensburg.de/Einrichtungen/Sportzentrum/ahs/","http://www-app.uni-regensburg.de/Einrichtungen/TZ/famos/hoersaele/","http://www-cgi.uni-regensburg.de/Einrichtungen/Sportzentrum/cms/sportzentrum.html","http://www-cgi.uni-regensburg.de/Fakultaeten/WiWi/roeder/downloadsgeneral.htm","http://www-huge.uni-regensburg.de/Veranstaltungen/Laborseminar/Work-in-Progress_winter_term_2013_2014.shtml","http://www-oc.chemie.uni-regensburg.de/studium/index_en.html","http://www-wiwi.uni-regensburg.de/Forschung/Auszeichnungen/index.html.de","http://www-wiwi.uni-regensburg.de/Forschung/Publikationen/Lehrstuhl-Hruschka.html.de","http://www-wiwi.uni-regensburg.de/Institute/BWL/Dowling/Forschung/Veroeffentlichungen/index.html.de","http://www-wiwi-cms.uni-regensburg.de/Personen/Wolfgang-Buchholz.html.de","https://elearning.uni-regensburg.de","https://lsf.uni-regensburg.de/","https://studierendenportal.uni-regensburg.de/qisserver/pages/cs/sys/portal/hisinoneStartPage.faces?first=yes","https://www.uni-regensburg.de/rechenzentrum/software/software-fuer-studierende/corel-edustore/index.html","https://www.uni-regensburg.de/rechenzentrum/unser-rz/index.html","https://www-flexnow.uni-regensburg.de/Flexnow/DiensteFrames.htm"
		"https://lsf.uni-regensburg.de/qisserver/rds?state=user&type=0"
	};
	
	
	
	public static void main(String[] args) throws IOException {
		//for(int i = 0; i < segments.length; i++) {
		for(int i = 0; i < segments.length; i++) {	
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
        
        
        boolean started = false;
        
        while (reader.next(key, content)) {
        	boolean isInURLs = false;
        	for(String s : testURLS) {
        		
        		if(s.equals(content.getBaseUrl())) {
        			isInURLs = true;
        			break;
        		}
        	}
        	if(!isInURLs) continue;

        	
        	if(index == 0) {
        		if(started) {
        			w.write("\n</add>");
        			w.close();
        		}
	            folder = new File("C:/Users/Jonathan/Desktop/testCollection");
	            newFile = new File("C:/Users/Jonathan/Desktop/testCollection/index"+folder.listFiles().length+".xml");
	            w = new FileWriter(newFile);
	            w.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<add>\n");
	            started=true;

	            
	        }
        	index++;
        	if(index == 100) index = 0;
            try {
            	// TODO save URLs, check if already indexed, check if already indexed for index.html
            	Document doc = Jsoup.parse(new String(content.getContent(), "utf-8"));
            //   Document doc = Jsoup.parse(new URL("http://www.ur.de/index.html"), 1000);
            	String s = bla.parseDocument(doc, content.getBaseUrl());
            	if(!s.equals("")) {
            		w.write(s);
            	}
            } catch (Exception e) {
            }
        }
        if(w != null) {
			w.write("\n</add>");
			w.close();
	    }
		if(reader != null) {
			reader.close();
		}
	}
	
	// TODO PDFs are still excluded!!
	private String parseDocument(Document doc, String url) {
		if(!url.endsWith("/") && !url.endsWith("html") && !url.endsWith("htm") && !url.endsWith("php") && !url.endsWith("jsp") && !url.endsWith(".de")) return ""; //&& !url.endsWith(".pdf")) return "";
		int cms = findOutWhichCMS(doc, url);
		String title = doc.select("title").text();
		
		try {
			doc = Jsoup.parse(removeUnnecessaryParts(doc, cms));
		} catch (OutOfMemoryError e) {
			return "";
		}
		String contents = doc.text();
		if(contents.contains("301 Moved Permanently") || contents.contains("302 Found 302")) return "";
		
		contents = contents.replaceAll("<", "&lt;");
		contents = contents.replaceAll(">", "&gt;");
		contents = contents.replaceAll("&", "&amp;");
		
		title = title.replaceAll("&", "&amp;");
		title = title.replaceAll("<", "&lt;");
		title = title.replaceAll(">", "&gt;");
		// remove control characters: 
		contents = contents.replaceAll("[\u0000-\u001f]", "");
		
		
		return this.saveToDoc(title, url, contents)+"\n";
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
	
	private String removeUnnecessaryParts(Document doc, int cms) throws OutOfMemoryError{	
		String htmlString = "";
				
		if(cms == UNI_DEFAULT) {
			htmlString += doc.getElementsByTag("head").html();

			doc.select("div.navigation").remove();
			doc.select("div.header").remove();
			doc.select("a#skipnav").remove();
			
			htmlString += doc.html();
			
		} else if(cms == UNI_BIOLOGY) {
			// Fun fact: biologie benutzt <br> und <table>s für die Optik.
			htmlString += doc.getElementsByTag("head").html();
			doc.select("div.navigation").remove();
			htmlString += doc.html();
			
		} else if(cms == UNI_ETHIKKOM) {
			htmlString += doc.getElementsByTag("head").html();
			doc.select("div.header").remove();
			htmlString += doc.html();
			
		} else if (cms == UNI_WIWI) {
			htmlString += doc.getElementsByTag("head").html();
			doc.select("div#header").remove();
			doc.select("div.topnavi").remove();
			htmlString += doc.html();
			
		} else if (cms == UNI_NAT_FAK_I) {
			// Fun fact: ein großer Table ;)
			htmlString += doc.getElementsByTag("head").html();
			doc.select("ul#MenuBar1").remove();
			htmlString += doc.html();
			
		} else if (cms == UNI_OC_CHEMIE) {

			htmlString += doc.getElementsByTag("head").html();

			doc.select("div.navigation").remove();
			doc.select("div.header").remove();
			doc.select("a#skipnav").remove();
			
			htmlString += doc.html();
			
		} else if (cms == UNI_PHYSICS) {
			htmlString += doc.getElementsByTag("head").html();
			doc.select("#phyHead").remove();
			doc.select("#phyMenuMainTabs").remove();
			htmlString += doc.html();
		} else if (cms == PDF) {
			return doc.toString();
		}
		
		else {
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