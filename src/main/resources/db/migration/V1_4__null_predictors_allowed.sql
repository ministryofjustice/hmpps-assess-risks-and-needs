ALTER TABLE predictors ALTER COLUMN predictor_level DROP NOT NULL;
ALTER TABLE predictors ALTER COLUMN predictor_score TYPE varchar(20);