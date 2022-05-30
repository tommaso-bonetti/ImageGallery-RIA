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
		
		this.display = function(message) {
			this.container.textContent = message;
			this.container.style.visibility = 'visible';
		};
		
		this.displayError = function (message) {
			this.container.classList.add('error');
			this.display(message);
		};
		
		this.hide = function() {
			debugger;
			this.container.style.visibility = 'hidden';
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
			self.alertContainer.hide();
			sendAsync('GET', 'FetchAlbumList?ownAlbums=' + self.ownAlbums, null, function(x) {
				if (x.readyState == XMLHttpRequest.DONE) {
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
		
		this.images = null;
		this.page = null;
		
		this.titleContainer = document.getElementById('albumTitle');
		this.creatorContainer = document.getElementById('albumCreator');
		this.dateContainer = document.getElementById('albumDate');
		this.showPrevious = document.getElementById('showPrev');
		this.showNext = document.getElementById('showNext');
		
		this.gridContainer.style.visibility = 'hidden';
		
		this.showPrevious.addEventListener('click', e => {
			if (this.page > 0) {
				this.page--;
				if (this.page == 0)
					e.target.style.visibility = 'hidden';

				this.populate();
			}
		});
		
		this.showNext.addEventListener('click', e => {
			if (this.images.length > this.page * 5) {
				this.page++;
				if (this.images.length <= this.page * 5)
					e.target.style.visibility = 'hidden';

				this.populate();
			}
		});
		
		this.load = function (albumId) {
			var self = this;
			
			sendAsync('GET', 'FetchAlbum?albumId=' + albumId, null, function (x) {
				if (x.readyState == XMLHttpRequest.DONE) {
					var message = x.responseText;
					
					if (x.status == 200) {
						var album = JSON.parse(message);
						
						self.gridContainer.style.visibility = 'visible';
						
						self.titleContainer.textContent = album.title;
						self.creatorContainer.textContent = album.ownerUsename;
						self.dateContainer.textContent = album.formattedDate;
						
						if (album.images.length == 0) {
							self.alertContainer.display('No images yet!');
						} else {
							self.showPrevious.style.visibility = 'hidden';
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
				imageContainer.classList.add('image-container-div');
				
				var anchor = document.createElement('a');
				var img = document.createElement('img');
				img.src = image.filePath;
				img.classList.add('image');
				anchor.appendChild(img);
				anchor.href = '#';
				anchor.setAttribute('imageId', image.id);
				anchor.addEventListener('click', e => imageDetails.load(e.target.getAttribute('imageId')), false);
				
				imageContainer.appendChild(anchor);
				gridItem.appendChild(imageContainer);
				
				var imageTitle = document.createElement('p');
				imageTitle.classList.add('image-title');
				gridItem.appendChild(imageTitle);
				
				this.grid.appendChild(gridItem);
			});
		};
	};
	
	function ImageDetails() {};
	
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
			
			//imagesToAdd = new ImageGrid();
			
			//imageDetails = new ImageDetails();
		};
		
		this.refresh = function () {
			welcomeMessage.display();
			ownAlbums.load();
			otherAlbums.load();
		}
	}
};