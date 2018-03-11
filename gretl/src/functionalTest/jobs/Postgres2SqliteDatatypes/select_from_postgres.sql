SELECT 
    myint, 
    myfloat, 
    mytext, 
    mydate, 
    mytime, 
    myuuid, 
    ST_AsText(mygeom) AS mygeom_wkt 
FROM postgres2sqlite.source_data