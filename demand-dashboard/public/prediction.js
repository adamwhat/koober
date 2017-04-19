$(function() {
  // todo: apply code to demand popup on map
//  $("#demand-form").submit(function(event) {
//    event.preventDefault();
//    var datetime = $("#date-select").val() + " " + $("#time-select").val();
//    var eventTime = new Date(datetime);
//    console.log(eventTime);
//
//    var postConfig = {
//      url: $("#demand-form").attr("action"),
//      data: JSON.stringify({
//        eventTime: eventTime
//      }),
//      contentType : 'application/json',
//      success: function(data) {
//        $("#demand-value").text(data.demand);
//      }
//    };
//
//    $.post(postConfig);
//  });

  // todo: use a default center based on data or get user's location
  var mapCenter = [-73.9440917, 40.7682802];
  $("#latitude-input").val(mapCenter[1]);
  $("#longitude-input").val(mapCenter[0]);

  var date = "2017-03-22"
  var time = "16:00"
  var lat = 40.7682802
  var lng = -73.9440917
  var temperature = 20
  var weather = 0
  var weatherArray = [1,0,0,0,0,0,0]

  $("#date-select").on("change", function(){
    console.log(typeof($("#date-select").val()))
    date = $("#date-select").val()
   });

  $("#time-select").on("change", function(){
    console.log($("#time-select").val())
    time = $("#time-select").val()
   });

  $("#temperature-input").on("input", function(data){
    temperature = parseFloat($("#temperature-input").val());
    console.log(temperature);
    console.log(typeof(temperature));
  });

  $("#weather-select").on("change", function(){
    weather = parseInt($("#weather-select").val())
    console.log(weather)
    console.log(typeof(weather))
    weatherArray = [0,0,0,0,0,0,0]
    weatherArray[weather] = 1
    console.log(weatherArray)
  });


  var predictionMap = new mapboxgl.Map({
    container: 'prediction-map',
    style: 'mapbox://styles/mapbox/streets-v9',
    zoom: 11,
    center: mapCenter
  });


  predictionMap.addControl(new mapboxgl.NavigationControl({position: 'top-left'}));


  predictionMap.on('click', function(data) {
    lat = data.lngLat.lat;
    lng = data.lngLat.lng;
    $("#latitude-input").val(lat);
    $("#longitude-input").val(lng);

    try {
      predictionMap.removeSource("demand");
      predictionMap.removeLayer("prediction");
    }
    catch (e) {
      // ignored
    }

//    predictionMap.addSource("demand", {
//      type: "geojson",
//      data: "/predict?eventTime=" + "2016-01-20T21:54:07.000-05:00" + "&lat=" + 40.7527999878 +
//      "&lng=" + -73.9436721802 + "&temperature=" + 28.34 + "&clear=" + 0 + "&fog=" + 0 + "&rain=" + 1 +
//      "&snow=" + 0 + "&hail=" + 0 + "&thunder=" + 0 + "&tornado=" + 0
//      //data: "/demand?lng=" + lng + "&lat=" + lat
//    });
    var latLngArray = makeCluster(lat, lng)
    var features = []
    for (i = 0; i < latLngArray[0].length; i ++){
        var query = buildQueryJson(latLngArray[0][i], latLngArray[1][i])
        $.ajax({
         url: "http://localhost:5000/queries.json",
         type: 'POST',
         dataType: 'json',
         contentType: 'application/json',
          data: query,
         success: function (d) {
            console.log(d)
            features.push({
                "properties": {
                    "Primary ID": 1,
                    "demand": d["demand"]
                  },
                  "geometry":{
                    "type": "Point",
                    "coordinates": [latLngArray[1][i], latLngArray[0][i]]
                  }
            });
        if (i == 0){
            new mapboxgl.Popup()
            .setLngLat(data.lngLat)
            .setHTML('<h2>Demand:' + d["demand"] + '</h2>')
            .addTo(predictionMap);
        }
        if (i == latLngArray[0].length-1) {
            predictionMap.addSource("demand", {
                type: "geojson",
                data: {"type": "Feature Collection",
                       "features": features
                    }
              });
            predictionMap.addLayer({
                  "id": "prediction",
                  "type": "circle",
                  "source": "demand",
                  "paint": {
                    "circle-color": {
                        property: 'demand',
                        type: 'exponential',
                        stops: [
                              [2.0, '#fee5d9'],
                              [4.0, '#fcae91'],
                              [6.0, '#fb6a4a'],
                              [8.0, '#de2d26'],
                              [10.0, '#a50f15']
                            ]
                    },
                    "circle-radius": {
                        'base': 1.75,
                        'stops': [[12, 3], [22, 180]]
                    },
                    'circle-opacity' : 0.8
                  }
              });
        }

       }, error: function(){
           alert("Cannot get data");
         }
        });

    }

    //todo: make call to predict

//    predictionMap.setFilter('prediction',
//        ['>=', 'properties.demand', '9.0'],
//        ['<', 'properties.demand', '10.0']
//    )

  });
  function makeCluster(lat, lng) {
    lats = [lat];
    lngs = [lng];
    lats.push(lat + 0.0016, lat + 0.0016, lat + 0.0016, lat, lat, lat - 0.0016, lat - 0.0016, lat - 0.0016);
    lats.push(lng - 0.0016, lng, lng + 0.0016, lng - 0.0016, lng + 0.0016, lng - 0.0016, lng, lng + 0.0016);
    return [lats, lngs];
  }

  function buildQueryJson(lat, lng){
    result = {}
    var datetime = date + " " + time;
    var eventTime = new Date(datetime);
    console.log(datetime);
    console.log(eventTime.toISOString());
    result = {
    "eventTime":eventTime.toISOString(),
    "lat":lat,
    "lng":lng,
    "temperature":temperature,
    "clear":weatherArray[0],
    "fog":weatherArray[1],
    "rain": weatherArray[2],
    "snow": weatherArray[3],
    "hail": weatherArray[4],
    "thunder":weatherArray[5],
    "tornado":weatherArray[6]
    }

    console.log(JSON.stringify(result))
    return JSON.stringify(result)
  }

});
