SELECT 
    ST_AsBinary(geom) AS geom 
FROM 
    db2dbwkb.source_data;