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

  var temperature = 20
  var weather = 0
  var weatherArray = [1,0,0,0,0,0,0]

  $("#temperature-input").on("input", function(data){
    temperature = parseInt($("#temperature-input").val());
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
    var lat = data.lngLat.lat;
    var lng = data.lngLat.lng;

    $("#latitude-input").val(lat);
    $("#longitude-input").val(lng);

    try {
      predictionMap.removeSource("demand");
      predictionMap.removeLayer("unclustered-points");
    }
    catch (e) {
      // ignored
    }

    predictionMap.addSource("demand", {
      type: "geojson",
      data: "/demand?lng=" + lng + "&lat=" + lat
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


    //todo: make call to predict
    new mapboxgl.Popup()
      .setLngLat(data.lngLat)
      .setHTML('<h2>Demand: 0</h2>')
      .addTo(predictionMap);

  });
});
