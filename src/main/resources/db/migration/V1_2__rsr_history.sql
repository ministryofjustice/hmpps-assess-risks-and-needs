ALTER TABLE offender_predictors_history
ADD COLUMN completed_date timestamp;

ALTER TABLE offender_predictors_history
ADD COLUMN score_type varchar(100);
