insert into offender_predictors_history(offender_predictor_uuid, predictor_type, algorithm_version, calculated_at, crn, predictor_trigger_source, source_id, source_answers, created_by, created_date, completed_date, score_type)
values
('b1538c1f-d776-4b1c-9233-bb9b9caa3584', 'RSR', '3', '2019-11-14 09:06', 'X123456', 'ASSESSMENTS_API', 'source_id', null, 'created_by', '2019-11-14 09:05', '2019-11-14 09:07', 'STATIC'),
('8a773307-4bf6-4efb-9a07-29cfd800ce25', 'RSR', '3', '2021-10-05 09:06', 'X123456', 'OASYS', 'source_id', null, 'created_by', '2021-09-14 09:05', '2021-09-14 09:07', 'DYNAMIC');


insert into predictors(predictor_uuid, offender_predictor_uuid, predictor_subtype, predictor_score, predictor_level, created_date)
values
('0a6b6695-6997-49de-8b78-29af15936258', 'b1538c1f-d776-4b1c-9233-bb9b9caa3584', 'RSR', '20.22', 'LOW', '2019-11-14 09:05'),
('c793474d-e223-44d2-b107-7a28a9847ae3', 'b1538c1f-d776-4b1c-9233-bb9b9caa3584', 'OSPC', '10.1', 'MEDIUM', '2019-11-14 09:05'),
('a024e6dc-564b-41c0-bd09-647e6fa55917', 'b1538c1f-d776-4b1c-9233-bb9b9caa3584', 'OSPI', '30.3', 'HIGH', '2019-11-14 09:05'),
('49f9f1be-6b89-4091-808b-badb893eef54', '8a773307-4bf6-4efb-9a07-29cfd800ce25', 'RSR', '40.44', 'HIGH', '2019-11-14 09:05');