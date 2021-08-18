CREATE TABLE IF NOT EXISTS offender_predictors_history
(
    offender_predictor_id           serial          primary key,
    offender_predictor_uuid         UUID            not null unique,
    predictor_type                  varchar(100)    not null,
    algorithm_version               varchar(10)     not null,
    calculated_at                   timestamp       not null,
    crn                             varchar(20)     not null,
    predictor_trigger_source        varchar(200)    not null,
    source_id                       varchar(200)    not null,
    source_answers                  JSONB,
    created_by                      text            not null,
    created_date                    timestamp       not null,
    CONSTRAINT predictors_history_unique_source UNIQUE(predictor_trigger_source, source_id)
);

CREATE INDEX idx_crn ON offender_predictors_history (crn);
CREATE INDEX idx_uuid ON offender_predictors_history (offender_predictor_uuid);

CREATE TABLE IF NOT EXISTS predictors
(
    predictor_id                  serial  primary key,
    predictor_uuid                UUID    not null unique,
    offender_predictor_uuid       UUID        NOT NULL,
    predictor_subtype             varchar(100)    not null,
    predictor_score               varchar(10)     not null,
    predictor_level               varchar(10)     not null,
    created_date                  timestamp       not null,
    FOREIGN KEY (offender_predictor_uuid) REFERENCES offender_predictors_history (offender_predictor_uuid)
);
