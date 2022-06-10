{
	// Page components
	let welcomeMessage, ownAlbums, otherAlbums, createAlbum, albumImages, imagesToAdd, imageDetails;
	let pageOrchestrator = new PageOrchestrator();
	
	window.addEventListener('load', () => {
		if (sessionStorage.getItem('username') == null) {
			window.location.href = 'login.html';
		} else {
			pageOrchestrator.start();
			pageOrchestrator.refresh();
		}
	}, false);
	
	function WelcomeMessage(_username, _container) {
		this.username = _username;
		this.container = document.getElementById(_container);
		
		this.display = function() {
			this.container.textContent = this.username;
		}
	};
	
	function AlertContainer(_container) {
		this.container = document.getElementById(_container);
		
		this.display = message => {
			this.container.textContent = message;
			this.container.style.display = 'block';
		};
		
		this.displayError = message => {
			this.container.classList.remove('success');
			this.container.classList.add('error');
			this.display(message);
		};
		
		this.displaySuccess = message => {
			this.container.classList.remove('error');
			this.container.classList.add('success');
			this.display(message);
		};
		
		this.hide = () => {
			this.container.style.display = 'none';
			this.container.classList.remove(...this.container.classList);
		};
	}
	
	function AlbumList(_tableContainer, _table, _alertContainer, _ownAlbums) {
		this.tableContainer = document.getElementById(_tableContainer);
		this.table = document.getElementById(_table);
		this.alertContainer = new AlertContainer(_alertContainer);
		this.ownAlbums = _ownAlbums;
		this.reorderForm = document.getElementById('reorderAlbums');
		this.saveReorder = null;
		
		if (this.ownAlbums) {
			this.saveReorder = document.getElementById('saveReorder');
			this.saveReorder.parentElement.style.display = 'none';
			
			this.saveReorder.addEventListener('click', e => {
				let order = Array.from(this.table.children).map(tr => tr.getAttribute('albumId'));
				this.reorderForm.elements['order'].value = '[' + order + ']';
				
				let self = this;
				sendAsync('POST', 'ReorderAlbums', this.reorderForm, function (x) {
					if (x.readyState == XMLHttpRequest.DONE) {
						let message = x.responseText;
						
						switch (x.status) {
							case 200: // ok
								self.alertContainer.displaySuccess('Successfully reordered album list!');
								setTimeout(() => self.alertContainer.hide(), 2000);
								break;
							case 400: // bad request
							case 401: // unauthorized
							case 500: // internal server error
								self.alertContainer.displayError(message);
								setTimeout(() => self.alertContainer.hide(), 2000);
								break;
							default:
								self.alertContainer.displayError('Unexpected error');
								setTimeout(() => self.alertContainer.hide(), 2000);
						}
						
						self.saveReorder.parentElement.style.display = 'none';
						self.load();
					}
				});
			});
		}
		
		this.load = function() {
			let self = this;
			sendAsync('GET', 'FetchAlbumList?ownAlbums=' + self.ownAlbums, null, function(x) {
				if (x.readyState == XMLHttpRequest.DONE) {
					let message = x.responseText;
					
					if (x.status == 200) {
						let albums = JSON.parse(message);
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
			let dragged = null;

			this.table.innerHTML = '';
			albums.forEach(album => {
				let row = document.createElement('tr');
				row.setAttribute('albumId', album.id);
				
				let title = document.createElement('td');
				title.textContent = album.title;
				
				if (this.ownAlbums) {
					title.draggable = true;
					title.classList.add('draggable');
					title.addEventListener('dragstart', e => dragged = e.target.parentNode);
					title.addEventListener('dragover', e => e.preventDefault());
					title.addEventListener('drop', e => {
						let rows = Array.from(e.target.closest('tbody').children);
						let destination = e.target.parentNode;
						
						if (dragged == null) return;
						
						if (rows.indexOf(destination) > rows.indexOf(dragged))
							destination.after(dragged);
						else
							destination.before(dragged);
						
						this.saveReorder.parentElement.style.display = 'block';
					});
				}
				
				row.appendChild(title);
				
				let creator = document.createElement('td');
				creator.textContent = album.ownerUsername;
				row.appendChild(creator);
				
				let date = document.createElement('td');
				date.textContent = album.formattedDate;
				row.appendChild(date);
				
				let details = document.createElement('td');
				let anchor = document.createElement('a');
				let text = document.createTextNode('See more');
				anchor.href = '#albumDetails';
				anchor.appendChild(text);
				anchor.setAttribute('albumId', album.id);
				anchor.addEventListener('click', e => {
					let albumId = e.target.getAttribute('albumId');
					sessionStorage.setItem('currentAlbum', albumId);
					albumImages.load(albumId);
				}, false);
				details.appendChild(anchor);
				row.appendChild(details);
				
				this.table.appendChild(row);
			});
		};
	};
	
	function AlbumImages() {
		this.gridContainer = document.getElementById('albumDetails');
		this.grid = document.getElementById('albumImagesGrid');
		this.alertContainer = new AlertContainer('albumImagesAlert');
		
		this.titleContainer = document.getElementById('albumTitle');
		this.creationDataContainer = document.getElementById('creationData');
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
			if (this.page > 1) {
				this.page--;
				this.showNext.style.visibility = 'visible';
				if (this.page == 1)
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
			let self = this;
			
			sendAsync('GET', 'FetchAlbum?albumId=' + albumId, null, function (x) {
				if (x.readyState == XMLHttpRequest.DONE) {
					self.clear();
					self.gridContainer.style.visibility = 'visible';
					
					let message = x.responseText;
					
					if (x.status == 200) {
						let album = JSON.parse(message);
						
						if (album.ownerUsername === sessionStorage.getItem('username')) {
							self.addImages.style.display = 'block';
							self.addImages.setAttribute('albumId', album.id);
						}

						self.albumId = album.id;
						self.titleContainer.appendChild(document.createTextNode(album.title));
						self.creatorContainer.appendChild(document.createTextNode(album.ownerUsername));
						self.dateContainer.appendChild(document.createTextNode(album.formattedDate));
						self.creationDataContainer.style.display = 'block';
						
						if (album.images.length == 0) {
							self.alertContainer.display('No images yet!');
						} else {
							self.images = album.images;
							self.images.forEach(image => {
								let img = document.createElement('img');
								img.src = '/ImageGallery-RIA' + image.filePath;
								img.classList.add('image');
								image.imgElement = img;
							});
							self.showNext.style.visibility = (self.images.length > 5) ?  'visible' : 'hidden';
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
			let currentImages = this.images.slice(5 * (this.page - 1), 5 * this.page); 
			this.grid.innerHTML = '';
			
			currentImages.forEach(image => {
				let gridItem = document.createElement('div');
				gridItem.classList.add('grid-item');
				
				let imageContainer = document.createElement('div');
				imageContainer.classList.add('image-container-grid');
				
				let anchor = document.createElement('a');
				anchor.appendChild(image.imgElement);
				anchor.href = '#albumDetails';
				
				imageContainer.appendChild(anchor);
				gridItem.appendChild(imageContainer);
				
				let imageTitle = document.createElement('p');
				imageTitle.classList.add('image-title');
				imageTitle.appendChild(document.createTextNode(image.title))
				gridItem.appendChild(imageTitle);
				
				gridItem.setAttribute('imageId', image.id);
				
				gridItem.addEventListener('click', e => {
					imageDetails.load(e.target.closest('div.grid-item').getAttribute('imageId'), this.albumId)
				}, false);
				
				let timeout;
				
				gridItem.addEventListener('mouseover', e => {
					if (timeout != null)
						clearTimeout(timeout);
					timeout = setTimeout(() => {
						imageDetails.load(e.target.closest('div.grid-item').getAttribute('imageId'), this.albumId)
					}, 500);
				}, false);
				
				gridItem.addEventListener('mouseout', e => {
					if (timeout != null)
						clearTimeout(timeout);
				}, false);
				
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
			
			this.creationDataContainer.style.display = 'hidden';
			
			this.showPrevious.style.visibility = 'hidden';
			this.showNext.style.visibility = 'hidden';
			this.addImages.removeAttribute('albumId');
			this.addImages.style.display = 'none';
			
			this.albumId = null;
			this.images = null;
			this.page = null;
		};
	};
	
	function ImagesToAdd() {
		this.gridContainer = document.getElementById('imagesToAdd');
		this.modalContent = document.getElementById('imagesToAddContent');
		this.grid = document.getElementById('imagesToAddGrid');
		this.alertContainer = new AlertContainer('imagesToAddAlert');
		
		this.titleContainer = document.getElementById('imagesToAddTitle');
		this.closeButton = document.getElementById('closeAddImagesModal');
		
		this.albumId = null;
		this.images = null;
		
		this.gridContainer.style.display = 'none';
		
		this.gridContainer.addEventListener('click', e => this.hide(), false);
		this.modalContent.addEventListener('click', e => e.stopPropagation(), false);
		this.closeButton.addEventListener('click', e => this.hide());
		
		this.load = function (albumId) {
			let self = this;
			
			sendAsync('GET', 'FetchImagesToAdd?albumId=' + albumId, null, function (x) {
				if (x.readyState == XMLHttpRequest.DONE) {
					self.clear();
					self.gridContainer.style.display = 'block';
					
					let message = x.responseText;
					
					if (x.status == 200) {
						let res = JSON.parse(message);
						
						let album = res.album;
						let images = res.images;
						
						self.albumId = album.id;
						self.titleContainer.appendChild(document.createTextNode('Add images to ' + album.title));
						
						if (images.length == 0) {
							self.alertContainer.display('No images to add!');
						} else {
							self.images = images;
							self.populate();
						}
					} else {
						self.titleContainer.appendChild(document.createTextNode('Add images to album'));
						self.alertContainer.displayError(message);
					}
				}
			});
		};
		
		this.populate = function () {
			this.grid.innerHTML = '';
			
			this.images.forEach(image => {
				let gridItem = document.createElement('div');
				gridItem.classList.add('grid-item');
				
				let imageContainer = document.createElement('div');
				imageContainer.classList.add('image-container-grid');
				
				let img = document.createElement('img');
				img.src = '/ImageGallery-RIA' + image.filePath;
				img.classList.add('image');				
				imageContainer.appendChild(img);
				gridItem.appendChild(imageContainer);
				
				let imageTitle = document.createElement('p');
				imageTitle.classList.add('image-title');
				imageTitle.appendChild(document.createTextNode(image.title))
				gridItem.appendChild(imageTitle);
				
				let form = document.createElement('form');
				form.action = '#';
				form.classList.add('align-center');
				
				let albumIdField = document.createElement('input');
				albumIdField.type = 'hidden';
				albumIdField.name = 'targetAlbum';
				albumIdField.value = this.albumId;
				form.appendChild(albumIdField);
				
				let imageIdField = document.createElement('input');
				imageIdField.type = 'hidden';
				imageIdField.name = 'targetImage';
				imageIdField.value = image.id;
				form.appendChild(imageIdField);
				
				let submit = document.createElement('input');
				submit.type = 'button';
				submit.value = 'Add';
				
				submit.addEventListener('click', e => {
					let self = this;
					sendAsync('POST', 'AddToAlbum', form, function (x) {
						if (x.readyState == XMLHttpRequest.DONE) {
							let message = x.responseText;
							
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
			this.gridContainer.style.display = 'none';
			this.grid.innerHTML = '';
			this.alertContainer.hide();
			
			this.titleContainer.innerHTML = '';
			
			this.album = null;
			this.images = null;
		};
		
		this.hide = function () {
			this.gridContainer.style.display = 'none';
		};
	};
	
	function ImageDetails() {
		this.modal = document.getElementById('selectedImageWrapper');
		this.modalContent = document.getElementById('selectedImage');
		this.commentList = new CommentList();
		this.albumSelect = new AlbumSelect();
		this.alertContainer = new AlertContainer('selImgAlert');
		
		this.imageContainer = document.getElementById('fullSizeImage');
		this.titleContainer = document.getElementById('selImgTitle');
		this.descContainer = document.getElementById('selImgDesc');
		this.dateContainer = document.getElementById('selImgDate');
		this.closeButton = document.getElementById('closeModal');
		
		this.currentAlbum = null;
		this.image = null;
		
		this.modal.style.display = 'none';
		
		this.modal.addEventListener('click', e => this.hide(), false);
		this.modalContent.addEventListener('click', e => e.stopPropagation(), false);
		this.closeButton.addEventListener('click', e => this.hide(), false);
		
		this.load = function (imageId, albumId) {			
			this.currentAlbum = albumId;
			let self = this;
			
			sendAsync('GET', 'FetchImage?imageId=' + imageId + "&albumId=" + albumId, null, function (x) {				
				if (x.readyState == XMLHttpRequest.DONE) {
					self.clear();
					self.modal.style.display = 'block';
					
					let message = x.responseText;
					
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
			this.albumSelect.load(this.image.id, this.currentAlbum);
			
			this.imageContainer.style.display = 'block';
			this.imageContainer.src = '/ImageGallery-RIA' + this.image.filePath;
			
			this.titleContainer.appendChild(document.createTextNode(this.image.title));
			this.descContainer.appendChild(document.createTextNode(this.image.description));
			this.dateContainer.appendChild(document.createTextNode('Uploaded ' + this.image.formattedDate));
		};
		
		this.clear = function () {
			this.modal.style.display = 'none';
			this.imageContainer.src = '';
			this.alertContainer.hide();
			
			this.titleContainer.innerHTML = '';
			this.descContainer.innerHTML = '';
			this.dateContainer.innerHTML = '';

			this.image = null;
			
			this.commentList.clear();
			this.albumSelect.clear();
		};
		
		this.hide = function () {
			this.modal.style.display = 'none';
		};
	};
	
	function CommentList() {
		this.commentList = document.getElementById('commentList');
		this.wrapper = document.getElementById('commentsWrapper');
		this.alertContainer = new AlertContainer('commentsAlert');
		this.form = document.getElementById('commentForm');
		this.formAlertContainer = new AlertContainer('commentFormError');
		
		this.imageId = null;
		this.comments = null;
		
		this.commentList.style.display = 'none';
		
		document.getElementById('publishComment').addEventListener('click', e => {
			let self = this;
			this.formAlertContainer.hide();
			
			let comment = self.form.elements['commentBody'].value;
			
			if (comment == null || comment.length == 0) {
				self.formAlertContainer.displayError('Empty comment body');
				console.log('Validity check failed - comment');
				return;
			}
				
			sendAsync('POST', 'PublishComment', this.form, function (x) {
				if (x.readyState == XMLHttpRequest.DONE) {
					let message = x.responseText;
					switch (x.status) {
						case 200:
							self.load(parseInt(message));
							break;
						case 400:
						case 401:
						case 500:
							self.formAlertContainer.displayError(x.responseText);
							break;
						default:
							self.formAlertContainer.displayError('Unexpected error');
					}
				}
			});
		});
		
		this.load = function (imageId) {
			this.imageId = imageId;
			let self = this;
			
			sendAsync('GET', 'FetchComments?imageId=' + imageId, null, function (x) {
				if (x.readyState == XMLHttpRequest.DONE) {
					self.clear();
					self.commentList.style.display = 'block';
					
					let message = x.responseText;
					
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
			
			if (this.comments.length == 0) this.alertContainer.display('No comments yet!');
			else {
				this.comments.forEach(comment => {
					let commentBox = document.createElement('div');
					commentBox.classList.add('comment');
					
					let body = document.createElement('p');
					body.classList.add('comment-body', 'text-90');
					body.appendChild(document.createTextNode(comment.body));
					commentBox.appendChild(body);
					
					let details = document.createElement('p');
					details.classList.add('comment-details', 'text-75');
					
					let by = document.createElement('span');
					by.classList.add('light-text');
					by.appendChild(document.createTextNode('by '));
					details.appendChild(by);
					
					let publisher = document.createElement('span');
					publisher.classList.add('bold-text');
					publisher.appendChild(document.createTextNode(comment.publisherUsername));
					details.appendChild(publisher);
					
					let date = document.createElement('span');
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
			this.formAlertContainer.hide();
			
			this.comments = null;
		};
	};
	
	function AlbumSelect() {
		this.form = document.getElementById('addToAlbumForm');
		this.select = document.getElementById('targetAlbum');
		this.submit = document.getElementById('addToAlbumSubmit');
		this.alertContainer = new AlertContainer('addToAlbumAlert');
		
		this.albumId = null;
		this.imageId = null;
		this.albums = null;
		
		this.submit.addEventListener('click', e => {
			if (this.select.value < 0) return;
			
			let self = this;
			
			sendAsync('POST', 'AddToAlbum', this.form, function (x) {
				if (x.readyState == XMLHttpRequest.DONE) {				
					if (x.status == 200)
						imageDetails.load(self.imageId, self.albumId);
					else
						self.alertContainer.displayError(x.responseText);
				}
			});
		}, false);
		
		this.load = function (imageId, albumId) {
			this.clear();
			this.albumId = albumId;
			this.imageId = imageId;
			let self = this;
			
			sendAsync('GET', 'FetchAlbumList?ownAlbums=true&albumId=' + albumId, null, function (x) {
				if (x.readyState == XMLHttpRequest.DONE) {
					let message = x.responseText;
					
					if (x.status == 200) {
						self.albums = JSON.parse(message);
						self.populate();
						self.form.style.display = 'block';
					} else {
						self.alertContainer.displayError(x.responseText);
					}
				}
			});
		};
		
		this.populate = function () {
			document.getElementById('addToAlbumTargetImg').value = this.imageId;
			
			let initialOption = document.createElement('option');
			initialOption.selected = true;
			initialOption.disabled = true;
			initialOption.value = -1;
			initialOption.appendChild(document.createTextNode('select an album...'));
			this.select.appendChild(initialOption);
			
			this.albums.forEach(album => {
				let option = document.createElement('option');
				option.value = album.id;
				option.appendChild(document.createTextNode(album.title));
				this.select.appendChild(option);
			});
		};
		
		this.clear = function () {
			this.form.style.display = 'none';
			this.select.innerHTML = '';
			this.alertContainer.hide();
			
			this.albums = null;
		};
	};
	
	function CreateAlbum() {
		this.createAlbumForm = document.getElementById('createAlbum');
		this.createAlbumAlert = new AlertContainer('createAlbumAlert');
		this.submit = document.getElementById('createAlbumSubmit');
		this.usernameField = document.getElementById('createAlbumUsername');
		
		this.createAlbumAlert.hide();
		
		this.submit.addEventListener('click', e => {
			this.usernameField.value = sessionStorage.getItem('username');
			
			let self = this;			
			sendAsync('POST', 'CreateAlbum', createAlbumForm, function (x) {
				if (x.readyState == XMLHttpRequest.DONE) {
					let message = x.responseText;
					
					if (x.status == 200) {
						createAlbumAlert.displaySuccess('Album created successfully!');
						setTimeout(() => createAlbumAlert.hide(), 2000);
						
						let albumId = parseInt(message);
						ownAlbums.load();
						albumImages.load(albumId);
						sessionStorage.setItem('currentAlbum', albumId);
					} else {
						self.createAlbumAlert.displayError(message);
					}
				}
			});
		});
	};
	
	function PageOrchestrator() {
		this.start = function() {
			// Initialize the logout button
			document.getElementById('logout').addEventListener('click', e => {
				sessionStorage.clear();
				sendAsync('GET', 'Logout', null, () => window.location.href = 'login.html');
			});
			
			// Initialize the main page components
			welcomeMessage = new WelcomeMessage(sessionStorage.getItem('username'), 'welcomeUsername');
			
			ownAlbums = new AlbumList('ownAlbumsContainer', 'ownAlbums', 'ownAlbumsAlert', true);
			
			otherAlbums = new AlbumList('otherAlbumsContainer', 'otherAlbums', 'otherAlbumsAlert', false);
			
			createAlbum = new CreateAlbum();
			
			albumImages = new AlbumImages();
			
			imagesToAdd = new ImagesToAdd();
			
			imageDetails = new ImageDetails();
		};
		
		this.refresh = function () {
			welcomeMessage.display();
			ownAlbums.load();
			otherAlbums.load();
			
			let albumId = sessionStorage.getItem('currentAlbum');
			if (albumId != null)
				albumImages.load(albumId);
		}
	}
};