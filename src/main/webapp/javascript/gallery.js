{
	// Page components
	let welcomeMessage, ownAlbums, otherAlbums, albumImages, imagesToAdd, imageDetails;
	let pageOrchestrator = new PageOrchestrator();
	
	window.addEventListener('load', () => {
		if (sessionStorage.getItem('username') == null) {
			window.location.href = 'login.html';
		} else {
			pageOrchestrator.start();
			pageOrchestrator.refresh();
		}
	}, false);
	
	function WelcomeMessage(_username, container) {
		this.username = _username;
		this.display = function() {
			container.textContent = this.username;
		}
	};
	
	function AlertContainer(_container) {
		this.container = _container;
		
		this.display = message => {
			this.container.textContent = message;
			this.container.style.display = 'block';
		};
		
		this.displayError = message => {
			this.container.classList.add('error');
			this.display(message);
		};
		
		this.hide = () => {
			this.container.style.display = 'none';
			this.container.classList.remove('error');
		};
	}
	
	function AlbumList(_tableContainer, _table, _alertContainer, _ownAlbums) {
		this.tableContainer = _tableContainer;
		this.table = _table;
		this.alertContainer = new AlertContainer(_alertContainer);
		this.ownAlbums = _ownAlbums;
		
		this.load = function() {
			var self = this;
			sendAsync('GET', 'FetchAlbumList?ownAlbums=' + self.ownAlbums, null, function(x) {
				if (x.readyState == XMLHttpRequest.DONE) {
					self.alertContainer.hide();
					var message = x.responseText;
					
					if (x.status == 200) {
						var albums = JSON.parse(message);
						if (albums.length == 0) {
							self.alertContainer.display('No albums yet!');
							return;
						}
						self.populate(albums);
					} else {
						self.alertContainer.displayError(message);
					}
				}
			});
		};
		
		this.populate = function(albums) {
			this.table.innerHTML = '';
			albums.forEach(album => {
				var row = document.createElement('tr');
				
				var title = document.createElement('td');
				title.textContent = album.title;
				row.appendChild(title);
				
				var creator = document.createElement('td');
				creator.textContent = album.ownerUsername;
				row.appendChild(creator);
				
				var date = document.createElement('td');
				date.textContent = album.formattedDate;
				row.appendChild(date);
				
				var details = document.createElement('td');
				var anchor = document.createElement('a');
				var text = document.createTextNode('See more');
				anchor.href = '#albumDetails';
				anchor.appendChild(text);
				anchor.setAttribute('albumId', album.id);
				anchor.addEventListener('click', e => albumImages.load(e.target.getAttribute('albumId')), false);
				details.appendChild(anchor);
				row.appendChild(details);
				
				this.table.appendChild(row);
			});
		};
	};
	
	function Gallery(_gridContainer, _grid, _alertContainer) {
		this.gridContainer = _gridContainer;
		this.grid = _grid;
		this.alertContainer = new AlertContainer(_alertContainer);
		
		this.titleContainer = document.getElementById('albumTitle');
		this.creatorContainer = document.getElementById('albumCreator');
		this.dateContainer = document.getElementById('albumDate');
		this.showPrevious = document.getElementById('showPrev');
		this.showNext = document.getElementById('showNext');
		this.addImages = document.getElementById('addImages');
		
		this.albumId = null;
		this.images = null;
		this.page = null;
		
		this.gridContainer.style.visibility = 'hidden';
		
		this.showPrevious.addEventListener('click', e => {
			if (this.page > 0) {
				this.page--;
				this.showPrevious.style.visibility = 'visible';
				if (this.page == 0)
					e.target.style.visibility = 'hidden';

				this.populate();
			}
		});
		
		this.showNext.addEventListener('click', e => {
			if (this.images.length > this.page * 5) {
				this.page++;
				this.showPrevious.style.visibility = 'visible';
				if (this.images.length <= this.page * 5)
					e.target.style.visibility = 'hidden';

				this.populate();
			}
		});
		
		this.addImages.addEventListener('click', e => imagesToAdd.load(e.target.getAttribute('albumId')), false);
		
		this.load = function (albumId) {
			var self = this;
			
			sendAsync('GET', 'FetchAlbum?albumId=' + albumId, null, function (x) {
				if (x.readyState == XMLHttpRequest.DONE) {
					self.clear();
					self.gridContainer.style.visibility = 'visible';
					
					var message = x.responseText;
					
					if (x.status == 200) {
						var album = JSON.parse(message);
						
						if (album.ownerUsername !== sessionStorage.getItem('username'))
							self.addImages.style.display = 'none';
						else
							self.addImages.setAttribute('albumId', album.id);

						self.albumId = album.id;
						self.titleContainer.appendChild(document.createTextNode(album.title));
						self.creatorContainer.appendChild(document.createTextNode(album.ownerUsername));
						self.dateContainer.appendChild(document.createTextNode(album.formattedDate));
						
						if (album.images.length == 0) {
							self.alertContainer.display('No images yet!');
						} else {
							self.showNext.style.visibility = 'visible';
							self.images = album.images;
							self.page = 1;
							self.populate();
						}
					} else {
						self.alertContainer.displayError(message);
					}
				}
			});
		};
		
		this.populate = function () {
			var currentImages = this.images.slice(5 * (this.page - 1), 5 * this.page); 
			this.grid.innerHTML = '';
			
			currentImages.forEach(image => {
				var gridItem = document.createElement('div');
				gridItem.classList.add('grid-item');
				
				var imageContainer = document.createElement('div');
				imageContainer.classList.add('image-container-grid');
				
				var anchor = document.createElement('a');
				var img = document.createElement('img');
				img.src = '/ImageGallery-RIA' + image.filePath;
				img.classList.add('image');
				anchor.appendChild(img);
				anchor.href = '#albumDetails';
				anchor.setAttribute('imageId', image.id);
				anchor.addEventListener('click', e => imageDetails.load(e.target.closest('a').getAttribute('imageId'), this.albumId), false);
				
				imageContainer.appendChild(anchor);
				gridItem.appendChild(imageContainer);
				
				var imageTitle = document.createElement('p');
				imageTitle.classList.add('image-title');
				imageTitle.appendChild(document.createTextNode(image.title))
				gridItem.appendChild(imageTitle);
				
				this.grid.appendChild(gridItem);
			});
		};
		
		this.clear = function () {
			this.gridContainer.style.visibility = 'hidden';
			this.grid.innerHTML = '';
			this.alertContainer.hide();
			
			this.titleContainer.innerHTML = '';
			this.creatorContainer.innerHTML = '';						
			this.dateContainer.innerHTML = '';
			
			this.showPrevious.style.visibility = 'hidden';
			this.showNext.style.visibility = 'hidden';
			this.addImages.removeAttribute('albumId');
			
			this.albumId = null;
			this.images = null;
			this.page = null;
		};
	};
	
	function MultiRowGallery(_gridContainer, _grid, _alertContainer) {
		this.gridContainer = _gridContainer;
		this.grid = _grid;
		this.alertContainer = new AlertContainer(_alertContainer);
		
		this.titleContainer = document.getElementById('imagesToAddTitle');
		
		this.albumId = null;
		this.images = null;
		
		this.gridContainer.style.visibility = 'hidden';
		
		this.load = function (albumId) {
			var self = this;
			
			sendAsync('GET', 'FetchImagesToAdd?albumId=' + albumId, null, function (x) {
				if (x.readyState == XMLHttpRequest.DONE) {
					self.clear();
					self.gridContainer.style.visibility = 'visible';
					
					var message = x.responseText;
					
					if (x.status == 200) {
						var res = JSON.parse(message);
						
						var album = res.album;
						var images = res.images;
						
						self.albumId = album.id;
						self.titleContainer.appendChild(document.createTextNode('Add images to ' + album.title));
						
						if (images.length == 0) {
							self.alertContainer.display('No images to add!');
						} else {
							self.images = images;
							self.populate();
						}
					} else {
						self.titleContainer.appendChild(document.createTextNode('Add images to album'))
						self.alertContainer.displayError(message);
					}
				}
			});
		};
		
		this.populate = function () {
			this.grid.innerHTML = '';
			
			this.images.forEach(image => {
				var gridItem = document.createElement('div');
				gridItem.classList.add('grid-item');
				
				var imageContainer = document.createElement('div');
				imageContainer.classList.add('image-container-grid');
				
				var img = document.createElement('img');
				img.src = '/ImageGallery-RIA' + image.filePath;
				img.classList.add('image');				
				imageContainer.appendChild(img);
				gridItem.appendChild(imageContainer);
				
				var imageTitle = document.createElement('p');
				imageTitle.classList.add('image-title');
				imageTitle.appendChild(document.createTextNode(image.title))
				gridItem.appendChild(imageTitle);
				
				var form = document.createElement('form');
				form.action = '#';
				form.classList.add('align-center');
				
				var albumIdField = document.createElement('input');
				albumIdField.type = 'hidden';
				albumIdField.name = 'targetAlbum';
				albumIdField.value = this.albumId;
				form.appendChild(albumIdField);
				
				var imageIdField = document.createElement('input');
				imageIdField.type = 'hidden';
				imageIdField.name = 'targetImage';
				imageIdField.value = image.id;
				form.appendChild(imageIdField);
				
				var submit = document.createElement('input');
				submit.type = 'button';
				submit.value = 'Add';
				
				submit.addEventListener('click', e => {
					var self = this;
					sendAsync('POST', 'AddToAlbum', form, function (x) {
						if (x.readyState == XMLHttpRequest.DONE) {
							var message = x.responseText;
							
							switch (x.status) {
								case 200: // ok
									albumImages.load(parseInt(message));
									self.load(parseInt(message));
									break;
								case 400:	// bad request
								case 401:	// unauthorized
								case 500: // internal server error
									self.alertContainer.displayError(message);
									break;
								default:
									self.alertContainer.displayError('Unexpected error');
							}
						}
					});
				});
				
				form.appendChild(submit);
				gridItem.appendChild(form);
				
				this.grid.appendChild(gridItem);
			});
		};
		
		this.clear = function () {
			this.gridContainer.style.visibility = 'hidden';
			this.grid.innerHTML = '';
			this.alertContainer.hide();
			
			this.titleContainer.innerHTML = '';
			
			this.album = null;
			this.images = null;
		};
	};
	
	function ImageDetails(_modal, _modalContent, _commentList, _albumSelect, _alertContainer) {
		this.modal = _modal;
		this.modalContent = _modalContent;
		this.commentList = new CommentList(_commentList);
		this.albumSelect = new AlbumSelect(_albumSelect);
		this.alertContainer = new AlertContainer(_alertContainer);
		
		this.imageContainer = document.getElementById('fullSizeImage');
		this.titleContainer = document.getElementById('selImgTitle');
		this.descContainer = document.getElementById('selImgDesc');
		this.dateContainer = document.getElementById('selImgDate');
		this.closeButton = document.getElementById('closeModal');
		
		this.currentAlbum = null;
		this.image = null;
		
		this.modal.style.display = 'none';
		
		this.closeButton.addEventListener('click', e => this.modal.style.display = 'none');
		
		this.load = function (imageId, albumId) {
			this.currentAlbum = albumId;
			var self = this;
			
			sendAsync('GET', 'FetchImage?imageId=' + imageId + "&albumId=" + albumId, null, function (x) {				
				if (x.readyState == XMLHttpRequest.DONE) {
					self.clear();
					self.modal.style.display = 'block';
					
					var message = x.responseText;
					
					if (x.status == 200) {
						self.image = JSON.parse(message);
						self.populate();
					} else {
						self.imageContainer.style.display = 'none';
						self.alertContainer.displayError(message);
					}
				}
			});
		};
		
		this.populate = function () {
			this.commentList.load(this.image.id);
			// this.albumSelect.load();
			
			this.imageContainer.style.display = 'block';
			this.imageContainer.src = '/ImageGallery-RIA' + this.image.filePath;
			
			this.titleContainer.appendChild(document.createTextNode(this.image.title));
			this.descContainer.appendChild(document.createTextNode(this.image.description));
			this.dateContainer.appendChild(document.createTextNode('Uploaded ' + this.image.formattedDate));
		};
		
		this.clear = function () {
			this.modal.style.display = 'none';
			this.imageContainer.src = '';
			
			this.titleContainer.innerHTML = '';
			this.descContainer.innerHTML = '';
			this.dateContainer.innerHTML = '';
		
			this.currentAlbum = null;
			this.image = null;
			
			this.commentList.clear();
			// this.albumSelect.clear();
		};
	};
	
	function CommentList(_commentList) {
		this.commentList = _commentList;
		this.wrapper = document.getElementById('commentsWrapper');
		this.alertContainer = new AlertContainer(document.getElementById('commentsAlert'));
		this.form = document.getElementById('commentForm');
		
		this.imageId = null;
		this.comments = null;
		
		this.commentList.style.display = 'none';
		
		this.load = function (imageId) {
			this.imageId = imageId;
			var self = this;
			
			sendAsync('GET', 'FetchComments?imageId=' + imageId, null, function (x) {
				if (x.readyState == XMLHttpRequest.DONE) {
					self.clear();
					self.commentList.style.display = 'block';
					
					var message = x.responseText;
					
					if (x.status == 200) {
						self.comments = JSON.parse(message);
						self.populate();
					} else {
						self.alertContainer.displayError(message);
					}
				}
			});
		};
		
		this.populate = function () {
			this.wrapper.innerHTML = '';
			
			document.getElementById('commentUsername').value = sessionStorage.getItem('username');
			document.getElementById('commentImage').value = this.imageId;
			document.getElementById('publishComment').addEventListener('click', e => {
				var self = this;
				var errorMsg = document.getElementById('commentFormError');
				errorMsg.innerHTML = '';
				debugger
				sendAsync('POST', 'PublishComment', this.form, function (x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						var message = x.responseText;
						switch (x.status) {
							case 200:
								self.load(parseInt(message));
								break;
							case 400:
							case 401:
							case 500:
								errorMsg.appendChild(document.createTextNode(x.responseText));
								break;
							default:
								errorMsg.appendChild(document.createTextNode('Unexpected error'));
						}
					}
				});
			});
			
			if (this.comments.length == 0) this.alertContainer.display('No comments yet.');
			else {
				this.comments.forEach(comment => {
					var commentBox = document.createElement('div');
					commentBox.classList.add('comment');
					
					var body = document.createElement('p');
					body.classList.add('comment-body', 'text-90');
					body.appendChild(document.createTextNode(comment.body));
					commentBox.appendChild(body);
					
					var details = document.createElement('p');
					details.classList.add('comment-details', 'text-75');
					
					var by = document.createElement('span');
					by.classList.add('light-text');
					by.appendChild(document.createTextNode('by '));
					details.appendChild(by);
					
					var publisher = document.createElement('span');
					publisher.classList.add('bold-text');
					publisher.appendChild(document.createTextNode(comment.publisherUsername));
					details.appendChild(publisher);
					
					var date = document.createElement('span');
					date.classList.add('light-text');
					date.appendChild(document.createTextNode(' - ' + comment.formattedDate));
					details.appendChild(date);
					
					commentBox.appendChild(details);
					
					this.wrapper.appendChild(commentBox);
				});
			}
		};
		
		this.clear = function () {
			this.commentList.style.display = 'hidden';
			this.wrapper.innerHTML = '';
			this.alertContainer.hide();
			
			this.comments = null;
		};
	};
	
	function AlbumSelect(_albumSelect) {};
	
	function PageOrchestrator() {
		this.start = function() {
			// Initialize the logout button
			document.getElementById('logout').addEventListener('click', e => {
				sessionStorage.removeItem('username');
			});
			
			// Initialize the main page components
			welcomeMessage = new WelcomeMessage(
					sessionStorage.getItem('username'),
					document.getElementById('welcomeUsername')
			);
			
			ownAlbums = new AlbumList(
					document.getElementById('ownAlbumsContainer'),
					document.getElementById('ownAlbums'),
					document.getElementById('ownAlbumsAlert'),
					true
			);
			
			otherAlbums = new AlbumList(
					document.getElementById('otherAlbumsContainer'),
					document.getElementById('otherAlbums'),
					document.getElementById('otherAlbumsAlert'),
					false
			);
			
			albumImages = new Gallery(
					document.getElementById('albumDetails'),
					document.getElementById('albumImagesGrid'),
					document.getElementById('albumImagesAlert')
			);
			
			imagesToAdd = new MultiRowGallery(
					document.getElementById('imagesToAdd'),
					document.getElementById('imagesToAddGrid'),
					document.getElementById('imagesToAddAlert')
			);
			
			imageDetails = new ImageDetails(
					document.getElementById('selectedImageWrapper'),
					document.getElementById('selectedImage'),
					document.getElementById('commentList'),
					document.getElementById('addToAlbumForm'),
					document.getElementById('selImgAlert')
			);
		};
		
		this.refresh = function () {
			welcomeMessage.display();
			ownAlbums.load();
			otherAlbums.load();
		}
	}
};