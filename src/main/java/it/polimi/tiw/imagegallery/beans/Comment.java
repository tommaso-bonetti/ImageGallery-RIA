package it.polimi.tiw.imagegallery.beans;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Comment {
	private int id;
	private String body;
	private int publisherId;
	private String publisherUsername;
	private int imageId;
	private Date publishedDate;
	private String formattedDate;
	
	private final transient DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getBody() {
		return body;
	}
	
	public void setBody(String body) {
		this.body = body;
	}
	
	public int getPublisherId() {
		return publisherId;
	}
	
	public void setPublisherId(int publisherId) {
		this.publisherId = publisherId;
	}
	
	public int getImageId() {
		return imageId;
	}
	
	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

	public String getPublisherUsername() {
		return publisherUsername;
	}

	public void setPublisherUsername(String publisherUsername) {
		this.publisherUsername = publisherUsername;
	}

	public Date getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
		this.setFormattedDate(formatter.format(publishedDate));
	}

	public String getFormattedDate() {
		return formattedDate;
	}

	private void setFormattedDate(String formattedDate) {
		this.formattedDate = formattedDate;
	}
}
