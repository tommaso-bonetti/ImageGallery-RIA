package it.polimi.tiw.imagegallery.beans;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Image {
	private int id;
	private String title;
	private String description;
	private Date uploadDate;
	private String formattedDate;
	private String filePath;
	private int ownerId;
	
	private final transient DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
	
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
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Date getUploadDate() {
		return uploadDate;
	}
	
	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
		this.setFormattedDate(formatter.format(uploadDate));
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public int getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	public String getFormattedDate() {
		return formattedDate;
	}

	private void setFormattedDate(String formattedDate) {
		this.formattedDate = formattedDate;
	}
}
