
:use system;

drop database gtfsuk if exists;
create database gtfsuk;

:use gtfsuk;

// run statements from schema.cypher


load csv with headers from
'file:///agency.txt' as row
create (a:Agency {id: row.agency_id, name: row.agency_name, url: row.agency_url, timezone: row.agency_timezone, lang: row.agency_lang});

// add the routes
load csv with headers from
'file:///routes.txt' as row
match (a:Agency {id: row.agency_id})
create (a)-[:OPERATES]->(r:Route {id: row.route_id, shortName: row.route_short_name,
                                  longName: row.route_long_name, type: toInteger(row.route_type)});

//add the stops
load csv with headers from
'file:///stops.txt' as row
create (s:Stop {id: row.stop_id, name: row.stop_name, location: point({latitude: toFloat(row.stop_lat), longitude: toFloat(row.stop_lon)}),
                platformCode: row.platform_code, parentStation: row.parent_station, locationType: row.location_type,
                timezone: row.stop_timezone, code: row.stop_code});

// add the trips
load csv with headers from
'file:///trips.txt' as row
match (r:Route {id: row.route_id})
create (r)<-[:USES]-(t:Trip {id: row.trip_id, serviceId: row.service_id,
                             headSign: row.trip_headsign, direction_id: toInteger(row.direction_id),
                             shortName: row.trip_short_name, blockId: row.block_id,
                             wheelchairAccessible:toInteger(row.wheelchair_accessible),
                             bikesAllowed: toInteger(row.bikes_allowed), shapeId: row.shape_id});

// setting service days
load csv with headers from
'file:///calendar.txt' as row
match (t:Trip) where t.serviceId = row.service_id
  set t.monday = row.monday starts with '1',
  t.tuesday = row.tuesday starts with '1',
  t.wednesday = row.wednesday starts with '1',
  t.thursday = row.thursday starts with '1',
  t.friday = row.friday starts with '1',
  t.saturday = row.saturday starts with '1',
  t.sunday = row.sunday starts with '1';
match (t:Trip) where t.monday = true set t:RUNS_1;
match (t:Trip) where t.tuesday = true set t:RUNS_2;
match (t:Trip) where t.wednesday = true set t:RUNS_3;
match (t:Trip) where t.thursday = true set t:RUNS_4;
match (t:Trip) where t.friday = true set t:RUNS_5;
match (t:Trip) where t.saturday = true set t:RUNS_6;
match (t:Trip) where t.sunday = true set t:RUNS_7;
match (t:Trip) set t.monday = null, t.tuesday = null, t.wednesday = null, t.wednesday = null, t.friday = null, t.saturday = null, t.sunday = null;

//add the StopTimes
:auto
using periodic commit
load csv with headers from
'file:///stop_times.txt' as row
match (t:Trip {id: row.trip_id}), (s:Stop {id: row.stop_id})
create (t)<-[:BELONGS_TO]-(st:StopTime {arrivalTime: row.arrival_time, departureTime: row.departure_time,
    arrivalOffset: duration({hours:toInteger(split(row.arrival_time, ':')[0]), minutes:toInteger(split(row.arrival_time, ':')[1]), seconds:toInteger(split(row.arrival_time, ':')[2])}),
    departureOffset: duration({hours:toInteger(split(row.departure_time, ':')[0]), minutes:toInteger(split(row.departure_time, ':')[1]), seconds:toInteger(split(row.departure_time, ':')[2])}),
    stopSequence: toInteger(row.stop_sequence)})-[:STOPS_AT]->(s);

// add transfer times
load csv with headers from
'file:///transfers.txt' as row
match (s:Stop {id:row.from_stop_id}) set s.minTransferTime = duration({seconds: toInteger(coalesce(row.min_transfer_time, "0"))});

//connect the StopTime sequences
call apoc.periodic.iterate('match (t:Trip) return t',
'match (t)<-[:BELONGS_TO]-(st) with st order by st.stopSequence asc
with collect(st) as stops
unwind range(0, size(stops)-2) as i
with stops[i] as curr, stops[i+1] as next
merge (curr)-[:NEXT_STOP]->(next)', {batchmode: "BATCH", parallel:true, parallel:true, batchSize:1});

// sample trip
match (t:Trip) where t.id = "339740" with t limit 1
match p=(t)<-[:BELONGS_TO]-(st)-[:STOPS_AT]->(s), p2=(st)-[:NEXT_STOP]->(st2)
return p, p2

match (start:Stop)<-[:STOPS_AT]-(st)-[:BELONGS_TO]->(t)
with st where start.name = 'Aberdeen' and t.saturday = true and st.departureOffset.seconds > duration({hours:7}).seconds
match (dest:Stop) where dest.name = 'Penzance' with dest, st
match p=(st)-[:NEXT_STOP*]->()-->(dest:Stop)
return p
