{
	// Page components
	let ownAlbums, otherAlbums, albumImages, imagesToAdd, imageDetails;
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
		
		this.hide = function() {
			this.container.style.visibility = 'hidden';
		};
	}
	
	function AlbumList(_tableContainer, _table, _alertContainer, _refreshUrl) {
		this.tableContainer = _tableContainer;
		this.table = _table;
		this.alertContainer = new AlertContainer(_alertContainer);
		this.refreshUrl = _refreshUrl;
		
		this.refresh() = function() {
			var self = this;
			self.alertContainer.hide();
			makeCall('GET', self.refreshUrl, null, function(x) {
				if (x.readyState == XMLHttpRequest.DONE) {
					var message = x.responseText;
					
					if (x.status == 200) {
						var albums = JSON.parse(message);
						if (albums.length == 0) {
							self.alertContainer.display('No albums yet!');
							return;
						}
						self.populate(albums);
					}
					else {}
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
				anchor.href = '#';
				anchor.appendChild(text);
				anchor.setAttribute('albumId', album.id);
				anchor.addEventListener('click', e => {
					albumImages.display(e.target.getAttribute('albumId'));
				}, false);
				details.appendChild(anchor);
				row.appendChild(details);
				
				this.table.appendChild(row);
			});
		};
	};
	
	function ImageGrid() {};
	
	function ImageDetails() {};
	
	function PageOrchestrator() {
		this.start = function() {
			// Display the welcome message
			var welcomeMessage = new WelcomeMessage(sessionStorage.getItem('username'), document.getElementById('welcomeUsername'));
			welcomeMessage.display();
			
			// Initialize the logout button
			document.getElementById('logout').addEventListener('click', e => {
				sessionStorage.removeItem('username');
			});
			
			// Initialize the main page components
			ownAlbums = new AlbumList(
					document.getElementById('ownAlbumsContainer'),
					document.getElementById('ownAlbums'),
					document.getElementById('ownAlbumsAlert'),
					'FetchOwnAlbums'
			);
			
			otherAlbums = new AlbumList(
					document.getElementById('otherAlbumsContainer'),
					document.getElementById('otherAlbums'),
					document.getElementById('otherAlbumsAlert'),
					'FetchOtherAlbums'
			);
			
			albumImages = new ImageGrid();
			
			imagesToAdd = new ImageGrid();
			
			imageDetails = new ImageDetails();
		};
	}
};