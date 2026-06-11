var theMap;

var layerRoute;

var layerDistrict;
var layerCounty;

var latMyInitialPosition;
var lngMyInitialPosition;

var pinCenter;
var pinDistrict;
var pinCounty;

const defaultLatPosition = 38.7900248; 
const defaultLngPosition = -9.2021936;



const latSintra = 38.775331718344475;
const lngSintra = -9.341318607261242;

const latPorto = 41.14961;
const lngPorto = -8.61099;

const latCoimbra = 40.20331;
const lngCoimbra = -8.41025;

const latFaro = 37.01936;
const lngFaro = -7.93044;

const latBraga = 41.55032;
const lngBraga = -8.42005;

const latEvora = 38.57115;
const lngEvora = -7.90971;

const latAveiro = 40.6405;
const lngAveiro = -8.6538;

const latSetubal = 38.5244;
const lngSetubal = -8.8882;

const latLeiria = 39.74453;
const lngLeiria = -8.80705;

const latVianaCastelo = 41.69611;
const lngVianaCastelo = -8.8441;

const latBeja = 38.01477;
const lngBeja = -7.86387;

const latLisboa = 38.7169;
const lngLisboa = -9.1392;


const iconSize = 50;

const defaultZoom = 10;

const osrKey = "5b3ce3597851110001cf624839defe1736044a39ac1399052ee090cd";

const popupOptions = {
  maxWidth: 400
}

const defaultMapOptions = {
  center: [ defaultLatPosition, defaultLngPosition ],
  zoom: defaultZoom
};

function upDateCoords(lat, lng) {
  let latPlaceHolder = document.getElementById( 'inputLat' );
  latMyInitialPosition = lat;
  latPlaceHolder.value = latMyInitialPosition;

  let lngPlaceHolder = document.getElementById( 'inputLng' );
  lngMyInitialPosition = lng;
  lngPlaceHolder.value = lngMyInitialPosition; 
}

function initVars() {
  layerRoute = null;

  layerDistrict = null;
  layerCounty = null;

  pinCenter = null;
  pinDistrict = null;
  pinCounty = null;

  if ( !navigator.geolocation ) {
    alert( "Browser does not suport geolocation. Using default values." );

    latMyInitialPosition = 39.69484;
    lngMyInitialPosition = -8.13031;
  }
  else {
    latMyInitialPosition = lngMyInitialPosition = null;

    navigator.geolocation.getCurrentPosition( 
      function(pos) {
        const coords = pos.coords;

        latMyInitialPosition = coords.latitude;
        lngMyInitialPosition = coords.longitude;

        const markerOptionsCenter = {
          title: "A minha posição.",
          draggable: true
        };

        upDateCoords( latMyInitialPosition, lngMyInitialPosition );

        pinCenter = new L.Marker( [ latMyInitialPosition, lngMyInitialPosition ] , markerOptionsCenter );
        pinCenter.addTo( theMap );
        pinCenter.on( 
          'move',
          (event) => {
            upDateCoords( event.target.getLatLng().lat, event.target.getLatLng().lng );
          }
        );

        pinCenter.bindPopup( 
          "<div class='pinPlaceHolder'>" +
            "<span class='pinText'>Um vídeo</span>" + 
            "<video class='pinMedia' width='350' poster='/static/videos/movie1.png' controls >" +
            "  <source src='/static/videos/movie1.mp4' type='video/mp4' />" +
            "</video>" +
          "</div>", 
          popupOptions );
      
      } );
  }
}

export function initMapa() { 
  initVars();

  const markerOptions = (title) => ({
    title: title,
  });

  theMap = new L.map(document.getElementById('map'), defaultMapOptions);

  let layer = new L.TileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png');
  theMap.addLayer(layer);

  let markers = L.markerClusterGroup();

  const addPin = (lat, lng, title, address) => {
    let pin = new L.Marker([lat, lng], markerOptions(title));
    pin.bindPopup(
      `<div class='pinPlaceHolder'>
         <span class='pinText'>${address}</span>
         <h2>${title}</h2>
       </div>`, 
      popupOptions
    );
    markers.addLayer(pin);
  };

  // Adicionando os pinos
  addPin(latSintra, lngSintra, "Supermercado XPTO Sintra", "R. Alto do Forte IC 19, 2635-018 Rio de Mouro");
  addPin(latPorto, lngPorto, "Supermercado XPTO Porto", "Avenida dos Aliados, Porto");
  addPin(latCoimbra, lngCoimbra, "Supermercado XPTO Coimbra", "Praça da República, Coimbra");
  addPin(latFaro, lngFaro, "Supermercado XPTO Faro", "Rua de Santo António, Faro");
  addPin(latBraga, lngBraga, "Supermercado XPTO Braga", "Praça da República, Braga");
  addPin(latEvora, lngEvora, "Supermercado XPTO Évora", "Praça do Giraldo, Évora");
  addPin(latAveiro, lngAveiro, "Supermercado XPTO Aveiro", "Cais da Fonte Nova, Aveiro");
  addPin(latSetubal, lngSetubal, "Supermercado XPTO Setúbal", "Avenida Luísa Todi, Setúbal");
  addPin(latLeiria, lngLeiria, "Supermercado XPTO Leiria", "Praça Rodrigues Lobo, Leiria");
  addPin(latVianaCastelo, lngVianaCastelo, "Supermercado XPTO Viana do Castelo", "Praça da República, Viana do Castelo");
  addPin(latBeja, lngBeja, "Supermercado XPTO Beja", "Praça da República, Beja");
  addPin(latLisboa, lngLisboa, "Supermercado XPTO Lisboa", "Praça do Comércio, Lisboa");

  theMap.addLayer(markers);
}

