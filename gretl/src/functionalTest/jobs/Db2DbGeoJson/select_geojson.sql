SELECT 
    ST_AsGeoJSON(geom) AS geom 
FROM 
    db2dbgeojson.source_data;