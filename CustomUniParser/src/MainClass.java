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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class MainClass {

	public static void main(String[] args) throws IOException {
		//Content c = readSegment("/home/jon/Desktop/IR/apache-nutch-1.4-bin/runtime/local/bin/crawl44/segments/20140705141227", "/home/jon/Desktop/IR/apache-nutch-1.4-bin/runtime/local/bin/crawl44/segments/20140705141227");
	
		Configuration conf = NutchConfiguration.create();
        Options opts = new Options();
        GenericOptionsParser parser = new GenericOptionsParser(conf, opts, new String[]{"C:/cygwin64/home/apache-nutch-1.4-bin/runtime/local/bin/crawl44/segments/20140705140223"});
        String[] remainingArgs = parser.getRemainingArgs();
        FileSystem fs = FileSystem.get(conf);
        String segment = remainingArgs[0];
        Path file = new Path(segment, Content.DIR_NAME + "/part-00000/data");
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, file, conf);
        Text key = new Text();
        Content content = new Content();
        // Loop through sequence files
        int index = 0;
        while (reader.next(key, content)) {
        	index++;
//        	if (index < 3) continue;
//        	if(index > 3) break;
            try {
                Document doc = Jsoup.parse(new String(content.getContent(), "utf-8"));
                parseDocument(doc);
            } catch (Exception e) {
            }
        }
        
	}
	
	private static void parseDocument(Document doc) {
		Elements e = doc.getElementsByTag("meta");
		for(int i = 0; i < e.size(); i++) {
			Element metaTag = e.get(i);
			String metaContent = "";
			if(metaTag.attributes().get("name").equals("description")) {
				System.out.println(metaTag.attributes().get("content"));
			}
		}
	}

}