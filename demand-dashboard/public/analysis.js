$(function() {
  // todo: use a default center based on data
  var mapCenter = [-73.9440917, 40.7682802];
  $("#latitude-input").val(mapCenter[1]);
  $("#longitude-input").val(mapCenter[0]);

  var actualDemandMap = new mapboxgl.Map({
    container: 'actual-demand-map',
    style: 'mapbox://styles/mapbox/streets-v9',
    zoom: 11,
    center: mapCenter
  });
  actualDemandMap.addSource("actualDemand", {
          type: "geojson",
          data: geoJSONData
          });
  actualDemandMap.addLayer({
            "id": "actual",
            "type": "circle",
            "source": "demand",
            "paint": {
              "circle-color": {
                  property: 'actualDemand',
                  type: 'exponential',
                  stops: [
                        [10.0, '#fee5d9'],
                        [20.0, '#fcae91'],
                        [30.0, '#fb6a4a'],
                        [40.0, '#de2d26'],
                        [50.0, '#a50f15']
                      ]
              },
              "circle-radius": {
                  'base': 1.75,
                  'stops': [[12, 3], [22, 180]]
              },
              'circle-opacity' : 0.8
            }
    });

  var gradientBoostedTreesMap = new mapboxgl.Map({
    container: 'gradient-boosted-trees-map',
    style: 'mapbox://styles/mapbox/streets-v9',
    zoom: 11,
    center: mapCenter
  });

  var linearRegressionMap = new mapboxgl.Map({
      container: 'linear-regression-map',
      style: 'mapbox://styles/mapbox/streets-v9',
      zoom: 11,
      center: mapCenter
    });

  var multinomialLogicalRegressionMap = new mapboxgl.Map({
      container: 'multinomial-logical-regression-map',
      style: 'mapbox://styles/mapbox/streets-v9',
      zoom: 11,
      center: mapCenter
    });

  var neuralNetworkMap = new mapboxgl.Map({
      container: 'neural-network-map',
      style: 'mapbox://styles/mapbox/streets-v9',
      zoom: 11,
      center: mapCenter
    });

  var randomForestMap = new mapboxgl.Map({
      container: 'random-forest-map',
      style: 'mapbox://styles/mapbox/streets-v9',
      zoom: 11,
      center: mapCenter
    });

  document.getElementById('date-slider').addEventListener('input', function(e) {
    document.getElementById('date-slider-value').textContent = "6/" + e.target.value.toString() + "/2017"
  });

  document.getElementById('time-slider').addEventListener('input', function(e) {
    var input = parseInt(e.target.value);
    document.getElementById('time-slider-value').textContent = (prettyNumbers(Math.floor(input/2)) + ":" + prettyNumbers((input%2)*30) + "-" +
                                                              (prettyNumbers(Math.floor((input+1)/2))) + ":" + prettyNumbers(((input+1)%2)*30))
  });

  function prettyNumbers(number){
    var result = number.toString()
    if (result.length == 1){
        return "0" + result
    }
    return result
  }
});