export function calcRoute() {
  let xmlHttp = getXmlHttpObject();

  let params = new Object();
  
  params.coordinates = [];

  params.coordinates.push( [lngMyInitialPosition, latMyInitialPosition] );
  if ( pinCounty!=null ) {
    params.coordinates.push( [ pinCounty.getLatLng().lng, pinCounty.getLatLng().lat ] );  
  }
  if ( pinDistrict!=null ) {
    params.coordinates.push( [ pinDistrict.getLatLng().lng, pinDistrict.getLatLng().lat ] );  
  }


  let baseURL = "https://api.openrouteservice.org/v2/directions/";
  let profile = "driving-car";
  let url = baseURL + profile + "/geojson";
  
  // Using Post
  xmlHttp.open( "POST", url, true );
  xmlHttp.onreadystatechange = () => {
    if( xmlHttp.readyState === XMLHttpRequest.DONE ) {
      if ( layerRoute!=null ) {
        theMap.removeLayer( layerRoute );
      }
  
      theMap.addLayer( (layerRoute=L.geoJSON( JSON.parse( xmlHttp.responseText ) ) ) ); 
    }
  };
  xmlHttp.setRequestHeader( 'Content-Type', 'application/json; charset=utf-8' );
  xmlHttp.setRequestHeader( 'Accept', 'application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8' );
  xmlHttp.setRequestHeader( 'Authorization', osrKey );
  xmlHttp.send( JSON.stringify( params ) );
}

export function resetMap() {
  if ( layerDistrict!=null ) {
    theMap.removeLayer( layerDistrict );
  }

  if ( layerCounty!=null ) {
    theMap.removeLayer( layerCounty );
  }

  if ( pinDistrict!=null ) {
    theMap.removeLayer( pinDistrict );
  }

  if ( pinCounty!=null ) {
    theMap.removeLayer( pinCounty );
  }

  theMap.setZoom( defaultZoom );
  theMap.panTo( L.latLng( defaultLatPosition, defaultLngPosition ), { animate: true } );
}

export function updateDistrictOnMap(district) {
  let xml = getXmlHttpObject();

  let url = "https://json.geoapi.pt/distrito/" + district;

  // Using GET
  xml.open( "GET", url, true );
  xml.onreadystatechange = () => {
    if( xml.readyState === XMLHttpRequest.DONE ) {

      let data = JSON.parse( xml.responseText )[ "geojson" ];
      let centroidDistrict = data[ 'properties' ][ 'centros' ][ 'centroide' ];
      let boundingBox = data[ 'bbox' ];

      if ( layerDistrict!=null ) {
        theMap.removeLayer( layerDistrict );
      }

      if ( pinDistrict!=null ) {
        theMap.removeLayer( pinDistrict );
      }

      theMap.addLayer( (layerDistrict = L.geoJSON( data, {color: 'red'} )) );
      theMap.addLayer( (pinDistrict = new L.Marker( [centroidDistrict[1], centroidDistrict[0] ], { title: district } )) );

      theMap.flyToBounds( L.latLngBounds( L.latLng( boundingBox[1], boundingBox[0] ), L.latLng( boundingBox[3], boundingBox[2] ) ) );
    }
  };
  xml.send( null );
}

export function updateCountyOnMap(county) {
  let xml = getXmlHttpObject();

  let url = "https://json.geoapi.pt/municipio/" + county;

  // Using GET
  xml.open( "GET", url, true );
  xml.onreadystatechange = () => {
    if( xml.readyState === XMLHttpRequest.DONE ) {

      let data = JSON.parse( xml.responseText )[ "geojsons" ][ 'municipio' ];
      let centroidCounty = data[ 'properties' ][ 'centros' ][ 'centroide' ];
      let boundingBox = data[ 'bbox' ];

      if ( layerCounty!=null ) {
        theMap.removeLayer( layerCounty );
      }

      if ( pinCounty!=null ) {
        theMap.removeLayer( pinCounty );
      }

      theMap.addLayer( (layerCounty = L.geoJSON( data, {color: 'blue'} )) );
      theMap.addLayer( (pinCounty = new L.Marker( [centroidCounty[1], centroidCounty[0] ], { title: county } )) );

      theMap.flyToBounds( L.latLngBounds( L.latLng( boundingBox[1], boundingBox[0] ), L.latLng( boundingBox[3], boundingBox[2] ) ) );
    }
  };
  xml.send( null );
}