package it.polimi.tiw.imagegallery.beans;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Album {
	private int id;
	private String title;
	private Date creationDate;
	private String formattedDate;
	private int ownerId;
	private String ownerUsername;
	
	private final transient DateFormat formatter;
	
	public Album() {
		super();
		formatter = new SimpleDateFormat("dd/MM/yyyy");
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
		this.setFormattedDate(formatter.format(creationDate));
	}
	
	public int getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerUsername() {
		return ownerUsername;
	}

	public void setOwnerUsername(String ownerUsername) {
		this.ownerUsername = ownerUsername;
	}

	public String getFormattedDate() {
		return formattedDate;
	}

	private void setFormattedDate(String formattedDate) {
		this.formattedDate = formattedDate;
	}
}
