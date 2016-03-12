package com.thecoffeedrinker.theforcereader.documentprocessor;

import java.io.IOException;
import java.net.URL;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

/**
 * Extract relevant parts of the news (article and picture) from a news web page.
 * @author carlo
 *
 */
public class HTMLDocumentExtractor {
	protected Source pageSource;
	

	public HTMLDocumentExtractor(URL url) throws IOException {
		pageSource=new Source(url);
	}
	
	public String getHeaderPicture(){
		Element pictureDiv = pageSource.getFirstElementByClass("news-thumbnails");
		if(pictureDiv==null){
			return new String();
		}else{
			Element imgElem = pictureDiv.getFirstElement(HTMLElementName.IMG);
			return imgElem.getAttributeValue("src");
		}
	}
	
	public String getArticle(){
		Element articleDiv = pageSource.getFirstElementByClass("news-teaser");
		if(articleDiv==null){
			return "<H1 style='margin:8px'>The article is not available; please try again later</H1>";
		}
		return articleDiv.toString();
	}
	

}
