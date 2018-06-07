package com.interwoven.teamsite.javax;

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
import org.dom4j.Visitor;

/**
 * org.dom4j.Visitor Interface Adapter
 * Extend this class and override the methods you
 * wish to implement
 * @author nbamford
 *
 */
public abstract class VisitorAdapter 
implements Visitor 
{

	public void visit(Document arg0) {
		//Empty Override
	}

	public void visit(DocumentType arg0) {
		//Empty Override
	}

	public void visit(Element arg0) {
		//Empty Override
	}

	public void visit(Attribute arg0) {
		//Empty Override
	}

	public void visit(CDATA arg0) {
		//Empty Override
	}

	public void visit(Comment arg0) {
		//Empty Override
	}

	public void visit(Entity arg0) {
		//Empty Override
	}

	public void visit(Namespace arg0) {
		//Empty Override
	}

	public void visit(ProcessingInstruction arg0) {
		//Empty Override
	}

	public void visit(Text arg0) {
		//Empty Override
	}

}
