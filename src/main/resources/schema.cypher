create constraint cons_agency_id if not exists on (a:Agency) assert a.id is unique;
create constraint cons_route_id if not exists on (r:Route) assert r.id is unique;
create constraint cons_trip_id if not exists on (t:Trip) assert t.id is unique;
create constraint cons_stop_id if not exists on (s:Stop) assert s.id is unique;
create index idx_trip_service if not exists for (t:Trip) on (t.serviceId);
create index idx_stoptime_seq if not exists for (st:StopTime) on (st.stopSequence);
create index idx_stop_name if not exists for (s:Stop) on (s.name);

