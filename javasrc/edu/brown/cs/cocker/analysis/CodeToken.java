package edu.brown.cs.cocker.analysis;

public class CodeToken
{
    String token_text;
    int token_property;

    public CodeToken(String text) {
	this.token_text = text;
	this.token_property = -1; //no property
    }

    public CodeToken(String text, int property) {
	this.token_text = text;
	this.token_property = property;
    }

    public String getText() { return token_text; }

    public void setText(String text) { this.token_text = text; }
    
    public int getProp() { return token_property; }

    public void setProp(int property) { this.token_property = property; }
}
