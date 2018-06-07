package com.interwoven.teamsite.nikon.util;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.CDATA;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.Entity;
import org.dom4j.Namespace;
import org.dom4j.ProcessingInstruction;
import org.dom4j.Text;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SearchAndReplaceVisitor
implements org.dom4j.Visitor
{
	private static Log log = LogFactory.getLog(SearchAndReplaceVisitor.class);

	private boolean dirty;

	Map<String, String> map;

	public SearchAndReplaceVisitor()
	{
		writeFile(new File("c:/temp/creatingVisitor.log"), "Created an instance of SearchAndReplaceVisitor");

	}

	public static void main(String[] args)
	{
		new SearchAndReplaceVisitor();
	}

	public SearchAndReplaceVisitor(Map<String, String> map)
	{
		this.map = map;
	}

	public void visit(Document arg0) {
		// TODO Auto-generated method stub

	}

	public void visit(DocumentType arg0) {
		// TODO Auto-generated method stub

	}

	public void visit(Element arg0) {
		// TODO Auto-generated method stub

	}

	public void visit(Attribute arg0) {
		if(map.containsKey(arg0.getValue()))
		{
			arg0.setValue(map.get(arg0.getValue()));
			dirty = true;
		}
	}

	public void visit(CDATA arg0) {
		// TODO Auto-generated method stub

	}

	public void visit(Comment arg0) {
		// TODO Auto-generated method stub

	}

	public void visit(Entity arg0) {
		// TODO Auto-generated method stub

	}

	public void visit(Namespace arg0) {
		// TODO Auto-generated method stub

	}

	public void visit(ProcessingInstruction arg0) {
		// TODO Auto-generated method stub

	}

	public void visit(Text arg0) {
		if(map.containsKey(arg0.getText()))
		{
			arg0.setText(map.get(arg0.getText()));
			dirty = true;
		}
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	private static void writeFile(File f, String message)
	{
		try
		{
			FileWriter fw = new FileWriter(f);
			fw.write(message);
			fw.flush();
			fw.close();
		}
		catch(Exception exception)
		{
			log.debug("Exception", exception);
		}
	}

}
